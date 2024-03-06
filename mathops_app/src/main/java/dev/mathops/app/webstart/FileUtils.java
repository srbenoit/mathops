package dev.mathops.app.webstart;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * File utilities.
 */
enum FileUtils {
    ;

    /** Indent for exception stack trace. */
    private static final String INDENT = "                     ";

    /**
     * Attempts to copy a source file to a destination.
     *
     * @param source the source directory
     * @param dest   the target directory
     * @return true if copy was successful; false if not
     */
    static boolean copyFile(final File source, final File dest) {

        boolean success = false;

        final byte[] buf = new byte[65536];

        try (final InputStream in = new FileInputStream(source)) {
            try (final OutputStream out = new FileOutputStream(dest)) {

                int size = in.read(buf);
                while (size > 0) {
                    out.write(buf, 0, size);
                    size = in.read(buf);
                }
                success = true;
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        if (success) {
            final byte[] f1 = FileLoader.loadFileAsBytes(source, false);
            final byte[] f2 = FileLoader.loadFileAsBytes(dest, false);

            if (!Arrays.equals(f1, f2)) {
                Log.warning(dest.getAbsolutePath() + ": Mismatch after copy - aborting");
                dest.delete();
                success = false;
            }
        }

        return success;
    }

    /**
     * Attempts to find the Java executable.
     *
     * @return the Java executable if found; null if not
     */
    static File findJavaExe() {

        File javaExe = null;

        final File javaHome = new File(System.getProperty("java.home"));
        final File javaBin = new File(javaHome, "bin");
        if (javaBin.exists()) {
            javaExe = new File(javaBin, "java.exe");
            if (!javaExe.exists()) {
                javaExe = new File(javaBin, "java");
                if (!javaExe.exists()) {
                    javaExe = null;
                }
            }
        }
        if (javaExe == null) {
            javaExe = new File(javaHome, "java.exe");
            if (!javaExe.exists()) {
                javaExe = new File(javaHome, "java");
                if (!javaExe.exists()) {
                    javaExe = null;
                }
            }
        }
        if (javaExe == null) {
            final File jre = new File(javaHome, "jre");
            final File jreBin = new File(jre, "bin");
            javaExe = new File(jreBin, "java.exe");
            if (!javaExe.exists()) {
                javaExe = new File(jreBin, "java");
                if (!javaExe.exists()) {
                    javaExe = null;
                }
            }
        }

        return javaExe;
    }

    /**
     * Appends a message to a log file.
     *
     * @param logFile the log file to which to append
     * @param msg     the objects that make up the message (if Exception, a stack trace is written; for all other
     *                objects; the result of {@code toString} is written)
     */
    static void log(final File logFile, final Object... msg) {

        final StringBuilder builder = new StringBuilder(100);

        for (final Object o : msg) {
            if (o instanceof Throwable thrown) {

                while (thrown != null) {
                    builder.append(CoreConstants.CRLF);
                    builder.append(INDENT).append(thrown.getClass().getSimpleName());

                    if (thrown.getLocalizedMessage() != null) {
                        builder.append(": ").append(thrown.getLocalizedMessage());
                    }

                    final StackTraceElement[] stack = thrown.getStackTrace();

                    for (final StackTraceElement stackTraceElement : stack) {
                        builder.append(CoreConstants.CRLF);
                        builder.append(INDENT).append(stackTraceElement.toString());
                    }

                    thrown = thrown.getCause();

                    if (thrown != null) {
                        builder.append(CoreConstants.CRLF);
                        builder.append(INDENT).append("CAUSED BY:");
                    }
                }

            } else {
                builder.append(o.toString());
            }
        }

        final String str = builder.toString();
        Log.info(str);

        try (final FileWriter w = new FileWriter(logFile, true)) {
            w.write(str);
            w.write(CoreConstants.CRLF);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }
}
