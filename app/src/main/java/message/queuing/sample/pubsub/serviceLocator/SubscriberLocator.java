package message.queuing.sample.pubsub.serviceLocator;

import java.util.List;
import java.util.Optional;

public interface SubscriberLocator {
    List<LocatedSubscriber> scan();
    Object instanceFor(LocatedSubscriber subscriber);
    void add(LocatedSubscriber subscriber);
    void remove(String exchange, String routingKey);
    Optional<LocatedSubscriber> getSubscriber(String exchangeName, String routingKey);

}
