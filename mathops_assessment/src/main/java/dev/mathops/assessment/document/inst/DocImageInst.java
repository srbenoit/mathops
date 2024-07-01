package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EPrimaryBaseline;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.xml.XmlEscaper;

import java.util.Objects;

/**
 * An instance of an image in a document.
 */
public final class DocImageInst extends AbstractDocObjectInst {

    /** The URL from which to load the image. */
    private final String source;

    /** The scaled width. */
    private final double width;

    /** The scaled height. */
    private final double height;

    /** The baseline used for alignment of this construction. */
    private final EPrimaryBaseline baseline;

    /** The alt text. */
    private final String altText;

    /**
     * Constructs a new {@code DocImageInst} object.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theSource      the source URL
     * @param theWidth       the image width
     * @param theHeight      the image height
     * @param theBaseline    the baseline to use to align the image (top of image to hanging baseline, bottom of image
     *                       to typographic baseline, or center line of image to center line or math-axis baseline)
     * @param theAltText     the alt text
     */
    public DocImageInst(final DocObjectInstStyle theStyle, final String theBgColorName, final String theSource,
                        final double theWidth, final double theHeight, final EPrimaryBaseline theBaseline,
                        final String theAltText) {

        super(theStyle, theBgColorName);

        if (theSource == null) {
            throw new IllegalArgumentException("Source URL may not be null");
        }
        if (theBaseline == null) {
            throw new IllegalArgumentException("Primary baseline may not be null");
        }

        this.source = theSource;
        this.width = theWidth;
        this.height = theHeight;
        this.baseline = theBaseline;
        this.altText = theAltText;
    }

    /**
     * Gets the source URL.
     *
     * @return the source URL (never {@code null})
     */
    public String getSource() {

        return this.source;
    }

    /**
     * Gets the image width.
     *
     * @return the width, in pixels
     */
    public double getWidth() {

        return this.width;
    }

    /**
     * Gets the image height.
     *
     * @return the height, in pixels
     */
    public double getHeight() {

        return this.height;
    }

    /**
     * Gets the baseline used to align the image in surrounding content.
     *
     * @return the baseline (top of image to hanging baseline, bottom of image to typographic baseline, or centerline of
     *         image to centerline or math-axis baseline)
     */
    public EPrimaryBaseline getBaseline() {

        return this.baseline;
    }

    /**
     * Gets the alt text.
     *
     * @return the alt text
     */
    public String getAltText() {

        return this.altText;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        xml.add("<image");
        addDocObjectInstXmlAttributes(xml);
        xml.addAttribute("src", this.source, 0);
        final String widthStr = Double.toString(this.width);
        xml.addAttribute("width", widthStr, 0);
        final String heightStr = Double.toString(this.height);
        xml.addAttribute("height", heightStr, 0);
        xml.addAttribute("baseline", this.baseline, 0);

        final String alt = getAltText();
        if (alt != null) {
            final String altStr = XmlEscaper.escape(alt);
            xml.add(" alt='", altStr, "'");
        }
        xml.add("/>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocImageInst");
        appendStyleString(builder);
        builder.add("{src=", this.source);
        final String widthStr = Double.toString(this.width);
        builder.add(",width=", widthStr);
        final String heightStr = Double.toString(this.height);
        builder.add(",height=", heightStr);
        builder.add(",baseline=", this.baseline);
        builder.add(",alt=", this.altText);
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

        return docObjectInstHashCode() + this.source.hashCode() + Double.hashCode(this.width)
                + Double.hashCode(this.height) + this.baseline.hashCode() + Objects.hashCode(this.altText);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        // NOTE: We don't do a "Math.abs(x - y) < epsilon" comparison since that could result in two object having
        // different hash codes, but still being considered equal, which violates the contract for hashCode.

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocImageInst image) {
            final String objSource = image.getSource();
            final String objAlt = image.getAltText();
            equal = checkDocObjectInstEquals(image)
                    && this.source.equals(objSource)
                    && this.width == image.getWidth()
                    && this.height == image.getHeight()
                    && this.baseline == image.getBaseline()
                    && Objects.equals(this.altText, objAlt);
        } else {
            equal = false;
        }

        return equal;
    }
}