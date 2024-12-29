package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A random integer-valued variable whose value is a "simple" angle measure in degrees, where "simple" means the value
 * is an integer number of degrees whose radian equivalent is a fraction multiplying PI whose denominator is no greater
 * than some given upper bound.
 */
public final class VariableRandomSimpleAngle extends AbstractVariable
        implements IRangedVariable, IExcludableVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "random-simple-angle";

    /** A zero-length array used to allocate other arrays. */
    private static final Formula[] ZERO_LEN_FORMULA_ARR = new Formula[0];

    /** The minimum random integer/real value. */
    private NumberOrFormula min;

    /** The maximum random integer/real value. */
    private NumberOrFormula max;

    /** The maximum denominator in the radian representation. */
    private NumberOrFormula maxDenom;

    /** A list of excluded random integer values, which may be formulae. */
    private Formula[] exclude;

    /**
     * Constructs a new {@code VariableRandomSimpleAngle}.
     *
     * @param theName the variable name
     */
    public VariableRandomSimpleAngle(final String theName) {

        super(theName, EType.INTEGER);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.RANDOM_SIMPLE_ANGLE;
    }

    /**
     * Tests whether this is an input variable.
     *
     * @return true if an input variable (false for objects of this class)
     */
    @Override
    public boolean isInput() {

        return false;
    }

    /**
     * Creates a copy of the variable. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    @Override
    VariableRandomSimpleAngle deepCopy() {

        final VariableRandomSimpleAngle copy = new VariableRandomSimpleAngle(this.name);

        populateCopy(copy);

        if (this.min != null) {
            copy.min = this.min.deepCopy();
        }
        if (this.max != null) {
            copy.max = this.max.deepCopy();
        }
        if (this.maxDenom != null) {
            copy.maxDenom = this.maxDenom.deepCopy();
        }

        if (this.exclude != null) {
            final int excludeLen = this.exclude.length;
            copy.exclude = new Formula[excludeLen];

            for (int i = 0; i < excludeLen; ++i) {
                copy.exclude[i] = this.exclude[i].deepCopy();
            }
        }

        return copy;
    }

    /**
     * Sets the minimum value.
     *
     * @param theMin the minimum value
     */
    @Override
    public void setMin(final NumberOrFormula theMin) {

        this.min = theMin;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    @Override
    public NumberOrFormula getMin() {

        return this.min;
    }

    /**
     * Sets the maximum value.
     *
     * @param theMax the maximum value
     */
    @Override
    public void setMax(final NumberOrFormula theMax) {

        this.max = theMax;
    }

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    @Override
    public NumberOrFormula getMax() {

        return this.max;
    }

    /**
     * Sets the maximum denominator.
     *
     * @param theMaxDenom the maximum denominator
     */
    void setMaxDenom(final NumberOrFormula theMaxDenom) {

        this.maxDenom = theMaxDenom;
    }

    /**
     * Gets the maximum denominator.
     *
     * @return the maximum denominator
     */
    public NumberOrFormula getMaxDenom() {

        return this.maxDenom;
    }

    /**
     * Sets the list of excluded value formulae.
     *
     * @param theExcludes the list of excluded value formulae
     */
    @Override
    public void setExcludes(final Formula[] theExcludes) {

        if (theExcludes == null) {
            this.exclude = null;
        } else {
            this.exclude = new Formula[theExcludes.length];
            System.arraycopy(theExcludes, 0, this.exclude, 0, theExcludes.length);
        }
    }

    /**
     * Sets the connection of excluded value formulae.
     *
     * @param theExcludes the connection of excluded value formulae
     */
    @Override
    public void setExcludes(final Collection<Formula> theExcludes) {

        if (theExcludes == null || theExcludes.isEmpty()) {
            this.exclude = null;
        } else {
            this.exclude = theExcludes.toArray(ZERO_LEN_FORMULA_ARR);
        }
    }

    /**
     * Gets the list of excluded value formulae, used to prevent values from being selected as random integers.
     *
     * @return the array of formulae for excluded values
     */
    @Override
    public Formula[] getExcludes() {

        if (this.exclude == null) {
            return null;
        }

        return this.exclude.clone();
    }

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.min) + Objects.hashCode(this.max)
                + Objects.hashCode(this.maxDenom) + Objects.hashCode(this.exclude);
    }

    /**
     * Tests whether this object is equal to another object. To be equal, the other object must be a {@code Parameter}
     * and be in exactly the same state.
     *
     * @param obj the object to test for equality
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final VariableRandomSimpleAngle variable) {
            equal = innerEquals(variable) && Objects.equals(this.min, variable.min)
                    && Objects.equals(this.max, variable.max)
                    && Objects.equals(this.maxDenom, variable.maxDenom)
                    && Arrays.equals(this.exclude, variable.exclude);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Clears the cached derived value.
     */
    @Override
    public void clearDerivedValues() {

        setValue(null);
    }

    /**
     * Generates a string representation of the parameter, including any assigned value. The format of the generated
     * string is: name=value (type)
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final Object value = getValue();
        builder.add(this.name, " = ", value, LPAREN, TYPE_TAG, " between ", this.min, " and ", this.max,
                " with max denominator ", this.maxDenom);

        final Formula[] exc = getExcludes();

        if (exc != null && exc.length > 0) {
            builder.add(" excluding ");
            final int excLength = exc.length;
            for (int i = 0; i < excLength; ++i) {
                if (i != 0) {
                    builder.add(CoreConstants.COMMA_CHAR);
                }
                builder.add(exc[i]);
            }
        }

        builder.add(RPAREN);

        return builder.toString();
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind0 = makeIndent(indent);
        final String ind1 = makeIndent(indent + 1);

        startXml(xml, indent, TYPE_TAG);
        final Object value = getValue();
        writeAttribute(xml, "value", value);

        if (this.min != null) {
            final Number minConstant = this.min.getNumber();
            writeAttribute(xml, "min", minConstant);
        }

        if (this.max != null) {
            final Number maxConstant = this.max.getNumber();
            writeAttribute(xml, "max", maxConstant);
        }

        if (this.maxDenom != null) {
            final Number maxDenomConstant = this.maxDenom.getNumber();
            writeAttribute(xml, "max-denom", maxDenomConstant);
        }

        if ((this.min != null && this.min.getFormula() != null)
                || (this.max != null && this.max.getFormula() != null)
                || (this.maxDenom != null && this.maxDenom.getFormula() != null)
                || (this.exclude != null && this.exclude.length > 0)) {

            xml.addln('>');

            if (this.min != null && this.min.getFormula() != null) {
                xml.add(ind1, "<min>");
                this.min.getFormula().appendChildrenXml(xml);
                xml.addln("</min>");
            }
            if (this.max != null && this.max.getFormula() != null) {
                xml.add(ind1, "<max>");
                this.max.getFormula().appendChildrenXml(xml);
                xml.addln("</max>");
            }
            if (this.maxDenom != null && this.maxDenom.getFormula() != null) {
                xml.add(ind1, "<max-denom>");
                this.maxDenom.getFormula().appendChildrenXml(xml);
                xml.addln("</max-denom>");
            }
            if (this.exclude != null) {
                for (final Formula formula : this.exclude) {
                    xml.add(ind1, "<exclude>");
                    formula.appendChildrenXml(xml);
                    xml.addln("</exclude>");
                }
            }

            xml.addln(ind0, "</var>");
        } else {
            xml.addln("/>");
        }
    }

    /**
     * Prints diagnostic information about a parameter, in HTML format, to a stream. The data will be embedded as a list
     * item in an unordered list.
     *
     * @param ps          the stream to print to
     * @param includeTree {@code true} to include a dump of the entire span tree structures
     */
    @Override
    public void printDiagnostics(final PrintStream ps, final boolean includeTree) {

        startDiagnostics(ps);

        ps.print(TYPE_TAG);
        if (this.min != null) {
            ps.print(" min=");
            ps.print(this.min);
        }
        if (this.max != null) {
            ps.print(" max=");
            ps.print(this.max);
        }
        if (this.maxDenom != null) {
            ps.print(" maxDenom=");
            ps.print(this.maxDenom);
        }

        final Formula[] exc = getExcludes();

        if (exc != null && exc.length > 0) {
            ps.print(" exclude=");
            final int excLength = exc.length;
            for (int i = 0; i < excLength; ++i) {
                if (i != 0) {
                    ps.print(CoreConstants.COMMA_CHAR);
                }
                ps.print(exc[i]);
            }
        }

        if (hasValue()) {
            ps.print(" generated=");
            final Object value = getValue();
            ps.print(value);
        }

        ps.println(RPAREN);
    }
}
