package dev.mathops.app.assessment.problemauthor;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.core.EPath;
import dev.mathops.core.PathList;

import javax.swing.JFileChooser;
import java.awt.EventQueue;
import java.io.File;

/**
 * The main application.
 */
final class ProblemAuthor implements Runnable {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ProblemAuthor() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final File src1 = PathList.getInstance().get(EPath.SOURCE_1_PATH);
        final File src2 = PathList.getInstance().get(EPath.SOURCE_2_PATH);
        final File src3 = PathList.getInstance().get(EPath.SOURCE_3_PATH);

        final File src1Inst = src1 == null ? null : new File(src1, "instruction");
        final File src2Inst = src2 == null ? null : new File(src2, "instruction");
        final File src3Inst = src3 == null ? null : new File(src3, "instruction");

        final File dir;

        if (src1Inst != null && src1Inst.exists() && src1Inst.isDirectory()) {
            dir = src1Inst;
        } else if (src2Inst != null && src2Inst.exists() && src2Inst.isDirectory()) {
            dir = src2Inst;
        } else if (src3Inst != null && src3Inst.exists() && src3Inst.isDirectory()) {
            dir = src3Inst;
        } else if (src1 != null && src1.exists() && src1.isDirectory()) {
            dir = src1;
        } else if (src2 != null && src2.exists() && src2.isDirectory()) {
            dir = src2;
        } else if (src3 != null && src3.exists() && src3.isDirectory()) {
            dir = src3;
        } else {
            dir = new File(System.getProperty("user.home"));
        }

        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(dir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose directory containing problem library");

        final int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File selected = chooser.getSelectedFile();
            new MainWindow(selected).setVisible(true);
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        EventQueue.invokeLater(new ProblemAuthor());
    }
}
