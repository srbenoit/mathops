package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.json.JSONObject;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawStmsgLogic;
import dev.mathops.db.rawlogic.RawSttermLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawStmsg;
import dev.mathops.db.rawrecord.RawStterm;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that scans the database each evening and determines any students to whom a personalized email should
 * be sent based on their current course status.
 */
final class CanvasConversationImporter extends SwingWorker<String, ScannerStatus> {

    /** Formatter that can parse date/time strings. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);

    /** The Canvas API. */
    private final CanvasApi api;

    /** The data cache. */
    private final Cache cache;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean cancel;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /**
     * Constructs a new {@code CanvasConversationImporter}.
     *
     * @param theCache          the data cache
     * @param theCanvasHost     the hostname of the Canvas installation
     * @param theAccessToken    the access token
     * @param theProgress       the progress bar
     * @param theInvokingButton the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel  the label for the progress bar - to be cleared when this process is complete
     */
    CanvasConversationImporter(final Cache theCache, final String theCanvasHost,
                               final String theAccessToken, final JProgressBar theProgress,
                               final JButton theInvokingButton, final JLabel theProgressLabel) {

        super();

        this.cache = theCache;
        this.api = new CanvasApi(theCanvasHost, theAccessToken);
        this.progressBar = theProgress;
        this.invokingButton = theInvokingButton;
        this.progressLabel = theProgressLabel;

        this.cancel = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    public void cancel() {

        this.cancel.set(true);
    }

    /**
     * Execute the task in a background thread.
     */
    @Override
    public String doInBackground() {

        // In terms of progress, the first 1% will be loading data, then next 99 will be scanning
        // students.

        this.cancel.set(false);

        try {
            publish(new ScannerStatus(0, 100,
                    "Querying 'stterm' records to get Canvas IDs of students"));

            final TermRec active = TermLogic.get(this.cache).queryActive(this.cache);
            final List<RawStterm> stterms = RawSttermLogic.queryAllByTerm(this.cache, active.term);

            final Map<String, RawStterm> canvasIdToStterm = new HashMap<>(stterms.size());
            for (final RawStterm row : stterms) {
                final RawStudent stu = RawStudentLogic.query(this.cache, row.stuId, false);

                if (stu != null && stu.canvasId != null) {
                    canvasIdToStterm.put(stu.canvasId, row);
                }
            }
            Log.info("Queried ", Integer.toString(canvasIdToStterm.size()), " canvas student IDs");

            //

            publish(new ScannerStatus(5, 100, "Querying most recent message dates"));
            final List<RawStmsg> messages = RawStmsgLogic.INSTANCE.queryAll(this.cache);

            final Map<String, LocalDate> csuIdToLatestMsgDate =
                    new HashMap<>(canvasIdToStterm.size());
            for (final RawStmsg row : messages) {
                final LocalDate existing = csuIdToLatestMsgDate.get(row.stuId);
                if (existing == null || existing.isBefore(row.msgDt)) {
                    csuIdToLatestMsgDate.put(row.stuId, row.msgDt);
                }
            }
            Log.info("Queried latest dates for ", Integer.toString(csuIdToLatestMsgDate.size()), " students");

            //

            publish(new ScannerStatus(10, 100, "Querying conversations"));

            final ApiResult result = this.api.paginatedApiCall("conversations", "GET");

            publish(new ScannerStatus(50, 100, "Sorting conversations"));

            if (!this.cancel.get()) {
                if (result.error == null) {
                    final List<JSONObject> list = result.arrayResponse;

                    Log.info("Retrieved information on " + list.size() + " conversations");

                    for (final JSONObject obj : list) {
                        if (!this.cancel.get()) {
                            processConversation(obj, canvasIdToStterm, csuIdToLatestMsgDate);
                        }
                    }
                } else {
                    publish(new ScannerStatus(100, 100, "ERROR: " + result.error));
                }
            }

            publish(new ScannerStatus(100, 100, "Finished"));
        } catch (

                final SQLException ex) {
            publish(new ScannerStatus(100, 100, "ERROR: " + ex.getMessage()));
        }

        return CoreConstants.EMPTY;
    }

    /**
     * Processes a single conversation record.
     *
     * @param obj                  the conversation object
     * @param canvasIdToStterm     map from canvas course ID to student term record
     * @param csuIdToLatestMsgDate map from student ID to date/time of last message we know of
     */
    private void processConversation(final JSONObject obj, final Map<String, RawStterm> canvasIdToStterm,
                                     final Map<String, LocalDate> csuIdToLatestMsgDate) {

        Log.info(obj.toJSONFriendly(0));

        final Object participantsObj = obj.getProperty("participants");

        if (participantsObj instanceof final Object[] list) {

            for (final Object entry : list) {
                if (entry instanceof final JSONObject partic) {

                    final Double particId = partic.getNumberProperty("id");
                    if (particId == null) {
                        Log.warning("No participant ID for participant in conversation");
                    } else {
                        final String id = Long.toString(particId.longValue());

                        if ("7829".equals(id)) {
                            // Instructor's ID - ignore that
                            continue;
                        }

                        final RawStterm stterm = canvasIdToStterm.get(id);
                        if (stterm == null) {
                            final String fullName = partic.getStringProperty("full_name");
                            Log.warning("No CSU ID found for canvas ID ", id, " (", fullName, ")");
                        } else {
                            final LocalDate latest = csuIdToLatestMsgDate.get(stterm.stuId);

                            final String lastMsgStr = obj.getStringProperty("last_message_at");

                            if (lastMsgStr == null) {
                                Log.warning("No last message date for conversation");
                            } else {
                                final LocalDate lastMsgDate = LocalDate.parse(lastMsgStr, DATE_FORMATTER);

                                if (lastMsgDate.isAfter(latest)) {
                                    // Need to record an "out of band" message date
                                    Log.info("Out of band for ", stterm.stuId, " of ", lastMsgDate);

                                    final RawStmsg newStmsg = new RawStmsg(stterm.stuId, lastMsgDate, stterm.pace,
                                            Integer.valueOf(0), "OOB", "Canvas", "Canvas");

                                    try {
                                        RawStmsgLogic.INSTANCE.insert(this.cache, newStmsg);
                                    } catch (final SQLException ex) {
                                        Log.warning(ex);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.warning("Entry in participants list was not JSONObject");
                }
            }
        } else if (participantsObj == null) {
            Log.warning("No participants list found for conversation");
        } else {
            Log.warning("Participants list was ", participantsObj.getClass().getName());
        }
    }

    /**
     * Called when an update is published.
     *
     * @param chunks data chunks
     */
    @Override
    protected void process(final List<ScannerStatus> chunks) {

        if (!chunks.isEmpty()) {
            for (final ScannerStatus stat : chunks) {
                final int progressValue = 1000 * stat.stepsCompleted / stat.totalSteps;
                this.progressBar.setValue(progressValue);
                this.progressBar.setString(stat.description);
            }
        }
    }

    /**
     * Called when the task is done.
     */
    @Override
    protected void done() {

        this.progressBar.setValue(0);
        this.progressBar.setString(CoreConstants.EMPTY);
        this.invokingButton.setEnabled(true);
        this.progressLabel.setText(CoreConstants.SPC);
    }
}
