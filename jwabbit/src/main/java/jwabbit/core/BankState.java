package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * The state of a bank unit for a partition.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "bank_state" struct.
 */
public final class BankState {

    /** WABBITEMU SOURCE: core/core.h: "NUM_BANKS" macro. */
    static final int NUM_BANKS = 5;

    /** WABBITEMU SOURCE: core/core.h: "MC_BANK_MASK" macro. */
    private static final int MC_BANK_MASK = 0xC000;

    /** WABBITEMU SOURCE: core/core.h: "MC_BASE_MASK" macro. */
    private static final int MC_BASE_MASK = ~MC_BANK_MASK;

    /** The memory the address refers to. */
    private Memory memory;

    /** The address within the memory buffer (already paged). */
    private int addr;

    /** The current 16KB page. */
    private int page;

    /** True if you can not write to this page (even if the flash is unlocked). */
    private boolean readOnly;

    /** True if this is on the ram chip(also effect write method for flash). */
    private boolean ram;

    /** True if you can not execute on this page. */
    private boolean noExec;

    /**
     * Constructs a new {@code BankState}.
     *
     * @param theMemory  the memory the address refers to
     * @param theAddr    the address within the memory buffer (already paged)
     * @param thePage    the current 16KB page
     * @param isReadOnly true if you can not write to this page (even if the flash is unlocked)
     * @param isRam      true if this is on the ram chip(also effect write method for flash)
     * @param isNoExec   true if you can not execute on this page
     */
    BankState(final Memory theMemory, final int theAddr, final int thePage,
              final boolean isReadOnly, final boolean isRam, final boolean isNoExec) {

        this.memory = theMemory;
        this.addr = theAddr;
        this.page = thePage;
        this.readOnly = isReadOnly;
        this.ram = isRam;
        this.noExec = isNoExec;
    }

    /**
     * Clears the bank state.
     */
    void clear() {

        this.memory = null;
        this.addr = 0;
        this.page = 0;
        this.readOnly = false;
        this.ram = false;
        this.noExec = false;
    }

    /**
     * Sets all values of a {@code BankState}.
     *
     * @param theMemory  the memory the address refers to
     * @param theAddr    the address within the memory buffer (already paged)
     * @param thePage    the current 16KB page
     * @param isReadOnly true if you can not write to this page (even if the flash is unlocked)
     * @param isRam      true if this is on the ram chip(also effect write method for flash)
     * @param isNoExec   true if you can not execute on this page
     */
    public void set(final Memory theMemory, final int theAddr, final int thePage,
                    final boolean isReadOnly, final boolean isRam, final boolean isNoExec) {

        this.memory = theMemory;
        this.addr = theAddr;
        this.page = thePage;
        this.readOnly = isReadOnly;
        this.ram = isRam;
        this.noExec = isNoExec;
    }

    /**
     * Gets the memory.
     *
     * @return the memory
     */
    public Memory getMem() {

        return this.memory;
    }

    /**
     * Sets the memory.
     *
     * @param theMem the memory
     */
    public void setMem(final Memory theMem) {

        this.memory = theMem;
    }

    /**
     * Sets the address.
     *
     * @param theAddr the address
     */
    public void setAddr(final int theAddr) {

        this.addr = theAddr;
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
     * Sets the page.
     *
     * @param thePage the page
     */
    public void setPage(final int thePage) {

        this.page = thePage;
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
     * Sets the flag that indicates the bank is read-only.
     *
     * @param isReadOnly true if you can not write to this page (even if the flash is unlocked)
     */
    public void setReadOnly(final boolean isReadOnly) {

        this.readOnly = isReadOnly;
    }

    /**
     * Tests whether the bank is read-only.
     *
     * @return true if you can not write to this page (even if the flash is unlocked)
     */
    public boolean isReadOnly() {

        return this.readOnly;
    }

    /**
     * Sets the flag that determines whether the bank is in RAM.
     *
     * @param isRam true if this is on the ram chip(also effect write method for flash)
     */
    public void setRam(final boolean isRam) {

        this.ram = isRam;
    }

    /**
     * Tests whether the bank is in RAM.
     *
     * @return true if this is on the ram chip(also effect write method for flash)
     */
    public boolean isRam() {

        return this.ram;
    }

    /**
     * Sets the flag that indicates whether the bank does not allow execution.
     *
     * @param isNoExec true if you can not execute on this page
     */
    public void setNoExec(final boolean isNoExec) {

        this.noExec = isNoExec;
    }

    /**
     * Tests whether the bank does not allow execution.
     *
     * @return true if you can not execute on this page
     */
    public boolean isNoExec() {

        return this.noExec;
    }

    /**
     * Extracts the bank from an address.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "mc_bank" macro.
     *
     * @param addr the address
     * @return the bank number (address divided by 16384)
     */
    public static int mcBank(final int addr) {

        return addr >> 14;
    }

    /**
     * Extracts the address within its bank from an address.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.h: "mc_base" macro.
     *
     * @param addr the address
     * @return the base address within its bank
     */
    public static int mcBase(final int addr) {

        return addr & MC_BASE_MASK;
    }

}
