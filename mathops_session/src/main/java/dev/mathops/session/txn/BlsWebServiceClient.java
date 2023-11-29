package dev.mathops.session.txn;

import dev.mathops.core.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.session.SessionCache;
import dev.mathops.session.file.SessionFileLoader;
import dev.mathops.session.SessionManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * The client end of a server connection. This connection encapsulates a bidirectional Java object stream, over a normal
 * socket connection. By itself, it performs no message exchanges. It simply creates the socket connection, creates the
 * object streams, and provides status and error information.
 */
public class BlsWebServiceClient implements HostnameVerifier, IWebServiceClient {

    /** An empty character array. */
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /** The scheme to use to communicate with the server. */
    private final String scheme;

    /** The IP address of the server to connect to. */
    private final InetAddress serverIp;

    /** The TCP port of the server to connect to. */
    private final int port;

    /** The session ID to use to communicate with the server. */
    private final String sessionId;

    /** Storage for inbound data. */
    private final List<char[]> inputData;

    /** The URL to which to connect. */
    private URL url;

    /** An SSL socket factory that has the appropriate key store installed. */
    private SSLSocketFactory sslSocketFactory;

    /**
     * Constructs a new {@code BlsWebServiceClient}.
     *
     * @param theScheme    the scheme to use to communicate with the server
     * @param theServer    the hostname of the server to connect to
     * @param thePort      the TCP port of the server to connect to
     * @param theSessionId the session ID to use to communicate with the server
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    public BlsWebServiceClient(final String theScheme, final String theServer, final int thePort,
                               final String theSessionId) throws UnknownHostException {

        this.scheme = theScheme;
        this.serverIp = InetAddress.getByName(theServer);
        this.port = thePort;
        this.sessionId = theSessionId;

        this.inputData = new ArrayList<>(5);
        this.url = null;

        System.setProperty("com.sun.security.enableCRLDP", "false");
        System.setProperty("com.sun.net.ssl.checkRevocation", "false");
        System.setProperty("ocsp.enable", "false");
    }

    /**
     * Verifies the validity of a hostname.
     *
     * @param s   the hostname to verify
     * @param sslSession the SSL session
     * @return {@code true} if the hostname is valid; {@code false} if not
     */
    @Override
    public final boolean verify(final String s, final SSLSession sslSession) {

        return Contexts.PRECALC_HOST.equals(s) || Contexts.PRECALCDEV_HOST.equals(s)
                || Contexts.PLACEMENT_HOST.equals(s) || Contexts.PLACEMENTDEV_HOST.equals(s)
                || Contexts.COURSE_HOST.equals(s) || Contexts.COURSEDEV_HOST.equals(s)
                || Contexts.TESTING_HOST.equals(s) || Contexts.TESTINGDEV_HOST.equals(s)
                || Contexts.ONLINE_HOST.equals(s) || Contexts.DEV_HOST.equals(s)
                || "numan.math.colostate.edu".equals(s);
    }

    /**
     * Initialize the object.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    @Override
    public final boolean init() {

        // Load the trust store
        try (final InputStream input = SessionFileLoader.openInputStream(getClass(), "client.kdb", true)) {

            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(input, "pace.not.imp".toCharArray());

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            final TrustManager defaultTrustManager = tmf.getTrustManagers()[0];
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{defaultTrustManager}, null);

            this.sslSocketFactory = context.getSocketFactory();

            HttpsURLConnection.setDefaultHostnameVerifier(this);

            final String sch = this.scheme;
            try {
                if ("http".equals(sch)) {
                    if (this.port == 80) {
                        this.url = new URL(this.scheme, this.serverIp.getHostName(), "/txn/txn.html");
                    } else {
                        this.url = new URL(this.scheme, this.serverIp.getHostName(), this.port, "/txn/txn.html");
                    }
                } else if ("https".equals(sch) && (this.port == 443)) {
                    this.url = new URL(this.scheme, this.serverIp.getHostName(), "/txn/txn.html");
                } else {
                    this.url = new URL(this.scheme, this.serverIp.getHostName(), this.port, "/txn/txn.html");
                }
            } catch (final MalformedURLException ex) {
                Log.warning(ex);
                this.url = null;
            }
        } catch (final KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException
                       | KeyManagementException ex) {
            Log.warning(ex);
        }

        return this.url != null;
    }

    /**
     * Tests whether the connection is in a state where data may be sent to the server. This does not necessarily imply
     * an open socket or other persistent connection.
     *
     * @return {@code true} if the connection is ready to be used; {@code false} if not
     */
    @Override
    public final boolean isOpen() {

        return this.url != null;
    }

    /**
     * Closes the object.
     */
    @Override
    public final void close() {

        this.url = null;
    }

    /**
     * Writes a block of data to the server.
     *
     * @param obj the data block to write
     * @return {@code true} if successful; {@code false} otherwise
     */
    @Override
    public final boolean writeObject(final String obj) {

        final byte[] bytes = obj.getBytes(StandardCharsets.UTF_8);
        boolean result = false;

        if (this.url != null) {

            try {
                final URLConnection conn;

                if ("https".equals(this.scheme)) {
                    conn = this.url.openConnection();
                    ((HttpsURLConnection) conn).setSSLSocketFactory(this.sslSocketFactory);
                } else {
                    conn = this.url.openConnection();
                }

                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).setRequestMethod("POST");
                    if (this.sessionId != null) {
                        conn.setRequestProperty("Cookie", SessionManager.SESSION_ID_COOKIE + "=" + this.sessionId);
                    }
                }

                conn.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                conn.setRequestProperty("Content-Type", "text/xml");
                conn.connect();

                try (final OutputStream out = conn.getOutputStream()) {
                    out.write(bytes);
                }

                final int len = conn.getContentLength();

                if (len > 0) {
                    final byte[] inBytes = new byte[len];
                    int total = 0;

                    try (final InputStream in = conn.getInputStream()) {
                        while (total < len) {
                            final int numRead = in.read(inBytes, total, len - total);

                            if (numRead > 0) {
                                total += numRead;
                            } else if (numRead < 0) {
                                throw new IOException("Failed to read from network");
                            }
                        }
                    }

                    this.inputData.add(new String(inBytes, StandardCharsets.UTF_8).toCharArray());
                } else {
                    this.inputData.add(EMPTY_CHAR_ARRAY);
                }

                result = true;
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

        return result;
    }

    /**
     * Read a block of data from the server.
     *
     * @param objName a friendly name for the object that is to be read, to allow logging of errors to better direct the
     *                developer to the problem
     * @return the object read, or {@code null} if an error occurred
     */
    @Override
    public final char[] readObject(final String objName) {

        char[] data = null;

        if (!this.inputData.isEmpty()) {
            data = this.inputData.remove(0);
        }

        return data;
    }

    /**
     * Main method to test the client code.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        try {
            final IWebServiceClient client = new BlsWebServiceClient("https", Contexts.TESTING_HOST, 443,
                    SessionCache.TEST_SESSION_ID);

            if (client.init()) {

                if (client.writeObject("<echo-request>foo</echo-request>")) {
                    final char[] data = client.readObject("test");

                    if (data == null) {
                        Log.warning("Client read failure");
                    } else {
                        Log.info("Received: ", new String(data));
                    }
                } else {
                    Log.warning("Client write failure");
                }

                client.close();
            } else {
                Log.warning("Client init failure");
            }
        } catch (final UnknownHostException ex) {
            Log.warning("Client test failure");
        }
    }
}
