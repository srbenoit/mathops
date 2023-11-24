package dev.mathops.web.site.lti;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.Base64;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Methods for processing OAuth 1.0 (rfc5849) exchanges.
 */
enum OAuth1 {
    ;

    /** Hex characters, used in percent encoding. */
    private static final String HEX = "0123456789ABCDEF";

    // /** Consumer key. */
    // private static final String CONSUMER_KEY = "waiojtaolsjfa";

    /** Shared secret. */
    private static final String SHARED_SECRET = "BPISAuef0-32845u2";

    // URL: https://coursedev.math.colostate.edu/lti/cartridge_basiclti_link.xml
    // Icon: https://coursedev.math.colostate.edu/www/images/favicon.ico

    /**
     * Extracts parameters and verifies a request.
     *
     * @param req    the request
     * @param params a map to which to add parsed parameters
     * @return the result
     */
    static EOAuthRequestVerifyResult verifyRequest(final HttpServletRequest req,
                                                   final Map<String, List<String>> params) {

        // Before querying any parameters, read the request body, then get the query string and
        // Authorization header, from which we will extract parameters.
        final String requestBody = readRequestBody(req);

        // Populate a map from parameter name to list of values
        collectParameters(req, requestBody, params);

        final EOAuthRequestVerifyResult result;

        String host = req.getHeader("Host");
        if (host == null) {
            result = EOAuthRequestVerifyResult.MISSING_HOST;
        } else {
            host = host.toLowerCase(Locale.ROOT);
            // Log.info("[OAuth] Host: " + host);

            String path = req.getRequestURI();
            final int q = path.indexOf('?');
            if (q >= 0) {
                path = path.substring(q);
            }
            // Log.info("[OAuth] Path: " + path);

            final List<String> signature = params.get("oauth_signature");
            if (signature == null || signature.size() != 1) {
                result = EOAuthRequestVerifyResult.MISSING_SIGNATURE;
            } else {
                // Log.info("[OAuth] Signature: " + signature);

                final List<String> sigMethod = params.get("oauth_signature_method");
                if (sigMethod == null || sigMethod.size() != 1) {
                    result = EOAuthRequestVerifyResult.MISSING_SIGNATURE_METHOD;
                } else {
                    // Log.info("[OAuth] Signature Method: " + sigMethod);

                    final List<String> version = params.get("oauth_version");
                    if (version == null || version.size() != 1 || !"1.0".equals(version.get(0))) {
                        result = EOAuthRequestVerifyResult.BAD_OAUTH_VERSION;
                    } else {
                        result = verifyResult(req, host, path, sigMethod.get(0), signature.get(0), params);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Collects request parameters.
     *
     * @param req         the request
     * @param requestBody the lines from the request body
     * @param params      the map to populate
     * @return a map from parameter name to a list of values
     */
    private static Map<String, List<String>> collectParameters(final HttpServletRequest req, final String requestBody,
                                                               final Map<String, List<String>> params) {

        // Construct the list of normalized parameters per 3.4.1.3.2 of rfc5849

        // Log.info("Q: " + req.getQueryString());

        parseWwwFormUrlencoded(params, req.getQueryString());

        final String authHeader = req.getHeader("Authorization");
        // Log.info("A: " + authHeader);

        if (authHeader != null && !authHeader.isEmpty()) {
            String h = authHeader.trim();
            if (h.startsWith("OAuth ")) {
                h = h.substring(6).trim();

                final String[] parts = h.split(CoreConstants.COMMA);
                for (final String part : parts) {
                    final String trimmed = part.trim();
                    final int eq = trimmed.indexOf('=');
                    if (eq < 0) {
                        Log.warning("Suspicious entry in Authorization: ", trimmed);
                    } else {
                        final String name = percentDecode(trimmed.substring(0, eq));
                        final String value = percentDecode(trimmed.substring(eq + 1));
                        final List<String> values = params.computeIfAbsent(name, s -> new ArrayList<>(2));
                        values.add(value);
                    }
                }
            }
        }

        // Log.info("B: " + requestBody);

        parseWwwFormUrlencoded(params, requestBody);

        return params;
    }

    /**
     * Parses "application/x-www-form-urlencoded" data into a collection of name/value parameter pairs.
     *
     * @param params  the map to which to add extracted parameters - a sorted map from concatenation of name+value to
     *                [name, value] array
     * @param toParse the string to parse
     */
    private static void parseWwwFormUrlencoded(final Map<? super String, List<String>> params, final String toParse) {

        if (toParse != null && !toParse.isEmpty()) {
            final String[] split = toParse.split("&");

            // Log.info("Scanning " + split.length
            // + " x-www-form-urlencoded parameters");

            for (final String part : split) {
                final String trimmed = part.trim();

                final int eq = trimmed.indexOf('=');
                if (eq < 0) {
                    Log.warning("Suspicious entry in www-form-urlencoded: ", trimmed);
                } else {
                    final String name = percentDecode(part.substring(0, eq));
                    final String value;
                    if ("oauth_signature".equals(name)) {
                        // Don't treat "+" as space in a signature
                        value = percentDecode(part.substring(eq + 1));
                    } else {
                        value = percentDecode(part.substring(eq + 1)).replace('+', ' ');
                    }
                    final List<String> values = params.computeIfAbsent(name, object -> new ArrayList<>(2));
                    values.add(value);
                }
            }
        }
    }

    /**
     * Reads the requests body.
     *
     * @param req the request
     * @return the list of lines read
     */
    private static String readRequestBody(final ServletRequest req) {

        final HtmlBuilder requestBody = new HtmlBuilder(500);

        try {
            try (final BufferedReader r = req.getReader()) {
                String line = r.readLine();
                while (line != null) {
                    requestBody.addln(line);
                    line = r.readLine();
                }
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        // Log.fine("[OAuth] Request body:");
        // Log.fine(requestBody.toString());

        return requestBody.toString();
    }

    /**
     * Verifies a request using OAuth 1.0.
     *
     * @param req       the request
     * @param host      the host (in lowercase)
     * @param path      the request path from the request URI
     * @param sigMethod the signature method
     * @param signature the provided OAuth signature
     * @param params    the parameter map to populate
     * @return the result
     */
    private static EOAuthRequestVerifyResult verifyResult(final ServletRequest req, final String host,
                                                          final String path, final String sigMethod,
                                                          final String signature,
                                                          final Map<String, List<String>> params) {

        final String baseString = computeBaseString(req, host, path, params);

        final String sig;

        // Next is HMAC-SHA1
        if ("HMAC-SHA1".equals(sigMethod)) {
            final String key = percentEncode(SHARED_SECRET) + "&";
            final byte[] sigBytes = HmacSha1.hmacSha1(key.getBytes(StandardCharsets.UTF_8),
                    baseString.getBytes(StandardCharsets.UTF_8));
            sig = Base64.encode(sigBytes).trim();
        } else if ("PLAINTEXT".equals(sigMethod)) {
            sig = percentEncode(SHARED_SECRET) + "&";
        } else {
            Log.warning("Unsupported signature method: ", sigMethod);
            sig = CoreConstants.EMPTY;
        }

        // Log.info("Computed signature: ", sig);
        // Log.info("Provided signature: ", signature.trim());

        final EOAuthRequestVerifyResult result;

        if (sig.equals(signature.trim())) {
            // Log.info("*** OAuth signatures match");
            result = EOAuthRequestVerifyResult.VERIFIED;
        } else {
            // Log.info("*** OAuth signatures DO NOT match");
            result = EOAuthRequestVerifyResult.SIGNATURE_MISMATCH;
        }

        return result;
    }

    /**
     * Computes the base string for the request.
     *
     * @param req    the request
     * @param host   the host (in lowercase)
     * @param path   the request path from the request URI
     * @param params the parameter map to populate
     * @return the base string
     */
    private static String computeBaseString(final ServletRequest req, final String host,
                                            final String path, final Map<String, List<String>> params) {

        // Base string is:

        // 1. http Request method, in uppercase
        // 2. '&'
        // 3. Base string URI, encoded
        // 4. '&'
        // 5. Request parameters, normalized, then encoded

        final HtmlBuilder baseString = new HtmlBuilder(250);
        baseString.add("POST&");

        final HtmlBuilder bsUri = new HtmlBuilder(50);
        String scheme = req.getScheme();
        if (scheme == null || scheme.isEmpty()) {
            scheme = "https";
        } else {
            scheme = scheme.toLowerCase(Locale.ROOT);
        }
        bsUri.add(scheme).add("://");
        if (host.endsWith(":443")) {
            bsUri.add(host.substring(0, host.length() - 4));
        } else {
            bsUri.add(host);
        }
        bsUri.add(path);
        // Log.info("Base String URI: " + bsUri.toString());
        baseString.add(percentEncode(bsUri.toString())).add('&');

        // Sort by name then value (omitting "realm" and "oauth_signature")

        final List<String> pairs = new ArrayList<>(params.size() + 3);
        for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
            final String name = entry.getKey();
            if ("realm".equals(name) || "oauth_signature".equals(name)) {
                continue;
            }

            for (final String v : entry.getValue()) {
                pairs.add(percentEncode(name) + "=" + percentEncode(v));
            }
        }
        pairs.sort(null);

        final HtmlBuilder normalized = new HtmlBuilder(50);
        boolean and = false;
        for (final String pair : pairs) {
            if (and) {
                normalized.add('&');
            }
            normalized.add(pair);
            and = true;
        }
        // Log.info("Normalized parameters: " + normalized.toString());
        baseString.add(percentEncode(normalized.toString()));

        // Log.info("Completed Base string: ", baseString.toString());

        return baseString.toString();
    }

    /**
     * Percent-encodes a string as specified in 3.6 of rfc5849.
     *
     * @param str the string to encode
     * @return the encoded string
     */
    private static String percentEncode(final String str) {

        final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        final HtmlBuilder encoded = new HtmlBuilder((bytes.length << 2) / 3);

        for (final int byt : bytes) {
            if (byt >= 'A' && byt <= 'Z' || byt >= 'a' && byt <= 'z' || byt >= '0' && byt <= '9' || byt == '-'
                    || byt == '.' || byt == '_' || byt == '~') {
                encoded.add((char) byt);
            } else {
                encoded.add('%');
                encoded.add(HEX.charAt((byt & 0xF0) >> 4));
                encoded.add(HEX.charAt(byt & 0x0F));
            }
        }

        return encoded.toString();
    }

    /**
     * Percent-decodes a string as specified in 3.6 of rfc5849.
     *
     * @param str the string to encode
     * @return the encoded string
     */
    private static String percentDecode(final String str) {

        final char[] chars = str.toCharArray();
        final int len = chars.length;

        final HtmlBuilder decoded = new HtmlBuilder(len);

        int i = 0;
        while (i < len) {
            final char ch = chars[i];

            if (ch == '%' && i + 2 < len) {
                final int hex1 = HEX.indexOf(chars[i + 1]);
                final int hex2 = HEX.indexOf(chars[i + 2]);

                if (hex1 >= 0 && hex2 >= 0) {
                    decoded.add((char) ((hex1 << 4) + hex2));
                    i += 3;
                } else {
                    decoded.add('%');
                    ++i;
                }
            } else {
                decoded.add(ch);
                ++i;
            }
        }

        return decoded.toString();
    }
}
