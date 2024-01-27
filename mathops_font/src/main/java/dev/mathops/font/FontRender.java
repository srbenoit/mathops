package dev.mathops.font;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

/**
 * A class to render some text.
 */
final class FontRender {

    /** The default panel width. */
    private static final int PANEL_WIDTH = 600;

    /** The default panel height. */
    private static final int PANEL_HEIGHT = 600;

    /** The default font size. */
    private static final int FONT_SIZE = 9;

    /** The path of the font to load. */
    private static final String PATH = "komika.ttf";

    /** A test string to print at small font size. */
    private static final String TEST1 = "The quick brown fox jumps over the lazy dog.";

    /** Error message text. */
    private static final String ERR = "Failed to read ";

    /** The panel. */
    private final FontRenderPanel renderPanel;

    /** The font to render. */
    private Font font;

    /**
     * Constructs a new {@code FontRender}.
     */
    private FontRender() {

        this.renderPanel = new FontRenderPanel(this);
        this.renderPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.renderPanel.setBackground(Color.WHITE);

        try (final InputStream input = new FileInputStream(PATH)) {
            this.font = Font.createFont(Font.TRUETYPE_FONT, input);
        } catch (final IOException | FontFormatException ex1) {
            Log.warning(ERR, PATH, ex1);
            this.font = new Font(Font.DIALOG, Font.PLAIN, FONT_SIZE);
        }
    }

    /**
     * Main method to create the panel and show it.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final JFrame frame = new JFrame("Render Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new FontRender().renderPanel);
        UIUtilities.packAndCenter(frame);
    }

    /**
     * The panel.
     */
    private static final class FontRenderPanel extends JPanel {

        /** The default LCD contrast to set. */
        private static final int LCD_CONTRAST = 100;

        /** Left edge at which to begin drawing. */
        private static final int LEFT = 6;

        /** Width of each column of text. */
        private static final int COLUMN = 150;

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 50159041185573735L;

        /** The owning {@code FontRender} object. */
        private final FontRender owner;

        /**
         * Constructs a new {@code FontRenderPanel}.
         *
         * @param owningFontRender the owning {@code FontRender} object
         */
        FontRenderPanel(final FontRender owningFontRender) {

            super();

            this.owner = owningFontRender;
        }

        /**
         * Renders the panel.
         *
         * @param g the {@code Graphics} to which to render
         */
        @Override
        public void paintComponent(final Graphics g) {

            super.paintComponent(g);

            if (g instanceof final Graphics2D g2d) {
                // Gather the font sizes to use
                final Font[] fonts = {this.owner.font.deriveFont(30.0f), this.owner.font.deriveFont(20.0f),
                        this.owner.font.deriveFont(10.0f), this.owner.font.deriveFont(8.0f),};

                // Gather the rendering modes to use and their labels
                int height = g.getFontMetrics(fonts[0]).getHeight();
                int yPos = height;

                final Object[] modes = {RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB,};
                final int numModes = modes.length;
                final String[] labels = {"Normal", "Antialias", "Gasp", "HBGR", "HRGB", "VBGR", "VRGB",};

                for (int i = 0; i < numModes; ++i) {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, modes[i]);
                    drawTestString(g2d, fonts, labels[i], yPos);
                    yPos += height;
                }

                g2d.setFont(fonts[fonts.length - 1]);
                height = g2d.getFontMetrics().getHeight();

                for (int i = 0; i < numModes; ++i) {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, modes[i]);
                    g2d.drawString("This is a test of font rendering at eight points (" + labels[i] + ")", LEFT, yPos);
                    yPos += height;
                }

                g2d.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, Integer.valueOf(LCD_CONTRAST));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                for (final Font value : fonts) {
                    g2d.setFont(value);
                    yPos += g2d.getFontMetrics().getAscent();
                    g2d.drawString("!\"#$%&'()*+,-./ (Using HRGB)", LEFT, yPos);
                    yPos += g2d.getFontMetrics().getDescent();
                    yPos += g2d.getFontMetrics().getLeading();
                }
            }
        }

        /**
         * Draws a test string in all font sizes.
         *
         * @param g2d   the {@code Graphics2D} to which to draw
         * @param fonts the list of fonts to use to draw the string
         * @param str   the string to draw
         * @param yPos  the Y position at which to draw
         */
        private static void drawTestString(final Graphics2D g2d, final Font[] fonts,
                                           final String str, final int yPos) {

            int xPos = LEFT;

            for (final Font value : fonts) {
                g2d.setFont(value);
                g2d.drawString(str, xPos, yPos);
                xPos += COLUMN;
            }

            g2d.drawString(TEST1, xPos, yPos);
        }
    }
}
