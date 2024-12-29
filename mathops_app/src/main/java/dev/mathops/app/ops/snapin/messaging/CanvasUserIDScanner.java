package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.app.canvas.data.UserInfo;
import dev.mathops.app.ops.snapin.CanvasCourseIdMap;
import dev.mathops.text.parser.json.JSONObject;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility that connects to Canvas and loads all students in a list of courses. The Canvas IDs of each student are
 * then stored in the "STUDENT" table.
 */
final class CanvasUserIDScanner extends SwingWorker<String, ScannerStatus> {

    /** The data cache. */
    private final Cache cache;

    /** The Canvas API. */
    private final CanvasApi api;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /**
     * Constructs a new {@code CanvasUserIDScanner}.
     *
     * @param theCache          the data cache
     * @param theCanvasHost     the hostname of the Canvas installation
     * @param theAccessToken    the access token
     * @param theProgress       the progress bar
     * @param theInvokingButton the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel  the label for the progress bar - to be cleared when this process is complete
     */
    CanvasUserIDScanner(final Cache theCache, final String theCanvasHost,
                        final String theAccessToken, final JProgressBar theProgress,
                        final JButton theInvokingButton, final JLabel theProgressLabel) {

        super();

        this.cache = theCache;
        this.api = new CanvasApi(theCanvasHost, theAccessToken);
        this.progressBar = theProgress;
        this.invokingButton = theInvokingButton;
        this.progressLabel = theProgressLabel;

        final UserInfo userInfo = this.api.fetchUser();
        if (userInfo == null) {
            throw new IllegalArgumentException("Unable to log in and check user ID.");
        }

        final String name = userInfo.getDisplayName();
        Log.info("Connected to Canvas as ", name);

    }

    /**
     * Execute the task in a background thread.
     */
    @Override
    public String doInBackground() {

        scanCourses();

        return CoreConstants.EMPTY;
    }

    /**
     * Scans and prints the list of all Canvas courses, so canvas course IDs can be installed in the database for use
     * when sending messages.
     */
    private void scanCourses() {

        publish(new ScannerStatus(0, 12, "Retrieving Canvas course IDs"));

        final CanvasCourseIdMap courseMap = new CanvasCourseIdMap();

        final Long crs117001 = courseMap.getCanvasId(RawRecordConstants.M117, "001");
        final Long crs117801 = courseMap.getCanvasId(RawRecordConstants.M117, "801");

        final Long crs118001 = courseMap.getCanvasId(RawRecordConstants.M118, "001");
        final Long crs118801 = courseMap.getCanvasId(RawRecordConstants.M118, "801");

        final Long crs124001 = courseMap.getCanvasId(RawRecordConstants.M124, "001");
        final Long crs124801 = courseMap.getCanvasId(RawRecordConstants.M124, "801");

        final Long crs125001 = courseMap.getCanvasId(RawRecordConstants.M125, "001");
        final Long crs125801 = courseMap.getCanvasId(RawRecordConstants.M125, "801");

        final Long crs126001 = courseMap.getCanvasId(RawRecordConstants.M126, "001");
        final Long crs126801 = courseMap.getCanvasId(RawRecordConstants.M126, "801");

        final Map<String, Long> csuIdToCanvasId = new HashMap<>(3200);

        publish(new ScannerStatus(1, 12, "Scanning MATH 117 section 001"));
        scan(crs117001, csuIdToCanvasId);

        publish(new ScannerStatus(2, 12, "Scanning MATH 117 section 801"));
        scan(crs117801, csuIdToCanvasId);

        publish(new ScannerStatus(3, 12, "Scanning MATH 118 section 001"));
        scan(crs118001, csuIdToCanvasId);

        publish(new ScannerStatus(4, 12, "Scanning MATH 118 section 801"));
        scan(crs118801, csuIdToCanvasId);

        publish(new ScannerStatus(5, 12, "Scanning MATH 124 section 001"));
        scan(crs124001, csuIdToCanvasId);

        publish(new ScannerStatus(6, 12, "Scanning MATH 124 section 801"));
        scan(crs124801, csuIdToCanvasId);

        publish(new ScannerStatus(7, 12, "Scanning MATH 125 section 001"));
        scan(crs125001, csuIdToCanvasId);

        publish(new ScannerStatus(8, 12, "Scanning MATH 125 section 801"));
        scan(crs125801, csuIdToCanvasId);

        publish(new ScannerStatus(9, 12, "Scanning MATH 126 section 001"));
        scan(crs126001, csuIdToCanvasId);

        publish(new ScannerStatus(10, 12, "Scanning MATH 126 section 801"));
        scan(crs126801, csuIdToCanvasId);

        Log.info("Found a total of " + csuIdToCanvasId.size() + " mappings.");

        publish(new ScannerStatus(11, 12, "Updating database with Canvas IDs"));
        try {
            for (final Map.Entry<String, Long> entry : csuIdToCanvasId.entrySet()) {
                final String csuId = entry.getKey();
                if (csuId == null) {
                    continue;
                }

                final Long canvasId = entry.getValue();

                final RawStudent student = RawStudentLogic.query(this.cache, csuId, false);

                if (student == null) {
                    Log.warning("No STUDENT row for ", csuId);
                } else {
                    final String canvasIdString = canvasId.toString();

                    if (!canvasIdString.equals(student.canvasId)) {
                        Log.info("Assigning CSU ID " + csuId + " to Canvas ID " + canvasIdString);
                        RawStudentLogic.updateCanvasId(this.cache, csuId, canvasIdString);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query active term", ex);
        }
    }

    /**
     * Queries the enrollment for a Canvas course, and adds all students found to a map from CSU ID to Canvas student
     * ID.
     *
     * @param canvasCourseId  the canvas course ID
     * @param csuIdToCanvasId the map to which to add mappings found
     */
    private void scan(final Long canvasCourseId, final Map<? super String, ? super Long> csuIdToCanvasId) {

        final String path = "courses/" + canvasCourseId + "/students";

        final ApiResult result = this.api.paginatedApiCall(path, "GET");

        if (result.arrayResponse != null) {
            final int count = result.arrayResponse.size();
            for (int i = 0; i < count; ++i) {
                final JSONObject obj = result.arrayResponse.get(i);

                final Object id = obj.getProperty("id");

                if (id instanceof Number) {
                    final Object sisId = obj.getProperty("sis_user_id");
                    if (sisId != null) {
                        final Long canvasId = Long.valueOf(((Number) id).longValue());

                        // Log.info("CSU ID " + sisId + " has Canvas ID " + canvasId);

                        csuIdToCanvasId.put(sisId.toString(), canvasId);
                    }
                }
            }
        } else {
            Log.warning("ERROR: " + result.error);
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
