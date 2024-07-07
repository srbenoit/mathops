package dev.mathops.web.site;

import dev.mathops.commons.log.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

/**
 * Reads a named key from a keystore and dumps it to a binary file in its native encoding format.
 * <p>
 * Generate a key pair with keytool, and use it to create a certificate request, then use this tool to dump the private
 * key to a binary file for use in OpenSSL.
 * <p>
 * Based on the DumpKey utility, Copyright 2007 by <a href="http://www.herongyang.com/">Dr. Herong Yang</a>.
 */
final class DumpKey {

    /**
     * Private constructor to prevent instantiation.
     */
    private DumpKey() {

        // No action
    }

    /**
     * Main method that runs the program.
     *
     * @param args command line arguments
     */
    public static void main(final String... args) {

        final String[] realArgs;
        if (args.length == 5) {
            realArgs = args;
        } else {
            // realArgs = new String[5];
            // realArgs[0] = "/bls/certs/godaddy.bekenlearning.com.signer.jks";
            // realArgs[1] = "tearsinrain";
            // realArgs[2] = "signer";
            // realArgs[3] = "tearsinrain";
            // realArgs[4] = "/bls/certs/godaddy.dumped";
            Log.fine("Usage:");
            Log.fine("java -cp bls8.jar edu.colostate.math.servlet.DumpKey ",
                    "keystorefile keystorepass alias keypass outfile");
            return;
        }

        final String jksFile = realArgs[0];
        final char[] jksPass = realArgs[1] == null ? null : realArgs[1].toCharArray();
        final String keyName = realArgs[2];
        final char[] keyPass = realArgs[3] == null ? null : realArgs[3].toCharArray();
        final String outFile = realArgs[4];

        try (final InputStream instr = new FileInputStream(jksFile);
             final FileOutputStream out = new FileOutputStream(outFile)) {

            final KeyStore jks = KeyStore.getInstance("jks");
            jks.load(instr, jksPass);

            final Key key = jks.getKey(keyName, keyPass);
            Log.info("Key algorithm: " + key.getAlgorithm());
            Log.info("Key format: " + key.getFormat());
            Log.info("Writing key in binary form to " + outFile);

            out.write(key.getEncoded());
        } catch (final Exception ex) {
            Log.warning("Failed to convert key", ex);
        }
    }
}
