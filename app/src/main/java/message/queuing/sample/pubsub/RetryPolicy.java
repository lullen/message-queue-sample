package message.queuing.sample.pubsub;

public interface RetryPolicy {
    int getRetryIn(int retry);

    boolean shouldRetry(int attempt);
}
