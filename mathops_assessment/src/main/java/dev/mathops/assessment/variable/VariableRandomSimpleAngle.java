package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

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

        return innerHashCode() + EqualityTests.objectHashCode(this.min)
                + EqualityTests.objectHashCode(this.max) + EqualityTests.objectHashCode(this.maxDenom)
                + EqualityTests.objectHashCode(this.exclude);
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
        } else if (obj instanceof final VariableRandomSimpleAngle var) {
            equal = innerEquals(var) && Objects.equals(this.min, var.min)
                    && Objects.equals(this.max, var.max)
                    && Objects.equals(this.maxDenom, var.maxDenom)
                    && Arrays.equals(this.exclude, var.exclude);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    @Override
    public void whyNotEqual(final AbstractVariable obj, final int indent) {

        innerWhyNotEqual(obj, indent);
        if (obj instanceof final VariableRandomSimpleAngle var) {

            if (!Objects.equals(this.min, var.min)) {
                if (this.min == null || var.min == null) {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomInt (min: " + this.min + "!=" + var.min + ")");
                } else {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomInt (min)");
                }
            }

            if (!Objects.equals(this.max, var.max)) {
                if (this.max == null || var.max == null) {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomInt (max: " + this.max + "!=" + var.max + ")");
                } else {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomInt (max)");
                }
            }

            if (!Objects.equals(this.maxDenom, var.maxDenom)) {
                if (this.maxDenom == null || var.maxDenom == null) {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (maxDenom: " + this.maxDenom + "!="
                            + var.maxDenom + ")");
                } else {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (maxDenom)");
                }
            }

            if (!Objects.equals(this.exclude, var.exclude)) {
                if (this.exclude == null || var.exclude == null) {
                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (exclude: " + this.exclude + "!="
                            + var.exclude + ")");
                } else {
                    final int excludeLen = this.exclude.length;
                    if (excludeLen == var.exclude.length) {
                        for (int i = 0; i < excludeLen; ++i) {
                            final Formula f1 = this.exclude[i];
                            final Formula f2 = var.exclude[i];

                            if (!Objects.equals(f1, f2)) {
                                if (f1 == null || f2 == null) {
                                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (exclude[",
                                            Integer.toString(i), "]: ", f1, "!=", f2, ")");
                                } else {
                                    Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (exclude[",
                                            Integer.toString(i), "])");
                                    f1.whyNotEqual(f2, indent + 1);
                                }
                            }
                        }
                    } else {
                        Log.info(makeIndent(indent), "UNEQUAL VariableRandomSimpleAngle (exclude.length: "
                                + Integer.valueOf(excludeLen) + "!="
                                + Integer.valueOf(var.exclude.length) + ")");
                    }
                }
            }
        }
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

        builder.add(this.name, " = ", getValue(), LPAREN, TYPE_TAG, " between ", this.min, " and ", this.max,
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
        writeAttribute(xml, "value", getValue());
        if (this.min != null) {
            writeAttribute(xml, "min", this.min.getNumber());
        }
        if (this.max != null) {
            writeAttribute(xml, "max", this.max.getNumber());
        }
        if (this.maxDenom != null) {
            writeAttribute(xml, "max-denom", this.maxDenom.getNumber());
        }
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
            ps.print(getValue());
        }

        ps.println(RPAREN);
    }
}
