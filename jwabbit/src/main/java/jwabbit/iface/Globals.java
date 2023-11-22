package jwabbit.iface;

import jwabbit.core.Memory;
import jwabbit.log.LoggedObject;

/**
 * A singleton object that collects globals from various source files.
 */
public final class Globals {

    /** WABBITEMU SOURCE: interface/calc.c, "FRAME_SUBDIVISIONS" macro. */
    public static final int FRAME_SUBDIVISIONS = 1024;

    /** WABBITEMU SOURCE: interface/calc.h, "MAX_REGISTERED_EVENTS" macro. */
    static final int MAX_REGISTERED_EVENTS = 0xFF;

    /** WABBITEMU SOURCE: interface/calc.h, "AVI_FPS" macro. */
    static final int AVI_FPS = 24;

    /** Object on which to synchronize creation of singleton instance. */
    private static final Object INST_SYNCH = new Object();

    /** The singleton instance. */
    private static Globals instance;

    /** WABBITEMU SOURCE: interface/calc.h, "break_on_exe_violation" boolean. */
    private final boolean breakOnExeViolation;

    /** WABBITEMU SOURCE: interface/calc.h, "break_on_invalid_flash" boolean. */
    private final boolean breakOnInvalidFlash;

    /** WABBITEMU SOURCE: interface/calc.h, "auto_turn_on" boolean. */
    private boolean autoTurnOn;

    /** WABBITEMU SOURCE: interface/calc.h, "portSettingsPath" character array. */
    private String portSettingsPath;

    /**
     * Constructs a new {@code Globals}.
     */
    private Globals() {

        super();

        this.breakOnExeViolation = false;
        this.breakOnInvalidFlash = false;
        this.autoTurnOn = false;
    }

    /**
     * Gets the singleton instance, creating it if it has not yet been created.
     *
     * @return the singleton instance
     */
    public static Globals get() {

        synchronized (INST_SYNCH) {
            if (instance == null) {
                instance = new Globals();
            }

            return instance;
        }
    }

    /**
     * Sets the flag that determines whether we should create new calculator on file load.
     *
     * @param isNewOnLoad true to create new calculator on file load
     */
    public void setNewCalcOnLoadFiles(final boolean isNewOnLoad) {

    }

    /**
     * Tests whether we should break on execution violation.
     *
     * @return true to break
     */
    public boolean isBreakOnExeViolation() {

        return this.breakOnExeViolation;
    }

    /**
     * Tests whether we should break on invalid flash.
     *
     * @return true to break
     */
    public boolean isBreakOnInvalidFlash() {

        return this.breakOnInvalidFlash;
    }

    /**
     * Tests whether we should automatically turn on calculator.
     *
     * @return true to automatically turn on calculator
     */
    boolean isAutoTurnOn() {

        return this.autoTurnOn;
    }

    /**
     * Sets the flag that determines whether we should automatically turn on calculator.
     *
     * @param isAutoTurnOn true to automatically turn on calculator
     */
    public void setAutoTurnOn(final boolean isAutoTurnOn) {

        this.autoTurnOn = isAutoTurnOn;
    }

    /**
     * WABBITEMU SOURCE: interface/calc.c, "calc_erase_certificate" function.
     *
     * @param mem  the memory
     * @param size the size
     */
    public static void calcEraseCertificate(final Memory mem, final int size) {

        if (mem == null || size < 0x8000) {
            LoggedObject.LOG.warning("Invalid arguments to erase certificate", mem, " ", Integer.toString(size),
                    new Exception());
            return;
        }

        for (int i = size - 0x8000; i < size - 0x8000 + Memory.PAGE_SIZE; ++i) {
            mem.set(i, 0x00FF);
        }

        mem.set(size - 0x8000, 0x00);
        mem.set(size - 0x8000 + 0x1FE0, 0x00);
        mem.set(size - 0x8000 + 0x1FE1, 0x00);
    }
}
