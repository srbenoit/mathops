package dev.mathops.app.review;

import dev.mathops.app.ClientBase;
import dev.mathops.app.PleaseWait;
import dev.mathops.app.TempFileCleaner;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.exam.ExamPanel;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.session.txn.messages.GetReviewExamRequest;
import dev.mathops.session.txn.messages.MessageFactory;
import dev.mathops.session.txn.messages.UpdateExamReply;
import dev.mathops.session.txn.messages.UpdateExamRequest;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serial;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

/**
 * This is an end-user application to be used to take unit review exams online. It takes as arguments a session ID,
 * student name, and exam version and presents the exam to the student. When the exam is complete, the student submits
 * it for grading, after which the student is shown a summary page of the results. The application is designed to be
 * completely customizable, and supports delivery of exams in multiple languages, allowing it to be branded and deployed
 * in a variety of settings.
 */
final class UnitReviewApp extends ClientBase implements ExamContainerInt, WindowListener {

    /** Application is being terminated. */
    private static final int FINISHED = 1;

    /** Application is starting the exam. */
    private static final int STARTING_EXAM = 8;

    /** Student is taking the exam. */
    private static final int TAKING_EXAM = 9;

    /** Student has finished the exam, it is ready for grading. */
    private static final int EXAM_FINISHED = 10;

    /** Application is displaying exam results to student. */
    private static final int VIEWING_MISSED = 12;

    /** A hash table of scores, key is subtest name, value is score. */
    private Map<String, Integer> subtestScores;

    /** A hash table of grades, key is grading rule name, value is grade. */
    private Map<String, Object> examGrades;

    /** The logged in user's name. */
    private final String username;

    /** The version of the exam selected. */
    private final String examVersion;

    /** The top-level window for the application. */
    private JFrame frame;

    /** The content panel of the primary frame. */
    private JPanel content;

    /** The exam panel. */
    private ExamPanel examPanel;

    /** The exam session. */
    private ExamSession examSession;

    /** The application's current state. */
    private int state = STARTING_EXAM;

    /** Flag indicating exam is practice. */
    private final boolean isPractice;

    /** The ID of the student taking the exam. */
    private String studentId;

    /**
     * Constructs a new {@code UnitReviewApp}.
     *
     * @param theScheme         the scheme to use to contact the server
     * @param theServer      the server host name
     * @param thePort        the server port
     * @param theStudentId   the student Id
     * @param theUsername    the logged in user's name
     * @param theExamVersion the exam version to take
     * @param practice       "true"if the exam is practice
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private UnitReviewApp(final String theScheme, final String theServer, final int thePort,
                          final String theStudentId, final String theUsername, final String theExamVersion,
                          final String practice) throws UnknownHostException {

        super(theScheme, theServer, thePort, null);

        this.studentId = theStudentId;
        this.username = theUsername;
        this.examVersion = theExamVersion;
        this.isPractice = "true".equalsIgnoreCase(practice);
    }

    /**
     * The application's main processing method. This should be called after object construction to run the testing
     * interaction with the user. This method retrieves the exam from the server and presents it to the student. When
     * the exam is finished, the student submits the exam for grading, and a summary of the results are presented, and
     * can be printed out.
     */
    private void go() {

        TempFileCleaner.clean();

        // Create a window for the application.
        if (!createWindow()) {
            return;
        }

        // Now we deliver the exam to the student. This requires requesting a unique realization of
        // the exam from the server, presenting the exam to the student, and gathering the
        // student's responses. The exam will end when the time limit expires or when the student
        // elects to submit the exam for grading.
        if (deliverExam()) {

            // Present the results to the student.
            if (!this.isPractice) {
                deliverResults();
            }

            showMissed();
        }

        killWindow();
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     *
     * @return {@code true} if the blocking window was created; {@code false} otherwise
     */
    private boolean createWindow() {

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = new Dimension(screen.width * 8 / 10, screen.height * 8 / 10);

        // FIXME: Hardcodes - move into command-line arguments?
        if (this.examVersion.startsWith("30")) {

            if (this.isPractice) {
                this.frame = new JFrame("Practice Unit Review Quiz");
                this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else {
                this.frame = new JFrame("Unit Review Quiz");
                this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                this.frame.addWindowListener(this);
            }
        } else {

            if (this.isPractice) {
                this.frame = new JFrame("Practice Unit Review Exam");
                this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else {
                this.frame = new JFrame("Unit Review Exam");
                this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                this.frame.addWindowListener(this);
            }
        }

        this.frame.setFocusableWindowState(true);

        this.content = new JPanel();
        this.content.setLayout(null);
        this.content.setBackground(ColorNames.getColor("steel blue"));
        this.content.setPreferredSize(size);
        this.frame.setContentPane(this.content);

        this.frame.pack();
        this.frame.setLocation(size.width / 10, size.height / 10);
        this.frame.setVisible(true);
        this.frame.toFront();

        this.frame.requestFocus();

        return true;
    }

    /**
     * Destroy the window.
     */
    private void killWindow() {

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
            this.frame = null;
        }
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
     * A method called when a timer expires.
     */
    @Override
    public void timerExpired() {

        // No action
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

        // No action
    }

    /**
     * We now obtain a unique realized exam from the server, and present it to the student. We collect the student's
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
        final PleaseWait pleaseWait = new PleaseWait(this, this.examVersion);
        pleaseWait.show(this.content);

        // Establish a secure connection with the server.
        if (connectToServer() != SUCCESS) {
            pleaseWait.close(this.content);
            JOptionPane.showMessageDialog(null, "Unable to connect to server", "Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        // Perform a network exchange to fetch a presented exam, then clear the Please Wait
        // dialog and restore the cursor.
        final int rc = doGetReviewExamExchange();

        // No need to remain connected as exam is being done
        disconnectFromServer();

        pleaseWait.close(this.content);

        if (rc != SUCCESS) {

            // Failed, so log out and start over
            return false;
        }

        // Add the exam panel to the interface, but do not yet choose a section or problem.
        this.state = TAKING_EXAM;

        final DisplayExamPanel panel = new DisplayExamPanel(this, this.content, this.studentId,
                this.username, this.examSession, this.isPractice);

        try {
            SwingUtilities.invokeAndWait(panel);
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        this.examPanel = panel.getExamPanel();
        this.examPanel.addActionListener(this);

        try {
            Thread.sleep(200L);
        } catch (final InterruptedException ex) {
            Log.warning(ex);
        }

        if (this.examSession.getExam().instructions == null) {
            this.examPanel.pickProblem(0, 0);
        } else {
            this.examPanel.showInstructions();
        }

        this.examSession.getExam().presentationTime = System.currentTimeMillis();

        // The exam can end one of two ways. Either the exam is finished (by the time expiring or
        // the student electing to submit it), or it is canceled by a proctor.
        while (this.frame.isVisible() && this.state == TAKING_EXAM) {

            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Log.info("Interrupted while taking exam");
            }
        }

        if (this.frame.isVisible()) {

            // Clear the exam panel from the application desktop.
            SwingUtilities.invokeLater(new KillExamPanel(this.content, this.examPanel));
            this.examSession.getExam().completionTime = System.currentTimeMillis();

            if (this.state == EXAM_FINISHED) {

                if (this.isPractice) {
                    if (connectToServer() == SUCCESS) {
                        doUpdateExamExchange(true);
                        disconnectFromServer();
                    }
                } else {

                    // Send the exam update to the server
                    int attempt = 1;
                    boolean success =
                            connectToServer() == SUCCESS && doUpdateExamExchange(true) == SUCCESS;

                    while (!success) {
                        ++attempt;

                        if (attempt == 5) {
                            final String[] msg =
                                    {"Unable to connect to the server to", "submit this review exam.",
                                            CoreConstants.SPC, "Keep trying to connect?"};

                            int sel = JOptionPane.showOptionDialog(this.frame, msg, "Error",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null,
                                    null);

                            if (sel == JOptionPane.NO_OPTION) {
                                sel = JOptionPane.showConfirmDialog(this.frame,
                                        "Are you sure you want to cancel this review exam?");

                                if (sel == JOptionPane.YES_OPTION) {
                                    return false;
                                }
                            }

                            attempt = 0;
                        }

                        try {
                            Thread.sleep(1000L);
                        } catch (final InterruptedException ex) {
                            Log.warning(ex);
                        }

                        success = connectToServer() == SUCCESS && doUpdateExamExchange(true) == SUCCESS;
                    }
                    disconnectFromServer();
                }
            }

            this.examSession.setState(EExamSessionState.CLOSED);
        }

        return true;
    }

    /**
     * After an exam is graded, we send the student's responses to the server, which grades the exam using the unique
     * realized answers that were generated with the exam, applies various logic to determine the exam results, and
     * responds with these results.<br>
     * <br>
     * We present the exam results to the student, and offer the option of printing the results, if the computer has
     * printing capabilities.
     */
    private void deliverResults() {

        // Since the start exam exchange may take some time, display a "please wait" message box.
        final FeedbackPanel feedback =
                new FeedbackPanel(this.username, this.subtestScores, this.examGrades, this.examVersion);
        feedback.show(this.content, this);

        // Wait for the user to close the feedback dialog.
        while (this.state == EXAM_FINISHED) {

            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
            }
        }

        feedback.close(this.content);
    }

    /**
     * Gets the exam panel.
     *
     * @return the exam panel
     */
    private ExamPanel getExamPanel() {

        return this.examPanel;
    }

    /**
     * Present the graded exam to the student, displaying the missed questions along with the correct answers (and
     * solutions, if available).
     */
    private void showMissed() {

        // Add the exam panel to the interface, but do not yet choose a section or problem.
        this.state = VIEWING_MISSED;
        this.examSession.setState(EExamSessionState.REVIEW_WITH_SOLUTIONS);

        final DisplayExamPanel panel = new DisplayExamPanel(this, this.content, this.studentId,
                this.username, this.examSession, this.isPractice);

        try {
            SwingUtilities.invokeAndWait(panel);
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        this.examPanel = panel.getExamPanel();
        this.examPanel.addActionListener(this);

        try {
            Thread.sleep(200L);
        } catch (final InterruptedException ex) {
            Log.warning(ex);
        }

        // No need to show instructions
        try {
            SwingUtilities.invokeAndWait(() -> this.examPanel.pickProblem(0, 0));
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        // Wait for the user to close out.
        while (this.frame.isVisible() && this.state == VIEWING_MISSED) {

            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
            }
        }

        if (this.frame.isVisible()) {
            // Clear the exam panel from the application desktop.
            SwingUtilities.invokeLater(new KillExamPanel(this.content, this.examPanel));
        }
        this.examSession.setState(EExamSessionState.CLOSED);
    }

    /**
     * Handler for clicks on the submission button.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (this.examSession != null) {

            if ("Logout".equals(cmd)) {
                this.state = FINISHED;
            } else if ("Larger".equals(cmd)) {
                this.examPanel.larger();
            } else if ("Smaller".equals(cmd)) {
                this.examPanel.smaller();
            } else {
                final ExamObj exam = this.examSession.getExam();

                if ("color-white".equals(cmd)) {
                    exam.setBackgroundColor("white",
                            ColorNames.getColor("white"));
                    this.examPanel.updateColor();
                } else if ("color-gold".equals(cmd)) {
                    exam.setBackgroundColor("gold",
                            ColorNames.getColor("gold"));
                    this.examPanel.updateColor();
                } else if ("color-purple".equals(cmd)) {
                    exam.setBackgroundColor("MediumPurple",
                            ColorNames.getColor("MediumPurple"));
                    this.examPanel.updateColor();
                } else if ("color-blue".equals(cmd)) {
                    exam.setBackgroundColor("MediumTurquoise",
                            ColorNames.getColor("MediumTurquoise"));
                    this.examPanel.updateColor();
                } else if ("Grade".equals(cmd)) {

                    if (this.state == VIEWING_MISSED) {
                        this.state = FINISHED;

                        return;
                    }

                    int numProblems = 0;
                    int numAnswered = 0;

                    // Test whether all questions have been answered. If not, warn the student.
                    for (int i = 0; i < exam.getNumSections(); i++) {
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

                    if (numAnswered < numProblems) {
                        final String[] msg = {"You have only answered " + numAnswered + " out of "
                                + numProblems + " questions.", null};

                        if (this.examVersion.startsWith("30")) {
                            if (this.isPractice) {
                                msg[1] =
                                        "Do you want to stop the practice quiz and see the answers?";
                            } else {
                                msg[1] = "Do you want to submit the review quiz for grading?";
                            }
                        } else if (this.isPractice) {
                            msg[1] = "Do you want to stop the practice exam and see the answers?";
                        } else {
                            msg[1] = "Do you want to submit the review exam for grading?";
                        }

                        if (JOptionPane.showConfirmDialog(this.examPanel, msg, "Confirm submission",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            this.state = EXAM_FINISHED;
                        } else {
                            this.examPanel.setEnabled(true);
                        }
                    } else {
                        this.state = EXAM_FINISHED;
                    }
                }
            }
        }
    }

    /**
     * Perform the message exchange with the server to begin the exam.
     *
     * @return SUCCESS on successful completion, an error code on failure
     */
    private int doGetReviewExamExchange() {

        try {

            // Send the exam start request, await the response
            final GetReviewExamRequest req =
                    new GetReviewExamRequest(this.studentId, this.examVersion, false, this.isPractice);

            if (!getServerConnection().writeObject(req.toXml())) {
                Log.warning("Can't write get exam request");
                JOptionPane.showMessageDialog(this.frame,
                        "Unable to request review exam from server", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return CANT_SEND;
            }

            Object obj = getServerConnection().readObject("GetReviewExamReply");

            if (obj == null) {
                Log.warning("Reply to get exam request was null");
                JOptionPane.showMessageDialog(this.frame, "Unable to load the requested exam.",
                        "Error", JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            } else if (!"[C".equals(obj.getClass().getName())) {
                Log.warning("Reply to get exam request was ",
                        obj.getClass().getName());
                JOptionPane.showMessageDialog(this.frame,
                        "Invalid reply to request for the review exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            }

            obj = MessageFactory.parseMessage((char[]) obj);

            if (obj == null) {
                Log.warning("Reply to get exam request was empty");
                JOptionPane.showMessageDialog(this.frame,
                        "Empty reply to request for the review exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            } else if (!(obj instanceof GetReviewExamReply)) {
                Log.warning("Reply to get exam request was message type ",
                        obj.getClass().getName());
                JOptionPane.showMessageDialog(this.frame,
                        "Unexpected reply to request for the review exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            }

            final GetReviewExamReply reply = (GetReviewExamReply) obj;

            if (reply.error == null) {
                final ExamObj exam = reply.presentedExam;
                this.examSession = new ExamSession(EExamSessionState.INTERACTING, exam);
                this.studentId = reply.studentId;
            } else {
                Log.warning("Reply had error: ", reply.error);
                JOptionPane.showMessageDialog(this.frame, reply.error, "Error",
                        JOptionPane.ERROR_MESSAGE);

                return FAILURE;
            }
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(this.frame, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            Log.warning(ex);

            return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * Send the current state of the exam to the server, so if something happens to the client machine, we can recover
     * the exam at the exact point of the failure.
     *
     * @param finalize {@code true} to finalize the exam; {@code false} otherwise
     * @return the status of the exchange
     */
    private int doUpdateExamExchange(final boolean finalize) {

        if (this.examSession == null) {
            Log.warning("No exam for which to send update");

            return ClientBase.FAILURE;
        }

        final ExamObj exam = this.examSession.getExam();

        if (finalize) {

            // Set the finalize time on the exam.
            exam.finalizeExam();
        }

        // Send the exam update request, await the response
        final UpdateExamRequest req = new UpdateExamRequest(this.studentId, exam.ref,
                Long.valueOf(exam.realizationTime), exam.exportState(), finalize, false);
        req.updateTime = Long.valueOf(System.currentTimeMillis());

        if (!getServerConnection().writeObject(req.toXml())) {
            return ClientBase.CANT_SEND;
        }

        final Object obj;

        try {
            obj = getServerConnection().readObject("UpdateExamReply");
        } catch (final Exception ex) {
            Log.warning(ex);

            if (finalize) {
                killWindow();
                JOptionPane.showMessageDialog(null,
                        "Unable to read response to exam status update.");
            }

            return ClientBase.FAILURE;
        }

        if (obj == null) {
            Log.warning("Unexpected response to exam status update: null");

            if (finalize) {
                killWindow();
                JOptionPane.showMessageDialog(null,
                        "Unexpected response to exam status update: null");
            }

            return ClientBase.UNEXPECTED_REPLY;
        } else if (!"[C".equals(obj.getClass().getName())) {
            Log.warning("Unexpected response to exam status update: ",
                    obj.getClass().getName());

            if (finalize) {
                killWindow();
                JOptionPane.showMessageDialog(null,
                        "Unexpected response to exam status update: " + obj.getClass().getName());
            }

            return ClientBase.UNEXPECTED_REPLY;
        }

        final UpdateExamReply reply;

        try {
            reply = new UpdateExamReply((char[]) obj);
        } catch (final IllegalArgumentException e) {
            Log.warning("Invalid response to exam status update.");

            if (finalize) {
                killWindow();
                JOptionPane.showMessageDialog(null, "Invalid response to exam status update.");
            }

            return ClientBase.UNEXPECTED_REPLY;
        }

        if (reply.status == UpdateExamReply.SUCCESS && finalize) {
            this.subtestScores = reply.subtestScores;
            this.examGrades = reply.examGrades;
            // this.missed = reply.getMissed();
        }

        return ClientBase.SUCCESS;
    }

    /**
     * Handles window opening events.
     *
     * @param e the window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {

        // Empty
    }

    /**
     * Handles window closing events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        if (this.state == VIEWING_MISSED) {

            // No need to confirm closure when simply viewing the missed problems
            this.frame.setVisible(false);
            this.frame.dispose();
        } else {
            final String[] message = {"If the exam is closed without submitting, all answers",
                    "entered so far will be lost.  Do you want to do this?"};

            final int choice = JOptionPane.showConfirmDialog(this.frame, message, "Close exam",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                this.frame.setVisible(false);
                this.frame.dispose();
            }
        }
    }

    /**
     * Handles window closed events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosed(final WindowEvent e) {

        // Empty
    }

    /**
     * Handles window iconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {

        // Empty
    }

    /**
     * Handles window deiconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {

        // Empty
    }

    /**
     * Handles window activated events.
     *
     * @param e the window event
     */
    @Override
    public void windowActivated(final WindowEvent e) {

        // Empty
    }

    /**
     * Handles window deactivated events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeactivated(final WindowEvent e) {

        // Empty
    }

    /**
     * Main method that launches the remote testing application.
     *
     * @param args command-line arguments (scheme, host, port, username, exam ID, practice mode)
     */
    public static void main(final String... args) {

        if (args.length == 7) {
            try {
                final int port = Integer.parseInt(args[2]);

                new UnitReviewApp(args[0], args[1], port, args[3], args[4], args[5], args[6]).go();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                }

                System.exit(0);
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        } else {
            try {
                new UnitReviewApp("https",
                        "precalc." + Contexts.DOMAIN, 443,
                        "111223333", "Steve",
                        "244RE", "true").go();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                }

                System.exit(0);
            } catch (final UnknownHostException e) {
                Log.warning(e);
            }
        }
    }
}

/**
 * A runnable class used to show the exam panel from the AWT event thread.
 */
class DisplayExamPanel implements Runnable {

    /** The content panel to which to add the exam panel. */
    private final JPanel contentPane;

    /** The owning application. */
    private final UnitReviewApp owner;

    /** The student ID. */
    private final String studentId;

    /** The username. */
    private final String username;

    /** The exam session. */
    private final ExamSession examSession;

    /** True if a practice exam; false if a normal exam. */
    private final boolean practice;

    /** The created ExamPanel. */
    private ExamPanel panel;

    /**
     * Creates a new {@code DisplayExamPanel}.
     *
     * @param theOwner       the application that owns the panel
     * @param content        the content panel to which to add the homework panel
     * @param theStudentId   the student ID
     * @param theUsername    the username
     * @param theExamSession the presented {@code Exam} to present to the student
     * @param isPractice     true if a practice exam; false if a normal exam
     */
    DisplayExamPanel(final UnitReviewApp theOwner, final JPanel content, final String theStudentId,
                     final String theUsername, final ExamSession theExamSession, final boolean isPractice) {

        this.owner = theOwner;
        this.contentPane = content;
        this.studentId = theStudentId;
        this.username = theUsername;
        this.examSession = theExamSession;
        this.practice = isPractice;
    }

    /**
     * Gets the generated exam panel.
     *
     * @return the generated panel
     */
    public ExamPanel getExamPanel() {

        return this.panel;
    }

    /**
     * Generates the window. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Properties skin = new DefaultSkin();

        if (this.examSession.getExam().examVersion.startsWith("30")) {
            skin.setProperty("bottom-bar-lbl",
                    "I am finished.  Submit the quiz for grading.");
        }

        final boolean populateAnswers = "111223333".equals(this.studentId)
                || RawStudent.TEST_STUDENT_ID.equals(this.studentId);

        this.panel = new ExamPanel(this.owner, skin, this.username, this.examSession,
                populateAnswers, this.practice, null, null);
        this.panel.setPreferredSize(this.contentPane.getSize());
        this.panel.setSize(this.contentPane.getSize());
        this.contentPane.add(this.panel, BorderLayout.CENTER);
        this.panel.buildUI();
        this.contentPane.setLayout(new BorderLayout());
        this.contentPane.add(this.panel, BorderLayout.CENTER);
        this.contentPane.revalidate();
        this.panel.setVisible(true);
    }
}

/**
 * A runnable class used to kill the exam panel from the AWT event thread.
 */
class KillExamPanel implements Runnable {

    /** The content panel to which to add the exam panel. */
    private final JPanel contentPane;

    /** The created ExamPanel. */
    private final ExamPanel panel;

    /**
     * Creates a new {@code KillExamPanel}.
     *
     * @param content  the content pane from which to remove the homework panel
     * @param thePanel the exam panel to kill
     */
    KillExamPanel(final JPanel content, final ExamPanel thePanel) {

        this.contentPane = content;
        this.panel = thePanel;
    }

    /**
     * Generates the window. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel.setVisible(false);
        this.contentPane.remove(this.panel);
    }
}

/**
 * A class that contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5429559994692685666L;

    /** The default settings. */
    private static final String[][] CONTENTS = {//
            {"top-bar-title", "$EXAM_TITLE"},
            {"top-bar-title-show-answers", "$EXAM_TITLE Answers"},
            {"top-bar-username", "$USERNAME"},
            {"top-bar-username-practice", "$USERNAME"},
            {"top-bar-username-zero-req", "$USERNAME"},
            {"top-bar-username-one-section", "$USERNAME"},
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
            {"bottom-bar-lbl",
                    "I am finished.  Submit the exam for grading."},
            {"bottom-bar-lbl-show-answers", "Close"},
            {"bottom-bar-lbl-practice", "Show the Answers"},

            {"show-problem-list", "true"},
            {"show-calculator", "false"},
            {"run-timer", "false"},};

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        for (final String[] content : CONTENTS) {
            setProperty(content[0], content[1]);
        }
    }
}
