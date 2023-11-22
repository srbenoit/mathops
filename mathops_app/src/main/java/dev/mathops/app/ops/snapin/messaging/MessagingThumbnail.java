package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.app.ops.snapin.AbstractThumbnailButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;

/**
 * A thumbnail panel for this snap-in.
 */
public final class MessagingThumbnail extends AbstractThumbnailButton {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -7280781022206044855L;

    /** The button label. */
    private static final String BTN_LBL = "Messaging";

    /** The envelope color. */
    private static final Color ENVELOPE_COLOR = new Color(255, 250, 240);

    /** The shadow color. */
    private static final Color SHADOW_COLOR = new Color(205, 200, 190);

    /** The envelope color. */
    private static final Color OUTLINE_COLOR = new Color(160, 80, 45);

    /**
     * Constructs a new {@code MessagingThumbnail}.
     */
    MessagingThumbnail() {

        super();
    }

    /**
     * Paints the button.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

        final Dimension size = getSize();
        final int w = size.width * 3 / 8;
        final int h = size.width / 4;
        final int cx = size.width / 2;
        final int cy = (size.height - 10) / 2;

        g.setColor(ENVELOPE_COLOR);
        g.fillRect(cx - w, cy - h, w << 1, h << 1);

        g.setColor(SHADOW_COLOR);
        g.drawLine(cx - w, cy - h + 1, cx, cy + 1);
        g.drawLine(cx + w, cy - h + 1, cx, cy + 1);
        g.drawLine(cx - w + 1, cy - h + 2, cx + 1, cy + 2);
        g.drawLine(cx + w + 1, cy - h + 2, cx + 1, cy + 2);
        g.drawRect(cx - w + 1, cy - h + 1, w << 1, h << 1);

        g.setColor(Color.BLACK);
        g.drawRect(cx - w, cy - h, w << 1, h << 1);
        g.setColor(OUTLINE_COLOR);
        g.drawLine(cx - w, cy - h, cx, cy);
        g.drawLine(cx + w, cy - h, cx, cy);

        g.setFont(FONT);
        final FontMetrics metr = g.getFontMetrics();
        final int txtW = metr.stringWidth(BTN_LBL);

        g.setColor(Color.BLACK);
        g.drawString(BTN_LBL, cx - txtW / 2, size.height - 5);
    }
}
