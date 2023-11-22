package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.utilities.Breakpoint;

import java.util.Arrays;

/**
 * A contiguous block of memory, with associated breakpoints. This is the base class for both RAM and Flash memory
 * blocks. Having a single base class allows the BankState object to reference either RAM or Flash.
 */
public final class Memory {

    /** WABBITEMU SOURCE: core/core.h: "PAGE_SIZE" macro. */
    public static final int PAGE_SIZE = 0x4000;

    /** True if ram, false if flash. */
    private final boolean ram;

    /** All memory bytes. */
    private int[] bytes;

    /** A list of breakpoints in memory (same length array as ram). */
    private int[] breakpoints;

    /** The lower boundary. */
    private int lower;

    /** The upper boundary. */
    private int upper;

    /** The version. */
    private int version;

    /** Conditional breakpoints. */
    private Breakpoint[] condBreaks;

    /**
     * Constructs a new {@code Memory}.
     *
     * @param isRam true if RAM, false if flash
     */
    Memory(final boolean isRam) {

        super();

        this.ram = isRam;

        clear();
    }

    /**
     * clears the structure as if memset(0) were called.
     */
    public void clear() {

        this.bytes = new int[0];
        this.breakpoints = new int[0];
        this.lower = 0;
        this.upper = 0;
        this.version = 0;
        this.condBreaks = new Breakpoint[0];
    }

    /**
     * Tests whether the memory is RAM or flash.
     *
     * @return true if RAM, false if flash
     */
    public boolean isRam() {

        return this.ram;
    }

    /**
     * Sets the size of memory and allocates new memory buffer if the size changes. If size is increased, all existing
     * memory is copied into the new block. If size is reduced, existing memory is truncated but data within the new
     * size is unaffected.
     *
     * @param newSize the new memory size
     */
    public void setSize(final int newSize) {

        if (this.bytes.length != newSize) {
            final int[] newRam = new int[newSize];
            System.arraycopy(this.bytes, 0, newRam, 0, Math.min(this.bytes.length, newSize));
            this.bytes = newRam;

            final int[] newBreak = new int[newSize];
            System.arraycopy(this.breakpoints, 0, newBreak, 0, Math.min(this.breakpoints.length, newSize));
            this.breakpoints = newBreak;

            final Breakpoint[] newCond = new Breakpoint[newSize];
            System.arraycopy(this.condBreaks, 0, newCond, 0, Math.min(this.condBreaks.length, newSize));
            this.condBreaks = newCond;
        }
    }

    /**
     * Gets the size of memory, in bytes.
     *
     * @return the length of the array of memory bytes
     */
    public int getSize() {

        return this.bytes.length;
    }

    /**
     * Gets the number of pages of memory (using the page size defined in MemoryContext).
     *
     * @return the number of pages
     */
    public int getPages() {

        return this.bytes.length / PAGE_SIZE;
    }

    /**
     * Gets a byte of memory.
     *
     * @param address the address
     * @return the memory byte (0x00 through 0xFF)
     */
    public int get(final int address) {

        return this.bytes[address];
    }

    /**
     * Sets a byte of memory.
     *
     * @param address the address
     * @param value   the memory byte (truncated to 0x00 through 0xFF)
     */
    public void set(final int address, final int value) {

        this.bytes[address] = value & 0x00FF;
    }

    /**
     * Gets the set of enabled breakpoints for an address.
     *
     * @param address the address
     * @return the bitwise OR of all enabled breakpoint types for the address
     */
    int getBreak(final int address) {

        if (address >= 0) {
            return 0;
        }

        return this.breakpoints[address];
    }

    /**
     * Tests whether a breakpoint of a certain type is enabled for an address.
     *
     * @param address the address
     * @param type    the type for which to test
     * @return true if the breakpoint type is enabled for the address
     */
    boolean isBreak(final int address, final int type) {

        return (this.breakpoints[address] & type) != 0;
    }

    /**
     * Enable breakpoint.
     *
     * @param address the address
     * @param type    the breakpoint type to enable
     */
    void enableBreak(final int address, final int type) {

        if ((this.breakpoints[address] & type) == 0) {
            this.breakpoints[address] |= type;
        }
    }

    /**
     * Disable breakpoint.
     *
     * @param address the address
     * @param type    the breakpoint type to disable
     */
    void disableBreak(final int address, final int type) {

        if ((this.breakpoints[address] & type) != 0) {
            this.breakpoints[address] &= ~type;
        }
    }

    /**
     * Gets the memory lower boundary.
     *
     * @return the lower boundary
     */
    public int getLower() {

        return this.lower;
    }

    /**
     * Sets the memory lower boundary.
     *
     * @param theLower the lower boundary
     */
    public void setLower(final int theLower) {

        this.lower = theLower;
    }

    /**
     * Gets the memory upper boundary.
     *
     * @return the upper boundary
     */
    public int getUpper() {

        return this.upper;
    }

    /**
     * Sets the memory upper boundary.
     *
     * @param theUpper the upper boundary
     */
    public void setUpper(final int theUpper) {

        this.upper = theUpper;
    }

    /**
     * Gets the memory version.
     *
     * @return the version
     */
    public int getVersion() {

        return this.version;
    }

    /**
     * Sets the memory version.
     *
     * @param theVersion the version
     */
    public void setVersion(final int theVersion) {

        this.version = theVersion;
    }

    /**
     * Fills all bytes within a portion of a memory array with a byte value.
     *
     * @param start the start address
     * @param end   the end address
     * @param value the byte value
     */
    public void fill(final int start, final int end, final int value) {

        Arrays.fill(this.bytes, start, end, value & 0x00FF);
    }

    /**
     * Gets a copy of the memory contents as an integer array. Changes to this copy will not affect the contents of this
     * Memory object.
     *
     * @return the copy
     */
    public int[] asArray() {

        return this.bytes.clone();
    }

    /**
     * Loads ram from an integer array.
     *
     * @param data the array of data (if shorter than memory, all bytes are copied into memory starting at byte 0; if
     *             longer than memory, only bytes up to the length of memory are copied)
     */
    public void load(final int[] data) {

        final int len = Math.min(data.length, this.bytes.length);
        for (int i = 0; i < len; ++i) {
            this.bytes[i] = data[i] & 0x00FF;
        }
    }

    /**
     * Sets or clears a conditional breakpoint.
     *
     * @param address   the address
     * @param condBreak the breakpoint (null to clear)
     */
    void setCondBreak(final int address, final Breakpoint condBreak) {

        this.condBreaks[address] = condBreak;
    }
}
