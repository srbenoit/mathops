package dev.mathops.app.checkout;

import dev.mathops.app.FrameToFront;
import dev.mathops.app.PopupPanel;
import dev.mathops.app.TempFileCleaner;
import dev.mathops.app.checkin.FieldPanel;
import dev.mathops.app.checkin.LoginDialog;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ChangeUI;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawlogic.RawClientPcLogic;
import dev.mathops.db.rawlogic.RawPendingExamLogic;
import dev.mathops.db.rawlogic.RawStexamLogic;
import dev.mathops.db.rawlogic.RawStvisitLogic;
import dev.mathops.db.rawrecord.RawClientPc;
import dev.mathops.db.rawrecord.RawPendingExam;
import dev.mathops.db.svc.term.TermLogic;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This is the checkout station application for use in the proctored testing center. It performs two basic functions:
 * checking students out of the testing center, and displaying a map of the stations in the testing center, with the
 * current status of each station reflected in its icon color.
 */
final class CheckoutApp extends KeyAdapter implements Runnable, ActionListener {

    /** The initializing state. */
    private static final int INITIALIZING = 0;

    /** State when the user is logging in to the database server. */
    private static final int LOGGING_IN = 1;

    /** Ground state in which student ID is to be scanned/entered. */
    private static final int AWAIT_STUDENT = 2;

    /** State indicating student is being analyzed. */
    private static final int ANALYZE_STUDENT = 3;

    /** State indicating entered student ID is bad. */
    private static final int BAD_STUDENT_ID = 4;

    /** Timeout (milliseconds) to wait for a forced submission to conclude. */
    private static final int FORCED_SUBMIT_TIMEOUT = 15000; // 15 seconds

    /** The testing center ID. */
    private final String centerId;

    /** The main frame for the application. */
    private JFrame frame;

    /** The top panel in the interface. */
    private FieldPanel top;

    /** The bottom panel in the interface. */
    private FieldPanel bottom;

    /** The center panel in the interface. */
    private CenterPanel center;

    /** The current state of the application. */
    private int state = INITIALIZING;

    /** The ID of the student attempting to check out. */
    private String studentId;

    /** The context. */
    private DbProfile dbProfile;

    /**
     * Constructs a new {@code CheckoutApp}.
     *
     * @param theCenterId the ID of the testing center this application manages
     */
    private CheckoutApp(final String theCenterId) {
        super();

        this.centerId = theCenterId;
    }

    /**
     * The application's main processing method. This should be called after object construction to run the checkout
     * process.
     *
     * @param fullScreen {@code true} to build screen in fullscreen mode
     */
    private void runCheckoutApplication(final boolean fullScreen) {

        // Clear out old Java font files from the temporary directory.
        TempFileCleaner.clean();

        this.dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.CHECKOUT_PATH);
        if (this.dbProfile == null) {
            throw new IllegalArgumentException("No 'checkout' code profile configured");
        }

        final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                // Force the person starting the checkout application to log in with a username and
                // password before starting the application, and create the backing store connection
                // to the database using this login information.
                doStartupLogin();

                // Now, we create a full-screen, top-level window and activate a thread that will
                // keep it on top of everything else on the desktop. All windows this application
                // creates will be children of this window, so they will not be obscured, but the
                // desktop will not be available.
                if (!createBlockingWindow(fullScreen)) {
                    return;
                }

                // Start the thread to keep the window on top
                new Thread(this).start();

                // Enter an infinite loop, waiting for checkouts. Each checkout consists of
                // scanning/typing a student ID, looking up the seat assigned to that student,
                // forcing submission of the exam if it is still in progress, then resetting the
                // seat for the next student.
                Log.info("Entering checkout process.");

                while (this.frame.isVisible()) {
                    this.bottom.setMessage("CHECK OUT");
                    this.bottom.repaint();

                    // Make sure we have focus, so we get keyboard & mouse events.
                    this.top.grabFocus();

                    // Prompt for the student ID
                    this.state = AWAIT_STUDENT;
                    this.studentId = null;
                    this.top.setMessage("Student ID: ");
                    this.top.setFieldValue(9, CoreConstants.EMPTY);
                    this.top.repaint();

                    this.frame.invalidate();
                    this.frame.repaint();

                    // Wait for an ID to be submitted (see <code>keyTyped</code> handler for how
                    // this occurs)
                    while (this.frame.isVisible() && this.state == AWAIT_STUDENT) {

                        // Prevent this loop from consuming too much CPU
                        try {
                            Thread.sleep(50L);
                        } catch (final InterruptedException e) {
                            // No action
                        }
                    }

                    if (this.state != ANALYZE_STUDENT) {
                        if (this.state == BAD_STUDENT_ID) {
                            PopupPanel.showPopupMessage(this, this.center, "Invalid student ID.", null, null,
                                    PopupPanel.STYLE_OK);
                        }

                        continue;
                    }

                    // We have a 9-digit student ID, so attempt to process it.
                    processStudentCheckout(cache, LocalDateTime.now());
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final Exception ex) {
            Log.severe(ex);
        }

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }

    /**
     * Attempts to connect to the database server. This will present a dialog box with login information.
     *
     * @throws SQLException if the database connection could not be established
     */
    private void doStartupLogin() throws SQLException {

        final DbContext dbctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        // Present a login dialog to gather username, password
        for (; ; ) {
            final LoginDialog dlg = new LoginDialog(dbctx.loginConfig.id, dbctx.loginConfig.user);

            if (dlg.gatherInformation()) {
                dbctx.loginConfig.setLogin(dlg.getUsername(), String.valueOf(dlg.getPassword()));
            } else {
                dlg.close();
                throw new SQLException("Login canceled by user");
            }

            try {
                final DbConnection conn = dbctx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    TermLogic.get(cache).queryActive(cache);
                    dlg.close();
                    break;
                } finally {
                    dbctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                JOptionPane.showMessageDialog(null, "Unable to connect to database");
            }
        }
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the user from accessing other applications while the process is
     * running.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
     * @return {@code true} if the blocking window was created; {@code false} otherwise
     */
    private boolean createBlockingWindow(final boolean fullScreen) {

        // Construct the window in the AWT dispatcher thread.
        final BlockingWindowBuilder builder = new BlockingWindowBuilder(this, this.dbProfile, this.centerId,
                fullScreen);

        try {
            SwingUtilities.invokeAndWait(builder);
        } catch (final Exception e) {
            // No action
        }

        this.frame = builder.getFrame();
        this.top = builder.getTop();
        this.bottom = builder.getBottom();
        this.center = builder.getCenter();

        // Start the thread that will keep the blocking window on top of all other objects on the
        // desktop.

        // new Thread(this).start();

        return true;
    }

    /**
     * Keeps the blocking window on top of all other windows.
     */
    @Override
    public void run() {

        final Runnable obj = new FrameToFront(this.frame);

        while (this.frame.isVisible()) {

            try {
                SwingUtilities.invokeAndWait(obj);
            } catch (final Exception e) {
                // No action
            }

            try {
                Thread.sleep(50L);
            } catch (final InterruptedException e) {
                // No action
            }
        }
    }

    /**
     * Perform checkout processing.
     *
     * @param cache the data cache
     * @param now   the date/time to consider as "now"
     * @throws SQLException if there was an error accessing the database
     */
    private void processStudentCheckout(final Cache cache, final LocalDateTime now)
            throws SQLException {

        RawStvisitLogic.endInProgressVisit(cache, this.studentId, now);

        final List<RawClientPc> comps = RawClientPcLogic.queryByTestingCenter(cache, this.centerId);
        if (comps.isEmpty()) {
            PopupPanel.showPopupMessage(this, this.center, "Unable to query testing stations", null,
                    "Inform a director immediately!", PopupPanel.STYLE_OK);
            return;
        }

        // Find which computer the student is currently assigned to.
        RawClientPc pc = null;

        for (final RawClientPc comp : comps) {

            if (this.studentId.equals(comp.currentStuId)) {
                if (pc != null) {
                    PopupPanel.showPopupMessage(this, this.center,
                            "Student was checked in to Station " + pc.stationNbr + " AND Station " + comp.stationNbr
                                    + CoreConstants.DOT, null, "Inform a director immediately!", PopupPanel.STYLE_OK);

                    return;
                }

                pc = comp;
            }
        }

        if (pc == null) {

            // No testing stations in this testing center have the student assigned. This is normal
            // if the student finished the exam and received their score.
            if (verifyNoPending(cache)) {

                // There is no pending exam, so verify that there was an exam submitted for the user within
                // the last few minutes (to catch impersonators using their own ID to check out).
                final boolean verified = RawStexamLogic.verifyRecentExam(cache, now, this.studentId);

                if (verified) {
                    PopupPanel.showPopupMessage(this, this.center, null, "Student checked out.", null,
                            PopupPanel.STYLE_OK);
                } else {
                    PopupPanel.showPopupMessage(this, this.center,
                            "There is no record of an exam taken by this student recently.", null,
                            "Inform a director immediately!", PopupPanel.STYLE_OK);
                }
            }

            return;
        }

        if (pc.currentStatus == null) {
            PopupPanel.showPopupMessage(this, this.center,
                    "Assigned testing station is configured improperly.", null,
                    "(No current status on record for the station.)", PopupPanel.STYLE_OK);
            return;
        }

        final Integer theStatus = pc.currentStatus;

        if (RawClientPc.STATUS_AWAIT_STUDENT.equals(theStatus)
                || RawClientPc.STATUS_LOGIN_NOCHECK.equals(theStatus)) {

            final String btn = PopupPanel.showPopupMessage(this, this.center,
                    "Student never signed in at testing station " + pc.stationNbr + CoreConstants.DOT,
                    null, "Do you want to cancel the student's check-in?", PopupPanel.STYLE_YES_NO);

            if ("Yes".equals(btn)) {
                // Cancel the check-in
                resetStation(cache, pc);
                verifyNoPending(cache);
                PopupPanel.showPopupMessage(this, this.center, "Check out cancelled.", null, null, PopupPanel.STYLE_OK);
            } else {
                PopupPanel.showPopupMessage(this, this.center,
                        "The student should sign-in at station " + pc.stationNbr + CoreConstants.DOT,
                        null, null, PopupPanel.STYLE_OK);
            }
        } else if (RawClientPc.STATUS_TAKING_EXAM.equals(theStatus)) {
            final String btn = PopupPanel.showPopupMessage(this, this.center,
                    "Student's exam is in progress at testing station " + pc.stationNbr + CoreConstants.DOT,
                    null, "Do you want to submit the exam for grading?", PopupPanel.STYLE_YES_NO);

            if ("Yes".equals(btn)) {
                // Submit the exam.
                submitExam(cache, pc);
                verifyNoPending(cache);
            } else {
                PopupPanel.showPopupMessage(this, this.center,
                        "The student should return to testing station " + pc.stationNbr + CoreConstants.COMMA,
                        null, " and complete the exam.", PopupPanel.STYLE_OK);
            }
        } else if (RawClientPc.STATUS_EXAM_RESULTS.equals(theStatus)) {
            resetStation(cache, pc);
            verifyNoPending(cache);
            PopupPanel.showPopupMessage(this, this.center, "Student checked out.", null, null, PopupPanel.STYLE_OK);
        } else {
            resetStation(cache, pc);
            verifyNoPending(cache);
            PopupPanel.showPopupMessage(this, this.center,
                    "Testing station " + pc.stationNbr + " was in an invalid state:", null,
                    theStatus + ".  Please inform a director.", PopupPanel.STYLE_OK);
        }

        this.bottom.setMessage(null);
        this.bottom.repaint();
    }

    /**
     * Resets a testing station by clearing its current test-taker data and resetting its state to a Locked state.
     *
     * @param cache the data cache
     * @param pc    the testing station to reset
     * @throws SQLException if there was an error accessing the database
     */
    private static void resetStation(final Cache cache, final RawClientPc pc) throws SQLException {

        Integer theState = RawClientPc.STATUS_LOCKED;

        if (RawClientPc.USAGE_PAPER.equals(pc.pcUsage)) {
            theState = RawClientPc.STATUS_PAPER_ONLY;
        }

        RawClientPcLogic.updateAllCurrent(cache, pc.computerId, theState, null, null, null, null);
    }

    /**
     * Force an exam that was left in progress to be submitted and graded, then clear the testing station.
     *
     * @param cache the data cache
     * @param pc    the testing station that the exam is active on
     * @throws SQLException if there is an error accessing the database
     */
    private void submitExam(final Cache cache, final RawClientPc pc) throws SQLException {

        // Show a message indicating what's going on
        PopupPanel panel = new PopupPanel(this, "Terminating exam in progress", null, "Please wait...",
                PopupPanel.STYLE_NO_BUTTONS);
        this.center.add(panel);
        panel.centerInDesktop();
        panel.setVisible(true);

        // Put the computer in the forced submission state
        RawClientPcLogic.updateCurrentStatus(cache, pc.computerId, RawClientPc.STATUS_FORCE_SUBMIT);

        // Now wait for the computer to change to the LOCKED state, or for a timeout
        final long timeout = System.currentTimeMillis() + (long) FORCED_SUBMIT_TIMEOUT;

        while (System.currentTimeMillis() < timeout) {

            // Wait one second between polling the station status
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            final RawClientPc updated = RawClientPcLogic.query(cache, pc.computerId);
            if (updated == null || updated.currentStatus == null) {

                // Error - change the panel to show an error message
                panel.setVisible(false);
                this.center.remove(panel);
                panel.dispose();
                panel = null;
                PopupPanel.showPopupMessage(this, this.center, "Error while trying to abort exam.", null,
                        "Please inform a director immediately", PopupPanel.STYLE_OK);
                break;
            }

            if (RawClientPc.STATUS_LOCKED.equals(updated.currentStatus)) {
                break;
            }
        }

        // Clear the message if one is still showing.
        if (panel != null) {
            panel.setVisible(false);
            this.center.remove(panel);
            panel.dispose();
        }
    }

    /**
     * Tests to be sure there are no pending exams left on record for the student checking out. If there is, it
     * indicates some error.
     *
     * @param cache the data cache
     * @return {@code true} if no pending row was found, {@code false} if a row was found
     * @throws SQLException if there is an error accessing the database
     */
    private boolean verifyNoPending(final Cache cache) throws SQLException {

        final List<RawPendingExam> exams =
                RawPendingExamLogic.queryByStudent(cache, this.studentId);

        if (!exams.isEmpty()) {
            PopupPanel.showPopupMessage(this, this.center, "A pending exam still exists for the student", null,
                    "Please send student to the office.", PopupPanel.STYLE_OK);

            return false;
        }

        return true;
    }

    /**
     * Handler for actions generated by application panels.
     *
     * @param e the action event generated by the panel
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if ("Login".equals(cmd)) {
            this.state = LOGGING_IN;
        } else if ("TopField".equals(cmd)) {
            final String value = this.top.getFieldValue();

            if (value.length() == 9) {
                this.studentId = value;
                this.state = ANALYZE_STUDENT;
            } else if ("exit".equalsIgnoreCase(value)) {
                System.exit(0);
            } else {
                this.state = BAD_STUDENT_ID;
            }
        }
    }

    /**
     * Handler for key typed events. The top panel will fire an action event when the ENTER key is typed, indicating a
     * student ID has been entered.
     *
     * @param e the key typed event
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        if (e.getKeyChar() == 22) {

            final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                final Object data = clip.getData(DataFlavor.stringFlavor);

                if (data != null) {
                    final char[] chars = data.toString().toCharArray();
                    for (final char aChar : chars) {
                        this.top.addToFieldValue(aChar);
                    }
                }
            } catch (final UnsupportedFlavorException | IOException ex) {
                Log.warning(ex);
            }
        }

        this.top.addToFieldValue(e.getKeyChar());
    }

    /**
     * Main method that launches the remote testing application.
     *
     * @param args command-line arguments: first (optional) argument is the testing center ID, and second (optional) is
     *             "windowed" to run in windowed mode
     */
    public static void main(final String... args) {

        ChangeUI.changeUI();

        boolean fullScreen = true;
        String centerId = "1";

        for (final String arg : args) {
            if ("windowed".equals(arg)) {
                fullScreen = false;
            } else {
                centerId = arg;
            }
        }

        ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final CheckoutApp app = new CheckoutApp(centerId);

        app.runCheckoutApplication(fullScreen);

        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Log.warning(ex);
        }

        System.exit(0);
    }
}

/**
 * Runnable class to be called in the AWT dispatcher thread to construct the blocking window. The window consists of a
 * fullscreen {@code JFrame} that contains a top {@code FieldPanel}, a {@code CenterPanel}, and a bottom
 * {@code FieldPanel}.
 */
final class BlockingWindowBuilder implements Runnable {

    /** The listener to install on all panels. */
    private final CheckoutApp listener;

    /** The database profile. */
    private final DbProfile dbProfile;

    /** The ID of the testing center being managed. */
    private final String testingCenterId;

    /** {@code true} to build screen in full-screen mode. */
    private final boolean full;

    /** The frame in which the background desktop pane will live. */
    private JFrame builderFrame;

    /** The top panel. */
    private FieldPanel topPanel;

    /** The bottom panel. */
    private FieldPanel bottomPanel;

    /** The center panel. */
    private CenterPanel centerPanel;

    /**
     * Constructs a new {@code BlockingWindowBuilder}.
     *
     * @param theListener  the key listener to install on all panels
     * @param theDbProfile the database profile
     * @param centerId     the ID of the testing center being managed
     * @param fullScreen   {@code true} to build screen in full-screen mode
     */
    BlockingWindowBuilder(final CheckoutApp theListener, final DbProfile theDbProfile, final String centerId,
                          final boolean fullScreen) {

        this.listener = theListener;
        this.dbProfile = theDbProfile;
        this.testingCenterId = centerId;
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
     * Gets the top panel.
     *
     * @return the top panel
     */
    public FieldPanel getTop() {

        return this.topPanel;
    }

    /**
     * Gets the bottom panel.
     *
     * @return the bottom panel
     */
    public FieldPanel getBottom() {

        return this.bottomPanel;
    }

    /**
     * Gets the center panel.
     *
     * @return the center panel
     */
    public CenterPanel getCenter() {

        return this.centerPanel;
    }

    /**
     * Constructs the blocking window.
     */
    @Override
    public void run() {

        final Dimension screen = this.full ? Toolkit.getDefaultToolkit().getScreenSize() : new Dimension(1040, 920);

        this.builderFrame = new JFrame("Checkout");
        this.builderFrame.setUndecorated(true);
        this.builderFrame.setFocusableWindowState(true);

        final JPanel content = new JPanel(new BorderLayout(1, 1));
        content.setPreferredSize(screen);
        this.builderFrame.setContentPane(content);
        content.setBackground(new Color(100, 100, 255));

        this.builderFrame.pack();
        this.builderFrame.setSize(screen);
        this.builderFrame.setLocation(0, 0);
        this.builderFrame.setVisible(true);
        this.builderFrame.toFront();
        this.builderFrame.requestFocus();

        this.topPanel = new FieldPanel(screen, this.listener, "TopField");
        content.add(this.topPanel, BorderLayout.PAGE_START);
        this.centerPanel = new CenterPanel(this.dbProfile, this.testingCenterId);
        content.add(this.centerPanel, BorderLayout.CENTER);
        this.bottomPanel = new FieldPanel(screen, this.listener, "BottomField");
        content.add(this.bottomPanel, BorderLayout.PAGE_END);

        this.topPanel.addKeyListener(this.listener);
        this.centerPanel.addKeyListener(this.listener);
        this.bottomPanel.addKeyListener(this.listener);
        content.addKeyListener(this.listener);
        this.builderFrame.getGlassPane().addKeyListener(this.listener);

        this.builderFrame.pack();

        // Start the map updater thread.
        new Thread(this.centerPanel).start();
    }
}
