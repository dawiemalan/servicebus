package dmcs.servicebus.connections.events;

import dmcs.servicebus.connections.Connection;
import dmcs.servicebus.connections.ConnectionManager;
import io.micronaut.core.annotation.Introspected;
import lombok.ToString;

@Introspected
@ToString
public class ConnectionClosedEvent<C extends Connection<C, M>, M> extends ConnectionEvent<C, M> {

    public ConnectionClosedEvent(ConnectionManager<C, M> manager, C connection) {
        super(manager, connection);
    }
}
