package message.queuing.sample.pubsub.rabbitmq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import message.queuing.sample.pubsub.HeaderContext;

@Component
public class RabbitContext implements HeaderContext {
    private static final String ATTEMPT_COUNT_HEADER = "xb-attempt-count";
    private static ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();

    @Override
    public Object get(String key) {
        return headers.get(key);
    }

    @Override
    public void put(String key, Object value) {
        headers.put(key, value);
    }

    @Override
    public int currentAttempt() {
        var attempt = headers.get(ATTEMPT_COUNT_HEADER);
        if (attempt == null) {
            return 0;
        } else {
            return (int) attempt;
        }
    }

    @Override
    public void addAttempt() {
        var attempt = headers.get(ATTEMPT_COUNT_HEADER);
        if (attempt != null) {
            var updatedAttempt = (int) attempt + 1;
            headers.put(ATTEMPT_COUNT_HEADER, updatedAttempt);
        } else {
            headers.put(ATTEMPT_COUNT_HEADER, 1);
        }
    }

    public String attemptHeader() {
        return ATTEMPT_COUNT_HEADER;
    }

    @Override
    public Map<String, Object> all() {
        // TODO Auto-generated method stub
        return null;
    }
}
