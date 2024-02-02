package dmcs.servicebus.connections;

public enum ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    RECONNECTING,
    CONNECTED,
    CLOSING,
    CLOSED
}
