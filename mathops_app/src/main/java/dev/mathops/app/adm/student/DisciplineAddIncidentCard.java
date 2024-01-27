package dev.mathops.app.adm.student;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.EDisciplineActionType;
import dev.mathops.db.enums.EDisciplineIncidentType;
import dev.mathops.db.old.rawrecord.RawDiscipline;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;

/**
 * A card within the "Discipline" tab of the admin app that allows the user to add a new incident.
 */
/* default */ class DisciplineAddIncidentCard extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2653445342460609089L;

    /** An action command. */
    private static final String SUBMIT_CMD = "SUBMIT";

    /** An action command. */
    private static final String RESET_CMD = "RESET";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** Courses. */
    private static final String[] COURSES = {"(None)", "Placement", "ELM Tutorial",
            "Precalc Tutorial", "MATH 117", "MATH 118",
            "MATH 124", "MATH 125", "MATH 126"};

    /** Courses. */
    private static final String[] UNITS = {"(None)", "1",
            "2", "3", "4", "5"};

    /** The owning discipline panel. */
    private final StudentDisciplinePanel owner;

    /** The current student ID. */
    private String studentId;

    /** The incident date month field. */
    private final JComboBox<Month> incidentDateMonth;

    /** The incident date day field. */
    private final JTextField incidentDateDay;

    /** The incident date year field. */
    private final JTextField incidentDateYear;

    /** The interviewer field. */
    private final JTextField interviewer;

    /** The proctor field. */
    private final JTextField proctor;

    /** The incident code field. */
    private final JComboBox<EDisciplineIncidentType> incidentType;

    /** The course field. */
    private final JComboBox<String> course;

    /** The unit field. */
    private final JComboBox<String> unit;

    /** The description field. */
    private final JTextArea description;

    /** The action code field. */
    private final JComboBox<EDisciplineActionType> actionType;

    /** The comments field. */
    private final JTextArea comments;

    /** An error message. */
    private final JLabel err;

    /**
     * Constructs a new {@code DisciplineAddIncidentCard}.
     *
     * @param theOwner         the owning discipline panel
     */
    DisciplineAddIncidentCard(final StudentDisciplinePanel theOwner) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.owner = theOwner;

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        add(center, BorderLayout.CENTER);

        final JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Skin.LIGHT);
        form.setBorder(BorderFactory.createCompoundBorder(//
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        center.add(form, BorderLayout.NORTH);

        final JLabel[] lbls = new JLabel[8];
        lbls[0] = new JLabel("Incident Date:");
        lbls[1] = new JLabel("Interviewer:");
        lbls[2] = new JLabel("Proctor:");
        lbls[3] = new JLabel("Incident Code:");
        lbls[4] = new JLabel("Course:");
        lbls[5] = new JLabel("Description:");
        lbls[6] = new JLabel("Action Code:");
        lbls[7] = new JLabel("Comments:");
        int maxw = 0;
        int maxh = 0;
        for (final JLabel lbl : lbls) {
            lbl.setForeground(Skin.LABEL_COLOR);
            lbl.setFont(Skin.BOLD_12_FONT);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension dim = lbl.getPreferredSize();
            maxw = Math.max(maxw, dim.width);
            maxh = Math.max(maxh, dim.height);
        }
        final Dimension dim = new Dimension(maxw, maxh);
        for (final JLabel lbl : lbls) {
            lbl.setPreferredSize(dim);
        }

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.setBackground(Skin.LIGHT);
        flow1.add(lbls[0]);

        this.incidentDateMonth = new JComboBox<>(Month.values());
        this.incidentDateMonth.setFont(Skin.BODY_12_FONT);
        this.incidentDateDay = new JTextField(2);
        this.incidentDateDay.setFont(Skin.BODY_12_FONT);
        this.incidentDateYear = new JTextField(4);
        this.incidentDateYear.setFont(Skin.BODY_12_FONT);

        flow1.add(this.incidentDateMonth);
        flow1.add(this.incidentDateDay);
        final JLabel commaLbl = new JLabel(", ");
        flow1.add(commaLbl);
        flow1.add(this.incidentDateYear);
        form.add(flow1, BorderLayout.NORTH);

        final JPanel inner1 = new JPanel(new BorderLayout());
        inner1.setBackground(Skin.LIGHT);
        form.add(inner1, BorderLayout.CENTER);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.setBackground(Skin.LIGHT);
        flow2.add(lbls[1]);
        this.interviewer = new JTextField(20);
        this.interviewer.setFont(Skin.BODY_12_FONT);
        flow2.add(this.interviewer);
        inner1.add(flow2, BorderLayout.NORTH);

        final JPanel inner2 = new JPanel(new BorderLayout());
        inner2.setBackground(Skin.LIGHT);
        inner1.add(inner2, BorderLayout.CENTER);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.setBackground(Skin.LIGHT);
        flow3.add(lbls[2]);
        this.proctor = new JTextField(20);
        this.proctor.setFont(Skin.BODY_12_FONT);
        flow3.add(this.proctor);
        inner2.add(flow3, BorderLayout.NORTH);

        final JPanel inner3 = new JPanel(new BorderLayout());
        inner3.setBackground(Skin.LIGHT);
        inner2.add(inner3, BorderLayout.CENTER);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.setBackground(Skin.LIGHT);
        flow4.add(lbls[3]);
        this.incidentType = new JComboBox<>(EDisciplineIncidentType.values());
        this.incidentType.setSelectedItem(EDisciplineIncidentType.OTHER);
        this.incidentType.setFont(Skin.BODY_12_FONT);
        flow4.add(this.incidentType);
        inner3.add(flow4, BorderLayout.NORTH);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.setBackground(Skin.LIGHT);
        flow5.add(lbls[4]);
        this.course = new JComboBox<>(COURSES);
        flow5.add(this.course);
        final JLabel unitLbl = new JLabel(", Unit ");
        flow5.add(unitLbl);
        this.unit = new JComboBox<>(UNITS);
        flow5.add(this.unit);
        inner3.add(flow5, BorderLayout.SOUTH);

        final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.setBackground(Skin.LIGHT);

        lbls[5].setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        final Dimension pref5 = lbls[5].getPreferredSize();
        lbls[5].setPreferredSize(new Dimension(pref5.width, pref5.height + 15));
        flow6.add(lbls[5]);

        this.description = new JTextArea(2, 40);
        this.description.setFont(Skin.BODY_12_FONT);
        flow6.add(this.description);
        inner2.add(flow6, BorderLayout.SOUTH);

        final JPanel flow7 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.setBackground(Skin.LIGHT);
        flow7.add(lbls[6]);
        this.actionType = new JComboBox<>(EDisciplineActionType.values());
        this.actionType.setFont(Skin.BODY_12_FONT);
        flow7.add(this.actionType);
        inner1.add(flow7, BorderLayout.SOUTH);

        final JPanel flow8 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.setBackground(Skin.LIGHT);

        lbls[7].setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        final Dimension pref7 = lbls[7].getPreferredSize();
        lbls[7].setPreferredSize(new Dimension(pref7.width, pref7.height + 15));
        flow8.add(lbls[7]);

        this.comments = new JTextArea(2, 40);
        this.comments.setFont(Skin.BODY_12_FONT);
        flow8.add(this.comments);
        form.add(flow8, BorderLayout.SOUTH);

        // Buttons

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.setBackground(Skin.WHITE);

        final JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand(SUBMIT_CMD);
        submitButton.addActionListener(this);
        buttons.add(submitButton);

        final JButton resetButton = new JButton("Reset");
        resetButton.setActionCommand(RESET_CMD);
        resetButton.addActionListener(this);
        buttons.add(resetButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(CANCEL_CMD);
        cancelButton.addActionListener(this);
        buttons.add(cancelButton);

        this.err = new JLabel(CoreConstants.SPC);
        this.err.setForeground(Skin.ERROR_COLOR);
        this.err.setFont(Skin.BODY_12_FONT);
        buttons.add(this.err);

        center.add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Resets the form.
     */
    public void reset() {

        this.studentId = null;

        final LocalDate today = LocalDate.now();

        this.incidentDateMonth.setSelectedItem(today.getMonth());
        this.incidentDateDay.setText(Integer.toString(today.getDayOfMonth()));
        this.incidentDateYear.setText(Integer.toString(today.getYear()));

        this.interviewer.setText(CoreConstants.EMPTY);
        this.proctor.setText(CoreConstants.EMPTY);
        this.incidentType.setSelectedItem(EDisciplineIncidentType.OTHER);
        this.course.setSelectedIndex(0);
        this.unit.setSelectedIndex(0);
        this.description.setText(CoreConstants.EMPTY);
        this.actionType.setSelectedItem(EDisciplineActionType.VERBAL_REPRIMAND);
        this.comments.setText(CoreConstants.EMPTY);

        this.incidentDateYear.setBackground(Skin.FIELD_BG);
        this.incidentDateDay.setBackground(Skin.FIELD_BG);

        this.err.setText(CoreConstants.SPC);
    }

    /**
     * Sets the current student ID.
     *
     * @param theStudentId the student ID
     */
    public void setStudentId(final String theStudentId) {

        this.studentId = theStudentId;
    }

    /**
     * Called when the "Submit" or "Reset" button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (SUBMIT_CMD.equals(cmd)) {
            processSubmit();
        } else if (RESET_CMD.equals(cmd)) {
            reset();
        } else if (CANCEL_CMD.equals(cmd)) {
            reset();
            this.owner.cancelAdd();
        }
    }

    /**
     * Handles a "Submit" action.
     */
    private void processSubmit() {

        int year = -1;
        int day = -1;
        boolean valid = true;

        try {
            year = Integer.parseInt(this.incidentDateYear.getText());
            if (year > 2000 && year < 3000) {
                this.incidentDateYear.setBackground(Skin.FIELD_BG);
            } else if (year > 0 && year < 100) {
                year += 2000;
                this.incidentDateYear.setBackground(Skin.FIELD_BG);
            } else {
                Log.warning("invalid year");
                this.incidentDateYear.setBackground(Skin.FIELD_ERROR_BG);
                valid = false;
            }
        } catch (final NumberFormatException ex) {
            Log.warning("invalid year", ex);
            this.incidentDateYear.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        try {
            day = Integer.parseInt(this.incidentDateDay.getText());
            this.incidentDateDay.setBackground(Skin.FIELD_BG);
        } catch (final NumberFormatException ex) {
            Log.warning("invalid day", ex);
            this.incidentDateDay.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        LocalDate date = null;
        try {
            date = LocalDate.of(year, (Month) this.incidentDateMonth.getSelectedItem(), day);
        } catch (final DateTimeException ex) {
            Log.warning("invalid day", ex);
            this.incidentDateDay.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        final String courseStr = (String) this.course.getSelectedItem();
        final String actualCourse = COURSES[0].equals(courseStr) ? null : courseStr;

        final String unitStr = (String) this.unit.getSelectedItem();
        final Integer actualUnit = UNITS[0].equals(unitStr) ? null : Integer.valueOf(unitStr);

        String descr = this.description.getText();
        if (descr.length() > 100) {
            descr = descr.substring(0, 100);
        }

        String comment = this.comments.getText();
        if (comment.length() > 100) {
            comment = comment.substring(0, 100);
        }

        String inter = this.interviewer.getText();
        if (inter.length() > 20) {
            inter = inter.substring(0, 20);
        }

        String proc = this.proctor.getText();
        if (proc.length() > 20) {
            proc = proc.substring(0, 20);
        }

        if (valid) {
            final RawDiscipline record = new RawDiscipline(this.studentId, date,
                    ((EDisciplineIncidentType) this.incidentType.getSelectedItem()).code, actualCourse,
                    actualUnit, descr, ((EDisciplineActionType) this.actionType.getSelectedItem()).code,
                    comment, inter, proc);

            // NOTE: on success, this will reset this card and make the incident list card active.
            // On failure, this card should display the error
            final String error = this.owner.createRecord(record);

            if (error != null) {
                this.err.setText(error);
            }
        }
    }
}
