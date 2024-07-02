package dev.mathops.app.canvas;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;
import dev.mathops.app.canvas.data.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A base class for classes that access the Canvas API.
 */
public final class CanvasApi {

    /** A request method. */
    public static final String GET = "GET";

    /** A request method. */
    public static final String POST = "POST";

    /** A commonly used character. */
    private static final char SLASH_CHAR = '/';

    /** The hostname of the Canvas installation. */
    private final String canvasHost;

    /** The access token. */
    private final String accessToken;

    /**
     * Constructs a new {@code CanvasApi}.
     *
     * @param theCanvasHost  the hostname of the Canvas installation
     * @param theAccessToken the access token
     */
    public CanvasApi(final String theCanvasHost, final String theAccessToken) {

        final int hostLen = theCanvasHost.length();

        if (!theCanvasHost.isEmpty() && (int) theCanvasHost.charAt(hostLen - 1) == (int) SLASH_CHAR) {
            this.canvasHost = theCanvasHost.substring(hostLen - 1);
        } else {
            this.canvasHost = theCanvasHost;
        }

        this.accessToken = theAccessToken;
    }

    /**
     * Gets the Canvas host to which we are attached.
     *
     * @return the canvas host
     */
    String getCanvasHost() {

        return this.canvasHost;
    }

    /**
     * Attempts to fetch the current user's information from Canvas.
     *
     * @return the user info if successful; {@code null} if not
     */
    public UserInfo fetchUser() {

        UserInfo info = null;

        final ApiResult result = apiCall("users/self", GET, null);

        if (result.response != null) {
            info = new UserInfo(result.response);
        } else {
            Log.warning("ERROR: " + result.error);
        }

        return info;
    }

    /**
     * Performs a Canvas API call and attempts to parse the response as a JSON object.
     *
     * @param path       the target path (the portion of the URL after "/api/v1/")
     * @param method     the method (GET or POST)
     * @param parameters parameters to add to the request; null if none
     * @return the result
     */
    public ApiResult apiCall(final String path, final String method,
                             final Map<String, List<String>> parameters) {

        final String targetUri = this.canvasHost + "/api/v1/" + path;

        ApiResult result;

        try {
            final URI uri = new URI(targetUri);
            final URL url = uri.toURL();

            try {
                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                if (parameters != null) {
                    final HtmlBuilder query = new HtmlBuilder(100);

                    boolean and = false;
                    for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                        for (final String value : entry.getValue()) {
                            if (and) {
                                query.add('&');
                            }
                            query.add(entry.getKey());
                            query.add('=');
                            query.add(URLEncoder.encode(value, StandardCharsets.UTF_8));
                            and = true;
                        }
                    }

                    conn.setDoOutput(true);
                    conn.connect();

                    try (final OutputStream out = conn.getOutputStream()) {
                        out.write(query.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                final int status = conn.getResponseCode();
                if (status == 200 || status == 201) {
                    final StringBuilder content = new StringBuilder(500);
                    try (final BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine = in.readLine();
                        while (inputLine != null) {
                            content.append(inputLine);
                            inputLine = in.readLine();
                        }
                    }
                    conn.disconnect();

                    final String responseText = content.toString();

                    if (responseText.isEmpty()) {
                        result = new ApiResult("Server returned no data");
                    } else {
                        try {
                            final Object obj = JSONParser.parseJSON(responseText);
                            if (obj instanceof final JSONObject json) {
                                result = new ApiResult(json);
                            } else if (obj instanceof final Object[] array) {
                                final ArrayList<JSONObject> list = new ArrayList<>(array.length);
                                for (final Object o : array) {
                                    if (o instanceof final JSONObject jsonObj) {
                                        list.add(jsonObj);
                                    }
                                }
                                result = new ApiResult(list);
                            } else {
                                result = new ApiResult("Unable to interpret response from server (" + obj + ")");
                            }
                        } catch (final ParsingException ex) {
                            result = new ApiResult("Unable to parse response from server");
                            Log.warning(ex);
                        }
                    }
                } else {
                    result = new ApiResult("Server returned status " + status);
                }
            } catch (final IOException ex) {
                result = new ApiResult("Unable to connect to Canvas server");
                Log.warning(ex);
            }
        } catch (final MalformedURLException | URISyntaxException ex) {
            result = new ApiResult("Invalid login request URL");
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Performs an API call that returns a "paginated" list and attempts to parse the response as a JSON object.
     *
     * @param path   the target path (the portion of the URL after "/api/v1/")
     * @param method the method (GET or POST)
     * @return the result
     */
    public ApiResult paginatedApiCall(final String path, final String method) {

        final String targetUri = "/api/v1/" + path;

        ApiResult result = null;

        String nextLink = this.canvasHost + targetUri;
        final List<JSONObject> records = new ArrayList<>(10);

        while (nextLink != null) {
            try {
                final URI uri = new URI(nextLink);
                final URL url = uri.toURL();

                try {
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod(method);
                    conn.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                    final int status = conn.getResponseCode();
                    if (status == 200 || status == 201) {
                        final StringBuilder content = new StringBuilder(500);
                        try (final BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            String inputLine = in.readLine();
                            while (inputLine != null) {
                                content.append(inputLine);
                                inputLine = in.readLine();
                            }
                        }

                        nextLink = null;
                        final String linkHeader = conn.getHeaderField("Link");
                        if (linkHeader != null) {
                            final String[] links = linkHeader.split(",");
                            for (final String link : links) {
                                if (link.endsWith(">; rel=\"next\"")) {
                                    nextLink = link.substring(1, link.length() - 13);
                                }
                            }
                        }

                        conn.disconnect();

                        final String responseText = content.toString();

                        try {
                            final Object obj = JSONParser.parseJSON(responseText);
                            if (obj instanceof final Object[] array) {
                                for (final Object o : array) {
                                    if (o instanceof JSONObject) {
                                        records.add((JSONObject) o);
                                    } else {
                                        result = new ApiResult("Unable to interpret item in response from server");
                                        break;
                                    }
                                }
                            } else {
                                result = new ApiResult("Unable to interpret response from server");
                                break;
                            }
                        } catch (final ParsingException ex) {
                            result = new ApiResult("Unable to parse response from server");
                            Log.warning(ex);
                            break;
                        }
                    } else {
                        result = new ApiResult("Server returned status " + status + " for " + targetUri);
                        break;
                    }
                } catch (final IOException ex) {
                    result = new ApiResult("Unable to connect to Canvas server");
                    Log.warning(ex);
                    break;
                }
            } catch (final MalformedURLException | URISyntaxException ex) {
                result = new ApiResult("Invalid login request URL");
                Log.warning(ex);
                break;
            }
        }

        if (result == null) {
            result = new ApiResult(records);
        }

        return result;
    }
}
