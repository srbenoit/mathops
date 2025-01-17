package dev.mathops.app.ui;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * A small application to exercise the DB field components.
 */
public class DbFieldExerciser implements Runnable {

    /**
     * Constructs a new {@code DbFieldExerciser}.
     */
    private DbFieldExerciser() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("DB Field Exerciser");

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setPreferredSize(new Dimension(600, 400));
        frame.setContentPane(content);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        final JLabel lbl1 = new JLabel("Integer Field 1:");
        flow1.add(lbl1);
        final DbIntegerField field1 = new DbIntegerField(ENullability.NULLS_ALLOWED, -999, 999);
        flow1.add(field1);
        content.add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        final JLabel lbl2 = new JLabel("Integer Field 2:");
        flow2.add(lbl2);
        final DbIntegerField field2 = new DbIntegerField(ENullability.NULLS_ALLOWED, 0, 9999);
        flow2.add(field2);
        content.add(flow2, StackedBorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new DbFieldExerciser());
    }
}
