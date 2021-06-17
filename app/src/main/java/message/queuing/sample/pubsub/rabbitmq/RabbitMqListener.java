package message.queuing.sample.pubsub.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import message.queuing.sample.proxy.Error;
import message.queuing.sample.proxy.StatusCode;
import message.queuing.sample.pubsub.ExceptionCheck;
import message.queuing.sample.pubsub.HeaderContext;
import message.queuing.sample.pubsub.ManualSubscriber;
import message.queuing.sample.pubsub.Three_Times_Exponentional;
import message.queuing.sample.pubsub.models.Event;
import message.queuing.sample.pubsub.models.EventAnnotation;
import message.queuing.sample.pubsub.models.EventResponse;
import message.queuing.sample.pubsub.models.ManualSubscription;
import message.queuing.sample.pubsub.serviceLocator.LocatedSubscriber;
import message.queuing.sample.pubsub.serviceLocator.SubscriberLocator;

@Component
public class RabbitMqListener implements ManualSubscriber {
    private SubscriberLocator subscriberLocator;
    private Gson json;
    private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    private Connection connection;
    private ApplicationContext context;
    private RabbitMq rabbit;



    public RabbitMqListener(RabbitMq rabbit, SubscriberLocator subscriberLocator,
            ApplicationContext context) {
        this.rabbit = rabbit;
        this.subscriberLocator = subscriberLocator;
        this.context = context;
        this.json = new GsonBuilder().create();
    }

    public void init() throws IOException, TimeoutException, Exception {
        connection = rabbit.getConnection();

        var subscribers = subscriberLocator.scan();
        System.out.println("Found " + subscribers.size());
        for (var subscriber : subscribers) {
            var queueName = getQueueName(subscriber.getEventType(), subscriber.getRoutingKey());
            channels.put(queueName,
                    createConsumer(subscriber.getEventType(),
                            subscriber.getRoutingKey(),
                            subscriber.getPrefetch(),
                            subscriber.getAutoAck()));
            System.out.println("Subscriber " + queueName);
        }
    }


    private Channel createConsumer(Class<?> eventType, String routingKey, int prefetch,
            boolean autoAck)
            throws IOException, Exception {
        var channel = connection.createChannel();
        if (channel == null) {
            throw new Exception("No channels available");
        }
        var annotation = eventType.getAnnotation(EventAnnotation.class);
        var exchangeName = camelToSnake(eventType.getSimpleName());

        String type;
        switch (annotation.queueType()) {
            case Fanout:
                type = "fanout";
                break;
            case Direct:
                type = "direct";
                break;
            case Topic:
                type = "topic";
                break;

            default:
                type = "fanout";
                break;
        }

        channel.exchangeDeclare(exchangeName, type);

        System.out.println("routing key: " + routingKey);
        var qName = getQueueName(eventType, routingKey);
        // boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        var queueName = channel.queueDeclare(qName, true, false, false, null).getQueue();
        System.out.println("listening on queue: " + queueName);
        channel.queueBind(queueName, exchangeName, routingKey);

        DeliverCallback deliverCallback = onDeliver(channel);
        channel.basicQos(prefetch);

        if (autoAck) {
            System.out.println("AUTO ACKKKK!!!!!!!!!!!!!!!");
        }

        channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
            // Cancel callback?
            System.out.println("cancel callback");
        });
        return channel;
    }

    private DeliverCallback onDeliver(Channel channel) {
        return (consumerTag, delivery) -> {
            var headerContext = context.getBean(RabbitContext.class);
            populateHeaders(headerContext, delivery);
            // System.out.println("Getting routing key " + delivery.getEnvelope().getRoutingKey());
            var message = new String(delivery.getBody(), "UTF-8");
            var subOptional = subscriberLocator.getSubscriber(delivery.getEnvelope().getExchange(),
                    delivery.getEnvelope().getRoutingKey());
            if (!subOptional.isPresent()) {
                System.out.println(
                        String.format("No subscriber found for exchange: %s and routing key %s",
                                delivery.getEnvelope().getExchange(),
                                delivery.getEnvelope().getRoutingKey()));
                return;
            }
            var sub = subOptional.get();
            var instance = subscriberLocator.instanceFor(sub);

            var request = (Event) json.fromJson(message, sub.getMethod().getParameterTypes()[0]);

            Object res;
            try {
                res = sub.getMethod().invoke(instance, request);
            } catch (Exception e) {
                var statusCode =
                        ExceptionCheck.isPoisonous(e.getCause()) ? StatusCode.NonTransientException
                                : StatusCode.Exception;
                res = new EventResponse(new Error(statusCode, e.toString()));
            }

            var asd = getAsd(headerContext, sub, res);
            switch (asd) {
                case Ack:
                    ackMessage(channel, delivery, sub);
                    break;
                case Nack:
                    nackMessage(channel, delivery, sub);
                    break;
                case Requeue:
                    ackMessage(channel, delivery, sub);
                    requeueMessage(channel, delivery.getEnvelope().getExchange(),
                            delivery.getEnvelope().getRoutingKey(),
                            delivery.getBody(), headerContext);
                    break;
                default:
                    break;
            }

        };
    }

    private Asd getAsd(HeaderContext headerContext, LocatedSubscriber sub, Object response)
            throws IOException {

        if (shouldNack(headerContext, sub, response)) {
            return Asd.Nack;
        } else if (shouldRequeue(headerContext, sub, response)) {
            return Asd.Requeue;
        } else if (shouldAck(headerContext, sub, response)) {
            return Asd.Ack;
        }
        return Asd.None;
        // if (eventRes.getError().getStatusCode() == StatusCode.NonTransientException) {
        // System.out.println("OUCH!");
        // return Asd.Nack;
        // } else if (eventRes.getError().hasError()) {
        // var attempt = headerContext.currentAttempt();
        // if (sub.getRetryPolicy().shouldRetry(attempt)) {
        // System.out.println("Requeuing!");
        // return Asd.Requeue;
        // } else {
        // System.out.println("Screw this!");
        // return Asd.Nack;
        // }
        // } else if (sub.getAutoAck()) {
        // return Asd.None;
        // }
        // } else {
        // return Asd.Ack;
        // }
        // return Asd.Ack;
    }

    private boolean shouldRequeue(HeaderContext headerContext, LocatedSubscriber sub,
            Object response) {
        var attempt = headerContext.currentAttempt();
        if (response instanceof EventResponse) {
            var eventRes = (EventResponse) response;
            if (eventRes.hasError() && sub.getRetryPolicy().shouldRetry(attempt)) {
                System.out.println("Requeuing!");
                return true;
            }
        }
        return false;
    }

    private boolean shouldNack(HeaderContext headerContext, LocatedSubscriber sub,
            Object response) {
        if (response instanceof EventResponse) {
            var eventRes = (EventResponse) response;
            if (eventRes.getError().getStatusCode() == StatusCode.NonTransientException) {
                System.out.println("OUCH!");
                return true;
            }
            var attempt = headerContext.currentAttempt();
            if (!sub.getRetryPolicy().shouldRetry(attempt)) {
                System.out.println("Too many retries, dropping!");
                return true;
            }
        }
        return sub.getAutoAck();
    }

    private boolean shouldAck(HeaderContext headerContext, LocatedSubscriber sub, Object response) {
        if (response instanceof EventResponse) {
            var eventRes = (EventResponse) response;
            return !eventRes.hasError();
        } else {
            return true;
        }
    }

    private void nackMessage(Channel channel, Delivery delivery, LocatedSubscriber sub)
            throws IOException {
        if (!sub.getAutoAck()) {
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        }
    }

    private void ackMessage(Channel channel, Delivery delivery, LocatedSubscriber sub)
            throws IOException {
        if (!sub.getAutoAck()) {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }

    private void requeueMessage(Channel channel, String exchange, String routingKey, byte[] message,
            HeaderContext headerContext)
            throws IOException {
        headerContext.addAttempt();
        var headers = headerContext.all();
        final var properties = new AMQP.BasicProperties().builder();
        properties.headers(headers);

        channel.basicPublish(
                exchange,
                routingKey,
                properties.build(),
                message);
    }

    private void populateHeaders(HeaderContext headerContext, Delivery delivery) {
        var headers = delivery.getProperties().getHeaders();
        if (headers != null) {
            headers
                    .entrySet()
                    .stream()
                    .forEach(h -> headerContext.put(h.getKey(), h.getValue()));
        }

        var attempt = headerContext.currentAttempt();
        System.out.println("Doing attempt #" + attempt);
    }

    public void disconnect() throws IOException {
        connection.close();
    }

    private String getExchangeName(Class<?> eventType) {
        var eventName = eventType.getSimpleName();
        return camelToSnake(eventName);
    }

    private String getQueueName(Class<?> eventType, String routingKey) {
        var appId = context.getId();

        var eventName = appId + eventType.getSimpleName();
        if (routingKey != null && !routingKey.isEmpty()) {
            eventName += routingKey.substring(0, 1).toUpperCase() + routingKey.substring(1);
        }
        return camelToSnake(eventName);
    }

    private static String camelToSnake(String str) {
        // Regular Expression
        String regex = "([a-z])([A-Z]+)";

        // Replacement string
        String replacement = "$1_$2";

        // Replace the given regex with replacement string and convert it to upper case.
        str = str.replaceAll(regex, replacement).toUpperCase();
        return str;
    }

    @Override
    public <T extends Event> void connect(ManualSubscription subscription, Class<T> clazz)
            throws IOException, Exception {
        var subscriber = new LocatedSubscriber(
                subscription.getEventType(),
                getExchangeName(subscription.getEventType()),
                subscription.getRouteKey(),
                subscription.getMethod(),
                false,
                Three_Times_Exponentional.class);


        subscriberLocator.add(subscriber);
        var channel = createConsumer(
                subscription.getEventType(),
                subscription.getRouteKey(),
                subscription.getPrefetch(),
                subscriber.getAutoAck());
        channels.put(getQueueName(subscription.getEventType(), subscription.getRouteKey()),
                channel);

    }

    @Override
    public void disconnect(Class<?> eventType, String routingKey) throws IOException {
        var key = getQueueName(eventType, routingKey);
        subscriberLocator.remove(getExchangeName(eventType), routingKey);
        var exists = channels.containsKey(key);
        if (!exists) {
            return;
        }
        var channel = channels.get(key);
        try {
            channel.close();
            channels.remove(key);
        } catch (IOException | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
