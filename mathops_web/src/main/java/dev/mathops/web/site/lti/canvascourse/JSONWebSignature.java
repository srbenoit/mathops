package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

/**
 * An implementation of JSON Web Signature (RFC 7515).
 */
public enum JSONWebSignature {
    ;

    /**
     * Validates a JSON Web Signature (JWS).
     *
     * @param jws the web signature in encoded form
     */
    static void validate(final String jws) {

        Log.info("Validating a JWS");

        final int dot1 = jws.indexOf('.');
        final int dot2 = jws.lastIndexOf('.');

        if (dot1 == -1 || dot2 == -1 || dot2 == dot1) {
            Log.warning("Failed to parse JWS in compact form.");
            // TODO: Try the JSON form?
        } else {
            final String part1 = jws.substring(0, dot1);
            final String part2 = jws.substring(dot1 + 1, dot2);
            final String part3 = jws.substring(dot2 + 1);
            validateCompactForm(part1, part2, part3);
        }
    }

    /**
     * Validates a JWS in the compact form.
     *
     * @param part1 the part of the JWS compact form that preceded the first period character
     * @param part2 the part of the JWS compact form between the first and last period characters
     * @param part3 the part of the JWS compact form after the last period
     */
    private static void validateCompactForm(final String part1, final String part2, final String part3) {

        final Base64.Decoder dec = Base64.getUrlDecoder();
        try {
            final byte[] bytes1 = dec.decode(part1);
            final String str1 = new String(bytes1, StandardCharsets.UTF_8);
            try {
                if (JSONParser.parseJSON(str1) instanceof final JSONObject joseHeader) {
                    final String alg = validateProtectedHeader(joseHeader);

                    if (alg != null) {

                        final byte[] bytes2 = dec.decode(part2);
                        final String str2 = new String(bytes2, StandardCharsets.UTF_8);

                        final byte[] bytes3 = dec.decode(part3);

                        Log.fine(str1);
                        Log.fine(str2);
                    }
                }
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse JWS protected header as JSON.", ex);
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning("Failed to Base64 decode JWS in compact form.", ex);
        }
    }

    /**
     * Verifies that all values in a JSON object representing a protected header are recognized and supported.
     *
     * @param joseHeader the header
     * @return the algorithm if valid; null if not
     */
    private static String validateProtectedHeader(final JSONObject joseHeader) {

        // {"typ":"JWT","alg":"RS256","kid":"2018-07-18T22:33:20Z"}

        final String result;

        final String alg = joseHeader.getStringProperty("alg");
        if ("RS256".equals(alg) || "ES256".equals(alg) || "RSA1_5".equals(alg) || "RSA-OAEP".equals(alg)
            || "A128KW".equals(alg) || "A256KW".equals(alg) || "dir".equals(alg) || "ECDH-ES".equals(alg)
            || " ECDH-ES+A128KW".equals(alg) || "ECDH-ES+A256KW".equals(alg)) {

            result = alg;
        } else {
            Log.warning("Unsupported 'alg' value in protected JWS header: ", alg);
            result = null;
        }

        return result;
    }
}
