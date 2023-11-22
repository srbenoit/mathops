package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.utilities.Breakpoint;

import java.util.Arrays;

/**
 * A memory context.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "memory_context" struct.
 */
public final class MemoryContext {

    /** The RAM memory. */
    private final Memory ramMemory;

    /** The Flash memory. */
    private final Memory flashMemory;

    /** Current flash commend. */
    private EnumFlashCommand step;

    /** Timer states to delay before allowing flash write. */
    private long flashWriteDelay;

    /** Whether flash is writable or not. */
    private boolean flashLocked;

    /** Last byte written to flash. */
    private int flashWriteByte;

    /** Whether there was an error programming the byte. */
    private boolean flashError;

    /** Flash toggles. */
    private int flashToggles;

    /** Correct bank state currently (either normal_banks or bootmap_banks). */
    private BankState[] banks;

    /** Current state of each bank. Fifth index is used to preserve the 4th in boot map */
    private final BankState[] normalBanks;

    /** Used to hold a backup of the banks when this is boot mapped. */
    private final BankState[] bootmapBanks;

    /** Special mapping used in boot that changes how paging works. */
    private boolean bootMapped;

    /** Check if boot code is still mapped to page 0 or not. */
    private boolean hasChangedPage0;

    /** Special for the 83p, used to determine which group of pages you are referring to. */
    private int protectedPageSet;

    /** Special for the 83p, used to determine which page of a set to protect. */
    private final int[] protectedPage;

    /** Protection mode: one of MODE0, MODE1, MODE2, MODE3 from CoreConstants. */
    private int protMode;

    /** Delays on SEs, typically they should be 0. */
    private int readOPFlashTStates;

    /** Delays on SEs, typically they should be 0. */
    private int readNOPFlashTStates;

    /** Delays on SEs, typically they should be 0. */
    private int writeFlashTStates;

    /** Delays on SEs, typically they should be 0. */
    private int readOPRamTStates;

    /** Delays on SEs, typically they should be 0. */
    private int readNOPRamTStates;

    /** Delays on SEs, typically they should be 0. */
    private int writeRamTStates;

    /** Stored value needed by port 06 device. */
    private int port06;

    /** Stored value needed by port 07 device. */
    private int port07;

    /** Stored value needed by port 0E device. */
    private int port0E;

    /** Stored value needed by port 0F device. */
    private int port0F;

    /** Stored value needed by port 24 device. */
    private int port24;

    /** Number of 64 byte chunks remapped from RAM page 0 to bank 3 (up to 18). */
    private final int[] port27RemapCount;

    /** Number of 64 byte chunks remapped from RAM page 1 to bank 2. */
    private final int[] port28RemapCount;

    /**
     * Constructs a new {@code MemoryContext}.
     */
    private MemoryContext() {

        super();

        this.ramMemory = new Memory(true);
        this.flashMemory = new Memory(false);

        this.normalBanks = new BankState[BankState.NUM_BANKS];
        this.bootmapBanks = new BankState[BankState.NUM_BANKS];

        for (int i = 0; i < BankState.NUM_BANKS; ++i) {
            this.normalBanks[i] = new BankState(null, 0, 0, false, false, false);
            this.bootmapBanks[i] = new BankState(null, 0, 0, false, false, false);
        }

        this.protectedPage = new int[4];
        this.port27RemapCount = new int[1];
        this.port28RemapCount = new int[1];

        clear();
    }

    /**
     * Constructs a new {@code MemoryContext} and adds it to the global list of all allocated memory contexts for
     * telemetric observation.
     *
     * @return the constructed {@code MemoryContext}
     */
    static MemoryContext createMemoryContext() {

        return new MemoryContext();
    }

    /**
     * clears the structure as if memset(0) were called.
     */
    public void clear() {

        this.ramMemory.clear();
        this.flashMemory.clear();

        this.step = EnumFlashCommand.FLASH_READ;
        this.flashWriteDelay = 0L;
        this.flashLocked = false;
        this.flashWriteByte = 0;
        this.flashError = false;
        this.flashToggles = 0;

        for (int i = 0; i < BankState.NUM_BANKS; ++i) {
            this.normalBanks[i].clear();
            this.bootmapBanks[i].clear();
        }
        this.banks = this.normalBanks;

        this.bootMapped = false;
        this.hasChangedPage0 = false;
        this.protectedPageSet = 0;
        Arrays.fill(this.protectedPage, 0);
        this.protMode = JWCoreConstants.MODE0;

        this.readOPFlashTStates = 0;
        this.readNOPFlashTStates = 0;
        this.writeFlashTStates = 0;
        this.readOPRamTStates = 0;
        this.readNOPRamTStates = 0;
        this.writeRamTStates = 0;

        this.port06 = 0;
        this.port07 = 0;
        this.port0E = 0;
        this.port0F = 0;
        this.port24 = 0;
        this.port27RemapCount[0] = 0;
        this.port28RemapCount[0] = 0;
    }

    /**
     * Gets the flash memory.
     *
     * @return the flash memory
     */
    public Memory getFlash() {

        return this.flashMemory;
    }

    /**
     * Gets the RAM memory.
     *
     * @return the RAM memory
     */
    public Memory getRam() {

        return this.ramMemory;
    }

    /**
     * Gets the flash programming step.
     *
     * @return the step
     */
    public EnumFlashCommand getStep() {

        return this.step;
    }

    /**
     * Sets the flash programming step.
     *
     * @param newStep the new step
     */
    public void setStep(final EnumFlashCommand newStep) {

        this.step = newStep;
    }

    /**
     * Gets the flash write delay.
     *
     * @return the delay
     */
    public long getFlashWriteDelay() {

        return this.flashWriteDelay;
    }

    /**
     * Sets the flash write delay.
     *
     * @param theDelay the delay
     */
    void setFlashWriteDelay(final long theDelay) {

        this.flashWriteDelay = theDelay;
    }

    /**
     * Tests whether flash is locked.
     *
     * @return {@code true} if flash is locked
     */
    public boolean isFlashLocked() {

        return this.flashLocked;
    }

    /**
     * Sets the flag that indicates whether flash is locked.
     *
     * @param locked {@code true} if flash is locked
     */
    public void setFlashLocked(final boolean locked) {

        this.flashLocked = locked;
    }

    /**
     * Gets the last byte written to flash.
     *
     * @return the byte
     */
    public int getFlashWriteByte() {

        return this.flashWriteByte;
    }

    /**
     * Tests whether there was an error programming the byte.
     *
     * @return {@code true} if there was an error
     */
    public boolean isFlashError() {

        return this.flashError;
    }

    /**
     * Sets the flag that indicates there was an error programming the byte.
     *
     * @param error {@code true} if there was an error
     */
    void setFlashError(final boolean error) {

        this.flashError = error;
    }

    /**
     * Gets the flash toggles.
     *
     * @return the toggles
     */
    public int getFlashToggles() {

        return this.flashToggles;
    }

    /**
     * Sets the flash toggles.
     *
     * @param theToggles the toggles
     */
    void setFlashToggles(final int theToggles) {

        this.flashToggles = theToggles & 0x00FF;
    }

    /**
     * Gets the currently active bank states.
     *
     * @return the current bank states (either normal or boot map banks)
     */
    public BankState[] getBanks() {

        return this.banks;
    }

    /**
     * Gets the normal bank states.
     *
     * @return the normal bank states
     */
    public BankState[] getNormalBanks() {

        return this.normalBanks;
    }

    /**
     * Gets the boot map bank states.
     *
     * @return the boot map bank states
     */
    public BankState[] getBootmapBanks() {

        return this.bootmapBanks;
    }

    /**
     * Makes the normal banks the active banks.
     */
    public void activateNormalBanks() {

        this.banks = this.normalBanks;
    }

    /**
     * Makes the boot map banks the active banks.
     */
    public void activateBootmapBanks() {

        this.banks = this.bootmapBanks;
    }

    /**
     * Tests whether memory is boot-mapped or not.
     *
     * @return true if boot mapped
     */
    public boolean isBootmapped() {

        return this.bootMapped;
    }

    /**
     * Sets the flag that indicates whether memory is boot-mapped or not.
     *
     * @param isBootmapped {@code true} if boot mapped; {@code false} if not
     */
    public void setBootmapped(final boolean isBootmapped) {

        this.bootMapped = isBootmapped;
    }

    /**
     * Tests whether page 0 has been changed or is still in boot mode.
     *
     * @return {@code true} of page 0 has been changed
     */
    public boolean isChangedPage0() {

        return this.hasChangedPage0;
    }

    /**
     * Sets the flag that indicates whether page 0 has been changed or is still in boot mode.
     *
     * @param changed {@code true} if page 0 has been changed
     */
    void setChangedPage0(final boolean changed) {

        this.hasChangedPage0 = changed;
    }

    /**
     * Gets the protected page set.
     *
     * @return the protected page set
     */
    public int getProtectedPageSet() {

        return this.protectedPageSet;
    }

    /**
     * Sets the protected page set.
     *
     * @param theSet the new protected page set
     */
    public void setProtectedPageSet(final int theSet) {

        this.protectedPageSet = theSet;
    }

    /**
     * Gets the protected page at a particular index.
     *
     * @param index the index
     * @return the protected page
     */
    public int getProtectedPage(final int index) {

        return this.protectedPage[index];
    }

    /**
     * Sets the protected page at a particular index.
     *
     * @param index the index
     * @param page  the protected page
     */
    public void setProtectedPage(final int index, final int page) {

        this.protectedPage[index] = page;
    }

    /**
     * Gets the protection mode.
     *
     * @return the protection mode
     */
    public int getProtMode() {

        return this.protMode;
    }

    /**
     * Sets the protection mode.
     *
     * @param theMode the new protection mode
     */
    public void setProtMode(final int theMode) {

        this.protMode = theMode;
    }

    /**
     * Gets the ticks consumed by a flash read op.
     *
     * @return the number of ticks.
     */
    public int getReadOPFlashTStates() {

        return this.readOPFlashTStates;
    }

    /**
     * Sets the ticks consumed by a flash read op.
     *
     * @param ticks the number of ticks.
     */
    public void setReadOPFlashTStates(final int ticks) {

        this.readOPFlashTStates = ticks;
    }

    /**
     * Gets the ticks consumed by a flash read nop.
     *
     * @return the number of ticks.
     */
    public int getReadNOPFlashTStates() {

        return this.readNOPFlashTStates;
    }

    /**
     * Sets the ticks consumed by a flash read nop.
     *
     * @param ticks the number of ticks.
     */
    public void setReadNOPFlashTStates(final int ticks) {

        this.readNOPFlashTStates = ticks;
    }

    /**
     * Gets the ticks consumed by a flash write.
     *
     * @return the number of ticks.
     */
    public int getWriteFlashTStates() {

        return this.writeFlashTStates;
    }

    /**
     * Sets the ticks consumed by a flash write.
     *
     * @param ticks the number of ticks.
     */
    public void setWriteFlashTStates(final int ticks) {

        this.writeFlashTStates = ticks;
    }

    /**
     * Gets the ticks consumed by a RAM read op.
     *
     * @return the number of ticks.
     */
    public int getReadOPRamTStates() {

        return this.readOPRamTStates;
    }

    /**
     * Sets the ticks consumed by a RAM read op.
     *
     * @param ticks the number of ticks.
     */
    public void setReadOPRamTStates(final int ticks) {

        this.readOPRamTStates = ticks;
    }

    /**
     * Gets the ticks consumed by a RAM read nop.
     *
     * @return the number of ticks.
     */
    public int getReadNOPRamTStates() {

        return this.readNOPRamTStates;
    }

    /**
     * Sets the ticks consumed by a RAM read nop.
     *
     * @param ticks the number of ticks.
     */
    public void setReadNOPRamTStates(final int ticks) {

        this.readNOPRamTStates = ticks;
    }

    /**
     * Gets the ticks consumed by a RAM write.
     *
     * @return the number of ticks.
     */
    public int getWriteRamTStates() {

        return this.writeRamTStates;
    }

    /**
     * Sets the ticks consumed by a RAM write.
     *
     * @param ticks the number of ticks.
     */
    public void setWriteRamTStates(final int ticks) {

        this.writeRamTStates = ticks;
    }

    /**
     * Gets the port 06 value.
     *
     * @return the port 06 value
     */
    public int getPort06() {

        return this.port06;
    }

    /**
     * Sets the port 06 value.
     *
     * @param theValue the port 06 value
     */
    public void setPort06(final int theValue) {

        this.port06 = theValue & 0x00FF;
    }

    /**
     * Gets the port 07 value.
     *
     * @return the port 07 value
     */
    public int getPort07() {

        return this.port07;
    }

    /**
     * Sets the port 07 value.
     *
     * @param theValue the port 07 value
     */
    public void setPort07(final int theValue) {

        this.port07 = theValue & 0x00FF;
    }

    /**
     * Gets the port 0E value.
     *
     * @return the port 0E value
     */
    public int getPort0E() {

        return this.port0E;
    }

    /**
     * Sets the port 0E value.
     *
     * @param theValue the port 0E value
     */
    public void setPort0E(final int theValue) {

        this.port0E = theValue & 0x00FF;
    }

    /**
     * Gets the port 0F value.
     *
     * @return the port 0F value
     */
    public int getPort0F() {

        return this.port0F;
    }

    /**
     * Sets the port 0F value.
     *
     * @param theValue the port 0F value
     */
    public void setPort0F(final int theValue) {

        this.port0F = theValue & 0x00FF;
    }

    /**
     * Gets the port 24 value.
     *
     * @return the port 24 value
     */
    public int getPort24() {

        return this.port24;
    }

    /**
     * Sets the port 24 value.
     *
     * @param theValue the port 24 value
     */
    public void setPort24(final int theValue) {

        this.port24 = theValue & 0x00FF;
    }

    /**
     * Gets the number of 64 byte chunks remapped from RAM page 0 to bank 3. This is an array of one integer that can be
     * passed to methods that need to update the value.
     *
     * @return the "number of chunks" array
     */
    public int[] getPort27RemapCount() {

        return this.port27RemapCount;
    }

    /**
     * Gets the number of 64 byte chunks remapped from RAM page 1 to bank 1. This is an array of one integer that can be
     * passed to methods that need to update the value.
     *
     * @return the "number of chunks" array
     */
    public int[] getPort28RemapCount() {

        return this.port28RemapCount;
    }

    /**
     * Reads a byte of memory. The CPU bus is not affected and memory breakpoints are not checked.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "mem_read" function.
     *
     * @param addr the address
     * @return the byte read
     */
    public int memRead(final int addr) {

        final int result;

        final int ushortAddr = addr & 0xFFFF;
        final int bank = BankState.mcBank(ushortAddr);
        final int base = BankState.mcBase(ushortAddr);

        if ((this.port27RemapCount[0] > 0) && !this.bootMapped && (bank == 3)
                && (ushortAddr >= (0x10000 - 64 * this.port27RemapCount[0])) && ushortAddr >= 0xFB64) {
            result = this.ramMemory.get(base);
        } else if ((this.port28RemapCount[0] > 0) && !this.bootMapped && (bank == 2)
                && (base < 64 * this.port28RemapCount[0])) {
            result = this.ramMemory.get(Memory.PAGE_SIZE + base);
        } else if (this.ramMemory.getVersion() == 2 && this.banks[bank].isRam()
                && this.banks[bank].getPage() > 2) {
            result = this.ramMemory.get(2 * Memory.PAGE_SIZE + base);
        } else {
            final BankState theBank = this.banks[bank];
            result = theBank.getMem().get(theBank.getAddr() + base);
        }

        return result;
    }

    /**
     * Reads a byte of memory using a "wide" unique address.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "wmem_read" function.
     *
     * @param waddr the address
     * @return the byte read
     */
    public int wmemRead(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;

        return mem.get(waddr.getPage() * Memory.PAGE_SIZE + (waddr.getAddr() % Memory.PAGE_SIZE));
    }

    /**
     * Reads a word of memory using a "wide" unique address.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "wmem_read16" function.
     *
     * @param waddr the address
     * @return the byte read
     */
    public int wmemRead16(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;

        return mem.get(waddr.getPage() * Memory.PAGE_SIZE + (waddr.getAddr() % Memory.PAGE_SIZE))
                + (mem.get(waddr.getPage() * Memory.PAGE_SIZE
                + ((waddr.getAddr() + 1) % Memory.PAGE_SIZE)) << 8);
    }

    /**
     * Writes a byte of memory.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "mem_write" function.
     *
     * @param addr the address
     * @param data the byte to write
     * @return the byte written
     */
    int memWrite(final int addr, final int data) {

        final int ushortAddr = addr & 0xFFFF;
        final int byteData = data & 0xFF;
        final int bank = BankState.mcBank(ushortAddr);
        final int base = BankState.mcBase(ushortAddr);

        if ((this.port27RemapCount[0] > 0) && !this.bootMapped && (bank == 3)
                && (ushortAddr >= (0x10000 - 64 * this.port27RemapCount[0])) && ushortAddr >= 0xFB64) {
            this.ramMemory.set(base, byteData);
        } else if ((this.port28RemapCount[0] > 0) && !this.bootMapped && (bank == 2)
                && (base < 64 * this.port28RemapCount[0])) {
            this.ramMemory.set(Memory.PAGE_SIZE + base, byteData);
        } else if (this.ramMemory.getVersion() == 2 && this.banks[bank].isRam()
                && this.banks[bank].getPage() > 2) {
            this.ramMemory.set(2 * Memory.PAGE_SIZE + base, byteData);
        } else {
            final BankState theBank = this.banks[bank];
            theBank.getMem().set(theBank.getAddr() + base, byteData);
        }

        return byteData;
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "flash_write_byte" function.
     *
     * @param addr the address
     * @param data the data
     */
    void flashWriteByte(final int addr, final int data) {

        final int ushortAddr = addr & 0x0000FFFF;
        final int byteData = data & 0x00FF;

        final int banknum = BankState.mcBank(ushortAddr);
        final BankState bank = this.banks[banknum];

        final Memory mem = bank.getMem();
        final int writeLocation = bank.getAddr() + BankState.mcBase(ushortAddr);

        mem.set(writeLocation, mem.get(writeLocation & byteData));
        this.flashWriteByte = byteData;

        if (mem.get(writeLocation) != byteData) {
            this.flashError = true;
        }

        this.step = EnumFlashCommand.FLASH_READ;
    }

    /**
     * Convert a Z80 address to a WideAddr.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "addr16_to_waddr" function.
     *
     * @param addr the address
     * @return the wide address
     */
    public WideAddr addr16ToWideAddr(final int addr) {

        final BankState bank = this.banks[BankState.mcBank(addr)];

        return new WideAddr(bank.getPage() & 0xFF, addr, bank.isRam());
    }

    /**
     * Convert a 32-bit address to a WideAddr.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "addr32_to_waddr" function.
     *
     * @param addr the address
     * @return the wide address
     */
    static WideAddr addr32ToWideAddr(final int addr) {

        return new WideAddr((addr / Memory.PAGE_SIZE) & 0xFF, (addr % Memory.PAGE_SIZE) & 0xFFFF, false);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "check_break" function.
     *
     * @param waddr the wide address
     * @return false if break
     */
    public boolean checkBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        return mem.isBreak(addr, JWCoreConstants.NORMAL_BREAK);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "check_mem_write_break" function.
     *
     * @param waddr the wide address
     * @return false if break
     */
    boolean checkMemWriteBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        return mem.isBreak(addr, JWCoreConstants.MEM_WRITE_BREAK);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "check_mem_read_break" function.
     *
     * @param waddr the wide address
     * @return false if break
     */
    boolean checkMemReadBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        return mem.isBreak(addr, JWCoreConstants.MEM_READ_BREAK);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "set_break" function.
     *
     * @param waddr the wide address
     */
    public void setBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.enableBreak(addr, JWCoreConstants.NORMAL_BREAK);
        addBreakpoint(JWCoreConstants.NORMAL_BREAK, waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "set_mem_write_break" function.
     *
     * @param waddr the wide address
     */
    public void setMemWriteBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.enableBreak(addr, JWCoreConstants.MEM_WRITE_BREAK);
        addBreakpoint(JWCoreConstants.MEM_WRITE_BREAK, waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "set_mem_read_break" function.
     *
     * @param waddr the wide address
     */
    public void setMemReadBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.enableBreak(addr, JWCoreConstants.MEM_READ_BREAK);
        addBreakpoint(JWCoreConstants.MEM_READ_BREAK, waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "clear_break" function.
     *
     * @param waddr the wide address
     */
    public void clearBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.disableBreak(addr, JWCoreConstants.NORMAL_BREAK);
        remBreakpoint(waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "clear_mem_write_break" function.
     *
     * @param waddr the wide address
     */
    public void clearMemWriteBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.disableBreak(addr, JWCoreConstants.MEM_WRITE_BREAK);
        remBreakpoint(waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "clear_mem_read_break" function.
     *
     * @param waddr the wide address
     */
    public void clearMemReadBreak(final WideAddr waddr) {

        final Memory mem = waddr.isRam() ? this.ramMemory : this.flashMemory;
        final int addr = Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr());

        mem.disableBreak(addr, JWCoreConstants.MEM_READ_BREAK);
        remBreakpoint(waddr);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "read2bytes" and "mem_read16" functions.
     *
     * @param addr the address
     * @return the value read
     */
    public int memRead16(final int addr) {

        return memRead(addr) + (memRead(addr + 1) << 8);
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "change_page" function.
     *
     * @param bank  the bank
     * @param page  the page
     * @param isRam true if ram
     */
    public void changePage(final int bank, final int page, final boolean isRam) {

        final int ucharPage = page & 0x00FF;
        final BankState theBank = this.normalBanks[bank];

        theBank.setRam(isRam);
        theBank.setPage(ucharPage);
        theBank.setAddr(page * Memory.PAGE_SIZE);

        if (isRam) {
            theBank.setMem(this.ramMemory);
            theBank.setReadOnly(false);
        } else {
            theBank.setMem(this.flashMemory);
            theBank.setReadOnly(ucharPage == this.flashMemory.getPages() - 1);
        }

        theBank.setNoExec(false);

        updateBootmapPages();
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "update_bootmap_pages" function.
     */
    public void updateBootmapPages() {

        this.bootmapBanks[0].setPage(this.normalBanks[0].getPage());
        this.bootmapBanks[0].setMem(this.normalBanks[0].getMem());
        this.bootmapBanks[0].setAddr(this.normalBanks[0].getAddr());
        this.bootmapBanks[0].setReadOnly(this.normalBanks[0].isReadOnly());
        this.bootmapBanks[0].setNoExec(this.normalBanks[0].isNoExec());
        this.bootmapBanks[0].setRam(this.normalBanks[0].isRam());

        this.bootmapBanks[1].setPage(this.normalBanks[1].getPage() & 0x00FE);
        this.bootmapBanks[1]
                .setMem(this.normalBanks[1].isRam() ? this.ramMemory : this.flashMemory);
        this.bootmapBanks[1].setAddr(this.bootmapBanks[1].getPage() * Memory.PAGE_SIZE);
        this.bootmapBanks[1].setReadOnly(false);
        this.bootmapBanks[1].setNoExec(false);
        this.bootmapBanks[1].setRam(this.normalBanks[1].isRam());

        this.bootmapBanks[2]
                .setPage(this.normalBanks[1].getPage() | (this.flashMemory.getVersion() == 1 ? 0 : 1));
        this.bootmapBanks[2]
                .setMem(this.normalBanks[1].isRam() ? this.ramMemory : this.flashMemory);
        this.bootmapBanks[2].setAddr(this.bootmapBanks[2].getPage() * Memory.PAGE_SIZE);
        this.bootmapBanks[2].setReadOnly(false);
        this.bootmapBanks[2].setNoExec(false);
        this.bootmapBanks[2].setRam(this.normalBanks[1].isRam());

        this.bootmapBanks[3].setPage(this.normalBanks[2].getPage());
        this.bootmapBanks[3]
                .setMem(this.normalBanks[2].isRam() ? this.ramMemory : this.flashMemory);
        this.bootmapBanks[3].setAddr(this.bootmapBanks[3].getPage() * Memory.PAGE_SIZE);
        this.bootmapBanks[3].setReadOnly(false);
        this.bootmapBanks[3].setNoExec(false);
        this.bootmapBanks[3].setRam(this.normalBanks[2].isRam());
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "endflash" function.
     */
    void endFlash() {

        if (this.step != EnumFlashCommand.FLASH_ERROR) {
            this.step = EnumFlashCommand.FLASH_READ;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/breakpoint.c: "add_breakpoint" function.
     *
     * @param type  the type
     * @param waddr the address
     */
    private void addBreakpoint(final int type, final WideAddr waddr) {

        final Breakpoint newBreak = new Breakpoint();

        newBreak.setActive(true);
        newBreak.setEndAddr(waddr.getAddr() & Memory.PAGE_SIZE);
        newBreak.setType(type);
        newBreak.setWaddr(
                new WideAddr(waddr.getPage(), waddr.getAddr() % Memory.PAGE_SIZE, waddr.isRam()));
        newBreak.setLabel(Integer.toHexString(waddr.getAddr()));

        if (waddr.isRam()) {
            this.ramMemory.setCondBreak(Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr()),
                    newBreak);
        } else {
            this.flashMemory.setCondBreak(Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr()),
                    newBreak);
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/breakpoint.c: "rem_breakpoint" function.
     *
     * @param waddr the address
     */
    private void remBreakpoint(final WideAddr waddr) {

        if (waddr.isRam()) {
            this.ramMemory.setCondBreak(Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr()), null);
        } else {
            this.flashMemory.setCondBreak(Memory.PAGE_SIZE * waddr.getPage() + BankState.mcBase(waddr.getAddr()), null);
        }
    }

    /**
     * Gets the breakpoint currently configured at an address.
     *
     * <p>
     * WABBITEMU SOURCE: utilities/breakpoint.c: "rem_breakpoint" function.
     *
     * @param waddr the address
     * @return the type
     */
    public int getBreakpoint(final WideAddr waddr) {

        final int result;

        if (waddr.isRam()) {
            result = this.ramMemory.getBreak(waddr.getAddr());
        } else {
            result = this.flashMemory.getBreak(waddr.getAddr());
        }

        return result;
    }
}
