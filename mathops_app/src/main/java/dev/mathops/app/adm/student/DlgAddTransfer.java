package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.DbConnection;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;

/**
 * A dialog to add a row to "FFR_TRNS".
 */
final class DlgAddTransfer extends JFrame {

    /** The course IDs with which to pre-populate the course ID dropdown. */
    private static final String[] COURSE_IDS = {"MATH 1++1B", "MATH 2++1B", "M 002", "M 055", "M 099", "M 100C",
            "M 101", "M 105", "M 117", "M 118", "M 124", "M 125", "M 126", "M 120", "M 127", "M 141", "M 155", "M 156",
            "M 160", "M 161", "M 229", "M 255", "M 261", "M 340", "M 369", "STAT 100", "STAT 201", "STAT 204"};

    /** The database connection. */
    private final DbConnection conn;

    /** The owning student courses panel to be refreshed if a transfer record is added. */
    private final StuCoursesPanel owner;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student Name. */
    private final JTextField studentNameField;

    /** The dropdown for the course ID (the user can enter a course ID as well). */
    private final JComboBox courseIdDropdown;


    /**
     * Constructs a new {@code DlgAddTransfer}.
     *
     * @param theConn  the database connection
     * @param theOwner the owning student courses panel to be refreshed if a transfer record is added
     */
    DlgAddTransfer(final DbConnection theConn, final StuCoursesPanel theOwner) {

        super("Add Transfer Credit");
        setBackground(Skin.LIGHTEST);

//        stu_id               char(9)                                 no
//        course               char(10)                                no
//        exam_placed          char(1)                                 no
//        exam_dt              date                                    no
//        dt_cr_refused        date                                    yes

        this.conn = theConn;
        this.owner = theOwner;

        final JPanel content = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        setContentPane(content);

        final JLabel[] labels = new JLabel[3];

        labels[0] = AdmPanelBase.makeLabelMedium("Student ID: ");
        labels[1] = AdmPanelBase.makeLabelMedium("Student Name: ");
        labels[2] = AdmPanelBase.makeLabelMedium("Course ID: ");
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        this.studentIdField = AdmPanelBase.makeTextFieldMedium(9);
        this.studentNameField = AdmPanelBase.makeTextFieldMedium(20);
        final Font fieldFont = this.studentIdField.getFont();

        this.courseIdDropdown = new JComboBox<>(COURSE_IDS);
        this.courseIdDropdown.setFont(fieldFont);
        this.courseIdDropdown.setEditable(true);
        this.courseIdDropdown.setVisible(false); // So it does not mess with frame size in "pack()" below




        final JPanel flow1 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow1.add(labels[0]);
        flow1.add(this.studentIdField);
        content.add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow2.add(labels[1]);
        flow2.add(this.studentNameField);
        content.add(flow2, StackedBorderLayout.NORTH);

        final JPanel flow3 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow3.add(labels[2]);
        flow3.add(this.courseIdDropdown);
        content.add(flow3, StackedBorderLayout.NORTH);

        pack();
        final Dimension size = getSize();

        Container parent = theOwner.getParent();
        while (parent != null) {
            if (parent instanceof final JFrame owningFrame) {
                final Rectangle bounds = owningFrame.getBounds();
                final int cx = bounds.x + (bounds.width / 2);
                final int cy = bounds.y + (bounds.height / 2);

                setLocation(cx - size.width / 2, cy - size.height / 2);
                break;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    void populateDisplay(final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);
        this.courseIdDropdown.setVisible(true);
    }
}

