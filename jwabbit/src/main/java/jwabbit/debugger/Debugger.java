package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.CalcBasicAction;
import jwabbit.CalcThread;
import jwabbit.ECalcAction;
import jwabbit.ICalcStateListener;
import jwabbit.Launcher;
import jwabbit.gui.Gui;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A singleton debugger that presents a frame that shows the status of the entire JWabbit environment and supports
 * controls such as single-step and breakpoint management.
 */
public final class Debugger implements Runnable, ICalcStateListener {

    /** A background color for debugger panels. */
    static final Color BG_COLOR = new Color(220, 220, 220);

    /** The main frame. */
    JFrame frame;

    /** The main panel. */
    private MainPanel mainPanel;

    /** The menu bar. */
    private DebuggerMenuBar menuBar;

    /** The toolbar. */
    private DebuggerToolBar toolBar;

    /** The slot with which this debugger is associated. */
    private final int slot;

    /** The refresh timer. */
    private final Timer timer;

    /**
     * Constructs a new {@code Debugger}.
     *
     * @param theSlot the slot with which this debugger is associated
     */
    public Debugger(final int theSlot) {

        super();

        this.slot = theSlot;

        this.frame = null;
        this.mainPanel = null;
        this.timer = new Timer("Debugger refresh timer");
    }

    /**
     * Gets the slot with which this debugger is associated.
     *
     * @return the slot
     */
    public int getSlot() {

        return this.slot;
    }

    /**
     * Shows the debugger view, creating its UI if it has not already been created.
     */
    public void show() {

        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /**
     * Constructs the UI in the AWT event thread, or if it has already been constructed, ensures it is visible.
     */
    @Override
    public void run() {

        final CalcThread thread = Launcher.getCalcThread(this.slot);
        if (thread != null) {

            if (this.frame == null) {

                this.frame = new JFrame("JWabbit Debugger [Slot " + this.slot + "]");
                this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

                final BufferedImage icon = Gui.loadImage("wabbitemu_16.png");
                if (icon != null) {
                    this.frame.setIconImage(icon);
                }

                final JPanel content = new JPanel(new BorderLayout());
                this.frame.setContentPane(content);

                final ActionHandler handler = new ActionHandler(this.slot, thread);
                this.menuBar = new DebuggerMenuBar(handler);
                this.toolBar = new DebuggerToolBar(handler);

                this.frame.setJMenuBar(this.menuBar);
                content.add(this.toolBar, BorderLayout.PAGE_START);

                this.mainPanel = new MainPanel(handler);
                content.add(this.mainPanel, BorderLayout.CENTER);
                thread.setCalcStateListener(this);

                handler.setMainAndBars(this.mainPanel, this.toolBar);

                this.frame.pack();
            }

            this.frame.setVisible(true);
            thread.enqueueAction(new CalcBasicAction(ECalcAction.REQUEST_STATE));

            this.timer.schedule(new RefreshTask(thread), 500L, 500L);
        }
    }

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    @Override
    public void calcState(final Calc theCalc) {

        // Called from the calculator thread - we will do updates in the AWT event thread, so we
        // suspend the calculator thread here and wait for the updates to occur in the AWT thread.

        if (this.frame.isVisible()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    /**
                     * Update all debugger panes from within the AWT thread.
                     */
                    @Override
                    public void run() {

                        Debugger.this.menuBar.setState(theCalc);
                        Debugger.this.toolBar.setState(theCalc);
                        Debugger.this.mainPanel.calcState(theCalc);

                        enableControls(!theCalc.isRunning());
                    }
                });
            } catch (final InvocationTargetException ex) {
                LoggedObject.LOG.warning("Exception while updating debugger with calculator state", ex);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.mainPanel.enableControls(enable);
    }

    /**
     * Converts an integer into a 4-digit hex string.
     *
     * @param value the value
     * @return the hex string
     */
    static String toHex4(final int value) {

        final StringBuilder builder = new StringBuilder(4);

        if (value < 0) {
            builder.append('-');
            builder.append(Integer.toHexString(value));
        } else if (value < 16) {
            builder.append("000");
            builder.append(Integer.toHexString(value));
        } else if (value < 256) {
            builder.append("00");
            builder.append(Integer.toHexString(value));
        } else if (value < 4096) {
            builder.append("0");
            builder.append(Integer.toHexString(value));
        } else if (value < 65536) {
            builder.append(Integer.toHexString(value));
        } else {
            builder.append("[ov]");
        }

        return builder.toString();
    }

    /**
     * Converts an integer into a 2-digit hex string.
     *
     * @param value the value
     * @return the hex string
     */
    static String toHex2(final int value) {

        final StringBuilder builder = new StringBuilder(4);

        if (value < 0) {
            builder.append('-');
            builder.append(Integer.toHexString(value));
        } else if (value < 16) {
            builder.append("0");
            builder.append(Integer.toHexString(value));
        } else if (value < 256) {
            builder.append(Integer.toHexString(value));
        } else {
            builder.append("[ov]");
        }

        return builder.toString();
    }

    /**
     * Closes the telemetry view.
     */
    public void die() {

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
            this.frame = null;
        }

        this.timer.cancel();
    }

    /**
     * A timer task to refresh the currently visible page at intervals.
     */
    private static final class RefreshTask extends TimerTask {

        /** The calculator thread. */
        private final CalcThread thread;

        /**
         * Constructs a new {@code RefreshTask}.
         *
         * @param theThread the calculator thread
         */
        RefreshTask(final CalcThread theThread) {

            super();

            this.thread = theThread;
        }

        /**
         * Method that will be executed on intervals by the refresh timer.
         */
        @Override
        public void run() {

            this.thread.enqueueAction(new CalcBasicAction(ECalcAction.REQUEST_STATE));
        }
    }
}
