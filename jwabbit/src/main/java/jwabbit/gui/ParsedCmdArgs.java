package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.hardware.EnumSendFlag;

/**
 * WABBITEMU SOURCE: gui/guicommandline.h, "ParsedCmdArgs" struct.
 */
class ParsedCmdArgs {

    /** WABBITEMU SOURCE: gui/guicommandline.h, "MAX_FILES" macro. */
    private static final int MAX_FILES = 255;

    /** rom_files. */
    private final String[] romFiles;

    /** utility_files. */
    private final String[] utilityFiles;

    /** archive_files. */
    private final String[] archiveFiles;

    /** ram_files. */
    private final String[] ramFiles;

    /** cur_files. */
    private final String[] curFiles;

    /** num_rom_files. */
    private int numRomFiles;

    /** num_utility_files. */
    private int numUtilityFiles;

    /** num_archive_files. */
    private int numArchiveFiles;

    /** num_ram_files. */
    private int numRamFiles;

    /** num_cur_files. */
    private int numCurFiles;

    /** silent_mode. */
    private boolean silentMode;

    /** force_new_instance. */
    private boolean forceNewInstance;

    /** no_create_calc. */
    private boolean noCreateCalc;

    /**
     * Constructs a new {@code ParsedCmdArgs}.
     */
    ParsedCmdArgs() {

        this.romFiles = new String[MAX_FILES];
        this.utilityFiles = new String[MAX_FILES];
        this.archiveFiles = new String[MAX_FILES];
        this.ramFiles = new String[MAX_FILES];
        this.curFiles = new String[MAX_FILES];
    }

    /**
     * Gets the number of ROM files.
     *
     * @return the number of files
     */
    final int getNumRomFiles() {

        return this.numRomFiles;
    }

    /**
     * Gets a ROM file name.
     *
     * @param index the index
     * @return the file name
     */
    final String getRomFile(final int index) {

        return this.romFiles[index];
    }

    /**
     * Tests whether in silent mode.
     *
     * @return true for silent mode
     */
    final boolean isSilentMode() {

        return this.silentMode;
    }

    /**
     * Tests whether to force a new instance.
     *
     * @return true to force new instance
     */
    final boolean isForceNewInstance() {

        return this.forceNewInstance;
    }

    /**
     * Tests whether not to create the calculator.
     *
     * @return true to not create the calculator
     */
    final boolean isNoCreateCalc() {

        return this.noCreateCalc;
    }

    /**
     * Clears the object and resets all field values to zero.
     */
    private void clear() {

        for (int i = 0; i < MAX_FILES; ++i) {
            this.romFiles[i] = null;
            this.utilityFiles[i] = null;
            this.archiveFiles[i] = null;
            this.ramFiles[i] = null;
            this.curFiles[i] = null;
        }

        this.numRomFiles = 0;
        this.numUtilityFiles = 0;
        this.numArchiveFiles = 0;
        this.numRamFiles = 0;
        this.numCurFiles = 0;

        this.silentMode = false;
        this.forceNewInstance = false;
        this.noCreateCalc = false;
    }

    /**
     * WABBITEMU SOURCE: gui/guicommandline.c, "ParseCommandLineArgs" function.
     *
     * @param args the command line arguments
     */
    final void parseCommandLineArgs(final String... args) {

        String tmpstring;
        EnumSendFlag ram = EnumSendFlag.SEND_CUR;

        clear();

        final int numArgs = args.length;
        for (int i = 1; i < numArgs; ++i) {

            tmpstring = args[i];
            if (tmpstring.length() < 2) {
                continue;
            }

            final char secondChar = Character.toUpperCase(tmpstring.charAt(1));

            if (tmpstring.charAt(0) != '-' && tmpstring.charAt(0) != '/') {

                final int dotIndex = tmpstring.indexOf('.');
                final String ext;
                if (dotIndex == -1) {
                    ext = "";
                } else {
                    ext = tmpstring.substring(dotIndex);
                }

                if (".rom".equalsIgnoreCase(ext) || ".sav".equalsIgnoreCase(ext) || ".clc".equalsIgnoreCase(ext)) {
                    this.romFiles[this.numRomFiles] = tmpstring;
                    ++this.numRomFiles;
                } else if (".brk".equalsIgnoreCase(ext) || ".lab".equalsIgnoreCase(ext) || ".zip".equalsIgnoreCase(ext)
                        || ".tig".equalsIgnoreCase(ext)) {
                    this.utilityFiles[this.numUtilityFiles] = tmpstring;
                    ++this.numUtilityFiles;
                } else if (ram == EnumSendFlag.SEND_CUR) {
                    this.curFiles[this.numCurFiles] = tmpstring;
                    ++this.numCurFiles;
                } else if (ram == EnumSendFlag.SEND_RAM) {
                    this.ramFiles[this.numRamFiles] = tmpstring;
                    ++this.numRamFiles;
                } else {
                    this.archiveFiles[this.numArchiveFiles] = tmpstring;
                    ++this.numArchiveFiles;
                }
            } else if (tmpstring.length() == 2) {
                if (secondChar == 'R') {
                    ram = EnumSendFlag.SEND_RAM;
                } else if (secondChar == 'A') {
                    ram = EnumSendFlag.SEND_ARC;
                } else if (secondChar == 'S') {
                    this.silentMode = true;
                } else if (secondChar == 'F') {
                } else if (secondChar == 'N') {
                    this.forceNewInstance = true;
                }
            } else if ("embedding".equalsIgnoreCase(tmpstring)) {
                this.noCreateCalc = true;
            }
        }
    }
}
