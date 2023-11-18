package dev.mathops.core.log;

import dev.mathops.core.builder.HtmlBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Rotates log files. Renames each indexed archive log file to the next larger index, discarding the file with maximum
 * index file if one exists, then renames the active log file to the first index archive. This is a utility class, not
 * intended to be instantiated, with only static methods.
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
    static String rotateLogs(final File logDir, final String fnameBase, final long maxNumFiles, final File curFile) {

        final HtmlBuilder err = new HtmlBuilder(100);

        // see how many numbered log files there are
        int onFile = 1;
        while ((long) onFile < maxNumFiles) {
            final String filename = makeFilename(fnameBase, onFile);
            final File file = new File(logDir, filename);
            if (!file.exists()) {
                break;
            }
            ++onFile;
        }

        // "onFile" will be one larger than the highest numbered log file found

        // starting at index of last file and working downward, rename files
        final String filename0 = makeFilename(fnameBase, onFile);
        File dstFile = new File(logDir, filename0);
        while (onFile > 1) {
            final String filename1 = makeFilename(fnameBase, onFile - 1);
            final File srcFile = new File(logDir, filename1);
            renameFile(srcFile, dstFile, err);
            dstFile = srcFile;
            --onFile;
        }

        // Move the active log file
        if (curFile.exists()) {
            if (!curFile.renameTo(dstFile)) {
                final String dstPath = dstFile.getAbsolutePath();
                final String curPath = curFile.getAbsolutePath();
                final String msg1 = Res.fmt(Res.RENAME_FAIL, curPath, dstPath);
                err.addln(msg1);

                if (copyFile(curFile, dstFile, err)) {
                    if (!curFile.delete()) {
                        final String msg2 = Res.fmt(Res.DELETE_FAIL, curPath);
                        err.addln(msg2);
                    }
                } else {
                    final String msg3 = Res.fmt(Res.COPY_FAIL, curPath, dstPath);
                    err.addln(msg3);
                }
            }
        }

        return err.length() > 0 ? err.toString() : null;
    }

    /**
     * Builds the filename of the archived log file for a given index.
     *
     * @param filenameBase the base of log filenames
     * @param index        the index
     * @return the log file name
     */
    private static String makeFilename(final String filenameBase, final int index) {

        return filenameBase + "_"
                + index / DEC_BASE / DEC_BASE
                + (index / DEC_BASE) % DEC_BASE + index % DEC_BASE
                + EXTENTION;
    }

    /**
     * Attempts to rename a source file to a destination file.
     *
     * @param srcFile the source file
     * @param dstFile the destination file
     * @param err     a {@code HtmlBuilder} to which to log errors
     */
    private static void renameFile(final File srcFile, final File dstFile, final HtmlBuilder err) {

        if (dstFile.exists() && !dstFile.delete()) {
            final String srcpath = srcFile.getPath();
            final String dstPath = dstFile.getPath();
            final String msg = Res.fmt(Res.COPY_FAIL, srcpath, dstPath);
            err.addln(msg);
        } else if (!srcFile.renameTo(dstFile)) {
            final String srcpath = srcFile.getPath();
            final String dstPath = dstFile.getPath();
            final String msg = Res.fmt(Res.RENAME_FAIL, srcpath, dstPath);
            err.addln(msg);
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
    private static boolean copyFile(final File source, final File dest, final HtmlBuilder err) {

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
            final String exMessage = ex.getMessage();
            final String msg = Res.fmt(Res.COPY_ERROR, exMessage);
            err.addln(msg);
        }

        return ok;
    }
}
