package dev.mathops.app.passwordhash;

import dev.mathops.core.EqualityTests;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.Base64;
import dev.mathops.core.parser.HexEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A utility class that accepts a salt string (referred to here as SALT) and a password string, and performs the
 * following steps:
 * <ul>
 * <li>Converts the salt string to a byte array using the UTF-8 character encoding (call this SALT)
 * <li>Converts the password string to a byte array using the UTF-8 character encoding
 * <li>Computes the SHA-256 hash of the password byte array (call this HASH)
 * <li>Computes the SHA-235 hash of the concatenation of SALT and HASH (in that order).
 * </ul>
 * This salted hash may be stored (along with a login name and the salt value) for later validation
 * of passwords.
 */
final class SaltedHasher {

    /** The SHA-256 digest. */
    private final MessageDigest digest;

    /**
     * Constructs a new {@code SaltedHasher}.
     */
    SaltedHasher() {

        MessageDigest sha;

        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException ex) {
            Log.severe(ex);
            sha = null;
        }

        this.digest = sha;
    }

    /**
     * Generates a random salt value of a specified length, with 6 bits of information per character. The result is
     * base64 encoded.
     *
     * @param len the length (in characters)
     * @return the generated salt value
     * @throws NoSuchAlgorithmException if the secure random number generator cannot be created
     */
    static String makeRandomSalt(final int len) throws NoSuchAlgorithmException {

        final SecureRandom rnd = SecureRandom.getInstanceStrong();
        final byte[] data = new byte[len];
        rnd.nextBytes(data);

        return Base64.encode(data);

    }

    /**
     * Computes the salted hash.
     *
     * @param salt     the salt value
     * @param password the password
     * @return the 32-byte SHA-256 hash of the concatenation of the salt value (as a UTF-8 byte array) and the SHA-256
     *         hash of the password (as a UTF-8 byte array)
     */
    byte[] compute(final String salt, final String password) {

        if (EqualityTests.isNullOrEmpty(salt) || EqualityTests.isNullOrEmpty(password)) {
            throw new IllegalArgumentException();
        }

        final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        // Do all computations that involve the digest in a synchronized block so multiple threads
        // don't work against the same message digest.
        synchronized (this) {

            // Compute the SHA-256 hash of the password
            this.digest.reset();
            final byte[] passwordHash = this.digest.digest(passwordBytes);

            // Concatenate the salt bytes and the password hash bytes
            final byte[] concatenated = new byte[saltBytes.length + passwordHash.length];
            System.arraycopy(saltBytes, 0, concatenated, 0, saltBytes.length);
            System.arraycopy(passwordHash, 0, concatenated, saltBytes.length, passwordHash.length);

            // Compute the SHA-hash of (Salt + PasswordHash)
            this.digest.reset();
            return this.digest.digest(concatenated);
        }
    }

    /**
     * Executes the utility.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        try {
            final String salt = makeRandomSalt(24);
            Log.fine(salt);

            final SaltedHasher gen = new SaltedHasher();
            final byte[] hash = gen.compute(salt, "this-is-the-password");
            final String encoded = HexEncoder.encodeLowercase(hash);
            Log.fine(encoded);
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
        }
    }
}
