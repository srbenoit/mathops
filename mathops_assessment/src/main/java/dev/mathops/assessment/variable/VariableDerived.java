package dev.mathops.assessment.variable;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.Irrational;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Objects;

/**
 * A derived value variable.
 */
public final class VariableDerived extends AbstractFormattableVariable
        implements IRangedVariable, IExcludableVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "derived";

    /** A zero-length array used to create other arrays. */
    private static final Formula[] ZERO_LEN_FORMULA_ARR = new Formula[0];

    /** The minimum integer/real value. */
    private NumberOrFormula min;

    /** The maximum integer/real value. */
    private NumberOrFormula max;

    /** A list of excluded random integer values, which may be formulae. */
    private Formula[] exclude;

    /** A formula used to derive the parameter from other parameters. */
    private Formula formula;

    /**
     * Constructs a new {@code IntVariable}.
     *
     * @param theName the variable name
     * @param theType the type of value this variable can store
     */
    public VariableDerived(final String theName, final EType theType) {

        super(theName, theType);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.DERIVED;
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
    VariableDerived deepCopy() {

        final VariableDerived copy = new VariableDerived(this.name, this.type);

        populateCopyFromVariable(copy);

        if (this.min != null) {
            copy.min = this.min.deepCopy();
        }
        if (this.max != null) {
            copy.max = this.max.deepCopy();
        }

        if (this.exclude != null) {
            final int len = this.exclude.length;
            copy.exclude = new Formula[len];

            for (int i = 0; i < len; ++i) {
                copy.exclude[i] = this.exclude[i].deepCopy();
            }
        }

        if (this.formula != null) {
            copy.formula = this.formula.deepCopy();
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
     * Sets the derived parameter formula.
     *
     * @param theFormula the formula
     */
    public void setFormula(final Formula theFormula) {

        this.formula = theFormula;
    }

    /**
     * Gets the derived parameter formula.
     *
     * @return the parameter formula
     */
    public Formula getFormula() {

        return this.formula;
    }

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.min)
                + Objects.hashCode(this.max) + Objects.hashCode(this.exclude)
                + Objects.hashCode(this.formula);
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
        } else if (obj instanceof final VariableDerived var) {
            equal = innerEqualsVariable(var) && Objects.equals(this.min, var.min)
                    && Objects.equals(this.max, var.max)
                    && Objects.equals(this.exclude, var.exclude)
                    && Objects.equals(this.formula, var.formula);
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

        builder.add(this.name, " = ", getValue(), LPAREN, TYPE_TAG, " between ", this.min, " and ", this.max);

        final Formula[] exc = getExcludes();

        if (exc != null) {
            final int excLen = exc.length;
            if (excLen > 0) {
                builder.add(" excluding ");
                for (int i = 0; i < excLen; ++i) {
                    if (i != 0) {
                        builder.add(CoreConstants.COMMA_CHAR);
                    }
                    builder.add(exc[i]);
                }
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

        final Object value = getValue();

        final String ind0 = AbstractXmlObject.makeIndent(indent);
        final String ind1 = AbstractXmlObject.makeIndent(indent + 1);

        startXml(xml, indent, TYPE_TAG);

        if (value instanceof Double && this.type == EType.INTEGER) {
            this.type = EType.REAL;
        }
        if (this.type == EType.ERROR) {
            Log.warning("Variable {", this.name, "} has ERROR type with value ", value);
        }

        AbstractXmlObject.writeAttribute(xml, "value-type", this.type);
        if (this.min != null) {
            AbstractXmlObject.writeAttribute(xml, "min", this.min.getNumber());
        }
        if (this.max != null) {
            AbstractXmlObject.writeAttribute(xml, "max", this.max.getNumber());
        }

        if (value instanceof Long) {
            AbstractXmlObject.writeAttribute(xml, "long", value.toString());
        } else if (value instanceof Double) {
            AbstractXmlObject.writeAttribute(xml, "double", value.toString());
        } else if (value instanceof Boolean) {
            AbstractXmlObject.writeAttribute(xml, "boolean", value.toString());
        } else if (value instanceof Irrational) {
            AbstractXmlObject.writeAttribute(xml, "irrational", value.toString());
        } else if (value instanceof final String str) {
            AbstractXmlObject.writeAttribute(xml, "string", str);
        } else if (value instanceof final IntegerVectorValue vec) {
            AbstractXmlObject.writeAttribute(xml, "int-vector", vec);
        }
        xml.addln('>');

        if (value instanceof final DocSimpleSpan span) {
            xml.addln(ind1, "<span>", span.toXml(0),
                    "</span>");
        }

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
        if (this.formula != null) {
            xml.add(ind1);
            this.formula.appendXml(xml);
            xml.addln();
        }
        if (this.exclude != null) {
            for (final Formula f : this.exclude) {
                xml.add(ind1, "<exclude>");
                f.appendChildrenXml(xml);
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
        ps.print(" formula=");
        ps.print(this.formula.toString().replace("<", "&lt;").replace(">", "&gt;"));

        if (hasValue()) {
            final Object value = getValue();

            if (value instanceof Long) {
                ps.print(" generated-integer=");
                ps.print(value);
            } else if (value instanceof Double) {
                ps.print(" generated-real=");
                ps.print(value);
            } else if (value instanceof Boolean) {
                ps.print(" generated-boolean=");
                ps.print(value);
            } else if (value instanceof Irrational) {
                ps.print(" generated-irrational=");
                ps.print(value);
            } else if (value instanceof String) {
                ps.print(" generated-string=");
                ps.print(value);
            } else if (value instanceof IntegerVectorValue) {
                ps.print(" generated-int-vector=");
                ps.print(value);
            } else if (value instanceof final DocSimpleSpan span) {
                ps.print(" generated-span=");

                if (includeTree) {
                    ps.println("<ol>");
                    span.printTree(ps);
                    ps.println("</ol>");
                } else {
                    span.print(ps, 0);
                }
            }
        }

        ps.println(RPAREN);
    }
}
