package dmcs.micronaut.test

interface ServiceBusSpecificationConfig {

    default boolean enableRedis() { return false }

    default boolean enableZooKeeper() { return false }

    default boolean enableRabbitMq() { return false }
}
