package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of log entries.
 */
class LogEntryList extends Synchronized {

    /** Initial size of the cached log entry list. */
    private static final int INIT_LIST_SIZE = 50;

    /** The accumulated log data. */
    private final List<LogEntry> logData;

    /** Flag indicating log messages should be written to the internal list. */
    private final boolean logToList;

    /** The maximum number of list entries to retain. */
    private final int maxListEntries;

    /**
     * Constructs a new {@code LogEntryList}.
     */
    LogEntryList() {

        super();

        this.logData = new ArrayList<>(INIT_LIST_SIZE);
        this.logToList = false;
        this.maxListEntries = Integer.MAX_VALUE;
    }

    /**
     * Adds a message to the list.
     *
     * @param msg the message to add
     */
    final void addToList(final String msg) {

        synchronized (getSynch()) {
            if (this.logToList) {
                this.logData.add(new LogEntry(msg));

                if (this.logData.size() > this.maxListEntries) {
                    this.logData.remove(0);
                }
            }
        }
    }
}
