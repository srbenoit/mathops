package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.WideAddr;

/**
 * WABBITEMU SOURCE: debugger/disassemble.h, "Z80_info" struct.
 */
public final class Z80Info {

    /** The index. */
    private int index;

    /** First object used in formatting instruction. */
    private Object a1;

    /** Second object used in formatting instruction. */
    private Object a2;

    /** Third object used in formatting instruction. */
    private Object a3;

    /** Fourth object used in formatting instruction. */
    private Object a4;

    /** Size of command. */
    private int size;

    /** Rhe opcode data (length is size). */
    private int[] opcodeData;

    /** Number of clocks. */
    private int clocks;

    /** Wide address. */
    private WideAddr waddr;

    /** Breakpoint. */
    private int bp;

    /** The expanded command, such as "jp $0B63". */
    private String expanded;

    /**
     * Constructs a new {@code Z80Info}.
     */
    public Z80Info() {

        this.waddr = null;
        this.opcodeData = null;
    }

    /**
     * Sets the index.
     *
     * @param theIndex the index
     */
    public void setIndex(final int theIndex) {

        this.index = theIndex;
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {

        return this.index;
    }

    /**
     * Gets the wide address.
     *
     * @return the wide address
     */
    WideAddr getWaddr() {

        return this.waddr;
    }

    /**
     * Sets the wide address.
     *
     * @param theAddr the wide address
     */
    void setWideAddr(final WideAddr theAddr) {

        this.waddr = theAddr;
    }

    /**
     * Sets the size.
     *
     * @param theSize the size
     */
    public void setSize(final int theSize) {

        this.size = theSize;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {

        return this.size;
    }

    /**
     * Sets the opcode data.
     *
     * @param theData the opcode data
     */
    void setOpcodeData(final int[] theData) {

        this.opcodeData = theData;
    }

    /**
     * Gets the opcode data.
     *
     * @return the opcode data
     */
    int[] getOpcodeData() {

        return this.opcodeData;
    }

    /**
     * Sets the number of clocks.
     *
     * @param theClocks the number of clocks
     */
    void setClocks(final int theClocks) {

        this.clocks = theClocks;
    }

    /**
     * Gets the number of clocks.
     *
     * @return the number of clocks
     */
    int getClocks() {

        return this.clocks;
    }

    /**
     * Sets the expanded command.
     *
     * @param theExpanded the expanded command
     */
    void setExpanded(final String theExpanded) {

        this.expanded = theExpanded;
    }

    /**
     * Gets the expanded command.
     *
     * @return the expanded command
     */
    public String getExpanded() {

        return this.expanded;
    }

    /**
     * Sets the first argument for the printed instruction.
     *
     * @param theObj the first argument
     */
    public void setA1(final Object theObj) {

        this.a1 = theObj;
    }

    /**
     * Gets the first argument for the printed instruction.
     *
     * @return the first argument
     */
    public Object getA1() {

        return this.a1;
    }

    /**
     * Sets the second argument for the printed instruction.
     *
     * @param theObj the second argument
     */
    public void setA2(final Object theObj) {

        this.a2 = theObj;
    }

    /**
     * Gets the second argument for the printed instruction.
     *
     * @return the second argument
     */
    public Object getA2() {

        return this.a2;
    }

    /**
     * Sets the third argument for the printed instruction.
     *
     * @param theObj the third argument
     */
    public void setA3(final Object theObj) {

        this.a3 = theObj;
    }

    /**
     * Gets the third argument for the printed instruction.
     *
     * @return the third argument
     */
    public Object getA3() {

        return this.a3;
    }

    /**
     * Sets the fourth argument for the printed instruction.
     *
     * @param theObj the fourth argument
     */
    void setA4(final Object theObj) {

        this.a4 = theObj;
    }

    /**
     * Gets the fourth argument for the printed instruction.
     *
     * @return the fourth argument
     */
    Object getA4() {

        return this.a4;
    }

    /**
     * Sets a particular argument.
     *
     * @param which  the index of the argument
     * @param theObj the argument
     */
    void setA(final int which, final Object theObj) {

        if (which == 0) {
            this.a1 = theObj;
        } else if (which == 1) {
            this.a2 = theObj;
        } else if (which == 2) {
            this.a3 = theObj;
        } else {
            this.a4 = theObj;
        }
    }

    /**
     * Gets the breakpoint associated with this instruction's address.
     *
     * @return the breakpoint
     */
    public int getBreakpoint() {

        return this.bp;
    }

    /**
     * Sets the breakpoint associated with this instruction's address.
     *
     * @param theBreakpoint the breakpoint
     */
    public void setBreakpoint(final int theBreakpoint) {

        this.bp = theBreakpoint;
    }
}
