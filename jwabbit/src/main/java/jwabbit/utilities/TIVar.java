package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.hardware.IByteArray;
import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: utilities/var.h, "TIVAR" struct.
 */
public final class TIVar implements IByteArray {

    /** 2-byte size of the header up to name, sometimes ignored. */
    private int headersize;

    /** 2-byte data size. */
    private int length;

    /** 1 byte type of variable. */
    private int vartype;

    /** 1 byte (85/86 only) name length is variable. */
    private int nameLength;

    /** 8-byte null-padded name. */
    private final int[] name = new int[8];

    /** 1 byte version, 0 83+only. */
    private int version;

    /** 1 byte flags, bit 7 is if flash 83+only. */
    private int flag;

    /** 2 byte duplicate of data size. */
    private int length2;

    /** Pointer to byte data. */
    private int[] data;

    /**
     * Constructs a new {@code TIVar}.
     */
    TIVar() {

        super();
    }

    /**
     * Gets the byte representation.
     *
     * @return the bytes
     */
    @Override
    public int[] getBytes() {

        final int[] bytes = new int[18 + this.length];

        bytes[0] = this.headersize & 0x00FF;
        bytes[1] = (this.headersize >> 8) & 0x00FF;

        bytes[2] = this.length & 0x00FF;
        bytes[3] = (this.length >> 8) & 0x00FF;

        bytes[4] = this.vartype & 0x00FF;

        bytes[5] = this.nameLength & 0x00FF;

        System.arraycopy(this.name, 0, bytes, 6, 8);

        bytes[14] = this.version & 0x00FF;

        bytes[15] = this.flag & 0x00FF;

        bytes[16] = this.length2 & 0x00FF;
        bytes[17] = (this.length2 >> 8) & 0x00FF;

        System.arraycopy(this.data, 0, bytes, 18, this.length);

        return bytes;
    }

    /**
     * Sets the header size.
     *
     * @param theHeadersize the header size
     */
    void setHeadersize(final int theHeadersize) {

        if (theHeadersize < 0 || theHeadersize > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid header size in TIVAR: " + theHeadersize, new IllegalArgumentException());
        }

        this.headersize = theHeadersize;
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

        if (theLength < 0 || theLength > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length in TIVAR: " + theLength, new IllegalArgumentException());
        }

        this.length = theLength;
    }

    /**
     * Sets the variable type.
     *
     * @param theVartype the variable type
     */
    void setVartype(final int theVartype) {

        if (theVartype < 0 || theVartype > 0x00FF) {
            LoggedObject.LOG.warning("Invalid variable type in TIVAR: " + theVartype, new IllegalArgumentException());
        }

        this.vartype = theVartype;
    }

    /**
     * Sets the name length.
     *
     * @param theNameLength the name length
     */
    void setNameLength(final int theNameLength) {

        if (theNameLength < 0 || theNameLength > 8) {
            LoggedObject.LOG.warning("Invalid name length in TIVAR: " + theNameLength, new IllegalArgumentException());
        }

        this.nameLength = theNameLength;
    }

    /**
     * Gets the name byte array.
     *
     * @return the array
     */
    public int[] getName() {

        return this.name;
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

        if (theVersion < 0 || theVersion > 255) {
            LoggedObject.LOG.warning("Invalid version in TIVAR: " + theVersion, new IllegalArgumentException());
        }

        this.version = theVersion;
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
    public void setFlag(final int theFlag) {

        if (theFlag < 0 || theFlag > 255) {
            LoggedObject.LOG.warning("Invalid flag in TIVAR: " + theFlag, new IllegalArgumentException());
        }

        this.flag = theFlag;
    }

    /**
     * Sets the length copy.
     *
     * @param theLength2 the length copy
     */
    void setLength2(final int theLength2) {

        if (theLength2 < 0 || theLength2 > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length copy in TIVAR: " + theLength2, new IllegalArgumentException());
        } else if (theLength2 != this.length) {
            LoggedObject.LOG.warning("Mismatched length copy in TIVAR: " + theLength2, new IllegalArgumentException());
        }

        this.length2 = theLength2;
    }

    /**
     * Gets the data array.
     *
     * @return the array
     */
    public int[] getData() {

        return this.data;
    }

    /**
     * Sets the data array.
     *
     * @param theData the data array
     */
    public void setData(final int[] theData) {

        this.data = theData;
    }
}
