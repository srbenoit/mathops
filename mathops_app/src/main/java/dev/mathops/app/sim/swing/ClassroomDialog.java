package dev.mathops.app.sim.swing;

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
 * The configuration includes a set of classrooms available at the campus (immutable) for which each class can indicate
 * compatibility.  A profile then select some subset of these rooms for inclusion in the simulation, and sets the
 * weekdays and times of day each is available, and which days operate on "50-minute blocks" vs. "75-minute blocks".
 */
final class ClassroomDialog extends WindowAdapter {

    /** The frame. */
    private final JFrame frame;

    /** The table to display campus rooms. */
    private final ClassroomDialogCampusRoomsTable roomsTable;

    /**
     * Constructs a new {@code ClassroomDialog}.
     */
    ClassroomDialog(final SpurSimulationData theData) {

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
        this.roomsTable = new ClassroomDialogCampusRoomsTable(theData);
        content.add(this.roomsTable, StackedBorderLayout.NORTH);

        // Show a list of named classroom profiles that select a suite of available rooms, weekdays and hours of
        // operation, and block schedule types for each weekday, with options to add/delete/update.

        this.frame.pack();
    }

    /**
     * Gets the size of the frame.
     *
     * @return the frame size
     */
    Dimension getSize() {

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
    void show() {

        this.frame.setVisible(true);
        this.frame.toFront();
    }

    /**
     * Sets the location of the frame.
     *
     * @param x the new frame location x coordinate
     * @param y the new frame location y coordinate
     */
    void setLocation(final int x, final int y) {

        this.frame.setLocation(x, y);
    }

    /**
     * Closes the dialog.
     */
    void close() {

        this.roomsTable.close();
        this.frame.setVisible(false);
        this.frame.dispose();
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(final WindowEvent e) {

        this.roomsTable.close();
        this.frame.dispose();
    }
}
