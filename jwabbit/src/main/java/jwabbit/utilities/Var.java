package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.log.LoggedObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * A var.
 */
public final class Var {

    /** WABBITEMU SOURCE: utilities/var.c, "self_test" array. */
    private static final String SELF_TEST = "Self Test?";

    /** WABBITEMU SOURCE: utilities/var.c, "catalog" array. */
    private static final String CATALOG = "CATALOG";

    /** WABBITEMU SOURCE: utilities/var.c, "txt73" array. */
    private static final String TXT73 = "GRAPH  EXPLORER  SOFTWARE";

    /** WABBITEMU SOURCE: utilities/var.c, "txt86" array. */
    private static final String TXT86 = "Already Installed";

    /**
     * Constructs a new {@code Var}.
     */
    public Var() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "CmpStringCase" function.
     *
     * @param str1   the first string
     * @param str2   the second string
     * @param start2 the start offset for the comparison in second string
     * @return true if strings are match
     */
    private static boolean cmpStringCase(final String str1, final int[] str2, final int start2) {

        final int len = str1.length();
        final char[] chars = new char[len];
        for (int i = 0; i < len; ++i) {
            chars[i] = (char) str2[i + start2];
        }

        return str1.equalsIgnoreCase(new String(chars));
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "FindRomVersion" function.
     *
     * @param result a string array into which to store the result
     * @param rom    the ROM data
     * @param size   the size of the ROM data
     * @return the model determined from the ROM
     */
    public static EnumCalcModel findRomVersion(final String[] result, final int[] rom, final int size) {

        EnumCalcModel calc;
        final int numChars = 32;

        final char[] chars = new char[numChars];

        if (size == (32 << 10)) {
            calc = EnumCalcModel.TI_81;
        } else if (size == (128 << 10)) {
            calc = EnumCalcModel.TI_82;
        } else if (size == (256 << 10)) {
            calc = EnumCalcModel.TI_83;
        } else if ((size >= (510 << 10)) && (size <= (590 << 10))) {
            calc = EnumCalcModel.TI_83P;
        } else if ((size >= (1016 << 10)) && (size <= (1030 << 10))) {
            calc = EnumCalcModel.TI_84P;
        } else if ((size >= (2044 << 10)) && (size <= (2260 << 10))) {
            calc = EnumCalcModel.TI_83PSE;
        } else if ((size >= (4090 << 10)) && (size <= (4100 << 10))) {
            calc = EnumCalcModel.TI_84PCSE;
        } else {
            LoggedObject.LOG.warning("Not a known ROM");
            return EnumCalcModel.INVALID_MODEL;
        }

        int i;
        int b;
        switch (calc) {
            case TI_81:
                // 1.1k doesn't ld a,* first
                if (rom[0] == 0xC3) {
                    chars[0] = '1';
                    chars[1] = '.';
                    chars[2] = '1';
                    chars[3] = 'K';
                    chars[4] = 0;
                } else if (rom[1] == 0x17) {
                    // 2.0V has different val to load
                    chars[0] = '2';
                    chars[1] = '.';
                    chars[2] = '0';
                    chars[3] = 'V';
                    chars[4] = 0;
                } else if (rom[5] == 0x09) {
                    // 1.6K outs a 0x09
                    chars[0] = '1';
                    chars[1] = '.';
                    chars[2] = '6';
                    chars[3] = 'K';
                    chars[4] = 0;
                } else {
                    // assume its a 1.8K for now
                    chars[0] = '1';
                    chars[1] = '.';
                    chars[2] = '8';
                    chars[3] = 'K';
                    chars[4] = 0;
                }
                break;

            case TI_82:
                final int catalogLen = CATALOG.length();
                for (i = 0; i < (size - catalogLen - 10); ++i) {
                    if (cmpStringCase(CATALOG, rom, i)) {
                        calc = EnumCalcModel.TI_85;
                        for (i = 0; i < (size - SELF_TEST.length() - 10); ++i) {
                            if (cmpStringCase(SELF_TEST, rom, i)) {
                                break;
                            }
                        }
                        for (; i < (size - 40); ++i) {
                            if (Character.isDigit((char) rom[i])) {
                                break;
                            }
                        }
                        if (i < (size - 40)) {
                            for (b = 0; (b + i) < (size - 4) && b < 32; ++b) {
                                if (rom[b + i] == ' ') {
                                    chars[b] = 0;
                                } else {
                                    chars[b] = (char) rom[b + i];
                                }
                            }
                            chars[31] = 0;
                        } else {
                            chars[0] = '?';
                            chars[1] = '?';
                            chars[2] = '?';
                            chars[3] = 0;
                        }
                        break;
                    }
                }
                if (calc != EnumCalcModel.TI_82) {
                    break;
                }
                //$FALL-THROUGH$ fall through

            case TI_83:
                final int txt86Len = TXT86.length();
                for (i = 0; i < (size - txt86Len - 10); ++i) {
                    if (cmpStringCase(TXT86, rom, i)) {
                        calc = EnumCalcModel.TI_86;
                        final int selfTestLen = SELF_TEST.length();
                        for (i = 0; i < size - selfTestLen - 10; ++i) {
                            if (cmpStringCase(SELF_TEST, rom, i)) {
                                break;
                            }
                        }
                        for (; i < size - 40; ++i) {
                            if (Character.isDigit((char) rom[i])) {
                                break;
                            }
                        }
                        if (i < size - 40) {
                            for (b = 0; (b + i) < (size - 4) && b < 32; ++b) {
                                if (rom[b + i] != ' ') {
                                    chars[b] = (char) rom[b + i];
                                } else {
                                    chars[b] = 0;
                                }
                            }
                            chars[31] = 0;
                        } else {
                            chars[0] = '?';
                            chars[1] = '?';
                            chars[2] = '?';
                            chars[3] = 0;
                        }
                        break;
                    }
                }

                if (calc == EnumCalcModel.TI_86) {
                    break;
                }

                final int selfTestLen = SELF_TEST.length();
                for (i = 0; i < (size - selfTestLen - 10); ++i) {
                    if (cmpStringCase(SELF_TEST, rom, i)) {
                        break;
                    }
                }
                if ((i + 64) < size) {
                    i += 10;
                    for (b = 0; b < 32; ++b) {
                        chars[b] = (char) rom[i];
                        ++i;
                    }
                    chars[31] = 0;
                } else {
                    chars[0] = '?';
                    chars[1] = '?';
                    chars[2] = '?';
                    chars[3] = 0;
                }
                break;

            case TI_83P:
                final int txt73Len = TXT73.length();
                for (i = 0; i < (size - txt73Len - 10); ++i) {
                    if (cmpStringCase(TXT73, rom, i)) {
                        calc = EnumCalcModel.TI_73;
                        break;
                    }
                }
                //$FALL-THROUGH$ fall through

            case TI_84P:
            case TI_83PSE:
            case TI_84PCSE:
                i = 0x0064;
                if (calc == EnumCalcModel.TI_84PCSE) {
                    ++i;
                }
                for (b = 0; b < 32; ++b) {
                    chars[b] = (char) rom[i];
                    ++i;
                }
                chars[31] = 0;
                if (calc == EnumCalcModel.TI_83PSE) {
                    if (chars[0] > '1') {
                        calc = EnumCalcModel.TI_84PSE;
                    }
                }
                break;

            default:
                break;
        }

        for (int k = 0; k < numChars; ++k) {
            if (chars[k] == 0) {
                result[0] = new String(chars, 0, k);
                break;
            }
        }

        return calc;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ReadIntelHex" function.
     *
     * @param ifile the input stream from which to read
     * @param ihex  the INTELHEX object to populate
     * @return 1 if successful; 0 if failure
     */
    public static int readIntelHex(final FileInputStream ifile, final IntelHex ihex) {

        final char[] chars = new char[600];
        int numRead = 0;

        try {
            // read until EOF or a newline is reached, up to 580 bytes
            for (int i = 0; i < 580; ++i) {
                final int byteRead = ifile.read();
                if (byteRead == -1) {
                    LoggedObject.LOG.warning("Failed to read first line INTELHEX");
                    return 0;
                } else {
                    chars[i] = (char) byteRead;
                    if (chars[i] == '\n') {
                        break;
                    }
                    ++numRead;
                }
            }
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to read first line INTELHEX", ex);
            return 0;
        }

        // Ignore a leading null byte
        final String strstr;
        if (chars[0] == 0) {
            strstr = new String(chars, 1, numRead - 1);
        } else {
            strstr = new String(chars, 0, numRead);
        }

        if (strstr.charAt(0) != ':') {
            LoggedObject.LOG.warning("Missing colon at start of INTELHEX first line: ", strstr);
            return 0;
        }

        final int size;
        final int addr;
        final int type;
        try {
            size = Integer.parseInt(strstr.substring(1, 3), 16);
            addr = Integer.parseInt(strstr.substring(3, 7), 16);
            type = Integer.parseInt(strstr.substring(7, 9), 16);
        } catch (final NumberFormatException ex) {
            LoggedObject.LOG.warning("Failed to parse header in INTELHEX line: ", strstr, ex);
            return 0;
        }

        ihex.setDataSize(size);
        ihex.setAddress(addr);
        ihex.setType(type);

        Arrays.fill(ihex.getData(), 0);

        int i;
        for (i = 0; i < size; ++i) {
            try {
                ihex.getData()[i] = Integer.parseInt(strstr.substring(9 + (i << 1), 11 + (i << 1)), 16);
            } catch (final NumberFormatException ex) {
                LoggedObject.LOG.warning("Failed to parse data in INTELHEX line: ", strstr, ex);
                return 0;
            }
        }
        try {
            final int byt = Integer.parseInt(strstr.substring(9 + (i << 1), 11 + (i << 1)), 16);
            IntelHex.setCheckSum(byt);
        } catch (final NumberFormatException ex) {
            LoggedObject.LOG.warning("Failed to parse checksum in INTELHEX line: ", strstr, ex);
            return 0;
        }

        return 1;
    }
}
