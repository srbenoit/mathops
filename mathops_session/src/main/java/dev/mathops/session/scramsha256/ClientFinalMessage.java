package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.HexEncoder;
import dev.mathops.commons.log.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The data that makes up the "client-final" message.
 *
 * <pre>
 * SALTED_PASSWORD = Hi(Normalize(password), SALT, ITER_COUNT)
 * CLIENT_KEY      = HMAC(SALTED_PASSWORD, "Client Key")
 * STORED_KEY      = H(CLIENT_KEY)
 * AUTH_MESSAGE    = CLIENT_FIRST_MESSAGE + "," + SERVER_FIRST_MESSAGE + "," + CNONCE + SNONCE
 * CLIENT_SIG      = HMAC(STORED_KEY, AUTH_MESSAGE)
 * CLIENT_PROOF    = CLIENT_KEY XOR CLIENT_SIG
 * CLIENT_FINAL    = CNONCE + SNONCE + "," + CLIENT_PROOF
 * </pre>
 */
class ClientFinalMessage {

    /** The authentication message. */
    final byte[] authMessage;

    /** The client signature. */
    private final byte[] clientSig;

    /** The client proof. */
    private final byte[] clientProof;

    /** The assembled 'client-final' message. */
    private final byte[] clientFinal;

    /** The hex encoding of the assembled 'client-final' message. */
    final String hex;

    /**
     * Constructs a new {@code ClientFinalMessage}.
     *
     * @param password    the entered password
     * @param clientFirst the client-first message
     * @param serverFirst the server-first message
     */
    ClientFinalMessage(final CharSequence password, final ClientFirstMessage clientFirst,
                       final ServerFirstMessage serverFirst) {

        final byte[] normPassword = ScramUtils.normalize(password);

        final byte[] saltedPassword = ScramUtils.hi(normPassword, serverFirst.salt, serverFirst.iterCount);

        final byte[] clientKey = ScramUtils.hmac_sha_256(saltedPassword, "Client Key".getBytes(StandardCharsets.UTF_8));

        final byte[] storedKey = ScramUtils.sha_256(clientKey);

        this.authMessage = assembleAuthMessage(clientFirst, serverFirst);

        this.clientSig = storedKey == null ? null : ScramUtils.hmac_sha_256(storedKey, this.authMessage);

        this.clientProof = new byte[32];
        if (clientKey != null && this.clientSig != null) {
            for (int i = 0; i < 32; ++i) {
                this.clientProof[i] = (byte) ((int) clientKey[i] ^ (int) this.clientSig[i]);
            }
        }

        this.clientFinal = new byte[93];
        System.arraycopy(clientFirst.cNonce, 0, this.clientFinal, 0, 30);
        System.arraycopy(serverFirst.sNonce, 0, this.clientFinal, 30, 30);
        this.clientFinal[60] = CoreConstants.COMMA_CHAR;
        System.arraycopy(this.clientProof, 0, this.clientFinal, 61, 32);

        this.hex = HexEncoder.encodeLowercase(this.clientFinal);
    }

    /**
     * Parses a {@code ClientFirstMessage} from its hex representation.
     *
     * @param theHex      the hex to parse
     * @param clientFirst the client-first message
     * @param serverFirst the server-first message
     * @param credentials the user credentials
     * @throws IllegalArgumentException if the message format is not valid
     */
    ClientFinalMessage(final String theHex, final ClientFirstMessage clientFirst,
                       final ServerFirstMessage serverFirst, final UserCredentials credentials)
            throws IllegalArgumentException {

        this.hex = theHex;
        this.clientFinal = HexEncoder.decode(theHex);

        if (this.clientFinal.length != 93) {
            throw new IllegalArgumentException("client-final message had invalid length: " + this.clientFinal.length);
        }

        if ((int) this.clientFinal[60] == CoreConstants.COMMA_CHAR) {
            // Verify that the client-nonce in the message matches that in the client-first
            for (int i = 0; i < 30; ++i) {
                if ((int) this.clientFinal[i] != (int) clientFirst.cNonce[i]) {
                    throw new IllegalArgumentException("client-final message had invalid client nonce");
                }
            }

            // Verify that the server-nonce in the message matches that in the server-first
            for (int i = 0; i < 30; ++i) {
                if ((int) this.clientFinal[30 + i] != (int) serverFirst.sNonce[i]) {
                    throw new IllegalArgumentException("client-final message had invalid server nonce");
                }
            }
        } else {
            throw new IllegalArgumentException("client-final message had invalid delimiters");
        }

        this.clientProof = new byte[32];
        System.arraycopy(this.clientFinal, 61, this.clientProof, 0, 32);

        this.authMessage = assembleAuthMessage(clientFirst, serverFirst);

        this.clientSig = ScramUtils.hmac_sha_256(credentials.storedKey, this.authMessage);

        final byte[] clientKey = new byte[32];
        if (this.clientSig != null) {
            for (int i = 0; i < 32; ++i) {
                clientKey[i] = (byte) ((int) this.clientSig[i] ^ (int) this.clientProof[i]);
            }
        }

        final byte[] test = ScramUtils.sha_256(clientKey);
        if (!Arrays.equals(test, credentials.storedKey)) {
            throw new IllegalArgumentException("Authentication failed");
        }
    }

    /**
     * Generates an authentication message.
     *
     * @param clientFirst the client-first message
     * @param serverFirst the server-first message
     * @return the assembled message
     */
    private static byte[] assembleAuthMessage(final ClientFirstMessage clientFirst,
                                              final ServerFirstMessage serverFirst) {

        final byte[] theAuthMessage = new byte[clientFirst.clientFirst.length + serverFirst.serverFirst.length + 62];

        System.arraycopy(clientFirst.clientFirst, 0, theAuthMessage, 0, clientFirst.clientFirst.length);

        int pos = clientFirst.clientFirst.length;
        theAuthMessage[pos] = CoreConstants.COMMA_CHAR;
        ++pos;
        System.arraycopy(serverFirst.serverFirst, 0, theAuthMessage, pos, serverFirst.serverFirst.length);
        pos += serverFirst.serverFirst.length;
        theAuthMessage[pos] = CoreConstants.COMMA_CHAR;
        ++pos;
        System.arraycopy(clientFirst.cNonce, 0, theAuthMessage, pos, 30);
        pos += 30;
        System.arraycopy(serverFirst.sNonce, 0, theAuthMessage, pos, 30);
        pos += 30;
        if (pos != theAuthMessage.length) {
            Log.warning("Length mismatch!");
        }

        return theAuthMessage;
    }
}
