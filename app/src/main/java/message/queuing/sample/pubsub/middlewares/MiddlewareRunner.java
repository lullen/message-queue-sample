// package message.queuing.sample.pubsub.middlewares;

// import java.lang.reflect.Method;
// import message.queuing.sample.pubsub.models.Event;
// import message.queuing.sample.pubsub.models.Response;

// public class MiddlewareRunner {
//     private ExceptionMiddleware firstMiddleware;

//     public MiddlewareRunner(
//             ExceptionMiddleware exceptionMiddleware,
//             EventRetryMiddleware eventRetryMiddleware,
//             CallingMiddleware callingMiddleware) {

//         exceptionMiddleware
//                 .setNext(eventRetryMiddleware)
//                 .setNext(callingMiddleware);
//         this.firstMiddleware = exceptionMiddleware;
//     }


//     public Response run(Method method, Event event) {
//         return firstMiddleware.onInvoke(method, event);
//     }
// }
