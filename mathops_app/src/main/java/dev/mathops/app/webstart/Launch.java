package dev.mathops.app.webstart;

import dev.mathops.commons.HexEncoder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A launcher application that runs from the application installation directory.
 *
 * <p>
 * This program must be launched with the working directory set to the top-level application installation directory. It
 * will expect that the 'launch', and 'update' subdirectories exist under that directory, and it must be running from
 * the JAR file in the 'launch' directory.
 *
 * <p>
 * This program does a number of things when started:
 * <ul>
 * <li>Downloads the latest XML file descriptors from the server
 * <li>Compares those descriptors to the latest downloaded updates
 * <li>For any updates indicated:
 * <ul>
 * <li>Downloads the updated JAR files to the "downloads" folder
 * <li>Verifies the hash on the downloaded file matches that in the XML descriptor
 * <li>If valid, copies the downloaded XML/JAR file to the "updates" folder
 * </ul>
 * <li>Checks the updates folder for updated versions
 * <li>If the updater has been updated, copy that update into place
 * <li>If the application has been updated, copy that update into place
 * <li>If the Launcher has been updated, execute the Updater; otherwise, execute the application
 * </ul>
 */
final class Launch implements Runnable {

    /** Launcher version. */
    private static final String VERSION = "1.2.018";

    /** Name of the "launch" subdirectory. */
    private static final String LAUNCH = "launch";

    /** Name of the "update" subdirectory. */
    private static final String UPDATE = "update";

    /** Name of the "download" subdirectory. */
    private static final String DOWNLOAD = "download";

    /** Name of the "launch.xml" file. */
    private static final String LAUNCH_XML = "launch.xml";

    /** Name of the "updater.xml" file. */
    private static final String UPDATER_XML = "updater.xml";

    /** Name of the "app.xml" file. */
    private static final String APP_XML = "app.xml";

    /** Root of URL from which to download files. */
    private static final String ROOT = "https://testing.math.colostate.edu/www/apps/";

    /** The main application directory (the home directory on execution). */
    private final File appDir;

    /** The launch subdirectory of the application directory. */
    private final File launchDir;

    /** The update subdirectory of the application directory. */
    private final File updateDir;

    /** The subdirectory of the application directory for downloads during validation. */
    private final File downloadDir;

    /** A log file. */
    private final File logFile;

    /** The main application descriptor. */
    private AppDescriptor app;

    /** The updater descriptor. */
    private AppDescriptor updater;

    /** The launcher descriptor. */
    private final AppDescriptor launch;

    /** The default font. */
    private final Font font;

    /** The splash screen frame. */
    private JFrame splash;

    /**
     * Private constructor to prevent instantiation.
     */
    private Launch() {

        this.appDir = new File(System.getProperty("user.dir"));

        this.launchDir = new File(this.appDir, LAUNCH);
        this.updateDir = new File(this.appDir, UPDATE);
        this.downloadDir = new File(this.appDir, DOWNLOAD);

        this.launchDir.mkdirs();
        this.updateDir.mkdirs();
        this.downloadDir.mkdirs();

        this.logFile = new File(this.launchDir, "launch.log");
        final File logBak1 = new File(this.launchDir, "launch1.log");
        final File logBak2 = new File(this.launchDir, "launch2.log");
        final File logBak3 = new File(this.launchDir, "launch3.log");
        if (logBak3.exists()) {
            logBak3.delete();
        }
        if (logBak2.exists()) {
            logBak2.renameTo(logBak3);
        }
        if (logBak1.exists()) {
            logBak1.renameTo(logBak2);
        }
        if (this.logFile.exists()) {
            this.logFile.renameTo(logBak1);
        }

        FileUtils.log(this.logFile, "Launcher ", VERSION, " starting");
        FileUtils.log(this.logFile, "Application Directory: ", this.appDir.getAbsolutePath());

        this.app = AppDescriptor.parse(new File(this.appDir, APP_XML));
        this.launch = AppDescriptor.parse(new File(this.launchDir, LAUNCH_XML));
        this.updater = AppDescriptor.parse(new File(this.launchDir, UPDATER_XML));

        Font theFont;
        try (final InputStream in = FileLoader.openInputStream(Launch.class, "ProximaNova-Reg-webfont.ttf", false)) {
            theFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (final IOException | FontFormatException ex) {
            final String msg = Res.get(Res.CANT_LOAD_FONT);
            FileUtils.log(this.logFile, msg, ex);
            theFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }

        this.font = theFont;
    }

    /**
     * Constructs the splash screen in the AWT event thread.
     */
    @Override
    public void run() {

        this.splash = new JFrame();
        this.splash.setUndecorated(true);

        final JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 77, 43));
        content.setBorder(BorderFactory.createMatteBorder(6, 6, 6, 6, new Color(200, 195, 114)));
        content.setPreferredSize(new Dimension(400, 100));
        this.splash.setContentPane(content);

        final String title = Res.get(Res.LAUNCH_TITLE);
        final JLabel top = new JLabel(title);
        top.setForeground(Color.white);
        top.setHorizontalAlignment(SwingConstants.CENTER);
        top.setFont(this.font.deriveFont(30.0f));
        content.add(top, BorderLayout.NORTH);

        final Box box = new Box(BoxLayout.PAGE_AXIS);
        box.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        content.add(box, BorderLayout.CENTER);

        final JSeparator sep = new JSeparator();
        sep.setForeground(Color.white);
        box.add(sep);

        if (this.app == null) {
            // No application
            final JLabel error = new JLabel(Res.get(Res.APP_NOT_INSTALL));
            error.setForeground(new Color(200, 195, 114));
            error.setFont(this.font.deriveFont(16.0f));
            box.add(error);
        } else if (this.launch == null) {
            // We have an application, but no launcher
            final JLabel error = new JLabel(Res.get(Res.LAUNCH_NOT_INSTALL));
            error.setForeground(new Color(200, 195, 114));
            error.setFont(this.font.deriveFont(16.0f));
            box.add(error);
        } else {
            // We have an application and a launcher
            final JLabel titleLbl = new JLabel(this.app.name);
            titleLbl.setForeground(Color.white);
            titleLbl.setFont(this.font.deriveFont(20.0f));
            box.add(titleLbl);
        }

        final JLabel info;
        if (this.launch == null) {
            info = new JLabel("Version not available");
        } else {
            info = new JLabel("Version " + this.launch.version);
        }
        info.setForeground(Color.white);
        info.setFont(this.font.deriveFont(10.0f));
        box.add(info);

        this.splash.pack();
        final Dimension size = this.splash.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.splash.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        this.splash.setVisible(true);
    }

    /**
     * Execute the launch process.
     *
     * @return true if successful; false if not
     */
    private boolean isLaunched() {

        // Download the latest XML file descriptors from the server, compare release dates against
        // current versions, and if a new release is indicated, download all files referenced in
        // the XML descriptor to the "download" directory.
        downloadLatest();

        // Check the updates folder for updated versions and copies any found into place
        final boolean[] updaterUpdated = {false};
        final boolean ok = checkForUpdates(updaterUpdated);

        if (ok) {
            // If the Updater has been updated, execute the Updater; otherwise, execute the app
            if (updaterUpdated[0]) {
                launchUpdater();
            } else {
                launchApp();
            }
        }

        return ok;
    }

    /**
     * Queries the network for a later version of the pre-launcher, launcher, or main application, and downloads any it
     * finds to the updates directory.
     *
     * <p>
     * Application repositories are stored here:
     *
     * <pre>
     * https://testing.math.colostate.edu/apps/[appname]/
     *   app.xml
     *   [resources found in app.xml]
     *   launch/updater.xml
     *   launch/[resources found in updater.xml]
     *   launch/launch.xml
     *   launch/[resources found in launch.xml]
     * </pre>
     */
    private void downloadLatest() {

        FileUtils.log(this.logFile, "Downloading descriptors for latest versions...");

        final String path = ROOT + this.app.name + "/";
        final String path2 = ROOT + this.app.name + "/launch/";

        try {
            final URI updaterUri = new URI(path2 + "updater.xml");
            final URL updaterUrl = updaterUri.toURL();

            final URI launchUri = new URI(path2 + "launch.xml");
            final URL launchUrl = launchUri.toURL();

            final URI appUri = new URI(path + "app.xml");
            final URL appUrl = appUri.toURL();

            FileUtils.log(this.logFile, "    Updater: ", updaterUrl);
            FileUtils.log(this.logFile, "    Launch:  ", launchUri);
            FileUtils.log(this.logFile, "    App:     ", appUrl);

            final AppDescriptor newUpdater = downloadAppDescriptor(updaterUrl);
            final AppDescriptor newLaunch = downloadAppDescriptor(launchUrl);
            final AppDescriptor newApp = downloadAppDescriptor(appUrl);

            if (newUpdater != null && newUpdater.releaseDate.isAfter(this.updater.releaseDate)) {
                final File dst = new File(this.downloadDir, LAUNCH);
                final File updateDst = new File(this.updateDir, LAUNCH);
                if (!dst.exists()) {
                    dst.mkdirs();
                }
                if (!updateDst.exists()) {
                    updateDst.mkdirs();
                }
                FileUtils.log(this.logFile, "  ", UPDATER_XML, ": update available");
                downloadAllFiles(dst, updateDst, newUpdater, path2, UPDATER_XML);
                writeAppDescriptorXml(newUpdater, UPDATER_XML, updateDst);
            }

            if (newLaunch != null && newLaunch.releaseDate.isAfter(this.launch.releaseDate)) {
                final File dst = new File(this.downloadDir, LAUNCH);
                final File updateDst = new File(this.updateDir, LAUNCH);
                if (!dst.exists()) {
                    dst.mkdirs();
                }
                if (!updateDst.exists()) {
                    updateDst.mkdirs();
                }
                FileUtils.log(this.logFile, "  ", LAUNCH_XML, ": update available");
                downloadAllFiles(dst, updateDst, newLaunch, path2, LAUNCH_XML);
                writeAppDescriptorXml(newLaunch, LAUNCH_XML, updateDst);
            }

            if (newApp != null && newApp.releaseDate.isAfter(this.app.releaseDate)) {
                FileUtils.log(this.logFile, "  ", APP_XML, ": update available");
                downloadAllFiles(this.downloadDir, this.updateDir, newApp, path, APP_XML);
                writeAppDescriptorXml(newApp, APP_XML, this.updateDir);
            }
        } catch (final URISyntaxException ex) {
            FileUtils.log(this.logFile, "  Unable to construct descriptors URIs", ex);
        } catch (final IOException ex) {
            FileUtils.log(this.logFile, "  Unable to download descriptors to test for update", ex);
        }
    }

    /**
     * Attempts to download and parse an application descriptor from a URL.
     *
     * @param url the URL
     * @return the parsed descriptor; null if unable to parse
     */
    private AppDescriptor downloadAppDescriptor(final URL url) {

        AppDescriptor result = null;

        try (final InputStream in = (InputStream) url.getContent()) {
            result = AppDescriptor.parse(in);

            if (result == null) {
                FileUtils.log(this.logFile, "  Failed to parse application descriptor at ", url);
            } else {
                FileUtils.log(this.logFile, "  Downloaded descriptor '", result.name, "' version ", result.version);
            }
        } catch (final IOException ex) {
            FileUtils.log(this.logFile, "  Unable to download application descriptor", ex);
        }

        return result;
    }

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
     * @param downloadDst        the target download directory
     * @param updateDst          the directory to which to copy files once verified
     * @param descriptor         the application descriptor
     * @param path               the URL path under which files can be found on the server
     * @param descriptorFilename the filename of the descriptor file
     */
    private void downloadAllFiles(final File downloadDst, final File updateDst,
                                  final AppDescriptor descriptor, final String path, final String descriptorFilename) {

        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            boolean allValid = true;

            for (final FileDescriptor fileDescriptor : descriptor.getFiles()) {

                FileUtils.log(this.logFile, "  Attempting to download ", fileDescriptor.name);

                sha256.reset();
                try {
                    final URI uri = new URI(path + fileDescriptor.name);
                    final URL url = uri.toURL();

                    final File file = new File(downloadDst, fileDescriptor.name);
                    downloadFile(url, file);

                    if (file.length() == fileDescriptor.size) {
                        // Verify the downloaded file
                        final byte[] hash = fileDescriptor.getSHA256();
                        final byte[] computedDigest = computeSHA256(file, sha256);

                        if (Arrays.equals(computedDigest, hash)) {
                            FileUtils.log(this.logFile, "  Downloaded ", fileDescriptor.name, " to '",
                                    downloadDst.getName(),
                                    "' folder and verified.");
                        } else {
                            FileUtils.log(this.logFile, "  Downloaded file has SHA256 "
                                                        + HexEncoder.encodeUppercase(
                                    computedDigest) + ", XML descriptor says "
                                                        + HexEncoder.encodeUppercase(hash));
                            allValid = false;
                            break;
                        }
                    } else {
                        FileUtils.log(this.logFile, "  Downloaded file has size " + file.length()
                                                    + ", XML descriptor says " + fileDescriptor.size);
                        allValid = false;
                        break;
                    }
                } catch (final URISyntaxException ex) {
                    FileUtils.log(this.logFile, "  Exception constructing URI:", ex);
                    allValid = false;
                    break;
                } catch (final IOException ex) {
                    FileUtils.log(this.logFile, "  Exception downloading file:", ex);
                    allValid = false;
                    break;
                }
            }

            if (allValid) {
                FileUtils.log(this.logFile, "  All application files downloaded and verified.");

                // All the files now exist in the download directory with valid hashes - move them
                // into the update directory (we make no effort to clean out unused files from the
                // "update" directory)

                for (final FileDescriptor file : descriptor.getFiles()) {

                    FileUtils.log(this.logFile, "  Moving ", file.name, " to '", updateDst.getName(), "' directory.");

                    final File src = new File(downloadDst, file.name);
                    final File upd = new File(updateDst, file.name);

                    if (upd.exists()) {
                        upd.delete();
                    }

                    if (!src.renameTo(upd)) {
                        FileUtils.log(this.logFile, "  Move failed - trying copy");
                        allValid = FileUtils.copyFile(src, upd);

                        src.delete();
                        if (!allValid) {
                            break;
                        }
                    }
                }

                if (allValid) {
                    // Write the new app descriptor so update can be applied
                    writeAppDescriptorXml(descriptor, descriptorFilename, updateDst);
                } else {
                    // Clean the updates directory so they won't get applied
                    for (final FileDescriptor file : descriptor.getFiles()) {
                        final File upd = new File(updateDst, file.name);
                        upd.delete();
                    }

                    final File descr = new File(updateDst, descriptorFilename);
                    descr.delete();
                }
            } else {
                FileUtils.log(this.logFile, "  Unable to downloaded and verify application files - skipping update.");
            }
        } catch (final NoSuchAlgorithmException ex) {
            FileUtils.log(this.logFile, "  Unable to obtain SHA256 digest", ex);
        }
    }

    /**
     * Attempts to download the contents of a URL and store it to a file.
     *
     * @param url  the URL
     * @param file the file
     * @throws IOException if there is an error reading from the URl or writing to the file
     */
    private static void downloadFile(final URL url, final File file) throws IOException {

        if (file.exists()) {
            file.delete();
        }

        final byte[] buffer = new byte[64 * 1024];

        try (final InputStream in = (InputStream) url.getContent();
             final OutputStream out = new FileOutputStream(file)) {

            int numRead = in.read(buffer);
            while (numRead > 0) {
                out.write(buffer, 0, numRead);
                numRead = in.read(buffer);
            }
        }
    }

    /**
     * Attempts to compute the SHA256 hash of a file.
     *
     * @param f      the file
     * @param sha256 the message digest
     * @return the hash; null if unable to read file
     */
    private byte[] computeSHA256(final File f, final MessageDigest sha256) {

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
                FileUtils.log(this.logFile, "  Exception computing hash of '", f.getAbsolutePath(), "'", ex);
            }
        }

        return result;
    }

    /**
     * Attempts to serialize an application descriptor to XML and write it to a file.
     *
     * @param desc     the application descriptor
     * @param filename the filename to which to write
     * @param dst      the file to which to write
     * @return true if successful; false if not
     */
    private boolean writeAppDescriptorXml(final AppDescriptor desc, final String filename, final File dst) {

        boolean success = false;

        try (final FileWriter fw = new FileWriter(new File(dst, filename), StandardCharsets.UTF_8)) {
            fw.write(desc.toXml());
            success = true;
        } catch (final IOException ex) {
            FileUtils.log(this.logFile, "  Failed to write ", filename, ex);
        }

        return success;
    }

    /**
     * Checks for updates, installing any found.
     *
     * @param updateLauncher an array whose [0] entry is set to true if the launcher should be updated
     * @return true if update was successful
     */
    private boolean checkForUpdates(final boolean[] updateLauncher) {

        boolean ok = true;

        FileUtils.log(this.logFile, "Launcher checking for updates...");

        // See if updater needs to be updated
        final File launchUpd = new File(this.updateDir, LAUNCH);

        if (launchUpd.exists() && launchUpd.isDirectory()) {
            final File newUpdaterXml = new File(launchUpd, UPDATER_XML);
            if (newUpdaterXml.exists()) {
                FileUtils.log(this.logFile, "  Found ", UPDATER_XML);

                final AppDescriptor newUpdater = AppDescriptor.parse(newUpdaterXml);

                if (newUpdater == null) {
                    FileUtils.log(this.logFile, "  Unable to parse ", newUpdaterXml.getAbsolutePath());
                } else if (newUpdater.releaseDate.isAfter(this.updater.releaseDate)) {
                    FileUtils.log(this.logFile, "  Updater ready for update");
                    ok = FileUpdater.updateApp(newUpdater, launchUpd, this.launchDir, this.logFile, UPDATER_XML);
                    if (ok) {
                        this.updater = newUpdater;
                    }
                } else if (newUpdater.releaseDate.isBefore(this.updater.releaseDate)) {
                    FileUtils.log(this.logFile, "  Release date in update is earlier than current");
                }
            }

            final File newLaunchXml = new File(launchUpd, LAUNCH_XML);
            if (newLaunchXml.exists()) {
                FileUtils.log(this.logFile, "  Found ", LAUNCH_XML);

                final AppDescriptor newLaunch = AppDescriptor.parse(newLaunchXml);

                if (newLaunch == null) {
                    FileUtils.log(this.logFile, "  Unable to parse ", newLaunchXml.getAbsolutePath());
                } else if (newLaunch.releaseDate.isAfter(this.launch.releaseDate)) {
                    FileUtils.log(this.logFile, "  Launch ready for update");
                    updateLauncher[0] = true;
                } else if (newLaunch.releaseDate.isBefore(this.launch.releaseDate)) {
                    FileUtils.log(this.logFile, "  Release date in launch is earlier than current");
                }
            }
        } else {
            FileUtils.log(this.logFile, Res.get(Res.LAUNCH_UPD_DIR_NOEXIST));
        }

        if (ok) {
            // See if app needs to be updated
            final File newAppXml = new File(this.updateDir, APP_XML);
            if (newAppXml.exists()) {
                FileUtils.log(this.logFile, "  Found ", APP_XML);

                final AppDescriptor newApp = AppDescriptor.parse(newAppXml);

                if (newApp == null) {
                    FileUtils.log(this.logFile, "  Unable to parse ", newAppXml.getAbsolutePath());
                } else if (newApp.releaseDate.isAfter(this.app.releaseDate)) {
                    FileUtils.log(this.logFile, "  App ready for update");
                    ok = FileUpdater.updateApp(newApp, this.updateDir, this.appDir, this.logFile, APP_XML);
                    if (ok) {
                        this.app = newApp;
                    }
                } else if (newApp.releaseDate.isBefore(this.app.releaseDate)) {
                    FileUtils.log(this.logFile, "  Release date in app is earlier than current");
                }
            }
        }

        return ok;
    }

    /**
     * Starts the updater
     */
    private void launchUpdater() {

        FileUtils.log(this.logFile, "Executing the updater");

        final File javaExe = FileUtils.findJavaExe();

        final String sep = FileSystems.getDefault().getSeparator();
        final char colonChar = "\\".equals(sep) ? ';' : ':';

        if (javaExe == null) {
            FileUtils.log(this.logFile, Res.get(Res.CANT_START_APP));
        } else {
            final List<String> cmd = new ArrayList<>(5);

            cmd.add(javaExe.getAbsolutePath());
            cmd.add("-cp");

            final StringBuilder cp = new StringBuilder(100);
            boolean semi = false;
            for (final FileDescriptor fd : this.updater.getFiles()) {
                if (semi) {
                    cp.append(colonChar);
                }
                cp.append("launch/");
                cp.append(fd.name);
                semi = true;
            }
            cmd.add(cp.toString());

            cmd.add(this.updater.mainClass);

            final StringBuilder debug = new StringBuilder(50);
            for (final String s : cmd) {
                debug.append(' ');
                debug.append(s);
            }
            FileUtils.log(this.logFile, debug.toString());

            final ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(this.appDir);

            try {
                final Process proc = pb.start();
                final InputStream stdout = proc.getInputStream();

                final long timeout = System.currentTimeMillis() + 10000L;

                try (final BufferedReader reader =
                             new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8))) {

                    while (System.currentTimeMillis() < timeout) {
                        if (reader.ready()) {
                            final String line = reader.readLine();
                            FileUtils.log(this.logFile, "] " + line);
                        } else {
                            try {
                                Thread.sleep(50L);
                            } catch (final InterruptedException ex) {
                                FileUtils.log(this.logFile, "  Interrupted: " + ex.getMessage());
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            } catch (final Exception ex) {
                FileUtils.log(this.logFile, "  Unable to start updater", ex);
            }
        }

    }

    /**
     * Starts the 'launch' app.
     */
    private void launchApp() {

        FileUtils.log(this.logFile, Res.get(Res.EXEC_APP));

        final File javaExe = FileUtils.findJavaExe();

        final String sep = FileSystems.getDefault().getSeparator();
        final char colonChar = "\\".equals(sep) ? ';' : ':';

        if (javaExe == null) {
            FileUtils.log(this.logFile, Res.get(Res.CANT_START_APP));
        } else {
            final List<String> cmd = new ArrayList<>(5);

            cmd.add(javaExe.getAbsolutePath());
            cmd.add("-cp");

            final StringBuilder cp = new StringBuilder(100);
            boolean semi = false;
            for (final FileDescriptor fd : this.app.getFiles()) {
                if (semi) {
                    cp.append(colonChar);
                }
                cp.append(fd.name);
                semi = true;
            }
            cmd.add(cp.toString());

            cmd.add(this.app.mainClass);

            final StringBuilder debug = new StringBuilder(50);
            for (final String s : cmd) {
                debug.append(' ');
                debug.append(s);
            }
            FileUtils.log(this.logFile, debug.toString());

            final ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(this.appDir);

            try {
                final Process proc = pb.start();
                final InputStream stdout = proc.getInputStream();

                try (final BufferedReader reader =
                             new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8))) {

                    while (proc.isAlive()) {
                        if (reader.ready()) {
                            final String line = reader.readLine();
                            FileUtils.log(this.logFile, "] " + line);
                        } else {
                            try {
                                Thread.sleep(50L);
                            } catch (final InterruptedException ex) {
                                FileUtils.log(this.logFile, "  Interrupted: " + ex.getMessage());
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            } catch (final IOException ex) {
                FileUtils.log(this.logFile, "  ", Res.get(Res.CANT_START_APP), ex);
            }
        }
    }

    /**
     * Closes the splash popup.
     */
    private void close() {

        if (this.splash != null) {
            this.splash.setVisible(false);
            this.splash.dispose();
            this.splash = null;
        }
    }

    /**
     * Launch the launcher.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final Launch launch = new Launch();

        try {
            SwingUtilities.invokeAndWait(launch);
            Thread.sleep(200L);

            if (!launch.isLaunched()) {
                // In case the app exits immediately, pause to prevent a fast spin
                Thread.sleep(1800L);
            }
        } catch (final InvocationTargetException | InterruptedException ex) {
            Log.warning(Res.get(Res.LAUNCH_FAILED), ex);
        }

        launch.close();

        Log.info(Res.get(Res.LAUNCH_EXIT));
    }
}
