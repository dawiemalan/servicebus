package dmcs.integrations.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import io.micronaut.rabbitmq.connect.ChannelInitializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@Singleton
public class RabbitChannelInitializer extends ChannelInitializer {

    private static final String SERVICEBUS_TOPIC_EXCHANGE = "servicebus.topics";
    private static final String SERVICEBUS_QUEUE_EXCHANGE = "servicebus.queues";

    @Override
    public void initialize(Channel channel, String name) throws IOException {

        // create topic exchange
        channel.exchangeDeclare(SERVICEBUS_TOPIC_EXCHANGE, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(SERVICEBUS_QUEUE_EXCHANGE, BuiltinExchangeType.DIRECT);

        Map<String, Object> args = new HashMap<>();
        args.put("x-max-priority", 100);
        args.put("x-message-ttl", 60000);
        // expire the queue if no consumers for 30 mins
        args.put("x-expires", 1800000);

        channel.queueDeclare("events", true, false, false, args);
        channel.queueBind("events", SERVICEBUS_TOPIC_EXCHANGE, "", args);
    }
}
