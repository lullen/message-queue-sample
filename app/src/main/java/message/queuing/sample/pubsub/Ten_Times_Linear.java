package message.queuing.sample.pubsub;

public class Ten_Times_Linear implements RetryPolicy {
    private final int delay = 5;
    private final int maxRetries = 10;

    @Override
    public int getRetryIn(int retry) {
        return delay;
    }

    @Override
    public boolean shouldRetry(int retry) {
        return retry < maxRetries;
    }

}
