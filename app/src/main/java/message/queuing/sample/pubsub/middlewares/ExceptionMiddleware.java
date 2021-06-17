// package message.queuing.sample.pubsub.middlewares;

// import java.io.InvalidClassException;
// import java.lang.reflect.Method;
// import java.util.Arrays;
// import message.queuing.sample.proxy.Error;
// import message.queuing.sample.proxy.StatusCode;
// import message.queuing.sample.pubsub.models.EventResponse;
// import message.queuing.sample.pubsub.models.Request;
// import message.queuing.sample.pubsub.models.Response;

// public class ExceptionMiddleware implements Middleware {

//     private Middleware next;

//     private Class<?>[] poisonousExceptions = new Class<?>[] {
//             InvalidClassException.class
//     };

//     public ExceptionMiddleware(Middleware next) {
//         this.next = next;
//     }

//     @Override
//     public Response onInvoke(Method method, Request event) {
//         try {
//             return next.onInvoke(method, event);
//         } catch (Exception e) {
//             if (isPoisonous(e)) {
//                 return new EventResponse(new Error(StatusCode.NonTransientException, "Poisonous message"));
//             } else {
//                 throw e;
//             }
//         }

//     }

//     public boolean isPoisonous(Exception e) {
//         var stream = Arrays.stream(poisonousExceptions);
//         var poisonous = stream.anyMatch(pe -> pe.isInstance(e));
//         System.out.println(String.format("Poisonous: %s. Type: %s", poisonous, e.getClass()));
//         return poisonous;
//     }

// }
