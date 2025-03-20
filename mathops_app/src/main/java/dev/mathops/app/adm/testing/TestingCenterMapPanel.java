package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.rawrecord.RawClientPc;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serial;
import java.util.List;

/**
 * A panel that renders a map of all configured testing centers, with all machines placed and with each machine's status
 * shown.
 */
final class TestingCenterMapPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3966709797464460260L;

    /** The list of clients to render. */
    private final List<RawClientPc> clients;

    /** The color in which to fill in the outline of the map. */
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

    /** The color for a "powered on" badge. */
    private final Color onColor;

    /** The color for a "powered off" badge. */
    private final Color offColor;

    /** The color for a "powering on" badge. */
    private final Color poweringColor;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /** The font in which to draw legend entries. */
    private final Font legendFont;

    /**
     * Constructs a new {@code TestingCenterMapPanel}.
     *
     * @param theClients the list of clients (access to this list should be synchronized on the list)
     */
    TestingCenterMapPanel(final List<RawClientPc> theClients) {

        super();

        this.clients = theClients;

        setBackground(Skin.OFF_WHITE_GREEN);

        setLayout(null);
        setFocusable(true);
        setDoubleBuffered(true);

        this.mapFill = new Color(250, 250, 250);
        this.mapOutline = Color.black;
        this.tableColor = new Color(205, 112, 84);
        this.tableOutline = new Color(139, 0, 0);

        this.pcErrorColor = new Color(235, 150, 150);
        this.pcWarningColor = new Color(212, 155, 240);
        this.pcLockedColor = new Color(250, 240, 160);
        this.pcPaperColor = new Color(214, 214, 214);
        this.pcAwaitColor = new Color(135, 227, 230);
        this.pcInExamColor = new Color(172, 230, 172);

        this.pcOutline = Color.black;
        this.pcNumber = Color.black;

        this.onColor = Color.GREEN;
        this.offColor = Color.RED;
        this.poweringColor = Color.ORANGE;

        this.pcFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        this.legendFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    }

    /**
     * Paints the panel.
     */
    @Override
    public void paintComponent(final Graphics g) {


        Log.info("paintComponent");

        super.paintComponent(g);

        if (g instanceof final Graphics2D g2d) {
            drawMap(g2d);
        }
    }

    /**
     * Refreshes the display.
     */
    void refresh() {

        repaint();
    }

    /**
     * Draws the map.
     *
     * @param g2d the {@code Graphics} to which to draw
     */
    private void drawMap(final Graphics2D g2d) {

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int w = getWidth();
        final int h = getHeight();

        final double xScale = (double) w / 1040.0;
        final double yScale = (double) h / 670.0;
        final double scld = Math.min(xScale, yScale);
        final float sclf = (float)scld;

        final int cx = w / 2;
        final int cy = h / 2;
        final int x = cx - Math.round(sclf * 340.0f);
        final int y = cy - Math.round(sclf * 290.0f);

        final Path2D path = new Path2D.Float();
        final Rectangle2D rect = new Rectangle2D.Float();
        final Ellipse2D oval = new Ellipse2D.Float();
        final RoundRectangle2D rrect = new RoundRectangle2D.Float();

        // Draw the quiet testing room outline

        path.moveTo((double) x - scld * 150.0, y);
        path.lineTo((double) x - scld * 70.0, y);
        path.lineTo((double) x - scld * 70.0, (double) y + scld * 100.0);
        path.lineTo((double) x - scld * 150.0, (double) y + scld * 100.0);
        path.closePath();

        g2d.setColor(this.mapFill);
        g2d.fill(path);
        g2d.setColor(this.mapOutline);
        g2d.draw(path);
        path.reset();

        // Draw the main room outline

        path.moveTo((double) x + scld * 28.0, y);
        path.lineTo((double) x + scld * 312.0, y);
        path.lineTo((double) x + scld * 312.0, (double) y + scld * 4.0);
        path.lineTo((double) x + scld * 336.0, (double) y + scld * 4.0);
        path.lineTo((double) x + scld * 336.0, y);
        path.lineTo((double) x + scld * 642.0, y);
        path.lineTo((double) x + scld * 642.0, (double) y + scld * 528.0);
        path.lineTo((double) x + scld * 222.0, (double) y + scld * 528.0);
        path.lineTo((double) x + scld * 222.0, (double) y + scld * 612.0);
        path.lineTo(x, (double) y + scld * 612.0);
        path.lineTo(x, (double) y + scld * 28.0);
        path.lineTo((double) x + scld * 28.0, (double) y + scld * 28.0);
        path.closePath();

        g2d.setColor(this.mapFill);
        g2d.fill(path);
        g2d.setColor(this.mapOutline);
        g2d.draw(path);
        path.reset();

        // Draw columns
        rect.setRect((double) x + scld * 312.0, (double) y + scld * 186.0, scld * 20.0, scld * 20.0);
        g2d.draw(rect);
        rect.setRect((double) x + scld * 312.0, (double) y + scld * 392.0, scld * 20.0, scld * 20.0);
        g2d.draw(rect);

        // Draw doors
        rect.setRect((double) x - scld * 115.0, y, scld * 40.0, scld * 3.0);
        g2d.fill(rect);

        rect.setRect((double) x + scld * 470.0, (double) y + scld * 526.0, scld * 40.0, scld * 5.0);
        g2d.fill(rect);
        rect.setRect((double) x + scld * 220.0, (double) y + scld * 538.0, scld * 5.0, scld * 40.0);
        g2d.fill(rect);
        rect.setRect((double) x + scld * 340.0, y, scld * 40.0, scld * 3.0);
        g2d.fill(rect);

        // Draw tables (first is in quiet testing)

        rrect.setRoundRect((double) x - scld * 147.0, (double) y + scld * 3.0, scld * 28.0, scld * 94.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 30.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 104.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 186.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 272.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 356.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 447.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 24.0, (double) y + scld * 521.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 98.0, (double) y + scld * 48.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 98.0, (double) y + scld * 174.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 98.0, (double) y + scld * 300.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 98.0, (double) y + scld * 426.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 86.0, (double) y + scld * 580.0, scld * 72.0, scld * 28.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 200.0, (double) y + scld * 48.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 200.0, (double) y + scld * 174.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 200.0, (double) y + scld * 300.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 200.0, (double) y + scld * 426.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 312.0, (double) y + scld * 38.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 312.0, (double) y + scld * 112.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 312.0, (double) y + scld * 208.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 312.0, (double) y + scld * 318.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 378.0, (double) y + scld * 52.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 378.0, (double) y + scld * 133.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 378.0, (double) y + scld * 219.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 378.0, (double) y + scld * 302.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 378.0, (double) y + scld * 384.0, scld * 28.0, scld * 72.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 452.0, (double) y + scld * 48.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 452.0, (double) y + scld * 174.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 452.0, (double) y + scld * 300.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 452.0, (double) y + scld * 426.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + scld * 550.0, (double) y + scld * 48.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 550.0, (double) y + scld * 174.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 550.0, (double) y + scld * 300.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + scld * 550.0, (double) y + scld * 426.0, scld * 72.0, scld * 56.0, scld * 4.0, scld * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        // Draw all client PCs with numbers

        g2d.setFont(this.pcFont.deriveFont(sclf * 15.0f));

        final FontMetrics fm = g2d.getFontMetrics();

        int numInUse = 0;
        int numAvailable = 0;

        synchronized (this.clients) {
            for (final RawClientPc client : this.clients) {

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
                    } else if (RawClientPc.STATUS_AWAIT_STUDENT.equals(status)) {
                        g2d.setColor(this.pcAwaitColor);
                        ++numInUse;
                    } else if (RawClientPc.STATUS_TAKING_EXAM.equals(status)
                            || RawClientPc.STATUS_EXAM_RESULTS.equals(status)) {
                        g2d.setColor(this.pcInExamColor);
                        ++numInUse;
                    } else {
                        g2d.setColor(this.pcErrorColor);
                    }

                    rect.setRect((double) x + scld * (double) (client.iconX.intValue() - 2),
                            (double) y + scld * (double) (client.iconY.intValue() - 1), scld * 28.0, scld * 22.0);
                    g2d.fill(rect);
                    g2d.setColor(this.pcOutline);
                    g2d.draw(rect);

                    g2d.setColor(this.pcNumber);
                    final int strw = fm.stringWidth(client.stationNbr);

                    final int midx = Math.round((float) x + sclf * (client.iconX.floatValue() + 12.0f));
                    final int midy = Math.round((float) y + sclf * (client.iconY.floatValue() + 9.5f));

                    g2d.drawString(client.stationNbr, midx - strw / 2,
                            midy + fm.getAscent() / 2 - 1);

                    // Draw the power status badge
                    switch (client.powerStatus) {
                        case RawClientPc.POWER_REPORTING_ON -> g2d.setColor(this.onColor);
                        case RawClientPc.POWER_OFF -> g2d.setColor(this.offColor);
                        case null, default -> g2d.setColor(this.poweringColor);
                    }

                    oval.setFrame((double) x + scld * (client.iconX.doubleValue() - 4.0),
                            (double) y + scld * (client.iconY.doubleValue() - 3.0), scld * 8.0, scld * 8.0);
                    g2d.fill(oval);
                    g2d.setColor(this.pcOutline);
                    g2d.draw(oval);
                }
            }
        }

        // Draw the legend
        final Font newLegendFont = this.legendFont.deriveFont(sclf * 18.0f);
        g2d.setFont(newLegendFont);

        g2d.setColor(Color.BLACK);
        g2d.drawString("LEGEND", (int) ((float) x + sclf * 680.0f), (int) ((float) y + sclf * 20.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 32.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcErrorColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Station Down", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 50.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 62.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcWarningColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Self-Configuring", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 80.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 92.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcLockedColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Station Locked", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 110.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 122.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcPaperColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Paper Exams Only", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 140.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 152.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcAwaitColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Student Login", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 170.0f));

        rect.setRect((double) x + scld * 680.0, (double) y + scld * 182.0, scld * 20.0, scld * 20.0);
        g2d.setColor(this.pcInExamColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Exam In Progress", (int) ((float) x + sclf * 706.0f), (int) ((float) y + sclf * 200.0f));

        g2d.setColor(this.pcOutline);
        g2d.drawString("Stations in use: " + numInUse, (int) ((float) x + sclf * 680.0f),
                (int) ((float) h - sclf * 60.0f));
        g2d.drawString("Stations available: " + numAvailable, (int) ((float) x + sclf * 680.0f),
                (int) ((float) h - sclf * 30.0f));
    }
}
