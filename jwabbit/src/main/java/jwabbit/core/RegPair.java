package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A 16-bit register which can be accessed as a 16-bit value, or as two 8-bit values (the high-order byte and the
 * low-order byte).
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "regpair" macro.
 */
final class RegPair {

    /** The value (only low-order 16 bits may be nonzero). */
    private int value;

    /**
     * Constructs a new {@code RegPair}.
     */
    RegPair() {

        // No action
    }

    /**
     * Gets the full 16-bit value as an unsigned value (0 to 65535).
     *
     * @return the 16-bit value
     */
    int get() {

        return this.value;
    }

    /**
     * Sets the full 16-bit value (same call for storing signed and unsigned values).
     *
     * @param theValue the new 16-bit value
     */
    void set(final int theValue) {

        this.value = theValue & 0x0000FFFF;
    }

    /**
     * Gets the low-order byte as an unsigned value (0 to 255).
     *
     * @return the low-order byte
     */
    public int getLo() {

        return this.value & 0x00FF;
    }

    /**
     * Sets the low-order byte value (same call for storing signed and unsigned values).
     *
     * @param theValue the new 8-bit value
     */
    public void setLo(final int theValue) {

        this.value = (this.value & 0x0000FF00) | (theValue & 0x000000FF);
    }

    /**
     * Gets the high-order byte as an unsigned value (0 to 255).
     *
     * @return the high-order byte
     */
    public int getHi() {

        return (this.value >> 8) & 0x00FF;
    }

    /**
     * Sets the high-order byte value (same call for storing signed and unsigned values).
     *
     * @param theValue the new 8-bit value
     */
    public void setHi(final int theValue) {

        this.value = (this.value & 0x000000FF) | ((theValue << 8) & 0x0000FF00);
    }
}
