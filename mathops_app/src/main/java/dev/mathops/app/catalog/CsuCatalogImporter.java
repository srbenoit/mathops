package dev.mathops.app.catalog;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * An application to crawl the catalog website to import course and program information.
 */
final class CsuCatalogImporter implements Runnable {

    /**
     * Constructs a new {@code CsuCatalogImporter}.
     */
    private CsuCatalogImporter() {

        // No action
        CourseImporter.importCourses();
    }

    /**
     * Constructs the UI on the AWT event thread.
     */
    @Override
    public void run() {

        final JFrame frame = new JFrame("CSU Catalog Importer");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(1024, 768));
        frame.setContentPane(content);

        frame.pack();
        final Dimension size = frame.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        frame.setVisible(true);
    }

    /**
     * Launches the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new CsuCatalogImporter());
    }
}
