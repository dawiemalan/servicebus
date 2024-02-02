package dmcs.servicebus.connections.events;

import dmcs.servicebus.connections.Connection;
import dmcs.servicebus.connections.ConnectionManager;
import dmcs.servicebus.events.EsbEvent;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.ToString;

@Introspected
@ToString
public class ConnectionEvent<C extends Connection<C, M>, M> extends EsbEvent {

    @Getter
    private final C connection;
    @Getter
    private final ConnectionManager<C, M> manager;

    protected ConnectionEvent(ConnectionManager<C, M> manager, C connection) {
        this.manager = manager;
        this.connection = connection;
    }
}
