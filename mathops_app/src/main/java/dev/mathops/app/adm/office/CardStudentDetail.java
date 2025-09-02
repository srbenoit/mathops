package dev.mathops.app.adm.office;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.office.placement.PlacementMathPlanPanel;
import dev.mathops.app.adm.office.placement.PlacementToolPanel;
import dev.mathops.app.adm.office.placement.PlacementTransferPanel;
import dev.mathops.app.adm.office.registration.CourseActivityPanel;
import dev.mathops.app.adm.office.registration.CourseDeadlinesPanel;
import dev.mathops.app.adm.office.registration.CourseExamsPanel;
import dev.mathops.app.adm.office.registration.CourseHistoryPanel;
import dev.mathops.app.adm.office.registration.CourseRegistrationsPanel;
import dev.mathops.app.adm.office.student.StuAppealsPanel;
import dev.mathops.app.adm.office.student.StuDisciplinePanel;
import dev.mathops.app.adm.office.student.StuHoldsPanel;
import dev.mathops.app.adm.office.student.StuInfoPanel;
import dev.mathops.app.adm.office.student.StuSummaryPanel;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.rec.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Objects;

/**
 * A card to display when the user selects a single student.
 *
 * <p>
 * This card show the selected student ID and name, and has buttons to toggle between "Student-Related Information",
 * "Placement-Related Information", and "Courses and Registrations" for that student, with a tabbed pane for each.  The
 * content of each of these tabbed panes are defined under the ".student", ".placement", and ".registration"
 * sub-packages, respectively.
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

    /** The currently selected student. */
    private final JTextField selectedStudentName;

    /** The currently selected student ID. */
    private final JTextField selectedStudentId;

    /** Panel with cards for various categories of student-related data. */
    private final JPanel cardPanel;

    /** Card layout to toggle between categories of student-related data. */
    private final CardLayout cardLayout;

    /** The tabbed layout with student-related data once a student is picked. */
    private final JTabbedPane studentRelatedTabs;

    /** The tabbed layout with placement-related data once a student is picked. */
    private final JTabbedPane placementRelatedTabs;

    /** The tabbed layout with course-related data once a student is picked. */
    private final JTabbedPane courseRegistrationTabs;

    /** The "Summary" panel. */
    private final StuSummaryPanel studentSummaryPanel;

    /** The "Info" panel. */
    private final StuInfoPanel stuInfoPanel;

    /** The "Discipline" panel. */
    private final StuDisciplinePanel stuDisciplinePanel;

    /** The "Holds" panel. */
    private final StuHoldsPanel stuHoldsPanel;

    /** The "Appeals and Accommodations" panel. */
    private final StuAppealsPanel stuAppealsPanel;

    /** The "MPT" panel. */
    private final PlacementToolPanel placementToolPanel;

    /** The "Math Plan" panel. */
    private final PlacementMathPlanPanel placementMathPlanPanel;

    /** The "Transfer Credit panel. */
    private final PlacementTransferPanel placementTransferPanel;

    /** The "Course" panel. */
    private final CourseRegistrationsPanel courseRegistrationsPanel;

    /** The "History" panel. */
    private final CourseHistoryPanel courseHistoryPanel;

    /** The "Deadlines" panel. */
    private final CourseDeadlinesPanel courseDeadlinesPanel;

    /** The "Activity" panel. */
    private final CourseActivityPanel courseActivityPanel;

    /** The "Exams" panel. */
    private final CourseExamsPanel courseExamsPanel;

    /**
     * Constructs a new {@code CardStudentDetail}.
     *
     * @param theCache    the data cache
     * @param theUserData the fixed data
     */
    CardStudentDetail(final Cache theCache, final UserData theUserData) {

        super();

        final JPanel panel = new JPanel(new StackedBorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        final Border myBorder = getBorder();
        panel.setBorder(myBorder);

        setBackground(Skin.LT_RED);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border pad3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, pad3);
        setBorder(newBorder);
        add(panel, BorderLayout.CENTER);

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
        final Border leftPad20 = BorderFactory.createEmptyBorder(0, 20, 0, 0);
        stuIdLbl.setBorder(leftPad20);
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
        final MatteBorder lineAboveBelow = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY);
        top2.setBorder(lineAboveBelow);

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

        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(this.cardLayout);
        panel.add(this.cardPanel, StackedBorderLayout.CENTER);

        //
        // Student-related information
        //

        this.studentRelatedTabs = new JTabbedPane();
        this.studentRelatedTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.studentRelatedTabs.setBackground(Skin.OFF_WHITE_RED);
        this.cardPanel.add(this.studentRelatedTabs, STU_INFO_CARD_KEY);

        this.studentSummaryPanel = new StuSummaryPanel(theCache);
        this.studentRelatedTabs.addTab("Summary", this.studentSummaryPanel);

        this.stuInfoPanel = new StuInfoPanel(theUserData);
        this.studentRelatedTabs.addTab("Info", this.stuInfoPanel);

        if (theUserData.getClearanceLevel("DISCIP") != null) {
            this.stuDisciplinePanel = new StuDisciplinePanel(theCache);
            this.studentRelatedTabs.addTab("Discipline", this.stuDisciplinePanel);
        } else {
            this.stuDisciplinePanel = null;
        }

        this.stuHoldsPanel = new StuHoldsPanel(theCache, theUserData);
        this.studentRelatedTabs.addTab("Holds", this.stuHoldsPanel);

        this.stuAppealsPanel = new StuAppealsPanel(theCache, theUserData);
        this.studentRelatedTabs.addTab("Accommodations & Appeals", this.stuAppealsPanel);

        //
        // Placement-related information
        //

        this.placementRelatedTabs = new JTabbedPane();
        this.placementRelatedTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.placementRelatedTabs.setBackground(Skin.OFF_WHITE_RED);
        this.cardPanel.add(this.placementRelatedTabs, PLACEMENT_INFO_CARD_KEY);

        this.placementToolPanel = new PlacementToolPanel();
        final JScrollPane scroll = new JScrollPane(this.placementToolPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setBlockIncrement(30);
        this.placementRelatedTabs.addTab("Placement", scroll);

        this.placementMathPlanPanel = new PlacementMathPlanPanel();
        this.placementRelatedTabs.addTab("Math Plan", this.placementMathPlanPanel);

        this.placementTransferPanel = new PlacementTransferPanel(theCache, theUserData);
        this.placementRelatedTabs.addTab("Transfer Credit", this.placementTransferPanel);

        //
        // Course-related information
        //

        this.courseRegistrationTabs = new JTabbedPane();
        this.courseRegistrationTabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.courseRegistrationTabs.setBackground(Skin.OFF_WHITE_RED);
        this.cardPanel.add(this.courseRegistrationTabs, COURSE_INFO_CARD_KEY);

        this.courseRegistrationsPanel = new CourseRegistrationsPanel(theCache);
        this.courseRegistrationTabs.addTab("Courses", this.courseRegistrationsPanel);

        this.courseHistoryPanel = new CourseHistoryPanel(theCache);
        this.courseRegistrationTabs.addTab("History", this.courseHistoryPanel);

        this.courseDeadlinesPanel = new CourseDeadlinesPanel(theCache, theUserData);
        this.courseRegistrationTabs.addTab("Deadlines", this.courseDeadlinesPanel);

        this.courseActivityPanel = new CourseActivityPanel();
        this.courseRegistrationTabs.addTab("Activity", this.courseActivityPanel);

        this.courseExamsPanel = new CourseExamsPanel(theCache, theUserData);
        this.courseRegistrationTabs.addTab("Exams", this.courseExamsPanel);
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

        if (Objects.nonNull(this.studentSummaryPanel)) {
            this.studentSummaryPanel.clearDisplay();
        }
        if (Objects.nonNull(this.stuInfoPanel)) {
            this.stuInfoPanel.clearDisplay();
        }
        if (Objects.nonNull(this.courseRegistrationsPanel)) {
            this.courseRegistrationsPanel.clearDisplay();
        }
        if (Objects.nonNull(this.courseHistoryPanel)) {
            this.courseHistoryPanel.clearDisplay();
        }
        if (Objects.nonNull(this.courseDeadlinesPanel)) {
            this.courseDeadlinesPanel.clearDisplay();
        }
        if (Objects.nonNull(this.courseActivityPanel)) {
            this.courseActivityPanel.clearDisplay();
        }
        if (Objects.nonNull(this.stuDisciplinePanel)) {
            this.stuDisciplinePanel.clearDisplay();
        }
        if (Objects.nonNull(this.stuHoldsPanel)) {
            this.stuHoldsPanel.clearDisplay();
        }
        if (Objects.nonNull(this.stuAppealsPanel)) {
            this.stuAppealsPanel.clearDisplay();
        }
        if (Objects.nonNull(this.courseExamsPanel)) {
            this.courseExamsPanel.clearDisplay();
        }
        if (Objects.nonNull(this.placementToolPanel)) {
            this.placementToolPanel.clearDisplay();
        }
        if (Objects.nonNull(this.placementMathPlanPanel)) {
            this.placementMathPlanPanel.clearDisplay();
        }
        if (Objects.nonNull(this.placementTransferPanel)) {
            this.placementTransferPanel.clearDisplay();
        }
    }

    /**
     * Sets the student data.
     *
     * @param cache   the data cache
     * @param theData the student data
     */
    void setStudent(final Cache cache, final StudentData theData) {

        if (Objects.nonNull(this.studentSummaryPanel)) {
            this.studentSummaryPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.stuInfoPanel)) {
            this.stuInfoPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.courseRegistrationsPanel)) {
            this.courseRegistrationsPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.courseHistoryPanel)) {
            this.courseHistoryPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.courseDeadlinesPanel)) {
            this.courseDeadlinesPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.courseActivityPanel)) {
            this.courseActivityPanel.setSelectedStudent(cache, theData);
        }
        if (Objects.nonNull(this.stuDisciplinePanel)) {
            this.stuDisciplinePanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.stuHoldsPanel)) {
            this.stuHoldsPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.stuAppealsPanel)) {
            this.stuAppealsPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.courseExamsPanel)) {
            this.courseExamsPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.placementToolPanel)) {
            this.placementToolPanel.setSelectedStudent(theData);
        }
        if (Objects.nonNull(this.placementTransferPanel)) {
            this.placementTransferPanel.setSelectedStudent(theData);
        }

        if (theData == null) {
            if (Objects.nonNull(this.placementMathPlanPanel)) {
                this.placementMathPlanPanel.setSelectedStudent(cache, null);
            }

            this.selectedStudentName.setText(CoreConstants.EMPTY);
            this.selectedStudentId.setText(CoreConstants.EMPTY);
            this.studentRelatedTabs.setEnabled(false);
            this.placementRelatedTabs.setEnabled(false);
            this.courseRegistrationTabs.setEnabled(false);
        } else {
            if (Objects.nonNull(this.placementMathPlanPanel)) {
                this.placementMathPlanPanel.setSelectedStudent(cache, theData.student.stuId);
            }

            final RawStudent student = theData.student;

            final StringBuilder name = new StringBuilder(100);
            name.append(student.firstName);
            if (Objects.nonNull(student.middleInitial)) {
                name.append(CoreConstants.SPC_CHAR).append(student.middleInitial).append(CoreConstants.DOT);
            }
            name.append(CoreConstants.SPC_CHAR).append(student.lastName);
            if (Objects.nonNull(student.prefName) && !student.prefName.equals(student.firstName)) {
                name.append(" (").append(student.prefName).append(')');
            }

            final String stuName = name.toString();
            this.selectedStudentName.setText(stuName);
            this.selectedStudentId.setText(student.stuId);
            this.studentRelatedTabs.setEnabled(true);
            this.studentRelatedTabs.setSelectedIndex(0);
            this.placementRelatedTabs.setEnabled(true);
            this.courseRegistrationTabs.setEnabled(true);

            this.cardLayout.show(this.cardPanel, STU_INFO_CARD_KEY);
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
            case STU_INFO_CMD -> this.cardLayout.show(this.cardPanel, STU_INFO_CARD_KEY);
            case PLACEMENT_INFO_CMD -> this.cardLayout.show(this.cardPanel, PLACEMENT_INFO_CARD_KEY);
            case COURSE_INFO_CMD -> this.cardLayout.show(this.cardPanel, COURSE_INFO_CARD_KEY);
            case null, default -> Log.info("CardStudentDetail: ", cmd);
        }
    }
}
