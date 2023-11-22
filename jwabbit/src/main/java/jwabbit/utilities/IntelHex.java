package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.log.LoggedObject;

/**
 * WABBIEMU SOURCE: utilities/var.h, "INTELHEX" struct.
 *
 * <p>
 * The WABBITEMU code also defines an "intelhex_t" structure in hardware/link.h. We do not implement that separately;
 * just use this one.
 */
public final class IntelHex {

    /** The data size. */
    private int dataSize;

    /** The address. */
    private int address;

    /** The type. */
    private int type;

    /** The data. */
    private final int[] data = new int[256];

    /**
     * Constructs a new {@code IntelHex}.
     */
    public IntelHex() {

        super();
    }

    /**
     * Gets the data size.
     *
     * @return the data size
     */
    public int getDataSize() {

        return this.dataSize;
    }

    /**
     * Sets the data size.
     *
     * @param theDataSize the data size
     */
    void setDataSize(final int theDataSize) {

        if (theDataSize < 0 || theDataSize > 0x00FF) {
            LoggedObject.LOG.warning("Invalid INTELHEX data size: " + theDataSize, new IllegalArgumentException());
        }

        this.dataSize = theDataSize & 0x00FF;
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

        if (theAddress < 0 || theAddress > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid INTELHEX address: " + theAddress, new IllegalArgumentException());
        }

        this.address = theAddress & 0x0000FFFF;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public int getType() {

        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param theType the type
     */
    public void setType(final int theType) {

        if (theType < 0 || theType > 5) {
            LoggedObject.LOG.warning("Invalid INTELHEX type: " + theType, new IllegalArgumentException());
        }

        this.type = theType & 0x00FF;
    }

    /**
     * Sets the checksum.
     *
     * @param theChecksum the checksum
     */
    static void setCheckSum(final int theChecksum) {

        if (theChecksum < 0 || theChecksum > 0x00FF) {
            LoggedObject.LOG.warning("Invalid INTELHEX checksum: " + theChecksum, new IllegalArgumentException());
        }
    }

    /**
     * Gets the data array.
     *
     * @return the data array
     */
    public int[] getData() {

        return this.data;
    }
}
