package dev.mathops.core.log;

/**
 * A logged object that manages a read lock and a write lock in such a way that only one thread may hold the write lock
 * at a given time, and only when there are no read locks, but multiple threads may hold read locks (assuming there is
 * no write lock). That is, the object can be written (mutated) by only one thread, but multiple threads can access the
 * object knowing that it will not be mutated at the same time.
 */
public class Locked {

    /** Object on which to synchronize access to locking mechanism. */
    private final Object lockSynch;

    /** The number of currently held read locks. */
    private final int numReaders;

    /** The number of currently held write locks (all by the same thread). */
    private int numWriters;

    /** The thread currently holding any open write locks. */
    private Thread curWriter;

    /**
     * Flag indicating the object is notifying listeners of a mutation (this is done without write locks held, but if a
     * listener attempts to mutate the object during notification by attempting to obtain a write lock, this must fail -
     * otherwise, a listener could mutate the object before subsequent listeners are notified of the original change).
     */
    private final boolean notifying;

    /**
     * Constructs a new {@code Locked}.
     */
    public Locked() {

        super();

        this.numReaders = 0;
        this.numWriters = 0;
        this.curWriter = null;
        this.notifying = false;
        this.lockSynch = new Object();
    }

    /**
     * Acquires a lock to begin mutating the data this lock protects. There can be no writing, notification of changes,
     * or reading going on in order to gain the lock. Additionally, a thread is allowed to gain more than one write lock,
     * as long as it doesn't attempt to gain additional write locks from within listener notification. Attempting to
     * gain a write lock from within a listener notification will result in an {@code IllegalStateException}.
     *
     * <p>
     * Calls to {@code writeLock} must be balanced with calls to {@code writeUnlock}, else the protected data will be
     * left in a locked state so that no reading or writing can be done.
     */
    public final void writeLock() {

        final Thread curThread = Thread.currentThread();

        synchronized (this.lockSynch) {
            try {
                while (this.numReaders > 0 || this.curWriter != null) {

                    if (curThread == this.curWriter) {
                        if (this.notifying) {
                            throw new IllegalStateException(Res.get(Res.MUT_IN_NOTIFY));
                        }

                        ++this.numWriters;
                        return;
                    }

                    this.lockSynch.wait();
                }

                this.curWriter = curThread;
                this.numWriters = 1;
            } catch (final InterruptedException ex) {

                // Restore the interrupted status
                curThread.interrupt();
            }
        }
    }

    /**
     * Releases a write lock previously obtained via {@code writeLock}. After decrementing the lock count if there are
     * no outstanding locks this will allow a new writer, or readers.
     */
    public final void writeUnlock() {

        synchronized (this.lockSynch) {

            --this.numWriters;
            if (this.numWriters <= 0) {
                this.numWriters = 0;
                this.curWriter = null;
                this.lockSynch.notifyAll();
            }
        }
    }

    /**
     * Tests whether the current thread holds the write lock.
     *
     * @return {@code true} if the calling thread holds the write lock
     */
    public final boolean isCurWriter() {

        return this.curWriter == Thread.currentThread();
    }
}
