package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.inst.DocPrimitiveRasterInst;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.XmlEscaper;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

/**
 * A raster image.
 */
final class DocPrimitiveRaster extends AbstractDocRectangleShape {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5269770064156882137L;

    /** The text contents of the object. */
    private BufferedImage image;

    /** The URL from which to load the image. */
    private URL source;

    /** The alpha. */
    private Double alpha;

    /**
     * Construct a new {@code DocPrimitiveRaster}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveRaster(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

    /**
     * Gets the source URL.
     *
     * @return the source URL
     */
    public URL getSource() {

        return this.source;
    }

    /**
     * Get the image.
     *
     * @return the image
     */
    public BufferedImage getImage() {

        return this.image;
    }

    /**
     * Set the source {@code URL}, reloading the image.
     *
     * @param theSource the new source {@code URL}
     */
    public void setSource(final URL theSource) {

        this.source = theSource;
        loadImage();
    }

    /**
     * Load the image from the source file.
     */
    private void loadImage() {

        // Flush the existing image if it exists
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }

        if (this.source != null) {
            try {
                this.image = ImageIO.read(this.source);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Gets the alpha value.
     *
     * @return the alpha value
     */
    public Double getAlpha() {

        return this.alpha;
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveRaster deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveRaster copy = new DocPrimitiveRaster(theOwner);

        final RectangleShapeTemplate myShape = getShape();
        if (myShape != null) {
            final RectangleShapeTemplate myShapeCopy = myShape.deepCopy();
            copy.setShape(myShapeCopy);
        }

        copy.source = this.source;
        copy.image = this.image;
        copy.alpha = this.alpha;

        copy.scale = this.scale;

        return copy;
    }

    /**
     * Set an attribute value used in drawing.
     *
     * @param name     the name of the attribute
     * @param theValue the attribute value
     * @param elem     an element to which to log errors
     * @param mode     the parser mode
     * @return true if the attribute was valid; false otherwise
     */
    boolean setAttr(final String name, final String theValue, final INode elem, final EParserMode mode) {

        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else {
            switch (name) {
                case "src" -> {
                    try {
                        final URI uri = new URI(theValue);
                        this.source = uri.toURL();

                        loadImage();
                        if (this.image == null) {
                            elem.logError("Unable to load image from URL '" + name + "' on raster primitive");
                        } else {
                            ok = true;
                        }
                    } catch (final MalformedURLException | URISyntaxException ex) {
                        elem.logError("Invalid source URL '" + name + "' on raster primitive");
                    }
                }
                case "alpha" -> {
                    this.alpha = parseDouble(theValue, elem, name, "raster primitive");
                    ok = this.alpha != null;
                }
                case null, default -> elem.logError("Unsupported attribute '" + name + "' on raster primitive");
            }
        }

        return ok;
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    @Override
    public void draw(final Graphics2D grx, final EvalContext context) {

        if (this.image != null) {
            final Rectangle2D bounds = getBoundsRect(context);

            if (bounds != null) {
                Composite origComp = null;

                if (this.alpha != null) {
                    origComp = grx.getComposite();
                    grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                            (float) this.alpha.doubleValue()));
                }

                double x = bounds.getX();
                double y = bounds.getY();
                double width = bounds.getWidth();
                double height = bounds.getHeight();

                if (width < 0.0) {
                    x += width;
                    width = -width;
                }
                if (height < 0.0) {
                    y += height;
                    height = -height;
                }

                final int x1 = Math.round((float) x * this.scale);
                final int y1 = Math.round((float) y * this.scale);
                final int x2 = x1 + Math.round((float) width * this.scale);
                final int y2 = y1 + Math.round((float) height * this.scale);

                grx.drawImage(this.image, x1, y1, x2, y2, 0, 0, this.image.getWidth(), this.image.getHeight(), null);

                if (origComp != null) {
                    grx.setComposite(origComp);
                }
            }
        }
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context) {

        // No action
    }

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
    @Override
    public DocPrimitiveRasterInst createInstance(final EvalContext evalContext) {

        final RectangleShapeTemplate shape = getShape();
        DocPrimitiveRasterInst result = null;

        if (Objects.nonNull(shape)) {
            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            final RectangleShapeInst shapeInst = getShape().createInstance(evalContext);
            result = new DocPrimitiveRasterInst(shapeInst, this.source.toExternalForm(), alphaValue);
        }

        return result;
    }

    /**
     * Write the XML representation of the object to a {@code v}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<raster");

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.addAttributes(xml);
        }

        if (this.source != null) {
            final String src = this.source.toExternalForm();
            xml.add(" src=\"", XmlEscaper.escape(src), CoreConstants.QUOTE);
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if (shape == null || shape.isConstant()) {
            xml.addln("/>");
        } else {
            xml.addln(">");
            shape.addChildElements(xml, indent + 1);
            xml.addln(ind, "</raster>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Raster";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.accumulateParameterNames(set);
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(getShape())
               + Objects.hashCode(this.source)
               + Objects.hashCode(this.alpha);
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
        } else if (obj instanceof final DocPrimitiveRaster raster) {
            equal = Objects.equals(getShape(), raster.getShape())
                    &&  Objects.equals(this.source, raster.source)
                    && Objects.equals(this.alpha, raster.alpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
