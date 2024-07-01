package dev.mathops.app.assessment.localtesting;

import dev.mathops.app.ClientBase;
import dev.mathops.app.DirectoryFilter;
import dev.mathops.app.PleaseWait;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.exam.ExamPanel;
import dev.mathops.app.exam.ExamPanelWrapper;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.FactoryBase;
import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlFileFilter;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.session.SessionCache;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.MessageFactory;
import dev.mathops.session.txn.messages.UpdateExamRequest;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * This is an application to open an exam XML file, generate a randomized exam and present it for testing.
 */
public final class LocalTestingApp extends ClientBase implements ExamContainerInt {

    /** The exam panel. */
    private ExamPanel examPanel;

    /** The top-level blocking window that prevents access to the desktop. */
    private JFrame frame;

    /** Flag indicating application is running in demo mode. */
    private final boolean demo;

    /** A desktop pane that will contain internal frames. */
    private JDesktopPane desk;

    /** The presented exam the student is to take. */
    private ExamObj exam;

    /** Flag to indicate the exam is in progress. */
    private boolean inExam;

    /** The directory relative to which instructional data is stored. */
    private File baseDir;

    /** The exam XML file. */
    private File examFile;

    /**
     * Constructs a new {@code LocalTestingApp}.
     *
     * @param theScheme       the scheme to use to contact the server
     * @param theServer    the server host name
     * @param thePort      the server port
     * @param theSessionId the session ID
     * @param isDemo       {@code true} if the application was launched ion demo mode
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private LocalTestingApp(final String theScheme, final String theServer, final int thePort,
                            final String theSessionId, final boolean isDemo) throws UnknownHostException {

        super(theScheme, theServer, thePort, theSessionId);

        this.demo = isDemo;
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

//    /**
//     * Gets the exam panel.
//     *
//     * @return the exam panel
//     */
//    public ExamPanel getExamPanel() {
//
//        return this.examPanel;
//    }

    /**
     * Sets the exam panel.
     *
     * @param theExamPanel the exam panel
     */
    void setExamPanel(final ExamPanel theExamPanel) {

        this.examPanel = theExamPanel;
    }

    /**
     * Called when a timer expires.
     */
    @Override
    public void timerExpired() {

        // No action
    }

    /**
     * Selects a problem to present in the current problem panel.
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
     * The application's main processing method. This should be called after object construction to run the
     * program.<br>
     * <br>
     * This method lets the user select an exam, then delivers the exam in a diagnostic view where the answers are
     * shown.
     */
    private void go() {

        if (chooseExam()) {
            deliverExam();
        }

        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     *
     * @return {@code true} if selection made; {@code false} if canceled
     */
    private boolean chooseExam() {

        boolean ok = true;

        // First, locate the directory that serves as the base
        final JFileChooser jfc = new JFileChooser();

        File file = PathList.getInstance().get(EPath.SOURCE_1_PATH);

        if (file != null && file.exists()) {
            jfc.setCurrentDirectory(file);
        } else {
            file = PathList.getInstance().get(EPath.SOURCE_2_PATH);

            if (file != null && file.exists()) {
                jfc.setCurrentDirectory(file);
            } else {
                file = PathList.getInstance().get(EPath.SOURCE_3_PATH);

                if (file != null && file.exists()) {
                    jfc.setCurrentDirectory(file);
                }
            }
        }

        jfc.setFileFilter(new DirectoryFilter());
        jfc.setDialogTitle("Instructional Data Path");
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.baseDir = jfc.getSelectedFile();

            // Now select an exam:
            file = new File(this.baseDir, "instruction");
            file = new File(file, "math");
            jfc.setCurrentDirectory(file);
            jfc.setFileFilter(new XmlFileFilter());
            jfc.setDialogTitle("Select an Exam");
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.examFile = jfc.getSelectedFile();
            } else {
                ok = false;
            }
        } else {
            ok = false;
        }

        return ok;
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     */
    private void createBlockingWindow() {

        this.frame = new JFrame("Exam Testing");
        this.frame.setUndecorated(true);
        this.frame.setFocusableWindowState(true);

        this.desk = new JDesktopPane();
        this.desk.setBackground(ColorNames.getColor("steel blue"));

        this.desk.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        this.frame.setContentPane(this.desk);

        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.toFront();

        this.frame.requestFocus();
    }

    /**
     * Destroys the blocking window.
     */
    private void killBlockingWindow() {

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
            this.frame = null;
        }
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

        // Now, we create a top-level window and activate a thread that will keep it on top of
        // everything else on the desktop. All windows this application creates will be children of
        // this window, so they will not be obscured, but the desktop will not be available.
        createBlockingWindow();

        // Since exam realization may take some time, display "please wait"
        final PleaseWait pleaseWait = new PleaseWait(this, CoreConstants.EMPTY);
        final Runnable show = new ShowPleaseWait(this.desk, pleaseWait);
        SwingUtilities.invokeLater(show);
        final Runnable kill = new KillPleaseWait(this.desk, pleaseWait);

        boolean ok = true;
        AbstractProblemTemplate p;

        // Generate the realized exam
        try {
            final XmlContent content = FactoryBase.getSourceContent(this.examFile);
            this.exam = ExamFactory.load(content, EParserMode.NORMAL);

            if (content.getErrors() != null) {
                final int numMessages = content.getErrors().size();
                if (numMessages != 0) {

                    try {
                        SwingUtilities.invokeAndWait(kill);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (final InvocationTargetException ex) {
                        Log.warning(ex);
                    }

                    killBlockingWindow();

                    JOptionPane.showMessageDialog(this.desk, content.getErrors().toString());
                    ok = false;
                }
            }

            if (this.exam.ref != null) {

                // Now we must add the exam's problems, so it can be realized.
                final int numSect = this.exam.getNumSections();

                for (int onSect = 0; onSect < numSect; onSect++) {

                    final ExamSection esect = this.exam.getSection(onSect);
                    final int numProb = esect.getNumProblems();

                    for (int onProb = 0; onProb < numProb; onProb++) {

                        final ExamProblem eprob = esect.getProblem(onProb);

                        final int num = eprob.getNumProblems();

                        for (int inx = 0; inx < num; inx++) {
                            p = eprob.getProblem(inx);
                            final String path = this.exam.refRoot + "." + p.id;
                            p = InstructionalCache.getProblem(this.baseDir, path);

                            eprob.setProblem(inx, p);
                        }
                    }
                }
            }

            if (ok && !this.exam.realize(false, false, 12345L)) {

                try {
                    SwingUtilities.invokeAndWait(kill);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (final InvocationTargetException ex) {
                    Log.warning(ex);
                }

                killBlockingWindow();

                JOptionPane.showMessageDialog(this.desk, "Error randomizing exam");
                ok = false;
            }

            if (ok) {
                GetExamReply reply = new GetExamReply();
                reply.presentedExam = this.exam;
                final char[] xml = reply.toXml().toCharArray();

                final AbstractMessageBase obj = MessageFactory.parseMessage(xml);

                if (obj instanceof GetExamReply) {
                    reply = (GetExamReply) obj;
                    this.exam = reply.presentedExam;

                    final int numSect = this.exam.getNumSections();
                    for (int i = 0; i < numSect; i++) {
                        final ExamSection sect = this.exam.getSection(i);

                        final int numProb = sect.getNumProblems();
                        for (int j = 0; j < numProb; j++) {
                            final ExamProblem prob = sect.getPresentedProblem(j);
                        }
                    }
                } else {
                    ok = false;
                }
            }

        } catch (final ParsingException ex) {
            Log.warning(ex);
            JOptionPane.showMessageDialog(this.desk, "Error parsing exam");
            ok = false;
        }

        if (this.exam.ref != null) {
            final int numSect = this.exam.getNumSections();

            for (int onSect = 0; onSect < numSect; onSect++) {
                final ExamSection esect = this.exam.getSection(onSect);
                final int numProb = esect.getNumProblems();

                for (int onProb = 0; onProb < numProb; onProb++) {
                    final ExamProblem eprob = esect.getProblem(onProb);
                }
            }
        }

        if (ok) {

            // Turn off the "please wait" message
            try {
                SwingUtilities.invokeAndWait(kill);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }

            final ExamSession session = new ExamSession(EExamSessionState.GENERATED, this.exam);

            final ExamDeliverer deliverer =
                    new ExamDeliverer(session, this, this.desk, this.frame, this.demo);

            try {
                SwingUtilities.invokeAndWait(deliverer);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
                ok = false;
            }

            final Runnable killer = new ExamKiller(this.desk, deliverer.getWrapper());

            if (ok) {
                this.inExam = true;

                // Wait for the tester to close the window.
                while (this.inExam && this.frame.isVisible()) {

                    try {
                        Thread.sleep(1000L);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            UpdateExamRequest req = new UpdateExamRequest("111223333", this.exam.ref,
                    Long.valueOf(this.exam.realizationTime), this.exam.exportState(), true, false);
            req.updateTime = Long.valueOf(System.currentTimeMillis());

            final String s1 = req.toXml();
            req = new UpdateExamRequest(s1.toCharArray());

            final String s2 = req.toXml();

            if (!s1.equals(s2)) {
                Log.info(s1);
                Log.info("*** ERROR *** ERROR *** ERROR ***");
                Log.info(s2);
                Log.info("*** ERROR *** ERROR *** ERROR ***");
            }

            // Clear the exam panel from the application desktop
            try {
                SwingUtilities.invokeAndWait(killer);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }

            // "Grade" the exam
            this.exam.finalizeExam();
            this.exam.setCurrentProblem(null, null);

            // TODO: Show solutions...

            final ExamSession session2 = new ExamSession(EExamSessionState.REVIEW_WITH_SOLUTIONS, this.exam);

            final ExamDeliverer deliverer2 = new ExamDeliverer(session2, this, this.desk, this.frame, false);

            try {
                SwingUtilities.invokeAndWait(deliverer2);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
                ok = false;
            }

            final Runnable killer2 = new ExamKiller(this.desk, deliverer2.getWrapper());

            if (ok) {
                this.inExam = true;

                // Wait for the tester to close the window.
                while (this.inExam && this.frame.isVisible()) {

                    try {
                        Thread.sleep(1000L);
                    } catch (final InterruptedException ex) {
                        Log.warning(ex);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // Clear the exam panel from the application desktop
            try {
                SwingUtilities.invokeAndWait(killer2);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }

            killBlockingWindow();
        }

        return ok;
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
            this.inExam = false;
        } else if ("Larger".equals(cmd)) {
            this.examPanel.larger();
        } else if ("Smaller".equals(cmd)) {
            this.examPanel.smaller();
        } else if ("color-white".equals(cmd)) {
            this.exam.setBackgroundColor("white", ColorNames.getColor("white"));
            this.examPanel.updateColor();
        } else if ("color-gold".equals(cmd)) {
            this.exam.setBackgroundColor("gold", ColorNames.getColor("gold"));
            this.examPanel.updateColor();
        } else if ("color-purple".equals(cmd)) {
            this.exam.setBackgroundColor("MediumPurple", ColorNames.getColor("MediumPurple"));
            this.examPanel.updateColor();
        } else if ("color-blue".equals(cmd)) {
            this.exam.setBackgroundColor("MediumTurquoise", ColorNames.getColor("MediumTurquoise"));
            this.examPanel.updateColor();
        }
    }

    /**
     * Launches the remote testing application.
     *
     * @param args command-line arguments - if the arguments list contains "public-internet", the application will
     *             configure itself to operate in a non-proctored, Internet setting
     */
    public static void main(final String... args) {

//        FlatLightLaf.setup();

        // Examine command-line for "demo" flag
        boolean demo = false;
        for (final String arg : args) {
            if ("demo".equals(arg)) {
                demo = true;
                break;
            }
        }

        try {
            final LocalTestingApp app = new LocalTestingApp("https", ClientBase.DEFAULT_HOST, ClientBase.DEFAULT_PORT,
                            SessionCache.ANONYMOUS_SESSION, demo);
            app.go();

            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
            }

            System.exit(0);
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }
}

/**
 * Class to display the exam panel in the AWT event thread.
 */
final class ExamDeliverer implements Runnable {

    /** The application owning the exam. */
    private final LocalTestingApp owner;

    /** Exam session. */
    private final ExamSession examSession;

    /** The desktop to which to add the exam. */
    private final JDesktopPane ownerDesk;

    /** The frame containing the desktop. */
    private final JFrame ownerFrame;

    /** True if we're running in demo mode. */
    private final boolean isDemo;

    /** The wrapper for the exam panel. */
    private ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code ExamDeliverer}.
     *
     * @param theExamSession exam session
     * @param theOwner       the application owning the exam
     * @param desk           the desktop to which to add the exam
     * @param frame          the frame containing the desktop
     * @param demo           true if we're running in demo mode
     */
    ExamDeliverer(final ExamSession theExamSession, final LocalTestingApp theOwner,
                  final JDesktopPane desk, final JFrame frame, final boolean demo) {

        this.owner = theOwner;
        this.examSession = theExamSession;
        this.ownerDesk = desk;
        this.ownerFrame = frame;
        this.isDemo = demo;
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
     * Post the exam panel to the screen. This will be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Properties skin = new DefaultSkin();

        if (this.isDemo) {
            skin.setProperty("bottom-bar-lbl", "Submit the Exam for Grading...");
        }

        final ExamPanel examPanel = new ExamPanel(this.owner, skin, "John Doe",
                this.examSession, false, false, null, null);

        this.owner.setExamPanel(examPanel);
        examPanel.addActionListener(this.owner);
        this.wrapper = new ExamPanelWrapper(examPanel);

        // Install the exam panel in the desktop window
        this.ownerDesk.add(this.wrapper);
        this.wrapper.makeFullscreen();
        this.wrapper.buildUI();
        this.wrapper.setVisible(true);

        if (examPanel.getExamSession().getExam().instructions == null) {
            examPanel.pickProblem(0, 0);
        } else {
            examPanel.showInstructions();
        }

        this.examSession.getExam().presentationTime = System.currentTimeMillis();
        if (this.examSession.getState() == EExamSessionState.GENERATED) {
            this.examSession.setState(EExamSessionState.INTERACTING);
        }

        this.ownerFrame.pack();
        this.wrapper.setVisible(true);
    }
}

/**
 * Class to kill the exam panel in the AWT event thread.
 */
final class ExamKiller implements Runnable {

    /** The desktop to which to add the exam. */
    private final JDesktopPane ownerDesk;

    /** The exam panel wrapper. */
    private final ExamPanelWrapper wrapper;

    /**
     * Constructs a new {@code ExamKiller}.
     *
     * @param desk       the desktop to which to add the exam
     * @param theWrapper the exam panel wrapper to be killed
     */
    ExamKiller(final JDesktopPane desk, final ExamPanelWrapper theWrapper) {

        this.ownerDesk = desk;
        this.wrapper = theWrapper;
    }

    /**
     * Posts the exam panel to the screen. This will be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.wrapper.setVisible(false);
        this.ownerDesk.remove(this.wrapper);
        this.wrapper.dispose();
    }
}

/**
 * Class to display the "Please Wait" message in the AWT event thread.
 */
final class ShowPleaseWait implements Runnable {

    /** The desktop to which to add the exam. */
    private final JDesktopPane ownerDesk;

    /** The please-wait panel. */
    private final PleaseWait panel;

    /**
     * Constructs a new {@code ShowPleaseWait}.
     *
     * @param desk     the desktop to which to add the exam
     * @param thePanel the panel to be shown
     */
    ShowPleaseWait(final JDesktopPane desk, final PleaseWait thePanel) {

        this.ownerDesk = desk;
        this.panel = thePanel;
    }

    /**
     * Run method to post the exam panel to the screen. This will be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.ownerDesk.add(this.panel);
        this.panel.centerInDesktop();
        this.panel.setVisible(true);
        this.ownerDesk.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }
}

/**
 * Class to remove the "Please Wait" message in the AWT event thread.
 */
final class KillPleaseWait implements Runnable {

    /** The desktop to which to add the exam. */
    private final JDesktopPane ownerDesk;

    /** The generated exam panel. */
    private final PleaseWait panel;

    /**
     * Constructs a new {@code KillPleaseWait}.
     *
     * @param desk     the desktop to which to add the exam
     * @param thePanel the dialog to be killed
     */
    KillPleaseWait(final JDesktopPane desk, final PleaseWait thePanel) {

        this.ownerDesk = desk;
        this.panel = thePanel;
    }

    /**
     * Posts the exam panel to the screen. This will be run from the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel.setVisible(false);
        this.ownerDesk.remove(this.panel);
        this.panel.dispose();
        this.ownerDesk.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}

/**
 * A class that contains the default settings for the application.
 */
final class DefaultSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -407333323259918970L;

    /**
     * Constructs a new {@code DefaultSkin} properties object.
     */
    DefaultSkin() {

        super();

        final String[][] mContents = {
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
                {"bottom-bar-lbl", "Exit"},
                {"bottom-bar-lbl-show-answers", "Exit"},
                {"bottom-bar-lbl-practice", "Exit"},

                {"show-problem-list", "true"},
                {"show-calculator", "true"},
                {"run-timer", "false"},};

        for (final String[] mContent : mContents) {
            setProperty(mContent[0], mContent[1]);
        }
    }
}
