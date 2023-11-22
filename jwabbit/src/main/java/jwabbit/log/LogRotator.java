package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Rotates log files. Renames each indexed archive log file to the next larger index, discarding the file with maximum
 * index file if one exists, then renames the active log file to the first index archive.
 */
final class LogRotator {

    /** File extension for log files. */
    private static final String EXTENTION = ".log";

    /** Base for decimal numbers. */
    private static final int DEC_BASE = 10;

    /** Buffer size for file copies. */
    private static final int BUF_SIZE = 2048;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LogRotator() {

        super();
    }

    /**
     * Rotates the log files when the active log file reaches its file size limit.
     *
     * @param logDir      the log directory
     * @param fnameBase   the base of log filenames
     * @param maxNumFiles the maximum number of files
     * @param curFile     the currently active log file
     * @return error text on failure
     */
    static String rotateLogs(final File logDir, final String fnameBase, final int maxNumFiles, final File curFile) {

        final StringBuilder err = new StringBuilder(100);

        // see how many log files there are
        int onFile = 1;
        while (onFile < maxNumFiles) {
            if (!new File(logDir, makeFilename(fnameBase, onFile)).exists()) {
                break;
            }
            ++onFile;
        }

        // starting at index of last file and working downward, rename files
        File dstFile = new File(logDir, makeFilename(fnameBase, onFile));

        while (onFile > 1) {
            final String source = makeFilename(fnameBase, onFile - 1);
            final File srcFile = new File(logDir, source);

            renameFile(srcFile, dstFile, err);

            dstFile = srcFile;
            --onFile;
        }

        // Move the active log file
        if (!curFile.renameTo(dstFile)) {
            err.append("Unable to rename ").append(curFile.getPath()).append(" to ").append(dstFile.getPath())
                    .append(" while rotating logs");

            if (copyFile(curFile, dstFile, err)) {
                if (!curFile.delete()) {
                    err.append("Unable to delete ").append(curFile.getPath());
                }
            } else {
                err.append("Unable to copy ").append(curFile.getPath()).append(" to ").append(dstFile.getPath())
                        .append(" while rotating logs");
            }
        }

        return err.isEmpty() ? null : err.toString();
    }

    /**
     * Builds the filename of the archived log file for a given index.
     *
     * @param filenameBase the base of log filenames
     * @param index        the index
     * @return the log file name
     */
    private static String makeFilename(final String filenameBase, final int index) {

        return filenameBase + '_' + index / DEC_BASE / DEC_BASE + (index / DEC_BASE) % DEC_BASE + index % DEC_BASE
                + EXTENTION;
    }

    /**
     * Attempts to rename a source file to a destination file.
     *
     * @param srcFile the source file
     * @param dstFile the destination file
     * @param err     a {@code StringBuilder} to which to log errors
     */
    private static void renameFile(final File srcFile, final File dstFile, final StringBuilder err) {

        if (dstFile.exists() && !dstFile.delete()) {
            err.append("Unable to copy ").append(srcFile.getPath()).append(" to ").append(dstFile.getPath())
                    .append(" while rotating logs");
        } else if (!srcFile.renameTo(dstFile)) {
            err.append("Unable to rename ").append(srcFile.getPath()).append(" to ").append(dstFile.getPath())
                    .append(" while rotating logs");
        }
    }

    /**
     * Attempts to copy a file.
     *
     * @param source the source file
     * @param dest   the destination file
     * @param err    a {@code HtmlBuilder} to which to log errors
     * @return {@code true} if copy succeeded
     */
    private static boolean copyFile(final File source, final File dest, final StringBuilder err) {

        boolean ok = false;

        final byte[] buffer = new byte[BUF_SIZE];
        try (final FileInputStream fis = new FileInputStream(source);
             final FileOutputStream fos = new FileOutputStream(dest)) {

            int len = fis.read(buffer);
            while (len > 0) {
                fos.write(buffer, 0, len);
                len = fis.read(buffer);
            }
            ok = true;
        } catch (final IOException ex) {
            err.append("Error copying file: ").append(ex.getMessage());
        }

        return ok;
    }
}
