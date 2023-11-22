package dev.mathops.app;

import dev.mathops.core.log.Log;

import java.io.File;
import java.io.FileFilter;

/**
 * Scans the temporary folder for files with names matching "+~JF*.tmp", and deleting * them.
 */
public final class TempFileCleaner extends Thread {

    /**
     * Constructs a new {@code TempFileCleaner}.
     */
    public TempFileCleaner() {

        super("TempFileCleaner");
    }

    /**
     * Cleans the temporary files.
     */
    public static void clean() {

        final long now = System.currentTimeMillis();

        try {
            final File tmp = new File(System.getProperty("java.io.tmpdir"));

            if (tmp.exists() && tmp.isDirectory()) {
                int count = 0;

                final File[] list = tmp.listFiles(new TempFileFilter());
                if (list != null) {
                    for (final File file : list) {
                        final long age = now - file.lastModified();

                        if (age > 86400000L && file.delete()) {
                            ++count;
                        }
                    }
                }

                Log.info("Cleaned ", Integer.toString(count), " of ", Integer.toString(list.length), " files");
            }
        } catch (final RuntimeException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Thread method allowing the cleaning process to be run in a Thread.
     */
    @Override
    public void run() {

        clean();
    }

    /**
     * Main method to run the cleaning process.
     *
     * @param args Command-line arguments.
     */
    public static void main(final String... args) {

        clean();
    }
}

/**
 * A file filter that matches only Java temporary files with the format "+~JF*.tmp".
 */
final class TempFileFilter implements FileFilter {

    /**
     * Constructs a new {@code TempFileFilter}.
     */
    TempFileFilter() {

        // No action
    }

    /**
     * Test whether the f should be included in the file list.
     *
     * @param pathname The file to test.
     * @return True if the file matches the pattern; false otherwise.
     */
    @Override
    public boolean accept(final File pathname) {

        final String name = pathname.getName();

        return name.startsWith("+~JF") && name.endsWith(".tmp");
    }
}
