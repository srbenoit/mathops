package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveTextInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A text primitive.
 */
final class DocPrimitiveText extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3514394611123605795L;

    /** The x coordinate. */
    private NumberOrFormula xCoord;

    /** The y coordinate. */
    private NumberOrFormula yCoord;

    /** The text anchor point. */
    private ETextAnchor anchor;

    /** The color name. */
    private String colorName;

    /** The color. */
    private Color color;

    /** The highlight color name. */
    private String highlightColorName;

    /** The highlight (background) color. */
    private Color highlightColor;

    /** The value. */
    private String value;

    /** Flag indicating glyph should come from STIX Text. */
    private boolean isStixText;

    /** Flag indicating glyph should come from STIX Math. */
    private boolean isStixMath;

    /** The font name. */
    private String fontName;

    /** The font size. */
    private Double fontSize;

    /** The font style. */
    private Integer fontStyle;

    /** The font. */
    private Font font;

    /** The alpha. */
    private Double alpha;

    /**
     * Construct a new {@code DocPrimitiveText}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveText(final AbstractDocPrimitiveContainer theOwner) {

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
     * Set the anchor point.
     *
     * @param theAnchor the new anchor point
     */
    private void setAnchor(final ETextAnchor theAnchor) {

        this.anchor = theAnchor;
    }

//    /**
//     * Get the anchor point.
//     *
//     * @return the anchor point
//     */
//    public ETextAnchor getAnchor() {
//
//        return this.anchor;
//    }

    /**
     * Gets the color name.
     *
     * @return the color name
     */
    public String getColorName() {

        return this.colorName;
    }

    /**
     * Gets the highlight color name.
     *
     * @return the highlight color name
     */
    public String getHighlightColorName() {

        return this.highlightColorName;
    }

//    /**
//     * Gets the text value.
//     *
//     * @return the value
//     */
//    public String getTextValue() {
//
//        return this.value;
//    }

    /**
     * Gets the font name.
     *
     * @return the font name
     */
    public String getFontName() {

        return this.fontName;
    }

    /**
     * Gets the font size.
     *
     * @return the font size
     */
    public Double getFontSize() {

        return this.fontSize;
    }

//    /**
//     * Gets the font style.
//     *
//     * @return the font style
//     */
//    public Integer getFontStyle() {
//
//        return this.fontStyle;
//    }

    /**
     * Gets the alpha value.
     *
     * @return the alpha value
     */
    public Double getAlpha() {

        return this.alpha;
    }

//    /**
//     * Sets the fill color.
//     *
//     * @param theColorName the name of the fill color
//     */
//    public void setFillColor(final String theColorName) {
//
//        this.colorName = theColorName;
//        this.color = ColorNames.getColor(theColorName);
//    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveText deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveText copy = new DocPrimitiveText(theOwner);

        if (this.xCoord != null) {
            copy.xCoord = this.xCoord.deepCopy();
        }

        if (this.yCoord != null) {
            copy.yCoord = this.yCoord.deepCopy();
        }

        copy.anchor = this.anchor;
        copy.colorName = this.colorName;
        copy.color = this.color;
        copy.highlightColorName = this.highlightColorName;
        copy.highlightColor = this.highlightColor;
        copy.value = this.value;
        copy.isStixText = this.isStixText;
        copy.isStixMath = this.isStixMath;

        copy.fontName = this.fontName;
        copy.fontSize = this.fontSize;
        copy.fontStyle = this.fontStyle;
        copy.font = this.font;
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
        } else if ("x".equals(name)) {
            this.xCoord = parseNumberOrFormula(theValue, elem, mode, "x", "text primitive");
            ok = this.xCoord != null;
        } else if ("y".equals(name)) {
            this.yCoord = parseNumberOrFormula(theValue, elem, mode, "y", "text primitive");
            ok = this.yCoord != null;
        } else if ("anchor".equals(name)) {

            final ETextAnchor anch = ETextAnchor.valueOf(theValue);
            if (anch != null) {
                this.anchor = anch;
                ok = true;
            } else {
                elem.logError("Invalid 'anchor' value (" + theValue + ") on text primitive");
            }
        } else if ("color".equals(name)) {

            if (ColorNames.isColorNameValid(theValue)) {
                this.color = ColorNames.getColor(theValue);
                this.colorName = theValue;
                ok = true;
            } else {
                elem.logError("Invalid 'color' value (" + theValue + ") on text primitive");
            }
        } else if ("highlight".equals(name)) {

            if (ColorNames.isColorNameValid(theValue)) {
                this.highlightColor = ColorNames.getColor(theValue);
                this.highlightColorName = theValue;
                ok = true;
            } else {
                elem.logError("Invalid 'highlight' value (" + theValue + ") on text primitive");
            }
        } else if ("value".equals(name)) {
            this.value = unescape(theValue);

            if (this.value != null &&  this.value.length() == 1) {
                final char ch = this.value.charAt(0);

                this.isStixText = ch == '\u03C0' || ch == '\u03D1' || ch == '\u03D5' || ch == '\u03D6'
                        || ch == '\u03F0' || ch == '\u03F1' || ch == '\u03F5' || ch == '\u2034'
                        || ch == '\u2057';

                this.isStixMath = ch == '\u2218' || ch == '\u221D' || ch == '\u2220' || ch == '\u2221'
                        || ch == '\u2229' || ch == '\u222A' || ch == '\u2243' || ch == '\u2266'
                        || ch == '\u2267' || ch == '\u2268' || ch == '\u2269' || ch == '\u226A'
                        || ch == '\u226B' || ch == '\u226C' || ch == '\u226E' || ch == '\u226F'
                        || ch == '\u2270' || ch == '\u2271' || ch == '\u2272' || ch == '\u2273'
                        || ch == '\u2276' || ch == '\u2277' || ch == '\u227A' || ch == '\u227B'
                        || ch == '\u227C' || ch == '\u227D' || ch == '\u227E' || ch == '\u227F'
                        || ch == '\u2280' || ch == '\u2281' || ch == '\u22D6' || ch == '\u22D7'
                        || ch == '\u22DA' || ch == '\u22DB' || ch == '\u22DE' || ch == '\u22DF'
                        || ch == '\u22E0' || ch == '\u22E1' || ch == '\u22E6' || ch == '\u22E7'
                        || ch == '\u22E8' || ch == '\u22E9' || ch == '\u22EF' || ch == '\u2322'
                        || ch == '\u2323' || ch == '\u2329' || ch == '\u232A' || ch == '\u25B3'
                        || ch == '\u2713' || ch == '\u27CB' || ch == '\u27CD' || ch == '\u27F8'
                        || ch == '\u27F9' || ch == '\u2A7D' || ch == '\u2A7E' || ch == '\u2A85'
                        || ch == '\u2A86' || ch == '\u2A87' || ch == '\u2A88' || ch == '\u2A89'
                        || ch == '\u2A8A' || ch == '\u2A8B' || ch == '\u2A8C' || ch == '\u2A95'
                        || ch == '\u2A96' || ch == '\u2AA1' || ch == '\u2AA2' || ch == '\u2AAF'
                        || ch == '\u2AB0' || ch == '\u2AB5' || ch == '\u2AB6' || ch == '\u2AB7'
                        || ch == '\u2AB8' || ch == '\u2AB9' || ch == '\u2ABA' || ch == '\u2ADB';

            } else {
                this.isStixText = false;
                this.isStixMath = false;
            }

            ok = this.value != null;
        } else if ("fontname".equals(name)) {

            final BundledFontManager fonts = BundledFontManager.getInstance();

            if (fonts.isFontNameValid(theValue)) {
                this.fontName = theValue;
                this.font = null;
                ok = true;
            } else {
                elem.logError("Invalid 'fontname' value (" + theValue + ") on text primitive");
            }
        } else if ("fontsize".equals(name)) {

            try {
                this.fontSize = Double.valueOf(theValue);
                ok = true;
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'fontsize' value (" + theValue + ") on text primitive");
            }
        } else if ("fontstyle".equals(name)) {
            final String lower = theValue.toLowerCase(Locale.ROOT);

            if ("plain".equals(lower)) {
                this.fontStyle = Integer.valueOf(Font.PLAIN);
                ok = true;
            }

            if ("bold".equals(lower)) {
                this.fontStyle = Integer.valueOf(Font.BOLD);
                ok = true;
            }

            if ("italic".equals(lower)) {
                this.fontStyle = Integer.valueOf(Font.ITALIC);
                ok = true;
            }

            if (("bold,italic".equals(lower)) || ("italic,bold".equals(lower))) {
                this.fontStyle = Integer.valueOf(Font.ITALIC | Font.BOLD);
                ok = true;
            }

            if (!ok) {
                elem.logError("Invalid 'fontstyle' value (" + theValue + ") on text primitive");
            }
        } else if ("alpha".equals(name)) {
            this.alpha = parseDouble(theValue, elem, name, "text primitive");
            ok = this.alpha != null;
        } else {
            elem.logError("Unsupported attribute '" + name + "' on text primitive");
        }

        return ok;
    }

    /**
     * Generates the string contents of the text primitive.
     *
     * @param theContext the evaluation context
     * @return the generated string (empty if this primitive has no content)
     */
    private String generateStringContents(final EvalContext theContext) {

        String work;
        AbstractVariable var;

        if (this.value == null) {
            work = CoreConstants.EMPTY;
        } else {
            // Substitute parameter values into text.
            work = this.value;

            boolean changed = true;
            while (changed) {
                final String prior = work;

                for (final String name : theContext.getVariableNames()) {
                    var = theContext.getVariable(name);

                    if (var != null) {
                        final String theValue = var.valueAsString();
                        final String newName = "{" + name + "}";
                        work = work.replace(newName, theValue);
                    }
                }

                changed = !prior.equals(work);
            }
        }

        return work;
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    @Override
    public void draw(final Graphics2D grx, final EvalContext context) {

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

        if (x != null && y != null) {

            final FontSpec spec = new FontSpec();

            if (this.fontName != null) {
                spec.fontName = this.fontName;
            } else if (this.owner.getFontName() != null) {
                spec.fontName = this.owner.getFontName();
            } else {
                spec.fontName = null;
            }

            if (this.fontSize != null) {
                spec.fontSize = this.fontSize.doubleValue() * (double) this.scale;
            } else {
                spec.fontSize = (double) this.owner.getFontSize();
            }

            if (this.fontStyle != null) {
                spec.fontStyle = this.fontStyle.intValue();
            } else {
                spec.fontStyle = this.owner.getFontStyle();
            }

            final BundledFontManager bfm = BundledFontManager.getInstance();
            this.font = bfm.getFont(spec);

            if (this.isStixText) {
                this.font = bfm.getFont("STIX Two Text Regular", (double) this.font.getSize(), this.font.getStyle());
            } else if (this.isStixMath) {
                this.font = bfm.getFont("STIX Two Math Regular", (double) this.font.getSize(), this.font.getStyle());
            }

            grx.setFont(Objects.requireNonNullElseGet(this.font, this.owner::getFont));

            final String str = generateStringContents(context);
            final FontRenderContext frc = grx.getFontRenderContext();
            final GlyphVector vect = grx.getFont().createGlyphVector(frc, str);
            final Rectangle2D visBounds = vect.getVisualBounds();

            float actualX = 0.0f;
            float actualY = 0.0f;
            if (this.anchor == null || this.anchor == ETextAnchor.SW) {
                actualX = x.floatValue() * this.scale;
                actualY = y.floatValue() * this.scale;
            } else {
                if (this.anchor == ETextAnchor.NW) {
                    actualX = x.floatValue() * this.scale;
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight();
                } else if (this.anchor == ETextAnchor.W) {
                    actualX = x.floatValue() * this.scale;
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.N) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight();
                } else if (this.anchor == ETextAnchor.C) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.S) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale;
                } else if (this.anchor == ETextAnchor.NE) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth();
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight();
                } else if (this.anchor == ETextAnchor.E) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth();
                    actualY = y.floatValue() * this.scale + (float) visBounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.SE) {
                    actualX = x.floatValue() * this.scale - (float) visBounds.getWidth();
                    actualY = y.floatValue() * this.scale;
                }
            }

            Composite origComp = null;

            if (this.alpha != null) {
                origComp = grx.getComposite();
                grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) this.alpha.doubleValue()));
            }

            if (this.highlightColor != null) {
                final float actualWidth = (float)(visBounds.getWidth() + 4.0) * this.scale;
                final float actualHeight = (float)(visBounds.getHeight() + 4.0) * this.scale;
                final float topY = actualY + (float) (visBounds.getY() + 2.0) * this.scale;
                final float leftX = actualX - 2.0f * this.scale;
                final Shape scaledBounds = new Rectangle2D.Float(leftX, topY, actualWidth, actualHeight);
                grx.setColor(this.highlightColor);
                grx.fill(scaledBounds);
            }

            if (this.color != null) {
                grx.setColor(this.color);
            } else {
                grx.setColor(Color.BLACK);
            }

            grx.drawString(str, Math.round(actualX), Math.round(actualY));

            if (origComp != null) {
                grx.setComposite(origComp);
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
    public DocPrimitiveTextInst createInstance(final EvalContext evalContext) {

        final Object xVal = this.xCoord.evaluate(evalContext);
        final Object yVal = this.yCoord.evaluate(evalContext);

        final DocPrimitiveTextInst result;

        if (xVal instanceof final Number xNbr && yVal instanceof final Number yNbr) {

            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();
            final float fsize = this.fontSize == null ? 12.0f : this.fontSize.floatValue();
            final int fstyle = this.fontStyle == null ? 0 : this.fontStyle.intValue();

            final DocObjectInstStyle objStyle = new DocObjectInstStyle(this.colorName, this.fontName, fsize, fstyle);

            result = new DocPrimitiveTextInst(xNbr.doubleValue(), yNbr.doubleValue(), this.value,this.anchor,
                    objStyle, alphaValue);
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

        xml.add(ind, "<text");

        if (this.xCoord != null && this.xCoord.getNumber() != null) {
            xml.add(" x=\"", this.xCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.yCoord != null && this.yCoord.getNumber() != null) {
            xml.add(" y=\"", this.yCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.anchor != null && this.anchor != ETextAnchor.SW) {
            xml.add(" anchor=\"", this.anchor.name(), CoreConstants.QUOTE);
        }

        if (this.colorName != null) {
            xml.add(" color=\"", this.colorName, CoreConstants.QUOTE);
        }

        if (this.value != null) {
            xml.add(" value=\"", XmlEscaper.escape(this.value), CoreConstants.QUOTE);
        }

        if (this.fontName != null) {
            xml.add(" fontname=\"", this.fontName, CoreConstants.QUOTE);
        }

        if (this.fontSize != null) {
            xml.add(" fontsize=\"", this.fontSize, CoreConstants.QUOTE);
        }

        if (this.fontStyle != null) {
            xml.add(" fontstyle=\"");

            switch (this.fontStyle.intValue()) {

                case 0:
                    xml.add("plain");
                    break;

                case Font.BOLD:
                    xml.add("bold");
                    break;

                case Font.ITALIC:
                    xml.add("italic");
                    break;

                case Font.BOLD | Font.ITALIC:
                    xml.add("bold,italic");
                    break;

                default:
                    break;
            }

            xml.add('"');
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if ((this.xCoord == null || this.xCoord.getFormula() == null)
                && (this.yCoord == null || this.yCoord.getFormula() == null)) {
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

            xml.addln(ind, "</text>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Text";
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

        if (this.value != null) {
            AbstractDocObjectTemplate.scanStringForParameterReferences(this.value, set);
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
                + Objects.hashCode(this.anchor)
                + Objects.hashCode(this.colorName)
                + Objects.hashCode(this.color)
                + Objects.hashCode(this.highlightColorName)
                + Objects.hashCode(this.highlightColor)
                + Objects.hashCode(this.value)
                + Objects.hashCode(this.fontName)
                + Objects.hashCode(this.fontSize)
                + Objects.hashCode(this.fontStyle)
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
        } else if (obj instanceof final DocPrimitiveText text) {
            equal = Objects.equals(this.xCoord, text.xCoord)
                    && Objects.equals(this.yCoord, text.yCoord)
                    && this.anchor == text.anchor
                    && Objects.equals(this.colorName, text.colorName)
                    && Objects.equals(this.color, text.color)
                    && Objects.equals(this.highlightColorName, text.highlightColorName)
                    && Objects.equals(this.highlightColor, text.highlightColor)
                    && Objects.equals(this.value, text.value)
                    && Objects.equals(this.fontName, text.fontName)
                    && Objects.equals(this.fontSize, text.fontSize)
                    && Objects.equals(this.fontStyle, text.fontStyle)
                    && Objects.equals(this.alpha, text.alpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
