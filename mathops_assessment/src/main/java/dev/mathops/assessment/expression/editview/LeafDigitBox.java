package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.ExprLeafDigit;
import dev.mathops.commons.builder.SimpleBuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * A laid out {@code ExprLeafDigit}.
 */
public final class LeafDigitBox extends AbstractObjectBox {

    /** The source object. */
    private final ExprLeafDigit source;

    /** The font. */
    private final Font font;

    /** The color. */
    private final Color color;

    /**
     * Constructs a new {@code LeafDigitBox}.
     *
     * @param theSource the source object
     * @param currentFontSize the current font size
     */
    LeafDigitBox(final ExprLeafDigit theSource, final float currentFontSize) {

        super();

        this.source = theSource;

        this.font = Fonts.math.deriveFont(currentFontSize);
        this.color = Color.BLACK;
        setAxisCenter(this.font);

        final int digit = this.source.getDigit();
        final String str = Integer.toString(digit);

        final FontMetrics metrics = Fonts.g2d.getFontMetrics(this.font);
        final int w = metrics.stringWidth(str);
        setWidth(w);

        final GlyphVector gvector = this.font.createGlyphVector(Fonts.frc, str);
        final Rectangle2D bounds = gvector.getVisualBounds();

        // Note: positive Y is down, so max is descent, min is ascent
        final double maxY = bounds.getMaxY();
        final double minY = bounds.getMinY();

        final int theBottom = (int)(-Math.ceil(maxY));
        final int theTop = (int)(-Math.floor(minY));
        setTopBottom(theTop, theBottom);
    }

    /**
     * Paints the contents of the box
     *
     * @param g2d the {@code Graphics2D} to which to draw
     */
    @Override
    public void paint(final Graphics2D g2d) {

        final int digit = this.source.getDigit();
        final String str = Integer.toString(digit);

        g2d.setColor(this.color);
        g2d.setFont(this.font);
        g2d.drawString(str, this.x, this.y);
    }

    /**
     * Generates the diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("LeafDigitBox{source=", this.source, ", font=", this.font, ", color=",
                this.color, "}");
    }
}
