package dev.mathops.app.checkin;

import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.SimpleBuilder;

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
import java.util.Objects;

/**
 * A panel that sets itself to 1/6 the height and the full width of containing window, and displays a text label, an
 * optional entry field, and an optional Back button.
 */
public final class FieldPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -9129903999583655885L;

    /** Object on which to synchronize changes. */
    private final Object synch;

    /** The listener to notify when a field is entered. */
    private final ActionListener listener;

    /** The action command to send when a field is entered. */
    private final String command;

    /** The text message to display in the panel. */
    private String message = null;

    /** The font in which to draw the message. */
    private Font messageFont = null;

    /** The color in which to paint the message. */
    private final Color color;

    /** The highlight color. */
    private final Color highlight;

    /** The shadow color. */
    private final Color shadow;

    /** The contents of the field. */
    private String fieldValue = null;

    /** The width of field to display (0 to hide field). */
    private int fieldWidth = 0;

    /** The off-screen image buffer. */
    private BufferedImage backBuffer = null;

    /** The graphics context of the back buffer. */
    private Graphics2D offScreen = null;

    /**
     * Constructs a new {@code FieldPanel}.
     *
     * @param screen      the size of the screen or window this panel is contained in
     * @param theListener the listener to notify when a field is entered
     * @param theCommand  the action command to fire when a field is entered
     */
    public FieldPanel(final Dimension screen, final ActionListener theListener, final String theCommand) {

        super();

        this.synch = new Object();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        this.listener = theListener;
        this.command = theCommand;

        setDoubleBuffered(false);
        setBackground(new Color(210, 210, 235));
        setPreferredSize(new Dimension(screen.width, screen.height / 7));

        final Color bg = getBackground();
        this.color = bg.darker();
        this.shadow = this.color.darker();
        this.highlight = bg.brighter();

        setFocusable(true);
    }

    /**
     * Sets the message text to display in the panel.
     *
     * @param theMessage the new message text
     */
    public void setMessage(final String theMessage) {

        synchronized (this.synch) {
            this.message = theMessage;
            this.messageFont = null;

            if (this.offScreen != null) {
                drawOffScreen(this.offScreen);
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

        synchronized (this.synch) {
            this.fieldWidth = width;
            this.fieldValue = value;
            this.messageFont = null;

            drawOffScreen(this.offScreen);
            repaint();
        }
    }

    /**
     * Gets the current value of the entry field.
     *
     * @return the field value
     */
    public String getFieldValue() {

        synchronized (this.synch) {
            return this.fieldValue;
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
                this.backBuffer =new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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

        // Called only from within contexts synchronized on this.synch

        // NOTE: Does not run in the AWT thread, but only draws to a private Graphics pointing to a
        // private BufferedImage, so it should not impact the AWT tree.

        if (Objects.nonNull(grx)) {

            final Dimension size = getSize();
            final int height = size.height;
            int width = size.width;

            final Color bgColor = getBackground();
            grx.setColor(bgColor);
            grx.fillRect(0, 0, width, height);

            if (this.fieldWidth > 0 && Objects.nonNull(this.message)) {
                // Both message and field are showing, so allow for a gap between
                width -= height / 4;
            }

            // Generate a string that is the width of the message and a number of nines equal to the
            // width of the field.
            final StringBuilder builder = new StringBuilder(50);
            if (Objects.nonNull(this.message)) {
                builder.append(this.message);
            }
            final int numDigits = Math.max(0, this.fieldWidth + 1);
            final String nines = "9".repeat(numDigits);
            builder.append(nines);
            final String test = builder.toString();

            int pts;

            if (!test.isEmpty()) {

                if (this.messageFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 200) {
                        this.messageFont = bfm.getFont(BundledFontManager.SERIF, pts, Font.BOLD);
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

                if (Objects.nonNull(this.message)) {
                    x2 = x1 + (height / 4) + (int) (fm.getStringBounds(this.message, grx).getWidth());
                } else {
                    x2 = x1;
                }

                final int ascent = fm.getAscent();
                int yPix = ((ascent * 3 / 4) + height) / 2;

                if ((yPix + fm.getDescent() + fm.getLeading()) > height) {
                    yPix = height - fm.getDescent() - fm.getLeading();
                }

                if (Objects.nonNull(this.message)) {
                    grx.setColor(this.shadow);
                    grx.drawString(this.message, x1 + 1, yPix);
                    grx.drawString(this.message, x1 - 1, yPix);
                    grx.drawString(this.message, x1, yPix + 1);
                    grx.drawString(this.message, x1, yPix - 1);

                    // grx.setColor(this.highlight);

                    grx.setColor(this.color);
                    grx.drawString(this.message, x1, yPix);
                }

                if (this.fieldWidth > 0) {
                    final int fw = (int) (fm.getStringBounds(test, grx).getWidth()
                            - fm.getStringBounds(this.message, grx).getWidth());

                    // Draw the background and border
                    grx.setColor(this.highlight);
                    final int fontHeight = fm.getHeight();
                    grx.fillRect(x2 - (height / 16), yPix - ascent, fw + (height / 8), fontHeight);

                    grx.setColor(this.shadow);
                    grx.drawRect(x2 - (height / 16), yPix - ascent, fw + (height / 8), fontHeight);

                    // Draw the current field value.
                    if (Objects.nonNull(this.fieldValue)) {
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

        synchronized (this.synch) {
            if (this.fieldWidth > 0) {
                if ((int)chr == 0x08) {
                    // Backspace
                    if (Objects.nonNull(this.fieldValue) && !this.fieldValue.isEmpty()) {
                        final int fieldLen = this.fieldValue.length();
                        this.fieldValue = this.fieldValue.substring(0, fieldLen - 1);
                    }
                } else if ((int)chr == '\n' || (int)chr == '\r') {
                    // Enter, so fire an action.
                    if (this.listener != null) {
                        final ActionEvent evt = new ActionEvent(this, 0, this.command);
                        this.listener.actionPerformed(evt);
                    }
                } else if (Character.isDigit(chr)) {
                    if (this.fieldValue == null || this.fieldValue.isEmpty()) {
                        this.fieldValue = Character.toString(chr);
                    } else if ((int) this.fieldValue.charAt(0) != 'E' && this.fieldValue.length() < this.fieldWidth) {
                        this.fieldValue += chr;
                    }
                } else if (this.fieldValue.startsWith("99")) {
                    // Allow letters after a '99'
                    this.fieldValue += Character.toUpperCase(chr);
                } else if (this.fieldValue.isEmpty() && ((int)chr == 'e' || (int)chr == 'E')) {
                    this.fieldValue = "E";
                } else if ("E".equals(this.fieldValue) && ((int)chr == 'x' || (int)chr == 'X')) {
                    this.fieldValue = "EX";
                } else if ("EX".equals(this.fieldValue) && ((int)chr == 'i' || (int)chr == 'I')) {
                    this.fieldValue = "EXI";
                } else if ("EXI".equals(this.fieldValue) && ((int)chr == 't' || (int)chr == 'T')) {
                    this.fieldValue = "EXIT";
                }

                drawOffScreen(this.offScreen);

                if (SwingUtilities.isEventDispatchThread()) {
                    final Graphics graphics = getGraphics();
                    paintComponent(graphics);
                } else {
                    repaint();
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

        final String fieldWidthStr = Integer.toString(this.fieldWidth);

        return SimpleBuilder.concat("FieldPanel{listener=", this.listener, ", command='", this.command, "', message='",
                this.message, "', messageFont=", this.messageFont, ", color=", this.color, ", highlight=",
                this.highlight, ", shadow=", this.shadow, ", fieldValue='", this.fieldValue, "', fieldWidth=",
                fieldWidthStr, ", backBuffer=", this.backBuffer, ", offScreen=", this.offScreen, "}");
    }
}
