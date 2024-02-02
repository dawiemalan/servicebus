package dmcs.micronaut.test

interface ServiceBusSpecificationConfig {

    default ServiceBusSpecification.DatabaseType getDatabaseType() {
        return ServiceBusSpecification.DatabaseType.MariaDB
    }

    default boolean enableArtemis() { return true }

    default boolean enableDatabase() { return true }

    default boolean enableLdap() { return false }

    default boolean enableRedis() { return false }

    default boolean enableZooKeeper() { return false }

    default boolean enableRabbitMq() { return false }
}
