package dev.mathops.app.assessment.qualitycontrol;

import dev.mathops.assessment.EParserMode;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serial;

/**
 * The main window.
 */
final class MainWindow extends JFrame implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3628697526915405964L;

    /** An action command. */
    private static final String SCAN_NORMAL_CMD = "SCAN";

    /** An action command. */
    private static final String SCAN_ALLOW_DEPR_CMD = "SCAN_NO_DEPR";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The library directory. */
    private final File libraryDir;

    /** The report. */
    private final JEditorPane report;

    /** The "Scan Normal" button. */
    private final JButton scanNormalButton;

    /** The "Scan Allow Deprecation" button. */
    private final JButton scanAllowDeprButton;

    /** The "Cancel" button. */
    private final JButton cancelButton;

    /** The field to enter the maximum number of errors to display. */
    private final JTextField maxErrorsField;

    /** The progress bar. */
    private final JProgressBar progress;

    /** The current scan worker. */
    private ScanWorker worker = null;

    /** Refresh report only once per second. */
    private long nextRefresh = 0L;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theLibraryDir the library directory
     */
    MainWindow(final File theLibraryDir) {

        super("Quality Control Scanner");

        this.libraryDir = theLibraryDir;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension pref = new Dimension(screen.width / 2, (screen.height << 2) / 5);
        content.setPreferredSize(pref);

        this.report = new JEditorPane("text/html", null);
        this.report.setEditable(false);
        this.report.setBackground(new Color(255, 255, 245));

        final JScrollPane scroll = new JScrollPane(this.report);
        scroll.getVerticalScrollBar().setBlockIncrement(5);

        content.add(scroll, StackedBorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));

        this.scanNormalButton = new JButton("Perform Normal Scan");
        this.scanNormalButton.setActionCommand(SCAN_NORMAL_CMD);
        this.scanNormalButton.addActionListener(this);
        buttons.add(this.scanNormalButton);

        this.scanAllowDeprButton = new JButton("Perform Scan Ignoring Deprecations");
        this.scanAllowDeprButton.setActionCommand(SCAN_ALLOW_DEPR_CMD);
        this.scanAllowDeprButton.addActionListener(this);
        buttons.add(this.scanAllowDeprButton);

        this.cancelButton = new JButton("Cancel Scan");
        this.cancelButton.setActionCommand(CANCEL_CMD);
        this.cancelButton.addActionListener(this);
        this.cancelButton.setEnabled(false);
        buttons.add(this.cancelButton);

        final JLabel lbl = new JLabel("         Maximum number of errors: " );
        buttons.add(lbl);
        this.maxErrorsField = new JTextField("200");
        buttons.add(this.maxErrorsField);

        content.add(buttons, StackedBorderLayout.SOUTH);

        this.progress = new JProgressBar(SwingConstants.HORIZONTAL);
        final Font font = this.progress.getFont();
        final Font larger = font.deriveFont((float) font.getSize() * 1.5f);
        this.progress.setFont(larger);

        this.progress.setMaximum(1000);
        this.progress.setValue(0);
        this.progress.setStringPainted(true);
        this.progress.setString(CoreConstants.SPC);

        content.add(this.progress, StackedBorderLayout.SOUTH);

        getRootPane().setDefaultButton(this.scanNormalButton);

        UIUtilities.packAndCenter(this);
    }

    /**
     * Gets the library directory.
     *
     * @return the library directory.
     */
    File getLibraryDir() {

        return this.libraryDir;
    }

    /**
     * Updates the UI with progress information. Called on the AWT event thread.
     *
     * @param update the update
     */
    void update(final ProgressUpdate update) {

        this.progress.setValue(Math.round(10.0f * update.percentDone));
        this.progress.setString(update.onStep);

        final long now = System.currentTimeMillis();
        if (now > this.nextRefresh) {
            this.report.setText(update.report);
            this.nextRefresh = now + 500L;
        }
    }

    /**
     * Called when the worker has finished.
     */
    void workerDone() {

        this.scanNormalButton.setEnabled(true);
        this.scanAllowDeprButton.setEnabled(true);
        this.cancelButton.setEnabled(false);
    }

    /**
     * Called when the "Scan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (SCAN_NORMAL_CMD.equals(cmd)) {
            this.progress.setValue(0);
            this.progress.setString(CoreConstants.SPC);

            final String maxErrorsStr = this.maxErrorsField.getText();
            int maxErrors;
            try {
                maxErrors = Integer.parseInt(maxErrorsStr);
            } catch (final NumberFormatException ex) {
                maxErrors = Integer.MAX_VALUE;
            }

            this.scanNormalButton.setEnabled(false);
            this.scanAllowDeprButton.setEnabled(false);
            this.cancelButton.setEnabled(true);
            this.worker = new ScanWorker(this, EParserMode.NORMAL, maxErrors);
            this.worker.execute();
        } else if (SCAN_ALLOW_DEPR_CMD.equals(cmd)) {
            this.progress.setValue(0);
            this.progress.setString(CoreConstants.SPC);

            final String maxErrorsStr = this.maxErrorsField.getText();
            int maxErrors;
            try {
                maxErrors = Integer.parseInt(maxErrorsStr);
            } catch (final NumberFormatException ex) {
                maxErrors = Integer.MAX_VALUE;
            }

            this.scanNormalButton.setEnabled(false);
            this.scanAllowDeprButton.setEnabled(false);
            this.cancelButton.setEnabled(true);
            this.worker = new ScanWorker(this, EParserMode.ALLOW_DEPRECATED, maxErrors);
            this.worker.execute();
        } else if (CANCEL_CMD.equals(cmd)) {
            if (this.worker != null) {
                this.worker.cancel(false);
            }
        }
    }
}
