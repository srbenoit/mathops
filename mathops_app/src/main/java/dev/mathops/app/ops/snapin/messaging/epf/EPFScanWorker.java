package dev.mathops.app.ops.snapin.messaging.epf;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.IProgressListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.app.ops.snapin.messaging.ScannerStatus;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.db.schema.RawRecordConstants;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A worker class that can run the EPF scan in the background.
 */
public final class EPFScanWorker extends SwingWorker<Integer, ScannerStatus> implements IProgressListener {

    /** The sections for which to generate EPF reports. */
//    private static final String[] SECTIONS = {"001", "401", "801", "809"};
    private static final String[] SECTIONS = {"002"};

    /** The scanner. */
    private final EPFStudents scanner;

    /** The automated messages report. */
    private final Map<String, MessageToSend> epf;

    /** The owning frame. */
    private final JFrame frame;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /**
     * Constructs a new {@code EPFScanWorker}.
     *
     * @param theCache          the data cache
     * @param theFrame          the owning frame
     * @param theProgress       the progress bar
     * @param theInvokingButton the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel  the label for the progress bar - to be cleared when this process is complete
     */
    public EPFScanWorker(final Cache theCache, final JFrame theFrame,
                         final JProgressBar theProgress, final JButton theInvokingButton,
                         final JLabel theProgressLabel) {
        super();

        this.epf = new HashMap<>(1000);
        this.scanner = new EPFStudents(theCache, this);
        this.frame = theFrame;
        this.progressBar = theProgress;
        this.invokingButton = theInvokingButton;
        this.progressLabel = theProgressLabel;
    }

    /**
     * Execute the task in a background thread.
     */
    @Override
    public Integer doInBackground() {

        publish(new ScannerStatus(0, 100, "Scanning for EPF students"));

        final Map<String, List<String>> incCourseSections = new HashMap<>(10);

        final List<String> sect117 = Arrays.asList(SECTIONS);
        final List<String> sect118 = Arrays.asList(SECTIONS);
        final List<String> sect124 = Arrays.asList(SECTIONS);
        final List<String> sect125 = Arrays.asList(SECTIONS);
        final List<String> sect126 = Arrays.asList(SECTIONS);

        incCourseSections.put(RawRecordConstants.M117, sect117);
        incCourseSections.put(RawRecordConstants.M118, sect118);
        incCourseSections.put(RawRecordConstants.M124, sect124);
        incCourseSections.put(RawRecordConstants.M125, sect125);
        incCourseSections.put(RawRecordConstants.M126, sect126);

        try {
            this.scanner.calculate(incCourseSections, this.epf);
        } catch (final Exception ex) {
            Log.severe(ex);
        }

        return Integer.valueOf(this.epf.size());
    }

    /**
     * Indicates progress.
     *
     * @param description    a description of the current operation
     * @param stepsCompleted the number of steps completed
     * @param totalSteps     the total number of steps
     */
    @Override
    public void progress(final String description, final int stepsCompleted, final int totalSteps) {

        publish(new ScannerStatus(stepsCompleted, totalSteps, description));
    }

    /**
     * Called when an update is published.
     *
     * @param chunks data chunks
     */
    @Override
    protected void process(final List<ScannerStatus> chunks) {

        if (!chunks.isEmpty()) {
            final ScannerStatus last = chunks.getLast();
            final int progressValue = last.totalSteps == 0 ? 1000 : (1000 * last.stepsCompleted / last.totalSteps);
            this.progressBar.setValue(progressValue);
            this.progressBar.setString(last.description);
        }
    }

    /**
     * Called when the task is done.
     */
    @Override
    protected void done() {

        this.progressBar.setValue(0);
        this.progressBar.setString(CoreConstants.EMPTY);
        this.invokingButton.setEnabled(true);
        this.progressLabel.setText(CoreConstants.SPC);

        final EPFResultsDialog dialog = new EPFResultsDialog(this.frame, this.epf);
        dialog.setVisible(true);
    }
}
