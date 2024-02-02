package dmcs.servicebus.exceptions;

public class DuplicateEndpointException extends ServiceBusException {

    public DuplicateEndpointException(String address) {
        this(address, null);
    }

    public DuplicateEndpointException(String address, Throwable cause) {
        super(String.format("Duplicate endpoint address: [%s]", address), cause);
    }
}
