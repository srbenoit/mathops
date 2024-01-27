package dev.mathops.app;

import dev.mathops.commons.log.Log;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.io.Serial;
import java.util.Properties;

/**
 * A simple dialog to tell the user that the operation in progress may take a while and to please be patient.
 */
public final  class PleaseWait extends InternalPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3338675220952641253L;

    /**
     * Constructs a new {@code PleaseWait} panel.
     *
     * @param theResOwner       the object that is creating the dialog. Skin resources are loaded from the PleaseWaitSkin path
     *                    relative to this object's position in the source tree
     * @param examVersion the exam version being requested
     */
    public PleaseWait(final Object theResOwner, final String examVersion) {

        super(theResOwner, null);

        final Properties res = new DefaultPleaseWaitSkin();

        if (examVersion.startsWith("30")) {
            final String existing = res.getProperty("wait-text");

            if (existing.contains("exam")) {
                final String updated = existing.replace("exams", "quizzes").replace("exam", "quiz");
                res.setProperty("wait-text", updated);
            }
        }

        try {
            SwingUtilities.invokeAndWait(new PleaseWaitGUIBuilder(this, res));
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }

    /**
     * Presents the dialog centered in the desktop panel, and set the cursor to the WAIT cursor.
     *
     * @param desktop the desktop panel
     */
    public void show(final JPanel desktop) {

        try {
            SwingUtilities.invokeAndWait(new ShowPleaseWait(desktop, this));
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }

    /**
     * Presents the dialog centered in the desktop panel, and set the cursor to the WAIT cursor.
     *
     * @param desktop the desktop panel
     */
    public void close(final JPanel desktop) {

        try {
            SwingUtilities.invokeAndWait(new ClosePleaseWait(desktop, this));
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }
}

/**
 * Construct the GUI in the AWT event thread.
 */
final class PleaseWaitGUIBuilder implements Runnable {

    /** The owning panel. */
    private final PleaseWait owner;

    /** The resource properties from which to get GUI settings. */
    private final Properties res;

    /**
     * Constructs a new {@code PleaseWaitGUIBuilder}.
     *
     * @param theOwner the owning panel
     * @param theRes   the resource properties from which to get GUI settings
     */
    PleaseWaitGUIBuilder(final PleaseWait theOwner, final Properties theRes) {

        this.owner = theOwner;
        this.res = theRes;
    }

    /**
     * Run method to construct the user interface, intended to be run in the AWT event dispatch thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Set the border for the frame, which may be decorated or undecorated,
        // and set the frame size and background (color or image)
        this.owner.setupFrame(this.res);

        // Create all the text labels
        this.owner.createSingleLabel(this.res, "wait");
        this.owner.pack();
    }
}

/**
 * Display the dialog in the AWT event thread.
 */
final class ShowPleaseWait implements Runnable {

    /** The desktop in which to install the panel. */
    private final JPanel content;

    /** The please-wait panel to show. */
    private final PleaseWait panel;

    /**
     * Constructs a new {@code ShowPleaseWait}.
     *
     * @param theContent the content pane to which to add the panel
     * @param thePanel   the panel to add to the desktop
     */
    ShowPleaseWait(final JPanel theContent, final PleaseWait thePanel) {

        this.content = theContent;
        this.panel = thePanel;
    }

    /**
     * Runnable method to display the dialog; intended to be called in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.content.add(this.panel);
        this.panel.centerInDesktop();
        this.panel.setVisible(true);
        this.content.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }
}

/**
 * Closes the dialog in the AWT event thread.
 */
final class ClosePleaseWait implements Runnable {

    /** The content pane from which to remove the panel. */
    private final JPanel content;

    /** The please-wait panel to close. */
    private final PleaseWait panel;

    /**
     * Constructs a new {@code ClosePleaseWait}.
     *
     * @param theContent the content pane from which to remove the panel
     * @param thePanel   the panel to close
     */
    ClosePleaseWait(final JPanel theContent, final PleaseWait thePanel) {

        this.content = theContent;
        this.panel = thePanel;
    }

    /**
     * Runnable method to close the dialog; intended to be called in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel.setVisible(false);
        this.content.remove(this.panel);
        this.panel.dispose();
        this.content.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}

/**
 * A resource bundle class that contains the default settings for the panel.
 */
final class DefaultPleaseWaitSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -608464449088627225L;

    /** Default settings. */
    private static final String[][] CONTENTS = {//
            {"panel-title", "Operation in Progress"},
            {"panel-width", "350"},
            {"panel-height", "100"},
            {"panel-border-style", "line"},
            {"panel-border-size", "1"},
            {"panel-border-color", "black"},
            {"panel-background-color", "gray80"},
            {"wait-text", "<HTML>This operation takes a few moments.<br>Please be patient...</HTML>"},
            {"wait-left", "15"},
            {"wait-top", "15"},
            {"wait-color", "black"},
            {"wait-font-name", "SANS"},
            {"wait-font-size", "16"},
            {"wait-font-style", "BOLD"},
            {"wait-alignment", "LEFT"},};

    /**
     * Constructs a new {@code DefaultPleaseWaitSkin} properties object.
     */
    DefaultPleaseWaitSkin() {

        super();

        for (final String[] content : CONTENTS) {
            setProperty(content[0], content[1]);
        }
    }
}
