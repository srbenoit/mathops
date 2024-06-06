package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;

/**
 * A card to display when the user selects a single student.
 */
final class CardStudentDetail extends AdminPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The fixed data. */
    private final FixedData fixed;

    /** The currently selected student. */
    private final JTextField selectedStudentName;

    /** The currently selected student ID. */
    private final JTextField selectedStudentId;

    /** The tabbed layout with actions once a student is picked. */
    private final JTabbedPane tabs;

    /** The "Summary" panel. */
    private final StudentSummaryPanel summaryPanel;

    /** The "Info" panel. */
    private final StudentInfoPanel infoPanel;

    /** The "Course" panel. */
    private final StudentCoursesPanel coursePanel;

    /** The "Deadlines" panel. */
    private final StudentDeadlinesPanel deadlinesPanel;

    /** The "Activity" panel. */
    private final StudentActivityPanel activityPanel;

    /** The "Discipline" panel. */
    private final StudentDisciplinePanel disciplinePanel;

    /** The "Holds" panel. */
    private final StudentHoldsPanel holdsPanel;

    /** The "Exams" panel. */
    private final StudentExamsPanel examsPanel;

    /** The "MPT" panel. */
    private final StudentPlacementPanel mptPanel;

    /** The "Math Plan" panel. */
    private final StudentMathPlanPanel mathPlanPanel;

    /**
     * Constructs a new {@code CardStudentDetail}.
     *
     * @param theCache         the data cache
     * @param liveContext      the database context used to access live data
     * @param theFixed         the fixed data
     */
    CardStudentDetail(final Cache theCache, final DbContext liveContext,
                      final FixedData theFixed, final Object theRenderingHint) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_RED);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.fixed = theFixed;

        // Top - [Pick] button and selected student name/ID

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 5));
        top.setBackground(Skin.OFF_WHITE_RED);
        panel.add(top, BorderLayout.PAGE_START);

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

        this.tabs = new JTabbedPane();
        this.tabs.setFont(Skin.BIG_BUTTON_16_FONT);
        this.tabs.setBackground(Skin.OFF_WHITE_RED);
        panel.add(this.tabs, BorderLayout.CENTER);

        this.summaryPanel = new StudentSummaryPanel(theCache);
        this.tabs.addTab("Summary", this.summaryPanel);

        this.infoPanel = new StudentInfoPanel(this.fixed);
        this.tabs.addTab("Info", this.infoPanel);

        this.coursePanel = new StudentCoursesPanel(theCache.conn);
        this.tabs.addTab("Courses", this.coursePanel);

        this.deadlinesPanel = new StudentDeadlinesPanel(theCache, this.fixed);
        this.tabs.addTab("Deadlines", this.deadlinesPanel);

        this.activityPanel = new StudentActivityPanel();
        this.tabs.addTab("Activity", this.activityPanel);

        if (this.fixed.getClearanceLevel("DISCIP") != null) {
            this.disciplinePanel = new StudentDisciplinePanel(theCache);
            this.tabs.addTab("Discipline", this.disciplinePanel);
        } else {
            this.disciplinePanel = null;
        }

        this.holdsPanel = new StudentHoldsPanel(theCache, this.fixed);
        this.tabs.addTab("Holds", this.holdsPanel);

        this.examsPanel =
                new StudentExamsPanel(theCache, liveContext, this.fixed);
        this.tabs.addTab("Exams", this.examsPanel);

        this.mptPanel = new StudentPlacementPanel();
        final JScrollPane scroll = new JScrollPane(this.mptPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setBlockIncrement(30);
        this.tabs.addTab("Placement", scroll);

        this.mathPlanPanel = new StudentMathPlanPanel(theRenderingHint);
        this.tabs.addTab("Math Plan", this.mathPlanPanel);
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // TODO:
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
        this.activityPanel.setSelectedStudent(this.fixed, theData);
        if (this.disciplinePanel != null) {
            this.disciplinePanel.setSelectedStudent(theData);
        }
        this.holdsPanel.setSelectedStudent(theData);
        this.examsPanel.setSelectedStudent(theData);
        this.mptPanel.setSelectedStudent(theData);

        if (theData == null) {
            this.mathPlanPanel.setSelectedStudent(cache, null);

            this.selectedStudentName.setText(CoreConstants.EMPTY);
            this.selectedStudentId.setText(CoreConstants.EMPTY);
            this.tabs.setEnabled(false);
        } else {
            this.mathPlanPanel.setSelectedStudent(cache, theData.getStudentId());

            try {
                final RawStudent student = theData.getStudentRecord();

                final StringBuilder name = new StringBuilder(100);
                name.append(student.firstName);
                if (student.middleInitial != null) {
                    name.append(' ').append(student.middleInitial).append('.');
                }
                name.append(' ').append(student.lastName);
                if (student.prefName != null && !student.prefName.equals(student.firstName)) {
                    name.append(" (").append(student.prefName).append(')');
                }

                this.selectedStudentName.setText(name.toString());
                this.selectedStudentId.setText(student.stuId);
                this.tabs.setEnabled(true);
                this.tabs.setSelectedIndex(0);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
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

        Log.info("CardStudentDetail: ", cmd);
    }
}
