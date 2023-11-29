package dev.mathops.app.deploy;

import dev.mathops.app.AppFileLoader;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.installation.Installation;
import dev.mathops.core.installation.Installations;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.HexEncoder;

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
 *
 * <p>
 * This program assumes all projects are managed by GIT, and are stored in a directory tree of this form:
 *
 * <pre>
 * {dir}/git
 *   /{project-name}
 *     /{project-name}
 *       /bin
 *       /lib
 *       /src
 * </pre> where {project-name} is one of the following:
 * <ul>
 * <li>bls01core
 * <li>bls21db
 * <li>bls
 * </ul>
 */
final class TestingAppBuilder {

    /** The version number. */
    private static final String VERSION = "1.2.010";

    /** The core project directory name. */
    private static final String CORE_DIR = "bls01core";

    /** The db project directory name. */
    private static final String DB_DIR = "bls21db";

    /** The main project directory name. */
    private static final String MAIN_DIR = "bls";

    /** Working deploy directory. */
    private static final String DEPLOY_DIR = "bls99deploy";

    /** The bin directory under each project directory. */
    private static final String BIN_DIR = "bin";

    /** The lib directory under each project directory. */
    private static final String LIB_DIR = "lib";

    /** Directory where GIT projects are stored. */
    private File gitDir;

    /** The target directory to which to copy generated files. */
    private final File targetDir;

    /** The set of package names in the JAR file. */
    private final Set<String> jarDirs;

    /**
     * Constructs a new {@code AdminJarBuilder}.
     */
    private TestingAppBuilder() {

        final String userHome = System.getProperty("user.home");
        final File userDir = new File(userHome);

        this.gitDir = new File(new File(userDir, "dev"), "git");
        if (!this.gitDir.exists()) {
            this.gitDir = new File(userDir, "git");
        }

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

                File deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
                if (!deployDir.exists()) {
                    deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
                }
                final File deployLib = new File(deployDir, LIB_DIR);

                final File bls8Jar = new File(deployLib, "bls8.jar");
                final File jwabbitJar = new File(deployLib, "jwabbit.jar");
                final File appXml = new File(deployLib, "app.xml");

                final File launchJar = new File(deployLib, "launch.jar");
                final File launchXml = new File(deployLib, "launch.xml");

                final File updaterJar = new File(deployLib, "updater.jar");
                final File updaterXml = new File(deployLib, "updater.xml");

                createXmlDescriptor(sha256, appXml, "testing",
                        "dev.mathops.app.teststation.TestStationApp", bls8Jar, jwabbitJar);

                createXmlDescriptor(sha256, launchXml, "launch",
                        "dev.mathops.app.webstart.Launch", launchJar);

                createXmlDescriptor(sha256, updaterXml, "updater",
                        "dev.mathops.app.webstart.Updater", updaterJar);

                // TODO: Copy all to /opt/public/app/testing

                copyFile(bls8Jar, this.targetDir);
                copyFile(jwabbitJar, this.targetDir);
                copyFile(appXml, this.targetDir);

                final File targetLaunch = new File(this.targetDir, "launch");
                if (!targetLaunch.exists()) {
                    if (!targetLaunch.mkdirs()) {
                        Log.warning("failed to create launch directory");
                    }
                }

                copyFile(launchJar, targetLaunch);
                copyFile(launchXml, targetLaunch);
                copyFile(updaterJar, targetLaunch);
                copyFile(updaterXml, targetLaunch);
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

        final byte[] bytes = AppFileLoader.loadFileAsBytes(source, true);
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

        final byte[] bytes = AppFileLoader.loadFileAsBytes(file, true);
        if (bytes != null) {
            final String hex = HexEncoder.encodeLowercase(sha256.digest(bytes));
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

        File coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        if (!coreDir.exists()) {
            coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        }

        File dbDir = new File(new File(this.gitDir, DB_DIR), DB_DIR);
        if (!dbDir.exists()) {
            dbDir = new File(new File(this.gitDir, DB_DIR), DB_DIR);
        }

        File mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        if (!mainDir.exists()) {
            mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        }

        File deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        if (!deployDir.exists()) {
            deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        }

        final File coreBin = new File(coreDir, BIN_DIR);
        final File dbBin = new File(dbDir, BIN_DIR);
        final File mainBin = new File(mainDir, BIN_DIR);

        final File deployLib = new File(deployDir, LIB_DIR);

        boolean success = checkDirectoriesExist(coreBin, dbBin, mainBin, deployLib);

        if (success) {

            try (final FileOutputStream out = new FileOutputStream(new File(deployLib, "bls8.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                // Ready to add files to the JAR...
                Log.finest(Res.fmt(Res.ADDING_FILES, CORE_DIR), CoreConstants.CRLF);
                addFiles(coreBin, coreBin, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, DB_DIR), CoreConstants.CRLF);
                addFiles(dbBin, dbBin, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, MAIN_DIR), CoreConstants.CRLF);
                addFiles(mainBin, mainBin, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.JAR_DONE, "bls8"), CoreConstants.CRLF);

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

        File coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        if (!coreDir.exists()) {
            coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        }

        File mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        if (!mainDir.exists()) {
            mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        }

        File deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        if (!deployDir.exists()) {
            deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        }

        final File coreBin = new File(coreDir, BIN_DIR);
        final File mainBin = new File(mainDir, BIN_DIR);
        final File mainBinEdu = new File(mainBin, "edu");
        final File mainBinEduCol = new File(mainBinEdu, "colostate");
        final File mainBinEduColMath = new File(mainBinEduCol, "math");
        final File mainBinEduColMathApp = new File(mainBinEduColMath, "app");
        final File mainBinWebstart = new File(mainBinEduColMathApp, "webstart");

        final File deployLib = new File(deployDir, LIB_DIR);

        boolean success = checkDirectoriesExist(coreBin, mainBin, deployLib);

        if (success) {

            try (final FileOutputStream out = new FileOutputStream(new File(deployLib, "launch.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                // Ready to add files to the JAR...
                Log.finest(Res.fmt(Res.ADDING_FILES, CORE_DIR), CoreConstants.CRLF);
                addFiles(coreBin, coreBin, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, MAIN_DIR), CoreConstants.CRLF);
                addFiles(mainBin, mainBinWebstart, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.JAR_DONE, "launch"), CoreConstants.CRLF);

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

        File coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        if (!coreDir.exists()) {
            coreDir = new File(new File(this.gitDir, CORE_DIR), CORE_DIR);
        }

        File mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        if (!mainDir.exists()) {
            mainDir = new File(new File(this.gitDir, MAIN_DIR), MAIN_DIR);
        }

        File deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        if (!deployDir.exists()) {
            deployDir = new File(new File(this.gitDir, DEPLOY_DIR), DEPLOY_DIR);
        }

        final File coreBin = new File(coreDir, BIN_DIR);
        final File mainBin = new File(mainDir, BIN_DIR);
        final File mainBinEdu = new File(mainBin, "edu");
        final File mainBinEduCol = new File(mainBinEdu, "colostate");
        final File mainBinEduColMath = new File(mainBinEduCol, "math");
        final File mainBinEduColMathApp = new File(mainBinEduColMath, "app");
        final File mainBinWebstart = new File(mainBinEduColMathApp, "webstart");

        final File deployLib = new File(deployDir, LIB_DIR);

        boolean success = checkDirectoriesExist(coreBin, mainBin, deployLib);

        if (success) {

            try (final FileOutputStream out = new FileOutputStream(//
                    new File(deployLib, "updater.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                // Ready to add files to the JAR...
                Log.finest(Res.fmt(Res.ADDING_FILES, CORE_DIR), CoreConstants.CRLF);
                addFiles(coreBin, coreBin, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, MAIN_DIR), CoreConstants.CRLF);
                addFiles(mainBin, mainBinWebstart, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.JAR_DONE, "updater"), CoreConstants.CRLF);

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
    private void addFiles(final File rootDir, final File dir, final JarOutputStream jar)
            throws IOException {

        if (new File(dir, "bls_ignore.txt").exists()) {
            Log.info("Ignoring ", dir.getAbsolutePath());
        } else {
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
                if ("package-info.class".equals(name) || "test".equals(name) || name.endsWith(".xlsx")) {
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
                    final byte[] bytes = AppFileLoader.loadFileAsBytes(file, true);
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
        htm.addln("Application-Name: Colorado State University Precalculus System");
        htm.addln("Permissions: all-permissions");
        htm.addln("Codebase: https://*.colostate.edu");
        htm.addln("Application-Library-Allowable-Codebase: *");
        htm.addln("Caller-Allowable-Codebase: *");
        htm.addln("Created-By: AdminJarBuilder 1.0 (BLS)");
        htm.addln("Main-Class: edu.colostate.math.db.app.adm.AdminApp");

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
        Log.finest(Res.get(Res.FINISHED), CoreConstants.CRLF);
    }
}
