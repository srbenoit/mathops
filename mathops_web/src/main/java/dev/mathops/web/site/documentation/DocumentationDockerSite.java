/*
 * Copyright (C) 2022 Steve Benoit
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the  License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If  not, see
 * <https://www.gnu.org/licenses/>.
 */

package dev.mathops.web.site.documentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * The site.
 */
public class DocumentationDockerSite implements HttpHandler {

    /**
     * Constructs a new {@code DocumentationDockerSite}.
     */
    public DocumentationDockerSite() {

        super();
    }

    /**
     * Handles a request.
     *
     * @param exchange the exchange containing the request from the client and used to send the response
     * @throws IOException if there is an error reading or writing
     */
    public final void handle(final HttpExchange exchange) throws IOException {

        final String response = "Documentation Site version 1.00";
        final long len = (long) response.length();

        exchange.sendResponseHeaders(200, len);
        final OutputStream os = exchange.getResponseBody();

        final byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        os.write(responseBytes);
        os.close();
    }

    /**
     * Main method to start the server.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/", new DocumentationDockerSite());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (final IOException e) {
            Log.warning(e);
        }
    }
}

