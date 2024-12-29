package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.app.canvas.data.UserInfo;
import dev.mathops.app.ops.snapin.canvas.model.Assignment;
import dev.mathops.app.ops.snapin.canvas.model.AssignmentGroup;
import dev.mathops.app.ops.snapin.canvas.model.CanvasModel;
import dev.mathops.app.ops.snapin.canvas.model.Course;
import dev.mathops.app.ops.snapin.canvas.model.CourseTerm;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility that connects to Canvas and loads all data into a model data structure in the owning {@code CanvasFull}
 * object, which can trigger updates to its display, and enabling of controls to add or alter Canvas configuration.
 */
final class ModelReader extends SwingWorker<String, ModelReaderStatus> {

    /** The total number of steps in the loading process. */
    private static final int TOTAL_STEPS = 100;

    /** The Canvas API. */
    private final CanvasApi api;

    /** The list of course code fragments of interest. */
    private final String[] courseCodeFragments;

    /** True to cancel/abort scan. */
    private final AtomicBoolean cancel;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /**
     * Constructs a new {@code CanvasModelReader}.
     *
     * @param theCanvasHost          the hostname of the Canvas installation
     * @param theAccessToken         the access token
     * @param theCourseCodeFragments a list of course code fragments (like "MATH-117") of interest (only courses with
     *                               codes that match this pattern will be considered)
     * @param theProgress            the progress bar
     * @param theInvokingButton      the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel       the label for the progress bar - to be cleared when this process is complete
     */
    ModelReader(final String theCanvasHost, final String theAccessToken, final String[] theCourseCodeFragments,
                final JProgressBar theProgress, final JButton theInvokingButton, final JLabel theProgressLabel) {

        super();

        this.api = new CanvasApi(theCanvasHost, theAccessToken);
        this.courseCodeFragments = theCourseCodeFragments.clone();
        this.progressBar = theProgress;
        this.invokingButton = theInvokingButton;
        this.progressLabel = theProgressLabel;

        final UserInfo userInfo = this.api.fetchUser();
        if (userInfo == null) {
            throw new IllegalArgumentException("Unable to log in and check user ID.");
        }

        final String name = userInfo.getDisplayName();
        Log.info("CanvasModelReader Connected to Canvas as ", name);

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

        final CanvasModel model = loadModel();

        return CoreConstants.EMPTY;
    }

    /**
     * Scans and prints the list of all Canvas courses, so canvas course IDs can be installed in the database for use
     * when sending messages.
     */
    private CanvasModel loadModel() {

        final CanvasModel model = new CanvasModel();

        loadAllCourses(model, "teacher");
        loadAllCourses(model, "designer");
        loadAllAssignmentGroups(model);

        return model;
    }

    /**
     * Queries Canvas for all courses for which the user has a specified enrollment type and stores them in the model.
     *
     * @param model the Canvas data model
     */
    private void loadAllCourses(final CanvasModel model, final String enrollmentType) {

        publish(new ModelReaderStatus(1, TOTAL_STEPS, "Retrieving Courses"));

        // GET /api/v1/courses

        final ApiResult courses = this.api.paginatedApiCall("courses?enrollment_type=" + enrollmentType, "GET");
        int numCourses = 0;

        if (courses.arrayResponse == null) {
            Log.warning(courses.error);
        } else {
            for (final JSONObject obj : courses.arrayResponse) {
                final Object code = obj.getProperty("course_code");
                if (code instanceof final String codeString) {

                    boolean ofInterest = false;
                    for (final String fragment : this.courseCodeFragments) {
                        if (codeString.contains(fragment)) {
                            ofInterest = true;
                            break;
                        }
                    }

                    if (ofInterest) {
                        final Object id = obj.getProperty("id");
                        final Object name = obj.getProperty("name");
                        final Object state = obj.getProperty("workflow_state");
                        final Object start = obj.getProperty("start_at");
                        final Object end = obj.getProperty("end_at");

                        if (id instanceof final Double idDouble
                                && name instanceof final String nameString
                                && state instanceof final String stateString) {

                            final double idDbl = idDouble.doubleValue();
                            final long idLong = Math.round(idDbl);

                            final int dash = codeString.indexOf('-');
                            final String term = dash == -1 ? CoreConstants.EMPTY : codeString.substring(0, dash);
                            final CourseTerm cTerm = model.courseTerms.computeIfAbsent(term, s -> new CourseTerm(term));

                            ZonedDateTime startDate = null;
                            if (start instanceof final String startString) {
                                try {
                                    startDate = ZonedDateTime.parse(startString);
                                } catch (final DateTimeParseException ex) {
                                    Log.warning(ex);
                                }
                            }

                            ZonedDateTime endDate = null;
                            if (end instanceof final String endString) {
                                try {
                                    endDate = ZonedDateTime.parse(endString);
                                } catch (final DateTimeParseException ex) {
                                    Log.warning(ex);
                                }
                            }

                            final Course crs = new Course(idLong, codeString, nameString, stateString, startDate,
                                    endDate);
                            cTerm.courses.add(crs);
                            ++numCourses;
                        }
                    }
                }
            }
        }

        Log.info("Found " + numCourses + " courses of interest spanning " + model.courseTerms.size() + " terms");
    }

    /**
     * Queries Canvas for all assignment groups defined in any Canvas courses that are currently running or will start
     * in the future.
     *
     * @param model the Canvas data model
     */
    private void loadAllAssignmentGroups(final CanvasModel model) {

        final LocalDate today = LocalDate.now();

        publish(new ModelReaderStatus(2, TOTAL_STEPS, "Retrieving Assignment Groups for Active and Future Courses"));

        for (final CourseTerm courseTerm : model.courseTerms.values()) {
            // Only consider non-development terms
            if (courseTerm.termId.startsWith("20")) {
                for (final Course course : courseTerm.courses) {
                    if (course.isActiveOrFuture(today)) {
                        loadCourseAssignmentGroups(course);
                    }
                }
            }
        }
    }

    /**
     * Queries Canvas for all assignment groups defined in any Canvas courses that are currently running or will start
     * in the future.
     *
     * @param course the Canvas course
     */
    private void loadCourseAssignmentGroups(final Course course) {

        // GET /api/v1/courses/:course_id/assignment_groups

        final ApiResult courses = this.api.paginatedApiCall("courses/" + course.id + "/assignment_groups", "GET");

        if (courses.arrayResponse == null) {
            Log.warning(courses.error);
        } else {
            for (final JSONObject obj : courses.arrayResponse) {
                final Object id = obj.getProperty("id");
                final Object name = obj.getProperty("name");

                if (id instanceof final Double idDouble && name instanceof final String nameString) {

                    final double idDbl = idDouble.doubleValue();
                    final long idLong = Math.round(idDbl);

                    final AssignmentGroup group = new AssignmentGroup(idLong, nameString);

                    course.assignmentGroups.add(group);
                }
            }
        }
    }

    /**
     * Queries Canvas for all assignments defined in any Canvas courses that are currently running or will start in the
     * future.
     *
     * @param model the Canvas data model
     */
    private void loadAllAssignments(final CanvasModel model) {

        final LocalDate today = LocalDate.now();

        publish(new ModelReaderStatus(2, TOTAL_STEPS, "Retrieving Assignments for Active and Future Courses"));

        for (final CourseTerm courseTerm : model.courseTerms.values()) {
            // Only consider non-development terms
            if (courseTerm.termId.startsWith("20")) {
                for (final Course course : courseTerm.courses) {
                    if (course.isActiveOrFuture(today)) {
                        loadCourseAssignments(course);
                    }
                }
            }
        }
    }

    /**
     * Queries Canvas for all assignments defined in any Canvas courses that are currently running or will start in the
     * future.
     *
     * @param course the Canvas course
     */
    private void loadCourseAssignments(final Course course) {

        // GET /api/v1/courses/:course_id/assignments

        final ApiResult courses = this.api.paginatedApiCall("courses/" + course.id + "/assignments", "GET");

        if (courses.arrayResponse == null) {
            Log.warning(courses.error);
        } else {
            for (final JSONObject obj : courses.arrayResponse) {
                final Object id = obj.getProperty("id");
                final Object name = obj.getProperty("name");
                final Object groupId = obj.getProperty("assignment_group_id");
                final Object description = obj.getProperty("description");
                final Object due = obj.getProperty("due_at");
                final Object external_tool_tag_attributes = obj.getProperty("external_tool_tag_attributes");
                final Object points_possible = obj.getProperty("points_possible");
                final Object submission_types = obj.getProperty("submission_types");
                final Object grading_type = obj.getProperty("grading_type");
                final Object published = obj.getProperty("published");
                final Object omit_from_final_grade = obj.getProperty("omit_from_final_grade");
                final Object hide_in_gradebook = obj.getProperty("hide_in_gradebook");
                final Object allowed_attempts = obj.getProperty("allowed_attempts");

                if (id instanceof final Double idDouble
                        && name instanceof final String nameString
                        && groupId instanceof final Double groupIdDouble) {

                    final double idDbl = idDouble.doubleValue();
                    final long idLong = Math.round(idDbl);

                    final double groupIdDbl = groupIdDouble.doubleValue();
                    final long groupIdLong = Math.round(groupIdDbl);

                    AssignmentGroup group = null;
                    for (AssignmentGroup test : course.assignmentGroups) {
                        if (test.id == groupIdLong) {
                            group = test;
                            break;
                        }
                    }
                    if (group == null) {
                        group = new AssignmentGroup(groupIdLong, "Unknown Group");
                        course.assignmentGroups.add(group);
                    }

                    final Assignment assign = new Assignment(idLong, nameString);
                    group.assignments.add(assign);
                }
            }
        }
    }

    /**
     * Called when an update is published.
     *
     * @param chunks data chunks
     */
    @Override
    protected void process(final List<ModelReaderStatus> chunks) {

        if (!chunks.isEmpty()) {
            for (final ModelReaderStatus stat : chunks) {
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

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "CanvasModelReader";
    }
}
