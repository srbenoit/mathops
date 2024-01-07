package dev.mathops.app.deploy;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.installation.Installation;
import dev.mathops.core.installation.Installations;
import dev.mathops.core.log.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Constructs the JWabbit app JAR file.
 */
final class JWabbitJarBuilder {

    /** Directory where project is stored. */
    private final File projectDir;

    /**
     * Constructs a new {@code AdminJarBuilder}.
     */
    private JWabbitJarBuilder() {

        final File userDir = new File(System.getProperty("user.home"));
        final File dev = new File(userDir, "dev");
        final File idea = new File(dev, "IDEA");
        this.projectDir = new File(idea, "mathops");
    }

    /**
     * Builds the WAR file.
     */
    private void build() {

        final File core = new File(this.projectDir, "mathops_core");
        final File coreRoot = new File(core, "build/classes/java/main");
        final File coreClasses = new File(coreRoot, "dev/mathops/core");

        final File jwabbit = new File(this.projectDir, "jwabbit");
        final File jwabbitRoot = new File(jwabbit, "build/classes/java/main");
        final File jwabbitClasses = new File(jwabbitRoot, "jwabbit");

        final File jars = new File(this.projectDir, "jars");

        boolean success = checkDirectoriesExist(coreClasses, jwabbitClasses, jars);

        if (success) {
            try (final FileOutputStream out = new FileOutputStream(new File(jars, "jwabbit.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, core), CoreConstants.CRLF);
                addFiles(coreRoot, coreClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, jwabbit), CoreConstants.CRLF);
                addFiles(jwabbitRoot, jwabbitClasses, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.JAR_DONE, "jwabbit"), CoreConstants.CRLF);

            } catch (final IOException ex) {
                Log.warning(Res.get(Res.JAR_WRITE_FAILED), ex);
            }
        }
    }

    /**
     * Recursively adds all files in a directory to a jar output stream.
     *
     * @param rootDir the root directory of the file tree
     * @param dir     the directory
     * @param jar     the jar output stream to which to add entries
     * @throws IOException if an exception occurs while writing
     */
    private void addFiles(final File rootDir, final File dir, final JarOutputStream jar)
            throws IOException {

        final int count = copyFiles(rootDir, dir, jar);

        if (count > 0) {
            // Build a log message with package name and number of files added
            String pkgName = dir.getName();

            File temp = dir.getParentFile();
            final StringBuilder builder = new StringBuilder(100);

            while (!temp.equals(rootDir)) {
                builder.append(temp.getName()).append('.').append(pkgName);
                pkgName = builder.toString();
                builder.setLength(0);
                temp = temp.getParentFile();
            }

            final HtmlBuilder msg = new HtmlBuilder(80);
            msg.add(' ').add(pkgName).padToLength(55);
            msg.addln(": ", Integer.toString(count), CoreConstants.SPC, Res.get(Res.FILES_COPIED));
            Log.finest(msg.toString());
        }
    }

    /**
     * Recursively copies files from a directory into the Jar stream.
     *
     * @param rootDir the root directory of the file tree
     * @param dir     the directory
     * @param jar     the jar output stream to which to add entries
     * @return the number of files copied
     * @throws IOException if an exception occurs while writing
     */
    private int copyFiles(final File rootDir, final File dir, final JarOutputStream jar) throws IOException {

        final File[] files = dir.listFiles();

        int count = 0;
        if (files != null) {
            for (final File file : files) {

                String name = file.getName();
                if ("package-info.class".equals(name) || "module-info.class".equals(name)) {
                    continue;
                }

                // Prepend relative path to the name
                File parent = file.getParentFile();
                final HtmlBuilder builder = new HtmlBuilder(100);
                while (!parent.equals(rootDir)) {
                    builder.add(parent.getName(), CoreConstants.SLASH, name);
                    name = builder.toString();
                    builder.reset();
                    parent = parent.getParentFile();
                }

                if (file.isDirectory()) {
                    addFiles(rootDir, file, jar);
                } else {
                    Log.info("Adding '", name, "'");
                    jar.putNextEntry(new ZipEntry(name));
                    final byte[] bytes = FileLoader.loadFileAsBytes(file, true);
                    if (bytes == null) {
                        throw new IOException(Res.fmt(Res.READ_FAILED, file.getAbsolutePath()));
                    }
                    jar.write(bytes);
                    ++count;
                }
                jar.closeEntry();
            }
        }

        return count;
    }

    /**
     * Given a list of {@code File} objects, tests that each exists and is a directory.
     *
     * @param dirs the list of {@code File} objects
     * @return {@code true} if all {@code File} represent existing directories
     */
    private static boolean checkDirectoriesExist(final File... dirs) {

        boolean good = true;

        for (final File test : dirs) {
            if (!test.exists() || !test.isDirectory()) {
                Log.warning(Res.fmt(Res.DIR_NOT_FOUND, test.getAbsolutePath()));
                good = false;
                break;
            }
        }

        return good;
    }

    /**
     * Adds the manifest file to a jar output stream.
     *
     * @param jar the {@code JarOutputStream} to which to add the manifest
     * @throws IOException if an exception occurs while writing
     */
    private static void addManifest(final JarOutputStream jar) throws IOException {

        jar.putNextEntry(new ZipEntry("META-INF/"));
        jar.closeEntry();

        final HtmlBuilder htm = new HtmlBuilder(500);
        htm.addln("Manifest-Version: 1.0");
        htm.addln("Application-Name: JWabbit TI Emulator");
        htm.addln("Permissions: all-permissions");
        htm.addln("Codebase: *");
        htm.addln("Application-Library-Allowable-Codebase: *");
        htm.addln("Caller-Allowable-Codebase: *");
        htm.addln("Created-By: JWabbitJarBuilder 2.00");
        htm.addln("Main-Class: jwabbit.Launcher");

        jar.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        jar.write(bytes);
        jar.closeEntry();
    }

    /**
     * Main method to execute the builder.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        // Use the default installation
        final Installation installation = Installations.get().getInstallation(null, null);
        Installations.setMyInstallation(installation);

        new JWabbitJarBuilder().build();
        Log.finest(Res.get(Res.FINISHED), CoreConstants.CRLF);
    }
}
