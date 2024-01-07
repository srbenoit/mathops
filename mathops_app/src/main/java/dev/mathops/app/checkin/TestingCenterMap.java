package dev.mathops.app.checkin;

import dev.mathops.core.file.FileLoader;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.font.BundledFontManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A class that can draw the testing center map.
 */
public final class TestingCenterMap {

    /** The client computer list (can be updated to update the map).  */
    private final List<RawClientPc> stations;

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
    private final BufferedImage wheelchairIcon;

    /**
     * Constructs a new {@code TestingCenterMap}.
     */
    public TestingCenterMap() {

        this.stations = new ArrayList<>(100);

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

        this.wheelchairIcon = FileLoader.loadFileAsImage(TestingCenterMap.class, "whlchr-tiny.jpg", true);
    }

    /**
     * Updates the list of testing stations.
     *
     * @param theStations the new testing stations list
     */
    public void updateTestingStations(final Collection<RawClientPc> theStations) {

        if (Objects.nonNull(theStations)) {
            synchronized (this.stations) {
                this.stations.clear();
                this.stations.addAll(theStations);
            }
        }
    }

    private static final int DESIRED_MAP_HEIGHT = 675;

    /**
     * Draws the map.  This should be called on the AWT event thread.
     *
     * @param g2d the {@code Graphics} to which to draw
     * @param width the width of the area in which to draw
     * @param height the height of the area in which to draw
     */
    public void drawMap(final Graphics2D g2d, final int width, final int height) {

        // The map itself is 642 wide by 612 high, and we would like a buffer top and bottom of at least 5%, so we want
        // a height of at least 675.  If the height we are given is less than this, scale...

        int sh = height;
        int sw = width;

        final AffineTransform origXform = g2d.getTransform();

        if (height < DESIRED_MAP_HEIGHT) {
            final double translateY = origXform.getTranslateY();
            g2d.translate(0.0, -translateY);

            final double scale = (double)height / (double) DESIRED_MAP_HEIGHT;
            g2d.scale(scale, scale);

            g2d.translate(0.0, translateY / scale);

            sh = (int)((double) height / scale);
            sw = (int)((double) width / scale);
        }

        final int x = Math.max(10, ((sw << 2) / 5 - 642) / 2);
        final int y = (sh - 612) / 2;

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
            clientList = this.stations;
        }

        if (Objects.nonNull(clientList)) {
            g2d.setFont(this.pcFont);

            final FontMetrics fm = g2d.getFontMetrics();

            int numInUse = 0;
            int numAvailable = 0;

            for (final RawClientPc client : clientList) {

                if (Objects.nonNull(client.iconX) && Objects.nonNull(client.iconY)
                        && Objects.nonNull(client.currentStatus)) {

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

                    final int clientX = client.iconX.intValue();
                    final int clientY = client.iconY.intValue();
                    g2d.fillRect(x + clientX, y + clientY, 20, 20);
                    g2d.setColor(this.pcOutline);
                    g2d.drawRect(x + clientX, y + clientY, 20, 20);
                    g2d.setColor(this.pcNumber);
                    final int nbrWidth = fm.stringWidth(client.stationNbr);
                    final int descent = fm.getDescent();
                    g2d.drawString(client.stationNbr, x + clientX + (20 - nbrWidth) / 2, y + clientY + 18 - descent);

                    if (Objects.nonNull(this.wheelchairIcon) && RawClientPc.USAGE_WHEELCHAIR.equals(client.pcUsage)) {
                        g2d.drawImage(this.wheelchairIcon, x + clientX + 17, y + clientY + 14, null);
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
            g2d.drawString("Stations in use: " + numInUse, x + 680, sh - 60);
            g2d.drawString("Stations available: " + numAvailable, x + 680, sw - 30);
        }

        g2d.setTransform(origXform);
    }
}
