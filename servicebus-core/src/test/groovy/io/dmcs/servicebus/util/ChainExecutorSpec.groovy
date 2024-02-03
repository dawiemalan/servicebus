package io.dmcs.servicebus.util

import io.dmcs.common.utils.chain.ChainExecutor
import io.dmcs.common.utils.chain.ChainHandler
import spock.lang.Specification

class ChainExecutorTest extends Specification {

    void execute_chain() {

        when:
        var input = new Input()
        var executor = new ChainExecutor<Input>()
        executor.addHandler(new Handler1())
        executor.execute(input)

        then:
        input.value == 1
    }

    static class Input {
        protected int value
    }

    static class Handler1 implements ChainHandler<Input> {

        @Override
        boolean proceed(Input input) throws Exception {
            input.value++
            return true
        }
    }
}
