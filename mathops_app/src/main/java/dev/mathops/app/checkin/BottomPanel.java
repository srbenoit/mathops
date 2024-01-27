package dev.mathops.app.checkin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
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
import java.util.Objects;

/**
 * The bottom panel, showing "CHECK IN", the current time, and the time remaining until closing. When an exam is to be
 * started, the exam's duration (including student time-limit factor adjustments) is compared with the time remaining to
 * close, and a warning is shown if the student does not have their full measure of time remaining.
 */
public final class BottomPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4327818980945601933L;

    /** Object on which to synchronize changes. */
    private final Object synch;

    /** The text message to display in the panel. */
    private String message = null;

    /** The font in which to draw the current time clock. */
    private Font clockFont = null;

    /** The font in which to draw the time remaining. */
    private Font closingFont = null;

    /** The color in which to paint the message. */
    private final Color color;

    /** The shadow color. */
    private final Color shadow;

    /** The off-screen image buffer. */
    private BufferedImage backBuffer = null;

    /** The graphics context of the back buffer. */
    private Graphics2D offScreen = null;

    /** The closing time. */
    private LocalTime closingTime = null;

    /** The current time string. */
    private String currentTimeStr;

    /** The closing time string. */
    private String closingTimeString = null;

    /**
     * Constructs a new {@code BottomPanel}.
     *
     * @param screen the size of the screen or window this panel is contained in
     */
    BottomPanel(final Dimension screen) {

        super();

        this.synch = new Object();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        setDoubleBuffered(false);
        setBackground(new Color(210, 210, 235));
        setPreferredSize(new Dimension(screen.width, screen.height / 7));

        final Color bg = getBackground();
        this.color = bg.darker();
        this.shadow = this.color.darker();

        setFocusable(true);

        final LocalTime now = LocalTime.now();
        this.currentTimeStr = TemporalUtils.FMT_HM_A.format(now);
    }

    /**
     * Sets the message text to display in the panel.
     *
     * @param theMessage the new message text
     */
    public void setMessage(final String theMessage) {

        synchronized (this.synch) {
            this.message = theMessage;

            if (this.offScreen != null) {
                drawOffScreen(this.offScreen);
                repaint();
            }
        }
    }

    /**
     * Sets the closing time.
     *
     * @param theClosingTime the closing time
     */
    void setClosingTime(final LocalTime theClosingTime) {

        synchronized (this.synch) {
            this.closingTime = theClosingTime;

            if (this.offScreen != null) {
                drawOffScreen(this.offScreen);
                repaint();
            }
        }
    }

    /**
     * Refreshes the display if needed.
     */
    public void refresh() {

        synchronized (this.synch) {
            final LocalTime now = LocalTime.now();

            String closing = CoreConstants.EMPTY;
            if (this.closingTime != null) {
                final int nowMin = TemporalUtils.minuteOfDay(now);
                final int closeMin = TemporalUtils.minuteOfDay(this.closingTime);
                final int remain = closeMin - nowMin;

                if (remain < 0) {
                    final String timeStr = TemporalUtils.FMT_HM_A.format(this.closingTime);
                    closing = SimpleBuilder.concat(timeStr, " [CLOSED]");
                } else {
                    final String remainStr = Integer.toString(remain);
                    closing = SimpleBuilder.concat("Closing in ", remainStr, " minutes.");
                }
            }

            this.currentTimeStr = TemporalUtils.FMT_HM_A.format(now);
            this.closingTimeString = closing;

            if (this.offScreen != null) {
                drawOffScreen(this.offScreen);
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

        synchronized (this.synch) {
            if (this.offScreen == null) {
                final int width = getWidth();
                final int height = getHeight();
                this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                this.offScreen = (Graphics2D) this.backBuffer.getGraphics();

                // Configure permanent attributes of the drawing context.
                this.offScreen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                drawOffScreen(this.offScreen);
            }

            g.drawImage(this.backBuffer, 0, 0, this);
        }
    }

    /**
     * Updates the off-screen image.
     */
    private void drawOffScreen(final Graphics2D grx) {

        // Called only from within blocks synchronized on "this.synch"

        // NOTE: Does not run in the AWT thread, but only draws to a private Graphics pointing to a private
        // BufferedImage, so it should not impact the AWT tree.

        if (Objects.nonNull(grx)) {
            final Color background = getBackground();

            final Dimension size = getSize();
            final int width = size.width;
            final int height = size.height;

            grx.setColor(background);
            grx.fillRect(0, 0, width, height);

            int right = width - height / 3;

            if (Objects.nonNull(this.currentTimeStr)) {

                if (this.clockFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    final double clockSize = (double) height * 0.45;
                    final double closingSize = clockSize * 0.43;
                    this.clockFont = bfm.getFont(BundledFontManager.SERIF, clockSize, Font.BOLD);
                    this.closingFont = bfm.getFont(BundledFontManager.SERIF, closingSize, Font.BOLD);
                }

                grx.setFont(this.clockFont);
                final FontMetrics fm2 = grx.getFontMetrics();

                final int timeWidth = fm2.stringWidth(this.currentTimeStr);
                int timeY = (height + fm2.getAscent()) / 2;
                int closingWidth = 0;
                int closingY = 0;

                if (Objects.nonNull(this.closingTimeString)) {
                    grx.setFont(this.closingFont);
                    final FontMetrics fm3 = grx.getFontMetrics();
                    closingWidth = fm3.stringWidth(this.closingTimeString);
                    grx.setFont(this.clockFont);

                    final int closingHeight = fm3.getHeight();
                    timeY -= closingHeight / 2;
                    closingY = timeY + closingHeight;
                }

                final int timeX = right - Math.max(timeWidth, closingWidth);

                grx.setColor(this.shadow);
                grx.drawString(this.currentTimeStr, timeX + 1, timeY);
                grx.drawString(this.currentTimeStr, timeX - 1, timeY);
                grx.drawString(this.currentTimeStr, timeX, timeY + 1);
                grx.drawString(this.currentTimeStr, timeX, timeY - 1);

                grx.setColor(this.color);
                grx.drawString(this.currentTimeStr, timeX, timeY);

                if (Objects.nonNull(this.closingTimeString)) {
                    grx.setFont(this.closingFont);
                    grx.setColor(this.shadow);
                    grx.drawString(this.closingTimeString, timeX, closingY);
                }

                right = timeX - height / 2;
            }

            final String test = this.message == null ? CoreConstants.EMPTY : this.message;
            int pts;

            if (!test.isEmpty()) {

                final int left = height / 3;
                final int avail = right - left;

                final BundledFontManager bfm = BundledFontManager.getInstance();
                pts = height * 3 / 5;
                Font messageFont = null;
                while (pts >= 10) {
                    messageFont = bfm.getFont(BundledFontManager.SERIF, (double) pts, Font.BOLD);
                    final FontMetrics fm = grx.getFontMetrics(messageFont);
                    final int textWidth = fm.stringWidth(test);

                    if (textWidth <= avail) {
                        break;
                    }
                    pts -= 10;
                }
                grx.setFont(messageFont);

                final FontMetrics fm = grx.getFontMetrics();
                int yPix = ((fm.getAscent() * 3 / 4) + height) / 2;

                if ((yPix + fm.getDescent() + fm.getLeading()) > height) {
                    yPix = height - fm.getDescent() - fm.getLeading();
                }

                if (this.message != null) {
                    grx.setColor(this.shadow);
                    grx.drawString(this.message, left + 1, yPix);
                    grx.drawString(this.message, left - 1, yPix);
                    grx.drawString(this.message, left, yPix + 1);
                    grx.drawString(this.message, left, yPix - 1);

                    grx.setColor(this.color);
                    grx.drawString(this.message, left, yPix);
                }
            }
        }
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        synchronized (this.synch) {
            return SimpleBuilder.concat("BottomPanel{message='", this.message, "', clockFont=", this.clockFont,
                    ", closingFont=", this.closingFont, ", color=", this.color, ", shadow=", this.shadow,
                    ", backBuffer=", this.backBuffer, ", offScreen=", this.offScreen, ", closingTime=",
                    this.closingTime, ", currentTimeStr='", this.currentTimeStr, "', closingTimeString='",
                    this.closingTimeString, "'}");
        }
    }
}
