package dev.mathops.app.hw;

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
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.db.Contexts;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.session.txn.messages.GetHomeworkReply;
import dev.mathops.session.txn.messages.GetHomeworkRequest;
import dev.mathops.session.txn.messages.MessageFactory;
import dev.mathops.session.txn.messages.SubmitHomeworkReply;
import dev.mathops.session.txn.messages.SubmitHomeworkRequest;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * This is an end-user application to be used to take homework assignments online. It takes as arguments a session ID,
 * student name, and assignment ID and presents the homework assignment to the student. When the assignment is complete,
 * the student submits it for grading, after which the student is shown a summary page of the results.
 */
final class HomeworkApp extends ClientBase implements ExamContainerInt {

    /** The panel that runs each section of the homework assignment. */
    private ExamPanel homeworkPanel;

    /** The top-level blocking window that prevents access to the desktop. */
    private JFrame frame;

    /** The homework session. */
    private ExamSession homeworkSession;

    /** The currently active section. */
    private int currentSection;

    /** The assignment ID being worked on. */
    private final String assignment;

    /** The content panel of the primary frame. */
    private JPanel content;

    /** Variable used to indicate grading has been requested. */
    private boolean grade;

    /** Minimum move-on score for the homework. */
    private Integer minMoveOn;

    /** Minimum mastery score for the homework. */
    private Integer minMastery;

    /** Flag indicating assignment is practice. */
    private final boolean isPractice;

    /** The ID of the student doing the homework. */
    private String studentId;

    /** A class to pick a section/problem in the AWT thread. */
    private ProblemPicker picker;

    /** A class to enable/disable the exam panel in the AWT thread. */
    private PanelEnabler enabler;

    /**
     * Constructs a new {@code HomeworkApp}.
     *
     * @param theScheme     the scheme to use to contact the server
     * @param theServer     the server host name
     * @param thePort       the server port
     * @param theStudentId  the student ID
     * @param theAssignment the assignment ID being worked on
     * @param practice      "true" if the assignment is practice (homework otherwise)
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private HomeworkApp(final String theScheme, final String theServer, final int thePort,
                        final String theStudentId, final String theAssignment, final String practice)
            throws UnknownHostException {

        super(theScheme, theServer, thePort, null);

        this.studentId = theStudentId;
        this.assignment = theAssignment;
        this.isPractice = "true".equalsIgnoreCase(practice);
    }

    /**
     * Gets the frame in which the homework is to be displayed.
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
     */
    private void go() {

        TempFileCleaner.clean();

        // We create a window for the application.
        final WindowCreator creator = new WindowCreator(this.isPractice);

        try {
            SwingUtilities.invokeAndWait(creator);
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        this.frame = creator.getCreatedFrame();

        if (this.frame != null) {
            this.content = (JPanel) this.frame.getContentPane();

            // Now we deliver the homework to the student. This requires requesting the raw
            // homework assignment from the server, and realizing each problem locally. The student
            // must get a problem correct before moving on to the next problem. A homework is done
            // when the last problem is completed.
            deliverHomework();

            // Destroy the window and close the server connection.
            final Runnable killer = new WindowKiller(this.frame);
            SwingUtilities.invokeLater(killer);
            this.frame = null;
        }
    }

    /**
     * Handler for actions generated by application panels.
     *
     * @param e the action event generated by the panel
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if ("Grade".equals(cmd)) {
            this.enabler.setEnable(false);
            SwingUtilities.invokeLater(this.enabler);
            this.grade = true;
        } else if ("Larger".equals(cmd)) {
            this.homeworkPanel.larger();
        } else if ("Smaller".equals(cmd)) {
            this.homeworkPanel.smaller();
        } else {
            final ExamObj homework = this.homeworkSession.getExam();

            if ("color-white".equals(cmd)) {
                homework.setBackgroundColor("white", ColorNames.getColor("white"));
                this.homeworkPanel.updateColor();
            } else if ("color-gold".equals(cmd)) {
                homework.setBackgroundColor("gold", ColorNames.getColor("gold"));
                this.homeworkPanel.updateColor();
            } else if ("color-purple".equals(cmd)) {
                homework.setBackgroundColor("MediumPurple", ColorNames.getColor("MediumPurple"));
                this.homeworkPanel.updateColor();
            } else if ("color-blue".equals(cmd)) {
                homework.setBackgroundColor("MediumTurquoise", ColorNames.getColor("MediumTurquoise"));
                this.homeworkPanel.updateColor();
            }
        }
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
     * Called when a timer expires.
     */
    @Override
    public void timerExpired() {

        // No action
    }

    /**
     * Writes the current exam state to disk.
     */
    @Override
    public void doCacheExamState() {

        // No action
    }

    /**
     * Delivers the homework assignment. We first request the assignment from the server, then construct the GUI in
     * which the student will complete the problems. Then, we run linearly through each section, randomizing the
     * problems in the section and presenting them to the student. The student works the problems and submits the
     * section, at which time it is graded. If the minimum move-on score was satisfied, the student is moved to the next
     * section. Students may go back to earlier sections at any time. If the score satisfies the mastery score, the
     * section is marked as mastered. Once the assignment is completed, it is submitted, and the server processes the
     * result.
     *
     * @return {@code true} if the assignment was delivered successfully, and was submitted for grading; {@code false}
     *         if the assignment was aborted, or could not be started for some reason
     */
    private boolean deliverHomework() {

        // Since the get homework exchange may take some time, display a "please wait" message
        // box, and change to the wait cursor.
        final PleaseWait pleaseWait = new PleaseWait(this, CoreConstants.EMPTY);
        pleaseWait.show();

        // Establish a secure connection with the server.
        if (connectToServer() != SUCCESS) {
            pleaseWait.close(this.content);
            Log.warning("Unable to connect to server");
            JOptionPane.showMessageDialog(this.frame, "Unable to connect to server", "Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        // Perform a network exchange to fetch a homework assignment, then clear the Please Wait
        // dialog and restore the cursor.
        final int rc = doGetHomeworkExchange();

        // No need to remain connected as homework is being done
        disconnectFromServer();
        pleaseWait.close(this.content);

        if (rc != SUCCESS || this.homeworkSession == null) {
            Log.warning("Unable to get assignment - aborting", null);
            return false;
        }

        // Add the exam panel to the interface, but do not yet choose a section or problem.
        final DisplayHomeworkPanel panel;

        synchronized (this) {
            panel = new DisplayHomeworkPanel(this, this.content, this.homeworkSession, this.isPractice,
                    this.minMoveOn, this.minMastery);
        }

        try {
            SwingUtilities.invokeAndWait(panel);
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        this.homeworkPanel = panel.getHomeworkPanel();
        this.picker = new ProblemPicker(this.homeworkPanel);
        this.enabler = new PanelEnabler(this.homeworkPanel);

        this.homeworkSession.setState(EExamSessionState.INTERACTING);
        final ExamObj homework = this.homeworkSession.getExam();

        // We set presentation time to just before we show the exam. The gap between realization
        // and presentation gives an indication of the speed with which we build the GUI.
        homework.presentationTime = System.currentTimeMillis();

        // We loop through the exam sections, and at each section, we randomize the problem(s) in
        // that section, and present it to the student. If the student answers enough correctly, we
        // move on to the next section. If not, we continue to regenerate the section until it is
        // completed sufficiently.
        this.currentSection = 0;

        while (this.frame.isVisible()) {
            int onSect = this.currentSection;

            ExamSection sect = homework.getSection(onSect);

            synchronized (this) {
                this.grade = false;

                for (int attempt = 1; attempt <= 5; attempt++) {

                    if (sect.realize(homework.getEvalContext())) {
                        this.picker.setSectionAndProblem(onSect, 0);
                        SwingUtilities.invokeLater(this.picker);

                        break;
                    }

                    Log.warning("Error on attempt " + attempt + " to generate section " + this.currentSection);
                }
            }

            // Wait for the section to be submitted
            while (this.frame.isVisible() && !this.grade) {

                if (this.currentSection != onSect) {
                    // User has changed sections.
                    onSect = this.currentSection;
                }

                try {
                    Thread.sleep(50L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                    Thread.currentThread().interrupt();
                }
            }

            if (this.frame.isVisible()) {

                synchronized (this) {
                    // User has requested that the current section be submitted and graded. We call
                    // a method to grade the section. This method gives us the new section to move
                    // to (based on user choice). If it returns -1, we are to submit the exam.
                    final int newSect = gradeSection(onSect);

                    if (newSect == -1) {
                        break; // Homework is finished - submit.
                    }

                    sect = homework.getSection(onSect);

                    if (!sect.passed) {
                        // Not completed, but it has been graded, so we must regenerate, so user
                        // can't go back and try another answer.

                        for (int attempt = 1; attempt <= 5; ++attempt) {

                            if (sect.realize(homework.getEvalContext())) {
                                sect.passed = false;
                                sect.mastered = false;
                                sect.score = Long.valueOf(0L);

                                break;
                            }

                            Log.warning("Error on attempt " + attempt + " to regenerate section "
                                    + this.currentSection);
                        }
                    }

                    this.currentSection = newSect;
                    this.enabler.setEnable(true);
                    SwingUtilities.invokeLater(this.enabler);
                }
            }
        }

        if (this.frame.isVisible()) {

            // We will only get here if the assignment has been completed. Clear the exam panel
            // from the application desktop, and if this is not practice, send completed homework
            // record to the server.
            SwingUtilities.invokeLater(new KillHomeworkPanel(this.content, this.homeworkPanel));
            homework.completionTime = System.currentTimeMillis();

            this.homeworkSession.setState(EExamSessionState.SUBMITTED);

            if (!this.isPractice) {

                // Establish a secure connection with the server.
                int attempt = 0;

                while (connectToServer() != SUCCESS) {
                    ++attempt;

                    if (attempt == 5) {
                        int sel = JOptionPane.showOptionDialog(this.frame,
                                "Unable to connect to the server.  Retry?", "Error",
                                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

                        if (sel == JOptionPane.NO_OPTION) {
                            sel = JOptionPane.showConfirmDialog(this.frame,
                                    "Are you sure you want to cancel this assignment?");

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
                        Thread.currentThread().interrupt();
                    }
                }

                doSubmitHomeworkExchange();
                disconnectFromServer();
                this.homeworkSession.setState(EExamSessionState.CLOSED);
            }
        }

        return true;
    }

    /**
     * Grades the current section, and optionally move to a different section.
     *
     * @param section the section being worked
     * @return the section to which to move
     */
    private int gradeSection(final int section) {

        final ExamObj homework = this.homeworkSession.getExam();

        // Get the active section
        ExamSection sect = homework.getSection(section);

        if (sect == null) {
            JOptionPane.showMessageDialog(this.frame, "Unable to obtain current section to grade");
            return section;
        }

        // Count the number of correct answers
        final int numProb = sect.getNumProblems();
        int score = 0;

        for (int i = 0; i < numProb; i++) {
            final ExamProblem prob = sect.getProblem(i);

            final AbstractProblemTemplate selected = prob.getSelectedProblem();

            if (selected.isAnswered() && selected.isCorrect(selected.getAnswer())) {
                score++;
            }
        }

        sect = homework.getSection(section);

        // Store the score
        if (sect.score == null) {
            sect.score = Long.valueOf((long) score);
        } else if (sect.score.intValue() < score) {
            sect.score = Long.valueOf((long) score);
        }

        // Determine the per-section move-on score, by dividing the assignment
        int min;

        if (this.minMoveOn == null) {
            min = 0;
        } else if (this.minMoveOn.intValue() == -1) {
            min = sect.getNumProblems();
        } else {
            min = this.minMoveOn.intValue() / homework.getNumSections();
        }

        if (score >= min) {
            sect.passed = true;
        }

        // Do the same with mastery score
        if (this.minMastery == null) {
            min = 0;
        } else if (this.minMastery.intValue() == -1) {
            min = sect.getNumProblems();
        } else {
            min = this.minMastery.intValue() / homework.getNumSections();
        }

        if (score >= min) {
            sect.mastered = true;
            sect.passed = true;
        }

        // If a section is completed, mark the next section as enabled
        int toSection = section;
        boolean warned = false;

        if (!this.isPractice) {
            JOptionPane.showMessageDialog(this.frame,
                    score > 0 ? "You are correct!" : "Your answer was incorrect.",
                    "Checking your Answer", JOptionPane.INFORMATION_MESSAGE);

            // If problem was incorrect, show the answer
            if (score == 0) {

                this.grade = false;

                final ExamPanel hp = this.homeworkPanel;
                try {
                    SwingUtilities.invokeAndWait(() ->
                            hp.setAnswersVisible(true, "M 101".equals(homework.course), "Go to the next problem..."));
                } catch (final InvocationTargetException | InterruptedException ex) {
                    Log.warning(ex);
                }

                this.enabler.setEnable(true);
                SwingUtilities.invokeLater(this.enabler);

                while (!this.grade && this.frame.isVisible()) {
                    try {
                        Thread.sleep(200L);
                    } catch (final Exception e) {
                        // No action
                    }
                }

                try {
                    SwingUtilities.invokeAndWait(() ->
                            hp.setAnswersVisible(false, false, "Submit this problem for grading."));
                } catch (final InvocationTargetException | InterruptedException ex) {
                    Log.warning(ex);
                }

                warned = true;
            }

            if (sect.passed && section + 1 < homework.getNumSections()) {
                homework.getSection(section + 1).enabled = true;
            }

            // See if ALL sections have reached the minimum move-on or mastery scores
            boolean allMastered = true;

            for (int i = 0; i < homework.getNumSections(); ++i) {

                if (!homework.getSection(i).mastered) {
                    allMastered = false;

                    break;
                }
            }

            // If all sections are mastered, submit the assignment.
            if (allMastered) {

                // All sections have been mastered - submit the homework
                JOptionPane.showMessageDialog(this.frame, "The assignment will now be recorded.",
                        "Assignment Complete", JOptionPane.INFORMATION_MESSAGE);

                // Update the top bar display before we leave
                final ExamPanel hp = this.homeworkPanel;
                try {
                    SwingUtilities.invokeAndWait(() -> hp.updateTop(section));
                } catch (final InvocationTargetException | InterruptedException ex) {
                    Log.warning(ex);
                }

                return -1;
            }

            // See if all prior sections were mastered

            for (int i = 0; i < section; i++) {

                if (!homework.getSection(i).mastered) {
                }
            }

            // If this section was mastered. we move on to the next, or submit if this is the
            // last section
            if (sect.mastered) {

                if (section == homework.getNumSections() - 1) {
                    JOptionPane.showMessageDialog(this.frame,
                            "The assignment will now be submitted.", "Assignment Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    toSection = -1;
                } else {

                    // Just move on to the next section.
                    toSection++;
                }
            } else if (sect.passed) {

                // Minimum move-on score was reached, but not mastery score
                final String[] message = {"This section has not yet been mastered.", "Would you like to try again?"};

                final int sel = JOptionPane.showConfirmDialog(this.frame, message,
                        "Section not Mastered", JOptionPane.YES_NO_OPTION);

                if (sel == JOptionPane.NO_OPTION) {

                    if (section == homework.getNumSections() - 1) {
                        JOptionPane.showMessageDialog(this.frame,
                                "The assignment will now be submitted.", "Assignment Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                        toSection = -1;
                    } else {
                        // Just move on to the next section.
                        toSection++;
                    }
                }
            } else if (!warned) {

                // Minimum move-on score was not reached - must regenerate...
                final String[] message = {"This section has not yet been completed.",
                        "A new version will be generated..."};
                JOptionPane.showMessageDialog(this.frame, message, "Section Not Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } else { // Practice mode

            if (score > 0) {
                JOptionPane.showMessageDialog(this.frame, "You are correct!", "Practice Problem",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this.frame, "Your answer was incorrect.", "Practice Problem",
                        JOptionPane.INFORMATION_MESSAGE);
                this.grade = false;

                // FIXME: Hardcodes
                this.homeworkPanel.setAnswersVisible(true, RawRecordConstants.M100T.equals(homework.course)
                        || RawRecordConstants.M1170.equals(homework.course)
                        || RawRecordConstants.M1180.equals(homework.course)
                        || RawRecordConstants.M1240.equals(homework.course)
                        || RawRecordConstants.M1250.equals(homework.course)
                        || RawRecordConstants.M1260.equals(homework.course), "Try another problem...");

                this.enabler.setEnable(true);
                SwingUtilities.invokeLater(this.enabler);

                while (!this.grade && this.frame.isVisible()) {
                    try {
                        Thread.sleep(200L);
                    } catch (final InterruptedException ex) {
                        Log.warning(ex);
                        Thread.currentThread().interrupt();
                    }
                }

                final ExamPanel hp = this.homeworkPanel;
                try {
                    SwingUtilities.invokeAndWait(() -> hp.setAnswersVisible(false, false, "Check my answer..."));
                } catch (final InvocationTargetException | InterruptedException ex) {
                    Log.warning(ex);
                }
            }

            // Advance to the next section, wrapping around
            ++toSection;

            if (toSection == homework.getNumSections()) {
                toSection = 0;
            }
        }

        // Update the top bar display
        this.homeworkPanel.updateTop(toSection);

        final ExamPanel hp = this.homeworkPanel;
        final int to = toSection;
        try {
            SwingUtilities.invokeAndWait(() -> hp.updateTop(to));
        } catch (final InvocationTargetException | InterruptedException ex) {
            Log.warning(ex);
        }

        return toSection;
    }

    /**
     * Performs the message exchange with the server to begin the exam.
     *
     * @return SUCCESS on successful completion, or an error code on failure
     */
    private int doGetHomeworkExchange() {

        // Send the get homework request, await the response
        final GetHomeworkRequest req = new GetHomeworkRequest(this.studentId, this.assignment, this.isPractice);

        if (!getServerConnection().writeObject(req.toXml())) {
            Log.warning("Unable to sent get homework request to server", null);
            return CANT_SEND;
        }

        Object obj = getServerConnection().readObject("GetHomeworkReply");

        if (!"[C".equals(obj.getClass().getName())) {
            Log.warning("Unexpected reply from server: " + obj.getClass().getName(), null);
            return UNEXPECTED_REPLY;
        }

        // LOG.fine(new String((char[]) obj));

        obj = MessageFactory.parseMessage((char[]) obj);

        if (!(obj instanceof final GetHomeworkReply reply)) {
            Log.warning("Invalid reply from server: "
                    + obj.getClass().getName(), null);
            return UNEXPECTED_REPLY;
        }

        if (reply.error == null) {

            synchronized (this) {
                this.homeworkSession = reply.homework == null ? null
                        : new ExamSession(EExamSessionState.GENERATED, reply.homework);
                this.minMoveOn = reply.minMoveOn;
                this.minMastery = reply.minMastery;
                this.studentId = reply.studentId;

                // LOG.fine("MIN MOVE ON = " + this.minMoveOn);
                // LOG.fine("MIN MASTERY = " + this.minMastery);
            }

            if (this.homeworkSession != null) {
                final ExamObj homework = this.homeworkSession.getExam();

                // Server will have set creation time, so we set realization (this gives us a
                // record of how fast we're doing the exchange)
                homework.realizationTime = System.currentTimeMillis();

                final int count = homework.getNumSections();

                if (count > 0) {
                    homework.getSection(0).enabled = true;
                } else {
                    JOptionPane.showMessageDialog(this.frame, "Assignment has no sections");
                    return FAILURE;
                }
            } else {
                JOptionPane.showMessageDialog(this.frame, "Assignment is unavailable.");
                return FAILURE;
            }
        } else {
            Log.warning("Error in reply from server: " + reply.error, null);
            JOptionPane.showMessageDialog(this.frame, reply.error.split("\n"));

            return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * Sends the current state of the exam to the server, so if something happens to the client machine, we can recover
     * the exam at the exact point of the failure.
     *
     * @return the status of the exchange
     */
    private int doSubmitHomeworkExchange() {

        if (this.homeworkSession == null) {
            Log.warning("No homework to submit", null);
            return ClientBase.FAILURE;
        }

        final ExamObj homework = this.homeworkSession.getExam();

        // Compute the score
        int score = 0;

        for (int i = 0; i < homework.getNumSections(); i++) {
            final Long value = homework.getSection(i).score;

            if (value != null) {
                score += value.intValue();
            }
        }

        // Send the exam update request, await the response
        final SubmitHomeworkRequest req = new SubmitHomeworkRequest(homework, homework.exportState(), score,
                this.studentId);

        if (!getServerConnection().writeObject(req.toXml())) {

            // Server may have restarted - try to reconnect
            if (this.connectToServer() == SUCCESS) {

                if (!getServerConnection().writeObject(req.toXml())) {
                    SwingUtilities.invokeLater(new WindowKiller(this.frame));
                    this.frame = null;
                    Log.warning("Unable to send assignment to server.", null);
                    JOptionPane.showMessageDialog(null, "Unable to send assignment to server.");

                    return ClientBase.CANT_SEND;
                }
            } else {
                SwingUtilities.invokeLater(new WindowKiller(this.frame));
                this.frame = null;
                Log.warning("Unable to send assignment to server.", null);
                JOptionPane.showMessageDialog(null, "Unable to send assignment to server.");

                return ClientBase.CANT_SEND;
            }
        }

        final Object obj;

        try {
            obj = getServerConnection().readObject("SubmitHomeworkReply");
        } catch (final Exception ex) {
            SwingUtilities.invokeLater(new WindowKiller(this.frame));
            this.frame = null;
            Log.warning("Unable to read response to assignment.", null);
            JOptionPane.showMessageDialog(null, "Unable to read response to assignment.");

            return ClientBase.FAILURE;
        }

        if (!"[C".equals(obj.getClass().getName())) {
            SwingUtilities.invokeLater(new WindowKiller(this.frame));
            this.frame = null;
            Log.warning("Unexpected response to assignment submission.", null);
            JOptionPane.showMessageDialog(null, "Unexpected response to assignment submission.");

            return ClientBase.UNEXPECTED_REPLY;
        }

        final SubmitHomeworkReply reply;

        try {
            reply = new SubmitHomeworkReply((char[]) obj);
        } catch (final IllegalArgumentException ex) {
            SwingUtilities.invokeLater(new WindowKiller(this.frame));
            this.frame = null;
            Log.warning("Invalid response to assignment submission.", ex);
            JOptionPane.showMessageDialog(null, "Invalid response to assignment submission.");

            return ClientBase.UNEXPECTED_REPLY;
        }

        if (reply.error != null) {
            JOptionPane.showMessageDialog(null, reply.error, "Error", JOptionPane.ERROR_MESSAGE);
        } else if (reply.result != null) {
            JOptionPane.showMessageDialog(null, reply.result, "Assignment Accepted", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Log.warning("Invalid response from server");
            JOptionPane.showMessageDialog(null, "Invalid response from server", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return ClientBase.SUCCESS;
    }

    /**
     * Launches the remote testing application.
     *
     * @param args command-line arguments (Scheme, host, port, student ID, assignment ID, practice mode (true|false))
     */
    public static void main(final String... args) {

        if (args.length == 6) {
            try {
                final int port = Long.valueOf(args[2]).intValue();
                final HomeworkApp app = new HomeworkApp(args[0], args[1], port, args[3], args[4], args[5]);
                app.go();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                    Thread.currentThread().interrupt();
                }

                System.exit(0);
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        } else {
            final HomeworkApp app;
            try {
                app = new HomeworkApp("https", "precalc." + Contexts.DOMAIN, 443, "111223333", "1713H", "false");
                app.go();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                }

                System.exit(0);
            } catch (final UnknownHostException ex) {
                Log.warning(ex);
            }

            // JOptionPane.showMessageDialog(null, "Unable to start program - invalid arguments");
        }
    }
}

/**
 * Generates the application frame and content panel from the AWT event thread.
 */
final class WindowCreator implements Runnable {

    /** The created frame. */
    private JFrame createdFrame;

    /** Flag indicating the assignment is being taken as practice. */
    private final boolean practice;

    /**
     * Constructs a new {@code WindowCreator}.
     *
     * @param isPractice {@code true} if assignment is being taken as practice
     */
    WindowCreator(final boolean isPractice) {

        this.practice = isPractice;
    }

    /**
     * Gets the generated frame.
     *
     * @return the generated {@code JFrame}
     */
    JFrame getCreatedFrame() {

        return this.createdFrame;
    }

    /**
     * Generates the frame. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen = new Dimension((screen.width << 3) / 10, (screen.height << 3) / 10);

        if (this.practice) {
            this.createdFrame = new JFrame("Practice Problems");
        } else {
            this.createdFrame = new JFrame("Required Assignment");
        }

        this.createdFrame.setFocusableWindowState(true);

        final JPanel content = new JPanel();
        content.setLayout(null); // So "Please Wait" dialog isn't sized
        content.setBackground(ColorNames.getColor("steel blue"));
        content.setPreferredSize(screen);
        this.createdFrame.setContentPane(content);

        this.createdFrame.pack();
        this.createdFrame.setLocation(screen.width / 8, screen.height / 10);
        this.createdFrame.setVisible(true);
        this.createdFrame.toFront();

        this.createdFrame.requestFocus();
    }
}

/**
 * Kills the application frame and content panel from the AWT event thread.
 */
final class WindowKiller implements Runnable {

    /** The frame this class will close and dispose. */
    private JFrame frameToKill;

    /**
     * Creates a new {@code WindowKiller}.
     *
     * @param frame the generated {@code JFrame}
     */
    WindowKiller(final JFrame frame) {

        this.frameToKill = frame;
    }

    /**
     * Closes and disposes the window. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.frameToKill != null) {
            this.frameToKill.setVisible(false);
            this.frameToKill.dispose();
            this.frameToKill = null;
        }
    }
}

/**
 * Picks a problem from the AWT event thread.
 */
final class ProblemPicker implements Runnable {

    /** The exam panel. */
    private final ExamPanel examPanel;

    /** The section to pick. */
    private int section;

    /** The problem to pick. */
    private int problem;

    /**
     * Creates a new {@code ProblemPicker}.
     *
     * @param theExamPanel the panel whose problem is to be picked
     */
    ProblemPicker(final ExamPanel theExamPanel) {

        this.examPanel = theExamPanel;
    }

    /**
     * Sets the section and problem to pick.
     *
     * @param theSection the section
     * @param theProblem the problem
     */
    void setSectionAndProblem(final int theSection, final int theProblem) {

        this.section = theSection;
        this.problem = theProblem;
    }

    /**
     * Picks the problem. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.examPanel != null) {
            this.examPanel.pickProblem(this.section, this.problem);
        }
    }
}

/**
 * Enables or disables the exam panel.
 */
final class PanelEnabler implements Runnable {

    /** The exam panel. */
    private final ExamPanel examPanel;

    /** The enable flag to use. */
    private boolean enable;

    /**
     * Create a new {@code PanelEnabler}.
     *
     * @param theExamPanel the panel that is to be enabled/disabled
     */
    PanelEnabler(final ExamPanel theExamPanel) {

        this.examPanel = theExamPanel;
    }

    /**
     * Sets the enable flag to use.
     *
     * @param shouldEnable {@code true} to enable panel; {@code false} to disable
     */
    public void setEnable(final boolean shouldEnable) {

        this.enable = shouldEnable;
    }

    /**
     * Sets the enabled flag. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.examPanel != null) {
            this.examPanel.setEnabled(this.enable);
        }
    }
}

/**
 * Shows the homework panel from the AWT event thread.
 */
final class DisplayHomeworkPanel implements Runnable {

    /** The owning application. */
    private final HomeworkApp owner;

    /** The homework session. */
    private final ExamSession homeworkSession;

    /** The content panel to which to add the homework panel. */
    private final JPanel contentPane;

    /** Flag indicating this is a practice problem set. */
    private final boolean practice;

    /** Minimum move-on score. */
    private final Integer moveon;

    /** Minimum mastery score. */
    private final Integer mastery;

    /** The created HomeworkPanel. */
    private ExamPanel panel;

    /**
     * Create a new {@code DisplayHomeworkPanel}.
     *
     * @param theOwner           the application that owns the panel
     * @param content            the content panel to which to add the homework panel
     * @param theHomeworkSession the homework session
     * @param isPractice         {@code true} if the assignment is a practice problem set; {@code false} if it is a
     *                           graded homework
     * @param theMoveon          the minimum move-on score
     * @param theMastery         the minimum mastery score
     */
    DisplayHomeworkPanel(final HomeworkApp theOwner, final JPanel content, final ExamSession theHomeworkSession,
                         final boolean isPractice, final Integer theMoveon, final Integer theMastery) {

        this.owner = theOwner;
        this.contentPane = content;
        this.homeworkSession = theHomeworkSession;
        this.practice = isPractice;
        this.moveon = theMoveon;
        this.mastery = theMastery;
    }

    /**
     * Gets the generated homework panel.
     *
     * @return the generated panel
     */
    ExamPanel getHomeworkPanel() {

        return this.panel;
    }

    /**
     * Generates the panel. Intended to be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Properties skin = FileLoader.loadFileAsProperties(HomeworkApp.class, "ExamPanelSkin",
                new DefaultSkin(), false);

        this.panel = new ExamPanel(this.owner, skin, CoreConstants.EMPTY, this.homeworkSession, false, this.practice,
                this.moveon, this.mastery);
        this.panel.setPreferredSize(this.contentPane.getSize());
        this.panel.setSize(this.contentPane.getSize());
        this.contentPane.setLayout(new BorderLayout());
        this.contentPane.add(this.panel, BorderLayout.CENTER);
        this.panel.buildUI();
        this.panel.setVisible(true);
        this.contentPane.revalidate();
        this.contentPane.repaint();
    }
}

/**
 * Kills the homework panel from the AWT event thread.
 */
final class KillHomeworkPanel implements Runnable {

    /** The content panel to which to add the homework panel. */
    private final JPanel contentPane;

    /** The created HomeworkPanel. */
    private final ExamPanel panel;

    /**
     * Creates a new {@code KillHomeworkPanel}.
     *
     * @param content  the content pane from which to remove the homework panel
     * @param thePanel the homework panel to kill
     */
    KillHomeworkPanel(final JPanel content, final ExamPanel thePanel) {

        this.contentPane = content;
        this.panel = thePanel;
    }

    /**
     * Removes the panel from the content pane. Intended to be run from the AWT event thread.
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
 * Contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5144109577228201621L;

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-title-show-answers", "$EXAM_TITLE Answers"},
                {"top-bar-username", CoreConstants.EMPTY},
                {"top-bar-username-practice", CoreConstants.EMPTY},
                {"top-bar-username-zero-req", CoreConstants.EMPTY},
                {"top-bar-username-one-section", CoreConstants.EMPTY},
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
                {"top-bar-show-sections", "true"},
                {"top-bar-show-sections-if-one", "false"},
                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "15"},
                {"top-bar-username-style", "bold"},
                {"top-bar-username-color", "navy"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},
                {"top-bar-section-font", "SANS"},
                {"top-bar-section-size", "14"},
                {"top-bar-section-style", "bold"},
                {"top-bar-section-color", "navy"},
                {"top-bar-section-x", "0.05"},
                {"top-bar-section-y", "62"},
                {"top-bar-section-alignment", "left"},
                {"top-bar-divider-y", "56"},
                {"top-bar-divider-color", "gray70"},
                {"top-bar-divider-start-x", "0.01"},
                {"top-bar-divider-end-x", "0.99"},

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
                {"bottom-bar-lbl", "Submit this problem for grading."},
                {"bottom-bar-lbl-show-answers", "Close"},
                {"bottom-bar-lbl-practice", "Check my answer..."},

                {"show-problem-list", "false"},
                {"show-calculator", "false"},
                {"run-timer", "false"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}
