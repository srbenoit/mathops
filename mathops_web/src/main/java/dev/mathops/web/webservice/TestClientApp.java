package dev.mathops.web.webservice;

import dev.mathops.core.log.Log;
import dev.mathops.session.scramsha256.ScramClientStub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A test client application that authenticates with the web service.
 */
final class TestClientApp {

    /** The client stub. */
    private final ScramClientStub stub;

    /**
     * Constructs a new {@code TestClientApp}.
     */
    private TestClientApp() {

        this.stub = new ScramClientStub("https://testingdev.math.colostate.edu/websvc/");
    }

    /**
     * Executes the tests.
     */
    private void go() {

        final String handshakeError = this.stub.handshake("sbenoit", "thinflation");

        if (handshakeError == null) {
            final String tok = this.stub.getToken();

            Log.info("Authentication token is: ", tok);

            try {
                final URL url = new URL(this.stub.siteUrl + "do-something.ws?token=" + tok);

                final URLConnection conn = url.openConnection();
                final Object content = conn.getContent();
                if (content == null) {
                    Log.warning("Server response from 'do-something.ws' was null");
                } else if (content instanceof InputStream) {

                    try (final InputStream in = (InputStream) content) {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                        final byte[] buffer = new byte[1024];
                        int count = in.read(buffer);
                        while (count > 0) {
                            baos.write(buffer, 0, count);
                            count = in.read(buffer);
                        }

                        Log.info(baos.toString());
                    }
                } else {
                    Log.warning("Server response from 'do-something.ws' was ", content.getClass().getName());
                }
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        } else {
            Log.info("Handshake error: ", handshakeError);
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param arguments command-line arguments
     */
    public static void main(final String... arguments) {

        new TestClientApp().go();
    }
}
