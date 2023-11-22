package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/ti_stdint.h, "STDINT" struct.
 */
public final class STDINT {

    /** The active interrupt mask. */
    private int intactive;

    /** The last check time 1. */
    private double lastchk1;

    /** The timer max 1. */
    private double timermax1;

    /** The last check time 2. */
    private double lastchk2;

    /** The timer max 2. */
    private double timermax2;

    /** The frequency. */
    private final double[] freq = new double[4];

    /** The mem. */
    private int mem;

    /** The xy. */
    private int xy;

    /** On backup. */
    private int onBackup;

    /** On latch. */
    private boolean onLatch;

    /**
     * STDINT.
     */
    public STDINT() {

        super();
    }

    /**
     * Gets the active interrupt mask value.
     *
     * <pre>
     * 0x01 = Interrupt when on key pressed
     * 0x02 = Single-speed standard interrupt, lastchk1 and timermax1
     * 0x04 = Double-speed standard interrupt, lastchk2 and timermax2
     * 0x08 = if 0, LCD can be turned on/off
     * </pre>
     *
     * @return the value
     */
    public int getIntactive() {

        return this.intactive;
    }

    /**
     * Sets the active interrupt mask value.
     *
     * @param theIntactive the value
     */
    public void setIntactive(final int theIntactive) {

        this.intactive = theIntactive;
    }

    /**
     * Gets the last check 1 time.
     *
     * @return the last check 1 time
     */
    public double getLastchk1() {

        return this.lastchk1;
    }

    /**
     * Sets the last check 1 time.
     *
     * @param theLastchk1 the last check 1 time
     */
    public void setLastchk1(final double theLastchk1) {

        this.lastchk1 = theLastchk1;
    }

    /**
     * Gets the timer max 1.
     *
     * @return the timer max
     */
    public double getTimermax1() {

        return this.timermax1;
    }

    /**
     * Sets the timer max 1.
     *
     * @param theTimermax1 the timer max
     */
    public void setTimermax1(final double theTimermax1) {

        this.timermax1 = theTimermax1;
    }

    /**
     * Gets the last check 2 time.
     *
     * @return the last check 2 time
     */
    public double getLastchk2() {

        return this.lastchk2;
    }

    /**
     * Sets the last check 2 time.
     *
     * @param theLastchk2 the last check 2 time
     */
    public void setLastchk2(final double theLastchk2) {

        this.lastchk2 = theLastchk2;
    }

    /**
     * Gets the timer max 2.
     *
     * @return the timer max
     */
    public double getTimermax2() {

        return this.timermax2;
    }

    /**
     * Sets the timer max 2.
     *
     * @param theTimermax2 the timer max
     */
    public void setTimermax2(final double theTimermax2) {

        this.timermax2 = theTimermax2;
    }

    /**
     * Gets an element of the frequency array.
     *
     * @param index the index
     * @return the frequency
     */
    public double getFreq(final int index) {

        return this.freq[index];
    }

    /**
     * Sets an element of the frequency array.
     *
     * @param index the index
     * @param value the new value
     */
    public void setFreq(final int index, final double value) {

        this.freq[index] = value;
    }

    /**
     * Gets the mem value.
     *
     * @return the mem value
     */
    public int getMem() {

        return this.mem;
    }

    /**
     * Sets the memory value.
     *
     * @param theMem the memory value
     */
    public void setMem(final int theMem) {

        this.mem = theMem & 0x00FF;
    }

    /**
     * Gets the XY value.
     *
     * @return the XY value
     */
    public int getXy() {

        return this.xy;
    }

    /**
     * Sets the XY value.
     *
     * @param theXy the XY value
     */
    public void setXy(final int theXy) {

        this.xy = theXy & 0x00FF;
    }

    /**
     * Gets the on backup flag.
     *
     * @return the on backup
     */
    public int getOnBackup() {

        return this.onBackup;
    }

    /**
     * Sets the on backup flag.
     *
     * @param theOnBackup the on backup
     */
    public void setOnBackup(final int theOnBackup) {

        this.onBackup = theOnBackup;
    }

    /**
     * Gets the on latch flag.
     *
     * @return the on latch
     */
    public boolean isOnLatch() {

        return this.onLatch;
    }

    /**
     * Sets the on latch flag.
     *
     * @param theOnLatch the on latch
     */
    public void setOnLatch(final boolean theOnLatch) {

        this.onLatch = theOnLatch;
    }
}
