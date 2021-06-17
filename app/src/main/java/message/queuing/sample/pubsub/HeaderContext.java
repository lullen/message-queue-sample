package message.queuing.sample.pubsub;

import java.util.Map;

public interface HeaderContext {
    Object get(String key);
    void put(String key, Object value);
    Map<String, Object> all();

    int currentAttempt();
    void addAttempt();
}
