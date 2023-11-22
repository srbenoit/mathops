package dev.mathops.web.websocket.help.queue;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity log for the help queue to allow administrators and developers to observe activity.
 */
public final class LiveHelpLog {

    /** Log entries. */
    private final List<String> entries;

    /**
     * Constructs a new {@code LiveHelpQueue}.
     */
    public LiveHelpLog() {

        this.entries = new ArrayList<>(20);
    }

    /**
     * Adds an entry to the log.
     *
     * @param entry the entry to add
     */
    public void log(final String entry) {

        synchronized (this) {
            this.entries.add(entry);

            if (this.entries.size() > 20) {
                this.entries.remove(0);
            }
        }
    }

    /**
     * Generates the serialized JSON representation of the queue, used to save its state on server shutdown so the queue
     * can be restored (using the {@code parse} factory method) after a restart.
     *
     * <pre>
     * { "log": [
     *    "Entry 1",
     *     ... additional entries ...
     *   ]
     * }
     * </pre>
     *
     * @return the JSON serialized representation
     */
    /* default */ String toJSON() {

        synchronized (this) {
            final int len = this.entries.size();

            final HtmlBuilder htm = new HtmlBuilder(20 + 100 * len);

            try {
                htm.addln("{ \"log\": [");

                for (int i = 0; i < len; ++i) {
                    htm.add("  \"",
                            this.entries.get(i).replace(CoreConstants.QUOTE, "\\\""),
                            CoreConstants.QUOTE);
                    if (i + 1 < len) {
                        htm.add(CoreConstants.COMMA_CHAR);
                    }
                    htm.addln();
                }

                htm.addln("  ]}");
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            return htm.toString();
        }
    }
}
