package dev.mathops.app.canvas.calls;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.parser.json.JSONObject;
import dev.mathops.core.ui.UIUtilities;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.List;

/**
 * An implementation of the "GET /api/v1/courses" call. This call returns the paginated list of active courses for the
 * current user.
 */
public final class ApiV1Courses extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 448577128785146075L;

    /** Action command. */
    private static final String CMD_EXECUTE = "EXECUTE";

    /** The Canvas API object. */
    private final CanvasApi api;

    /** The dropdown to select enrollment type. */
    private final JComboBox<String> enrollmentType;

    /** The dropdown to select enrollment role ID. */
    private final JComboBox<String> enrollmentRoleId;

    /** The dropdown to select enrollment state. */
    private final JComboBox<String> enrollmentState;

    /** The dropdown to select enrollment state. */
    private final JComboBox<String> state;

    /** The checkbox to select exclusion of blueprint courses. */
    private final JCheckBox excludeBlueprint;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeNeedsGradingCount;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeSyllabusBody;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includePublicDescription;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeTotalScores;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeCurrentGradingPeriodScores;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeGradingPeriods;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeTerm;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeAccount;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeCourseProgress;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeSections;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeStorageQuotaUsedMb;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeTotalStudents;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includePassbackStatus;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeFavorites;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeTeachers;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeObservedUsers;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeCourseImage;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeBannerImage;

    /** The checkbox to select inclusion of . */
    private final JCheckBox includeConcluded;

    /** The "execute" button. */
    private final JButton executeBtn;

    /** The constructed request text. */
    private final JTextArea requestText;

    /** The constructed result text. */
    private final JTextArea resultText;

    /**
     * Constructs a new {@code ApiV1Courses}.
     *
     * @param theApi the Canvas API interface
     */
    public ApiV1Courses(final CanvasApi theApi) {

        super(new StackedBorderLayout());

        this.api = theApi;

        setBorder(BorderFactory.createEtchedBorder());

        final JLabel[] labels1 = new JLabel[6];

        labels1[0] = new JLabel("Enrollment Type:");
        labels1[1] = new JLabel("Enrollment Role ID:");
        labels1[2] = new JLabel("Enrollment State:");
        labels1[3] = new JLabel("Exclude Blueprint Courses:");
        labels1[4] = new JLabel("Include:");
        labels1[5] = new JLabel("State:");

        UIUtilities.makeLabelsSameSizeRightAligned(labels1);

        final String[] enrollmentTypes = {"(any)", "teacher", "student", "ta", "observer", "designer"};
        this.enrollmentType = new JComboBox<>(enrollmentTypes);

        final String[] enrollmentRoleIds = {"(any)", "StudentEnrollment", "TeacherEnrollment", "TaEnrollment",
                "ObserverEnrollment", "DesignerEnrollment"};
        this.enrollmentRoleId = new JComboBox<>(enrollmentRoleIds);

        final String[] enrollmentStates = {"(any)", "active", "invited_or_pending", "completed"};
        this.enrollmentState = new JComboBox<>(enrollmentStates);

        final String[] states = {"(default)", "unpublished", "available", "completed", "deleted"};
        this.state = new JComboBox<>(states);

        this.excludeBlueprint = new JCheckBox();

        this.includeNeedsGradingCount = new JCheckBox("needs_grading_count");
        this.includeSyllabusBody = new JCheckBox("syllabus_body");
        this.includePublicDescription = new JCheckBox("public_description");
        this.includeTotalScores = new JCheckBox("total_scores");
        this.includeCurrentGradingPeriodScores = new JCheckBox("current_grading_period_scores");
        this.includeGradingPeriods = new JCheckBox("grading_periods");
        this.includeTerm = new JCheckBox("term");
        this.includeAccount = new JCheckBox("account");
        this.includeCourseProgress = new JCheckBox("course_progress");
        this.includeSections = new JCheckBox("sections");
        this.includeStorageQuotaUsedMb = new JCheckBox("storage_quota_used_mb");
        this.includeTotalStudents = new JCheckBox("total_students");
        this.includePassbackStatus = new JCheckBox("passback_status");
        this.includeFavorites = new JCheckBox("favorites");
        this.includeTeachers = new JCheckBox("teachers");
        this.includeObservedUsers = new JCheckBox("observed_users");
        this.includeCourseImage = new JCheckBox("course_image");
        this.includeBannerImage = new JCheckBox("banner_image");
        this.includeConcluded = new JCheckBox("concluded");

        this.requestText = new JTextArea(3, 30);
        final int fontSize = this.requestText.getFont().getSize();
        final Font newFont = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        this.requestText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        this.requestText.setFont(newFont);
        this.requestText.setEditable(false);
        this.requestText.setBackground(Color.WHITE);

        this.resultText = new JTextArea(3, 30);
        this.resultText.setEditable(false);
        this.resultText.setFont(newFont);
        this.resultText.setBackground(Color.WHITE);
        final JScrollPane resultScroll = new JScrollPane(this.resultText);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow1.add(labels1[0]);
        flow1.add(this.enrollmentType);
        add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow2.add(labels1[1]);
        flow2.add(this.enrollmentRoleId);
        add(flow2, StackedBorderLayout.NORTH);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow3.add(labels1[2]);
        flow3.add(this.enrollmentState);
        add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow4.add(labels1[3]);
        flow4.add(this.excludeBlueprint);
        add(flow4, StackedBorderLayout.NORTH);

        //

        final JPanel includes = new JPanel(new StackedBorderLayout());
        includes.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        final JPanel includesLbl = new JPanel(new StackedBorderLayout());
        includesLbl.add(labels1[4], StackedBorderLayout.NORTH);
        includes.add(includesLbl, StackedBorderLayout.WEST);
        final JPanel includesItems = new JPanel(new StackedBorderLayout());
        includesItems.setBorder(BorderFactory.createEmptyBorder(0, 6, 3, 3));
        includes.add(includesItems, StackedBorderLayout.CENTER);

        includesItems.add(this.includeNeedsGradingCount, StackedBorderLayout.NORTH);
        includesItems.add(this.includeSyllabusBody, StackedBorderLayout.NORTH);
        includesItems.add(this.includePublicDescription, StackedBorderLayout.NORTH);
        includesItems.add(this.includeTotalScores, StackedBorderLayout.NORTH);
        includesItems.add(this.includeCurrentGradingPeriodScores, StackedBorderLayout.NORTH);
        includesItems.add(this.includeGradingPeriods, StackedBorderLayout.NORTH);
        includesItems.add(this.includeTerm, StackedBorderLayout.NORTH);
        includesItems.add(this.includeAccount, StackedBorderLayout.NORTH);
        includesItems.add(this.includeCourseProgress, StackedBorderLayout.NORTH);
        includesItems.add(this.includeSections, StackedBorderLayout.NORTH);
        includesItems.add(this.includeStorageQuotaUsedMb, StackedBorderLayout.NORTH);
        includesItems.add(this.includeTotalStudents, StackedBorderLayout.NORTH);
        includesItems.add(this.includePassbackStatus, StackedBorderLayout.NORTH);
        includesItems.add(this.includeFavorites, StackedBorderLayout.NORTH);
        includesItems.add(this.includeTeachers, StackedBorderLayout.NORTH);
        includesItems.add(this.includeObservedUsers, StackedBorderLayout.NORTH);
        includesItems.add(this.includeCourseImage, StackedBorderLayout.NORTH);
        includesItems.add(this.includeBannerImage, StackedBorderLayout.NORTH);
        includesItems.add(this.includeConcluded, StackedBorderLayout.NORTH);
        add(includes, StackedBorderLayout.NORTH);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow5.add(labels1[5]);
        flow5.add(this.state);
        add(flow5, StackedBorderLayout.NORTH);

        final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        this.executeBtn = new JButton("Execute");
        this.executeBtn.setActionCommand(CMD_EXECUTE);
        this.executeBtn.addActionListener(this);
        flow6.add(this.executeBtn);
        add(flow6, StackedBorderLayout.NORTH);

        final JPanel flow7 = new JPanel(new StackedBorderLayout());
        flow7.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        flow7.add(this.requestText);
        add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = new JPanel(new StackedBorderLayout());
        flow8.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        flow8.add(resultScroll);
        add(flow8, StackedBorderLayout.CENTER);
    }

    /**
     * Called when the "execute" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_EXECUTE.equals(cmd)) {
            this.executeBtn.setEnabled(false);
            this.resultText.setText(CoreConstants.EMPTY);
            new ApiV1CoursesWorker(this).execute();
        }
    }

    /**
     * A worker to execute the Canvas query and present the results.
     */
    private static final class ApiV1CoursesWorker extends SwingWorker<ApiResult, String> {

        /** The owner. */
        private final ApiV1Courses owner;

        /** The request string. */
        private final String request;

        /**
         * Constructs a new {@code ApiV1CoursesWorker}.
         *
         * @param theOwner the owner
         */
        private ApiV1CoursesWorker(final ApiV1Courses theOwner) {

            super();

            // The constructor is executed from the AWT event thread, so it can update UI state

            this.owner = theOwner;

            final StringBuilder builder = new StringBuilder(200);
            char symbol = '?';

            builder.append("courses");

            if (!"(any)".equals(theOwner.enrollmentType.getSelectedItem())) {
                builder.append(symbol);
                builder.append("enrollment_type=");
                builder.append(theOwner.enrollmentType.getSelectedItem());
                symbol = '&';
            }

            if (!"(any)".equals(theOwner.enrollmentRoleId.getSelectedItem())) {
                builder.append(symbol);
                builder.append("enrollment_role_id=");
                builder.append(theOwner.enrollmentRoleId.getSelectedItem());
                symbol = '&';
            }

            if (!"(any)".equals(theOwner.enrollmentState.getSelectedItem())) {
                builder.append(symbol);
                builder.append("enrollment_state=");
                builder.append(theOwner.enrollmentState.getSelectedItem());
                symbol = '&';
            }

            if (theOwner.excludeBlueprint.isSelected()) {
                builder.append(symbol);
                builder.append("exclude_blueprint_courses=true");
                symbol = '&';
            }

            if (theOwner.includeNeedsGradingCount.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=needs_grading_count");
                symbol = '&';
            }
            if (theOwner.includeSyllabusBody.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=syllabus_body");
                symbol = '&';
            }
            if (theOwner.includePublicDescription.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=public_description");
                symbol = '&';
            }
            if (theOwner.includeTotalScores.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=total_scores");
                symbol = '&';
            }
            if (theOwner.includeCurrentGradingPeriodScores.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=current_grading_period_scores");
                symbol = '&';
            }
            if (theOwner.includeGradingPeriods.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=grading_periods");
                symbol = '&';
            }
            if (theOwner.includeTerm.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=term");
                symbol = '&';
            }
            if (theOwner.includeAccount.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=account");
                symbol = '&';
            }
            if (theOwner.includeCourseProgress.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=course_progress");
                symbol = '&';
            }
            if (theOwner.includeSections.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=sections");
                symbol = '&';
            }
            if (theOwner.includeStorageQuotaUsedMb.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=storage_quota_used_mb");
                symbol = '&';
            }
            if (theOwner.includeTotalStudents.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=total_students");
                symbol = '&';
            }
            if (theOwner.includePassbackStatus.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=passback_status");
                symbol = '&';
            }
            if (theOwner.includeFavorites.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=favorites");
                symbol = '&';
            }
            if (theOwner.includeTeachers.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=teachers");
                symbol = '&';
            }
            if (theOwner.includeObservedUsers.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=observed_users");
                symbol = '&';
            }
            if (theOwner.includeCourseImage.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=course_image");
                symbol = '&';
            }
            if (theOwner.includeBannerImage.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=banner_image");
                symbol = '&';
            }
            if (theOwner.includeConcluded.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=concluded");
                symbol = '&';
            }

            if (!"(default)".equals(theOwner.state.getSelectedItem())) {
                builder.append(symbol);
                builder.append("state[]=");
                builder.append(theOwner.state.getSelectedItem());
            }

            this.request = builder.toString();
            theOwner.requestText.setText(this.request);
        }

        /**
         * Runs in a non-AWT thread to perform the task.
         */
        @Override
        public ApiResult doInBackground() {

            final ApiResult result = this.owner.api.paginatedApiCall(this.request, CanvasApi.GET);

            if (result.error == null) {
                if (result.response == null) {
                    final StringBuilder builder = new StringBuilder(100);
                    for (final JSONObject json : result.arrayResponse) {
                        builder.append(json.toJSONFriendly(0));
                        builder.append(CoreConstants.CRLF);
                        builder.append(CoreConstants.CRLF);
                    }
                    publish(builder.toString());
                } else {
                    publish(result.response.toJSONFriendly(0));
                }
            } else {
                publish("ERROR: " + result.error);
            }

            return result;
        }

        /**
         * Called on the AWT event thread after one or more invocations of "publish" have occurred in the
         * {@code doInBackground} method.
         *
         * @param chunks the data published from the background thread
         */
        @Override
        protected void process(final List<String> chunks) {

            final int size = chunks.size();
            if (size > 0) {
                this.owner.resultText.setText(chunks.get(size - 1));
            }
        }

        /**
         * Called when the background task is finished
         */
        @Override
        protected void done() {

            this.owner.executeBtn.setEnabled(true);
        }
    }
}
