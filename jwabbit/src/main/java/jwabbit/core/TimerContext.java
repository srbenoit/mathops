package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A timer context.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "timer_context" struct.
 */
public final class TimerContext {

    /** False to turn off accounting of total elapsed time. */
    private static final boolean TIMER_ELAPSED = true;

    /** Number of ticks since last reset. */
    private long tStates;

    /** Clock frequency, in ticks per second. */
    private int freq;

    /** Elapsed time, in seconds (used only if {@code NO_TIMER_ELAPSED} is false). */
    private double elapsed;

    /** This isn't used anymore (except for sound and interrupts). */
    private double lasttime;

    /** The timer version. */
    private int timerVersion;

    /**
     * Constructs a new {@code TimerContext}.
     */
    private TimerContext() {

        super();

        clear();
    }

    /**
     * Constructs a new {@code TimerContext}.
     *
     * @return the constructed {@code TimerContext}
     */
    static TimerContext createTimerContext() {

        return new TimerContext();
    }

    /**
     * Clears the structure as if memset(0) were called.
     */
    void clear() {

        this.tStates = 0L;
        this.freq = 0;
        this.elapsed = 0.0;
        this.lasttime = 0.0;
        this.timerVersion = 0;
    }

    /**
     * Gets the total number of ticks since the last reset.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "tc_tstates" macro.
     *
     * @return the tick count
     */
    public long getTStates() {

        return this.tStates;
    }

    /**
     * Sets the tick count since last reset.
     *
     * @param newTStates the new tick count
     */
    public void setTStates(final long newTStates) {

        this.tStates = newTStates;
    }

    /**
     * Gets the frequency.
     *
     * @return the frequency, in ticks per second
     */
    public int getFreq() {

        return this.freq;
    }

    /**
     * Sets the frequency.
     *
     * @param newFreq the new frequency, in ticks per second
     */
    public void setFreq(final int newFreq) {

        this.freq = newFreq;
    }

    /**
     * Gets the total elapsed time.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "tc_elapsed" macro.
     *
     * @return the elapsed time, in seconds
     */
    public double getElapsed() {

        return this.elapsed;
    }

    /**
     * Sets the elapsed time since last reset.
     *
     * @param newElapsed the new elapsed time, in seconds
     */
    public void setElapsed(final double newElapsed) {

        this.elapsed = newElapsed;
    }

    /**
     * Gets the last time.
     *
     * @return the last time, in seconds
     */
    public double getLasttime() {

        return this.lasttime;
    }

    /**
     * Sets the last time.
     *
     * @param newLast the new last time, in seconds
     */
    public void setLastTime(final double newLast) {

        this.lasttime = newLast;
    }

    /**
     * Gets the timer version.
     *
     * @return the timer version
     */
    public int getTimerVersion() {

        return this.timerVersion;
    }

    /**
     * Initialize a timer context.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "tc_init" macro.
     *
     * @param theFreq the timer frequency
     * @return 0
     */
    public int tcInit(final int theFreq) {

        this.tStates = 0L;
        this.elapsed = 0.0;
        this.freq = theFreq;

        return 0;
    }

    /**
     * Adds a number of ticks to the {@code tstates} counter and adds the number of seconds this tick count represents
     * to the elapsed time.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "tc_add" macro.
     *
     * @param num the number of ticks to add
     */
    public void tcAdd(final long num) {

        this.tStates += num;

        if (TIMER_ELAPSED) {
            this.elapsed += (double) num / (double) this.freq;
        }
    }

    /**
     * Subtracts a number of ticks from the {@code tstates} counter and subtracts the number of seconds this tick count
     * represents from the elapsed time.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "tc_sub" macro.
     *
     * @param num the number of ticks to add
     */
    public void tcSub(final long num) {

        this.tStates -= num;

        if (TIMER_ELAPSED) {
            this.elapsed -= (double) num / (double) this.freq;
        }
    }

    /**
     * Adds a number of ticks to the {@code tstates} counter and adds the number of seconds this tick count represents
     * to the elapsed time, but only if the model of a calculator is {@code EnumCalcModel.TI_83PSE} or higher.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "SEtc_add" macro.
     *
     * @param cpu the CPU whose model to test
     * @param num the number of ticks to add
     */
    void setcAdd(final CPU cpu, final long num) {

        if (cpu.getPIOContext().getModel().ordinal() >= EnumCalcModel.TI_83PSE.ordinal()) {
            this.tStates += num;

            if (TIMER_ELAPSED) {
                this.elapsed += (double) num / (double) this.freq;
            }
        }
    }
}
