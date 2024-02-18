package io.dmcs.servicebus.connections.exceptions;

public class NotConnectedException extends ConnectionException {

    public NotConnectedException() {
        super();
    }

    public NotConnectedException(String message) {
        super(message);
    }

    public NotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotConnectedException(Throwable cause) {
        super(cause);
    }

}
