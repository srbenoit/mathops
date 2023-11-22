package dev.mathops.app.placement;

import dev.mathops.app.ClientBase;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.exam.ExamPanel;
import dev.mathops.app.exam.ExamPanelWrapper;
import dev.mathops.app.placement.results.Results;
import dev.mathops.app.placement.survey.Survey;
import dev.mathops.app.simplewizard.Wizard;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.db.Contexts;
import dev.mathops.db.rawrecord.RawStsurveyqa;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.rawrecord.RawSurveyqa;
import dev.mathops.font.BundledFontManager;
import dev.mathops.session.txn.messages.AbstractReplyBase;
import dev.mathops.session.txn.messages.ExamStartResultRequest;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetExamRequest;
import dev.mathops.session.txn.messages.PlacementStatusReply;
import dev.mathops.session.txn.messages.PlacementStatusRequest;
import dev.mathops.session.txn.messages.SurveyStatusReply;
import dev.mathops.session.txn.messages.SurveyStatusRequest;
import dev.mathops.session.txn.messages.SurveySubmitReply;
import dev.mathops.session.txn.messages.SurveySubmitRequest;
import dev.mathops.session.txn.messages.UpdateExamReply;
import dev.mathops.session.txn.messages.UpdateExamRequest;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An application to deliver the placement exam to a remote user.
 */
final class PlacementApp extends ClientBase implements Runnable, ExamContainerInt {

    /** Application version number. */
    private static final String VERSION = "1.4";

    /** The logged in user's name. */
    private final String name;

    /** The exam ID. */
    private final String examId;

    /** Flag indicating exam is being taken in proctored setting. */
    private final boolean proctored;

    /** The main thread for the application. */
    private final Thread thread;

    /** The top-level blocking window that prevents access to the desktop. */
    private JFrame frame;

    /** A desktop pane that will contain internal frames. */
    private JDesktopPane desk;

    /** Flag indicating an exam is in progress. */
    private boolean inProgress;

    /** The realized exam being taken. */
    private ExamObj exam;

    /** The exam panel. */
    private ExamPanel examPanel;

    /** The wrapper to present the exam panel in an internal frame. */
    private ExamPanelWrapper examPanelWrapper;

    /** A dialog to confirm that the exam is complete. */
    private AreYouFinished confirm;

    /** The ID of the student taking the exam. */
    private String studentId;

    /**
     * Construct a new {@code PlacementApp}.
     *
     * @param theScheme       the scheme to use to contact the server
     * @param theServer    the server host name
     * @param thePort      the server port
     * @param theStudentId the student ID
     * @param theName      the logged in user's display name
     * @param theExamId    the ID of the exam to launch
     * @param isProctored  {@code true} if exam is being taken in a proctored setting; {@code false} if not
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private PlacementApp(final String theScheme, final String theServer, final int thePort,
                         final String theStudentId, final String theName, final String theExamId,
                         final boolean isProctored) throws UnknownHostException {

        super(theScheme, theServer, thePort, null);

        this.studentId = theStudentId;
        this.name = theName;
        this.examId = theExamId;
        this.proctored = isProctored;

        this.thread = Thread.currentThread();
    }

    /**
     * The application's main processing method. This should be called after object construction to run the testing
     * interaction with the user. This method first connects securely to the server, then requests the student survey
     * information relating to the current session. That information will indicate whether or not the student has
     * completed the required survey information. If survey info needs to be gathered, the survey is presented. Then,
     * the student's eligibility to take the math challenge exam is tested, and if the student is ineligible, the reason
     * for that ineligibility is shown to the student. If all preconditions are met, the exam is started. Once
     * submitted, the server will reply with an indication of whether or not the results are available, and if they are,
     * the application requests them and presents them to the student.
     */
    private void go() {

        final boolean ok;

        // Test for a recovery file in the student's home directory. If one is found, try to submit it (then erase it).
        // If this happens, we don't want to proceed to deliver a new exam.
        if (recoverEarlierFailure() != FAILURE) {

            // Now we need to check the student's status regarding the survey questions associated with the placement
            // exam. This method sends the session ID to the server, and the server responds with the list of survey
            // questions. If any questions have not been answered by the student, the survey wizard is presented.
            if (doSurvey() == SUCCESS) {

                // Now we deliver the exam to the student. This requires requesting a unique realization of the exam
                // from the server, presenting the exam to the student, and gathering the student's responses. The exam
                // will end when the time limit expires, when the student elects submit the exam for grading.
                ok = deliverExam();

                if (ok) {
                    // We now request and present the student's placement status, using a wizard interface.
                    deliverResults();
                }
            }
        }
    }

    /**
     * Gets the ID of the student taking the exam.
     *
     * @return the student ID
     */
    public String getStudentId() {

        return this.studentId;
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
     * Writes the current exam state to disk.
     */
    @Override
    public void doCacheExamState() {

        final UpdateExamRequest req = new UpdateExamRequest(this.studentId, this.exam.ref,
                Long.valueOf(this.exam.realizationTime), this.exam.exportState(), false, this.proctored);

        // Try to write a file to the user's home directory
        final String home = System.getProperty("user.home");

        try {
            final File file = new File(new File(home), "csu_placement_exam.emergency");
            try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(req.toXml());
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Polls the server for the student's status regarding the survey questions that need to be answered.
     *
     * @return SUCCESS if we should proceed to the exam; FAILURE if not
     */
    private int recoverEarlierFailure() {

        // See if a recovery file exists.
        final String home = System.getProperty("user.home");
        File file = new File(home);
        file = new File(file, "csu_placement_exam.recovery");

        if (!file.exists()) {
            file = new File(file, "csu_placement_exam.emergency");
        }

        if (!file.exists()) {
            // No recovery or emergency file, so proceed with exam normally
            return SUCCESS;
        }

        final String[] msg1 = {"An exam record was found on your computer that did not",
                "get properly submitted after an earlier exam.", CoreConstants.SPC,
                "Would you like to recover and re-submit this exam now?"};

        int rc = JOptionPane.showOptionDialog(null, msg1, "Exam Recovery",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (rc == JOptionPane.NO_OPTION) {
            final String[] msg2 = {"Should the old exam record be deleted?", CoreConstants.SPC,
                    "WARNING! Deleting an old exam file will cause the",
                    "exam data to be lost.  This exam was NOT sent to",
                    "Colorado State University for grading.", CoreConstants.SPC,
                    "If it is not deleted, you will be prompted again",
                    "to recover the exam the next time you run the placement exam program. ",
                    "A new exam may not be taken until the old exam record",
                    "is either deleted or successfully submitted."};

            rc = JOptionPane.showOptionDialog(null, msg2, "Exam Recovery",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (rc == JOptionPane.YES_OPTION) {
                final String[] msg3 = {"This will delete the unsubmitted exam record.",
                        CoreConstants.SPC, "This operation cannot be undone.  Are you sure?"};

                rc = JOptionPane.showOptionDialog(null, msg3, "Exam Recovery",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (rc == JOptionPane.YES_OPTION) {

                    if (!file.delete()) {
                        final String[] msg4 = {"Unable to delete the exam recovery file."};

                         JOptionPane.showOptionDialog(null, msg4, "Exam Recovery",
                                 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                    }

                    return FAILURE;
                }
            }

            final String[] msg5 = {"The old exam record will be left in place.", CoreConstants.SPC,
                    "Run the placement exam application again to delete",
                    "or submit the old exam record."};

            JOptionPane.showMessageDialog(null, msg5, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

            return FAILURE;
        }

        final byte[] data = new byte[(int) file.length()];

        try (final FileInputStream in = new FileInputStream(file)) {
            in.read(data);
        } catch (final IOException e) {
            final String[] msg6 = {"Unable to read the recovery file.", "(" + e.getMessage() + ")",
                    "Contact precalc_math@colostate.edu for assistance."};

            JOptionPane.showMessageDialog(null, msg6, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

            return FAILURE;
        }

        final UpdateExamRequest req;
        try {
            req = new UpdateExamRequest(new String(data, StandardCharsets.UTF_8).toCharArray());
            req.indicateRecovered();
        } catch (final IllegalArgumentException e) {
            final String[] msg7 = {"Unable to load the recovery file.", "(" + e.getMessage() + ")",
                    "Contact precalc_math@colostate.edu for assistance."};

            JOptionPane.showMessageDialog(null, msg7, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

            return FAILURE;
        }

        AbstractReplyBase rep = null;
        while (rep == null) {
            rep = doExchange(req, "Update Exam", true);

            if (rep == null) {
                final String[] msg8 = {"Unable to connect to the server.",
                        "Check your Internet connection and try again. Would you like to retry now?"};

                rc = JOptionPane.showOptionDialog(null, msg8, "Exam Recovery",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (rc == JOptionPane.NO_OPTION) {
                    final String[] msg9 = {"The old exam record will be left in place. ",
                            "Run the placement exam application again to delete",
                            "or submit the old exam record."};

                    JOptionPane.showMessageDialog(null, msg9, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

                    return FAILURE;
                }
            }
        }

        final UpdateExamReply reply;
        if (rep instanceof UpdateExamReply) {
            reply = (UpdateExamReply) rep;
        } else {
            final String[] msg10 = {"Unexpected reply from the server.", //
                    "(" + rep.getClass().getName() + ")",
                    "Contact precalc_math@colostate.edu for assistance."};

            JOptionPane.showMessageDialog(null, msg10, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

            return FAILURE;
        }

        if (reply.status == UpdateExamReply.SUCCESS) {
            final String[] msg11 = {"The exam was recovered and submitted.", CoreConstants.SPC,
                    "Your exam results will be available on the web site."};
            JOptionPane.showMessageDialog(null, msg11, "Exam Recovery", JOptionPane.INFORMATION_MESSAGE);
            if (!file.delete()) {
                Log.warning("Failed to delete ", file.getAbsolutePath());
            }

            return FAILURE;
        }

        final String[] msg12 = {"The server did not accept the submitted exam.", null,
                "Contact precalc_math@colostate.edu for assistance."};

        if (reply.error != null) {
            msg12[1] = "(" + reply.error + ")";
        } else {
            msg12[1] = CoreConstants.EMPTY;
        }

        JOptionPane.showMessageDialog(null, msg12, "Exam Recovery", JOptionPane.ERROR_MESSAGE);

        return FAILURE;
    }

    /**
     * Polls the server for the student's status regarding the survey questions that need to be answered.
     *
     * @return SUCCESS if successful; an error code otherwise
     */
    private int doSurvey() {

        // Request the student's survey status, leaving the server connection open
        final SurveyStatusRequest request = new SurveyStatusRequest(this.studentId, this.examId);
        AbstractReplyBase rep = doExchange(request, "Survey status", true);

        final SurveyStatusReply reply;
        if (rep == null) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server.");
            Log.warning("Reply to survey status request was null");

            return FAILURE;
        } else if (rep instanceof SurveyStatusReply) {
            reply = (SurveyStatusReply) rep;
        } else {
            JOptionPane.showMessageDialog(null, "Invalid reply to survey status request.");
            Log.warning("Reply to survey status request was of class ", rep.getClass().getName());

            return UNEXPECTED_REPLY;
        }

        if (reply.error != null) {
            JOptionPane.showMessageDialog(null, reply.error);
            Log.warning("Reply to survey status request had error: ", reply.error);

            return FAILURE;
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

            for (int inx = 0; inx < highest; inx++) {
                for (final RawSurveyqa question : reply.questions) {
                    if (question != null && question.surveyNbr != null && question.surveyNbr.intValue() == inx + 1) {
                        questions[inx] = question.questionDesc;

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

            for (int inx = 0; inx < highest; inx++) {
                for (final RawStsurveyqa answer : reply.answers) {
                    if (answer != null && answer.surveyNbr != null && answer.surveyNbr.intValue() == inx + 1) {
                        answers[inx] = answer.stuAnswer;

                        break;
                    }
                }
            }
        }

        // Present the dialog
        final String examTitle = "PPPPP".equals(this.examId)
                ? "the Math Challenge Exam" : "the Math Placement Tool";

        if (Survey.doSurvey(this.frame, questions, answers,
                examTitle) == Wizard.FINISH_RETURN_CODE) {

            // Send updated responses to the server, leaving the connection open
            final SurveySubmitRequest request2 = new SurveySubmitRequest(this.studentId, this.examId, answers);
            rep = doExchange(request2, "Survey Submit", true);

            if (rep == null) {
                JOptionPane.showMessageDialog(null, "Unable to connect to server.");
                Log.warning("Reply to survey submit was null");

                return FAILURE;
            } else if (rep instanceof final SurveySubmitReply reply2) {

                if (reply2.error != null) {
                    JOptionPane.showMessageDialog(null, reply2.error);
                    Log.warning("Reply to survey submit had error: ", reply2.error);

                    return FAILURE;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid reply to survey submission.");
                Log.warning("Reply to survey submit was of class ", rep.getClass().getName());

                return UNEXPECTED_REPLY;
            }

            return SUCCESS;
        }

        return FAILURE;
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     *
     * @return {@code true} if the blocking window was created; {@code false} otherwise
     */
    private boolean createBlockingWindow() {

        final BuildBlockingWindow builder = new BuildBlockingWindow();

        try {
            SwingUtilities.invokeAndWait(builder);
        } catch (final Exception e) {
            Log.warning(e);
        }

        this.frame = builder.getFrame();
        this.desk = builder.getDesktop();

        // Start the thread that will keep the blocking window on top of all
        // other objects on the desktop.
        new Thread(this).start();

        return true;
    }

    /**
     * Destroys the blocking window.
     */
    private void killBlockingWindow() {

        final Runnable closer = new CloseBlockingWindow(this.frame);

        try {
            SwingUtilities.invokeAndWait(closer);
        } catch (final Exception e) {
            Log.warning(e);
        }

        this.desk = null;
        this.frame = null;
    }

    /**
     * Gets the frame in which the exam is to be displayed.
     *
     * @return the frame
     */
    @Override
    public JFrame getFrame() {

        return this.frame;
    }

    /**
     * Obtains a unique realized exam from the server, and presents it to the student. We collect the student's
     * responses to the exam, and allow the student to submit the exam for grading. We also monitor the time limit on
     * the exam, providing time warnings to the student as time runs low. If the time expires, the exam is submitted
     * automatically.<br>
     * <br>
     * On the screen will be the name of the logged in student, the current time and time remaining (if the exam is
     * time-limited), the list of exam questions, and the current question. Buttons will be provided for submitting the
     * exam for grading, and for accessing proctor functions. The proctor functions button will bring up a proctor login
     * dialog, which will allow access to a dialog allowing the proctor to pause the exam, resume the paused exam, or
     * abort the exam.
     *
     * @return {@code true} if the exam was delivered successfully, and was submitted for grading; {@code false} if the
     *         exam was aborted, or could not be started for some reason
     */
    private boolean deliverExam() {

        // Since the start exam exchange may take some time, display a "please wait" message box.
        final PleaseWait pleaseWait = new PleaseWait();
        pleaseWait.showPopup();

        // Perform a network exchange to fetch a presented exam, then clear the Please Wait dialog
        // and restore the cursor.
        final int rc = doStartExamExchange();

        pleaseWait.closePopup();

        if (rc != SUCCESS) {

            // Error message will have been shown in doStartExamExchange, which will have cleared
            // the Please Wait dialog as well.
            return false;
        }

        // Now, we create a full-screen, top-level window and activate a thread that will keep it
        // on top of everything else on the desktop. All windows this application creates will be
        // children of this window, so they will not be obscured, but' the desktop will not be
        // available.
        boolean ok = true;
        if (createBlockingWindow()) {
            final ExamSession session = new ExamSession(EExamSessionState.INTERACTING, this.exam);
            final ShowExamPanel showExam = new ShowExamPanel(this.desk, this, this.name, session);

            try {
                SwingUtilities.invokeAndWait(showExam);
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            this.examPanelWrapper = showExam.getWrapper();
            this.examPanel = this.examPanelWrapper.getContent();

            this.inProgress = true;
            this.exam.presentationTime = System.currentTimeMillis();

            // Wait for the exam to end.
            while (this.inProgress && this.frame.isVisible()) {

                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException ex) {
                    Log.finest("Interrupted showing exam");
                }
            }

            if (!this.inProgress) {
                this.exam.completionTime = System.currentTimeMillis();
                this.exam.proctored = this.proctored;
                preSubmit();

                // Clear the exam panel from the application desktop
                try {
                    final Runnable closeExam =
                            new CloseExamPanel(this.desk, this.examPanelWrapper);

                    SwingUtilities.invokeAndWait(closeExam);
                } catch (final Exception ex) {
                    Log.warning(ex);
                }

                killBlockingWindow();

                // Exam was submitted for grading - send to server
                ok = doUpdateExamExchange() == ClientBase.SUCCESS;

                if (ok) {
                    final String home = System.getProperty("user.home");
                    final File file = new File(new File(home), "csu_placement_exam.recovery");
                    if (file.delete()) {
                        Log.warning("Failed to delete ", file.getAbsolutePath());
                    }
                }
            } else {
                ok = false;
            }
        }

        // Delete the emergency recovery file (used only if app crashes before submit attempt)
        final String home = System.getProperty("user.home");
        final File file = new File(new File(home), "csu_placement_exam.emergency");
        if (!file.delete()) {
            Log.warning("Failed to delete ", file.getAbsolutePath());
        }

        this.examPanel = null;

        return ok;
    }

    /**
     * Performs the message exchange with the server to begin the exam.
     *
     * @return SUCCESS on successful completion; or an error code on failure
     */
    private int doStartExamExchange() {

        // Send the exam start request, await the response, then allow the server connection to
        // close.
        final GetExamRequest req = new GetExamRequest(this.studentId, this.examId, false);
        req.examType = "Q";

        final AbstractReplyBase rep = doExchange(req, "Get Exam", false);
        final GetExamReply reply;
        if (rep == null) {
            Log.warning("Start exam exchange returned null");
            killBlockingWindow();
            JOptionPane.showMessageDialog(null, "Unable to communicate with server.");

            return UNEXPECTED_REPLY;
        } else if (rep instanceof GetExamReply) {
            reply = (GetExamReply) rep;
        } else {
            killBlockingWindow();
            JOptionPane.showMessageDialog(null, "Invalid reply to get exam request.");
            Log.warning("Start exam exchange returned class ",
                    rep.getClass().getName());

            return UNEXPECTED_REPLY;
        }

        switch (reply.status) {

            case GetExamReply.SUCCESS:
                this.exam = reply.presentedExam;
                if (this.exam == null) {

                    killBlockingWindow();

                    if (reply.error != null) {
                        Log.warning("Start exam exchange returned error: ", reply.error, " for student ",
                                this.studentId);

                        // Split message across lines if needed
                        JOptionPane.showMessageDialog(null, reply.error.split("\n"));
                    } else {
                        Log.warning("Start exam exchange had null exam");
                        JOptionPane.showMessageDialog(null, "Server did not deliver an exam.");
                    }

                    return FAILURE;
                }

                // Validate exam has all problems selected
                if (validateExam()) {
                    this.studentId = reply.studentId;
                } else {
                    sendExamStartFailure();
                    Log.warning("Exam did not have all problems selected");
                    final String[] msg = {"The exam could not be generated.", "Please try starting the exam again."};
                    JOptionPane.showMessageDialog(null, msg);
                    return FAILURE;
                }
                break;

            case GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE:
                Log.warning("Start exam exchange: could not load exam template");
                killBlockingWindow();
                JOptionPane.showMessageDialog(null, "Server could not locate exam template.");

                return FAILURE;

            case GetExamReply.CANNOT_REALIZE_EXAM:
                Log.warning("Start exam exchange: could not realize exam");
                killBlockingWindow();
                JOptionPane.showMessageDialog(null, "Server could not generate the exam.");

                return FAILURE;

            case GetExamReply.CANNOT_PRESENT_EXAM:
                Log.warning("Start exam exchange: could not present exam");
                killBlockingWindow();
                JOptionPane.showMessageDialog(null, "Server could not construct the exam.");

                return FAILURE;

            default:
                return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * Tests that every problem on the exam has a selected problem.
     *
     * @return {@code true} if valid; {@code false} if not
     */
    private boolean validateExam() {

        final String[] fontNames = {
                "Arial", "Times New Roman",
                "Martin_Vogels_Symbole", "ESSTIXEight",
                "ESSTIXEleven", "ESSTIXFifteen",
                "ESSTIXFive", "ESSTIXFour",
                "ESSTIXFourteen", "ESSTIXNine",
                "ESSTIXOne", "ESSTIXSeven",
                "ESSTIXSeventeen", "ESSTIXSix",
                "ESSTIXSixteen", "ESSTIXTen",
                "ESSTIXThirteen", "ESSTIXThree",
                "ESSTIXTwelve", "ESSTIXTwo"};

        boolean valid = true;
        final int numSect = this.exam.getNumSections();

        for (int i = 0; i < numSect; i++) {
            final ExamSection sect = this.exam.getSection(i);

            final int numProb = sect.getNumProblems();

            for (int j = 0; j < numProb; j++) {
                final ExamProblem prob = sect.getPresentedProblem(j);

                if (prob.getSelectedProblem() == null) {
                    Log.warning("Problem ", prob.problemName, " had no selected problem");
                    valid = false;
                }
            }
        }

        final BundledFontManager bfm = BundledFontManager.getInstance();
        for (final String test : fontNames) {
            if (!bfm.canGetFont(test)) {
                Log.warning("Font ", test, " had was not loaded");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Sends a failure notification to the server that the exam did not start properly.
     */
    private void sendExamStartFailure() {

        final ExamStartResultRequest req;

        // Send the exam start request, await the response
        req = new ExamStartResultRequest(ExamStartResultRequest.CANT_PARSE_EXAM,
                this.exam.examVersion, this.exam.serialNumber);

        req.machineId = getMachineId();

        doExchange(req, "ExamStartResult", true);
    }

    /**
     * Writes out a recovery file immediately after the exam has been submitted.
     */
    private void preSubmit() {

        // Build an exam update request that will store backup data
        final UpdateExamRequest req = new UpdateExamRequest(this.studentId, this.exam.ref,
                Long.valueOf(this.exam.realizationTime), this.exam.exportState(), true, this.proctored);
        writeRecoveryFile(req);

        // Have every submitted exam sent to the server's exceptions log if possible.
        Log.info("Exam submitted for student ", this.studentId, req.toXml());
    }

    /**
     * Sends the current state of the exam to the server, so if something happens to the client machine, we can recover
     * the exam at the exact point of the failure.
     *
     * @return the status of the exchange
     */
    private int doUpdateExamExchange() {

        if (this.exam == null) {
            Log.warning("Update exam exchange: exam was null");

            return ClientBase.FAILURE;
        }

        // Kill the window that would prevent the user from getting to the desktop to fix a broken
        // Internet connection, in case we fail.
        killBlockingWindow();

        // Send the exam update request, await the response, but leave the connection open so we
        // can poll results
        final UpdateExamRequest req = new UpdateExamRequest(this.studentId, this.exam.ref,
                Long.valueOf(this.exam.realizationTime), this.exam.exportState(), true, this.proctored);

        // DIAGNOSTICS: If question 3 was unanswered, gather and log the student's system
        // information and the list of unanswered problems.
        final Object[][] ans = req.getAnswers();
        if (ans[3] == null) {
            final HtmlBuilder builder = new HtmlBuilder(250);
            builder.addln("Placement app version ", VERSION, ", Student ", this.studentId, CoreConstants.SPC, this.name,
                    " may have had a failed exam delivery.");
            builder.addln("    java.version=", System.getProperty("java.version"));
            builder.addln("    java.vm.version=", System.getProperty("java.vm.version"));
            builder.addln("    os.name=", System.getProperty("os.name"));
            builder.addln("    os.arch=", System.getProperty("os.arch"));
            builder.addln("    os.version=", System.getProperty("os.version"));
            builder.addln("    mem.total=", Long.toString(Runtime.getRuntime().totalMemory()));
            builder.addln("    mem.free=", Long.toString(Runtime.getRuntime().freeMemory()));

            final int numAns = ans.length;
            for (int i = 1; i < numAns; ++i) {
                if (ans[i] == null) {
                    builder.addln("  Problem ", Integer.toString(i), " unanswered");
                }
            }
            builder.add(this.exam.toXmlString(0));
            Log.warning(builder.toString());
        }

        for (; ; ) {
            final AbstractReplyBase rep = doExchange(req, "Update Exam", true);

            if (rep instanceof final UpdateExamReply reply) {

                if (reply.status == UpdateExamReply.SUCCESS) {
                    return ClientBase.SUCCESS;
                }

                // Server rejected the submission, save to recovery file.
                final String err = writeRecoveryFile(req);

                if (err == null) {
                    Log.warning("Update exam exchange: server did not accept: ", reply.error);
                    final String[] msg1 = {"The server did not accept the exam submission.", null,
                            "Your exam has been stored in a recovery file.",
                            "Contact precalc_math@colostate.edu for assistance."};

                    if (reply.error != null) {
                        msg1[1] = "(" + reply.error + ")";
                    } else {
                        msg1[1] = CoreConstants.EMPTY;
                    }

                    JOptionPane.showMessageDialog(null, msg1);

                    return UNEXPECTED_REPLY;
                }

                // Failed to write recovery file.
                final String[] msg2 = {"The server did not accept the exam submission.",
                        "WARNING: The program tried to save your exam in a",
                        "recovery file to be re-submitted the next time you",
                        "run the program.  The save failed, with this error:",
                        "  " + err, CoreConstants.SPC,
                        "Do you want to re-try the exam submission?"};

                int choice = JOptionPane.showConfirmDialog(null, msg2, "File Save Error",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.NO_OPTION) {
                    final String[] msg3 = {"Your exam data will be lost!", CoreConstants.SPC, "Are you sure?"};

                    choice = JOptionPane.showConfirmDialog(null, msg3, "Discard Exam Data", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        Log.warning("Student chose to discard cached exam data");

                        return UNEXPECTED_REPLY;
                    }
                }
            } else {

                // Student's data is about to be lost - back it up!
                final String err = writeRecoveryFile(req);

                if (err == null) {
                    Log.warning("Failed exam submission was cached for ", "later submission");
                    final String[] msg4 = {"Unable to connect to the server.",
                            "Your exam has been saved in a recovery file.  The",
                            "next time you run the Placement Exam application,",
                            "it will attempt to re-submit the exam."};
                    JOptionPane.showMessageDialog(null, msg4);

                    return UNEXPECTED_REPLY;
                }

                // Failed to write recovery file.
                Log.warning("Failed exam submission could not be cached");
                final String[] msg5 = {"Unable to connect to the server.",
                        "WARNING: The program tried to save your exam in a",
                        "recovery file to be re-submitted the next time you",
                        "run the program.  The save failed, with this error:", "  " + err,
                        CoreConstants.SPC, "Do you want to re-try the exam submission?"};

                int choice = JOptionPane.showConfirmDialog(null, msg5, "File Save Error",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.NO_OPTION) {
                    final String[] msg6 = {"Your exam data will be lost!", CoreConstants.SPC, "Are you sure?"};

                    choice = JOptionPane.showConfirmDialog(null, msg6, "Discard Exam Data", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        return UNEXPECTED_REPLY;
                    }
                }
            }
        }
    }

    /**
     * Writes a backup copy of the student's data to a file so they can email it to us in the event the network
     * connection did not work when the exam was submitted.
     *
     * @param req the update exam request that could not be submitted
     * @return an error message if the write failed; {@code null} if it succeeded
     */
    private static String writeRecoveryFile(final UpdateExamRequest req) {

        String err = null;

        // Try to write a file to the user's home directory
        final String home = System.getProperty("user.home");

        final File file = new File(new File(home), "csu_placement_exam.recovery");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(req.toXml());
        } catch (final IOException ex) {
            err = ex.getMessage();
        }

        return err;
    }

    /**
     * After an exam is graded, sends the student's responses to the server, which grades the exam using the unique
     * realized answers that were generated with the exam, applies various logic to determine the exam results, and
     * responds with these results.<br>
     * <br>
     * We present the exam results to the student, and offer the option of printing the results, if the computer has
     * printing capabilities.
     *
     * @return SUCCESS if successful; an error code otherwise
     */
    private int deliverResults() {

        // Request the student's survey status, leaving the connection open so we can send the
        // logout message quickly.
        final PlacementStatusRequest request = new PlacementStatusRequest(this.studentId);
        final AbstractReplyBase rep = doExchange(request, "Placement Status", true);

        final int result;

        if (rep == null) {
            Log.warning("Reply to placement status request was null");
            JOptionPane.showMessageDialog(null, "Unable to communicate with server.");

            result = UNEXPECTED_REPLY;
        } else {
            final PlacementStatusReply reply;

            if (rep instanceof PlacementStatusReply) {
                reply = (PlacementStatusReply) rep;

                if (reply.error != null) {
                    JOptionPane.showMessageDialog(null, reply.error);
                    Log.warning("Reply to placement status request had an error: ", reply.error);

                    result = FAILURE;
                } else {
                    // Present the dialog with the results.
                    Results.doResults(reply);

                    result = SUCCESS;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid reply to placement status request.");
                Log.warning("Reply to placement status request was of class ", rep.getClass().getName());
                result = UNEXPECTED_REPLY;
            }
        }

        return result;
    }

    /**
     * Keeps the blocking window on top of all other windows.
     */
    @Override
    public void run() {

        final Runnable obj = new FrameToFront(this.frame);

        while (this.frame != null && this.frame.isVisible()) {

            try {
                SwingUtilities.invokeAndWait(obj);
            } catch (final Exception e) { /* Empty */
            }

            try {
                Thread.sleep(50L);
            } catch (final InterruptedException e) { /* Empty */
            }
        }
    }

    /**
     * Handles clicks on the submission button.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        // NOTE: This runs in the AWT Event thread.

        if (!SwingUtilities.isEventDispatchThread()) {
           Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.exam != null) {

            final String cmd = e.getActionCommand();

            if ("Logout".equals(cmd)) {
                this.inProgress = false;
                this.thread.interrupt();
            } else if ("Larger".equals(cmd)) {
                this.examPanel.larger();
            } else if ("Smaller".equals(cmd)) {
                this.examPanel.smaller();
            } else if ("color-white".equals(cmd)) {
                this.exam.setBackgroundColor("white",
                        ColorNames.getColor("white"));
                this.examPanel.updateColor();
            } else if ("color-gold".equals(cmd)) {
                this.exam.setBackgroundColor("gold",
                        ColorNames.getColor("gold"));
                this.examPanel.updateColor();
            } else if ("color-purple".equals(cmd)) {
                this.exam.setBackgroundColor("MediumPurple",
                        ColorNames.getColor("MediumPurple"));
                this.examPanel.updateColor();
            } else if ("color-blue".equals(cmd)) {
                this.exam.setBackgroundColor("MediumTurquoise",
                        ColorNames.getColor("MediumTurquoise"));
                this.examPanel.updateColor();
            } else if ("Grade".equals(cmd)) {

                if (this.examPanel == null) {
                    return;
                }

                this.examPanel.setEnabled(false);

                // Test whether all questions have been answered. If not, warn the student.
                int numAnswered = 0;
                int numProblems = 0;

                try {
                    final int numSect = this.exam.getNumSections();
                    for (int inx = 0; inx < numSect; inx++) {
                        final ExamSection section = this.exam.getSection(inx);

                        if (section != null) {

                            final int numProb = section.getNumProblems();
                            for (int jnx = 0; jnx < numProb; jnx++) {
                                numProblems++;
                                final ExamProblem presented = section.getPresentedProblem(jnx);

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
                    Log.warning(ex);
                }

                this.confirm = new AreYouFinished(numProblems, numAnswered, this);
                this.examPanelWrapper.getDesktopPane().add(this.confirm);
                this.examPanelWrapper.getDesktopPane().setLayer(this.confirm, JLayeredPane.POPUP_LAYER.intValue());
                this.confirm.setVisible(true);
                this.confirm.centerInDesktop();
            } else if ("Yes".equals(cmd)) {
                this.confirm.setVisible(false);
                this.examPanelWrapper.getDesktopPane().remove(this.confirm);
                this.confirm.dispose();
                this.confirm = null;

                this.inProgress = false;
                this.thread.interrupt();
            } else if ("No".equals(cmd)) {
                this.confirm.setVisible(false);
                this.examPanelWrapper.getDesktopPane().remove(this.confirm);
                this.confirm.dispose();
                this.confirm = null;

                this.examPanel.setEnabled(true);
            }
        }
    }

    /**
     * Called when a timer expires.
     */
    @Override
    public void timerExpired() {

        // Shut off the exam
        this.inProgress = false;
        this.thread.interrupt();
    }

    /**
     * Main method to launch the placement exam application.
     *
     * @param args command-line arguments (scheme, host, port, screen name, exam ID, proctored)
     */
    public static void main(final String... args) {

        if (args.length == 0) {

            try {
                final PlacementApp app = new PlacementApp("https", "placement." + Contexts.DOMAIN, 443,
                        "888888888", "Test Student", "MPTUN", true);

                try {
                    app.go();
                    Thread.sleep(500L);
                } catch (final Exception ex) {
                    final List<String> error = new ArrayList<>(40);
                    error.add("There was an error launching the placement exam");
                    error.add(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());

                    for (final StackTraceElement st : ex.getStackTrace()) {
                        error.add("   " + st.toString());
                    }

                    JOptionPane.showMessageDialog(null, error.toArray());
                    Log.warning(ex);
                }

                app.transmitErrors();
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            System.exit(0);
        } else if (args.length == 7) {

            try {
                final int port = Long.valueOf(args[2]).intValue();

                final PlacementApp app = new PlacementApp(args[0], args[1], port, args[3], args[4], args[5],
                        "TRUE".equalsIgnoreCase(args[6]));

                try {
                    app.go();
                    Thread.sleep(500L);
                } catch (final Exception ex) {
                    final List<String> error = new ArrayList<>(40);
                    error.add("There was an error launching the placement exam");
                    error.add(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());

                    for (final StackTraceElement st : ex.getStackTrace()) {
                        error.add("   " + st.toString());
                    }

                    JOptionPane.showMessageDialog(null, error.toArray());
                    Log.warning(ex);
                }

                app.transmitErrors();
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(null, "Unable to start program - invalid arguments");
        }
    }
}

/**
 * Runnable that brings a particular frame to the front of the window stacking order.
 */
final class FrameToFront implements Runnable {

    /** The frame that is to be brought to the front. */
    private final JFrame target;

    /**
     * Constructs a new {@code FrameToFront} object.
     *
     * @param theTarget the frame that is to be brought to the front
     */
    FrameToFront(final JFrame theTarget) {

        this.target = theTarget;
    }

    /**
     * Bring the target frame to the front of the stacking order.
     */
    @Override
    public void run() {

        this.target.toFront();
    }
}

/**
 * Runnable that constructs the background frame and desktop panel.
 */
final class BuildBlockingWindow implements Runnable {

    /** The frame for the application. */
    private JFrame frame;

    /** The desktop pane for the application. */
    private JDesktopPane desktop;

    /**
     * Constructs a new {@code BuildBlockingWindow}.
     */
    BuildBlockingWindow() {

        // No action
    }

    /**
     * Gets the constructed frame.
     *
     * @return the constructed frame
     */
    public JFrame getFrame() {

        return this.frame;
    }

    /**
     * Gets the constructed desktop pane.
     *
     * @return the constructed desktop pane
     */
    public JDesktopPane getDesktop() {

        return this.desktop;
    }

    /**
     * Builds the frame and desktop pane and makes them visible. This is intended to run in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gs = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gs.getDefaultConfiguration();

        this.frame = new JFrame();
        this.frame.setUndecorated(true);
        this.frame.setFocusableWindowState(true);
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.desktop = new JDesktopPane();
        this.desktop.setBackground(ColorNames.getColor("steel blue"));

        // Make the desktop fill the available screen area.
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        size = new Dimension(size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);
        this.desktop.setPreferredSize(size);
        this.desktop.setLocation(insets.left, insets.top);

        this.frame.setContentPane(this.desktop);

        this.frame.pack();
        this.frame.setLocation(0, 0);
        this.frame.setVisible(true);
        this.frame.toFront();

        this.frame.requestFocus();
    }
}

/**
 * Runnable that closes and disposes of the background frame.
 */
final class CloseBlockingWindow implements Runnable {

    /** The frame to be closed. */
    private JFrame appFrame;

    /**
     * Constructs a new {@code CloseBlockingWindow}.
     *
     * @param frame the frame to close
     */
    CloseBlockingWindow(final JFrame frame) {

        this.appFrame = frame;
    }

    /**
     * Closes the frame. This is intended to run in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.appFrame != null) {
            this.appFrame.setVisible(false);
            this.appFrame.dispose();
            this.appFrame = null;
        }
    }
}

/**
 * Runnable that constructs and displays an exam panel in the background window.
 */
final class ShowExamPanel implements Runnable {

    /** The desktop panel that will hold the exam panel. */
    private final JDesktopPane owner;

    /** The application. */
    private final PlacementApp app;

    /** The username to pass to the exam panel. */
    private final String name;

    /** The exam the panel is to display. */
    private final ExamSession examSession;

    /** The exam panel wrapper to be displayed. */
    private ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code ShowExamPanel}.
     *
     * @param theOwner       the desktop panel that will hold the exam panel
     * @param theApp         the application
     * @param theName        the username to pass to the exam panel
     * @param theExamSession the exam session
     */
    ShowExamPanel(final JDesktopPane theOwner, final PlacementApp theApp, final String theName,
                  final ExamSession theExamSession) {

        this.owner = theOwner;
        this.app = theApp;
        this.name = theName;
        this.examSession = theExamSession;
    }

    /**
     * Gets the generated exam panel wrapper.
     *
     * @return the generated panel wrapper
     */
    public ExamPanelWrapper getWrapper() {

        return this.wrapper;
    }

    /**
     * Creates the panel and adds it to the desktop. This is intended to run in the AWT event thread.
     */
    @Override
    public void run() {

        final Properties skin = FileLoader.loadFileAsProperties(PlacementApp.class, "ExamPanelSkin",
                new DefaultSkin(), false);

        final String stuId = this.app.getStudentId();
        final boolean populateAnswers = "111223333".equals(stuId) || RawStudent.TEST_STUDENT_ID.equals(stuId);
        final ExamPanel panel = new ExamPanel(this.app, skin, this.name, this.examSession, populateAnswers, false,
                null, null);
        panel.addActionListener(this.app);
        this.wrapper = new ExamPanelWrapper(panel);

        // Install the exam panel in the desktop window
        this.owner.add(this.wrapper);
        this.wrapper.makeFullscreen();
        this.wrapper.buildUI();

        // If no instructions, go directly to the first problem
        if (this.examSession.getExam().instructions == null) {
            panel.pickProblem(0, 0);
        } else {
            panel.showInstructions();
        }

        this.wrapper.pack();
        this.wrapper.setVisible(true);
    }
}

/**
 * Runnable that closes and disposes of the exam panel in the background window.
 */
final class CloseExamPanel implements Runnable {

    /** The desktop panel that holds the exam panel. */
    private final JDesktopPane owner;

    /** The exam panel wrapper to be closed. */
    private final ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code CloseExamPanel}.
     *
     * @param theOwner   the desktop panel that holds the exam panel
     * @param theWrapper the exam panel wrapper to be closed
     */
    CloseExamPanel(final JDesktopPane theOwner, final ExamPanelWrapper theWrapper) {

        this.owner = theOwner;
        this.wrapper = theWrapper;
    }

    /**
     * Clears and disposes of the exam panel. This is intended to run in the AWT event thread.
     */
    @Override
    public void run() {

        this.wrapper.setVisible(false);
        this.owner.remove(this.wrapper);
        this.wrapper.dispose();
    }
}

/**
 * Contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7342009192277909920L;

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        final String[][] contents = {
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
                {"show-calculator", "false"},
                {"run-timer", "true"},};

        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}
