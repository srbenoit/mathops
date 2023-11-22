package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.Launcher;
import jwabbit.core.AudioFrameCallback;
import jwabbit.core.BankState;
import jwabbit.core.CPU;
import jwabbit.core.CalcDebugCallback;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.ExeViolationCallback;
import jwabbit.core.InvalidFlashCallback;
import jwabbit.core.JWCoreConstants;
import jwabbit.core.LcdEnqueueCallback;
import jwabbit.core.MemReadCallback;
import jwabbit.core.MemWriteCallback;
import jwabbit.core.PIOContext;
import jwabbit.core.PortDebugCallback;
import jwabbit.core.WideAddr;
import jwabbit.gui.CalcUI;
import jwabbit.gui.Registry;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.ColorLCD;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.Hw81;
import jwabbit.hardware.Hw83;
import jwabbit.hardware.Hw83p;
import jwabbit.hardware.Hw83pse;
import jwabbit.hardware.Hw86;
import jwabbit.hardware.KeyName;
import jwabbit.hardware.KeyNames;
import jwabbit.hardware.Keypad;
import jwabbit.hardware.LCD;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.AUDIO;
import jwabbit.utilities.TIFILE;
import jwabbit.utilities.Var;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * WABBITEMU SOURCE: interface/calc.h, "tagCALC" struct.
 */
public final class Calc {

    /** Am empty string array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** An empty array ot key names. */
    private static final KeyName[] NO_KEY_NAMES = new KeyName[0];

    /** The breakpoint callback. */
    private final IBreakpointCallback breakpointCallback;

    /** The slot. */
    private int slot;

    /** The ROM path. */
    private String romPath;

    /** The calculator model. */
    private EnumCalcModel model;

    /** The time error. */
    private long timeError;

    /** Flag indicating calculator is active. */
    private boolean active;

    /** Flag indicating calculator is running. */
    private boolean running;

    /** Fake running. */
    private boolean fakeRunning;

    /** The calculator's CPU. */
    private final CPU cpu;

    /** Flag indicating calculator runs at maximum speed. */
    private boolean maxSpeed;

    /** The calculator speed. */
    private int speed;

    /** Labeled memory addresses. */
    private final Label[] labels;

    /** Registered events. */
    private final RegisteredEvent[] registeredEvents;

    /** List of disabled keys. */
    private String[] disabledKeys;

    /**
     * Constructs a new {@code Calc}.
     */
    public Calc() {

        super();

        this.slot = -1;

        this.romPath = "";
        this.active = false;
        this.running = false;
        this.fakeRunning = false;
        this.disabledKeys = null;

        this.cpu = CPU.createCPU(this);

        this.maxSpeed = false;

        final int count = 10000;
        this.labels = new Label[count];
        for (int i = 0; i < count; ++i) {
            this.labels[i] = new Label();
        }
        this.registeredEvents = new RegisteredEvent[Globals.MAX_REGISTERED_EVENTS];
        for (int i = 0; i < Globals.MAX_REGISTERED_EVENTS; ++i) {
            this.registeredEvents[i] = new RegisteredEvent();
        }

        this.speed = 100;
        this.breakpointCallback = new CalcDebugCallback();
    }

    /**
     * Gets the breakpoint callback.
     *
     * @return the callback
     */
    public IBreakpointCallback getBreakpointCallback() {

        return this.breakpointCallback;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_slot_free" function.
     */
    public void die() {

        this.active = false;

        this.cpu.getMemoryContext().getFlash().clear();
        this.cpu.getMemoryContext().getRam().clear();

        final PIOContext pio = this.cpu.getPIOContext();
        if (pio.getLink() != null && pio.getLink().getAudio() != null) {
            pio.getLink().getAudio().killSound();
        }

        Launcher.clearLinkHub(this.slot);

        pio.setLink(null);
        pio.setKeypad(null);
        pio.setStdint(null);
        pio.setSeAux(null);
        pio.setLcd(null);
    }

    /**
     * Sets the slot.
     *
     * @param theSlot the slot
     */
    public void setSlot(final int theSlot) {

        this.slot = theSlot;
    }

    /**
     * Gets the slot.
     *
     * @return the slot
     */
    public int getSlot() {

        return this.slot;
    }

    /**
     * Sets the ROM path.
     *
     * @param theRomPath the ROM path
     */
    public void setRomPath(final String theRomPath) {

        this.romPath = theRomPath;
    }

    /**
     * Gets the ROM path.
     *
     * @return the ROM path
     */
    public String getRomPath() {

        return this.romPath;
    }

    /**
     * Sets the calculator model.
     *
     * @param theModel the calculator model
     */
    public void setModel(final EnumCalcModel theModel) {

        this.model = theModel;
    }

    /**
     * Gets the calculator model.
     *
     * @return the calculator model
     */
    public EnumCalcModel getModel() {

        return this.model;
    }

    /**
     * Sets the flag that indicates calculator is active.
     *
     * @param isActive true if active
     */
    public void setActive(final boolean isActive) {

        this.active = isActive;
    }

    /**
     * Tests whether calculator is active.
     *
     * @return true if active
     */
    public boolean isActive() {

        return this.active;
    }

    /**
     * Sets the flag that indicates calculator is running.
     *
     * @param isRunning true if running
     */
    public void setRunning(final boolean isRunning) {

        this.running = isRunning;
    }

    /**
     * Tests whether calculator is running.
     *
     * @return true if running
     */
    public boolean isRunning() {

        return this.running;
    }

    /**
     * Gets the CPU.
     *
     * @return the CPU
     */
    public CPU getCPU() {

        return this.cpu;
    }

    /**
     * Sets the flag that indicates maximum speed.
     *
     * @param isMaxSpeed true for maximum speed
     */
    public void setMaxSpeed(final boolean isMaxSpeed) {

        this.maxSpeed = isMaxSpeed;
    }

    /**
     * Gets the flag that indicates maximum speed.
     *
     * @return true for maximum speed
     */
    public boolean isMaxSpeed() {

        return this.maxSpeed;
    }

    /**
     * Sets the speed.
     *
     * @param theSpeed the speed
     */
    public void setSpeed(final int theSpeed) {

        this.speed = theSpeed;
    }

    /**
     * Gets the speed.
     *
     * @return the speed
     */
    public int getSpeed() {

        return this.speed;
    }

    /**
     * Gets the number of labels.
     *
     * @return the number of labels
     */
    public int getNumLabels() {

        return this.labels.length;
    }

    /**
     * Gets the label at a particular index.
     *
     * @param index the index
     * @return the label
     */
    public Label getLabel(final int index) {

        return this.labels[index];
    }

    /**
     * Set the calculator buttons to a particular profile (a profile is a set of buttons that should be disabled).
     *
     * @param profileName the name of the profile to set the calculator to
     */
    public void setProfile(final String profileName) {

        String line;
        int index;
        int lastIndex;
        String keyName;
        final List<String> list;

        if ("full".equals(profileName)) {
            this.disabledKeys = EMPTY_STRING_ARRAY;
            return;
        }

        // Try setting some disabled keys - copy the XML to a temporary file, then tell the
        // calculator to load it.
        final String name = "jwabbit/profiles/" + profileName + ".xml";

        try (final InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {

            if (input != null) {
                list = new ArrayList<>(10);

                try (final BufferedReader reader =
                             new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    for (line = reader.readLine(); line != null; line = reader.readLine()) {

                        index = line.indexOf("<key value=\"");

                        if (index != -1) {
                            lastIndex = line.indexOf('"', index + 12);

                            if (lastIndex != -1) {
                                keyName = line.substring(index + 12, lastIndex);
                                list.add(keyName);
                            }
                        }
                    }
                }

                this.disabledKeys = list.toArray(EMPTY_STRING_ARRAY);
            } else {
                LoggedObject.LOG.warning("Unable to locate XML resource " + name);
            }
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to read XML key profile ", name, ex);
        }
    }

    /**
     * Gets the list of disabled keys.
     *
     * @return the array of disabled keys
     */
    public String[] getDisabledKeys() {

        return this.disabledKeys == null ? null : this.disabledKeys.clone();
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "audio_init" function.
     *
     * @return the error code
     */
    private int audioInit() {

        final AUDIO audio = this.cpu.getPIOContext().getLink().getAudio();

        audio.setEnabled(false);
        audio.setInit(false);
        audio.setTimerContext(this.cpu.getTimerContext());
        audio.setCpu(this.cpu);

        return 0;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "setup_callbacks" function.
     */
    private void setupCallbacks() {

        this.cpu.setExeViolationCallback(new ExeViolationCallback());
        this.cpu.setInvalidFlashCallback(new InvalidFlashCallback());
        this.cpu.setMemReadBreakCallback(new MemReadCallback());
        this.cpu.setMemWriteBreakCallback(new MemWriteCallback());
        this.cpu.setLcdEnqueueCallback(new LcdEnqueueCallback());
        this.cpu.getPIOContext().setBreakpointCallback(new PortDebugCallback());
        this.cpu.getPIOContext().getLink().getAudio().setAudioFrameCallback(new AudioFrameCallback());
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_81" function.
     *
     * @param version the version string
     * @return the error code
     */
    private int calcInit81(final CharSequence version) {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw81.memoryInit81(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_2);
        this.cpu.clearDevices();

        if (version.charAt(0) == '2') {
            error |= Hw83.deviceInit83(this.cpu, true);
            error |= audioInit();
        } else {
            error |= Hw81.deviceInit81(this.cpu);
        }

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_83" function.
     *
     * @param os the OS name
     * @return the error code
     */
    private int calcInit83(final String os) {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw83.memoryInit83(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_6);
        this.cpu.clearDevices();

        final boolean isBad82 = (this.model == EnumCalcModel.TI_82) && !os.startsWith("19.006");
        error |= Hw83.deviceInit83(this.cpu, isBad82);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_86" function.
     *
     * @return the error code
     */
    private int calcInit86() {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw86.memoryInit86(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_4_8);
        this.cpu.clearDevices();
        error |= Hw86.deviceInit86(this.cpu);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_83p" function.
     *
     * @return the error code
     */
    public int calcInit83p() {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw83p.memoryInit83p(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_6);
        this.cpu.clearDevices();
        error |= Hw83p.deviceInit83p(this.cpu);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_83pse" function.
     *
     * @return the error code
     */
    public int calcInit83pse() {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw83pse.memoryInit83pse(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_6);
        this.cpu.clearDevices();
        error |= Hw83pse.deviceInit83pse(this.cpu, this.model);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_84p" function.
     *
     * @return the error code
     */
    public int calcInit84p() {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw83pse.memoryInit84p(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_6);
        this.cpu.clearDevices();
        error |= Hw83pse.deviceInit83pse(this.cpu, EnumCalcModel.TI_84P);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_84pcse" function.
     *
     * @return the error code
     */
    public int calcInit84pcse() {

        // NOTE: CPU init is moved first since it clears memory
        int error = this.cpu.cpuInit();

        error |= Hw83pse.memoryInit84pcse(this.cpu.getMemoryContext());
        error |= this.cpu.getTimerContext().tcInit(JWCoreConstants.MHZ_6);
        this.cpu.clearDevices();
        error |= Hw83pse.deviceInit83pse(this.cpu, EnumCalcModel.TI_84PCSE);
        error |= audioInit();

        setupCallbacks();

        return error;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_init_model" function.
     *
     * @param theModel  the model
     * @param verString the OS version
     * @return the error code
     */
    private int calcInitModel(final EnumCalcModel theModel, final String verString) {

        return switch (theModel) {
            case TI_81 -> calcInit81(verString);
            case TI_82, TI_83 -> calcInit83(verString);
            case TI_73, TI_83P -> calcInit83p();
            case TI_84PSE, TI_83PSE -> calcInit83pse();
            case TI_84PCSE -> calcInit84pcse();
            case TI_84P -> calcInit84p();
            case TI_85, TI_86 -> calcInit86();
            default -> -1;
        };
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "notify_event" function.
     *
     * @param eventType the event type
     */
    public void notifyEvent(final EnumEventType eventType) {

        for (int i = 0; i < Globals.MAX_REGISTERED_EVENTS; ++i) {
            if (this.registeredEvents[i].getType() == eventType) {
                this.registeredEvents[i].getCallback().exec(this, this.registeredEvents[i].getCalcUI());
            }
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "rom_load" function.
     *
     * @param filename the ROM filename
     * @return true if success
     */
    public boolean romLoad(final String filename) {

        final TIFILE tifile = TIFILE.importvar(filename, false);
        if (tifile == null) {
            return false;
        }

        this.speed = 100;
        if (this.active) {
            Launcher.deleteCalc(this.slot);
        }

        this.model = tifile.getModel();

        int error;
        if (tifile.getType() == TIFILE.SAV_TYPE) {
            this.active = true;

            final String[] verString = new String[1];
            if (tifile.getModel() == EnumCalcModel.TI_81 || tifile.getModel() == EnumCalcModel.TI_82
                    || tifile.getModel() == EnumCalcModel.TI_83) {

                final int[] size = new int[1];
                final int[] rom = tifile.getSave().getRomOnly(size);
                Var.findRomVersion(verString, rom, size[0]);
            }

            error = calcInitModel(tifile.getModel(), verString[0]);
            if (error != 0) {
                tifile.freeTiFile();
                return false;
            }

            error = tifile.getSave().loadSlot(this) ? 0 : 1;
            if (error != 0) {
                tifile.freeTiFile();
                return false;
            }

            this.romPath = filename;
            final String[] version = new String[1];
            Var.findRomVersion(version, this.cpu.getMemoryContext().getFlash().asArray(),
                    this.cpu.getMemoryContext().getFlash().getSize());

        } else if (tifile.getType() == TIFILE.ROM_TYPE) {

            switch (tifile.getModel()) {
                case TI_81:
                    error = calcInit81(tifile.getRom().getVersion());
                    copyRomToFlash(tifile);
                    break;
                case TI_82:
                case TI_83:
                    error = calcInit83(tifile.getRom().getVersion());
                    copyRomToFlash(tifile);
                    break;
                case TI_85:
                case TI_86:
                    error = calcInit86();
                    copyRomToFlash(tifile);
                    break;
                case TI_73:
                case TI_83P:
                    error = calcInit83p();
                    copyRomToFlash(tifile);
                    Globals.calcEraseCertificate(this.cpu.getMemoryContext().getFlash(),
                            this.cpu.getMemoryContext().getFlash().getSize());
                    break;
                case TI_84P:
                    error = calcInit84p();
                    copyRomToFlash(tifile);
                    Globals.calcEraseCertificate(this.cpu.getMemoryContext().getFlash(),
                            this.cpu.getMemoryContext().getFlash().getSize());
                    break;
                case TI_84PSE:
                case TI_83PSE:
                    error = calcInit83pse();
                    copyRomToFlash(tifile);
                    Globals.calcEraseCertificate(this.cpu.getMemoryContext().getFlash(),
                            this.cpu.getMemoryContext().getFlash().getSize());
                    break;
                case TI_84PCSE:
                    error = calcInit84pcse();
                    copyRomToFlash(tifile);
                    Globals.calcEraseCertificate(this.cpu.getMemoryContext().getFlash(),
                            this.cpu.getMemoryContext().getFlash().getSize());
                    break;

                case INVALID_MODEL:
                default:
                    LoggedObject.LOG.warning("Unhandled case in romLoad: ", tifile.getModel());
                    tifile.freeTiFile();
                    return false;
            }

            if (error != 0) {
                tifile.freeTiFile();
                return false;
            }

            this.active = true;
            this.romPath = filename;

            // Handle saving successfully loaded ROM location better!
            Registry.saveWabbitKey("rom_path", filename);
        } else {
            LoggedObject.LOG.warning("Unhandled file type in romLoad: " + tifile.getType());
            return false;
        }

        this.cpu.getPIOContext().setModel(this.model);

        if (tifile.getSave() == null) {
            calcReset();
        }

        this.running = true;
        if (Globals.get().isAutoTurnOn()) {
            calcTurnOn();
        }

        notifyEvent(EnumEventType.ROM_LOAD_EVENT);

        tifile.freeTiFile();
        return true;
    }

    /**
     * Copies the loaded ROM image into flash.
     *
     * <p>
     * (Aggregates common operations from several WABBITEMU calls).
     *
     * @param file the file with the loaded ROM image
     */
    private void copyRomToFlash(final TIFILE file) {

        final int toCopy = Math.min(this.cpu.getMemoryContext().getFlash().getSize(), file.getRom().getSize());

        final int[] tempFlash = new int[toCopy];
        System.arraycopy(file.getRom().getData(), 0, tempFlash, 0, toCopy);
        this.cpu.getMemoryContext().getFlash().load(tempFlash);
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_reset" function.
     */
    public void calcReset() {

        this.cpu.cpuReset();
        final AbstractLCDBase lcdBase = this.cpu.getPIOContext().getLcd();
        if (lcdBase instanceof LCD) {
            ((LCD) lcdBase).reset();
        } else if (lcdBase instanceof ColorLCD) {
            ((ColorLCD) lcdBase).reset();
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_run_tstates" function.
     *
     * @param states the number of timer states
     */
    public void calcRunTstates(final long states) {

        final long timeEnd = this.cpu.getTimerContext().getTStates() + states - this.timeError;

        // No breakpoint test on the line we're running from, so we can run from a breakpoint without
        // stopping instantly
        if (this.running) {
            this.cpu.step();
            postStep();
        }

        while (this.running) {
            final int pc = this.cpu.getPC();

            if (this.cpu.getMemoryContext().checkBreak(this.cpu.getMemoryContext().addr16ToWideAddr(pc))) {
                this.running = false;
                this.breakpointCallback.exec(this);
                return;
            }

            this.cpu.step();
            postStep();

            if (this.cpu.getTimerContext().getTStates() >= timeEnd) {
                this.timeError = this.cpu.getTimerContext().getTStates() - timeEnd;
                break;
            }
        }
    }

    /**
     * Executes instructions until some number of timer states have elapsed, or the PC reaches a specified target
     * address (the instruction at the target address will not be executed).
     *
     * @param states       the number of timer states
     * @param runToAddress an optional address at which to stop running
     */
    public void calcRunToAddr(final long states, final WideAddr runToAddress) {

        final long timeEnd = this.cpu.getTimerContext().getTStates() + states - this.timeError;

        this.cpu.step();
        postStep();

        while (this.running) {
            final int pc = this.cpu.getPC();

            if (runToAddress != null) {
                final WideAddr test = this.cpu.getMemoryContext().addr16ToWideAddr(pc);
                if (test.getPage() == runToAddress.getPage() && test.getAddr() == runToAddress.getAddr()) {
                    return;
                }
            }

            if (this.cpu.getMemoryContext().checkBreak(this.cpu.getMemoryContext().addr16ToWideAddr(pc))) {
                this.running = false;
                this.breakpointCallback.exec(this);
                return;
            }

            this.cpu.step();
            postStep();

            if (this.cpu.getTimerContext().getTStates() >= timeEnd) {
                this.timeError = this.cpu.getTimerContext().getTStates() - timeEnd;
                break;
            }
        }
    }

    /**
     * Executes instructions until some number of timer states have elapsed, or a return instruction has just been
     * executed.
     *
     * @param states the number of timer states
     */
    public void calcRunToReturn(final long states) {

        final long timeEnd = this.cpu.getTimerContext().getTStates() + states - this.timeError;

        this.cpu.step();
        postStep();

        while (this.running) {
            final int pc = this.cpu.getPC();

            if (this.cpu.getMemoryContext().checkBreak(this.cpu.getMemoryContext().addr16ToWideAddr(pc))) {
                this.running = false;
                this.breakpointCallback.exec(this);
                return;
            }

            this.cpu.step();
            postStep();

            if (this.cpu.getTimerContext().getTStates() >= timeEnd) {
                this.timeError = this.cpu.getTimerContext().getTStates() - timeEnd;
                break;
            }

            if (this.cpu.isReturned()) {
                break;
            }
        }
    }

    /**
     * Runs the calculator for some number of instruction steps.
     *
     * @param steps the number of steps
     */
    public void calcRunSteps(final int steps) {

        int remaining = steps;

        this.cpu.step();
        postStep();

        --remaining;

        while (remaining > 0) {
            final int pc = this.cpu.getPC();

            if (this.cpu.getMemoryContext().checkBreak(this.cpu.getMemoryContext().addr16ToWideAddr(pc))) {
                this.running = false;
                this.breakpointCallback.exec(this);
                return;
            }

            this.cpu.step();
            postStep();

            --remaining;
        }
    }

    /**
     * Performs processing after each instruction step.
     */
    private void postStep() {

        final AbstractLCDBase lcdBase = this.cpu.getPIOContext().getLcd();
        if (lcdBase != null) {
            if ((this.cpu.getTimerContext().getElapsed() - lcdBase.getLastAviFrame())
                    >= (1.0 / (double) Globals.AVI_FPS)) {
                this.notifyEvent(EnumEventType.AVI_VIDEO_FRAME_EVENT);
                lcdBase.setLastAviFrame(lcdBase.getLastAviFrame() + 1.0 / (double) Globals.AVI_FPS);
            }
        }
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_turn_on" function.
     */
    public void calcTurnOn() {

        if (this.cpu.getPIOContext().getLcd().isActive()) {
            // LOG.info("LCD already active, not turning on");
            return;
        }

        final boolean origRunning = this.running;
        while (this.fakeRunning) {
            try {
                Thread.sleep(10L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        this.fakeRunning = true;
        this.running = true;

        int time = this.cpu.getTimerContext().getFreq();
        if (this.model.ordinal() >= EnumCalcModel.TI_84PCSE.ordinal()) {
            time <<= 1;
        }

        calcRunTstates((long) time);

        final Keypad keypad = this.cpu.getPIOContext().getKeypad();
        keypad.keypadPress(this, HardwareConstants.KEYGROUP_ON, HardwareConstants.KEYBIT_ON);
        calcRunTstates((long) (time / 2));
        keypad.keypadRelease(this, HardwareConstants.KEYGROUP_ON, HardwareConstants.KEYBIT_ON);

        int tries = 0;
        do {
            ++tries;
            calcRunTstates((long) (time / 2));
        } while (!this.cpu.isHalt() && tries < 3);

        this.running = origRunning;
        this.fakeRunning = false;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_register_event" function.
     *
     * @param eventType the event type
     * @param callback  the callback
     * @param theCalcUI the calculator UI to pass to the callback
     */
    public void calcRegisterEvent(final EnumEventType eventType, final IEventCallback callback,
                                  final CalcUI theCalcUI) {

        for (int i = 0; i < Globals.MAX_REGISTERED_EVENTS; ++i) {
            if (this.registeredEvents[i].getType() == EnumEventType.NO_EVENT) {
                this.registeredEvents[i].setType(eventType);
                this.registeredEvents[i].setCallback(callback);
                this.registeredEvents[i].setCalcUI(theCalcUI);
                break;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/label.c, "VoidLabels" function.
     */
    public void voidLabels() {

        for (int i = 0; this.labels[i].getName() != null; ++i) {
            this.labels[i].setName(null);
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/label.c, "FindAddressLabel" function.
     *
     * @param waddr the wide address whose label to find
     * @return the label if found, {@code null} if no label for the wide address
     */
    public String findAddressLabel(final WideAddr waddr) {

        for (int i = 0; this.labels[i].getName() != null; ++i) {
            final Label label = this.labels[i];

            if (label.isRam() == waddr.isRam() && label.getPage() == waddr.getPage()
                    && BankState.mcBase(label.getAddr()) == BankState.mcBase(waddr.getAddr())) {

                return label.getName();
            }
        }

        return null;
    }

    /**
     * Tests whether a key is currently enabled.
     *
     * @param group the group
     * @param bit   the bit
     * @param state the state when the key was pressed
     * @return true if enabled; false if disabled
     */
    public boolean isKeyEnabled(final int group, final int bit, final EnumKeypadState state) {

        final KeyName[] names = switch (this.model) {
            case TI_83P, TI_83PSE, TI_84P, TI_84PSE, TI_84PCSE -> KeyNames.KEY_NAMES_83P_83PSE_84PSE;
            default -> NO_KEY_NAMES;
        };

        String name = null;
        for (final KeyName keyName : names) {
            if (keyName.getGroup() == group && keyName.getBit() == bit && keyName.getState() == state) {
                name = keyName.getName();
            }
        }

        // See if the key is in the list of disabled keys
        boolean enabled = true;
        if (name != null && this.disabledKeys != null) {
            for (final String disabledKey : this.disabledKeys) {
                if (name.equals(disabledKey)) {
                    enabled = false;
                    break;
                }
            }
        }

        return enabled;
    }
}
