package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.checkin.CourseNumbers;
import dev.mathops.app.checkin.DataCheckInAttempt;
import dev.mathops.app.checkin.DataCourseExams;
import dev.mathops.app.checkin.DataExamStatus;
import dev.mathops.app.checkin.DataNonCourseExams;
import dev.mathops.app.checkin.LogicCheckIn;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A card panel that allows an administrator to issue an exam to a testing center machine manually.
 */
final class TestingIssueCard extends AdminPanelBase implements ActionListener, FocusListener {

    /** An action command. */
    private static final String STU = "STU";

    /** An action command. */
    private static final String ELIG = "ELIG";

    /** An action command. */
    private static final String UNIT_1 = "Unit 1";

    /** An action command. */
    private static final String UNIT_2 = "Unit 2";

    /** An action command. */
    private static final String UNIT_3 = "Unit 3";

    /** An action command. */
    private static final String UNIT_4 = "Unit 4";

    /** An action command. */
    private static final String FINAL = "Final";

    /** An action command. */
    private static final String MASTERY = "Mastery";

    /** An action command. */
    private static final String CHALLENGE= "Challenge";

    /** A commonly used integer. */
    static final Integer ZERO = Integer.valueOf(0);

    /** A commonly used integer. */
    static final Integer ONE = Integer.valueOf(1);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6444481681579133884L;

    /** The main window frame. */
    private final JFrame frame;

    /** The data cache. */
    private final Cache cache;

    /** The check-in logic. */
    private final LogicCheckIn logic;

    /** The student ID field. */
    private final JTextField studentIdField;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The student status display field. */
    private final JLabel studentStatusDisplay;

    /** The button to toggle enforcement of eligibility checks. */
    private final JCheckBox enforceEligible;

    /** Map from exam key top its button panel. */
    private final Map<String, ExamButtonPane> examButtons;

    /**
     * Constructs a new {@code TestingIssueCard}.
     *
     * @param theCache         the data cache
     * @param theFrame         the main window frame
     */
    TestingIssueCard(final Cache theCache, final JFrame theFrame) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_CYAN);
        final Border border = getBorder();
        panel.setBorder(border);

        setBackground(Skin.LT_CYAN);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border margin = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder mainBorder = BorderFactory.createCompoundBorder(bevel, margin);
        setBorder(mainBorder);
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;
        this.frame = theFrame;
        final ZonedDateTime now = ZonedDateTime.now();
        this.logic = new LogicCheckIn(theCache, now);
        this.examButtons = new HashMap<>(40);

        final JLabel issueExamHdr = makeHeader("Issue Exam", false);
        panel.add(issueExamHdr, BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new StackedBorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_CYAN);
        panel.add(center, BorderLayout.CENTER);

        // Top row: Student ID entry
        final JPanel north = new JPanel(new BorderLayout(0, 0));
        north.setBackground(Skin.OFF_WHITE_CYAN);

        // Center: grid of buttons just like check-in
        final JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Skin.OFF_WHITE_CYAN);

        // Bottom: Checkbox to allow exams to be issued for which student is not eligible
        final JPanel south = new JPanel(new BorderLayout(10, 10));
        south.setBackground(Skin.OFF_WHITE_CYAN);

        center.add(north, StackedBorderLayout.NORTH);
        center.add(grid, StackedBorderLayout.NORTH);
        center.add(south, StackedBorderLayout.SOUTH);

        //

        final JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdLabel.setFont(Skin.MEDIUM_HEADER_15_FONT);
        studentIdLabel.setForeground(Skin.LABEL_COLOR);
        studentIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        this.studentIdField = new JTextField(12);
        this.studentIdField.setBackground(Skin.FIELD_BG);
        this.studentIdField.setActionCommand(STU);
        this.studentIdField.addActionListener(this);
        this.studentIdField.addFocusListener(this);

        this.studentNameDisplay = new JLabel(CoreConstants.SPC);
        this.studentNameDisplay.setFont(Skin.MEDIUM_HEADER_15_FONT);
        this.studentNameDisplay.setForeground(Skin.ERROR_COLOR);
        final Dimension pref = this.studentIdField.getPreferredSize();
        this.studentNameDisplay.setPreferredSize(new Dimension(pref.width * 3, pref.height));

        final JPanel studentFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        final Border bottomPad = BorderFactory.createEmptyBorder(0, 0, 5, 0);
        studentFlow.setBorder(bottomPad);
        studentFlow.setBackground(Skin.OFF_WHITE_CYAN);
        studentFlow.add(studentIdLabel);
        studentFlow.add(this.studentIdField);
        studentFlow.add(new JLabel("     "));
        studentFlow.add(this.studentNameDisplay);

        north.add(studentFlow, BorderLayout.PAGE_START);

        this.studentStatusDisplay = new JLabel(CoreConstants.SPC);
        this.studentStatusDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        this.studentStatusDisplay.setFont(Skin.MEDIUM_15_FONT);
        north.add(this.studentStatusDisplay, BorderLayout.PAGE_END);

        // Grid of exam buttons
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        final Border courseMargin = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        final MatteBorder courseLineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM);
        final CompoundBorder courseBorder = BorderFactory.createCompoundBorder(courseLineBorder, courseMargin);
        final Border outline = BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM);

        c.gridx = 0;

        c.gridy = 0;
        final JPanel header117 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header117.setBorder(outline);
        header117.setBackground(Skin.WHITE);
        final JLabel title117 = new JLabel(RawRecordConstants.MATH117);
        title117.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title117.setForeground(Skin.LABEL_COLOR2);
        header117.add(title117);
        grid.add(header117, c);

        c.gridy = 1;
        c.weighty = 1.0;
        final ExamButtonPane m117u1 = new ExamButtonPane(UNIT_1, this, "M 117-1");
        this.examButtons.put("M 117-1", m117u1);
        grid.add(m117u1, c);
        c.gridy = 2;
        final ExamButtonPane m117u2 = new ExamButtonPane(UNIT_2, this, "M 117-2");
        this.examButtons.put("M 117-2", m117u2);
        grid.add(m117u2, c);
        c.gridy = 3;
        final ExamButtonPane m117u3 = new ExamButtonPane(UNIT_3, this, "M 117-3");
        this.examButtons.put("M 117-3", m117u3);
        grid.add(m117u3, c);
        c.gridy = 4;
        final ExamButtonPane m117u4 = new ExamButtonPane(UNIT_4, this, "M 117-4");
        this.examButtons.put("M 117-4", m117u4);
        grid.add(m117u4, c);
        c.gridy = 5;
        final ExamButtonPane m117u5 = new ExamButtonPane(FINAL, this, "M 117-5");
        this.examButtons.put("M 117-5", m117u5);
        grid.add(m117u5, c);
        c.gridy = 6;
        final ExamButtonPane m117ma = new ExamButtonPane(MASTERY, this, "M 117-M");
        this.examButtons.put("M 117-M", m117ma);
        grid.add(m117ma, c);
        c.gridy = 7;
        c.weighty = 0.0;
        final JPanel spc117 = new JPanel();
        spc117.setBackground(Skin.OFF_WHITE_CYAN);
        spc117.setBorder(courseBorder);
        grid.add(spc117, c);
        c.gridy = 8;
        c.weighty = 1.0;
        final ExamButtonPane m117ch = new ExamButtonPane(CHALLENGE, this, "M 117-C");
        this.examButtons.put("M 117-C", m117ch);
        grid.add(m117ch, c);
        c.weighty = 0.0;

        c.gridx = 1;

        c.gridy = 0;
        final JPanel header118 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header118.setBorder(outline);
        header118.setBackground(Skin.WHITE);
        final JLabel title118 = new JLabel(RawRecordConstants.MATH118);
        title118.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title118.setForeground(Skin.LABEL_COLOR2);
        header118.add(title118);
        grid.add(header118, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m118u1 = new ExamButtonPane(UNIT_1, this, "M 118-1");
        this.examButtons.put("M 118-1", m118u1);
        grid.add(m118u1, c);
        c.gridy = 2;
        final ExamButtonPane m118u2 = new ExamButtonPane(UNIT_2, this, "M 118-2");
        this.examButtons.put("M 118-2", m118u2);
        grid.add(m118u2, c);
        c.gridy = 3;
        final ExamButtonPane m118u3 = new ExamButtonPane(UNIT_3, this, "M 118-3");
        this.examButtons.put("M 118-3", m118u3);
        grid.add(m118u3, c);
        c.gridy = 4;
        final ExamButtonPane m118u4 = new ExamButtonPane(UNIT_4, this, "M 118-4");
        this.examButtons.put("M 118-4", m118u4);
        grid.add(m118u4, c);
        c.gridy = 5;
        final ExamButtonPane m118u5 = new ExamButtonPane(FINAL, this, "M 118-5");
        this.examButtons.put("M 118-5", m118u5);
        grid.add(m118u5, c);
        c.gridy = 6;
        final ExamButtonPane m118ma = new ExamButtonPane(MASTERY, this, "M 118-M");
        this.examButtons.put("M 118-M", m118ma);
        grid.add(m118ma, c);
        c.gridy = 7;
        c.weighty = 0.0;
        final JPanel spc118 = new JPanel();
        spc118.setBackground(Skin.OFF_WHITE_CYAN);
        spc118.setBorder(courseBorder);
        grid.add(spc118, c);
        c.gridy = 8;
        c.weighty = 1.0;
        final ExamButtonPane m118ch = new ExamButtonPane(CHALLENGE, this, "M 118-C");
        this.examButtons.put("M 118-C", m118ch);
        grid.add(m118ch, c);
        c.weighty = 0.0;

        c.gridx = 2;

        c.gridy = 0;
        final JPanel header124 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header124.setBorder(outline);
        header124.setBackground(Skin.WHITE);
        final JLabel title124 = new JLabel(RawRecordConstants.MATH124);
        title124.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title124.setForeground(Skin.LABEL_COLOR2);
        header124.add(title124);
        grid.add(header124, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m124u1 = new ExamButtonPane(UNIT_1, this, "M 124-1");
        this.examButtons.put("M 124-1", m124u1);
        grid.add(m124u1, c);
        c.gridy = 2;
        final ExamButtonPane m124u2 = new ExamButtonPane(UNIT_2, this, "M 124-2");
        this.examButtons.put("M 124-2", m124u2);
        grid.add(m124u2, c);
        c.gridy = 3;
        final ExamButtonPane m124u3 = new ExamButtonPane(UNIT_3, this, "M 124-3");
        this.examButtons.put("M 124-3", m124u3);
        grid.add(m124u3, c);
        c.gridy = 4;
        final ExamButtonPane m124u4 = new ExamButtonPane(UNIT_4, this, "M 124-4");
        this.examButtons.put("M 124-4", m124u4);
        grid.add(m124u4, c);
        c.gridy = 5;
        final ExamButtonPane m124u5 = new ExamButtonPane(FINAL, this, "M 124-5");
        this.examButtons.put("M 124-5", m124u5);
        grid.add(m124u5, c);
        c.gridy = 6;
        final ExamButtonPane m124ma = new ExamButtonPane(MASTERY, this, "M 124-M");
        this.examButtons.put("M 124-M", m124ma);
        grid.add(m124ma, c);
        c.gridy = 7;
        c.weighty = 0.0;
        final JPanel spc124 = new JPanel();
        spc124.setBackground(Skin.OFF_WHITE_CYAN);
        spc124.setBorder(courseBorder);
        grid.add(spc124, c);
        c.gridy = 8;
        c.weighty = 1.0;
        final ExamButtonPane m124ch = new ExamButtonPane(CHALLENGE, this, "M 124-C");
        this.examButtons.put("M 124-C", m124ch);
        grid.add(m124ch, c);
        c.weighty = 0.0;

        c.gridx = 3;

        c.gridy = 0;
        final JPanel header125 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header125.setBorder(outline);
        header125.setBackground(Skin.WHITE);
        final JLabel title125 = new JLabel(RawRecordConstants.MATH125);
        title125.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title125.setForeground(Skin.LABEL_COLOR2);
        header125.add(title125);
        grid.add(header125, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m125u1 = new ExamButtonPane(UNIT_1, this, "M 125-1");
        this.examButtons.put("M 125-1", m125u1);
        grid.add(m125u1, c);
        c.gridy = 2;
        final ExamButtonPane m125u2 = new ExamButtonPane(UNIT_2, this, "M 125-2");
        this.examButtons.put("M 125-2", m125u2);
        grid.add(m125u2, c);
        c.gridy = 3;
        final ExamButtonPane m125u3 = new ExamButtonPane(UNIT_3, this, "M 125-3");
        this.examButtons.put("M 125-3", m125u3);
        grid.add(m125u3, c);
        c.gridy = 4;
        final ExamButtonPane m125u4 = new ExamButtonPane(UNIT_4, this, "M 125-4");
        this.examButtons.put("M 125-4", m125u4);
        grid.add(m125u4, c);
        c.gridy = 5;
        final ExamButtonPane m125u5 = new ExamButtonPane(FINAL, this, "M 125-5");
        this.examButtons.put("M 125-5", m125u5);
        grid.add(m125u5, c);
        c.gridy = 6;
        final ExamButtonPane m125ma = new ExamButtonPane(MASTERY, this, "M 125-M");
        this.examButtons.put("M 125-M", m125ma);
        grid.add(m125ma, c);
        c.gridy = 7;
        c.weighty = 0.0;
        final JPanel spc125 = new JPanel();
        spc125.setBackground(Skin.OFF_WHITE_CYAN);
        spc125.setBorder(courseBorder);
        grid.add(spc125, c);
        c.gridy = 8;
        c.weighty = 1.0;
        final ExamButtonPane m125ch = new ExamButtonPane(CHALLENGE, this, "M 125-C");
        this.examButtons.put("M 125-C", m125ch);
        grid.add(m125ch, c);
        c.weighty = 0.0;

        c.gridx = 4;

        c.gridy = 0;
        final JPanel header126 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header126.setBorder(outline);
        header126.setBackground(Skin.WHITE);
        final JLabel title126 = new JLabel(RawRecordConstants.MATH124);
        title126.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title126.setForeground(Skin.LABEL_COLOR2);
        header126.add(title126);
        grid.add(header126, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m126u1 = new ExamButtonPane(UNIT_1, this, "M 126-1");
        this.examButtons.put("M 126-1", m126u1);
        grid.add(m126u1, c);
        c.gridy = 2;
        final ExamButtonPane m126u2 = new ExamButtonPane(UNIT_2, this, "M 126-2");
        this.examButtons.put("M 126-2", m126u2);
        grid.add(m126u2, c);
        c.gridy = 3;
        final ExamButtonPane m126u3 = new ExamButtonPane(UNIT_3, this, "M 126-3");
        this.examButtons.put("M 126-3", m126u3);
        grid.add(m126u3, c);
        c.gridy = 4;
        final ExamButtonPane m126u4 = new ExamButtonPane(UNIT_4, this, "M 126-4");
        this.examButtons.put("M 126-4", m126u4);
        grid.add(m126u4, c);
        c.gridy = 5;
        final ExamButtonPane m126u5 = new ExamButtonPane(FINAL, this, "M 126-5");
        this.examButtons.put("M 126-5", m126u5);
        grid.add(m126u5, c);
        c.gridy = 6;
        final ExamButtonPane m126ma = new ExamButtonPane(MASTERY, this, "M 126-M");
        this.examButtons.put("M 126-M", m126ma);
        grid.add(m126ma, c);
        c.gridy = 7;
        c.weighty = 0.0;
        final JPanel spc126 = new JPanel();
        spc126.setBackground(Skin.OFF_WHITE_CYAN);
        spc126.setBorder(courseBorder);
        grid.add(spc126, c);
        c.gridy = 8;
        c.weighty = 1.0;
        final ExamButtonPane m126ch = new ExamButtonPane(CHALLENGE, this, "M 126-C");
        this.examButtons.put("M 126-C", m126ch);
        grid.add(m126ch, c);
        c.weighty = 0.0;

        c.gridx = 5;

        c.gridy = 0;
        final JPanel vgap1 = new JPanel();
        vgap1.setBackground(Skin.OFF_WHITE_CYAN);
        final Border padding = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        vgap1.setBorder(padding);
        grid.add(vgap1, c);

        c.gridx = 6;

        c.gridy = 0;
        final JPanel headerTut = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        headerTut.setBorder(outline);
        headerTut.setBackground(Skin.WHITE);
        final JLabel titleTut = new JLabel("Tutorials");
        titleTut.setFont(Skin.MEDIUM_HEADER_15_FONT);
        titleTut.setForeground(Skin.LABEL_COLOR2);
        headerTut.add(titleTut);
        grid.add(headerTut, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane elm = new ExamButtonPane("ELM Exam", this, "M 100T-4");
        this.examButtons.put("M 100T-4", elm);
        grid.add(elm, c);
        c.gridy = 2;
        final ExamButtonPane pre117 = new ExamButtonPane("Algebra I", this, "M 1170-4");
        this.examButtons.put("M 1170-4", pre117);
        grid.add(pre117, c);
        c.gridy = 3;
        final ExamButtonPane pre118 = new ExamButtonPane("Algebra II", this, "M 1180-4");
        this.examButtons.put("M 1180-4", pre118);
        grid.add(pre118, c);
        c.gridy = 4;
        final ExamButtonPane pre124 = new ExamButtonPane("Functions", this, "M 1240-4");
        this.examButtons.put("M 1240-4", pre124);
        grid.add(pre124, c);
        c.gridy = 5;
        final ExamButtonPane pre125 = new ExamButtonPane("Trig. I", this, "M 1250-4");
        this.examButtons.put("M 1250-4", pre125);
        grid.add(pre125, c);
        c.gridy = 6;
        c.gridheight = 2;
        final Insets orig = c.insets;
        c.insets = new Insets(0, 0, 12, 0);
        final ExamButtonPane pre126 = new ExamButtonPane("Trig. II", this, "M 1260-4");
        this.examButtons.put("M 1260-4", pre126);
        grid.add(pre126, c);
        c.gridheight = 1;
        c.insets = orig;

        c.gridx = 7;

        c.gridy = 0;
        final JPanel headerOther = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        headerOther.setBorder(outline);
        headerOther.setBackground(Skin.WHITE);
        final JLabel titleOther = new JLabel("Other");
        titleOther.setFont(Skin.MEDIUM_HEADER_15_FONT);
        titleOther.setForeground(Skin.LABEL_COLOR2);
        headerOther.add(titleOther);
        grid.add(headerOther, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane users = new ExamButtonPane("User's Exam", this, "M 100U-1");
        this.examButtons.put("M 100U-1", users);
        grid.add(users, c);
        c.gridy = 2;
        final ExamButtonPane placement = new ExamButtonPane("Placement", this, "M 100P-1");
        this.examButtons.put("M 100P-1", placement);
        grid.add(placement, c);

        // Bottom panel

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonFlow.setBackground(Skin.OFF_WHITE_CYAN);

        this.enforceEligible = new JCheckBox("Enforce Eligibility Rules");
        this.enforceEligible.setFont(Skin.BIG_BUTTON_16_FONT);
        this.enforceEligible.setSelected(true);
        this.enforceEligible.addActionListener(this);
        this.enforceEligible.setActionCommand(ELIG);
        buttonFlow.add(this.enforceEligible);

        south.add(buttonFlow, BorderLayout.PAGE_START);

        try {
            if (!this.logic.isInitialized()) {
                this.studentStatusDisplay.setText("Unable to initialize available exams logic.");
            }
        } catch (final SQLException ex) {
            this.studentStatusDisplay.setText("Unable to initialize available exams logic.");
        }
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (STU.equals(cmd) || ELIG.equals(cmd)) {
            processStudentId();
        } else {
            final boolean check = this.enforceEligible.isSelected();
            final String stuId = this.studentIdField.getText();
            final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);

            switch (cmd) {
                case "M 117-1" -> startCourseExam(cleanStu, RawRecordConstants.M117, 1, check);
                case "M 117-2" -> startCourseExam(cleanStu, RawRecordConstants.M117, 2, check);
                case "M 117-3" -> startCourseExam(cleanStu, RawRecordConstants.M117, 3, check);
                case "M 117-4" -> startCourseExam(cleanStu, RawRecordConstants.M117, 4, check);
                case "M 117-5" -> startCourseExam(cleanStu, RawRecordConstants.M117, 5, check);
                case "M 117-M" -> startMasteryExam(cleanStu, RawRecordConstants.MATH117, check);
                case "M 117-C" -> startChallengeExam(cleanStu, ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, check);
                case "M 118-1" -> startCourseExam(cleanStu, RawRecordConstants.M118, 1, check);
                case "M 118-2" -> startCourseExam(cleanStu, RawRecordConstants.M118, 2, check);
                case "M 118-3" -> startCourseExam(cleanStu, RawRecordConstants.M118, 3, check);
                case "M 118-4" -> startCourseExam(cleanStu, RawRecordConstants.M118, 4, check);
                case "M 118-5" -> startCourseExam(cleanStu, RawRecordConstants.M118, 5, check);
                case "M 118-M" -> startMasteryExam(cleanStu, RawRecordConstants.MATH118, check);
                case "M 118-C" -> startChallengeExam(cleanStu, ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, check);
                case "M 124-1" -> startCourseExam(cleanStu, RawRecordConstants.M124, 1, check);
                case "M 124-2" -> startCourseExam(cleanStu, RawRecordConstants.M124, 2, check);
                case "M 124-3" -> startCourseExam(cleanStu, RawRecordConstants.M124, 3, check);
                case "M 124-4" -> startCourseExam(cleanStu, RawRecordConstants.M124, 4, check);
                case "M 124-5" -> startCourseExam(cleanStu, RawRecordConstants.M124, 5, check);
                case "M 124-M" -> startMasteryExam(cleanStu, RawRecordConstants.MATH124, check);
                case "M 124-C" -> startChallengeExam(cleanStu, ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, check);
                case "M 125-1" -> startCourseExam(cleanStu, RawRecordConstants.M125, 1, check);
                case "M 125-2" -> startCourseExam(cleanStu, RawRecordConstants.M125, 2, check);
                case "M 125-3" -> startCourseExam(cleanStu, RawRecordConstants.M125, 3, check);
                case "M 125-4" -> startCourseExam(cleanStu, RawRecordConstants.M125, 4, check);
                case "M 125-5" -> startCourseExam(cleanStu, RawRecordConstants.M125, 5, check);
                case "M 125-M" -> startMasteryExam(cleanStu, RawRecordConstants.MATH125, check);
                case "M 125-C" -> startChallengeExam(cleanStu, ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, check);
                case "M 126-1" -> startCourseExam(cleanStu, RawRecordConstants.M126, 1, check);
                case "M 126-2" -> startCourseExam(cleanStu, RawRecordConstants.M126, 2, check);
                case "M 126-3" -> startCourseExam(cleanStu, RawRecordConstants.M126, 3, check);
                case "M 126-4" -> startCourseExam(cleanStu, RawRecordConstants.M126, 4, check);
                case "M 126-5" -> startCourseExam(cleanStu, RawRecordConstants.M126, 5, check);
                case "M 126-M" -> startMasteryExam(cleanStu, RawRecordConstants.MATH126, check);
                case "M 126-C" -> startChallengeExam(cleanStu, ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, check);
                case "M 100T-4" -> startCourseExam(cleanStu, RawRecordConstants.M100T, 4, check);
                case "M 1170-4" -> startCourseExam(cleanStu, RawRecordConstants.M1170, 4, check);
                case "M 1180-4" -> startCourseExam(cleanStu, RawRecordConstants.M1180, 4, check);
                case "M 1240-4" -> startCourseExam(cleanStu, RawRecordConstants.M1240, 4, check);
                case "M 1250-4" -> startCourseExam(cleanStu, RawRecordConstants.M1250, 4, check);
                case "M 1260-4" -> startCourseExam(cleanStu, RawRecordConstants.M1260, 4, check);
                case "M 100U-1" -> startUsersExam(cleanStu, check);
                case "M 100P-1" -> startPlacementTool(cleanStu, check);
                case null, default -> Log.warning("Unrecognized action command: ", cmd);
            }
        }
    }

    /**
     * Attempts to start a course exam.
     *
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param unit     the unit
     * @param check    true to enable eligibility checks
     */
    private void startCourseExam(final String stuId, final String courseId, final int unit, final boolean check) {

        try {
            final Integer unitObj = Integer.valueOf(unit);
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache, courseId, unitObj,
                    unit == 5 ? RawStexam.FINAL_EXAM : RawStexam.UNIT_EXAM);

            if (examRec != null) {
                new StartExamDialog(this.cache, this.frame, stuId, courseId, unit, examRec.version,
                        check).setVisible(true);
                reset();
            }
        } catch (final SQLException ex) {
            Log.warning("Error querying selected exam.", ex);
        }
    }

    /**
     * Attempts to start a mastery exam.
     *
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param check    true to enable eligibility checks
     */
    private void startMasteryExam(final String stuId, final String courseId, final boolean check) {

        // TODO:
    }

    /**
     * Attempts to start a challenge exam.
     *
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param check    true to enable eligibility checks
     */
    private void startChallengeExam(final String stuId, final String courseId, final boolean check) {

        Log.info("Attempting to start challenge exam.");

        try {
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache, courseId, ZERO, "CH");

            if (examRec == null) {
                Log.warning("Could not find an exam of type 'CH' for ", courseId);
            } else {
                new StartExamDialog(this.cache, this.frame, stuId, courseId, 0, examRec.version,
                        check).setVisible(true);
                reset();
            }
        } catch (final SQLException ex) {
            Log.warning("Error querying selected exam.", ex);
        }
    }

    /**
     * Attempts to start a User's exam.
     *
     * @param stuId the student ID
     * @param check true to enable eligibility checks
     */
    private void startUsersExam(final String stuId, final boolean check) {

        try {
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache, RawRecordConstants.M100U, ONE,
                    "Q");

            if (examRec != null) {
                new StartExamDialog(this.cache, this.frame, stuId, RawRecordConstants.M100U, 1, examRec.version,
                        check).setVisible(true);
                reset();
            }
        } catch (final SQLException ex) {
            Log.warning("Error querying selected exam.", ex);
        }
    }

    /**
     * Attempts to start a User's exam.
     *
     * @param stuId the student ID
     * @param check true to enable eligibility checks
     */
    private void startPlacementTool(final String stuId, final boolean check) {

        // We want the "MPTTC" version in the testing center.

        new StartExamDialog(this.cache, this.frame, stuId, RawRecordConstants.M100P, 1, "MPTTC",
                check).setVisible(true);
        reset();
    }

    /**
     * Called when a text field gains focus.
     *
     * @param e the focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when a text field loses focus.
     *
     * @param e the focus event
     */
    @Override
    public void focusLost(final FocusEvent e) {

        if (e.getComponent() == this.studentIdField) {
            if (this.studentIdField.getText().isEmpty()) {
                this.studentIdField.requestFocus();
            } else {
                processStudentId();
            }
        }
    }

    /**
     * Sets focus.
     */
    public void focus() {

        this.studentIdField.requestFocus();
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        this.studentIdField.setText(CoreConstants.EMPTY);
        this.studentIdField.setBackground(Skin.FIELD_BG);

        this.studentNameDisplay.setText(CoreConstants.SPC);
        this.studentIdField.requestFocus();

        for (final ExamButtonPane pane : this.examButtons.values()) {
            pane.disableButton();
        }
    }

    /**
     * Called when a student ID is entered and "Return" is pressed in that field (typically, by the bar code scanner
     * reading a student ID card).
     */
    private void processStudentId() {

        this.studentNameDisplay.setText(CoreConstants.SPC);

        final String stuId = this.studentIdField.getText();

        final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                .replace(CoreConstants.DASH, CoreConstants.EMPTY);

        final String foundFirstName;
        final String foundLastName;
        final String foundPrefName;
        final String sql1 = "SELECT first_name, last_name, pref_name FROM student WHERE stu_id=?";

        this.studentStatusDisplay.setText(CoreConstants.SPC);

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql1)) {
            ps.setString(1, cleanStu);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundFirstName = rs.getString(1);
                    foundLastName = rs.getString(2);
                    foundPrefName = rs.getString(3);
                    this.studentIdField.setBackground(Skin.FIELD_BG);

                    final String first = foundFirstName == null ? CoreConstants.EMPTY : foundFirstName.trim();
                    final String last = foundLastName == null ? CoreConstants.EMPTY : foundLastName.trim();
                    final String pref = foundPrefName == null ? CoreConstants.EMPTY : foundPrefName.trim();

                    if (pref.isEmpty() || pref.equals(first)) {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last);
                    } else {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last + " (" + pref + ")");
                    }

                    studentFound(cleanStu);
                } else {
                    this.studentStatusDisplay.setText("Student not found.");
                    this.studentIdField.setBackground(Skin.FIELD_ERROR_BG);
                }
            }
        } catch (final SQLException ex) {
            if (ex.getMessage() == null) {
                this.studentStatusDisplay.setText("Error querying student table: " + ex.getClass().getSimpleName());
            } else {
                this.studentStatusDisplay.setText("Error querying student table: " + ex.getMessage());
            }
        }
    }

    /**
     * Called when the entered student ID has been verified. This method will query the student's registrations,
     * determine what exams they are eligible for, and will enable the corresponding exam buttons.
     *
     * @param cleanedStuId the student ID
     * @throws SQLException if there is an error accessing the database
     */
    private void studentFound(final String cleanedStuId) throws SQLException{

        final boolean enforceElig = this.enforceEligible.isSelected();
        final DataCheckInAttempt info = this.logic.performCheckInLogic(cleanedStuId, enforceElig);

        if (info == null) {
            this.studentStatusDisplay.setText("Unable to determine eligible exams.");
        } else if (info.error == null) {
            // Present the complete list of exams on the screen with unavailable exams dimmed, and available exams lit.

            final DataCourseExams course17 = info.getCourseExams(CourseNumbers.MATH117);
            final ExamButtonPane pane117u1 = this.examButtons.get("M 117-1");
            updateCourseExamButton(pane117u1, course17.unit1Exam);
            final ExamButtonPane pane117u2 = this.examButtons.get("M 117-2");
            updateCourseExamButton(pane117u2, course17.unit2Exam);
            final ExamButtonPane pane117u3 = this.examButtons.get("M 117-3");
            updateCourseExamButton(pane117u3, course17.unit3Exam);
            final ExamButtonPane pane117u4 = this.examButtons.get("M 117-4");
            updateCourseExamButton(pane117u4, course17.unit4Exam);
            final ExamButtonPane pane117u5 = this.examButtons.get("M 117-5");
            updateCourseExamButton(pane117u5, course17.finalExam);
            final ExamButtonPane pane117ma = this.examButtons.get("M 117-M");
            updateCourseExamButton(pane117ma, course17.masteryExam);
            final ExamButtonPane pane117ch = this.examButtons.get("M 117-C");
            updateCourseExamButton(pane117ch, course17.challengeExam);

            final DataCourseExams course18 = info.getCourseExams(CourseNumbers.MATH118);
            final ExamButtonPane pane118u1 = this.examButtons.get("M 118-1");
            updateCourseExamButton(pane118u1, course18.unit1Exam);
            final ExamButtonPane pane118u2 = this.examButtons.get("M 118-2");
            updateCourseExamButton(pane118u2, course18.unit2Exam);
            final ExamButtonPane pane118u3 = this.examButtons.get("M 118-3");
            updateCourseExamButton(pane118u3, course18.unit3Exam);
            final ExamButtonPane pane118u4 = this.examButtons.get("M 118-4");
            updateCourseExamButton(pane118u4, course18.unit4Exam);
            final ExamButtonPane pane118u5 = this.examButtons.get("M 118-5");
            updateCourseExamButton(pane118u5, course18.finalExam);
            final ExamButtonPane pane118ma = this.examButtons.get("M 118-M");
            updateCourseExamButton(pane118ma, course18.masteryExam);
            final ExamButtonPane pane118ch = this.examButtons.get("M 118-C");
            updateCourseExamButton(pane118ch, course18.challengeExam);

            final DataCourseExams course24 = info.getCourseExams(CourseNumbers.MATH124);
            final ExamButtonPane pane124u1 = this.examButtons.get("M 124-1");
            updateCourseExamButton(pane124u1, course24.unit1Exam);
            final ExamButtonPane pane124u2 = this.examButtons.get("M 124-2");
            updateCourseExamButton(pane124u2, course24.unit2Exam);
            final ExamButtonPane pane124u3 = this.examButtons.get("M 124-3");
            updateCourseExamButton(pane124u3, course24.unit3Exam);
            final ExamButtonPane pane124u4 = this.examButtons.get("M 124-4");
            updateCourseExamButton(pane124u4, course24.unit4Exam);
            final ExamButtonPane pane124u5 = this.examButtons.get("M 124-5");
            updateCourseExamButton(pane124u5, course24.finalExam);
            final ExamButtonPane pane124ma = this.examButtons.get("M 124-M");
            updateCourseExamButton(pane124ma, course24.masteryExam);
            final ExamButtonPane pane124ch = this.examButtons.get("M 124-C");
            updateCourseExamButton(pane124ch, course24.challengeExam);

            final DataCourseExams course25 = info.getCourseExams(CourseNumbers.MATH125);
            final ExamButtonPane pane125u1 = this.examButtons.get("M 125-1");
            updateCourseExamButton(pane125u1, course25.unit1Exam);
            final ExamButtonPane pane125u2 = this.examButtons.get("M 125-2");
            updateCourseExamButton(pane125u2, course25.unit2Exam);
            final ExamButtonPane pane125u3 = this.examButtons.get("M 125-3");
            updateCourseExamButton(pane125u3, course25.unit3Exam);
            final ExamButtonPane pane125u4 = this.examButtons.get("M 125-4");
            updateCourseExamButton(pane125u4, course25.unit4Exam);
            final ExamButtonPane pane125u5 = this.examButtons.get("M 125-5");
            updateCourseExamButton(pane125u5, course25.finalExam);
            final ExamButtonPane pane125ma = this.examButtons.get("M 125-M");
            updateCourseExamButton(pane125ma, course25.masteryExam);
            final ExamButtonPane pane125ch = this.examButtons.get("M 125-C");
            updateCourseExamButton(pane125ch, course25.challengeExam);

            final DataCourseExams course26 = info.getCourseExams(CourseNumbers.MATH126);
            final ExamButtonPane pane126u1 = this.examButtons.get("M 126-1");
            updateCourseExamButton(pane126u1, course26.unit1Exam);
            final ExamButtonPane pane126u2 = this.examButtons.get("M 126-2");
            updateCourseExamButton(pane126u2, course26.unit2Exam);
            final ExamButtonPane pane126u3 = this.examButtons.get("M 126-3");
            updateCourseExamButton(pane126u3, course26.unit3Exam);
            final ExamButtonPane pane126u4 = this.examButtons.get("M 126-4");
            updateCourseExamButton(pane126u4, course26.unit4Exam);
            final ExamButtonPane pane126u5 = this.examButtons.get("M 126-5");
            updateCourseExamButton(pane126u5, course26.finalExam);
            final ExamButtonPane pane126ma = this.examButtons.get("M 126-M");
            updateCourseExamButton(pane126ma, course26.masteryExam);
            final ExamButtonPane pane126ch = this.examButtons.get("M 126-C");
            updateCourseExamButton(pane126ch, course26.challengeExam);

            final DataNonCourseExams nonCourse = info.nonCourseExams;
            final ExamButtonPane paneElm = this.examButtons.get("M 100T-4");
            updateCourseExamButton(paneElm, nonCourse.elmExam);
            final ExamButtonPane paneTut117 = this.examButtons.get("M 1170-4");
            updateCourseExamButton(paneTut117, nonCourse.precalc117);
            final ExamButtonPane paneTut118 = this.examButtons.get("M 1180-4");
            updateCourseExamButton(paneTut118, nonCourse.precalc118);
            final ExamButtonPane paneTut124 = this.examButtons.get("M 1240-4");
            updateCourseExamButton(paneTut124, nonCourse.precalc124);
            final ExamButtonPane paneTut125 = this.examButtons.get("M 1250-4");
            updateCourseExamButton(paneTut125, nonCourse.precalc125);
            final ExamButtonPane paneTut126 = this.examButtons.get("M 1260-4");
            updateCourseExamButton(paneTut126, nonCourse.precalc126);
            final ExamButtonPane paneUsers = this.examButtons.get("M 100U-1");
            updateCourseExamButton(paneUsers, nonCourse.usersExam);
            final ExamButtonPane paneMpt = this.examButtons.get("M 100P-1");
            updateCourseExamButton(paneMpt, nonCourse.placement);
        } else {
            this.studentStatusDisplay.setText(info.error[0]);
        }
    }

    /**
     * Updates an exam button with the status of that exam.
     *
     * @param button the button to update
     * @param status the exam status
     */
    private void updateCourseExamButton(final ExamButtonPane button, final DataExamStatus status) {

        final HtmlBuilder tooltip = new HtmlBuilder(100);
        boolean newline = false;
        for (final String msg : status.eligibilityOverrides) {
            if (newline) {
                // NOTE: we don't just "addln" each line since the trailing newline on the last item is annoying.
                tooltip.addln();
            }
            tooltip.add(msg);
            newline = true;
        }
        final String tipString = tooltip.toString();
        button.setTooltip(tipString);

        if (status.available) {
            button.enableButton();
            button.setStatusText(Objects.requireNonNullElse(status.note, CoreConstants.SPC));
        } else {
            button.disableButton();
            button.setStatusText(Objects.requireNonNullElse(status.note, CoreConstants.SPC));
            if ("Not Registered".equals(status.note) || "Not Eligible".equals(status.note)) {
                button.indicateNotRegistered();
            }
        }

    }
}
