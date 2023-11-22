package dev.mathops.app.assessment;

import dev.mathops.core.log.Log;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 * A GUI for the instructional tester, which draws a pair of labeled progress bars (one for directories, one for the
 * current file) and a list of error messages.
 */
final class InstructionTesterGUI implements ActionListener {

    /** Width of progress bars. */
    private static final int WIDTH = 800;

    /** The constructed JFrame. */
    private JFrame guiFrame;

    /** The label for the top constructed progress bar. */
    private JLabel guiProgressLabel1;

    /** The top constructed progress bar. */
    private JProgressBar guiProgressBar1;

    /** The label for the bottom constructed progress bar. */
    private JLabel guiProgressLabel2;

    /** The bottom constructed progress bar. */
    private JProgressBar guiProgressBar2;

    /** The constructed finished button. */
    private JButton guiFinished;

    /** The constructed cancel button. */
    private JButton guiCancel;

    /** The constructed errors list. */
    private JTextArea guiErrors;

    /** Flag indicating cancel button has been pressed. */
    private boolean cancelled;

    /**
     * Construct a new {@code InstructionTesterGUI}.
     */
    InstructionTesterGUI() {

        // No action
    }

    /**
     * Create and display the on-screen window.
     */
    void create() {

        final GUICreator creator = new GUICreator(this);

        if (SwingUtilities.isEventDispatchThread()) {
            creator.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(creator);

                // Get the created objects for reference
                this.guiFrame = creator.getFrame();
                this.guiProgressLabel1 = creator.getProgressLabel1();
                this.guiProgressBar1 = creator.getProgressBar1();
                this.guiProgressLabel2 = creator.getProgressLabel2();
                this.guiProgressBar2 = creator.getProgressBar2();
                this.guiErrors = creator.getErrors();
                this.guiCancel = creator.getCancel();
                this.guiFinished = creator.getFinished();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Destroy the on-screen window.
     */
    void destroy() {

        final Runnable destroyer = new GUIDestroyer(this.guiFrame);

        if (SwingUtilities.isEventDispatchThread()) {
            destroyer.run();
        } else {
            SwingUtilities.invokeLater(destroyer);
        }
    }

    /**
     * Cause the frame to be repainted.
     */
    void repaint() {

        this.guiFrame.repaint();
    }

    /**
     * Handle for action events from the cancel button.
     *
     * @param e the action event to process
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        // Called in the AWT event thread

        if ("Cancel".equals(cmd)) {
            this.cancelled = true;
            this.guiCancel.setEnabled(false);
        }

        if ("Finished".equals(cmd)) {
            this.cancelled = true;
            this.guiCancel.setEnabled(false);
        }
    }

    /**
     * Test whether the cancel button has been pressed.
     *
     * @return true if cancel has been pressed; false otherwise
     */
    boolean getCancelled() {

        return this.cancelled;
    }

    /**
     * Set the text label of the top progress bar.
     *
     * @param text the new text label value
     */
    void setTopProgressLabel(final String text) {

        final Runnable setter = new LabelSetter(this.guiProgressLabel1, text);

        if (SwingUtilities.isEventDispatchThread()) {
            setter.run();
        } else {
            SwingUtilities.invokeLater(setter);
        }
    }

    /**
     * Set the text label of the bottom progress bar.
     *
     * @param text the new text label value
     */
    void setBottomProgressLabel(final String text) {

        final Runnable setter = new LabelSetter(this.guiProgressLabel2, text);

        if (SwingUtilities.isEventDispatchThread()) {
            setter.run();
        } else {
            SwingUtilities.invokeLater(setter);
        }
    }

    /**
     * Set the progress bar value for the top progress bar.
     *
     * @param current the current progress value
     * @param max     the maximum possible progress value
     */
    void setTopProgressValue(final int current, final int max) {

        int pixels = current * WIDTH / max;

        if (pixels > WIDTH) {
            pixels = WIDTH;
        }

        final Runnable setter = new ProgressSetter(this.guiProgressBar1, pixels);

        if (SwingUtilities.isEventDispatchThread()) {
            setter.run();
        } else {
            SwingUtilities.invokeLater(setter);
        }
    }

    /**
     * Set the progress bar value for the top progress bar.
     *
     * @param current the current progress value
     * @param max     the maximum possible progress value
     */
    void setBottomProgressValue(final int current, final int max) {

        int pixels = current * WIDTH / max;

        if (pixels > WIDTH) {
            pixels = WIDTH;
        }

        final Runnable setter = new ProgressSetter(this.guiProgressBar2, pixels);

        if (SwingUtilities.isEventDispatchThread()) {
            setter.run();
        } else {
            SwingUtilities.invokeLater(setter);
        }
    }

    /**
     * Log an error message.
     *
     * @param error the error text to log
     */
    void logError(final String error) {

        Log.warning(error);

        final Runnable logger = new ErrorLogger(this.guiErrors, error);

        if (SwingUtilities.isEventDispatchThread()) {
            logger.run();
        } else {
            SwingUtilities.invokeLater(logger);
        }
    }

    /**
     * Set button states to indicate processing has finished.
     */
    void indicateFinished() {

        final Runnable finisher = new Finisher(this.guiFinished, this.guiCancel);

        if (SwingUtilities.isEventDispatchThread()) {
            finisher.run();
        } else {
            SwingUtilities.invokeLater(finisher);
        }
    }

    /**
     * Class to build the user interface in the AWT event dispatcher thread.
     */
    private static final class GUICreator implements Runnable {

        /** The listener to register with the cancel button. */
        private final ActionListener listener;

        /** The constructed JFrame. */
        private JFrame frame;

        /** The label for the top constructed progress bar. */
        private JLabel progressLabel1;

        /** The top constructed progress bar. */
        private JProgressBar progressBar1;

        /** The label for the bottom constructed progress bar. */
        private JLabel progressLabel2;

        /** The bottom constructed progress bar. */
        private JProgressBar progressBar2;

        /** The constructed errors list. */
        private JTextArea errors;

        /** The constructed cancel button. */
        private JButton cancel;

        /** The constructed finished button. */
        private JButton finished;

        /**
         * Construct a new {@code GUICreator}.
         *
         * @param theListener the listener to register with the cancel button
         */
        GUICreator(final ActionListener theListener) {

            this.listener = theListener;
        }

        /**
         * Construct the user interface.
         */
        @Override
        public void run() {

            // Runs in the AWT event dispatcher thread
            this.frame = new JFrame("Instructional Materials Tester");

            final JPanel content = new JPanel(new BorderLayout());
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.frame.setContentPane(content);
            this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Build the top pane, with progress bars and labels
            JPanel inner = new JPanel(new GridLayout(6, 1, 10, 10));
            this.progressLabel1 = new JLabel();
            inner.add(this.progressLabel1);
            this.progressBar1 = new JProgressBar();
            this.progressBar1.setMaximum(WIDTH);
            this.progressBar1.setPreferredSize(new Dimension(WIDTH, 10));
            inner.add(this.progressBar1);
            inner.add(new JLabel()); // Spacer
            this.progressLabel2 = new JLabel();
            inner.add(this.progressLabel2);
            this.progressBar2 = new JProgressBar();
            this.progressBar2.setMaximum(WIDTH);
            this.progressBar2.setPreferredSize(new Dimension(WIDTH, 10));
            inner.add(this.progressBar2);
            inner.add(new JLabel()); // Spacer
            content.add(inner, BorderLayout.PAGE_START);

            // Build the center pane with a scrolling errors list
            this.errors = new JTextArea();
            this.errors.setPreferredSize(new Dimension(WIDTH - 20, WIDTH / 2));
            final JScrollPane scroll = new JScrollPane(this.errors);
            scroll.setWheelScrollingEnabled(true);
            content.add(scroll, BorderLayout.CENTER);

            // Build the bottom pane with a cancel button
            inner = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 10));
            this.finished = new JButton("Finished");
            this.finished.addActionListener(this.listener);
            this.finished.setEnabled(false);
            inner.add(this.finished);
            this.cancel = new JButton("Cancel");
            this.cancel.addActionListener(this.listener);
            inner.add(this.cancel);
            content.add(inner, BorderLayout.PAGE_END);

            // Center the frame on the screen
            this.frame.pack();
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            final int x = (screen.width - this.frame.getWidth()) / 2;
            final int y = (screen.height - this.frame.getHeight()) / 2;
            this.frame.setLocation(x, y);

            this.frame.setVisible(true);
        }

        /**
         * Get the frame.
         *
         * @return the constructed JFrame
         */
        JFrame getFrame() {

            return this.frame;
        }

        /**
         * Get the top progress bar label.
         *
         * @return the label for the top constructed progress bar
         */
        JLabel getProgressLabel1() {

            return this.progressLabel1;
        }

        /**
         * Get the top progress bar.
         *
         * @return the top constructed progress bar
         */
        JProgressBar getProgressBar1() {

            return this.progressBar1;
        }

        /**
         * Get the bottom progress bar label.
         *
         * @return the label for the bottom constructed progress bar
         */
        JLabel getProgressLabel2() {

            return this.progressLabel2;
        }

        /**
         * Get the bottom progress bar.
         *
         * @return the bottom constructed progress bar
         */
        JProgressBar getProgressBar2() {

            return this.progressBar2;
        }

        /**
         * Get the errors list.
         *
         * @return the constructed errors list
         */
        JTextArea getErrors() {

            return this.errors;
        }

        /**
         * Get the cancel button.
         *
         * @return the constructed cancel button
         */
        JButton getCancel() {

            return this.cancel;
        }

        /**
         * Get the finished button.
         *
         * @return the constructed finished button
         */
        JButton getFinished() {

            return this.finished;
        }
    }

    /**
     * Class to destroy the user interface in the AWT event dispatcher thread.
     */
    private static final class GUIDestroyer implements Runnable {

        /** The JFrame to be destroyed. */
        private final JFrame frame;

        /**
         * Construct a new {@code GUIDestroyer}.
         *
         * @param theFrame the frame to be destroyed
         */
        GUIDestroyer(final JFrame theFrame) {

            this.frame = theFrame;
        }

        /**
         * Destroy the user interface.
         */
        @Override
        public void run() {

            // Runs in the AWT event dispatcher thread
            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }

    /**
     * Class to set the value of a label in the AWT event dispatcher thread.
     */
    private static final class LabelSetter implements Runnable {

        /** The label whose value is to be set. */
        private final JLabel label;

        /** The text to which to set the label's value. */
        private final String text;

        /**
         * Construct a new {@code LabelSetter}.
         *
         * @param theLabel the label whose value is to be set
         * @param theText  the text to which to set the label's value
         */
        LabelSetter(final JLabel theLabel, final String theText) {

            this.label = theLabel;
            this.text = theText;
        }

        /**
         * Update the label.
         */
        @Override
        public void run() {

            this.label.setText(this.text);
        }
    }

    /**
     * Class to set the value of a label in the AWT event dispatcher thread.
     */
    private static final class ProgressSetter implements Runnable {

        /** The progress bar whose value is to be set. */
        private final JProgressBar bar;

        /** The number of pixels of progress to show, out of WIDTH possible. */
        private final int pixels;

        /**
         * Construct a new {@code LabelSetter}.
         *
         * @param theBar    the progress bar whose value is to be set
         * @param thePixels the number of pixels of progress to show
         */
        ProgressSetter(final JProgressBar theBar, final int thePixels) {

            this.bar = theBar;
            this.pixels = thePixels;
        }

        /**
         * Update the progress bar.
         */
        @Override
        public void run() {

            this.bar.setValue(this.pixels);
        }
    }

    /**
     * Class to add text to the error panel in the AWT event dispatcher thread.
     */
    private static final class ErrorLogger implements Runnable {

        /** The text area to update. */
        private final JTextArea area;

        /** The new text to append to the text area. */
        private final String text;

        /**
         * Construct a new {@code ErrorLogger}.
         *
         * @param theArea the text area to update
         * @param theText the new text to append to the text area
         */
        ErrorLogger(final JTextArea theArea, final String theText) {

            this.area = theArea;
            this.text = theText;
        }

        /**
         * Update the text area.
         */
        @Override
        public void run() {

            this.area.append(this.text);
            this.area.append("\n");
            this.area.setRows(this.area.getLineCount());
        }
    }

    /**
     * Class to set button states in the AWT event dispatcher thread.
     */
    private static final class Finisher implements Runnable {

        /** The finished button. */
        private final JButton finished;

        /** The cancel button. */
        private final JButton cancel;

        /**
         * Construct a new {@code Finisher}.
         *
         * @param finishedBtn the finished button
         * @param cancelBtn   the cancel button
         */
        Finisher(final JButton finishedBtn, final JButton cancelBtn) {

            this.finished = finishedBtn;
            this.cancel = cancelBtn;
        }

        /**
         * Update the button states.
         */
        @Override
        public void run() {

            this.finished.setEnabled(true);
            this.cancel.setEnabled(false);
        }
    }
}
