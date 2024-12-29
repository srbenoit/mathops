package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A document object that may contain primitives.
 */
public abstract class AbstractDocPrimitiveContainer extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2417177152567410545L;

    /** The original width. */
    int origWidth;

    /** The original height. */
    int origHeight;

    /** The coordinate systems in which coordinates can be specified. */
    private CoordinateSystems coordinates;

    /** The list of primitives that make up a drawing. */
    private final List<AbstractDocPrimitive> primitives;

    /** The off-screen buffer to which to draw the graph. */
    private BufferedImage offscreen;

    /** The alternative text for the generated image for accessibility. */
    private final String altText;

    /**
     * Construct a new {@code AbstractDocPrimitiveContainer}.
     *
     * @param width      the width of the object
     * @param height     the height of the object
     * @param theAltText the alternative text for the generated image for accessibility
     */
    AbstractDocPrimitiveContainer(final int width, final int height, final String theAltText) {

        super();

        this.origWidth = width;
        this.origHeight = height;
        setWidth(width);
        setHeight(height);
        this.primitives = new ArrayList<>(3);

        final Integer widthObj = Integer.valueOf(width);
        final Integer heightObj = Integer.valueOf(height);
        final Integer borderObj = Integer.valueOf(0);
        this.coordinates = new CoordinateSystems(widthObj, heightObj, borderObj);

        this.altText = theAltText;
    }

    /**
     * Gets the alt text.
     *
     * @return the alt text
     */
    public final String getAltText() {

        return this.altText;
    }

    /**
     * Sets the "original" (pre-scaled) size.
     *
     * @param newOrigWidth  the new "original" width
     * @param newOrigHeight the new "original" height
     */
    final void setOrigSize(final int newOrigWidth, final int newOrigHeight) {

        this.origWidth = newOrigWidth;
        this.origHeight = newOrigHeight;
    }

    /**
     * Gets the off-screen image.
     *
     * @return the off-screen image
     */
    public final BufferedImage getOffscreen() {

        return this.offscreen;
    }

    /**
     * Sets the "coordinate systems in which coordinates can be specified.
     *
     * @param theCoordinates the new coordinate systems
     */
    public final void setCoordinates(final CoordinateSystems theCoordinates) {

        if (theCoordinates != null) {
            this.coordinates = theCoordinates;
        }
    }

    /**
     * Gets the "coordinate systems in which coordinates can be specified.
     *
     * @return the coordinate systems
     */
    public final CoordinateSystems getCoordinates() {

        return this.coordinates;
    }

    /**
     * Add a primitive to the drawing.
     *
     * @param primitive the primitive to add
     */
    final void addPrimitive(final AbstractDocPrimitive primitive) {

        this.primitives.add(primitive);
    }

    /**
     * Gets the list of primitives.
     *
     * @return the list of primitives
     */
    final List<AbstractDocPrimitive> getPrimitives() {

        return this.primitives;
    }

    /**
     * Draw the graph.
     *
     * @param grx the {@code Graphics} object to which to draw the graph
     */
    @Override
    public final void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        if (this.offscreen != null) {
            prePaint(grx);
            innerPaintComponent(grx);
            grx.drawImage(this.offscreen, 0, 0, null);
            postPaint(grx);
        }
    }

    /**
     * Ensures the off-screen image exists and matches the current size.
     *
     * @param width  the new image width
     * @param height the new image height
     */
    final void innerCreateOffscreen(final int width, final int height) {

        if (this.offscreen == null || (this.offscreen.getWidth() != width)
            || (this.offscreen.getHeight() != height)) {
            this.offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }

    /**
     * Draw the graph to an off-screen image.
     *
     * @param forceWhite true to force background rectangle to be white if it is the first primitive in the drawing and
     *                   it is filled
     * @param context    the evaluation context
     */
    public abstract void buildOffscreen(boolean forceWhite, EvalContext context);

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final float scale = getScale();
        setWidth((int) ((float) this.origWidth * scale));
        setHeight((int) ((float) this.origHeight * scale));

        final int height = getHeight();
        setBaseLine(height);
        setCenterLine(height / 2);

        // Do layout on drawing primitives (only affects spans for now)
        for (final AbstractDocPrimitive p : this.primitives) {
            p.scale = scale;
            p.doLayout(context);
        }

        buildOffscreen(false, context);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        if (this.altText != null) {
            scanStringForParameterReferences(this.altText, set);
        }

        for (final AbstractDocPrimitive prim : this.primitives) {
            prim.accumulateParameterNames(set);
        }
    }

    /**
     * Appends the XML representation of all primitives.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the number of spaces to indent the printout
     */
    final void appendPrimitivesXml(final HtmlBuilder builder, final int indent) {

        // Print contents (drawing primitives and functions with attributes)
        for (final AbstractDocPrimitive prim : this.primitives) {
            prim.toXml(builder, indent);
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
    public final void toLaTeX(final File dir, final int[] fileIndex,
                              final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                              final char[] mode, final EvalContext context) {

        buildOffscreen(true, context);

        // First, write the graphics image as a PNG file.
        final String theName = "image_" + fileIndex[0] + ".png";
        final File file = new File(dir, theName);

        // if (f.exists() && (!overwriteAll[0])) {
        // TODO: Ask user if they want to overwrite the existing file
        // }

        try {
            ImageIO.write(this.offscreen, "PNG", file);
            fileIndex[0]++;

            // Now tell the LaTeX file to include the image.
            if (mode[0] == '$') {
                builder.add('$');
            } else if (mode[0] == 'M') {
                builder.add("\\]");
            }

            mode[0] = 'T';

            if (mode[1] != 'T') {
                builder.add("\\begin{center}\n");
            }

            builder.add("\\includegraphics[width=",
                    Integer.toString(this.offscreen.getWidth() / 2), "pt,height=",
                    Integer.toString(this.offscreen.getHeight() / 2), "pt]{",
                    theName, "}\n");

            if (mode[1] != 'T') {
                builder.add("\\end{center}\n");
            }
        } catch (final IOException e) {
            // TODO: Tell the user we could not write the file
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int primitiveContainerHashCode() {

        return innerHashCode() + this.coordinates.hashCode() + this.origWidth + (this.origHeight << 8)
               + Objects.hashCode(this.altText);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean primitiveContainerEquals(final AbstractDocPrimitiveContainer obj) {

        return innerEquals(obj)
               && this.coordinates.equals(obj.getCoordinates())
               && this.origWidth == obj.origWidth
               && this.origHeight == obj.origHeight
               && Objects.equals(this.altText, obj.altText);
    }
}
