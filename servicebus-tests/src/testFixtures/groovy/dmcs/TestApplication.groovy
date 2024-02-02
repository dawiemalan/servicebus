package dmcs

import groovy.util.logging.Slf4j
import io.micronaut.runtime.Micronaut

@Slf4j
class TestApplication {

    static void main(String[] args) {

        def context = Micronaut.build(args)
                .packages("dmcs")
                .banner(false)
                .mainClass(TestApplication)
                .start()
    }
}
