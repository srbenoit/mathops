package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "XTAL" struct.
 */
public final class XTAL {

    /** Actual real time of last tick. */
    private double lastTime;

    /** Ticks of the xtal timer. */
    private long ticks;

    /** Timers. */
    private final Timer[] timers;

    /**
     * Constructs a new {@code XTAL}.
     */
    XTAL() {

        super();

        this.timers = new Timer[]{new Timer(), new Timer(), new Timer()};
    }

    /**
     * Gets the actual real time of last tick.
     *
     * @return the last time
     */
    public double getLastTime() {

        return this.lastTime;
    }

    /**
     * Sets the actual real time of last tick.
     *
     * @param theLastTime the last time
     */
    public void setLastTime(final double theLastTime) {

        this.lastTime = theLastTime;
    }

    /**
     * Gets the ticks of the xtal timer.
     *
     * @return the ticks
     */
    public long getTicks() {

        return this.ticks;
    }

    /**
     * Sets the ticks of the xtal timer.
     *
     * @param theTicks the ticks
     */
    public void setTicks(final long theTicks) {

        this.ticks = theTicks;
    }

    /**
     * Gets the number of timers.
     *
     * @return the number of timers
     */
    public int getNumTimers() {

        return this.timers.length;
    }

    /**
     * Gets a timer.
     *
     * @param index the index
     * @return the timer
     */
    public Timer getTimer(final int index) {

        return this.timers[index];
    }
}
