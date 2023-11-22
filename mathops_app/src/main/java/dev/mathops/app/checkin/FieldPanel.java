package dev.mathops.app.checkin;

import dev.mathops.font.BundledFontManager;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * A panel that sets itself to 1/6 the height and the full width of containing window, and displays a text label, an
 * optional entry field, and an optional Back button.
 */
public final class FieldPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -9129903999583655885L;

    /** The listener to notify when a field is entered. */
    private final ActionListener listener;

    /** The action command to send when a field is entered. */
    private final String command;

    /** The text message to display in the panel. */
    private String message;

    /** The font in which to draw the message. */
    private Font messageFont;

    /** The color in which to paint the message. */
    private Color color;

    /** The highlight color. */
    private Color highlight;

    /** The shadow color. */
    private Color shadow;

    /** The contents of the field. */
    private String fieldValue;

    /** The width of field to display (0 to hide field). */
    private int fieldWidth;

    /** Flag controlling whether the Back button is drawn. */
    private boolean showBack;

    /** The offscreen image buffer. */
    private BufferedImage backbuffer;

    /** The graphics context of the back buffer. */
    private Graphics2D offscreen;

    /**
     * Constructs a new {@code FieldPanel}.
     *
     * @param screen      the size of the screen or window this panel is contained in
     * @param theListener the listener to notify when a field is entered
     * @param theCommand  the action command to fire when a field is entered
     */
    public FieldPanel(final Dimension screen, final ActionListener theListener,
                      final String theCommand) {

        super();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        this.listener = theListener;
        this.command = theCommand;

        setDoubleBuffered(false);
        setBackground(new Color(210, 210, 235));
        setPreferredSize(new Dimension(screen.width, screen.height / 7));
        updateColor();
        setFocusable(true);
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
        r = (r << 1) / 3;
        g = (g << 1) / 3;
        b = (b << 1) / 3;
        this.color = new Color(r, g, b);

        r = bg.getRed();
        g = bg.getGreen();
        b = bg.getBlue();
        r = r + ((255 - r) / 2);
        g = g + ((255 - g) / 2);
        b = b + ((255 - b) / 2);
        this.highlight = new Color(r, g, b);

        r = this.color.getRed();
        g = this.color.getGreen();
        b = this.color.getBlue();
        r = (r << 1) / 3;
        g = (g << 1) / 3;
        b = (b << 1) / 3;
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

            if (this.offscreen != null) {
                drawOffscreen();
                repaint();
            }
        }
    }

    /**
     * Sets the field value and width.
     *
     * @param width the width to set the field to (0 to hide field)
     * @param value the new field value, or null for empty field
     */
    public void setFieldValue(final int width, final String value) {
        synchronized (this) {

            this.fieldWidth = width;
            this.fieldValue = value;
            this.messageFont = null;

            if (this.offscreen != null) {
                drawOffscreen();
                repaint();
            }
        }
    }

    /**
     * Gets the current value of the entry field.
     *
     * @return the field value
     */
    public String getFieldValue() {
        synchronized (this) {

            return this.fieldValue;
        }
    }

//    /**
//     * Sets the flag that controls whether the back button is drawn.
//     *
//     * @param shouldShowBack {@code true} to show the button; {@code false} otherwise
//     */
//    public void setShowBack(final boolean shouldShowBack) {
//        synchronized (this) {
//
//            this.showBack = shouldShowBack;
//
//            if (this.offscreen != null) {
//                drawOffscreen();
//            }
//        }
//    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {
        synchronized (this) {

            if (this.offscreen == null) {
                this.backbuffer =
                        new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
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
     * Updates the offscreen image.
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
            int width = size.width;

            // We determine the widths and horizontal positioning of each screen element. First, if
            // there is a Back button, it consumes the right end of the panel.
            if (this.showBack) {
                // Back button is square, so reduce width by height
                width -= height;
            }

            if (this.fieldWidth > 0 && this.message != null) {
                // Both message and field are showing, so allow for a gap between
                width -= height / 4;
            }

            // Generate a string that is the width of the message and a number of nines equal to the
            // width of the field.
            final StringBuilder builder = new StringBuilder(50);
            if (this.message != null) {
                builder.append(this.message);
            }
            builder.append("9".repeat(Math.max(0, this.fieldWidth + 1)));
            final String test = builder.toString();

            int pts;

            if (!test.isEmpty()) {

                if (this.messageFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 200) {
                        this.messageFont = bfm.getFont(BundledFontManager.SERIF, (double) pts, Font.BOLD);
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
                final int x1 = (int) ((double) width - fm.getStringBounds(test, grx).getWidth()) / 2;
                final int x2;

                if (this.message != null) {
                    x2 = x1 + (height / 4) + (int) (fm.getStringBounds(this.message, grx).getWidth());
                } else {
                    x2 = x1;
                }

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
                    //
                    // grx.setColor(this.highlight);

                    grx.setColor(this.color);
                    grx.drawString(this.message, x1, yPix);
                }

                if (this.fieldWidth > 0) {
                    final int fw = (int) (fm.getStringBounds(test, grx).getWidth()
                            - fm.getStringBounds(this.message, grx).getWidth());

                    // Draw the background and border
                    grx.setColor(this.highlight);
                    grx.fillRect(x2 - (height / 16), yPix - fm.getAscent(), fw + (height / 8), fm.getHeight());

                    grx.setColor(this.shadow);
                    grx.drawRect(x2 - (height / 16), yPix - fm.getAscent(), fw + (height / 8), fm.getHeight());

                    // Draw the current field value.
                    if (this.fieldValue != null) {
                        grx.setColor(Color.black);
                        grx.drawString(this.fieldValue, x2, yPix);
                    }
                }
            }
        }
    }

    /**
     * Adds a typed character to the field value.
     *
     * @param chr the typed character
     */
    public void addToFieldValue(final char chr) {

        synchronized (this) {

            if (this.fieldWidth > 0) {

                if (chr == 0x08) {
                    // Backspace
                    if ((this.fieldValue != null) && (!this.fieldValue.isEmpty())) {
                        this.fieldValue = this.fieldValue.substring(0, this.fieldValue.length() - 1);
                    }
                } else if (chr == '\n' || chr == '\r') {
                    // Enter, so fire an action.
                    if (this.listener != null) {
                        final ActionEvent evt = new ActionEvent(this, 0, this.command);
                        this.listener.actionPerformed(evt);
                    }
                } else if (Character.isDigit(chr)) {
                    if (this.fieldValue == null || this.fieldValue.isEmpty()) {
                        this.fieldValue = Character.toString(chr);
                    } else if (this.fieldValue.charAt(0) != 'E' && this.fieldValue.length() < this.fieldWidth) {
                        this.fieldValue += chr;
                    }
                } else if (this.fieldValue.startsWith("99")) {
                    // Allow letters after a '99'
                    this.fieldValue += Character.toUpperCase(chr);
                } else if (this.fieldValue.isEmpty() && (chr == 'e' || chr == 'E')) {
                    this.fieldValue = "E";
                } else if ("E".equals(this.fieldValue) && (chr == 'x' || chr == 'X')) {
                    this.fieldValue = "EX";
                } else if ("EX".equals(this.fieldValue) && (chr == 'i' || chr == 'I')) {
                    this.fieldValue = "EXI";
                } else if ("EXI".equals(this.fieldValue) && (chr == 't' || chr == 'T')) {
                    this.fieldValue = "EXIT";
                }

                if (this.offscreen != null) {
                    drawOffscreen();
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    paintComponent(getGraphics());
                } else {
                    repaint();
                }
            }
        }
    }
}
