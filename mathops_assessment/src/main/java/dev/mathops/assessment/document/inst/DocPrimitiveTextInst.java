package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A text primitive.
 */
public final class DocPrimitiveTextInst extends AbstractPrimitiveInst {

    /** The x coordinate. */
    private final double x;

    /** The y coordinate. */
    private final double y;

    /** The text anchor point. */
    private final ETextAnchor anchor;

    /** The text. */
    private final String text;

    /** The font style and color. */
    private final DocObjectInstStyle style;

    /** The alpha. */
    private final double alpha;

    /**
     * Construct a new {@code DocPrimitiveTextInst}.
     *
     * @param theX      the x coordinate
     * @param theY      the y coordinate
     * @param theAnchor the text anchor point (null treated as SW)
     * @param theText   the text to draw
     * @param theStyle  the style
     * @param theAlpha  the alpha
     */
    public DocPrimitiveTextInst(final double theX, final double theY, final String theText, final ETextAnchor theAnchor,
                                final DocObjectInstStyle theStyle, final double theAlpha) {

        super();

        if (theText == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        if (theStyle == null) {
            throw new IllegalArgumentException("Style may not be null");
        }

        this.x = theX;
        this.y = theY;
        this.anchor = theAnchor;
        this.text = theText;
        this.style = theStyle;
        this.alpha = theAlpha;
    }

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {

        return this.x;
    }

    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {

        return this.y;
    }

    /**
     * Gets the text anchor point.
     *
     * @return the text anchor point
     */
    public ETextAnchor getAnchor() {

        return this.anchor;
    }

    /**
     * Gets the text to be drawn.
     *
     * @return the text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Gets the background color name.
     *
     * @return the background color name; {@code null} for transparent
     */
    public DocObjectInstStyle getStyle() {

        return this.style;
    }

    /**
     * Gets the alpha.
     *
     * @return the alpha
     */
    public double getAlpha() {

        return this.alpha;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent the printout
     */
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }

        xml.add("<text");
        xml.addAttribute("x", Double.toString(this.x), 0);
        xml.addAttribute("y", Double.toString(this.y), 0);
        if (this.anchor != null) {
            xml.addAttribute("anchor", this.anchor.name(), 0);
        }
        xml.addAttribute("text", this.text, 0);
        this.style.appendXmlAttributes(xml);
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            xml.addAttribute("alpha", Double.toString(this.alpha), 0);
        }

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln("/>");
        } else {
            xml.add("/>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(200);

        builder.add("DocPrimitiveSpanInst{x=", Double.toString(this.x), ",y=", Double.toString(this.y), ",",
                this.style.toString());
        if (this.anchor != null) {
            builder.add(",anchor=", this.anchor.name());
        }
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            builder.add(",alpha=", Double.toString(this.alpha));
        }
        builder.add(",text=", this.text, "}");

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Double.hashCode(this.x) + Double.hashCode(this.y) + Objects.hashCode(this.anchor) + this.text.hashCode()
                + this.style.hashCode() + Double.hashCode(this.alpha);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocPrimitiveTextInst txt) {
            equal = this.x == txt.x
                    && this.y == txt.y
                    && this.anchor == txt.anchor
                    && this.text.equals(txt.text)
                    && this.style.equals(txt.style)
                    && this.alpha == txt.alpha;
        } else {
            equal = false;
        }

        return equal;
    }
}
