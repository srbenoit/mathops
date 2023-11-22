package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "MD5" struct.
 */
public final class MD5 {

    /** Mask to get only low-order 32 bits, leaving upper bits zero. */
    private static final long UINT32_MASK = 0x0FFFFFFFFL;

    /** The A register. */
    private long a;

    /** The B register. */
    private long b;

    /** The C register. */
    private long c;

    /** The D register. */
    private long d;

    /** The X register. */
    private long x;

    /** The AC register. */
    private long ac;

    /** The S register. */
    private int s;

    /** The mode. */
    private int mode;

    /**
     * Constructs a new {@code MD5}.
     */
    MD5() {

        // No action
    }

    /**
     * Gets the A register value.
     *
     * @return the A register value
     */
    public long getA() {

        return this.a;
    }

    /**
     * Sets the A register value.
     *
     * @param theA the A register value
     */
    public void setA(final long theA) {

        this.a = theA & UINT32_MASK;
    }

    /**
     * Gets the B register value.
     *
     * @return the B register value
     */
    public long getB() {

        return this.b;
    }

    /**
     * Sets the B register value.
     *
     * @param theB the B register value
     */
    public void setB(final long theB) {

        this.b = theB & UINT32_MASK;
    }

    /**
     * Gets the C register value.
     *
     * @return the C register value
     */
    public long getC() {

        return this.c;
    }

    /**
     * Sets the C register value.
     *
     * @param theC the C register value
     */
    public void setC(final long theC) {

        this.c = theC & UINT32_MASK;
    }

    /**
     * Gets the D register value.
     *
     * @return the D register value
     */
    public long getD() {

        return this.d;
    }

    /**
     * Sets the D register value.
     *
     * @param theD the D register value
     */
    public void setD(final long theD) {

        this.d = theD & UINT32_MASK;
    }

    /**
     * Gets the X register value.
     *
     * @return the X register value
     */
    public long getX() {

        return this.x;
    }

    /**
     * Sets the X register value.
     *
     * @param theX the X register value
     */
    public void setX(final long theX) {

        this.x = theX & UINT32_MASK;
    }

    /**
     * Gets the AC register value.
     *
     * @return the AC register value
     */
    public long getAC() {

        return this.ac;
    }

    /**
     * Sets the AC register value.
     *
     * @param theAC the AC register value
     */
    public void setAC(final long theAC) {

        this.ac = theAC & UINT32_MASK;
    }

    /**
     * Gets a numbered register value.
     *
     * @param index the index
     * @return the register value
     */
    public long getReg(final int index) {

        return switch (index) {
            case 0 -> this.a;
            case 1 -> this.b;
            case 2 -> this.c;
            case 3 -> this.d;
            case 4 -> this.x;
            default -> this.ac;
        };
    }

    /**
     * Sets a numbered register value.
     *
     * @param index the index
     * @param value the register value
     */
    public void setReg(final int index, final long value) {

        switch (index) {
            case 0:
                this.a = value & UINT32_MASK;
                break;
            case 1:
                this.b = value & UINT32_MASK;
                break;
            case 2:
                this.c = value & UINT32_MASK;
                break;
            case 3:
                this.d = value & UINT32_MASK;
                break;
            case 4:
                this.x = value & UINT32_MASK;
                break;
            default:
                this.ac = value & UINT32_MASK;
                break;
        }
    }

    /**
     * Gets the S register value.
     *
     * @return the S register value
     */
    public int getS() {

        return this.s;
    }

    /**
     * Sets the S register value.
     *
     * @param theS the S register value
     */
    public void setS(final int theS) {

        this.s = theS & 0x00FF;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public int getMode() {

        return this.mode;
    }

    /**
     * Sets the mode.
     *
     * @param theMode the mode
     */
    public void setMode(final int theMode) {

        this.mode = theMode & 0x00FF;
    }
}
