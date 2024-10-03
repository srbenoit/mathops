package dev.mathops.app.sim.rooms;

import dev.mathops.app.sim.SpurSimulationData;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A dialog to manage profiles that define available classrooms and labs.
 *
 * <p>
 * A configuration includes a set of classrooms available at the campus (immutable) for which each class can indicate
 * compatibility.  A profile then select some subset of these rooms for inclusion in the simulation, and sets the
 * weekdays and times of day each is available, and which days operate on "50-minute blocks" vs. "75-minute blocks".
 */
public final class RoomSetsDlg extends WindowAdapter {

    /** The frame. */
    private final JFrame frame;

    /** The list of defined room sets. */
    private final RoomSetsDlgRoomSetsList setsList;

    /** The center panel to which we can add a {@code RoomSetsDlgCampusRoomsTable} when a set is selected. */
    private final JPanel center;

    /** The room set currently being displayed in the table view. */
    private RoomSet displayedSet;

    /** The displayed table. */
    private RoomSetsDlgCampusRoomsTable displayedTable;

    /**
     * Constructs a new {@code CampusRoomsDialog}.
     *
     * @param theData the simulation data
     */
    public RoomSetsDlg(final SpurSimulationData theData) {

        super();

        this.frame = new JFrame("Classroom and Lab Configuration");
        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.setPreferredSize(new Dimension(800, 600));
        this.frame.setContentPane(content);

        // Show a scrollable table of all the classrooms on campus, with options to add/delete/update
        final JLabel lbl1 = new JLabel("Campus classrooms and labs:");
        content.add(lbl1, StackedBorderLayout.NORTH);

        this.center = new JPanel(new StackedBorderLayout());

        this.center.setPreferredSize(new Dimension(400, 200));
        content.add(this.center, StackedBorderLayout.CENTER);

        this.setsList = new RoomSetsDlgRoomSetsList(this, theData);
        content.add(this.setsList, StackedBorderLayout.WEST);

        // Show a list of named classroom profiles that select a suite of available rooms, weekdays and hours of
        // operation, and block schedule types for each weekday, with options to add/delete/update.

        this.frame.pack();
    }

    /**
     * Gets the size of the frame.
     *
     * @return the frame size
     */
    public Dimension getSize() {

        return this.frame.getSize();
    }

    /**
     * Gets the location of the frame.
     *
     * @return the frame location
     */
    Point getLocation() {

        return this.frame.getLocation();
    }

    /**
     * Makes the frame visible and brings it to the front.
     */
    public void show() {

        this.frame.setVisible(true);
        this.frame.toFront();
    }

    /**
     * Sets the location of the frame.
     *
     * @param x the new frame location x coordinate
     * @param y the new frame location y coordinate
     */
    public void setLocation(final int x, final int y) {

        this.frame.setLocation(x, y);
    }

    /**
     * Closes the dialog.
     */
    public void close() {

        if (this.displayedTable != null) {
            this.displayedTable.close();
            this.displayedTable = null;
            this.displayedSet = null;
        }

        this.frame.setVisible(false);
        this.frame.dispose();
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(final WindowEvent e) {

        if (this.displayedTable != null) {
            this.displayedTable.close();
            this.displayedTable = null;
            this.displayedSet = null;
        }

        this.frame.dispose();
    }

    /**
     * Called when the selected room set changes.
     *
     * @param set the selected set (null if none is selected)
     */
    void picked(final RoomSet set) {

        if (set == null) {
            if (this.displayedSet != null) {
                this.center.remove(this.displayedTable);
                this.displayedTable = null;
                this.displayedSet = null;

                this.center.invalidate();
                this.center.revalidate();
                this.center.repaint();
            }
        } else if (this.displayedSet != set) {
            if (this.displayedTable != null) {
                this.center.remove(this.displayedTable);
            }

            final RoomSetTableModel model = set.getTableModel();

            this.displayedSet = set;
            this.displayedTable = new RoomSetsDlgCampusRoomsTable(model);
            this.center.add(this.displayedTable, StackedBorderLayout.CENTER);

            this.center.invalidate();
            this.center.revalidate();
            this.center.repaint();
        }
    }
}