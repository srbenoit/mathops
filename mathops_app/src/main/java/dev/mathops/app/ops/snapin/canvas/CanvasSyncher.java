package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.app.canvas.data.UserInfo;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.util.List;
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

//        publish(new SyncherStatus(0, 20, "Retrieving Users"));
//
//        // GET /api/v1/courses/:course_id/users
//
//        final ApiResult r1 = this.api.paginatedApiCall("courses/57154/users?enrollment_type[]=student", "GET");
//
//        if (r1.arrayResponse == null) {
//            Log.warning(r1.error);
//        } else {
//            final int count = r1.arrayResponse.size();
//            Log.info("Found " + count + " enrolled students");
//
//            for (final JSONObject obj : r1.arrayResponse) {
//                Log.info("ID=", obj.getProperty("id"));
//                Log.info("  name=", obj.getProperty("name"));
//                Log.info("  sortable_name=", obj.getProperty("sortable_name"));
//                Log.info("  sis_user_id=", obj.getProperty("sis_user_id"));
//                Log.info("  integration_id=", obj.getProperty("integration_id"));
//                Log.info("  login_id=", obj.getProperty("login_id"));
//                Log.info("  email=", obj.getProperty("email"));
//            }
//        }

        // List external tools and create the "Learning Target Assessment" external tool if needed.

        // GET /api/v1/courses/:course_id/external_tools

        final ApiResult r0 = this.api.paginatedApiCall("courses/57154/external_tools", "GET");
        if (r0.arrayResponse == null) {
            Log.warning(r0.error);
        } else {
            final int count = r0.arrayResponse.size();
            Log.info("Found " + count + " external tools");
            boolean foundLTExternal = false;

            for (final JSONObject obj : r0.arrayResponse) {
                final Object name = obj.getProperty("name");
                if ("Learning Target Assignment Tool".equals(name)) {
                    foundLTExternal = true;
                }

                Log.info("ID=", obj.getProperty("id"));
                Log.info("  domain=", obj.getProperty("domain"));
                Log.info("  name=", name);
                Log.info("  url=", obj.getProperty("url"));
                Log.info("  consumer_key=", obj.getProperty("consumer_key"));
            }

            if (!foundLTExternal) {
                // POST /api/v1/courses/:course_id/external_tools
                Log.info("Attempting to create external tool...");

                final ApiResult x1 = this.api.paginatedApiCall("courses/57154/external_tools"
                        + "?name=Learning%20Target%20Assignment%20Tool"
                        + "&privacy_level=public"
                        + "&consumer_key=asdfg"
                        + "&shared_secret=dlsdf"
                        + "&url=https://testing.math.colostate.edu/lta/lta.html", "POST");

                if (x1.response instanceof JSONObject obj) {
                    Log.info("ID=", obj.getProperty("id"));
                    Log.info("  domain=", obj.getProperty("domain"));
                    Log.info("  name=", obj.getProperty("name"));
                    Log.info("  url=", obj.getProperty("url"));
                    Log.info("  consumer_key=", obj.getProperty("consumer_key"));
                } else {
                    Log.warning(x1.error);
                }
            }
        }




        // List assignment groups, if there is not one called "Learning Target Assignments", create it...

        // GET /api/v1/courses/:course_id/assignment_groups

//        final ApiResult r2 = this.api.paginatedApiCall("courses/57154/assignment_groups", "GET");
//        Object foundLTid = null;
//
//        if (r2.arrayResponse == null) {
//            Log.warning(r2.error);
//        } else {
//            final int count = r2.arrayResponse.size();
//            Log.info("Found " + count + " assignment groups");
//
//            for (final JSONObject obj : r2.arrayResponse) {
//                final Object name = obj.getProperty("name");
//                if ("Learning Target Assignments".equals(name)) {
//                    foundLTid = obj.getProperty("id");
//                }
//
//                Log.info("ID=", obj.getProperty("id"));
//                Log.info("  name=", name);
//                Log.info("  position=", obj.getProperty("position"));
//            }
//
//            if (foundLTid == null) {
//                // POST /api/v1/courses/:course_id/assignment_groups
//
//                final ApiResult x1 = this.api.paginatedApiCall("courses/57154/assignment_groups"
//                        + "?name=Learning%20Target%20Assignments", "POST");
//
//                if (x1.response instanceof JSONObject obj) {
//                    foundLTid = obj.getProperty("id");
//                }
//            }
//        }

        // GET /api/v1/courses/:course_id/assignments

//        final ApiResult r3 = this.api.paginatedApiCall("courses/57154/assignments", "GET");
//
//        if (r3.arrayResponse == null) {
//            Log.warning(r3.error);
//        } else {
//            final int count = r3.arrayResponse.size();
//            Log.info("Found " + count + " assignments");
//
//            boolean foundLT11 = false;
//            boolean foundLT12 = false;
//            boolean foundLT13 = false;
//
//            for (final JSONObject obj : r3.arrayResponse) {
//                final Object name = obj.getProperty("name");
//                if ("Learning Target Assignment 1.1".equals(name)) {
//                    foundLT11 = true;
//                } else if ("Learning Target Assignment 1.2".equals(name)) {
//                    foundLT12 = true;
//                } else if ("Learning Target Assignment 1.3".equals(name)) {
//                    foundLT13 = true;
//                }
//
//                Log.info("ID=", obj.getProperty("id"));
//                Log.info("  name=", name);
//                Log.info("  html_url=", obj.getProperty("html_url"));
//                Log.info("  assignment_group_id=", obj.getProperty("assignment_group_id"));
//                Log.info("  position=", obj.getProperty("position"));
//                Log.info("  points_possible=", obj.getProperty("points_possible"));
//                Log.info("  submission_types=", obj.getProperty("submission_types"));
//                Log.info("  quiz_id=", obj.getProperty("quiz_id"));
//                Log.info("  discussion_topic=", obj.getProperty("discussion_topic"));
//                Log.info("  assignment_visibility=", obj.getProperty("assignment_visibility"));
//                Log.info("  overrides=", obj.getProperty("overrides"));
//                Log.info("  omit_from_final_grade=", obj.getProperty("omit_from_final_grade"));
//                Log.info("  hide_in_gradebook=", obj.getProperty("hide_in_gradebook"));
//                Log.info("  important_dates=", obj.getProperty("important_dates"));
//                Log.info("  is_quiz_assignment=", obj.getProperty("is_quiz_assignment"));
//                Log.info("  workflow_state=", obj.getProperty("workflow_state"));
//            }
//
//            if (foundLTid != null) {
//                if (!foundLT11) {
//                    // POST /api/v1/courses/:course_id/assignments
//
//                    final ApiResult x1 = this.api.paginatedApiCall("courses/57154/assignments"
//                            + "?assignment[name]=Learning%20Target%20Assignment%201.1"
//                            + "&assignment[submission_types][]=external_tool"
//                            + "&assignment[grading_type]=pass_fail"
//                            + "&assignment[assignment_group_id]=" + foundLTid
//                            + "&assignment[published]=true"
//                            + "&assignment[omit_from_final_grade]=true"
//                            + "&assignment[allowed_attempts]=-1", "POST");
//                }
//                if (!foundLT12) {
//                    // POST /api/v1/courses/:course_id/assignments
//
//                    final ApiResult x1 = this.api.paginatedApiCall("courses/57154/assignments"
//                            + "?assignment[name]=Learning%20Target%20Assignment%201.2"
//                            + "&assignment[submission_types][]=external_tool"
//                            + "&assignment[grading_type]=pass_fail"
//                            + "&assignment[assignment_group_id]=" + foundLTid
//                            + "&assignment[published]=true"
//                            + "&assignment[omit_from_final_grade]=true"
//                            + "&assignment[allowed_attempts]=-1", "POST");
//                }
//                if (!foundLT13) {
//                    // POST /api/v1/courses/:course_id/assignments
//
//                    final ApiResult x1 = this.api.paginatedApiCall("courses/57154/assignments"
//                            + "?assignment[name]=Learning%20Target%20Assignment%201.3"
//                            + "&assignment[submission_types][]=external_tool"
//                            + "&assignment[grading_type]=pass_fail"
//                            + "&assignment[assignment_group_id]=" + foundLTid
//                            + "&assignment[published]=true"
//                            + "&assignment[omit_from_final_grade]=true"
//                            + "&assignment[allowed_attempts]=-1", "POST");
//                }
//            }
//        }
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
