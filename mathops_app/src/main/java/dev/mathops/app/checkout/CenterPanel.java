package dev.mathops.app.checkout;

import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.font.BundledFontManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

/**
 * A panel to appear in the center of the checkin application. This panel displays a map of the testing center, showing
 * the status of all stations, and is used for buttons when selecting course/unit for exams.
 */
class CenterPanel extends JPanel implements Runnable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7029995559760361198L;

    /** Application version. */
    private static final String VERSION = "v2.3.4 (Aug 22 2022)";

    /** The database profile. */
    private final DbProfile dbProfile;

    /** The testing center ID being managed. */
    private final String testingCenterId;

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

    /** The client computer list. */
    private transient List<RawClientPc> clients;

    /**
     * Constructs a new {@code CenterPanel}.
     *
     * @param theDbProfile       the database profile
     * @param theTestingCenterId the testing center ID being managed
     */
    CenterPanel(final DbProfile theDbProfile, final String theTestingCenterId) {

        super();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

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
    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {
        synchronized (this) {

            final Graphics2D g2d = (Graphics2D) g;

            super.paintComponent(g);

            // Configure permanent attributes of the drawing context.
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            drawMap(g2d);

            g2d.setFont(this.pcFont);
            final int h = getHeight();
            g2d.setColor(Color.GRAY);
            g2d.drawString(VERSION, 8, h - 8);
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
        final int x = (w - 642) / 2;
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
        for (final RawClientPc client : clientList) {

            if (client.iconX != null && client.iconY != null && client.currentStatus != null) {

                final Integer status = client.currentStatus;

                if (RawClientPc.STATUS_FORCE_SUBMIT.equals(status)) {
                    g2d.setColor(this.pcWarningColor);
                    ++numInUse;
                } else if (RawClientPc.STATUS_LOCKED.equals(status)) {
                    g2d.setColor(this.pcLockedColor);
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
            }
        }

        // Draw the legend
        g2d.setColor(Color.WHITE);
        g2d.drawString("LEGEND", x + 680, y + 20);

        g2d.setColor(this.pcErrorColor);
        g2d.fillRect(x + 680, y + 35, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 35, 20, 20);
        g2d.drawString("Station Down", x + 706, y + 50);

        g2d.setColor(this.pcWarningColor);
        g2d.fillRect(x + 680, y + 60, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 60, 20, 20);
        g2d.drawString("Self-Configuring", x + 706, y + 75);

        g2d.setColor(this.pcLockedColor);
        g2d.fillRect(x + 680, y + 85, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 85, 20, 20);
        g2d.drawString("Station Locked", x + 706, y + 100);

        g2d.setColor(this.pcPaperColor);
        g2d.fillRect(x + 680, y + 110, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 110, 20, 20);
        g2d.drawString("Paper Exams Only", x + 706, y + 125);

        g2d.setColor(this.pcAwaitColor);
        g2d.fillRect(x + 680, y + 135, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 135, 20, 20);
        g2d.drawString("Student Login", x + 706, y + 150);

        g2d.setColor(this.pcInExamColor);
        g2d.fillRect(x + 680, y + 160, 20, 20);
        g2d.setColor(this.pcOutline);
        g2d.drawRect(x + 680, y + 160, 20, 20);
        g2d.drawString("Exam In Progress", x + 706, y + 175);

        g2d.setColor(this.pcOutline);
        g2d.drawString("Stations in use: " + numInUse, x + 680, h - 30);
        // g2d.drawString("Stations available: " + numAvailable, x + 680, h - 10);
    }

    /**
     * Updates the list of client computers periodically.
     */
    @Override
    public void run() {

        List<RawClientPc> clientList;

        // Every 5 seconds, query the testing stations
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
                Log.warning(ex);
            }
        }
    }
}
