package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocTextInst;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.assessment.formula.RealVectorValue;
import dev.mathops.assessment.variable.AbstractFormattableVariable;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A reference to a parameter.
 */
public final class DocParameterReference extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6888020305849056206L;

    /** The parameter name. */
    private String parameterName;

    /**
     * The contents collected when this object is laid out, represented as a simple span. The contents of this span are
     * rendered when this object is rendered. This is not cached - it is re-generated from variable values each time
     * layout is performed, allowing changing input values to be reflected.
     */
    private DocSimpleSpan laidOutContents;

    /**
     * Construct a new {@code DocParameterReference} object.
     */
    private DocParameterReference() {

        super();
    }

    /**
     * Construct a new {@code DocParameterReference} object with an initial parameter name value.
     *
     * @param theParameterName the parameter name
     */
    public DocParameterReference(final String theParameterName) {

        super();

        setParameterName(theParameterName);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects. Note that this does not copy laid out
     * contents, which are re-generated on each layout operation.
     *
     * @return the cloned object
     */
    @Override
    public DocParameterReference deepCopy() {

        final DocParameterReference copy = new DocParameterReference();

        copy.copyObjectFrom(this);
        copy.parameterName = this.parameterName;

        return copy;
    }

    /**
     * Get the parameter name.
     *
     * @return the parameter name
     */
    public String getVariableName() {

        return this.parameterName;
    }

    /**
     * Set the parameter name.
     *
     * @param theParameterName the new parameter name
     */
    private void setParameterName(final String theParameterName) {

        if (!theParameterName.isEmpty() && theParameterName.charAt(0) == '\\') {
            throw new IllegalArgumentException(theParameterName);
        }

        this.parameterName = theParameterName;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        // TODO: If this object is a span, check the left-align of the first item in that span.

        return BASELINE;
    }

    /**
     * Recompute the size of the object's bounding box, and those of its children. This base class method simply calls
     * {@code doLayout} on all children. It will be up to overriding subclasses to set the locations of the children
     * relative to each other.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final AbstractVariable var = context.getVariable(this.parameterName);

        this.laidOutContents = new DocSimpleSpan();

        if (var == null) {
            Log.warning("Variable {", this.parameterName, "} undefined; emitting empty string");
            this.laidOutContents.add(new DocText(CoreConstants.EMPTY));
        } else {
            final Object value = var.getValue();

            if (value == null) {
                // Log.warning("Variable {", this.parameterName,
                // "} has no value - emitting empty string");
                this.laidOutContents.add(new DocText(CoreConstants.EMPTY));
            } else if (value instanceof Boolean) {
                this.laidOutContents.add(new DocText(value.toString()));
            } else if (value instanceof final Number numberValue) {
                final String str;
                if (var instanceof final AbstractFormattableVariable formattable) {
                    str = formattable.valueAsString();
                } else {
                    str = numberValue.toString().replace('-', '\u2014');
                }
                this.laidOutContents.add(new DocText(str));
            } else if (value instanceof IntegerVectorValue || value instanceof RealVectorValue) {
                this.laidOutContents.add(new DocText(value.toString()));
            } else if (value instanceof final String stringValue) {
                this.laidOutContents.add(new DocText(stringValue));
            } else if (value instanceof final DocSimpleSpan spanValue) {

                // The Variable value is a span - we cannot add that span or its children
                // directly because other references to the same variable will return the
                // same span, and layout data will stomp earlier uses. Make a deep copy...

                this.laidOutContents.copySpanFrom(spanValue);

                // If the span has inputs, we need to replace the original inputs in the document
                // with the new ones (with the same name) in the span

                AbstractDocContainer parent = getParent();
                while (parent != null && parent.getParent() != null) {
                    parent = parent.getParent();
                }

                if (parent instanceof final DocColumn col) {
                    final List<AbstractDocInput> docInputs = col.getInputs();

                    final List<AbstractDocInput> newInputs = new ArrayList<>(10);
                    this.laidOutContents.accumulateInputs(newInputs);

                    for (final AbstractDocInput inp : newInputs) {
                        final Iterator<AbstractDocInput> iter = docInputs.iterator();
                        while (iter.hasNext()) {
                            final AbstractDocInput item = iter.next();
                            if (item.getName().equals(inp.getName())) {
                                iter.remove();
                                break;
                            }
                        }
                        docInputs.add(inp);
                    }
//                } else {
//                    Log.warning("Top object in document tree is ", parent);
                }

            } else {
                Log.warning("Unexpected value type: ", value.getClass().getSimpleName());
            }
        }

        // We have assembled a span of laid out contents - allow it to lay itself out...
        // If a parameter reference child added nested parameter references to the laid out content
        // list, those references should be expanded by this call, to any recursive depth.

        this.laidOutContents.setParent(getParent());
        this.laidOutContents.setScale(getScale());

        this.laidOutContents.doLayout(context, mathMode);
    }

    /**
     * Get the laid out contents.
     *
     * @return the laid out contents, which must be flowed by a Paragraph container.
     */
    DocSimpleSpan getLaidOutContents() {

        return this.laidOutContents;
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        if (this.laidOutContents != null) {
            this.laidOutContents.paintComponent(grx, mathMode);
        }
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        if (this.parameterName != null) {
            set.add(this.parameterName);
        }
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public AbstractDocObjectInst createInstance(final EvalContext evalContext) {

        AbstractDocObjectInst result = null;

        final AbstractVariable var = evalContext.getVariable(this.parameterName);

        if (var == null) {
            Log.warning("Variable {", this.parameterName, "} undefined.");
        } else if (var.isInput()) {
            // FIXME: For now, we do not allow the user's responses to get built-in to solution documents.  Change?
            Log.warning("Input variable value used in document - invalid.");
        } else {
            final Object value = var.getValue();

            if (value == null) {
                Log.warning("Variable {", this.parameterName, "} has no value - emitting empty span");
                result = new DocTextInst(null, null, CoreConstants.EMPTY);
            } else if (value instanceof Boolean) {
                // Emit boolean as "TRUE" or "FALSE"
                result = new DocTextInst(null, null, var.toString());
            } else if (value instanceof final Number numberValue) {
                // Emit numbers using their numeric representation
                // FIXME: Could we emit {\minus} rather than \u2212 directly?
                final String str = var instanceof final AbstractFormattableVariable formattable
                        ? formattable.valueAsString() : numberValue.toString().replace('-', '\u2212');
                result = new DocTextInst(null, null, str);
            } else if (value instanceof IntegerVectorValue || value instanceof RealVectorValue) {
                // FIXME: Does the following replace '-' minus signs with better symbols?
                result = new DocTextInst(null, null, value.toString());
            } else if (value instanceof final DocSimpleSpan spanValue) {
                result = spanValue.createInstance(evalContext);
            } else {
                Log.warning("Unexpected value type: ", value.getClass().getSimpleName());
            }
        }

        return result;
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("{", this.parameterName, "}");
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     * @param context      the evaluation context
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll, final HtmlBuilder builder,
                        final boolean showAnswers, final char[] mode, final EvalContext context) {

        final AbstractVariable param = context.getVariable(this.parameterName);

        if ((param != null) && (param.hasValue())) {
            final Object value = param.getValue();

            if (value instanceof AbstractDocObjectTemplate) {
                ((AbstractDocObjectTemplate) value).toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode,
                        context);
            } else if (value instanceof Double) {
                // Round to 4 places
                final double dbl = ((Double) value).doubleValue();
                final DecimalFormat fmt = new DecimalFormat("#,##0.####");
                builder.add(fmt.format(dbl));
            } else {
                String val = value.toString();
                val = val.replace("$", "\\$");
                val = val.replace("&", "\\&");
                val = val.replace("%", "\\%");
                builder.add(val);
            }
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>ParameterReference {" + this.parameterName + "}</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "{" + this.parameterName + "}";
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        // return innerHashCode() + Objects.hashCode(this.parameterName);

        return Objects.hashCode(this.parameterName);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocParameterReference ref) {
            equal = Objects.equals(this.parameterName, ref.parameterName);
        } else {
            equal = false;
        }

        return equal;
    }
}
