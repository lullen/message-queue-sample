package message.queuing.sample.pubsub;

import java.io.InvalidClassException;
import java.util.Arrays;

public final class ExceptionCheck {
    private static Class<?>[] exceptions = new Class<?>[] {
            InvalidClassException.class
    };

    public static boolean isPoisonous(Throwable e) {
        var stream = Arrays.stream(exceptions);
        System.out.println(e.getClass().getSimpleName());
        var poisonous = stream.anyMatch(pe -> pe.isInstance(e));
        return poisonous;
    }
}
