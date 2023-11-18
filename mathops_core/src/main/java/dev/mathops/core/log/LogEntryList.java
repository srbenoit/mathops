package dev.mathops.core.log;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;

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
    private boolean logToList;

    /** The maximum number of list entries to retain. */
    private int maxListEntries;

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
    public final void addToList(final String msg) {

        synchronized (getSynch()) {
            if (this.logToList) {
                this.logData.add(new LogEntry(msg));

                if (this.logData.size() > this.maxListEntries) {
                    this.logData.remove(0);
                }
            }
        }
    }

    /**
     * Sets the flag so future log messages will be written to the internal list.
     *
     * @param maxEntries the maximum number of entries to retain in the list (-1 for no limit)
     */
    public final void startList(final int maxEntries) {

        synchronized (getSynch()) {
            this.logToList = true;
            this.maxListEntries = maxEntries;
        }
    }

    /**
     * Sets the flag so future log messages will not be written to the internal list.
     */
    public final void stopList() {

        synchronized (getSynch()) {
            this.logToList = false;
        }
    }

    /**
     * Clears the internal list of log messages.
     */
    public final void clearList() {

        synchronized (getSynch()) {
            this.logData.clear();
        }
    }

    /**
     * Gets the number of log messages in the internal list.
     *
     * @return the number of messages
     */
    public final int getNumInList() {

        synchronized (getSynch()) {
            return this.logData.size();
        }
    }

    /**
     * Gets one of the log messages from the internal list.
     *
     * @param index the index of the message to retrieve
     * @return the log message
     */
    public final LogEntry getListMessage(final int index) {

        synchronized (getSynch()) {
            return this.logData.get(index);
        }
    }

    /**
     * Generates the string that simply lists error messages, without timestamps. Useful when messages are being
     * accumulated during a parse process and a list of build errors is to be reported at the end of the process.
     *
     * @return the string
     */
    public final String errorMessagesAsString() {

        final int size = this.logData.size();
        final HtmlBuilder htm = new HtmlBuilder(size * 100);

        for (final LogEntry logDatum : this.logData) {
            htm.add(logDatum.message);
            htm.add(CoreConstants.CRLF);
        }

        return htm.toString();
    }
}
