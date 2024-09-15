package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A document object that may contain primitives.
 */
public abstract class AbstractPrimitiveContainerInst extends AbstractDocObjectInst {

    /** The width of the canvas. */
    private final int width;

    /** The height of the canvas. */
    private final int height;

    /** The coordinate systems in which coordinates can be specified. */
    private final CoordinateSystems coordinates;

    /** The alt text for generated images. */
    private final String altText;

    /** The border for the graph ({@code null} if there is no border). */
    private final StrokeStyle border;

    /** The list of primitives that make up a drawing. */
    private final List<AbstractPrimitiveInst> primitives;

    /**
     * Construct a new {@code AbstractPrimitiveContainerInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theWidth       the width of the object
     * @param theHeight      the height of the object
     * @param theCoordinates the coordinate systems in which coordinates can be specified
     * @param theAltText     the alternative text for accessibility of generated images
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param thePrimitives  the list of primitives
     */
    AbstractPrimitiveContainerInst(final DocObjectInstStyle theStyle, final String theBgColorName, final int theWidth,
                                   final int theHeight, final CoordinateSystems theCoordinates,
                                   final String theAltText, final StrokeStyle theBorder,
                                   final List<? extends AbstractPrimitiveInst> thePrimitives) {

        super(theStyle, theBgColorName);

        this.width = theWidth;
        this.height = theHeight;
        this.coordinates = theCoordinates;
        this.altText = theAltText;
        this.border = theBorder;
        this.primitives = new ArrayList<>(thePrimitives);
    }

    /**
     * Gets the width of the object.
     *
     * @return the width
     */
    public final int getWidth() {

        return this.width;
    }

    /**
     * Gets the height of the object.
     *
     * @return the height
     */
    public final int getHeight() {

        return this.height;
    }

    /**
     * Gets the coordinate systems in which coordinates can be specified.
     *
     * @return the coordinate systems
     */
    public final CoordinateSystems getCoordinates() {

        return this.coordinates;
    }

    /**
     * Gets the alternative text for accessible generated images.
     *
     * @return the alternative text (could be {@code null})
     */
    public final String getAltText() {

        return this.altText;
    }

    /**
     * Gets the border specification.
     *
     * @return the border specification; {@code null} if there is no border
     */
    public final StrokeStyle getBorder() {

        return this.border;
    }

    /**
     * Get the number of primitives this object contains.
     *
     * @return the number of primitives
     */
    public final int numPrimitives() {

        return this.primitives.size();
    }

    /**
     * Get an unmodifiable view of the list of primitives.
     *
     * @return the list of primitives
     */
    public final List<AbstractPrimitiveInst> getPrimitives() {

        return Collections.unmodifiableList(this.primitives);
    }

    /**
     * Add XML attributes specific to input fields to an XML block.
     *
     * @param xml the {@code HtmlBuilder} to which to append the XML attributes
     */
    final void addPrimitiveContainerInstXmlAttributes(final HtmlBuilder xml) {

        xml.addAttribute("width", Integer.toString(this.width), 0);
        xml.addAttribute("height", Integer.toString(this.height), 0);
        xml.addAttribute("alt", this.altText, 0);
        if (this.border != null) {
            this.border.appendXmlAttributes(xml, "border");
        }

        addDocObjectInstXmlAttributes(xml);
    }

    /**
     * Adds the XML representations for all primitives to a {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent each child
     */
    final void appendPrimitivesXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        for (final AbstractPrimitiveInst primitive : this.primitives) {
            primitive.toXml(xml, xmlStyle, indent);
        }
    }

    /**
     * Adds style, width, height, background, and border information as part of string representation generation.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     */
    final void appendPrimitiveContainerString(final HtmlBuilder builder) {

        appendStyleString(builder);
        builder.add("{width=", Integer.toString(this.width), ",height=", Integer.toString(this.height));
        if (this.altText != null) {
            builder.add(",alt='", this.altText, "'");
        }
        builder.add('}');
        if (this.border != null) {
            builder.add(this.border.toString());
        }
    }

    /**
     * Generates an integer hash code for the style settings, width, height, and children that can be used when
     * calculating the hash code for a subclass of this class.
     *
     * @return the hash code of the object's style settings, width, height, and children
     */
    final int docPrimitiveContainerInstHashCode() {

        return docObjectInstHashCode() + this.width + this.height
               + Objects.hashCode(this.coordinates)
               + Objects.hashCode(this.altText)
               + Objects.hashCode(this.border)
               + Objects.hashCode(this.primitives);
    }

    /**
     * Checks whether the style settings, width, height, and list of children in a given object are equal to those in
     * this object.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the style settings, width, height, and children from the objects are equal; {@code false}
     *         otherwise
     */
    final boolean checkDocPrimitiveContainerInstEquals(final AbstractPrimitiveContainerInst obj) {

        return checkDocObjectInstEquals(obj)
               && this.width == obj.width
               && this.height == obj.height
               && Objects.equals(this.coordinates, obj.coordinates)
               && Objects.equals(this.altText, obj.altText)
               && Objects.equals(this.border, obj.border)
               && Objects.equals(this.primitives, obj.primitives);
    }
}
