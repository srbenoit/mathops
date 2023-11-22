package dev.mathops.web.deploy;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Constructs the servlet WAR file.
 *
 * <p>
 * This program assumes a single IDEA project structure of this form:
 *
 * <pre>
 * {user.home}/dev/IDEA/precalculus
 *    /src
 *    /lib
 *    /out/production/precalculus
 * </pre>
 */
final class ServletWarBuilder {

    /** The directory in which to store the generated jar file. */
    private static final String JARS_DIR = "jars";

    /** Directory where project is stored. */
    private final File projectDir;

    /**
     * Constructs a new {@code ServletWarBuilder}.
     */
    private ServletWarBuilder() {

        final File userDir = new File(System.getProperty("user.home"));
        final File dev = new File(userDir, "dev");
        final File idea = new File(dev, "IDEA");
        this.projectDir = new File(idea, "mathcontainers");
    }

    /**
     * Builds the WAR file.
     */
    private void build() {

        if (buildRootJar()) {
            buildRootWar();
        }
    }

    /**
     * Builds the ROOT.jar file.
     *
     * @return {@code true} if successful
     */
    private boolean buildRootJar() {

        final File core = new File(this.projectDir, "mathcontainers_core");
        final File coreRoot = new File(core, "target/classes");
        final File coreClasses = new File(coreRoot, "edu/colostate/math/containers/core");

        final File db = new File(this.projectDir, "mathcontainers_db");
        final File dbRoot = new File(db, "target/classes");
        final File dbClasses = new File(dbRoot, "edu/colostate/math/containers/db");

        final File dbapp = new File(this.projectDir, "mathcontainers_dbapp");
        final File dbappRoot = new File(dbapp, "target/classes");
        final File dbappClasses1 = new File(dbappRoot, "edu/colostate/math/containers/dbapp/batch");
        final File dbappClasses2 = new File(dbappRoot, "edu/colostate/math/containers/dbapp/report");

        final File font = new File(this.projectDir, "mathcontainers_font");
        final File fontRoot = new File(font, "target/classes");
        final File fontClasses = new File(fontRoot, "edu/colostate/math/containers/font");

        final File assessment = new File(this.projectDir, "mathcontainers_assessment");
        final File assessmentRoot = new File(assessment, "target/classes");
        final File assessmentClasses = new File(assessmentRoot, "edu/colostate/math/containers/assessment");

        final File session = new File(this.projectDir, "mathcontainers_session");
        final File sessionRoot = new File(session, "target/classes");
        final File sessionClasses = new File(sessionRoot, "edu/colostate/math/containers/session");

        final File web = new File(this.projectDir, "mathcontainers_web");
        final File webRoot = new File(web, "target/classes");
        final File webClasses = new File(webRoot, "edu/colostate/math/containers/web");

        final File jars = new File(this.projectDir, "jars");

        boolean success = checkDirectoriesExist(coreClasses, dbClasses, dbappClasses1, dbappClasses2, fontClasses,
                assessmentClasses, sessionClasses, webClasses, jars);

        if (success) {
            try (final FileOutputStream out = new FileOutputStream(new File(jars, "ROOT.jar"));
                 final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
                 final JarOutputStream jar = new JarOutputStream(bos)) {

                addManifest(jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, core), CoreConstants.CRLF);
                addFiles(coreRoot, coreClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, db), CoreConstants.CRLF);
                addFiles(dbRoot, dbClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, dbapp), CoreConstants.CRLF);
                addFiles(dbappRoot, dbappClasses1, jar);
                addFiles(dbappRoot, dbappClasses2, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, font), CoreConstants.CRLF);
                addFiles(fontRoot, fontClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, assessment), CoreConstants.CRLF);
                addFiles(assessmentRoot, assessmentClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, session), CoreConstants.CRLF);
                addFiles(sessionRoot, sessionClasses, jar);

                Log.finest(Res.fmt(Res.ADDING_FILES, web), CoreConstants.CRLF);
                addFiles(webRoot, webClasses, jar);

                jar.finish();
                Log.finest(Res.fmt(Res.JAR_DONE, "ROOT"), CoreConstants.CRLF);
            } catch (final IOException ex) {
                Log.warning(Res.get(Res.JAR_WRITE_FAILED), ex);
                success = false;
            }
        }

        return success;
    }

    /**
     * Builds the ROOT.war file.
     */
    private void buildRootWar() {

        final File web = new File(this.projectDir, "mathcontainers_web");
        final File webRoot = new File(web, "target/classes");

        final File deployDir = new File(this.projectDir, JARS_DIR);

        try (final FileOutputStream out = new FileOutputStream(new File(deployDir, "ROOT.war"));

             final BufferedOutputStream bos = new BufferedOutputStream(out, 128 << 10);
             final JarOutputStream war = new JarOutputStream(bos)) {

            addManifest(war);

            war.putNextEntry(new ZipEntry("WEB-INF/"));
            war.closeEntry();

            war.putNextEntry(new ZipEntry("WEB-INF/classes/"));
            war.closeEntry();

            war.putNextEntry(new ZipEntry("WEB-INF/lib/"));
            war.closeEntry();

            final File jarFile = new File(deployDir, "ROOT.jar");
            war.putNextEntry(new ZipEntry("WEB-INF/lib/ROOT.jar"));
            war.write(FileLoader.loadFileAsBytes(jarFile, true));
            war.closeEntry();

            final File fonts3File = new File(deployDir, "minfonts3.jar");
            war.putNextEntry(new ZipEntry("WEB-INF/lib/minfonts3.jar"));
            war.write(FileLoader.loadFileAsBytes(fonts3File, true));
            war.closeEntry();

            final File webFile = new File(webRoot, "edu/colostate/math/containers/web/deploy/web.xml");
            war.putNextEntry(new ZipEntry("WEB-INF/web.xml"));
            war.write(FileLoader.loadFileAsBytes(webFile, true));
            war.closeEntry();

            final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss 'on' EEEE, MMM d, yyyy", Locale.US);
            final String now = fmt.format(new Date());
            war.putNextEntry(new ZipEntry("WEB-INF/classes/date.txt"));
            war.write(now.getBytes(StandardCharsets.UTF_8));
            war.closeEntry();
            Log.finest(Res.fmt(Res.WAR_DONE, "ROOT"), CoreConstants.CRLF);

        } catch (final IOException ex) {
            Log.warning(Res.get(Res.WAR_WRITE_FAILED), ex);
        }
    }

    /**
     * Recursively adds all files in a directory to a jar output stream. All "module_info.class" and
     * "package-info.class" files will be skipped, and any directory that contains a "war_ignore.txt" file (and its
     * descendants) will be ignored.
     *
     * @param rootDir the root directory of the file tree
     * @param dir     the directory
     * @param jar     the jar output stream to which to add entries
     * @throws IOException if an exception occurs while writing
     */
    private void addFiles(final File rootDir, final File dir, final JarOutputStream jar)
            throws IOException {

        if (!new File(dir, "war_ignore.txt").exists()) {
            final int count = copyFiles(rootDir, dir, jar);

            if (count > 0) {
                // Build a log message with package name and number of files added
                String pkgName = dir.getName();

                File temp = dir.getParentFile();
                final StringBuilder builder = new StringBuilder(100);

                if (temp == null) {
                    Log.warning("Null parent: dir=", dir.getAbsolutePath(), " root=", rootDir.getAbsolutePath());
                } else {
                    while (!temp.equals(rootDir)) {
                        builder.append(temp.getName()).append('.').append(pkgName);
                        pkgName = builder.toString();
                        builder.setLength(0);
                        temp = temp.getParentFile();
                        if (temp == null) {
                            Log.warning("Null parent: dir=", dir.getAbsolutePath(), " root=", rootDir.getAbsolutePath());
                            break;
                        }
                    }
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
        htm.addln("Application-Name: Colorado State University Precalculus System");
        htm.addln("Permissions: all-permissions");
        htm.addln("Codebase: https://*.colostate.edu");
        htm.addln("Application-Library-Allowable-Codebase: *");
        htm.addln("Caller-Allowable-Codebase: *");
        htm.addln("Created-By: ServletWarBuilder 2.00");

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

        new ServletWarBuilder().build();
        Log.finest(Res.get(Res.FINISHED), CoreConstants.CRLF);
    }
}
