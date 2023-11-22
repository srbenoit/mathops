package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.EnumFlashCommand;
import jwabbit.core.Interrupt;
import jwabbit.core.JWCoreConstants;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.core.PIOContext;
import jwabbit.core.TimerContext;
import jwabbit.core.WideAddr;
import jwabbit.hardware.ColorLCD;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.LCD;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;
import jwabbit.hardware.SEAux;
import jwabbit.hardware.STDINT;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * WABBITEMU SOURCE: utilities/savestate.h, "SAVESTATE_t" struct.
 */
public final class SaveState {

    /** WABBITEMU SOURCE: utilities/savestate.h, "DETECT_STR" macro. */
    static final String DETECT_STR = "*WABBIT*";

    /** WABBITEMU SOURCE: utilities/savestate.h, "DETECT_CMP_STR" macro. */
    static final String DETECT_CMP_STR = "*WABCMP*";

    /** WABBITEMU SOURCE: utilities/savestate.h, "FLASH_HEADER" macro. */
    static final String FLASH_HEADER = "**TIFL**";

    /** WABBITEMU SOURCE: utilities/savestate.h, "MAX_CHUNKS" macro. */
    private static final int MAX_CHUNKS = 0;

    /** WABBITEMU SOURCE: utilities/savestate.h, "CUR_MAJOR" macro. */
    private static final int CUR_MAJOR = 0;

    /** WABBITEMU SOURCE: utilities/savestate.h, "MEM_C_CMD_BUILD" macro. */
    private static final int MEM_C_CMD_BUILD = 1;

    /** WABBITEMU SOURCE: utilities/savestate.h, "LCD_SCREEN_ADDR_BUILD" macro. */
    private static final int LCD_SCREEN_ADDR_BUILD = 2;

    /** WABBITEMU SOURCE: utilities/savestate.h, "CPU_MODEL_BITS_BUILD" macro. */
    private static final int CPU_MODEL_BITS_BUILD = 2;

    /** WABBITEMU SOURCE: utilities/savestate.h, "NEW_CONTRAST_MODEL_BUILD" macro. */
    private static final int NEW_CONTRAST_MODEL_BUILD = 3;

    /** WABBITEMU SOURCE: utilities/savestate.h, "CPU_tag" macro. */
    private static final char[] CPU_TAG = "CPU ".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "MEM_tag" macro. */
    private static final char[] MEM_TAG = "MEMC".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "ROM_tag" macro. */
    private static final char[] ROM_TAG = "ROM ".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "RAM_tag" macro. */
    private static final char[] RAM_TAG = "RAM ".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "TIMER_tag" macro. */
    private static final char[] TIMER_TAG = "TIME".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "LCD_tag" macro. */
    private static final char[] LCD_TAG = "LCD ".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "LINK_tag" macro. */
    private static final char[] LINK_TAG = "LINK".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "STDINT_tag" macro. */
    private static final char[] STDINT_TAG = "STDI".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "SE_AUX_tag" macro. */
    private static final char[] SE_AUX_TAG = "SEAX".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "USB_tag" macro. */
    private static final char[] USB_TAG = "USB ".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "REMAP_tag" macro. */
    private static final char[] REMAP_TAG = "RMAP".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "RAM_LIMIT_tag" macro. */
    private static final char[] RAM_LIMIT_TAG = "RMLM".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "FLASH_BREAKS_tag" macro. */
    private static final char[] FLASH_BREAKS_TAG = "FBRK".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "NUM_FLASH_BREAKS_tag" macro. */
    private static final char[] NUM_FLASH_BREAKS_TAG = "NFBK".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "NUM_RAM_BREAKS_tag" macro. */
    private static final char[] NUM_RAM_BREAKS_TAG = "NRBK".toCharArray();

    /** WABBITEMU SOURCE: utilities/savestate.h, "MAX_SAVESTATE_AUTHOR_LENGTH" macro. */
    private static final int MAX_SAVESTATE_AUTHOR_LENGTH = 32;

    /** WABBITEMU SOURCE: utilities/savestate.h, "MAX_SAVESTATE_COMMENT_LENGTH" macro. */
    private static final int MAX_SAVESTATE_COMMENT_LENGTH = 64;

    /** The version major number. */
    private int versionMajor;

    /** The version minor number. */
    private int versionMinor;

    /** The version build number. */
    private int versionBuild;

    /** The model. */
    private EnumCalcModel model;

    /** The chunk count. */
    private int chunkCount;

    /** The author. */
    private String author;

    /** The comment. */
    private String comment;

    /** The chunks. */
    private final Chunk[] chunks = new Chunk[MAX_CHUNKS];

    /**
     * Constructs a new {@code SaveState}.
     */
    private SaveState() {

        // No action
    }

    /**
     * Gets the calculator model.
     *
     * @return the model
     */
    public EnumCalcModel getModel() {

        return this.model;
    }

    /**
     * Sets the calculator model.
     *
     * @param theModel the model
     */
    public void setModel(final EnumCalcModel theModel) {

        this.model = theModel;
    }

    /**
     * Gets the author.
     *
     * @return the author
     */
    public String getAuthor() {

        return this.author;
    }

    /**
     * Sets the author.
     *
     * @param theAuthor the author
     */
    public void setAuthor(final String theAuthor) {

        this.author = theAuthor;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {

        return this.comment;
    }

    /**
     * Sets the comment.
     *
     * @param theComment the comment
     */
    public void setComment(final String theComment) {

        this.comment = theComment;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "cmpTags" function.
     *
     * @param str1 the first character array to compare
     * @param str2 the second character array to compare
     * @return true if character arrays match in their first 4 characters
     */
    private static boolean cmpTags(final char[] str1, final char[] str2) {

        for (int i = 0; i < 4; ++i) {
            if (str1[i] != str2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "fgeti" function.
     *
     * @param stream the stream from which to read
     * @return the integer
     * @throws IOException on a read error
     */
    private static int fgeti(final InputStream stream) throws IOException {

        return stream.read() + (stream.read() << 8) + (stream.read() << 16) + (stream.read() << 24);
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "ClearSave" function.
     */
    private void clearSave() {

        for (int i = 0; i < this.chunkCount; ++i) {
            if (this.chunks[i] != null) {
                this.chunks[i].setData(null);
                this.chunks[i] = null;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "FreeSave" function.
     */
    void freeSave() {

        clearSave();
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "FindChunk" function.
     *
     * @param tag the tag
     * @return the chunk, null if not found
     */
    private Chunk findChunk(final char[] tag) {

        for (int i = 0; i < this.chunkCount; ++i) {
            if (cmpTags(this.chunks[i].getTag(), tag)) {
                this.chunks[i].setPnt(0);
                return this.chunks[i];
            }
        }

        return null;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "NewChunk" function.
     *
     * @param tag the tag
     * @return the new chunk
     */
    private Chunk newChunk(final char[] tag) {

        final int chunk = this.chunkCount;

        if (findChunk(tag) != null) {
            LoggedObject.LOG.warning("Error: chunk '%s' already exists", tag);
            return null;
        }

        if (this.chunks[chunk] != null) {
            LoggedObject.LOG.warning("Error new chunk was not null.");
        }
        this.chunks[chunk] = new Chunk();

        this.chunks[chunk].getTag()[0] = tag[0];
        this.chunks[chunk].getTag()[1] = tag[1];
        this.chunks[chunk].getTag()[2] = tag[2];
        this.chunks[chunk].getTag()[3] = tag[3];
        this.chunks[chunk].setSize(0);
        this.chunks[chunk].setData(null);
        this.chunks[chunk].setPnt(0);
        ++this.chunkCount;

        return this.chunks[chunk];
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadCPU" function.
     *
     * @param cpu the CPU into which to store loaded data
     * @return true on success
     */
    private boolean loadCPU(final CPU cpu) {

        final Chunk chunk = findChunk(CPU_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        cpu.setA((int) chunk.readChar());
        cpu.setF((int) chunk.readChar());
        cpu.setB((int) chunk.readChar());
        cpu.setC((int) chunk.readChar());
        cpu.setD((int) chunk.readChar());
        cpu.setE((int) chunk.readChar());
        cpu.setH((int) chunk.readChar());
        cpu.setL((int) chunk.readChar());

        cpu.setAprime((int) chunk.readChar());
        cpu.setFprime((int) chunk.readChar());
        cpu.setBprime((int) chunk.readChar());
        cpu.setCprime((int) chunk.readChar());
        cpu.setDprime((int) chunk.readChar());
        cpu.setEprime((int) chunk.readChar());
        cpu.setHprime((int) chunk.readChar());
        cpu.setLprime((int) chunk.readChar());

        cpu.setIXL((int) chunk.readChar());
        cpu.setIXH((int) chunk.readChar());
        cpu.setIYL((int) chunk.readChar());
        cpu.setIYH((int) chunk.readChar());

        cpu.setPC(chunk.readShort());
        cpu.setSP(chunk.readShort());

        cpu.setI((int) chunk.readChar());
        cpu.setR((int) chunk.readChar());
        cpu.setBus((int) chunk.readChar());

        cpu.setIMode(chunk.readInt());

        cpu.setInterrupt(chunk.readInt() != 0);
        cpu.setEiBlock(chunk.readInt() != 0);
        cpu.setIff1(chunk.readInt() != 0);
        cpu.setIff2(chunk.readInt() != 0);
        cpu.setHalt(chunk.readInt() != 0);

        cpu.setRead(chunk.readInt() != 0);
        cpu.setWrite(chunk.readInt() != 0);
        cpu.setOutput(chunk.readInt() != 0);
        cpu.setInput(chunk.readInt() != 0);
        cpu.setPrefix(chunk.readInt());

        for (int i = 0; i < 256; ++i) {
            final Interrupt val = cpu.getPIOContext().getInterrupt(i);
            val.setInterruptVal(chunk.readInt());
            val.setSkipFactor(chunk.readInt());
            val.setSkipCount(chunk.readInt());
        }

        if (this.versionBuild >= CPU_MODEL_BITS_BUILD) {
            cpu.setModelBits(chunk.readInt());
        } else {
            cpu.setModelBits(this.model == EnumCalcModel.TI_84P ? 0 : 1);
        }

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadMEM" function.
     *
     * @param mem the memory context into which to store loaded data
     * @return true on success
     */
    private boolean loadMEM(final MemoryContext mem) {

        Chunk chunk = findChunk(MEM_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        mem.getFlash().setSize(chunk.readInt());
        chunk.readInt();
        mem.getRam().setSize(chunk.readInt());
        chunk.readInt();
        mem.setStep(EnumFlashCommand.values()[chunk.readInt()]);
        if (this.versionBuild <= MEM_C_CMD_BUILD) {
            // dummy read for compatibility. this used to read mem_c->cmd but is no longer needed
            chunk.readChar();
        }
        mem.setBootmapped(chunk.readInt() != 0);
        mem.setFlashLocked(chunk.readInt() != 0);
        mem.getFlash().setVersion(chunk.readInt());

        for (int i = 0; i < 5; ++i) {
            mem.getNormalBanks()[i].setPage(chunk.readInt());
            mem.getNormalBanks()[i].setReadOnly(chunk.readInt() != 0);
            mem.getNormalBanks()[i].setRam(chunk.readInt() != 0);
            mem.getNormalBanks()[i].setNoExec(chunk.readInt() != 0);
            if (mem.getNormalBanks()[i].isRam()) {
                mem.getNormalBanks()[i].setMem(mem.getRam());
                mem.getNormalBanks()[i].setAddr(mem.getNormalBanks()[i].getPage() * Memory.PAGE_SIZE);
            } else {
                mem.getNormalBanks()[i].setMem(mem.getFlash());
                mem.getNormalBanks()[i].setAddr(mem.getNormalBanks()[i].getPage() * Memory.PAGE_SIZE);
            }
        }
        if (mem.isBootmapped()) {
            mem.updateBootmapPages();
            mem.activateBootmapBanks();
        } else {
            mem.activateNormalBanks();
        }

        mem.setReadOPFlashTStates(chunk.readInt());
        mem.setReadNOPFlashTStates(chunk.readInt());
        mem.setWriteFlashTStates(chunk.readInt());
        mem.setReadOPRamTStates(chunk.readInt());
        mem.setReadNOPRamTStates(chunk.readInt());
        mem.setWriteRamTStates(chunk.readInt());

        mem.getFlash().setUpper(chunk.readInt());
        mem.getFlash().setLower(chunk.readInt());

        chunk = findChunk(ROM_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);
        final int[] tempFlash = new int[mem.getFlash().getSize()];
        chunk.readBlock(tempFlash, mem.getFlash().getSize());
        mem.getFlash().load(tempFlash);

        chunk = findChunk(RAM_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);
        final int[] tempRam = new int[mem.getRam().getSize()];
        chunk.readBlock(tempRam, mem.getRam().getSize());
        mem.getRam().load(tempFlash);

        chunk = findChunk(REMAP_TAG);
        if (chunk != null) {
            chunk.setPnt(0);
            mem.getPort27RemapCount()[0] = chunk.readInt();
            mem.getPort28RemapCount()[0] = chunk.readInt();
        }

        chunk = findChunk(RAM_LIMIT_TAG);
        if (chunk != null) {
            chunk.setPnt(0);
            mem.getRam().setUpper(chunk.readInt());
            mem.getRam().setLower(chunk.readInt());
        }

        chunk = findChunk(NUM_FLASH_BREAKS_TAG);
        if (chunk != null) {
            final int numFlashBreaks = chunk.readInt();
            chunk = findChunk(FLASH_BREAKS_TAG);
            if (chunk != null) {
                for (int i = 0; i < numFlashBreaks; ++i) {
                    final int addr = chunk.readInt();
                    final WideAddr waddr = new WideAddr(addr / Memory.PAGE_SIZE, addr % Memory.PAGE_SIZE, false);
                    final int type = chunk.readInt();
                    switch (type) {
                        case JWCoreConstants.MEM_READ_BREAK:
                            mem.setMemReadBreak(waddr);
                            break;
                        case JWCoreConstants.MEM_WRITE_BREAK:
                            // WABBITEMU has "setMemReadBreak" here.
                            mem.setMemWriteBreak(waddr);
                            break;
                        default:
                            mem.setBreak(waddr);
                            break;
                    }
                }
            }
        }

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadTIMER" function.
     *
     * @param time the timer context into which to store loaded data
     * @return true on success
     */
    private boolean loadTIMER(final TimerContext time) {

        final Chunk chunk = findChunk(TIMER_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        time.setTStates(chunk.readLong());
        time.setFreq((int) chunk.readLong());
        time.setElapsed(chunk.readDouble());
        time.setLastTime(chunk.readDouble());

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadLCD" function.
     *
     * @param lcd the LCD into which to store loaded data
     * @return true on success
     */
    private boolean loadLCD(final LCD lcd) {

        final Chunk chunk = findChunk(LCD_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        lcd.setActive(chunk.readInt() != 0);
        lcd.setWordLen(chunk.readInt());
        lcd.setX(chunk.readInt());
        lcd.setY(chunk.readInt());
        lcd.setZ(chunk.readInt());
        lcd.setCursorMode(chunk.readInt());
        lcd.setContrast(chunk.readInt());
        final int baseLevel = chunk.readInt();

        if (this.versionBuild >= NEW_CONTRAST_MODEL_BUILD) {
            lcd.setBaseLevel(baseLevel);
        } else {
            lcd.setModelBaselevel(this.model);
            // we can't rely on the old contrast value, just reset it to the midpoint
            lcd.setContrast(HardwareConstants.LCD_MID_CONTRAST);
        }

        chunk.readBlock(lcd.getDisplay(), HardwareConstants.DISPLAY_SIZE);

        lcd.setFront(chunk.readInt());
        for (int row = 0; row < HardwareConstants.LCD_MAX_SHADES; ++row) {
            for (int col = 0; col < HardwareConstants.DISPLAY_SIZE; ++col) {
                lcd.setQueue(row, col, (int) chunk.readChar());
            }
        }

        lcd.setShades(chunk.readInt());
        lcd.setMode(chunk.readInt());
        lcd.setTime(chunk.readDouble());
        lcd.setUfps(chunk.readDouble());
        lcd.setUfpsLast(chunk.readDouble());
        lcd.setLastGifFrame(chunk.readDouble());
        lcd.setWriteAvg(chunk.readDouble());
        lcd.setWriteLast(chunk.readDouble());

        if (this.versionBuild >= LCD_SCREEN_ADDR_BUILD) {
            lcd.setScreenAddr(chunk.readShort());
        } else {
            // use the default and hope you were not using a custom location
            lcd.setScreenAddr(0xFC00);
        }

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadColorLCD" function.
     *
     * @param lcd the ColorLCD into which to store loaded data
     * @return true on success
     */
    private boolean loadColorLCD(final ColorLCD lcd) {

        final Chunk chunk = findChunk(LCD_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        lcd.setActive(chunk.readInt() != 0);
        lcd.setX(chunk.readInt());
        lcd.setY(chunk.readInt());
        lcd.setZ(chunk.readInt());
        lcd.setCursorMode(chunk.readInt());
        lcd.setContrast(chunk.readInt());
        lcd.setTime(chunk.readDouble());
        lcd.setUfps(chunk.readDouble());
        lcd.setUfpsLast(chunk.readDouble());
        lcd.setLastGifFrame(chunk.readDouble());
        lcd.setWriteAvg(chunk.readDouble());
        lcd.setWriteLast(chunk.readDouble());

        chunk.readBlock(lcd.getDisplay(), HardwareConstants.COLOR_LCD_DISPLAY_SIZE);
        chunk.readBlock(lcd.getQueuedImage(), HardwareConstants.COLOR_LCD_DISPLAY_SIZE);
        chunk.readBlock(lcd.getRegisters(), lcd.getRegisters().length);

        lcd.setCurrentRegister(chunk.readInt());
        lcd.setReadBuffer(chunk.readInt());
        lcd.setWriteBuffer(chunk.readInt());
        lcd.setReadStep(chunk.readInt());
        lcd.setWriteStep(chunk.readInt());
        lcd.setFrameRate(chunk.readInt());
        lcd.setFront(chunk.readInt());

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadLINK" function.
     *
     * @param link the Link into which to store loaded data
     * @return true on success
     */
    private boolean loadLINK(final Link link) {

        final Chunk chunk = findChunk(LINK_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        link.setHost((int) chunk.readChar());

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadSTDINT" function.
     *
     * @param stdint the STDINT into which to store loaded data
     * @return true on success
     */
    private boolean loadSTDINT(final STDINT stdint) {

        final Chunk chunk = findChunk(STDINT_TAG);
        if (chunk == null) {
            return false;
        }

        chunk.setPnt(0);

        stdint.setIntactive((int) chunk.readChar());
        stdint.setLastchk1(chunk.readDouble());
        stdint.setTimermax1(chunk.readDouble());
        stdint.setLastchk2(chunk.readDouble());
        stdint.setTimermax2(chunk.readDouble());
        for (int i = 0; i < 4; ++i) {
            stdint.setFreq(i, chunk.readDouble());
        }
        stdint.setMem(chunk.readInt());
        stdint.setXy(chunk.readInt());

        return true;
    }

    /**
     * Loads a LINKASSIST.
     *
     * @param linkAssist the LINKASSIST into which to store loaded data
     */
    private void loadLinkAssist(final LinkAssist linkAssist) {

        if (linkAssist == null) {
            return;
        }

        final Chunk chunk = findChunk(SE_AUX_TAG);
        if (chunk == null) {
            return;
        }

        final boolean is83p = this.model.ordinal() < EnumCalcModel.TI_83PSE.ordinal() && this.versionMinor == 1;
        if (is83p) {
            linkAssist.setLinkEnable((int) chunk.readChar());
            linkAssist.setIn((int) chunk.readChar());
            linkAssist.setOut((int) chunk.readChar());
            linkAssist.setWorking((int) chunk.readChar());
            linkAssist.setReceiving(chunk.readInt() != 0);
            linkAssist.setRead(chunk.readInt() != 0);
            linkAssist.setReady(chunk.readInt() != 0);
            linkAssist.setError(chunk.readInt() != 0);
            linkAssist.setSending(chunk.readInt() != 0);
            linkAssist.setLastAccess(chunk.readDouble());
            linkAssist.setBit(chunk.readInt());
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadSE_AUX" function.
     *
     * @param cpu   the CPU (needed for compatibility)
     * @param seAux the STDINT into which to store loaded data
     */
    private void loadSEAUX(final CPU cpu, final SEAux seAux) {

        if (seAux == null) {
            return;
        }

        Chunk chunk = findChunk(SE_AUX_TAG);
        if (chunk == null) {
            return;
        }

        final boolean is83p = this.model.ordinal() < EnumCalcModel.TI_83PSE.ordinal() && this.versionMinor == 1;
        if (is83p) {
            // Should be handled by loadLinkAssist
            return;
        }

        seAux.getClock().setEnable((int) chunk.readChar());
        seAux.getClock().setSet((long) chunk.readInt());
        seAux.getClock().setBase((long) chunk.readInt());
        seAux.getClock().setLasttime(chunk.readDouble());

        for (int i = 0; i < 7; i++) {
            seAux.getDelay().setReg(i, (int) chunk.readChar());
        }

        for (int i = 0; i < 6; i++) {
            seAux.getMd5().setReg(i, (long) chunk.readInt());
        }

        seAux.getMd5().setS((int) chunk.readChar());
        seAux.getMd5().setMode((int) chunk.readChar());

        seAux.getLinka().setLinkEnable((int) chunk.readChar());
        seAux.getLinka().setIn((int) chunk.readChar());
        seAux.getLinka().setOut((int) chunk.readChar());
        seAux.getLinka().setWorking((int) chunk.readChar());
        seAux.getLinka().setReceiving(chunk.readInt() != 0);
        seAux.getLinka().setRead(chunk.readInt() != 0);
        seAux.getLinka().setReady(chunk.readInt() != 0);
        seAux.getLinka().setError(chunk.readInt() != 0);
        seAux.getLinka().setSending(chunk.readInt() != 0);
        seAux.getLinka().setLastAccess(chunk.readDouble());
        seAux.getLinka().setBit(chunk.readInt());

        seAux.getXtal().setLastTime(chunk.readDouble());
        seAux.getXtal().setTicks(chunk.readLong());

        for (int i = 0; i < 3; i++) {
            seAux.getXtal().getTimer(i).setLastTstates(chunk.readLong());
            seAux.getXtal().getTimer(i).setLastTicks(chunk.readDouble());
            seAux.getXtal().getTimer(i).setDivisor(chunk.readDouble());
            seAux.getXtal().getTimer(i).setLoop(chunk.readInt() != 0);
            seAux.getXtal().getTimer(i).setInterrupt(chunk.readInt() != 0);
            seAux.getXtal().getTimer(i).setUnderflow(chunk.readInt() != 0);
            seAux.getXtal().getTimer(i).setGenerate(chunk.readInt() != 0);
            seAux.getXtal().getTimer(i).setActive(chunk.readInt() != 0);
            seAux.getXtal().getTimer(i).setClock((int) chunk.readChar());
            seAux.getXtal().getTimer(i).setCount((int) chunk.readChar());
            seAux.getXtal().getTimer(i).setMax((int) chunk.readChar());
        }

        if (this.versionMinor >= 1) {
            // originally this was part of the SE_AUX struct now it's contained in the core, and as
            // such this minor hack
            cpu.setModelBits(chunk.readInt());
        }

        chunk = findChunk(USB_TAG);
        if (chunk == null) {
            return;
        }
        chunk.setPnt(0);

        seAux.getUsb().setUSBLineState((long) chunk.readInt());
        seAux.getUsb().setUSBEvents((long) chunk.readInt());
        seAux.getUsb().setUSBEventMask((long) chunk.readInt());
        seAux.getUsb().setLineInterrupt(chunk.readInt() != 0);
        seAux.getUsb().setProtocolInterrupt(chunk.readInt() != 0);
        seAux.getUsb().setProtocolInterruptEnabled(chunk.readInt() != 0);
        seAux.getUsb().setDevAddress((long) chunk.readInt());
        seAux.getUsb().setPort4A((int) chunk.readChar());
        seAux.getUsb().setPort4C((int) chunk.readChar());
        seAux.getUsb().setPort54((int) chunk.readChar());
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadSlot_Unsafe" function.
     *
     * @param calc the calculator
     * @return true on success
     */
    private boolean loadSlotUnsafe(final Calc calc) {

        if (calc == null || !calc.isActive()) {
            return false;
        }

        final boolean runsave = calc.isRunning();
        calc.setRunning(false);

        final CPU cpu = calc.getCPU();
        final PIOContext pio = cpu.getPIOContext();

        boolean success = loadCPU(cpu);
        if (!success) {
            return false;
        }
        success = loadMEM(calc.getCPU().getMemoryContext());
        if (!success) {
            return false;
        }
        success = loadTIMER(calc.getCPU().getTimerContext());
        if (!success) {
            return false;
        }

        if (calc.getModel().ordinal() >= EnumCalcModel.TI_84PCSE.ordinal()) {
            success = loadColorLCD((ColorLCD) pio.getLcd());
        } else {
            success = loadLCD((LCD) pio.getLcd());
        }
        if (!success) {
            return false;
        }

        success = loadLINK(pio.getLink());
        if (!success) {
            return false;
        }

        success = loadSTDINT(pio.getStdint());
        if (!success) {
            return false;
        }

        if (pio.getLinkAssist() != null) {
            loadLinkAssist(pio.getLinkAssist());
        }
        if (pio.getSeAux() != null) {
            loadSEAUX(cpu, pio.getSeAux());
        }

        calc.setRunning(runsave);

        return true;
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "LoadSlot" function.
     *
     * @param calc the calculator
     * @return true on success
     */
    public boolean loadSlot(final Calc calc) {

        try {
            return loadSlotUnsafe(calc);
        } catch (final IllegalStateException ex) {
            LoggedObject.LOG.warning("Exception loading save state", ex);
            return false;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.cpp, "GetRomOnly" function.
     *
     * @param size the chunk size
     * @return the chunk data
     */
    public int[] getRomOnly(final int[] size) {

        final Chunk chunk = findChunk(ROM_TAG);

        size[0] = 0;
        if (chunk == null) {
            return null;
        }

        size[0] = chunk.getSize();
        return chunk.getData();
    }

    /**
     * WABBITEMU SOURCE: utilities/savestate.c, "ReadSave" function.
     *
     * @param ifile the file to read
     * @return the save state
     * @throws IOException if an error reading occurs
     */
    static SaveState readSave(final FileInputStream ifile) throws IOException {

        // We are going to need to seek, and file input streams may not support mark/reset,
        // so we load the whole file into a byte array, then read from a byte array input
        // stream, which does support mark/reset.
        final byte[] data = readFile(ifile);
        if (data == null) {
            return null;
        }

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            // mark at start, so we can do a "seek" later
            bais.mark(500);

            final byte[] strBytes = new byte[128];

            // read first 8 characters to verify magic tag and see if compressed
            final int detectCmpLen = DETECT_CMP_STR.length();
            bais.read(strBytes, 0, detectCmpLen);
            strBytes[8] = (byte) 0;

            // if file is compressed, and we have no zlib, fail
            boolean mismatch = false;
            for (int i = 0; i < detectCmpLen; ++i) {
                if ((int) strBytes[i] != DETECT_CMP_STR.charAt(i)) {
                    mismatch = true;
                    break;
                }
            }
            if (!mismatch) {
                return null;
            }

            // test for uncompressed file - if not this, fail
            mismatch = false;
            final int detectLen = DETECT_STR.length();
            for (int i = 0; i < detectLen; ++i) {
                if ((int) strBytes[i] != DETECT_STR.charAt(i)) {
                    mismatch = true;
                    break;
                }
            }
            if (mismatch) {
                LoggedObject.LOG.warning("Readsave detect string failed.");
                return null;
            }

            SaveState save = new SaveState();

            final int chunkOffset = fgeti(bais);

            Chunk chunk;

            save.versionMajor = fgeti(bais);
            save.versionMinor = fgeti(bais);
            save.versionBuild = fgeti(bais);

            if (save.versionMajor != CUR_MAJOR) {
                LoggedObject.LOG.warning("Save not compatible at all, sorry");
                return null;
            }

            save.model = EnumCalcModel.values()[fgeti(bais)];
            final int chunkCount = fgeti(bais);

            final byte[] authorBytes = new byte[MAX_SAVESTATE_AUTHOR_LENGTH];
            bais.read(authorBytes);
            int end1 = authorBytes.length;
            while ((int) authorBytes[end1 - 1] == 0) {
                --end1;
            }
            save.author = new String(authorBytes, 0, end1, StandardCharsets.UTF_8);

            final byte[] commentBytes = new byte[MAX_SAVESTATE_COMMENT_LENGTH];
            bais.read(commentBytes);
            int end2 = commentBytes.length;
            while ((int) commentBytes[end2 - 1] == 0) {
                --end2;
            }
            save.comment = new String(commentBytes, 0, end2, StandardCharsets.UTF_8);

            // Now "seek" to the chunk offset
            bais.reset();
            bais.skip((long) (chunkOffset + 8 + 4));

            Arrays.fill(save.chunks, null);
            save.chunkCount = 0;
            for (int i = 0; i < chunkCount; ++i) {
                final char[] tag = new char[4];
                if (bais.available() < 8) {
                    save = null;
                    break;
                }
                tag[0] = (char) bais.read();
                tag[1] = (char) bais.read();
                tag[2] = (char) bais.read();
                tag[3] = (char) bais.read();
                chunk = save.newChunk(tag);
                if (chunk != null) {
                    chunk.setSize(fgeti(bais));

                    final int chunkSize = chunk.getSize();
                    if (bais.available() < chunkSize) {
                        save = null;
                        break;
                    }
                    chunk.setData(new int[chunkSize]);
                    final byte[] bytes = new byte[chunkSize];
                    bais.read(bytes);
                    for (int k = 0; k < chunkSize; ++k) {
                        chunk.getData()[k] = (int) bytes[k];
                    }
                }
            }

            return save;
        }
    }

    /**
     * Read a file into a temporary buffer.
     *
     * @param ifile the file input stream to read
     * @return the byte buffer with the file data; null on error
     */
    private static byte[] readFile(final FileInputStream ifile) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] temp = new byte[1024];

        try {
            int numRead = ifile.read(temp);
            while (numRead > 0) {
                baos.write(temp, 0, numRead);
                numRead = ifile.read(temp);
            }
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to read save file", ex);
            return null;
        }

        return baos.toByteArray();
    }
}
