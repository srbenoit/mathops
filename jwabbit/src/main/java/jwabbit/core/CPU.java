package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.hardware.Link;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.AUDIO;

/**
 * A Z-80 CPU and associated support hardware.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "CPU" struct.
 */
public final class CPU {

    /** WABBITEMU SOURCE: core/alu.h, "ADD_INSTR" macro. */
    public static final int ADD_INSTR = 0;

    /** WABBITEMU SOURCE: core/alu.h, "SUB_INSTR" macro. */
    public static final int SUB_INSTR = JWCoreConstants.N_MASK;

    /** The Calc that owns this context. */
    private final Calc owningCalc;

    /** The a and f registers. */
    private final RegPair af;

    /** The b and c registers. */
    private final RegPair bc;

    /** The d and e registers. */
    private final RegPair de;

    /** The hl register. */
    private final RegPair hl;

    /** The a' and f' registers. */
    private final RegPair afp;

    /** The b' and c' registers. */
    private final RegPair bcp;

    /** The d' and e' registers. */
    private final RegPair dep;

    /** The hl' register. */
    private final RegPair hlp;

    /** The ix register. */
    private final RegPair ix;

    /** The iy register. */
    private final RegPair iy;

    /** The pc (program counter) register. */
    private int pc;

    /** The sp (stack pointer) register. */
    private int sp;

    /** I register. */
    private int iReg;

    /** R register. */
    private int rReg;

    /** Bus data. */
    private int bus;

    /** Link write data. */
    private int linkWrite;

    /** Interrupt mode. */
    private int imode;

    /** Interrupt flag. */
    private boolean interrupt;

    /** Ei_block flag. */
    private boolean eiBlock;

    /** Iff1 flag. */
    private boolean iff1;

    /** Iff2 flag. */
    private boolean iff2;

    /** Halt flag. */
    private boolean halt;

    /** Read flag. */
    private boolean read;

    /** Write flag. */
    private boolean write;

    /** Output flag. */
    private boolean output;

    /** Input flag. */
    private boolean input;

    /** Prefix. */
    private int prefix;

    /** PIO context. */
    private final PIOContext pio;

    /** Memory context context. */
    private final MemoryContext mem;

    /** Timer context. */
    private final TimerContext timer;

    /** CPU version. */
    private int cpuVersion;

    /** Model bits. */
    private int modelBits;

    /** Previous instructions for time reversal. */
    private final ReverseTime[] prevInstructionList;

    /** The previous instruction in the previous instruction list. */
    private ReverseTime prevInstruction;

    /** The index of the reverse instruction. */
    private int reverseInstr;

    /** True if last instruction was a link instruction. */
    private boolean linkInstruction;

    /** Last time when linked. */
    private long linkingTime;

    /** Last time enter was pressed. */
    private long hasHitEnter;

    /** The profiler. */
    private final Profiler profiler;

    /** The execution violation callback. */
    private ICpuCallback exeViolationCallback;

    /** The invalid flash callback. */
    private ICpuCallback invalidFlashCallback;

    /** The memory read break callback. */
    private ICpuCallback memReadBreakCallback;

    /** The memory write break callback. */
    private ICpuCallback memWriteBreakCallback;

    /** The LCD enqueue callback. */
    private ICpuCallback lcdEnqueueCallback;

    /** Flag indicating the last opcode executed was a return that actually returned. */
    private boolean returned;

    /**
     * Constructs a new {@code CPU}.
     *
     * @param theOwningCalc the owning calculator
     */
    private CPU(final Calc theOwningCalc) {

        super();

        this.owningCalc = theOwningCalc;

        this.af = new RegPair();
        this.bc = new RegPair();
        this.de = new RegPair();

        this.hl = new RegPair();
        this.afp = new RegPair();
        this.bcp = new RegPair();
        this.dep = new RegPair();
        this.hlp = new RegPair();
        this.ix = new RegPair();
        this.iy = new RegPair();

        this.mem = MemoryContext.createMemoryContext();
        this.timer = TimerContext.createTimerContext();

        this.pio = new PIOContext();
        final int count = 512;
        this.prevInstructionList = new ReverseTime[count];
        for (int i = 0; i < count; ++i) {
            this.prevInstructionList[i] = new ReverseTime();
        }
        this.prevInstruction = null;
        this.profiler = new Profiler();

        this.exeViolationCallback = null;
        this.invalidFlashCallback = null;
        this.memReadBreakCallback = null;
        this.memWriteBreakCallback = null;
        this.lcdEnqueueCallback = null;
    }

    /**
     * Sets the flag that indicates the most recent opcode was a return (that actually returned).
     *
     * @param isReturned true if returned, false if not
     */
    public void setReturned(final boolean isReturned) {

        this.returned = isReturned;
    }

    /**
     * Gets the flag that indicates the most recent opcode was a return (that actually returned).
     *
     * @return true if returned, false if not
     */
    public boolean isReturned() {

        return this.returned;
    }

    /**
     * Constructs a new {@code CPU}.
     *
     * @param theOwningCalc the owning calculator
     * @return the constructed {@code CPU}
     */
    public static CPU createCPU(final Calc theOwningCalc) {

        return new CPU(theOwningCalc);
    }

    /**
     * Sets all members to zero as if "memset(0)" was called on a structure.
     */
    private void clear() {

        this.af.set(0);
        this.bc.set(0);
        this.de.set(0);
        this.hl.set(0);

        this.afp.set(0);
        this.bcp.set(0);
        this.dep.set(0);
        this.hlp.set(0);

        this.ix.set(0);
        this.iy.set(0);

        this.pc = 0;
        this.sp = 0;
        this.iReg = 0;
        this.rReg = 0;
        this.bus = 0;
        this.linkWrite = 0;
        this.imode = 0;

        this.interrupt = false;
        this.eiBlock = false;
        this.iff1 = false;
        this.iff2 = false;
        this.halt = false;
        this.read = false;
        this.write = false;
        this.output = false;
        this.input = false;

        this.prefix = 0;
        this.pio.clear();

        this.mem.clear();
        this.timer.clear();

        this.cpuVersion = 0;
        this.modelBits = 0;

        for (final ReverseTime reverseTime : this.prevInstructionList) {
            reverseTime.clear();
        }
        this.prevInstruction = null;
        this.reverseInstr = 0;

        this.linkInstruction = false;
        this.linkingTime = 0L;
        this.hasHitEnter = 0L;
        this.profiler.clear();

        this.exeViolationCallback = null;
        this.invalidFlashCallback = null;
        this.memReadBreakCallback = null;
        this.memWriteBreakCallback = null;
        this.lcdEnqueueCallback = null;

        this.returned = false;
    }

    /**
     * Gets the {@code Calc} that owns this timer context.
     *
     * @return the owning {@code Calc}
     */
    Calc getOwningCalc() {

        return this.owningCalc;
    }

    /**
     * Gets the value of the AF register.
     *
     * @return the AF register value
     */
    public int getAF() {

        return this.af.get();
    }

    /**
     * Sets the value of the AF register.
     *
     * @param theAF the AF register value
     */
    public void setAF(final int theAF) {

        this.af.set(theAF);
    }

    /**
     * Gets the value of the A register.
     *
     * @return the A register value
     */
    public int getA() {

        return this.af.getHi();
    }

    /**
     * Sets the value of the A register.
     *
     * @param theA the A register value
     */
    public void setA(final int theA) {

        this.af.setHi(theA);
    }

    /**
     * Adds a value to the A register.
     *
     * @param delta the value to add
     */
    public void addA(final int delta) {

        this.af.setHi(this.af.getHi() + delta);
    }

    /**
     * Gets the value of the flags register.
     *
     * @return the flags register value
     */
    public int getF() {

        return this.af.getLo();
    }

    /**
     * Sets the value of the flags register.
     *
     * @param theF the flags register value
     */
    public void setF(final int theF) {

        this.af.setLo(theF);
    }

    /**
     * Gets the value of the BC register.
     *
     * @return the BC register value
     */
    public int getBC() {

        return this.bc.get();
    }

    /**
     * Sets the value of the BC register.
     *
     * @param theBC the BC register value
     */
    public void setBC(final int theBC) {

        this.bc.set(theBC);
    }

    /**
     * Adds a value to the BC register.
     *
     * @param delta the value to add
     */
    public void addBC(final int delta) {

        this.bc.set(this.bc.get() + delta);
    }

    /**
     * Gets the value of the B register.
     *
     * @return the B register value
     */
    public int getB() {

        return this.bc.getHi();
    }

    /**
     * Sets the value of the B register.
     *
     * @param theB the B register value
     */
    public void setB(final int theB) {

        this.bc.setHi(theB);
    }

    /**
     * Adds the value to the B register.
     *
     * @param delta the value to add
     */
    public void addB(final int delta) {

        this.bc.setHi(this.bc.getHi() + delta);
    }

    /**
     * Gets the value of the C register.
     *
     * @return the C register value
     */
    public int getC() {

        return this.bc.getLo();
    }

    /**
     * Sets the value of the C register.
     *
     * @param theC the C register value
     */
    public void setC(final int theC) {

        this.bc.setLo(theC);
    }

    /**
     * Adds a value to the C register.
     *
     * @param delta the value to add
     */
    public void addC(final int delta) {

        this.bc.setLo(this.bc.getLo() + delta);
    }

    /**
     * Gets the value of the DE register.
     *
     * @return the DE register value
     */
    public int getDE() {

        return this.de.get();
    }

    /**
     * Sets the value of the DE register.
     *
     * @param theDE the DE register value
     */
    public void setDE(final int theDE) {

        this.de.set(theDE);
    }

    /**
     * Adds a value to the DE register.
     *
     * @param delta the value to add
     */
    public void addDE(final int delta) {

        this.de.set(this.de.get() + delta);
    }

    /**
     * Gets the value of the D register.
     *
     * @return the D register value
     */
    public int getD() {

        return this.de.getHi();
    }

    /**
     * Sets the value of the D register.
     *
     * @param theD the D register value
     */
    public void setD(final int theD) {

        this.de.setHi(theD);
    }

    /**
     * Adds a value to the D register.
     *
     * @param delta the value to add
     */
    public void addD(final int delta) {

        this.de.setHi(this.de.getHi() + delta);
    }

    /**
     * Gets the value of the E register.
     *
     * @return the E register value
     */
    public int getE() {

        return this.de.getLo();
    }

    /**
     * Sets the value of the E register.
     *
     * @param theE the E register value
     */
    public void setE(final int theE) {

        this.de.setLo(theE);
    }

    /**
     * Adds a value to the E register.
     *
     * @param delta the value to add
     */
    public void addE(final int delta) {

        this.de.setLo(this.de.getLo() + delta);
    }

    /**
     * Gets the value of the HL register.
     *
     * @return the HL register value
     */
    public int getHL() {

        return this.hl.get();
    }

    /**
     * Sets the value of the HL register.
     *
     * @param theHL the HL register value
     */
    public void setHL(final int theHL) {

        this.hl.set(theHL);
    }

    /**
     * Adds a value to the HL register.
     *
     * @param delta the value to add
     */
    public void addHL(final int delta) {

        this.hl.set(this.hl.get() + delta);
    }

    /**
     * Gets the value of the H register.
     *
     * @return the H register value
     */
    public int getH() {

        return this.hl.getHi();
    }

    /**
     * Sets the value of the H register.
     *
     * @param theH the H register value
     */
    public void setH(final int theH) {

        this.hl.setHi(theH);
    }

    /**
     * Adds a value to the H register.
     *
     * @param delta the value to add
     */
    public void addH(final int delta) {

        this.hl.setHi(this.hl.getHi() + delta);
    }

    /**
     * Gets the value of the L register.
     *
     * @return the L register value
     */
    public int getL() {

        return this.hl.getLo();
    }

    /**
     * Sets the value of the L register.
     *
     * @param theL the L register value
     */
    public void setL(final int theL) {

        this.hl.setLo(theL);
    }

    /**
     * Adds a value to the L register.
     *
     * @param delta the value to add
     */
    public void addL(final int delta) {

        this.hl.setLo(this.hl.getLo() + delta);
    }

    /**
     * Gets the value of the AF prime register.
     *
     * @return the AF prime register value
     */
    public int getAFprime() {

        return this.afp.get();
    }

    /**
     * Sets the value of the AF prime register.
     *
     * @param theAFPrime the AF prime register value
     */
    public void setAFprime(final int theAFPrime) {

        this.afp.set(theAFPrime);
    }

    /**
     * Sets the value of the A prime register.
     *
     * @param theAPrime the A prime register value
     */
    public void setAprime(final int theAPrime) {

        this.afp.setHi(theAPrime);
    }

    /**
     * Sets the value of the F prime register.
     *
     * @param theFPrime the F prime register value
     */
    public void setFprime(final int theFPrime) {

        this.afp.setLo(theFPrime);
    }

    /**
     * Gets the value of the BC prime register.
     *
     * @return the BC prime register value
     */
    public int getBCprime() {

        return this.bcp.get();
    }

    /**
     * Sets the value of the BC prime register.
     *
     * @param theBCPrime the BC prime register value
     */
    public void setBCprime(final int theBCPrime) {

        this.bcp.set(theBCPrime);
    }

    /**
     * Sets the value of the B prime register.
     *
     * @param theBPrime the B prime register value
     */
    public void setBprime(final int theBPrime) {

        this.bcp.setHi(theBPrime);
    }

    /**
     * Sets the value of the C prime register.
     *
     * @param theCPrime the C prime register value
     */
    public void setCprime(final int theCPrime) {

        this.bcp.setLo(theCPrime);
    }

    /**
     * Gets the value of the DE prime register.
     *
     * @return the DE prime register value
     */
    public int getDEprime() {

        return this.dep.get();
    }

    /**
     * Sets the value of the DE prime register.
     *
     * @param theDEPrime the DE prime register value
     */
    public void setDEprime(final int theDEPrime) {

        this.dep.set(theDEPrime);
    }

    /**
     * Sets the value of the D prime register.
     *
     * @param theDPrime the D prime register value
     */
    public void setDprime(final int theDPrime) {

        this.dep.setHi(theDPrime);
    }

    /**
     * Sets the value of the E prime register.
     *
     * @param theEPrime the E prime register value
     */
    public void setEprime(final int theEPrime) {

        this.dep.setLo(theEPrime);
    }

    /**
     * Gets the value of the HL prime register.
     *
     * @return the HL prime register value
     */
    public int getHLprime() {

        return this.hlp.get();
    }

    /**
     * Sets the value of the HL prime register.
     *
     * @param theHLPrime the HL prime register value
     */
    public void setHLprime(final int theHLPrime) {

        this.hlp.set(theHLPrime);
    }

    /**
     * Sets the value of the H prime register.
     *
     * @param theHPrime the H prime register value
     */
    public void setHprime(final int theHPrime) {

        this.hlp.setHi(theHPrime);
    }

    /**
     * Sets the value of the L prime register.
     *
     * @param theLPrime the L prime register value
     */
    public void setLprime(final int theLPrime) {

        this.hlp.setLo(theLPrime);
    }

    /**
     * Gets the value of the IX register.
     *
     * @return the IX register value
     */
    public int getIX() {

        return this.ix.get();
    }

    /**
     * Sets the value of the IX register.
     *
     * @param theIx the IX register value
     */
    public void setIX(final int theIx) {

        this.ix.set(theIx);
    }

    /**
     * Adds a value to the IX register.
     *
     * @param delta the value to add
     */
    public void addIX(final int delta) {

        this.ix.set(this.ix.get() + delta);
    }

    /**
     * Gets the value of the IXL register.
     *
     * @return the IXL register value
     */
    public int getIXL() {

        return this.ix.getLo();
    }

    /**
     * Sets the value of the IXL register.
     *
     * @param theIxl the IXL register value
     */
    public void setIXL(final int theIxl) {

        this.ix.setLo(theIxl);
    }

    /**
     * Adds a value to the IXL register.
     *
     * @param delta the value to add
     */
    public void addIXL(final int delta) {

        this.ix.setLo(this.ix.getLo() + delta);
    }

    /**
     * Gets the value of the IXH register.
     *
     * @return the IXH register value
     */
    public int getIXH() {

        return this.ix.getHi();
    }

    /**
     * Sets the value of the IXH register.
     *
     * @param theIxh the IXH register value
     */
    public void setIXH(final int theIxh) {

        this.ix.setHi(theIxh);
    }

    /**
     * Adds a value to the IXH register.
     *
     * @param delta the value to add
     */
    public void addIXH(final int delta) {

        this.ix.setHi(this.ix.getHi() + delta);
    }

    /**
     * Gets the value of the IY register.
     *
     * @return the IY register value
     */
    public int getIY() {

        return this.iy.get();
    }

    /**
     * Sets the value of the IY register.
     *
     * @param theIy the IY register value
     */
    public void setIY(final int theIy) {

        this.iy.set(theIy);
    }

    /**
     * Adds a value to the IY register.
     *
     * @param delta the value to add
     */
    public void addIY(final int delta) {

        this.iy.set(this.iy.get() + delta);
    }

    /**
     * Gets the value of the IYL register.
     *
     * @return the IYL register value
     */
    public int getIYL() {

        return this.iy.getLo();
    }

    /**
     * Sets the value of the IYL register.
     *
     * @param theIyl the IYL register value
     */
    public void setIYL(final int theIyl) {

        this.iy.setLo(theIyl);
    }

    /**
     * Adds a value to the IYL register.
     *
     * @param delta the value to add
     */
    public void addIYL(final int delta) {

        this.iy.setLo(this.iy.getLo() + delta);
    }

    /**
     * Gets the value of the IYH register.
     *
     * @return the IYH register value
     */
    public int getIYH() {

        return this.iy.getHi();
    }

    /**
     * Sets the value of the IYH register.
     *
     * @param theIyh the IYH register value
     */
    public void setIYH(final int theIyh) {

        this.iy.setHi(theIyh);
    }

    /**
     * Adds a value to the IYH register.
     *
     * @param delta the value to add
     */
    public void addIYH(final int delta) {

        this.iy.setHi(this.iy.getHi() + delta);
    }

    /**
     * Gets the value of the PC register.
     *
     * @return the PC register value
     */
    public int getPC() {

        return this.pc;
    }

    /**
     * Sets the value of the PC register.
     *
     * @param thePc the PC register value
     */
    public void setPC(final int thePc) {

        this.pc = thePc & 0x0000FFFF;
    }

    /**
     * Adds a value to the PC register.
     *
     * @param delta the value to add
     */
    public void addPC(final int delta) {

        this.pc = (this.pc + delta) & 0x0000FFFF;
    }

    /**
     * Gets the value of the SP register.
     *
     * @return the SP register value
     */
    public int getSP() {

        return this.sp;
    }

    /**
     * Sets the value of the SP register.
     *
     * @param theSp the SP register value
     */
    public void setSP(final int theSp) {

        this.sp = theSp & 0x0000FFFF;
    }

    /**
     * Adds a value to the SP register.
     *
     * @param delta the value to add
     */
    public void addSP(final int delta) {

        this.sp = (this.sp + delta) & 0x0000FFFF;
    }

    /**
     * Gets the value of the I register.
     *
     * @return the I register value
     */
    public int getI() {

        return this.iReg;
    }

    /**
     * Sets the value of the I register.
     *
     * @param theI the I register value
     */
    public void setI(final int theI) {

        this.iReg = theI & 0x00FF;
    }

    /**
     * Gets the value of the R register.
     *
     * @return the R register value
     */
    public int getR() {

        return this.rReg;
    }

    /**
     * Sets the value of the R register.
     *
     * @param theR the R register value
     */
    public void setR(final int theR) {

        this.rReg = theR & 0x00FF;
    }

    /**
     * Gets the value of the bus data.
     *
     * @return the bus data
     */
    public int getBus() {

        return this.bus;
    }

    /**
     * Sets the value of the bus data.
     *
     * @param theBus the bus data value
     */
    public void setBus(final int theBus) {

        this.bus = theBus & 0x00FF;
    }

    /**
     * Gets the value of the link write data.
     *
     * @return the link write data
     */
    public int getLinkWrite() {

        return this.linkWrite;
    }

    /**
     * Sets the value of the link write data.
     *
     * @param theLinkWrite the link write data value
     */
    public void setLinkWrite(final int theLinkWrite) {

        this.linkWrite = theLinkWrite & 0x00FF;
    }

    /**
     * Gets the value of the interrupt mode.
     *
     * @return the interrupt mode
     */
    public int getIMode() {

        return this.imode;
    }

    /**
     * Sets the interrupt mode.
     *
     * @param theIMode the interrupt mode
     */
    public void setIMode(final int theIMode) {

        this.imode = theIMode;
    }

    /**
     * Gets the interrupt flag.
     *
     * @return the interrupt flag
     */
    public boolean isInterrupt() {

        return this.interrupt;
    }

    /**
     * Sets the interrupt flag.
     *
     * @param isInterrupt the interrupt flag
     */
    public void setInterrupt(final boolean isInterrupt) {

        this.interrupt = isInterrupt;
    }

    /**
     * Gets the EI block flag.
     *
     * @return the EI block flag
     */
    public boolean isEiBlock() {

        return this.eiBlock;
    }

    /**
     * Sets the EI block flag.
     *
     * @param isEIBlock the EI block flag
     */
    public void setEiBlock(final boolean isEIBlock) {

        this.eiBlock = isEIBlock;
    }

    /**
     * Gets the IFF1 flag.
     *
     * @return the IFF1 flag
     */
    public boolean isIff1() {

        return this.iff1;
    }

    /**
     * Sets the IFF1 flag.
     *
     * @param isIff1 the IFF1 flag
     */
    public void setIff1(final boolean isIff1) {

        this.iff1 = isIff1;
    }

    /**
     * Gets the IFF2 flag.
     *
     * @return the IFF2 flag
     */
    public boolean isIff2() {

        return this.iff2;
    }

    /**
     * Sets the IFF2 flag.
     *
     * @param isIff2 the IFF2 flag
     */
    public void setIff2(final boolean isIff2) {

        this.iff2 = isIff2;
    }

    /**
     * Gets the halt flag.
     *
     * @return the halt flag
     */
    public boolean isHalt() {

        return this.halt;
    }

    /**
     * Sets the halt flag.
     *
     * @param isHalt the halt flag
     */
    public void setHalt(final boolean isHalt) {

        this.halt = isHalt;
    }

    /**
     * Gets the read flag.
     *
     * @return the read flag
     */
    public boolean isRead() {

        return this.read;
    }

    /**
     * Sets the read flag.
     *
     * @param isRead the read flag
     */
    public void setRead(final boolean isRead) {

        this.read = isRead;
    }

    /**
     * Gets the write flag.
     *
     * @return the write flag
     */
    public boolean isWrite() {

        return this.write;
    }

    /**
     * Sets the write flag.
     *
     * @param isWrite the write flag
     */
    public void setWrite(final boolean isWrite) {

        this.write = isWrite;
    }

    /**
     * Gets the output flag.
     *
     * @return the output flag
     */
    public boolean isOutput() {

        return this.output;
    }

    /**
     * Sets the output flag.
     *
     * @param isOutput the output flag
     */
    public void setOutput(final boolean isOutput) {

        this.output = isOutput;
    }

    /**
     * Gets the input flag.
     *
     * @return the input flag
     */
    public boolean isInput() {

        return this.input;
    }

    /**
     * Sets the input flag.
     *
     * @param isInput the input flag
     */
    public void setInput(final boolean isInput) {

        this.input = isInput;
    }

    /**
     * Gets the prefix.
     *
     * @return the prefix
     */
    public int getPrefix() {

        return this.prefix;
    }

    /**
     * Sets the prefix.
     *
     * @param thePrefix the prefix
     */
    public void setPrefix(final int thePrefix) {

        this.prefix = thePrefix;
    }

    /**
     * Gets the PIO context.
     *
     * @return the PIO context
     */
    public PIOContext getPIOContext() {

        return this.pio;
    }

    /**
     * Gets the memory context.
     *
     * @return the memory context
     */
    public MemoryContext getMemoryContext() {

        return this.mem;
    }

    /**
     * Gets the timer context.
     *
     * @return the timer context
     */
    public TimerContext getTimerContext() {

        return this.timer;
    }

    /**
     * Gets the CPU version.
     *
     * @return the version
     */
    public int getVersion() {

        return this.cpuVersion;
    }

    /**
     * Sets the CPU version.
     *
     * @param theCpuVersion the version
     */
    public void setVersion(final int theCpuVersion) {

        this.cpuVersion = theCpuVersion;
    }

    /**
     * Gets the model bits.
     *
     * @return the model bits
     */
    public int getModelBits() {

        return this.modelBits;
    }

    /**
     * Sets the model bits.
     *
     * @param theModelBits the model bits
     */
    public void setModelBits(final int theModelBits) {

        this.modelBits = theModelBits;
    }

    /**
     * Gets the previous instruction.
     *
     * @return the previous instruction
     */
    public ReverseTime getPrevInstruction() {

        return this.prevInstruction;
    }

    /**
     * Gets the link instruction flag.
     *
     * @return the link instruction flag
     */
    public boolean isLinkInstruction() {

        return this.linkInstruction;
    }

    /**
     * Gets the linking time.
     *
     * @return the linking time
     */
    public long getLinkingTime() {

        return this.linkingTime;
    }

    /**
     * Gets the last time enter was pressed.
     *
     * @return the enter press time
     */
    public long getHasHitEnter() {

        return this.hasHitEnter;
    }

    /**
     * Sets the execution violation callback.
     *
     * @param theCallback the callback
     */
    public void setExeViolationCallback(final ICpuCallback theCallback) {

        this.exeViolationCallback = theCallback;
    }

    /**
     * Sets the invalid flash callback.
     *
     * @param theCallback the callback
     */
    public void setInvalidFlashCallback(final ICpuCallback theCallback) {

        this.invalidFlashCallback = theCallback;
    }

    /**
     * Sets the memory read break callback.
     *
     * @param theCallback the callback
     */
    public void setMemReadBreakCallback(final ICpuCallback theCallback) {

        this.memReadBreakCallback = theCallback;
    }

    /**
     * Sets the memory write break callback.
     *
     * @param theCallback the callback
     */
    public void setMemWriteBreakCallback(final ICpuCallback theCallback) {

        this.memWriteBreakCallback = theCallback;
    }

    /**
     * Gets the LCD enqueue callback.
     *
     * @return the callback
     */
    public ICpuCallback getLcdEnqueueCallback() {

        return this.lcdEnqueueCallback;
    }

    /**
     * Sets the LCD enqueue callback.
     *
     * @param theCallback the callback
     */
    public void setLcdEnqueueCallback(final ICpuCallback theCallback) {

        this.lcdEnqueueCallback = theCallback;
    }

    /**
     * Returns only the bits from the CPU flags register contained in a mask.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "unaffect" macro.
     *
     * @param mask the mask
     * @return the masked flags register
     */
    public int unaffect(final int mask) {

        return getF() & mask;
    }

    /**
     * Returns a boolean as the zero bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "dozero" macro.
     *
     * @param tval the boolean value
     * @return the zero bit value if true; 0 if false
     */
    private static int dozero(final boolean tval) {

        return tval ? JWCoreConstants.ZERO_MASK : 0;
    }

    /**
     * Returns a boolean as the X5 bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "dox5" macro.
     *
     * @param tval the boolean value
     * @return the X5 bit value if true; 0 if false
     */
    public static int dox5(final boolean tval) {

        return tval ? JWCoreConstants.X5_MASK : 0;
    }

    /**
     * Returns a boolean as the HC bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "dohc" macro.
     *
     * @param tval the boolean value
     * @return the HC bit value if true; 0 if false
     */
    public static int dohc(final boolean tval) {

        return tval ? JWCoreConstants.HC_MASK : 0;
    }

    /**
     * Returns a boolean as the X3 bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "dox3" macro.
     *
     * @param tval the boolean value
     * @return the X3 bit value if true; 0 if false
     */
    public static int dox3(final boolean tval) {

        return tval ? JWCoreConstants.X3_MASK : 0;
    }

    /**
     * Returns a boolean as the parity (PV) bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "doparity" macro.
     *
     * @param tval the boolean value
     * @return the parity (PV) bit value if true; 0 if false
     */
    public static int doparity(final boolean tval) {

        return tval ? JWCoreConstants.PV_MASK : 0;
    }

    /**
     * Returns a boolean as the carry bit of the flags register.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "carry" macro.
     *
     * @param tval the boolean value
     * @return the carry bit value if true; 0 if false
     */
    public static int carry(final boolean tval) {

        return tval ? JWCoreConstants.CARRY_MASK : 0;
    }

    /**
     * Tests the sign bit (0x80) of an 8-bit register value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "signchk" macro.
     *
     * @param tval the value
     * @return the value's sign bit (0x00 or 0x80)
     */
    public static int signchk(final int tval) {

        return tval & JWCoreConstants.SIGN_MASK;
    }

    /**
     * Tests whether an 8-bit register value is zero.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "zerochk" macro.
     *
     * @param zchk the value (only the low-order 8 bits are tested)
     * @return the zero bit value if the value is 0; 0 if it is nonzero
     */
    public static int zerochk(final int zchk) {

        return dozero((zchk & 0x00FF) == 0);
    }

    /**
     * Tests the 0x20 (X5_MASK) bit of an 8-bit register value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "x5chk" macro.
     *
     * @param tval the value
     * @return the value's X5_MASK bit (0x00 or 0x20)
     */
    public static int x5chk(final int tval) {

        return tval & JWCoreConstants.X5_MASK;
    }

    /**
     * Adds the low-order nibble of two 8-bit values with a carry, and tests whether the result overflows into the 5th
     * bit.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "hcaddchk" macro.
     *
     * @param opr1  the first value
     * @param opr2  the second value
     * @param carry the carry
     * @return HC_MASK (0x10) if overflow into 5th bit; 0 if not
     */
    public static int hcaddchk(final int opr1, final int opr2, final int carry) {

        return ((opr1 & 0x0F) + (opr2 & 0x0F) + carry) & JWCoreConstants.HC_MASK;
    }

    /**
     * Subtracts the low-order nibble of two one 8-bit value from that of another, subtracts a carry, and tests whether
     * the result overflows into the 5th bit.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "hcsubchk" macro.
     *
     * @param opr1  the first value
     * @param opr2  the second value
     * @param carry the carry
     * @return HC_MASK (0x10) if overflow into 5th bit; 0 if not
     */
    public static int hcsubchk(final int opr1, final int opr2, final int carry) {

        return ((opr1 & 0x0F) - (opr2 & 0x0F) - carry) & JWCoreConstants.HC_MASK;
    }

    /**
     * Tests the 0x08 (X3_MASK) bit of an 8-bit register value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "x3chk" macro.
     *
     * @param tval the value
     * @return the value's X3_MASK bit (0x00 or 0x08)
     */
    public static int x3chk(final int tval) {

        return tval & JWCoreConstants.X3_MASK;
    }

    /**
     * Tests whether an addition overflowed.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "vchkadd" macro.
     *
     * @param opr1 the first term added
     * @param opr2 the second term added
     * @param res  the result of the addition
     * @return PV_MASK if overflow occurred; 0 if not
     */
    public static int vchkadd(final int opr1, final int opr2, final int res) {

        final boolean test1 = (opr1 & 0x80) == (opr2 & 0x80);
        final boolean test2 = (opr1 & 0x80) != (res & 0x80);

        return ((test1 ? 1 : 0) & (test2 ? 1 : 0)) << 2;
    }

    /**
     * Tests whether a subtraction overflowed.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "vchksub" macro.
     *
     * @param opr1 the first term
     * @param opr2 the term subtracted from the first term
     * @param res  the result of the subtraction
     * @return PV_MASK if overflow occurred; 0 if not
     */
    public static int vchksub(final int opr1, final int opr2, final int res) {

        final boolean test1 = (opr1 & 0x80) != (opr2 & 0x80);
        final boolean test2 = (opr1 & 0x80) != (res & 0x80);

        return ((test1 ? 1 : 0) & (test2 ? 1 : 0)) << 2;
    }

    /**
     * Computes the parity of a value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "parity" macro.
     *
     * @param opr1 the value
     * @return PV_MASK if odd parity; 0 if not
     */
    public static int parity(final int opr1) {

        return (((opr1 ^ (opr1 >> 1) ^ (opr1 >> 2) ^ (opr1 >> 3) ^ (opr1 >> 4) ^ (opr1 >> 5)
                ^ (opr1 >> 6) ^ (opr1 >> 7)) & 1) ^ 1) * JWCoreConstants.PV_MASK;
    }

    /**
     * Tests whether a value has the carry bit set.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "carrychk" macro.
     *
     * @param tval the value
     * @return CARRY_MASK if carry bit (0x100) was set; 0 if not
     */
    public static int carrychk(final int tval) {

        return (tval & 0x100) >> 8;
    }

    /**
     * Tests the sign of a 16-bit register value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "signchk16" macro.
     *
     * @param tval the value
     * @return SIGN_MASK if the sign bit was set; 0 if not
     */
    public static int signchk16(final int tval) {

        return (tval >> 8) & JWCoreConstants.SIGN_MASK;
    }

    /**
     * Tests whether a 16-bit register value is zero (only the low-order 16 bits are tested).
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "zerochk16" macro.
     *
     * @param tval the value
     * @return ZERO_MASK if the sign bit was set; 0 if not
     */
    public static int zerochk16(final int tval) {

        return dozero((tval & 0x0000FFFF) == 0);
    }

    /**
     * Tests whether the upper byte of a 16-bit value has its X5 bit (0x20) set.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "x5chk16" macro.
     *
     * @param tval the value
     * @return ZERO_MASK if the sign bit was set; 0 if not
     */
    public static int x5chk16(final int tval) {

        return (tval >> 8) & JWCoreConstants.X5_MASK;
    }

    /**
     * Adds the low-order three nibbles of two 16-bit values with a carry, and tests whether the result overflows into
     * the 13th bit.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "hcaddchk16" macro.
     *
     * @param opr1  the first value
     * @param opr2  the second value
     * @param carry the carry
     * @return HC_MASK (0x10) if overflow into 13th bit; 0 if not
     */
    public static int hcaddchk16(final int opr1, final int opr2, final int carry) {

        return (((opr1 & 0x0fff) + (opr2 & 0x0fff) + carry) & 0x1000) >> 8;
    }

    /**
     * Subtracts the low-order three nibbles of one 16-bit value from another, subtracts a carry, and tests whether the
     * result overflows into the 13th bit.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "hcsubchk16" macro.
     *
     * @param opr1  the first value
     * @param opr2  the second value
     * @param carry the carry
     * @return HC_MASK (0x10) if overflow into 13th bit; 0 if not
     */
    public static int hcsubchk16(final int opr1, final int opr2, final int carry) {

        return (((opr1 & 0x0fff) - (opr2 & 0x0fff) - carry) & 0x1000) >> 8;
    }

    /**
     * Tests the X3 (0x08) bit of the high-order byte of a 16-bit value.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "x3chk16" macro.
     *
     * @param tval the value
     * @return X3_MASK if the X3 bit of the high byte was set; 0 if not
     */
    public static int x3chk16(final int tval) {

        return (tval >> 8) & JWCoreConstants.X3_MASK;
    }

    /**
     * Tests whether an addition of 16-bit values overflowed.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "vchkadd16" macro.
     *
     * @param opr1 the first term added
     * @param opr2 the second term added
     * @param res  the result of the addition
     * @return PV_MASK if overflow occurred; 0 if not
     */
    public static int vchkadd16(final int opr1, final int opr2, final int res) {

        final boolean test1 = (opr1 & 0x00008000) == (opr2 & 0x00008000);
        final boolean test2 = (opr1 & 0x00008000) != (res & 0x00008000);

        return ((test1 ? 1 : 0) & (test2 ? 1 : 0)) << 2;
    }

    /**
     * Tests whether subtraction of 16-bit values overflowed.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "vchksub16" macro.
     *
     * @param opr1 the first term added
     * @param opr2 the second term added
     * @param res  the result of the addition
     * @return PV_MASK if overflow occurred; 0 if not
     */
    public static int vchksub16(final int opr1, final int opr2, final int res) {

        final boolean test1 = (opr1 & 0x00008000) != (opr2 & 0x00008000);
        final boolean test2 = (opr1 & 0x00008000) != (res & 0x00008000);

        return ((test1 ? 1 : 0) & (test2 ? 1 : 0)) << 2;
    }

    /**
     * Tests whether a 16-bit value has the carry bit set.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.h, "carrychk16" macro.
     *
     * @param tval the value
     * @return CARRY_MASK if carry bit (0x100) was set; 0 if not
     */
    public static int carrychk16(final int tval) {

        return (tval & 0x10000) >> 16;
    }

    /**
     * Initializes a CPU.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_init" function.
     *
     * @return 0
     */
    public int cpuInit() {

        clear();

        this.mem.getPort27RemapCount()[0] = 0;
        this.mem.getPort28RemapCount()[0] = 0;
        this.mem.setFlashWriteDelay(200L);

        this.prevInstruction = this.prevInstructionList[0];

        return 0;
    }

    /**
     * Clear RAM and start calculator at $0000.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_reset" function.
     */
    public void cpuReset() {

        // SRB: Added these to make debugging easier - more consistent starting point
        this.af.set(0);
        this.bc.set(0);
        this.de.set(0);
        this.hl.set(0);
        this.afp.set(0);
        this.bcp.set(0);
        this.dep.set(0);
        this.hlp.set(0);
        this.rReg = 0;
        this.bus = 0;
        this.pio.getStdint().setIntactive(0);

        this.sp = 0;
        this.interrupt = false;
        this.imode = 1;
        this.eiBlock = false;
        this.iff1 = false;
        this.iff2 = false;
        this.halt = false;
        this.read = false;
        this.write = false;
        this.output = false;
        this.input = false;
        this.prefix = 0;
        this.pc = 0;
        this.mem.getPort27RemapCount()[0] = 0;
        this.mem.getPort28RemapCount()[0] = 0;
        this.mem.getRam().setLower(0);
        this.mem.getRam().setUpper(0x3FF);
        this.mem.activateNormalBanks();
        this.mem.setBootmapped(false);
        this.mem.setChangedPage0(false);

        this.mem.setProtectedPage(0, 0);
        this.mem.setProtectedPage(1, 0);
        this.mem.setProtectedPage(2, 0);
        this.mem.setProtectedPage(3, 0);
        this.mem.setProtectedPageSet(0);

        this.prevInstruction = this.prevInstructionList[0];
        for (final ReverseTime reverseTime : this.prevInstructionList) {
            reverseTime.clear();
        }
        this.reverseInstr = 0;

        final BankState[] banks = this.mem.getNormalBanks();
        final Memory ram = this.mem.getRam();
        final Memory flash = this.mem.getFlash();

        switch (this.pio.getModel()) {
            case TI_81:
                banks[0].set(flash, 0, 0, false, false, false);
                banks[1].set(flash, Memory.PAGE_SIZE, 0x1, false, false, false);
                banks[2].set(flash, Memory.PAGE_SIZE, 0x1, false, false, false);
                banks[3].set(ram, 0, 0, false, true, false);
                break;

            case TI_82:
            case TI_83:
                banks[0].set(flash, 0, 0, false, false, false);
                banks[1].set(flash, 0, 0, false, false, false);
                banks[2].set(ram, Memory.PAGE_SIZE, 0x01, false, true, false);
                banks[3].set(ram, 0, 0, false, true, false);
                break;

            case TI_85:
            case TI_86:
                banks[0].set(flash, 0, 0, false, false, false);
                banks[1].set(flash, 0x0F * Memory.PAGE_SIZE, 0x0F, false, false, false);
                banks[2].set(flash, 0, 0, false, false, false);
                banks[3].set(ram, 0, 0, false, true, false);
                break;

            case TI_73:
            case TI_83P:
            case TI_83PSE:
            case TI_84P:
            case TI_84PSE:
            case TI_84PCSE:
                final int bootpage = flash.getPages() - 1;
                banks[0].set(flash, bootpage * Memory.PAGE_SIZE, bootpage, false, false, false);
                banks[1].set(flash, 0, 0, false, false, false);
                banks[2].set(flash, 0, 0, false, false, false);
                banks[3].set(ram, 0, 0, false, true, false);
                break;

            case INVALID_MODEL:
            default:
                LoggedObject.LOG.warning("Unhandled model");
                break;
        }

        banks[4].set(null, 0, 0, false, false, false);

    }

    /**
     * WABBITEMU SOURCE: core/device.c: "handle_pio" function.
     */
    private void handlePio() {

        for (int i = this.pio.getNumInterrupt() - 1; i >= 0; --i) {
            final Interrupt intVal = this.pio.getInterrupt(i);

            intVal.setSkipCount(intVal.getSkipCount() - 1);

            if (intVal.getSkipCount() == 0) {
                final IDevice device = intVal.getDevice();
                if (device != null && device.isActive()) {
                    device.runCode(this);
                }
                intVal.setSkipCount(intVal.getSkipFactor());
            }
        }
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "is_privileged_page" function.
     *
     * @return true if privileged page
     */
    public boolean isPrivilegedPage() {

        // privileged pages are as follows
        // TI 83+ = 1C, 1D, 1F
        // TI 83+SE = 7C, 7D, 7F
        // TI-84+ = 2F, 3C, 3D, 3F
        // TI 84+SE = 6F, 7C, 7D, 7F

        final BankState bank = this.mem.getBanks()[BankState.mcBank(this.pc)];
        if (bank.isRam()) {
            return false;
        }

        final int maxPages = this.mem.getFlash().getPages();
        final int page = bank.getPage();

        return (page >= maxPages - 4 && page != maxPages - 2)
                || (((this.pio.getModel().ordinal() >= EnumCalcModel.TI_84P.ordinal())
                && page == maxPages - 0x11));
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "is_allowed_exec" function.
     *
     * @return true if execution is allowed
     */
    private boolean isAllowedExec() {

        final BankState bank = this.mem.getBanks()[BankState.mcBank(this.pc)];

        if (this.pio.getModel().ordinal() <= EnumCalcModel.TI_83P.ordinal()) {
            final int protectedVal;

            if (bank.isRam()) {
                protectedVal = this.mem.getProtectedPage(3);
                if ((protectedVal & 0x01) != 0 && bank.getPage() == 0) {
                    return false;
                }
                return (protectedVal & 0x20) == 0 || bank.getPage() != 1;
            } else if (bank.getPage() < 0x08) {
                return true;
            } else if (bank.getPage() >= 0x1C) {
                return true;
            }

            protectedVal = this.mem.getProtectedPage((bank.getPage() - 8) / 8);

            return (protectedVal & (0x01 << ((bank.getPage() - 8) % 8))) == 0;
        }

        if (!bank.isRam()) {
            return bank.getPage() <= this.mem.getFlash().getLower() || bank.getPage() > this.mem.getFlash().getUpper();
        }

        if ((bank.getPage() & (2 >> (this.mem.getProtMode() + 1))) != 0) {
            return true;
        }

        int globalAddr = bank.getPage() * Memory.PAGE_SIZE + (this.pc & 0x3FFF);

        if ((this.mem.getPort27RemapCount()[0] > 0) && !this.mem.isBootmapped()
                && (BankState.mcBank(this.pc) == 3)
                && (this.pc >= (0x10000 - 64 * this.mem.getPort27RemapCount()[0]))
                && this.pc >= 0xFB64) {
            globalAddr = BankState.mcBase(this.pc);
        } else if ((this.mem.getPort28RemapCount()[0] > 0) && !this.mem.isBootmapped()
                && (BankState.mcBank(this.pc) == 2)
                && (BankState.mcBase(this.pc) < 64 * this.mem.getPort28RemapCount()[0])) {
            globalAddr = Memory.PAGE_SIZE + BankState.mcBase(this.pc);
        }

        return globalAddr >= this.mem.getRam().getLower() && globalAddr <= this.mem.getRam().getUpper();
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "endflash_break" function.
     */
    private void endFlashBreak() {

        if (this.invalidFlashCallback != null) {
            this.invalidFlashCallback.exec(this);
        }
        this.mem.endFlash();
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "CPU_opcode_fetch" function.
     */
    void cpuOpcodeFetch() {

        final int bankNum = BankState.mcBank(this.pc);
        final BankState bank = this.mem.getBanks()[bankNum];

        if (!this.mem.isChangedPage0() && !bank.isRam()
                && (bankNum == 1 || (this.mem.isBootmapped() && bankNum == 2))) {

            this.mem.changePage(0, 0, false);
            this.mem.setChangedPage0(true);
        }

        if (!isAllowedExec()) {
            if (this.exeViolationCallback != null) {
                this.exeViolationCallback.exec(this);
            } else {
                LoggedObject.LOG.warning("Execution when not allowed - resetting");
                cpuReset();
            }
        }

        if (!bank.isRam() && this.mem.getStep() != EnumFlashCommand.FLASH_READ) {
            endFlashBreak();
        }

        setBus(this.mem.memRead(this.pc));

        if (bank.isRam()) {
            this.timer.setcAdd(this, (long) this.mem.getReadOPRamTStates());
        } else {
            this.timer.setcAdd(this, (long) this.mem.getReadOPFlashTStates());
        }

        addPC(1);

        setR((this.rReg & 0x80) + ((this.rReg + 1) & 0x7F));
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "flash_autoselect" function.
     *
     * @param addr the address
     * @return the flash chip manufacturer ID
     */
    private int flashAutoselect(final int addr) {

        final int result;

        final int offset = addr & 0x3FFF;
        if (offset == 0) {
            result = 1;
        } else if (offset == 2) {
            switch (this.pio.getModel()) {
                case TI_84P:
                    result = 0xDA;
                    break;

                case TI_83PSE:
                case TI_84PSE:
                    result = 0xC4;
                    break;

                case INVALID_MODEL:
                case TI_73:
                case TI_81:
                case TI_82:
                case TI_83:
                case TI_83P:
                case TI_84PCSE:
                case TI_85:
                case TI_86:
                default:
                    if (this.cpuVersion == 1) {
                        result = 0x23;
                    } else {
                        result = 0xB9;
                    }
                    break;
            }
        } else if (offset == 4) {
            result = 0;
        } else {
            endFlashBreak();
            result = 0;
        }

        return result;
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "flash_read" function.
     *
     * @param addr the address
     * @return the byte read
     */
    private int flashRead(final int addr) {

        final int result;

        if (this.mem.isFlashError()) {
            int errorValue = ((~this.mem.getFlashWriteByte() & 0x80) | 0x20) & 0x00FF;
            errorValue |= this.mem.getFlashToggles();
            this.mem.setFlashToggles(this.mem.getFlashToggles() ^ 0x40);
            this.mem.setFlashError(false);
            result = errorValue;
        } else if (this.mem.getStep() == EnumFlashCommand.FLASH_READ
                || this.mem.getStep() == EnumFlashCommand.FLASH_FASTMODE) {
            result = this.mem.memRead(addr);
        } else if (this.mem.getStep() == EnumFlashCommand.FLASH_AUTOSELECT) {
            result = flashAutoselect(addr);
        } else {
            endFlashBreak();
            result = this.mem.memRead(addr);
        }

        return result;
    }

    /**
     * Reads a byte of data from memory and stores it on the CPU bus, adding T states if on an SE model, and executing
     * breakpoint callbacks if a memory read breakpoint is hit.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_mem_read" function.
     *
     * @param addr the address
     * @return the data byte read (also on the bus)
     */
    public int cpuMemRead(final int addr) {

        if (this.mem.checkMemReadBreak(this.mem.addr16ToWideAddr(addr))) {
            if (this.memReadBreakCallback != null) {
                this.memReadBreakCallback.exec(this);
            }
        }

        if (this.mem.getBanks()[BankState.mcBank(addr)].isRam()) {
            setBus(this.mem.memRead(addr));
            this.timer.setcAdd(this, (long) this.mem.getReadNOPRamTStates());
        } else {
            setBus(flashRead(addr));
            this.timer.setcAdd(this, (long) this.mem.getReadNOPFlashTStates());
        }

        return this.bus;
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "flash_write" function.
     *
     * @param addr the address
     * @param data the data
     */
    private void flashWrite(final int addr, final int data) {

        final int ushortAddr = addr & 0x0000FFFF;
        final int byteData = data & 0x00FF;

        final int bank = BankState.mcBank(ushortAddr);

        if (byteData == 0x00F0 && this.mem.getStep() != EnumFlashCommand.FLASH_PROGRAM
                && this.mem.getStep() != EnumFlashCommand.FLASH_FASTMODE_PROG) {
            this.mem.endFlash();
            return;
        }

        this.mem.setFlashError(false);

        switch (this.mem.getStep()) {
            case FLASH_READ:
                if (((ushortAddr & 0x0FFF) == 0x0AAA) && (byteData == 0x00AA)) {
                    this.mem.setStep(EnumFlashCommand.FLASH_AA);
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_AA:
                if (((ushortAddr & 0x0FFF) == 0x0555) && (byteData == 0x0055)) {
                    this.mem.setStep(EnumFlashCommand.FLASH_55);
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_55:
                if ((ushortAddr & 0x0FFF) == 0x0AAA) {
                    switch (byteData) {
                        case JWCoreConstants.FLASH_BYTE_PROGRAM:
                            this.mem.setStep(EnumFlashCommand.FLASH_PROGRAM);
                            break;
                        case JWCoreConstants.FLASH_BYTE_ERASE:
                            this.mem.setStep(EnumFlashCommand.FLASH_ERASE);
                            break;
                        case JWCoreConstants.FLASH_BYTE_FASTMODE:
                            if (this.mem.getFlash().getVersion() == 1) {
                                endFlashBreak();
                            } else {
                                this.mem.setStep(EnumFlashCommand.FLASH_FASTMODE);
                            }
                            break;
                        case JWCoreConstants.FLASH_BYTE_AUTOSELECT:
                            this.mem.setStep(EnumFlashCommand.FLASH_AUTOSELECT);
                            break;
                        default:
                            endFlashBreak();
                            break;
                    }
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_PROGRAM:
                this.mem.flashWriteByte(ushortAddr, byteData);
                if (this.mem.checkMemWriteBreak(this.mem.addr16ToWideAddr(ushortAddr))) {
                    if (this.memWriteBreakCallback != null) {
                        this.memWriteBreakCallback.exec(this);
                    }
                }
                this.mem.endFlash();
                break;

            case FLASH_ERASE:
                if (((ushortAddr & 0x0FFF) == 0x0AAA) && (byteData == 0x00AA)) {
                    this.mem.setStep(EnumFlashCommand.FLASH_ERASE_AA);
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_ERASE_AA:
                if (((ushortAddr & 0x0FFF) == 0x0555) && (byteData == 0x0055)) {
                    this.mem.setStep(EnumFlashCommand.FLASH_ERASE_55);
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_ERASE_55:
                if (((ushortAddr & 0x0FFF) == 0x0AAA) && (byteData == 0x10)) {
                    for (int i = 0; i < this.mem.getFlash().getSize(); ++i) {
                        this.mem.getFlash().set(i, 0x00FF);

                        if (this.mem.checkMemWriteBreak(MemoryContext.addr32ToWideAddr(i))) {
                            if (this.memWriteBreakCallback != null) {
                                this.memWriteBreakCallback.exec(this);
                            }
                        }
                    }
                } else if (byteData == 0x30) {
                    final int spage = (this.mem.getBanks()[bank].getPage() << 1) + ((ushortAddr >> 13) & 0x01);
                    final int pages = this.mem.getFlash().getPages();
                    final int totalPages = pages << 1;
                    final int startaddr;
                    final int endaddr;

                    if (spage < totalPages - 8) {
                        startaddr = (spage & 0x01FF) << 13;
                        endaddr = startaddr + (Memory.PAGE_SIZE << 2);
                    } else if (spage < totalPages - 4) {
                        startaddr = (pages - 4) * Memory.PAGE_SIZE;
                        endaddr = (pages - 2) * Memory.PAGE_SIZE;
                    } else if (spage < totalPages - 3) {
                        startaddr = (pages - 2) * Memory.PAGE_SIZE;
                        endaddr = (pages - 2) * Memory.PAGE_SIZE + Memory.PAGE_SIZE / 2;
                    } else if (spage < totalPages - 2) {
                        startaddr = (pages - 2) * Memory.PAGE_SIZE + Memory.PAGE_SIZE / 2;
                        endaddr = (pages - 1) * Memory.PAGE_SIZE;
                    } else if (spage < totalPages) {
                        startaddr = (pages - 1) * Memory.PAGE_SIZE;
                        endaddr = pages * Memory.PAGE_SIZE;
                    } else {
                        endFlashBreak();
                        break;
                    }

                    for (int i = startaddr; i < endaddr; ++i) {
                        this.mem.getFlash().set(i, 0x00FF);

                        if (this.mem.checkMemWriteBreak(MemoryContext.addr32ToWideAddr(i))) {
                            if (this.memWriteBreakCallback != null) {
                                this.memWriteBreakCallback.exec(this);
                            }
                        }
                    }
                } else {
                    endFlashBreak();
                }
                this.mem.endFlash();
                break;

            case FLASH_FASTMODE:
                if (byteData == JWCoreConstants.FLASH_BYTE_FASTMODE_EXIT) {
                    this.mem.setStep(EnumFlashCommand.FLASH_FASTMODE_EXIT);
                } else if (byteData == JWCoreConstants.FLASH_BYTE_FASTMODE_PROG) {
                    this.mem.setStep(EnumFlashCommand.FLASH_FASTMODE_PROG);
                } else {
                    endFlashBreak();
                }
                break;

            case FLASH_FASTMODE_EXIT:
                this.mem.setStep(EnumFlashCommand.FLASH_FASTMODE);
                break;

            case FLASH_FASTMODE_PROG:
                this.mem.flashWriteByte(ushortAddr, byteData);
                if (this.mem.checkMemWriteBreak(this.mem.addr16ToWideAddr(ushortAddr))) {
                    if (this.memWriteBreakCallback != null) {
                        this.memWriteBreakCallback.exec(this);
                    }
                }
                this.mem.setStep(EnumFlashCommand.FLASH_FASTMODE);
                break;

            case FLASH_AUTOSELECT:
            case FLASH_ERROR:
            default:
                endFlashBreak();
                break;
        }
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "check_flash_write_valid" function.
     *
     * @param page the page
     * @return true if writing to that page is valid
     */
    private boolean checkFlashWriteValid(final int page) {

        return !this.mem.isFlashLocked()
                && this.pio.getModel().ordinal() >= EnumCalcModel.TI_73.ordinal()
                && (((page != 0x3f && page != 0x2F) || ((this.modelBits & 0x03) != 0))
                && ((page != 0x7F && page != 0x6F) || ((this.modelBits & 0x02) != 0)
                || ((this.modelBits & 0x01) == 0)));
    }

    /**
     * Writes a byte of data to memory.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_mem_write" function.
     *
     * @param addr the address
     * @param data the data byte to write
     */
    public void cpuMemWrite(final int addr, final int data) {

        final int ushortAddr = addr & 0x0000FFFF;
        final int byteData = data & 0x00FF;

        final BankState bank = this.mem.getBanks()[BankState.mcBank(ushortAddr)];

        if (bank.isRam()) {
            if (!bank.isReadOnly()) {
                this.mem.memWrite(ushortAddr, byteData);

                if (this.mem.checkMemWriteBreak(this.mem.addr16ToWideAddr(ushortAddr))) {
                    if (this.memWriteBreakCallback != null) {
                        this.memWriteBreakCallback.exec(this);
                    }
                }
            }
            this.timer.setcAdd(this, (long) this.mem.getWriteRamTStates());
        } else {
            if (checkFlashWriteValid(bank.getPage())) {
                flashWrite(ushortAddr, byteData);
            } else if (this.invalidFlashCallback != null) {
                this.invalidFlashCallback.exec(this);
            }

            this.timer.setcAdd(this, (long) this.mem.getWriteFlashTStates());
        }

        setBus(byteData);
    }

    /**
     * Caches a previous instruction record.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_add_prev_instr" function.
     */
    private void cpuAddPrevInstr() {

        if (this.prevInstruction != null) {
            this.prevInstruction.setFlag(getF());
            this.prevInstruction.setBus(this.bus);
            this.prevInstruction.setR(this.rReg);
        }

        ++this.reverseInstr;
        if (this.reverseInstr >= this.prevInstructionList.length) {
            this.reverseInstr = 0;
        }

        this.prevInstruction = this.prevInstructionList[this.reverseInstr];
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "CPU_opcode_run" function.
     */
    void cpuOpcodeRun() {

        final int theBus = this.bus;
        this.returned = false;

        if (ReverseInfoTable.opcodeReverseInfo[theBus] != null) {
            ReverseInfoTable.opcodeReverseInfo[theBus].exec(this);
        }

        final int time = OpTable.opcode[theBus].exec(this);
        if (time != 0) {
            this.timer.tcAdd((long) time);
        }
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "handle_interrupt" function.
     */
    private void handleInterrupt() {

        if (this.iff1) {
            this.iff1 = false;
            this.iff2 = false;

            if (this.imode == 0) {
                this.halt = false;
                cpuOpcodeRun();
            } else if (this.imode == 1) {
                this.timer.tcAdd(8L);
                this.halt = false;
                setBus(0x00FF);
                cpuAddPrevInstr();
                cpuOpcodeRun();
            } else if (this.imode == 2) {
                this.timer.tcAdd(19L);
                this.halt = false;
                int vector = ((this.iReg << 8) + this.bus) & 0x0000FFFF;
                int reg = cpuMemRead(vector) & 0x0000FFFF;
                ++vector;
                reg = (reg + (cpuMemRead(vector) << 8)) & 0x0000FFFF;
                addSP(-1);
                cpuMemWrite(this.sp, (this.pc >> 8) & 0x00FF);
                addSP(-1);
                cpuMemWrite(this.sp, this.pc & 0x00FF);
                setPC(reg);
            }
        }
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "handle_profiling" function.
     *
     * @param oldTStates the old timer states value
     * @param theOldPC   the old PC value
     */
    private void handleProfiling(final long oldTStates, final int theOldPC) {

        final long time = this.timer.getTStates() - oldTStates;
        this.profiler.setTotalTime(this.profiler.getTotalTime() + time);

        final BankState bank = this.mem.getBanks()[BankState.mcBank(theOldPC)];
        final int block = (theOldPC % Memory.PAGE_SIZE) / this.profiler.getBlockSize();

        if (bank.isRam()) {
            this.profiler.getRamData()[bank.getPage()][block] += time;
        } else {
            this.profiler.getFlashData()[bank.getPage()][block] += time;
        }
    }

    /**
     * WABBITEMU SOURCE: core/core.c: "CPU_step" function.
     *
     * @return 0
     */
    public int step() {

        this.interrupt = false;
        this.eiBlock = false;

        final int theOldPC = this.pc;
        final long oldTStates = this.timer.getTStates();

        cpuAddPrevInstr();

        if (this.halt) {
            this.timer.tcAdd((long) (4 * JWCoreConstants.HALT_SCALE));
            setR((this.rReg & 0x80) + ((this.rReg + JWCoreConstants.HALT_SCALE) & 0x7F));
        } else {
            cpuOpcodeFetch();
            cpuOpcodeRun();
        }

        handlePio();

        if (this.interrupt && !this.eiBlock) {
            final int edPrefix = this.mem.memRead(this.pc - 2);
            final int instruction = this.mem.memRead(this.pc - 1);
            if (edPrefix == 0xED && (instruction == 0x57 || instruction == 0x5F)) {
                setF(getF() & ~JWCoreConstants.PV_MASK);
            }
            handleInterrupt();
        }

        if (this.profiler.isRunning()) {
            this.handleProfiling(oldTStates, theOldPC);
        }

        return 0;
    }

    /**
     * WABBITEMU SOURCE: core/device.c: "ClearDevices" function.
     */
    public void clearDevices() {

        for (int i = 0; i <= this.pio.getMaxInterrupt(); ++i) {
            if (this.pio.getDevice(i) != null) {
                this.pio.getDevice(i).setActive(false);
            }
            if (this.pio.getInterrupt(i) != null) {
                this.pio.getInterrupt(i).setSkipFactor(1);
                this.pio.getInterrupt(i).setSkipCount(1);
                this.pio.getInterrupt(i).setDevice(null);

            }
        }
        this.pio.setNumInterrupt(0);
    }

    /**
     * WABBITEMU SOURCE: core/device.c: "device_output" function.
     *
     * @param dev the device
     */
    public void deviceOutput(final int dev) {

        final IDevice device = this.pio.getDevice(dev);

        if (device != null && device.isActive()) {
            this.output = true;

            this.pio.setMostRecentOutput(dev, this.bus);

            if (!device.isProtected() || !this.mem.isFlashLocked()) {
                device.runCode(this);
            }

            if (device.isBreakpoint()) {
                this.pio.getBreakpointCallback().exec(this);
            }

            if (this.output) {
                // Device is not responding
                LoggedObject.LOG.warning("Device ", Integer.toString(dev), " not responding for output");
                this.output = false;
            }
        }

    }

    /**
     * WABBITEMU SOURCE: core/device.c: "device_input" function.
     *
     * @param dev the device
     */
    public void deviceInput(final int dev) {

        final IDevice device = this.pio.getDevice(dev);

        if (device != null && device.isActive()) {
            this.input = true;
            if (device.isBreakpoint()) {
                this.pio.getBreakpointCallback().exec(this);
            }

            device.runCode(this);

            if (this.input) {
                LoggedObject.LOG.warning("Device " + dev + " not responding for input");
                // Device is not responding
                this.input = false;
                setBus(0xFF);
                this.pio.setMostRecentInput(dev, 0xFF);
                return;
            }

            this.pio.setMostRecentInput(dev, this.bus);
        } else {
            setBus(0xFF);
        }

    }

    /**
     * WABBITEMU SOURCE: core/device.c: "Append_interrupt_device" function.
     *
     * @param port the port
     * @param skip skip factor
     */
    public void appendInterruptDevice(final int port, final int skip) {

        final Interrupt intVal = this.pio.getInterrupt(this.pio.getNumInterrupt());

        intVal.setDevice(this.pio.getDevice(port));
        intVal.setSkipFactor(skip);

        this.pio.setNumInterrupt(this.pio.getNumInterrupt() + 1);
    }

    /**
     * WABBITEMU SOURCE: core/device.c: "Modify_interrupt_device" function.
     *
     * @param port the port
     * @param skip skip factor
     */
    public void modifyInterruptDevice(final int port, final int skip) {

        final IDevice device = this.pio.getDevice(port);

        for (int i = 0; i < this.pio.getNumInterrupt(); ++i) {
            if (this.pio.getInterrupt(i).getDevice() == device) {
                this.pio.getInterrupt(i).setSkipFactor(skip);
                break;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "GetCPUSpeed" function.
     *
     * @return the speed (0x00 through 0x03)
     */
    public int getCPUSpeed() {

        return switch (this.timer.getFreq()) {
            case JWCoreConstants.MHZ_15 -> 0x01;
            case JWCoreConstants.MHZ_20 -> 0x02;
            case JWCoreConstants.MHZ_25 -> 0x03;
            default -> 0x00;
        };
    }

    /**
     * WABBITEMU SOURCE: utilities/sound.c, "FlippedLeft" function.
     *
     * @param on 1 if on, 0 if not
     */
    public void flippedLeft(final int on) {

        final Link link = this.pio.getLink();
        final AUDIO audio = link.getAudio();

        if (!audio.isEnabled()) {
            return;
        }

        final double elapsed = this.timer.getElapsed();

        if (on == 1) {
            audio.setLastFlipLeft(elapsed);
        } else if (on == 0) {
            audio.setHighLengLeft(audio.getHighLengLeft() + elapsed - audio.getLastFlipLeft());
        }
        audio.setLeftOn(on);

    }

    /**
     * WABBITEMU SOURCE: utilities/sound.c, "FlippedRight" function.
     *
     * @param on 1 if on, 0 if not
     */
    public void flippedRight(final int on) {

        final Link link = this.pio.getLink();
        final AUDIO audio = link.getAudio();

        if (!audio.isEnabled()) {
            return;
        }

        final double elapsed = this.timer.getElapsed();

        if (on == 1) {
            audio.setLastFlipRight(elapsed);
        } else if (on == 0) {
            audio.setHighLengRight(audio.getHighLengRight() + elapsed - audio.getLastFlipRight());
        }
        audio.setRightOn(on);
    }

    /**
     * WABBITEMU SOURCE: utilities/sound.c, "nextsample" function.
     */
    public void nextSample() {

        final Link link = this.pio.getLink();
        final AUDIO audio = link.getAudio();

        if (!audio.isEnabled()) {
            return;
        }

        if (this.timer.getElapsed() < (audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH)) {
            return;
        }

        if (audio.getRightOn() == 1) {
            if ((audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH) > audio.getLastFlipRight()) {
                audio.setHighLengRight(audio.getHighLengRight()
                        + ((audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH) - audio.getLastFlipRight()));
                audio.setLastFlipRight(audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH);
            }
        }

        if (audio.getLeftOn() == 1) {
            if ((audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH) > audio.getLastFlipLeft()) {
                audio.setHighLengLeft(audio.getHighLengLeft()
                        + ((audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH) - audio.getLastFlipLeft()));
                audio.setLastFlipLeft(audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH);
            }
        }

        if (audio.getHighLengLeft() < 0.0) {
            LoggedObject.LOG.warning("Left less than 0");
            audio.setHighLengLeft(0.0);
        }
        if (audio.getHighLengLeft() > (double) AUDIO.SAMPLE_LENGTH) {
            audio.setHighLengLeft((double) AUDIO.SAMPLE_LENGTH);
        }

        if (audio.getHighLengRight() < 0.0) {
            LoggedObject.LOG.warning("right less than 0");
            audio.setHighLengRight(0.0);
        }
        if (audio.getHighLengRight() > (double) AUDIO.SAMPLE_LENGTH) {
            audio.setHighLengRight((double) AUDIO.SAMPLE_LENGTH);
        }

        final double max = 255.0 * audio.getVolume();
        final double lower = (255.0 - max) / 2.0;
        double tmp = (audio.getHighLengLeft() * max * (double) AUDIO.SAMPLE_RATE) + lower;
        if (tmp < 0.0) {
            LoggedObject.LOG.warning("Left less than 0");
            tmp = 0.0;
        }
        if (tmp > 255.0) {
            LoggedObject.LOG.warning("Left greater than 255");
            tmp = 255.0;
        }

        final int left = ((int) tmp) & 0x00FF;

        tmp = (audio.getHighLengRight() * max * (double) AUDIO.SAMPLE_RATE) + lower;
        if (tmp < 0.0) {
            LoggedObject.LOG.warning("Right less than 0");
            tmp = 0.0;
        }
        if (tmp > 255.0) {
            LoggedObject.LOG.warning("Right greater than 255");
            tmp = 255.0;
        }

        final int right = ((int) tmp) & 0x00FF;

        audio.getBuffer()[audio.getCurPnt()].setLeft(left);
        audio.getBuffer()[audio.getCurPnt()].setRight(right);

        audio.setCurPnt((audio.getCurPnt() + 1) % AUDIO.BUFFER_SAMPLES);

        audio.setHighLengRight(0.0);
        audio.setHighLengLeft(0.0);
        audio.setLastSample(audio.getLastSample() + (double) AUDIO.SAMPLE_LENGTH);

        if ((audio.getLastSample() + ((double) AUDIO.SAMPLE_LENGTH * 2.0)) < this.timer.getElapsed()) {
            LoggedObject.LOG.warning("Last sample out of sync");
            audio.setLastSample(this.timer.getElapsed());
        }
    }
}
