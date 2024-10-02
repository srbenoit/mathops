package dev.mathops.app.sim.rooms;

import dev.mathops.app.sim.SpurSimulation;
import dev.mathops.app.sim.SpurSimulationData;
import dev.mathops.app.sim.SpurSimulationDataListener;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel within the room sets dialog that displays the list of all room sets that have been defined.  A simulation run
 * will select one of these room sets.  Each set has a unique name (which the table displays).  Selecting a set name
 * populates the room set panel with the rooms in that set.
 */
final class RoomSetsDlgRoomSetsList extends JPanel implements ActionListener, SpurSimulationDataListener {

    /** An action command. */
    private static final String CMD_ADD_ROOM_SET = "ADD_ROOM_SET";

    /** The simulation configuration data. */
    private final SpurSimulationData data;

    /**
     * Constructs a new {@code RoomSetsDlgRoomSetsList}.
     */
    RoomSetsDlgRoomSetsList(final SpurSimulationData theData) {

        super(new StackedBorderLayout(3, 3));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SpurSimulation.ACCENT_COLOR),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        this.data = theData;

        final RoomSetsListModel listModel = theData.getRoomSetListModel();
        final JList<RoomSet> list = new JList<>(listModel);

        final JScrollPane scroll = new JScrollPane();
        scroll.setPreferredSize(list.getPreferredSize());
        scroll.setViewportView(list);

        final JPanel center = new JPanel(new StackedBorderLayout());
        center.setBorder(BorderFactory.createEtchedBorder());
        center.setBackground(list.getBackground());
        center.add(scroll, StackedBorderLayout.WEST);

        add(center, StackedBorderLayout.CENTER);

        final JButton addRoom = new JButton("Add Room Set...");
        addRoom.setActionCommand(CMD_ADD_ROOM_SET);
        addRoom.addActionListener(this);

        final JPanel buttonBar1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        buttonBar1.add(addRoom);
        add(buttonBar1, StackedBorderLayout.SOUTH);

        theData.addListener(this);
        updateSimulationData();
    }

    /**
     * Closes this table and any open dialogs (called when its containing window closes).
     */
    void close() {

        if (this.addRoomDialog != null) {
            this.addRoomDialog.setVisible(false);
            this.addRoomDialog.dispose();
            this.addRoomDialog = null;
        }
    }

    /**
     * Called when a button is activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_ADD_ROOM_SET.equals(cmd)) {
            if (this.addRoomDialog == null) {
                final RoomSetTableModel tableModel = this.data.getCampusRoomsTableModel();
                this.addRoomDialog = new RoomSetsDlgAddCampusRoom(tableModel);
            } else {
                this.addRoomDialog.reset();
            }

            final Point location = getLocationOnScreen();
            final Dimension size = getSize();
            final Dimension dialogSize = this.addRoomDialog.getSize();

            final int x = location.x + (size.width - dialogSize.width) / 2;
            final int y = location.y + (size.height - dialogSize.height) / 2;
            this.addRoomDialog.setLocation(x, y);

            this.addRoomDialog.setVisible(true);
            this.addRoomDialog.toFront();
        }
    }

    /**
     * Called (on the AWT thread) when the simulation data is updated.
     */
    public void updateSimulationData() {

    }
}
