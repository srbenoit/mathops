package dev.mathops.assessment.document.template;

import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.assessment.variable.VariableInputReal;
import dev.mathops.assessment.variable.VariableInputString;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;

import java.awt.Color;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The base class for document objects that function as inputs. Each input has a name under which it stores user entries
 * in the parameter set.
 */
public abstract class AbstractDocInput extends AbstractDocContainer {

    // FIXME: This class must inherit from AbstractDocContainer so it gets the UI events,
    // maybe make the UI event handling an interface

    /** A background color to use for disabled fields. */
    static final Color DISABLED_BG_COLOR;

    /** A foreground color to use for disabled fields. */
    static final Color DISABLED_FG_COLOR;

    /** A background color to use for disabled fields. */
    static final Color ENABLED_BG_COLOR;

    /** A foreground color to use for disabled fields. */
    static final Color ENABLED_FG_COLOR;

    /** A background color to use for disabled fields. */
    static final Color SELECTED_BG_COLOR;

    /** A background color to use for fields containing errors. */
    static final Color ERROR_BG_COLOR;

    /** A foreground color to use for disabled fields. */
    static final Color SELECTED_FG_COLOR;

    /** A color to use for the edit caret. */
    static final Color CARET_COLOR;

    /** A color for highlighting selected content. */
    static final Color HIGHLIGHT_COLOR;

    /** A color for a drop shadow around the field. */
    static final Color SHADOW_COLOR;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1489921332826140642L;

    /** Formula to determine if the input is enabled. */
    @Deprecated
    private Formula enabledFormula;

    /** The name of a variable that will control enabled state of this input. */
    private String enabledVarName;

    /** The value of {@code enabledVarName} that will enable this input (Boolean or Long). */
    private Object enabledVarValue;

    /** A list of listeners to be notified when the input value changes. */
    private transient List<InputChangeListener> listeners;

    /** The name of the input's value in the parameter set. */
    private final String name;

    /** Flag indicating the input is selected and should receive keystrokes. */
    private boolean selected;

    /** The evaluation context to which the input is bound. */
    private EvalContext evalContext;

    /* Static initialization to create the default colors */
    static {
        // Default values
        DISABLED_BG_COLOR = ColorNames.getColor("gray90");
        DISABLED_FG_COLOR = ColorNames.getColor("gray50");

        ENABLED_BG_COLOR = ColorNames.getColor("white");
        ENABLED_FG_COLOR = ColorNames.getColor("black");

        SELECTED_BG_COLOR = ColorNames.getColor("wheat1");
        SELECTED_FG_COLOR = ColorNames.getColor("black");

        ERROR_BG_COLOR = ColorNames.getColor("burlywood");
        CARET_COLOR = ColorNames.getColor("RoyalBlue4");
        HIGHLIGHT_COLOR = ColorNames.getColor("LightSteelBlue3");
        SHADOW_COLOR = ColorNames.getColor("gray70");
    }

    /**
     * Construct a new {@code AbstractDocInput}.
     *
     * @param theName the name of the input's value in the parameter set
     */
    AbstractDocInput(final String theName) {

        super();

        this.name = theName;
    }

    /**
     * Copy information from a source {@code AbstractDocInput} object, including all underlying {@code DocFormattable}
     * information.
     *
     * @param source the {@code DocObject} from which to copy data
     */
    final void copyObjectFromInput(final AbstractDocInput source) {

        copyObjectFromContainer(source);

        this.enabledVarName = source.enabledVarName;
        this.enabledVarValue = source.enabledVarValue;

        if (source.enabledFormula != null) {
            this.enabledFormula = source.enabledFormula.deepCopy();
        }
    }

    /**
     * Binds the input to the corresponding variable in an evaluation context, creating that variable if not already
     * present.
     *
     * @param theContext the evaluation context
     */
    public void bind(final EvalContext theContext) {

        this.evalContext = theContext;
    }

    /**
     * Gets the evaluation context to which the input is bound.
     *
     * @return the evaluation context
     */
    final EvalContext getEvalContext() {

        return this.evalContext;
    }

    /**
     * Add a listener that is to be notified when the input's value changes.
     *
     * @param listener the listener to add
     */
    final void addInputChangeListener(final InputChangeListener listener) {
        synchronized (this) {

            if (this.listeners == null) {
                this.listeners = new ArrayList<>(1);
            }

            this.listeners.add(listener);
        }
    }

    /**
     * Removes a listener that is to be notified when the input's value changes.
     *
     * @param listener the listener to remove
     */
    final void removeInputChangeListener(final InputChangeListener listener) {
        synchronized (this) {

            if (this.listeners != null) {
                this.listeners.remove(listener);
            }

        }
    }

    /**
     * Notify all registered listeners that the value has changed.
     */
    final void notifyChangeListeners() {
        synchronized (this) {

            if (this.listeners != null) {
                for (final InputChangeListener listener : this.listeners) {
                    listener.inputChanged(this);
                }
            }
        }
    }

    /**
     * Get the name of the input.
     *
     * @return the input name
     */
    public String getName() {

        return this.name;
    }

    /**
     * Determine whether the input is currently enabled.
     *
     * @return true if the input is enabled; false otherwise
     */
    public final boolean isEnabled() {

        boolean enabled = true;
        final Object obj;

        if (this.evalContext != null) {
            if (this.enabledFormula == null) {
                if (this.enabledVarName != null) {
                    final Object value = this.evalContext.getVariable(this.enabledVarName).getValue();
                    enabled = value != null && value.equals(this.enabledVarValue);
                }
            } else {
                obj = this.enabledFormula.evaluate(this.evalContext);

                if (obj instanceof Boolean) {
                    enabled = ((Boolean) obj).booleanValue();
                } else {
                    enabled = false;
                }
            }
        }

        return enabled;
    }

    /**
     * Gets the enabled formula for the input.
     *
     * @return the formula to evaluate to determine whether this input is enabled
     */
    public final Formula getEnabledFormula() {

        return this.enabledFormula;
    }

    /**
     * Set the enabled formula for the input.
     *
     * @param theEnabledFormula the formula to evaluate to determine whether this input is enabled
     */
    public void setEnabledFormula(final Formula theEnabledFormula) {

        this.enabledFormula = theEnabledFormula;
    }

    /**
     * Gets the name of the variable whose value controls whether this input is enabled.
     *
     * @return the variable name
     */
    public final String getEnabledVarName() {

        return this.enabledVarName;
    }

    /**
     * Set the name of the variable whose value controls whether this input is enabled.
     *
     * @param theEnabledVarName the new variable name
     */
    final void setEnabledVarName(final String theEnabledVarName) {

        this.enabledVarName = theEnabledVarName;
    }

    /**
     * Gets the value that the enable-control variable must have in order for this input to be enabled.
     *
     * @return the variable name
     */
    public final Object getEnabledVarValue() {

        return this.enabledVarValue;
    }

    /**
     * Set the value that the enable-control variable must have in order for this input to be enabled.
     *
     * @param theEnabledVarValue the new variable value
     */
    final void setEnabledVarValue(final Boolean theEnabledVarValue) {

        this.enabledVarValue = theEnabledVarValue;
    }

    /**
     * Set the value that the enable-control variable must have in order for this input to be enabled.
     *
     * @param theEnabledVarValue the new variable value
     */
    final void setEnabledVarValue(final Long theEnabledVarValue) {

        this.enabledVarValue = theEnabledVarValue;
    }

    /**
     * Store a value for the object in the evaluation context.
     *
     * @param value the value to store
     */
    final void storeValue(final Object value) {

        if (this.evalContext != null) {
            AbstractVariable var = this.evalContext.getVariable(this.name);

            if (var == null) {

                if (value == null || value instanceof Double) {
                    var = new VariableInputReal(this.name);
                } else if (value instanceof Long) {
                    var = new VariableInputInteger(this.name);
                } else if (value instanceof String) {
                    var = new VariableInputString(this.name);
                } else {
                    throw new IllegalArgumentException(Res.get(Res.BAD_DATA_TYPE));
                }

                var.setValue(value);
                this.evalContext.addVariable(var);
            } else if (value != null) {

                if (value instanceof Long) {
                    if (!(var instanceof VariableInputInteger)) {
                        throw new IllegalArgumentException(Res.get(Res.BAD_DATA_TYPE));
                    }
                } else if (value instanceof Double) {
                    if (!(var instanceof VariableInputReal)) {
                        throw new IllegalArgumentException(Res.get(Res.BAD_DATA_TYPE));
                    }
                } else if (value instanceof String) {
                    if (!(var instanceof VariableInputString)) {
                        throw new IllegalArgumentException(Res.get(Res.BAD_DATA_TYPE));
                    }
                } else {
                    throw new IllegalArgumentException(Res.get(Res.BAD_DATA_TYPE));
                }

                if (!value.equals(var.getValue())) {
                    var.setValue(value);
                }
            } else if (var.getValue() != null) {
                var.setValue(null);
            }
        }
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public final void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        if (this.name != null) {
            set.add(this.name);
        }

        if (this.enabledFormula != null) {
            set.addAll(this.enabledFormula.params.keySet());
        }

        if (this.enabledVarName != null) {
            set.add(this.enabledVarName);
        }
    }

    /**
     * Add XML attributes specific to input fields to an XML block.
     *
     * @param builder the {@code HtmlBuilder} to which to append the XML attributes
     */
    final void addXmlAttributes(final HtmlBuilder builder) {

        builder.add(" name='", this.name, "'");

        if (this.enabledVarName != null) {
            builder.add(" enabled-var-name='", this.enabledVarName, "'");
        }
        if (this.enabledVarValue != null) {
            builder.add(" enabled-var-value='", this.enabledVarValue, "'");
        }

        printFormat(builder, 1.0f);
    }

    /**
     * Set the selected state of the input.
     *
     * @param isSelected true if the input is selected; false otherwise
     */
    public void setSelected(final boolean isSelected) {

        // This is overridden - don't make the member public

        this.selected = isSelected;
    }

    /**
     * Tests the selected state.
     *
     * @return true if selected
     */
    public final boolean isSelected() {

        return this.selected;
    }

    /**
     * Clear the input's value.
     */
    public abstract void clear();

    /**
     * Set the value of the input based on a String.
     *
     * @param theValue the String representation of the value to set
     * @return true if the value was set; false otherwise
     */
    public abstract boolean setValue(String theValue);

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public final int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int inputInnerHashCode() {

        return innerHashCode() + Objects.hashCode(this.enabledFormula)
                + Objects.hashCode(this.enabledVarName)
                + Objects.hashCode(this.enabledVarValue)
                + Objects.hashCode(this.name);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean inputInnerEquals(final AbstractDocInput obj) {

        return innerEquals(obj)
                && Objects.equals(this.enabledFormula, obj.enabledFormula)
                && Objects.equals(this.enabledVarName, obj.enabledVarName)
                && Objects.equals(this.enabledVarValue, obj.enabledVarValue)
                && this.name.equals(obj.name);
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    final void inputInnerWhyNotEqual(final AbstractDocInput obj, final int indent) {

        innerWhyNotEqual(obj, indent);

        if (!Objects.equals(this.enabledFormula, obj.enabledFormula)) {
            if (this.enabledFormula == null || obj.enabledFormula == null) {
                Log.info(makeIndent(indent), "UNEQUAL AbstractDocInput (enabledFormula: ",
                        this.enabledFormula, CoreConstants.SLASH, obj.enabledFormula, ")");
            } else {
                Log.info(makeIndent(indent), "UNEQUAL AbstractDocInput (enabledFormula...)");
                this.enabledFormula.whyNotEqual(obj.enabledFormula, indent + 1);
            }
        }

        if (!Objects.equals(this.enabledVarName, obj.enabledVarName)) {
            Log.info(makeIndent(indent), "UNEQUAL AbstractDocInput (enabledVarName: ", this.enabledVarName, "!=",
                    obj.enabledVarName, ")");
        }

        if (!Objects.equals(this.enabledVarValue, obj.enabledVarValue)) {
            Log.info(makeIndent(indent), "UNEQUAL AbstractDocInput (enabledVarValue: ", this.enabledVarValue, "!=",
                    obj.enabledVarValue, ")");
        }

        if (!Objects.equals(this.name, obj.name)) {
            Log.info(makeIndent(indent), "UNEQUAL AbstractDocInput (name: ", this.name, "!=", obj.name, ")");
        }
    }
}
