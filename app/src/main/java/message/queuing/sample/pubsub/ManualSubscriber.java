package message.queuing.sample.pubsub;

import java.io.IOException;
import message.queuing.sample.pubsub.models.Event;
import message.queuing.sample.pubsub.models.ManualSubscription;

public interface ManualSubscriber {
    <T extends Event> void connect(ManualSubscription subscription, Class<T> clazz) throws IOException, Exception;

    void disconnect(Class<?> eventType, String routingKey) throws IOException;
}
