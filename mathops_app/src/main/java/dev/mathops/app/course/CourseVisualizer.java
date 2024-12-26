package dev.mathops.app.course;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.io.File;

/**
 * An application that presents a course directory structure in a UI to facilitate easier comprehension of course
 * structure and materials.
 */
public final class CourseVisualizer implements Runnable {

    /** The course directory being visualized. */
    private final File courseDir;

    /**
     * Constructs a new {@code CourseVisualizer}.
     *
     * @param defaultDirectories a list of directories for which to test - if any of these exist, the first one found
     *                           (in the order supplied) is used as the default directory when choosing the course to
     *                           visualize (users can still choose a different directory).
     */
    private CourseVisualizer(final File... defaultDirectories) {

        this.courseDir = chooseCourseDirectory(defaultDirectories);
    }

    /**
     * Asks the user to select the directory to visualize.
     *
     * @param defaultDirectories a list of directories for which to test - if any of these exist, the first one found
     *                           (in the order supplied) is used as the default directory when choosing the course to
     *                           visualize (users can still choose a different directory).
     * @return the selected directory; {@code null} if the user chose "Cancel"
     */
    private static File chooseCourseDirectory(final File... defaultDirectories) {

        File result = null;

        File dir = null;

        if (defaultDirectories != null) {
            for (final File testDir : defaultDirectories) {
                if (testDir.exists() && testDir.isDirectory()) {
                    dir = testDir;
                    break;
                }
            }
        }
        if (dir == null) {
            final String home = System.getProperty("user.home");
            dir = new File(home);
        }

        final JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle("Select course directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            result = chooser.getSelectedFile();
            final String path = result.getAbsolutePath();
            Log.info("Selected path: ", path);
        }

        return result;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        if (this.courseDir != null) {
            final MainWindow window = new MainWindow(this.courseDir);
            window.init();

            UIUtilities.packAndCenter(window);
            window.setVisible(true);
        }
    }

    /**
     * Launches the app.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final File dir1 = new File("D:/OneDrive - Colostate/Precalculus");
        final File dir2 = new File("F:/OneDrive - Colostate/Precalculus");

        FlatLightLaf.setup();
//        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new CourseVisualizer(dir1, dir2));
    }
}
