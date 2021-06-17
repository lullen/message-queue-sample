package message.queuing.sample.pubsub.models;

import message.queuing.sample.proxy.Error;
import message.queuing.sample.proxy.StatusCode;

public class EventResponse implements Response {
    private Error error = new Error();

    public EventResponse(Error error) {
        this.error = error;
    }

    public EventResponse() {
    }

    public Error getError() {
        return error;
    }
    
    public boolean hasError() {
        return error.getStatusCode() != StatusCode.Ok;
    }
}
