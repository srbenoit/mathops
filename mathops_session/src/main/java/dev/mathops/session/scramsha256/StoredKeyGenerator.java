package dev.mathops.session.scramsha256;

import dev.mathops.core.log.Log;
import dev.mathops.core.parser.Base64;
import dev.mathops.core.parser.HexEncoder;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A utility to generate a salt and stored key for a user based on a password.
 */
enum StoredKeyGenerator {
    ;

    /**
     * Generates a random salt value of a specified length, with 6 bits of information per character. The result is
     * base64 encoded.
     *
     * @param length the length (in characters)
     * @return the generated salt value
     * @throws NoSuchAlgorithmException if the secure random number generator cannot be created
     */
    private static byte[] makeRandomSalt(final int length) throws NoSuchAlgorithmException {

        final SecureRandom rnd = SecureRandom.getInstanceStrong();
        final byte[] data = new byte[length];
        rnd.nextBytes(data);

        return data;

    }

    /**
     * Computes the salted password.
     *
     * @param salt       the salt value
     * @param iterations the number of iterations
     * @param password   the password
     * @return the 32-byte SHA-256 hash of the concatenation of the salt value (as a UTF-8 byte array) and the SHA-256
     *         hash of the password (as a UTF-8 byte array)
     */
    private static byte[] computeSaltedPassword(final byte[] salt, final int iterations, final CharSequence password) {

        final byte[] normPassword = ScramUtils.normalize(password);

        return ScramUtils.hi(normPassword, salt, iterations);
    }

    /**
     * Computes the stored key.
     *
     * @param saltedPassword the salted password
     * @return the 32-byte SHA-256 hash of the concatenation of the salt value (as a UTF-8 byte array) and the SHA-256
     *         hash of the password (as a UTF-8 byte array)
     */
    private static byte[] computeStoredKey(final byte[] saltedPassword) {

        final byte[] clientKey = ScramUtils.hmac_sha_256(saltedPassword, "Client Key".getBytes(StandardCharsets.UTF_8));

        return ScramUtils.sha_256(clientKey);
    }

    /**
     * Executes the utility.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        try {
            final byte[] salt = makeRandomSalt(24);
            final byte[] saltedPassword = computeSaltedPassword(salt, UserCredentialsManager.ITERATIONS, "thinflation");

            final byte[] storedKey = computeStoredKey(saltedPassword);
            final byte[] serverKey = ScramUtils.hmac_sha_256(saltedPassword, "Server Key".getBytes(StandardCharsets.UTF_8));

            final String saltBase64 = Base64.encode(salt);
            final String storedKeyHex = HexEncoder.encodeUppercase(storedKey);
            final String serverKeyHex = HexEncoder.encodeUppercase(serverKey);

            Log.fine("Salt base64 is '", saltBase64, "'");
            Log.fine("Stored key hex '", storedKeyHex, "'");
            Log.fine("Server key hex '", serverKeyHex, "'");

        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
        }
    }
}
