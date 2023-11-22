package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: utilities/var.h, "TIBACKUP" struct.
 */
final class TIBackup {

    /** 2-byte data size. */
    private int length1;

    /** 2-byte data size. */
    private int length2;

    /** 2-byte data size. */
    private int length3;

    /** 2-byte duplicate of data size. */
    private int address;

    /** 2-byte repeat of the data length. */
    private int length1a;

    /** pointer to byte data. */
    private int[] data1;

    /** 2-byte data size. */
    private int length2a;

    /** pointer to byte data. */
    private int[] data2;

    /** 2-byte data size. */
    private int length3a;

    /** Pointer to byte data. */
    private int[] data3;

    /**
     * Constructs a new {@code TIBackup}.
     */
    TIBackup() {

        super();
    }

    /**
     * Sets the header size.
     *
     * @param theHeaderSize the header size
     */
    static void setHeadersize(final int theHeaderSize) {

        if (theHeaderSize < 0 || theHeaderSize > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid header size in TIBACKUP: " + theHeaderSize,
                    new IllegalArgumentException());
        }
    }

    /**
     * Gets the length of data 1.
     *
     * @return the length of data 1
     */
    int getLength1() {

        return this.length1;
    }

    /**
     * Sets the length of data 1.
     *
     * @param theLength1 the length of data 1
     */
    void setLength1(final int theLength1) {

        if (theLength1 < 0 || theLength1 > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 1 in TIBACKUP: " + theLength1, new IllegalArgumentException());
        }

        this.length1 = theLength1;
    }

    /**
     * Sets the variable type.
     *
     * @param theVarType the variable type
     */
    static void setVartype(final int theVarType) {

        if (theVarType < 0 || theVarType > 0x00FF) {
            LoggedObject.LOG.warning("Invalid variable type in TIBACKUP: " + theVarType,
                    new IllegalArgumentException());
        }
    }

    /**
     * Gets the length of data 2.
     *
     * @return the length of data 2
     */
    int getLength2() {

        return this.length2;
    }

    /**
     * Sets the length of data 2.
     *
     * @param theLength2 the length of data 2
     */
    void setLength2(final int theLength2) {

        if (theLength2 < 0 || theLength2 > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 2 in TIBACKUP: " + theLength2, new IllegalArgumentException());
        }

        this.length2 = theLength2;
    }

    /**
     * Gets the length of data 3.
     *
     * @return the length of data 3
     */
    int getLength3() {

        return this.length3;
    }

    /**
     * Sets the length of data 3.
     *
     * @param theLength3 the length of data 3
     */
    void setLength3(final int theLength3) {

        if (theLength3 < 0 || theLength3 > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 3 in TIBACKUP: " + theLength3, new IllegalArgumentException());
        }

        this.length3 = theLength3;
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
            LoggedObject.LOG.warning("Invalid address in TIBACKUP: " + theAddress, new IllegalArgumentException());
        }

        this.address = theAddress;
    }

    /**
     * Gets the copy of length of data 1.
     *
     * @return the copy of length of data 1
     */
    int getLength1a() {

        return this.length1a;
    }

    /**
     * Sets the copy of length of data 1.
     *
     * @param theLength1a the copy of length of data 1
     */
    void setLength1a(final int theLength1a) {

        if (theLength1a < 0 || theLength1a > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 1a in TIBACKUP: " + theLength1a, new IllegalArgumentException());
        } else if (theLength1a != this.length1) {
            LoggedObject.LOG.warning("Mismatched length 1a in TIBACKUP: " + theLength1a,
                    new IllegalArgumentException());
        }

        this.length1a = theLength1a;
    }

    /**
     * Gets the data byte array 1.
     *
     * @return the array
     */
    int[] getData1() {

        return this.data1;
    }

    /**
     * Sets the data byte array 1.
     *
     * @param theData1 the array
     */
    void setData1(final int[] theData1) {

        this.data1 = theData1;
    }

    /**
     * Gets the copy of length of data 2.
     *
     * @return the copy of length of data 2
     */
    int getLength2a() {

        return this.length2a;
    }

    /**
     * Sets the copy of length of data 2.
     *
     * @param theLength2a the copy of length of data 2
     */
    void setLength2a(final int theLength2a) {

        if (theLength2a < 0 || theLength2a > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 2a in TIBACKUP: " + theLength2a, new IllegalArgumentException());
        } else if (theLength2a != this.length2) {
            LoggedObject.LOG.warning("Mismatched length 2a in TIBACKUP: " + theLength2a,
                    new IllegalArgumentException());
        }

        this.length2a = theLength2a;
    }

    /**
     * Gets the data byte array 2.
     *
     * @return the array
     */
    int[] getData2() {

        return this.data2;
    }

    /**
     * Sets the data byte array 2.
     *
     * @param theData2 the array
     */
    void setData2(final int[] theData2) {

        this.data2 = theData2;
    }

    /**
     * Gets the copy of length of data 3.
     *
     * @return the copy of length of data 3
     */
    int getLength3a() {

        return this.length3a;
    }

    /**
     * Sets the copy of length of data 3.
     *
     * @param theLength3a the copy of length of data 3
     */
    void setLength3a(final int theLength3a) {

        if (theLength3a < 0 || theLength3a > 0x0000FFFF) {
            LoggedObject.LOG.warning("Invalid length 3a in TIBACKUP: " + theLength3a, new IllegalArgumentException());
        } else if (theLength3a != this.length2) {
            LoggedObject.LOG.warning("Mismatched length 3a in TIBACKUP: " + theLength3a,
                    new IllegalArgumentException());
        }

        this.length3a = theLength3a;
    }

    /**
     * Gets the data byte array 3.
     *
     * @return the array
     */
    int[] getData3() {

        return this.data3;
    }

    /**
     * Sets the data byte array 3.
     *
     * @param theData3 the array
     */
    void setData3(final int[] theData3) {

        this.data3 = theData3;
    }
}
