package dev.mathops.assessment.expression.editview;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A class that creates a frame with a demonstration window for expression editing.
 */
public class ExpressionEditingDemo implements Runnable {

    /**
     * Constructs a new {@code ExpressionEditingDemo}.
     */
    private ExpressionEditingDemo() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event dispatch thread.
     */
    public void run() {

        final JFrame frame = new JFrame("Expression Editing Demo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final ExpressionEditPanel editPanel = new ExpressionEditPanel();
        frame.setContentPane(editPanel);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Main method to run the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new ExpressionEditingDemo());
    }
}
