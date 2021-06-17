package message.queuing.sample.pubsub.models;

import java.lang.reflect.Method;
import java.util.function.Function;
import message.queuing.sample.pubsub.BudbeeService;
import message.queuing.sample.pubsub.RetryPolicy;
import message.queuing.sample.pubsub.Three_Times_Exponentional;

public class ManualSubscription {
    Class<? extends RetryPolicy> retryPolicy = Three_Times_Exponentional.class;
    int prefetch = 5;
    boolean autoAck = true;
    Class<? extends Event> eventType;
    String routeKey;
    private Method method;


    public ManualSubscription(Method method, String routeKey) {
        this.eventType = (Class<? extends Event>) method.getParameterTypes()[0];
        this.routeKey = routeKey;
        this.method = method;
    }

    public ManualSubscription(Class<? extends Event> eventType, String routeKey,
            Function<Class<BudbeeService>, Method> method) {
        this.eventType = eventType;
        this.routeKey = routeKey;
    }

    public ManualSubscription(Class<? extends Event> eventType, String routeKey,
            Class<? extends RetryPolicy> retryPolicy, int prefetch, boolean autoAck) {
        this.retryPolicy = retryPolicy;
        this.prefetch = prefetch;
        this.autoAck = autoAck;
        this.eventType = eventType;
        this.routeKey = routeKey;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public int getPrefetch() {
        return prefetch;
    }

    public Method getMethod() {
        return method;
    }

    public boolean getAutoAck() {
        return autoAck;
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?> eventType) {
        try {
            return clazz.getMethod(method, eventType);
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
