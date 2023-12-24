package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.Irrational;
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
 * A random choice-valued variable.
 */
public final class VariableRandomChoice extends AbstractFormattableVariable implements IExcludableVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "random-choice";

    /** A zero-length array used to create other arrays. */
    private static final Formula[] ZERO_LEN_FORMULA_ARR = new Formula[0];

    /** A list of excluded random integer values, which may be formulae. */
    private Formula[] exclude;

    /** A list of values from which to choose. */
    private Formula[] chooseFrom;

    /**
     * Constructs a new {@code VariableRandomChoice}.
     *
     * @param theName the variable name
     * @param theType the type of value this variable can store
     */
    public VariableRandomChoice(final String theName, final EType theType) {

        super(theName, theType);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.RANDOM_CHOICE;
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
     * Creates a copy of the parameter. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    @Override
    VariableRandomChoice deepCopy() {

        final VariableRandomChoice copy = new VariableRandomChoice(this.name, this.type);

        populateCopyFromVariable(copy);

        if (this.exclude != null) {
            final int len = this.exclude.length;
            copy.exclude = new Formula[len];

            for (int i = 0; i < len; ++i) {
                copy.exclude[i] = this.exclude[i].deepCopy();
            }
        }

        if (this.chooseFrom != null) {
            final int len = this.chooseFrom.length;
            copy.chooseFrom = new Formula[len];

            for (int i = 0; i < len; ++i) {
                copy.chooseFrom[i] = this.chooseFrom[i].deepCopy();
            }
        }

        return copy;
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
     * Sets the list of formulae from which a random choice parameter selects.
     *
     * @param theChooseFrom the list of formulae for choices
     */
    void setChooseFromList(final Formula[] theChooseFrom) {

        if (theChooseFrom == null) {
            this.chooseFrom = null;
        } else {
            this.chooseFrom = new Formula[theChooseFrom.length];
            System.arraycopy(theChooseFrom, 0, this.chooseFrom, 0, theChooseFrom.length);
        }
    }

    /**
     * Sets the collection of values from which to choose.
     *
     * @param theChoices the collection of values
     */
    void setChooseFromList(final Collection<Formula> theChoices) {

        if (theChoices == null || theChoices.isEmpty()) {
            this.chooseFrom = null;
        } else {
            this.chooseFrom = theChoices.toArray(ZERO_LEN_FORMULA_ARR);
        }
    }

    /**
     * Gets the list of formulae from which a random choice parameter selects.
     *
     * @return the array of formulae for choices
     */
    public Formula[] getChooseFromList() {

        if (this.chooseFrom == null) {
            return null;
        }

        return this.chooseFrom.clone();
    }

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.exclude);
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
        } else if (obj instanceof final VariableRandomChoice var) {
            equal = innerEqualsVariable(var) && Objects.equals(this.exclude, var.exclude)
                    && Objects.equals(this.chooseFrom, var.chooseFrom);
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

        htm.add(this.name, " = ", getValue(), LPAREN, TYPE_TAG);

        if (this.chooseFrom != null) {
            htm.add(" from ");
            final int numChoose = this.chooseFrom.length;
            for (int i = 0; i < numChoose; ++i) {
                if (i != 0) {
                    htm.add(CoreConstants.COMMA_CHAR);
                }
                htm.add(this.chooseFrom[i]);
            }
        }

        final Formula[] exc = getExcludes();

        if (exc != null) {
            final int numExc = exc.length;
            if (numExc> 0) {
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

        final Object value = getValue();

        final String ind0 = makeIndent(indent);
        final String ind1 = makeIndent(indent + 1);

        startXml(xml, indent, TYPE_TAG);
        writeAttribute(xml, "value-type", this.type);

        if (value instanceof Long) {
            writeAttribute(xml, "long", value.toString());
        } else if (value instanceof Double) {
            writeAttribute(xml, "double", value.toString());
        } else if (value instanceof Boolean) {
            writeAttribute(xml, "boolean", value.toString());
        } else if (value instanceof Irrational) {
            writeAttribute(xml, "irrational", value.toString());
        } else if (value instanceof String) {
            writeAttribute(xml, "string", value.toString());
        } else if (value instanceof IntegerVectorValue) {
            writeAttribute(xml, "int-vector", value.toString());
        }
        xml.addln('>');

        if (value instanceof final DocSimpleSpan span) {
            xml.addln(ind1, "<span>", span.toXml(0), "</span>");
        }

        if (this.chooseFrom != null) {
            for (final Formula formula : this.chooseFrom) {
                xml.add(ind1, "<choose-from>");
                formula.appendChildrenXml(xml);
                xml.addln("</choose-from>");
            }
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
        if (this.chooseFrom != null) {
            ps.print(" choose-from=");

            final int chooseLength = this.chooseFrom.length;
            for (int i = 0; i < chooseLength; ++i) {
                if (i != 0) {
                    ps.print(CoreConstants.COMMA_CHAR);
                }
                ps.print(this.chooseFrom[i]);
            }
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
