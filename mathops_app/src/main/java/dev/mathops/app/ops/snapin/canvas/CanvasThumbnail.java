package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.app.ops.snapin.AbstractThumbnailButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.Serial;

/**
 * A thumbnail panel for this snap-in.
 */
public class CanvasThumbnail extends AbstractThumbnailButton {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -2598091748172390433L;

    /** The button label. */
    private static final String BTN_LBL = "Canvas";

    /**
     * Constructs a new {@code CanvasThumbnail}.
     */
    CanvasThumbnail() {

        super();
    }

    /**
     * Paints the button.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public final void paintComponent(final Graphics g) {

        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

        final Dimension size = getSize();
        final int cx = size.width / 2;
        final int cy = (size.height - 10) / 2;
        final double r = (double) Math.min(size.width, (size.height - 10)) / 3.0;

        final Path2D path = new Path2D.Double();
        final Ellipse2D head = new Ellipse2D.Double();

        g.setColor(Color.RED);
        for (int i = 0; i < 8; ++i) {
            final double angleRad = Math.toRadians(45.0 * (double)i);
            final double mid1AngleRad = Math.toRadians(45.0 * (double)i - 15.0);
            final double mid2AngleRad = Math.toRadians(45.0 * (double)i + 15.0);
            final double startAngleRad = Math.toRadians(45.0 * (double)i - 17.5);
            final double endAngleRad = Math.toRadians(45.0 * (double)i + 17.5);

            final double startX = (double) cx + r * StrictMath.cos(startAngleRad);
            final double startY = (double) cy + r * StrictMath.sin(startAngleRad);

            final double mid1X = (double) cx + r * StrictMath.cos(mid1AngleRad) * 0.68;
            final double mid1Y = (double) cy + r * StrictMath.sin(mid1AngleRad) * 0.68;

            final double mid2X = (double) cx + r * StrictMath.cos(mid2AngleRad) * 0.68;
            final double mid2Y = (double) cy + r * StrictMath.sin(mid2AngleRad) * 0.68;

            final double endX = (double) cx + r * StrictMath.cos(endAngleRad);
            final double endY = (double) cy + r * StrictMath.sin(endAngleRad);

            final double centerX = (double) cx + r * StrictMath.cos(angleRad);
            final double centerY = (double) cy + r * StrictMath.sin(angleRad);

            path.moveTo(startX, startY);
            path.curveTo(mid1X, mid1Y, mid2X, mid2Y, endX, endY);
            path.curveTo(centerX, centerY, centerX, centerY, startX, startY);
            path.closePath();
            g2d.fill(path);
            path.reset();

            final double headX = (double) cx + r * StrictMath.cos(angleRad) * 0.54;
            final double headY = (double) cy + r * StrictMath.sin(angleRad) * 0.54;
            final double headR = r / 12.0;

            head.setFrame(headX - headR, headY - headR, 2.0 * headR, 2.0 * headR);
            g2d.fill(head);

        }

        g.setFont(FONT);
        final FontMetrics metr = g.getFontMetrics();
        final int txtW = metr.stringWidth(BTN_LBL);

        g.setColor(Color.BLACK);
        g.drawString(BTN_LBL, cx - txtW / 2, size.height - 5);
    }
}
