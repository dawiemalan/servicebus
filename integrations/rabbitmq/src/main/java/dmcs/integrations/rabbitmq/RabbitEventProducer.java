package dmcs.integrations.rabbitmq;

import dmcs.servicebus.messaging.EsbMessage;

//@RabbitClient("servicebus.topics")
//@MessageHeader(name = "x-sb-region-id", value = "${servicebus.region:unknown}")
//@MessageHeader(name = "x-sb-env", value = "${servicebus.env:unknown}")
public interface RabbitEventProducer {

    void send(EsbMessage message);
}
