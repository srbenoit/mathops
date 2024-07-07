package dev.mathops.session.scramsha256;

/**
 * Credentials associated with a username. These credentials are loaded from the "logins" table and carry the
 * information needed to perform SCRAM-SHA-256 authentication.
 */
public final class UserCredentials {

    /** The user's role. */
    public final String role;

    /** The normalized username. */
    final byte[] normalizedUsername;

    /** A 30-byte salt, randomly selected for each user. */
    public final byte[] salt;

    /** The iteration count (from 4096 to 9999). */
    final int iterCount;

    /** A 32-byte stored key. */
    final byte[] storedKey;

    /** A 32-byte server key. */
    final byte[] serverKey;

    /**
     * Constructs a new {@code UserCredentials}.
     *
     * @param theRole      the user's role
     * @param username     the username
     * @param theSalt      the salt (24 bytes)
     * @param theStoredKey the stored key (32 bytes)
     * @param theServerKey the server key (32 bytes)
     * @param iterations   the number of iterations
     * @throws IllegalArgumentException if any argument is null, the username or password is empty, or the number of
     *                                  iterations is less than 4096 or greater than 9999
     */
    UserCredentials(final String theRole, final CharSequence username,
                    final byte[] theSalt, final byte[] theStoredKey, final byte[] theServerKey,
                    final int iterations) throws IllegalArgumentException {

        if (theRole == null || theRole.isEmpty()) {
            throw new IllegalArgumentException("Role may not be null or empty");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username may not be null or empty");
        }
        if (theSalt == null || theSalt.length != 24) {
            throw new IllegalArgumentException("24-byte salt must be provided");
        }
        if (theStoredKey == null || theStoredKey.length != 32) {
            throw new IllegalArgumentException("32-byte stored key must be provided");
        }
        if (theServerKey == null || theServerKey.length != 32) {
            throw new IllegalArgumentException("32-byte server key must be provided");
        }
        if (iterations < 4096 || iterations > 9999) {
            throw new IllegalArgumentException("Iterations must be in [4096, 9999]");
        }

        this.role = theRole;
        this.normalizedUsername = ScramUtils.normalize(username);
        this.salt = theSalt.clone();
        this.iterCount = iterations;
        this.storedKey = theStoredKey.clone();
        this.serverKey = theServerKey.clone();
    }
}
