// package message.queuing.sample.pubsub.middlewares;

// import java.lang.reflect.Method;
// import message.queuing.sample.proxy.StatusCode;
// import message.queuing.sample.pubsub.models.EventResponse;
// import message.queuing.sample.pubsub.models.Request;
// import message.queuing.sample.pubsub.models.Response;

// public class EventRetryMiddleware implements Middleware {
//     private Middleware next;

//     public EventRetryMiddleware(Middleware next) {
//         this.next = next;
//     }

//     @Override
//     public Response onInvoke(Method method, Request request) {
        

//     }

//     @Override
//     public void preInvoke(Method method, Request request) {
//         // TODO Auto-generated method stub
        
//     }

//     @Override
//     public Response postInvoke(Method method, Response request) {
//         if (next == null) {
//             return new EventResponse();
//         }


//         var res = next.onInvoke(method, request);
//         if (res instanceof EventResponse) {
//             var eventRes = (EventResponse)res;
//             if (eventRes.getError().getStatusCode() == StatusCode.Exception) {
//                 // drop
//             } else if (eventRes.getError().hasError()) {
//                 // retry if not too many retries
//             } else {
//                 // ack
//             }
//         }

//         return res;
        
//     }

//     @Override
//     public Middleware setNext(Middleware next) {
//         // TODO Auto-generated method stub
//         return null;
//     }
// }
