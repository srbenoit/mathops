package dev.mathops.session.scramsha256;

import dev.mathops.core.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * A client stub that an application (such as a Swing application) can use to communicate with a server stub within a
 * web service.
 *
 * <p>
 * The stub is a thread that polls the server on a regular schedule, and which can notify a listener when state is
 * updated.
 */
public final class ScramClientStub {

    /** Random number generator. */
    private final Random rnd;

    /**
     * The URL of the server site, with trailing '/', to which a page subpath can be appended, like
     * "https://example.com/".
     */
    public final String siteUrl;

    /** The username. */
    private String username;

    /** The password. */
    private String password;

    /** The negotiated token. */
    private String token;

    /**
     * Constructs a new {@code ClientStub}.
     *
     * @param theServerSiteUrl the URL of the server site, with trailing '/', to which a page subpath can be appended,
     *                         like "https://example.com/".
     * @throws IllegalArgumentException of the provided URL is null, does not begin with "https://" or does not end with
     *                                  a '/'
     */
    public ScramClientStub(final String theServerSiteUrl) throws IllegalArgumentException {

        if (theServerSiteUrl == null || !theServerSiteUrl.startsWith("https://")
                || !theServerSiteUrl.endsWith("/")) {
            throw new IllegalArgumentException("Invalid monitor site URL.");
        }

        this.siteUrl = theServerSiteUrl;

        Random theRnd;

        try {
            theRnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException ex1) {
            try {
                Log.warning(ex1);
                theRnd = SecureRandom.getInstanceStrong();
            } catch (final NoSuchAlgorithmException ex2) {
                Log.warning(ex2);
                theRnd = new Random(System.currentTimeMillis());
            }
        }

        this.rnd = theRnd;
        this.token = null;
    }

    /**
     * Performs the handshake with the server, exchanging SCRAM-SHA-256 messages to establish a security token that can
     * be used for subsequent polling.
     *
     * @param theUsername the authentication username
     * @param thePassword the authentication password
     * @return {@code null} if handshake succeeded; an error message if not
     */
    public String handshake(final String theUsername, final String thePassword) {

        String result = null;

        if (theUsername == null || theUsername.isEmpty()) {
            result = "Invalid username";
        } else if (thePassword == null || thePassword.isEmpty()) {
            result = "Invalid password";
        } else {
            final ClientFirstMessage clientFirst = new ClientFirstMessage(theUsername, this.rnd);

            final String serverFirstHex = getServerResponse("client-first.ws",
                    "first=" + clientFirst.hex);

            if (serverFirstHex == null) {
                result = "Server not responding.";
            } else {
                try {
                    final ServerFirstMessage serverFirst =
                            new ServerFirstMessage(serverFirstHex, clientFirst);

                    final ClientFinalMessage clientFinal =
                            new ClientFinalMessage(thePassword, clientFirst, serverFirst);

                    final String serverFinalHex = getServerResponse(//
                            "client-final.ws",
                            "final=" + clientFinal.hex);
                    if (serverFinalHex == null) {
                        result = "Server not responding.";
                    } else {
                        try {
                            final ServerFinalMessage serverFinal =
                                    new ServerFinalMessage(serverFinalHex);

                            this.token = serverFinal.token;
                            this.username = theUsername;
                            this.password = thePassword;
                        } catch (final IllegalArgumentException ex) {
                            result = "Invalid login.";
                        }
                    }
                } catch (final IllegalArgumentException ex) {
                    result = ex.getMessage();
                    Log.warning(result);
                }
            }
        }

        if (result != null) {
            Log.warning(result);
        }

        return result;
    }

    /**
     * Gets the token.
     *
     * @return the token; null if authentication failed.
     */
    public String getToken() {

        return this.token;
    }

    /**
     * Attempts to send a query to the server and to read its response as a String.
     *
     * @param page  the page subpath, like 'client-first.html'
     * @param query the query string, like 'a=b&c=d'
     * @return the response; null if an error occurred
     */
    private String getServerResponse(final String page, final String query) {

        String result = null;

        try {
            final URL url;
            if (query == null) {
                url = new URL(this.siteUrl + page);
            } else {
                url = new URL(this.siteUrl + page + "?" + query);
            }

            final URLConnection conn = url.openConnection();
            final Object content = conn.getContent();
            if (content == null) {
                Log.warning("Server response from '", page,
                        "' was null");
            } else if (content instanceof InputStream) {

                try (final InputStream in = (InputStream) content) {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                    final byte[] buffer = new byte[1024];
                    int count = in.read(buffer);
                    while (count > 0) {
                        baos.write(buffer, 0, count);
                        count = in.read(buffer);
                    }

                    result = baos.toString();

                    if ("!Unable to validate token.".equals(result)) {
                        // Make one attempt to re-validate
                        if (handshake(this.username, this.password) == null) {

                            final URLConnection conn2 = url.openConnection();
                            final Object content2 = conn2.getContent();
                            if (content2 == null) {
                                Log.warning("Server response from '", page,
                                        "' was null");
                            } else if (content2 instanceof InputStream) {

                                try (final InputStream in2 = (InputStream) content2) {
                                    final ByteArrayOutputStream baos2 =
                                            new ByteArrayOutputStream(1024);
                                    count = in2.read(buffer);
                                    while (count > 0) {
                                        baos2.write(buffer, 0, count);
                                        count = in2.read(buffer);
                                    }

                                    result = baos2.toString();
                                }
                            } else {
                                Log.warning("Server response from '", page,
                                        "' was ", content.getClass().getName());
                            }

                        }
                    }
                }
            } else {
                Log.warning("Server response from '", page, "' was ",
                        content.getClass().getName());
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        return result;
    }
}
