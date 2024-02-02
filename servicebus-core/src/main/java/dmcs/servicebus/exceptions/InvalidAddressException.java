package dmcs.servicebus.exceptions;

public class InvalidAddressException extends ServiceBusException {

    public InvalidAddressException(String address) {
        this(address, null);
    }

    public InvalidAddressException(String address, Throwable cause) {
        super(String.format("Invalid address: [%s]", address), cause);
    }
}
