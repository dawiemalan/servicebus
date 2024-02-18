package io.dmcs.servicebus.connections;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.common.executors.ChainExecutor;
import io.dmcs.common.listeners.ListenerManager;
import io.dmcs.common.listeners.StandardListenerManager;
import io.dmcs.common.utils.CloseableUtils;
import io.dmcs.servicebus.connections.events.ConnectionClosedEvent;
import io.dmcs.servicebus.connections.events.ConnectionConnectedEvent;
import io.dmcs.servicebus.connections.events.ConnectionErrorEvent;
import io.dmcs.servicebus.connections.events.ConnectionMessageRcvdEvent;
import io.dmcs.servicebus.connections.events.ConnectionStatusEvent;
import io.dmcs.servicebus.connections.exceptions.ConnectionException;
import io.dmcs.servicebus.connections.exceptions.NotConnectedException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unused"})
@Slf4j
public abstract class AbstractConnectionManager<C extends Connection<C, M>, M>
        implements ConnectionManager<C, M>, ConnectionListener<C, M> {

    public static final Timer TIMER = new HashedWheelTimer(2, TimeUnit.SECONDS);

    private final StandardListenerManager<ConnectionListener<C, M>> listeners = StandardListenerManager.standard();

    private final Map<HandlerStage, ChainExecutor<C>> stageHandlers = new ConcurrentHashMap<>();
    protected final Map<String, C> connections = new ConcurrentHashMap<>();
    @Getter
    protected final ObjectMapper objectMapper;

    protected AbstractConnectionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ListenerManager getListenerManager() {
        return listeners;
    }

    public Stream<C> getConnections() {
        return connections.values().stream();
    }

    public Optional<C> getConnection(String id) {
        return Optional.of(connections.get(id));
    }

    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * Creates a new Connection, adds it to a connection state watchdog, and executes connect logic
     */
//	public C openConnection(C connection) {
//
//		C connection = createConnection(context);
//
//		//noinspection unchecked
//		connection.addListener(this);
//		connection.setContextId(context.getId());
//		context.setConnection(connection);
//		connections.put(context.getId(), connection);
//
//		//noinspection unchecked
//		connection.addListener(this);
//		connection.setStatus(ConnectionStatus.CONNECTING);
//		executeStageHandler(HandlerStage.BEFORE_CONNECT, context);
//
//		connection.connect();
//		connection.setStatus(ConnectionStatus.CONNECTED);
//		executeStageHandler(HandlerStage.AFTER_CONNECT, context);
//
//		return connection;
//	}
    public void closeConnection(C connection) {

        connection.setStatus(ConnectionStatus.CLOSING);
        executeStageHandler(HandlerStage.BEFORE_CLOSE, connection);

        // prevent recursive calls
        connection.removeListener(this);

        CloseableUtils.closeQuietly(connection);
        connection.setStatus(ConnectionStatus.CLOSED);
        executeStageHandler(HandlerStage.AFTER_CLOSE, connection);
    }

    protected void checkLiveness(C connection) {

        //connection.getContext().timeout.
        executeStageHandler(HandlerStage.AFTER_LIVENESS_CHECK, connection);
    }

    private void executeStageHandler(HandlerStage stage, C connection) {

        try {
            var handler = stageHandlers.get(stage);
            if (handler != null)
                handler.execute(connection);
        } catch (Exception e) {
            log.error("Error executing stage handler {}: {}", stage, e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    @SafeVarargs
    public final AbstractConnectionManager<C, M> withHandler(HandlerStage stage, Predicate<C>... predicates) {

        var handler = new ChainExecutor<>(predicates);

        if (stageHandlers.put(stage, handler) != null)
            log.warn("Replacing stage handler {}", handler);

        return this;
    }

    public AbstractConnectionManager<C, M> withHandler(HandlerStage stage, ChainExecutor<C> handler) {

        if (stageHandlers.put(stage, handler) != null)
            log.warn("Replacing stage handler {}", handler);

        return this;
    }

    @Override
    public void connected(C connection) {
        connections.put(connection.getId(), connection);
        connection.addListener(this);
        publishEvent(new ConnectionConnectedEvent<>(this, connection));
        listeners.forEach(l -> l.connected(connection));
        executeStageHandler(HandlerStage.AFTER_CONNECT, connection);
    }

    @Override
    public void closed(C connection) {
        publishEvent(new ConnectionClosedEvent<>(this, connection));
        listeners.forEach(l -> l.closed(connection));
        connections.remove(connection.getId());
        connection.removeListener(this);
        executeStageHandler(HandlerStage.AFTER_CLOSE, connection);
    }

    @Override
    public void messageReceived(C connection, M message) {
        executeStageHandler(HandlerStage.BEFORE_MESSAGE, connection);
        publishEvent(new ConnectionMessageRcvdEvent<>(this, connection, message));
        listeners.forEach(l -> l.messageReceived(connection, message));
        executeStageHandler(HandlerStage.AFTER_MESSAGE, connection);
    }

    @Override
    public void messageSent(C connection, M message) {
        listeners.forEach(l -> l.messageSent(connection, message));
    }

    @Override
    public void statusChanged(C connection, ConnectionStatus before, ConnectionStatus after) {

        publishEvent(new ConnectionStatusEvent<>(this, connection, before, after));

        listeners.forEach(l -> l.statusChanged(connection, before, after));
        if (after == ConnectionStatus.CLOSED)
            connections.remove(connection.getId());
    }

    @Override
    public void errorOccurred(C connection, RuntimeException e) {
        publishEvent(new ConnectionErrorEvent<>(this, connection, e));
        listeners.forEach(l -> l.errorOccurred(connection, e));
    }

    public void sendMessage(String connectionId, M message) throws ConnectionException {

        var connection = getConnection(connectionId).orElseThrow(NotConnectedException::new);
        if (connection.getStatus() != ConnectionStatus.CONNECTED)
            throw new NotConnectedException(String.format("Not connected, status: %s", connection.getStatus()));

        connection.sendMessage(message);
        listeners.forEach(l -> l.messageSent(connection, message));
        executeStageHandler(HandlerStage.AFTER_MESSAGE, connection);
    }

    public enum HandlerStage {
        BEFORE_CONNECT,
        AFTER_CONNECT,
        BEFORE_CLOSE,
        AFTER_CLOSE,
        ON_TIMEOUT,
        BEFORE_MESSAGE, // before a message is processed
        AFTER_MESSAGE, // after a message is processed
        AFTER_LIVENESS_CHECK
    }
}
