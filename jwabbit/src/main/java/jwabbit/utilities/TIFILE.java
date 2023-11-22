package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.Memory;
import jwabbit.log.LoggedObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * WABBITEMU SOURCE: utilities/var.h, "TIFILE" struct.
 */
public final class TIFILE {

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    public static final int ROM_TYPE = 1;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    public static final int FLASH_TYPE = 2;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    public static final int SAV_TYPE = 4;

    /** WABBITEMU SOURCE: utilities/var.h, "TI_FILE_HEADER_SIZE" macro. */
    private static final int TI_FILE_HEADER_SIZE = 8 + 3 + 42;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    private static final int VAR_TYPE = 3;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    private static final int BACKUP_TYPE = 5;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    private static final int LABEL_TYPE = 6;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    private static final int BREAKPOINT_TYPE = 7;

    /** WABBITEMU SOURCE: utilities/var.h, "TifileVarType_t" enum. */
    private static final int GROUP_TYPE = 8;

    /** A length global. */
    private static int length2;

    /** The signature. */
    private final int[] sig = new int[8];

    /** The sub-signature. */
    private final int[] subsig = new int[3];

    /** The comment. */
    private final int[] comment = new int[42];

    /** The length. */
    private int length;

    /** The variable object. */
    private TIVar var;

    /** An array of variable objects. */
    private final TIVar[] vars = new TIVar[256];

    /** The model. */
    private EnumCalcModel model;

    /** The type. */
    private int type;

    /** The ROM object. */
    private ROM rom;

    /** The flash object. */
    private TIFlash flash;

    /** The save state object. */
    private SaveState save;

    /** The backup object. */
    private TIBackup backup;

    /**
     * SOURCE: utilities/var.c, "NullTiFile" and "InitTiFile" functions.
     */
    private TIFILE() {

        this.type = VAR_TYPE;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "InitTiFile" function.
     *
     * @return the new TIFILE
     */
    private static TIFILE initTiFile() {

        final TIFILE tifile = new TIFILE();
        tifile.nullTiFile();

        return tifile;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "NullTiFile" function.
     */
    private void nullTiFile() {

        Arrays.fill(this.sig, 0);
        Arrays.fill(this.subsig, 0);
        Arrays.fill(this.comment, 0);
        this.length = 0;
        this.var = null;
        Arrays.fill(this.vars, null);
        this.model = EnumCalcModel.TI_81;
        this.type = VAR_TYPE;
        this.rom = null;
        this.flash = null;
        this.save = null;
        this.backup = null;
    }

    /**
     * Sets a byte within the first 53 bytes of the structure.
     *
     * @param offset the offset
     * @param value  the byte value
     */
    private void setByte(final int offset, final int value) {

        if (offset < 8) {
            this.sig[offset] = value;
        } else if (offset < 11) {
            this.subsig[offset - 8] = value;
        } else if (offset < 53) {
            this.comment[offset - 11] = value;
        }
    }

    /**
     * Gets the 8-byte array for signature.
     *
     * @return the array
     */
    private int[] getSig() {

        return this.sig;
    }

    /**
     * Gets the 42-byte array for comment.
     *
     * @return the array
     */
    public int[] getComment() {

        return this.comment;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public int getLength() {

        return this.length;
    }

    /**
     * Sets the length.
     *
     * @param theLength the length
     */
    public void setLength(final int theLength) {

        if (theLength < 0 || theLength > 255) {
            LoggedObject.LOG.warning("Invalid length value in TIFILE " + theLength, new IllegalArgumentException());
        }

        this.length = theLength;
    }

    /**
     * Gets the variable object.
     *
     * @return the variable object
     */
    public TIVar getVar() {

        return this.var;
    }

    /**
     * Sets the variable object.
     *
     * @param theVar the variable object
     */
    public void setVar(final TIVar theVar) {

        this.var = theVar;
    }

    /**
     * Sets the checksum.
     *
     * @param theChksum the checksum
     */
    private static void setChksum(final int theChksum) {

        if (theChksum < 0 || theChksum > 255) {
            LoggedObject.LOG.warning("Invalid checksum value in TIFILE " + theChksum, new IllegalArgumentException());
        }
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public EnumCalcModel getModel() {

        return this.model;
    }

    /**
     * Sets the model.
     *
     * @param theModel the model
     */
    public void setModel(final EnumCalcModel theModel) {

        this.model = theModel;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public int getType() {

        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param theType the type
     */
    private void setType(final int theType) {

        if (theType < 0 || theType > 9) {
            LoggedObject.LOG.warning("Invalid type value in TIFILE " + theType, new IllegalArgumentException());
        }

        this.type = theType;
    }

    /**
     * Gets the ROM object.
     *
     * @return the ROM object
     */
    public ROM getRom() {

        return this.rom;
    }

    /**
     * Sets the ROM object.
     *
     * @param theRom the ROM object
     */
    public void setRom(final ROM theRom) {

        this.rom = theRom;
    }

    /**
     * Gets the flash object.
     *
     * @return the flash object
     */
    public TIFlash getFlash() {

        return this.flash;
    }

    /**
     * Sets the flash object.
     *
     * @param theFlash the flash object
     */
    public void setFlash(final TIFlash theFlash) {

        this.flash = theFlash;
    }

    /**
     * Gets the save state object.
     *
     * @return the save state object
     */
    public SaveState getSave() {

        return this.save;
    }

    /**
     * Sets the save state object.
     *
     * @param theSave the save state object
     */
    private void setSave(final SaveState theSave) {

        this.save = theSave;
    }

    /**
     * Gets the vars object.
     *
     * @return the vars object
     */
    public TIVar[] getVars() {

        return this.vars;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ImportFlashFile" function.
     *
     * @param infile the input stream to read
     * @param tifile the TI file to populate
     * @return {@code tifile} on success; null on error
     */
    private static TIFILE importFlashFile(final FileInputStream infile, final TIFILE tifile) {

        for (int i = 0; i < 256; i++) {
            tifile.flash.getPagesize()[i] = 0;
            tifile.flash.getData()[i] = null;
        }

        final IntelHex record = new IntelHex();
        int currentPage = -1;
        int highestAddress = 0;
        int totalPages = 0;
        boolean done = false;

        if (tifile.flash.getType() == TIFlash.FLASH_TYPE_OS) {
            // Find the first page, usually after the first line
            do {
                if (Var.readIntelHex(infile, record) == 0) {
                    tifile.freeTiFile();
                    return null;
                }
            } while (record.getType() != 0x02 || record.getDataSize() != 2);
            currentPage = ((record.getData()[0] << 8) | record.getData()[1]) & 0xFF;
            if (tifile.flash.getData()[currentPage] == null) {
                tifile.flash.getData()[currentPage] = new int[Memory.PAGE_SIZE];
            }
        }

        while (!done) {
            Var.readIntelHex(infile, record);

            switch (record.getType()) {
                case 0:
                    if (currentPage > -1) {
                        int i;
                        for (i = 0; i < record.getDataSize(); ++i) {
                            tifile.flash.getData()[currentPage][(i + record.getAddress()) & 0x3FFF] =
                                    record.getData()[i];
                        }
                        if (highestAddress < i + record.getAddress()) {
                            highestAddress = i + record.getAddress();
                        }
                    }
                    break;
                case 1:
                    done = true;
                    if (currentPage == -1) {
                        LoggedObject.LOG.warning("invalid current page");
                        tifile.freeTiFile();
                        return null;
                    }
                    tifile.flash.getPagesize()[currentPage] = highestAddress - Memory.PAGE_SIZE;
                    tifile.flash.setPages(totalPages);
                    break;
                case 2:
                    if (currentPage > -1) {
                        tifile.flash.getPagesize()[currentPage] = highestAddress - Memory.PAGE_SIZE;
                    }
                    totalPages++;
                    currentPage = ((record.getData()[0] << 8) | record.getData()[1]) & 0xFF;
                    if (tifile.flash.getData()[currentPage] == null) {
                        tifile.flash.getData()[currentPage] = new int[Memory.PAGE_SIZE];
                    }
                    highestAddress = 0;
                    break;
                default:
                    LoggedObject.LOG.warning("unknown record");
                    tifile.freeTiFile();
                    return null;
            }
        }

        if (tifile.flash.getDevice() == 0x74) {
            tifile.model = EnumCalcModel.TI_73;
        } else if (tifile.flash.getDevice() == 0x73) {
            tifile.model = EnumCalcModel.TI_83P;
        } else {
            LoggedObject.LOG.warning("unknown device " + tifile.flash.getDevice());
            tifile.freeTiFile();
            return null;
        }

        return tifile;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ImportROMFile" function.
     *
     * @param file   the file from which the ROM is being loaded
     * @param infile the stream from which to read
     * @param tifile the TI file to populate
     * @return {@code tifile} on success; null on error
     * @throws IOException on a read error
     */
    private static TIFILE importROMFile(final File file, final FileInputStream infile,
                                        final TIFILE tifile) throws IOException {

        final int size = (int) file.length();

        tifile.rom = new ROM();
        final int[] romData = new int[size];
        tifile.rom.setData(romData);

        // We have already read the first 8 bytes from the input stream and stored them in the
        // 8 bytes "signature" field of the TIFILE's TIFLASH object.
        System.arraycopy(tifile.getSig(), 0, romData, 0, 8);
        int i = 8;

        final byte[] buf = new byte[2048];
        while (i < size) {
            final int toRead = Math.min(buf.length, size - i);
            final int numRead = infile.read(buf, 0, toRead);

            if (numRead == -1) {
                LoggedObject.LOG.warning("Reached end of ROM file before all data read (read " + i + "/" + size + ")");
                tifile.freeTiFile();
                return null;
            }

            for (int j = 0; j < numRead; ++j) {
                tifile.rom.getData()[i] = (int) buf[j];
                ++i;
            }
        }
        tifile.rom.setSize(size);

        final String[] ver = new String[1];
        final EnumCalcModel calc = Var.findRomVersion(ver, tifile.rom.getData(), size);
        if (calc == EnumCalcModel.INVALID_MODEL) {
            tifile.freeTiFile();
            return null;
        }
        tifile.model = calc;

        return tifile;
    }

    /**
     * Imports a backup file.
     *
     * <p>
     * SOURCE: utilities/var.c, "ImportBackup" function.
     *
     * @param infile the stream from which to read
     * @param tifile the TI file to populate
     * @return {@code tifile} on success; null on error
     * @throws IOException on a read error
     */
    private static TIFILE importBackup(final FileInputStream infile, final TIFILE tifile)
            throws IOException {

        tifile.backup.setData1(null);
        tifile.backup.setData2(null);
        tifile.backup.setData3(null);

        int tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength2(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength2(tifile.backup.getLength2() + (tmp << 8));

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength3(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength3(tifile.backup.getLength3() + (tmp << 8));

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setAddress(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setAddress(tifile.backup.getAddress() + (tmp << 8));

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength1a(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength1a(tifile.backup.getLength1a() + (tmp << 8));

        tifile.backup.setData1(new int[tifile.backup.getLength1()]);
        for (int i = 0; i < tifile.backup.getLength1(); ++i) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read backup file");
                tifile.freeTiFile();
                return null;
            }
            tifile.backup.getData1()[i] = tmp;
        }

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength2a(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength2a(tifile.backup.getLength2a() + (tmp << 8));

        tifile.backup.setData2(new int[tifile.backup.getLength2()]);
        for (int i = 0; i < tifile.backup.getLength2(); ++i) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read backup file");
                tifile.freeTiFile();
                return null;
            }
            tifile.backup.getData2()[i] = tmp;
        }

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength3a(tmp);
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read backup file");
            tifile.freeTiFile();
            return null;
        }
        tifile.backup.setLength3a(tifile.backup.getLength3a() + (tmp << 8));

        tifile.backup.setData3(new int[tifile.backup.getLength3()]);
        for (int i = 0; i < tifile.backup.getLength3(); ++i) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read backup file");
                tifile.freeTiFile();
                return null;
            }
            tifile.backup.getData3()[i] = tmp;
        }

        tifile.type = BACKUP_TYPE;

        return tifile;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ReadTiFileHeader" function.
     *
     * @param infile the stream from which to read
     * @param tifile the TI file to populate
     * @throws IOException on a read error
     */
    private static void readTiFileHeader(final FileInputStream infile, final TIFILE tifile)
            throws IOException {

        final int headerStringLen = 8;
        final byte[] headerString = new byte[headerStringLen];

        if (infile.read(headerString) != headerStringLen) {
            LoggedObject.LOG.warning("Failed to read header string bytes");
            return;
        }

        final char[] hdrChars = new char[headerStringLen];
        for (int k = 0; k < headerStringLen; ++k) {
            hdrChars[k] = (char) headerString[k];
        }
        final String hdrString = new String(hdrChars);

        if (SaveState.DETECT_STR.equalsIgnoreCase(hdrString) || SaveState.DETECT_CMP_STR.equalsIgnoreCase(hdrString)) {
            LoggedObject.LOG.info("  File identified as a saved state");
            tifile.type = SAV_TYPE;
            return;
        }

        if (SaveState.FLASH_HEADER.equalsIgnoreCase(hdrString)) {
            LoggedObject.LOG.info("  File identified as a flash file");
            tifile.type = FLASH_TYPE;

            tifile.flash = new TIFlash();

            // We can't rewind, so store our already-read 8 bytes as if we're re-reading
            for (int i = 0; i < 8; ++i) {
                tifile.flash.setByte(i, (int) headerString[i]);
            }
            for (int i = 8; i < TIFlash.TI_FLASH_HEADER_SIZE; ++i) {
                final int tmp = infile.read();
                if (tmp == -1) {
                    LoggedObject.LOG.warning("Failed to read TI file header byte");
                    tifile.freeTiFile();
                    return;
                }
                tifile.flash.setByte(i, tmp);
            }
            return;
        }

        // Store first 8 bytes read so that if it's a ROM we have it
        for (int i = 0; i < 8; ++i) {
            tifile.setByte(i, (int) headerString[i]);
        }

        // It maybe a rom if it doesn't have the Standard header
        if (!"**TI73**".equalsIgnoreCase(hdrString) && !"**TI82**".equalsIgnoreCase(hdrString)
                && !"**TI83**".equalsIgnoreCase(hdrString) && !"**TI83F*".equalsIgnoreCase(hdrString)
                && !"**TI85**".equalsIgnoreCase(hdrString) && !"**TI86**".equalsIgnoreCase(hdrString)) {

            tifile.type = ROM_TYPE;
            return;
        }

        for (int i = 8; i < TI_FILE_HEADER_SIZE; ++i) {
            final int tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read TI file header byte");
                tifile.freeTiFile();
                return;
            }
            tifile.setByte(i, tmp);
        }

        final char[] sigChars = new char[8];
        int len = 0;
        for (int k = 0; k < 8; ++k) {
            sigChars[k] = (char) tifile.getSig()[k];
            if (sigChars[k] == 0) {
                break;
            }
            ++len;
        }
        final String sigStr = new String(sigChars, 0, len);

        if ("**TI73**".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_73;
        } else if ("**TI82**".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_82;
        } else if ("**TI83**".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_83;
        } else if ("**TI83F*".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_83P;
        } else if ("**TI85**".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_85;
        } else if ("**TI86**".equalsIgnoreCase(sigStr)) {
            tifile.model = EnumCalcModel.TI_86;
        } else {
            LoggedObject.LOG.warning("  Unrecognized Sig String: " + sigStr);
            tifile.freeTiFile();
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ImportVarFile" function.
     *
     * @param infile    the stream from which to read
     * @param tifile    the TI file to populate
     * @param varNumber the variable number
     * @return {@code tifile} on success; null on error
     * @throws IOException on a read error
     */
    private static TIFILE importVarFile(final FileInputStream infile, final TIFILE tifile,
                                        final int varNumber) throws IOException {

        int tmp;

        if (varNumber == 0) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file byte");
                tifile.freeTiFile();
                return null;
            }
            length2 = tmp;
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file byte");
                tifile.freeTiFile();
                return null;
            }
            length2 += tmp << 8;
        }

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        int headersize = tmp;
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        headersize += tmp << 8;

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        int length = tmp;
        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        length += tmp << 8;

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        final int vartype = tmp;

        if ((tifile.model == EnumCalcModel.TI_73 && vartype == 0x13)
                || (tifile.model == EnumCalcModel.TI_82 && vartype == 0x0F)
                || (tifile.model == EnumCalcModel.TI_85 && vartype == 0x1D)) {
            tifile.backup = new TIBackup();
            TIBackup.setHeadersize(headersize);
            tifile.backup.setLength1(length);
            TIBackup.setVartype(vartype);
            return importBackup(infile, tifile);
        }

        if (length2 > length + 17 || tifile.type == GROUP_TYPE) {
            tifile.type = GROUP_TYPE;
        } else {
            tifile.type = VAR_TYPE;
        }

        tifile.var = new TIVar();
        tifile.vars[varNumber] = tifile.var;

        int nameLength = 8;
        if (tifile.model == EnumCalcModel.TI_86 || tifile.model == EnumCalcModel.TI_85) {
            // skip name length
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file byte");
                tifile.freeTiFile();
                return null;
            }
            nameLength = tmp;
        }

        tifile.var.setNameLength(nameLength);
        tifile.var.setHeadersize(headersize);
        tifile.var.setLength(length);
        tifile.var.setVartype(vartype);

        if (tifile.model == EnumCalcModel.TI_86) {
            nameLength = 8;
        }

        for (int i = 0; i < nameLength; ++i) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file byte");
                tifile.freeTiFile();
                return null;
            }
            tifile.var.getName()[i] = tmp;
        }

        // From here forward, "ptr" lies beyond name, so we're filling the rest of the TIVAR
        // structure in a bad way - make this right!

        // Read version and flag fields...
        if (tifile.model == EnumCalcModel.TI_83P) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file version byte");
                tifile.freeTiFile();
                return null;
            }
            tifile.var.setVersion(tmp);
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file flag byte");
                tifile.freeTiFile();
                return null;
            }
            tifile.var.setFlag(tmp);
        } else {
            tifile.var.setVersion(0);
            tifile.var.setFlag(0);
        }

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        int len2 = tmp;

        tmp = infile.read();
        if (tmp == -1) {
            LoggedObject.LOG.warning("Failed to read var file byte");
            tifile.freeTiFile();
            return null;
        }
        len2 = len2 + (tmp << 8);
        tifile.var.setLength2(len2);

        if (len2 != tifile.var.getLength()) {
            LoggedObject.LOG.warning("Var file import length mismatch");
        }

        tifile.var.setData(new int[tifile.var.getLength()]);

        for (int i = 0; i < tifile.var.getLength(); ++i) {
            tmp = infile.read();
            if (tmp == -1) {
                LoggedObject.LOG.warning("Failed to read var file byte");
                tifile.freeTiFile();
                return null;
            }
            tifile.var.getData()[i] = tmp;
        }

        TIFILE result = tifile;

        if (tifile.type == GROUP_TYPE) {
            if (varNumber != 0) {
                return tifile;
            }

            int varNum = varNumber;
            while (result != null) {
                length2 -= tifile.var.getLength() + 17;
                if (length2 <= 0) {
                    break;
                }
                ++varNum;
                result = importVarFile(infile, tifile, varNum);
            }
        }

        // WABBITEMU does not do this null check, but it should.
        if (result != null) {
            setChksum((infile.read() & 0x00FF) + ((infile.read() & 0x00FF) << 8));
        }

        return result;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "ImportVarData" function.
     *
     * @param file   the file being read
     * @param infile the stream from which to read
     * @param tifile the TI file to populate
     * @return {@code tifile} on success; null on error
     * @throws IOException on a read error
     */
    private static TIFILE importVarData(final File file, final FileInputStream infile,
                                        final TIFILE tifile) throws IOException {

        switch (tifile.type) {
            case ROM_TYPE:
                return importROMFile(file, infile, tifile);
            case FLASH_TYPE:
                return importFlashFile(infile, tifile);
            case SAV_TYPE:
                tifile.save = SaveState.readSave(infile);
                if (tifile.save == null) {
                    tifile.freeTiFile();
                    return null;
                }
                tifile.model = tifile.save.getModel();
                return tifile;
            case VAR_TYPE:
                return importVarFile(infile, tifile, 0);
            default:
                return null;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "importvar" function.
     *
     * @param filePath        the file path
     * @param onlyCheckHeader true to only read and check the header
     * @return the loaded {@code TIFILE} on success; null on error
     */
    public static TIFILE importvar(final String filePath, final boolean onlyCheckHeader) {

        final String extension;
        final int lastDot = filePath.lastIndexOf('.');
        if (lastDot == -1) {
            extension = "";
        } else {
            extension = filePath.substring(lastDot);
        }

        TIFILE tifile = initTiFile();

        if (".lab".equalsIgnoreCase(extension)) {
            tifile.setType(LABEL_TYPE);
            return tifile;
        }

        if (".brk".equalsIgnoreCase(extension)) {
            tifile.setType(BREAKPOINT_TYPE);
            return tifile;
        }

        try (final FileInputStream infile = new FileInputStream(filePath)) {
            readTiFileHeader(infile, tifile);

            if (onlyCheckHeader && tifile.type != ROM_TYPE && tifile.type != SAV_TYPE) {
                return tifile;
            }

            tifile = importVarData(new File(filePath), infile, tifile);
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Exception reading file ", filePath);
            tifile.freeTiFile();
            tifile = null;
        }

        return tifile;
    }

    /**
     * WABBITEMU SOURCE: utilities/var.c, "FreeTiFile" function.
     */
    public void freeTiFile() {

        if (this.save != null) {
            this.save.freeSave();
        }

        Arrays.fill(this.vars, null);
        this.flash = null;
        this.rom = null;
    }
}
