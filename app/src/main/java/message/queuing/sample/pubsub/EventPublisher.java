package message.queuing.sample.pubsub;

import java.io.IOException;
import message.queuing.sample.proxy.Empty;
import message.queuing.sample.proxy.Response;
import message.queuing.sample.pubsub.models.Event;

public interface EventPublisher {
    Response<Empty> publish(Event event) throws IOException;
    Response<Empty> publish(Event event, String routingKey) throws IOException;
}
