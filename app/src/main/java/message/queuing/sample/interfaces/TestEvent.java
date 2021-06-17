package message.queuing.sample.interfaces;

import message.queuing.sample.pubsub.models.Event;
import message.queuing.sample.pubsub.models.EventAnnotation;
import message.queuing.sample.pubsub.models.QueueType;

@EventAnnotation(queueType = QueueType.Direct)
public class TestEvent implements Event {
    private String name;
    private String text;

    public TestEvent() {

    }

    public TestEvent(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }
}
