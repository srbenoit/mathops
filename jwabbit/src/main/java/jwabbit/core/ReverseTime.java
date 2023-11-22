package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A tracking object to allow time reversal on processor execution.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "reverse_time" struct.
 */
public final class ReverseTime {

    /** The flags register value. */
    private int flag;

    /** A data value. */
    private final RegPair data1 = new RegPair();

    /** A data value. */
    private final RegPair data2 = new RegPair();

    /** The bus value. */
    private int bus;

    /** The r register value. */
    private int r;

    /**
     * Constructs a new {@code ReverseTime}.
     */
    ReverseTime() {

        // No action
    }

    /**
     * Clears the data values to zero.
     */
    void clear() {

        this.flag = 0;
        this.data1.set(0);
        this.data2.set(0);
        this.bus = 0;
        this.r = 0;
    }

    /**
     * Gets the flag register value.
     *
     * @return the flag register value
     */
    public int getFlag() {

        return this.flag;
    }

    /**
     * Sets the flag register value.
     *
     * @param theFlag the flag register value
     */
    public void setFlag(final int theFlag) {

        this.flag = theFlag & 0x00FF;
    }

    /**
     * Gets theCPU bus value.
     *
     * @return the CPU bus value
     */
    public int getBus() {

        return this.bus;
    }

    /**
     * Sets theCPU bus value.
     *
     * @param theBus the CPU bus value
     */
    public void setBus(final int theBus) {

        this.bus = theBus & 0x00FF;
    }

    /**
     * Gets the value of the data1 register.
     *
     * @return the data1 register value
     */
    public int getData1() {

        return this.data1.get();
    }

    /**
     * Gets the low byte of the value of the data1 register.
     *
     * @return the low byte of the data1 register value
     */
    public int getData1Lo() {

        return this.data1.getLo();
    }

    /**
     * Sets the value of the data1 register.
     *
     * @param theData1 the data1 register value
     */
    public void setData1(final int theData1) {

        this.data1.set(theData1);
    }

    /**
     * Sets the low byte of the data1 register.
     *
     * @param theData1 the data1 register low byte
     */
    public void setData1Lo(final int theData1) {

        this.data1.setLo(theData1);
    }

    /**
     * Sets the high byte of the data1 register.
     *
     * @param theData1 the data1 register high byte
     */
    public void setData1Hi(final int theData1) {

        this.data1.setHi(theData1);
    }

    /**
     * Gets the value of the data2 register.
     *
     * @return the data2 register value
     */
    public int getData2() {

        return this.data2.get();
    }

    /**
     * Gets the low byte of the value of the data2 register.
     *
     * @return the low byte of the data2 register value
     */
    public int getData2Lo() {

        return this.data2.getLo();
    }

    /**
     * Sets the value of the data2 register.
     *
     * @param theData2 the data2 register value
     */
    public void setData2(final int theData2) {

        this.data2.set(theData2);
    }

    /**
     * Sets the low byte of the data2 register.
     *
     * @param theData2 the data2 register low byte
     */
    public void setData2Lo(final int theData2) {

        this.data2.setLo(theData2);
    }

    /**
     * Sets the high byte of the data2 register.
     *
     * @param theData2 the data2 register high byte
     */
    public void setData2Hi(final int theData2) {

        this.data2.setHi(theData2);
    }

    /**
     * Gets the R register value.
     *
     * @return the R register value
     */
    public int getR() {

        return this.r;
    }

    /**
     * Sets the R register value.
     *
     * @param theR the R register value
     */
    public void setR(final int theR) {

        this.r = theR & 0x00FF;
    }
}
