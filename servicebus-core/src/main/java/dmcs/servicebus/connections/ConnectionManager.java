package dmcs.servicebus.connections;

import dmcs.common.listeners.Listenable;
import dmcs.common.utils.chain.ChainExecutor;
import dmcs.servicebus.connections.events.ConnectionEvent;
import dmcs.servicebus.connections.exceptions.ConnectionException;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @param <C> connection type
 * @param <M> message type
 */
public interface ConnectionManager<C extends Connection<C, M>, M> extends Listenable<ConnectionListener<C, M>> {

    Stream<C> getConnections();

    Optional<C> getConnection(String id);

    int getConnectionCount();

    void closeConnection(C connection);

    AbstractConnectionManager<C, M> withHandler(AbstractConnectionManager.HandlerStage stage, Predicate<C>... predicates);

    AbstractConnectionManager<C, M> withHandler(AbstractConnectionManager.HandlerStage stage, ChainExecutor<C> handler);

    void sendMessage(String connectionId, M message) throws ConnectionException;

    void publishEvent(ConnectionEvent<C, M> event);
}
