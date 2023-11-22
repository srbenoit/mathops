package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.core.WideAddr;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.BCall;
import jwabbit.utilities.Flag;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Dissasembler.
 */
public final class Disassemble {

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_NOP" macro. */
    private static final int DA_NOP = 0;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EX_AF_AF_" macro. */
    private static final int DA_EX_AF_AF_2 = 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DJNZ_X" macro. */
    private static final int DA_DJNZ_X = 2;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JR_X" macro. */
    private static final int DA_JR_X = 3;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JR_CC_X" macro. */
    private static final int DA_JR_CC_X = 4;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_RP_X" macro. */
    private static final int DA_LD_RP_X = 5;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ADD_HL_RP" macro. */
    private static final int DA_ADD_HL_RP = 6;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__BC__A" macro. */
    private static final int DA_LD_BC_A2 = 7;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_A__BC_" macro. */
    private static final int DA_LD_A_BC_2 = 8;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__DE__A" macro. */
    private static final int DA_LD_DE_A2 = 9;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_A__DE_" macro. */
    private static final int DA_LD_A_DE_2 = 10;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__X__HL" macro. */
    private static final int DA_LD_X_HL2 = 11;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_HL__X_" macro. */
    private static final int DA_LD_HL_X_2 = 12;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__X__A" macro. */
    private static final int DA_LD_X_A2 = 13;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_A__X_" macro. */
    private static final int DA_LD_A_X_2 = 14;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_INC_RP" macro. */
    private static final int DA_INC_RP = 15;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DEC_RP" macro. */
    private static final int DA_DEC_RP = 16;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_INC_R" macro. */
    private static final int DA_INC_R = 17;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DEC_R" macro. */
    private static final int DA_DEC_R = 18;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_R_X" macro. */
    private static final int DA_LD_R_X = 19;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RLCA" macro. */
    private static final int DA_RLCA = 20;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RRCA" macro. */
    private static final int DA_RRCA = 21;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RLA" macro. */
    private static final int DA_RLA = 22;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RRA" macro. */
    private static final int DA_RRA = 23;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DAA" macro. */
    private static final int DA_DAA = 24;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_CPL" macro. */
    private static final int DA_CPL = 25;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_SCF" macro. */
    private static final int DA_SCF = 26;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_CCF" macro. */
    private static final int DA_CCF = 27;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_R_R" macro. */
    private static final int DA_LD_R_R = 28;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_HALT" macro. */
    private static final int DA_HALT = 29;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ALU" macro. */
    private static final int DA_ALU = 30;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ALU_A" macro. */
    private static final int DA_ALU_A = 31;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RET_CC" macro. */
    private static final int DA_RET_CC = 32;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_POP_RP" macro. */
    private static final int DA_POP_RP = 33;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RET" macro. */
    private static final int DA_RET = 34;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EXX" macro. */
    private static final int DA_EXX = 35;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JP_HL" macro. */
    private static final int DA_JP_HL = 36;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_SP_HL" macro. */
    private static final int DA_LD_SP_HL = 37;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JP_CC_X" macro. */
    private static final int DA_JP_CC_X = 38;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JP_X" macro. */
    private static final int DA_JP_X = 39;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_OUT__X__A" macro. */
    private static final int DA_OUT_X_A2 = 40;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_IN_A__X_" macro. */
    private static final int DA_IN_A_X_2 = 41;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EX__SP__HL" macro. */
    private static final int DA_EX_SP_HL2 = 42;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EX_DE_HL" macro. */
    private static final int DA_EX_DE_HL = 43;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DI" macro. */
    private static final int DA_DI = 44;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EI" macro. */
    private static final int DA_EI = 45;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_CALL_CC_X" macro. */
    private static final int DA_CALL_CC_X = 46;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_PUSH_RP" macro. */
    private static final int DA_PUSH_RP = 47;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_CALL_X" macro. */
    private static final int DA_CALL_X = 48;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ALU_X" macro. */
    private static final int DA_ALU_X = 49;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RST_X" macro. */
    private static final int DA_RST_X = 51;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ROT" macro. */
    private static final int DA_ROT = 52;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BIT" macro. */
    private static final int DA_BIT = 53;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RES" macro. */
    private static final int DA_RES = 54;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_SET" macro. */
    private static final int DA_SET = 55;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_IN_R__C_" macro. */
    private static final int DA_IN_R_C_2 = 56;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_OUT__C__R" macro. */
    private static final int DA_OUT_C_R2 = 57;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_SBC_HL_RP" macro. */
    private static final int DA_SBC_HL_RP = 58;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ADC_HL_RP" macro. */
    private static final int DA_ADC_HL_RP = 59;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__X__RP" macro. */
    private static final int DA_LD_X_RP2 = 60;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_RP__X_" macro. */
    private static final int DA_LD_RP_X_2 = 61;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_NEG" macro. */
    private static final int DA_NEG = 62;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RETN" macro. */
    private static final int DA_RETN = 63;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RETI" macro. */
    private static final int DA_RETI = 64;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_IM_X" macro. */
    private static final int DA_IM_X = 65;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_I_A" macro. */
    private static final int DA_LD_I_A = 66;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_R_A" macro. */
    private static final int DA_LD_R_A = 67;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_A_I" macro. */
    private static final int DA_LD_A_I = 68;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_A_R" macro. */
    private static final int DA_LD_A_R = 69;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RRD" macro. */
    private static final int DA_RRD = 70;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RLD" macro. */
    private static final int DA_RLD = 71;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_NOP_ED" macro. */
    private static final int DA_NOP_ED = 72;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BLI" macro. */
    private static final int DA_BLI = 73;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ROT_R" macro. */
    private static final int DA_ROT_R = 74;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ROT_I" macro. */
    private static final int DA_ROT_I = 78;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ADD_RI_RP" macro. */
    private static final int DA_ADD_RI_RP = 82;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_RI__X_" macro. */
    private static final int DA_LD_RI_X_2 = 83;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__X__RI" macro. */
    private static final int DA_LD_X_RI2 = 84;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_INC_RI" macro. */
    private static final int DA_INC_RI = 85;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_RI_X" macro. */
    private static final int DA_LD_RI_X = 87;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_RI_R" macro. */
    private static final int DA_LD_RI_R = 88;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_R_RI" macro. */
    private static final int DA_LD_R_RI = 89;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_ALU_RI" macro. */
    private static final int DA_ALU_RI = 90;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_JP_RI" macro. */
    private static final int DA_JP_RI = 92;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_EX__SP__RI" macro. */
    private static final int DA_EX_SP_RI2 = 94;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LABEL" macro. */
    private static final int DA_LABEL = 95;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BCALL" macro. */
    private static final int DA_BCALL = 96;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BCALL_N" macro. */
    private static final int DA_BCALL_N = 97;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BIT_RF" macro. */
    private static final int DA_BIT_RF = 98;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BIT_IF" macro. */
    private static final int DA_BIT_IF = 101;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BJUMP" macro. */
    private static final int DA_BJUMP = 104;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BJUMP_N" macro. */
    private static final int DA_BJUMP_N = 105;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__HL__X" macro. */
    private static final int DA_LD_HL_X2 = DA_BJUMP_N + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD__HL__R" macro. */
    private static final int DA_LD_HL_R2 = DA_LD_HL_X2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_LD_R__HL_" macro. */
    private static final int DA_LD_R_HL_2 = DA_LD_HL_R2;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_INC__HL_" macro. */
    private static final int DA_INC_HL_2 = DA_LD_R_HL_2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_DEC__HL_" macro. */
    private static final int DA_DEC_HL_2 = DA_INC_HL_2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_BIT__HL_" macro. */
    private static final int DA_BIT_HL_2 = DA_DEC_HL_2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_RES__HL_" macro. */
    private static final int DA_RES_HL_2 = DA_BIT_HL_2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.h, "DA_SET__HL_" macro. */
    private static final int DA_SET_HL_2 = DA_RES_HL_2 + 1;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_C" macro. */
    private static final int R_C = 1;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R__HL_" macro. */
    private static final int R_HL_2 = 6;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_A" macro. */
    private static final int R_A = 7;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_BC" macro. */
    private static final int R_BC = 0;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_DE" macro. */
    private static final int R_DE = 1;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_HL" macro. */
    private static final int R_HL = 2;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "R_SP" macro. */
    private static final int R_SP = 3;

    /** WABBITEMU SOURCE: debugger/disassemble.c, "r" array. */
    private static final String[] R = {"b", "c", "d", "e", "h", "l", "(hl)", "a", "f"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "r8i" array. */
    private static final String[][] R8I = {
            {"b", "c", "d", "e", "ixh", "ixl", "ix", "a", "f"},
            {"b", "c", "d", "e", "iyh", "iyl", "iy", "a", "f"}};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "rp" array. */
    private static final String[] RP = {"bc", "de", "hl", "sp"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "rpi" array. */
    private static final String[][] RPI = {
            {"bc", "de", "ix", "sp"},
            {"bc", "de", "iy", "sp"}};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "rp2" array. */
    private static final String[] RP2 = {"bc", "de", "hl", "af"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "rpi2" array. */
    private static final String[][] RP2I = {
            {"bc", "de", "ix", "af"},
            {"bc", "de", "iy", "af"}};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "ri" array. */
    private static final String[] RI = {"ix", "iy"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "cc" array. */
    private static final String[] CC = {"nz", "z", "nc", "c", "po", "pe", "p", "m"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "alu" array. */
    private static final String[] ALU = {"add", "adc", "sub", "sbc", "and", "xor", "or", "cp"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "rot" array. */
    private static final String[] ROT = {"rlc", "rrc", "rl", "rr", "sla", "sra", "sll", "srl"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "im" array. */
    private static final String[] IM = {"0", "0/1", "1", "2", "0", "0/1", "1", "2"};

    /** WABBITEMU SOURCE: debugger/disassemble.c, "bli" array. */
    private static final String[][] BLI = {
            {"ldi", "cpi", "ini", "outi"},
            {"ldd", "cpd", "ind", "outd"},
            {"ldir", "cpir", "inir", "otir"},
            {"lddr", "cpdr", "indr", "otdr"}};

    /**
     * Constructs a new {@code Disassemble}.
     */
    public Disassemble() {

        super();
    }

    /**
     * WABBITEMU SOURCE: debugger/disassemble.c, "GetNextAddr" function.
     *
     * @param memc  the memory context
     * @param type  the view type
     * @param waddr the wide address
     * @return the next wide address (may return {@code waddr} with its fields updated)
     */
    private static WideAddr getNextAddr(final MemoryContext memc, final EViewType type,
                                        final WideAddr waddr) {

        WideAddr result = waddr;

        switch (type) {
            case REGULAR:
                result = memc.addr16ToWideAddr(waddr.getAddr() + 1);
                break;

            case RAM:
                final int raddr = (waddr.getAddr() + 1) % Memory.PAGE_SIZE;
                int rpage = waddr.getPage();
                if ((raddr % Memory.PAGE_SIZE) == 0) {
                    ++rpage;
                }
                final int rpages = memc.getRam().getPages();
                result = new WideAddr(rpage % rpages, raddr, true);
                break;

            case FLASH:
                final int faddr = (waddr.getAddr() + 1) % Memory.PAGE_SIZE;
                int fpage = waddr.getPage();
                if ((faddr % Memory.PAGE_SIZE) == 0) {
                    ++fpage;
                }
                final int fpages = memc.getFlash().getPages();
                result = new WideAddr(fpage % fpages, faddr, false);
                break;

            default:
                LoggedObject.LOG.warning("Unhandled case");
                break;
        }

        return result;
    }

    /**
     * Disassembles all data in memory starting at address 0.
     *
     * @param calc the calculator
     * @return the disassembly results
     */
    static Z80Info[] disassembleAll(final Calc calc) {

        if (calc.getCPU().getMemoryContext().getFlash().getSize() == 0) {
            LoggedObject.LOG.info("Calc flash is zero length");
            return new Z80Info[0];
        }

        final Z80Info[] temp = new Z80Info[2];
        temp[0] = new Z80Info();
        temp[1] = new Z80Info();

        final List<Z80Info> result = new ArrayList<>(1000);

        int priorAddr;
        int newAddr = 0;

        final WideAddr[] addr = {calc.getCPU().getMemoryContext().addr16ToWideAddr(0)};

        do {
            priorAddr = newAddr;

            final int count = disasmSingle(calc, EViewType.REGULAR, addr, temp, 0, false);
            if (count > 0) {
                result.add(temp[0]);
                temp[0] = new Z80Info();
            }
            if (count > 1) {
                result.add(temp[1]);
                temp[1] = new Z80Info();
            }

            newAddr = addr[0].getPage() * Memory.PAGE_SIZE + addr[0].getAddr();
        } while (newAddr > priorAddr);

        return result.toArray(new Z80Info[0]);
    }

    /**
     * Disassembles a single instruction, which will result in one or more Z80Info objects being populated.
     *
     * @param calc      the calculator
     * @param type      the display type
     * @param waddr     an array whose [0] elements is the wide address of the instruction to disassemble and which will
     *                  be populated with the address of the instruction following the current instruction on exit
     * @param result    the array of Z80Info objects in which to populate results (one or two will be populated)
     * @param start     the index of the first element in {@code result} to populate
     * @param tiosDebug TIOS debug flag
     * @return the number of result objects populated
     */
    public static int disasmSingle(final Calc calc, final EViewType type, final WideAddr[] waddr,
                                   final Z80Info[] result, final int start, final boolean tiosDebug) {

        int prefix = 0;
        int pi = 0;
        int resultIndex = start;
        WideAddr curAddr = waddr[0];
        final WideAddr startAddr = new WideAddr(curAddr);

        final MemoryContext memc = calc.getCPU().getMemoryContext();

        result[resultIndex].setWideAddr(curAddr);
        result[resultIndex].setBreakpoint(memc.getBreakpoint(curAddr));

        final String labelname = calc.findAddressLabel(curAddr);

        // If there is a label on this address, store it in a <code>Z80Info</code>
        if (labelname != null) {
            result[resultIndex].setIndex(DA_LABEL);
            result[resultIndex].setA1(labelname);
            result[resultIndex].setSize(0);
            result[resultIndex].setClocks(0);
            result[resultIndex].setOpcodeData(new int[0]);
            ++resultIndex;
            if (resultIndex >= result.length) {
                return 1;
            }
            result[resultIndex].setWideAddr(curAddr);
        }

        int data = memc.wmemRead(curAddr);
        curAddr = getNextAddr(memc, type, curAddr);

        if (data == 0xDD || data == 0xFD) {
            prefix = data;
            pi = (prefix >> 5) & 1;
            data = memc.wmemRead(curAddr);
            curAddr = getNextAddr(memc, type, curAddr);
        }

        if (data == 0xCB) {
            int offset = 0;
            data = memc.wmemRead(curAddr);
            curAddr = getNextAddr(memc, type, curAddr);

            if (prefix != 0) {
                offset = (char) data;
                data = memc.wmemRead(curAddr);
                curAddr = getNextAddr(memc, type, curAddr);
            }

            final int x = (data & 0xC0) >> 6;
            final int y = (data & 0x38) >> 3;
            final int z = data & 0x07;
            result[resultIndex].setA1(Integer.valueOf(y));
            result[resultIndex].setA2(R[z]);

            switch (x) {
                case 0: /* X = 0 */
                    result[resultIndex].setIndex(DA_ROT);
                    result[resultIndex].setA1(ROT[y]);
                    break;
                case 1:
                    result[resultIndex].setIndex((result[resultIndex].getA2().equals(R[R_HL_2]) && prefix == 0)
                            ? DA_BIT_HL_2 : DA_BIT);
                    break;
                case 2:
                    result[resultIndex].setIndex((result[resultIndex].getA2().equals(R[R_HL_2]) && prefix == 0)
                            ? DA_RES_HL_2 : DA_RES);
                    break;
                case 3:
                    result[resultIndex].setIndex((result[resultIndex].getA2().equals(R[R_HL_2]) && prefix == 0)
                            ? DA_SET_HL_2 : DA_SET);
                    break;
                default:
                    LoggedObject.LOG.warning("Unhandled case");
                    break;
            }

            if (prefix != 0) {
                final String[] flagname = new String[1];
                final String[] bitname = new String[1];
                Flag.findFlags(offset, y, flagname, bitname);

                // Special IY flags
                if ((prefix == 0xFD) && (x != 0) && (calc.getCPU().getIY() == 0x89F0)
                        && (calc.getModel().ordinal() >= EnumCalcModel.TI_83P.ordinal()) && tiosDebug
                        && flagname[0] != null && bitname[0] != null) {

                    if (z == 6) {
                        result[resultIndex].setIndex(result[resultIndex].getIndex() + (DA_BIT_IF - DA_BIT));
                    } else {
                        result[resultIndex].setIndex(result[resultIndex].getIndex() + (DA_BIT_RF - DA_BIT));
                        // old register target receives the result
                        result[resultIndex].setA4(result[resultIndex].getA2());
                    }
                    result[resultIndex].setA1(bitname[0]);
                    result[resultIndex].setA2("iy");
                    result[resultIndex].setA3(flagname[0]);
                } else if (z == 6) {
                    result[resultIndex].setIndex(result[resultIndex].getIndex() + (DA_ROT_I - DA_ROT));
                    result[resultIndex].setA2(RI[pi]);
                    result[resultIndex].setA3(Integer.valueOf(offset));
                } else {
                    result[resultIndex].setIndex(result[resultIndex].getIndex() + (DA_ROT_R - DA_ROT));
                    result[resultIndex].setA4(result[resultIndex].getA2());
                    result[resultIndex].setA3(Integer.valueOf(offset));
                    result[resultIndex].setA2(RI[pi]);
                }
            }
        } else if (data == 0xED) {
            data = memc.wmemRead(curAddr);
            curAddr = getNextAddr(memc, type, curAddr);
            final int x = (data & 0xC0) >> 6;
            int y = (data & 0x38) >> 3;
            final int z = data & 0x07;
            final int p = (data & 0x30) >> 4;
            final int q = y & 1;

            if (x == 1) {
                if (z == 0) {
                    if (y == 6) {
                        y = 8;
                    }
                    result[resultIndex].setIndex(DA_IN_R_C_2);
                    result[resultIndex].setA1(R[y]);
                    result[resultIndex].setA2(R[R_C]);
                } else if (z == 1) {
                    if (y == 6) {
                        y = 8;
                    }
                    result[resultIndex].setIndex(DA_OUT_C_R2);
                    result[resultIndex].setA1(R[R_C]);
                    result[resultIndex].setA2(R[y]);
                } else if (z == 2) {
                    if (q == 0) {
                        result[resultIndex].setIndex(DA_SBC_HL_RP);
                    } else {
                        result[resultIndex].setIndex(DA_ADC_HL_RP);
                    }
                    result[resultIndex].setA1(RP[R_HL]);
                    result[resultIndex].setA2(RP[p]);
                } else if (z == 3) {
                    if (q == 0) {
                        result[resultIndex].setIndex(DA_LD_X_RP2);
                        result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                        curAddr = getNextAddr(memc, type, curAddr);
                        curAddr = getNextAddr(memc, type, curAddr);
                        result[resultIndex].setA2(RP[p]);
                    } else {
                        result[resultIndex].setIndex(DA_LD_RP_X_2);
                        result[resultIndex].setA1(RP[p]);
                        result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                        curAddr = getNextAddr(memc, type, curAddr);
                        curAddr = getNextAddr(memc, type, curAddr);
                    }
                } else if (z == 4) {
                    result[resultIndex].setIndex(DA_NEG);
                } else if (z == 5) {
                    if (y == 1) {
                        result[resultIndex].setIndex(DA_RETI);
                    } else {
                        result[resultIndex].setIndex(DA_RETN);
                    }
                } else if (z == 6) {
                    result[resultIndex].setIndex(DA_IM_X);
                    result[resultIndex].setA1(IM[y]);
                } else {
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_LD_I_A);
                            break;
                        case 1:
                            result[resultIndex].setIndex(DA_LD_R_A);
                            break;
                        case 2:
                            result[resultIndex].setIndex(DA_LD_A_I);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_LD_A_R);
                            break;
                        case 4:
                            result[resultIndex].setIndex(DA_RRD);
                            break;
                        case 5:
                            result[resultIndex].setIndex(DA_RLD);
                            break;
                        default:
                            result[resultIndex].setIndex(DA_NOP_ED);
                            break;
                    }
                }
            } else
                /* FOR X = 2 */
                if (x == 2) {
                    if (y >= 4) {
                        if (z < 4) {
                            result[resultIndex].setIndex(DA_BLI);
                            result[resultIndex].setA1(BLI[y - 4][z]);
                        }
                    } else {
                        result[resultIndex].setIndex(DA_NOP_ED);
                    }
                } else {
                    result[resultIndex].setIndex(DA_NOP_ED);
                }
        } else {
            final int x = (data & 0xC0) >> 6;
            final int y = (data & 0x38) >> 3;
            final int z = data & 0x07;
            final int p = (data & 0x30) >> 4;
            final int q = y & 1;

            final byte offset = (byte) (memc.wmemRead(curAddr));

            if (x == 0) {
                if (z == 0) {
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_NOP);
                            break;
                        case 1:
                            result[resultIndex].setIndex(DA_EX_AF_AF_2);
                            result[resultIndex].setA1(RP2[3]);
                            result[resultIndex].setA2("af'");
                            break;
                        case 2:
                            result[resultIndex].setIndex(DA_DJNZ_X);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_JR_X);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        default:
                            result[resultIndex].setIndex(DA_JR_CC_X);
                            result[resultIndex].setA1(CC[y - 4]);
                            result[resultIndex].setA2(Integer.valueOf(memc.wmemRead(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                    }
                } else if (z == 1) {
                    /* ix, iy ready */
                    if (q == 0) {
                        result[resultIndex].setIndex(DA_LD_RP_X);
                        result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                        curAddr = getNextAddr(memc, type, curAddr);
                        curAddr = getNextAddr(memc, type, curAddr);
                        if (prefix != 0 && p == 2) {
                            result[resultIndex].setA1(RI[pi]);
                        } else {
                            result[resultIndex].setA1(RP[p]);
                        }
                    } else if (prefix == 0) {
                        result[resultIndex].setIndex(DA_ADD_HL_RP);
                        result[resultIndex].setA1(RP[R_HL]);
                        result[resultIndex].setA2(RP[p]);
                    } else {
                        result[resultIndex].setIndex(DA_ADD_RI_RP);
                        result[resultIndex].setA1(RI[pi]);
                        result[resultIndex].setA2(RPI[pi][p]);
                    }
                } else if (z == 2) {
                    /* ix, iy ready */
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_LD_BC_A2);
                            result[resultIndex].setA1(RP[R_BC]);
                            result[resultIndex].setA2(R[R_A]);
                            break;
                        case 1:
                            result[resultIndex].setIndex(DA_LD_A_BC_2);
                            result[resultIndex].setA1(R[R_A]);
                            result[resultIndex].setA2(RP[R_BC]);
                            break;
                        case 2:
                            result[resultIndex].setIndex(DA_LD_DE_A2);
                            result[resultIndex].setA1(RP[R_DE]);
                            result[resultIndex].setA2(R[R_A]);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_LD_A_DE_2);
                            result[resultIndex].setA1(R[R_A]);
                            result[resultIndex].setA2(RP[R_DE]);
                            break;
                        case 4:
                            if (prefix == 0) {
                                result[resultIndex].setIndex(DA_LD_X_HL2);
                                result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                                curAddr = getNextAddr(memc, type, curAddr);
                                curAddr = getNextAddr(memc, type, curAddr);
                                result[resultIndex].setA2(RP[R_HL]);
                            } else {
                                result[resultIndex].setIndex(DA_LD_X_RI2);
                                result[resultIndex].setA2(RI[pi]);
                                result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                                curAddr = getNextAddr(memc, type, curAddr);
                                curAddr = getNextAddr(memc, type, curAddr);
                            }
                            break;
                        case 5:
                            if (prefix == 0) {
                                result[resultIndex].setIndex(DA_LD_HL_X_2);
                                result[resultIndex].setA1(RP[R_HL]);
                            } else {
                                result[resultIndex].setIndex(DA_LD_RI_X_2);
                                result[resultIndex].setA1(RI[pi]);
                            }
                            result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        case 6:
                            result[resultIndex].setIndex(DA_LD_X_A2);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            curAddr = getNextAddr(memc, type, curAddr);
                            result[resultIndex].setA2(R[R_A]);
                            break;
                        case 7:
                            result[resultIndex].setIndex(DA_LD_A_X_2);
                            result[resultIndex].setA1(R[R_A]);
                            result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        default:
                            LoggedObject.LOG.warning("Unhandled case");
                            break;
                    }
                } else if (z == 3) {
                    /* ix, iy ready */
                    result[resultIndex].setIndex((q == 0) ? DA_INC_RP : DA_DEC_RP);
                    result[resultIndex].setA1(prefix != 0 ? RPI[pi][p] : RP[p]);
                } else if (z < 6) {
                    /* ix, iy ready */
                    result[resultIndex].setIndex((z == 4) ? DA_INC_R : DA_DEC_R);
                    result[resultIndex].setA1(prefix != 0 ? R8I[pi][y] : R[y]);
                    if (result[resultIndex].getA1().equals(R[R_HL_2])) {
                        result[resultIndex].setIndex((z == 4) ? DA_INC_HL_2 : DA_DEC_HL_2);
                    }
                    if (prefix != 0 && y == 6) {
                        result[resultIndex]
                                .setIndex(result[resultIndex].getIndex() + (DA_INC_RI - DA_INC_R));
                        result[resultIndex].setA2(Integer.valueOf((int) offset));
                        curAddr = getNextAddr(memc, type, curAddr);
                    }
                } else if (z == 6) {
                    /* ix, iy ready */
                    result[resultIndex].setIndex(DA_LD_R_X);
                    result[resultIndex].setA1(prefix != 0 ? R8I[pi][y] : R[y]);
                    if (result[resultIndex].getA1().equals(R[R_HL_2])) {
                        result[resultIndex].setIndex(DA_LD_HL_X2);
                    }
                    if (prefix != 0 && y == 6) {
                        result[resultIndex].setIndex(DA_LD_RI_X);
                        result[resultIndex].setA2(Integer.valueOf((int) offset));
                        curAddr = getNextAddr(memc, type, curAddr);
                        result[resultIndex].setA3(Integer.valueOf(memc.wmemRead(curAddr)));
                    } else {
                        result[resultIndex].setA2(Integer.valueOf(memc.wmemRead(curAddr)));
                    }
                    curAddr = getNextAddr(memc, type, curAddr);
                } else { /* ix, iy ready */
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_RLCA);
                            break;
                        case 1:
                            result[resultIndex].setIndex(DA_RRCA);
                            break;
                        case 2:
                            result[resultIndex].setIndex(DA_RLA);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_RRA);
                            break;
                        case 4:
                            result[resultIndex].setIndex(DA_DAA);
                            break;
                        case 5:
                            result[resultIndex].setIndex(DA_CPL);
                            break;
                        case 6:
                            result[resultIndex].setIndex(DA_SCF);
                            break;
                        case 7:
                            result[resultIndex].setIndex(DA_CCF);
                            break;
                        default:
                            LoggedObject.LOG.warning("Unhandled case");
                            break;
                    }
                }
            } else if (x == 1) {
                /* ix, iy ready */
                if (z == 6 && y == 6) {
                    result[resultIndex].setIndex(DA_HALT);
                } else {
                    result[resultIndex].setIndex(DA_LD_R_R);
                    result[resultIndex].setA1(prefix != 0 ? R8I[pi][y] : R[y]);
                    if (result[resultIndex].getA1().equals(R[R_HL_2])) {
                        result[resultIndex].setIndex(DA_LD_HL_R2);
                    }
                    result[resultIndex].setA2(prefix != 0 ? R8I[pi][z] : R[z]);
                    if (result[resultIndex].getA2().equals(R[R_HL_2])) {
                        result[resultIndex].setIndex(DA_LD_R_HL_2);
                    }
                    if (prefix != 0) {
                        if (y == 6) {
                            curAddr = getNextAddr(memc, type, curAddr);
                            result[resultIndex].setIndex(DA_LD_RI_R);
                            result[resultIndex].setA1(RI[pi]);
                            result[resultIndex].setA3(R[z]);
                            result[resultIndex].setA2(Integer.valueOf((int) offset));
                        } else if (z == 6) {
                            curAddr = getNextAddr(memc, type, curAddr);
                            result[resultIndex].setA1(R[y]);
                            result[resultIndex].setA2(RI[pi]);
                            result[resultIndex].setIndex(DA_LD_R_RI);
                            result[resultIndex].setA3(Integer.valueOf((int) offset));
                        }
                    }
                }
            } else if (x == 2) {
                /* ix, iy ready */
                int which = 0;
                result[resultIndex].setA(which, ALU[y]);
                ++which;
                if (y == 0 || y == 1 || y == 3) {
                    result[resultIndex].setA(which, R[R_A]);
                    ++which;
                    result[resultIndex].setIndex(DA_ALU_A);
                } else {
                    result[resultIndex].setIndex(DA_ALU);
                }

                result[resultIndex].setA(which, prefix != 0 ? R8I[pi][z] : R[z]);
                ++which;
                if (prefix != 0 && z == 6) {
                    result[resultIndex].setIndex(result[resultIndex].getIndex() + DA_ALU_RI - DA_ALU);
                    result[resultIndex].setA(which, Integer.valueOf((int) offset));
                    ++which;
                    curAddr = getNextAddr(memc, type, curAddr);
                }
            } else { // (x == 3)
                if (z == 0) {
                    result[resultIndex].setIndex(DA_RET_CC);
                    result[resultIndex].setA1(CC[y]);
                } else if (z == 1) {
                    if (q == 0) {
                        result[resultIndex].setIndex(DA_POP_RP);
                        result[resultIndex].setA1(prefix != 0 ? RP2I[pi][p] : RP2[p]);
                    } else {
                        switch (p) {
                            case 0:
                                result[resultIndex].setIndex(DA_RET);
                                break;
                            case 1:
                                result[resultIndex].setIndex(DA_EXX);
                                break;
                            case 2:
                                result[resultIndex].setIndex(prefix != 0 ? DA_JP_RI : DA_JP_HL);
                                result[resultIndex].setA1(prefix != 0 ? RI[pi] : RP[R_HL]);
                                break;
                            case 3:
                                result[resultIndex].setIndex(DA_LD_SP_HL);
                                result[resultIndex].setA1(RP[R_SP]);
                                result[resultIndex].setA2(prefix != 0 ? RI[pi] : RP[R_HL]);
                                break;
                            default:
                                LoggedObject.LOG.warning("Unhandled case");
                                break;
                        }
                    }
                } else if (z == 2) {
                    result[resultIndex].setIndex(DA_JP_CC_X);
                    result[resultIndex].setA1(CC[y]);
                    result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                    curAddr = getNextAddr(memc, type, curAddr);
                    curAddr = getNextAddr(memc, type, curAddr);
                } else if (z == 3) {
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_JP_X);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        case 2:
                            result[resultIndex].setIndex(DA_OUT_X_A2);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            result[resultIndex].setA2(R[R_A]);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_IN_A_X_2);
                            result[resultIndex].setA1(R[R_A]);
                            result[resultIndex].setA2(Integer.valueOf(memc.wmemRead(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            break;
                        case 4:
                            result[resultIndex].setIndex(prefix != 0 ? DA_EX_SP_RI2 : DA_EX_SP_HL2);
                            result[resultIndex].setA1(RP[R_SP]);
                            result[resultIndex].setA2(prefix != 0 ? RI[pi] : RP[R_HL]);
                            break;
                        case 5:
                            result[resultIndex].setIndex(DA_EX_DE_HL);
                            result[resultIndex].setA1(RP[R_DE]);
                            result[resultIndex].setA2(RP[R_HL]);
                            break;
                        case 6:
                            result[resultIndex].setIndex(DA_DI);
                            break;
                        case 7:
                            result[resultIndex].setIndex(DA_EI);
                            break;
                        default:
                            break;
                    }
                } else if (z == 4) {
                    result[resultIndex].setIndex(DA_CALL_CC_X);
                    result[resultIndex].setA1(CC[y]);
                    result[resultIndex].setA2(Integer.valueOf(memc.wmemRead16(curAddr)));
                    curAddr = getNextAddr(memc, type, curAddr);
                    curAddr = getNextAddr(memc, type, curAddr);
                } else if (z == 5) {
                    if (q == 0) {
                        result[resultIndex].setIndex(DA_PUSH_RP);
                        result[resultIndex].setA1(prefix != 0 ? RP2I[pi][p] : RP2[p]);
                    } else {
                        if (p == 0) {
                            result[resultIndex].setIndex(DA_CALL_X);
                            result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                            curAddr = getNextAddr(memc, type, curAddr);
                            curAddr = getNextAddr(memc, type, curAddr);

                            if (result[resultIndex].getA1().equals(Integer.valueOf(0x0050)) && tiosDebug) {

                                result[resultIndex].setIndex(DA_BJUMP);
                                result[resultIndex].setA1(Integer.valueOf(memc.wmemRead16(curAddr)));
                                curAddr = getNextAddr(memc, type, curAddr);
                                curAddr = getNextAddr(memc, type, curAddr);
                                final Object a1obj = result[resultIndex].getA1();
                                if (a1obj instanceof Integer) {
                                    final String name = BCall.findBcall(((Integer) a1obj).intValue(), calc.getModel());
                                    if (name == null) {
                                        result[resultIndex].setIndex(DA_BJUMP_N);
                                    } else {
                                        result[resultIndex].setA1(name);
                                    }
                                } else {
                                    result[resultIndex].setIndex(DA_BJUMP_N);
                                }
                            }
                        }
                    }
                } else if (z == 6) {
                    switch (y) {
                        case 0:
                            result[resultIndex].setIndex(DA_ADD_HL_RP);
                            result[resultIndex].setA1(R[R_A]);
                            break;
                        case 1:
                            result[resultIndex].setIndex(DA_ADC_HL_RP);
                            result[resultIndex].setA1(R[R_A]);
                            break;
                        case 3:
                            result[resultIndex].setIndex(DA_SBC_HL_RP);
                            result[resultIndex].setA1(R[R_A]);
                            break;
                        default:
                            result[resultIndex].setIndex(DA_ALU);
                            result[resultIndex].setA1(ALU[y]);
                            break;
                    }
                    // Does this not just undo what was done in the switch above?
                    result[resultIndex].setIndex(DA_ALU_X);
                    result[resultIndex].setA1(ALU[y]);
                    result[resultIndex].setA2(Integer.valueOf(memc.wmemRead(curAddr)));
                    curAddr = getNextAddr(memc, type, curAddr);
                } else { // (z == 7)
                    if (y == 5 && tiosDebug) {
                        result[resultIndex].setIndex(DA_BCALL);
                        final int tmp = memc.wmemRead16(curAddr);
                        curAddr = getNextAddr(memc, type, curAddr);
                        curAddr = getNextAddr(memc, type, curAddr);
                        final String name = BCall.findBcall(tmp, calc.getModel());
                        if (name == null) {
                            result[resultIndex].setIndex(DA_BCALL_N);
                            result[resultIndex].setA1(Integer.valueOf(tmp));
                        } else {
                            result[resultIndex].setA1(name);
                        }
                    } else {
                        result[resultIndex].setIndex(DA_RST_X);
                        result[resultIndex].setA1(Integer.valueOf(y << 3));
                    }
                }
            }
        }

        final int size = Math.abs((curAddr.getAddr() - startAddr.getAddr()) & 0xFF);
        result[resultIndex].setSize(size);

        // Collect and store the instruction data
        final int[] opcodedata = new int[size];
        for (int pos = 0; pos < size; ++pos) {
            curAddr = new WideAddr(curAddr.getPage(), startAddr.getAddr() + pos, curAddr.isRam());
            opcodedata[pos] = memc.wmemRead(curAddr);
        }
        result[resultIndex].setOpcodeData(opcodedata);
        curAddr = new WideAddr(curAddr.getPage(), startAddr.getAddr() + size, curAddr.isRam());

        Object modA1 = result[resultIndex].getA1();
        Object modA2 = result[resultIndex].getA2();

        result[resultIndex].setClocks(Z80Com.DA_OPCODE[result[resultIndex].getIndex()].getClocks());

        // Expand the format
        final String in = Z80Com.DA_OPCODE[result[resultIndex].getIndex()].getFormat();
        final StringBuilder out = new StringBuilder(32);
        int inOffset = 0;

        while (inOffset < in.length()) {
            if (in.charAt(inOffset) == '%') {

                switch (in.charAt(inOffset + 1)) {
                    case 'g':
                        final int addr = result[resultIndex].getWaddr().getAddr() + 2;
                        if (result[resultIndex].getIndex() == DA_JR_CC_X) {
                            final Object a2obj = result[resultIndex].getA2();
                            if (a2obj instanceof Integer) {
                                final int a2val = ((Integer) a2obj).intValue();
                                modA2 = Integer.valueOf((addr + a2val) & 0x0000FFFF);
                            }
                        } else {
                            final Object a1obj = result[resultIndex].getA1();
                            if (a1obj instanceof Integer) {
                                final int a1val = ((Integer) a1obj).intValue();
                                modA1 = Integer.valueOf((addr + a1val) & 0x0000FFFF);
                            }
                        }
                        //$FALL-THROUGH$ fall through
                    case 'a':
                        out.append("$%04X");
                        inOffset += 2;
                        break;
                    case 'h':
                        out.append("%+d");
                        inOffset += 2;
                        break;
                    case 'r':
                    case 'c':
                    case 'l':
                        out.append(in.charAt(inOffset));
                        ++inOffset;
                        out.append('s');
                        ++inOffset;
                        break;
                    case 'x':
                        out.append("$%02X");
                        inOffset += 2;
                        break;
                    default:
                        out.append(in.charAt(inOffset));
                        ++inOffset;
                        out.append(in.charAt(inOffset));
                        ++inOffset;
                        break;
                }
            } else {
                out.append(in.charAt(inOffset));
                ++inOffset;
            }
        }

        final StringBuilder sb = new StringBuilder(100);
        try (final Formatter formatter = new Formatter(sb, Locale.getDefault())) {
            formatter.format(out.toString(), modA1, modA2, result[resultIndex].getA3(), result[resultIndex].getA4());
        }
        result[resultIndex].setExpanded(sb.toString());

        waddr[0] = curAddr;
        return resultIndex - start + 1;
    }
}
