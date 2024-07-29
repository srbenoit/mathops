package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A panel that shows the student's Transfer Credit.
 */
final class PlacementTransferPanel extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String ADD_TRANSFER_CMD = "ADD_TRANSFER";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7889549799220741800L;

    /** The data cache. */
    private final Cache cache;

    /** A table that shows all transfer credit. */
    private final JTableTransferCredit transferTable;

    /** Scroll pane for transfer credit results. */
    private final JScrollPane transferScroll;

    /** Button to add transfer credit. */
    private final JButton addTransfer;

    /** An error message. */
    private final JLabel error;

    /** Data on the current student. */
    private StudentData currentStudentData = null;

    /** The dialog to add a transfer credit record. */
    private DlgAddTransfer addTransferDialog = null;

    /**
     * Constructs a new {@code PlacementTransferPanel}.
     *
     * @param theCache the data cache
     * @param theFixed the fixed data container
     */
    PlacementTransferPanel(final Cache theCache, final FixedData theFixed) {

        super();
        setBackground(Skin.WHITE);

        this.cache = theCache;

        final Integer placementPermission = theFixed.getClearanceLevel("STU_PLCMT");
        final boolean addTransferAllowed = placementPermission != null && placementPermission.intValue() < 4;

        final JPanel north = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        north.setBackground(Skin.WHITE);
        north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));
        add(north, StackedBorderLayout.NORTH);

        final JPanel transferBlock = new JPanel(new BorderLayout(5, 5));
        transferBlock.setBackground(Skin.WHITE);
        transferBlock.add(makeHeader("Transfer Credit", false), BorderLayout.PAGE_START);

        this.transferTable = new JTableTransferCredit();
        this.transferTable.setFillsViewportHeight(true);

        this.transferScroll = new JScrollPane(this.transferTable);
        this.transferScroll.setPreferredSize(this.transferTable.getPreferredScrollSize(this.transferScroll, 3));
        transferBlock.add(this.transferScroll, BorderLayout.CENTER);

        // See if this user is allowed to add transfer credit...
        this.addTransfer = new JButton("Add Transfer Credit...");

        if (addTransferAllowed) {
            final JPanel transferButtonBar = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
            transferButtonBar.setBackground(Skin.WHITE);
            this.addTransfer.setFont(Skin.BUTTON_13_FONT);
            this.addTransfer.setActionCommand(ADD_TRANSFER_CMD);
            this.addTransfer.addActionListener(this);
            this.addTransfer.setEnabled(false);
            transferButtonBar.add(this.addTransfer);
            transferBlock.add(transferButtonBar, BorderLayout.PAGE_END);
        }

        add(transferBlock, StackedBorderLayout.WEST);

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

            this.transferScroll.setPreferredSize(this.transferTable.getPreferredScrollSize(this.transferScroll, 3));
        }
    }

    /**
     * Clears all displayed fields.
     */
    void clearDisplay() {

        this.transferTable.clear();
        this.addTransfer.setEnabled(false);

        if (this.addTransferDialog != null) {
            this.addTransferDialog.setVisible(false);
            this.addTransferDialog.dispose();
            this.addTransferDialog = null;
        }
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.currentStudentData = data;

        this.transferTable.clear();
        this.transferTable.addData(data.studentTransferCredit, 2);
        this.addTransfer.setEnabled(true);
    }

    /**
     * Called after transfer credit has been updated.
     */
    void updateTransferCreditList() {

        if (this.currentStudentData != null) {
            this.currentStudentData.updateTransferCreditList(this.cache);
            populateDisplay(this.currentStudentData);
        }
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_TRANSFER_CMD.equals(cmd)) {

            if (this.currentStudentData != null) {
                if (this.addTransferDialog == null) {
                    this.addTransferDialog = new DlgAddTransfer(this.cache, this);
                }

                this.addTransferDialog.populateDisplay(this.currentStudentData);
                this.addTransferDialog.setVisible(true);
                this.addTransferDialog.toFront();
            }
        }
    }
}
