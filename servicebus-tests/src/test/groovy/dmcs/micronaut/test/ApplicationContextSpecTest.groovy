package dmcs.micronaut.test

import dmcs.TestApplication

class ApplicationContextSpecTest extends ApplicationContextSpecification {

    @Override
    Class getMainClass() {
        return TestApplication
    }

    @Override
    void configure() {
        addConfigProperties(
                'something', 'orother'
        )
    }

    void 'test app start'() {
        when:
        def ctx = applicationContext

        then:
        ctx
        ctx.isRunning()
    }
}
