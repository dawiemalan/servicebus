package dmcs.servicebus.connections;

import dmcs.common.listeners.ListenerManager;
import dmcs.common.listeners.StandardListenerManager;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @param <C> connection implementation class
 * @param <M> message implementation class
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractConnection<C extends Connection, M> implements Connection<C, M> {

    private final StandardListenerManager<ConnectionListener<C, M>> listeners = StandardListenerManager.standard();

    @Getter
    @Setter
    private String id = UUID.randomUUID().toString();
    @Getter
    protected ConnectionStatus status = ConnectionStatus.DISCONNECTED;
    @Getter
    @Setter
    protected Duration reconnectRetryInterval = Duration.ofSeconds(5);
    @Getter
    @Setter
    protected boolean autoReconnect = false;
    @Getter
    protected final MutableConvertibleValues<Object> attributes = MutableConvertibleValues.of(new LinkedHashMap<>());

    @Getter
    protected ZonedDateTime lastActivity = ZonedDateTime.now();

    /**
     * Close the connection after this time of no activity, disabled when null
     */
    @Getter
    @Setter
    private Duration maxIdleTime;

    @Override
    public ListenerManager getListenerManager() {
        return listeners;
    }

    protected void fireClosed() {
        listeners.forEach(listener -> listener.closed((C) this));
    }

    /**
     * Implementations should call this in {@link #sendMessage} function
     *
     * @param message
     */
    protected void fireMessageSent(M message) {
        listeners.forEach(listener -> listener.messageSent((C) this, message));
    }

    protected void fireMessageReceived(M message) {
        listeners.forEach(listener -> listener.messageReceived((C) this, message));
    }

    protected void fireErrorOccurred(RuntimeException e) {
        listeners.forEach(listener -> listener.errorOccurred((C) this, e));
    }

    public void setStatus(ConnectionStatus status) {

        // ignore if no change
        if (this.status == status)
            return;

        var oldStatus = this.status;
        this.status = status;
        listeners.forEach(listener -> listener.statusChanged((C) this, oldStatus, this.status));
    }

    @Override
    public void addListener(ConnectionListener<C, M> connectionListener) {
        listeners.addListener(connectionListener);
    }

    @Override
    public void addListener(ConnectionListener<C, M> connectionListener, Executor executor) {
        listeners.addListener(connectionListener, executor);
    }

    @Override
    public void removeListener(ConnectionListener<C, M> connectionListener) {
        listeners.removeListener(connectionListener);
    }

    /**
     * Returns the time since last activity
     */
    public Duration getIdleTime() {
        return Duration.ofMillis(ChronoUnit.MILLIS.between(lastActivity, ZonedDateTime.now()));
    }

    public void touch() {
        lastActivity = ZonedDateTime.now();
    }

    protected void reconnect() {

        if (!isAutoReconnect() || status == ConnectionStatus.CLOSED)
            return;

        status = ConnectionStatus.RECONNECTING;

        AbstractConnectionManager.TIMER.newTimeout(timeout -> {
            connect();
        }, reconnectRetryInterval.toMillis(), TimeUnit.MILLISECONDS);
    }
}
