package message.queuing.sample.interfaces;

import java.io.IOException;
import java.io.InvalidClassException;
import message.queuing.sample.pubsub.BudbeeService;
import message.queuing.sample.pubsub.Subscriber;
import message.queuing.sample.pubsub.Ten_Times_Linear;
import message.queuing.sample.pubsub.models.EventResponse;

public interface Work {
    // RetryPolicy should be enum
    EventResponse doWork(TestEvent event);

    EventResponse noRoute(TestEvent event) throws InvalidClassException, IOException;
}
