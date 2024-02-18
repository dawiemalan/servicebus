package io.dmcs.common.executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChainExecutorTest {

	@Test
	void testExecute() throws Exception {

		var input = new Input();
		var executor = new ChainExecutor<Input>();
		executor.addHandler(new Handler1());
		executor.execute(input);

		Assertions.assertEquals(1, input.value);
	}

	static class Input {
		protected int value;
	}

	static class Handler1 implements ChainHandler<Input> {

		@Override
		public boolean proceed(Input input) throws Exception {
			input.value++;
			return true;
		}
	}

}
