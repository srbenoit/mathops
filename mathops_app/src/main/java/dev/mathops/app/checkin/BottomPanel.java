package dev.mathops.app.checkin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.font.BundledFontManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.time.LocalTime;

/**
 * The bottom panel, showing "CHECK IN", the current time, and the time remaining until closing. When an exam is to be
 * started, the exam's duration (including student time-limit factor adjustments) is compared with the time remaining to
 * close, and a warning is shown if the student does not have their full measure of time remaining.
 */
public class BottomPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4327818980945601933L;

    /** The text message to display in the panel. */
    private String message;

    /** The font in which to draw the message. */
    private Font messageFont;

    /** The font in which to draw the current time clock. */
    private Font clockFont;

    /** The font in which to draw the time remaining. */
    private Font remainFont;

    /** The color in which to paint the message. */
    private Color color;

    /** The shadow color. */
    private Color shadow;

    /** The offscreen image buffer. */
    private BufferedImage backbuffer;

    /** The graphics context of the back buffer. */
    private Graphics2D offscreen;

    /** The closing time. */
    private LocalTime closingTime;

    /** The current time string. */
    private String currentTimeStr;

    /** The closing time string. */
    private String closingTimeString;

    /**
     * Constructs a new {@code BottomPanel}.
     *
     * @param screen the size of the screen or window this panel is contained in
     */
    public BottomPanel(final Dimension screen) {

        super();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        setDoubleBuffered(false);
        setBackground(new Color(210, 210, 235));
        setPreferredSize(new Dimension(screen.width, screen.height / 7));
        updateColor();
        setFocusable(true);

        final LocalTime now = LocalTime.now();
        this.currentTimeStr = TemporalUtils.FMT_HM_A.format(now);
    }

    /**
     * Updates the color in which to paint the text, based on the current background color.
     */
    private void updateColor() {

        // NOTE: Runs in the AWT event thread.

        final Color bg = getBackground();

        // Generate a slightly lighter color than the background
        int r = bg.getRed();
        int g = bg.getGreen();
        int b = bg.getBlue();
        r = r * 2 / 3;
        g = g * 2 / 3;
        b = b * 2 / 3;
        this.color = new Color(r, g, b);

        r = this.color.getRed();
        g = this.color.getGreen();
        b = this.color.getBlue();
        r = r * 2 / 3;
        g = g * 2 / 3;
        b = b * 2 / 3;
        this.shadow = new Color(r, g, b);
    }

    /**
     * Sets the message text to display in the panel.
     *
     * @param theMessage the new message text
     */
    public void setMessage(final String theMessage) {
        synchronized (this) {

            this.message = theMessage;
            this.messageFont = null;
            this.clockFont = null;
            this.remainFont = null;

            if (this.offscreen != null) {
                drawOffscreen();
                repaint();
            }
        }
    }

    /**
     * Sets the closing time.
     *
     * @param theClosingTime the closing time
     */
    public void setClosingTime(final LocalTime theClosingTime) {
        synchronized (this) {

            this.closingTime = theClosingTime;

            if (this.offscreen != null) {
                drawOffscreen();
                repaint();
            }
        }
    }

    /**
     * Refreshes the display if needed.
     */
    public void refresh() {
        synchronized (this) {

            final LocalTime now = LocalTime.now();

            String closing = CoreConstants.EMPTY;
            if (this.closingTime != null) {
                final int nowMin = TemporalUtils.minuteOfDay(now);
                final int closeMin = TemporalUtils.minuteOfDay(this.closingTime);
                final int remain = closeMin - nowMin;

                if (remain < 0) {
                    closing = SimpleBuilder.concat(TemporalUtils.FMT_HM_A.format(this.closingTime),
                            " [CLOSED]");
                } else {
                    closing =
                            SimpleBuilder.concat("Closing in ", Integer.toString(remain), " minutes.");
                }
            }

            this.currentTimeStr = TemporalUtils.FMT_HM_A.format(now);
            this.closingTimeString = closing;

            if (this.offscreen != null) {
                drawOffscreen();
                repaint();
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
        synchronized (this) {

            if (this.offscreen == null) {
                this.backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                this.offscreen = (Graphics2D) this.backbuffer.getGraphics();

                // Configure permanent attributes of the drawing context.
                this.offscreen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                drawOffscreen();
            }

            g.drawImage(this.backbuffer, 0, 0, this);
        }
    }

    /**
     * Updates the off-screen image.
     */
    private void drawOffscreen() {
        synchronized (this) {

            // NOTE: Does not run in the AWT thread, but only draws to a private Graphics pointing to a
            // private BufferedImage, so it should not impact the AWT tree.

            final Graphics2D grx = this.offscreen;
            if (grx == null) {
                return;
            }

            grx.setColor(getBackground());
            grx.fillRect(0, 0, getWidth(), getHeight());

            final Dimension size = getSize();
            final int height = size.height;
            final int width = size.width;

            // Generate a string that is the width of the message and a number of nines equal to the
            // width of the field.
            final String test = (this.message != null) ? this.message : CoreConstants.EMPTY;

            int pts;

            if (!test.isEmpty()) {

                if (this.messageFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 200) {
                        this.messageFont = bfm.getFont(BundledFontManager.SERIF, (double) pts, Font.BOLD);
                        this.clockFont = bfm.getFont(BundledFontManager.SERIF, (double) (pts * 3) / 5.0, Font.BOLD);
                        this.remainFont =
                                bfm.getFont(BundledFontManager.SERIF, (double) (pts * 3) / 10.0, Font.PLAIN);

                        final FontMetrics fm = grx.getFontMetrics(this.messageFont);

                        if ((double) fm.stringWidth(test) >= ((double) width * 0.8)
                                || (double) fm.getHeight() >= ((double) height * 0.8)) {
                            break;
                        }

                        pts += 4;
                    }
                }

                grx.setFont(this.messageFont);

                final FontMetrics fm = grx.getFontMetrics();
                final int x1 = getHeight() / 3;

                int yPix = ((fm.getAscent() * 3 / 4) + height) / 2;

                if ((yPix + fm.getDescent() + fm.getLeading()) > height) {
                    yPix = height - fm.getDescent() - fm.getLeading();
                }

                if (this.message != null) {
                    grx.setColor(this.shadow);
                    grx.drawString(this.message, x1 + 1, yPix);
                    grx.drawString(this.message, x1 - 1, yPix);
                    grx.drawString(this.message, x1, yPix + 1);
                    grx.drawString(this.message, x1, yPix - 1);

                    grx.setColor(this.color);
                    grx.drawString(this.message, x1, yPix);
                }

                if (this.currentTimeStr != null) {
                    grx.setFont(this.clockFont);

                    final FontMetrics fm2 = grx.getFontMetrics();
                    final int yPix2 = yPix - (fm.getAscent() - fm2.getAscent());

                    final int x2 = getWidth() - x1 - fm2.stringWidth(this.currentTimeStr);

                    grx.setColor(this.shadow);
                    grx.drawString(this.currentTimeStr, x2 + 1, yPix2);
                    grx.drawString(this.currentTimeStr, x2 - 1, yPix2);
                    grx.drawString(this.currentTimeStr, x2, yPix2 + 1);
                    grx.drawString(this.currentTimeStr, x2, yPix2 - 1);

                    grx.setColor(this.color);
                    grx.drawString(this.currentTimeStr, x2, yPix2);

                    if (this.closingTimeString != null) {
                        Log.info(this.closingTimeString);
                        grx.setFont(this.remainFont);

                        grx.setColor(this.shadow);
                        grx.drawString(this.closingTimeString, x2, yPix);
                    }
                }
            }
        }
    }
}
