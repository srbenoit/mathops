package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import dev.mathops.core.CoreConstants;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.MemoryContext;
import jwabbit.hardware.HardwareConstants;
import jwabbit.log.LoggedObject;

/** Methods from "state.c". */
public final class CalcState {

    /** WABBITEMU SOURCE: interface/state.h, "PTEMP_83P" macro. */
    private static final int PTEMP_83P = 0x0000982E;

    /** WABBITEMU SOURCE: interface/state.h, "PROGPTR_83P" macro. */
    private static final int PROGPTR_83P = 0x00009830;

    /** WABBITEMU SOURCE: interface/state.h, "SYMTABLE_83P" macro. */
    private static final int SYMTABLE_83P = 0x0000FE66;

    /** WABBITEMU SOURCE: interface/state.h, "PTEMP_84PCSE" macro. */
    private static final int PTEMP_84PCSE = 0x00009E0F;

    /** WABBITEMU SOURCE: interface/state.h, "PROGPTR_84PCSE" macro. */
    private static final int PROGPTR_84PCSE = 0x00009E11;

    /** WABBITEMU SOURCE: interface/state.h, "SYMTABLE_84PCSE" macro. */
    private static final int SYMTABLE_84PCSE = 0x0000FD9E;

    /** WABBITEMU SOURCE: interface/state.h, "tAns" macro (ASCII lowercase r). */
    private static final int T_ANS = 0x72;

    /** The prior state. */
    private static EnumKeypadState priorState = EnumKeypadState.NORMAL;

    /** The number of consecutive polls in 'normal' state. */
    private static int numNormal;

    /**
     * Constructs a new {@code CalcState}.
     */
    public CalcState() {

        super();
    }

    /**
     * WABBITEMU SOURCE: interface/state.h, "circ10" macro.
     *
     * @param z the input number
     * @return the converted number
     */
    private static int circ10(final int z) {

        return (z < 10) ? (z + 1) % 10 : z;
    }

    /**
     * WABBITEMU SOURCE: interface/state.c, "state_build_symlist_83P" function.
     *
     * @param cpu     the CPU
     * @param symlist the symbol list
     * @return the symbol list
     */
    private static SymList stateBuildSymlist83P(final CPU cpu, final SymList symlist) {

        if (symlist == null) {
            return null;
        }

        final MemoryContext mem = cpu.getMemoryContext();
        final int modelOrd = cpu.getPIOContext().getModel().ordinal();

        final int pTemp = modelOrd >= EnumCalcModel.TI_84PCSE.ordinal() ? PTEMP_84PCSE : PTEMP_83P;
        final int progPtr = modelOrd >= EnumCalcModel.TI_84PCSE.ordinal() ? PROGPTR_84PCSE : PROGPTR_83P;
        final int symTable = modelOrd >= EnumCalcModel.TI_84PCSE.ordinal() ? SYMTABLE_84PCSE : SYMTABLE_83P;

        final int end = mem.memRead16(pTemp);
        final int prog = mem.memRead16(progPtr);
        int stp = symTable;

        // Verify VAT integrity
        if (modelOrd < EnumCalcModel.TI_83P.ordinal()) {
            LoggedObject.LOG.warning("Model lower than 83+");
            return null;
        }
        if (stp < end || stp < prog) {
            LoggedObject.LOG.warning("1 stp = " + stp + ", end = " + end + ", prog = " + prog);
            return null;
        }
        if (end > prog || end < 0x00009D95) {
            LoggedObject.LOG.warning("2 stp = " + stp + ", end = " + end + ", prog = " + prog);
            return null;
        }

        symlist.setCount(0);

        // Loop through while stp is still in the symbol table
        int index = 0;
        while (stp > end && stp > 0x0000C000 && index < 2048) {
            final Symbol83p sym = symlist.getSymbol(index);

            sym.setTypeID(mem.memRead(stp) & 0x1F);
            --stp;
            sym.setTypeID2(mem.memRead(stp));
            --stp;
            sym.setVersion(mem.memRead(stp));
            --stp;
            sym.setAddress(mem.memRead(stp));
            --stp;
            sym.setAddress(sym.getAddress() + (mem.memRead(stp) << 8));
            --stp;
            sym.setPage(mem.memRead(stp));
            --stp;
            sym.setLength(mem.memRead(sym.getAddress() - 1) + (mem.memRead(sym.getAddress()) << 8));

            if (stp > prog) {
                final char[] namechars = new char[3];
                for (int i = 0; i < 3; ++i) {
                    namechars[i] = (char) mem.memRead(stp);
                    --stp;
                }

                if (namechars[0] == 0) {
                    sym.setName("");
                } else if (namechars[1] == 0) {
                    sym.setName(new String(namechars, 0, 1));
                } else if (namechars[2] == 0) {
                    sym.setName(new String(namechars, 0, 2));
                } else {
                    sym.setName(new String(namechars));
                }
                symlist.setProgramsIndex(index + 1);
            } else {
                final int nameLen = mem.memRead(stp);
                --stp;
                if (nameLen == 0) {
                    sym.setName("");
                } else {
                    final char[] namechars = new char[nameLen];
                    for (int i = 0; i < nameLen; ++i) {
                        namechars[i] = (char) mem.memRead(stp);
                        --stp;
                    }
                    sym.setName(new String(namechars));
                }
                symlist.setLastIndex(index);
            }

            final String symName = symbolNameToString(cpu.getPIOContext().getModel(), sym);
            if (symName == null) {
                continue;
            }

            symlist.setCount(symlist.getCount() + 1);

            ++index;
        }

        return symlist;
    }

    /**
     * Find a symbol in a symlist. Provide a name and name length and the symbol, if found is returned. Otherwise,
     * return null.
     *
     * <p>
     * WABBITEMU SOURCE: interface/state.c, "search_symlist" function.
     *
     * @param symlist the symbol list
     * @param name    the name for which to search
     * @return the symbol if found; null if not
     */
    private static Symbol83p searchSymlist(final SymList symlist, final String name) {

        for (int i = 0; i <= symlist.getLastIndex(); ++i) {
            if (symlist.getSymbol(i).getName().equals(name)) {
                return symlist.getSymbol(i);
            }
        }
        return null;
    }

    /**
     * WABBITEMU SOURCE: interface/state.c, "Symbol_Name_to_String" function.
     *
     * @param model the model
     * @param sym   the symbol
     * @return the symbol name
     */
    private static String symbolNameToString(final EnumCalcModel model, final Symbol83p sym) {

        final char[] ansName = {T_ANS};

        if (sym.getName().equals(new String(ansName))) {
            return "Ans";
        }

        if (model == EnumCalcModel.TI_86) {
            return sym.getName();
        }

        switch (sym.getTypeID()) {
            case HardwareConstants.PROG_OBJ:
            case HardwareConstants.PROT_PROG_OBJ:
            case HardwareConstants.APP_VAR_OBJ:
            case HardwareConstants.GROP_OBJ:
                return sym.getName();

            case HardwareConstants.PICT_PBJ:
                return "Pic" + circ10(sym.getName().charAt(1));

            case HardwareConstants.GDB_OBJ:
                return "GDB" + circ10(sym.getName().charAt(1));

            case HardwareConstants.STRNG_OBJ:
                return "Str" + circ10(sym.getName().charAt(1));

            case HardwareConstants.REAL_OBJ:
            case HardwareConstants.CPLX_OBJ:
                return sym.getName().isEmpty() ? null : Character.toString(sym.getName().charAt(0));

            case HardwareConstants.LIST_OBJ:
            case HardwareConstants.C_LIST_OBJ:
                if (sym.getName().length() > 1 && sym.getName().charAt(1) < 6) {
                    return "L" + ((int) sym.getName().charAt(1) + 1);
                } else if (!sym.getName().isEmpty()) {
                    return sym.getName().substring(1);
                } else {
                    return null;
                }

            case HardwareConstants.MAT_OBJ:
                if (sym.getName().charAt(0) == 0x5C && sym.getName().length() > 1) {
                    return "[" + (char) ((int) 'A' + (int) sym.getName().charAt(1)) + "]";
                }
                return null;

            case HardwareConstants.EQU_OBJ_2:
                if (sym.getName().charAt(0) != 0x5E) {
                    return null;
                }

                final int b = (int) sym.getName().charAt(1) & 0x0F;
                switch ((int) sym.getName().charAt(1) & 0xF0) {
                    case 0x10: // Y1
                        return "Y" + circ10(b);

                    case 0x20: // X1t Y1t
                        if ((b % 2) == 0) {
                            return "X" + ((b / 2) + 1) % 6 + "T";
                        }
                        return "Y" + ((b / 2) + 1) % 6 + "T";

                    case 0x40: // r1
                        return "R" + (b + 1) % 6;

                    case 0x80: // Y1
                        switch (b) {
                            case 0:
                                return "Un";
                            case 1:
                                return "Vn";
                            case 2:
                                return "Wn";
                            default:
                                LoggedObject.LOG.warning("Unhandled b");
                                break;
                        }
                        //$FALL-THROUGH$ fall through
                    default:
                        return null;
                } // fall through

            default:
                return null;
        }
    }

    /**
     * WABBITEMU SOURCE: interface/state.c, "GetRealAns" function.
     *
     * @param cpu the CPU
     * @return the symbol name
     */
    public static String getRealAns(final CPU cpu) {

        SymList symlist = new SymList();

        symlist = stateBuildSymlist83P(cpu, symlist);
        if (symlist == null) {
            return null;
        }

        final String ansName = new String(new char[]{T_ANS});
        final Symbol83p sym = searchSymlist(symlist, ansName);
        if (sym == null) {
            return null;
        }

        return symbolToString(cpu, sym);
    }

    /**
     * Gets the current keypad state.
     *
     * @param calc the calculator
     * @return the state of the keypad
     */
    public static EnumKeypadState getKeypadState(final Calc calc) {

        final int addr;

        // Add remaining supported models

        if (calc.getModel() == EnumCalcModel.TI_84PSE || calc.getModel() == EnumCalcModel.TI_84P
                || calc.getModel() == EnumCalcModel.TI_83PSE
                || calc.getModel() == EnumCalcModel.TI_83P) {
            addr = 0x8A02;
        } else if (calc.getModel() == EnumCalcModel.TI_84PCSE) {
            addr = 0x8B38;
        } else if (calc.getModel() == EnumCalcModel.TI_73) {
            addr = 0x856D;
        } else if (calc.getModel() == EnumCalcModel.TI_82) {
            addr = 0x853A;
        } else if (calc.getModel() == EnumCalcModel.TI_85) {
            addr = 0x8358;
        } else if (calc.getModel() == EnumCalcModel.TI_86) {
            addr = 0xC3F7;
        } else {
            // Unable to determine state
            addr = -1;
        }

        // : 85 and 86 cut off the right side of the screen

        // Only change state to "NORMAL" if we have seen "NORMAL" for 10 consecutive polls
        final EnumKeypadState state;

        if (addr == -1) {
            state = EnumKeypadState.NORMAL;
        } else {
            final MemoryContext mem = calc.getCPU().getMemoryContext();
            final int byt = mem.memRead(addr);

            // Bit 0x08 represents 2ND
            // Bit 0x10 represents ALPHA
            if ((byt & 0x08) == 0x08) {
                state = EnumKeypadState.SECOND;
                numNormal = 0;
            } else if ((byt & 0x10) == 0x10) {
                state = EnumKeypadState.ALPHA;
                numNormal = 0;
            } else {
                ++numNormal;
                if (numNormal > 10) {
                    state = EnumKeypadState.NORMAL;
                } else {
                    state = priorState;
                }
            }

            priorState = state;
        }

        return state;
    }

    /**
     * Print a symbol's value to a string.
     *
     * <p>
     * WABBITEMU SOURCE: interface/state.c, "symbol_to_string" function.
     *
     * @param cpu the CPU
     * @param sym the symbol
     * @return the symbol name
     */
    private static String symbolToString(final CPU cpu, final Symbol83p sym) {

        int i;
        final Symbol83p elem;
        int ptr;

        switch (sym.getTypeID()) {
            case HardwareConstants.CPLX_OBJ:
            case HardwareConstants.REAL_OBJ:
                ptr = sym.getAddress();
                final StringBuilder str1 = new StringBuilder(30);

                // NOTE: original code had "TI_num_extract" label here and goto below. I have copied
                // this code again in the "goto" case since it can be executed at most once.
                final int type = cpu.getMemoryContext().memRead(ptr);
                ++ptr;
                int exp = cpu.getMemoryContext().memRead(ptr) ^ 0x80;
                ++ptr;
                if (exp == -128) {
                    exp = 128;
                }

                final int[] fp = new int[14];
                int sigdigs = 1;

                for (i = 0; i < 14; i += 2, ++ptr) {
                    final int nextByte = cpu.getMemoryContext().memRead(ptr);
                    fp[i] = nextByte >> 4;
                    fp[i + 1] = nextByte & 0x0F;
                    if (fp[i] != 0) {
                        sigdigs = i + 1;
                    }

                    if (fp[i + 1] != 0) {
                        sigdigs = i + 2;
                    }
                }

                if ((type & 0x80) != 0) {
                    str1.append('-');
                }

                if (Math.abs(exp) > 14) {
                    for (i = 0; i < sigdigs; ++i) {
                        str1.append((char) (fp[i] + (int) '0'));
                        if ((i + 1) < sigdigs && i == 0) {
                            str1.append('.');
                        }
                    }

                    str1.append("*10^");
                    str1.append(exp);
                } else {
                    for (i = Math.min(exp, 0); i < sigdigs || i < (exp + 1); ++i) {
                        if (i >= 0) {
                            str1.append((char) (fp[i] + (int) '0'));
                        } else {
                            str1.append('0');
                        }
                        if ((i + 1) < sigdigs && i == exp) {
                            str1.append('.');
                        }
                    }
                }

                if ((type & 0x0F) == 0x0C) {
                    final int type2 = cpu.getMemoryContext().memRead(ptr);
                    ++ptr;
                    exp = cpu.getMemoryContext().memRead(ptr) ^ 0x80;
                    ++ptr;
                    if (exp == -128) {
                        exp = 128;
                    }

                    sigdigs = 1;

                    for (i = 0; i < 14; i += 2, ++ptr) {
                        final int nextByte = cpu.getMemoryContext().memRead(ptr);
                        fp[i] = nextByte >> 4;
                        fp[i + 1] = nextByte & 0x0F;
                        if (fp[i] != 0) {
                            sigdigs = i + 1;
                        }

                        if (fp[i + 1] != 0) {
                            sigdigs = i + 2;
                        }
                    }

                    if ((type2 & 0x80) == 0) {
                        str1.append('+');
                    } else {
                        str1.append('-');
                    }

                    if (Math.abs(exp) > 14) {
                        for (i = 0; i < sigdigs; ++i) {
                            str1.append((char) (fp[i] + (int) '0'));
                            if ((i + 1) < sigdigs && i == 0) {
                                str1.append('.');
                            }
                        }

                        str1.append("*10^");
                        str1.append(exp);
                    } else {
                        for (i = Math.min(exp, 0); i < sigdigs || i < (exp + 1); ++i) {
                            if (i >= 0) {
                                str1.append((char) (fp[i] + (int) '0'));
                            } else {
                                str1.append('0');
                            }
                            if ((i + 1) < sigdigs && i == exp) {
                                str1.append('.');
                            }
                        }
                    }

                    str1.append('i');
                }

                return str1.toString();

            case HardwareConstants.C_LIST_OBJ:
            case HardwareConstants.LIST_OBJ:
                elem = new Symbol83p();

                // A false symbol for each element of the array
                elem.setTypeID(sym.getTypeID() - 1);
                elem.setPage(0);

                final int size = cpu.getMemoryContext().memRead16(sym.getAddress());
                final StringBuilder str2 = new StringBuilder(30);
                str2.append('{');

                for (i = 0, ptr = sym.getAddress() + 2; i < size; ++i) {
                    if (i != 0) {
                        str2.append(CoreConstants.COMMA_CHAR);
                    }
                    elem.setAddress(ptr);
                    str2.append(symbolToString(cpu, elem));
                    ptr += (elem.getTypeID() == HardwareConstants.REAL_OBJ) ? 9 : 18;
                }

                str2.append('}');
                return str2.toString();

            case HardwareConstants.MAT_OBJ:
                elem = new Symbol83p();

                elem.setTypeID(HardwareConstants.REAL_OBJ);
                elem.setPage(0);

                final int cols = cpu.getMemoryContext().memRead(sym.getAddress());
                final int rows = cpu.getMemoryContext().memRead(sym.getAddress() + 1);
                final StringBuilder str3 = new StringBuilder(30);

                str3.append('[');
                ptr = sym.getAddress() + 2;
                for (int j = 0; j < rows; ++j) {
                    if (j != 0) {
                        str3.append(' ');
                    }

                    str3.append('[');
                    for (i = 0; i < cols; i++, ptr += 9) {
                        if (i != 0) {
                            str3.append(CoreConstants.COMMA_CHAR);
                        }
                        elem.setAddress(ptr);
                        str3.append(symbolToString(cpu, elem));
                    }
                    str3.append(']');
                    if (j + 1 < rows) {
                        str3.append(CoreConstants.CRLF);
                    }
                }

                str3.append(']');
                return str3.toString();

            case HardwareConstants.STRNG_OBJ:
                // There's a problem here with character set conversions
                ptr = sym.getAddress();
                int strLen = cpu.getMemoryContext().memRead(ptr);
                ++ptr;
                strLen += cpu.getMemoryContext().memRead(ptr) << 8;
                ++ptr;
                final StringBuilder str4 = new StringBuilder(30);
                for (i = 0; i < strLen; ++i) {
                    str4.append((char) cpu.getMemoryContext().memRead(ptr));
                    ++ptr;
                }
                return str4.toString();

            default:
                return "unsupported";
        }
    }
}
