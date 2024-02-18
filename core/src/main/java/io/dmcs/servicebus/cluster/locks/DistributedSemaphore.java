package io.dmcs.servicebus.cluster.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface DistributedSemaphore {

    /**
     * Set the data to put for the node created by this semaphore. This must be called prior to calling one
     * of the acquire() methods.
     *
     * @param nodeData node data
     */
    void setNodeData(byte[] nodeData);

    /**
     * Return a list of all current nodes participating in the semaphore
     *
     * @return list of nodes
     * @throws Exception ZK errors, interruptions, etc.
     */
    Collection<String> getParticipantNodes();

    /**
     * Convenience method. Closes all leases in the given collection of leases
     *
     * @param leases leases to close
     */
    void returnAll(Collection<Lease> leases);

    /**
     * Convenience method. Closes the lease
     *
     * @param lease lease to close
     */
    void returnLease(Lease lease);

    /**
     * <p>Acquire a lease. If no leases are available, this method blocks until either the maximum
     * number of leases is increased or another client/process closes a lease.</p>
     * <p>The client must close the lease when it is done with it. You should do this in a
     * <code>finally</code> block.</p>
     *
     * @return the new lease
     * @throws Exception ZK errors, interruptions, etc.
     */
    Lease acquire() throws Exception;

    /**
     * <p>Acquire <code>qty</code> leases. If there are not enough leases available, this method
     * blocks until either the maximum number of leases is increased enough or other clients/processes
     * close enough leases.</p>
     * <p>The client must close the leases when it is done with them. You should do this in a
     * <code>finally</code> block. NOTE: You can use {@link #returnAll(Collection)} for this.</p>
     *
     * @param qty number of leases to acquire
     * @return the new leases
     * @throws Exception ZK errors, interruptions, etc.
     */
    Collection<Lease> acquire(int qty) throws Exception;

    /**
     * <p>Acquire a lease. If no leases are available, this method blocks until either the maximum
     * number of leases is increased or another client/process closes a lease. However, this method
     * will only block to a maximum of the time parameters given.</p>
     * <p>The client must close the lease when it is done with it. You should do this in a
     * <code>finally</code> block.</p>
     *
     * @param time time to wait
     * @param unit time unit
     * @return the new lease or null if time ran out
     * @throws Exception ZK errors, interruptions, etc.
     */
    Lease acquire(long time, TimeUnit unit) throws Exception;

    /**
     * <p>Acquire <code>qty</code> leases. If there are not enough leases available, this method
     * blocks until either the maximum number of leases is increased enough or other clients/processes
     * close enough leases. However, this method will only block to a maximum of the time
     * parameters given. If time expires before all leases are acquired, the subset of acquired
     * leases are automatically closed.</p>
     * <p>The client must close the leases when it is done with them. You should do this in a
     * <code>finally</code> block. NOTE: You can use {@link #returnAll(Collection)} for this.</p>
     *
     * @param qty  number of leases to acquire
     * @param time time to wait
     * @param unit time unit
     * @return the new leases or null if time ran out
     * @throws Exception ZK errors, interruptions, etc.
     */
    Collection<Lease> acquire(int qty, long time, TimeUnit unit) throws Exception;
}
