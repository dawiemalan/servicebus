package dmcs.servicebus.exceptions;

public class ServiceBusException extends Exception {

    public ServiceBusException(String message) {
        super(message);
    }

    public ServiceBusException(String message, Throwable cause) {
        super(message, cause);
    }
}
