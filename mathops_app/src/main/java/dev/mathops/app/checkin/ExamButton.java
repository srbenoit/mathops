package dev.mathops.app.checkin;

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
class ExamButton extends JButton {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -244686957864152149L;

    /** The exam title. */
    private String title;

    /** The sub-text message string. */
    private String message;

    /** The sub-text font. */
    private Font subFont;

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
    public void setSubFont(final Font font) {

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

        final Color bg = getBackground();

        // if (isEnabled()) {
        setBackground(this.activeBackground);
        // }

        // Let the superclass paint the button border and background.
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Now, paint the labels
        g2d.setFont(getFont());

        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(this.title);
        final int width = getWidth();
        final int height = getHeight();

        int x = (width - w) / 2;
        int y = (height / 2) + (fm.getAscent() / 3);
        g2d.drawString(this.title, x, y);

        if (this.message != null) {
            if (this.subFont != null) {
                g.setFont(this.subFont);
                fm = g2d.getFontMetrics();
            }

            w = fm.stringWidth(this.message);
            x = (width - w) / 2;
            y += fm.getHeight() * 6 / 5;
            g.drawString(this.message, x, y);
        }

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

        setBackground(bg);
    }
}
