package dev.mathops.app.sim.swing;

import dev.mathops.app.sim.rooms.CampusRoom;
import dev.mathops.app.sim.rooms.CampusRoomsTableModel;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to add a new campus room.
 */
final class ClassroomDialogAddCampusRoom extends JFrame implements ActionListener, DocumentListener {

    /** An action command. */
    private static final String CMD_CREATE_ROOM = "CREATE_ROOM";

    /** An action command. */
    private static final String CMD_CANCEL = "CANCEL";

    /** The table model to which to add a room when created. */
    private final CampusRoomsTableModel tableModel;

    /** The field with the room name. */
    private final JTextField nameField;

    /** The field with the room capacity. */
    private final JTextField capField;

    /** An error message. */
    private final JLabel error;

    /** The button to create a room. */
    private final JButton createRoomBtn;

    /**
     * Constructs a new {@code ClassroomDialogAddCampusRoom}.
     */
    ClassroomDialogAddCampusRoom(final CampusRoomsTableModel theTableModel) {

        super("Add a Campus Room");

        this.tableModel = theTableModel;

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);

        this.nameField = new JTextField(15);
        this.capField = new JTextField(5);
        this.error = new JLabel(CoreConstants.SPC);
        this.error.setForeground(Color.RED);

        final JLabel[] labels = new JLabel[2];
        labels[0] = new JLabel("Room Name:");
        labels[1] = new JLabel("Maximum Capacity:");
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        row1.add(labels[0]);
        row1.add(this.nameField);
        content.add(row1, StackedBorderLayout.NORTH);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        row2.add(labels[1]);
        row2.add(this.capField);
        content.add(row2, StackedBorderLayout.NORTH);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        row3.add(this.error);
        content.add(row3, StackedBorderLayout.NORTH);

        this.createRoomBtn = new JButton("Create Room");
        this.createRoomBtn.setActionCommand(CMD_CREATE_ROOM);
        this.createRoomBtn.setEnabled(false);

        final JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setActionCommand(CMD_CANCEL);

        final JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));
        buttonBar.add(this.createRoomBtn);
        buttonBar.add(cancelBtn);
        add(buttonBar, StackedBorderLayout.SOUTH);

        pack();

        this.nameField.getDocument().addDocumentListener(this);
        this.capField.getDocument().addDocumentListener(this);
        this.createRoomBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
    }

    /**
     * Resets the dialog.
     */
    void reset() {

        this.nameField.setText(CoreConstants.EMPTY);
        this.capField.setText(CoreConstants.EMPTY);
        this.createRoomBtn.setEnabled(false);

        this.error.setText(CoreConstants.SPC);
    }

    /**
     * Called when a button is activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_CREATE_ROOM.equals(cmd)) {
            final String name = this.nameField.getText();
            final String cap = this.capField.getText();

            if (!(name.isBlank() || cap.isBlank())) {
                final String trimmedName = name.trim();

                if (this.tableModel.getByName(trimmedName) == null) {
                    try {
                        final String trimmed = cap.trim();
                        final int capacity = Integer.parseInt(trimmed);
                        if (capacity > 0) {
                            final CampusRoom newRoom = new CampusRoom(name, capacity);
                            this.tableModel.add(newRoom);
                        }
                    } catch (final NumberFormatException ex) {
                        Log.warning(ex);
                    }
                }
            }

            setVisible(false);
        } else if (CMD_CANCEL.equals(cmd)) {
            setVisible(false);
        }
    }

    /**
     * Called when data is inserted into a text field's document.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        validateFields();
    }

    /**
     * Called when data is removed from a text field's document.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        validateFields();
    }

    /**
     * Called when data is updated in a text field's document.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        validateFields();
    }

    /**
     * Validates field contents and sets the enabled status of the "Create Room" button.
     */
    private void validateFields() {

        final String name = this.nameField.getText();
        final String cap = this.capField.getText();

        boolean valid = !(name.isBlank() || cap.isBlank());

        if (valid) {
            final String trimmedName = name.trim();
            valid = this.tableModel.getByName(trimmedName) == null;

            if (valid) {
                try {
                    final String trimmed = cap.trim();
                    final int capacity = Integer.parseInt(trimmed);
                    valid = capacity > 0;
                    if (valid) {
                        this.error.setText(CoreConstants.SPC);
                    } else {
                        this.error.setText("Invalid room capacity.");
                    }
                } catch (final NumberFormatException ex) {
                    valid = false;
                    this.error.setText("Invalid room capacity.");
                }
            } else {
                this.error.setText("Room name already exists.");
            }

            this.createRoomBtn.setEnabled(valid);
        }
    }
}