package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.app.canvas.data.Assignment;
import dev.mathops.app.canvas.data.UserInfo;
import dev.mathops.app.ops.snapin.CanvasCourseIdMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility that connects to Canvas and loads all assignment data in a list of courses. The results are stored in a
 * data structure in the owning {@code CanvasFull} object, which can trigger updates to its display, and enabling of
 * controls to add or alter Canvas configuration.
 */
final class CanvasSyncher extends SwingWorker<String, SyncherStatus> {

    /** The Canvas API. */
    private final CanvasApi api;

    /** True to cancel/abort scan. */
    private final AtomicBoolean cancel;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /**
     * Constructs a new {@code CanvasSyncher}.
     *
     * @param theCanvasHost     the hostname of the Canvas installation
     * @param theAccessToken    the access token
     * @param theProgress       the progress bar
     * @param theInvokingButton the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel  the label for the progress bar - to be cleared when this process is complete
     */
    CanvasSyncher(final String theCanvasHost, final String theAccessToken, final JProgressBar theProgress,
                  final JButton theInvokingButton, final JLabel theProgressLabel) {

        super();

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

        this.cancel = new AtomicBoolean(false);
    }

    /**
     * Cancels the scan.
     */
    public void cancelScan() {

        this.cancel.set(true);
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

        // Fifteen sections (after cross-listing)

        publish(new SyncherStatus(0, 20, "Retrieving Canvas course IDs"));
        final CanvasCourseIdMap courseMap = new CanvasCourseIdMap();

        final Map<Long, List<Assignment>> assignmentsMap = scanCanvasAssignments(courseMap);

        // TODO: Load "Assignment Extension" data from Canvas

        publish(new SyncherStatus(16, 20, "Gathering Student Enrollments"));

        // TODO: Load "STCOURSE" data from Database and organize each registration by pace,
        // track,index within pace to get default due dates group, and gather "stmilestones"

        publish(new SyncherStatus(17, 20, "Making Updates to Canvas Due Dates"));

        // TODO: Update all Canvas due dates as needed

        publish(new SyncherStatus(18, 20, "Generating Report"));

        // TODO: Generate a report and populate the UI
    }

    /**
     * Scans Canvas assignments in the indicated courses. This loads all "Assignment" objects from the course map and
     * their associated "AssignmentOverride" objects.
     *
     * @param courseMap the map from course/section to canvas ID
     * @return a map from course ID to the list of parsed Assignment objects to populate (on success, this map will have
     *         a new entry for the specified course ID with the list of assignments found)
     */
    private Map<Long, List<Assignment>> scanCanvasAssignments(final CanvasCourseIdMap courseMap) {

        final Map<Long, List<Assignment>> assignmentsMap = new HashMap<>(10);

        publish(new SyncherStatus(1, 20, "Scanning Canvas Assignments - MATH 117 (001)"));
        // final Long c117001 = courseMap.getCanvasId(RawRecordConstants.M117, "001");
        // queryAssignments(c117001, assignmentsMap);

        publish(new SyncherStatus(2, 20, "Scanning Canvas Assignments - MATH 117 (002)"));
        // final Long c117002 = courseMap.getCanvasId(RawRecordConstants.M117, "002");
        // queryAssignments(c117002, assignmentsMap);

        publish(new SyncherStatus(3, 20, "Scanning Canvas Assignments - MATH 117 (80x)"));
        final Long c117801 = courseMap.getCanvasId(RawRecordConstants.M117, "801");
        queryAssignments(c117801, assignmentsMap);

        publish(new SyncherStatus(4, 20, "Scanning Canvas Assignments - MATH 118 (001)"));
        // final Long c118001 = courseMap.getCanvasId(RawRecordConstants.M118, "001");
        // queryAssignments(c118001, assignmentsMap);

        publish(new SyncherStatus(5, 20, "Scanning Canvas Assignments - MATH 118 (002)"));
        // final Long c118002 = courseMap.getCanvasId(RawRecordConstants.M118, "002");
        // queryAssignments(c118002, assignmentsMap);

        publish(new SyncherStatus(6, 20, "Scanning Canvas Assignments - MATH 118 (80x)"));
        final Long c118801 = courseMap.getCanvasId(RawRecordConstants.M118, "801");
        queryAssignments(c118801, assignmentsMap);

        publish(new SyncherStatus(7, 20, "Scanning Canvas Assignments - MATH 124 (001)"));
        // final Long c124001 = courseMap.getCanvasId(RawRecordConstants.M124, "001");
        // queryAssignments(c124001, assignmentsMap);

        publish(new SyncherStatus(8, 20, "Scanning Canvas Assignments - MATH 124 (002)"));
        // final Long c124002 = courseMap.getCanvasId(RawRecordConstants.M124, "002");
        // queryAssignments(c124002, assignmentsMap);

        publish(new SyncherStatus(9, 20, "Scanning Canvas Assignments - MATH 124 (80x)"));
        final Long c124801 = courseMap.getCanvasId(RawRecordConstants.M124, "801");
        queryAssignments(c124801, assignmentsMap);

        publish(new SyncherStatus(10, 20, "Scanning Canvas Assignments - MATH 125 (001)"));
        // final Long c125001 = courseMap.getCanvasId(RawRecordConstants.M125, "001");
        // queryAssignments(c125001, assignmentsMap);

        publish(new SyncherStatus(11, 20, "Scanning Canvas Assignments - MATH 125 (002)"));
        // final Long c125002 = courseMap.getCanvasId(RawRecordConstants.M125, "002");
        // queryAssignments(c125002, assignmentsMap);

        publish(new SyncherStatus(12, 20, "Scanning Canvas Assignments - MATH 125 (801)"));
        final Long c125801 = courseMap.getCanvasId(RawRecordConstants.M125, "801");
        queryAssignments(c125801, assignmentsMap);

        publish(new SyncherStatus(13, 20, "Scanning Canvas Assignments - MATH 126 (001)"));
        // final Long c126001 = courseMap.getCanvasId(RawRecordConstants.M126, "001");
        // queryAssignments(c126001, assignmentsMap);

        publish(new SyncherStatus(14, 20, "Scanning Canvas Assignments - MATH 126 (002)"));
        // final Long c126002 = courseMap.getCanvasId(RawRecordConstants.M126, "002");
        // queryAssignments(c126002, assignmentsMap);

        publish(new SyncherStatus(15, 20, "Scanning Canvas Assignments - MATH 126 (80x)"));
        final Long c126801 = courseMap.getCanvasId(RawRecordConstants.M126, "801");
        queryAssignments(c126801, assignmentsMap);

        return assignmentsMap;
    }

    /**
     * Performs an assignments query from Canvas, then extracts the resulting array of {@code Assignment} objects from
     * the reply and adds them to a map.
     *
     * @param courseId       the course ID to query
     * @param assignmentsMap a map from course ID to the list of parsed Assignment objects to populate (on success, this
     *                       map will have a new entry for the specified course ID with the list of assignments found)
     */
    private void queryAssignments(final Long courseId,
                                  final Map<? super Long, ? super List<Assignment>> assignmentsMap) {

        // GET /api/v1/courses/:course_id/assignments?include[]=overrides

        final ApiResult result = this.api.paginatedApiCall("courses/"
                + courseId + "/assignments?include[]=overrides", "GET");

        if (result.arrayResponse == null) {
            Log.warning(result.error);
        } else {
            final List<Assignment> list = new ArrayList<>(result.arrayResponse.size());

            for (final JSONObject obj : result.arrayResponse) {
                list.add(new Assignment(obj));
            }

            assignmentsMap.put(courseId, list);
        }
    }

    /**
     * Called when an update is published.
     *
     * @param chunks data chunks
     */
    @Override
    protected void process(final List<SyncherStatus> chunks) {

        if (!chunks.isEmpty()) {
            for (final SyncherStatus stat : chunks) {
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
