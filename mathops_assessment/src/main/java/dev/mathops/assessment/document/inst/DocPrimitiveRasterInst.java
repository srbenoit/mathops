package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A raster image.
 */
public final class DocPrimitiveRasterInst extends AbstractPrimitiveInst {

    /** The bounding rectangle in which to place the image. */
    private final BoundingRect bounds;

    /** The URL from which to load the image. */
    private final String source;

    /** The alpha. */
    private final double alpha;

    /**
     * Construct a new {@code DocPrimitiveRaster}.
     *
     * @param theBounds the bounding rectangle in which to place the image
     * @param theSource the URL from which to load the image
     * @param theAlpha  the alpha
     */
    public DocPrimitiveRasterInst(final BoundingRect theBounds, final String theSource, final double theAlpha) {

        super();

        if (theBounds == null) {
            throw new IllegalArgumentException("Bounding rectangle may not be null");
        }
        if (theSource == null) {
            throw new IllegalArgumentException("Source URL may not be null");
        }

        this.bounds = theBounds;
        this.source = theSource;
        this.alpha = theAlpha;
    }

    /**
     * Gets the bounding rectangle in which to place the image.
     *
     * @return the bounding rectangle
     */
    public BoundingRect getBounds() {

        return this.bounds;
    }

    /**
     * Gets the URL from which to load the image.
     *
     * @return the source URL
     */
    public String getSource() {

        return this.source;
    }

    /**
     * Gets the alpha to use when drawing the image.
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
        xml.add("<raster");
        this.bounds.appendXmlAttributes(xml);
        xml.addAttribute("source", this.source, 0);
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

        builder.add("DocPrimitiveRasterInst{", this.bounds.toString(), ",source=", this.source);
        if (Math.abs(this.alpha - 1.0) > 0.01) {
            builder.add(",alpha=", Double.toString(this.alpha));
        }
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

        return this.bounds.hashCode() + this.source.hashCode() + Double.hashCode(this.alpha);
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
        } else if (obj instanceof final DocPrimitiveRasterInst raster) {
            equal = this.bounds.equals(raster.bounds)
                    && this.source.equals(raster.source)
                    && this.alpha == raster.alpha;
        } else {
            equal = false;
        }

        return equal;
    }
}
