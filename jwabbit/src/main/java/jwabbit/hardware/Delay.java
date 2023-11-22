package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "DELAY" struct.
 */
public final class Delay {

    /** LCD 1. */
    private int lcd1;

    /** LCD 1. */
    private int lcd2;

    /** LCD 1. */
    private int lcd3;

    /** LCD 1. */
    private int lcd4;

    /** Unknown. */
    private int unknown;

    /** Memory access delay. */
    private int mad;

    /** LCD wait. */
    private int lcdwait;

    /**
     * Constructs a new {@code Delay}.
     */
    public Delay() {

        // No action
    }

    /**
     * Gets the LCD1 register value.
     *
     * @return the register value
     */
    public int getLcd1() {

        return this.lcd1;
    }

    /**
     * Gets the LCD2 register value.
     *
     * @return the register value
     */
    public int getLcd2() {

        return this.lcd2;
    }

    /**
     * Gets the LCD3 register value.
     *
     * @return the register value
     */
    public int getLcd3() {

        return this.lcd3;
    }

    /**
     * Gets the LCD4 register value.
     *
     * @return the register value
     */
    public int getLcd4() {

        return this.lcd4;
    }

    /**
     * Gets the unknown register value.
     *
     * @return the register value
     */
    public int getUnknown() {

        return this.unknown;
    }

    /**
     * Sets the unknown register value.
     *
     * @param theValue the register value
     */
    public void setUnknown(final int theValue) {

        this.unknown = theValue & 0x00FF;
    }

    /**
     * Gets the MAD register value.
     *
     * @return the register value
     */
    public int getMad() {

        return this.mad;
    }

    /**
     * Gets the LCD wait register value.
     *
     * @return the register value
     */
    public int getLcdWait() {

        return this.lcdwait;
    }

    /**
     * Gets a numbered register value.
     *
     * @param index the index
     * @return the register value
     */
    public int getReg(final int index) {

        return switch (index) {
            case 0 -> this.lcd1;
            case 1 -> this.lcd2;
            case 2 -> this.lcd3;
            case 3 -> this.lcd4;
            case 4 -> this.unknown;
            case 5 -> this.mad;
            default -> this.lcdwait;
        };
    }

    /**
     * Sets a numbered register value.
     *
     * @param index the index
     * @param value the register value
     */
    public void setReg(final int index, final int value) {

        switch (index) {
            case 0:
                this.lcd1 = value & 0x00FF;
                break;
            case 1:
                this.lcd2 = value & 0x00FF;
                break;
            case 2:
                this.lcd3 = value & 0x00FF;
                break;
            case 3:
                this.lcd4 = value & 0x00FF;
                break;
            case 4:
                this.unknown = value & 0x00FF;
                break;
            case 5:
                this.mad = value & 0x00FF;
                break;
            default:
                this.lcdwait = value & 0x00FF;
                break;
        }
    }
}
