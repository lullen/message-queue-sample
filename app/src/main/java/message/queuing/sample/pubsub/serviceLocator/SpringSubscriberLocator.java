package message.queuing.sample.pubsub.serviceLocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import message.queuing.sample.pubsub.BudbeeService;
import message.queuing.sample.pubsub.Subscriber;
import message.queuing.sample.pubsub.Subscribers;
import message.queuing.sample.pubsub.models.Event;

@Component
public class SpringSubscriberLocator implements SubscriberLocator {
    private static final Logger log = LoggerFactory.getLogger(SpringSubscriberLocator.class);
    private List<LocatedSubscriber> subscriptions = new ArrayList<>();

    private ApplicationContext applicationContext;

    public SpringSubscriberLocator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Scans for BudbeeService and gets all the subscribers. Stores all the subscribers in a hashmap
     * where key = queue name
     */
    @Override
    public List<LocatedSubscriber> scan() {

        var serviceDictionary = applicationContext.getBeansWithAnnotation(BudbeeService.class);
        var services = serviceDictionary
                .values()
                .stream()
                .flatMap(b -> Stream.concat(
                        Arrays.stream(b.getClass().getInterfaces()),
                        Stream.of(b.getClass())))
                .collect(Collectors.toUnmodifiableList());

        services.forEach(s -> {
            for (var method : s.getMethods()) {
                var subscribers = new ArrayList<Subscriber>();

                var subscriberList = method.getAnnotation(Subscribers.class);
                if (subscriberList != null) {
                    subscribers.addAll(Arrays.asList(subscriberList.value()));
                } else {
                    var singleSubscriber = method.getAnnotation(Subscriber.class);
                    if (singleSubscriber != null) {
                        subscribers.add(singleSubscriber);
                    }
                }

                for (var subscriber : subscribers) {
                    var eventType = (Class<Event>) method.getParameterTypes()[0];
                    var sub = new LocatedSubscriber(
                            eventType,
                            camelToSnake(eventType.getSimpleName()),
                            getRoutingKey(subscriber.routingKey()),
                            method,
                            subscriber.autoAck(),
                            subscriber.retryPolicy());
                    log.info("Registering {} on {}", sub.getMethod().getName(),
                            sub.getExchangeName());
                    subscriptions.add(sub);
                }
            }
        });
        return subscriptions;
    }

    private String getRoutingKey(String key) {

        // Matches ${Key}
        String regex = "^(\\$\\{)(.*)(\\})";

        String replacement = "$2";
        if (key.matches(regex)) {
            // Removes ${} to get env key
            var envKey = key.replaceAll(regex, replacement);
            // Get value from application.properties
            return applicationContext.getEnvironment().getProperty(envKey);
        }

        return key;

    }

    public Optional<LocatedSubscriber> getSubscriber(String exchangeName, String routingKey) {
        // System.out.println(String.format("Getting subscriber from %s with key %s", exchangeName,
        // routingKey));
        var optional = subscriptions
                .stream()
                .filter(s -> s.getExchangeName().equals(exchangeName)
                        && s.getRoutingKey().equals(routingKey))
                .findFirst();
        return optional;
    }

    @Override
    public Object instanceFor(LocatedSubscriber subscriber) {
        var instance = applicationContext.getBean(subscriber.getMethod().getDeclaringClass());
        return instance;
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
    public void add(LocatedSubscriber subscriber) {
        subscriptions.add(subscriber);
    }

    @Override
    public void remove(String exchange, String routingKey) {
        subscriptions.removeIf(
                s -> s.getExchangeName().equals(exchange) && s.getRoutingKey().equals(routingKey));
    }

}

