package dev.mathops.web.front;

import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A utility class that loads the build date/time on servlet initialization, and makes that available to servlet
 * handlers to decorate generated pages.
 */
public enum BuildDateTime {
    ;

    /** Filename to load. */
    private static final String FILENAME = "date.txt";

    /**
     * Reads and returns the build date/time from the date/time file.
     *
     * @return the string read; {@code null} if unable to read
     */
    public static String getValue() {

        String datetime = null;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILENAME)) {

            if (in != null) {
                final byte[] data = new byte[50];
                final int size = in.read(data);
                datetime = new String(data, 0, size, StandardCharsets.UTF_8).trim();
            }
        } catch (final IOException ex) {
            final String msg = Res.get(Res.CANT_GET_BUILD_DTIME);
            Log.warning(msg, ex);
        }

        return datetime;
    }
}
