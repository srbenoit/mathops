package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.variable.EvalContext;

import java.awt.geom.Rectangle2D;
import java.io.Serial;

/**
 * A base class for primitives that are specified by a rectangular shape.
 */
abstract class AbstractDocRectangleShape extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4095958485891665915L;

    /** The rectangle shape. */
    private RectangleShapeTemplate shape = null;

    /**
     * Construct a new {@code AbstractDocRectangleShape}.
     *
     * @param theOwner the object that owns this primitive
     */
    AbstractDocRectangleShape(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

    /**
     * Determines the bounding rectangle from attribute settings.
     *
     * @param context the evaluation context
     * @return the bounding rectangle; {@code null} if that rectangle could not be determined
     */
    Rectangle2D getBoundsRect(final EvalContext context) {

        final CoordinateSystems coordinates = this.owner.getCoordinates();

        return this.shape == null ? null : this.shape.getBoundsRect(context, coordinates);
    }

    /**
     * Sets the shape.
     *
     * @param theShape the shape
     */
    final void setShape(final RectangleShapeTemplate theShape) {

        this.shape = theShape;
    }

    /**
     * Gets the shape.
     *
     * @return the shape
     */
    public final RectangleShapeTemplate getShape() {

        return this.shape;
    }
}
