package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * SOURCE: interface/core.h, "label_struct" struct.
 */
public final class Label {

    /** The label name. */
    private String name;

    /** True if a RAM location. */
    private boolean ram;

    /** Unsigned 8-bit page. */
    private int page;

    /** Unsigned 16-bit address. */
    private int addr;

    /**
     * Constructs a new {@code Label}.
     */
    public Label() {

        // No action
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {

        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param theName the name
     */
    public void setName(final String theName) {

        this.name = theName;
    }

    /**
     * Tests whether label is in RAM.
     *
     * @return true if in RAM
     */
    public boolean isRam() {

        return this.ram;
    }

    /**
     * Sets the flag indicating label is in RAM.
     *
     * @param isRam true if in RAM
     */
    public void setRam(final boolean isRam) {

        this.ram = isRam;
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
     * Sets the page.
     *
     * @param thePage the page
     */
    public void setPage(final int thePage) {

        this.page = thePage & 0x00FF;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public int getAddr() {

        return this.addr;
    }

    /**
     * Sets the address.
     *
     * @param theAddr the address
     */
    public void setAddr(final int theAddr) {

        this.addr = theAddr & 0x0000FFFF;
    }
}
