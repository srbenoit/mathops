package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.parser.HexEncoder;

import java.nio.charset.StandardCharsets;

/**
 * The data that makes up the "server-final" message.
 *
 * <pre>
 * SERVER_SIG[32] + "," + TOKEN[30]
 * </pre>
 */
/* default */ class ServerFinalMessage {

    /** The computed server signature. */
    private final byte[] serverSig;

    /** The decoded message. */
    private final byte[] serverFinal;

    /** The security token. */
    /* default */ final String token;

    /** The hex representation. */
    /* default */ final String hex;

    /**
     * Constructs a {@code ServerFinalMessage}.
     *
     * @param clientFinal the {@code ClientFinalMessage} message (used to validate that the server-first message matches
     *                    this request)
     * @param credentials the user credentials
     * @param theToken    the security token (30 URL-safe characters)
     * @throws IllegalArgumentException if there is an error in the message
     */
    /* default */ ServerFinalMessage(final ClientFinalMessage clientFinal,
                                     final UserCredentials credentials, final String theToken) throws IllegalArgumentException {

        if (clientFinal == null) {
            throw new IllegalArgumentException("ClientFinal may not be null");
        }
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        if (theToken == null || theToken.length() != 30) {
            throw new IllegalArgumentException("Invalid token");
        }

        this.serverSig = ScramUtils.hmac_sha_256(credentials.serverKey, clientFinal.authMessage);
        this.token = theToken;

        this.serverFinal = new byte[63];
        System.arraycopy(this.serverSig, 0, this.serverFinal, 0, 32);
        this.serverFinal[32] = CoreConstants.COMMA_CHAR;
        System.arraycopy(this.token.getBytes(StandardCharsets.UTF_8), 0, this.serverFinal, 33, 30);

        this.hex = HexEncoder.encodeLowercase(this.serverFinal);
    }

    /**
     * Constructs a {@code ServerFinalMessage} from a hex representation returned by the server in response to the
     * 'client-first' message, and verifies that its format is correct and that its client nonce matches that from the
     * 'client-first' message, to ensure it should be associated with that request.
     *
     * @param theHex the hex to parse
     * @throws IllegalArgumentException if there is an error in the message
     */
    /* default */ ServerFinalMessage(final String theHex) throws IllegalArgumentException {

        if (theHex.charAt(0) == '!') {
            throw new IllegalArgumentException(theHex.substring(1));
        }

        this.hex = theHex;
        this.serverFinal = HexEncoder.decode(this.hex);

        if (this.serverFinal.length != 63) {
            throw new IllegalArgumentException("server-final message had invalid length: " +
                    this.serverFinal.length);
        }

        if ((int) this.serverFinal[32] != CoreConstants.COMMA_CHAR) {
            throw new IllegalArgumentException("server-final message had invalid delimiters");
        }

        this.serverSig = new byte[32];
        System.arraycopy(this.serverFinal, 0, this.serverSig, 0, 32);

        this.token = new String(this.serverFinal, 33, 30, StandardCharsets.UTF_8);
    }
}
