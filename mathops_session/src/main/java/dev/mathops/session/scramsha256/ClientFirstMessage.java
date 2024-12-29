package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.HexEncoder;

import java.util.random.RandomGenerator;

/**
 * The "client-first" message in a SCRAM-SHA-256 authentication interchange.
 *
 * <pre>
 * NORM_USERNAME = Normalize(username)
 * CNONCE        = Random 30-byte nonce
 * CLIENT_FIRST  = NORM_USERNAME + "," + CNONCE
 * </pre>
 */
class ClientFirstMessage {

    /** The normalized username. */
    final byte[] normalizedUsername;

    /** The client nonce (30 bytes). */
    final byte[] cNonce;

    /** The raw 'client-first' message. */
    final byte[] clientFirst;

    /** The hex encoding of the assembled 'client-first' message. */
    final String hex;

    /**
     * Constructs a new {@code ClientFirstMessage}.
     *
     * @param username the username
     * @param rnd      a source of random numbers
     * @throws IllegalArgumentException of either argument is null or the username is empty
     */
    ClientFirstMessage(final CharSequence username, final RandomGenerator rnd) {

        if (username == null) {
            throw new IllegalArgumentException("Username may not be null");
        }
        if (rnd == null) {
            throw new IllegalArgumentException("Random source may not be null");
        }

        this.normalizedUsername = ScramUtils.normalize(username);
        if (this.normalizedUsername.length < 1) {
            throw new IllegalArgumentException("Username may not be empty");
        }

        this.cNonce = new byte[30];
        rnd.nextBytes(this.cNonce);

        this.clientFirst = new byte[31 + this.normalizedUsername.length];

        System.arraycopy(this.normalizedUsername, 0, this.clientFirst, 0,
                this.normalizedUsername.length);
        int pos = this.normalizedUsername.length;
        this.clientFirst[pos] = CoreConstants.COMMA_CHAR;
        ++pos;
        System.arraycopy(this.cNonce, 0, this.clientFirst, pos, 30);

        this.hex = HexEncoder.encodeLowercase(this.clientFirst);
    }

    /**
     * Parses a {@code ClientFirstMessage} from its hex representation.
     *
     * @param theHex the hex to parse
     * @throws IllegalArgumentException if the message format is not valid
     */
    ClientFirstMessage(final String theHex) throws IllegalArgumentException {

        this.hex = theHex;
        this.clientFirst = HexEncoder.decode(theHex);

        final int len = this.clientFirst.length;
        if (len < 32 || (int) this.clientFirst[len - 31] != CoreConstants.COMMA_CHAR) {
            throw new IllegalArgumentException("Invalid message data");
        }

        this.normalizedUsername = new byte[len - 31];
        this.cNonce = new byte[30];

        System.arraycopy(this.clientFirst, 0, this.normalizedUsername, 0, len - 31);
        System.arraycopy(this.clientFirst, len - 30, this.cNonce, 0, 30);
    }

    ///**
    // * Test code.
    // *
    // * @param args command-line arguments
    // */
    // public static void main(final String... args) {
    //
    // final Random rnd = new Random(System.currentTimeMillis());
    //
    // final ClientFirstMessage msg1 = new ClientFirstMessage("steve.benoit", rnd);
    // final ClientFirstMessage msg2 = new ClientFirstMessage(msg1.hex);
    //
    // if (!Arrays.equals(msg1.normalizedUsername, msg2.normalizedUsername)) {
    // Log.warning("Normalized username values do not match");
    // }
    // if (!Arrays.equals(msg1.cNonce, msg2.cNonce)) {
    // Log.warning("NONCE values do not match");
    // }
    // if (!Arrays.equals(msg1.clientFirst, msg2.clientFirst)) {
    // Log.warning("Raw messages do not match");
    // }
    // if (!msg1.hex.equals(msg2.hex)) {
    // Log.warning("Hex encodings do not match");
    // }
    // Log.info("VALID: ", new String(msg1.normalizedUsername),
    // ": ", msg1.hex);
    // }
}
