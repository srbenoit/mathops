package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.app.ops.snapin.canvas.model.CanvasModel;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.app.ops.snapin.AbstractFullPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A full-screen panel for this snap-in.
 *
 * <p>
 * At the top of the window is a "synchronize" button to scan the database and Canvas for deltas, and to apply any
 * needed changes to Canvas to bring into alignment. A scroll bar at the bottom of the window shows progress.
 */
public final class CanvasFull extends AbstractFullPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3820074436551595018L;

    /** Font for group titles. */
    private static final Font TITLE_FONT = new Font(Font.DIALOG, Font.BOLD, 14);

    /** Font for field labels. */
    private static final Font LABEL_FONT = new Font(Font.DIALOG, Font.BOLD, 12);

    /** Button action command. */
    private static final String CMD_SYNCH = "SYNCH";

    /** The synchronize button. */
    private final JButton synchronize;

    /** Progress bar. */
    private final JProgressBar progress;

    /** The label on the status bar. */
    private final JLabel statusBarLabel;

    /** The Canvas model reader. */
    private final ModelReader reader;

    /**
     * Constructs a new {@code CanvasFull}.
     *
     * @param theFrame the owning frame
     * @param accessToken the Canvas access token
     */
    CanvasFull(final JFrame theFrame, final String accessToken) {

        super();

        // TODO: Get these from settings

        final String canvasHost = "https://colostate.instructure.com";

        final String[] codesOfInterest = {"MATH-117", "MATH-118", "MATH-124", "MATH-125", "MATH-126"};

        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(padding);

        this.synchronize = new JButton("Synchronize");
        this.synchronize.setActionCommand(CMD_SYNCH);
        this.synchronize.addActionListener(this);

        final JPanel south = new JPanel(new StackedBorderLayout(10, 0));
        south.add(this.synchronize, StackedBorderLayout.WEST);

        final JPanel southCenter = new JPanel(new StackedBorderLayout(10, 0));
        south.add(southCenter, StackedBorderLayout.CENTER);

        this.progress = new JProgressBar(0, 1000);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        this.progress.setBorder(bevel);
        this.progress.setStringPainted(true);
        this.progress.setFont(TITLE_FONT);
        this.progress.setString(CoreConstants.EMPTY);
        southCenter.add(this.progress, StackedBorderLayout.SOUTH);

        this.statusBarLabel = new JLabel("Status");
        this.statusBarLabel.setFont(LABEL_FONT);
        final Border statusPad = BorderFactory.createEmptyBorder(0, 0, 4, 0);
        this.statusBarLabel.setBorder(statusPad);
        southCenter.add(this.statusBarLabel, StackedBorderLayout.SOUTH);

        this.reader = new ModelReader(canvasHost, accessToken, codesOfInterest, this.progress,
                this.synchronize, this.statusBarLabel);

        add(south, StackedBorderLayout.SOUTH);
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    @Override
    public void tick() {

        // No action
    }

    /**
     * Called when a button click generates an action event.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_SYNCH.equals(cmd)) {
            doSynch();
        }
    }

    /**
     * Performs a synchronize with Canvas.
     */
    private void doSynch() {

        this.statusBarLabel.setText("Synchronizing with Canvas");
        this.synchronize.setEnabled(false);

        this.reader.execute();
    }

    /**
     * Updates the UI to reflect a newly loaded Canvas data model.
     *
     * @param model the data model
     */
    private void updateUI(final CanvasModel model) {

        final boolean dispatchThread = SwingUtilities.isEventDispatchThread();

        Log.info("Updating UI: DispatchThread = ", dispatchThread ? "True" : "False");
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "CanvasFull";
    }
}
