package message.queuing.sample.pubsub.models;

public enum QueueType {
    /**
     * Send to all queues
     */
    Fanout,
    /** Send to queues by routing key */
    Direct,
    /** Send to queues by pattern */
    Topic
}
