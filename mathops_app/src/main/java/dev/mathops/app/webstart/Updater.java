package dev.mathops.app.webstart;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.UIUtilities;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * The updater application is executed when the Launcher application needs to be updated (since it may not be able to
 * update itself if the OS locks its JAR file). This application performs only the following functions:
 *
 * <ul>
 * <li>Check the "./update/launch" subdirectory for a valid "launch.xml" and "launch.jar" set, and
 * verify that the jar's has matches the xml file.
 * <li>If valid, an update is performed by:
 * <ul>
 * <li>if the ./bak folder does not exist, it is created
 * <li>if the launch folder does not exist under ./bak, it is created
 * <li>if ./bak/launch/launch.xml.bak2 exists, it is deleted
 * <li>if ./bak/launch/launch.jar.bak2 exists, it is deleted
 * <li>if ./bak/launch/launch.xml.bak1 and bak/launch/launch.jar.bak1 both exist, they are renamed
 * to ./bak/launch/launch.xml.bak2 and bak/launch/launch.jar.bak2, respectively; if only one exists,
 * it is deleted
 * <li>./launch/launch.xml is copied to ./bak/launch/launch.xml.bak1
 * <li>./launch/launch.jar is copied to ./bak/launch/launch.jar.bak1
 * </ul>
 * </ul>
 * When the update is finished, this application terminates. It is assumed that an operating system
 * script will then re-execute the launcher
 *
 * <p>
 * This program must be launched with the working directory set to the top-level application
 * installation directory. It will expect that the 'launch' and 'update' subdirectories exist under
 * that directory, and it must be running from the updater.jar file in the 'launch' directory.
 *
 * <p>
 * To start the program on a Unix platform:
 *
 * <pre>
 * cd [Application-Install-Dir]
 * java -cp launch/updater.jar edu.colostate.math.app.webstart.Updater
 * </pre>
 */
final class Updater implements Runnable {

    /** Updater version. */
    private static final String VERSION = "1.2.005";

    /** Name of the "launch" subdirectory. */
    private static final String LAUNCH = "launch";

    /** Name of the "update" subdirectory. */
    private static final String UPDATE = "update";

    /** Name of the "bak" subdirectory. */
    private static final String BAK = "bak";

    /** Name of the "launch.xml" file. */
    private static final String LAUNCH_XML = "launch.xml";

    /** Name of the "launch.jar" file. */
    public static final String LAUNCH_JAR = "launch.jar";

    /** Name of the "app.xml" file. */
    public static final String APP_XML = "app.xml";

    /** Background color. */
    private static final Color BACKGROUND = new Color(30, 77, 43);

    /** Background color. */
    private static final Color BORDER = new Color(200, 195, 114);

    /** Background color. */
    private static final Color FIELD = new Color(237, 232, 99);

    /** The main application directory (the home directory on execution). */
    private final File appDir;

    /** The launch subdirectory of the application directory. */
    private final File launchDir;

    /** The update/launch subdirectory of the application directory. */
    private final File updateLaunchDir;

    /** The backup subdirectory of the application directory. */
    private final File bakDir;

    /** The backup/launch subdirectory of the application directory. */
    private final File bakLaunchDir;

    /** A log file. */
    private final File logFile;

    /** The default font. */
    private final Font font;

    /** The splash screen frame. */
    private JFrame splash;

    /** Label that will show installed version. */
    private JLabel installedVersion;

    /** Label that will show available date. */
    private JLabel installedDate;

    /** Label that will show available version. */
    private JLabel availableVersion;

    /** Label that will show available date. */
    private JLabel availableDate;

    /** Label that will show status. */
    private JLabel status;

    /**
     * Private constructor to prevent instantiation.
     */
    private Updater() {

        this.appDir = new File(System.getProperty("user.dir"));

        this.launchDir = new File(this.appDir, LAUNCH);
        if (!this.launchDir.exists() && !this.launchDir.mkdirs()) {
            Log.warning("Unable to create directory: ", this.launchDir.getAbsolutePath());
        }

        final File updateDir = new File(this.appDir, UPDATE);
        if (!updateDir.exists() && !updateDir.mkdirs()) {
            Log.warning("Unable to create directory: ", updateDir.getAbsolutePath());
        }

        this.updateLaunchDir = new File(updateDir, LAUNCH);
        if (!this.updateLaunchDir.exists() && !this.updateLaunchDir.mkdirs()) {
            Log.warning("Unable to create directory: ", this.updateLaunchDir.getAbsolutePath());
        }

        this.bakDir = new File(this.appDir, BAK);
        if (!this.bakDir.exists() && !this.bakDir.mkdirs()) {
            Log.warning("Unable to create directory: ", this.bakDir.getAbsolutePath());
        }

        this.bakLaunchDir = new File(this.bakDir, LAUNCH);
        if (!this.bakLaunchDir.exists() && !this.bakLaunchDir.mkdirs()) {
            Log.warning("Unable to create directory: ", this.bakLaunchDir.getAbsolutePath());
        }

        this.logFile = new File(this.launchDir, "updater.log");
        this.logFile.delete();

        FileUtils.log(this.logFile, "Updater ", VERSION, " starting");
        FileUtils.log(this.logFile, "Application Directory: ", this.appDir.getAbsolutePath());
        FileUtils.log(this.logFile, CoreConstants.EMPTY);

        Font theFont;
        try (final InputStream in = FileLoader.openInputStream(Updater.class, //
                "ProximaNova-Reg-webfont.ttf", false)) {
            theFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (final IOException | FontFormatException ex) {
            FileUtils.log(this.logFile, Res.get(Res.CANT_LOAD_FONT), ex);
            theFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }

        this.font = theFont;
    }

    /**
     * Constructs the display frame in the AWT event thread.
     */
    @Override
    public void run() {

        this.splash = new JFrame();
        this.splash.setUndecorated(true);

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(10, 10, 10, 10, BORDER),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        this.splash.setContentPane(content);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 180, 5));
        topFlow.setBackground(BACKGROUND);

        final JLabel top = new JLabel(Res.get(Res.UPDATER_TITLE));
        top.setForeground(Color.white);
        top.setHorizontalAlignment(SwingConstants.CENTER);
        top.setFont(this.font.deriveFont(30.0f));
        topFlow.add(top);
        content.add(topFlow, BorderLayout.NORTH);

        final JSeparator sep = new JSeparator();
        sep.setForeground(Color.white);
        content.add(sep, BorderLayout.NORTH);

        final Font boldFont = this.font.deriveFont(Font.BOLD, 18.0f);
        final Font plainFont = this.font.deriveFont(18.0f);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));
        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));
        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));

        flow1.setBackground(BACKGROUND);
        flow2.setBackground(BACKGROUND);
        flow3.setBackground(BACKGROUND);

        final JLabel lbl1 = new JLabel("Installed Version:");
        lbl1.setFont(boldFont);
        lbl1.setForeground(Color.WHITE);
        final JLabel lbl2 = new JLabel("Available Version:");
        lbl2.setFont(boldFont);
        lbl2.setForeground(Color.WHITE);
        final JLabel lbl3 = new JLabel("Update Status:");
        lbl3.setFont(boldFont);
        lbl3.setForeground(Color.WHITE);

        final Dimension pref1 = lbl1.getPreferredSize();
        final Dimension pref2 = lbl2.getPreferredSize();
        final Dimension pref3 = lbl3.getPreferredSize();
        final int maxW = Math.max(pref1.width, Math.max(pref2.width, pref3.width));
        final int maxH = Math.max(pref1.height, Math.max(pref2.height, pref3.height));
        final Dimension newPref = new Dimension(maxW, maxH);

        lbl1.setPreferredSize(newPref);
        lbl1.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl2.setPreferredSize(newPref);
        lbl2.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl3.setPreferredSize(newPref);
        lbl3.setHorizontalAlignment(SwingConstants.RIGHT);

        flow1.add(lbl1);
        flow2.add(lbl2);
        flow3.add(lbl3);
        content.add(flow1, BorderLayout.NORTH);
        content.add(flow2, BorderLayout.NORTH);
        content.add(flow3, BorderLayout.NORTH);

        this.installedVersion = new JLabel(CoreConstants.SPC);
        this.installedVersion.setFont(plainFont);
        this.installedVersion.setForeground(FIELD);

        this.installedDate = new JLabel(CoreConstants.SPC);
        this.installedDate.setFont(plainFont);
        this.installedDate.setForeground(FIELD);

        this.availableVersion = new JLabel(CoreConstants.SPC);
        this.availableVersion.setFont(plainFont);
        this.availableVersion.setForeground(FIELD);

        this.availableDate = new JLabel(CoreConstants.SPC);
        this.availableDate.setFont(plainFont);
        this.availableDate.setForeground(FIELD);

        this.status = new JLabel("Working...");
        this.status.setFont(plainFont);
        this.status.setForeground(FIELD);

        flow1.add(this.installedVersion);
        flow1.add(this.installedDate);
        flow2.add(this.availableVersion);
        flow2.add(this.availableDate);
        flow3.add(this.status);

        UIUtilities.packAndCenter(this.splash);
    }

    /**
     * Verifies that the update files exist, the XML file can be loaded, and that the JAR file's hash matches that given
     * in the XML file.
     *
     * @return true if the update is valid
     */
    private boolean doUpdate() {

        boolean valid = false;

        // Load the old an new app XML files and check that the release date is more recent in the
        // update directory

        if (this.launchDir.exists() && this.launchDir.isDirectory()) {

            final File updateLaunchXml = new File(this.updateLaunchDir, LAUNCH_XML);

            if (updateLaunchXml.exists()) {
                final AppDescriptor updateLaunchApp = AppDescriptor.parse(updateLaunchXml);

                if (updateLaunchApp == null) {
                    this.status.setText(Res.get(Res.UPDATE_LAUNCH_XML_BAD));
                    FileUtils.log(this.logFile, Res.get(Res.UPDATE_LAUNCH_XML_BAD));
                } else {
                    this.availableVersion.setText(updateLaunchApp.version);
                    this.availableDate
                            .setText(TemporalUtils.FMT_MDY.format(updateLaunchApp.releaseDate));

                    final File launchXml = new File(this.launchDir, LAUNCH_XML);

                    if (launchXml.exists()) {
                        final AppDescriptor launchApp = AppDescriptor.parse(launchXml);

                        if (launchApp == null) {
                            // launch.xml bad - try to copy it over
                            valid = FileUpdater.updateApp(updateLaunchApp, this.updateLaunchDir,
                                    this.launchDir, this.logFile, LAUNCH_XML);
                            if (valid) {
                                this.status.setText("Finished");
                            } else {
                                this.status.setText("Update failed");
                            }
                        } else {
                            this.installedVersion.setText(updateLaunchApp.version);
                            this.installedDate
                                    .setText(TemporalUtils.FMT_MDY.format(updateLaunchApp.releaseDate));

                            if (updateLaunchApp.releaseDate.isAfter(launchApp.releaseDate)) {
                                // Update is more recent - copy into place
                                valid = FileUpdater.updateApp(updateLaunchApp, this.updateLaunchDir,
                                        this.launchDir, this.logFile, LAUNCH_XML);
                                if (valid) {
                                    this.status.setText("Finished");
                                } else {
                                    this.status.setText("Update failed");
                                }
                            } else {
                                this.status.setText(Res.get(Res.NO_LAUNCH_UPD_NEEDED));
                                FileUtils.log(this.logFile, Res.get(Res.NO_LAUNCH_UPD_NEEDED));
                            }
                        }
                    } else {
                        // launch.xml missing - try to copy it over
                        valid = FileUpdater.updateApp(updateLaunchApp, this.updateLaunchDir,
                                this.launchDir, this.logFile, LAUNCH_XML);
                        if (valid) {
                            this.status.setText("Finished");
                        } else {
                            this.status.setText("Update failed");
                        }
                    }
                }
            } else {
                this.status.setText(Res.get(Res.UPDATE_LAUNCH_XML_NOEXIST));
                FileUtils.log(this.logFile, Res.get(Res.UPDATE_LAUNCH_XML_NOEXIST));
            }
        } else {
            this.status.setText(Res.get(Res.LAUNCH_DIR_NOEXIST));
            FileUtils.log(this.logFile, Res.get(Res.LAUNCH_DIR_NOEXIST));
        }

        return valid;
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

        final Updater updater = new Updater();

        try {
            SwingUtilities.invokeAndWait(updater);
            Thread.sleep(200L);

            if (!updater.doUpdate()) {
                // If there are no updates or the update failed, that means this program was
                // launched in error, and it will probably happen again and again, so pause a little
                // to prevent fast spin
                Thread.sleep(4000L);
            }
        } catch (final InvocationTargetException | InterruptedException ex) {
            Log.warning(Res.get(Res.UPDATER_FAILED), ex);
        }

        updater.close();
        Log.info(Res.get(Res.UPDATER_EXIT));
    }
}
