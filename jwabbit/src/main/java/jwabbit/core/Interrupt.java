package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An interrupt.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "interrupt" struct.
 */
public final class Interrupt {

    /** The interrupt value. */
    private int interruptVal;

    /** The skip value. */
    private int skipFactor;

    /** The skip count. */
    private int skipCount;

    /** The device. */
    private IDevice device;

    /**
     * Constructs a new {@code Interrupt}.
     */
    Interrupt() {

        // No action
    }

    /**
     * Gets the interrupt value.
     *
     * @return the interrupt value
     */
    public int getInterruptVal() {

        return this.interruptVal;
    }

    /**
     * Sets the interrupt value.
     *
     * @param theInterruptVal the interrupt value
     */
    public void setInterruptVal(final int theInterruptVal) {

        this.interruptVal = theInterruptVal & 0x00FF;
    }

    /**
     * Gets the skip factor.
     *
     * @return the skip factor
     */
    public int getSkipFactor() {

        return this.skipFactor;
    }

    /**
     * Sets the skip factor.
     *
     * @param theSkipFactor the skip factor
     */
    public void setSkipFactor(final int theSkipFactor) {

        this.skipFactor = theSkipFactor & 0x00FF;
    }

    /**
     * Gets the skip count.
     *
     * @return the skip count
     */
    public int getSkipCount() {

        return this.skipCount;
    }

    /**
     * Sets the skip count.
     *
     * @param theSkipCount the skip count
     */
    public void setSkipCount(final int theSkipCount) {

        this.skipCount = theSkipCount & 0x00FF;
    }

    /**
     * Gets the device.
     *
     * @return the device
     */
    public IDevice getDevice() {

        return this.device;
    }

    /**
     * Sets the device.
     *
     * @param theDevice the device
     */
    public void setDevice(final IDevice theDevice) {

        this.device = theDevice;
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    void clear() {

        this.interruptVal = 0;
        this.skipFactor = 0;
        this.skipCount = 0;
        this.device = null;
    }
}
