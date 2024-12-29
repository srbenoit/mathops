package dev.mathops.app.canvas.calls;

import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.parser.json.JSONObject;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
public final class ApiV1Sections extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 448577128785146076L;

    /** Action command. */
    private static final String CMD_EXECUTE = "EXECUTE";

    /** The Canvas API object. */
    private final CanvasApi api;

    /** The course ID. */
    private final JTextField course;

    /** The checkbox to select inclusion of students. */
    private final JCheckBox includeStudents;

    /** The checkbox to select inclusion of avatar URL. */
    private final JCheckBox includeAvatarUrl;

    /** The checkbox to select inclusion of enrollments. */
    private final JCheckBox includeEnrollments;

    /** The checkbox to select inclusion of total students. */
    private final JCheckBox includeTotalStudents;

    /** The checkbox to select inclusion of pass-back status. */
    private final JCheckBox includePassbackStatus;

    /** The checkbox to select inclusion of permissions. */
    private final JCheckBox includePermissions;

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
    public ApiV1Sections(final CanvasApi theApi) {

        super(new StackedBorderLayout());

        this.api = theApi;

        setBorder(BorderFactory.createEtchedBorder());

        final JLabel[] labels1 = new JLabel[2];

        labels1[0] = new JLabel("Course:");
        labels1[1] = new JLabel("Include:");

        UIUtilities.makeLabelsSameSizeRightAligned(labels1);

        this.course = new JTextField(10);

        this.includeStudents = new JCheckBox("students");
        this.includeAvatarUrl = new JCheckBox("avatar_url");
        this.includeEnrollments = new JCheckBox("enrollments");
        this.includeTotalStudents = new JCheckBox("total_students");
        this.includePassbackStatus = new JCheckBox("passback_status");
        this.includePermissions = new JCheckBox("permissions");

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
        flow1.add(this.course);
        add(flow1, StackedBorderLayout.NORTH);

        //

        final JPanel includes = new JPanel(new StackedBorderLayout());
        includes.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        final JPanel includesLbl = new JPanel(new StackedBorderLayout());
        includesLbl.add(labels1[1], StackedBorderLayout.NORTH);
        includes.add(includesLbl, StackedBorderLayout.WEST);
        final JPanel includesItems = new JPanel(new StackedBorderLayout());
        includesItems.setBorder(BorderFactory.createEmptyBorder(0, 6, 3, 3));
        includes.add(includesItems, StackedBorderLayout.CENTER);

        includesItems.add(this.includeStudents, StackedBorderLayout.NORTH);
        includesItems.add(this.includeAvatarUrl, StackedBorderLayout.NORTH);
        includesItems.add(this.includeEnrollments, StackedBorderLayout.NORTH);
        includesItems.add(this.includeTotalStudents, StackedBorderLayout.NORTH);
        includesItems.add(this.includePassbackStatus, StackedBorderLayout.NORTH);
        includesItems.add(this.includePermissions, StackedBorderLayout.NORTH);
        add(includes, StackedBorderLayout.NORTH);

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
            new ApiV1SectionsWorker(this).execute();
        }
    }

    /**
     * A worker to execute the Canvas query and present the results.
     */
    private static final class ApiV1SectionsWorker extends SwingWorker<ApiResult, String> {

        /** The owner. */
        private final ApiV1Sections owner;

        /** The request string. */
        private final String request;

        /**
         * Constructs a new {@code ApiV1SectionsWorker}.
         *
         * @param theOwner the owner
         */
        private ApiV1SectionsWorker(final ApiV1Sections theOwner) {

            super();

            // The constructor is executed from the AWT event thread, so it can update UI state

            this.owner = theOwner;

            final StringBuilder builder = new StringBuilder(200);
            char symbol = '?';

            final String courseId = theOwner.course.getText();

            builder.append("courses/");
            builder.append(courseId);
            builder.append("/sections");

            if (theOwner.includeStudents.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=students");
                symbol = '&';
            }
            if (theOwner.includeAvatarUrl.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=avarat_url");
                symbol = '&';
            }
            if (theOwner.includeEnrollments.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=enrollments");
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
            if (theOwner.includePermissions.isSelected()) {
                builder.append(symbol);
                builder.append("include[]=permissions");
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
