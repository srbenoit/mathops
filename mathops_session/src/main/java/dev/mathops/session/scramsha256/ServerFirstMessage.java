package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.HexEncoder;

import java.util.Arrays;
import java.util.random.RandomGenerator;

/**
 * The data that makes up the "server-first" message.
 *
 * <pre>
 * CNONCE       = Data from 'client-first' message
 * SNONCE       = Random 30-byte nonce
 * SALT         = Salt value from credentials associated with 'username' from 'client-first'
 * ITER_COUNT   = Iteration count from credentials associated with 'username' from 'client-first'
 * SERVER_FIRST = CNONCE[30] + SNONCE[30] + "," + SALT[30] + ",i=" + ITER_COUNT[4]
 * </pre>
 */
class ServerFirstMessage {

    /** The server nonce (30 bytes). */
    final byte[] sNonce;

    /** The salt (30 bytes). */
    final byte[] salt;

    /** The iteration count (from 4096 to 9999). */
    final int iterCount;

    /** The raw message. */
    final byte[] serverFirst;

    /** The hex encoding of the assembled 'server-first' message. */
    final String hex;

    /**
     * Parses a {@code ServerFirstMessage} from a client-first message and the user credentials associated with the
     * username in that message.
     *
     * @param theClientFirst the client-first message
     * @param theCredentials the user credentials
     * @param rnd            a source of random numbers
     * @throws IllegalArgumentException if either argument is null or the credentials do not match the username in the
     *                                  client-first message
     */
    ServerFirstMessage(final ClientFirstMessage theClientFirst, final UserCredentials theCredentials,
                       final RandomGenerator rnd) throws IllegalArgumentException {

        if (theClientFirst == null) {
            throw new IllegalArgumentException("ClientFirst message may not be null");
        }
        if (theCredentials == null) {
            throw new IllegalArgumentException("User credentials may not be null");
        }
        if (rnd == null) {
            throw new IllegalArgumentException("Random source may not be null");
        }
        if (theCredentials.normalizedUsername == null) {
            throw new IllegalArgumentException("User credentials did not contain normalized username");
        }
        if (theCredentials.salt == null) {
            throw new IllegalArgumentException("User credentials did not contain salt");
        }
        if (theClientFirst.normalizedUsername == null) {
            throw new IllegalArgumentException("Client first message did not contain normalized username");
        }
        if (theClientFirst.cNonce == null) {
            throw new IllegalArgumentException("Client first message did not contain client NONCE");
        }
        if (!Arrays.equals(theCredentials.normalizedUsername, theClientFirst.normalizedUsername)) {
            throw new IllegalArgumentException("User name in credentials does not match that in client-first");
        }

        this.sNonce = new byte[30];
        rnd.nextBytes(this.sNonce);

        this.salt = new byte[24];
        System.arraycopy(theCredentials.salt, 0, this.salt, 0, 24);

        this.iterCount = theCredentials.iterCount;

        this.serverFirst = new byte[92];
        System.arraycopy(theClientFirst.cNonce, 0, this.serverFirst, 0, 30);
        System.arraycopy(this.sNonce, 0, this.serverFirst, 30, 30);
        int pos = 60;
        this.serverFirst[pos] = CoreConstants.COMMA_CHAR;
        pos++;
        System.arraycopy(this.salt, 0, this.serverFirst, pos, 24);
        pos += 24;
        this.serverFirst[pos] = CoreConstants.COMMA_CHAR;
        pos++;
        this.serverFirst[pos] = 'i';
        pos++;
        this.serverFirst[pos] = '=';
        pos++;

        this.serverFirst[pos] = (byte) ('0' + this.iterCount / 1000);
        pos++;
        this.serverFirst[pos] = (byte) ('0' + this.iterCount % 1000 / 100);
        pos++;
        this.serverFirst[pos] = (byte) ('0' + this.iterCount % 100 / 10);
        pos++;
        this.serverFirst[pos] = (byte) ('0' + this.iterCount % 10);

        this.hex = HexEncoder.encodeLowercase(this.serverFirst);
    }

    /**
     * Constructs a {@code ServerFirstMessage} from a hex representation returned by the server in response to the
     * 'client-first' message, and verifies that its format is correct and that its client nonce matches that from the
     * 'client-first' message, to ensure it should be associated with that request.
     *
     * @param theHex      the hex to parse
     * @param clientFirst the {@code ClientFirstMessage} message
     * @throws IllegalArgumentException if there is an error in the message
     */
    ServerFirstMessage(final String theHex, final ClientFirstMessage clientFirst) throws IllegalArgumentException {

        if (theHex.charAt(0) == '!') {
            throw new IllegalArgumentException(theHex.substring(1));
        }

        this.hex = theHex;
        this.serverFirst = HexEncoder.decode(this.hex);

        if (this.serverFirst.length != 92) {
            throw new IllegalArgumentException("server-first message had invalid length: " + this.serverFirst.length);
        }

        if ((int) this.serverFirst[60] == CoreConstants.COMMA_CHAR
                && (int) this.serverFirst[85] == CoreConstants.COMMA_CHAR
                && (int) this.serverFirst[86] == 'i' && (int) this.serverFirst[87] == '='
                && (int) this.serverFirst[88] >= '0' && (int) this.serverFirst[88] <= '9'
                && (int) this.serverFirst[89] >= '0' && (int) this.serverFirst[89] <= '9'
                && (int) this.serverFirst[90] >= '0' && (int) this.serverFirst[90] <= '9'
                && (int) this.serverFirst[91] >= '0' && (int) this.serverFirst[91] <= '9') {

            // Verify that the client-nonce in the server's message matches ours
            for (int i = 0; i < 30; ++i) {
                if ((int) this.serverFirst[i] != (int) clientFirst.cNonce[i]) {
                    throw new IllegalArgumentException("server-first message had invalid client nonce");
                }
            }
        } else {
            throw new IllegalArgumentException("server-first message had invalid delimiters");
        }

        this.sNonce = new byte[30];
        this.salt = new byte[24];

        System.arraycopy(this.serverFirst, 30, this.sNonce, 0, 30);
        System.arraycopy(this.serverFirst, 61, this.salt, 0, 24);

        this.iterCount = ((int) this.serverFirst[88] - '0') * 1000 + ((int) this.serverFirst[89] - '0') * 100
                + ((int) this.serverFirst[90] - '0') * 10 + (int) this.serverFirst[91] - '0';

        if (this.iterCount < 4096) {
            throw new IllegalArgumentException("server-first message had invalid iteration count: " + this.iterCount);
        }
    }
}
