package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 * <p>
 * ORIGINAL COPYRIGHT NOTICE:
 * <p>
 * Copyright (c) 2010 Spencer Putt. Support for screen-color palettes and variable sized gifs Copyright (c) 2005 Patai
 * Gergely This code is partly based on (i. e. blatantly ripped from) Whirlgif Copyright (c) 1997,1998 by Hans
 * Dinsen-Hansen The algorithms are inspired by those of gifcode.c Copyright (c) 1995,1996 Michael A. Mayer All rights
 * reserved.
 * <p>
 * This software may be freely copied, modified and redistributed without fee provided that above copyright notices are
 * preserved intact on all copies and modified copies.
 * <p>
 * There is no warranty or other guarantee of fitness of this software. It is provided solely "as is". The author(s)
 * disclaim(s) all responsibility and liability with respect to this software's usage or its effect upon hardware or
 * computer systems.
 * <p>
 * The Graphics Interchange format (c) is the Copyright property of Compuserve Incorporated. Gif(sm) is a Service Mark
 * property of Compuserve Incorporated.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.LCD;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A GIF.
 */
public final class Gif {

    /** WABBITEMU SOURCE: utilities/gif.h, "GIF_FRAME" macro. */
    public static final int GIF_FRAME = 2;

    /** WABBITEMU SOURCE: utilities/gif.h, "GIF_END" macro. */
    public static final int GIF_END = 3;

    /** WABBITEMU SOURCE: utilities/gif.h, "GIF_IDLE" macro. */
    private static final int GIF_IDLE = 0;

    /** WABBITEMU SOURCE: utilities/gif.h, "GIF_START" macro. */
    private static final int GIF_START = 1;

    /** Singleton instance. */
    private static final Gif INSTANCE = new Gif();

    /** WABBITEMU SOURCE: utilities/gif.h, "GIF_FRAME_MAX" macro. */
    private static final int GIF_FRAME_MAX = 320 * 240;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "TERMIN" macro. */
    private static final int TERMIN = 'T';

    /** WABBITEMU SOURCE: utilities/gif.cpp, "LOOKUP" macro. */
    private static final int LOOKUP = 'L';

    /** WABBITEMU SOURCE: utilities/gif.cpp, "SEARCH" macro. */
    private static final int SEARCH = 'S';

    /** WABBITEMU SOURCE: utilities/gif.cpp, "ARRNO" macro. */
    private static final int ARRNO = 20;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "BLOKLEN" macro. */
    private static final int BLOKLEN = 255;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "BUFLEN" macro. */
    private static final int BUFLEN = 1000;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "LCD_HIGH_MUL" macro within "gif_writer" function. */
    private static final int LCD_HIGH_MUL = 6;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_write_state" global variable. */
    public int gifWriteState = GIF_IDLE;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "screenshot_autosave" global variable. */
    private boolean screenshotAutosave;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "screenshot_use_increasing" global variable. */
    private boolean screenshotUseIncreasing;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_colors" global variable. */
    private int gifColors = 8;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_base_delay" global variable. */
    private int gifBaseDelay;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_delay" global variable. */
    private int gifDelay;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_xs" global variable. */
    private int gifXs;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_ys" global variable. */
    private int gifYs;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_x" global variable. */
    private int gifFrameX;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_y" global variable. */
    private int gifFrameY;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_xs" global variable. */
    private int gifFrameXs;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_ys" global variable. */
    private int gifFrameYs;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame" global array. */
    private final int[] gifFrame;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_time" global variable. */
    private int gifTime;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_newframe" global variable. */
    private int gifNewframe;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_bw" global variable. */
    private boolean gifBw;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_color" global variable. */
    private boolean gifColor;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "chainlen" global variable. */
    private int chainlen;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "maxchainlen" global variable. */
    private int maxchainlen;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "nbits" global variable. */
    private int nbits;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "need" global variable. */
    private int need = 8;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "empty" global variable. */
    private final GifTree[] empty;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "GifRoot" global variable. */
    private final GifTree gifRoot;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "baseNode," global variable. */
    private GifTree[] topNode;

    /** We don't increment "topNode", just track its index. */
    private int topNodeIndex;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "baseNode," global variable. */
    private GifTree[] baseNode;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "nodeArray" global variable. */
    private GifTree[] nodeArray;

    /** The node array index. */
    private int nodeArrayIndex;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "palette" global variable. */
    private final Map<Integer, Integer> palette;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "last_index" global variable. */
    private int lastIndex;

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_header" static within gif_writer function. */
    private final int[] gifHeaderStart = {'G', 'I', 'F', '8', '9', 'a', 96, 0, 64, 0, 0xf2, 0x0f, 0};

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_info" static within gif_writer function. */
    private final int[] gifInfo = {0x21, 0xff, 0x0b, 'N', 'E', 'T', 'S', 'C', 'A', 'P', 'E', '2', '.', '0', 3, 1, 0,
            0, 0, 0x21, 0xfe, 8, 'W', 'a', 'b', 'b', 'i', 't', 0, 0, 0};

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_img" static within gif_writer function. */
    private final int[] gifImg = {0x21, 0xf9, 4, 5, 11, 0, 0x0f, 0, 0x2c, 0, 0, 0, 0, 96, 0, 64, 0, 0};

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_old" static within gif_writer function. */
    private final int[] gifFrameOld = new int[GIF_FRAME_MAX];

    /** WABBITEMU SOURCE: utilities/gif.cpp, "gif_frame_out" static within gif_writer function. */
    private final int[] gifFrameOut = new int[GIF_FRAME_MAX];

    /**
     * Java can't seek in a file while writing, so we write to a byte array within "gif_writer", then when the array is
     * built, dump to a file.
     */
    private ByteArrayOutputStream baos;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Gif() {

        super();

        this.gifFrame = new int[GIF_FRAME_MAX];
        this.empty = new GifTree[256];
        this.gifRoot = new GifTree(LOOKUP, 0, 0, this.empty);
        for (int i = 0; i < 256; ++i) {
            this.empty[i] = new GifTree(0, 0, 0, null);
        }
        this.palette = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static Gif get() {

        return INSTANCE;
    }

    /**
     * Gets the number of GIF colors.
     *
     * @return the number of colors
     */
    private int getGifColors() {

        return this.gifColors;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_clear_tree" function.
     *
     * @param root the tree root
     */
    private void gifClearTree(final GifTree root) {

        this.maxchainlen = 0;
        this.nodeArray = root.node;
        this.nodeArrayIndex = 0;
        int xxIndex = 0;

        final GifTree[] intermediate = new GifTree[256];
        for (int i = 0; i < 256; ++i) {
            intermediate[i] = new GifTree(0, 0, 0, null);
        }
        for (int i = 0; i < ARRNO; ++i) {
            // simulate memmove - use an intermediate buffer in case ranges overlap
            for (int j = 0; j < 256; ++j) {
                intermediate[j].set(this.empty[j]);
            }
            for (int j = 0; j < 256; ++j) {
                this.nodeArray[xxIndex].set(intermediate[j]);
            }
            xxIndex += 256;
        }

        this.topNode = this.baseNode;
        this.topNodeIndex = 0;

        for (int i = 0; i < 256; ++i) {
            ++this.topNodeIndex;
            final GifTree newNode = this.topNode[this.topNodeIndex];
            root.node[i] = newNode;

            newNode.nxt = null;
            newNode.alt = null;
            newNode.code = i;
            newNode.ix = i;
            newNode.typ = TERMIN;
            newNode.node = this.empty;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_code_to_buffer" function.
     *
     * @param code the code
     * @param n    the N
     * @param buf  the buffer
     * @param pos  the position in the buffer at which to begin
     * @return the index into the buffer
     */
    private int gifCodeToBuffer(final int code, final int n, final int[] buf, final int pos) {

        int mask;
        int bufIndex = pos;
        int theN = n;
        int theCode = code;

        if (theN < 0) {
            if (this.need < 8) {
                ++bufIndex;
                buf[bufIndex] = 0x0;
            }
            this.need = 8;
            return bufIndex;
        }

        while (theN >= this.need) {
            mask = (1 << this.need) - 1;
            buf[bufIndex] += ((mask & theCode) << (8 - this.need)) & 0x00FF;
            ++bufIndex;
            buf[bufIndex] = 0x0;
            theCode = theCode >> this.need;
            theN -= this.need;
            this.need = 8;
        }

        if (theN != 0) {
            mask = (1 << theN) - 1;
            buf[bufIndex] += ((mask & theCode) << (8 - this.need)) & 0x00FF;
            this.need -= theN;
        }

        return bufIndex;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_encode" function.
     *
     * @param fout   the output file stream
     * @param pixels the array of pixels
     * @param siz    the size
     * @return the size
     */
    private int gifEncode(final ByteArrayOutputStream fout, final int[] pixels, final int siz) {

        this.empty[0] = null;
        this.need = 8;

        this.nodeArray = this.empty;
        this.nodeArrayIndex = 0;

        // memmove(++nodeArray, empty, 255 * sizeof(GIF_TREE **));
        for (int i = 255; i >= 0; --i) {
            this.empty[i + i].set(this.empty[i]);
        }

        final int[] buffer = new int[BUFLEN + 1];
        int pos = 1;
        buffer[pos] = 0x0;

        fout.write(8);
        int fsize = 1;

        final int cc = 1 << 8;

        this.baseNode = new GifTree[4094];
        this.topNode = this.baseNode;
        this.topNodeIndex = 0;

        final GifTree first = this.gifRoot;
        first.node = new GifTree[256 * ARRNO];
        this.nodeArray = first.node;
        this.nodeArrayIndex = 0;

        final int lastArrayIndex = 256 * ARRNO - cc;

        gifClearTree(first);

        int cLength = 8 + 1;
        pos = gifCodeToBuffer(cc, cLength, buffer, pos);
        int pixelsIndex = 0;

        GifTree curNode = first;
        int next = cc + 2;
        GifTree newNode;
        while (pixelsIndex < siz) {
            if (curNode.node[pixels[pixelsIndex]] != null) {
                curNode = curNode.node[pixels[pixelsIndex]];
                ++pixelsIndex;
                ++this.chainlen;
                continue;
            } else if (curNode.typ == SEARCH) {
                newNode = curNode.nxt;
                while (newNode.alt != null) {
                    if (newNode.ix == pixels[pixelsIndex]) {
                        break;
                    }
                    newNode = newNode.alt;
                }
                if (newNode.ix == pixels[pixelsIndex]) {
                    ++pixelsIndex;
                    ++this.chainlen;
                    curNode = newNode;
                    continue;
                }
            }

            ++this.topNodeIndex;
            newNode = this.topNode[this.topNodeIndex];

            switch (curNode.typ) {
                case LOOKUP:
                    newNode.nxt = null;
                    newNode.alt = null;
                    curNode.node[pixels[pixelsIndex]] = newNode;
                    break;
                case SEARCH:
                    if (this.nodeArrayIndex != lastArrayIndex) {
                        this.nodeArrayIndex += cc;
                        curNode.node = this.nodeArray;
                        curNode.typ = LOOKUP;
                        curNode.node[pixels[pixelsIndex]] = newNode;
                        curNode.node[(curNode.nxt).ix] = curNode.nxt;
                        newNode.nxt = null;
                        newNode.alt = null;
                        curNode.nxt = null;
                        break;
                    }
                    //$FALL-THROUGH$ fall through
                case TERMIN:
                    newNode.alt = curNode.nxt;
                    newNode.nxt = null;
                    curNode.nxt = newNode;
                    curNode.typ = SEARCH;
                    break;
                default:
                    LoggedObject.LOG.warning("Unhandled node type");
                    break;
            }
            newNode.code = next;
            newNode.ix = pixels[pixelsIndex];
            newNode.typ = TERMIN;
            newNode.node = this.empty;

            pos = gifCodeToBuffer(curNode.code, cLength, buffer, pos);
            if (this.chainlen > this.maxchainlen) {
                this.maxchainlen = this.chainlen;
            }
            this.chainlen = 0;
            if (pos > BLOKLEN) {
                buffer[0] = BLOKLEN;
                for (int index = 0; index < BLOKLEN + 1; ++index) {
                    fout.write(buffer[index]);
                }
                fsize += BLOKLEN + 1;
                buffer[1] = buffer[BLOKLEN + 1];
                buffer[2] = buffer[BLOKLEN + 2];
                buffer[3] = buffer[BLOKLEN + 3];
                buffer[4] = buffer[BLOKLEN + 4];
                pos -= BLOKLEN;
            }
            curNode = first;

            if (next == (1 << cLength)) {
                ++cLength;
            }
            ++next;
            if (next == 0x0fff) {
                gifClearTree(first);
                pos = gifCodeToBuffer(cc, cLength, buffer, pos);
                if (pos > BLOKLEN) {
                    buffer[0] = BLOKLEN;
                    for (int index = 0; index < BLOKLEN + 1; ++index) {
                        fout.write(buffer[index]);
                    }
                    fsize += BLOKLEN + 1;
                    buffer[1] = buffer[BLOKLEN + 1];
                    buffer[2] = buffer[BLOKLEN + 2];
                    buffer[3] = buffer[BLOKLEN + 3];
                    buffer[4] = buffer[BLOKLEN + 4];
                    pos -= BLOKLEN;
                }
                next = cc + 2;
                cLength = 8 + 1;
            }
        }

        pos = gifCodeToBuffer(curNode.code, cLength, buffer, pos);
        if (pos > BLOKLEN - 3) {
            buffer[0] = BLOKLEN - 3;
            for (int index = 0; index < BLOKLEN - 2; ++index) {
                fout.write(buffer[index]);
            }
            fsize += BLOKLEN - 2;
            buffer[1] = buffer[BLOKLEN - 2];
            buffer[2] = buffer[BLOKLEN - 3];
            buffer[3] = buffer[BLOKLEN];
            buffer[4] = buffer[BLOKLEN + 1];
            buffer[5] = buffer[BLOKLEN + 2];
            pos -= BLOKLEN - 3;
        }
        final int eoi = cc + 1;
        pos = gifCodeToBuffer(eoi, cLength, buffer, pos);
        pos = gifCodeToBuffer(0x0, -1, buffer, pos);
        buffer[0] = pos;

        for (int i = 0; i < pos + 1; ++i) {
            fout.write(buffer[i]);
        }
        fsize += pos + 1;

        return fsize;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_palette_color" function.
     *
     * @param idx the index
     * @return the color
     */
    private int gifPaletteColor(final int idx) {

        for (final Map.Entry<Integer, Integer> it : this.palette.entrySet()) {
            if (it.getValue().intValue() == idx) {
                return it.getKey().intValue();
            }
        }

        return 0;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_find_best_match" function.
     *
     * @param rgb the color to match
     * @return the best matching color
     */
    private int gifFindBestMatch(final int rgb) {

        final int r = rgb >> 16;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;
        int bestError = 0xFFFFFF;
        int bestErrorKey = 0;

        for (final Map.Entry<Integer, Integer> it : this.palette.entrySet()) {
            final int newRgb = it.getKey().intValue();

            final int newR = newRgb >> 16;
            final int newG = (newRgb >> 8) & 0xFF;
            final int newB = newRgb & 0xFF;

            final int errorR = newR - r;
            final int errorG = newG - g;
            final int errorB = newB - b;

            final int totalError = errorR * errorR + errorG * errorG + errorB * errorB;
            if (totalError < bestError) {
                bestError = totalError;
                bestErrorKey = it.getValue().intValue();
            }
        }

        return bestErrorKey;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_add_extra_color" function.
     *
     * @param rgb the color to add
     * @return the index at which color was added (or the index of the best existing color if no more spaces at which to
     *         add)
     */
    private int gifAddExtraColor(final int rgb) {

        final int index;
        if (this.lastIndex > 0xFF) {
            index = gifFindBestMatch(rgb);
        } else {
            index = this.lastIndex;
            ++this.lastIndex;
            // NOTE: 0x0F is always black
            if (this.lastIndex == 0x0F) {
                this.lastIndex++;
            }
        }

        this.palette.put(Integer.valueOf(rgb), Integer.valueOf(index));

        return index;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_clear_palette" function.
     */
    private void gifClearPalette() {

        this.palette.clear();
        this.lastIndex = 0;
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.c, "gif_convert_color_to_index" function.
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the index
     */
    private int gifConvertColorToIndex(final int r, final int g, final int b) {

        final int rgb = (r << 16) | (g << 8) | b;
        final Integer it = this.palette.get(Integer.valueOf(rgb));
        if (it != null) {
            return it.intValue();
        }

        return gifAddExtraColor(rgb);
    }

    /**
     * SOURCE: utilities/gif.c, "gif_writer" function.
     *
     * @param shades the number of shades
     */
    private void gifWriter(final int shades) {

        // flags = 1 111 0 010
        final int[] gifHeader = new int[13 + (3 << 8)];
        System.arraycopy(this.gifHeaderStart, 0, gifHeader, 0, this.gifHeaderStart.length);

        switch (this.gifWriteState) {

            case GIF_IDLE:
                break;

            case GIF_START:
                this.gifColors = shades + 1;
                for (int i = 0; i < this.gifColors; ++i) {

                    final double colorRatio = 1.0 - ((double) i / (double) (this.gifColors - 1));
                    if (this.gifColors == 256) {
                        final int color = 0;
                        gifHeader[13 + i * 3] = (color >> 16) & 0x00FF;
                        gifHeader[14 + i * 3] = (color >> 8) & 0x00FF;
                        gifHeader[15 + i * 3] = color & 0x00FF;
                    } else if (this.gifBw) {
                        gifHeader[13 + i * 3] = (int) ((double) 0x00FF * colorRatio);
                        gifHeader[14 + i * 3] = (int) ((double) 0x00FF * colorRatio);
                        gifHeader[15 + i * 3] = (int) ((double) 0x00FF * colorRatio);
                    } else {
                        gifHeader[13 + i * 3] = (int) ((double) (0x9E - (0x9E / LCD_HIGH_MUL)) * colorRatio
                                + (double) (0x9E / LCD_HIGH_MUL));
                        gifHeader[14 + i * 3] = (int) ((double) (0xAB - (0xAB / LCD_HIGH_MUL)) * colorRatio
                                + (double) (0xAB / LCD_HIGH_MUL));
                        gifHeader[15 + i * 3] = (int) ((double) (0x88 - (0x88 / LCD_HIGH_MUL)) * colorRatio
                                + (double) (0x88 / LCD_HIGH_MUL));
                    }
                }

                final int paletteBits;
                if (this.gifColors <= 8) {
                    paletteBits = 3;
                } else if (this.gifColors <= 16) {
                    paletteBits = 4;
                } else {
                    paletteBits = 8;
                }

                gifHeader[10] = 0xF0 | (paletteBits - 1);
                this.gifFrameXs = this.gifXs;
                this.gifFrameYs = this.gifYs;
                this.gifFrameX = 0;
                this.gifFrameY = 0;
                gifHeader[6] = this.gifXs & 0x00FF;
                gifHeader[7] = (this.gifXs >> 8) & 0x00FF;
                gifHeader[8] = this.gifYs & 0x00FF;
                gifHeader[9] = (this.gifYs >> 8) & 0x00FF;

                this.baos = new ByteArrayOutputStream(1000);
                for (int index = 0; index < 13 + (3 * (1 << paletteBits)); ++index) {
                    this.baos.write(gifHeader[index]);
                }
                for (int index = 0; index < 31; ++index) {
                    this.baos.write(this.gifInfo[index]);
                }

                this.gifWriteState = GIF_FRAME;
                this.gifDelay = this.gifBaseDelay;
                this.gifImg[3] = 4;
                System.arraycopy(this.gifFrame, 0, this.gifFrameOld, 0, GIF_FRAME_MAX);
                System.arraycopy(this.gifFrame, 0, this.gifFrameOut, 0, GIF_FRAME_MAX);
                break;

            case GIF_FRAME:
                int i;
                for (i = 0; i < this.gifXs * this.gifYs; ++i) {
                    if (this.gifFrame[i] != this.gifFrameOld[i]) {
                        break;
                    }
                }

                if (i == this.gifXs * this.gifYs) {
                    this.gifDelay += this.gifBaseDelay;
                } else {
                    this.gifImg[3] = 5;
                    this.gifImg[4] = this.gifDelay & 0x00FF;
                    this.gifImg[5] = (this.gifDelay >> 8) & 0x00FF;
                    this.gifImg[9] = this.gifFrameX & 0x00FF;
                    this.gifImg[10] = (this.gifFrameX >> 8) & 0x00FF;
                    this.gifImg[11] = this.gifFrameY & 0x00FF;
                    this.gifImg[12] = (this.gifFrameY >> 8) & 0x00FF;
                    this.gifImg[13] = this.gifFrameXs & 0x00FF;
                    this.gifImg[14] = (this.gifFrameXs >> 8) & 0x00FF;
                    this.gifImg[15] = this.gifFrameYs & 0x00FF;
                    this.gifImg[16] = (this.gifFrameYs >> 8) & 0x00FF;
                    this.gifDelay = this.gifBaseDelay & 0x00FF;

                    if (this.baos == null) {
                        LoggedObject.LOG.warning("attempting to write before opening output stream");
                    } else {
                        for (int index = 0; index < 18; ++index) {
                            this.baos.write(this.gifImg[index]);
                        }
                        i = gifEncode(this.baos, this.gifFrameOut, this.gifFrameXs * this.gifFrameYs);
                        if (i == -1) {
                            this.gifWriteState = GIF_END;
                        } else {
                            this.baos.write(0);
                        }
                    }

                    this.gifFrameX = this.gifXs;
                    this.gifFrameY = this.gifYs;
                    this.gifFrameXs = 0;
                    this.gifFrameYs = 0;
                    for (i = 0; i < this.gifYs; ++i) {
                        for (int j = 0; j < this.gifXs; ++j) {
                            if (this.gifFrame[i * this.gifXs + j] != this.gifFrameOld[i * this.gifXs + j]) {
                                if (this.gifFrameX > j) {
                                    this.gifFrameX = j;
                                }
                                if (this.gifFrameY > i) {
                                    this.gifFrameY = i;
                                }
                                if (this.gifFrameXs < j) {
                                    this.gifFrameXs = j;
                                }
                                if (this.gifFrameYs < i) {
                                    this.gifFrameYs = i;
                                }
                            }
                        }
                    }
                    if (this.gifFrameX == this.gifXs) {
                        this.gifFrameX = 0;
                        this.gifFrameY = 0;
                        this.gifFrameXs = 1;
                        this.gifFrameYs = 1;
                    } else {
                        this.gifFrameXs -= this.gifFrameX - 1;
                        this.gifFrameYs -= this.gifFrameY - 1;
                    }
                    final int k = this.gifFrameY * this.gifXs + this.gifFrameX;
                    for (i = 0; i < this.gifFrameYs; ++i) {
                        for (int j = 0; j < this.gifFrameXs; ++j) {
                            final int l = this.gifFrame[i * this.gifXs + j + k];
                            this.gifFrameOut[i * this.gifFrameXs + j] =
                                    (l == this.gifFrameOld[i * this.gifXs + j + k]) ? 0x0f : l;
                        }
                    }
                    System.arraycopy(this.gifFrame, 0, this.gifFrameOld, 0, GIF_FRAME_MAX);
                }
                break;

            case GIF_END:
                this.gifImg[4] = this.gifDelay & 0x00FF;
                this.gifImg[5] = (this.gifDelay >> 8) & 0x00FF;
                this.gifImg[9] = this.gifFrameX & 0x00FF;
                this.gifImg[10] = (this.gifFrameX >> 8) & 0x00FF;
                this.gifImg[11] = this.gifFrameY & 0x00FF;
                this.gifImg[12] = (this.gifFrameY >> 8) & 0x00FF;
                this.gifImg[13] = this.gifFrameXs & 0x00FF;
                this.gifImg[14] = (this.gifFrameXs >> 8) & 0x00FF;
                this.gifImg[15] = this.gifFrameYs & 0x00FF;
                this.gifImg[16] = (this.gifFrameYs >> 8) & 0x00FF;

                if (this.baos == null) {
                    LoggedObject.LOG.warning("attempting to write before opening output stream");
                } else {
                    for (int index = 0; index < 18; ++index) {
                        this.baos.write(this.gifImg[index]);
                    }
                    i = gifEncode(this.baos, this.gifFrameOut, this.gifFrameXs * this.gifFrameYs);
                    if (i == -1) {
                        this.gifWriteState = GIF_IDLE;
                    } else {
                        this.baos.write(0);
                    }
                    this.baos.write(0x3b);

                    final byte[] fileBuffer = this.baos.toByteArray();
                    this.baos = null;

                    // Write out the special colors table
                    if (this.gifColors == 256) {
                        int index = 13;
                        for (i = 0; i < this.gifColors; ++i) {
                            final int color = gifPaletteColor(i);
                            fileBuffer[index] = (byte) ((color >> 16) & 0xFF);
                            ++index;
                            fileBuffer[index] = (byte) ((color >> 8) & 0xFF);
                            ++index;
                            fileBuffer[index] = (byte) (color & 0xFF);
                            ++index;
                        }
                    }

                    final String screenshotFileName = "wabbitemu.gif";
                    try (final FileOutputStream fp = new FileOutputStream(screenshotFileName, true)) {
                        fp.write(fileBuffer);
                    } catch (final IOException ex) {
                        LoggedObject.LOG.warning("Failed to write GIF screenshot", ex);
                    }
                }

                this.gifWriteState = GIF_IDLE;
                break;

            default:
                LoggedObject.LOG.warning("Unhangled GIF state");
                break;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/screenshothandle.c, "generate_gif_image" struct.
     *
     * @param lcd     the LCD
     * @param gifSize the GIF size (scale)
     * @return true
     */
    private int[] generateGifImage(final AbstractLCDBase lcd, final int gifSize) {

        final int[] gif;

        if (lcd instanceof LCD) {

            final int[] image = ((LCD) lcd).updateImage();

            gif = new int[lcd.getWidth() * gifSize * lcd.getHeight() * gifSize];
            final int gifHeight = lcd.getHeight() * gifSize;
            final int gifWidth = lcd.getDisplayWidth() * gifSize;

            for (int row = 0; row < gifHeight; ++row) {
                for (int col = 0; col < gifWidth; ++col) {
                    int color;
                    if (lcd.getBytesPerPixel() > 1) {
                        final int idx = (row / gifSize) * lcd.getWidth() * lcd.getBytesPerPixel()
                                + (col / gifSize) * lcd.getBytesPerPixel();
                        final int b = image[idx];
                        final int g = image[idx + 1];
                        final int r = image[idx + 2];
                        color = gifConvertColorToIndex(r, g, b);
                    } else {
                        final int part = 255 / get().gifColors;
                        color = image[(row / gifSize) * lcd.getWidth() * lcd.getBytesPerPixel()
                                + (col / gifSize) * lcd.getBytesPerPixel()];
                        color = (color + (part / 2)) / part;
                    }

                    gif[row * gifWidth + col] = color;
                }
            }
        } else {
            // Handle the color LCD
            gif = null;
        }

        return gif;
    }

    /**
     * WABBITEMU SOURCE: utilities/screenshothandle.c, "handle_screenshot" function.
     *
     * @param calc the calculator
     */
    public void handleScreenshot(final Calc calc) {

        int size = 0;

        final boolean runningBackup = calc.isRunning();
        calc.setRunning(false);
        AbstractLCDBase lcd = calc.getCPU().getPIOContext().getLcd();

        int shades = 0;
        if (calc.getModel().ordinal() < EnumCalcModel.TI_84PCSE.ordinal() && lcd != null
                && shades < ((LCD) lcd).getShades()) {
            shades = ((LCD) lcd).getShades();
            size = 2;
        } else if (calc.getModel() == EnumCalcModel.TI_84PCSE) {
            shades = 255;
            size = 1;
        }

        final int calcPos = 0;

        final int gifBaseDelayStart = 4;
        switch (this.gifWriteState) {
            case GIF_IDLE:
                this.gifNewframe = 0;
                break;

            case GIF_START:
                this.gifXs = 0;

                gifClearPalette();

                lcd = calc.getCPU().getPIOContext().getLcd();

                final int gifIndivXs = lcd.getDisplayWidth() * size;
                this.gifBaseDelay = gifBaseDelayStart;
                this.gifTime = 0;
                this.gifNewframe = 1;
                this.gifColors = shades + 1;

                final int[] gif = generateGifImage(lcd, size);

                for (int i = 0; i < this.gifYs; ++i) {
                    for (int j = 0; j < gifIndivXs; ++j) {
                        this.gifFrame[i * this.gifXs + j + calcPos] = gif[i * gifIndivXs + j];
                    }
                }
                break;

            case GIF_FRAME:
                this.gifTime += 1;
                if (this.gifTime >= this.gifBaseDelay) {
                    this.gifTime -= this.gifBaseDelay;
                    this.gifNewframe = 1;

                    lcd = calc.getCPU().getPIOContext().getLcd();
                    final int gifIndivXs2;

                    final int[] theGif = generateGifImage(lcd, size);
                    gifIndivXs2 = lcd.getDisplayWidth() * size;

                    for (int i = 0; i < this.gifYs; ++i) {
                        for (int j = 0; j < gifIndivXs2; ++j) {
                            this.gifFrame[i * this.gifXs + j + calcPos] =
                                    theGif[i * gifIndivXs2 + j];
                        }
                    }
                }
                break;

            case GIF_END:
                this.gifNewframe = 1;
                break;

            default:
                LoggedObject.LOG.warning("Unhandled GIF state");
                break;
        }

        calc.setRunning(runningBackup);

        if (this.gifNewframe != 0) {
            this.gifNewframe = 0;
            gifWriter(shades);
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/gif.cpp, "GIF_TREE" struct.
     */
    private static final class GifTree {

        /** The type. */
        int typ;

        /** The code. */
        int code;

        /** The index. */
        int ix;

        /** The node. */
        GifTree[] node;

        /** The next. */
        GifTree nxt;

        /** The alt. */
        GifTree alt;

        /**
         * Constructs a new {@code GifTree}.
         *
         * @param theType the type
         * @param theCode the code
         * @param theIx   the IX
         * @param theNode the node tree array
         */
        GifTree(final int theType, final int theCode, final int theIx, final GifTree[] theNode) {

            this.typ = theType;
            this.code = theCode;
            this.ix = theIx;
            this.node = theNode;
            this.nxt = null;
            this.alt = null;
        }

        /**
         * Sets the values of this object from another.
         *
         * @param source the source
         */
        void set(final GifTree source) {

            this.typ = source.typ;
            this.code = source.code;
            this.ix = source.ix;
            this.node = (source.node == null) ? null : source.node.clone();
            this.nxt = source.nxt;
            this.alt = source.alt;
        }
    }
}
