package dev.mathops.app.sim.rooms;

import dev.mathops.app.sim.SpurSimulation;
import dev.mathops.app.sim.SpurSimulationData;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel within the room sets dialog that displays the list of all room sets that have been defined.  A simulation run
 * will select one of these room sets.  Each set has a unique name (which the table displays).  Selecting a set name
 * populates the room set panel with the rooms in that set.
 *
 * <p>
 * This panel also has buttons for the user to add a new set or delete an existing set.
 */
final class RoomSetsDlgRoomSetsList extends JPanel implements ActionListener, ListSelectionListener {

    /** An action command. */
    private static final String CMD_ADD_ROOM_SET = "ADD_ROOM_SET";

    /** An action command. */
    private static final String CMD_DELETE_ROOM_SET = "DELETE_ROOM_SET";

    /** The containing dialog. */
    private final RoomSetsDlg container;

    /** The list model. */
    private final RoomSetsListModel listModel;

    /** The list control. */
    private final JList<RoomSet> list;

    /** The button to delete a room set - enabled only when a room set is selected. */
    private final JButton deleteRoomSet;

    /**
     * Constructs a new {@code RoomSetsDlgRoomSetsList}.
     */
    RoomSetsDlgRoomSetsList(final RoomSetsDlg theContainer, final SpurSimulationData theData) {

        super(new StackedBorderLayout(0, 0));

        this.container = theContainer;

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SpurSimulation.ACCENT_COLOR),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        this.listModel = theData.getRoomSetListModel();
        this.list = new JList<>(this.listModel);

        final JScrollPane scroll = new JScrollPane();
        scroll.setPreferredSize(new Dimension(180, 150));
        scroll.setViewportView(this.list);

        final JPanel center = new JPanel(new StackedBorderLayout());
        center.setBorder(BorderFactory.createEtchedBorder());
        center.setBackground(this.list.getBackground());
        center.add(scroll, StackedBorderLayout.WEST);

        add(center, StackedBorderLayout.CENTER);

        final JPanel south = new JPanel(new StackedBorderLayout(7, 7));
        south.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(south, StackedBorderLayout.SOUTH);

        final JButton addRoomSet = new JButton("Add Room Set...");
        addRoomSet.setActionCommand(CMD_ADD_ROOM_SET);

        this.deleteRoomSet = new JButton("Delete Room Set...");
        this.deleteRoomSet.setActionCommand(CMD_DELETE_ROOM_SET);
        this.deleteRoomSet.setEnabled(false);

        south.add(this.deleteRoomSet, StackedBorderLayout.SOUTH);
        south.add(addRoomSet, StackedBorderLayout.SOUTH);

        this.list.addListSelectionListener(this);
        addRoomSet.addActionListener(this);
        this.deleteRoomSet.addActionListener(this);
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

            final String newName = JOptionPane.showInputDialog(this, "New room set name:", "Add Room Set",
                    JOptionPane.QUESTION_MESSAGE);

            if (newName != null) {
                if (this.listModel.hasName(newName)) {
                    JOptionPane.showMessageDialog(this, "There is already a room set with that name.",
                            "Add Room Set", JOptionPane.ERROR_MESSAGE);
                } else {
                    final RoomSet set = new RoomSet(newName, this.listModel);
                    this.listModel.canAddElement(set);
                }
            }
        } else if (CMD_DELETE_ROOM_SET.equals(cmd)) {
            final int index = this.list.getSelectedIndex();
            this.listModel.removeElement(index);
        }
    }

    /**
     * Called when the list selection changes.
     *
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        final int index = this.list.getSelectedIndex();

        this.deleteRoomSet.setEnabled(index >= 0);

        if (index >= 0) {
            final RoomSet selectedSet = this.listModel.getElementAt(index);
            this.container.picked(selectedSet);
        } else {
            this.container.picked(null);
        }
    }
}
