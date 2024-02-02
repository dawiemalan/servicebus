package dmcs.servicebus.connections.events;

import dmcs.servicebus.connections.Connection;
import dmcs.servicebus.connections.ConnectionManager;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.ToString;

@Introspected
@ToString
public class ConnectionErrorEvent<C extends Connection<C, M>, M> extends ConnectionEvent<C, M> {

    @Getter
    private final RuntimeException exception;

    public ConnectionErrorEvent(ConnectionManager<C, M> manager, C connection, RuntimeException e) {
        super(manager, connection);
        this.exception = e;
    }
}
