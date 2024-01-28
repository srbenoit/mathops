package dev.mathops.app.deploy;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.installation.Installations;
import dev.mathops.commons.log.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Constructs the admin app JAR file.
 */
final class AdminJarBuilder {

    /** Directory where project is stored. */
    private final File projectDir;

    /**
     * Constructs a new {@code AdminJarBuilder}.
     */
    private AdminJarBuilder() {

        final File userDir = new File(System.getProperty("user.home"));
        final File dev = new File(userDir, "dev");
        final File idea = new File(dev, "IDEA");
        this.projectDir = new File(idea, "mathops");
    }

    /**
     * Builds the Jar file with a specified name and specified main class to run when the file is launched.
     *
     * @param mainClassName the main class name
     * @param targetFilename the target filename
     */
    private void build(final String mainClassName, final String targetFilename) {

        final File db = new File(this.projectDir, "mathops_db");
        final File dbRoot = new File(db, "build/classes/java/main");
        final File dbClasses = new File(dbRoot, "dev/mathops/db");

        final File dbjobs = new File(this.projectDir, "mathops_dbjobs");
        final File dbjobsRoot = new File(dbjobs, "build/classes/java/main");
        final File dbjobsClasses1 = new File(dbjobsRoot, "dev/mathops/dbjobs");

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

        final boolean success = checkDirectoriesExist(dbClasses, dbjobsClasses1, fontClasses, assessmentClasses,
                sessionClasses, appClasses, jars);

        if (success) {
            try (final FileOutputStream out = new FileOutputStream(new File(jars, targetFilename));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(mainClassName, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, db), CoreConstants.CRLF);
                addFiles(dbRoot, dbClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, dbjobs), CoreConstants.CRLF);
                addFiles(dbjobsRoot, dbjobsClasses1, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, font), CoreConstants.CRLF);
                addFiles(fontRoot, fontClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, assessment), CoreConstants.CRLF);
                addFiles(assessmentRoot, assessmentClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, session), CoreConstants.CRLF);
                addFiles(sessionRoot, sessionClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, app), CoreConstants.CRLF);
                addFiles(appRoot, appClasses, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.FILE_CREATED, targetFilename), CoreConstants.CRLF);

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

        if (!new File(dir, "admin_ignore.txt").exists()) {

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
     * @param mainClass the main class name
     * @param jar the {@code JarOutputStream} to which to add the manifest
     * @throws IOException if an exception occurs while writing
     */
    private static void addManifest(final String mainClass, final JarOutputStream jar) throws IOException {

        jar.putNextEntry(new ZipEntry("META-INF/"));
        jar.closeEntry();

        final HtmlBuilder htm = new HtmlBuilder(500);
        htm.addln("Manifest-Version: 1.0");
        htm.addln("Application-Name: Colorado State University Math Operations System");
        htm.addln("Permissions: all-permissions");
        htm.addln("Codebase: *");
        htm.addln("Application-Library-Allowable-Codebase: *");
        htm.addln("Caller-Allowable-Codebase: *");
        htm.addln("Main-Class: ", mainClass);

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

        new AdminJarBuilder().build("dev.mathops.app.adm.AdminApp", "ADMIN.jar");
        new AdminJarBuilder().build("dev.mathops.app.assessment.problemauthor.ProblemAuthor", "problem_author.jar");
        new AdminJarBuilder().build("dev.mathops.app.assessment.localtesting.LocalTestingApp", "exam_tester.jar");
        new AdminJarBuilder().build("dev.mathops.app.assessment.qualitycontrol.QualityControlScanner",
                "quality_control.jar");
        Log.finest(Res.get(Res.FINISHED), CoreConstants.CRLF);
    }
}
