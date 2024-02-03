package io.dmcs.common.utils.chain;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Executor class to execute a list of handlers.
 *
 * @param <I> the input object type
 */
public class ChainExecutor<I> {

    protected List<ChainHandler<I>> handlerList = new LinkedList<>();

    public ChainExecutor(Collection<ChainHandler<I>> handlers) {
        handlerList.addAll(handlers);
    }

    @SafeVarargs
    public ChainExecutor(Predicate<I>... predicates) {
        Stream.of(predicates).forEach(p -> handlerList.add(p::test));
    }

    /**
     * Method to add a handler to execution list
     *
     * @param handler the handler to add
     */
    public void addHandler(ChainHandler<I> handler) {
        handlerList.add(handler);
    }

    /**
     * Method to execute the given list of handlers for a given input and status object.
     *
     * @param input the input object
     * @throws Exception if an error occurs
     */
    public void execute(I input) throws Exception {

        for (ChainHandler<I> handler : handlerList) {
            if (handler != null && !handler.skip(input) && !handler.proceed(input))
                break;
        }
    }
}
