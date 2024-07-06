package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A span primitive.
 */
public final class DocPrimitiveSpanInst extends AbstractPrimitiveInst {

    /** The x coordinate. */
    private final double x;

    /** The y coordinate. */
    private final double y;

    /** The text anchor point. */
    private final ETextAnchor anchor;

    /** The span. */
    private final DocNonwrappingSpanInst span;

    /** The background color name; {@code null} for transparent. */
    private final String bgColorName;

    /** The alpha. */
    private final double alpha;

    /**
     * Construct a new {@code DocPrimitiveSpanInst}.
     *
     * @param theX the x coordinate
     * @param theY the y coordinate
     * @param theSpan the span to draw
     * @param theAnchor the text anchor point (null treated as SW)
     * @param theBgColorName the background color name
     * @param theAlpha the alpha
     */
    public DocPrimitiveSpanInst(final double theX, final double theY, final DocNonwrappingSpanInst theSpan,
                                final ETextAnchor theAnchor, final String theBgColorName, final double theAlpha) {

        super();

        if (theSpan == null) {
            throw new IllegalArgumentException("Span may not be null");
        }

        this.x = theX;
        this.y = theY;
        this.anchor = theAnchor;
        this.span = theSpan;
        this.bgColorName = theBgColorName;
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
     * Gets the span to be drawn.
     *
     * @return the span
     */
    public DocNonwrappingSpanInst getSpan() {

        return this.span;
    }

    /**
     * Gets the background color name.
     *
     * @return the background color name; {@code null} for transparent
     */
    public String getBgColorName() {

        return this.bgColorName;
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
        xml.add("<span");
        xml.addAttribute("x", Double.toString(this.x), 0);
        xml.addAttribute("y", Double.toString(this.y), 0);
        if (this.anchor != null) {
            xml.addAttribute("anchor", this.anchor.name(), 0);
        }
        xml.addAttribute("bgcolor", this.bgColorName, 0);
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            xml.addAttribute("alpha", Double.toString(this.alpha), 0);
        }

        if (xmlStyle == EXmlStyle.INDENTED) {
            final String ind2 = makeIndent(indent + 1);
            xml.addln(">");
            xml.add(ind2);
        } else {
            xml.add(">");
        }

        this.span.appendChildrenXml(xml, xmlStyle, indent + 1);

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
            xml.add(ind,"</span>");
        } else {
            xml.add("</span>");
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

        builder.add("DocPrimitiveSpanInst{x=", Double.toString(this.x), ",y=", Double.toString(this.y));
        if (this.anchor != null) {
            builder.add(",anchor=", this.anchor.name());
        }
        if (this.bgColorName != null) {
            builder.add(",bgColor=", this.bgColorName);
        }
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            builder.add(",alpha=", Double.toString(this.alpha));
        }
        builder.add(',');
        builder.add(this.span);
        builder.add('}');

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Double.hashCode(this.x) + Double.hashCode(this.y) + Objects.hashCode(this.anchor)
                + Objects.hashCode(this.bgColorName) + this.span.hashCode() + Double.hashCode(this.alpha);
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
        } else if (obj instanceof final DocPrimitiveSpanInst primitiveSpan) {
            equal = this.x == primitiveSpan.x
                    && this.y == primitiveSpan.y
                    && Objects.equals(this.anchor, primitiveSpan.anchor)
                    && Objects.equals(this.bgColorName, primitiveSpan.bgColorName)
                    && this.span.equals(primitiveSpan.span)
                    && this.alpha == primitiveSpan.alpha;
        } else {
            equal = false;
        }

        return equal;
    }
}
