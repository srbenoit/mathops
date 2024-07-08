package dev.mathops.app.wigner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The domain boundary.
 */
public final class Boundary implements HttpHandler {

    /**
     * Constructs a new {@code Boundary}.
     */
    private Boundary() {

        // No action
    }

    /**
     * Handles an HTTP request.
     *
     * @param exchange the exchange containing the request from the client and used to send the response
     * @throws IOException if there is an I/O error
     */
    @Override
    public void handle(final HttpExchange exchange) throws IOException {

        final URI uri = exchange.getRequestURI();
        final String path = uri.getPath();

        if (path.startsWith("/")) {
            final String subpath = path.substring(1);
            final String query = uri.getQuery();

            String response;

            if ("check".equals(subpath)) {
                if (query == null) {
                    response = "Invalid 'check' without query";
                } else {
                    final String[] parts = query.split("&");
                    if (parts.length == 2) {
                        if (parts[0].startsWith("i=")) {
                            final String substring0 = parts[0].substring(2);
                            try {
                                final int index = Integer.parseInt(substring0);
                                if (parts[1].startsWith("v=")) {
                                    final String substring1 = parts[1].substring(2);
                                    try {
                                        final int value = Integer.parseInt(substring1);
                                        final boolean match = pollRandomSource(index, value);
                                        response = match ? "Y" : "N";
                                    } catch (final NumberFormatException ex) {
                                        response = "Invalid 'check' value: " + query;
                                    }
                                } else {
                                    response = "Invalid 'check' query: " + query;
                                }
                            } catch (final NumberFormatException ex) {
                                response = "Invalid 'check' index: " + query;
                            }
                        } else {
                            response = "Invalid 'check' query: " + query;
                        }
                    } else {
                        response = "Invalid 'check', expecting 2 parameters, found " + parts.length;
                    }
                }
            } else {
                response = "Unrecognized request: " + subpath;
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * Polls the random source, testing whether a particular digit has a particular value.
     *
     * @param index the index of the value to test
     * @param value the value for which to test
     * @return true if the random source reported that the entry with the specified index has the specified value
     */
    public boolean pollRandomSource(final int index, final int value) {

        boolean result = false;

        try {
            final URI uri = new URI("http://localhost:8000/test?i=" + index + "&v=" + value);
            final URL url = uri.toURL();

            final URLConnection conn = url.openConnection();
            try (final InputStream in = conn.getInputStream();
                 final InputStreamReader reader = new InputStreamReader(in)) {

                final char[] buffer = new char[40];
                final StringBuilder builder = new StringBuilder(40);

                int count = reader.read(buffer);
                while (count > 0) {
                    builder.append(new String(buffer, 0, count));
                    count = reader.read(buffer);
                }

                final String fetched = builder.toString().trim();

                if ("1".equals(fetched)) {
                    result = true;
                }
            }
        } catch (final MalformedURLException ex) {
            Log.warning("Invalid URL.", ex);
        } catch (final URISyntaxException ex) {
            Log.warning("Invalid URI.", ex);
        } catch (final IOException ex) {
            Log.warning("Failed to fetch random number data.", ex);
        }

        return result;
    }

    /**
     * Tests the class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final HttpHandler boundary = new Boundary();

        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);

            server.createContext("/", boundary);
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }
}
