package message.queuing.sample.pubsub;

public class Three_Times_Exponentional implements RetryPolicy {
    private final int delay = 2;
    private final int maxRetries = 3;

    @Override
    public int getRetryIn(int retry) {
        return (int) Math.pow(delay, retry);
    }

    @Override
    public boolean shouldRetry(int retry) {
        return retry < maxRetries;
    }
}
