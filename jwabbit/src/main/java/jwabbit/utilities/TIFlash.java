package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: utilities/var.h, "TIFLASH" struct.
 */
public final class TIFlash {

    /** WABBITEMU SOURCE: utilities/var.h, "TI_FLASH_HEADER_SIZE" macro. */
    static final int TI_FLASH_HEADER_SIZE = 8 + 2 + 1 + 1 + 4 + 1 + 8 + 23 + 1 + 1 + 24 + 4;

    /** WABBITEMU SOURCE: utilities/var.h, "FLASH_TYPE_OS" macro. */
    static final int FLASH_TYPE_OS = 0x23;

    /** 8-byte signature. */
    private final int[] sig = new int[8];

    /** 2-byte revision. */
    private final int[] rev = new int[2];

    /** 1 byte flag. */
    private int flag;

    /** 1 byte object. */
    private int object;

    /** 4 byte date. */
    private final int[] date = new int[4];

    /** 8 byte name. */
    private final int[] name = new int[8];

    /** 23 bytes of filler. */
    private final int[] filler = new int[23];

    /** 1 byte device. */
    private int device;

    /** 1 byte type. */
    private int type;

    /** 24 bytes of filler. */
    private final int[] filler2 = new int[24];

    /** 4 byte hex size. */
    private int hexsize;

    /** 256 bytes of page sizes. */
    private final int[] pagesize = new int[256];

    /** 256 data buffers. */
    private final int[][] data = new int[256][];

    /** Total number of pages. */
    private int pages;

    /**
     * Constructs a new {@code TIFLASH}.
     */
    TIFlash() {

        super();
    }

    /**
     * Sets a byte within the first 78 bytes of the structure.
     *
     * @param offset the offset
     * @param value  the byte value
     */
    void setByte(final int offset, final int value) {

        if (value < 0 || value > 255) {
            LoggedObject.LOG.warning("Invalid byte (" + value + ") being set in TIFLASH offset " + offset,
                    new IllegalArgumentException());
        }

        if (offset < 8) {
            this.sig[offset] = value;
        } else if (offset < 10) {
            this.rev[offset - 8] = value;
        } else if (offset < 11) {
            setFlag(value);
        } else if (offset < 12) {
            setObject(value);
        } else if (offset < 16) {
            this.date[offset - 12] = value;
        } else if (offset < 17) {
            setNamelength(value);
        } else if (offset < 25) {
            this.name[offset - 17] = value;
        } else if (offset < 48) {
            this.filler[offset - 25] = value;
        } else if (offset < 49) {
            setDevice(value);
        } else if (offset < 50) {
            setType(value);
        } else if (offset < 74) {
            this.filler2[offset - 50] = value;
        } else if (offset < 75) {
            setHexsize((this.hexsize & 0xFFFFFF00) | (value & 0x00FF));
        } else if (offset < 76) {
            setHexsize((this.hexsize & 0xFFFF00FF) | ((value & 0x00FF) << 8));
        } else if (offset < 77) {
            setHexsize((this.hexsize & 0xFF00FFFF) | ((value & 0x00FF) << 16));
        } else if (offset < 78) {
            setHexsize((this.hexsize & 0x00FFFFFF) | ((value & 0x00FF) << 24));
        }
    }

    /**
     * Gets the flag.
     *
     * @return the flag
     */
    public int getFlag() {

        return this.flag;
    }

    /**
     * Sets the flag.
     *
     * @param theFlag the flag
     */
    private void setFlag(final int theFlag) {

        if (theFlag < 0 || theFlag > 255) {
            LoggedObject.LOG.warning("Invalid flag value in TIFLASH " + theFlag, new IllegalArgumentException());
        }

        this.flag = theFlag;
    }

    /**
     * Gets the object.
     *
     * @return the object
     */
    public int getObject() {

        return this.object;
    }

    /**
     * Sets the object.
     *
     * @param theObject the object
     */
    private void setObject(final int theObject) {

        if (theObject < 0 || theObject > 255) {
            LoggedObject.LOG.warning("Invalid object value in TIFLASH " + theObject, new IllegalArgumentException());
        }

        this.object = theObject;
    }

    /**
     * Sets the name length.
     *
     * @param theNameLength the name length
     */
    private static void setNamelength(final int theNameLength) {

        if (theNameLength < 0 || theNameLength > 8) {
            LoggedObject.LOG.warning("Invalid name length value in TIFLASH " + theNameLength,
                    new IllegalArgumentException());
        }
    }

    /**
     * Gets the device.
     *
     * @return the device
     */
    public int getDevice() {

        return this.device;
    }

    /**
     * Sets the device.
     *
     * @param theDevice the device
     */
    private void setDevice(final int theDevice) {

        if (theDevice < 0 || theDevice > 8) {
            LoggedObject.LOG.warning("Invalid device value in TIFLASH " + theDevice, new IllegalArgumentException());
        }

        this.device = theDevice;
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
    private void setType(final int theType) {

        if (theType < 0x23 || theType > 0x24) {
            LoggedObject.LOG.warning("Invalid type value in TIFLASH " + theType, new IllegalArgumentException());
        }

        this.type = theType;
    }

    /**
     * Sets the hex size.
     *
     * @param theHexsize the hex size
     */
    private void setHexsize(final int theHexsize) {

        if (theHexsize < 0) {
            LoggedObject.LOG.warning("Invalid hex size value in TIFLASH " + theHexsize, new IllegalArgumentException());
        }

        this.hexsize = theHexsize;
    }

    /**
     * Gets the total pages.
     *
     * @return the total pages
     */
    public int getPages() {

        return this.pages;
    }

    /**
     * Sets the total pages.
     *
     * @param thePages the total pages
     */
    public void setPages(final int thePages) {

        if (thePages < 0) {
            LoggedObject.LOG.warning("Invalid total pages value in TIFLASH " + thePages,
                    new IllegalArgumentException());
        }

        this.pages = thePages;
    }

    /**
     * Gets the 8-byte array with signature.
     *
     * @return the array
     */
    public int[] getSig() {

        return this.sig;
    }

    /**
     * Gets the 4-byte array with date.
     *
     * @return the array
     */
    public int[] getDate() {

        return this.date;
    }

    /**
     * Gets the 8-byte array with name.
     *
     * @return the array
     */
    public int[] getName() {

        return this.name;
    }

    /**
     * Gets the 23-byte array with filler data.
     *
     * @return the array
     */
    public int[] getFiller() {

        return this.filler;
    }

    /**
     * Gets the array of integer page sizes.
     *
     * @return the array
     */
    int[] getPagesize() {

        return this.pagesize;
    }

    /**
     * Gets the array of arrays of page data.
     *
     * @return the array
     */
    public int[][] getData() {

        return this.data;
    }
}
