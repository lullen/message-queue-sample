package message.queuing.sample.proxy;

public class Response<T> {
    private Error error;
    private T result;

    public Response(T result) {
        this.result = result;
    }

    public Response(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
