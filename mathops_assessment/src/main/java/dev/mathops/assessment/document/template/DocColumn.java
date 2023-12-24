package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocColumnInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A column of paragraphs, which will lay out the paragraphs vertically, one after the other, with the top of each
 * paragraph aligned with the bottom of the prior one. All paragraphs will be the same width.
 */
public final class DocColumn extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5005621134852007267L;

    /** The document width, to be applied to all contained paragraphs. */
    private int width;

    /** Ordered list of all input controls. */
    private final List<AbstractDocInput> inputs;

    /**
     * Construct a new {@code DocColumn}.
     */
    public DocColumn() {

        super();

        this.inputs = new ArrayList<>(5);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocColumn deepCopy() {

        final DocColumn copy = new DocColumn();

        copy.copyObjectFromContainer(this);
        copy.width = this.width;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Add a new object.
     *
     * @param comp the {@code DocObject} being added
     */
    @Override
    public void add(final AbstractDocObjectTemplate comp) {

        if (comp instanceof DocParagraph || comp instanceof DocVSpace) {
            comp.uncacheFont();
            comp.setWidth(this.width);
            super.add(comp);
        } else {
            throw new IllegalArgumentException("Bad object type");
        }
    }

    /**
     * Set the width of the document.
     *
     * @param theWidth the width to set
     */
    public void setColumnWidth(final int theWidth) {

        setWidth(theWidth);

        if (theWidth != this.width) {
            this.width = theWidth;

            // Apply the width to all contained paragraphs
            for (final AbstractDocObjectTemplate child : getChildren()) {
                if (child instanceof DocParagraph || child instanceof DocVSpace) {
                    child.setWidth(theWidth);
                }
            }
        }
    }

//    /**
//     * Tests whether this document column is non-null and references a named parameter.
//     *
//     * @param paramName the parameter name
//     * @return {@code true} if the formulas array is not null and contains at least one formula
//     *         that references the parameter
//     */
//     public boolean referencesParam(final String paramName) {
//
//     final Set<String> names = parameterNames();
//
//     boolean found = false;
//
//     for (final String name : names) {
//     if (name.equals(paramName)) {
//     found = true;
//     break;
//     }
//     }
//
//     return found;
//     }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        int height = 0;

        // Lay out all children (generates paragraph heights).
        super.doLayout(context, mathMode);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.setX(0);
            child.setY(height);
            height += child.getHeight();
            child.setWidth(this.width);
        }

        setWidth(this.width);
        setHeight(height);
    }

    /**
     * Add any parameter names referenced by the object or its children to a list of names. A name should not be added
     * if it already exists on the list.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        accumulateChildrenParameterNames(set);
    }

    /**
     * Draw the object.
     *
     * @param grx the {@code Graphics} to draw to
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
        }

        postPaint(grx);
    }

    /**
     * Generates an iteration of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the iteration document object; null if unable to create the iteration
     */
    @Override
    public DocColumnInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final List<AbstractDocObjectTemplate> templateChildren = getChildren();

        final List<AbstractDocObjectInst> theChildren = new ArrayList<>(templateChildren.size());

        for (final AbstractDocObjectTemplate child : templateChildren) {
            theChildren.add(child.createInstance(evalContext));
        }

        return new DocColumnInst(objStyle, null, theChildren, this.tag);
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = AbstractDocObjectTemplate.makeIndent(indent);

        if (this.tag != null) {
            xml.addln(ind, "<", this.tag, ">");

            for (final AbstractDocObjectTemplate child : getChildren()) {
                child.toXml(xml, indent + 1);
            }

            xml.addln(ind, "</", this.tag, ">");
        } else {
            for (final AbstractDocObjectTemplate child : getChildren()) {
                child.toXml(xml, indent + 1);
            }
        }
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
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll, final HtmlBuilder builder,
                        final boolean showAnswers, final char[] mode, final EvalContext context) {

        if (mode[0] == '$') {
            builder.add('$');
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }

        if (mode[0] == '$') {
            builder.add('$');
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';
    }

    /**
     * Print the document to a {@code PrintStream}, in XML format.
     *
     * @param ps     the {@code PrintStream} to which to print the information
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void print(final PrintStream ps, final int indent) {

        ps.print(toXml(indent));
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        /* Empty */
    }

    /**
     * Print a representation of the document (for diagnostic purposes).
     *
     * @param ps          the PrintStream to which to write the HTML-formatted data
     * @param includeTree true to include a dump of the entire document tree structure
     */
    public void printDiagnostics(final PrintStream ps, final boolean includeTree) {

        ps.print("<table>");

        ps.print("<tr><td>Document of type '<b>");
        ps.print(this.tag);
        ps.print("</b>'</td></tr>");

        for (final AbstractDocObjectTemplate child : getChildren()) {
            ps.print("<tr><td>");
            ps.print(child.toString());
            ps.print("</td></tr>");
        }

        if (includeTree) {
            ps.print("<tr><td><ol>");

            for (final AbstractDocObjectTemplate child : getChildren()) {
                child.printTree(ps);
            }

            ps.print("</ol></td></tr>");
        }

        ps.print("</table>");
    }

    /**
     * Handler for key presses. Keys are propagated to all children, but only children who have focus should react to
     * them.
     *
     * @param keyChar   the key character
     * @param keyCode   the key code
     * @param modifiers modifiers (CTRL, ALT, SHIFT, etc.) to the key press
     * @return true if a change requiring repaint occurred
     */
    @Override
    public boolean processKey(final char keyChar, final int keyCode, final int modifiers, final EvalContext context) {

        boolean repaint = false;
        final boolean shift;
        int sel = 0;
        int count = 0;
        int which;

        if (keyCode == KeyEvent.VK_TAB) {

            if (this.inputs != null) {
                final int numInputs = this.inputs.size();

                // Determine the index of the selected input, and count the
                // number of enabled inputs.
                for (int i = 0; i < numInputs; ++i) {

                    if (this.inputs.get(i).isEnabled()) {
                        count++;
                    }

                    if (this.inputs.get(i).isSelected()) {
                        sel = i;
                    }
                }
            }

            if (count > 0) {
                shift = (modifiers & InputEvent.SHIFT_DOWN_MASK) != 0;
                final int numInputs = this.inputs.size();

                if (shift) {
                    // Select the prior enabled input
                    for (int i = 1; i < numInputs; ++i) {
                        which = sel - i;

                        if (which < 0) {
                            which += numInputs;
                        }

                        if (this.inputs.get(which).isEnabled()) {
                            sel = which;
                            break;
                        }
                    }
                } else {
                    // Select the next enabled input
                    for (int i = 1; i < numInputs; ++i) {
                        which = sel + i;

                        if (which >= numInputs) {
                            which -= numInputs;
                        }

                        if (this.inputs.get(which).isEnabled()) {
                            sel = which;
                            break;
                        }
                    }
                }

                // Set the new selection
                for (int i = 0; i < numInputs; ++i) {
                    this.inputs.get(i).setSelected(i == sel);
                }

                repaint = true;
            }
        }

        // Forward key to all children
        if (super.processKey(keyChar, keyCode, modifiers, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Clear the values of all user inputs.
     */
    public void clearInputs() {

        if (this.inputs != null) {
            for (final AbstractDocInput input : this.inputs) {
                input.clear();
            }
        }
    }

    /**
     * Gets the list of inputs.
     *
     * @return the inputs
     */
    public List<AbstractDocInput> getInputs() {

        return this.inputs;
    }

    /**
     * Get the list of values currently set in the inputs.
     *
     * @return the input values
     */
    public Serializable[] getInputValues() {

        final Serializable[] values;
        Serializable[] nonnulls = null;
        int count = 0;

        if (this.inputs != null) {
            final int numInputs = this.inputs.size();
            values = new Serializable[numInputs];

            for (int i = 0; i < numInputs; ++i) {

                if (this.inputs.get(i) != null) {
                    values[i] = this.inputs.get(i).toString();

                    if ("null".equals(values[i])) {
                        values[i] = null;
                    }

                    if (values[i] != null) {
                        count++;
                    }
                }
            }

            nonnulls = new Serializable[count];

            for (int i = numInputs - 1; i >= 0; --i) {

                if (values[i] != null) {
                    count--;
                    nonnulls[count] = values[i];
                }
            }
        }

        return nonnulls;
    }

    /**
     * Store a list of values in the object's inputs.
     *
     * @param values the list of values
     * @return true if the values could be stored; false if not
     */
    public boolean setInputValues(final Object[] values) {

        boolean result = true;

        if (values != null && this.inputs != null) {

            for (final AbstractDocInput in : this.inputs) {
                boolean searching = true;

                for (final Object o : values) {

                    if (o instanceof String str) {
                        final int open = str.indexOf('{');
                        final int close = str.indexOf('}');

                        if (open == -1 || close == -1 || close < open) {
                            result = false;
                            break;
                        }

                        final String theTag = str.substring(open + 1, close);

                        if (theTag.equals(in.getName())) {

                            if (in instanceof final DocInputCheckbox check) {
                                final String valueStr = str.substring(close + 1);
                                try {
                                    final long value = Long.parseLong(valueStr);
                                    if ((check.value & value) == value) {
                                        str = "{" + theTag + "}=" + valueStr;
                                        if (!in.setValue(str)) {
                                            // Log.warning(" Failed to set value ", str);
                                            result = false;
                                            break;
                                        }
                                    }
                                } catch (final NumberFormatException ex) {
                                    if (!in.setValue(str)) {
                                        // Log.warning(" Failed to set value ", str);
                                        result = false;
                                        break;
                                    }
                                }
                            } else if (!in.setValue(str)) {
                                // Log.warning(" Failed to set value ", str);
                                result = false;
                                break;
                            }

                            searching = false;
                        }
                    } else {
                        if (o == null) {
                            Log.warning("  Value is null rather than expected String");
                        } else {
                            Log.warning("  Value is " + o.getClass().getName(), " rather than expected String");
                        }
                        result = false;
                        break;
                    }
                }

                if (searching) {
                    // Log.warning(" Found no input matching any provided values.");

                    // set an empty value to trigger use of default value
                    in.setValue("{" + in.getName() + "}=");
                }
            }
        }

        return result;
    }

    /**
     * Add a listener that is to be notified when the input's value changes.
     *
     * @param listener the listener to add
     */
    public void addInputChangeListener(final InputChangeListener listener) {

        if (this.inputs != null) {
            for (final AbstractDocInput input : this.inputs) {
                input.addInputChangeListener(listener);
            }
        }
    }

    /**
     * Removes a listener that is to be notified when the input's value changes.
     *
     * @param listener the listener to remove
     */
    public void removeInputChangeListener(final InputChangeListener listener) {

        if (this.inputs != null) {
            for (final AbstractDocInput input : this.inputs) {
                input.removeInputChangeListener(listener);
            }
        }
    }

    /**
     * Rescan the object, rebuilding the list of inputs.
     *
     * @param evalContext    the evaluation context
     * @param resetSelection true to reset the selection to the first input
     */
    public void refreshInputs(final EvalContext evalContext, final boolean resetSelection) {

        final List<AbstractDocInput> newList = new ArrayList<>(5);

        evalContext.accumulateInputs(newList);
        accumulateInputs(newList);

        for (final AbstractDocInput in : newList) {
            if (!this.inputs.contains(in)) {
                this.inputs.add(in);
            }
        }

        if (resetSelection) {
            final int numInputs = this.inputs.size();
            for (int i = 0; i < numInputs; ++i) {
                this.inputs.get(i).setSelected(i == 0);
            }
        }
    }

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     */
    public void setRelativeSize(final int relSize) {

        final float scale = (float) StrictMath.pow(2.5, (double) relSize / 4.0);

        setScale(scale);

        for (final AbstractDocInput input : this.inputs) {
            input.setScale(scale);
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final DocColumn column) {
            equal = innerEquals(column);
        } else {
            equal = false;
        }

        return equal;
    }
}
