package dev.mathops.app.checkin;

import dev.mathops.app.FrameToFront;
import dev.mathops.app.PopupPanel;
import dev.mathops.app.TempFileCleaner;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ChangeUI;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawStvisitLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is the check-in station application for use in the proctored testing center. It performs two basic functions:
 * checking students into the testing center, and displaying a map of the stations in the testing center, with the
 * current status of each station reflected in its icon color.
 */
final class CheckinApp extends KeyAdapter implements Runnable, ActionListener {

    /** The initializing state. */
    private static final int INITIALIZING = 0;

    /** State when the entered username/password are being processed. */
    private static final int LOGGING_IN = 2;

    /** Ground state in which student ID is to be scanned/entered. */
    private static final int AWAIT_STUDENT = 3;

    /** State indicating student's eligibility is being analyzed. */
    private static final int ANALYZE_STUDENT = 4;

    /** State indicating entered student ID is bad. */
    private static final int BAD_STUDENT_ID = 5;

    /** State indicating exam is being selected. */
    private static final int CHOOSING_EXAM = 6;

    /** State indicating exam is being issued. */
    private static final int ISSUE_EXAM = 7;

    /** Minimum distance to try to seat people doing the same problems. */
    private static final int THRESHOLD = 200;

    /** The testing center ID. */
    private final String centerId;

    /** The database profile to use. */
    private DbProfile dbProfile;

    /** The main frame for the application. */
    private JFrame frame;

    /** The top panel in the interface. */
    private FieldPanel top;

    /** The bottom panel in the interface. */
    private BottomPanel bottom;

    /** The center panel in the interface. */
    private CenterPanel center;

    /** The current state of the application. */
    private int state = INITIALIZING;

    /** The ID of the student attempting to check in. */
    private String studentId;

    /** Information on the student attempting to check in. */
    private DataCheckInAttempt info;

    /** Random number generator used to assign seats. */
    private final Random random;

    /**
     * Constructs a new {@code CheckInApp}.
     *
     * @param theCenterId the ID of the testing center this application manages
     */
    private CheckinApp(final String theCenterId) {

        super();

        this.centerId = theCenterId;
        final long seed = System.currentTimeMillis();
        this.random = new Random(seed);
    }

    /**
     * The application's main processing method. This should be called after object construction to run the check-in
     * process.
     *
     * @param now        the date/time to consider as "now"
     * @param fullScreen {@code true} to build screen in full-screen mode
     */
    private void runCheckInApplication(final ZonedDateTime now, final boolean fullScreen) {

        this.dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.CHECKIN_PATH);
        if (this.dbProfile == null) {
            throw new IllegalArgumentException("No 'checkin' code profile configured");
        }

        final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        long nextStatusUpdate = 0L;

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                // Clear out old Java font files from the temporary directory.
                TempFileCleaner.clean();

                // Force the person starting the checkin application to log in with a username and password before
                // starting the application, and create the backing store connection to the database using this login
                // information.
                doStartupLogin();

                // Before we begin, we initialize checkin logic. This provides a quick validation that our database
                // connection is working.
                final LogicCheckIn logic = new LogicCheckIn(cache, now);

                // Now, we create a full-screen, top-level window and activate a thread that will keep it on top of
                // everything else on the desktop. All windows this application creates will be children of this
                // window, so they will not be obscured, but the desktop will not be available.
                if (logic.isInitialized()) {
                    createBlockingWindow(fullScreen);

                    final LocalTime closing = determineClosing(cache);

                    this.bottom.setMessage("CHECK IN");
                    this.bottom.setClosingTime(closing);

                    // Start the thread to keep the window on top
                    new Thread(this).start();

                    // Enter an infinite loop, waiting for checkins. Each checkin consists of scanning/typing a student
                    // ID, displaying the courses or special exams the student could possibly take, and allowing the
                    // staff member to select the exam to assign to the student.
                    while (this.frame.isVisible()) {

                        // Make sure we have focus, so we get keyboard & mouse events.
                        this.top.grabFocus();

                        // Prompt for the student ID
                        this.state = AWAIT_STUDENT;
                        this.studentId = null;
                        this.info = null;
                        this.top.setMessage("Student ID: ");
                        this.top.setFieldValue(9, CoreConstants.EMPTY);
                        this.top.repaint();

                        this.frame.invalidate();
                        this.frame.repaint();

                        // Wait for an ID to be submitted.
                        while (this.frame.isVisible() && this.state == AWAIT_STUDENT) {

                            final long timestamp = System.currentTimeMillis();
                            if (timestamp > nextStatusUpdate) {
                                this.bottom.refresh();
                                updateClientStatus();
                                nextStatusUpdate = timestamp + 5000L;
                            }

                            try {
                                Thread.sleep(50L);
                            } catch (final InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        if (this.frame.isVisible()) {
                            if (this.state != ANALYZE_STUDENT) {
                                if (this.state == BAD_STUDENT_ID) {
                                    PopupPanel.showPopupMessage(this, this.center, "Invalid student ID.", null, null,
                                            PopupPanel.STYLE_OK);
                                }

                                continue;
                            }

                            // We have a 9-digit student ID, so attempt to process it.
                            processStudentCheckin(cache, logic);
                        }
                    }
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.severe(ex);
        }

        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }

    /**
     * Attempts to determine the closing time from the "start_dt1" record in CAMPUS_CALENDAR.
     *
     * @param cache the data cache
     * @return the determined local time; null if unable to determine
     */
    private static LocalTime determineClosing(final Cache cache) {

        final LocalDate today = LocalDate.now();
        final DayOfWeek weekday = today.getDayOfWeek();

        LocalTime closing = null;

        final List<RawCampusCalendar> calendarRows;
        try {
            calendarRows = cache.getSystemData().getCampusCalendarsByType(RawCampusCalendar.DT_DESC_START_DATE_1);

            if (calendarRows.isEmpty()) {
                Log.warning("Unable to query 'start_dt1' calender record");
            } else {
                final RawCampusCalendar row = calendarRows.getFirst();

                String end = null;
                if ("Monday - Thursday".equals(row.weekdays1)
                        && (weekday == DayOfWeek.MONDAY || weekday == DayOfWeek.TUESDAY
                        || weekday == DayOfWeek.WEDNESDAY || weekday == DayOfWeek.THURSDAY)) {
                    end = row.closeTime1;
                } else if ("Monday - Friday".equals(row.weekdays1)) {
                    end = row.closeTime1;
                } else if ("Friday".equals(row.weekdays2) && weekday == DayOfWeek.FRIDAY) {
                    end = row.closeTime2;
                } else {
                    Log.warning("Unable to determine which close time to use.");
                }

                if (end != null) {
                    final int endLen = end.length();
                    final String hours = end.substring(0, endLen - 6);
                    final int hour = 12 + Integer.parseInt(hours);

                    try {
                        if (end.endsWith(":00 pm")) {
                            closing = LocalTime.of(hour, 0);
                        } else if (end.endsWith(":15 pm")) {
                            closing = LocalTime.of(hour, 15);
                        } else if (end.endsWith(":30 pm")) {
                            closing = LocalTime.of(hour, 30);
                        } else if (end.endsWith(":45 pm")) {
                            closing = LocalTime.of(hour, 45);
                        } else {
                            Log.warning("Unsupported format for close time");
                        }
                    } catch (final NumberFormatException ex) {
                        Log.warning("Unsupported value for close time", ex);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Unable to query calender table", ex);
        }

        return closing;
    }

    /**
     * Scans the client computers and attempts to update the status to conform to the desired usage, if in an
     * inconsistent state and there is no student using the station.
     *
     * <p>
     * If any computers have a Usage field that indicates {@code EComputerUsage.PAPER} but are in the t
     */
    private void updateClientStatus() {

        final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);
            try {
                final List<RawClientPc> clients = RawClientPcLogic.queryByTestingCenter(cache, this.centerId);

                for (final RawClientPc client : clients) {
                    final String usage = client.pcUsage;

                    if (RawClientPc.USAGE_PAPER.equals(usage)) {
                        if (RawClientPc.STATUS_LOCKED.equals(client.currentStatus)) {

                            RawClientPcLogic.updateCurrentStatus(cache, client.computerId,
                                    RawClientPc.STATUS_PAPER_ONLY);
                        }
                    } else if ((RawClientPc.USAGE_ONLINE.equals(usage) || RawClientPc.USAGE_BOTH.equals(usage))
                            && RawClientPc.STATUS_PAPER_ONLY.equals(client.currentStatus)) {

                        RawClientPcLogic.updateCurrentStatus(cache, client.computerId,
                                RawClientPc.STATUS_LOCKED);
                    }
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Attempts to connect to the database server. This will present a dialog box with login information.
     */
    private void doStartupLogin() {

        final DbContext dbctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);

        final LoginDialog dlg = new LoginDialog(dbctx.loginConfig.id, dbctx.loginConfig.user);

        // Present a login dialog to gather username, password
        for (; ; ) {
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
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * For the selected student ID, looks up the student information, and populates the {@code StudentCheckInInfo}
     * object with eligibility information, which will include holds, exams the student is eligible for.
     *
     * @param cache the data cache
     * @param logic the implementation of check-in logic to use
     * @throws SQLException if there is an error accessing the database
     */
    private void processStudentCheckin(final Cache cache, final LogicCheckIn logic) throws SQLException {

        // Perform check-in logic, resulting in a check-in info structure for the student, or null on any error.
        this.info = logic.performCheckInLogic(this.studentId, true);

        if (this.info != null) {
            // If there were any errors, display them and abort
            if (this.info.error != null) {

                if (this.info.error.length == 2) {
                    PopupPanel.showPopupMessage(this, this.center, this.info.error[0], null, this.info.error[1],
                            PopupPanel.STYLE_OK);
                } else {
                    PopupPanel.showPopupMessage(this, this.center, this.info.error[0], null, CoreConstants.EMPTY,
                            PopupPanel.STYLE_OK);
                }

                return;
            }

            // Display the student name at the bottom of the screen.
            final RawStudent student = this.info.studentData.student;
            final String name = student.getScreenName();
            this.bottom.setMessage(name);

            // There is no pending exam, so process the check-in now.
            displayHolds(this.info);

            // Present the complete list of exams on the screen with the unavailable exams dimmed, and the available
            // exams lit.

            // Enter a loop to handle mistakes when choosing the exam. The staff member may go back to the exams list
            // after selecting an exam in error.
            this.state = CHOOSING_EXAM;

            while (this.state == CHOOSING_EXAM) {
                // Display the list of exams available to the student.
                this.center.showAvailableExams(this.info);

                // Wait for the staff member to choose one of the exams, or cancel out of the transaction.
                while (this.frame.isVisible() && this.state == CHOOSING_EXAM) {
                    try {
                        Thread.sleep(100L);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Clear the displayed list of exams.
                this.center.hideAvailableExams();

                // If an exam was selected, go on to process the selection.
                if (this.state == ISSUE_EXAM) {

                    // Reserve a seat for the exam, or show an error message if there are no seats that can accommodate
                    // the exam selected.
                    final boolean ok = reserveSeat(cache);

                    // Display the selected exam for confirmation. For a paper exam, confirmation consists of scanning
                    // a calculator. For an online exam, there is a button to press to confirm. In either case, there
                    // is a "Back" button to return to the exams list.
                    if (ok) {
                        if (confirmIssuance()) {
                            startExam(cache);
                        } else {
                            // Not confirmed, return to selecting exam
                            this.state = CHOOSING_EXAM;
                        }
                    } else {
                        this.studentId = null;
                        this.info = null;
                        this.state = AWAIT_STUDENT;
                    }
                }
            }

            this.bottom.setMessage("CHECK IN");
            this.bottom.repaint();
        }
    }

    /**
     * Tests whether there are holds that will prevent the student from testing.
     *
     * @param checkInInfo the data object that contains the holds to display
     */
    private void displayHolds(final DataCheckInAttempt checkInInfo) {

        final List<String> holds = checkInInfo.studentData.holdsToShow;

        if (holds != null) {
            for (String str : holds) {
                // Eliminate runs of multiple spaces
                while (str.contains("  ")) {
                    str = str.replace("  ", CoreConstants.SPC);
                }

                PopupPanel.showPopupMessage(this, this.center, "Inform the student of the following HOLD:", null, str,
                        PopupPanel.STYLE_OK);
            }
        }
    }

    /**
     * Given a selected course/unit for a proctored exam, determines a free seat within the testing center that can
     * support that exam, if any seats are free which can accommodate the selected exam.
     *
     * @param cache      the data cache
     * @return {@code true} if a seat was reserved, {@code false} if it could not be
     * @throws SQLException if there is an error accessing the database
     */
    private boolean reserveSeat(final Cache cache) throws SQLException {

        final List<RawClientPc> choices;
        boolean ok = false;

        // Query all testing_station records with a desired state (STATE_LOCKED or
        // STATE_PAPER_ONLY), based on paper/online status of the exam. Then for each computer,
        // compute the distance to the nearest station running the same course (and unit).

        final Integer theStatus = RawClientPc.STATUS_LOCKED;

        final List<RawClientPc> clients = RawClientPcLogic.queryByTestingCenter(cache, this.centerId);
        if (clients.isEmpty()) {
            return false;
        }

        final int numClients = clients.size();
        final Double[] distances = new Double[numClients];

        for (int i = 0; i < numClients; i++) {

            final RawClientPc client = clients.get(i);

            // If the seat already has this student ID, it's an error
            if (client.currentStuId != null && client.currentStuId.equals(this.studentId)
                    && client.currentStatus != null) {

                final Integer status = client.currentStatus;

                if (RawClientPc.STATUS_AWAIT_STUDENT.equals(status)
                        || RawClientPc.STATUS_TAKING_EXAM.equals(status)
                        || RawClientPc.STATUS_FORCE_SUBMIT.equals(status)
                        || RawClientPc.STATUS_CANCEL_EXAM.equals(status)
                        || RawClientPc.STATUS_LOGIN_NOCHECK.equals(status)) {
                    PopupPanel.showPopupMessage(this, this.center, "Student already at seat " + client.stationNbr,
                            null, null, PopupPanel.STYLE_OK);
                    Log.info("Student ", this.studentId, " already at seat ", client.stationNbr);

                    return false;
                }
            }

            // Computers not in the desired state are ineligible.
            if (client.currentStatus == null || !client.currentStatus.equals(theStatus)) {
                continue;
            }

            // This is an eligible computer, so find the shortest distance to any other system running
            // this course/unit
            double min = Double.MAX_VALUE;

            for (int j = 0; j < numClients; j++) {

                if (i == j) {
                    continue; // Don't measure distance to itself
                }

                final RawClientPc test = clients.get(j);

                // Ignore systems not running an exam or not the current course.
                if (test.currentCourse == null || !test.currentCourse.equals(this.info.selections.course)) {
                    continue;
                }

                // If the exams are different units and each exam is below unit 5 (the final), then
                // they aren't "similar" and can be seated near each other.
                if (test.currentUnit != null && client.currentUnit != null
                        && test.currentUnit.intValue() < 5 && client.currentUnit.intValue() < 5
                        && client.currentUnit.intValue() != test.currentUnit.intValue()) {
                    continue;
                }

                // If we get here, the system is running a similar exam, so we compute the
                // distance, and see if this is the nearest yet.
                if (client.iconX != null && client.iconY != null && test.iconX != null && test.iconY != null) {

                    // Compute distance using iconX and iconY
                    final int clientX = client.iconX.intValue();
                    final int testX = test.iconX.intValue();
                    final int clientY = client.iconY.intValue();
                    final int testY = test.iconY.intValue();
                    final double dx = (double)(clientX - testX);
                    final double dy = (double)(clientY - testY);

                    final double distSq = dx * dx + dy *dy;
                    final double dist = Math.sqrt(distSq);

                    if (dist < min) {
                        min = dist;
                    }
                }
            }

            // Store the distance to the nearest system running a similar exam.
            distances[i] = Double.valueOf(min);
        }

        // Determine the largest minimum distance in the list, trying first to honor wheelchair
        // status, then falling back to assigning any open seat if none of the matching status
        // machines are more than THRESHOLD minimum distance.
        double min = 0.0;

        for (int i = 0; i < numClients; i++) {
            if (RawClientPc.USAGE_WHEELCHAIR.equals(clients.get(i).pcUsage) && distances[i] != null
                    && distances[i].doubleValue() >= min) {
                min = distances[i].doubleValue();
            }
        }

        if (min < (double) THRESHOLD) {
            for (int i = 0; i < numClients; i++) {
                if (distances[i] != null && distances[i].doubleValue() >= min) {
                    min = distances[i].doubleValue();
                }
            }
        }

        // If min > THRESHOLD, just use THRESHOLD
        if (min > (double) THRESHOLD) {
            min = (double) THRESHOLD;
        }

        // Now, any systems whose minimum distance is at least [min] are potential seat assignments

        // Now accumulate all systems whose minimum distance is larger than the minimum distance,
        // trying first to honor wheelchair status
        choices = new ArrayList<>(5);

        for (int i = 0; i < numClients; ++i) {
            final RawClientPc client = clients.get(i);

            if (distances[i] != null && distances[i].doubleValue() >= min) {
                choices.add(client);
            }
        }

        if (choices.isEmpty()) {
            // Fallback pass that ignores wheelchair state
            for (int i = 0; i < numClients; i++) {
                if (distances[i] != null && distances[i].doubleValue() >= min) {
                    choices.add(clients.get(i));
                }
            }
        }

        // Emergency: No systems found, so accumulate all open systems.
        if (choices.isEmpty()) {

            // Fallback pass that ignores wheelchair state
            for (int i = 0; i < numClients; i++) {
                if (distances[i] != null) {
                    choices.add(clients.get(i));
                }
            }
        }

        // Now pick from the choices
        final int numChoices = choices.size();
        if (numChoices > 0) {
            final int selectedChoiceIndex = this.random.nextInt(numChoices);
            final RawClientPc chosen = choices.get(selectedChoiceIndex);

            if (chosen != null) {
                this.info.selections.reservedSeat = chosen;
                Log.info("Station ", chosen.stationNbr, " reserved for ", this.info.selections.course);
                ok = true;
            } else {
                PopupPanel.showPopupMessage(this, this.center, "No seats available", null, null, PopupPanel.STYLE_OK);
                Log.info("Attempt to reserve seat for ", this.info.selections.course, " failed due to lack of seats");
            }
        } else {
            PopupPanel.showPopupMessage(this, this.center, "No seats available", null, null, PopupPanel.STYLE_OK);
            Log.info("Attempt to reserve seat for ", this.info.selections.course, " failed due to lack of seats");
        }

        return ok;
    }

    /**
     * For non-paper exams, where we do not issue a calculator, we must confirm issuance. to ensure the checkin staff
     * did not click the wrong exam button. On confirmation, set the selected testing station to begin the exam. This
     * method is called only if the selected course is not "PAPER".
     *
     * @return {@code true} if issuance is confirmed, {@code false} if it is not
     */
    private boolean confirmIssuance() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final String course = this.info.selections.course;
        String warn = null;

        Log.info("Selected exam ID: " + this.info.selections.exam.version);
        Log.info("Selected exam type: " + this.info.selections.exam.examType);

        if ("CH".equals(this.info.selections.exam.examType)) {
            htm.add("Assigning ", this.info.selections.exam.buttonLabel, " at station ",
                    this.info.selections.reservedSeat.stationNbr);
            warn = "TELL STUDENT $20 FEE WILL BE BILLED TO ACCOUNT!";
        } else if (RawRecordConstants.M117.equals(course) || RawRecordConstants.M118.equals(course)
                || RawRecordConstants.M124.equals(course) || RawRecordConstants.M125.equals(course)
                || RawRecordConstants.M126.equals(course)) {
            htm.add("Assigning ", course, CoreConstants.SPC, this.info.selections.exam.buttonLabel, " at station ",
                    this.info.selections.reservedSeat.stationNbr);
        } else {
            htm.add("Assigning ", this.info.selections.exam.buttonLabel, " at station ",
                    this.info.selections.reservedSeat.stationNbr);
        }

        final String htmStr = htm.toString();
        final String cmd = PopupPanel.showPopupMessage(this, this.center, htmStr, warn,
                "Do you want to start this exam?", PopupPanel.STYLE_YES_NO);

        return "Yes".equalsIgnoreCase(cmd);
    }

    /**
     * Starts the exam by updating the testing station record with the student and exam information, and display the
     * assigned seat to the staff.
     *
     * @param cache the data cache
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean startExam(final Cache cache) throws SQLException {

        final RawClientPc station = this.info.selections.reservedSeat;

        final String stuId = this.info.studentData.stuId;

        final LocalDateTime now = LocalDateTime.now();
        RawStvisitLogic.INSTANCE.startNewVisit(cache, stuId, now, "TC", station.stationNbr);

        final Integer selectedUnitObj = Integer.valueOf(this.info.selections.unit);
        final boolean ok = RawClientPcLogic.updateAllCurrent(cache, station.computerId,
                RawClientPc.STATUS_AWAIT_STUDENT, stuId, this.info.selections.course, selectedUnitObj,
                this.info.selections.exam.version);

        if (!ok) {
            PopupPanel.showPopupMessage(this, this.center, "Unable to complete check in process", null,
                    "Please inform a director.", PopupPanel.STYLE_OK);
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
     * Handles the selection of an exam (this occurs in the AWT event dispatching thread).
     *
     * @param cache  the data cache
     * @param course the course selected
     * @param unit   the unit selected
     * @param type   the exam type (L, Q, U, F, CH, or MA)
     * @throws SQLException if there is an error accessing the database
     */
    void chooseExam(final Cache cache, final String course, final int unit, final String type) throws SQLException {

        final RawStudent student = this.info.studentData.student;
        final String name = student.getScreenName();

        if (course == null) {
            this.info = null;
            this.state = AWAIT_STUDENT;
            this.bottom.setMessage("CHECK IN");
        } else {
            this.info.selections.course = course;
            this.info.selections.unit = unit;

            final SystemData systemData = cache.getSystemData();

            // Look up the version of the exam

            if (RawRecordConstants.M100P.equals(course)) {
                this.info.selections.exam = systemData.getActiveExam("MPTTC");
                this.bottom.setMessage(name);
            } else if (RawRecordConstants.M100U.equals(course)) {
                this.info.selections.exam = systemData.getActiveExam("UOOOO");
                this.bottom.setMessage(name);
            } else {
                final Integer unitObj = Integer.valueOf(unit);
                if ("CH".equals(type)) {
                    this.info.selections.exam = systemData.getActiveExamByCourseUnitType(course, unitObj, type);
                    if (this.info.selections.exam == null) {
                        Log.warning("Unable to query exam ", course, CoreConstants.SPC, unitObj, CoreConstants.SPC,
                                type);
                        this.bottom.setMessage(name);
                    }
                } else if ("MA".equals(type)) {
                    // The user has selected a generic "mastery exam" which must create a synthetic exam with
                    // all the standards for which the user is eligible...

                    // All check-in needs to do is look up a general "RawExam" record with the course and unit
                    // specified, and store that so the server can generate the proper synthetic exam.

                    this.info.selections.exam = systemData.getActiveExamByCourseUnitType(course, unitObj, type);
                    if (this.info.selections.exam == null) {
                        this.bottom.setMessage("INVALID EXAM");
                    } else {
                        this.bottom.setMessage(name);
                        Log.info("Exam version is ", this.info.selections.exam.version);
                    }


                } else {
                    this.info.selections.exam = systemData.getActiveExamByCourseUnitType(course, unitObj, type);
                    if (this.info.selections.exam == null) {
                        this.bottom.setMessage("INVALID EXAM");
                    } else {
                        this.bottom.setMessage(name);
                        Log.info("Exam version is ", this.info.selections.exam.version);
                    }
                }
            }

            this.bottom.repaint();
            this.state = this.info.selections.exam == null ? AWAIT_STUDENT : ISSUE_EXAM;
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

        final char keyChar = e.getKeyChar();

        if ((int) keyChar == 22) {

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
                Log.warning("Exception getting clipboard content", ex);
            }
        } else {
            this.top.addToFieldValue(keyChar);
        }
    }

    /**
     * Main method that launches the remote testing application.
     *
     * @param args command-line arguments: first (optional) argument is the testing center ID, and second (optional) is
     *             "windowed" to run in windowed mode
     */
    public static void main(final String... args) {

        ChangeUI.changeUI();

        String centerId = "1";

        boolean fullScreen = true;
        for (final String arg : args) {
            if ("windowed".equals(arg)) {
                fullScreen = false;
            } else if ("2".equals(arg) || "3".equals(arg) || "4".equals(arg)) {
                centerId = arg;
            }
        }

        ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final CheckinApp app = new CheckinApp(centerId);
        final ZonedDateTime now = ZonedDateTime.now();
        app.runCheckInApplication(now, fullScreen);    }
}

/**
 * Runnable class to be called in the AWT dispatcher thread to construct the blocking window. The window consists of a
 * full-screen {@code JFrame} that contains a top {@code FieldPanel}, a {@code CenterPanel}, and a bottom
 * {@code FieldPanel}.
 */
final class BlockingWindowBuilder implements Runnable {

    /** The listener to install on all panels. */
    private final CheckinApp listener;

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
    private BottomPanel bottomPanel = null;

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
    BlockingWindowBuilder(final CheckinApp theListener, final DbProfile theDbProfile, final String centerId,
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
    BottomPanel getBottomPanel() {

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

        this.builderFrame = new JFrame("Checkin");
        if (this.full) {
            this.builderFrame.setUndecorated(true);
        }
        this.builderFrame.setFocusableWindowState(true);
        this.builderFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout(1, 1));
        content.setPreferredSize(screen);
        this.builderFrame.setContentPane(content);
        content.setBackground(new Color(120, 120, 180));

        this.builderFrame.setSize(screen);
        this.builderFrame.setLocation(0, 0);
        this.builderFrame.setVisible(true);
        this.builderFrame.toFront();
        this.builderFrame.requestFocus();

        this.topPanel = new FieldPanel(screen, this.listener, "TopField");
        content.add(this.topPanel, BorderLayout.PAGE_START);
        this.centerPanel = new CenterPanel(this.listener, this.dbProfile, this.testingCenterId);
        content.add(this.centerPanel, BorderLayout.CENTER);
        this.bottomPanel = new BottomPanel(screen);
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

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String fullStr = Boolean.toString(this.full);

        return SimpleBuilder.concat("BlockingWindowBuilder{listener=", this.listener, ", dbProfile=", this.dbProfile,
                ", testingCenterId='", this.testingCenterId, "', full=", fullStr, ", builderFrame=", this.builderFrame,
                ", topPanel=", this.topPanel, ", bottomPanel=", this.bottomPanel, ", centerPanel=", this.centerPanel,
                "}");
    }
}
