package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.JWCoreConstants;
import jwabbit.core.MemoryContext;
import jwabbit.core.TimerContext;
import jwabbit.core.WideAddr;
import jwabbit.debugger.Debugger;
import jwabbit.debugger.Disassemble;
import jwabbit.debugger.EViewType;
import jwabbit.debugger.Z80Info;
import jwabbit.gui.wizard.WizardRunner;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.KEYPROG;
import jwabbit.hardware.LCD;
import jwabbit.iface.Calc;
import jwabbit.iface.CalcState;
import jwabbit.iface.EnumKeypadState;
import jwabbit.iface.Globals;
import jwabbit.log.LoggedThread;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A thread that runs one calculator and manages an event queue that can manage the calculator's operations. The
 * calculator's steps and timed runs are all managed by this thread, and all updates to calculator state should occur in
 * this thread to ensure state is not changes while calculator is executing.
 *
 * <p>
 * All accesses to calculator state for display or debugging should go through this thread, which allows the calculator
 * to run without synchronization. In particular, when the debugger process requires a telemetry update, it can request
 * it from this thread, and the data will be gathered atomically while the calculator is momentarily paused.
 */
public final class CalcThread extends LoggedThread {

    /** The most recently queried answer. */
    private String lastAns;

    /** RTests whether the thread is finished. */
    private boolean finished;

    /** The calculator this thread runs. */
    private final Calc calc;

    /** A queue of actions. */
    private final Deque<ICalcAction> actions;

    /** Remaining steps to execute in single-step mode. */
    private int remainingSteps;

    /** Target PC when running to an address. */
    private WideAddr runToAddr;

    /** Flag indicating process should run until a return has been executed. */
    private boolean runToReturn;

    /** The state listener. */
    private ICalcStateListener stateListener;

    /** The Java timestamp when the time last changed. */
    private long lastSpeedChange;

    /** The calculator elapsed value when the time last changed. */
    private double lastSpeedChangeElapsed;

    /** A queue of characters to enter via the keypad. */
    private final Deque<KEYPROG> keyQueue;

    /** The time the keyQueue's head key was last pressed. */
    private long lastKeyDown;

    /** The time the keyQueue's head key was last released (and the key removed from queue). */
    private long lastKeyUp;

    /** The most recent keypad state. */
    private EnumKeypadState keyState;

    /**
     * Constructs a new {@code CalcThread}.
     *
     * @param theCalc the calculator this thread runs
     */
    CalcThread(final Calc theCalc) {

        super("Calculator thread " + theCalc.getSlot());

        this.calc = theCalc;

        this.actions = new LinkedList<>();
        this.keyQueue = new LinkedList<>();
        this.lastKeyDown = 0L;
        this.lastKeyUp = 0L;
        this.keyState = null;
        this.finished = false;

        // Request an initial dump of the LCD since it will not necessarily update right away.
        this.actions.addLast(new CalcBasicAction(ECalcAction.UPDATE_LCD));
    }

    /**
     * Gets the slot in which this thread's calculator is installed.
     *
     * @return the slot
     */
    public int getSlot() {

        return this.calc.getSlot();
    }

    /**
     * Sets the calculator state listener.
     *
     * @param theListener the listener (null to clear)
     */
    public void setCalcStateListener(final ICalcStateListener theListener) {

        this.stateListener = theListener;
    }

    /**
     * Enqueue an action.
     *
     * @param action the action to enqueue
     */
    public void enqueueAction(final ICalcAction action) {

        synchronized (this.actions) {
            this.actions.addLast(action);
        }
    }

    /**
     * Clears the most recent answer.
     */
    public void clearLastAnswer() {

        this.lastAns = null;
    }

    /**
     * Gets the most recent answer.
     *
     * @return the most recent answer
     */
    public String getLastAnswer() {

        return this.lastAns;
    }

    /**
     * The main thread method.
     */
    @Override
    public void run() {

        ICalcAction action;

        final TimerContext tc = this.calc.getCPU().getTimerContext();

        int keyTime = this.calc.getCPU().getTimerContext().getFreq();
        if (this.calc.getModel().ordinal() >= EnumCalcModel.TI_84PCSE.ordinal()) {
            keyTime <<= 1;
        }

        for (; ; ) {
            final boolean oldRunning = this.calc.isRunning();

            if (this.calc.isActive() && this.calc.isRunning()) {

                final EnumKeypadState theState = CalcState.getKeypadState(this.calc);
                if (theState != this.keyState) {
                    // Keypad state changed - update disabled keys
                    this.keyState = theState;
                    Launcher.getCalcUI(this.calc.getSlot()).repaint();
                }

                final int speed = this.calc.getSpeed();
                // 15M / 50 / 1024 = 292
                final long time = ((long) speed * (long) this.calc.getCPU().getTimerContext().getFreq()
                        / (long) JWCoreConstants.FPS / 100L) / (long) Globals.FRAME_SUBDIVISIONS;
                this.calc.calcRunTstates(time);
            } else if (this.remainingSteps != 0) {
                this.calc.calcRunSteps(this.remainingSteps);
                this.remainingSteps = 0;
            } else if (this.runToAddr != null) {
                final int speed = this.calc.getSpeed();
                final long time = ((long) speed * (long) this.calc.getCPU().getTimerContext().getFreq()
                        / (long) JWCoreConstants.FPS / 100L) / (long) Globals.FRAME_SUBDIVISIONS;
                this.calc.calcRunToAddr(time, this.runToAddr);
            } else if (this.runToReturn) {
                final int speed = this.calc.getSpeed();
                final long time = ((long) speed * (long) this.calc.getCPU().getTimerContext().getFreq()
                        / (long) JWCoreConstants.FPS / 100L) / (long) Globals.FRAME_SUBDIVISIONS;
                this.calc.calcRunToReturn(time);
            } else {
                try {
                    sleep(10L);
                } catch (final InterruptedException ex) {
                    enqueueAction(new CalcBasicAction(ECalcAction.CLOSE));
                    Thread.currentThread().interrupt();
                }
            }

            // If stepping or running has changed the running state, notify debugger
            if (this.calc.isRunning() != oldRunning) {
                enqueueAction(new CalcBasicAction(ECalcAction.REQUEST_STATE));
            }

            // If there are keys to send, send them...
            if (!this.keyQueue.isEmpty()) {
                final long now = this.calc.getCPU().getTimerContext().getTStates();

                if (this.lastKeyDown != 0L) {
                    if (this.lastKeyDown + (long) (keyTime / 2) < now) {
                        // Release the key
                        final KEYPROG key = this.keyQueue.poll();
                        this.calc.getCPU().getPIOContext().getKeypad().keypadRelease(this.calc,
                                key.getGroup(), key.getBit());
                        this.lastKeyDown = 0L;
                        this.lastKeyUp = now;
                    }
                } else if (this.lastKeyUp != 0L) {
                    if (this.lastKeyDown + (long) keyTime < now) {
                        // Press the key
                        final KEYPROG key = this.keyQueue.peek();
                        this.calc.getCPU().getPIOContext().getKeypad().keypadPress(this.calc,
                                key.getGroup(), key.getBit());
                        this.lastKeyDown = now;
                    }
                } else {
                    // Press the key
                    final KEYPROG key = this.keyQueue.peek();
                    this.calc.getCPU().getPIOContext().getKeypad().keypadPress(this.calc,
                            key.getGroup(), key.getBit());
                    this.lastKeyDown = now;
                }
            }

            synchronized (this.actions) {
                action = this.actions.poll();
            }

            if (action != null) {
                if (action.getType() == ECalcAction.CLOSE) {
                    break;
                }
                processAction(action);
            }

            if (this.lastSpeedChange == 0L) {
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = tc.getElapsed();
            } else if (!this.calc.isMaxSpeed()) {

                final long now = System.currentTimeMillis();
                final double nowElapsed = tc.getElapsed();
                final double totalElapsed = nowElapsed - this.lastSpeedChangeElapsed;

                final long wantTicks = (long) (totalElapsed * 100000.0 / (double) this.calc.getSpeed());
                final long actualTicks = now - this.lastSpeedChange;

                if (wantTicks > actualTicks) {
                    try {
                        Thread.sleep(wantTicks - actualTicks);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        LOG.info("Calculator thread terminating");
        this.finished = true;
    }

    /**
     * Tests whether the thread is finished.
     *
     * @return true if finished
     */
    public boolean isFinished() {

        return this.finished;
    }

    /**
     * Processes a queued calculator action.
     *
     * @param action the action
     */
    private void processAction(final ICalcAction action) {

        switch (action.getType()) {

            case RESET:
                final boolean origRunning = this.calc.isRunning();
                this.calc.setRunning(false);
                this.calc.calcReset();
                this.remainingSteps = 0;
                this.runToAddr = null;
                this.runToReturn = false;
                this.lastSpeedChange = 0L;
                this.lastSpeedChangeElapsed = 0.0;
                if (origRunning) {
                    this.calc.setRunning(true);
                    this.calc.calcTurnOn();
                }
                break;

            case TURN_ON:
                this.calc.setRunning(true);
                this.calc.calcTurnOn();
                this.remainingSteps = 0;
                this.runToAddr = null;
                this.runToReturn = false;
                this.lastSpeedChange = 0L;
                this.lastSpeedChangeElapsed = 0.0;
                break;

            case RUN:
                this.calc.setRunning(true);
                this.remainingSteps = 0;
                this.runToAddr = null;
                this.runToReturn = false;
                this.lastSpeedChange = 0L;
                this.lastSpeedChangeElapsed = 0.0;
                break;

            case STEP:
                if (!this.calc.isRunning()) {
                    this.remainingSteps = 1;
                    this.runToAddr = null;
                    this.runToReturn = false;
                }
                break;

            case STEP_N:
                if (!this.calc.isRunning()) {
                    final CalcStepNAction stepN = (CalcStepNAction) action;
                    this.remainingSteps = stepN.getNumSteps();
                    this.runToAddr = null;
                    this.runToReturn = false;
                }
                break;

            case STEP_UNTIL_PC:
                if (!this.calc.isRunning()) {
                    final CalcStepUntilPCAction stepPC = (CalcStepUntilPCAction) action;
                    this.remainingSteps = 0;
                    this.runToAddr = this.calc.getCPU().getMemoryContext().addr16ToWideAddr(stepPC.getTargetPC());
                    this.runToReturn = false;
                }
                break;

            case STEP_OVER:
                if (!this.calc.isRunning()) {
                    // If the current instruction is "Call" or "Call Condition", then execute until the
                    // PC is at the instruction after the current line. If not, just step.
                    final int oldPc = this.calc.getCPU().getPC();
                    final WideAddr[] waddr = new WideAddr[1];
                    waddr[0] = this.calc.getCPU().getMemoryContext().addr16ToWideAddr(oldPc);
                    final Z80Info[] result = {new Z80Info()};
                    Disassemble.disasmSingle(this.calc, EViewType.REGULAR, waddr, result, 0, false);

                    // A crude way to test for "call" instructions, but we've already done the work
                    if (result[0].getExpanded().contains("call")) {
                        this.remainingSteps = 0;
                        this.runToAddr = waddr[0];
                    } else {
                        this.remainingSteps = 1;
                        this.runToAddr = null;
                    }
                    this.runToReturn = false;
                }
                break;

            case STEP_OUT:
                this.remainingSteps = 0;
                this.runToAddr = null;
                this.runToReturn = true;
                break;

            case STOP:
                this.calc.setRunning(false);
                this.remainingSteps = 0;
                this.runToAddr = null;
                break;

            case TAKE_SCREENSHOT:
            case START_RECORD:
            case END_RECORD:
                break;

            case SET_SPEED_25:
                this.calc.setSpeed(25);
                this.calc.setMaxSpeed(false);
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = this.calc.getCPU().getTimerContext().getElapsed();
                break;

            case SET_SPEED_50:
                this.calc.setSpeed(50);
                this.calc.setMaxSpeed(false);
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = this.calc.getCPU().getTimerContext().getElapsed();
                break;

            case SET_SPEED_100:
                this.calc.setSpeed(100);
                this.calc.setMaxSpeed(false);
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = this.calc.getCPU().getTimerContext().getElapsed();
                break;

            case SET_SPEED_200:
                this.calc.setSpeed(200);
                this.calc.setMaxSpeed(false);
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = this.calc.getCPU().getTimerContext().getElapsed();
                break;

            case SET_SPEED_400:
                this.calc.setSpeed(400);
                this.calc.setMaxSpeed(false);
                this.lastSpeedChange = System.currentTimeMillis();
                this.lastSpeedChangeElapsed = this.calc.getCPU().getTimerContext().getElapsed();
                break;

            case SET_SPEED_MAX:
                this.calc.setMaxSpeed(true);
                // avoid pause when we come out of max-speed while time "catches up"
                this.lastSpeedChange = 0L;
                this.lastSpeedChangeElapsed = 0.0;
                break;

            case SET_PROFILE:
                final CalcSetProfileAction setProfile = (CalcSetProfileAction) action;
                this.calc.setProfile(setProfile.getProfile());
                Launcher.getCalcUI(this.calc.getSlot()).repaint();
                break;

            case CONNECT_VLINK:
                Launcher.linkConnectHub(this.calc.getSlot());
                break;

            case GET_LAST_ANSWER:
                this.lastAns = CalcState.getRealAns(this.calc.getCPU());
                break;

            case COPY_LAST_ANSWER:
                this.lastAns = CalcState.getRealAns(this.calc.getCPU());
                if (this.lastAns != null && !this.lastAns.isEmpty()) {
                    final StringSelection sel = new StringSelection(this.lastAns);
                    try {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                    } catch (final IllegalStateException ex) {
                        LOG.warning("Failed to set clipboard");
                    }
                }
                break;

            case COPY_TEXT:
                this.lastAns = CalcState.getRealAns(this.calc.getCPU());
                if (this.lastAns != null && !this.lastAns.isEmpty()
                        && action instanceof CalcCopyTextAction) {
                    final JTextComponent field = ((CalcCopyTextAction) action).getField();

                    if (field != null) {
                        SwingUtilities.invokeLater(new Runnable() {

                            /** Populates the field. */
                            @Override
                            public void run() {

                                field.setText(CalcThread.this.lastAns);
                            }
                        });
                    }
                }
                break;

            case PASTE:
                try {
                    final Object obj = Toolkit.getDefaultToolkit().getSystemClipboard()
                            .getData(DataFlavor.stringFlavor);
                    if (obj instanceof String) {
                        final char[] chars = ((String) obj).toCharArray();
                        for (final char aChar : chars) {
                            final KEYPROG prog = KEYPROG.fromChar(aChar);
                            if (prog != null) {
                                this.keyQueue.addLast(prog);
                            }
                        }
                    } else {
                        LOG.warning("Clipboard data was not string: ", obj.getClass().getName());
                    }
                } catch (final HeadlessException | UnsupportedFlavorException | IOException ex) {
                    LOG.warning("Failed to get clipboard data", ex);
                }
                break;

            case DEBUG:
                final int slot = this.calc.getSlot();
                final Debugger debugger = Launcher.getDebugger(slot);
                if (debugger != null) {
                    debugger.show();
                } else {
                    final Debugger newDebug = new Debugger(this.calc.getSlot());
                    Launcher.setDebugger(slot, newDebug);
                    SwingUtilities.invokeLater(newDebug);
                }
                break;

            case CLOSE:
                break;

            case KEY_PRESSED:
                final CalcKeyPressAction press = (CalcKeyPressAction) action;
                this.calc.getCPU().getPIOContext().getKeypad().keypadPress(this.calc, press.getGroup(),
                        press.getBit());
                break;

            case KEY_RELEASED:
                final CalcKeyReleaseAction release = (CalcKeyReleaseAction) action;
                this.calc.getCPU().getPIOContext().getKeypad().keypadRelease(this.calc,
                        release.getGroup(), release.getBit());
                break;

            case KEY_KEYPRESSED:
                final CalcKeyKeyPressAction keypress = (CalcKeyKeyPressAction) action;

                this.calc.getCPU().getPIOContext().getKeypad().keypadKeyPress(this.calc,
                        keypress.getVk(), keypress.getLoc(), null);
                break;

            case KEY_KEYRELEASED:
                final CalcKeyKeyReleaseAction keyrelease = (CalcKeyKeyReleaseAction) action;
                this.calc.getCPU().getPIOContext().getKeypad().keypadKeyRelease(this.calc,
                        keyrelease.getVk(), keyrelease.getLoc());
                break;

            case BREAKPOINT_TOGGLE:
                final CalcToggleBreakAction breakToggle = (CalcToggleBreakAction) action;
                final int type = breakToggle.getBreakpointType();
                final MemoryContext memc = this.calc.getCPU().getMemoryContext();
                final int bp = memc.getBreakpoint(breakToggle.getAddress());

                if ((type & JWCoreConstants.NORMAL_BREAK) != 0) {
                    if ((bp & JWCoreConstants.NORMAL_BREAK) == 0) {
                        memc.setBreak(breakToggle.getAddress());
                    } else {
                        memc.clearBreak(breakToggle.getAddress());
                    }
                }

                if ((type & JWCoreConstants.MEM_READ_BREAK) != 0) {
                    if ((bp & JWCoreConstants.MEM_READ_BREAK) == 0) {
                        memc.setMemReadBreak(breakToggle.getAddress());
                    } else {
                        memc.clearMemReadBreak(breakToggle.getAddress());
                    }
                }

                if ((type & JWCoreConstants.MEM_WRITE_BREAK) != 0) {
                    if ((bp & JWCoreConstants.MEM_WRITE_BREAK) == 0) {
                        memc.setMemWriteBreak(breakToggle.getAddress());
                    } else {
                        memc.clearMemWriteBreak(breakToggle.getAddress());
                    }
                }
                break;

            case REQUEST_STATE:
                this.stateListener.calcState(this.calc);
                break;

            case UPDATE_LCD:
                final AbstractLCDBase lcd = this.calc.getCPU().getPIOContext().getLcd();
                if (lcd instanceof LCD) {
                    ((LCD) lcd).fireUpdate();
                }
                break;

            case RUN_WIZARD:
                new WizardRunner().start();
                break;

            case DISABLE_SOUND:
            case ENABLE_SOUND:
            default:
                LOG.warning("Unhandled action: ", action.getType().name());
                break;
        }
    }
}
