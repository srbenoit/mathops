package dev.mathops.assessment.variable;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.Irrational;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A parameter that can be used to randomize or uniquely generate a problem. Parameters can be constant types (integer,
 * real, boolean or string), randomized types (integer, real or boolean), or derived values.
 */
public abstract class AbstractVariable extends AbstractXmlObject {

    /** A common string. */
    static final String LPAREN = " (";

    /** A common string. */
    static final String RPAREN = ")";

    /** The name of the parameter. */
    public String name;

    /** The type of value this variable will store. */
    public EType type;

    /** The value of the parameter. */
    private Object value;

    /** Comments attached to the variable. */
    private List<String> comments;

    /**
     * Constructs a new {@code AbstractVariable}.
     *
     * @param theName the variable name
     * @param theType the type of value this variable can store
     */
    AbstractVariable(final String theName, final EType theType) {

        super();

        if (theName == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Type may not be null");
        }

        this.name = theName;
        this.type = theType;
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    public abstract EVariableType getVariableType();

    /**
     * Updates the variable type (only used for the old-style "param" elements).
     *
     * @param theType the type
     */
    public final void setType(final EType theType) {

        if (theType == null) {
            throw new IllegalArgumentException("Type may not be null");
        }

        this.type = theType;
    }

    /**
     * Tests whether this is an input variable.
     *
     * @return true if an input variable
     */
    public abstract boolean isInput();

    /**
     * Creates a copy of the parameter. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    abstract AbstractVariable deepCopy();

    /**
     * Populates the fields of a copy of this variable.
     *
     * @param copy the copy
     */
    final void populateCopy(final AbstractVariable copy) {

        if (this.value != null) {

            if (this.value instanceof AbstractDocObjectTemplate) {
                copy.value = ((AbstractDocObjectTemplate) this.value).deepCopy();
            } else {
                // Other possible values are immutable, so copy reference
                copy.value = this.value;
            }
        }

        if (this.comments != null) {
            copy.comments = new ArrayList<>(this.comments);
        }
    }

    /**
     * Sets the variable's value.
     *
     * @param theValue a {@code Double}, {@code Irrational}, {@code Long}, {@code Boolean}, {@code String},
     *                 {@code DocSimpleSpan} , or {@code IntegerVectorValue} value, or {@code null} if the variable has
     *                 no value
     */
    public final void setValue(final Object theValue) {

        if (theValue instanceof Double || theValue instanceof Irrational || theValue instanceof Long
                || theValue instanceof Boolean || theValue instanceof String
                || theValue instanceof DocSimpleSpan || theValue instanceof IntegerVectorValue
                || theValue == null) {

            this.value = theValue;
        } else {
            throw new IllegalArgumentException("Invalid  data type: " + theValue.getClass().getName());
        }
    }

    /**
     * Tests whether the parameter has a value set or not.
     *
     * @return {@code true} if a value has been set; {@code false} otherwise
     */
    public final boolean hasValue() {

        return this.value != null;
    }

    /**
     * Gets the parameter's value.
     *
     * @return a {@code Long},{@code Double}, {@code Boolean}, {@code String}, {@code DocSimpleSpan}, or
     *         {@code IntegerVectorValue} value, or {@code null}if the parameter has no value
     */
    public final Object getValue() {

        return this.value;
    }

    /**
     * Retrieves a string representation of this parameter's value.
     *
     * @return the parameter value string
     */
    public String valueAsString() {

        final Object v = this.value;
        final String str;

        if (v instanceof Long) {
            final DecimalFormat fmt = new DecimalFormat();
            str = fmt.format(((Long) v).longValue());
        } else if (v instanceof Double) {
            // By default, we truncate reals at 8 decimal points
            final DecimalFormat fmt = new DecimalFormat();
            fmt.setMaximumFractionDigits(8);
            str = fmt.format(((Double) v).doubleValue());
        } else if (v instanceof Boolean) {
            str = ((Boolean) v).toString().toUpperCase(Locale.ROOT);
        } else if (v != null) {
            str = v.toString();
        } else {
            str = "null";
        }

        return str;
    }

    /**
     * Clears the cached derived value.
     */
    public abstract void clearDerivedValues();

    /**
     * Adds a comment.
     *
     * @param theComment the comment text
     */
    final void addComment(final String theComment) {

        if (this.comments == null) {
            this.comments = new ArrayList<>(1);
        }
        this.comments.add(theComment);
    }

    /**
     * Gets the list of comments.
     *
     * @return the list of comments (an empty list if there are no comments)
     */
    public final List<String> getComments() {

        return this.comments == null ? new ArrayList<>(0) : this.comments;
    }

    /**
     * Generates a string representation of the parameter, including any assigned value. The format of the generated
     * string is: name=value (type)
     *
     * @return the string representation
     */
    @Override
    public abstract String toString();

    /**
     * Starts the XML representation, emitting attributes for members in this base class.
     *
     * @param xml       the {@code HtmlBuilder} to which to write the XML
     * @param indent    the number of spaces to indent the printout
     * @param typeLabel the type (the value of the 'type' attribute)
     */
    void startXml(final HtmlBuilder xml, final int indent, final String typeLabel) {

        final String ind = makeIndent(indent);

        if (this.comments != null) {
            for (final String s : this.comments) {
                xml.addln(ind, "<!-- ", s, " -->");
            }
        }

        xml.add(ind, "<var");

        writeAttribute(xml, "name", this.name);
        writeAttribute(xml, "type", typeLabel);
    }

    /**
     * Starts the diagnostic information.
     *
     * @param ps the stream to print to
     */
    final void startDiagnostics(final PrintStream ps) {

        ps.print("<tt><b>");
        ps.print(this.name);
        ps.print(" (");
        ps.print(this.type.label);
        ps.print(")</b></tt>");

        ps.print(LPAREN);
    }

    /**
     * Prints diagnostic information about a parameter, in HTML format, to a stream. The data will be embedded as a list
     * item in an unordered list.
     *
     * @param ps          the stream to print to
     * @param includeTree {@code true} to include a dump of the entire span tree structures
     */
    public abstract void printDiagnostics(PrintStream ps, boolean includeTree);

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public abstract int hashCode();

    /**
     * Tests whether this object is equal to another object. To be equal, the other object must be a {@code Parameter}
     * and be in exactly the same state.
     *
     * @param obj the object to test for equality
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Gets the hash code of the fields in the base class.
     *
     * @return the hash code
     */
    int innerHashCode() {

        return this.name.hashCode() + this.type.hashCode() + Objects.hashCode(this.value);
    }

    /**
     * Tests whether this object is equal to another object. To be equal, the other object must be a {@code Parameter}
     * and be in exactly the same state.
     *
     * @param obj the object to test for equality
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    boolean innerEquals(final AbstractVariable obj) {

        return this.name.equals(obj.name) && this.type == obj.type && Objects.equals(this.value, obj.value);
    }
}
