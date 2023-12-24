package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.inst.DocNonwrappingSpanInst;
import dev.mathops.assessment.document.inst.DocPrimitiveSpanInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.Serial;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A span primitive.
 */
final class DocPrimitiveSpan extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7857281241941136984L;

    /** The x coordinate. */
    private NumberOrFormula xCoord;

    /** The y coordinate. */
    private NumberOrFormula yCoord;

    /** The text anchor point. */
    private ETextAnchor anchor;

    /** The filled flag. */
    private Boolean filled;

    /** The color name. */
    private String colorName;

    /** The color. */
    private Color color;

    /**
     * The span (we use a non-wrapping span here rather than a DocSimpleSpan since the latter is not able to do its own
     * layout).
     */
    private DocNonwrappingSpan span;

    /** The font name. */
    private String fontName;

    /** The font size. */
    private Float fontSize;

    /** The font style. */
    private Integer fontStyle;

    /** The font. */
    private Font font;

    /** The alpha. */
    private Double alpha;

    /**
     * Construct a new {@code DocPrimitiveSpan}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveSpan(final AbstractDocPrimitiveContainer theOwner) {

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
     * Gets the filled flag.
     *
     * @return the flag
     */
    public boolean isFilled() {

        return Boolean.TRUE.equals(this.filled);
    }

    /**
     * Gets the color name.
     *
     * @return the color name
     */
    public String getColorName() {

        return this.colorName;
    }

//    /**
//     * Gets the span value.
//     *
//     * @return the value
//     */
//    public DocNonwrappingSpan getSpanValue() {
//
//        return this.span;
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
    public Float getFontSize() {

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
    public DocPrimitiveSpan deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveSpan copy = new DocPrimitiveSpan(theOwner);

        if (this.xCoord != null) {
            copy.xCoord = this.xCoord.deepCopy();
        }

        if (this.yCoord != null) {
            copy.yCoord = this.yCoord.deepCopy();
        }

        copy.anchor = this.anchor;
        copy.filled = this.filled;
        copy.colorName = this.colorName;
        copy.color = this.color;

        if (this.span != null) {
            copy.span = this.span.deepCopy();
        }

        copy.fontName = this.fontName;
        copy.fontSize = this.fontSize;
        copy.fontStyle = this.fontStyle;
        copy.font = this.font;
        copy.alpha = this.alpha;

        copy.setScale(this.scale);

        return copy;
    }

    /**
     * Sets the span.
     *
     * @param theSpan the span
     */
    public void setSpan(final DocNonwrappingSpan theSpan) {

        this.span = theSpan;
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

        final BundledFontManager fonts;
        final String lower;
        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else {
            if ("x".equals(name)) {
                this.xCoord = parseNumberOrFormula(theValue, elem, mode, "x", "span primitive");
                ok = this.xCoord != null;
            } else if ("y".equals(name)) {
                this.yCoord = parseNumberOrFormula(theValue, elem, mode, "y", "span primitive");
                ok = this.yCoord != null;
            } else if ("anchor".equals(name)) {

                final ETextAnchor anch = ETextAnchor.valueOf(theValue);
                if (anch != null) {
                    this.anchor = anch;
                    ok = true;
                } else {
                    elem.logError("Invalid 'anchor' value (" + theValue + ") on text primitive");
                }
            } else if ("filled".equals(name)) {

                try {
                    this.filled = VariableFactory.parseBooleanValue(theValue);
                    ok = true;
                } catch (final IllegalArgumentException e) {
                    elem.logError("Invalid 'filled' value (" + theValue + ") on span primitive");
                }
            } else if ("color".equals(name)) {

                if (ColorNames.isColorNameValid(theValue)) {
                    this.color = ColorNames.getColor(theValue);
                    this.colorName = theValue;
                    ok = true;
                } else {
                    elem.logError("Invalid 'color' value (" + theValue + ") on span primitive");
                }
            } else if ("fontname".equals(name)) {

                fonts = BundledFontManager.getInstance();

                if (fonts.isFontNameValid(theValue)) {
                    this.fontName = theValue;
                    this.font = null;
                    ok = true;
                } else {
                    elem.logError("Invalid 'fontname' value (" + theValue + ") on span primitive");
                }
            } else if ("fontsize".equals(name)) {

                try {
                    this.fontSize = Float.valueOf(theValue);
                    ok = true;
                } catch (final NumberFormatException e) {
                    elem.logError("Invalid 'fontsize' value (" + theValue + ") on span primitive");
                }
            } else if ("fontstyle".equals(name)) {
                lower = theValue.toLowerCase(Locale.ROOT);

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
                    elem.logError("Invalid 'fontstyle' value (" + theValue + ") on span primitive");
                }
            } else if ("alpha".equals(name)) {
                this.alpha = parseDouble(theValue, elem, name, "span primitive");
                ok = this.alpha != null;
            } else {
                elem.logError("Unsupported attribute '" + name + "' on span primitive");
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

        if (this.color != null) {
            grx.setColor(this.color);
        } else {
            grx.setColor(Color.BLACK);
        }

        Composite origComp = null;

        if (this.alpha != null) {
            origComp = grx.getComposite();
            grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) this.alpha.doubleValue()));
        }

        if (this.fontName != null) {
            this.span.setFontName(this.fontName);
        }

        if (this.fontSize != null) {
            this.span.setFontSize(this.fontSize.floatValue() * this.scale);
        }

        if (this.fontStyle != null) {
            this.span.setFontStyle(this.fontStyle);
        }

        final FontSpec spec = new FontSpec();

        spec.fontName = Objects.requireNonNullElse(this.fontName, BundledFontManager.SANS);

        if (this.fontSize != null) {
            spec.fontSize = this.fontSize.doubleValue() * (double) this.scale;
        } else {
            spec.fontSize = 10.0 * (double) this.scale;
        }

        if (this.fontStyle != null) {
            spec.fontStyle = this.fontStyle.intValue();
        } else {
            spec.fontStyle = Font.PLAIN;
        }

        this.font = BundledFontManager.getInstance().getFont(spec);

        if (this.span != null) {
            if (x != null && y != null) {
                this.span.doLayout(context, ELayoutMode.TEXT);

                float actualX = 0.0f;
                float actualY = 0.0f;
                if (this.anchor == null || this.anchor == ETextAnchor.NW) {
                    actualX = x.floatValue() * this.scale;
                    actualY = y.floatValue() * this.scale;
                } else {if (this.anchor == ETextAnchor.W) {
                        actualX = x.floatValue() * this.scale;
                        actualY = y.floatValue() * this.scale - (float) this.span.getHeight() * 0.5f;
                    } else if (this.anchor == ETextAnchor.SW) {
                        actualX = x.floatValue() * this.scale;
                        actualY = y.floatValue() * this.scale - (float) this.span.getHeight();
                    } else if (this.anchor == ETextAnchor.N) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth() * 0.5f;
                        actualY = y.floatValue() * this.scale;
                    } else if (this.anchor == ETextAnchor.C) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth() * 0.5f;
                        actualY = y.floatValue() * this.scale - (float) this.span.getHeight() * 0.5f;
                    } else if (this.anchor == ETextAnchor.S) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth() * 0.5f;
                        actualY = y.floatValue() * this.scale - (float) this.span.getHeight();
                    } else if (this.anchor == ETextAnchor.NE) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth();
                        actualY = y.floatValue() * this.scale;
                    } else if (this.anchor == ETextAnchor.E) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth();
                        actualY = y.floatValue() * this.scale + (float) this.span.getHeight() * 0.5f;
                    } else if (this.anchor == ETextAnchor.SE) {
                        actualX = x.floatValue() * this.scale - (float) this.span.getWidth();
                        actualY = y.floatValue() * this.scale - (float) this.span.getHeight();
                    }
                }

                this.span.setX((int) Math.round(actualX));
                this.span.setY((int) Math.round(actualY));
                this.span.paintComponent(grx, ELayoutMode.TEXT);
            }

            this.span.setFontName(null);
            this.span.setFontSize(0.0f);
            this.span.setFontStyle(null);
        }

        if (origComp != null) {
            grx.setComposite(origComp);
        }
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context) {

        if (this.span != null) {
            this.span.doLayout(context, ELayoutMode.TEXT);
        }
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
    public DocPrimitiveSpanInst createInstance(final EvalContext evalContext) {

        final Object xVal = this.xCoord.evaluate(evalContext);
        final Object yVal = this.yCoord.evaluate(evalContext);

        final DocNonwrappingSpanInst spanInst = this.span.createInstance(evalContext);

        final DocPrimitiveSpanInst result;

        if (xVal instanceof final Number xNbr && yVal instanceof final Number yNbr) {

            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            result = new DocPrimitiveSpanInst(xNbr.doubleValue(), yNbr.doubleValue(), spanInst, this.anchor, null,
                    alphaValue);
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

        xml.add(ind, "<span");

        if (this.xCoord != null && this.xCoord.getNumber() != null) {
            xml.add(" x=\"", this.xCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.yCoord != null && this.yCoord.getNumber() != null) {
            xml.add(" y=\"", this.yCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.anchor != null && this.anchor != ETextAnchor.SW) {
            xml.add(" anchor=\"", this.anchor.name(), CoreConstants.QUOTE);
        }

        if (this.filled != null) {
            xml.add(" filled=\"", this.filled, CoreConstants.QUOTE);
        }

        if (this.colorName != null) {
            xml.add(" color=\"", this.colorName, CoreConstants.QUOTE);
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

        xml.add('>');

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

        if (this.span != null) {
            xml.add(ind2, "<content>");
            for (final AbstractDocObjectTemplate child : this.span.getChildren()) {
                child.toXml(xml, 0);
            }
            xml.addln("</content>");
        }

        xml.addln(ind, "</span>");
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Span";
    }

    /**
     * Sets the scale.
     *
     * @param theScale the new scale
     */
    @Override
    public void setScale(final float theScale) {

        if (this.span != null) {
            this.span.setScale(theScale);
        }

        this.scale = theScale;
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        if (this.xCoord != null && this.xCoord.getFormula() != null) {
            set.addAll(this.xCoord.getFormula().params.keySet());
        }

        if (this.yCoord != null && this.yCoord.getFormula() != null) {
            set.addAll(this.yCoord.getFormula().params.keySet());
        }

        if (this.span != null) {
            this.span.accumulateParameterNames(set);
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
                + Objects.hashCode(this.filled)
                + Objects.hashCode(this.colorName)
                + Objects.hashCode(this.color)
                + Objects.hashCode(this.span)
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
        } else if (obj instanceof final DocPrimitiveSpan spn) {
            equal = Objects.equals(this.xCoord, spn.xCoord)
                    && Objects.equals(this.yCoord, spn.yCoord)
                    && Objects.equals(this.anchor, spn.anchor)
                    && Objects.equals(this.filled, spn.filled)
                    && Objects.equals(this.colorName, spn.colorName)
                    && Objects.equals(this.color, spn.color)
                    && Objects.equals(this.span, spn.span)
                    && Objects.equals(this.fontName, spn.fontName)
                    && Objects.equals(this.fontSize, spn.fontSize)
                    && Objects.equals(this.fontStyle, spn.fontStyle)
                    && Objects.equals(this.alpha, spn.alpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
