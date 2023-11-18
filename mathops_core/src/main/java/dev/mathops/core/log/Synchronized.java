package dev.mathops.core.log;

/**
 * Base class for objects with a synchronization object.
 */
class Synchronized {

    /** Object on which to synchronize member access. */
    private final Object synch;

    /**
     * Constructs a new {@code Synchronized}.
     */
    Synchronized() {

        this.synch = new Object();
    }

    /**
     * Gets the object on which to synchronize access to member variables.
     *
     * @return the synchronization object
     */
    final Object getSynch() {

        return this.synch;
    }
}
