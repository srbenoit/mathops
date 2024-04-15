package dev.mathops.db.oldadmin;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A console panel that shows text (perhaps inverted) and az cursor.
 */
public final class Console extends JPanel {

    /** Margin on left and right. */
    private static final int LEFT_RIGHT_MARGIN = 5;

    /** Margin on top and bottom. */
    private static final int TOP_BOTTOM_MARGIN = 5;

    /** The font. */
    private final Font font;

    /** The off-screen image. */
    private final BufferedImage offscreen;

    /** A {@code Graphics2D} that draws to the off-screen image. */
    private final Graphics2D g2d;

    /** The character width. */
    private final int charWidth;

    /** The character height. */
    private final int charHeight;

    /** The character ascent. */
    private final int ascent;

    /**
     * Constructs a new {@code Console}.
     *
     * @param numColumns the number of columns of text
     * @param numLines the number of lines of text
     */
    Console(final int numColumns, final int numLines) {

        super();

        setBackground(Color.BLACK);

        final byte[] fontBytes = FileLoader.loadFileAsBytes(Console.class, "Consolas.ttf", true);
        Font loadedFont = null;
        if (fontBytes == null) {
            loadedFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        } else {
            try (final ByteArrayInputStream in = new ByteArrayInputStream(fontBytes)) {
                final Font onePoint = Font.createFont(Font.TRUETYPE_FONT, in);
                loadedFont = onePoint.deriveFont(13f);
                Log.info("Loaded the Consolas font.");
            } catch (final IOException | FontFormatException ex) {
                loadedFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
            }
        }
        this.font = loadedFont;

        final BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = temp.createGraphics();
        g.setFont(this.font);
        final FontMetrics metr = g.getFontMetrics();
        this.charWidth = metr.stringWidth("0");
        this.charHeight = metr.getHeight();
        this.ascent = metr.getAscent();
        g.dispose();

        final int windowWidth = 2 + LEFT_RIGHT_MARGIN + this.charWidth * numColumns;
        final int windowHeight = 2 + TOP_BOTTOM_MARGIN + this.charHeight * numLines;

        setPreferredSize(new Dimension(windowWidth, windowHeight));

        this.offscreen = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.g2d = this.offscreen.createGraphics();
        this.g2d.setFont(this.font);
        this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        buildScreenImage();
    }

    /**
     * Builds the off-screen image.
     */
    private void buildScreenImage() {

        synchronized (this.offscreen) {
            final int w = this.offscreen.getWidth();
            final int h = this.offscreen.getHeight();

            this.g2d.setColor(Color.BLACK);
            this.g2d.fillRect(0, 0, w, h);

            this.g2d.setColor(Color.WHITE);
            int x = LEFT_RIGHT_MARGIN;
            int y = TOP_BOTTOM_MARGIN + this.ascent;

            this.g2d.drawString("M", x, y);
            x += this.charWidth;
            this.g2d.drawString("A", x, y);
            x += this.charWidth;
            this.g2d.drawString("I", x, y);
            x += this.charWidth;
            this.g2d.drawString("N", x, y);
            x += this.charWidth;
            x += this.charWidth;
            this.g2d.drawString("ADMIN", x, y);
        }
    }

    /**
     * Paints the component.
     *
     * @param g the {@code Graphics} object to protect
     */
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        synchronized (this.offscreen) {
            g.drawImage(this.offscreen, 0, 0, null);
        }
    }
}
