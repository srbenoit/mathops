package dev.mathops.app.checkout;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.app.FrameToFront;
import dev.mathops.app.PopupPanel;
import dev.mathops.app.TempFileCleaner;
import dev.mathops.app.checkin.FieldPanel;
import dev.mathops.app.checkin.LoginDialog;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStvisitLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawPendingExam;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This is the checkout station application for use in the proctored testing center. It performs two basic functions:
 * checking students out of the testing center, and displaying a map of the stations in the testing center, with the
 * current status of each station reflected in its icon color.
 */
final class CheckOutApp extends KeyAdapter implements Runnable, ActionListener {

    /** An action command. */
    static final String TOP_FIELD_CMD = "TopField";

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
    private JFrame frame = null;

    /** The top panel in the interface. */
    private FieldPanel top = null;

    /** The bottom panel in the interface. */
    private FieldPanel bottom = null;

    /** The center panel in the interface. */
    private CenterPanel center = null;

    /** The current state of the application. */
    private int state = INITIALIZING;

    /** The ID of the student attempting to check out. */
    private String studentId = null;

    /** The context. */
    private DbProfile dbProfile = null;

    /**
     * Constructs a new {@code CheckOutApp}.
     *
     * @param theCenterId the ID of the testing center this application manages
     */
    private CheckOutApp(final String theCenterId) {
        super();

        this.centerId = theCenterId;
    }

    /**
     * The application's main processing method. This should be called after object construction to run the checkout
     * process.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
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
                // Force the person starting the checkout application to log in with a username and password before
                // starting the application.
                doStartupLogin();

                // Now, we create a full-screen, top-level window and activate a thread that will keep it on top of
                // everything else on the desktop. All windows this application creates will be children of this
                // window, so they will not be obscured, but the desktop will not be available.
                createBlockingWindow(fullScreen);

                // Start the thread to keep the window on top
                new Thread(this).start();
                checkoutLoop(cache);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException | RuntimeException ex) {
            Log.severe(ex);
        }

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }

    /**
     * The main loop, which processes checkouts until the program is terminated.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private void checkoutLoop( final Cache cache) throws SQLException {


        // Enter an infinite loop, waiting for checkouts. Each checkout consists of scanning/typing a student
        // ID, looking up the seat assigned to that student, forcing submission of the exam if it is still in
        // progress, then resetting the seat for the next student.
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
            final LocalDateTime now = LocalDateTime.now();
            processStudentCheckout(cache, now);
        }
    }

    /**
     * Attempts to connect to the database server. This will present a dialog box with login information.
     */
    private void doStartupLogin() {

        final DbContext dbctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        // Present a login dialog to gather username, password
        final LoginDialog dlg = new LoginDialog(dbctx.loginConfig.id, dbctx.loginConfig.user);

        while (true) {
            if (dlg.gatherInformation()) {
                final String username = dlg.getUsername();
                final char[] passwordChars = dlg.getPassword();
                final String password = String.valueOf(passwordChars);
                dbctx.loginConfig.setLogin(username, password);

                try {
                    final DbConnection conn = dbctx.checkOutConnection();
                    final Cache cache = new Cache(this.dbProfile, conn);

                    try {
                        cache.getSystemData().getActiveTerm();
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
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the user from accessing other applications while the process is
     * running.
     *
     * @param fullScreen {@code true} to build screen in full-screen mode
     */
    private void createBlockingWindow(final boolean fullScreen) {

        // Construct the window in the AWT dispatcher thread.
        final BlockingWindowBuilder builder = new BlockingWindowBuilder(this, this.dbProfile, this.centerId,
                fullScreen);

        try {
            SwingUtilities.invokeAndWait(builder);

            this.frame = builder.getBuilderFrame();
            this.top = builder.getTopPanel();
            this.bottom = builder.getBottomPanel();
            this.center = builder.getCenterPanel();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (final InvocationTargetException ex) {
            Log.warning(ex);
        }
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
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }

            try {
                Thread.sleep(50L);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
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
    private void processStudentCheckout(final Cache cache, final LocalDateTime now) throws SQLException {

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
                    final String msg1 = SimpleBuilder.concat("Student was checked in to Station ", pc.stationNbr,
                            " AND Station ", comp.stationNbr, CoreConstants.DOT);
                    PopupPanel.showPopupMessage(this, this.center, msg1, null, "Inform a director immediately!",
                            PopupPanel.STYLE_OK);
                    return;
                }
                pc = comp;
            }
        }

        if (pc == null) {
            // No testing stations in this testing center have the student assigned. This is normal if the student
            // finished the exam and received their score.
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
        } else {
            if (pc.currentStatus == null) {
                PopupPanel.showPopupMessage(this, this.center,
                        "Assigned testing station is configured improperly.", null,
                        "(No current status on record for the station.)", PopupPanel.STYLE_OK);
                return;
            }

            final Integer theStatus = pc.currentStatus;

            if (RawClientPc.STATUS_AWAIT_STUDENT.equals(theStatus)
                    || RawClientPc.STATUS_LOGIN_NOCHECK.equals(theStatus)) {

                final String msg1 = SimpleBuilder.concat("Student never signed in at testing station ", pc.stationNbr,
                        CoreConstants.DOT);
                final String btn = PopupPanel.showPopupMessage(this, this.center, msg1, null,
                        "Do you want to cancel the student's check-in?", PopupPanel.STYLE_YES_NO);

                if ("Yes".equals(btn)) {
                    // Cancel the check-in
                    resetStation(cache, pc);
                    verifyNoPending(cache);
                    PopupPanel.showPopupMessage(this, this.center, "Check-in cancelled.", null, null,
                            PopupPanel.STYLE_OK);
                } else {
                    final String msg2 = SimpleBuilder.concat("The student should sign-in at station ", pc.stationNbr,
                            CoreConstants.DOT);
                    PopupPanel.showPopupMessage(this, this.center, msg2, null, null, PopupPanel.STYLE_OK);
                }
            } else if (RawClientPc.STATUS_TAKING_EXAM.equals(theStatus)) {
                final String msg3 = SimpleBuilder.concat("Student's exam is in progress at testing station ",
                        pc.stationNbr, CoreConstants.DOT);
                final String btn = PopupPanel.showPopupMessage(this, this.center, msg3, null,
                        "Do you want to submit the exam for grading?", PopupPanel.STYLE_YES_NO);

                if ("Yes".equals(btn)) {
                    submitExam(cache, pc);
                    verifyNoPending(cache);
                } else {
                    final String msg4 = SimpleBuilder.concat("The student should return to testing station ",
                            pc.stationNbr, CoreConstants.COMMA);
                    PopupPanel.showPopupMessage(this, this.center, msg4, null, " and complete the exam.",
                            PopupPanel.STYLE_OK);
                }
            } else if (RawClientPc.STATUS_EXAM_RESULTS.equals(theStatus)) {
                resetStation(cache, pc);
                verifyNoPending(cache);
                PopupPanel.showPopupMessage(this, this.center, "Student checked out.", null, null, PopupPanel.STYLE_OK);
            } else {
                resetStation(cache, pc);
                verifyNoPending(cache);
                final String msg5a = SimpleBuilder.concat("Testing station ", pc.stationNbr,
                        " was in an invalid state:");
                final String msg5b = SimpleBuilder.concat(theStatus, ".  Please inform a director.");
                PopupPanel.showPopupMessage(this, this.center, msg5a, null, msg5b, PopupPanel.STYLE_OK);
            }

            this.bottom.setMessage(null);
            this.bottom.repaint();
        }
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

        boolean result = true;

        final List<RawPendingExam> exams = RawPendingExamLogic.queryByStudent(cache, this.studentId);

        if (!exams.isEmpty()) {
            PopupPanel.showPopupMessage(this, this.center, "A pending exam still exists for the student", null,
                    "Please send student to the office.", PopupPanel.STYLE_OK);

            result = false;
        }

        return result;
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
        } else
            if (TOP_FIELD_CMD.equals(cmd)) {
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

        if ((int) e.getKeyChar() == 22) {

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

        FlatLightLaf.setup();

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

        final CheckOutApp app = new CheckOutApp(centerId);
        app.runCheckoutApplication(fullScreen);
    }
}

/**
 * Runnable class to be called in the AWT dispatcher thread to construct the blocking window. The window consists of a
 * full-screen {@code JFrame} that contains a top {@code FieldPanel}, a {@code CenterPanel}, and a bottom
 * {@code FieldPanel}.
 */
final class BlockingWindowBuilder implements Runnable {

    /** The listener to install on all panels. */
    private final CheckOutApp listener;

    /** The database profile. */
    private final DbProfile dbProfile;

    /** The ID of the testing center being managed. */
    private final String testingCenterId;

    /** {@code true} to build screen in full-screen mode. */
    private final boolean full;

    /** The frame in which the background desktop pane will live. */
    private JFrame builderFrame = null;

    /** The top panel. */
    private FieldPanel topPanel = null;

    /** The bottom panel. */
    private FieldPanel bottomPanel = null;

    /** The center panel. */
    private CenterPanel centerPanel = null;

    /**
     * Constructs a new {@code BlockingWindowBuilder}.
     *
     * @param theListener  the key listener to install on all panels
     * @param theDbProfile the database profile
     * @param centerId     the ID of the testing center being managed
     * @param fullScreen   {@code true} to build screen in full-screen mode
     */
    BlockingWindowBuilder(final CheckOutApp theListener, final DbProfile theDbProfile, final String centerId,
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
    JFrame getBuilderFrame() {

        return this.builderFrame;
    }

    /**
     * Gets the top panel.
     *
     * @return the top panel
     */
    FieldPanel getTopPanel() {

        return this.topPanel;
    }

    /**
     * Gets the bottom panel.
     *
     * @return the bottom panel
     */
    FieldPanel getBottomPanel() {

        return this.bottomPanel;
    }

    /**
     * Gets the center panel.
     *
     * @return the center panel
     */
    CenterPanel getCenterPanel() {

        return this.centerPanel;
    }

    /**
     * Constructs the blocking window.
     */
    @Override
    public void run() {

        final Dimension screen = this.full ? Toolkit.getDefaultToolkit().getScreenSize() : new Dimension(1280, 720);

        this.builderFrame = new JFrame("Checkout");
        if (this.full) {
            this.builderFrame.setUndecorated(true);
        }
        this.builderFrame.setFocusableWindowState(true);
        this.builderFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout(1, 1));
        content.setPreferredSize(screen);
        this.builderFrame.setContentPane(content);
        content.setBackground(new Color(100, 100, 255));

        this.builderFrame.setSize(screen);
        this.builderFrame.setLocation(0, 0);
        this.builderFrame.setVisible(true);
        this.builderFrame.toFront();
        this.builderFrame.requestFocus();

        this.topPanel = new FieldPanel(screen, this.listener, CheckOutApp.TOP_FIELD_CMD);
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
