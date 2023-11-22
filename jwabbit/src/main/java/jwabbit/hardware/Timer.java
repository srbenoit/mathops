package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "TIMER" struct.
 */
public final class Timer {

    /** The last timer states. */
    private long lastTstates;

    /** The last ticks. */
    private double lastTicks;

    /** The divisor. */
    private double divisor;

    /** The loop flag. */
    private boolean loop;

    /** The interrupt flag. */
    private boolean interrupt;

    /** The underflow flag. */
    private boolean underflow;

    /** The generate flag. */
    private boolean generate;

    /** The active flag. */
    private boolean active;

    /** The clock. */
    private int clock;

    /** The count. */
    private int count;

    /** The maximum. */
    private int max;

    /**
     * Constructs a new {@code Timer}.
     */
    public Timer() {

        // No action
    }

    /**
     * Gets the last timer states.
     *
     * @return the last timer states
     */
    public long getLastTstates() {

        return this.lastTstates;
    }

    /**
     * Sets the last timer states.
     *
     * @param theLastTstates the last timer states
     */
    public void setLastTstates(final long theLastTstates) {

        this.lastTstates = theLastTstates;
    }

    /**
     * Gets the last ticks.
     *
     * @return the last ticks
     */
    public double getLastTicks() {

        return this.lastTicks;
    }

    /**
     * Sets the last ticks.
     *
     * @param theLastTicks the last ticks
     */
    public void setLastTicks(final double theLastTicks) {

        this.lastTicks = theLastTicks;
    }

    /**
     * Gets the divisor.
     *
     * @return the divisor
     */
    public double getDivisor() {

        return this.divisor;
    }

    /**
     * Sets the divisor.
     *
     * @param theDivisor the divisor
     */
    public void setDivisor(final double theDivisor) {

        this.divisor = theDivisor;
    }

    /**
     * Gets the loop flag.
     *
     * @return the loop flag
     */
    public boolean isLoop() {

        return this.loop;
    }

    /**
     * Sets the loop flag.
     *
     * @param isLoop the loop flag
     */
    public void setLoop(final boolean isLoop) {

        this.loop = isLoop;
    }

    /**
     * Gets the interrupt flag.
     *
     * @return the interrupt flag
     */
    public boolean isInterrupt() {

        return this.interrupt;
    }

    /**
     * Sets the interrupt flag.
     *
     * @param isInterrupt the interrupt flag
     */
    public void setInterrupt(final boolean isInterrupt) {

        this.interrupt = isInterrupt;
    }

    /**
     * Gets the underflow flag.
     *
     * @return the underflow flag
     */
    public boolean isUnderflow() {

        return this.underflow;
    }

    /**
     * Sets the underflow flag.
     *
     * @param isUnderflow the underflow flag
     */
    public void setUnderflow(final boolean isUnderflow) {

        this.underflow = isUnderflow;
    }

    /**
     * Gets the generate flag.
     *
     * @return the generate flag
     */
    public boolean isGenerate() {

        return this.generate;
    }

    /**
     * Sets the generate flag.
     *
     * @param isGenerate the generate flag
     */
    public void setGenerate(final boolean isGenerate) {

        this.generate = isGenerate;
    }

    /**
     * Gets the active flag.
     *
     * @return the active flag
     */
    public boolean isActive() {

        return this.active;
    }

    /**
     * Sets the active flag.
     *
     * @param isActive the active flag
     */
    public void setActive(final boolean isActive) {

        this.active = isActive;
    }

    /**
     * Gets the clock.
     *
     * @return the clock
     */
    public int getClock() {

        return this.clock & 0x00FF;
    }

    /**
     * Sets the clock.
     *
     * @param theClock the clock
     */
    public void setClock(final int theClock) {

        this.clock = theClock;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {

        return this.count & 0x00FF;
    }

    /**
     * Sets the count.
     *
     * @param theCount the count
     */
    public void setCount(final int theCount) {

        this.count = theCount;
    }

    /**
     * Gets the maximum.
     *
     * @return the maximum
     */
    public int getMax() {

        return this.max;
    }

    /**
     * Sets the maximum.
     *
     * @param theMax the maximum
     */
    public void setMax(final int theMax) {

        this.max = theMax & 0x00FF;
    }
}
