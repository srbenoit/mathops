package dev.mathops.app.adm.management.general;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.rec.main.FacilityRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to add a facility.
 */
final class AddFacilityDialog extends JFrame implements ActionListener {

    /** An action command. */
    private static final String CREATE_CMD = "CREATE";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The owning panel to notify on "Submit". */
    private final GeneralFacilitiesPanel owner;

    /** A field to enter the facility ID. */
    private final TextField id;

    /** A field to enter the facility name. */
    private final TextField name;

    /** A field to enter the facility building name. */
    private final TextField building;

    /** A field to enter the facility room number. */
    private final TextField room;

    /**
     * Constructs a new {@code AddFacilityDialog}.,
     *
     * @param theOwner the owning panel to notify on "Submit".
     */
    AddFacilityDialog(final GeneralFacilitiesPanel theOwner) {

        super("Create new Facility");

        this.owner = theOwner;

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.setBackground(Skin.OFF_WHITE_GREEN);
        setContentPane(content);

        final JLabel header = AdmPanelBase.makeHeader("Create a new Facility", false);
        add(header, StackedBorderLayout.NORTH);

        final JTextArea notes1 = new JTextArea(3, 30);
        notes1.setEditable(false);
        notes1.setLineWrap(false);
        notes1.setBackground(Skin.OFF_WHITE_GREEN);
        notes1.setText("""
                A "facility" can represent a physical facility like a testing center,
                classroom, or office, or a virtual facility like online help or
                tutoring in a student-facing location like ALVS.""");
        notes1.setFont(Skin.BODY_12_FONT);
        add(notes1, StackedBorderLayout.NORTH);

        final JTextArea notes2 = new JTextArea(1, 30);
        notes2.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
        notes2.setEditable(false);
        notes2.setLineWrap(false);
        notes2.setBackground(Skin.OFF_WHITE_GREEN);
        notes2.setText("For virtual facilities, building and room number can be blank.");
        notes2.setFont(Skin.BODY_12_FONT);
        add(notes2, StackedBorderLayout.NORTH);

        this.id = new TextField(7);
        this.id.setFont(Skin.BODY_12_FONT);

        this.name = new TextField(30);
        this.name.setFont(Skin.BODY_12_FONT);

        this.building = new TextField(20);
        this.building.setFont(Skin.BODY_12_FONT);

        this.room = new TextField(7);
        this.room.setFont(Skin.BODY_12_FONT);

        final JLabel[] labels = new JLabel[4];
        labels[0] = AdmPanelBase.makeLabelMedium("Facility ID: ");
        labels[1] = AdmPanelBase.makeLabelMedium("Facility Name: ");
        labels[2] = AdmPanelBase.makeLabelMedium("Building Name: ");
        labels[3] = AdmPanelBase.makeLabelMedium("Room Number: ");
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        flow1.setBackground(Skin.OFF_WHITE_GREEN);
        flow1.add(labels[0]);
        flow1.add(this.id);
        add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        flow2.setBackground(Skin.OFF_WHITE_GREEN);
        flow2.add(labels[1]);
        flow2.add(this.name);
        add(flow2, StackedBorderLayout.NORTH);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        flow3.setBackground(Skin.OFF_WHITE_GREEN);
        flow3.add(labels[2]);
        flow3.add(this.building);
        add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        flow4.setBackground(Skin.OFF_WHITE_GREEN);
        flow4.add(labels[3]);
        flow4.add(this.room);
        add(flow4, StackedBorderLayout.NORTH);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        buttonFlow.setBackground(Skin.OFF_WHITE_GREEN);
        final JButton createBtn = new JButton("Create Facility");
        createBtn.setActionCommand(CREATE_CMD);
        final JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setActionCommand(CANCEL_CMD);
        buttonFlow.add(createBtn);
        buttonFlow.add(cancelBtn);
        add(buttonFlow, StackedBorderLayout.SOUTH);

        pack();
        setResizable(false);

        final Dimension mySize = getSize();
        final Point ownerLocation = theOwner.getLocationOnScreen();
        final Dimension ownerSize = theOwner.getSize();

        final int myX = ownerLocation.x + (ownerSize.width - mySize.width) / 2;
        final int myY = ownerLocation.y + (ownerSize.height - mySize.height) / 4;
        setLocation(myX, myY);

        createBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        setVisible(true);
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CREATE_CMD.equals(cmd)) {
            final String theId = this.id.getText();
            final String theName = this.name.getText();
            final String theBuilding = this.building.getText();
            final String theRoom = this.room.getText();

            boolean good = true;
            if (theId == null || theId.isBlank()) {
                this.id.setBackground(Skin.FIELD_ERROR_BG);
                good = false;
            } else {
                this.id.setBackground(Skin.FIELD_BG);
            }
            if (theName == null || theName.isBlank()) {
                this.name.setBackground(Skin.FIELD_ERROR_BG);
                good = false;
            } else {
                this.name.setBackground(Skin.FIELD_BG);
            }

            if (good) {
                final FacilityRec rec = new FacilityRec(theId, theName, theBuilding, theRoom);
                final String[] error = this.owner.addFacility(rec);
                if (error == null) {
                    close();
                } else {
                    JOptionPane.showMessageDialog(this, error, "Create new Facility", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            close();
        }
    }

    /**
     * Clears and closes the dialog.
     */
    private void close() {

        this.id.setText(CoreConstants.EMPTY);
        this.name.setText(CoreConstants.EMPTY);
        this.building.setText(CoreConstants.EMPTY);
        this.room.setText(CoreConstants.EMPTY);
        setVisible(false);
    }
}
