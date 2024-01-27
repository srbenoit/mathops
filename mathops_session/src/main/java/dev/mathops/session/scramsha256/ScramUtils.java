package dev.mathops.session.scramsha256;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.unicode.UnicodeCharacter;
import dev.mathops.commons.unicode.UnicodeCharacterSet;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities needed for the SCRAM-SHA256 authentication protocol.
 */
/* default */  enum ScramUtils {
    ;

    /** The Unicode character set. */
    private static final UnicodeCharacterSet UNICODE = UnicodeCharacterSet.getInstance();

    /**
     * Performs normalization of usernames and passwords as specified in RFC 5802, which references RFC 4013, which in
     * turn references RFC 3454 and Unicode Standard Annex #15.
     *
     * @param toNormalize the string to normalize
     * @return the normalized string, in UTF-8
     */
    /* default */
    static byte[] normalize(final CharSequence toNormalize) {

        final int[] codePoints = toNormalize.codePoints().toArray();

        final StringBuilder mapped = new StringBuilder(codePoints.length);

        for (final int cp : codePoints) {
            // "Map to nothing" table B.1 from RFC 3454
            if (cp == 0x00AD || cp == 0x034F || cp == 0x1806 || cp == 0x180B || cp == 0x180C || cp == 0x180D ||
                    cp == 0x200B || cp == 0x200C || cp == 0x200D || cp == 0x2060 || cp == 0xFE00 || cp == 0xFE01 ||
                    cp == 0xFE02 || cp == 0xFE03 || cp == 0xFE04 || cp == 0xFE05 || cp == 0xFE06 || cp == 0xFE07 ||
                    cp == 0xFE08 || cp == 0xFE09 || cp == 0xFE0A || cp == 0xFE0B || cp == 0xFE0C || cp == 0xFE0D ||
                    cp == 0xFE0E || cp == 0xFE0F || cp == 0xFEFF) {
                continue;
            }

            // "Map to space" table C.1.2 from RFC 3454
            if (cp == 0x00A0 || cp == 0x1680 || cp == 0x2000 || cp == 0x2001 || cp == 0x2002 || cp == 0x2003 ||
                    cp == 0x2004 || cp == 0x2005 || cp == 0x2006 || cp == 0x2007 || cp == 0x2008 || cp == 0x2009 ||
                    cp == 0x200A || cp == 0x202F || cp == 0x205F || cp == 0x3000) {
                mapped.appendCodePoint(0x20);
            } else {
                decomp(cp, mapped);
            }
        }

        return mapped.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Recursively decomposes a Unicode code point.
     *
     * @param codePoint the code point
     * @param mapped    the {@code StringBuilder} to which to append decomposed code points
     */

    private static void decomp(final int codePoint, final StringBuilder mapped) {

        final UnicodeCharacter cp = UNICODE.getCharacter(codePoint);

        if (cp == null) {
            mapped.appendCodePoint(codePoint);
        } else{
            final Integer[] decomp = cp.getDecompMappingCodePoints();

            if (decomp == null) {
                mapped.appendCodePoint(codePoint);
            } else {
                // This includes compatibility decompositions
                for (final Integer i : decomp) {
                    decomp(i.intValue(), mapped);
                }
            }
        }
    }

    /**
     * Computes the SHA-256 hash of a string.
     *
     * @param stringBytes the string (of any nonzero size)
     * @return the hash (32 bytes in length)
     */
    /* default */
    static byte[] sha_256(final byte[] stringBytes) {

        try {
            final MessageDigest dig = MessageDigest.getInstance("SHA-256");
            return dig.digest(stringBytes);
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
            return null;
        }
    }

    /**
     * Performs the HMAC-SHA-256 keyed hash algorithm defined in RFC 2104.
     *
     * @param key the key (of any nonzero size)
     * @param stringBytes the string (of any nonzero size)
     * @return the keyed hash (32 bytes in length)
     */
    /* default */
    static byte[] hmac_sha_256(final byte[] key, final byte[] stringBytes) {

        try {
            final MessageDigest dig = MessageDigest.getInstance("SHA-256");

            final byte[] actualKey;
            if (key.length <= 64) {
                actualKey = key;
            } else {
                actualKey = dig.digest(key);
            }

            final byte[] keyXorIpad = new byte[64];
            final byte[] keyXorOpad = new byte[64];
            System.arraycopy(actualKey, 0, keyXorIpad, 0, Math.min(64, actualKey.length));
            System.arraycopy(actualKey, 0, keyXorOpad, 0, Math.min(64, actualKey.length));
            for (int i = 0; i < 64; ++i) {
                keyXorIpad[i] = (byte) ((int) keyXorIpad[i] ^ 0x36);
                keyXorOpad[i] = (byte) ((int) keyXorOpad[i] ^ 0x5C);
            }

            final byte[] arg1 = new byte[64 + stringBytes.length];
            System.arraycopy(keyXorIpad, 0, arg1, 0, 64);
            System.arraycopy(stringBytes, 0, arg1, 64, stringBytes.length);
            final byte[] inner = dig.digest(arg1);

            final byte[] arg2 = new byte[64 + inner.length];
            System.arraycopy(keyXorOpad, 0, arg2, 0, 64);
            System.arraycopy(inner, 0, arg2, 64, inner.length);

            return dig.digest(arg2);
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
            return null;
        }
    }

    /**
     * Computes the "HI" iterated hash of a string with a salt and iteration count.
     *
     * @param stringBytes       the string to hash
     * @param salt      the salt
     * @param iterCount the iteration count
     * @return the iterated hash
     */
    /* default */
    static byte[] hi(final byte[] stringBytes, final byte[] salt, final int iterCount) {

        final byte[] u1str = new byte[salt.length + 4];
        System.arraycopy(salt, 0, u1str, 0, salt.length);
        u1str[salt.length] = (byte) (iterCount >> 24);
        u1str[salt.length + 1] = (byte) (iterCount >> 16);
        u1str[salt.length + 2] = (byte) (iterCount >> 8);
        u1str[salt.length + 3] = (byte) iterCount;

        final byte[] hi = new byte[32];

        final byte[][] u = new byte[iterCount][];

        u[0] = hmac_sha_256(stringBytes, u1str);
        System.arraycopy(u[0], 0, hi, 0, 32);

        for (int i = 1; i < iterCount; ++i) {
            u[i] = hmac_sha_256(stringBytes, u[i - 1]);

            for (int j = 0; j < 32; ++j) {
                hi[j] = (byte) ((int) hi[j] ^ (int) u[i][j]);
            }
        }

        return hi;
    }
}
