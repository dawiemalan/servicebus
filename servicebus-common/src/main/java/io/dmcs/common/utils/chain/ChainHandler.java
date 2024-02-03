package io.dmcs.common.utils.chain;

/**
 * Interface to represent a chain of responsibility handler
 * ChainHandler is equal to a single node or processor in chain of responsibility design.
 *
 * @param <I> the input object
 */
public interface ChainHandler<I> {

    /**
     * Give the handler the option of being skipped
     *
     * @param input the input object
     * @return true if current handler should be skipped
     */
    default boolean skip(I input) {
        return false;
    }

    /**
     * Method to do the processing.
     *
     * @param input the input object
     * @return false if processing should end at the current handler
     * @throws Exception if an error occurs
     */
    boolean proceed(I input) throws Exception;

}
