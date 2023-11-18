package dev.mathops.core.parser;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A utility class to perform base-64 encode and decode functions, per RFC 2045, section 6.8.
 */
public enum Base64 {
    ;

    /** The 64 characters that make up the base-64 alphabet (legal in XML). */
    private static final char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',};

    /** A reverse lookup table, mapping ASCII into byte values. */
    private static final byte[] REVERSE = {(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) 62, (byte) -1, (byte) -1, (byte) -1, (byte) 63, (byte) 52, (byte) 53, (byte) 54, (byte) 55,
            (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5,
            (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15,
            (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24,
            (byte) 25, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 26, (byte) 27,
            (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36,
            (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45,
            (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1,};

    /** The character to use for padding to an even multiple of 3 characters. */
    private static final char PAD = '=';

    /** The number of bytes that get converted into a Base64 block. */
    private static final int BYTES_PER_BLOCK = 3;

    /** The number of characters in a Base64 block. */
    private static final int CHARS_PER_BLOCK = 4;

    /** A byte to indicate padding. */
    private static final byte PAD_INDICATOR = (byte) 99;

    /** Number of blocks to print on a single line. */
    private static final int BLOCKS_PER_LINE = 16;

    /** A byte with the least significant six bits set. */
    private static final byte LS_6_BITS = (byte) 0x3F;

    /** A byte with the least significant four bits set. */
    private static final byte LS_4_BITS = (byte) 0x0F;

    /** A byte with the least significant two bits set. */
    private static final byte LS_2_BITS = (byte) 0x03;

    /** A 2-bit shift. */
    private static final int SHIFT_2 = 2;

    /** A 4-bit shift. */
    private static final int SHIFT_4 = 4;

    /** A 6-bit shift. */
    private static final int SHIFT_6 = 6;

    /**
     * Converts a block of binary data to a base-64 text representation.
     *
     * @param data          the data to encode
     * @param blocksPerLine the number of blocks per line
     * @return the base-64 encoded data
     */
    private static String encode(final byte[] data, final int blocksPerLine) {

        final HtmlBuilder str = new HtmlBuilder((int) ((float) data.length * 1.5f));
        int len = data.length;

        // March through blocks of 3 bytes, converting to 4 characters each
        int inx;
        int count = 0;

        for (inx = BYTES_PER_BLOCK - 1; inx < len; inx += BYTES_PER_BLOCK) {
            final byte byte1 = data[inx - 2];
            final byte byte2 = data[inx - 1];
            final byte byte3 = data[inx];

            str.add(CHARS[(int) byte1 >> SHIFT_2 & (int) LS_6_BITS]);
            str.add(CHARS[((int) byte1 & (int) LS_2_BITS) << SHIFT_4 | (int) byte2 >> SHIFT_4 & (int) LS_4_BITS]);
            str.add(CHARS[((int) byte2 & (int) LS_4_BITS) << SHIFT_2 | (int) byte3 >> SHIFT_6 & (int) LS_2_BITS]);
            str.add(CHARS[(int) byte3 & (int) LS_6_BITS]);

            ++count;

            if (count == blocksPerLine) {
                str.add(CoreConstants.CRLF);
                count = 0;
            }
        }

        // Treat the last bytes (1 byte converts to two characters + 2 pads, 2 bytes convert to
        // three characters + 1 pad)
        len %= BYTES_PER_BLOCK;

        if (len == 1) {
            final byte byte1 = data[inx - 2];

            str.add(CHARS[(int) byte1 >> SHIFT_2 & (int) LS_6_BITS]);
            str.add(CHARS[((int) byte1 & (int) LS_2_BITS) << SHIFT_4]);
            str.add(PAD);
            str.add(PAD);
        } else if (len == 2) {
            final byte byte1 = data[inx - 2];
            final byte byte2 = data[inx - 1];

            str.add(CHARS[(int) byte1 >> SHIFT_2 & (int) LS_6_BITS]);
            str.add(CHARS[((int) byte1 & (int) LS_2_BITS) << SHIFT_4 | (int) byte2 >> SHIFT_4 & (int) LS_4_BITS]);
            str.add(CHARS[((int) byte2 & (int) LS_4_BITS) << SHIFT_2]);
            str.add(PAD);
        }

        return str.toString();
    }

    /**
     * Converts a block of binary data to a base-64 text representation.
     *
     * @param data the data to encode
     * @return the base-64 encoded data
     */
    public static String encode(final byte[] data) {

        return encode(data, BLOCKS_PER_LINE);
    }

    /**
     * Converts data encoded in base-64 back into its original bytes.
     *
     * @param base64 the encoded data
     * @return the decoded bytes; or {@code null} on any error
     * @throws ParsingException if the data could not be decoded
     */
    public static byte[] decode(final String base64) throws ParsingException {

        final byte[] block = new byte[CHARS_PER_BLOCK];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final byte[] raw = base64.replace("\r", CoreConstants.EMPTY).replace("\n", CoreConstants.EMPTY)
                .getBytes(StandardCharsets.UTF_8);
        final int len = raw.length;

        // March through blocks of 4 characters, converting to 3 bytes each
        for (int i = 0; i < len; i += CHARS_PER_BLOCK) {

            nextBlock(raw, i, block);

            byte data = (byte) ((int) block[0] << SHIFT_2 | (int) block[1] >> SHIFT_4);
            baos.write(data);

            if ((int) block[CHARS_PER_BLOCK - 1] < (int) PAD_INDICATOR) {
                data = (byte) ((int) block[1] << SHIFT_4 | (int) block[2] >> SHIFT_2);
                baos.write(data);
                data = (byte) ((int) block[2] << SHIFT_6 | (int) block[CHARS_PER_BLOCK - 1]);
                baos.write(data);
            } else {

                if (len != i + CHARS_PER_BLOCK) {
                    throw new ParsingException(i - CHARS_PER_BLOCK + 1, i + 1, Res.get(Res.B64_PAD_BEFORE_END));
                }

                if ((int) block[CHARS_PER_BLOCK - 2] < (int) PAD_INDICATOR) {
                    data = (byte) ((int) block[1] << SHIFT_4 | (int) block[2] >> SHIFT_2);
                    baos.write(data);

                    if (((int) block[CHARS_PER_BLOCK - 2] & (int) LS_2_BITS) != 0) {
                        throw new ParsingException(i - CHARS_PER_BLOCK + 1, i + 1, Res.get(Res.B64_BAD_THIRD));
                    }
                } else if (((int) block[1] & (int) LS_4_BITS) != 0) {
                    throw new ParsingException(i - CHARS_PER_BLOCK + 1, i + 1, Res.get(Res.B64_BAD_SECOND));
                }
            }
        }

        return baos.toByteArray();
    }

    /**
     * Extracts a block of CHARS_PER_BLOCK bytes from the raw data and verifies that all are within range.
     *
     * @param raw   the raw data block
     * @param first the index of the first of the CHARS_PER_BLOCK bytes
     * @param data  the data array into which to place the CHARS_PER_BLOCK bytes
     * @throws ParsingException if the data could not be decoded
     */
    private static void nextBlock(final byte[] raw, final int first, final byte[] data)
            throws ParsingException {

        for (int i = 0; i < CHARS_PER_BLOCK; ++i) {
            data[i] = raw[first + i];

            if ((int) data[i] < 0) {
                throw new ParsingException(first + i, first + i,
                        Res.fmt(Res.B64_OUT_OF_RANGE, Integer.toString(data[i])));
            }

            if ((int) data[i] == PAD) {
                data[i] = PAD_INDICATOR;
            } else {
                data[i] = REVERSE[data[i]];

                if ((int) data[i] == -1) {
                    throw new ParsingException(first + i, first + i + 1,
                            Res.fmt(Res.B64_BAD_CHAR, Integer.toString(data[i])));
                }
            }
        }

        if ((int) data[0] == (int) PAD_INDICATOR) {
            throw new ParsingException(first, first + 1, Res.get(Res.B64_BYTE1_PAD));
        }

        if ((int) data[1] == (int) PAD_INDICATOR) {
            throw new ParsingException(first + 1, first + 2, Res.get(Res.B64_BYTE2_PAD));
        }
    }
}
