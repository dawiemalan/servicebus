package dmcs.integrations.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitConnectionManager {

    private ConnectionFactory connectionFactory;

    public RabbitConnectionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
