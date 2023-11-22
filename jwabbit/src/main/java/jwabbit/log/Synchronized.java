package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

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
