package io.dmcs.micronaut.test

import dmcs.micronaut.test.ServiceBusSpecification
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Title

@Title('Specification with defaults')
@MicronautTest
class SpecificationTest extends ServiceBusSpecification {

    void 'test app start'() {
        when:
        def ctx = applicationContext

        then:
        ctx
        ctx.isRunning()
    }
}
