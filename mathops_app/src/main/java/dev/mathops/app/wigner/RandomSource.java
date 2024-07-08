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
import java.nio.charset.StandardCharsets;

/**
 * The random value source.  This uses the "RANDOM.ORG" randomness source, which uses atmospheric noise.
 *
 * <p>
 * This class gathers and stores a sequence of 10 integers, each from 0 to 9, and allows each to be queried, or allows
 * queries like "is digit 4 a 7?"
 */
public final class RandomSource implements HttpHandler {

    /** The number of random values to retrieve per randomization. */
    private static final int ARRAY_LEN = 10;

    /** The minimum value of each number. */
    private static final int MIN_VALUE = 0;

    /** The maximum value of each number. */
    private static final int MAX_VALUE = 9;

    /** The digits. */
    private final int[] data;

    /**
     * Constructs a new {@code RandomSource}.
     */
    private RandomSource() {

        this.data = new int[ARRAY_LEN];
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

            switch (subpath) {
                case "randomize" -> {
                    final boolean result = ramdomize();
                    response = result ? "1" : "0";
                }
                case "get" -> {
                    if (query == null) {
                        response = "Invalid 'get' without query";
                    } else if (query.startsWith("i=")) {
                        final String substring = query.substring(2);
                        try {
                            final int index = Integer.parseInt(substring);
                            if (index < 0 || index >= ARRAY_LEN) {
                                response = "'get' index out of range: " + query;
                            } else {
                                final int value = this.data[index];
                                response = Integer.toString(value);
                            }
                        } catch (final NumberFormatException ex) {
                            response = "Invalid 'get' index: " + query;
                        }
                    } else {
                        response = "Invalid 'get' query: " + query;
                    }
                }
                case "test" -> {
                    if (query == null) {
                        response = "Invalid 'test' without query";
                    } else {
                        final String[] parts = query.split("&");
                        if (parts.length == 2) {
                            if (parts[0].startsWith("i=")) {
                                final String substring0 = parts[0].substring(2);
                                try {
                                    final int index = Integer.parseInt(substring0);
                                    if (index < 0 || index >= ARRAY_LEN) {
                                        response = "'test' index out of range: " + query;
                                    } else if (parts[1].startsWith("v=")) {
                                        final String substring1 = parts[1].substring(2);
                                        try {
                                            final int value = Integer.parseInt(substring1);
                                            if (value < MIN_VALUE || value > MAX_VALUE) {
                                                response = "'test' value out of range: " + query;
                                            } else {
                                                final int actual = this.data[index];
                                                final boolean equal = actual == value;
                                                response = equal ? "1" : "0";
                                            }
                                        } catch (final NumberFormatException ex) {
                                            response = "Invalid 'test' value: " + query;
                                        }
                                    } else {
                                        response = "Invalid 'test' query: " + query;
                                    }
                                } catch (final NumberFormatException ex) {
                                    response = "Invalid 'test' index: " + query;
                                }
                            } else {
                                response = "Invalid 'test' query: " + query;
                            }
                        } else {
                            response = "Invalid 'test', expecting 2 parameters, found " + parts.length;
                        }
                    }
                }
                default -> response = "Unrecognized request: " + subpath;
            }

            final int responseLen = response.length();
            exchange.sendResponseHeaders(200, (long) responseLen);
            final byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

            try (final OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Performs randomization, storing a new set of random values.
     *
     * @return true if successful
     */
    public boolean ramdomize() {

        boolean ok = false;

        try {
            final URI uri = new URI(
                    "https://www.random.org/integers/?num=10&min=0&max=9&col=1&base=10&format=plain&rnd=new");
            final URL url = uri.toURL();

            final URLConnection conn = url.openConnection();
            try (final InputStream in = conn.getInputStream();
                 final InputStreamReader reader = new InputStreamReader(in)) {

                final char[] buffer = new char[400];
                final StringBuilder builder = new StringBuilder(400);

                int count = reader.read(buffer);
                while (count > 0) {
                    builder.append(new String(buffer, 0, count));
                    count = reader.read(buffer);
                }

                final String fetched = builder.toString().trim();
                final String[] lines = fetched.split("\\r?\\n|\\r");

                if (lines.length == 10) {
                    for (int i = 0; i < 10; ++i) {
                        this.data[i] = Integer.parseInt(lines[i]);
                    }
                    Log.info("Data has been fetched.");
                } else {
                    Log.warning("Received " + lines.length + " lines rather than 10.");
                    Log.fine("[", fetched, "]");
                }
                ok = true;
            }
        } catch (final MalformedURLException ex) {
            Log.warning("Invalid URL.", ex);
        } catch (final URISyntaxException ex) {
            Log.warning("Invalid URI.", ex);
        } catch (final IOException ex) {
            Log.warning("Failed to fetch random number data.", ex);
        }

        return ok;
    }

    /**
     * Tests the class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final HttpHandler source = new RandomSource();

        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/", source);
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }
}
