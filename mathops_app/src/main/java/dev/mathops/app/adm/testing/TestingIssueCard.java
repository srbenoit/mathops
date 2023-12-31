package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A card panel that allows an administrator to issue an exam to a testing center machine manually.
 */
class TestingIssueCard extends AdminPanelBase implements ActionListener, FocusListener {

    /** An action command. */
    private static final String STU = "STU";

    /** An action command. */
    private static final String ELIG = "ELIG";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6444481681579133884L;

    /** The main window frame. */
    private final JFrame frame;

    /** The data cache. */
    private final Cache cache;

    /** The checkin logic. */
    private final CheckinLogic logic;

    /** The student ID field. */
    private final JTextField studentIdField;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The student status display field. */
    private final JLabel studentStatusDisplay;

    /** The center panel where the grid of exam buttons will be displayed. */
    private final JPanel grid;

    /** The button to toggle enforcement of eligibility checks. */
    private final JCheckBox enforceEligible;

    /** Map from exam key top it's button panel. */
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
        panel.setBorder(getBorder());

        setBackground(Skin.LT_CYAN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;
        this.frame = theFrame;
        this.logic = new CheckinLogic(theCache);
        this.examButtons = new HashMap<>(40);

        panel.add(makeHeader("Issue Exam", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_CYAN);
        panel.add(center, BorderLayout.CENTER);

        // Top row: Student ID entry
        final JPanel north = new JPanel(new BorderLayout(0, 0));
        north.setBackground(Skin.OFF_WHITE_CYAN);

        // Center: grid of buttons just like checkin
        this.grid = new JPanel(new GridBagLayout());
        this.grid.setBackground(Skin.OFF_WHITE_CYAN);

        // Bottom: Checkbox to allow exams to be issued for which student is not eligible
        final JPanel south = new JPanel(new BorderLayout(10, 10));
        south.setBackground(Skin.OFF_WHITE_CYAN);

        center.add(north, BorderLayout.NORTH);
        center.add(this.grid, BorderLayout.CENTER);
        center.add(south, BorderLayout.SOUTH);

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
        studentFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        studentFlow.setBackground(Skin.OFF_WHITE_CYAN);
        studentFlow.add(studentIdLabel);
        studentFlow.add(this.studentIdField);
        studentFlow.add(new JLabel("     "));
        studentFlow.add(this.studentNameDisplay);

        north.add(studentFlow, BorderLayout.NORTH);

        this.studentStatusDisplay = new JLabel(CoreConstants.SPC);
        this.studentStatusDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        this.studentStatusDisplay.setFont(Skin.MEDIUM_15_FONT);
        north.add(this.studentStatusDisplay, BorderLayout.SOUTH);

        // Grid of exam buttons
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;

        c.gridy = 0;
        final JPanel header117 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header117.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        header117.setBackground(Skin.WHITE);
        final JLabel title117 = new JLabel("MATH 117");
        title117.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title117.setForeground(Skin.LABEL_COLOR2);
        header117.add(title117);
        this.grid.add(header117, c);

        c.gridy = 1;
        c.weighty = 1.0;
        final ExamButtonPane m117u1 =
                new ExamButtonPane("Unit 1", this, "M 117-1");
        this.examButtons.put("M 117-1", m117u1);
        this.grid.add(m117u1, c);
        c.gridy = 2;
        final ExamButtonPane m117u2 =
                new ExamButtonPane("Unit 2", this, "M 117-2");
        this.examButtons.put("M 117-2", m117u2);
        this.grid.add(m117u2, c);
        c.gridy = 3;
        final ExamButtonPane m117u3 =
                new ExamButtonPane("Unit 3", this, "M 117-3");
        this.examButtons.put("M 117-3", m117u3);
        this.grid.add(m117u3, c);
        c.gridy = 4;
        final ExamButtonPane m117u4 =
                new ExamButtonPane("Unit 4", this, "M 117-4");
        this.examButtons.put("M 117-4", m117u4);
        this.grid.add(m117u4, c);
        c.gridy = 5;
        final ExamButtonPane m117u5 =
                new ExamButtonPane("Final", this, "M 117-5");
        this.examButtons.put("M 117-5", m117u5);
        this.grid.add(m117u5, c);
        c.gridy = 6;
        c.weighty = 0.0;
        final JPanel spc117 = new JPanel();
        spc117.setBackground(Skin.OFF_WHITE_CYAN);
        spc117.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.grid.add(spc117, c);
        c.gridy = 7;
        c.weighty = 1.0;
        final ExamButtonPane m117ch =
                new ExamButtonPane("Challenge", this, "M 117-C");
        this.examButtons.put("MC117-0", m117ch);
        this.grid.add(m117ch, c);
        c.weighty = 0.0;

        c.gridx = 1;

        c.gridy = 0;
        final JPanel header118 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header118.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        header118.setBackground(Skin.WHITE);
        final JLabel title118 = new JLabel("MATH 118");
        title118.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title118.setForeground(Skin.LABEL_COLOR2);
        header118.add(title118);
        this.grid.add(header118, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m118u1 =
                new ExamButtonPane("Unit 1", this, "M 118-1");
        this.examButtons.put("M 118-1", m118u1);
        this.grid.add(m118u1, c);
        c.gridy = 2;
        final ExamButtonPane m118u2 =
                new ExamButtonPane("Unit 2", this, "M 118-2");
        this.examButtons.put("M 118-2", m118u2);
        this.grid.add(m118u2, c);
        c.gridy = 3;
        final ExamButtonPane m118u3 =
                new ExamButtonPane("Unit 3", this, "M 118-3");
        this.examButtons.put("M 118-3", m118u3);
        this.grid.add(m118u3, c);
        c.gridy = 4;
        final ExamButtonPane m118u4 =
                new ExamButtonPane("Unit 4", this, "M 118-4");
        this.examButtons.put("M 118-4", m118u4);
        this.grid.add(m118u4, c);
        c.gridy = 5;
        final ExamButtonPane m118u5 =
                new ExamButtonPane("Final", this, "M 118-5");
        this.examButtons.put("M 118-5", m118u5);
        this.grid.add(m118u5, c);
        c.gridy = 6;
        c.weighty = 0.0;
        final JPanel spc118 = new JPanel();
        spc118.setBackground(Skin.OFF_WHITE_CYAN);
        spc118.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.grid.add(spc118, c);
        c.gridy = 7;
        c.weighty = 1.0;
        final ExamButtonPane m118ch =
                new ExamButtonPane("Challenge", this, "M 118-C");
        this.examButtons.put("MC118-0", m118ch);
        this.grid.add(m118ch, c);
        c.weighty = 0.0;

        c.gridx = 2;

        c.gridy = 0;
        final JPanel header124 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header124.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        header124.setBackground(Skin.WHITE);
        final JLabel title124 = new JLabel("MATH 124");
        title124.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title124.setForeground(Skin.LABEL_COLOR2);
        header124.add(title124);
        this.grid.add(header124, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m124u1 =
                new ExamButtonPane("Unit 1", this, "M 124-1");
        this.examButtons.put("M 124-1", m124u1);
        this.grid.add(m124u1, c);
        c.gridy = 2;
        final ExamButtonPane m124u2 =
                new ExamButtonPane("Unit 2", this, "M 124-2");
        this.examButtons.put("M 124-2", m124u2);
        this.grid.add(m124u2, c);
        c.gridy = 3;
        final ExamButtonPane m124u3 =
                new ExamButtonPane("Unit 3", this, "M 124-3");
        this.examButtons.put("M 124-3", m124u3);
        this.grid.add(m124u3, c);
        c.gridy = 4;
        final ExamButtonPane m124u4 =
                new ExamButtonPane("Unit 4", this, "M 124-4");
        this.examButtons.put("M 124-4", m124u4);
        this.grid.add(m124u4, c);
        c.gridy = 5;
        final ExamButtonPane m124u5 =
                new ExamButtonPane("Final", this, "M 124-5");
        this.examButtons.put("M 124-5", m124u5);
        this.grid.add(m124u5, c);
        c.gridy = 6;
        c.weighty = 0.0;
        final JPanel spc124 = new JPanel();
        spc124.setBackground(Skin.OFF_WHITE_CYAN);
        spc124.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.grid.add(spc124, c);
        c.gridy = 7;
        c.weighty = 1.0;
        final ExamButtonPane m124ch =
                new ExamButtonPane("Challenge", this, "M 124-C");
        this.examButtons.put("MC124-0", m124ch);
        this.grid.add(m124ch, c);
        c.weighty = 0.0;

        c.gridx = 3;

        c.gridy = 0;
        final JPanel header125 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header125.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        header125.setBackground(Skin.WHITE);
        final JLabel title125 = new JLabel("MATH 125");
        title125.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title125.setForeground(Skin.LABEL_COLOR2);
        header125.add(title125);
        this.grid.add(header125, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m125u1 =
                new ExamButtonPane("Unit 1", this, "M 125-1");
        this.examButtons.put("M 125-1", m125u1);
        this.grid.add(m125u1, c);
        c.gridy = 2;
        final ExamButtonPane m125u2 =
                new ExamButtonPane("Unit 2", this, "M 125-2");
        this.examButtons.put("M 125-2", m125u2);
        this.grid.add(m125u2, c);
        c.gridy = 3;
        final ExamButtonPane m125u3 =
                new ExamButtonPane("Unit 3", this, "M 125-3");
        this.examButtons.put("M 125-3", m125u3);
        this.grid.add(m125u3, c);
        c.gridy = 4;
        final ExamButtonPane m125u4 =
                new ExamButtonPane("Unit 4", this, "M 125-4");
        this.examButtons.put("M 125-4", m125u4);
        this.grid.add(m125u4, c);
        c.gridy = 5;
        final ExamButtonPane m125u5 =
                new ExamButtonPane("Final", this, "M 125-5");
        this.examButtons.put("M 125-5", m125u5);
        this.grid.add(m125u5, c);
        c.gridy = 6;
        c.weighty = 0.0;
        final JPanel spc125 = new JPanel();
        spc125.setBackground(Skin.OFF_WHITE_CYAN);
        spc125.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.grid.add(spc125, c);
        c.gridy = 7;
        c.weighty = 1.0;
        final ExamButtonPane m125ch =
                new ExamButtonPane("Challenge", this, "M 125-C");
        this.examButtons.put("MC125-0", m125ch);
        this.grid.add(m125ch, c);
        c.weighty = 0.0;

        c.gridx = 4;

        c.gridy = 0;
        final JPanel header126 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        header126.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        header126.setBackground(Skin.WHITE);
        final JLabel title126 = new JLabel("MATH 126");
        title126.setFont(Skin.MEDIUM_HEADER_15_FONT);
        title126.setForeground(Skin.LABEL_COLOR2);
        header126.add(title126);
        this.grid.add(header126, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane m126u1 =
                new ExamButtonPane("Unit 1", this, "M 126-1");
        this.examButtons.put("M 126-1", m126u1);
        this.grid.add(m126u1, c);
        c.gridy = 2;
        final ExamButtonPane m126u2 =
                new ExamButtonPane("Unit 2", this, "M 126-2");
        this.examButtons.put("M 126-2", m126u2);
        this.grid.add(m126u2, c);
        c.gridy = 3;
        final ExamButtonPane m126u3 =
                new ExamButtonPane("Unit 3", this, "M 126-3");
        this.examButtons.put("M 126-3", m126u3);
        this.grid.add(m126u3, c);
        c.gridy = 4;
        final ExamButtonPane m126u4 =
                new ExamButtonPane("Unit 4", this, "M 126-4");
        this.examButtons.put("M 126-4", m126u4);
        this.grid.add(m126u4, c);
        c.gridy = 5;
        final ExamButtonPane m126u5 =
                new ExamButtonPane("Final", this, "M 126-5");
        this.examButtons.put("M 126-5", m126u5);
        this.grid.add(m126u5, c);
        c.gridy = 6;
        c.weighty = 0.0;
        final JPanel spc126 = new JPanel();
        spc126.setBackground(Skin.OFF_WHITE_CYAN);
        spc126.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        this.grid.add(spc126, c);
        c.gridy = 7;
        c.weighty = 1.0;
        final ExamButtonPane m126ch =
                new ExamButtonPane("Challenge", this, "M 126-C");
        this.examButtons.put("MC126-0", m126ch);
        this.grid.add(m126ch, c);
        c.weighty = 0.0;

        c.gridx = 5;

        c.gridy = 0;
        final JPanel vgap1 = new JPanel();
        vgap1.setBackground(Skin.OFF_WHITE_CYAN);
        vgap1.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.grid.add(vgap1, c);

        c.gridx = 6;

        c.gridy = 0;
        final JPanel headerTut = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        headerTut.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        headerTut.setBackground(Skin.WHITE);
        final JLabel titleTut = new JLabel("Tutorials");
        titleTut.setFont(Skin.MEDIUM_HEADER_15_FONT);
        titleTut.setForeground(Skin.LABEL_COLOR2);
        headerTut.add(titleTut);
        this.grid.add(headerTut, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane elm =
                new ExamButtonPane("ELM Exam", this, "M 100T-4");
        this.examButtons.put("M 100T-4", elm);
        this.grid.add(elm, c);
        c.gridy = 2;
        final ExamButtonPane pre117 =
                new ExamButtonPane("Algebra I", this, "M 1170-4");
        this.examButtons.put("M 1170-4", pre117);
        this.grid.add(pre117, c);
        c.gridy = 3;
        final ExamButtonPane pre118 =
                new ExamButtonPane("Algebra II", this, "M 1180-4");
        this.examButtons.put("M 1180-4", pre118);
        this.grid.add(pre118, c);
        c.gridy = 4;
        final ExamButtonPane pre124 =
                new ExamButtonPane("Functions", this, "M 1240-4");
        this.examButtons.put("M 1240-4", pre124);
        this.grid.add(pre124, c);
        c.gridy = 5;
        final ExamButtonPane pre125 =
                new ExamButtonPane("Trig. I", this, "M 1250-4");
        this.examButtons.put("M 1250-4", pre125);
        this.grid.add(pre125, c);
        c.gridy = 6;
        c.gridheight = 2;
        final Insets orig = c.insets;
        c.insets = new Insets(0, 0, 12, 0);
        final ExamButtonPane pre126 =
                new ExamButtonPane("Trig. II", this, "M 1260-4");
        this.examButtons.put("M 1260-4", pre126);
        this.grid.add(pre126, c);
        c.gridheight = 1;
        c.insets = orig;

        c.gridx = 7;

        c.gridy = 0;
        final JPanel headerOther = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        headerOther.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));
        headerOther.setBackground(Skin.WHITE);
        final JLabel titleOther = new JLabel("Other");
        titleOther.setFont(Skin.MEDIUM_HEADER_15_FONT);
        titleOther.setForeground(Skin.LABEL_COLOR2);
        headerOther.add(titleOther);
        this.grid.add(headerOther, c);

        c.weighty = 1.0;
        c.gridy = 1;
        final ExamButtonPane users =
                new ExamButtonPane("User's Exam", this, "M 100U-1");
        this.examButtons.put("M 100U-1", users);
        this.grid.add(users, c);
        c.gridy = 2;
        final ExamButtonPane placement =
                new ExamButtonPane("Placement", this, "M 100P-1");
        this.examButtons.put("M 100P-1", placement);
        this.grid.add(placement, c);

        // Bottom panel

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonFlow.setBackground(Skin.OFF_WHITE_CYAN);

        this.enforceEligible = new JCheckBox("Enforce Eligibility Rules");
        this.enforceEligible.setFont(Skin.BIG_BUTTON_16_FONT);
        this.enforceEligible.setSelected(true);
        this.enforceEligible.addActionListener(this);
        this.enforceEligible.setActionCommand(ELIG);
        buttonFlow.add(this.enforceEligible);

        south.add(buttonFlow, BorderLayout.NORTH);
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

            if ("M 117-1".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M117, 1, check);
            } else if ("M 117-2".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M117, 2, check);
            } else if ("M 117-3".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M117, 3, check);
            } else if ("M 117-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M117, 4, check);
            } else if ("M 117-5".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M117, 5, check);
            } else if ("M 117-C".equals(cmd)) {
                startChallengeExam(cleanStu, ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, check);
            } else if ("M 118-1".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M118, 1, check);
            } else if ("M 118-2".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M118, 2, check);
            } else if ("M 118-3".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M118, 3, check);
            } else if ("M 118-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M118, 4, check);
            } else if ("M 118-5".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M118, 5, check);
            } else if ("M 118-C".equals(cmd)) {
                startChallengeExam(cleanStu, ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, check);
            } else if ("M 124-1".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M124, 1, check);
            } else if ("M 124-2".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M124, 2, check);
            } else if ("M 124-3".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M124, 3, check);
            } else if ("M 124-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M124, 4, check);
            } else if ("M 124-5".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M124, 5, check);
            } else if ("M 124-C".equals(cmd)) {
                startChallengeExam(cleanStu, ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, check);
            } else if ("M 125-1".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M125, 1, check);
            } else if ("M 125-2".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M125, 2, check);
            } else if ("M 125-3".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M125, 3, check);
            } else if ("M 125-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M125, 4, check);
            } else if ("M 125-5".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M125, 5, check);
            } else if ("M 125-C".equals(cmd)) {
                startChallengeExam(cleanStu, ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, check);
            } else if ("M 126-1".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M126, 1, check);
            } else if ("M 126-2".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M126, 2, check);
            } else if ("M 126-3".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M126, 3, check);
            } else if ("M 126-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M126, 4, check);
            } else if ("M 126-5".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M126, 5, check);
            } else if ("M 126-C".equals(cmd)) {
                startChallengeExam(cleanStu, ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, check);
            } else if ("M 100T-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M100T, 4, check);
            } else if ("M 1170-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M1170, 4, check);
            } else if ("M 1180-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M1180, 4, check);
            } else if ("M 1240-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M1240, 4, check);
            } else if ("M 1250-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M1250, 4, check);
            } else if ("M 1260-4".equals(cmd)) {
                startCourseExam(cleanStu, RawRecordConstants.M1260, 4, check);
            } else if ("M 100U-1".equals(cmd)) {
                startUsersExam(cleanStu, check);
            } else if ("M 100P-1".equals(cmd)) {
                startPlacementTool(cleanStu, check);
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
    private void startCourseExam(final String stuId, final String courseId, final int unit,
                                 final boolean check) {

        try {
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache, courseId,
                    Integer.valueOf(unit), unit == 5 ? RawStexam.FINAL_EXAM : RawStexam.UNIT_EXAM);

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
     * Attempts to start a challenge exam.
     *
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param check    true to enable eligibility checks
     */
    private void startChallengeExam(final String stuId, final String courseId,
                                    final boolean check) {

        try {
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache, courseId,
                    Integer.valueOf(1), "CH");

            if (examRec != null) {
                new StartExamDialog(this.cache, this.frame, stuId, courseId, 1, examRec.version,
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
            final RawExam examRec = RawExamLogic.queryActiveByCourseUnitType(this.cache,
                    RawRecordConstants.M100U, Integer.valueOf(1), "Q");

            if (examRec != null) {
                new StartExamDialog(this.cache, this.frame, stuId, RawRecordConstants.M100U, 1,
                        examRec.version, check).setVisible(true);
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
    public void reset() {

        this.studentIdField.setText(CoreConstants.EMPTY);
        this.studentIdField.setBackground(Skin.FIELD_BG);

        this.studentNameDisplay.setText(CoreConstants.SPC);
        this.studentIdField.requestFocus();

        for (final ExamButtonPane pane : this.examButtons.values()) {
            pane.disable();
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
        final String sql1 = "SELECT first_name, last_name, pref_name "
                + "FROM student WHERE stu_id=?";

        this.studentStatusDisplay.setText(CoreConstants.SPC);

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql1)) {
            ps.setString(1, cleanStu);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundFirstName = rs.getString(1);
                    foundLastName = rs.getString(2);
                    foundPrefName = rs.getString(3);
                    this.studentIdField.setBackground(Skin.FIELD_BG);

                    final String first =
                            foundFirstName == null ? CoreConstants.EMPTY : foundFirstName.trim();
                    final String last =
                            foundLastName == null ? CoreConstants.EMPTY : foundLastName.trim();
                    final String pref =
                            foundPrefName == null ? CoreConstants.EMPTY : foundPrefName.trim();

                    if (pref.isEmpty() || pref.equals(first)) {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last);
                    } else {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last //
                                + " (" + pref + ")");
                    }

                    studentFound(cleanStu);
                } else {
                    this.studentStatusDisplay.setText("Student not found.");
                    this.studentIdField.setBackground(Skin.FIELD_ERROR_BG);
                }
            }
        } catch (final SQLException ex) {
            if (ex.getMessage() == null) {
                this.studentStatusDisplay
                        .setText("Error querying student table: " + ex.getClass().getSimpleName());
            } else {
                this.studentStatusDisplay
                        .setText("Error querying student table: " + ex.getMessage());
            }
        }
    }

    /**
     * Called when the entered student ID has been verified. This method will query the student's registrations,
     * determine what exams they are eligible for, and will enable the corresponding exam buttons.
     *
     * @param cleanedStuId the student ID
     */
    private void studentFound(final String cleanedStuId) {

        final boolean enforceElig = this.enforceEligible.isSelected();
        final StudentCheckinInfo info = this.logic.performCheckinLogic(cleanedStuId, enforceElig);

        if (info == null) {
            this.studentStatusDisplay.setText("Unable to determine eligible exams.");
        } else if (info.error == null) {

            // Present the complete list of exams on the screen with the unavailable exams
            // dimmed, and the available exams lit.

            for (final Map.Entry<String, AvailableExam> entry : info.availableExams.entrySet()) {

                final ExamButtonPane pane = this.examButtons.get(entry.getKey());
                if (pane != null) {
                    pane.reset();
                    final AvailableExam avail = entry.getValue();

                    if (avail.available) {
                        pane.enable();
                        pane.setStatusText(Objects.requireNonNullElse(avail.note, CoreConstants.SPC));
                    } else {
                        pane.disable();
                        pane.setStatusText(avail.whyNot);
                        if ("Not Registered".equals(avail.whyNot) || "Not Eligible".equals(avail.whyNot)) {
                            pane.indicateNotRegistered();
                        }
                    }
                }
            }
        } else {
            this.studentStatusDisplay.setText(info.error[0]);
        }
    }
}
