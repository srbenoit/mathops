package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: interface/state.h, "symbol83P_t" struct.
 */
final class Symbol83p {

    /** The type ID. */
    private int typeID;

    /** The version. */
    private int version;

    /** The address. */
    private int address;

    /** The page. */
    private int page;

    /** The length. */
    private int length;

    /** The name. */
    private String name;

    /**
     * Constructs a new {@code Symbol83p}.
     */
    Symbol83p() {

        // No action
    }

    /**
     * Gets the type ID.
     *
     * @return the type ID
     */
    public int getTypeID() {

        return this.typeID;
    }

    /**
     * Sets the type ID.
     *
     * @param theTypeID the type ID
     */
    void setTypeID(final int theTypeID) {

        this.typeID = theTypeID & 0x00FF;
    }

    /**
     * Sets the second type ID.
     *
     * @param theTypeID2 the second type ID
     */
    public void setTypeID2(final int theTypeID2) {

        // No action
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public int getVersion() {

        return this.version;
    }

    /**
     * Sets the version.
     *
     * @param theVersion the version
     */
    public void setVersion(final int theVersion) {

        this.version = theVersion & 0x00FF;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public int getAddress() {

        return this.address;
    }

    /**
     * Sets the address.
     *
     * @param theAddress the address
     */
    public void setAddress(final int theAddress) {

        this.address = theAddress & 0x0000FFFF;
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
     * Gets the length.
     *
     * @return the length
     */
    public int getLength() {

        return this.length;
    }

    /**
     * Sets the length.
     *
     * @param theLength the length
     */
    public void setLength(final int theLength) {

        this.length = theLength & 0x00FF;
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
}
