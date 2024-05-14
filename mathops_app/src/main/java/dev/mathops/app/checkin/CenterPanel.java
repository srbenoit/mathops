package dev.mathops.app.checkin;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.font.BundledFontManager;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel to appear in the center of the check-in application. This panel displays a map of the testing center,
 * showing the status of all stations, and is used for buttons when selecting course/unit for exams.
 */
final class CenterPanel extends JPanel implements ActionListener, Runnable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8968168443957197770L;

    /** Application version. */
    private static final String VERSION = "v2.3.6 (May 13, 2024)";

    /** Label string. */
    private static final String UNIT_1 = "Unit 1";

    /** Label string. */
    private static final String UNIT_2 = "Unit 2";

    /** Label string. */
    private static final String UNIT_3 = "Unit 3";

    /** Label string. */
    private static final String UNIT_4 = "Unit 4";

    /** Label string. */
    private static final String FINAL = "Final";

    /** Label string. */
    private static final String MASTERY = "Mastery";

    /** Label string. */
    private static final String CHALLENGE = "Challenge";

    /** A commonly used string. */
    private static final String NOT_REGISTERED = "Not Registered";

    /** A commonly used string. */
    private static final String TUTORIALS = "Tutorials";

    /** A commonly used string. */
    private static final String OTHER = "Other";

    /** Key. */
    private static final String USERS = "users";

    /** Key. */
    private static final String PLACEMENT = "mpt";

    /** Key. */
    private static final String ELM = "elm";

    /** Key. */
    private static final String PRECALC17 = "precalc17";

    /** Key. */
    private static final String PRECALC18 = "precalc18";

    /** Key. */
    private static final String PRECALC24 = "precalc24";

    /** Key. */
    private static final String PRECALC25 = "precalc25";

    /** Key. */
    private static final String PRECALC26 = "precalc26";

    /** Label string. */
    private static final String CANCEL = "Cancel";

    /** Object on which to synchronize. */
    private final Object synch;

    /** The application that owns this panel. */
    private final CheckinApp owner;

    /** The database profile. */
    private final DbProfile dbProfile;

    /** The testing center ID being managed. */
    private final String testingCenterId;

    /** Data indicating which exams to show, or null to hide exams and show the map. */
    private DataCheckInAttempt data = null;

    /** The font to use when drawing exam buttons. */
    private Font headerFont = null;

    /** The set of exam buttons. */
    private Map<String, ExamButton> buttons = null;

    /** The testing center map. */
    private final TestingCenterMap map;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /**
     * Constructs a new {@code CenterPanel}.
     *
     * @param theOwner           the application that owns this panel
     * @param theDbProfile       the database profile
     * @param theTestingCenterId the testing center ID being managed
     */
    CenterPanel(final CheckinApp theOwner, final DbProfile theDbProfile, final String theTestingCenterId) {

        super();

        this.synch = new Object();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        this.owner = theOwner;
        this.dbProfile = theDbProfile;
        this.testingCenterId = theTestingCenterId;

        setLayout(null);
        setBackground(new Color(170, 170, 200));
        setFocusable(true);
        setDoubleBuffered(true);

        this.map = new TestingCenterMap();

        final BundledFontManager bfm = BundledFontManager.getInstance();
        this.pcFont = bfm.getFont(BundledFontManager.SANS, 14.0, Font.BOLD);
    }

    /**
     * Sets the displayed list of available exams.
     *
     * @param theData the check-in attempt data
     */
    void showAvailableExams(final DataCheckInAttempt theData) {

        synchronized (this.synch) {
            this.data = theData;

            try {
                SwingUtilities.invokeLater(new ShowButtons(this.buttons));
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Clears the displayed list of available exams.
     */
    void hideAvailableExams() {

        synchronized (this.synch) {
            this.data = null;

            try {
                SwingUtilities.invokeLater(new HideButtons(this.buttons));
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        synchronized (this.synch) {
            final Graphics2D g2d = (Graphics2D) g;

            // Lazily create the buttons, and generate the font.
            if (this.buttons == null) {
                buildButtons(g);
            }

            super.paintComponent(g);

            // Configure permanent attributes of the drawing context.
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (this.data == null) {
                final int width = getWidth();
                final int height = getHeight();
                this.map.drawMap(g2d, width, height);
            } else {
                drawExamButtons(g2d);
            }

            final Font font = this.pcFont.deriveFont(10.0f);
            g2d.setFont(font);
            final int h = getHeight();
            g2d.setColor(Color.GRAY);
            g2d.drawString(VERSION, 8, h - 3);
        }
    }

    /**
     * Draws the exam buttons on the panel.
     *
     * @param g2d the {@code Graphics} to which to draw
     */
    private void drawExamButtons(final Graphics2D g2d) {

        // NOTE: Runs in the AWT event thread.


        // Divide the screen into a 7x6 grid.
        final int width = getWidth();
        final int height = getHeight();

        final int w = width / 7 - 5;
        final int h = height / 7 - 2;

        // Make sure we have a font that will fit
        if (this.headerFont == null) {
            final BundledFontManager bfm = BundledFontManager.getInstance();

            int pts = 10;

            while (pts < 100) {
                this.headerFont = bfm.getFont(BundledFontManager.SANS, (double) pts, Font.BOLD);

                final FontMetrics fm = g2d.getFontMetrics(this.headerFont);

                if ((double) fm.stringWidth("MATH") >= (double) w * 0.5 || fm.getHeight() >= h / 2) {
                    break;
                }

                pts += 2;
            }
        }

        // Draw the buttons - dim if unavailable, lit if available.
        g2d.setFont(this.headerFont);

        final FontMetrics fm = g2d.getFontMetrics();

        // Background for "Challenge" portion of the courses
        int y = 6 * h + 5;
        g2d.setColor(new Color(150, 150, 180));
        g2d.fillRect(0, y, 5 * w, height - y);

        g2d.setColor(new Color(190, 190, 220));
        g2d.fillRect(w, 0, w, y);
        g2d.fillRect(3 * w, 0, w, y);

        g2d.setColor(new Color(170, 170, 170));
        g2d.fillRect(5 * w, 0, 25, height);

        g2d.setColor(new Color(190, 190, 190));
        g2d.fillRect(5 * w + 25, 0, width - 5 * w - 25, height);

        g2d.setColor(new Color(30, 30, 55));

        final DataCourseExams exams117 = this.data.getCourseExams(CourseNumbers.MATH117);
        final DataCourseExams exams118 = this.data.getCourseExams(CourseNumbers.MATH118);
        final DataCourseExams exams124 = this.data.getCourseExams(CourseNumbers.MATH124);
        final DataCourseExams exams125 = this.data.getCourseExams(CourseNumbers.MATH125);
        final DataCourseExams exams126 = this.data.getCourseExams(CourseNumbers.MATH126);

        y = h / 2 - fm.getDescent();

        final int lblSizeMath = fm.stringWidth("MATH");

        g2d.drawString("MATH", (w - lblSizeMath) / 2, y);
        final int nbrSize117 = fm.stringWidth("117");
        final int fontHeight = fm.getHeight();
        g2d.drawString("117", (w - nbrSize117) / 2, y + fontHeight);

        if (!(exams117.registeredInOld || exams117.registeredInNew)) {
            g2d.setFont(this.pcFont);
            final FontMetrics metrics = g2d.getFontMetrics();
            final int strWidth = metrics.stringWidth(NOT_REGISTERED);
            g2d.drawString(NOT_REGISTERED, (w - strWidth) / 2, height / 2);
            g2d.setFont(this.headerFont);
        }

        g2d.drawString("MATH", w + (w - lblSizeMath) / 2, y);
        final int nbrSize118 = fm.stringWidth("118");
        g2d.drawString("118", w + (w - nbrSize118) / 2, y + fontHeight);

        if (!(exams118.registeredInOld || exams118.registeredInNew)) {
            g2d.setFont(this.pcFont);
            final FontMetrics metrics = g2d.getFontMetrics();
            final int strWidth = metrics.stringWidth(NOT_REGISTERED);
            g2d.drawString(NOT_REGISTERED, w + (w - strWidth) / 2, height / 2);
            g2d.setFont(this.headerFont);
        }

        g2d.drawString("MATH", 2 * w + (w - lblSizeMath) / 2, y);
        final int nbrSize124 = fm.stringWidth("124");
        g2d.drawString("124", 2 * w + (w - nbrSize124) / 2, y + fontHeight);

        if (!(exams124.registeredInOld || exams124.registeredInNew)) {
            g2d.setFont(this.pcFont);
            final FontMetrics metrics = g2d.getFontMetrics();
            final int strWidth = metrics.stringWidth(NOT_REGISTERED);
            g2d.drawString(NOT_REGISTERED, 2 * w + (w - strWidth) / 2, height / 2);
            g2d.setFont(this.headerFont);
        }

        g2d.drawString("MATH", 3 * w + (w - lblSizeMath) / 2, y);
        final int nbrSize125 = fm.stringWidth("125");
        g2d.drawString("125", 3 * w + (w - nbrSize125) / 2, y + fontHeight);

        if (!(exams125.registeredInOld || exams125.registeredInNew)) {
            g2d.setFont(this.pcFont);
            final FontMetrics metrics = g2d.getFontMetrics();
            final int strWidth = metrics.stringWidth(NOT_REGISTERED);
            g2d.drawString(NOT_REGISTERED, 3 * w + (w - strWidth) / 2, height / 2);
            g2d.setFont(this.headerFont);
        }

        g2d.drawString("MATH", 4 * w + (w - lblSizeMath) / 2, y);
        final int nbrSize126 = fm.stringWidth("126");
        g2d.drawString("126", 4 * w + (w - nbrSize126) / 2, y + fontHeight);

        if (!(exams126.registeredInOld || exams126.registeredInNew)) {
            g2d.setFont(this.pcFont);
            final FontMetrics metrics = g2d.getFontMetrics();
            final int strWidth = metrics.stringWidth(NOT_REGISTERED);
            g2d.drawString(NOT_REGISTERED, 4 * w + (w - strWidth) / 2, height / 2);
            g2d.setFont(this.headerFont);
        }

        final int lblSizeTut = fm.stringWidth(TUTORIALS);
        g2d.drawString(TUTORIALS, 5 * w + 30 + (w - lblSizeTut) / 2, y + fontHeight);

        final int lblSizeOther = fm.stringWidth(OTHER);
        g2d.drawString(OTHER, 6 * w + 30 + (w - lblSizeOther) / 2, y + fontHeight);

        g2d.drawLine(w, 0, w, height);
        g2d.drawLine(2 * w, 0, 2 * w, height);
        g2d.drawLine(3 * w, 0, 3 * w, height);
        g2d.drawLine(4 * w, 0, 4 * w, height);
        g2d.drawLine(5 * w, 0, 5 * w, height);
        g2d.drawLine(5 * w + 25, 0, 5 * w + 25, height);
        g2d.drawLine(6 * w + 30, 0, 6 * w + 30, height);
        g2d.drawLine(0, 6 * h + 5, 5 * w, 6 * h + 5);

        // Enable buttons according to what exams are available

        updateCourseButtons(exams117, "117");
        updateCourseButtons(exams118, "118");
        updateCourseButtons(exams124, "124");
        updateCourseButtons(exams125, "125");
        updateCourseButtons(exams126, "126");
        enableButton(USERS, "User's Exam", this.data.nonCourseExams.usersExam);
        enableButton(PLACEMENT, "Placement", this.data.nonCourseExams.placement);
        enableButton(ELM, "ELM Exam", this.data.nonCourseExams.elmExam);
        enableButton(PRECALC17, "Algebra I", this.data.nonCourseExams.precalc117);
        enableButton(PRECALC18, "Algebra II", this.data.nonCourseExams.precalc118);
        enableButton(PRECALC24, "Functions", this.data.nonCourseExams.precalc124);
        enableButton(PRECALC25, "Trig. I", this.data.nonCourseExams.precalc125);
        enableButton(PRECALC26, "Trig. II", this.data.nonCourseExams.precalc126);

        final DataExamStatus cancel = DataExamStatus.available(null, 0);
        enableButton(CANCEL, CANCEL, cancel);
    }

    /**
     * Updates the buttons associated with a single course.
     *
     * @param exams the exams data for that course
     * @param tagPrefix the tag prefix used to obtain the course's buttons
     */
    private void updateCourseButtons( final DataCourseExams exams, final String tagPrefix) {

        final String keyUnit1 = tagPrefix + "-1";
        final String keyUnit2 = tagPrefix + "-2";
        final String keyUnit3 = tagPrefix + "-3";
        final String keyUnit4 = tagPrefix + "-4";
        final String keyUnit5 = tagPrefix + "-5";
        final String keyMastery = tagPrefix + "-MA";
        final String keyChal = exams.courseNumbers.challengeId();

        this.buttons.get(keyUnit1).setVisible(exams.registeredInOld);
        this.buttons.get(keyUnit2).setVisible(exams.registeredInOld);
        this.buttons.get(keyUnit3).setVisible(exams.registeredInOld);
        this.buttons.get(keyUnit4).setVisible(exams.registeredInOld);
        this.buttons.get(keyUnit5).setVisible(exams.registeredInOld);
        this.buttons.get(keyMastery).setVisible(exams.registeredInNew);

        DataExamStatus exam;

        if (exams.registeredInNew) {
            exam = exams.masteryExam;
            enableButton(keyMastery, MASTERY, exam);
        } else if (exams.registeredInOld) {
            exam = exams.unit1Exam;
            enableButton(keyUnit1, UNIT_1, exam);
            exam = exams.unit2Exam;
            enableButton(keyUnit2, UNIT_2, exam);
            exam = exams.unit3Exam;
            enableButton(keyUnit3, UNIT_3, exam);
            exam = exams.unit4Exam;
            enableButton(keyUnit4, UNIT_4, exam);
            exam = exams.finalExam;
            enableButton(keyUnit5, FINAL, exam);
        }

        exam = exams.challengeExam;
        enableButton(keyChal, CHALLENGE, exam);
    }

    /**
     * Sets the enabled state of a particular button.
     *
     * @param key   the key under which the button is stored
     * @param label the button's main label
     * @param exam  the information on the exam's availability
     */
    private void enableButton(final String key, final String label, final DataExamStatus exam) {

        final ExamButton btn = this.buttons.get(key);

        // NOTE: Runs in the AWT event thread.

        if (btn == null) {
            Log.warning("Unable to find button with key '", key, "'");
        } else {
            btn.setTitle(label);
            btn.setVisible(true);
            btn.setMessage(exam.note);

            btn.setEnabled(exam.available);
            btn.setForeground(exam.available ? Color.BLACK : Color.GRAY);
        }
    }

    /**
     * Generates the set of buttons for the exams.
     *
     * @param grx the {@code Graphics} to which buttons will be drawn
     */
    private void buildButtons(final Graphics grx) {

        // NOTE: Runs in the AWT event thread.

        // Divide the screen into a 7x6 grid.
        final int w = getWidth() / 7 - 5;
        final int h = getHeight() / 7 - 2;

        // Make sure we have a font that will fit
        final BundledFontManager bfm = BundledFontManager.getInstance();

        int pts = 10;
        Font font = null;

        while (pts < 100) {
            font = bfm.getFont(BundledFontManager.SANS, (double) pts, Font.BOLD);

            final FontMetrics fm = grx.getFontMetrics(font);

            if ((double) fm.stringWidth("User's Exam") >= (double) w * 0.8
                    || (double) fm.getHeight() >= (double) h * 0.8) {
                break;
            }

            pts += 2;
        }

        final Font font2 = bfm.getFont(BundledFontManager.SANS, (double) pts / 2.0, Font.BOLD);

        final Color courseColor = new Color(140, 200, 140);
        final Color challengeColor = new Color(190, 140, 190);
        final Color tutorialColor = new Color(190, 190, 140);
        final Color otherColor = new Color(140, 190, 190);
        final Color cancelColor = new Color(200, 140, 140);

        this.buttons = new HashMap<>(70);
        int x = 0;

        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "117-MA");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "117-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "117-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "117-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "117-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "117-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M117_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "118-MA");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "118-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "118-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "118-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "118-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "118-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M118_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "124-MA");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "124-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "124-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "124-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "124-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "124-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M124_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "125-MA");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "125-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "125-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "125-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "125-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "125-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M125_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "126-MA");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "126-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "126-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "126-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "126-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "126-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M126_CHALLENGE_EXAM_ID);

        x += w + 25;
        makeButton(tutorialColor, font, font2, "ELM Tutorial", x, h, w + 5, h, ELM);
        makeButton(tutorialColor, font, font2, "Precalc", x, 2 * h, w + 5, h, PRECALC17);
        makeButton(tutorialColor, font, font2, "Precalc", x, 3 * h, w + 5, h, PRECALC18);
        makeButton(tutorialColor, font, font2, "Precalc", x, 4 * h, w + 5, h, PRECALC24);
        makeButton(tutorialColor, font, font2, "Precalc", x, 5 * h, w + 5, h, PRECALC25);
        makeButton(tutorialColor, font, font2, "Precalc", x, 6 * h, w + 5, h, PRECALC26);

        x += w + 5;
        makeButton(otherColor, font, font2, "User's Exam", x, h, w + 10, h, USERS);
        makeButton(otherColor, font, font2, "Math Placement", x, 2 * h, w + 10, h, PLACEMENT);

        // Make the cancel button
        final ExamButton btn = new ExamButton(cancelColor);
        btn.setTitle(CANCEL);

        btn.setFont(font);
        btn.setDefaultCapable(false);
        btn.setFocusable(false);
        btn.setVisible(false);
        btn.setEnabled(false);
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setActionCommand(CANCEL);
        btn.addActionListener(this);

        this.buttons.put(CANCEL, btn);

        add(btn);
        btn.setSize((w << 1) / 3, (h << 1) / 3);
        btn.setLocation(6 * w + 30 + w / 5, 6 * h + h / 6);
    }

    /**
     * Creates a single button and add it to the panel.
     *
     * @param activeBackground the background color for the button when active
     * @param font             the font for the exam title
     * @param font2            the font for the sub-text message
     * @param title            the title to place on the button
     * @param xPos             the X position of the left of the button's area
     * @param yPos             the Y position of the top of the button's area
     * @param width            the width of the button's area
     * @param height           the height of the button's area
     * @param key              the key under which to file the button in the hash table
     */
    private void makeButton(final Color activeBackground, final Font font, final Font font2, final String title,
                            final int xPos, final int yPos, final int width, final int height, final String key) {

        // NOTE: Runs in the AWT event thread.

        final ExamButton btn = new ExamButton(activeBackground);
        btn.setTitle(title);

        btn.setFont(font);
        btn.setSubFont(font2);
        btn.setDefaultCapable(false);
        btn.setFocusable(false);
        btn.setVisible(false);
        btn.setEnabled(false);
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setActionCommand(key);
        btn.addActionListener(this);

        this.buttons.put(key, btn);

        add(btn);
        btn.setLocation(xPos + 5, yPos + 5);
        btn.setSize(width - 10, height - 10);
    }

    /**
     * Handler for actions generated by pressing buttons.
     *
     * @param e the action event being processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        Log.info("Command = ", cmd);

        // NOTE: Runs in the AWT event thread.

        final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            try {
                final Cache cache = new Cache(this.dbProfile, conn);

                if (USERS.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M100U, 1, "L");
                } else if (PLACEMENT.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M100P, 0, "Q");
                } else if (ELM.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M100T, 4, "U");
                } else if (PRECALC17.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M1170, 4, "U");
                } else if (PRECALC18.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M1180, 4, "U");
                } else if (PRECALC24.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M1240, 4, "U");
                } else if (PRECALC25.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M1250, 4, "U");
                } else if (PRECALC26.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M1260, 4, "U");

                } else if (ChallengeExamLogic.M117_CHALLENGE_EXAM_ID.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 0, "CH");
                } else if (ChallengeExamLogic.M118_CHALLENGE_EXAM_ID.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 0, "CH");
                } else if (ChallengeExamLogic.M124_CHALLENGE_EXAM_ID.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 0, "CH");
                } else if (ChallengeExamLogic.M125_CHALLENGE_EXAM_ID.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 0, "CH");
                } else if (ChallengeExamLogic.M126_CHALLENGE_EXAM_ID.equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 0, "CH");

                } else if ("117-1".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 1, "U");
                } else if ("117-2".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 2, "U");
                } else if ("117-3".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 3, "U");
                } else if ("117-4".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 4, "U");
                } else if ("117-5".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M117, 5, "F");

                } else if ("118-1".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 1, "U");
                } else if ("118-2".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 2, "U");
                } else if ("118-3".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 3, "U");
                } else if ("118-4".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 4, "U");
                } else if ("118-5".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M118, 5, "F");

                } else if ("124-1".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 1, "U");
                } else if ("124-2".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 2, "U");
                } else if ("124-3".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 3, "U");
                } else if ("124-4".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 4, "U");
                } else if ("124-5".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M124, 5, "F");

                } else if ("125-1".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 1, "U");
                } else if ("125-2".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 2, "U");
                } else if ("125-3".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 3, "U");
                } else if ("125-4".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 4, "U");
                } else if ("125-5".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M125, 5, "F");

                } else if ("126-1".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 1, "U");
                } else if ("126-2".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 2, "U");
                } else if ("126-3".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 3, "U");
                } else if ("126-4".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 4, "U");
                } else if ("126-5".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M126, 5, "F");

                } else if ("117-MA".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.MATH117, 0, "MA");
                } else if ("118-MA".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.MATH118, 0, "MA");
                } else if ("124-MA".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.MATH124, 0, "MA");
                } else if ("125-MA".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.MATH125, 0, "MA");
                } else if ("126-MA".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.MATH126, 0, "MA");

                } else if (CANCEL.equals(cmd)) {
                    this.owner.chooseExam(cache, null, 0, null);
                } else {
                    Log.warning("Unknown action command: " + cmd);
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Updates the list of client computers.
     */
    @Override
    public void run() {

        // Every 5 seconds, query the testing stations.
        while (isVisible()) {

            final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
            try {
                final DbConnection conn = ctx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
                try {
                    final List<RawClientPc> stations = RawClientPcLogic.queryByTestingCenter(cache,
                            this.testingCenterId);
                    this.map.updateTestingStations(stations);
                    repaint();
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }

            try {
                Thread.sleep(5000L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Runnable class to show the exam selection buttons. This is intended to run in the AWT event thread.
     */
    private final class ShowButtons implements Runnable {

        /** The set of exam buttons. */
        private final Map<String, ExamButton> examButtons;

        /**
         * Constructs a new {@code ShowButtons}.
         *
         * @param theButtons the set of exam buttons
         */
        ShowButtons(final Map<String, ExamButton> theButtons) {

            this.examButtons = theButtons;
        }

        /**
         * Shows the buttons.
         */
        @Override
        public void run() {

            for (final ExamButton btn : this.examButtons.values()) {
                btn.setVisible(true);
            }

            repaint();
        }
    }

    /**
     * Runnable class to hide the exam selection buttons.  This is intended to run in the AWT event thread.
     */
    private final class HideButtons implements Runnable {

        /** The set of exam buttons. */
        private final Map<String, ExamButton> examButtons;

        /**
         * Constructs a new {@code HideButtons}.
         *
         * @param theButtons the set of exam buttons
         */
        HideButtons(final Map<String, ExamButton> theButtons) {

            this.examButtons = theButtons;
        }

        /**
         * Hides the buttons.
         */
        @Override
        public void run() {

            for (final ExamButton btn : this.examButtons.values()) {
                btn.setVisible(false);
            }

            repaint();
        }
    }
}
