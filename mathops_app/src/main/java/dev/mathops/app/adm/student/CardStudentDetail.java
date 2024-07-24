package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card to display when the user selects a single student.
 */
final class CardStudentDetail extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The key to select the student information card. */
    private static final String STU_INFO_CARD_KEY = "STU_INFO";

    /** The key to select the placement information card. */
    private static final String PLACEMENT_INFO_CARD_KEY = "PLACEMENT_INFO";

    /** The key to select the course information card. */
    private static final String COURSE_INFO_CARD_KEY = "COURSE_INFO";

    /** An action command. */
    private static final String STU_INFO_CMD = "STU_INFO";

    /** An action command. */
    private static final String PLACEMENT_INFO_CMD = "PLACEMENT_INFO";

    /** An action command. */
    private static final String COURSE_INFO_CMD = "COURSE_INFO";

    /** The fixed data. */
    private final FixedData fixed;

    /** The currently selected student. */
    private final JTextField selectedStudentName;

    /** The currently selected student ID. */
    private final JTextField selectedStudentId;

    /** The tabbed layout with student-related data once a student is picked. */
    private final JTabbedPane studentInfoTabs;

    /** The tabbed layout with placement-related data once a student is picked. */
    private final JTabbedPane placementInfoTabs;

    /** The tabbed layout with course-related data once a student is picked. */
    private final JTabbedPane courseInfoTabs;

    /** The "Summary" panel. */
    private final StuSummaryPanel summaryPanel;

    /** The "Info" panel. */
    private final StuInfoPanel infoPanel;

    /** The "Discipline" panel. */
    private final StuDisciplinePanel disciplinePanel;

    /** The "Holds" panel. */
    private final StuHoldsPanel holdsPanel;

    /** The "Appeals and Accommodations" panel. */
    private final StuAppealsPanel appealsPanel;

    /** The "MPT" panel. */
    private final PlacementToolPanel mptPanel;

    /** The "Math Plan" panel. */
    private final PlacementMathPlanPanel mathPlanPanel;

    /** The "Transfer Credit panel. */
    private final PlacementTransferPanel transferPanel;

    /** The "Course" panel. */
    private final CourseRegistrationsPanel coursePanel;

    /** The "Deadlines" panel. */
    private final CourseDeadlinesPanel deadlinesPanel;

    /** The "Activity" panel. */
    private final CourseActivityPanel activityPanel;

    /** The "Exams" panel. */
    private final CourseExamsPanel examsPanel;

    /** Panel with cards for various categories of student-related data. */
    private final JPanel studentCardPanel;

    /** Card layout for the various categories of student-related data. */
    private final CardLayout studentCardLayout;

    /**
     * Constructs a new {@code CardStudentDetail}.
     *
     * @param theCache    the data cache
     * @param liveContext the database context used to access live data
     * @param theFixed    the fixed data
     */
    CardStudentDetail(final Cache theCache, final DbContext liveContext, final FixedData theFixed) {

        super();

        final JPanel panel = new JPanel(new StackedBorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_RED);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.fixed = theFixed;

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 5));
        top.setBackground(Skin.OFF_WHITE_RED);
        panel.add(top, StackedBorderLayout.NORTH);

        final JLabel selectedLbl = new JLabel("Selected Student: ");
        selectedLbl.setFont(Skin.BIG_BUTTON_16_FONT);
        selectedLbl.setForeground(Skin.LABEL_COLOR);
        top.add(selectedLbl);

        this.selectedStudentName = new JTextField(16);
        this.selectedStudentName.setBackground(Skin.OFF_WHITE_RED);
        this.selectedStudentName.setBorder(null);
        this.selectedStudentName.setFont(Skin.SUB_HEADER_16_FONT);
        top.add(this.selectedStudentName);

        final JLabel stuIdLbl = new JLabel("Student ID: ");
        stuIdLbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        stuIdLbl.setFont(Skin.BIG_BUTTON_16_FONT);
        stuIdLbl.setForeground(Skin.LABEL_COLOR);
        top.add(stuIdLbl);

        this.selectedStudentId = new JTextField(9);
        this.selectedStudentId.setBackground(Skin.OFF_WHITE_RED);
        this.selectedStudentId.setBorder(null);
        this.selectedStudentId.setEditable(false);
        this.selectedStudentId.setFont(Skin.SUB_HEADER_16_FONT);
        top.add(this.selectedStudentId);

        //
        // Major categories...
        //

        final JPanel top2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 10));
        top2.setBackground(Skin.LIGHTER_BLUE);
        panel.add(top2, StackedBorderLayout.NORTH);
        top2.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));

        final JToggleButton btn1 = new JToggleButton("Student-Related Information");
        btn1.setFont(Skin.BIG_BUTTON_16_FONT);
        btn1.setActionCommand(STU_INFO_CMD);
        btn1.addActionListener(this);
        top2.add(btn1);
        top2.add(new JLabel("       "));

        final JToggleButton btn2 = new JToggleButton("Placement-Related Information");
        btn2.setFont(Skin.BIG_BUTTON_16_FONT);
        btn2.setActionCommand(PLACEMENT_INFO_CMD);
        btn2.addActionListener(this);
        top2.add(btn2);
        top2.add(new JLabel("       "));

        final JToggleButton btn3 = new JToggleButton("Courses and Registrations");
        btn3.setFont(Skin.BIG_BUTTON_16_FONT);
        btn3.setActionCommand(COURSE_INFO_CMD);
        btn3.addActionListener(this);
        top2.add(btn3);

        final ButtonGroup group = new ButtonGroup();
        group.add(btn1);
        group.add(btn2);
        group.add(btn3);
        btn1.setSelected(true);

        this.studentCardLayout = new CardLayout();
        this.studentCardPanel = new JPanel(this.studentCardLayout);
        panel.add(this.studentCardPanel, StackedBorderLayout.CENTER);

        //
        // Student-related information
        //

        this.studentInfoTabs = new JTabbedPane();
        this.studentInfoTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.studentInfoTabs.setBackground(Skin.OFF_WHITE_RED);
        this.studentCardPanel.add(this.studentInfoTabs, STU_INFO_CARD_KEY);

        this.summaryPanel = new StuSummaryPanel(theCache);
        this.studentInfoTabs.addTab("Summary", this.summaryPanel);

        this.infoPanel = new StuInfoPanel(this.fixed);
        this.studentInfoTabs.addTab("Info", this.infoPanel);

        if (this.fixed.getClearanceLevel("DISCIP") != null) {
            this.disciplinePanel = new StuDisciplinePanel(theCache);
            this.studentInfoTabs.addTab("Discipline", this.disciplinePanel);
        } else {
            this.disciplinePanel = null;
        }

        this.holdsPanel = new StuHoldsPanel(theCache, this.fixed);
        this.studentInfoTabs.addTab("Holds", this.holdsPanel);

        this.appealsPanel = new StuAppealsPanel(theCache, this.fixed);
        this.studentInfoTabs.addTab("Accommodations & Appeals", this.appealsPanel);

        //
        // Placement-related information
        //

        this.placementInfoTabs = new JTabbedPane();
        this.placementInfoTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.placementInfoTabs.setBackground(Skin.OFF_WHITE_RED);
        this.studentCardPanel.add(this.placementInfoTabs, PLACEMENT_INFO_CARD_KEY);

        this.mptPanel = new PlacementToolPanel();
        final JScrollPane scroll = new JScrollPane(this.mptPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setBlockIncrement(30);
        this.placementInfoTabs.addTab("Placement", scroll);

        this.mathPlanPanel = new PlacementMathPlanPanel();
        this.placementInfoTabs.addTab("Math Plan", this.mathPlanPanel);

        this.transferPanel = new PlacementTransferPanel(theCache, this.fixed);
        this.placementInfoTabs.addTab("Transfer Credit", this.transferPanel);

        //
        // Course-related information
        //

        this.courseInfoTabs = new JTabbedPane();
        this.courseInfoTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.courseInfoTabs.setBackground(Skin.OFF_WHITE_RED);
        this.studentCardPanel.add(this.courseInfoTabs, COURSE_INFO_CARD_KEY);

        this.coursePanel = new CourseRegistrationsPanel(theCache);
        this.courseInfoTabs.addTab("Courses", this.coursePanel);

        this.deadlinesPanel = new CourseDeadlinesPanel(theCache, this.fixed);
        this.courseInfoTabs.addTab("Deadlines", this.deadlinesPanel);

        this.activityPanel = new CourseActivityPanel();
        this.courseInfoTabs.addTab("Activity", this.activityPanel);

        this.examsPanel = new CourseExamsPanel(theCache, liveContext, this.fixed);
        this.courseInfoTabs.addTab("Exams", this.examsPanel);
    }

    /**
     * Sets the focus when this panel is activated.
     */
    void focus() {

        // TODO:
    }

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    void clearDisplay() {

        this.summaryPanel.clearDisplay();
        this.infoPanel.clearDisplay();
        this.coursePanel.clearDisplay();
        this.deadlinesPanel.clearDisplay();
        this.activityPanel.clearDisplay();
        this.disciplinePanel.clearDisplay();
        this.holdsPanel.clearDisplay();
        this.appealsPanel.clearDisplay();
        this.examsPanel.clearDisplay();
        this.mptPanel.clearDisplay();
        this.mathPlanPanel.clearDisplay();
        this.transferPanel.clearDisplay();
    }

    /**
     * Sets the student data.
     *
     * @param cache   the data cache
     * @param theData the student data
     */
    void setStudent(final Cache cache, final StudentData theData) {

        this.summaryPanel.setSelectedStudent(theData);
        this.infoPanel.setSelectedStudent(theData);
        this.coursePanel.setSelectedStudent(theData);
        this.deadlinesPanel.setSelectedStudent(theData);
        this.activityPanel.setSelectedStudent(cache, theData);
        if (this.disciplinePanel != null) {
            this.disciplinePanel.setSelectedStudent(theData);
        }
        this.holdsPanel.setSelectedStudent(theData);
        this.appealsPanel.setSelectedStudent(theData);
        this.examsPanel.setSelectedStudent(theData);
        this.mptPanel.setSelectedStudent(theData);
        this.transferPanel.setSelectedStudent(theData);

        if (theData == null) {
            this.mathPlanPanel.setSelectedStudent(cache, null);

            this.selectedStudentName.setText(CoreConstants.EMPTY);
            this.selectedStudentId.setText(CoreConstants.EMPTY);
            this.studentInfoTabs.setEnabled(false);
            this.placementInfoTabs.setEnabled(false);
            this.courseInfoTabs.setEnabled(false);
        } else {
            this.mathPlanPanel.setSelectedStudent(cache, theData.student.stuId);

            final RawStudent student = theData.student;

            final StringBuilder name = new StringBuilder(100);
            name.append(student.firstName);
            if (student.middleInitial != null) {
                name.append(CoreConstants.SPC_CHAR).append(student.middleInitial).append(CoreConstants.DOT);
            }
            name.append(CoreConstants.SPC_CHAR).append(student.lastName);
            if (student.prefName != null && !student.prefName.equals(student.firstName)) {
                name.append(" (").append(student.prefName).append(')');
            }

            final String stuName = name.toString();
            this.selectedStudentName.setText(stuName);
            this.selectedStudentId.setText(student.stuId);
            this.studentInfoTabs.setEnabled(true);
            this.studentInfoTabs.setSelectedIndex(0);
            this.placementInfoTabs.setEnabled(true);
            this.courseInfoTabs.setEnabled(true);

            this.studentCardLayout.show(this.studentCardPanel, STU_INFO_CARD_KEY);
        }
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        switch (cmd) {
            case STU_INFO_CMD -> this.studentCardLayout.show(this.studentCardPanel, STU_INFO_CARD_KEY);
            case PLACEMENT_INFO_CMD -> this.studentCardLayout.show(this.studentCardPanel, PLACEMENT_INFO_CARD_KEY);
            case COURSE_INFO_CMD -> this.studentCardLayout.show(this.studentCardPanel, COURSE_INFO_CARD_KEY);
            case null, default -> Log.info("CardStudentDetail: ", cmd);
        }
    }
}
