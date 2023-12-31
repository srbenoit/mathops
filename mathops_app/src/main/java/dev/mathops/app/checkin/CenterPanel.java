package dev.mathops.app.checkin;

import dev.mathops.app.AppFileLoader;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel to appear in the center of the checkin application. This panel displays a map of the testing center, showing
 * the status of all stations, and is used for buttons when selecting course/unit for exams.
 */
final class CenterPanel extends JPanel implements ActionListener, Runnable, MouseListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8968168443957197770L;

    /** Application version. */
    private static final String VERSION = "v2.3.4 (Aug 22 2022)";

    // /** String to indicate button should be hidden. */
    // private static final String HIDE = "HIDE"; 

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

    /** List of exams to show, or null to hide exams. */
    private Map<String, ExamStatus> exams;

    /** The font to use when drawing exam buttons. */
    private Font headerFont;

    /** The set of exam buttons. */
    private Map<String, ExamButton> buttons;

    /** The client computer list. */
    private List<RawClientPc> clients;

    /** The color in which to fill the map. */
    private final Color mapFill;

    /** The color in which to draw the outline of the map. */
    private final Color mapOutline;

    /** The color in which to draw the tables. */
    private final Color tableColor;

    /** The color in which to draw the outline of the tables. */
    private final Color tableOutline;

    /** The color in which to draw the PCs in an error state. */
    private final Color pcErrorColor;

    /** The color in which to draw the PCs in a warning state. */
    private final Color pcWarningColor;

    /** The color in which to draw the PCs in a locked state. */
    private final Color pcLockedColor;

    /** The color in which to draw the PCs in a paper-only state. */
    private final Color pcPaperColor;

    /** The color in which to draw the PCs in an await-student state. */
    private final Color pcAwaitColor;

    /** The color in which to draw the PCs in an exam-taking state. */
    private final Color pcInExamColor;

    /** The color in which to draw the outline of the PCs. */
    private final Color pcOutline;

    /** The color in which to draw numbers on the PCs. */
    private final Color pcNumber;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /** The font in which to draw legend entries. */
    private final Font legendFont;

    /** An icon to label a station as wheelchair accessible. */
    private BufferedImage wheelchairIcon;

    /** The wheelchair button in its lit state. */
    private BufferedImage wheelchairLit;

    /** Flag indicating student needs wheelchair access. */
    private boolean isWheelchair;

    /** The rectangle where the wheelchair button is drawn. */
    private Rectangle wheelchairRect;

    /** The unit of the ELM tutorial the student is eligible for. */
    private Integer elmUnit;

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

        this.mapFill = ColorNames.getColor("gainsboro");
        this.mapOutline = ColorNames.getColor("black");
        this.tableColor = ColorNames.getColor("salmon3");
        this.tableOutline = ColorNames.getColor("red4");

        this.pcErrorColor = ColorNames.getColor("red4");
        this.pcWarningColor = ColorNames.getColor("DarkOrchid4");
        this.pcLockedColor = ColorNames.getColor("gold4");
        this.pcPaperColor = ColorNames.getColor("gray25");
        this.pcAwaitColor = ColorNames.getColor("turquoise4");
        this.pcInExamColor = ColorNames.getColor("green4");

        this.pcOutline = ColorNames.getColor("black");
        this.pcNumber = ColorNames.getColor("gray88");

        final BundledFontManager bfm = BundledFontManager.getInstance();
        this.pcFont = bfm.getFont(BundledFontManager.SANS, 14.0, Font.BOLD);
        this.legendFont = bfm.getFont(BundledFontManager.SANS, 18.0, Font.BOLD);

        addMouseListener(this);
    }

    /**
     * Sets the displayed list of available exams.
     *
     * @param theExams the list of available exams
     */
    void showAvailableExams(final Map<String, ExamStatus> theExams) {

        synchronized (this.synch) {
            this.exams = new HashMap<>(theExams);

            try {
                SwingUtilities.invokeLater(new ShowButtons(this.buttons));
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Clears the displayed list of available exams.
     */
    void hideAvailableExams() {

        synchronized (this.synch) {
            this.exams = null;

            try {
                SwingUtilities.invokeLater(new HideButtons(this.buttons));
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Sets the flag indicating the student needs a wheelchair.
     *
     * @param wheelchair {@code true} if the student requires wheelchair access; {@code false} if not
     */
    void setIsWheelchair(final boolean wheelchair) {

        this.isWheelchair = wheelchair;
    }

    /**
     * Sets the unit of the tutorial the student is eligible for.
     *
     * @param theUnit the unit number
     */
    void setTutorialUnit(final Integer theUnit) {

        this.elmUnit = theUnit;
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
                loadImages();
            }

            super.paintComponent(g);

            // Configure permanent attributes of the drawing context.
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (this.exams == null) {
                drawMap(g2d);
            } else {
                drawExamButtons(g2d);
            }

            g2d.setFont(this.pcFont.deriveFont(10.0f));
            final int h = getHeight();
            g2d.setColor(Color.GRAY);
            g2d.drawString(VERSION, 8, h - 3);
        }
    }

    /**
     * Draws the map.
     *
     * @param g2d the {@code Graphics} to which to draw
     */
    private void drawMap(final Graphics2D g2d) {

        // NOTE: Runs in the AWT event thread.

        int w = getWidth();
        final int h = getHeight();
        final int x = Math.max(10, ((w << 2) / 5 - 642) / 2);
        final int y = (h - 612) / 2;

        // Draw the room outline
        final Polygon floor = new Polygon();
        floor.addPoint(x + 28, y);
        floor.addPoint(x + 312, y);
        floor.addPoint(x + 312, y + 4);
        floor.addPoint(x + 336, y + 4);
        floor.addPoint(x + 336, y);
        floor.addPoint(x + 642, y);
        floor.addPoint(x + 642, y + 528);
        floor.addPoint(x + 222, y + 528);
        floor.addPoint(x + 222, y + 612);
        floor.addPoint(x, y + 612);
        floor.addPoint(x, y + 28);
        floor.addPoint(x + 28, y + 28);

        g2d.setColor(this.mapFill);
        g2d.fill(floor);
        g2d.setColor(this.mapOutline);
        g2d.draw(floor);

        // Draw columns
        g2d.drawRect(x + 312, y + 186, 20, 20);
        g2d.drawRect(x + 312, y + 392, 20, 20);

        // Draw doors
        g2d.fillRect(x + 470, y + 526, 40, 5);
        g2d.fillRect(x + 220, y + 538, 5, 40);
        g2d.fillRect(x + 340, y, 40, 3);

        // Draw tables
        g2d.setColor(this.tableColor);
        g2d.fillRoundRect(x + 24, y + 30, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 104, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 186, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 272, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 356, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 447, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 24, y + 521, 24, 72, 4, 4);

        g2d.fillRoundRect(x + 96, y + 52, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 96, y + 178, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 96, y + 304, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 96, y + 430, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 84, y + 584, 72, 24, 4, 4);

        g2d.fillRoundRect(x + 198, y + 52, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 198, y + 178, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 198, y + 304, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 198, y + 430, 72, 48, 4, 4);

        g2d.fillRoundRect(x + 312, y + 38, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 312, y + 112, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 312, y + 208, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 312, y + 318, 24, 72, 4, 4);

        g2d.fillRoundRect(x + 378, y + 52, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 378, y + 133, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 378, y + 219, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 378, y + 302, 24, 72, 4, 4);
        g2d.fillRoundRect(x + 378, y + 384, 24, 72, 4, 4);

        g2d.fillRoundRect(x + 450, y + 52, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 450, y + 178, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 450, y + 304, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 450, y + 430, 72, 48, 4, 4);

        g2d.fillRoundRect(x + 548, y + 52, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 548, y + 178, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 548, y + 304, 72, 48, 4, 4);
        g2d.fillRoundRect(x + 548, y + 430, 72, 48, 4, 4);

        // Draw table outlines in darker line
        g2d.setColor(this.tableOutline);
        g2d.drawRoundRect(x + 24, y + 30, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 104, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 186, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 272, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 356, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 447, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 24, y + 521, 24, 72, 4, 4);

        g2d.drawRoundRect(x + 96, y + 52, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 96, y + 178, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 96, y + 304, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 96, y + 430, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 84, y + 584, 72, 24, 4, 4);

        g2d.drawRoundRect(x + 198, y + 52, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 198, y + 178, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 198, y + 304, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 198, y + 430, 72, 48, 4, 4);

        g2d.drawRoundRect(x + 312, y + 38, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 312, y + 112, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 312, y + 208, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 312, y + 318, 24, 72, 4, 4);

        g2d.drawRoundRect(x + 378, y + 52, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 378, y + 133, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 378, y + 219, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 378, y + 302, 24, 72, 4, 4);
        g2d.drawRoundRect(x + 378, y + 384, 24, 72, 4, 4);

        g2d.drawRoundRect(x + 450, y + 52, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 450, y + 178, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 450, y + 304, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 450, y + 430, 72, 48, 4, 4);

        g2d.drawRoundRect(x + 548, y + 52, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 548, y + 178, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 548, y + 304, 72, 48, 4, 4);
        g2d.drawRoundRect(x + 548, y + 430, 72, 48, 4, 4);

        // Now draw the client computers with numbers
        final List<RawClientPc> clientList;

        synchronized (this) {
            clientList = this.clients;
        }

        if (clientList == null) {
            return;
        }

        g2d.setFont(this.pcFont);

        final FontMetrics fm = g2d.getFontMetrics();

        int numInUse = 0;
        int numAvailable = 0;

        for (final RawClientPc client : clientList) {

            if (client.iconX != null && client.iconY != null && client.currentStatus != null) {

                final Integer status = client.currentStatus;

                if (RawClientPc.STATUS_FORCE_SUBMIT.equals(status)) {
                    g2d.setColor(this.pcWarningColor);
                    ++numInUse;
                } else if (RawClientPc.STATUS_LOCKED.equals(status)) {
                    g2d.setColor(this.pcLockedColor);
                    ++numAvailable;
                } else if (RawClientPc.STATUS_PAPER_ONLY.equals(status)) {
                    g2d.setColor(this.pcPaperColor);
                } else if (RawClientPc.STATUS_AWAIT_STUDENT.equals(status)
                        || RawClientPc.STATUS_LOGIN_NOCHECK.equals(status)) {
                    g2d.setColor(this.pcAwaitColor);
                    ++numInUse;
                } else if (RawClientPc.STATUS_TAKING_EXAM.equals(status)
                        || RawClientPc.STATUS_EXAM_RESULTS.equals(status)) {
                    g2d.setColor(this.pcInExamColor);
                    ++numInUse;
                } else {
                    g2d.setColor(this.pcErrorColor);
                }

                g2d.fillRect(x + client.iconX.intValue(), y + client.iconY.intValue(), 20, 20);
                g2d.setColor(this.pcOutline);
                g2d.drawRect(x + client.iconX.intValue(), y + client.iconY.intValue(), 20, 20);
                g2d.setColor(this.pcNumber);
                w = fm.stringWidth(client.stationNbr);
                g2d.drawString(client.stationNbr, x + client.iconX.intValue() + (20 - w) / 2,
                        y + client.iconY.intValue() + 18 - fm.getDescent());

                if (this.wheelchairIcon != null && RawClientPc.USAGE_WHEELCHAIR.equals(client.pcUsage)) {
                    g2d.drawImage(this.wheelchairIcon, x + client.iconX.intValue() + 17,
                            y + client.iconY.intValue() + 14, null);
                }
            }
        }

        // Draw the legend
        g2d.setFont(this.legendFont);

        g2d.setColor(Color.WHITE);
        g2d.drawString("LEGEND", x + 680, y + 20);

        g2d.setColor(this.pcErrorColor);
        g2d.fillRect(x + 680, y + 32, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 32, 20, 20);
        g2d.drawString("Station Down", x + 706, y + 50);

        g2d.setColor(this.pcWarningColor);
        g2d.fillRect(x + 680, y + 62, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 62, 20, 20);
        g2d.drawString("Self-Configuring", x + 706, y + 80);

        g2d.setColor(this.pcLockedColor);
        g2d.fillRect(x + 680, y + 92, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 92, 20, 20);
        g2d.drawString("Station Locked", x + 706, y + 110);

        g2d.setColor(this.pcPaperColor);
        g2d.fillRect(x + 680, y + 122, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 122, 20, 20);
        g2d.drawString("Paper Exams Only", x + 706, y + 140);

        g2d.setColor(this.pcAwaitColor);
        g2d.fillRect(x + 680, y + 152, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 152, 20, 20);
        g2d.drawString("Student Login", x + 706, y + 170);

        g2d.setColor(this.pcInExamColor);
        g2d.fillRect(x + 680, y + 182, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 182, 20, 20);
        g2d.drawString("Exam In Progress", x + 706, y + 200);

        g2d.setColor(this.pcOutline);
        g2d.drawString("Stations in use: " + numInUse, x + 680, h - 60);
        g2d.drawString("Stations available: " + numAvailable, x + 680, h - 30);
    }

    /**
     * Draws the exam buttons on the panel.
     *
     * @param g2d the {@code Graphics} to which to draw
     */
    private void drawExamButtons(final Graphics2D g2d) {

        // NOTE: Runs in the AWT event thread.

        // Divide the screen into a 7x6 grid.
        final int w = getWidth() / 7 - 5;
        final int h = getHeight() / 7 - 2;

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
        g2d.fillRect(0, y, 5 * w, getHeight() - y);

        g2d.setColor(new Color(190, 190, 220));
        g2d.fillRect(w, 0, w, y);
        g2d.fillRect(3 * w, 0, w, y);

        g2d.setColor(new Color(170, 170, 170));
        g2d.fillRect(5 * w, 0, 25, getHeight());

        g2d.setColor(new Color(190, 190, 190));
        g2d.fillRect(5 * w + 25, 0, getWidth() - 5 * w - 25, getHeight());

        g2d.setColor(new Color(30, 30, 55));

        y = h / 2 - fm.getDescent();

        int siz = fm.stringWidth("MATH");
        g2d.drawString("MATH", (w - siz) / 2, y);
        siz = fm.stringWidth("117");
        g2d.drawString("117", (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("MATH");
        g2d.drawString("MATH", w + (w - siz) / 2, y);
        siz = fm.stringWidth("118");
        g2d.drawString("118", w + (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("MATH");
        g2d.drawString("MATH", 2 * w + (w - siz) / 2, y);
        siz = fm.stringWidth("124");
        g2d.drawString("124", 2 * w + (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("MATH");
        g2d.drawString("MATH", 3 * w + (w - siz) / 2, y);
        siz = fm.stringWidth("125");
        g2d.drawString("125", 3 * w + (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("MATH");
        g2d.drawString("MATH", 4 * w + (w - siz) / 2, y);
        siz = fm.stringWidth("126");
        g2d.drawString("126", 4 * w + (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("Tutorials");
        g2d.drawString("Tutorials", 5 * w + 30 + (w - siz) / 2, y + fm.getHeight());

        siz = fm.stringWidth("Other");
        g2d.drawString("Other", 6 * w + 30 + (w - siz) / 2, y + fm.getHeight());

        g2d.drawLine(w, 0, w, getHeight());
        g2d.drawLine(2 * w, 0, 2 * w, getHeight());
        g2d.drawLine(3 * w, 0, 3 * w, getHeight());
        g2d.drawLine(4 * w, 0, 4 * w, getHeight());
        g2d.drawLine(5 * w, 0, 5 * w, getHeight());
        g2d.drawLine(5 * w + 25, 0, 5 * w + 25, getHeight());
        g2d.drawLine(6 * w + 30, 0, 6 * w + 30, getHeight());
        g2d.drawLine(0, 6 * h + 5, 5 * w, 6 * h + 5);

        // Enable buttons according to what exams are available

        ExamStatus exam;

        final boolean standardsBased117 = hasExam(RawRecordConstants.M117, 40);
        this.buttons.get("117-1").setVisible(!standardsBased117);
        this.buttons.get("117-2").setVisible(!standardsBased117);
        this.buttons.get("117-3").setVisible(!standardsBased117);
        this.buttons.get("117-4").setVisible(!standardsBased117);
        this.buttons.get("117-5").setVisible(!standardsBased117);
        this.buttons.get("117-40").setVisible(standardsBased117);

        if (standardsBased117) {
            exam = getExam(RawRecordConstants.M117, 40);
            enableButton("117-40", exam.newLabel == null ? MASTERY : exam.newLabel, exam);
        } else {
            exam = getExam(RawRecordConstants.M117, 1);
            enableButton("117-1", exam.newLabel == null ? UNIT_1 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M117, 2);
            enableButton("117-2", exam.newLabel == null ? UNIT_2 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M117, 3);
            enableButton("117-3", exam.newLabel == null ? UNIT_3 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M117, 4);
            enableButton("117-4", exam.newLabel == null ? UNIT_4 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M117, 5);
            enableButton("117-5", exam.newLabel == null ? FINAL : exam.newLabel, exam);
        }

        exam = getExam(ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, 0);
        enableButton(ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, exam.newLabel == null
                ? CHALLENGE : exam.newLabel, exam);

        final boolean standardsBased118 = hasExam(RawRecordConstants.M118, 40);
        this.buttons.get("118-1").setVisible(!standardsBased118);
        this.buttons.get("118-2").setVisible(!standardsBased118);
        this.buttons.get("118-3").setVisible(!standardsBased118);
        this.buttons.get("118-4").setVisible(!standardsBased118);
        this.buttons.get("118-5").setVisible(!standardsBased118);
        this.buttons.get("118-40").setVisible(standardsBased118);

        if (standardsBased118) {
            exam = getExam(RawRecordConstants.M118, 40);
            enableButton("118-40", exam.newLabel == null ? MASTERY : exam.newLabel, exam);
        } else {
            exam = getExam(RawRecordConstants.M118, 1);
            enableButton("118-1", exam.newLabel == null ? UNIT_1 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M118, 2);
            enableButton("118-2", exam.newLabel == null ? UNIT_2 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M118, 3);
            enableButton("118-3", exam.newLabel == null ? UNIT_3 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M118, 4);
            enableButton("118-4", exam.newLabel == null ? UNIT_4 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M118, 5);
            enableButton("118-5", exam.newLabel == null ? FINAL : exam.newLabel, exam);
        }

        exam = getExam(ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, 0);
        enableButton(ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, exam.newLabel == null
                ? CHALLENGE : exam.newLabel, exam);

        final boolean standardsBased124 = hasExam(RawRecordConstants.M124, 40);
        this.buttons.get("124-1").setVisible(!standardsBased124);
        this.buttons.get("124-2").setVisible(!standardsBased124);
        this.buttons.get("124-3").setVisible(!standardsBased124);
        this.buttons.get("124-4").setVisible(!standardsBased124);
        this.buttons.get("124-5").setVisible(!standardsBased124);
        this.buttons.get("124-40").setVisible(standardsBased124);

        if (standardsBased124) {
            exam = getExam(RawRecordConstants.M124, 40);
            enableButton("124-40", exam.newLabel == null ? MASTERY : exam.newLabel, exam);
        } else {
            exam = getExam(RawRecordConstants.M124, 1);
            enableButton("124-1", exam.newLabel == null ? UNIT_1 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M124, 2);
            enableButton("124-2", exam.newLabel == null ? UNIT_2 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M124, 3);
            enableButton("124-3", exam.newLabel == null ? UNIT_3 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M124, 4);
            enableButton("124-4", exam.newLabel == null ? UNIT_4 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M124, 5);
            enableButton("124-5", exam.newLabel == null ? FINAL : exam.newLabel, exam);
        }

        exam = getExam(ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, 0);
        enableButton(ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, exam.newLabel == null
                ? CHALLENGE : exam.newLabel, exam);

        final boolean standardsBased125 = hasExam(RawRecordConstants.M125, 40);
        this.buttons.get("125-1").setVisible(!standardsBased125);
        this.buttons.get("125-2").setVisible(!standardsBased125);
        this.buttons.get("125-3").setVisible(!standardsBased125);
        this.buttons.get("125-4").setVisible(!standardsBased125);
        this.buttons.get("125-5").setVisible(!standardsBased125);
        this.buttons.get("125-40").setVisible(standardsBased125);

        if (standardsBased125) {
            exam = getExam(RawRecordConstants.M125, 40);
            enableButton("125-40", exam.newLabel == null ? MASTERY : exam.newLabel, exam);
        } else {
            exam = getExam(RawRecordConstants.M125, 1);
            enableButton("125-1", exam.newLabel == null ? UNIT_1 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M125, 2);
            enableButton("125-2", exam.newLabel == null ? UNIT_2 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M125, 3);
            enableButton("125-3", exam.newLabel == null ? UNIT_3 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M125, 4);
            enableButton("125-4", exam.newLabel == null ? UNIT_4 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M125, 5);
            enableButton("125-5", exam.newLabel == null ? FINAL : exam.newLabel, exam);
        }

        exam = getExam(ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, 0);
        enableButton(ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, exam.newLabel == null
                ? CHALLENGE : exam.newLabel, exam);

        final boolean standardsBased126 = hasExam(RawRecordConstants.M126, 40);
        this.buttons.get("126-1").setVisible(!standardsBased126);
        this.buttons.get("126-2").setVisible(!standardsBased126);
        this.buttons.get("126-3").setVisible(!standardsBased126);
        this.buttons.get("126-4").setVisible(!standardsBased126);
        this.buttons.get("126-5").setVisible(!standardsBased126);
        this.buttons.get("126-40").setVisible(standardsBased126);

        if (standardsBased126) {
            exam = getExam(RawRecordConstants.M126, 40);
            enableButton("126-40", exam.newLabel == null
                    ? MASTERY : exam.newLabel, exam);
        } else {
            exam = getExam(RawRecordConstants.M126, 1);
            enableButton("126-1", exam.newLabel == null
                    ? UNIT_1 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M126, 2);
            enableButton("126-2", exam.newLabel == null
                    ? UNIT_2 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M126, 3);
            enableButton("126-3", exam.newLabel == null
                    ? UNIT_3 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M126, 4);
            enableButton("126-4", exam.newLabel == null
                    ? UNIT_4 : exam.newLabel, exam);
            exam = getExam(RawRecordConstants.M126, 5);
            enableButton("126-5", exam.newLabel == null
                    ? FINAL : exam.newLabel, exam);
        }

        exam = getExam(ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, 0);
        enableButton(ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, exam.newLabel == null
                ? CHALLENGE : exam.newLabel, exam);

        exam = getExam(RawRecordConstants.M100U, 1);
        enableButton("users", "User's Exam", exam);

        exam = getExam(RawRecordConstants.M100P, 1);
        enableButton("mpe", "Placement", exam);

        exam = getExam(RawRecordConstants.M100T, this.elmUnit == null ? 0 : this.elmUnit.intValue());
        enableButton(ELM, "ELM Exam", exam);

        exam = getExam(RawRecordConstants.M1170, 4);
        enableButton(PRECALC17, "Algebra I", exam);

        exam = getExam(RawRecordConstants.M1180, 4);
        enableButton(PRECALC18, "Algebra II", exam);

        exam = getExam(RawRecordConstants.M1240, 4);
        enableButton(PRECALC24, "Functions", exam);

        exam = getExam(RawRecordConstants.M1250, 4);
        enableButton(PRECALC25, "Trig. I", exam);

        exam = getExam(RawRecordConstants.M1260, 4);
        enableButton(PRECALC26, "Trig. II", exam);

        exam = new ExamStatus(null, 0);
        enableButton(CANCEL, CANCEL, exam);

        // Draw the wheelchair icon if appropriate for the student.
        if (this.isWheelchair) {
            final BufferedImage img = this.wheelchairLit;

            final ExamButton btn = this.buttons.get(ELM);

            if (img != null && btn != null) {
                final int x = btn.getX() + btn.getWidth() - img.getWidth();
                y = btn.getY() + btn.getHeight() + getHeight() / 6 - img.getHeight();
                this.wheelchairRect = new Rectangle(x, y, img.getWidth(), img.getHeight());
                g2d.drawImage(img, x, y, null);
            }
        }
    }

    /**
     * Retrieves the record for a particular course/unit.
     *
     * @param course the course to test
     * @param unit   the unit to test
     * @return the exam, or {@code null} if not found
     */
    private boolean hasExam(final String course, final int unit) {

        final String key = course + CoreConstants.DASH + unit;

        ExamStatus avail = null;

        if (this.exams != null) {
            avail = this.exams.get(key);
        }

        return avail != null;
    }

    /**
     * Retrieves the record for a particular course/unit.
     *
     * @param course the course to test
     * @param unit   the unit to test
     * @return the exam, or {@code null} if not found
     */
    private ExamStatus getExam(final String course, final int unit) {

        final String key = course + CoreConstants.DASH + unit;

        ExamStatus avail = null;

        if (this.exams != null) {
            avail = this.exams.get(key);

            if (avail == null) {
                avail = new ExamStatus(course, unit);
                avail.available = false;
                avail.whyNot = "Unknown State";
                this.exams.put(key, avail);
            }
        }

        return avail;
    }

    /**
     * Sets the enabled state of a particular button.
     *
     * @param key   the key under which the button is stored
     * @param label the button's main label
     * @param exam  the information on the exam's availability
     */
    private void enableButton(final String key, final String label, final ExamStatus exam) {

        final ExamButton btn = this.buttons.get(key);

        // NOTE: Runs in the AWT event thread.

        if (btn == null) {
            Log.warning("Unable to find button with key '", key, "'");
        } else {
            btn.setTitle(label);
            btn.setVisible(true);

            if (exam.whyNot == null) {
                if (ELM.equals(key) && this.elmUnit != null) {

                    // FIXME: get this into data somehow
                    if (this.elmUnit.intValue() == 3) {
                        btn.setMessage("For M 105");
                    } else if (this.elmUnit.intValue() == 4) {
                        btn.setMessage("For M 117");
                    } else {
                        btn.setMessage(null);
                    }
                } else {
                    btn.setMessage(null);
                }
            } else {
                btn.setMessage(exam.whyNot);
            }

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

        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "117-40");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "117-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "117-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "117-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "117-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "117-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M117_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "118-40");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "118-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "118-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "118-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "118-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "118-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M118_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "124-40");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "124-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "124-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "124-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "124-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "124-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M124_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "125-40");
        makeButton(courseColor, font, font2, UNIT_1, x, h, w, h, "125-1");
        makeButton(courseColor, font, font2, UNIT_2, x, 2 * h, w, h, "125-2");
        makeButton(courseColor, font, font2, UNIT_3, x, 3 * h, w, h, "125-3");
        makeButton(courseColor, font, font2, UNIT_4, x, 4 * h, w, h, "125-4");
        makeButton(courseColor, font, font2, FINAL, x, 5 * h, w, h, "125-5");
        makeButton(challengeColor, font, font2, CHALLENGE, x, 6 * h + 10, w, h,
                ChallengeExamLogic.M125_CHALLENGE_EXAM_ID);

        x += w;
        makeButton(courseColor, font, font2, MASTERY, x, h, w, h, "126-40");
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
        makeButton(otherColor, font, font2, "User's Exam", x, h, w + 10, h, "users");
        makeButton(otherColor, font, font2, "Math Placement", x, 2 * h, w + 10, h, "mpe");

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
     * Reads the image files for the wheelchair icon and buttons into buffered images.
     */
    private void loadImages() {

        this.wheelchairIcon = AppFileLoader.loadFileAsImage(CenterPanel.class, "whlchr-tiny.jpg", true);

        // this.mWheelchairDim = FileLoader.loadFileAsImage(this, "whlchr-dim.jpg"); 

        this.wheelchairLit = AppFileLoader.loadFileAsImage(CenterPanel.class, "whlchr-lit.jpg", true);
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
     * Handler for mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        if (this.wheelchairRect != null && this.wheelchairRect.contains(e.getX(), e.getY())) {
            this.owner.setWheelchair(!this.isWheelchair);
        }
    }

    /**
     * Handler for mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        // No action
    }

    /**
     * Handler for mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Handler for mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Handler for mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
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

                if ("users".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M100U, 1, "L");
                } else if ("mpe".equals(cmd)) {
                    this.owner.chooseExam(cache, RawRecordConstants.M100P, 0, "Q");
                } else if (ELM.equals(cmd)) {
                    if (this.elmUnit != null) {
                        this.owner.chooseExam(cache, RawRecordConstants.M100T, this.elmUnit.intValue(), "U");
                    }
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

        List<RawClientPc> clientList;

        // Every 5 seconds, query the testing stations.
        while (isVisible()) {

            final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
            try {
                final DbConnection conn = ctx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
                try {
                    clientList = RawClientPcLogic.queryByTestingCenter(cache, this.testingCenterId);
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                clientList = null;
            }

            if (clientList != null) {
                synchronized (this) {
                    this.clients = clientList;
                }
                repaint();
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
        private final Map<String, ? extends ExamButton> examButtons;

        /**
         * Constructs a new {@code ShowButtons}.
         *
         * @param theButtons the set of exam buttons
         */
        ShowButtons(final Map<String, ? extends ExamButton> theButtons) {

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
     * Runnable class to hide the exam selection buttons. This is intended to run in the AWT event thread.
     */
    private final class HideButtons implements Runnable {

        /** The set of exam buttons. */
        private final Map<String, ? extends ExamButton> examButtons;

        /**
         * Constructs a new {@code HideButtons}.
         *
         * @param theButtons the set of exam buttons
         */
        HideButtons(final Map<String, ? extends ExamButton> theButtons) {

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
