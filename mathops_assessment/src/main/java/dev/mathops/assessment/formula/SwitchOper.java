package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FESwitch;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A test operation that evaluates a boolean condition, and depending on the result, returns the evaluated value of one
 * of two subexpressions. This can be interpreted logically is "if X is true, then A, otherwise B".
 */
public class SwitchOper extends AbstractFormulaObject implements IEditableFormulaObject {

    /** The test condition, which must evaluate to an integer. */
    public AbstractFormulaObject condition;

    /** The list of cases. */
    public final List<SwitchCase> cases;

    /** The default value. */
    public AbstractFormulaObject defaultValue;

    /**
     * Construct a new {@code SwitchOper}.
     */
    public SwitchOper() {

        super();

        this.cases = new ArrayList<>(10);
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public SwitchOper deepCopy() {

        final SwitchOper copy = new SwitchOper();

        if (this.condition != null) {
            copy.condition = this.condition.deepCopy();
        }
        final int count = this.cases.size();
        for (int i = 0; i < count; ++i) {
            copy.cases.add(this.cases.get(i).deepCopy());
        }
        if (this.defaultValue != null) {
            copy.defaultValue = this.defaultValue.deepCopy();
        }

        return copy;
    }

    /**
     * Generate the string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public String toString() {

        final HtmlBuilder str = new HtmlBuilder(50);
        str.add("switch (", this.condition.toString(), ") {");

        for (final SwitchCase c : this.cases) {
            c.appendString(str);
        }
        if (this.defaultValue != null) {
            str.add(" default: ", this.defaultValue.toString());
        }

        return str.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return the type; EType.ERROR if no type can be determined
     */
    @Override
    public EType getType(final EvalContext context) {

        EType result = null;

        if (this.defaultValue != null) {
            result = this.defaultValue.getType(context);
        }

        for (final SwitchCase c : this.cases) {
            final EType caseType = c.value.getType(context);

            if (result == null) {
                result = caseType;
            } else if (result == EType.INTEGER && caseType == EType.REAL) {
                result = EType.REAL;
            } else if (result == EType.REAL && caseType == EType.INTEGER) {
                result = EType.REAL;
            } else if (result != caseType) {
                result = EType.ERROR;
                break;
            }
        }

        if (result == null) {
            result = EType.ERROR;
        }

        return result;
    }

    /**
     * Tests whether this object is a simple constant value.
     *
     * @return true if a constant value (false for objects of this class)
     */
    @Override
    public boolean isConstant() {

        return false;
    }

    /**
     * Evaluates the object within the tree. Subclasses should override this to produce the correct value.
     *
     * @param context the context under which to evaluate the formula
     * @return an Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        Object result = null;

        if (this.condition == null) {
            result = new ErrorValue("Switch cannot be evaluated without condition");
        } else {
            final Object decision = this.condition.evaluate(context);
            if (decision instanceof final Long l) {
                final int decisionvalue = l.intValue();

                for (final SwitchCase c : this.cases) {
                    if (c.toMatch == decisionvalue) {
                        result = c.value.evaluate(context);
                        break;
                    }
                }

                if (result == null) {
                    if (this.defaultValue == null) {
                        result = new ErrorValue(//
                                "No cases matched, and no default value provided.");
                    } else {
                        result = this.defaultValue.evaluate(context);
                    }
                }
            } else {
                result = new ErrorValue(//
                        "Switch condition did not evaluate to an integer.");
            }
        }

        return result;
    }

    /**
     * Simplifies a formula by replacing all parameter references to constant values with the constant itself, and then
     * performing any constant-valued evaluations. For example, if a formula contained "3 * ({x} - 4)" and the parameter
     * {x} was a constant integer with value 7, this formula would be simplified to a single integer constant with value
     * 9.
     *
     * @param context the context under which to evaluate the formula
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    @Override
    public AbstractFormulaObject simplify(final EvalContext context) {

        AbstractFormulaObject result = this;

        if (this.condition != null && this.condition.isConstant()) {
            final Object decision = this.condition.evaluate(context);
            if (decision instanceof final Long l) {
                final int decisionvalue = l.intValue();

                for (final SwitchCase c : this.cases) {
                    if (c.toMatch == decisionvalue) {
                        result = c.value.simplify(context);
                        break;
                    }
                }

                if (result == null) {
                    if (this.defaultValue == null) {
                        result = new ErrorValue(//
                                "No cases matched, and no default value provided.");
                    } else {
                        result = this.defaultValue.simplify(context);
                    }
                }
            }
        } else {
            final SwitchOper clone = new SwitchOper();

            if (this.condition != null) {
                clone.condition = this.condition.simplify(context);
            }
            if (this.defaultValue != null) {
                clone.defaultValue = this.defaultValue.simplify(context);
            }

            for (final SwitchCase c : this.cases) {
                clone.cases.add(c.simplify(context));
            }

            result = clone;
        }

        return result;
    }

    /**
     * Generates an {@code AbstractFEObject} for this object.
     *
     * @param theFontSize the font size for the generated object
     * @return the generated {@code AbstractFEObject}
     */
    @Override
    public AbstractFEObject generateFEObject(final int theFontSize) {

        // TODO:

        return new FESwitch(theFontSize);
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        int hash = this.cases.hashCode();

        if (this.condition != null) {
            hash += this.condition.hashCode();
        }
        if (this.defaultValue != null) {
            hash += this.defaultValue.hashCode();
        }

        return hash;
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final SwitchOper oper) {
            equal = Objects.equals(this.condition, oper.condition)
                    && Objects.equals(this.defaultValue, oper.defaultValue)
                    && this.cases.equals(oper.cases);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Appends an XML representation of the formula to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    @Override
    public void appendXml(final HtmlBuilder xml) {

        xml.add("<switch>");
        if (this.condition != null) {
            xml.add("<condition>");
            this.condition.appendXml(xml);
            xml.add("</condition>");
        }
        for (final SwitchCase c : this.cases) {
            xml.add("<case value='", Integer.toString(c.toMatch), "'>");
            c.value.appendXml(xml);
            xml.add("</case>");
        }
        if (this.defaultValue != null) {
            xml.add("<default>");
            this.defaultValue.appendXml(xml);
            xml.add("</default>");
        }
        xml.add("</switch>");
    }

    /**
     * Appends a diagnostic representation of the formula.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param indent the indent level
     */
    @Override
    public void printDiagnostics(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent * 3);

        xml.addln(ind, "Switch Operation:");
        if (this.condition != null) {
            xml.addln(ind, "Condition:");
            this.condition.printDiagnostics(xml, indent + 1);
        }
        for (final SwitchCase c : this.cases) {
            xml.addln(ind, "Case ", Integer.toString(c.toMatch), ":");
            c.value.printDiagnostics(xml, indent + 1);
        }
        if (this.defaultValue != null) {
            xml.addln(ind, "Default value:");
            this.defaultValue.printDiagnostics(xml, indent + 1);
        }
    }
}
