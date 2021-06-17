package message.queuing.sample.pubsub;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Subscribers.class)
public @interface Subscriber {
    Class<? extends RetryPolicy> retryPolicy() default Three_Times_Exponentional.class;

    int prefetch() default 5;

    boolean autoAck() default false;

    String routingKey() default "";
}