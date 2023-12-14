package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveTextInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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

    /** The value. */
    private String value;

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
        copy.value = this.value;

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

        final BundledFontManager fonts;
        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else if ("x".equals(name)) {
            try {
                final Number num = NumberParser.parse(theValue);
                this.xCoord = new NumberOrFormula(num);
                ok = true;
            } catch (final NumberFormatException ex) {
                if (mode.reportDeprecated) {
                    elem.logError("Deprecated use of formula in 'x' attribute on text primitive");
                }
                try {
                    final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                    this.xCoord = new NumberOrFormula(form);
                    ok = true;
                } catch (final IllegalArgumentException e) {
                    elem.logError("Invalid 'x' value (" + theValue + ") on text primitive");
                }
            }
        } else if ("y".equals(name)) {
            try {
                final Number num = NumberParser.parse(theValue);
                this.yCoord = new NumberOrFormula(num);
                ok = true;
            } catch (final NumberFormatException ex) {
                if (mode.reportDeprecated) {
                    elem.logError("Deprecated use of formula in 'y' attribute on text primitive");
                }
                try {
                    final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                    this.yCoord = new NumberOrFormula(form);
                    ok = true;
                } catch (final IllegalArgumentException e) {
                    elem.logError("Invalid 'y' value (" + theValue + ") on text primitive");
                }
            }
        } else if ("anchor".equals(name)) {

            final ETextAnchor anch = ETextAnchor.valueOf(theValue);
            if (anch != null) {
                this.anchor = anch;
                ok = true;
            } else {
                elem.logError("Invalid 'anchor' value (" + theValue + ") on text primitive");
            }
        }  else if ("color".equals(name)) {

            if (ColorNames.isColorNameValid(theValue)) {
                this.color = ColorNames.getColor(theValue);
                this.colorName = theValue;
                ok = true;
            } else {
                elem.logError("Invalid 'color' value (" + theValue + ") on text primitive");
            }
        } else if ("value".equals(name)) {
            this.value = unescape(theValue);
            ok = this.value != null;
        } else if ("fontname".equals(name)) {

            fonts = BundledFontManager.getInstance();

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

            try {
                this.alpha = Double.valueOf(theValue);
                ok = true;
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'alpha' value (" + theValue + ") on text primitive");
            }
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

            if (this.color != null) {
                grx.setColor(this.color);
            } else {
                grx.setColor(Color.BLACK);
            }

            Composite origComp = null;

            if (this.alpha != null) {
                origComp = grx.getComposite();
                grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) this.alpha.doubleValue()));
            }

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

            this.font = BundledFontManager.getInstance().getFont(spec);

            grx.setFont(Objects.requireNonNullElseGet(this.font, this.owner::getFont));

            final String str = generateStringContents(context);

            float actualX = 0.0f;
            float actualY = 0.0f;
            if (this.anchor == null || this.anchor == ETextAnchor.SW) {
                actualX = x.floatValue() * this.scale;
                actualY = y.floatValue() * this.scale;
            } else {
                final FontRenderContext frc = grx.getFontRenderContext();
                final GlyphVector vect = grx.getFont().createGlyphVector(frc, str);
                final Rectangle2D bounds = vect.getVisualBounds();

                if (this.anchor == ETextAnchor.NW) {
                    actualX = x.floatValue() * this.scale;
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight();
                } else if (this.anchor == ETextAnchor.W) {
                    actualX = x.floatValue() * this.scale;
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.N) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight();
                } else if (this.anchor == ETextAnchor.C) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.S) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth() * 0.5f;
                    actualY = y.floatValue() * this.scale;
                } else if (this.anchor == ETextAnchor.NE) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth();
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight();
                } else if (this.anchor == ETextAnchor.E) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth();
                    actualY = y.floatValue() * this.scale + (float) bounds.getHeight() * 0.5f;
                } else if (this.anchor == ETextAnchor.SE) {
                    actualX = x.floatValue() * this.scale - (float) bounds.getWidth();
                    actualY = y.floatValue() * this.scale;
                }
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
            // Value in "text" objects may contain parameter or symbol references
            final int len = this.value.length();
            int pos = 0;
            while (pos < len) {
                final int open = this.value.indexOf('{', pos);
                if (open == -1) {
                    break;
                }

                final int close = this.value.indexOf('}', open + 1);
                if (close == -1) {
                    break;
                }

                final String name = this.value.substring(open + 1, close);

                if (!(!name.isEmpty() && name.charAt(0) == '\\')) {
                    final int bracket = name.indexOf('[');
                    if (bracket == -1) {
                        set.add(name);
                    } else {
                        set.add(name.substring(0, bracket));
                    }
                }
                pos = close + 1;
            }
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.xCoord)
                + EqualityTests.objectHashCode(this.yCoord)
                + EqualityTests.objectHashCode(this.anchor)
                + EqualityTests.objectHashCode(this.colorName)
                + EqualityTests.objectHashCode(this.color)
                + EqualityTests.objectHashCode(this.value)
                + EqualityTests.objectHashCode(this.fontName)
                + EqualityTests.objectHashCode(this.fontSize)
                + EqualityTests.objectHashCode(this.fontStyle)
                + EqualityTests.objectHashCode(this.alpha);
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
                    && Objects.equals(this.anchor, text.anchor)
                    && Objects.equals(this.colorName, text.colorName)
                    && Objects.equals(this.color, text.color)
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

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param other  the other object
     * @param indent the indent level
     */
    @Override
    public void whyNotEqual(final Object other, final int indent) {

        if (other instanceof final DocPrimitiveText obj) {

            if (!Objects.equals(this.xCoord, obj.xCoord)) {
                if (this.xCoord == null || obj.xCoord == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (xCoord: ", this.xCoord, CoreConstants.SLASH,
                            obj.xCoord, ")");
                }
            }

            if (!Objects.equals(this.yCoord, obj.yCoord)) {
                if (this.yCoord == null || obj.yCoord == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (yCoord: ", this.yCoord, CoreConstants.SLASH,
                            obj.yCoord, ")");
                }
            }

            if (!Objects.equals(this.anchor, obj.anchor)) {
                if (this.anchor == null || obj.anchor == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (anchor: ", this.anchor, CoreConstants.SLASH,
                            obj.anchor, ")");
                }
            }

            if (!Objects.equals(this.colorName, obj.colorName)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (colorName: ", this.colorName, "!=",
                        obj.colorName, ")");
            }

            if (!Objects.equals(this.color, obj.color)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (color: ", this.color, "!=", obj.color, ")");
            }

            if (!Objects.equals(this.value, obj.value)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (value: ", this.value, "!=", obj.value, ")");
            }

            if (!Objects.equals(this.fontName, obj.fontName)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (fontName: ", this.fontName, "!=",
                        obj.fontName, ")");
            }

            if (!Objects.equals(this.fontSize, obj.fontSize)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimit iveText (fontSize: ", this.fontSize, "!=",
                        obj.fontSize, ")");
            }

            if (!Objects.equals(this.fontStyle, obj.fontStyle)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (fontStyle: ", this.fontStyle, "!=",
                        obj.fontStyle, ")");
            }

            if (!Objects.equals(this.alpha, obj.alpha)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText (alpha: ", this.alpha, "!=", obj.alpha, ")");
            }
        } else {
            Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveText because other is ", other.getClass().getName());
        }
    }
}
