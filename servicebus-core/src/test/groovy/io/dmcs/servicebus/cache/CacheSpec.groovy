package io.dmcs.servicebus.cache

import dmcs.TestApplication
import dmcs.micronaut.test.ServiceBusSpecification
import groovy.util.logging.Slf4j
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Ignore

@MicronautTest(application = TestApplication)
@Slf4j
@Ignore
class CacheSpec extends ServiceBusSpecification {

//	@Inject
//	@Shared
//	TestCacheService testCacheService

    void test() {
        when:
        def value = testCacheService.getValue('a value', 'dummy')
        def value1 = testCacheService.getValue('a value')

        then:
        value == value1
        Thread.sleep(5000)

        when:
        testCacheService.putValue('a value')
        value1 = testCacheService.getValue('a value')

        then:
        value != value1

        when:
        testCacheService.evict('a value')
        value1 = testCacheService.getValue('a value')

        then:
        value != value1
    }

    @Override
    boolean enableArtemis() {
        return false
    }

    @Override
    boolean enableDatabase() {
        return false
    }

    @Override
    boolean enableZooKeeper() {
        return true
    }

    @Override
    boolean enableRabbitMq() {
        return true
    }
}
