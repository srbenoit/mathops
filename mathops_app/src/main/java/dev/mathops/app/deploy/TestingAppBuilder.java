package dev.mathops.app.deploy;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.HexEncoder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.installation.Installations;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Creates the various '.jar' and '.xml' files for the "Testing" app (to be hosted on numan in the
 * /opt/public/apps/testing folder, and automatically downloaded by testing stations).
 */
final class TestingAppBuilder {

    /** The version number. */
    private static final String VERSION = "1.2.026";

    /** Directory where project is stored. */
    private final File projectDir;

    /** Directory where commons project is stored. */
    private final File commonsDir;

    /** Directory where text project is stored. */
    private final File textDir;

    /** The target directory to which to copy generated files. */
    private final File targetDir;

    /** The set of package names in the JAR file. */
    private final Set<String> jarDirs;

    /**
     * Constructs a new {@code TestingAppBuilder}.
     */
    private TestingAppBuilder() {

        final File userDir = new File(System.getProperty("user.home"));
        final File dev = new File(userDir, "dev");
        final File idea = new File(dev, "IDEA");
        this.projectDir = new File(idea, "mathops");
        this.commonsDir = new File(idea, "mathops_commons");
        this.textDir = new File(idea, "mathops_text");

        this.targetDir = new File("/opt/public/www/apps/testing");
        if (!this.targetDir.exists()) {
            if (!this.targetDir.mkdirs()) {
                Log.warning("failed to create testing directory");
            }
        }

        this.jarDirs = new HashSet<>(10);
    }

    /**
     * Builds the WAR file.
     */
    private void build() {

        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            // NOTE: non-short-circuiting AND test so we see all errors
            if (checkBuildBls8Jar() && checkBuildLaunchJar() && checkBuildUpdaterJar()) {

                final File jars = new File(this.projectDir, "jars");

                final File bls8Jar = new File(jars, "bls8.jar");

                final File commonsOut = new File(this.commonsDir, "out");
                final File commonsOutLibs = new File(commonsOut, "libs");
                final File commonsJar = new File(commonsOutLibs, "mathops_commons.jar");

                final File textOut = new File(this.textDir, "out");
                final File textOutLibs = new File(textOut, "libs");
                final File textJar = new File(textOutLibs, "mathops_text.jar");

                final File jwabbitJar = new File(jars, "jwabbit.jar");
                final File appXml = new File(jars, "app.xml");

                final File launchJar = new File(jars, "launch.jar");
                final File launchXml = new File(jars, "launch.xml");

                final File updaterJar = new File(jars, "updater.jar");
                final File updaterXml = new File(jars, "updater.xml");

                createXmlDescriptor(sha256, appXml, "testing", "dev.mathops.app.teststation.TestStationApp", bls8Jar,
                        commonsJar, textJar, jwabbitJar);
                createXmlDescriptor(sha256, launchXml, "launch", "dev.mathops.app.webstart.Launch", commonsJar, textJar,
                        launchJar);
                createXmlDescriptor(sha256, updaterXml, "updater", "dev.mathops.app.webstart.Updater", commonsJar,
                        textJar, updaterJar);

                // Copy all to /opt/public/app/testing

                copyFile(bls8Jar, this.targetDir);
                copyFile(commonsJar, this.targetDir);
                copyFile(textJar, this.targetDir);
                copyFile(jwabbitJar, this.targetDir);
                copyFile(appXml, this.targetDir);

                final File targetLaunch = new File(this.targetDir, "launch");
                if (!targetLaunch.exists()) {
                    if (!targetLaunch.mkdirs()) {
                        Log.warning("failed to create launch directory");
                    }
                }

                copyFile(commonsJar, targetLaunch);
                copyFile(textJar, targetLaunch);
                copyFile(launchJar, targetLaunch);
                copyFile(launchXml, targetLaunch);
                copyFile(updaterJar, targetLaunch);
                copyFile(updaterXml, targetLaunch);

                final String targetPath = this.targetDir.getAbsolutePath();
                final String message = Res.fmt(Res.FILE_CREATED, "Testing application files", targetPath);
                Log.finest(message, CoreConstants.CRLF);
            }
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Copies a file to a target directory. If there is already a file in the target directory with the same name, that
     * file is overwritten!
     *
     * @param source    the source file
     * @param targetDir the target directory
     */
    private static void copyFile(final File source, final File targetDir) {

        final byte[] bytes = FileLoader.loadFileAsBytes(source, true);
        if (bytes != null) {
            final File target = new File(targetDir, source.getName());
            try (final FileOutputStream out = new FileOutputStream(target)) {
                out.write(bytes);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

    }

    /**
     * Generates the "*.xml" descriptor file for a connection of application files.
     *
     * @param sha256        the message digest object
     * @param xmlFile       the file for the XML descriptor
     * @param appName       the application name
     * @param mainClassName the main class name
     * @param files         the list of files to include
     */
    private static void createXmlDescriptor(final MessageDigest sha256, final File xmlFile, final String appName,
                                            final String mainClassName, final File... files) {

        final String dateString = ZonedDateTime.now().toString();

        final HtmlBuilder xml = new HtmlBuilder(500);

        xml.addln("<app name='", appName, "' version='", VERSION, "' releaseDate='", dateString, "'");
        xml.addln("     mainClass='", mainClassName, "'>");

        for (final File file : files) {
            appendFileDescriptor(sha256, xml, file);
        }

        xml.addln("</app>");

        try (final FileWriter writer = new FileWriter(xmlFile, StandardCharsets.UTF_8)) {
            writer.write(xml.toString());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the "*.xml" descriptor file for a connection of application files.
     *
     * @param sha256 the message digest object
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param file   the file to process
     */
    private static void appendFileDescriptor(final MessageDigest sha256, final HtmlBuilder xml, final File file) {

        final byte[] bytes = FileLoader.loadFileAsBytes(file, true);
        if (bytes != null) {
            final String hex = HexEncoder.encodeUppercase(sha256.digest(bytes));
            xml.addln("  <file name='", file.getName(), "' size='", Integer.toString(bytes.length), "' sha256='", hex,
                    "'/>");
        }
    }

    /**
     * Builds the bls8.jar file.
     *
     * @return {@code true} if successful
     */
    private boolean checkBuildBls8Jar() {

        final File db = new File(this.projectDir, "mathops_db");
        final File dbRoot = new File(db, "build/classes/java/main");
        final File dbClasses = new File(dbRoot, "dev/mathops/db");

        final File font = new File(this.projectDir, "mathops_font");
        final File fontRoot = new File(font, "build/classes/java/main");
        final File fontClasses = new File(fontRoot, "dev/mathops/font");

        final File assessment = new File(this.projectDir, "mathops_assessment");
        final File assessmentRoot = new File(assessment, "build/classes/java/main");
        final File assessmentClasses = new File(assessmentRoot, "dev/mathops/assessment");

        final File session = new File(this.projectDir, "mathops_session");
        final File sessionRoot = new File(session, "build/classes/java/main");
        final File sessionClasses = new File(sessionRoot, "dev/mathops/session");

        final File app = new File(this.projectDir, "mathops_app");
        final File appRoot = new File(app, "build/classes/java/main");
        final File appClasses = new File(appRoot, "dev/mathops/app");

        final File jars = new File(this.projectDir, "jars");

        boolean success = checkDirectoriesExist(dbClasses, fontClasses, assessmentClasses, sessionClasses, appClasses,
                jars);

        if (success) {

            try (final FileOutputStream out = new FileOutputStream(new File(jars, "bls8.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, db), CoreConstants.CRLF);
                addFiles(dbRoot, dbClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, font), CoreConstants.CRLF);
                addFiles(fontRoot, fontClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, assessment), CoreConstants.CRLF);
                addFiles(assessmentRoot, assessmentClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, session), CoreConstants.CRLF);
                addFiles(sessionRoot, sessionClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, app), CoreConstants.CRLF);
                addFiles(appRoot, appClasses, jar);

                jar.finish();

                final String jarsPath = jars.getAbsolutePath();
                Log.finest(Res.fmt(Res.FILE_CREATED, "bls8", jarsPath), CoreConstants.CRLF);

            } catch (final IOException ex) {
                Log.warning(Res.get(Res.JAR_WRITE_FAILED), ex);
                success = false;
            }
        }

        return success;
    }

    /**
     * Builds the launch.jar file.
     *
     * @return {@code true} if successful
     */
    private boolean checkBuildLaunchJar() {

        final File app = new File(this.projectDir, "mathops_app");
        final File appRoot = new File(app, "build/classes/java/main");
        final File appClasses = new File(appRoot, "dev/mathops/app");
        final File wsClasses = new File(appRoot, "dev/mathops/app/webstart");

        final File jars = new File(this.projectDir, "jars");

        boolean success = checkDirectoriesExist(appClasses, jars);

        if (success) {

            try (final FileOutputStream out = new FileOutputStream(new File(jars, "launch.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, app), CoreConstants.CRLF);
                addFiles(appRoot, wsClasses, jar);

                jar.finish();

                final String jarsPath = jars.getAbsolutePath();
                Log.finest(Res.fmt(Res.FILE_CREATED, "launch", jarsPath), CoreConstants.CRLF);
            } catch (final IOException ex) {
                Log.warning(Res.get(Res.JAR_WRITE_FAILED), ex);
                success = false;
            }
        }

        return success;
    }

    /**
     * Builds the updater.jar file.
     *
     * @return {@code true} if successful
     */
    private boolean checkBuildUpdaterJar() {

        final File app = new File(this.projectDir, "mathops_app");
        final File appRoot = new File(app, "build/classes/java/main");
        final File appClasses = new File(appRoot, "dev/mathops/app");
        final File wsClasses = new File(appRoot, "dev/mathops/app/webstart");

        final File jars = new File(this.projectDir, "jars");

        boolean success = checkDirectoriesExist(appClasses, jars);

        if (success) {
            try (final FileOutputStream out = new FileOutputStream(new File(jars, "updater.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, app), CoreConstants.CRLF);
                addFiles(appRoot, wsClasses, jar);

                jar.finish();

                final String jarsPath = jars.getAbsolutePath();
                Log.finest(Res.fmt(Res.FILE_CREATED, "updater", jarsPath), CoreConstants.CRLF);
            } catch (final IOException ex) {
                Log.warning(Res.get(Res.JAR_WRITE_FAILED), ex);
                success = false;
            }
        }

        return success;
    }

    /**
     * Recursively adds all files in a directory to a jar output stream. All "package-info.class" files,
     * "AllJUnitTests.class" files, or directories named "test" will be skipped.
     *
     * @param rootDir the root directory of the file tree
     * @param dir     the directory
     * @param jar     the jar output stream to which to add entries
     * @throws IOException if an exception occurs while writing
     */
    private void addFiles(final File rootDir, final File dir, final JarOutputStream jar) throws IOException {

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
                if ("package-info.class".equals(name) || name.endsWith(".xlsx")) {
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
                    if (!this.jarDirs.contains(name)) {
                        jar.putNextEntry(new ZipEntry(name + CoreConstants.SLASH));
                        this.jarDirs.add(name);
                    }
                    addFiles(rootDir, file, jar);
                } else {
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
        htm.addln("Application-Name: Colorado State University Math Operations System");
        htm.addln("Permissions: all-permissions");
        htm.addln("Codebase: *");
        htm.addln("Application-Library-Allowable-Codebase: *");
        htm.addln("Caller-Allowable-Codebase: *");
        htm.addln("Main-Class: dev.mathops.app.teststation.TestStationApp");

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

        new TestingAppBuilder().build();
        final String msg = Res.get(Res.FINISHED);
        Log.finest(msg, CoreConstants.CRLF);
    }
}
