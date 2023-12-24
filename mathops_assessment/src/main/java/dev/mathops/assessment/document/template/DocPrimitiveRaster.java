package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.inst.DocPrimitiveRasterInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.XmlEscaper;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

/**
 * A raster image.
 */
final class DocPrimitiveRaster extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5269770064156882137L;

    /** The x coordinate. */
    private NumberOrFormula xCoord;

    /** The y coordinate. */
    private NumberOrFormula yCoord;

    /** The width. */
    private NumberOrFormula width;

    /** The height. */
    private NumberOrFormula height;

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
     * Sets the x coordinate.
     *
     * @param theXCoord the x coordinate
     */
    void setXCoord(final NumberOrFormula theXCoord) {

        this.xCoord = theXCoord;
    }

//    /**
//     * Gets the x coordinate.
//     *
//     * @return the x coordinate
//     */
//    public NumberOrFormula getXCoordConstant() {
//
//        return this.xCoord;
//    }

    /**
     * Sets the y coordinate.
     *
     * @param theYCoord the y coordinate
     */
    void setYCoord(final NumberOrFormula theYCoord) {

        this.yCoord = theYCoord;
    }

//    /**
//     * Gets the y coordinate.
//     *
//     * @return the y coordinate
//     */
//    public NumberOrFormula getYCoord() {
//
//        return this.yCoord;
//    }

    /**
     * Sets the width.
     *
     * @param theWidth the width
     */
    public void setWidth(final NumberOrFormula theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public NumberOrFormula getWidth() {

        return this.width;
    }

    /**
     * Sets the height.
     *
     * @param theHeight the height
     */
    public void setHeight(final NumberOrFormula theHeight) {

        this.height = theHeight;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public NumberOrFormula getHeight() {

        return this.height;
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

        if (this.xCoord != null) {
            copy.xCoord = this.xCoord.deepCopy();
        }

        if (this.yCoord != null) {
            copy.yCoord = this.yCoord.deepCopy();
        }

        if (this.width != null) {
            copy.width = this.width.deepCopy();
        }

        if (this.height != null) {
            copy.height = this.height.deepCopy();
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
            if ("x".equals(name)) {
                this.xCoord = parseNumberOrFormula(theValue, elem, mode, "x", "raster primitive");
                ok = this.xCoord != null;
            } else if ("y".equals(name)) {
                this.yCoord = parseNumberOrFormula(theValue, elem, mode, "y", "raster primitive");
                ok = this.yCoord != null;
            } else if ("width".equals(name)) {
                this.width = parseNumberOrFormula(theValue, elem, mode, "width", "raster primitive");
                ok = this.width != null;
            } else if ("height".equals(name)) {
                this.height = parseNumberOrFormula(theValue, elem, mode, "height", "raster primitive");
                ok = this.height != null;
            } else if ("src".equals(name)) {
                try {
                    this.source = new URL(theValue);
                    loadImage();
                    if (this.image == null) {
                        elem.logError("Unable to load image from URL '" + name + "' on raster primitive");
                    } else {
                        ok = true;
                    }
                } catch (final MalformedURLException ex) {
                    elem.logError("Invalid source URL '" + name + "' on raster primitive");
                }
            } else if ("alpha".equals(name)) {
                this.alpha = parseDouble(theValue, elem, name, "raster primitive");
                ok = this.alpha != null;
            } else {
                elem.logError("Unsupported attribute '" + name + "' on raster primitive");
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
            Object result;

            // Evaluate formulae
            Long x = null;
            if (this.xCoord != null) {
                result = this.xCoord.evaluate(context);

                if (result instanceof final Long longResult) {
                    x = longResult;
                } else if (result instanceof final Number numResult) {
                    x = Long.valueOf(Math.round(numResult.doubleValue()));
                }
            }

            Long y = null;
            if (this.yCoord != null) {
                result = this.yCoord.evaluate(context);

                if (result instanceof final Long longResult) {
                    y = longResult;
                } else if (result instanceof final Number numResult) {
                    y = Long.valueOf(Math.round(numResult.doubleValue()));
                }
            }

            Long w = null;
            if (this.width != null) {
                result = this.width.evaluate(context);

                if (result instanceof final Long longResult) {
                    w = longResult;
                } else if (result instanceof final Number numResult) {
                    w = Long.valueOf(Math.round(numResult.doubleValue()));
                }
            }

            Long h = null;
            if (this.height != null) {
                result = this.height.evaluate(context);

                if (result instanceof final Long longResult) {
                    h = longResult;
                } else if (result instanceof final Number numResult) {
                    h = Long.valueOf(Math.round(numResult.doubleValue()));
                }
            }

            if (x != null && y != null && w != null && h != null) {
                Composite origComp = null;

                if (this.alpha != null) {
                    origComp = grx.getComposite();
                    grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                            (float) this.alpha.doubleValue()));
                }

                final int x1 = Math.round(x.floatValue() * this.scale);
                final int y1 = Math.round(y.floatValue() * this.scale);
                final int x2 = x1 + Math.round(w.floatValue() * this.scale);
                final int y2 = y1 + Math.round(h.floatValue() * this.scale);

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

        final Object xVal = this.xCoord == null ? null : this.xCoord.evaluate(evalContext);
        final Object yVal = this.yCoord == null ? null : this.yCoord.evaluate(evalContext);
        final Object wVal = this.width == null ? null : this.width.evaluate(evalContext);
        final Object hVal = this.height == null ? null : this.height.evaluate(evalContext);

        final DocPrimitiveRasterInst result;

        if (xVal instanceof final Number xNbr && yVal instanceof final Number yNbr
                && wVal instanceof final Number wNbr && hVal instanceof final Number hNbr) {

            final BoundingRect bounds = new BoundingRect(xNbr.doubleValue(), yNbr.doubleValue(),
                    wNbr.doubleValue(), hNbr.doubleValue());
            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            result = new DocPrimitiveRasterInst(bounds, this.source.toExternalForm(), alphaValue);
        } else {
            result = null;
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

        if (this.xCoord != null && this.xCoord.getNumber() != null) {
            xml.add(" x=\"", this.xCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.yCoord != null && this.yCoord.getNumber() != null) {
            xml.add(" y=\"", this.yCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.width != null && this.width.getNumber() != null) {
            xml.add(" width=\"", this.width.getNumber(), CoreConstants.QUOTE);
        }

        if (this.height != null && this.height.getNumber() != null) {
            xml.add(" height=\"", this.height.getNumber(), CoreConstants.QUOTE);
        }

        if (this.source != null) {
            final String src = this.source.toExternalForm();
            xml.add(" src=\"", XmlEscaper.escape(src), CoreConstants.QUOTE);
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if ((this.xCoord == null || this.xCoord.getFormula() == null)
                && (this.yCoord == null || this.yCoord.getFormula() == null)
                && (this.width == null || this.width.getFormula() == null)
                && (this.height == null || this.height.getFormula() == null)) {
            xml.addln("/>");
        } else {
            xml.addln(">");

            if (this.xCoord != null && this.xCoord.getFormula() != null) {
                xml.add(ind2, "<x>");
                this.xCoord.getFormula().appendChildrenXml(xml);
                xml.addln("</x>");
            }

            if (this.yCoord != null && this.yCoord.getFormula() != null) {
                xml.add(ind2, "<y>");
                this.yCoord.getFormula().appendChildrenXml(xml);
                xml.addln("</y>");
            }

            if (this.width != null && this.width.getFormula() != null) {
                xml.add(ind2, "<width>");
                this.width.getFormula().appendChildrenXml(xml);
                xml.addln("</width>");
            }

            if (this.height != null && this.height.getFormula() != null) {
                xml.add(ind2, "<height>");
                this.height.getFormula().appendChildrenXml(xml);
                xml.addln("</height>");
            }

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

        if (this.xCoord != null && this.xCoord.getFormula() != null) {
            set.addAll(this.xCoord.getFormula().params.keySet());
        }

        if (this.yCoord != null && this.yCoord.getFormula() != null) {
            set.addAll(this.yCoord.getFormula().params.keySet());
        }

        if (this.width != null && this.width.getFormula() != null) {
            set.addAll(this.width.getFormula().params.keySet());
        }

        if (this.height != null && this.height.getFormula() != null) {
            set.addAll(this.height.getFormula().params.keySet());
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.xCoord)
                + Objects.hashCode(this.yCoord)
                + Objects.hashCode(this.width)
                + Objects.hashCode(this.height)
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
            equal = Objects.equals(this.xCoord, raster.xCoord)
                    && Objects.equals(this.yCoord, raster.yCoord)
                    && Objects.equals(this.width, raster.width)
                    && Objects.equals(this.height, raster.height)
                    && Objects.equals(this.source, raster.source)
                    && Objects.equals(this.alpha, raster.alpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
