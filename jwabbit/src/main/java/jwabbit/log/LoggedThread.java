package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A thread that includes a static logger to which diagnostic messages can be logged.
 */
public class LoggedThread extends Thread {

    /** A log to which to write diagnostic messages. */
    protected static final ObjLogger LOG;

    static {
        LOG = new ObjLogger();
    }

    /**
     * Constructs a new {@code LoggedThread}.
     *
     * @param name the name of the thread
     */
    public LoggedThread(final String name) {

        super(name);
    }
}
