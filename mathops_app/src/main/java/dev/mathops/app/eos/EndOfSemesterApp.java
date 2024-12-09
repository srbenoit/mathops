package dev.mathops.app.eos;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.app.eos.s1.S100CheckForNonReturnedResources;
import dev.mathops.app.eos.s1.S101EditNextTermDataFiles;
import dev.mathops.app.eos.s1.S102IdentifyIncompletes;
import dev.mathops.app.eos.s1.S103RegistrationUpdate;
import dev.mathops.app.eos.s1.S104UploadQueuedPlacementScores;
import dev.mathops.app.eos.s1.S105ApplyForfeits;
import dev.mathops.app.eos.s1.S106CheckStudentsBelow54;
import dev.mathops.app.eos.s1.S107ProcessIncompletes;
import dev.mathops.app.eos.s1.S108GradeSnapshot;
import dev.mathops.app.eos.s1.S109UpdateProctorUWindows;
import dev.mathops.app.eos.s1.S110CreateCanvasShells;
import dev.mathops.app.eos.s2.S201PlaceSitesInMaintenanceMode;
import dev.mathops.app.eos.s2.S202ExecuteBatchJobs;
import dev.mathops.app.eos.s2.S203BoundProductionDatabaseAndExport;
import dev.mathops.app.eos.s2.S204EditDatabaseExportForDEV;
import dev.mathops.app.eos.s2.S205DropDEVDatabaseAndImport;
import dev.mathops.app.eos.s2.S206PointWebSitesAtDEV;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * An application to automate end-of-semester tasks.
 */
final class EndOfSemesterApp implements Runnable, ActionListener {

    /** Saves the state of the process to a file. */
    private static final String SAVE_CMD = "SAVE";

    /** Loads the state of the process from a file. */
    private static final String LOAD_CMD = "LOAD";

    /** The section 1 heading. */
    private static final String SECTION01 = "Phase 1: Review and Prepare";

    /** The section 2 heading. */
    private static final String SECTION02 = "Phase 2: Move Placement and Tutorials to DEV";

    /** The section 5 heading. */
    private static final String SECTION05 = "Phase 5: Some other stuff...";

    /** The list of steps. */
    private final List<AbstractStep> steps;

    /**
     * Constructs a new {@code EndOfSemesterApp}.
     */
    private EndOfSemesterApp() {

        this.steps = new ArrayList<>(100);
    }

    /**
     * Constructs the UI on the AWT event thread.
     */
    @Override
    public void run() {

        final Class<?> cls = getClass();
        final Image expandImg = FileLoader.loadFileAsImage(cls, "expand.png", true);
        final Image collapseImg = FileLoader.loadFileAsImage(cls, "collapse.png", true);

        final JFrame frame = new JFrame("End-of-Semester Processing");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = screen.width * 3 / 4;
        final int h = screen.height * 3 / 4;

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setPreferredSize(new Dimension(w, h));
        frame.setContentPane(content);

        final StepList stepList = new StepList(expandImg, collapseImg);
        final JScrollPane stepListScroll = new JScrollPane(stepList);
        content.add(stepListScroll, StackedBorderLayout.CENTER);

        final StepDisplay status = new StepDisplay(h - 20, w / 2);
        content.add(status, StackedBorderLayout.EAST);

        final S100CheckForNonReturnedResources step100 = new S100CheckForNonReturnedResources(stepList, status);
        this.steps.add(step100);
        stepList.addStep(SECTION01, step100);

        final S101EditNextTermDataFiles step101 = new S101EditNextTermDataFiles(stepList, status);
        this.steps.add(step101);
        stepList.addStep(SECTION01, step101);

        final S102IdentifyIncompletes step102 = new S102IdentifyIncompletes(stepList, status);
        this.steps.add(step102);
        stepList.addStep(SECTION01, step102);

        final S103RegistrationUpdate step103 = new S103RegistrationUpdate(stepList, status);
        this.steps.add(step103);
        stepList.addStep(SECTION01, step103);

        final S104UploadQueuedPlacementScores step104 = new S104UploadQueuedPlacementScores(stepList, status);
        this.steps.add(step104);
        stepList.addStep(SECTION01, step104);

        final S105ApplyForfeits step105 = new S105ApplyForfeits(stepList, status);
        this.steps.add(step105);
        stepList.addStep(SECTION01, step105);

        final S106CheckStudentsBelow54 step106 = new S106CheckStudentsBelow54(stepList, status);
        this.steps.add(step106);
        stepList.addStep(SECTION01, step106);

        final S107ProcessIncompletes step107 = new S107ProcessIncompletes(stepList, status);
        this.steps.add(step107);
        stepList.addStep(SECTION01, step107);

        final S108GradeSnapshot step108 = new S108GradeSnapshot(stepList, status);
        this.steps.add(step108);
        stepList.addStep(SECTION01, step108);

        final S109UpdateProctorUWindows step109 = new S109UpdateProctorUWindows(stepList, status);
        this.steps.add(step109);
        stepList.addStep(SECTION01, step109);

        final S110CreateCanvasShells step110 = new S110CreateCanvasShells(stepList, status);
        this.steps.add(step110);
        stepList.addStep(SECTION01, step110);

        //

        final S201PlaceSitesInMaintenanceMode step201 = new S201PlaceSitesInMaintenanceMode(stepList, status);
        this.steps.add(step201);
        stepList.addStep(SECTION02, step201);

        final S202ExecuteBatchJobs step202 = new S202ExecuteBatchJobs(stepList, status);
        this.steps.add(step202);
        stepList.addStep(SECTION02, step202);

        final S203BoundProductionDatabaseAndExport step203 = new S203BoundProductionDatabaseAndExport(stepList, status);
        this.steps.add(step203);
        stepList.addStep(SECTION02, step203);

        final S204EditDatabaseExportForDEV step204 = new S204EditDatabaseExportForDEV(stepList, status);
        this.steps.add(step204);
        stepList.addStep(SECTION02, step204);

        final S205DropDEVDatabaseAndImport step205 = new S205DropDEVDatabaseAndImport(stepList, status);
        this.steps.add(step205);
        stepList.addStep(SECTION02, step205);

        final S206PointWebSitesAtDEV step206 = new S206PointWebSitesAtDEV(stepList, status);
        this.steps.add(step206);
        stepList.addStep(SECTION02, step206);

        //
        //

        //

        final S500CreateArchiveTables step500 = new S500CreateArchiveTables(stepList, status, null);
        this.steps.add(step500);
        stepList.addStep(SECTION05, step500);

        final S501ArchiveData step501 = new S501ArchiveData(stepList, status, null, null);
        this.steps.add(step501);
        stepList.addStep(SECTION05, step501);

        //
        stepList.collapseSection(SECTION02);
        stepList.collapseSection(SECTION05);

        final JButton saveBtn = new JButton("Save progress...");
        saveBtn.setActionCommand(SAVE_CMD);
        saveBtn.addActionListener(this);
        final JButton loadBtn = new JButton("Load saved progress...");
        loadBtn.setActionCommand(LOAD_CMD);
        loadBtn.addActionListener(this);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));
        buttons.add(saveBtn);
        buttons.add(loadBtn);
        content.add(buttons, StackedBorderLayout.SOUTH);

        frame.pack();
        final Dimension size = frame.getSize();

        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        frame.setVisible(true);
    }

    /**
     * Called when the "Save" or "Load" buttons are activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (SAVE_CMD.equals(cmd)) {
            Log.info("Save");

            // For each step, we create a JSON object with the step number, finished status, report, and notes.

            final JSONObject json = new JSONObject();

            final int count = this.steps.size();
            final JSONObject[] stepObjects = new JSONObject[count];

            for (int i = 0; i < count; ++i) {
                final JSONObject stepObj = new JSONObject();
                stepObjects[i] = stepObj;

                final AbstractStep step = this.steps.get(i);
                stepObj.setProperty("number", Integer.valueOf(step.stepNumber));
                stepObj.setProperty("finished", Boolean.valueOf(step.isFinished()));
                final String results = step.results;
                if (results != null && !results.isBlank()) {
                    stepObj.setProperty("results", results);
                }
                final String notes = step.notes;
                if (notes != null && !notes.isBlank()) {
                    stepObj.setProperty("notes", notes);
                }
            }
            json.setProperty("steps", stepObjects);

        } else if (LOAD_CMD.equals(cmd)) {
            Log.info("Load");
        }
    }

    /**
     * Launches the application and creates the login window.
     *
     * <pre>
     * --username foo --password bar
     * </pre>
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new EndOfSemesterApp());
    }
}
