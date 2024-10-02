package dev.mathops.app.sim.rooms;

import dev.mathops.app.sim.SpurSimulation;
import dev.mathops.app.sim.SpurSimulationData;
import dev.mathops.app.sim.SpurSimulationDataListener;
import dev.mathops.app.sim.swing.ButtonColumn;
import dev.mathops.app.sim.swing.ButtonColumnTableModel;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel within the classroom dialog that displays the set of all rooms that exist on campus and that could be used
 * for classes or labs.  Each room has a unique name and seating capacity.
 *
 * <p>
 * When courses are defined, they will indicate the set of campus rooms that are "compatible" with the course, or with
 * some portion of the course (a class section, a lab section, a recitation section, etc.).
 */
final class RoomSetsDlgCampusRoomsTable extends JPanel implements ActionListener, SpurSimulationDataListener {

    /** An action command. */
    private static final String CMD_ADD_ROOM = "ADD_ROOM";

    /** The simulation configuration data. */
    private final SpurSimulationData data;

    /** A dialog to use when adding a new campus room. */
    private RoomSetsDlgAddCampusRoom addRoomDialog = null;

    /**
     * Constructs a new {@code ClassroomDialogCampusRoomsTable}.
     */
    RoomSetsDlgCampusRoomsTable(final SpurSimulationData theData) {

        super(new StackedBorderLayout(3, 3));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SpurSimulation.ACCENT_COLOR),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        this.data = theData;

        final JButton addRoom = new JButton("Add Room...");
        addRoom.setActionCommand(CMD_ADD_ROOM);
        addRoom.addActionListener(this);

        final TableColumnModel columnModel = new DefaultTableColumnModel();
        final TableColumn column1 = new TableColumn(0, 125);
        column1.setMaxWidth(125);
        column1.setHeaderValue("Room Name");
        columnModel.addColumn(column1);
        final TableColumn column2 = new TableColumn(1, 125);
        column2.setMaxWidth(125);
        column2.setHeaderValue("Maximum Capacity");
        columnModel.addColumn(column2);
        final TableColumn column3 = new TableColumn(2, 75);
        column3.setMaxWidth(75);
        column3.setHeaderValue(" ");
        columnModel.addColumn(column3);

        final TableModel tableModel = this.data.getCampusRoomsTableModel();
        final JTable table = new JTable(tableModel, columnModel);
        table.setShowHorizontalLines(true);

        Action delete = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                Object delete = table.getModel().getValueAt(modelRow, 2);
                Window window = SwingUtilities.windowForComponent(table);

                int result = JOptionPane.showConfirmDialog(
                        window,
                        "Are you sure you want to " + delete,
                        "Delete Row Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
//					System.out.println( "Deleting row: " + modelRow);
                    ((ButtonColumnTableModel) table.getModel()).removeRow(modelRow);
                }
            }
        };

        final JScrollPane scroll = new JScrollPane();
        scroll.setPreferredSize(table.getPreferredSize());
        scroll.setViewportView(table);

        final ButtonColumn buttonColumn = new ButtonColumn(table, delete, 2);

        final JPanel center = new JPanel(new StackedBorderLayout());
        center.setBorder(BorderFactory.createEtchedBorder());
        center.setBackground(table.getBackground());
        center.add(scroll, StackedBorderLayout.WEST);

        add(center, StackedBorderLayout.CENTER);

        final JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        buttonBar.add(addRoom);
        add(buttonBar, StackedBorderLayout.SOUTH);

        setPreferredSize(new Dimension(700, 300));
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

        if (CMD_ADD_ROOM.equals(cmd)) {
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
