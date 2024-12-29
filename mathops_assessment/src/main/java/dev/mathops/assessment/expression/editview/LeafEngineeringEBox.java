package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.ExprLeafEngineeringE;
import dev.mathops.text.builder.SimpleBuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * A laid out {@code ExprLeafEngineeringE}.
 */
public final class LeafEngineeringEBox extends AbstractObjectBox {

    /** String to display. */
    private static final String STRING = "E";

    /** The source object. */
    private final ExprLeafEngineeringE source;

    /** The font. */
    private final Font font;

    /** The color. */
    private final Color color;

    /**
     * Constructs a new {@code LeafEngineeringEBox}.
     *
     * @param theSource the source object
     * @param currentFontSize the current font size
     */
    LeafEngineeringEBox(final ExprLeafEngineeringE theSource, final float currentFontSize) {

        super();

        this.source = theSource;

        this.font = Fonts.sans.deriveFont(Font.BOLD, currentFontSize * 0.65f);
        this.color = Color.BLACK;
        setAxisCenter(this.font);

        final FontMetrics metrics = Fonts.g2d.getFontMetrics(this.font);
        final int w = metrics.stringWidth(STRING);
        setWidth(w);

        final GlyphVector gvector = this.font.createGlyphVector(Fonts.frc, STRING);
        final Rectangle2D bounds = gvector.getVisualBounds();

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

        g2d.setColor(this.color);
        g2d.setFont(this.font);
        g2d.drawString(STRING, this.x, this.y);
    }

    /**
     * Generates the diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("LeafEngineeringEBox{source=", this.source, ", font=", this.font, ", color=",
                this.color, "}");
    }
}
