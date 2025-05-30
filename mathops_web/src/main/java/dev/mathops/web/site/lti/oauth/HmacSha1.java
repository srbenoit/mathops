package dev.mathops.web.site.lti.oauth;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementation of HMAC-SHA-1.
 */
public enum HmacSha1 {
    ;

    // /** Hex characters, used in percent encoding */
    // private static final String UC_HEX = "0123456789ABCDEF";

    /** Hex characters, used in percent encoding. */
    private static final String LC_HEX = "0123456789abcdef";

    /** A zero-length array. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Performs HMAC-SHA1 digest per RFC 2104.
     *
     * @param key  the key
     * @param text the text
     * @return the digest
     */
    public static byte[] hmacSha1(final byte[] key, final byte[] text) {

        byte[] result;

        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA1");

            final byte[] actualKey;
            if (key.length <= 64) {
                actualKey = key;
            } else {
                actualKey = sha1.digest(key);
            }

            final byte[] ipad = new byte[64];
            final byte[] opad = new byte[64];
            Arrays.fill(ipad, (byte) 0x36);
            Arrays.fill(opad, (byte) 0x5C);

            final byte[] keyXorOpad = new byte[64];
            final byte[] keyXorIpad = new byte[64];
            System.arraycopy(actualKey, 0, keyXorOpad, 0, Math.min(64, actualKey.length));
            System.arraycopy(actualKey, 0, keyXorIpad, 0, Math.min(64, actualKey.length));
            for (int i = 0; i < 64; ++i) {
                keyXorOpad[i] = (byte) ((int) keyXorOpad[i] ^ 0x5C);
                keyXorIpad[i] = (byte) ((int) keyXorIpad[i] ^ 0x36);
            }

            final byte[] inner = new byte[64 + text.length];
            System.arraycopy(keyXorIpad, 0, inner, 0, 64);
            System.arraycopy(text, 0, inner, 64, text.length);

            final byte[] text2 = sha1.digest(inner);

            final byte[] outer = new byte[64 + text2.length];
            System.arraycopy(keyXorOpad, 0, outer, 0, 64);
            System.arraycopy(text2, 0, outer, 64, text2.length);

            result = sha1.digest(outer);
        } catch (final NoSuchAlgorithmException ex) {
            // TODO Auto-generated catch block
            Log.warning(ex);
            result = ZERO_LEN_BYTE_ARR;
        }

        return result;
    }

    /**
     * Generates a hex string representation of an array of bytes.
     *
     * @param data the byte array
     * @return the hex representation
     */
    private static String bytesToLCHex(final byte[] data) {

        final char[] hex = new char[(data.length << 1)];
        int pos = 0;
        for (final int datum : data) {
            hex[pos] = LC_HEX.charAt(datum >> 4 & 0x0F);
            ++pos;
            hex[pos] = LC_HEX.charAt(datum & 0x0F);
            ++pos;
        }

        return new String(hex);
    }

    /**
     * Main method with test vectors.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final byte[] key1 = new byte[20];
        Arrays.fill(key1, (byte) 0x0b);
        final byte[] text1 = "Hi There".getBytes(StandardCharsets.UTF_8);
        final byte[] digest1 = hmacSha1(key1, text1);
        final String hex1 = bytesToLCHex(digest1);

        if ("b617318655057264e28bc0b6fb378c8ef146be00".equals(hex1)) {
            Log.fine("TEST 1: PASSED");
        } else {
            Log.fine("TEST 1: FAILED" + key1.length
                    + CoreConstants.SLASH + text1.length);
        }

        final byte[] key2 = {'J', 'e', 'f', 'e'};
        final byte[] text2 = "what do ya want for nothing?"
                .getBytes(StandardCharsets.UTF_8);
        final byte[] digest2 = hmacSha1(key2, text2);
        final String hex2 = bytesToLCHex(digest2);

        if ("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79".equals(hex2)) {
            Log.fine("TEST 2: PASSED");
        } else {
            Log.fine("TEST 2: FAILED" + key2.length
                    + CoreConstants.SLASH + text2.length);
        }

        final byte[] key3 = new byte[20];
        Arrays.fill(key3, (byte) 0xaa);
        final byte[] text3 = new byte[50];
        Arrays.fill(text3, (byte) 0xdd);
        final byte[] digest3 = hmacSha1(key3, text3);
        final String hex3 = bytesToLCHex(digest3);

        if ("125d7342b9ac11cd91a39af48aa17b4f63f175d3".equals(hex3)) {
            Log.fine("TEST 3: PASSED");
        } else {
            Log.fine("TEST 3: FAILED " + key3.length
                    + CoreConstants.SLASH + text3.length);
        }

        final byte[] key4 = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17,
                (byte) 0x18, (byte) 0x19};

        final byte[] text4 = new byte[50];
        Arrays.fill(text4, (byte) 0xcd);
        final byte[] digest4 = hmacSha1(key4, text4);
        final String hex4 = bytesToLCHex(digest4);

        if ("4c9007f4026250c6bc8414f9bf50c86c2d7235da".equals(hex4)) {
            Log.fine("TEST 4: PASSED");
        } else {
            Log.fine("TEST 4: FAILED" + key4.length + CoreConstants.SLASH + text4.length);
        }

        final byte[] key5 = new byte[20];
        Arrays.fill(key5, (byte) 0x0c);
        final byte[] text5 = "Test With Truncation".getBytes(StandardCharsets.UTF_8);
        final byte[] digest5 = hmacSha1(key5, text5);
        final String hex5 = bytesToLCHex(digest5);

        if ("4c1a03424b55e07fe7f27be1d58bb9324a9a5a04".equals(hex5)) {
            Log.fine("TEST 5: PASSED");
        } else {
            Log.fine("TEST 5: FAILED" + key5.length + CoreConstants.SLASH + text5.length);
        }

        final byte[] key6 = new byte[80];
        Arrays.fill(key6, (byte) 0xaa);
        final byte[] text6 = "Test Using Larger Than Block-Size Key - Hash Key First".getBytes(StandardCharsets.UTF_8);
        final byte[] digest6 = hmacSha1(key6, text6);
        final String hex6 = bytesToLCHex(digest6);

        if ("aa4ae5e15272d00e95705637ce8a3b55ed402112".equals(hex6)) {
            Log.fine("TEST 6: PASSED");
        } else {
            Log.fine("TEST 6: FAILED" + key6.length + CoreConstants.SLASH + text6.length);
        }

        final byte[] key7 = new byte[80];
        Arrays.fill(key7, (byte) 0xaa);
        final byte[] text7 = "Test Using Larger Than Block-Size Key and Larger Than One Block-Size Data"
                .getBytes(StandardCharsets.UTF_8);
        final byte[] digest7 = hmacSha1(key7, text7);
        final String hex7 = bytesToLCHex(digest7);

        if ("e8e99d0f45237d786d6bbaa7965c7808bbff1a91".equals(hex7)) {
            Log.fine("TEST 7: PASSED");
        } else {
            Log.fine("TEST 7: FAILED" + key7.length + CoreConstants.SLASH + text7.length);
        }
    }
}
