package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Gathers installation parameters like the domain name and whether to install as a PROD or a DEV system.
 */
enum GatherParameters {
    ;

    /**
     * Performs the gathering.
     *
     * @param state the build state
     * @return {@code true} if the source tree is acceptable
     */
    static boolean areParametersValid(final BuildState state) {

        boolean ok;

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            ok = isDomainSpecified(state, reader) && isTypeSpecified(state, reader);
        } catch (final IOException ex) {
            Log.fine("Error reading console input");
            ok = false;
        }

        Log.fine();

        return ok;
    }

    /**
     * Verifies that the "/opt/build" directory exists.
     *
     * @param state  the state (if successful, the /opt/build directory is stored therein)
     * @param reader the reader
     * @return {@code true} if the directory exists
     * @throws IOException if there is an exception reading from {@code System.in}
     */
    private static boolean isDomainSpecified(final BuildState state, final BufferedReader reader) throws IOException {

        boolean ok;

        Log.fine();
        Log.fine("Under what domain name will this server operate?");

        for (; ; ) {
            Log.fine("[default: math.colostate.edu] : ");
            String domain = reader.readLine();

            if (domain == null || domain.trim().isEmpty()) {
                domain = "math.colostate.edu";
                state.setDomain(domain);
                Log.fine("Using default domain...");
                ok = true;
                break;
            }

            ok = isDomainValid(domain);
            if (ok) {
                state.setDomain(domain);
                Log.fine("Using domain: " + domain);
                break;
            }
        }

        return ok;
    }

    /**
     * Validates that a domain contains tokens (each consisting of letters, numbers, and dashed, not starting or ending
     * with a dash) separated by dots.
     *
     * @param theDomain the domain to validate
     * @return {@code true} if valid
     */
    private static boolean isDomainValid(final String theDomain) {

        boolean ok;

        if (theDomain.startsWith(CoreConstants.DOT) || theDomain.endsWith(CoreConstants.DOT)) {
            Log.fine("Domain may not begin or end with '.'");
            ok = false;
        } else {
            final String[] tokens = theDomain.split("\\.");
            if (tokens.length < 2) {
                Log.fine("Domain must include at least one '.'");
                ok = false;
            } else {
                ok = true;
                for (final String token : tokens) {
                    ok = ok && isTokenValid(token);
                }
            }
        }

        return ok;
    }

    /**
     * Validates a token in a domain. A token must not start or end with '-', and must contain only letters, digits, and
     * '-' characters.
     *
     * @param theToken the token to validate
     * @return {@code true} if valid
     */
    private static boolean isTokenValid(final String theToken) {

        boolean ok;

        if (theToken.startsWith(CoreConstants.DASH) || theToken.endsWith(CoreConstants.DASH)) {
            Log.fine("Domain components may not begin or end with '-'");
            ok = false;
        } else {
            final char[] chars = theToken.toCharArray();
            if (chars.length < 1) {
                Log.fine("Domain may not contain adjacent '.' characters");
                ok = false;
            } else {
                ok = true;
                for (final char c : chars) {
                    if (c != '-' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9')) {
                        Log.fine("Invalid character in domain: " + c);
                        ok = false;
                        break;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * Verifies that the "/opt/build/src" directory exists.
     *
     * @param state  the state (if successful, the /opt/build/src directory is stored therein)
     * @param reader the reader
     * @return {@code true} if the directory exists
     * @throws IOException if there is an exception reading from {@code System.in}
     */
    private static boolean isTypeSpecified(final BuildState state, final BufferedReader reader) throws IOException {

        boolean ok;

        Log.fine();
        Log.fine("What type of server will this be (PROD | DEV | TEST)?");

        for (; ; ) {
            Log.fine("[default: PROD] : ");
            final String str = reader.readLine();

            String type;
            if (str == null) {
                type = "PROD";
                Log.fine("Using default type...");
                ok = true;
            } else {
                type = str.trim().toUpperCase(Locale.ROOT);

                if (type.isEmpty()) {
                    type = "PROD";
                    Log.fine("Using default type...");
                    ok = true;
                } else if ("PROD".equals(type) || "DEV".equals(type) || "TEST".equals(type)) {
                    Log.fine("Using type: " + type);
                    ok = true;
                } else {
                    Log.fine("ERROR: Invalid type.");
                    ok = false;
                }
            }

            if (ok) {
                state.setType(type);
                break;
            }
        }

        return ok;
    }
}
