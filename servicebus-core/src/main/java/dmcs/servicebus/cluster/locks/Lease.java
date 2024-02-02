package dmcs.servicebus.cluster.locks;

import java.io.IOException;

/**
 * Represents an acquired lease from a {@link DistributedSemaphore}. It is the client's responsibility
 * to close this lease when it is no longer needed so that other blocked clients can use it. If the
 * client crashes (or its session expires, etc.) the lease will automatically be closed.
 */
public interface Lease extends AutoCloseable {
    /**
     * Releases the lease so that other clients/processes can acquire it
     *
     * @throws IOException errors
     */
    @Override
    void close() throws IOException;

    /**
     * Return the data stored in the node for this lease
     *
     * @return data
     * @throws Exception errors
     */
    byte[] getData() throws Exception;

    /**
     * Return the semaphore name for this lease
     *
     * @return data
     */
    String getName();
}
