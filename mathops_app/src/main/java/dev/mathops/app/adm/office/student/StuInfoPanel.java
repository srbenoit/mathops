package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.Serial;
import java.time.LocalDate;
import java.time.Period;

/**
 * The "Info" panel of the admin system.
 */
public final class StuInfoPanel extends AdmPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4908492242412815193L;

    /** A display for the student ID. */
    private final JTextField studentId;

    /** A display for the student's PIDM. */
    private final JTextField pidm;

    /** A display for the student's first name. */
    private final JTextField firstName;

    /** A display for the student's middle initial. */
    private final JTextField middleInitial;

    /** A display for the student's last name. */
    private final JTextField lastName;

    /** A display for the student's preferred first name. */
    private final JTextField prefFirstName;

    /** A display for the student's application term. */
    private final JTextField applicationTerm;

    /** A display for the student's college. */
    private final JTextField college;

    /** A display for the student's department. */
    private final JTextField department;

    /** A display for the student's program code. */
    private final JTextField programCode;

    /** A display for the student's campus. */
    private final JTextField campus;

    /** A display for the student's high school GPA. */
    private final JTextField hsGpa;

    /** A display for the student's high school class rank. */
    private final JTextField hsClassRank;

    /** A display for the student's high school class size. */
    private final JTextField hsClassSize;

    /** A display for the student's ACT score. */
    private final JTextField actScore;

    /** A display for the student's SAT score. */
    private final JTextField satScore;

    /** A display for the student's birth date. */
    private final JTextField birthdate;

    /** A display for the student's age. */
    private final JTextField age;

    /** A display for the student's time limit factor. */
    private final JTextField timelimitFactor;

    /** A display for the student's licensed status */
    private final JTextField licensed;

    /** A display for the student's email. */
    private final JTextField studentEmail;

    /** A display for the student's adviser's email. */
    private final JTextField adviserEmail;

    /** A table that shows all special student categories. */
    private final JTableSpecialCategories specialTable;

    /** The scroll pane for the special student categories table. */
    private final JScrollPane specialScroll;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code StudentInfoPanel}.
     *
     * @param theFixed         fixed data
     */
    public StuInfoPanel(final UserData theFixed) {

        super();
        setBackground(Skin.LIGHTEST);

        final Integer clearanceLevel = theFixed.getClearanceLevel("STU_MENU");
        final boolean fullAccess = clearanceLevel != null && clearanceLevel.intValue() < 3;

        // Left side: Student table data

        final JPanel left = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);
        add(left, StackedBorderLayout.WEST);

        left.add(makeHeader("Student Information", false), StackedBorderLayout.NORTH);

        this.studentId = makeTextField(7);
        this.pidm = makeTextField(6);
        this.applicationTerm = makeTextField(4);
        this.firstName = makeTextField(10);
        this.middleInitial = makeTextField(1);
        this.lastName = makeTextField(10);
        this.prefFirstName = makeTextField(10);
        this.college = makeTextField(2);
        this.department = makeTextField(4);
        this.programCode = makeTextField(7);
        this.campus = makeTextField(2);
        this.hsGpa = makeTextField(3);
        this.hsClassRank = makeTextField(4);
        this.hsClassSize = makeTextField(4);
        this.actScore = makeTextField(2);
        this.satScore = makeTextField(3);
        this.birthdate = makeTextField(9);
        this.age = makeTextField(3);
        this.timelimitFactor = makeTextField(3);
        this.licensed = makeTextField(2);
        this.studentEmail = makeTextField(25);
        this.adviserEmail = makeTextField(25);

        final JPanel row1 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        row1.setBackground(Skin.LIGHTEST);
        row1.add(makeLabel("Student ID:"));
        row1.add(this.studentId);
        row1.add(new JLabel(CoreConstants.SPC));
        row1.add(makeLabel("PIDM:"));
        row1.add(this.pidm);
        row1.add(new JLabel(CoreConstants.SPC));
        row1.add(makeLabel("Application Term:"));
        row1.add(this.applicationTerm);
        left.add(row1, StackedBorderLayout.NORTH);

        final JLabel leftParen = new JLabel(" (");
        leftParen.setFont(Skin.MEDIUM_15_FONT);
        final JLabel rightParen = new JLabel(")");
        rightParen.setFont(Skin.MEDIUM_15_FONT);

        final JPanel row2 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        row2.setBackground(Skin.LIGHTEST);
        row2.add(makeLabel("Name:"));
        row2.add(this.firstName);
        row2.add(this.middleInitial);
        row2.add(this.lastName);
        row2.add(leftParen);
        row2.add(this.prefFirstName);
        row2.add(rightParen);
        left.add(row2, StackedBorderLayout.NORTH);

        if (fullAccess) {
            final JPanel row3 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row3.setBackground(Skin.LIGHTEST);
            row3.add(makeLabel("Birth Date:"));
            row3.add(this.birthdate);
            row3.add(new JLabel(CoreConstants.SPC));
            row3.add(makeLabel("Age:"));
            row3.add(this.age);
            left.add(row3, StackedBorderLayout.NORTH);
        }

        final JPanel row4 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        row4.setBackground(Skin.LIGHTEST);
        row4.add(makeLabel("Passed User's Exam:"));
        row4.add(this.licensed);
        row4.add(new JLabel(CoreConstants.SPC));
        row4.add(makeLabel("Timelimit Factor:"));
        row4.add(this.timelimitFactor);
        left.add(row4, StackedBorderLayout.NORTH);

        if (fullAccess) {
            left.add(makeHeader("Academic Program", true), BorderLayout.NORTH);

            final JPanel row6 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row6.setBackground(Skin.LIGHTEST);
            row6.add(makeLabel("College:"));
            row6.add(this.college);
            row6.add(new JLabel(CoreConstants.SPC));
            row6.add(makeLabel("Dept.:"));
            row6.add(this.department);
            row6.add(new JLabel(CoreConstants.SPC));
            row6.add(makeLabel("Program:"));
            row6.add(this.programCode);
            row6.add(new JLabel(CoreConstants.SPC));
            row6.add(makeLabel("Campus:"));
            row6.add(this.campus);
            left.add(row6, StackedBorderLayout.NORTH);

            left.add(makeHeader("Academic History", true), StackedBorderLayout.NORTH);

            final JPanel row8 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row8.setBackground(Skin.LIGHTEST);
            row8.add(makeLabel("High School GPA:"));
            row8.add(this.hsGpa);
            row8.add(new JLabel(CoreConstants.SPC));
            row8.add(makeLabel("Class Rank:"));
            row8.add(this.hsClassRank);
            row8.add(new JLabel(CoreConstants.SPC));
            row8.add(makeLabel("Class Size:"));
            row8.add(this.hsClassSize);
            left.add(row8, StackedBorderLayout.NORTH);

            final JPanel row9 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row9.setBackground(Skin.LIGHTEST);
            row9.add(makeLabel("ACT Score:"));
            row9.add(this.actScore);
            row9.add(new JLabel(CoreConstants.SPC));
            row9.add(makeLabel("SAT Score:"));
            row9.add(this.satScore);
            left.add(row9, StackedBorderLayout.NORTH);

            left.add(makeHeader("Contact Information", true), BorderLayout.NORTH);

            final JPanel row11 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row11.setBackground(Skin.LIGHTEST);
            row11.add(makeLabel("Student Email:"));
            row11.add(this.studentEmail);
            left.add(row11, StackedBorderLayout.NORTH);

            final JPanel row12 = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
            row12.setBackground(Skin.LIGHTEST);
            row12.add(makeLabel("Adviser Email:"));
            row12.add(this.adviserEmail);
            left.add(row12, StackedBorderLayout.NORTH);
        }

        // Right side: membership in "special_stus" categories

        final JPanel right = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        right.setBackground(Skin.LIGHTEST);
        add(right, StackedBorderLayout.EAST);

        right.add(makeHeader("Special Categories", false), StackedBorderLayout.NORTH);

        this.specialTable = new JTableSpecialCategories();
        this.specialTable.setFillsViewportHeight(true);
        this.specialScroll = new JScrollPane(this.specialTable);
        this.specialScroll.setPreferredSize(this.specialTable.getPreferredSize());
        right.add(this.specialScroll, StackedBorderLayout.CENTER);

        this.error = makeError();
        add(this.error, StackedBorderLayout.SOUTH);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        this.error.setText(CoreConstants.SPC);
        clearDisplay();

        if (data != null) {
            populateDisplay(data);
            this.specialScroll.setPreferredSize(this.specialTable.getPreferredScrollSize(this.specialScroll, 3));
        }
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        this.studentId.setText(CoreConstants.EMPTY);
        this.pidm.setText(CoreConstants.EMPTY);
        this.pidm.setText(CoreConstants.EMPTY);
        this.firstName.setText(CoreConstants.EMPTY);
        this.middleInitial.setText(CoreConstants.EMPTY);
        this.lastName.setText(CoreConstants.EMPTY);
        this.prefFirstName.setText(CoreConstants.EMPTY);
        this.applicationTerm.setText(CoreConstants.EMPTY);
        this.college.setText(CoreConstants.EMPTY);
        this.department.setText(CoreConstants.EMPTY);
        this.programCode.setText(CoreConstants.EMPTY);
        this.campus.setText(CoreConstants.EMPTY);
        this.hsGpa.setText(CoreConstants.EMPTY);
        this.hsClassRank.setText(CoreConstants.EMPTY);
        this.hsClassSize.setText(CoreConstants.EMPTY);
        this.actScore.setText(CoreConstants.EMPTY);
        this.satScore.setText(CoreConstants.EMPTY);
        this.birthdate.setText(CoreConstants.EMPTY);
        this.age.setText(CoreConstants.EMPTY);
        this.timelimitFactor.setText(CoreConstants.EMPTY);
        this.licensed.setText(CoreConstants.EMPTY);
        this.studentEmail.setText(CoreConstants.EMPTY);
        this.adviserEmail.setText(CoreConstants.EMPTY);

        this.specialTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.studentId.setText(data.student.stuId);

        this.pidm.setText(data.student.pidm == null ? CoreConstants.EMPTY : data.student.pidm.toString());

        this.firstName.setText(data.student.firstName == null ? CoreConstants.EMPTY : data.student.firstName);

        this.middleInitial.setText(data.student.middleInitial == null ? CoreConstants.EMPTY
                : data.student.middleInitial);

        this.lastName.setText(data.student.lastName == null ? CoreConstants.EMPTY : data.student.lastName);

        this.prefFirstName.setText(data.student.prefName == null ? CoreConstants.EMPTY : data.student.prefName);

        this.applicationTerm.setText(data.student.aplnTerm == null ? CoreConstants.EMPTY
                : data.student.aplnTerm.shortString);

        this.college.setText(data.student.college == null ? CoreConstants.EMPTY : data.student.college);

        this.department.setText(data.student.dept == null ? CoreConstants.EMPTY : data.student.dept);

        this.programCode.setText(data.student.programCode == null ? CoreConstants.EMPTY : data.student.programCode);

        this.campus.setText(data.student.campus == null ? CoreConstants.EMPTY : data.student.campus);

        this.hsGpa.setText(data.student.hsGpa == null ? CoreConstants.EMPTY : data.student.hsGpa);

        this.hsClassRank.setText(data.student.hsClassRank == null ? CoreConstants.EMPTY
                : data.student.hsClassRank.toString());

        this.hsClassSize.setText(data.student.hsSizeClass == null ? CoreConstants.EMPTY
                : data.student.hsSizeClass.toString());

        this.actScore.setText(data.student.actScore == null ? CoreConstants.EMPTY : data.student.actScore.toString());

        this.satScore.setText(data.student.satScore == null ? CoreConstants.EMPTY : data.student.satScore.toString());

        if (data.student.birthdate == null) {
            this.birthdate.setText(CoreConstants.EMPTY);
            this.age.setText(CoreConstants.EMPTY);
        } else {
            this.birthdate.setText(TemporalUtils.FMT_MDY.format(data.student.birthdate));
            final Period agePeriod = data.student.birthdate.until(LocalDate.now());
            this.age.setText(Integer.toString(agePeriod.getYears()));
        }

        this.timelimitFactor.setText(data.student.timelimitFactor == null ? CoreConstants.EMPTY
                : data.student.timelimitFactor.toString());

        this.licensed.setText(data.student.licensed == null ? CoreConstants.EMPTY : data.student.licensed);

        this.studentEmail.setText(data.student.stuEmail == null ? CoreConstants.EMPTY : data.student.stuEmail);

        this.adviserEmail.setText(data.student.adviserEmail == null ? CoreConstants.EMPTY : data.student.adviserEmail);

        this.specialTable.clear();
        this.specialTable.addData(data.studentCategories, 2);
    }
}
