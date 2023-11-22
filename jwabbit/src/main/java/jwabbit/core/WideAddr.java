package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An immutable wide address. All the information required to address a byte of memory.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "waddr" struct.
 */
public final class WideAddr {

    /** True if the address lies in RAM. */
    private final boolean ram;

    /** The page. */
    private final int page;

    /** The address within the page. */
    private final int addr;

    /**
     * Constructs a new {@code WideAddr}.
     *
     * @param thePage the page
     * @param theAddr the address
     * @param isRam   true if the address is in RAM; false if in flash
     */
    public WideAddr(final int thePage, final int theAddr, final boolean isRam) {

        this.ram = isRam;
        this.page = thePage & 0x00FF;
        this.addr = theAddr & 0x0000FFFF;
    }

    /**
     * Constructs a new {@code WideAddr} by copying values from another.
     *
     * @param src the source to copy
     */
    public WideAddr(final WideAddr src) {

        this.ram = src.ram;
        this.page = src.page;
        this.addr = src.addr;
    }

    /**
     * Tests whether this address is in RAM.
     *
     * @return true if in RAM, false if in flash
     */
    public boolean isRam() {

        return this.ram;
    }

    /**
     * Gets the page.
     *
     * @return the page
     */
    public int getPage() {

        return this.page;
    }

    /**
     * Gets the address within the page.
     *
     * @return the address
     */
    public int getAddr() {

        return this.addr;
    }
}
