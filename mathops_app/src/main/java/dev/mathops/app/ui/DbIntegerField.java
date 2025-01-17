package dev.mathops.app.ui;

import dev.mathops.commons.CoreConstants;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

/**
 * A component that allows the user to enter an integer ot to specify NULL or a query criteria for integer fields.
 *
 * <p>
 * This field can display integer data from an existing record (including displaying "NULL" when the value is null), can
 * allow the user to enter integer data for a new record, or can enter a criterion for a query.  Query criteria must be
 * one of the following:
 * <ul>
 *     <li>IS NULL (a special "character" rendered as a single glyph)</li>
 *     <li>IS NOT NULL (a special "character" rendered as a single glyph)</li>
 *     <li>15 (matches field values of 15 exactly)</li>
 *     <li>=15 (matches field values of 15 exactly)</li>
 *     <li>!=15 (matches any field value except 15)</li>
 *     <li>&lt;&gt;15 (matches any field value except 15)</li>
 *     <li>&lt;15 (matches any field value less than 15)</li>
 *     <li>&lt;=15 (matches any field value less than or equal to 15)</li>
 *     <li>&gt;15 (matches any field value greater than 15)</li>
 *     <li>&gt;=15 (matches any field value greater than or equal to 15)</li>
 *     <li>Any two of the greater than or less than comparisons</li>
 * </ul>
 */
public final class DbIntegerField extends AbstractDbField {

    /** Left and right side padding. */
    private static final int LEFT_RIGHT_PADDING = 5;

    /** Top and bottom side padding. */
    private static final int TOP_BOTTOM_PADDING = 3;

    /** The minimum allowed value. */
    private final int min;

    /** The maximum allowed value. */
    private final int max;

    /** A graphics object used to obtain font metrics and font render context objects. */
    private final Graphics2D g2d;

    /** The text label. */
    private JLabel text;

    /** A flag indicating the object has been initialized. */
    private boolean initialized = false;

    /**
     * Constructs a new {@code DBIntegerField} in the "DISPLAY_EXISTING_DATA" mode.
     *
     * @param theNullability the nullability of the field
     * @param theMin         the minimum allowed value
     * @param theMax         the maximum allowed value
     */
    public DbIntegerField(final ENullability theNullability, final int theMin, final int theMax) {

        super(theNullability);

        this.min = theMin;
        this.max = theMax;

        final BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.g2d = img.createGraphics();

        this.text = new JLabel("Foo");
        add(this.text);

        final JTextField dummyField = new JTextField();
        setBorder(dummyField.getBorder());
        setBackground(dummyField.getBackground());
        setForeground(dummyField.getForeground());

        updatePreferredSize();

        setFocusable(true);
        this.initialized = true;
    }

    /**
     * Sets the font.  This updates the preferred size.
     *
     * @param theFont the desired {@code Font} for this component
     */
    public void setFont(final Font theFont) {

        super.setFont(theFont);

        if (this.initialized) {
            updatePreferredSize();
        }
    }

    /**
     * Updates the preferred size based on the current font.
     */
    private void updatePreferredSize() {

        final Insets insets = getInsets();

        final Font font = getFont();
        final FontMetrics metrics = this.g2d.getFontMetrics(font);
        final int ascent = metrics.getAscent();
        final int descent = metrics.getDescent();
        final int textHeight = ascent + descent;

        final String minStr = Integer.toString(this.min);
        final String maxStr = Integer.toString(this.max);
        final int minWidth = metrics.stringWidth(minStr);
        final int maxWidth = metrics.stringWidth(maxStr);

        final int textWidth = Math.max(minWidth, maxWidth);

        final int width = textWidth + insets.left + insets.right + 2 * LEFT_RIGHT_PADDING;
        final int height = textHeight + insets.top + insets.bottom + 2 * TOP_BOTTOM_PADDING;

        final Dimension dim = new Dimension(width, height);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setSize(dim);

        this.text.setSize(new Dimension(textWidth, textHeight));
        this.text.setLocation(insets.left + LEFT_RIGHT_PADDING, insets.top + TOP_BOTTOM_PADDING);

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Sets the field mode.
     *
     * @param newMode the new mode
     */
    public void setMode(final EDbFieldMode newMode) {

        innerSetMode(newMode);
    }

    /**
     * Clears the field.
     */
    @Override
    public void clear() {

        this.text.setText(CoreConstants.EMPTY);
        repaint();
    }

    /**
     * Sets the field to display a "NULL" value (used in modes that display data; for modes that accept query criteria,
     * for example to set an "IS NULL" or "IS NOT NULL" query condition, use {@see setToIsNull} or
     * {@see setToIsNotNull}).
     */
    public void setToNull() {

    }

    /**
     * Sets the field to "IS NULL" as a query condition.
     */
    public void setToIsNull() {

    }

    /**
     * Sets the field to "IS NOT NULL" as a query condition.
     */
    public void setToIsNotNull() {

    }

//    /**
//     * Paints the component.
//     *
//     * @param g the {@code Graphics} to which to draw
//     */
//    public void paintComponent(final Graphics g) {
//
//        super.paintComponent(g);
//    }
}
