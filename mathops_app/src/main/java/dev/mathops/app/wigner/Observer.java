package dev.mathops.app.wigner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * The observer, who simply gets notified of a positive result.
 */
public final class Observer implements HttpHandler {

    /**
     * Constructs a new {@code Observer}.
     */
    private Observer() {

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

            if ("notify".equals(subpath)) {
                if (query == null) {
                    response = "Invalid 'notify' without query";
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

                                        Log.info("Notification that data[" + index + "] = " + value);
                                        response = "Thank you.";
                                    } catch (final NumberFormatException ex) {
                                        response = "Invalid 'notify' value: " + query;
                                    }
                                } else {
                                    response = "Invalid 'notify' query: " + query;
                                }
                            } catch (final NumberFormatException ex) {
                                response = "Invalid 'notify' index: " + query;
                            }
                        } else {
                            response = "Invalid 'notify' query: " + query;
                        }
                    } else {
                        response = "Invalid 'notify', expecting 2 parameters, found " + parts.length;
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
     * Tests the class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final HttpHandler observer = new Observer();

        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);

            server.createContext("/", observer);
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }
}
