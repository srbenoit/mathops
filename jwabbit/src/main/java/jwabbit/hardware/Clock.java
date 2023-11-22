package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "CLOCK" struct.
 */
public final class Clock {

    /** Enable. */
    private int enable;

    /** Set. */
    private long set;

    /** Base. */
    private long base;

    /** Last time. */
    private double lasttime;

    /**
     * Constructs a new {@code Clock}.
     */
    Clock() {

        super();
    }

    /**
     * Gets the enabled state.
     *
     * @return the enabled state
     */
    public int getEnable() {

        return this.enable;
    }

    /**
     * Sets the enabled state.
     *
     * @param theEnable the enabled state
     */
    public void setEnable(final int theEnable) {

        this.enable = theEnable & 0x00FF;
    }

    /**
     * Gets the set.
     *
     * @return the set
     */
    public long getSet() {

        return this.set;
    }

    /**
     * Sets the set.
     *
     * @param theSet the set
     */
    public void setSet(final long theSet) {

        this.set = theSet;
    }

    /**
     * Gets the base.
     *
     * @return the base
     */
    public long getBase() {

        return this.base;
    }

    /**
     * Sets the base.
     *
     * @param theBase the base
     */
    public void setBase(final long theBase) {

        this.base = theBase;
    }

    /**
     * Gets the last time.
     *
     * @return the last time
     */
    public double getLasttime() {

        return this.lasttime;
    }

    /**
     * Sets the last time.
     *
     * @param theLasttime the last time
     */
    public void setLasttime(final double theLasttime) {

        this.lasttime = theLasttime;
    }
}
