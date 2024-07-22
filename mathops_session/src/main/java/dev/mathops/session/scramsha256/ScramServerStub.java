package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.HexEncoder;
import dev.mathops.db.old.Cache;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A server stub that a web service can use to manage SCRAM-SHA-256 authentication.
 */
public final class ScramServerStub {

    /** Timeout on client-first requests. */
    private static final long REQUEST_TIMEOUT = (long) (1000 * 60);

    /** Timeout on token (milliseconds). */
    private static final long TOKEN_TIMEOUT = (long) (1000 * 60 * 5); // 5 minutes

    /** The user credentials manager. */
    private final UserCredentialsManager credManager;

    /** A source of random numbers. */
    private final Random random;

    /** The list of pending requests. */
    private final List<Request> requests;

    /** Map from token to timeout for that token. */
    private final Map<String, Long> tokenTimeouts;

    /** Map from token to user credentials. */
    private final Map<String, UserCredentials> tokenCredentials;

    /**
     * Constructs a new {@code ScramServerStub}.
     *
     * @param cache the data cache
     */
    public ScramServerStub(final Cache cache) {

        this.credManager = UserCredentialsManager.getInstance(cache);

        Random rnd;
        try {
            rnd = SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
            final long seed = System.currentTimeMillis();
            rnd = new Random(seed);
        }

        this.random = rnd;

        this.requests = new ArrayList<>(10);
        this.tokenTimeouts = new HashMap<>(10);
        this.tokenCredentials = new HashMap<>(10);
    }

    /**
     * Called when the website receives a "client-first" message.
     *
     * @param hex the hex request
     * @return the reply to send to the client (either the hex of a "server-first" message, or "!" followed by an error
     *         message
     */
    public String handleClientFirst(final String hex) {

        String result;

        final long now = System.currentTimeMillis();
        this.requests.removeIf(request -> request.timeout < now);

        if (this.requests.size() > 100) {
            Log.warning("Too many pending requests");
            result = "!Too many pending requests";
        } else {
            try {
                final ClientFirstMessage clientFirst = new ClientFirstMessage(hex);
                final UserCredentials cred =
                        this.credManager.getCredentials(new String(clientFirst.normalizedUsername,
                                StandardCharsets.UTF_8));

                if (cred == null) {
                    Log.warning("Invalid username");
                    result = "!Invalid username";
                } else {
                    // Request is valid - store for the next step in the process
                    final ServerFirstMessage serverFirst = new ServerFirstMessage(clientFirst, cred, this.random);
                    result = serverFirst.hex;

                    final Request req = new Request(cred, clientFirst, serverFirst, now + REQUEST_TIMEOUT);
                    this.requests.add(req);
                }

            } catch (final IllegalArgumentException ex) {
                Log.warning("Unable to parse client-first message: ", ex.getMessage());
                result = "!Invalid client-first request";
            }
        }

        return result;
    }

    /**
     * Called when the website receives a "client-final" message.
     *
     * @param hex the hex request
     * @return the reply to send to the client (either the hex of a "server-first" message, or "!" followed by an error
     *         message
     */
    public String handleClientFinal(final String hex) {

        String result;

        try {
            final byte[] decoded = HexEncoder.decode(hex);
            if (decoded.length == 93) {

                final byte[] cNonce = new byte[30];
                final byte[] sNonce = new byte[30];
                System.arraycopy(decoded, 0, cNonce, 0, 30);
                System.arraycopy(decoded, 30, sNonce, 0, 30);

                Request req = null;
                for (final Request test : this.requests) {
                    if (Arrays.equals(cNonce, test.clientFirst.cNonce)
                            && Arrays.equals(sNonce, test.serverFirst.sNonce)) {
                        req = test;
                    }
                }

                if (req == null) {
                    Log.warning("client-final without matching client-first");
                    result = "!client-final without matching client-first";
                } else {
                    final ClientFinalMessage clientFinal = new ClientFinalMessage(hex, req.clientFirst,
                            req.serverFirst, req.credentials);

                    final String token = CoreConstants.newId(30);
                    final ServerFinalMessage serverFinal = new ServerFinalMessage(clientFinal, req.credentials, token);
                    result = serverFinal.hex;

                    final long now = System.currentTimeMillis();
                    final Long timeout = Long.valueOf(now + TOKEN_TIMEOUT);
                    this.tokenTimeouts.put(token, timeout);
                    this.tokenCredentials.put(token, req.credentials);

                    Log.info("SCRAM-SHA-256 authentication of user ", new String(req.credentials.normalizedUsername,
                            StandardCharsets.UTF_8));
                }
            } else {
                Log.warning("Invalid client-final message");
                result = "!Invalid client-final message";
            }
        } catch (final IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            Log.warning("Invalid client-final message: ", msg);
            result = "!Invalid client-final message";
        }

        return result;
    }

    /**
     * Validates a token.
     *
     * @param token the token to test
     * @return the user credentials associated with that token; null if token is not valid
     */
    public UserCredentials validateToken(final String token) {

        UserCredentials result = null;

        final Long timeout = this.tokenTimeouts.get(token);
        if (timeout != null) {
            final long now = System.currentTimeMillis();

            if (timeout.longValue() < now) {
                this.tokenCredentials.remove(token);
                this.tokenTimeouts.remove(token);
            } else {
                result = this.tokenCredentials.get(token);

                if (result == null) {
                    Log.warning("Timeout present but credentials not");
                    this.tokenTimeouts.remove(token);
                } else {
                    this.tokenTimeouts.put(token, Long.valueOf(now + TOKEN_TIMEOUT));
                }
            }
        }

        return result;
    }

    /**
     * A request.
     */
    static class Request {

        /** The user credentials. */
        final UserCredentials credentials;

        /** The client-first message. */
        final ClientFirstMessage clientFirst;

        /** The server-first message. */
        final ServerFirstMessage serverFirst;

        /** The time when this request times out. */
        final long timeout;

        /**
         * Constructs a new {@code Request}.
         *
         * @param theCredentials the user credentials
         * @param theClientFirst the client-first message
         * @param theServerFirst the server-first message
         * @param theTimeout     the timeout
         */
        Request(final UserCredentials theCredentials, final ClientFirstMessage theClientFirst,
                final ServerFirstMessage theServerFirst, final long theTimeout) {

            this.credentials = theCredentials;
            this.clientFirst = theClientFirst;
            this.serverFirst = theServerFirst;
            this.timeout = theTimeout;
        }
    }
}
