package message.queuing.sample.pubsub.serviceLocator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import message.queuing.sample.pubsub.RetryPolicy;
import message.queuing.sample.pubsub.models.Event;
import message.queuing.sample.pubsub.models.QueueType;

public class LocatedSubscriber {
    private Method method;
    private boolean autoAck;
    private QueueType queueType;
    private int prefetch;
    private RetryPolicy retryPolicy;
    private Class<? extends Event> eventType;
    private String exchangeName;
    private String routingKey;

    public LocatedSubscriber(Class<? extends Event> eventType, String exchangeName,
            String routingKey, Method method,
            boolean autoAck, Class<? extends RetryPolicy> retryPolicy) {

        this.eventType = eventType;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.method = method;
        this.autoAck = autoAck;
        try {
            this.retryPolicy = retryPolicy.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }

    public Method getMethod() {
        return method;
    }

    public boolean getAutoAck() {
        return autoAck;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public int getPrefetch() {
        return prefetch;
    }
}

