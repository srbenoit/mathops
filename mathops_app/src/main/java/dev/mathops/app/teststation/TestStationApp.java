package dev.mathops.app.teststation;

import dev.mathops.app.ClientBase;
import dev.mathops.app.FrameToFront;
import dev.mathops.app.TempFileCleaner;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.exam.ExamPanel;
import dev.mathops.app.exam.ExamPanelWrapper;
import dev.mathops.app.placement.survey.Survey;
import dev.mathops.app.simplewizard.Wizard;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ChangeUI;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.session.SessionCache;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.AbstractReplyBase;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.ExamStartResultReply;
import dev.mathops.session.txn.messages.ExamStartResultRequest;
import dev.mathops.session.txn.messages.ExceptionSubmissionRequest;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetExamRequest;
import dev.mathops.session.txn.messages.MessageFactory;
import dev.mathops.session.txn.messages.SurveyStatusReply;
import dev.mathops.session.txn.messages.SurveyStatusRequest;
import dev.mathops.session.txn.messages.SurveySubmitReply;
import dev.mathops.session.txn.messages.SurveySubmitRequest;
import dev.mathops.session.txn.messages.TestingStationInfoReply;
import dev.mathops.session.txn.messages.TestingStationInfoRequest;
import dev.mathops.session.txn.messages.TestingStationResetRequest;
import dev.mathops.session.txn.messages.TestingStationStatusReply;
import dev.mathops.session.txn.messages.TestingStationStatusRequest;
import dev.mathops.session.txn.messages.UpdateExamReply;
import dev.mathops.session.txn.messages.UpdateExamRequest;
import jwabbit.gui.Registry;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is an end-user application to be used in a proctored testing center. It operates under the control of a
 * centralized check-in station, and allows students to take proctored exams. It constantly queries a central server for
 * its operational state, and performs accordingly.
 */
public final class TestStationApp extends ClientBase implements Runnable, ExamContainerInt {

    /** Version number for screen displays. */
    static final String VERSION = "v2.5.19 (July. 12, 2024)";

    /** The main frame for the application. */
    private JFrame frame;

    /** The user home directory. */
    private final File userHomeDir;

    /** The desktop pane that holds all internal frames. */
    private BackgroundPane desk;

    /** The status label at the bottom of the desktop. */
    private JLabel statusLabel;

    /** The desktop background colors for each state. */
    private Map<Integer, Color> colors;

    /** The name of the testing center this station is in. */
    private String testingCenterName;

    /** The number of this testing station. */
    private String stationNumber;

    /** The current status of the station. */
    private Integer status = RawClientPc.STATUS_ERROR;

    /** The ID of the student who is to take the exam. */
    private String currentStudentId;

    /** The name of the student taking the exam. */
    private String currentStudentName;

    /** The current version of exam being taken. */
    private String currentVersion;

    /** The exam session. */
    private ExamSession examSession;

    /** The panel showing the active exam. */
    private ExamPanel examPanel;

    /** The wrapper to present the exam panel in an internal frame. */
    private ExamPanelWrapper examPanelWrapper;

    /** Flag to indicate grading should take place. */
    private String grade;

    /** Grades from the submitted exam. */
    private Map<String, Integer> examScores;

    /** Grades from the submitted exam. */
    private Map<String, Object> examGrades;

    /** Error grading the exam. */
    private String examError;

    /** A dialog to confirm that the exam is complete. */
    private AreYouFinished confirm;

    /** Flag controlling whether coupons are used when exam is started. */
    private boolean checkCoupons;

    /** Flag controlling whether eligibility is tested when exam is started. */
    private boolean checkEligibility;

    /** The currently displayed error text. */
    private String errorDisplay;

    /**
     * Constructs a new {@code TestStationApp}.
     *
     * @param theServer    the server host name
     * @param thePort      the server port
     * @param theSessionId the session ID
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private TestStationApp(final String theServer, final int thePort, final String theSessionId)
            throws UnknownHostException {

        super(theServer, thePort, theSessionId);

        Log.info("Connected to ", theServer);

        // Set the ROM location to load a particular calculator
        final String home = System.getProperty("user.home");
        Log.info(home);

        this.userHomeDir = new File(home);
        setPublicInternet(false);

        final String romPath = home + "/.testing/_TI-84PCSE.rom";
        if (new File(romPath).exists()) {
            Registry.saveWabbitKey("rom_path", romPath);
        } else {
            final String romPath2 = home + "/.pace/_TI-84PCSE.rom";
            Registry.saveWabbitKey("rom_path", romPath2);
        }

        Registry.saveWabbitKey("faceplate_color", Integer.valueOf(0x501010));
        Registry.saveWabbitKey("auto_turn_on", "true");
        Registry.saveWabbitKey("check_updates", "false");
        Registry.saveWabbitKey("exit_save_state", "false");
    }

    /**
     * Keeps the blocking window on top of all other windows.
     */
    @Override
    public void run() {

        final Runnable obj = new FrameToFront(this.frame);

        try {
            while (this.frame.isVisible()) {
                try {
                    SwingUtilities.invokeAndWait(obj);
                } catch (final InvocationTargetException ex) {
                    Log.warning(ex);
                }

                Thread.sleep(50L);
            }
        } catch (final InterruptedException ex) {
            // On interrupt, exit this thread so the application can close down gracefully
            Log.warning(ex);
        }
    }

    /**
     * Get the {@code JFrame}.
     *
     * @return the frame
     */
    @Override
    public JFrame getFrame() {

        return this.frame;
    }

    /**
     * The application's main processing method. This should be called after object construction to run the testing
     * interaction with the user.
     *
     * <p>
     * This method runs in an infinite loop. It polls the server for the state of this testing station, and when a
     * change of state is detected, it changes behavior appropriately.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
     */
    private void execute(final boolean fullScreen) {

        try {
            Log.info("   Creating main window...");
            // Create a full-screen, top-level window and activate a thread that will keep it on
            // top of everything else on the desktop. All windows this application creates will be
            // children of this window, so they will not be obscured, but the desktop will not be
            // available.
            if (!createBlockingWindow(fullScreen)) {
                return;
            }

            Log.info("   Initializing...");
            // Initialize the application by connecting to the server and obtaining a testing
            // station ID and testing center name. This method will not return until successful.
            // The status member variable will be set to one of UNINITIALIZED, LOCKED, or
            // PAPER_ONLY from EComputerState.
            initialize();

            // See if there was a leftover exam in progress that did not get submitted properly,
            // and if so, submit it now.
            checkExamCache();

            // Now we enter the main loop of the application. Based on the status variable, we
            // process that status until the status changes.
            while (this.frame.isVisible()) {

                showError("Current state: " + this.status);

                if (RawClientPc.STATUS_ERROR.equals(this.status)) {
                    initialize();
                } else if (RawClientPc.STATUS_UNINITIALIZED.equals(this.status)) {
                    runUninitialized();
                } else if (RawClientPc.STATUS_PAPER_ONLY.equals(this.status)) {
                    runPaperOnly();
                } else if (RawClientPc.STATUS_LOCKED.equals(this.status)) {
                    runLocked();
                } else if (RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)) {
                    this.checkCoupons = true;
                    this.checkEligibility = true;
                    runAwaitStudent();
                } else if (RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {
                    this.checkCoupons = false;
                    this.checkEligibility = false;
                    runAwaitStudent();
                } else if (RawClientPc.STATUS_TAKING_EXAM.equals(this.status)) {
                    runExam();
                } else {
                    reset();
                }

                // Safety to prevent rapid loop.
                Thread.sleep(50L);
            }
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        if (this.frame != null) {
            this.frame.dispose();
        }
    }

    /**
     * Create the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
     * @return {@code true} if the blocking window was created; false otherwise
     * @throws InterruptedException if the process is interrupted
     */
    private boolean createBlockingWindow(final boolean fullScreen) throws InterruptedException {

        this.colors = new HashMap<>(15);
        this.colors.put(RawClientPc.STATUS_UNINITIALIZED, ColorNames.getColor("gray20"));
        this.colors.put(RawClientPc.STATUS_LOCKED, ColorNames.getColor("DarkSlateGray"));
        this.colors.put(RawClientPc.STATUS_PAPER_ONLY, ColorNames.getColor("gray20"));
        this.colors.put(RawClientPc.STATUS_AWAIT_STUDENT, ColorNames.getColor("MidnightBlue"));
        this.colors.put(RawClientPc.STATUS_TAKING_EXAM, ColorNames.getColor("DarkGreen"));
        this.colors.put(RawClientPc.STATUS_EXAM_RESULTS, ColorNames.getColor("DarkOrchid4"));
        this.colors.put(RawClientPc.STATUS_FORCE_SUBMIT, ColorNames.getColor("SaddleBrown"));
        this.colors.put(RawClientPc.STATUS_CANCEL_EXAM, ColorNames.getColor("MidnightBlue"));
        this.colors.put(RawClientPc.STATUS_LOGIN_NOCHECK, ColorNames.getColor("MidnightBlue"));

        // Construct the window in the AWT dispatcher thread.
        final BlockingWindowBuilder builder = new BlockingWindowBuilder(fullScreen);
        boolean result;

        try {
            SwingUtilities.invokeAndWait(builder);
            result = true;
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
            result = false;
        }

        if (result) {
            this.frame = builder.getFrame();
            this.desk = builder.getDesk();
            this.statusLabel = builder.getStatusLabel();
            this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            // Start the thread that will keep the blocking window on top of all other objects on the desktop.
            // new Thread(this).start();
        }

        return result;
    }

    /**
     * Connect to the server and request our testing station number and testing center name. This method will not return
     * until it is successful.
     *
     * @throws InterruptedException if the process is interrupted during initialization
     */
    private void initialize() throws InterruptedException {

        showError("Cleaning temporary files...");

        // Show a status message while we clean out temporary files
        StatusDisplay theStatus = new StatusDisplay(this.desk, this.colors.get(RawClientPc.STATUS_UNINITIALIZED),
                "Cleaning Temporary Files...", null, "\u0055");

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }
        Thread.sleep(100L);

        TempFileCleaner.clean();

        showError("Connecting to server...");

        // Show a status message while we try to reach the server.
        theStatus = new StatusDisplay(this.desk, this.colors.get(RawClientPc.STATUS_UNINITIALIZED),
                "Connecting to server...", null, "\u0055");

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            try {
                if (connectToServer() == SUCCESS) {

                    // The following method will not succeed unless it gets to a state of
                    // UNINITIALIZED, LOCKED, or PAPER_ONLY.
                    if (getTestingStationInfo()) {
                        showError("Connection to server successful");
                        return;
                    }

                    disconnectFromServer();
                } else {
                    showError("Server not yet responding...");
                }
            } catch (final Exception ex) {
                Log.warning(ex);
                showError("Error connecting to server: " + ex.getMessage());
            }

            Thread.sleep(3000L);
        }
    }

    /**
     * Connect to the server and request our testing station number and testing center name. This method will not return
     * until it is successful.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void checkExamCache() throws InterruptedException {

        showError("Checking for cached exam");

        // If there is no cached exam (or cached exam has no status), bail out.
        File f = new File(this.userHomeDir, "exam-in-progress.xml");

        if (!f.exists()) {
            showError("No cached exam on record");
            return;
        }

        showError("Cached exam found - attempting to load");

        f = new File(this.userHomeDir, "update-exam.xml");

        if (!f.exists()) {
            showError("Cached exam missing update data");

            // Clear cached exam that had no status.
            f = new File(this.userHomeDir, "exam-in-progress.xml");

            try {
                f.delete();
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            return;
        }

        showError("Reading cached exam data");

        final UpdateExamRequest req;

        // Try to read the stored exam state. If we can't, log it and move on.
        char[] buf = null;

        try {
            final long len = f.length();
            buf = new char[(int) len];

            try (final FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                fr.read(buf);
            }

            req = new UpdateExamRequest(buf);
            req.indicateRecovered();
        } catch (final Exception ex) {

            if (buf != null) {
                Log.warning(String.valueOf(buf), ex);
            } else {
                Log.warning(ex);
            }

            return;
        }

        showError("Submitting recovered exam");

        // Show a status message while we try to submit the exam.
        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(RawClientPc.STATUS_UNINITIALIZED),
                "Submitting recovered exam...", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        // Try (repeatedly) to send the stored state to the server.
        for (; ; ) {

            // Pause between tries (and on the first attempt, make sure there is a pause so the
            // above status message is visible).
            Thread.sleep(5000L);

            if (!getServerConnection().writeObject(req.toXml())) {
                showError("Can't send completed exam to server");
                Log.warning("Can't send completed exam to server");

                continue;
            }

            final Object obj = getServerConnection().readObject("UpdateExamReply");

            if (obj == null) {
                showError("Can't read completed exam acknowledgement from server");
                Log.warning("Can't read completed exam acknowledgement from server");

                continue;
            }

            if (!"[C".equals(obj.getClass().getName())) {
                showError("Completed exam acknowledgement was " + obj.getClass().getName());
                Log.warning("Completed exam acknowledgement was ", obj.getClass().getName());

                continue;
            }

            final UpdateExamReply reply;

            try {
                reply = new UpdateExamReply((char[]) obj);
            } catch (final IllegalArgumentException ex) {
                showError("Unable to evaluate completed exam acknowledgement");
                Log.warning(ex);

                continue;
            }

            if (reply.status != UpdateExamReply.SUCCESS) {
                showError("Server did not acknowledge completed exam properly.");
                Log.warning("Server did not acknowledge completed exam properly.");

                continue;
            }

            break;
        }

        // Remove the cached files
        f = new File(this.userHomeDir, "exam-in-progress.xml");

        try {
            f.delete();
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        f = new File(this.userHomeDir, "update-exam.xml");

        try {
            f.delete();
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        showError("Cleared cached exam");
    }

    /**
     * Send a request to the server to verify that this machine is in fact a valid testing station. This check is based
     * on the certificate ID used to establish the SSL session. On successful return, the testing center name and
     * station number will be filled in.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    private boolean getTestingStationInfo() {

        final TestingStationInfoRequest request = new TestingStationInfoRequest();
        request.machineId = getMachineId();

        final AbstractReplyBase obj = doExchange(request, "TestingStationInfo", true);

        if (obj == null) {
            showError("Unable to read testing station information from the server.");
            Log.warning("Unable to read testing station information from the server.");
            return false;
        } else if (!(obj instanceof TestingStationInfoReply)) {
            showError("Testing station information was " + obj.getClass().getName() + CoreConstants.DOT);
            Log.warning("Testing station information was ", obj.getClass().getName(), CoreConstants.DOT);
            return false;
        }

        final TestingStationInfoReply reply = (TestingStationInfoReply) obj;

        if (reply.error != null) {
            showError("Testing station information error: " + reply.error + CoreConstants.DOT);
            Log.warning("Testing station information error:", reply.error, CoreConstants.DOT);
            return false;
        }

        if (reply.status == null) {
            showError("Testing station information did not indicate status.");
            Log.warning("Testing station information did not indicate status.");
            return false;
        }

        if (RawClientPc.STATUS_UNINITIALIZED.equals(reply.status)
                || RawClientPc.STATUS_LOCKED.equals(reply.status)
                || RawClientPc.STATUS_PAPER_ONLY.equals(reply.status)) {
            this.status = reply.status;
        } else {
            showError("Requesting a server-reset of this station...");
            reset();
            return false;
        }

        this.testingCenterName = reply.testingCenterName;
        this.stationNumber = reply.stationNumber;

        return true;
    }

    /**
     * Send a request to the server for this station's current status. On successful return, the status variable will be
     * filled in.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    private boolean getTestingStationStatus() {

        final TestingStationStatusRequest request = new TestingStationStatusRequest();
        request.machineId = getMachineId();

        // Send the request
        final AbstractReplyBase obj = doExchange(request, "TestingStationStatus", true);

        if (obj == null) {
            showError("Unable to read testing station status from the server.");
            Log.warning("Unable to read testing station status from the server.");
            return false;
        } else if (!(obj instanceof TestingStationStatusReply)) {
            showError("Testing station status was " + obj.getClass().getName() + CoreConstants.DOT);
            Log.warning("Testing station status was ", obj.getClass().getName(), CoreConstants.DOT);
            return false;
        }

        final TestingStationStatusReply reply = (TestingStationStatusReply) obj;

        if (reply.error != null) {
            showError("Testing station status error: " + reply.error + CoreConstants.DOT);
            Log.warning("Testing station status error: ", reply.error, CoreConstants.DOT);
            return false;
        }

        if (reply.status == null) {
            showError("Testing station status was incomplete.");
            Log.warning("Testing station status was incomplete.");
            return false;
        }

        this.status = reply.status;
        this.currentStudentId = reply.studentId;
        this.currentStudentName = reply.studentName;
        this.currentVersion = reply.version;

        return true;
    }

    /**
     * Perform the message exchange with the server to begin the exam.
     *
     * @return SUCCESS on successful completion; an error code on failure
     */
    private boolean getExam() {

        this.examSession = null;

        // Send the exam start request, await the response
        final GetExamRequest req = new GetExamRequest(this.currentStudentId, this.currentVersion, false);

        // FIXME: Get exam type a better way
        if (this.currentVersion.endsWith("FIN")) {
            req.examType = "F";
        } else if (this.currentVersion.startsWith("P")) {
            req.examType = "Q";
        } else if (this.currentVersion.endsWith("U")) {
            req.examType = "L";
        } else {
            req.examType = "U";
        }

        req.machineId = getMachineId();
        req.checkCoupons = this.checkCoupons;
        req.checkEligibility = this.checkEligibility;

        final AbstractReplyBase obj = doExchange(req, "GetExam", true);

        if (obj == null) {
            showError("Unable to read exam from the server.");
            Log.warning("Unable to read exam from the server.");
            doExamStartResult(ExamStartResultRequest.CANT_GET_EXAM);

            return false;
        } else if (!(obj instanceof GetExamReply)) {
            showError("Exam was " + obj.getClass().getName() + CoreConstants.DOT);
            Log.warning("Exam was ", obj.getClass().getName(), CoreConstants.DOT);
            doExamStartResult(ExamStartResultRequest.CANT_PARSE_EXAM);
            return false;
        }

        final GetExamReply reply = (GetExamReply) obj;

        if (reply.error != null) {
            showError("Exam generation error: " + reply.error + CoreConstants.DOT);
            Log.warning("Exam generation error: ", reply.error, CoreConstants.DOT);
            doExamStartResult(ExamStartResultRequest.CANT_GET_EXAM);
            return false;
        }

        final ExamObj exam = reply.presentedExam;
        this.examSession = new ExamSession(EExamSessionState.INTERACTING, exam);
        doExamStartResult(ExamStartResultRequest.EXAM_STARTED);

        return true;
    }

    /**
     * Perform the message exchange with the server to indicate we could or could not begin the exam.
     *
     * @param result the result of the attempt to start the exam
     */
    private void doExamStartResult(final int result) {

        final ExamStartResultRequest req;

        // Send the exam start request, await the response
        if (this.examSession == null) {
            req = new ExamStartResultRequest(result, this.currentVersion, null);
        } else {
            req = new ExamStartResultRequest(result, this.currentVersion, this.examSession.getExam().serialNumber);
        }

        req.machineId = getMachineId();

        final AbstractReplyBase obj = doExchange(req, "ExamStartResult", true);

        if (obj == null) {
            showError("Unable to read exam start results from the server.");
            Log.warning("Unable to read exam start results from the server.", null);
            return;
        } else if (!(obj instanceof ExamStartResultReply)) {
            showError("Exam start result was " + obj.getClass().getName() + CoreConstants.DOT);
            Log.warning("Exam start result was ", obj.getClass().getName(), CoreConstants.DOT);
            return;
        }

        final ExamStartResultReply reply = (ExamStartResultReply) obj;

        if (reply.error != null) {
            showError("Exam start error: " + reply.error + CoreConstants.DOT);
            Log.warning("Exam start error: ", reply.error, CoreConstants.DOT);
        }
    }

    /**
     * Writes the current exam state to disk.
     */
    @Override
    public void doCacheExamState() {

        // NOTE: This runs in the AWT Event thread.

        if (this.userHomeDir.exists()) {
            final ExamObj exam = this.examSession.getExam();

            // Store an update exam request as if we were submitting.
            final UpdateExamRequest req = new UpdateExamRequest(this.currentStudentId, exam.ref,
                    Long.valueOf(exam.realizationTime), exam.exportState(), true, true);
            req.machineId = getMachineId();

            final File f = new File(this.userHomeDir, "update-exam.xml");

            try {
                if (f.exists() && !f.delete()) {
                    Log.warning("Unable to delete existing update file.");
                }

                try (final FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                    fw.write(req.toXml());
                }
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        } else {
            Log.warning("Home directory does not exist");
        }
    }

    /**
     * Send the finalized exam to the server.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    private boolean doSendExam() {

        final ExamObj exam = this.examSession.getExam();

        // Send the exam update request, await the response
        final UpdateExamRequest req = new UpdateExamRequest(this.currentStudentId, exam.ref,
                Long.valueOf(exam.realizationTime), exam.exportState(), true, true);
        req.machineId = getMachineId();

        final AbstractReplyBase obj = doExchange(req, "UpdateExam", true);

        if (obj == null) {
            showError("Unable to read exam update results from the server.");
            Log.warning("Unable to read exam update results from the server.");
            return false;
        } else if (!(obj instanceof UpdateExamReply)) {
            showError("Exam update result was " + obj.getClass().getName() + CoreConstants.DOT);
            Log.warning("Exam update result was ", obj.getClass().getName(), CoreConstants.DOT);
            return false;
        }

        final UpdateExamReply reply = (UpdateExamReply) obj;

        if (reply.status != UpdateExamReply.SUCCESS) {
            showError("Server did not acknowledge completed exam properly.");
            Log.warning("Server did not acknowledge completed exam properly.");
            return false;
        }

        // Extract results data for display
        this.examGrades = reply.examGrades;
        this.examScores = reply.subtestScores;
        this.examError = reply.error;

        // Clear the exam cache after successful submission
        File f = new File(this.userHomeDir, "exam-in-progress.xml");

        try {
            f.delete();
        } catch (final Exception ex) {
            Log.fine(ex);
        }

        f = new File(this.userHomeDir, "update-exam.xml");

        try {
            f.delete();
        } catch (final Exception ex) {
            Log.fine(ex);
        }

        return true;
    }

    /**
     * Perform the message exchange with the server to reset the testing station.
     */
    private void reset() {

        showError("Resetting...");

        final TestingStationResetRequest request = new TestingStationResetRequest();
        request.machineId = getMachineId();
        doExchange(request, "TestingStationReset", true);
    }

    /**
     * Perform the message exchange with the server to reset the testing station, then waits for its state to change to
     * one of the ground states.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void resetAndWait() throws InterruptedException {

        reset();

        int count = 0;

        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (getTestingStationStatus()) {

                if (RawClientPc.STATUS_LOCKED.equals(this.status) //
                        || RawClientPc.STATUS_PAPER_ONLY.equals(this.status) //
                        || RawClientPc.STATUS_UNINITIALIZED.equals(this.status)) {

                    // Reached a ground state
                    break;
                }
            } else if (!getServerConnection().isOpen()) {
                this.status = RawClientPc.STATUS_ERROR;
                Thread.sleep(3000L);
                break;
            }

            // In this state, poll every 3 seconds.
            for (int i = 0; i < 6; i++) {
                this.desk.advance();
                Thread.sleep(500L);
            }

            count++;

            if (count == 20) { // 60 seconds in this state, so reset.
                reset();
                count = 0; // Re-request a reset in 60 more seconds.
            }
        }
    }

    /**
     * Process the "uninitialized" state. This state simply polls the server periodically for a new state.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runUninitialized() throws InterruptedException {

        // Reset all variables
        this.currentVersion = null;
        this.currentStudentName = null;
        this.currentStudentId = null;
        this.examSession = null;
        this.examPanel = null;
        this.examScores = null;
        this.examGrades = null;
        this.examError = null;
        this.grade = null;

        showError("Station not yet authorized");

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "This station is not yet authorized.", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.fine(ex);
        }

        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // In this state, poll only every 15 seconds.
            for (int i = 0; this.frame.isVisible() && i < 30; i++) {
                this.desk.advance();
                Thread.sleep(500L);
            }

            if (getTestingStationStatus()) {

                if (RawClientPc.STATUS_UNINITIALIZED.equals(this.status)) {

                    // No change - stay in this state.
                    break;
                } else if (RawClientPc.STATUS_LOCKED.equals(this.status)
                        || RawClientPc.STATUS_PAPER_ONLY.equals(this.status)
                        || RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)
                        || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {

                    // Leave this state for another state.
                    break;
                } else {
                    showError("Requesting a server-reset of this station...");
                    reset();
                }
            } else if (!getServerConnection().isOpen()) {
                showError("Connection to server failed");
                this.status = RawClientPc.STATUS_ERROR;
                Thread.sleep(3000L);
                break;
            }
        }
    }

    /**
     * Process the "paper exams only" state. This state simply polls the server periodically for a new state.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runPaperOnly() throws InterruptedException {

        // Reset all variables
        this.currentVersion = null;
        this.currentStudentName = null;
        this.currentStudentId = null;
        this.examSession = null;
        this.examPanel = null;
        this.examScores = null;
        this.examGrades = null;
        this.examError = null;
        this.grade = null;

        showError("Paper exams only");

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "Paper Exams Only, Please", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (getTestingStationStatus()) {

                if (!RawClientPc.STATUS_PAPER_ONLY.equals(this.status)) {

                    if (RawClientPc.STATUS_UNINITIALIZED.equals(this.status)
                            || RawClientPc.STATUS_LOCKED.equals(this.status)
                            || RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)
                            || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {

                        break;
                    }

                    showError("Requesting a server-reset of this station...");
                    reset();
                }
            } else if (!getServerConnection().isOpen()) {
                showError("Connection to server failed");
                this.status = RawClientPc.STATUS_ERROR;
                Thread.sleep(3000L);
                break;
            }

            // In this state, poll only every 15 seconds.
            for (int i = 0; this.frame.isVisible() && i < 30; i++) {
                this.desk.advance();
                Thread.sleep(500L);
            }
        }
    }

    /**
     * Process the "locked" state. This state simply polls the server periodically for a new state.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runLocked() throws InterruptedException {

        // Reset all variables
        this.currentVersion = null;
        this.currentStudentName = null;
        this.currentStudentId = null;
        this.examSession = null;
        this.examPanel = null;
        this.examScores = null;
        this.examGrades = null;
        this.examError = null;
        this.grade = null;

        showError("Station locked");

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "This station is locked", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (getTestingStationStatus()) {

                if (!RawClientPc.STATUS_LOCKED.equals(this.status)) {

                    if (RawClientPc.STATUS_UNINITIALIZED.equals(this.status)
                            || RawClientPc.STATUS_PAPER_ONLY.equals(this.status)
                            || RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)
                            || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {

                        // Leave this state for another state.
                        break;
                    }

                    showError("Requesting a server-reset of this station...");
                    reset();
                }
            } else if (!getServerConnection().isOpen()) {
                showError("Connection to server failed");
                this.status = RawClientPc.STATUS_ERROR;
                break;
            }

            // In this state, poll every 5 seconds.
            for (int i = 0; this.frame.isVisible() && i < 10; i++) {
                this.desk.advance();
                Thread.sleep(500L);
            }
        }
    }

    /**
     * Process the "await student" state. This state requests student login as confirmation they have arrived at the
     * seat.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runAwaitStudent() throws InterruptedException {

        showError("Awaiting student login");

        // Before doing anything visible, test that we have enough information
        if (this.currentStudentId == null) {
            Log.warning("Invalid login: no current student ID");
            showError("Awaiting student login - no student ID");
            runInvalidAwaitStudent();
        } else if (this.currentVersion == null) {
            Log.warning("Invalid login: no current version");
            showError("Awaiting student login - no exam version");
            runInvalidAwaitStudent();
        } else {
            runAwaitIndividualStudent();
        }
    }

    /**
     * Await the login of an identified student, whose student ID is known.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runAwaitIndividualStudent() throws InterruptedException {

        showError("Awaiting individual student login");

        StatusDisplay theStatus = new StatusDisplay(this.desk, this.colors.get(this.status), "Student Login",
                this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        final StartExamPanel dialog = new StartExamPanel(this.desk, this.currentStudentId,
                this.colors.get(this.status));
        final Runnable adder = new PanelAdder(this.desk, dialog);

        try {
            SwingUtilities.invokeAndWait(adder);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        int timer = 3 * 60 * 4; // 3 minute timeout
        final TimeoutUpdater updater = new TimeoutUpdater(dialog.getProgressBar(), timer);

        while (timer > 0 && dialog.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // Server can cancel the checkin, so poll for state.
            if (getTestingStationStatus()) {

                if (RawClientPc.STATUS_UNINITIALIZED.equals(this.status)
                        || RawClientPc.STATUS_LOCKED.equals(this.status)
                        || RawClientPc.STATUS_PAPER_ONLY.equals(this.status)) {

                    // Leave this state for another state.
                    final Runnable remover = new PanelRemover(this.desk, dialog);

                    try {
                        SwingUtilities.invokeAndWait(remover);
                    } catch (final InvocationTargetException ex) {
                        Log.warning(ex);
                    }

                    return;
                }

                if (!(RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)
                        || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status))) {

                    // Leave this state for another state.
                    final Runnable remover = new PanelRemover(this.desk, dialog);

                    try {
                        SwingUtilities.invokeAndWait(remover);
                    } catch (final InvocationTargetException ex) {
                        Log.warning(ex);
                    }

                    showError("Requesting a server-reset of this station...");
                    resetAndWait();

                    return;
                }
            } else if (!getServerConnection().isOpen()) {
                showError("Connection to server failed");
                Log.warning("Server connection closed during login");
                this.status = RawClientPc.STATUS_ERROR;

                final Runnable remover = new PanelRemover(this.desk, dialog);

                try {
                    SwingUtilities.invokeAndWait(remover);
                } catch (final InvocationTargetException ex) {
                    Log.warning(ex);
                }
                Thread.sleep(3000L);

                return;
            }

            // In this state, poll every 5 seconds, also watching for dialog to get submitted
            if (RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status)
                    || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {

                for (int i = 0; dialog.isVisible() && i < 20 && timer > 0; i++) {

                    // Quarter seconds between updates
                    Thread.sleep(250L);
                    timer--;
                    updater.setValue(timer);

                    try {
                        SwingUtilities.invokeAndWait(updater);
                    } catch (final InvocationTargetException ex) {
                        Log.warning(ex);
                    }
                }
            } else {
                Thread.sleep(5000L);
            }
        }

        final Runnable remover = new PanelRemover(this.desk, dialog);

        try {
            SwingUtilities.invokeAndWait(remover);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        // If login fails, reset and wait.
        if (!dialog.succeeded()) {
            showError("Student login failed");
            runLoginFailed();
            return;
        }

        // Login succeeded, so retrieve the exam
        theStatus = new StatusDisplay(this.desk, this.colors.get(this.status), "Generating Exam",
                this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        showError("Getting exam");

        if (!getExam()) {
            showError("Failed to get exam");
            resetAndWait();
            return;
        }

        showError("Exam retrieved");

        // Wait for the state to change to TAKING_EXAM
        while (this.frame.isVisible()) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (getTestingStationStatus()) {

                if (RawClientPc.STATUS_TAKING_EXAM.equals(this.status)
                        || RawClientPc.STATUS_UNINITIALIZED.equals(this.status)
                        || RawClientPc.STATUS_LOCKED.equals(this.status)
                        || RawClientPc.STATUS_PAPER_ONLY.equals(this.status)) {
                    break;
                }

                if (!(RawClientPc.STATUS_AWAIT_STUDENT.equals(this.status))
                        || RawClientPc.STATUS_LOGIN_NOCHECK.equals(this.status)) {
                    showError("Requesting a server-reset of this station...");
                    resetAndWait();
                    break;
                }

            } else if (!getServerConnection().isOpen()) {
                showError("Connection to server failed");
                this.status = RawClientPc.STATUS_ERROR;
                Thread.sleep(3000L);
                break;
            }

            // In this state, poll every 3 seconds.
            for (int i = 0; this.frame.isVisible() && i < 6; i++) {
                Thread.sleep(500L);
            }
        }
    }

    /**
     * Process the "login failed" state. This state resets the station and polls the server periodically for a new
     * state.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runLoginFailed() throws InterruptedException {

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "Invalid Login", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        // Make sure the message remains visible for a 5 seconds.
        for (int i = 0; this.frame.isVisible() && i < 10; i++) {
            this.desk.advance();
            Thread.sleep(500L);
        }

        // Reset and wait for state to change.
        resetAndWait();
    }

    /**
     * Process an invalid state in which AWAIT STUDENT has been indicated but no student & version information has been
     * provided.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runInvalidAwaitStudent() throws InterruptedException {

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "Invalid Exam Checkin.", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        // Reset and wait for state to change.
        resetAndWait();
    }

    /**
     * Present the exam to the student.
     *
     * @throws InterruptedException if the process is interrupted
     */
    private void runExam() throws InterruptedException {

        showError("Caching exam for emergency recovery");

        final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(this.status),
                "Exam Starting", this.testingCenterName, this.stationNumber);

        try {
            SwingUtilities.invokeAndWait(theStatus);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        final ExamObj exam = this.examSession.getExam();

        // Cache the exam in case it crashes.
        if (this.userHomeDir.exists()) {
            final File file = new File(this.userHomeDir, "exam-in-progress.xml");

            try {
                if (file.exists()) {
                    if (!file.delete()) {
                        Log.warning("Unable to delete existing exam cache file.");
                    }
                }

                try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                    fw.write(exam.toXmlString(0));
                }
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

        if ("PPPPP".equals(exam.examVersion) || "MPTPU".equals(exam.examVersion)) {
            showError("Showing pre-exam survey");
            doSurvey();
        }

        showError("Showing honor pledge");
        JOptionPane.showOptionDialog(this.desk,
                "I will not give, receive, or use any unauthorized assistance.", "HONOR PLEDGE",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"I Agree", "I Abstain"},
                null);
        showError("Honor pledge answered");

        this.examGrades = null;
        this.examScores = null;
        this.examError = null;
        this.grade = null;

        final Properties skin = new DefaultSkin();

        final Dimension deskSize = this.desk.getSize();
        final ExamPanelBuilder builder = new ExamPanelBuilder(skin, deskSize, this.currentStudentName, this,
                this.examSession);

        try {
            SwingUtilities.invokeAndWait(builder);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        this.examPanel = builder.getPanel();
        this.examPanelWrapper = builder.getWrapper();

        showError("Displaying exam");

        final Runnable displayer = new ExamPanelDisplay(this.desk, this.examPanelWrapper);

        try {
            SwingUtilities.invokeAndWait(displayer);
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }

        showError("Exam started");
        exam.presentationTime = System.currentTimeMillis();

        // Now we wait for grading to be indicated, while periodically polling the server. NOTE:
        // the only state changes we allow in this state are a force submit or cancel exam! Other
        // server issues are ignored.

        while (this.grade == null) {

            // If we receive an "interrupted exception" in this condition, force immediate
            // submission
            if (Thread.currentThread().isInterrupted()) {
                this.status = RawClientPc.STATUS_FORCE_SUBMIT;
                this.grade = "Interrupt forced submission";
                break;
            }

            if (getTestingStationStatus()) {

                if (RawClientPc.STATUS_FORCE_SUBMIT.equals(this.status)) {
                    Log.warning("Force submit received");
                    showError("Exam submission has been forced");
                    this.grade = "Checkout forced submission";
                    break;
                } else if (RawClientPc.STATUS_CANCEL_EXAM.equals(this.status)) {
                    Log.warning("Cancel exam received");
                    showError("Exam was canceled on the server");
                    this.grade = "Exam has been cancelled";
                    break;
                }
            } else {
                Log.warning("Get testing station status failed");

                if (!getServerConnection().isOpen()) {
                    showError("Connection to server failed - retrying");

                    Thread.sleep(3000L);
                    // Connection is closed, so try to reconnect, ignoring errors
                    connectToServer();
                }
            }

            // In this state, poll every 5 seconds.
            try {
                Thread.sleep(5000L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
                this.status = RawClientPc.STATUS_FORCE_SUBMIT;
                this.grade = "Interrupt forced submission";

                break;
            }
        }

        // If the exam was canceled, indicate as much and then reset.
        if (RawClientPc.STATUS_CANCEL_EXAM.equals(this.status)) {
            showError("Canceling exam");

            // Remove the exam panel and submit the exam.
            final Runnable clear = new ClearExam(this.desk, this.examPanelWrapper,
                    this.colors.get(this.status), "Canceling Exam...");

            try {
                SwingUtilities.invokeAndWait(clear);
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            } catch (final InterruptedException ex) {
                Log.fine("Interrupted while clearing exam panel", ex);
            }

            // Make sure the message remains visible for a 5 seconds.
            for (int i = 0; i < 10; i++) {
                this.desk.advance();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    // If interrupted here, continue canceling exam
                    Log.warning(ex);
                    break;
                }
            }

            // Remove the cached files
            File f = new File(this.userHomeDir, "exam-in-progress.xml");

            try {
                f.delete();
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            f = new File(this.userHomeDir, "update-exam.xml");

            try {
                f.delete();
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            this.frame.setVisible(false);
            this.frame.dispose();
            showError("Exam canceled");
        } else {

            final String txt = exam.examVersion.startsWith("30") ? "Submitting Quiz..." : "Submitting Exam...";
            showError(txt);

            // Remove the exam panel and submit the exam.
            final Runnable clear = new ClearExam(this.desk, this.examPanelWrapper, this.colors.get(this.status), txt);

            try {
                SwingUtilities.invokeAndWait(clear);
            } catch (final InvocationTargetException | InterruptedException ex) {
                Log.warning(ex);
            }

            while (this.frame.isVisible()) {

                showError("Sending data to server");

                if (doSendExam()) {

                    // An interrupt here is fine - exam is already submitted
                    showError("Showing results");
                    runExamResults();
                    showError("Done showing results");

                    return;
                }

                // An interrupt here should be ignored - we keep trying to submit the exam
                try {
                    Thread.sleep(5000L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                }
            }
        }
    }

    /**
     * Poll the server for the student's status regarding the survey questions that need to be answered.
     */
    private void doSurvey() {

        final ExamObj exam = this.examSession.getExam();

        final SurveyStatusRequest request = new SurveyStatusRequest(this.currentStudentId, exam.examVersion);
        request.machineId = getMachineId();

        AbstractReplyBase rep = doExchange(request, "Survey status", true);

        final SurveyStatusReply reply;

        if (rep == null) {
            Log.warning("Unable to connect to server.");
            return;
        } else if (rep instanceof SurveyStatusReply) {
            reply = (SurveyStatusReply) rep;
        } else {
            Log.warning("Invalid SurveyStatusReply: ", rep);
            return;
        }

        if (reply.error != null) {
            Log.warning("Unable to get survey status: ", reply.error);
            return;
        }

        // Create questions list
        final String[] questions = new String[21];

        if (reply.questions != null) {

            // Find the highest question number
            int highest = 0;

            for (final RawSurveyqa question : reply.questions) {
                if (question.surveyNbr.intValue() > highest) {
                    highest = question.surveyNbr.intValue();
                }
            }

            for (int i = 0; i < highest; i++) {
                for (final RawSurveyqa question : reply.questions) {
                    if (question != null && question.surveyNbr != null && question.surveyNbr.intValue() == i + 1) {
                        questions[i] = question.questionDesc;

                        break;
                    }
                }
            }
        }

        // Create answers list and populate any the student already has entered
        final String[] answers = new String[21];

        if (reply.answers != null) {

            // Find the highest answer number
            int highest = 0;

            for (final RawStsurveyqa answer : reply.answers) {
                if (answer.surveyNbr.intValue() > highest) {
                    highest = answer.surveyNbr.intValue();
                }
            }

            for (int i = 0; i < highest; i++) {
                for (final RawStsurveyqa answer : reply.answers) {
                    if (answer != null && answer.surveyNbr != null && answer.surveyNbr.intValue() == i + 1) {
                        answers[i] = answer.stuAnswer;

                        break;
                    }
                }
            }
        }

        // Present the dialog
        if (Survey.doSurvey(this.frame, questions, answers, "the CSU Math Challenge Exam")
                == Wizard.FINISH_RETURN_CODE) {

            // Send updated responses to the server, leaving the connection open
            final SurveySubmitRequest request2 = new SurveySubmitRequest(this.currentStudentId, exam.examVersion,
                    answers);
            request2.machineId = getMachineId();

            rep = doExchange(request2, "Survey Submit", true);

            final SurveySubmitReply reply2;

            if (rep == null) {
                Log.warning("Unable to connect to server.");
                return;
            } else if (rep instanceof SurveySubmitReply) {
                reply2 = (SurveySubmitReply) rep;
            } else {
                Log.warning("Invalid SurveySubmitReply: ", rep);
                return;
            }

            if (reply2.error != null) {
                Log.warning("Error in reply to survey submit: ", reply2.error);
            }
        }
    }

    /**
     * Display the results of the exam to the user.
     */
    private void runExamResults() {

        if (this.examScores != null || this.examGrades != null || this.examError != null) {

            showError("Showing exam results");

            final Runnable theStatus = new StatusDisplay(this.desk, this.colors.get(RawClientPc.STATUS_EXAM_RESULTS),
                    "Exam Results", this.testingCenterName, this.stationNumber);

            try {
                SwingUtilities.invokeAndWait(theStatus);
            } catch (final Exception ex) {
                Log.warning("Failed to show exam results status", ex);
            }

            final FeedbackPanel panel = new FeedbackPanel(this.desk, this.examScores, this.examError,
                    this.colors.get(RawClientPc.STATUS_EXAM_RESULTS));

            final Runnable adder = new PanelAdder(this.desk, panel);

            try {
                SwingUtilities.invokeAndWait(adder);
            } catch (final Exception ex) {
                Log.warning("Failed to show exam results panel", ex);
            }

            showError("Showing exam results...");
            panel.waitForDone(30000L);
            showError("Clearing exam results");

            final Runnable remover = new PanelRemover(this.desk, panel);

            try {
                SwingUtilities.invokeAndWait(remover);
            } catch (final Exception ex) {
                Log.warning("Failed to remove exam results panel", ex);
            }
        } else {
            showError("Exam results with missing data");
            Log.warning("Exam results called with missing data.", null);
        }

        showError("Done showing exam results");
        this.frame.setVisible(false);
        this.frame.dispose();
    }

    /**
     * Display a small error message on the screen.
     *
     * @param msg the error message to display
     */
    private void showError(final String msg) {

        ErrorDisplay err = null;

        // Only do the GUI update if the message has changed.
        if (this.errorDisplay == null) {
            if (msg != null) {
                err = new ErrorDisplay(this.statusLabel,
                        msg + CoreConstants.SPC + TemporalUtils.FMT_MDY.format(LocalDateTime.now()));

            }
        } else if (!this.errorDisplay.equals(msg)) {
            err = new ErrorDisplay(this.statusLabel,
                    msg + CoreConstants.SPC + TemporalUtils.FMT_MDY.format(LocalDateTime.now()));
        }

        if (err != null) {
            this.errorDisplay = msg;
            SwingUtilities.invokeLater(err);
        }

        Log.info(msg);
    }

    /**
     * Select a problem to present in the current problem panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    @Override
    public void pickProblem(final int sectionIndex, final int problemIndex) {

        // Empty
    }

    /**
     * Handler for actions generated by application panels.
     *
     * @param e the action event generated by the panel
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.examSession == null) {
            return;
        }

        final String cmd = e.getActionCommand();
        int numAnswered = 0;
        int numProblems = 0;

        switch (cmd) {
            case "Larger" -> this.examPanel.larger();
            case "Smaller" -> this.examPanel.smaller();
            case "Yes" -> {
                this.confirm.setVisible(false);
                this.examPanelWrapper.getDesktopPane().remove(this.confirm);
                this.confirm.dispose();
                this.confirm = null;

                this.grade = "Exam submitted by student.";
            }
            case "No" -> {
                this.confirm.setVisible(false);
                this.examPanelWrapper.getDesktopPane().remove(this.confirm);
                this.confirm.dispose();
                this.confirm = null;

                this.examPanel.setEnabled(true);
            }
            case null, default -> {
                final ExamObj exam = this.examSession.getExam();

                if ("color-white".equals(cmd)) {
                    exam.setBackgroundColor("white", ColorNames.getColor("white"));
                    this.examPanel.updateColor();
                } else if ("color-gold".equals(cmd)) {
                    exam.setBackgroundColor("gold", ColorNames.getColor("gold"));
                    this.examPanel.updateColor();
                } else if ("color-purple".equals(cmd)) {
                    exam.setBackgroundColor("MediumPurple", ColorNames.getColor("MediumPurple"));
                    this.examPanel.updateColor();
                } else if ("color-blue".equals(cmd)) {
                    exam.setBackgroundColor("MediumTurquoise", ColorNames.getColor("MediumTurquoise"));
                    this.examPanel.updateColor();
                } else if ("Grade".equals(cmd)) {
                    this.examPanel.setEnabled(false);

                    // Test whether all questions have been answered. If not, warn the student.
                    try {
                        final int numSections = exam.getNumSections();
                        for (int i = 0; i < numSections; i++) {

                            final ExamSection section = exam.getSection(i);

                            if (section != null) {

                                for (int j = 0; j < section.getNumProblems(); j++) {
                                    numProblems++;

                                    final ExamProblem presented = section.getPresentedProblem(j);

                                    if (presented != null) {

                                        if (presented.getSelectedProblem() != null) {

                                            if (presented.getSelectedProblem().isAnswered()) {
                                                numAnswered++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (final Exception ex) {
                        Log.warning("Exception while grading", ex);
                    }

                    this.confirm = new AreYouFinished(numProblems, numAnswered, this);
                    this.examPanelWrapper.getDesktopPane().add(this.confirm);
                    this.examPanelWrapper.getDesktopPane().setLayer(this.confirm, JLayeredPane.POPUP_LAYER.intValue());
                    this.confirm.setVisible(true);
                    this.confirm.centerInDesktop();
                }
            }
        }
    }

    /**
     * A method called when a timer expires.
     */
    @Override
    public void timerExpired() {

        Log.warning("Exam timed out - started at ",
                new Date(this.examSession.getExam().presentationTime).toString(), ", expired at ",
                new Date().toString());
        this.grade = "Exam timed out - submitting.";
    }

    /**
     * Override the base class method to do a server message exchange, so we can use the error logging function on any
     * failure.
     *
     * @param request   the request to send to the server
     * @param name      the name of the type of transaction being performed
     * @param leaveOpen {@code true} to leave the connection open after the exchange; {@code false} to close the
     *                  connection
     * @return the server's reply, or null on any error
     */
    @Override
    public AbstractReplyBase doExchange(final AbstractRequestBase request,
                                        final String name, final boolean leaveOpen) {

        AbstractReplyBase reply = null;
        final boolean isException = request instanceof ExceptionSubmissionRequest;

        // Open the connection if needed
        if (!getServerConnection().isOpen()) {
            if (connectToServer() != SUCCESS) {
                if (!isException) {
                    Log.warning("doExchange was unable to connect to server");
                }
                return null;
            }
        }

        // Perform the exchange of messages
        if (getServerConnection().writeObject(request.toXml())) {
            final char[] xml = getServerConnection().readObject(name + " reply");

            if (xml != null) {
//                Log.info("Response to ", request.getClass().getName(), " is length ", Integer.toString(xml.length));

                final AbstractMessageBase msg = MessageFactory.parseMessage(xml);

                if (msg == null) {
                    if (!isException) {
                        Log.warning("doExchange received null");
                    }
                } else if (msg instanceof AbstractReplyBase) {
                    reply = (AbstractReplyBase) msg;
                } else {
                    if (!isException) {
                        Log.warning("doExchange received ", msg.getClass().getName());
                    }
                }
            } else if (!isException) {
                Log.warning("doExchange was unable to read reply to ", request.getClass().getName());
            }
        } else if (!isException) {
            Log.warning("doExchange was unable to write ", request.getClass().getName());
        }

        // Close the connection if we were not directed to leave it open.
        if (!leaveOpen) {
            disconnectFromServer();
        }

        return reply;
    }

    /**
     * Main method that launches the proctored testing application.
     *
     * @param args command-line arguments: if "windowed" appears in the arguments, the application will run in a window;
     *             if "dev" appears, the development host is used
     */
    public static void main(final String... args) {

        ChangeUI.changeUI();

        boolean fullScreen = true;
        boolean useDev = false;
        for (final String arg : args) {
            if ("windowed".equals(arg)) {
                fullScreen = false;
            } else if ("dev".equals(arg)) {
                useDev = true;
            }
        }

        final String host = useDev ? Contexts.TESTINGDEV_HOST : Contexts.TESTING_HOST;

        try {
            final TestStationApp app = new TestStationApp(host, 80, SessionCache.TEST_SESSION_ID);

            try {
                app.execute(fullScreen);
            } catch (final Exception ex) {
                JOptionPane.showMessageDialog(null, "Testing station app crashed: " + ex.getMessage());
                Log.warning(ex);
            }
        } catch (final Exception ex) {
            Log.severe(ex);
            JOptionPane.showMessageDialog(null, "Testing station constructor crashed: " + ex.getMessage());
        }

        System.exit(0);
    }
}

/**
 * Runnable class to be called in the AWT dispatcher thread to construct the blocking window. The window consists of a
 * full-screen {@code JFrame} that contains a {@code BackgroundPane}.
 */
final class BlockingWindowBuilder implements Runnable {

    /** {@code true} to build screen in full-screen mode. */
    private final boolean full;

    /** The frame in which the background desktop pane will live. */
    private JFrame builderFrame;

    /** The desktop background. */
    private BackgroundPane builderDesk;

    /** The desktop backgrounds. */
    private JLabel statusLabel;

    /**
     * Constructs a new {@code BlockingWindowBuilder}.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
     */
    BlockingWindowBuilder(final boolean fullScreen) {

        this.full = fullScreen;
    }

    /**
     * Gets the generated frame.
     *
     * @return the frame
     */
    public JFrame getFrame() {

        return this.builderFrame;
    }

    /**
     * Gets the background panel.
     *
     * @return the background panel
     */
    public BackgroundPane getDesk() {

        return this.builderDesk;
    }

    /**
     * Gets the status label.
     *
     * @return the status label
     */
    JLabel getStatusLabel() {

        return this.statusLabel;
    }

    /**
     * Constructs the blocking window.
     */
    @Override
    public void run() {

        final Dimension screen = this.full ? Toolkit.getDefaultToolkit().getScreenSize() : new Dimension(1280, 720);

        this.builderFrame = new JFrame("Testing Station");
        this.builderFrame.setBackground(Color.BLACK);
        this.builderFrame.setUndecorated(this.full);
        this.builderFrame.setFocusableWindowState(true);

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(screen);
        content.setBackground(Color.black);
        this.builderFrame.setContentPane(content);

        this.statusLabel = new JLabel("Creating blocking window");
        this.statusLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        this.statusLabel.setBackground(Color.BLACK);
        this.statusLabel.setForeground(new Color(80, 80, 80));
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(this.statusLabel, BorderLayout.PAGE_END);

        this.builderDesk = new BackgroundPane();
        this.builderDesk.setBackground(Color.BLACK);
        content.add(this.builderDesk, BorderLayout.CENTER);

        this.builderFrame.pack();
        this.builderFrame.setLocation(0, 0);
        this.builderFrame.setVisible(true);
        this.builderFrame.toFront();
        this.builderFrame.requestFocus();
    }
}

/**
 * Runnable class to set the background color and status string, but leave the testing center name and station number as
 * they are. This is intended to be run in the AWT dispatcher thread.
 */
final class StatusDisplay implements Runnable {

    /** The background pane to update. */
    private final BackgroundPane background;

    /** The color to set the background to. */
    private final Color color;

    /** The status text to set. */
    private final String statusText;

    /** The testing center name text to set. */
    private final String centerName;

    /** The station number text to set. */
    private final String stationNum;

    /**
     * Constructs a new {@code StatusDisplay}.
     *
     * @param theBackground        the background pane to update
     * @param theColor             the color to set the background to
     * @param theStatus            the status text to set
     * @param theTestingCenterName the testing center name text to set
     * @param theStationNumber     the station number text to set
     */
    StatusDisplay(final BackgroundPane theBackground, final Color theColor, final String theStatus,
                  final String theTestingCenterName, final String theStationNumber) {

        this.background = theBackground;
        this.color = theColor == null ? Color.white : theColor;
        this.statusText = theStatus;
        this.centerName = theTestingCenterName;
        this.stationNum = theStationNumber;
    }

    /**
     * Performs the updates.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.background.setBackground(this.color);
        this.background.setStatus(this.statusText);
        this.background.setCenterName(this.centerName);
        this.background.setStationNumber(this.stationNum);

        this.background.invalidate();
        this.background.repaint();
    }
}

/**
 * Sets the error string. This is intended to be run in the AWT dispatcher thread.
 */
final class ErrorDisplay implements Runnable {

    /** The label to update. */
    private final JLabel label;

    /** The status text to set. */
    private final String errorMsg;

    /**
     * Constructs a new {@code ErrorDisplay}.
     *
     * @param theLabel the label pane to update
     * @param theError the error text to set
     */
    ErrorDisplay(final JLabel theLabel, final String theError) {

        this.label = theLabel;
        this.errorMsg = theError == null ? CoreConstants.SPC : theError;
    }

    /**
     * Performs the update.
     */
    @Override
    public void run() {

        this.label.setText(this.errorMsg);
    }
}

/**
 * Adds a panel to the desktop. This is intended to be run in the AWT dispatcher thread.
 */
final class PanelAdder implements Runnable {

    /** The background pane to update. */
    private final BackgroundPane background;

    /** The panel to be added to the desktop. */
    private final JInternalFrame panel;

    /**
     * Constructs a new {@code PanelAdder}.
     *
     * @param theBackground the background pane to update
     * @param thePanel      the panel to add
     */
    PanelAdder(final BackgroundPane theBackground, final JInternalFrame thePanel) {

        this.background = theBackground;
        this.panel = thePanel;
    }

    /**
     * Performs the addition.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.background.add(this.panel);
        this.panel.setFocusable(true);
        this.panel.setVisible(true);
        this.panel.toFront();
        this.panel.requestFocus();
        try {
            this.panel.setSelected(true);
        } catch (final PropertyVetoException ex) {
            Log.warning("Selection was vetoed", ex);
        }

        if (this.panel instanceof final StartExamPanel startPanel) {
            startPanel.focusInput();
        }
    }
}

/**
 * Removes a panel from the desktop. This is intended to be run in the AWT dispatcher thread.
 */
final class PanelRemover implements Runnable {

    /** The background pane to update. */
    private final BackgroundPane background;

    /** The panel to be removed from the desktop. */
    private final JInternalFrame panel;

    /**
     * Constructs a new {@code PanelRemover}.
     *
     * @param theBackground the background pane to update
     * @param thePanel      the panel to remove
     */
    PanelRemover(final BackgroundPane theBackground, final JInternalFrame thePanel) {

        this.background = theBackground;
        this.panel = thePanel;
    }

    /**
     * Performs the removal.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.background.remove(this.panel);
        this.panel.setVisible(false);
        this.panel.dispose();
    }
}

/**
 * Creates an exam panel. This is intended to be run in the AWT dispatcher thread.
 */
final class ExamPanelBuilder implements Runnable {

    /** The skin. */
    private final Properties skin;

    /** The current student name. */
    private final String currentStudentName;

    /** The owning app. */
    private final TestStationApp owner;

    /** The exam session. */
    private final ExamSession examSession;

    /** The generated panel. */
    private ExamPanel panel;

    /** The generated panel wrapper. */
    private ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code ExamPanelBuilder}.
     *
     * @param theSkin           the skin
     * @param theContainerSize  the container's size
     * @param theCurStudentName the current student name
     * @param theOwner          the owning app
     * @param theExamSession    the exam session
     */
    ExamPanelBuilder(final Properties theSkin, final Dimension theContainerSize, final String theCurStudentName,
                     final TestStationApp theOwner, final ExamSession theExamSession) {


        Log.info("Container size is ", theContainerSize);
        this.skin = theSkin;
        this.currentStudentName = theCurStudentName;
        this.owner = theOwner;
        this.examSession = theExamSession;
    }

    /**
     * Thread method to construct the panel in the AWT event thread.
     */
    @Override
    public void run() {

        this.panel = new ExamPanel(this.owner, this.skin, this.currentStudentName, this.examSession, false, false,
                null, null);
        this.panel.addActionListener(this.owner);
        this.wrapper = new ExamPanelWrapper(this.panel);
    }

    /**
     * Gets the generated panel.
     *
     * @return the panel
     */
    public ExamPanel getPanel() {

        return this.panel;
    }

    /**
     * Gets the generated panel wrapper.
     *
     * @return the panel wrapper
     */
    public ExamPanelWrapper getWrapper() {

        return this.wrapper;
    }
}

/**
 * Installs an exam panel as a full-screen display in the desktop. This is intended to be run in the AWT dispatcher
 * thread.
 */
final class ExamPanelDisplay implements Runnable {

    /** The background pane to update. */
    private final BackgroundPane background;

    /** The exam panel wrapper to be displayed. */
    private final ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code ExamPanelDisplay}.
     *
     * @param theBackground the desktop window to add the exam to
     * @param theWrapper    the exam panel wrapper to be displayed
     */
    ExamPanelDisplay(final BackgroundPane theBackground, final ExamPanelWrapper theWrapper) {

        this.background = theBackground;
        this.wrapper = theWrapper;
    }

    /**
     * Adds the panel to the desktop.
     */
    @Override
    public void run() {

        final ExamPanel panel = this.wrapper.getContent();

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.background.add(this.wrapper);
        this.wrapper.makeFullscreen();
        this.wrapper.setSize(this.wrapper.getPreferredSize());
        this.wrapper.buildUI();

        if (panel.getExamSession().getExam().instructions == null) {
            panel.pickProblem(0, 0);
        } else {
            panel.showInstructions();
        }

        this.wrapper.pack();
        this.wrapper.show();

        this.wrapper.requestFocus();
        this.wrapper.requestFocusInWindow();
    }
}

/**
 * Removes the exam window from the desktop after the exam. This is intended to be run in the AWT dispatcher thread.
 */
final class ClearExam implements Runnable {

    /** The background pane to update. */
    private final BackgroundPane background;

    /** The exam panel to be displayed. */
    private ExamPanelWrapper wrapper;

    /** The background color to set after the exam. */
    private final Color color;

    /** The status text to display. */
    private final String text;

    /**
     * Constructs a new {@code ClearExam}.
     *
     * @param theBackground the desktop window to add the exam to
     * @param theWrapper    the exam panel wrapper to be displayed
     * @param theColor      the background color to set after the exam
     * @param theText       the status text to display
     */
    ClearExam(final BackgroundPane theBackground, final ExamPanelWrapper theWrapper,
              final Color theColor, final String theText) {

        this.background = theBackground;
        this.wrapper = theWrapper;
        this.color = theColor;
        this.text = theText;
    }

    /**
     * Adds the panel to the desktop.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Clear the exam panel from the application desktop
        this.wrapper.setVisible(false);
        this.background.remove(this.wrapper);
        this.wrapper.dispose();
        this.wrapper = null;

        // Submit the exam, and set the state in case the server does not.
        this.background.setBackground(this.color);
        this.background.setStatus(this.text);
        this.background.invalidate();
        this.background.repaint();
    }
}

/**
 * Updates the login timeout display.
 */
final class TimeoutUpdater implements Runnable {

    /** The progress bar to be updated. */
    private final JProgressBar progressBar;

    /** The maximum value to set. */
    private final int max;

    /** The value to be set. */
    private int value;

    /**
     * Constructs a new {@code TimeoutUpdater}.
     *
     * @param theProgressBar the progress bar to be updated
     * @param theMax         the maximum value to set
     */
    TimeoutUpdater(final JProgressBar theProgressBar, final int theMax) {

        this.progressBar = theProgressBar;
        this.max = theMax;
    }

    /**
     * Sets the value to which the progress bar will be updated on the next invocation of the {@code run} method.
     *
     * @param theValue the value to which to set the progress bar
     */
    public void setValue(final int theValue) {
        synchronized (this) {

            this.value = theValue;
        }
    }

    /**
     * Updates the progress bar.
     */
    @Override
    public void run() {
        synchronized (this) {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            this.progressBar.setMaximum(this.max);
            this.progressBar.setValue(this.value);
        }
    }
}

/**
 * A class that contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -569310176568552327L;

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        final String[][] contents = {//
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-title-show-answers", "$EXAM_TITLE Answers"},
                {"top-bar-username", "Logged In As: $USERNAME"},
                {"top-bar-username-practice", "Logged In As: $USERNAME"},
                {"top-bar-username-zero-req", "Logged In As: $USERNAME"},
                {"top-bar-username-one-section", "Logged In As: $USERNAME"},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray80"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "1"},
                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "14"},
                {"top-bar-username-style", "plain"},
                {"top-bar-username-color", "black"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},
                {"top-bar-clock-format", "Current Time: HH:MM"},
                {"top-bar-clock-font", "SANS"},
                {"top-bar-clock-size", "14"},
                {"top-bar-clock-style", "plain"},
                {"top-bar-clock-color", "black"},
                {"top-bar-clock-x", "0.5"},
                {"top-bar-clock-y", "53"},
                {"top-bar-clock-alignment", "center"},
                {"top-bar-timer-format", "Time Remaining: HH:MM:SS"},
                {"top-bar-timer-font", "SANS"},
                {"top-bar-timer-size", "14"},
                {"top-bar-timer-style", "plain"},
                {"top-bar-timer-color", "black"},
                {"top-bar-timer-x", "0.99"},
                {"top-bar-timer-y", "53"},
                {"top-bar-timer-alignment", "right"},
                {"top-bar-show-sections", "false"},
                {"top-bar-show-sections-if-one", "false"},

                {"bottom-bar-background-color", "alice blue"},
                {"bottom-bar-button-font", "SANS"},
                {"bottom-bar-button-size", "20"},
                {"bottom-bar-button-style", "plain"},
                {"bottom-bar-label-font", "SANS"},
                {"bottom-bar-label-size", "16"},
                {"bottom-bar-label-style", "plain"},
                {"bottom-bar-border-color", "steel blue"},
                {"bottom-bar-border-size", "1"},
                {"bottom-bar-border-inset", "1"},
                {"bottom-bar-padding-size", "0"},
                {"bottom-bar-lbl", "I am finished.  Submit the exam for grading."},
                {"bottom-bar-lbl-show-answers", "Close"},
                {"bottom-bar-lbl-practice", "Show the Answers"},

                {"show-problem-list", "true"},
                {"show-calculator", "true"},
                {"run-timer", "true"},};

        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}
