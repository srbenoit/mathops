package dev.mathops.app.adm.student;

import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawHoldType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
 * A card within the "Holds" tab of the admin app that allows the user to add a new hold.
 */
/* default */ class HoldsAddCard extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3081496571704009194L;

    /** An action command. */
    private static final String SUBMIT_CMD = "SUBMIT";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The owning discipline panel. */
    private final StudentHoldsPanel owner;

    /** The fixed data. */
    private final FixedData fixed;

    /** The current student ID. */
    private String studentId;

    /** The hold ID field. */
    private final JComboBox<String> holdId;

    /** The incident date month field. */
    private final JComboBox<Month> holdDateMonth;

    /** The incident date day field. */
    private final JTextField holdDateDay;

    /** The incident date year field. */
    private final JTextField holdDateYear;

    /** An error message. */
    private final JLabel err;

    /**
     * Constructs a new {@code HoldsAddCard}.
     *
     * @param theOwner         the owning discipline panel
     * @param theFixed         the fixed data
     */
    /* default */ HoldsAddCard(final StudentHoldsPanel theOwner, final FixedData theFixed) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.owner = theOwner;
        this.fixed = theFixed;

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        add(center, BorderLayout.CENTER);

        final JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Skin.LIGHT);
        form.setBorder(BorderFactory.createCompoundBorder(//
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        center.add(form, BorderLayout.NORTH);

        final JLabel[] lbls = new JLabel[2];
        lbls[0] = new JLabel("Hold:");
        lbls[1] = new JLabel("When Applied:");
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

        // Generate the list of hold IDs with descriptions for the dropdown
        final String[] holdIds = new String[this.fixed.holdTypes.size()];
        int i = 0;
        for (final RawHoldType type : this.fixed.holdTypes) {
            holdIds[i] = type.holdId + ": "
                    + RawAdminHoldLogic.getStaffMessage(type.holdId) + " ("
                    + type.sevAdminHold + ")";
            ++i;
        }

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.setBackground(Skin.LIGHT);
        flow1.add(lbls[0]);
        this.holdId = new JComboBox<>(holdIds);
        flow1.add(this.holdId);
        form.add(flow1, BorderLayout.NORTH);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.setBackground(Skin.LIGHT);
        flow2.add(lbls[1]);

        this.holdDateMonth = new JComboBox<>(Month.values());
        this.holdDateMonth.setFont(Skin.BODY_12_FONT);
        this.holdDateDay = new JTextField(2);
        this.holdDateDay.setFont(Skin.BODY_12_FONT);
        this.holdDateYear = new JTextField(4);
        this.holdDateYear.setFont(Skin.BODY_12_FONT);

        flow2.add(this.holdDateMonth);
        flow2.add(this.holdDateDay);
        final JLabel commaLbl = new JLabel(", ");
        flow2.add(commaLbl);
        flow2.add(this.holdDateYear);
        form.add(flow2, BorderLayout.CENTER);

        // Buttons

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.setBackground(Skin.WHITE);

        final JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand(SUBMIT_CMD);
        submitButton.addActionListener(this);
        buttons.add(submitButton);

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
    /* default */ void reset() {

        final LocalDate today = LocalDate.now();

        this.holdId.setSelectedIndex(0);

        this.holdDateMonth.setSelectedItem(today.getMonth());
        this.holdDateDay.setText(Integer.toString(today.getDayOfMonth()));
        this.holdDateYear.setText(Integer.toString(today.getYear()));

        this.err.setText(CoreConstants.SPC);
    }

    /**
     * Sets the current student ID.
     *
     * @param theStudentId the student ID
     */
    public void setStudentId(final String theStudentId) {

        // Log.info("Student ID set to " + theStudentId);

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

        final String selectedHold = (String) this.holdId.getSelectedItem();
        final String selHoldId = selectedHold == null ? null : selectedHold.substring(0, 2);

        try {
            year = Integer.parseInt(this.holdDateYear.getText());
            if (year > 2000 && year < 3000) {
                this.holdDateYear.setBackground(Skin.FIELD_BG);
            } else if (year > 0 && year < 100) {
                year += 2000;
                this.holdDateYear.setBackground(Skin.FIELD_BG);
            } else {
                Log.warning("invalid year");
                this.holdDateYear.setBackground(Skin.FIELD_ERROR_BG);
                valid = false;
            }
        } catch (final NumberFormatException ex) {
            Log.warning("invalid year", ex);
            this.holdDateYear.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        try {
            day = Integer.parseInt(this.holdDateDay.getText());
            this.holdDateDay.setBackground(Skin.FIELD_BG);
        } catch (final NumberFormatException ex) {
            Log.warning("invalid day", ex);
            this.holdDateDay.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        LocalDate date = null;
        try {
            date = LocalDate.of(year, (Month) this.holdDateMonth.getSelectedItem(), day);
        } catch (final DateTimeException ex) {
            Log.warning("invalid day", ex);
            this.holdDateDay.setBackground(Skin.FIELD_ERROR_BG);
            valid = false;
        }

        RawHoldType holdType = null;
        for (final RawHoldType test : this.fixed.holdTypes) {
            if (test.holdId.equals(selHoldId)) {
                holdType = test;
                break;
            }
        }

        if (holdType == null) {
            Log.warning("invalid hold ID: ", selHoldId);
        } else if (valid) {
            final RawAdminHold record = new RawAdminHold(this.studentId, selHoldId,
                    holdType.sevAdminHold, Integer.valueOf(0), date);

            // NOTE: on success, this will reset this card and make the hold list card active.
            // On failure, this card should display the error
            final String error = this.owner.createRecord(record);

            if (error != null) {
                this.err.setText(error);
            }
        }
    }
}
