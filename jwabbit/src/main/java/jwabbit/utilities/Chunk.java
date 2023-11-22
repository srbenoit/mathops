package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.util.Arrays;

/**
 * WABBITEMU SOURCE: utilities/savestate.h, "CHUNK_t" struct.
 */
final class Chunk {

    /** The tag. */
    private final char[] tag = new char[4];

    /** The pnt. */
    private int pnt;

    /** The size. */
    private int size;

    /** The data. */
    private int[] data;

    /**
     * Constructs a new {@code Chunk}.
     */
    Chunk() {

        super();
    }

    /**
     * Gets the tag.
     *
     * @return the character
     */
    public char[] getTag() {

        return this.tag;
    }

    /**
     * Gets the pnt.
     *
     * @return the pnt
     */
    public int getPnt() {

        return this.pnt;
    }

    /**
     * Sets the pnt.
     *
     * @param thePnt the pnt
     */
    void setPnt(final int thePnt) {

        this.pnt = thePnt;
    }

    /**
     * Gets the chunk size.
     *
     * @return the size
     */
    public int getSize() {

        return this.size;
    }

    /**
     * Sets the chunk size.
     *
     * @param theSize the size
     */
    public void setSize(final int theSize) {

        this.size = theSize;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public int[] getData() {

        return this.data;
    }

    /**
     * Sets the data.
     *
     * @param theData the data
     */
    public void setData(final int[] theData) {

        this.data = theData;
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "CheckPNT" function.
     */
    private void checkPNT() {

        if (this.size < this.pnt) {
            throw new IllegalStateException();
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadChar" function.
     *
     * @return the data value
     */
    byte readChar() {

        if (this.data == null) {
            return (byte) 0;
        }

        final int value = this.data[this.pnt];
        ++this.pnt;
        checkPNT();

        return (byte) value;
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadShort" function.
     *
     * @return the data value
     */
    int readShort() {

        if (this.data == null) {
            return 0;
        }

        int value = this.data[this.pnt];
        ++this.pnt;
        value += this.data[this.pnt] << 8;
        ++this.pnt;

        checkPNT();

        return value;
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadInt" function.
     *
     * @return the data value
     */
    int readInt() {

        if (this.data == null) {
            return 0;
        }

        int value = this.data[this.pnt];
        ++this.pnt;
        value += this.data[this.pnt] << 8;
        ++this.pnt;
        value += this.data[this.pnt] << 16;
        ++this.pnt;
        value += this.data[this.pnt] << 24;
        ++this.pnt;

        checkPNT();

        return value;
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadLong" function.
     *
     * @return the data value
     */
    long readLong() {

        if (this.data == null) {
            return 0L;
        }

        long value = (long) this.data[this.pnt];
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 8;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 16;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 24;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 32;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 40;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 48;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 56;
        ++this.pnt;

        checkPNT();

        return value;
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadDouble" function.
     *
     * @return the data value
     */
    double readDouble() {

        if (this.data == null) {
            return 0.0;
        }

        long value = (long) this.data[this.pnt];
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 8;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 16;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 24;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 32;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 40;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 48;
        ++this.pnt;
        value += ((long) this.data[this.pnt]) << 56;
        ++this.pnt;

        checkPNT();

        return Double.longBitsToDouble(value);
    }

    /**
     * WABBITEMU SOURCE: hardware/savestate.cpp, "ReadBlock" function.
     *
     * @param theData   the array into which to read data
     * @param theLength the length of data to read
     */
    void readBlock(final int[] theData, final int theLength) {

        if (this.data == null) {
            Arrays.fill(theData, 0);
            return;
        }

        if (Math.min(theLength, this.size) >= 0) {
            System.arraycopy(this.data, this.pnt, theData, 0, Math.min(theLength, this.size));
        }
        this.pnt += theLength;
        checkPNT();
    }
}
