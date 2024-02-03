package io.dmcs.integrations.rabbitmq;

import io.dmcs.servicebus.address.EndpointAddress;
import io.dmcs.servicebus.messaging.EsbMessage;
import io.dmcs.servicebus.messaging.MessageIntegration;
import io.dmcs.servicebus.messaging.MessageRouter;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

//@Singleton
//@RabbitListener
@Slf4j
public class RabbitMessageIntegration extends MessageIntegration {

    @Inject
    RabbitEventProducer eventProducer;

    public RabbitMessageIntegration(MessageRouter messageRouter, EndpointAddress endpointAddress) {
        super(messageRouter, endpointAddress);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Queue(value = "events", numberOfConsumers = "6")
    @SneakyThrows
    public void onMessage(EsbMessage message, RabbitAcknowledgement acknowledgement) {

        try {
            messageRouter.onMessage(message);
            acknowledgement.ack();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            acknowledgement.nack();
        }
    }

    @Override
    public void send(EsbMessage message) {
        eventProducer.send(message);
    }
}
