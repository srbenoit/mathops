package dev.mathops.app.assessment.examviewer;

import dev.mathops.app.ClientBase;
import dev.mathops.app.PleaseWait;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.exam.ExamPanel;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.session.txn.messages.GetPastExamReply;
import dev.mathops.session.txn.messages.GetPastExamRequest;
import dev.mathops.session.txn.messages.MessageFactory;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * An end-user application to be used to take unit review exams online. It takes as arguments a session ID, student
 * name, and exam version and presents the exam to the student. When the exam is complete, the student submits it for
 * grading, after which the student is shown a summary page of the results. The application is designed to be completely
 * customizable, and supports delivery of exams in multiple languages, allowing it to be branded and deployed in a
 * variety of settings.
 */
final class ExamViewerApp extends ClientBase implements ExamContainerInt {

    /** Application is being terminated. */
    private static final int FINISHED = 1;

    /** Application is reconstructing the exam. */
    private static final int LOADING_EXAM = 2;

    /** Student is viewing the exam. */
    private static final int VIEWING_EXAM = 3;

    /** The path of the realized exam XML file. */
    private final String xmlFile;

    /** The path of the updates file with the student's answers. */
    private final String updateFile;

    /** The top-level window for the application. */
    private JFrame frame;

    /** The content panel of the primary frame. */
    private JPanel content;

    /** The exam panel. */
    private ExamPanel examPanel;

    /** The exam session. */
    private ExamSession examSession;

    /** The application's current state. */
    private int state = LOADING_EXAM;

    /**
     * Constructs a new {@code ExamViewerApp}.
     *
     * @param theServer     the server host name
     * @param thePort       the server port
     * @param theXmlFile    the path of the realized exam XML file
     * @param theUpdateFile the path of the updates file for the student's answers
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private ExamViewerApp(final String theServer, final int thePort,
                          final String theXmlFile, final String theUpdateFile) throws UnknownHostException {

        super(theServer, thePort, null);

        this.xmlFile = theXmlFile;
        this.updateFile = theUpdateFile;
    }

    /**
     * The application's main processing method. This should be called after object construction to run the testing
     * interaction with the user. This method retrieves the exam from the server and presents it to the student. When
     * the exam is finished, the student submits the exam for grading, and a summary of the results are presented, and
     * can be printed out.
     */
    private void go() {

        // Create a window for the application.
        if (createWindow()) {
            viewExam();
            killWindow();
        }

        System.exit(0);
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

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen = new Dimension((screen.width << 3) / 10, (screen.height << 3) / 10);

        this.frame = new JFrame("Exam Viewer");
        this.frame.setFocusableWindowState(true);

        this.content = new JPanel();
        this.content.setLayout(null);
        this.content.setBackground(ColorNames.getColor("steel blue"));
        this.content.setPreferredSize(screen);
        this.frame.setContentPane(this.content);

        this.frame.pack();
        this.frame.setLocation(screen.width / 10, screen.height / 10);
        this.frame.setVisible(true);
        this.frame.toFront();

        this.frame.requestFocus();

        return true;
    }

    /**
     * Destroys the window.
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
     * Sends a request to the server with the XML and update file path (relative to the logged-in user's directory, so
     * you can't get an exam for someone else!), download the exam and updates XML data, and rebuild the exam as it was
     * when it was taken. Present the exam with answers displayed, in a non-editable form.
     */
    private void viewExam() {

        // Since the start exam exchange may take some time, display a "please wait" message box.
        final PleaseWait pleaseWait = new PleaseWait(this, CoreConstants.EMPTY);
        pleaseWait.show(this.content);

        // Establish a secure connection with the server.
        if (connectToServer() != SUCCESS) {
            pleaseWait.close(this.content);
            JOptionPane.showMessageDialog(null, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Perform a network exchange to fetch a presented exam, then clear
        // the Please Wait dialog and restore the cursor.
        final int rc = doGetPastExamExchange();

        // No need to remain connected as exam is being done
        disconnectFromServer();

        pleaseWait.close(this.content);

        if (rc != SUCCESS) {
            return;
        }

        // Add the exam panel to the interface, but do not yet choose a section or problem.
        this.state = VIEWING_EXAM;

        final DisplayExamPanel panel = new DisplayExamPanel(this, this.content, this.examSession);

        try {
            SwingUtilities.invokeAndWait(panel);
        } catch (final Exception e) {
            Log.warning(e);
        }

        this.examPanel = panel.getExamPanel();

        try {
            Thread.sleep(200L);
        } catch (final Exception e) { /* Empty */
        }

        this.examPanel.pickProblem(0, 0);
        this.examSession.getExam().presentationTime = System.currentTimeMillis();

        while (this.frame.isVisible() && this.state == VIEWING_EXAM) {

            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
                Thread.currentThread().interrupt();
            }
        }

        if (this.frame.isVisible()) {

            // Clear the exam panel from the application desktop.
            SwingUtilities.invokeLater(new KillExamPanel(this.content, this.examPanel));
        }
    }

    /**
     * Called when a timer expires.
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
     * Performs the message exchange with the server to begin the exam.
     *
     * @return SUCCESS on successful completion, or an error code on failure
     */
    private int doGetPastExamExchange() {

        try {
            // Send the request, await the response
            final GetPastExamRequest req = new GetPastExamRequest(this.xmlFile, this.updateFile);

            if (!getServerConnection().writeObject(req.toXml())) {
                Log.warning("Can't write get exam request");
                JOptionPane.showMessageDialog(this.frame, "Unable to request review exam from server", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return CANT_SEND;
            }

            Object obj = getServerConnection().readObject("GetPastExamReply");

            if (obj == null) {
                Log.warning("Reply to get exam request was null");
                JOptionPane.showMessageDialog(this.frame, "Unable to load the requested exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            } else if (!"[C".equals(obj.getClass().getName())) {
                Log.warning("Reply to get exam request was " + obj.getClass().getName());
                JOptionPane.showMessageDialog(this.frame, "Invalid reply to request for the review exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            }

            obj = MessageFactory.parseMessage((char[]) obj);

            if (obj == null) {
                Log.warning("Reply to view exam request was null");
                JOptionPane.showMessageDialog(this.frame, "Server could not send exam record.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            } else if (!(obj instanceof GetPastExamReply)) {
                Log.warning("Reply to view exam request was message type " + obj.getClass().getName());
                JOptionPane.showMessageDialog(this.frame, "Unexpected reply to request for the review exam.", "Error",
                        JOptionPane.ERROR_MESSAGE);

                return UNEXPECTED_REPLY;
            }

            final GetPastExamReply reply = (GetPastExamReply) obj;

            if (reply.error == null) {
                this.examSession = new ExamSession(EExamSessionState.REVIEW_WITH_SOLUTIONS, reply.exam);
            } else {
                Log.warning("Reply had error: " + reply.error);
                JOptionPane.showMessageDialog(this.frame, reply.error, "Error", JOptionPane.ERROR_MESSAGE);

                return FAILURE;
            }
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(this.frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Log.warning(ex);

            return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * Handler for clicks on the submission button.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (this.examSession != null) {
            final String cmd = e.getActionCommand();

            if ("Grade".equals(cmd)) {
                this.state = FINISHED;
            } else if ("Larger".equals(cmd)) {
                this.examPanel.larger();
            } else if ("Smaller".equals(cmd)) {
                this.examPanel.smaller();
            } else {
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
                }
            }
        }
    }

    /**
     * Launches the remote testing application.
     *
     * @param args command-line arguments (scheme, host, port, exam xml file, update file)
     */
    public static void main(final String... args) {

        if (args.length == 5) {
            try {
                final int port = Integer.parseInt(args[1]);
                final ExamViewerApp app = new ExamViewerApp(args[0], port, args[2], args[3]);
                app.go();

                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                    Thread.currentThread().interrupt();
                }
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Unable to start program - invalid arguments");
        }
    }
}

/**
 * A runnable class used to show the exam panel from the AWT event thread.
 */
final class DisplayExamPanel implements Runnable {

    /** The content panel to which to add the exam panel. */
    private final JPanel contentPane;

    /** The owning application. */
    private final ExamViewerApp owner;

    /** The exam session. */
    private final ExamSession examSession;

    /** The created ExamPanel. */
    private ExamPanel panel;

    /**
     * Create a new {@code DisplayExamPanel}.
     *
     * @param theOwner the application that owns the panel
     * @param content  the content panel to which to add the homework panel
     * @param exam     the presented {@code Exam} to present to the student
     */
    DisplayExamPanel(final ExamViewerApp theOwner, final JPanel content, final ExamSession exam) {

        this.owner = theOwner;
        this.contentPane = content;
        this.examSession = exam;
    }

    /**
     * Gets the generated exam panel.
     *
     * @return the generated panel
     */
    ExamPanel getExamPanel() {

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

        this.panel = new ExamPanel(this.owner, skin, CoreConstants.EMPTY, this.examSession, false, false, null, null);
        final Dimension size = this.contentPane.getSize();
        this.panel.setPreferredSize(size);
        this.panel.setSize(size);
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
final class KillExamPanel implements Runnable {

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
 * Contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6551243335703591832L;

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        final String[][] contents = {
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
                {"bottom-bar-lbl", "I am finished.  Submit the exam for grading."},
                {"bottom-bar-lbl-show-answers", "Close"},
                {"bottom-bar-lbl-practice", "Show the Answers"},

                {"show-problem-list", "true"},
                {"show-calculator", "false"},
                {"run-timer", "false"},};

        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}
