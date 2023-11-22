package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.CalcBasicAction;
import jwabbit.ECalcAction;
import jwabbit.Launcher;
import jwabbit.log.ObjLogger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main frame that shows the calculator skin (if skin enabled) or nothing (if not enabled). The LCD itself is shown
 * by a separate frame that overlays the main frame.
 *
 * <p>
 * This frame can be resized. When a skin is enabled, the resizing "prefers" sizes where the LCD has an integer scale,
 * but allows any scale. When no skin is enabled, sizes are limited to
 */
final class MainFrame extends JFrame implements WindowListener {

    /** A log to which to write diagnostic messages. */
    private static final ObjLogger LOG;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3312970956613058610L;

    /** The action handler. */
    private final CalcActionHandler handler;

    /** The menu bar. */
    private final CalcMenuBar menuBar;

    /** The skin panel. */
    private final CalculatorPanel skinPanel;

    static {
        LOG = new ObjLogger();
    }

    /**
     * Constructs a new {@code MainFrame}.
     *
     * @param theCalcUI    the calculator UI that owns this frame
     * @param theSkinPanel the calculator panel
     */
    MainFrame(final CalcUI theCalcUI, final CalculatorPanel theSkinPanel) {

        super("JWabbitemu");

        if (!SwingUtilities.isEventDispatchThread()) {
            LOG.warning("WARNING: constructing a MainFrame outside the AWT event thread", new IllegalStateException());
        }

        // setUndecorated(true);
        // setBackground(new Color(0, 0, 0, 0));

        this.skinPanel = theSkinPanel;

        if (Boolean.TRUE.equals(Registry.queryWabbitKey("always_on_top"))) {
            setAlwaysOnTop(true);
        }

        // Note that we cannot disable the "maximize" button while allowing resize (even if using a JDialog), so we
        // leave it enabled, and will intercept maximize events to leave the window at maximum height but proportional
        // width.

        // Load the window icon
        final BufferedImage img = Gui.loadImage("wabbitemu_16.png");
        if (img != null) {
            setIconImage(img);
        }

        // Window disposes when user hits the close button - the CalcUI is made a window listener, so it can detect
        // window closure and close all associated frames.
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(theCalcUI);
        addComponentListener(theCalcUI);

        // Build the menu bar
        this.handler = new CalcActionHandler(Launcher.getCalcThread(theCalcUI.getSlot()));
        this.menuBar = new CalcMenuBar(this.handler);
        setJMenuBar(this.menuBar);

        setContentPane(this.skinPanel);

        addWindowListener(this);

        pack();
    }

    /**
     * Handles window open events.
     *
     * @param e the window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window closing events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window closed events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosed(final WindowEvent e) {

        // Window is closing, so terminate thread
        this.handler.getThread().enqueueAction(new CalcBasicAction(ECalcAction.CLOSE));
    }

    /**
     * Handles window iconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {

        // Pause execution while iconified, but save state so we can resume on deiconify
        // this.handler.getThread().enqueueAction(new CalcBasicAction(ECalcAction.STOP));
    }

    /**
     * Handles window deiconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {

        // Pause execution while iconified, but save state so we can resume on deiconify
        // this.handler.getThread().enqueueAction(new CalcBasicAction(ECalcAction.RUN));
    }

    /**
     * Handles window activated events.
     *
     * @param e the window event
     */
    @Override
    public void windowActivated(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window deactivated events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeactivated(final WindowEvent e) {

        // No action
    }
}
