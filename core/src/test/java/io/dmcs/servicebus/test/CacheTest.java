package io.dmcs.servicebus.test;

import io.dmcs.test.TestApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.Ignore;

@MicronautTest(application = TestApplication.class)
@Ignore
class CacheTest {

	@Inject
//	@Shared
//	TestCacheService testCacheService;

    void test() {
//        when:
//        def value = testCacheService.getValue('a value', 'dummy')
//        def value1 = testCacheService.getValue('a value')
//
//        then:
//        value == value1
//        Thread.sleep(5000)
//
//        when:
//        testCacheService.putValue('a value')
//        value1 = testCacheService.getValue('a value')
//
//        then:
//        value != value1
//
//        when:
//        testCacheService.evict('a value')
//        value1 = testCacheService.getValue('a value')
//
//        then:
//        value != value1
    }
}
