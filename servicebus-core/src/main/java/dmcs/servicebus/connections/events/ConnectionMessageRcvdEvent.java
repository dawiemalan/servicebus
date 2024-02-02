package dmcs.servicebus.connections.events;

import dmcs.servicebus.connections.Connection;
import dmcs.servicebus.connections.ConnectionManager;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.ToString;

@Introspected
@ToString
public class ConnectionMessageRcvdEvent<C extends Connection<C, M>, M> extends ConnectionEvent<C, M> {

    @Getter
    private final M message;

    public ConnectionMessageRcvdEvent(ConnectionManager<C, M> manager, C connection, M message) {
        super(manager, connection);
        this.message = message;
    }
}
