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
 * A random integer-valued variable.
 */
public final class VariableRandomInteger extends AbstractFormattableVariable
        implements IRangedVariable, IExcludableVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "random-int";

    /** The minimum value. */
    private NumberOrFormula min = null;

    /** The maximum value. */
    private NumberOrFormula max = null;

    /** A list of excluded random integer values, which may be formulae. */
    private Formula[] exclude = null;

    /**
     * Constructs a new {@code VariableRandomInteger}.
     *
     * @param theName the variable name
     */
    public VariableRandomInteger(final String theName) {

        super(theName, EType.INTEGER);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.RANDOM_INTEGER;
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
    VariableRandomInteger deepCopy() {

        final VariableRandomInteger copy = new VariableRandomInteger(this.name);

        populateCopyFromVariable(copy);

        if (this.min != null) {
            copy.min = this.min.deepCopy();
        }
        if (this.max != null) {
            copy.max = this.max.deepCopy();
        }

        if (this.exclude != null) {
            final int numExc = this.exclude.length;
            copy.exclude = new Formula[numExc];

            for (int i = 0; i < numExc; ++i) {
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
            this.exclude = theExcludes.toArray(new Formula[0]);
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

        return innerHashCode() + Objects.hashCode(this.min)
                + Objects.hashCode(this.max)
                + Objects.hashCode(this.exclude);
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
        } else if (obj instanceof final VariableRandomInteger var) {
            equal = innerEqualsVariable(var) && Objects.equals(this.min, var.min)
                    && Objects.equals(this.max, var.max)
                    && Arrays.equals(this.exclude, var.exclude);
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

        final HtmlBuilder htm = new HtmlBuilder(100);

        final Object value = getValue();
        htm.add(this.name, " = ", value, LPAREN, TYPE_TAG, " between ", this.min, " and ", this.max);

        final Formula[] exc = getExcludes();

        if (exc != null) {
            final int numExc = exc.length;
            if (numExc > 0) {
                htm.add(" excluding ");
                for (int i = 0; i < numExc; ++i) {
                    if (i != 0) {
                        htm.add(CoreConstants.COMMA_CHAR);
                    }
                    htm.add(exc[i]);
                }
            }
        }

        htm.add(RPAREN);

        return htm.toString();
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

        if ((this.min != null && this.min.getFormula() != null)
                || (this.max != null && this.max.getFormula() != null)
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
