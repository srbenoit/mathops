package dev.mathops.app.webstart;

import dev.mathops.commons.parser.HexEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * An updater that tests the presence and integrity of an update, and if valid (and newer than the current version),
 * installs the update by copying files from a source directory to a target directory. The source is typically under
 * ./appdir/update, and the targer is under ./appdir.
 */
enum FileUpdater {
    ;

    /**
     * Called when there is an updated application descriptor in the updates directory.
     *
     * <p>
     * This method verifies that all files called for in the application descriptor are present in the updates directory
     * and have correct size and hash.
     *
     * <p>
     * Prior to calling this method, the existing files from the source directory should have been archived.
     *
     * @param descriptor         the application descriptor
     * @param sourceDir          the source directory in which updates are stored
     * @param targetDir          the target directory
     * @param logFile            the log file
     * @param descriptorFilename the filename of the descriptor
     * @return true if successful; false if unsuccessful
     */
    static boolean updateApp(final AppDescriptor descriptor, final File sourceDir,
                             final File targetDir, final File logFile, final String descriptorFilename) {

        boolean success = true;

        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            // Verify all files are present and correct
            for (final FileDescriptor file : descriptor.getFiles()) {

                final File src = new File(sourceDir, file.name);

                FileUtils.log(logFile, "  Verifying ", file.name);

                if (src.length() == file.size) {
                    final byte[] hash = computeSHA256(src, sha256, logFile);

                    if (hash == null) {
                        success = false;
                    } else {
                        final byte[] expected = file.getSHA256();

                        if (!Arrays.equals(hash, expected)) {
                            FileUtils.log(logFile, "  File '", src.getAbsolutePath(),
                                    "' has hash ", HexEncoder.encodeUppercase(hash),
                                    " but descriptor in XML shows ",
                                    HexEncoder.encodeUppercase(expected));
                            success = false;
                        }
                    }
                } else {
                    FileUtils.log(logFile, "  File '", src.getAbsolutePath(),
                            "' has size ", Long.toString(src.length()),
                            " but descriptor in XML shows ", Long.toString(file.size));

                    success = false;
                }
            }

            if (success) {

                // Copy files into place
                for (final FileDescriptor file : descriptor.getFiles()) {
                    final File src = new File(sourceDir, file.name);
                    final File dst = new File(targetDir, file.name);

                    FileUtils.log(logFile, "  Installing ", file.name);

                    if (dst.exists()) {
                        dst.delete();
                    }

                    if (FileUtils.copyFile(src, dst)) {
                        src.delete();
                    } else {
                        FileUtils.log(logFile, "  Failed to copy '",
                                src.getAbsolutePath(), "' to '", dst.getAbsolutePath(),
                                "'");
                        success = false;
                        break;
                    }
                }

                if (success) {
                    final File src = new File(sourceDir, descriptorFilename);
                    src.delete();
                    writeAppDescriptorXml(descriptor, descriptorFilename, targetDir, logFile);
                } else {
                    // A file copy failed (disk full?) - delete all files in target
                    for (final FileDescriptor file : descriptor.getFiles()) {
                        final File dst = new File(targetDir, file.name);
                        if (dst.exists()) {
                            dst.delete();
                        }
                    }
                }
            }
        } catch (final NoSuchAlgorithmException ex) {
            FileUtils.log(logFile, "  Unable to obtain SHA256 digest", ex);
        }

        return success;
    }

    /**
     * Attempts to serialize an application descriptor to XML and write it to a file.
     *
     * @param desc     the application descriptor
     * @param filename the filename to which to write
     * @param dst      the file to which to write
     * @param logFile  the log file
     * @return true if successful; false if not
     */
    private static boolean writeAppDescriptorXml(final AppDescriptor desc, final String filename,
                                                 final File dst, final File logFile) {

        boolean success = false;

        try (final FileWriter fw = new FileWriter(new File(dst, filename), StandardCharsets.UTF_8)) {
            fw.write(desc.toXml());
            success = true;
        } catch (final IOException ex) {
            FileUtils.log(logFile, "  Failed to write ", filename, ex);
        }

        return success;
    }

    /**
     * Attempts to compute the SHA256 hash of a file.
     *
     * @param f       the file
     * @param sha256  the message digest
     * @param logFile the log file
     * @return the hash; null if unable to read file
     */
    private static byte[] computeSHA256(final File f, final MessageDigest sha256,
                                        final File logFile) {

        final byte[] buffer = new byte[64 * 1024];
        byte[] result = null;

        if (sha256 != null) {
            try (final InputStream in = new FileInputStream(f)) {
                int numRead = in.read(buffer);
                while (numRead > 0) {
                    sha256.update(buffer, 0, numRead);
                    numRead = in.read(buffer);
                }

                result = sha256.digest();
            } catch (final IOException ex) {
                FileUtils.log(logFile, "  Exception computing hash of '",
                        f.getAbsolutePath(), "'", ex);
            }
        }

        return result;
    }
}
