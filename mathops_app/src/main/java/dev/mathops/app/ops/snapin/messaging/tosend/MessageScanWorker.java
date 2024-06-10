package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.app.ops.snapin.messaging.EPF;
import dev.mathops.app.ops.snapin.messaging.EmailsNeeded;
import dev.mathops.app.ops.snapin.messaging.IProgressListener;
import dev.mathops.app.ops.snapin.messaging.MessagingFull;
import dev.mathops.app.ops.snapin.messaging.ScannerStatus;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A worker class that can run the message scan in the background.
 */
public final class MessageScanWorker extends SwingWorker<String, ScannerStatus> implements IProgressListener {

    /** The system data object. */
    private final SystemData systemData;

    /** The owning window to provide with results of the scan. */
    private final MessagingFull owner;

    /** The progress bar. */
    private final JProgressBar progressBar;

    /** The button that invoked the action. */
    private final JButton invokingButton;

    /** The label for the progress bar. */
    private final JLabel progressLabel;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /**
     * Constructs a new {@code MessageScanWorker}.
     *
     * @param theCache          the data cache
     * @param theOwner          the owning window to provide with results of the scan
     * @param theProgress       the progress bar
     * @param theInvokingButton the invoking button, which will get re-enabled once the process is complete
     * @param theProgressLabel  the label for the progress bar - to be cleared when this process is complete
     */
    public MessageScanWorker(final Cache theCache, final MessagingFull theOwner, final JProgressBar theProgress,
                             final JButton theInvokingButton, final JLabel theProgressLabel) {
        super();

        this.systemData = new SystemData(theCache);
        this.owner = theOwner;

        this.progressBar = theProgress;
        this.invokingButton = theInvokingButton;
        this.progressLabel = theProgressLabel;

        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Execute the task in a background thread.
     */
    @Override
    public String doInBackground() {

        final Map<String, List<String>> incCourseSections = new HashMap<>(10);

        final String[] sections = {"001", "002", "401", "801", "809"};
        final List<String> sect117 = Arrays.asList(sections);
        final List<String> sect118 = Arrays.asList(sections);
        final List<String> sect124 = Arrays.asList(sections);
        final List<String> sect125 = Arrays.asList(sections);
        final List<String> sect126 = Arrays.asList(sections);

        incCourseSections.put(RawRecordConstants.M117, sect117);
        incCourseSections.put(RawRecordConstants.M118, sect118);
        incCourseSections.put(RawRecordConstants.M124, sect124);
        incCourseSections.put(RawRecordConstants.M125, sect125);
        incCourseSections.put(RawRecordConstants.M126, sect126);

        final MessagePopulationBuilder popScanner = new MessagePopulationBuilder(this.systemData, incCourseSections);

        try {
            publish(new ScannerStatus(0, 300, "Scanning registrations"));

            popScanner.scan();
            Log.info("Scanner says " + popScanner.totalStudents + " total students");

            this.canceled.set(false);
            try {
                progress("Querying instructors", 1, 300);

                final Map<Integer, Map<String, String>> instructors = EmailsNeeded.getInstructors();
                final TermRec act = this.systemData.getActiveTerm();

                if (act == null) {
                    Log.warning("ERROR: Cannot query active term");
                } else {
                    progress("Gathering milestones", 2, 300);

                    // Map from pace to map from track to list of milestones.
                    final Map<Integer, Map<String, List<RawMilestone>>> msMap = EPF.gatherMilestones(this.systemData,
                            act.term);

                    // Now see who is due for a communication that they have not been sent.
                    final LocalDate today = LocalDate.now();
                    final int numStudents = popScanner.totalStudents;
                    final int totalSteps = 3 + numStudents;
                    int completed = 3;

                    // if (!this.canceled.get()) {
                    // completed = processPopulation(today, completed, totalSteps,
                    // popScanner.nonCountedIncomplete, act, instructors, msMap);
                    // }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.five, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.fiveWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.four, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.fourWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.three, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.threeWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoA, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoAWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoB, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoBWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoC, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.twoCWithForfeit, act,
                                instructors, msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.oneA, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        completed = processPopulation(today, completed, totalSteps, popScanner.oneB, act, instructors,
                                msMap);
                    }
                    if (!this.canceled.get()) {
                        processPopulation(today, completed, totalSteps, popScanner.oneC, act, instructors, msMap);
                    }

                    progress("Finished", totalSteps, totalSteps);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }

            this.owner.messageScanFinished(popScanner);
        } catch (final SQLException ex) {
            Log.warning("Failed to query student registration data.", ex);
        }

        return CoreConstants.EMPTY;
    }

    /**
     * Processes a single category.
     *
     * @param today            the current date
     * @param initialCompleted the number of students completed on entry
     * @param totalSteps       the total number of steps
     * @param population       the population to process
     * @param act              the active term
     * @param instructors      the instructors
     * @param msMap            milestones
     * @return the number of students completed on exit
     * @throws SQLException if there is an error accessing the database
     */
    private int processPopulation(final LocalDate today, final int initialCompleted,
                                  final int totalSteps, final Population population, final TermRec act,
                                  final Map<Integer, ? extends Map<String, String>> instructors,
                                  final Map<Integer, ? extends Map<String, List<RawMilestone>>> msMap)
            throws SQLException {

        int completed = initialCompleted;

        for (final Map.Entry<String, PopulationSection> e : population.sections.entrySet()) {

            final PopulationSection popSect = e.getValue();
            final Map<String, List<RawStcourse>> stuToRegsMap = popSect.students;

            for (final Map.Entry<String, List<RawStcourse>> stuEntry : stuToRegsMap.entrySet()) {

                final String studentId = stuEntry.getKey();
                final List<RawStcourse> regs = stuEntry.getValue();

                final String descr = "Processing student " + (completed - 3) + " out of " + (totalSteps - 3);
                progress(descr, completed, totalSteps);

                final StudentData studentData = new StudentData(this.cache, studentId, ELiveRefreshes.NONE);

                EmailsNeeded.processStudent(studentData, regs, today, msMap, act, popSect.messagesDue, instructors);

                ++completed;
            }
        }

        return completed;
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
            final ScannerStatus last = chunks.get(chunks.size() - 1);
            final int progressValue = 1000 * last.stepsCompleted / last.totalSteps;
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
    }
}
