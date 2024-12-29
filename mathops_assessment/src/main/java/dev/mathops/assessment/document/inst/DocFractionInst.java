package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EFractionFormat;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An instance of a container object that presents two other instance objects as a fraction, with a horizontal line
 * drawn between them.
 */
public final class DocFractionInst extends AbstractDocObjectInst {

    /** The numerator. */
    private final DocNonwrappingSpanInst numerator;

    /** The denominator. */
    private final DocNonwrappingSpanInst denominator;

    /** The fraction format. */
    private final EFractionFormat fractionFormat;

    /**
     * Construct a new {@code DocFractionInst} object.
     *
     * @param theStyle          the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName    the background color name ({@code null} if transparent)
     * @param theNumerator      the object acting as a numerator
     * @param theDenominator    the object acting as a denominator
     * @param theFractionFormat the fraction format
     */
    public DocFractionInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                           final DocNonwrappingSpanInst theNumerator,
                           final DocNonwrappingSpanInst theDenominator,
                           final EFractionFormat theFractionFormat) {

        super(theStyle, theBgColorName);

        if (theNumerator == null) {
            throw new IllegalArgumentException("The numerator may not be null");
        }
        if (theDenominator == null) {
            throw new IllegalArgumentException("The denominator may not be null");
        }
        if (theFractionFormat == null) {
            throw new IllegalArgumentException("The fraction format may not be null");
        }

        this.numerator = theNumerator;
        this.denominator = theDenominator;
        this.fractionFormat = theFractionFormat;
    }

    /**
     * Gets the numerator.
     *
     * @return the numerator
     */
    public DocNonwrappingSpanInst getNumerator() {

        return this.numerator;
    }

    /**
     * Gets the denominator.
     *
     * @return the denominator
     */
    public DocNonwrappingSpanInst getDenominator() {

        return this.denominator;
    }

    /**
     * Gets the fraction format.
     *
     * @return the fraction format
     */
    public EFractionFormat getFractionFormat() {

        return this.fractionFormat;
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

        xml.add("<fraction");
        addDocObjectInstXmlAttributes(xml);
        xml.addAttribute("format", this.fractionFormat, 0);
        xml.add('>');

        this.numerator.toXml(xml, xmlStyle, indent + 1);
        this.denominator.toXml(xml, xmlStyle, indent + 1);

        xml.add("</fraction>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocFractionInst");
        appendStyleString(builder);
        builder.add("[format=", this.fractionFormat, "]");
        builder.add(':');

        final String numeratorStr = this.numerator.toString();
        builder.add("numerator=", numeratorStr);

        final String denominatorStr = this.denominator.toString();
        builder.add(",denominator=", denominatorStr);

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectInstHashCode() + this.numerator.hashCode() + this.denominator.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocFractionInst fraction) {
            final AbstractDocContainerInst objNumerator = fraction.getNumerator();
            final AbstractDocContainerInst objDenominator = fraction.getDenominator();
            equal = checkDocObjectInstEquals(fraction)
                    && this.numerator.equals(objNumerator)
                    && this.denominator.equals(objDenominator)
                    && this.fractionFormat == fraction.getFractionFormat();
        } else {
            equal = false;
        }

        return equal;
    }
}
