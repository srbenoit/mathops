package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.inst.AbstractPrimitiveInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.builder.HtmlBuilder;

import java.awt.Graphics2D;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * A primitive used in creating a drawing or graph.
 */
abstract class AbstractDocPrimitive implements Serializable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2306985975971639784L;

    /** The owning drawing. */
    final AbstractDocPrimitiveContainer owner;

    /** The scale. */
    public float scale = 1.0f;

    /**
     * Construct a new {@code AbstractDocDrawingPrimitive}.
     *
     * @param theOwner the object that owns this primitive
     */
    AbstractDocPrimitive(final AbstractDocPrimitiveContainer theOwner) {

        this.owner = theOwner;
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    public abstract AbstractDocPrimitive deepCopy(AbstractDocPrimitiveContainer theOwner);

    /**
     * Sets the scale.
     *
     * @param theScale the new scale
     */
    void setScale(final float theScale) {

        this.scale = theScale;
    }

    /**
     * Gets the scale.
     *
     * @return the scale
     */
    public final float getScale() {

        return this.scale;
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    public abstract void draw(Graphics2D grx, EvalContext context);

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    public abstract void doLayout(EvalContext context);

    /**
     * Generates an instance of this primitive based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance primitive object; null if unable to create the instance
     */
    public abstract AbstractPrimitiveInst createInstance(EvalContext evalContext);

    /**
     * Write the XML representation of the object to a {@code v}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    public abstract void toXml(HtmlBuilder xml, int indent);

    /**
     * Convert any Unicode escape sequences found in a string into their corresponding special characters.
     *
     * @param value the string to convert
     * @return the converted string, or null on any error
     */
    static String unescape(final String value) {

        String converted = value;

        int escape = converted.indexOf("\\u");

        while (escape != -1) {
            if (escape >= (converted.length() - 5)) {
                converted = null;
                break;
            }

            try {
                final char ch =
                        (char) Integer.parseInt(converted.substring(escape + 2, escape + 6), 16);

                converted = converted.substring(0, escape) + Character.valueOf(ch)
                        + converted.substring(escape + 6);

                escape = converted.indexOf("\\u");
            } catch (final NumberFormatException e) {
                converted = null;
                break;
            }
        }

        return converted;
    }

    /**
     * Create a string for a particular indentation level.
     *
     * @param indent The number of spaces to indent.
     * @return A string with the requested number of spaces.
     */
    static String makeIndent(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(indent);

        for (int i = 0; i < indent; ++i) {
            builder.add(' ');
        }

        return builder.toString();
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    public abstract void accumulateParameterNames(Set<String> set);

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public abstract int hashCode();

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param other  the other object
     * @param indent the indent level
     */
    public abstract void whyNotEqual(Object other, int indent);
}
