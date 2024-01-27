package dev.mathops.web.front;

import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A singleton that loads the build date/time on servlet initialization, and makes that available to servlet handlers to
 * decorate generated pages.
 */
public final class BuildDateTime {

    /** Object on which to synchronize instance creation. */
    private static final Object SYNCH = new Object();

    /** Filename to load. */
    private static final String FILENAME = "date.txt";

    /** The singleton instance. */
    private static BuildDateTime instance;

    /** The date/time when the WAR file was built. */
    public final String value;

    /**
     * Constructs a new {@code BuildDateTime}, which loads and parses the build date/time from a text file (created
     * during WAR file construction).
     */
    private BuildDateTime() {

        String datetime = null;

        try (final InputStream in =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(FILENAME)) {

            if (in != null) {
                final byte[] data = new byte[50];
                final int size = in.read(data);
                datetime = new String(data, 0, size, StandardCharsets.UTF_8).trim();
            }
        } catch (final IOException ex) {
            Log.warning(Res.get(Res.CANT_GET_BUILD_DTIME), ex);
        }

        this.value = datetime;
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static BuildDateTime get() {

        synchronized (SYNCH) {
            if (instance == null) {
                instance = new BuildDateTime();
            }

            return instance;
        }
    }
}
