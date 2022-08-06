package chariot.internal;

public class Base implements RequestHandler {

    protected final InternalClient client;

    protected Base(InternalClient client) {
        this.client = client;
    }

    public RequestResult request(RequestParameters parameters) {
        return client.request(parameters);
    }

}
