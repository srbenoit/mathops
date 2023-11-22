package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.debugger.Debugger;
import jwabbit.gui.CalcUI;
import jwabbit.gui.WabbitemuModule;
import jwabbit.hardware.Link;
import jwabbit.iface.Calc;
import jwabbit.iface.EnumEventType;
import jwabbit.log.LoggedObject;

import javax.swing.UIManager;

/**
 * A launcher class with a main method to run the emulator program, to avoid having to search in sub-packages for the
 * correct class to launch.
 */
public final class Launcher {

    /** Initial number of slots (each slot may have calculator and optional debugger). */
    private static final int BLOCK_SIZE = 8;

    /** Object on which to synchronize access to global slot arrays. */
    private static final Object GLOBAL_SYNCH;

    /** The link object that acts as the hub. */
    private static final Link LINK_HUB;

    /** WABBITEMU SOURCE: interface/calc.h, "calcs" array. */
    private static Calc[] calcs;

    /** The calculator UI associated with each calculator. */
    private static CalcUI[] calcUIs;

    /** The thread that runs each calculator. */
    private static CalcThread[] calcThreads;

    /** The debugger associated with each calculator. */
    private static Debugger[] debuggers;

    /**
     * A link hub list (when a calculator is connected to the link hub, its Link's host array is stored here; when
     * disconnected , the calculator's entry here is null).
     */
    private static int[][] linkHubList;

    /*
     * Static initialization.
     */
    static {
        GLOBAL_SYNCH = new Object();
        calcs = new Calc[BLOCK_SIZE];
        calcUIs = new CalcUI[BLOCK_SIZE];
        calcThreads = new CalcThread[BLOCK_SIZE];
        debuggers = new Debugger[BLOCK_SIZE];
        linkHubList = new int[BLOCK_SIZE][];

        LINK_HUB = new Link();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private Launcher() {

        super();
    }

    /**
     * Gets the number of calculators currently in existence.
     *
     * <p>
     * WABBITEMU SOURCE: interface/calc.c, "calc_count" function.
     *
     * @return the number of calculators
     */
    public static int getNumCalcs() {

        int count = 0;

        synchronized (GLOBAL_SYNCH) {

            final int numCalcs = calcs.length;
            for (int i = 0; i < numCalcs; ++i) {
                if (calcs[i] != null) {

                    if (calcThreads[i] != null && calcThreads[i].isFinished()) {
                        deleteCalc(i);
                    } else {
                        ++count;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Adds a calculator in the next available slot.
     *
     * <p>
     * WABBITEMU SOURCE: interface/calc.c, "calc_slot_new" function.
     *
     * @param theCalc    the calculator to add
     * @param buildFrame true to construct a {@code MainFrame} and install the calculator panel in the frame; false to
     *                   simply construct the calculator panel, so it can be hosted within another component or frame
     * @return the slot in which the calculator was added, -1 on any error
     */
    public static int addCalc(final Calc theCalc, final boolean buildFrame) {

        if (theCalc == null) {
            throw new IllegalArgumentException();
        }

        if (theCalc.getSlot() != -1) {
            // if the calculator is already in a slot, remove it before we re-add it
            deleteCalc(theCalc.getSlot());
        }

        int slot = -1;

        synchronized (GLOBAL_SYNCH) {

            final int numCalcs = calcs.length;
            for (int i = 0; i < numCalcs; ++i) {
                if (calcs[i] == null) {
                    slot = i;
                    break;
                }
            }

            // extend slot storage if needed
            if (slot == -1) {
                slot = numCalcs;
                final int newLen = numCalcs + BLOCK_SIZE;

                final Calc[] newCalcs = new Calc[newLen];
                final CalcUI[] newCalcUIs = new CalcUI[newLen];
                final CalcThread[] newCalcThreads = new CalcThread[newLen];
                final Debugger[] newDebuggers = new Debugger[newLen];
                final int[][] newLinkHubList = new int[newLen][];

                System.arraycopy(calcs, 0, newCalcs, 0, numCalcs);
                System.arraycopy(calcUIs, 0, newCalcUIs, 0, numCalcs);
                System.arraycopy(calcThreads, 0, newCalcThreads, 0, numCalcs);
                System.arraycopy(debuggers, 0, newDebuggers, 0, numCalcs);
                System.arraycopy(linkHubList, 0, newLinkHubList, 0, numCalcs);

                calcs = newCalcs;
                calcUIs = newCalcUIs;
                calcThreads = newCalcThreads;
                debuggers = newDebuggers;
                linkHubList = newLinkHubList;
            }
        }

        theCalc.setSpeed(100);
        theCalc.setSlot(slot);

        synchronized (GLOBAL_SYNCH) {
            calcs[slot] = theCalc;
            calcUIs[slot] = null;
            calcThreads[slot] = new CalcThread(theCalc);
            debuggers[slot] = null;
            linkHubList[slot] = null;
        }

        final CalcUI theCalcUI = WabbitemuModule.createCalcUI(theCalc, buildFrame);

        synchronized (GLOBAL_SYNCH) {
            if (theCalcUI == null) {
                slot = -1;
                theCalc.setSlot(-1);
//                calcs[slot] = null;
//                calcUIs[slot] = null;
//                calcThreads[slot] = null;
//                debuggers[slot] = null;
//                linkHubList[slot] = null;
            } else {
                calcUIs[slot] = theCalcUI;
            }
        }

        return slot;
    }

    /**
     * Deletes a calculator and its associated UI and possible debugger.
     *
     * <p>
     * WABBITEMU SOURCE: interface/calc.c, "calc_slot_free" function.
     *
     * @param slot the slot
     */
    public static void deleteCalc(final int slot) {

        synchronized (GLOBAL_SYNCH) {

            if (calcs[slot] != null) {

                if (calcThreads[slot].isAlive()) {
                    LoggedObject.LOG.warning("Can't delete calculator whose thread is still alive", new Exception());
                } else {
                    if (debuggers[slot] != null) {
                        debuggers[slot].die();
                        debuggers[slot] = null;
                    }

                    if (calcUIs[slot] != null) {
                        calcUIs[slot].die();
                        calcUIs[slot] = null;
                    }

                    calcThreads[slot] = null;

                    calcs[slot].die();
                    calcs[slot] = null;

                    linkHubList[slot] = null;
                }
            }
        }
    }

    /**
     * Gets the calculator at a particular slot.
     *
     * @param slot the slot
     * @return the calculator
     */
    public static Calc getCalc(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            return calcs[slot];
        }
    }

    /**
     * Gets the calculator UI at a particular slot.
     *
     * @param slot the slot
     * @return the calculator UI
     */
    public static CalcUI getCalcUI(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            return calcUIs[slot];
        }
    }

    /**
     * Gets the calculator thread at a particular slot.
     *
     * @param slot the slot
     * @return the calculator thread
     */
    public static CalcThread getCalcThread(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            return calcThreads[slot];
        }
    }

    /**
     * Gets the debugger at a particular slot.
     *
     * @param slot the slot
     * @return the debugger
     */
    public static Debugger getDebugger(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            return debuggers[slot];
        }
    }

    /**
     * Sets the debugger at a particular slot.
     *
     * @param slot        the slot
     * @param theDebugger the debugger
     */
    public static void setDebugger(final int slot, final Debugger theDebugger) {

        synchronized (GLOBAL_SYNCH) {
            if (debuggers[slot] != null) {
                debuggers[slot].die();
            }
            if (calcs[slot] != null) {
                debuggers[slot] = theDebugger;
            }
        }
    }

    /**
     * Clears link hub for a particular slot.
     *
     * @param slot the slot
     */
    public static void clearLinkHub(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            linkHubList[slot] = null;
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "link_connect_hub" function.
     *
     * @param slot the slot
     */
    static void linkConnectHub(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            if (calcs[slot] != null) {
                final Link theLink = calcs[slot].getCPU().getPIOContext().getLink();
                theLink.setClient(LINK_HUB.getHostArray());
                linkHubList[slot] = theLink.getHostArray();
            }
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "link_connected_hub" function.
     *
     * @param slot the link hub slot to test
     * @return true if the slot is connected
     */
    private static boolean linkConnectedHub(final int slot) {

        synchronized (GLOBAL_SYNCH) {
            return linkHubList[slot] != null;
        }
    }

    /**
     * Pauses (sets to not running) all calculators connected to the link hub.
     *
     * <p>
     * WABBITEMU SOURCE: interface/calc.c, "calc_pause_linked" function.
     */
    public static void calcPauseLinked() {

        synchronized (GLOBAL_SYNCH) {
            final int numCalcs = calcs.length;
            for (int i = 0; i < numCalcs; ++i) {
                if (calcs[i] != null && calcs[i].isActive() && calcs[i].isRunning() && linkConnectedHub(i)) {
                    calcSetRunning(calcs[i], false);
                }
            }
        }
    }

    /**
     * Resumes (sets to running) all calculators connected to the link hub.
     *
     * <p>
     * WABBITEMU SOURCE: interface/calc.c, "calc_unpause_linked" function.
     */
    private static void calcUnpauseLinked() {

        synchronized (GLOBAL_SYNCH) {
            final int numCalcs = calcs.length;
            for (int i = 0; i < numCalcs; ++i) {
                if (calcs[i] != null && calcs[i].isActive() && !calcs[i].isRunning() && linkConnectedHub(i)) {
                    calcSetRunning(calcs[i], true);
                }
            }
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_set_running" function.
     *
     * @param theCalc the calculator
     * @param running true if running; false if not
     */
    public static void calcSetRunning(final Calc theCalc, final boolean running) {

        theCalc.setRunning(running);
        theCalc.notifyEvent(EnumEventType.ROM_RUNNING_EVENT);

        if (linkConnectedHub(theCalc.getSlot())) {
            if (running) {
                calcUnpauseLinked();
            } else {
                calcPauseLinked();
            }
        }
    }

    /**
     * Sets the look-and-feel for the application.
     */
    private static void setLookAndFeel() {

        final String target;
        if (System.getProperty("os.name").contains("Windows")) {
            target = "Windows";
        } else {
            target = "Nimbus";
        }

        try {
            for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (target.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (final Exception e) {
            LoggedObject.LOG.warning("Nimbus look & feel not available");
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        setLookAndFeel();

        WabbitemuModule.MODULE.winMain(args);
    }
}
