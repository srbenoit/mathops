package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A formula primitive (rendered as a graph of the function).
 */
public final class DocPrimitiveFormulaInst extends AbstractPrimitiveInst {

    /** The formula to graph (may include ONLY the domain variable). */
    private final Formula formula;

    /** The name of the domain variable that will be used for graphing. */
    private final String domainVarName;

    /** The stroke style. */
    private final StrokeStyleInst strokeStyle;

    /** The minimum domain variable value from which to draw the function. */
    private final double domainMin;

    /** The maximum domain variable value to which to draw the function. */
    private final double domainMax;

    /**
     * Construct a new {@code DocPrimitiveFormula}.
     *
     * @param theFormula       the formula to graph
     * @param theDomainVarName the name of the domain variable in the formula
     * @param theStrokeStyle   the stroke style
     * @param theDomainMin     the minimum domain variable value from which to draw the function
     * @param theDomainMax     the maximum domain variable value from which to draw the function
     */
    public DocPrimitiveFormulaInst(final Formula theFormula, final String theDomainVarName,
                                   final StrokeStyleInst theStrokeStyle, final double theDomainMin,
                                   final double theDomainMax) {

        super();

        if (theFormula == null) {
            throw new IllegalArgumentException("Formula may not be null");
        }
        if (theDomainVarName == null) {
            throw new IllegalArgumentException("Domain variable name may not be null");
        }
        if (theStrokeStyle == null) {
            throw new IllegalArgumentException("Stroke style may not be null");
        }

        this.formula = theFormula;
        this.domainVarName = theDomainVarName;
        this.strokeStyle = theStrokeStyle;
        this.domainMin = theDomainMin;
        this.domainMax = theDomainMax;
    }

    /**
     * Gets the formula to be plotted.
     *
     * @return the formula
     */
    public Formula getFormula() {

        return this.formula;
    }

    /**
     * Gets the domain variable name.
     *
     * @return the domain variable name
     */
    public String getDomainVarName() {

        return this.domainVarName;
    }

    /**
     * Gets the stroke style.
     *
     * @return the stroke style
     */
    public StrokeStyleInst getStrokeStyle() {

        return this.strokeStyle;
    }

    /**
     * Gets the minimum domain variable value from which to draw the function.
     *
     * @return the minimum domain value
     */
    public double getDomainMin() {

        return this.domainMin;
    }

    /**
     * Gets the maximum domain variable value from which to draw the function.
     *
     * @return the maximum domain value
     */
    public double getDomainMax() {

        return this.domainMax;
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

        xml.add("<formula");
        if (!"x".equals(this.domainVarName)) {
            xml.addAttribute("domain-var", this.domainVarName, 0);
        }
        this.strokeStyle.appendXmlAttributes(xml, CoreConstants.EMPTY);
        xml.addAttribute("domain-min", Double.toString(this.domainMin), 0);
        xml.addAttribute("domain-max", Double.toString(this.domainMax), 0);

        if (xmlStyle == EXmlStyle.INDENTED) {
            final String ind2 = makeIndent(indent + 1);
            xml.addln(">");
            xml.add(ind2);
        } else {
            xml.add(">");
        }

        if (this.formula != null) {
            xml.add("<expr>");
            this.formula.appendChildrenXml(xml);
            xml.add("</expr>");
        }

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
            xml.add(ind,"</formula>");
        } else {
            xml.add("</formula>");
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

        builder.add("DocPrimitiveFormulaInst{formula=", this.formula.toString(), ",domainVar=", this.domainVarName,
                ",", this.strokeStyle, ",min=", Double.toString(this.domainMin), ",max=",
                Double.toString(this.domainMax));

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.formula.hashCode() + this.domainVarName.hashCode() + this.strokeStyle.hashCode()
                + Double.hashCode(this.domainMin) + Double.hashCode(this.domainMax);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        // NOTE: We don't do a "Math.abs(x - y) < epsilon" comparison since that could result in two objects having
        // different hash codes, but still being considered equal, which violates the contract for hashCode.

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocPrimitiveFormulaInst form) {
            equal = this.formula.equals(form.formula)
                    && this.domainVarName.equals(form.domainVarName)
                    && this.strokeStyle.equals(form.strokeStyle)
                    && this.domainMin == form.domainMin
                    && this.domainMax == form.domainMax;
        } else {
            equal = false;
        }

        return equal;
    }
}
