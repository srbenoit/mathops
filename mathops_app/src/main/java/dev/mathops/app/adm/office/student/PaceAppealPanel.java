package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A panel that creates or edits a "Pace appeal" record.
 */
final class PaceAppealPanel extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3042908501448883872L;

    /** The "interviewer" field. */
    private final JTextField interviewerField;

    /** The "appeal date" field. */
    private final JTextField appealDateField;

    /** The "relief given" checkbox. */
    private final JCheckBox reliefGiven;

    /** The "new deadline" field. */
    private final JTextField newDeadlineField;

    /** The "# attempts" field. */
    private final JTextField nbrAttemptsField;

    /** The "circumstances" text area. */
    private final JTextArea circumstancesArea;

    /** The "comments" text area. */
    private final JTextArea commentsArea;

    /** The "Apply" button. */
    private final JButton applyBtn;

    /** The record currently being edited; null if a new record is being created. */
    private RawPaceAppeals recordBeingEdited = null;

    /** The record currently being created; null if a record is being edited. */
    private RawPaceAppeals recordBeingCreated = null;

    /**
     * Constructs a new {@code PaceAppealPanel}.
     *
     */
    PaceAppealPanel() {

        super(new StackedBorderLayout(5, 5));

        setBackground(Skin.LIGHTEST);
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        final JLabel[] labels = new JLabel[5];
        labels[0] = new JLabel("Interviewer:");
        labels[1] = new JLabel("Appeal Date:");
        labels[2] = new JLabel("Relief Given:");
        labels[3] = new JLabel("New Deadline:");
        labels[4] = new JLabel("# Attempts:");
        int maxW = 0;
        int maxH = 0;
        for (final JLabel lbl : labels) {
            lbl.setFont(Skin.MEDIUM_15_FONT);
            final Dimension pref = lbl.getPreferredSize();
            maxW = Math.max(maxW, pref.width);
            maxH = Math.max(maxH, pref.height);
        }
        final Dimension newPref = new Dimension(maxW, maxH);
        for (final JLabel lbl : labels) {
            lbl.setPreferredSize(newPref);
        }

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
        row1.setBackground(Skin.LIGHTEST);
        row1.add(labels[0]);
        this.interviewerField = new JTextField(15);
        this.interviewerField.setEnabled(false);
        row1.add(this.interviewerField);
        add(row1, BorderLayout.NORTH);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
        row2.setBackground(Skin.LIGHTEST);
        row2.add(labels[1]);
        this.appealDateField = new JTextField(15);
        this.appealDateField.setEnabled(false);
        row2.add(this.appealDateField);
        add(row2, BorderLayout.NORTH);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
        row3.setBackground(Skin.LIGHTEST);
        row3.add(labels[2]);
        this.reliefGiven = new JCheckBox();
        this.reliefGiven.setEnabled(false);
        row3.add(this.reliefGiven);
        add(row3, BorderLayout.NORTH);

        final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
        row4.setBackground(Skin.LIGHTEST);
        row4.add(labels[3]);
        this.newDeadlineField = new JTextField(15);
        this.newDeadlineField.setEnabled(false);
        row4.add(this.newDeadlineField);
        add(row4, BorderLayout.NORTH);

        final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
        row5.setBackground(Skin.LIGHTEST);
        row5.add(labels[4]);
        this.nbrAttemptsField = new JTextField(15);
        this.nbrAttemptsField.setEnabled(false);
        row5.add(this.nbrAttemptsField);
        add(row5, BorderLayout.NORTH);

        final JLabel lbl5 = new JLabel("Circumstances:");
        lbl5.setFont(Skin.MEDIUM_15_FONT);
        add(lbl5, BorderLayout.NORTH);
        this.circumstancesArea = new JTextArea(3, 20);
        this.circumstancesArea.setEnabled(false);
        add(this.circumstancesArea, BorderLayout.NORTH);

        final JLabel lbl6 = new JLabel("Comments:");
        lbl6.setFont(Skin.MEDIUM_15_FONT);
        add(lbl6, BorderLayout.NORTH);
        this.commentsArea = new JTextArea(3, 20);
        this.commentsArea.setEnabled(false);
        add(this.commentsArea, BorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        buttons.setBackground(Skin.LIGHTEST);
        this.applyBtn = new JButton("Apply");
        this.applyBtn.setEnabled(false);
        this.applyBtn.setFont(Skin.MEDIUM_15_FONT);
        buttons.add(this.applyBtn);
        add(buttons, BorderLayout.NORTH);
    }

    /**
     * Sets a record to edit. This populates all field values from that record, allows editing, and when "Apply" is
     * pressed, the record will be updated.
     *
     * @param theRecord the record to edit; {@code null} to clear and disable the form
     */
    public void editRecord(final RawPaceAppeals theRecord) {

        this.recordBeingEdited = theRecord;
        this.recordBeingCreated = null;

        populateFields(theRecord);
    }

    /**
     * Sets a record to create. This populates any supplied field values from that record, allows editing, and when
     * "Apply" is pressed, the record will be created.
     *
     * @param theRecord the record to create; {@code null} to clear and disable the form
     */
    public void createRecord(final RawPaceAppeals theRecord) {

        this.recordBeingEdited = null;
        this.recordBeingCreated = theRecord;

        populateFields(theRecord);
    }

    /**
     * Populates the fields from a record.
     *
     * @param theRecord the record; {@code null} to clear and disable the form
     */
    private void populateFields(final RawPaceAppeals theRecord) {

        final boolean enabled;
        if (theRecord == null) {
            this.interviewerField.setText(CoreConstants.EMPTY);
            this.appealDateField.setText(CoreConstants.EMPTY);
            this.newDeadlineField.setText(CoreConstants.EMPTY);
            this.nbrAttemptsField.setText(CoreConstants.EMPTY);
            this.circumstancesArea.setText(CoreConstants.EMPTY);
            this.commentsArea.setText(CoreConstants.EMPTY);
            enabled = false;
        } else {
            final String interviewerStr =
                    theRecord.interviewer == null ? CoreConstants.EMPTY : theRecord.interviewer;
            this.interviewerField.setText(interviewerStr);

            final String appealDtStr = theRecord.appealDt == null ? CoreConstants.EMPTY
                    : TemporalUtils.FMT_MDY.format(theRecord.appealDt);
            this.appealDateField.setText(appealDtStr);

            final String newDeadlineStr = theRecord.newDeadlineDt == null ? CoreConstants.EMPTY
                    : TemporalUtils.FMT_MDY.format(theRecord.newDeadlineDt);
            this.newDeadlineField.setText(newDeadlineStr);

            final String nbrAtmptsStr = theRecord.nbrAtmptsAllow == null ? CoreConstants.EMPTY
                    : theRecord.nbrAtmptsAllow.toString();
            this.nbrAttemptsField.setText(nbrAtmptsStr);

            final String circumstancesStr =
                    theRecord.circumstances == null ? CoreConstants.EMPTY : theRecord.circumstances;
            this.circumstancesArea.setText(circumstancesStr);

            final String commentStr =
                    theRecord.comment == null ? CoreConstants.EMPTY : theRecord.comment;
            this.commentsArea.setText(commentStr);

            enabled = true;
        }

        this.interviewerField.setEnabled(enabled);
        this.appealDateField.setEnabled(enabled);
        this.reliefGiven.setEnabled(enabled);
        this.newDeadlineField.setEnabled(enabled);
        this.nbrAttemptsField.setEnabled(enabled);
        this.circumstancesArea.setEnabled(enabled);
        this.commentsArea.setEnabled(enabled);
        this.applyBtn.setEnabled(enabled);
    }

    /**
     * Called when the "Apply" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (this.recordBeingCreated == null) {
            if (this.recordBeingEdited != null) {

                // Update the record
            }
        } else {

            // Create the record
        }

    }
}
