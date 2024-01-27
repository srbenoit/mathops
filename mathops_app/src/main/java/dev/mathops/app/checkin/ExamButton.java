package dev.mathops.app.checkin;

import dev.mathops.commons.builder.SimpleBuilder;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;

/**
 * A subclass of {@code JButton} that supports a multi-line, centered display consisting of a primary button label (the
 * name of the exam), and a subtext message giving the reason the exam is unavailable, or indicating that the exam is
 * available conditionally. The labels are drawn with text antialiasing.
 */
final class ExamButton extends JButton {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -244686957864152149L;

    /** The exam title. */
    private String title = null;

    /** The sub-text message string. */
    private String message = null;

    /** The sub-text font. */
    private Font subFont = null;

    /** The background color if the button is active. */
    private final Color activeBackground;

    /**
     * Constructs a new {@code ExamButton}.
     *
     * @param theActiveBackground the active background
     */
    ExamButton(final Color theActiveBackground) {

        super();

        this.activeBackground = theActiveBackground;
    }

    /**
     * Sets the sub-text font.
     *
     * @param font the font for sub-text messages
     */
    void setSubFont(final Font font) {

        this.subFont = font;
    }

    /**
     * Sets the exam title.
     *
     * @param theTitle the exam title
     */
    public void setTitle(final String theTitle) {

        this.title = theTitle;
    }

    /**
     * Sets the message.
     *
     * @param theMessage the sub-text message string
     */
    public void setMessage(final String theMessage) {

        this.message = theMessage;
    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        final Graphics2D g2d = (Graphics2D) g;
        final Color origBg = getBackground();

        setBackground(this.activeBackground);

        super.paintComponent(g);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final Font font = getFont();
        g2d.setFont(font);

        FontMetrics fm = g.getFontMetrics();
        final int titleWidth = fm.stringWidth(this.title);

        final int width = getWidth();
        final int height = getHeight();
        final int titleX = (width - titleWidth) / 2;
        final int titleY = (height / 2) + (fm.getAscent() / 3);

        g2d.drawString(this.title, titleX, titleY);

        if (this.message != null) {
            if (this.subFont != null) {
                g.setFont(this.subFont);
                fm = g2d.getFontMetrics();
            }

            final int messageWidth = fm.stringWidth(this.message);
            final int messageX = (width - messageWidth) / 2;
            final int messageY = titleY + fm.getHeight() * 6 / 5;
            g.drawString(this.message, messageX, messageY);
        }

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

        setBackground(origBg);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("ExamButton{title='", this.title, "', message='", this.message,
                "', subFont=", this.subFont, ", activeBackground=", this.activeBackground, "}");
    }
}
