package io.dmcs.servicebus.micronaut.cluster.impl.websocket.client;

import io.dmcs.servicebus.connections.AbstractConnection;
import io.dmcs.servicebus.connections.ConnectionStatus;
import io.dmcs.servicebus.connections.exceptions.ConnectionException;
import io.dmcs.servicebus.connections.exceptions.NotConnectedException;
import io.dmcs.servicebus.messaging.EsbMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnError;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

@Slf4j
public class WebSocketClientConnection extends AbstractConnection<WebSocketClientConnection, EsbMessage> {

    private URI uri;
    private WebSocketSession session;
    private WsClient client;
    private final ApplicationContext applicationContext;

    public WebSocketClientConnection(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        autoReconnect = true;
    }

    public void connect(@NonNull URI uri) {

        this.uri = uri;
        connect();
    }

    @Override
    public void connect() {

        var webSocket = applicationContext.getBean(WebSocketClient.class);

        log.debug("Opening ws connection to {}", uri);
        Mono.from(webSocket.connect(WsClient.class, uri))
                .doOnError(throwable -> {
                    log.error(String.format("%s %s, uri: ", throwable.getMessage(), uri));
                    setStatus(ConnectionStatus.DISCONNECTED);
                    reconnect();
                })
                .doOnSuccess(wsClient -> {
                    this.client = wsClient;
                    this.client.connection = this;
                    log.info("Connected to {}", uri);
                    setStatus(ConnectionStatus.CONNECTED);
                }).subscribe();
    }

    public void close(CloseReason reason) {
        session.close(reason);
    }

    protected void onOpen(WebSocketSession session) {
        this.session = session;
        setStatus(ConnectionStatus.CONNECTED);
    }

    protected void onClose(CloseReason reason) {
        log.info("Connection {} closed, reason: {}", uri, reason);
        if (reason == CloseReason.NORMAL) {
            setStatus(ConnectionStatus.CLOSED);
            return;
        }

        setStatus(ConnectionStatus.DISCONNECTED);
        reconnect();
    }

    protected void onError(WebSocketSession session, RuntimeException e) {
        log.error("{} error: {}", session.getRequestURI(), e.getMessage());
        fireErrorOccurred(e);
    }

    protected void onMessage(EsbMessage message) {
        fireMessageReceived(message);
    }

    @Override
    public void sendMessage(EsbMessage message) throws ConnectionException {

        if (client == null || getStatus() != ConnectionStatus.CONNECTED)
            throw new NotConnectedException();

        client.send(message);
    }

    @Override
    public void close() throws IOException {

        if (session != null)
            session.close(CloseReason.NORMAL);
        session = null;
        setStatus(ConnectionStatus.CLOSED);
    }

    @ClientWebSocket
    abstract static class WsClient implements Closeable {

        protected WebSocketClientConnection connection;

        @OnOpen
        public void onOpen(WebSocketSession session) {
            if (connection != null)
                connection.onOpen(session);
        }

        @OnClose
        public void onClose(CloseReason reason) {
            if (connection != null)
                connection.onClose(reason);
        }

        @OnError
        public void onError(WebSocketSession session, RuntimeException e) {
            if (connection != null)
                connection.onError(session, e);
        }

        @OnMessage
        public void onMessage(EsbMessage message) {
            if (connection != null)
                connection.onMessage(message);
        }

        public abstract void send(@NonNull EsbMessage message);

    }
}
