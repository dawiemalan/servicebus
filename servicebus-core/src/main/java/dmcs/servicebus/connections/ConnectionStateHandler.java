package dmcs.servicebus.connections;


import dmcs.common.utils.chain.ChainExecutor;
import dmcs.common.utils.chain.ChainHandler;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Chain of actions to take on connection status change
 */
public class ConnectionStateHandler<C extends Connection> extends ChainExecutor<C> {

    public ConnectionStateHandler(Collection<ChainHandler<C>> handlers) {
        handlerList.addAll(handlers);
    }

    @SafeVarargs
    public ConnectionStateHandler(Predicate<C>... predicates) {
        Stream.of(predicates).forEach(p -> handlerList.add(p::test));
    }
}
