package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.DocObjectLayoutBounds;
import dev.mathops.assessment.document.DocObjectStyle;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The base class for all objects that make up documents.
 */
public abstract class AbstractDocObjectTemplate implements Serializable {

    /** The default base font size. */
    public static final float DEFAULT_BASE_FONT_SIZE = 24.0f;

    /** Plain font style (default). */
    public static final int PLAIN = Font.PLAIN;

    /** Bold font style. */
    public static final int BOLD = Font.BOLD;

    /** Italic font style. */
    public static final int ITALIC = Font.ITALIC;

    /** Underline font style. */
    public static final int UNDERLINE = 0x04;

    /** Overline font style. */
    public static final int OVERLINE = 0x08;

    /** Strike through font style. */
    public static final int STRIKETHROUGH = 0x10;

    /** Boxed font style. */
    public static final int BOXED = 0x20;

    /** Hidden font style. */
    public static final int HIDDEN = 0x40;

    /** Align baseline of object to baseline of current line. */
    static final int BASELINE = 1;

    /** Align centerline of object to centerline of current line. */
    static final int CENTERLINE = 2;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7492832285509796852L;

    /** A default format to apply when no other formats are defined. */
    private static final DocObjectStyle defaultFormat;

    /** Flag to toggle showing of bounding box when rendering. */
    private static boolean showBoundsFlag;

    /** Flag to toggle showing of baseline when rendering. */
    private static boolean showBaselineFlag;

    /** Flag to toggle showing of centerline when rendering. */
    private static boolean showCenterlineFlag;

    /** The object that contains this object. */
    private AbstractDocContainer parent;

    /** Object style (created lazily when first value is set). */
    private DocObjectStyle style;

    /** The layout bounds, once they are computed. */
    private DocObjectLayoutBounds bounds;

    /** The rendering scale. */
    private float scale = 1.0f;

    /* Static initialization to create the default format. */
    static {
        final BundledFontManager bfm = BundledFontManager.getInstance();

        defaultFormat = new DocObjectStyle();

        // Default values
        defaultFormat.colorName = "black";
        defaultFormat.fontName = BundledFontManager.SERIF;
        defaultFormat.fontSize = DEFAULT_BASE_FONT_SIZE;
        defaultFormat.fontStyle = Integer.valueOf(PLAIN);

        Font font = bfm.getFont(BundledFontManager.SERIF, (double) DEFAULT_BASE_FONT_SIZE, PLAIN);
        if (font == null) {
            font = new Font("Dialog", Font.PLAIN, (int) DEFAULT_BASE_FONT_SIZE);
        }
        defaultFormat.font = font;
    }

    /**
     * Construct a new {@code AbstractDocObject}.
     */
    AbstractDocObjectTemplate() {

        // showBoundsFlag = true;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    public abstract AbstractDocObjectTemplate deepCopy();

    /**
     * Copy information from a source {@code DocObject} object, including all underlying {@code DocFormattable}
     * information.
     *
     * @param source the {@code DocObject} from which to copy data
     */
    final void copyObjectFrom(final AbstractDocObjectTemplate source) {

        if (source.bounds == null) {
            this.bounds = null;
        } else {
            if (this.bounds == null) {
                this.bounds = new DocObjectLayoutBounds();
            }
            this.bounds.x = source.bounds.x;
            this.bounds.y = source.bounds.y;
            this.bounds.width = source.bounds.width;
            this.bounds.height = source.bounds.height;
            this.bounds.baseLine = source.bounds.baseLine;
            this.bounds.centerLine = source.bounds.centerLine;
        }

        copyFormatFrom(source);
    }

    /**
     * Copy the style attributes from another object. This allows a user to create a {@code DocFormattable} object by
     * itself, containing just format data, and apply that to various other objects.
     *
     * @param source the object to copy format attributes from
     */
    private void copyFormatFrom(final AbstractDocObjectTemplate source) {

        if (source.style == null) {
            this.style = null;
        } else {
            if (this.style == null) {
                this.style = new DocObjectStyle();
            }
            this.style.colorName = source.style.colorName;
            this.style.fontName = source.style.fontName;
            this.style.fontSize = source.style.fontSize;
            this.style.fontScale = source.style.fontScale;
            this.style.fontStyle = source.style.fontStyle;
            this.style.font = source.style.font;
        }
    }

    /**
     * Sets the rendering scale.
     *
     * @param theScale the scale
     */
    void setScale(final float theScale) {

        this.scale = theScale;

        uncacheFont();
    }

    /**
     * Gets the rendering scale.
     *
     * @return the scale
     */
    public final float getScale() {

        return this.scale;
    }

    /**
     * This method uncaches the font for an object. It should be used whenever an object's parentage changes, or when a
     * parent's font attributes change. It descends through the tree uncaching fonts of all children.
     */
    void uncacheFont() {

        if (this.style != null) {
            this.style.font = null;
        }
    }

    /**
     * Set the {@code DocObject} that owns this object.
     *
     * @param theParent the parent object
     */
    final void setParent(final AbstractDocContainer theParent) {

        this.parent = theParent;
    }

    /**
     * Get the {@code DocObject} that owns this object.
     *
     * @return the parent object
     */
    final AbstractDocContainer getParent() {

        return this.parent;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    protected abstract int getLeftAlign();

    /**
     * Set the font color name.
     *
     * @param theColorName the name of the new font color
     */
    public final void setColorName(final String theColorName) {

        if (this.style == null) {
            this.style = new DocObjectStyle();
        }
        this.style.colorName = theColorName;
    }

    /**
     * Get the font color name. If no local color name is defined, all ancestors are searched for a color name
     * specification. If none is found, the default color name is returned. This method will never return {@code null}.
     *
     * @return the name of the font color to be applied to the object
     */
    public final String getColorName() {

        for (AbstractDocObjectTemplate obj = this; obj != null; obj = obj.parent) {
            if (obj.style != null && obj.style.colorName != null) {
                return obj.style.colorName;
            }
        }

        return defaultFormat.colorName;
    }

    /**
     * Set the font name.
     *
     * @param theFontName the new font name
     */
    public final void setFontName(final String theFontName) {

        if (this.style == null) {
            this.style = new DocObjectStyle();
        }
        this.style.fontName = theFontName;
        uncacheFont();
    }

    /**
     * Get the font name. If no local name is defined, all ancestors are searched for a font name specification. If none
     * is found, the default font name is returned. This method will never return {@code null}.
     *
     * @return the font name to be applied for the object
     */
    public final String getFontName() {

        for (AbstractDocObjectTemplate obj = this; obj != null; obj = obj.parent) {
            if (obj.style != null && obj.style.fontName != null) {
                return obj.style.fontName;
            }
        }

        return defaultFormat.fontName;
    }

    /**
     * Mutator for the font size.
     *
     * @param theFontSize the new font size
     */
    public final void setFontSize(final float theFontSize) {

        if (this.style == null) {
            this.style = new DocObjectStyle();
        }
        this.style.fontSize = theFontSize;

        uncacheFont();
    }

    /**
     * Accessor for the font size. If no local size is defined, all ancestors are searched for a font size
     * specification. If none is found, the default font size is returned.
     *
     * @return the font size to be applied to the object
     */
    public final int getFontSize() {

        float theScale = 1.0f;

        for (AbstractDocObjectTemplate obj = this; obj != null; obj = obj.parent) {
            if (obj.style != null) {
                if (obj.style.fontSize != 0.0f) {
                    return (int) (obj.style.fontSize * theScale * this.scale);
                } else if (obj.getFontScale() != 1.0f) {
                    theScale *= obj.getFontScale();
                }
            }
        }

        return (int) (defaultFormat.fontSize * theScale * this.scale);
    }

    /**
     * Get the default font size.
     *
     * @return the default font size
     */
    public static float getDefaultFontSize() {

        return defaultFormat.fontSize;
    }

    /**
     * Set the font scale.
     *
     * @param theFontScale the new font scale
     */
    final void setFontScale(final float theFontScale) {

        if (this.style == null) {
            this.style = new DocObjectStyle();
        }
        this.style.fontScale = theFontScale;
        uncacheFont();
    }

    /**
     * Get the font scale.
     *
     * @return the font scale to be applied to the object
     */
    public final float getFontScale() {

        return this.style == null ? 1.0f : this.style.fontScale;
    }

    /**
     * Set the font style.
     *
     * @param theFontStyle the new font style
     */
    public final void setFontStyle(final Integer theFontStyle) {

        if (this.style == null) {
            this.style = new DocObjectStyle();
        }
        this.style.fontStyle = theFontStyle;

        uncacheFont();
    }

    /**
     * Accessor for the font style. If no local style is defined, all ancestors are searched for a font style
     * specification. If none is found, the default font style is returned.
     *
     * @return the font style to be applied for the object
     */
    public final int getFontStyle() {

        for (AbstractDocObjectTemplate obj = this; obj != null; obj = obj.parent) {
            if (obj.style != null && obj.style.fontStyle != null) {
                return obj.style.fontStyle.intValue();
            }
        }

        return defaultFormat.fontStyle.intValue();
    }

    /**
     * Test whether the font is boldface.
     *
     * @return {@code true} if boldface, {@code false} otherwise
     */
    public final boolean isBold() {

        return (getFontStyle() & BOLD) == BOLD;
    }

    /**
     * Test whether the font is italics.
     *
     * @return {@code true} if italics, {@code false} otherwise
     */
    final boolean isItalic() {

        return (getFontStyle() & ITALIC) == ITALIC;
    }

    /**
     * Test whether the font is underlined.
     *
     * @return {@code true} if underlined, {@code false} otherwise
     */
    final boolean isUnderline() {

        return (getFontStyle() & UNDERLINE) == UNDERLINE;
    }

    /**
     * Test whether the font is overlined.
     *
     * @return {@code true} if overlined, {@code false} otherwise
     */
    final boolean isOverline() {

        return (getFontStyle() & OVERLINE) == OVERLINE;
    }

    /**
     * Test whether the font is strike-through.
     *
     * @return {@code true} if strike-through, {@code false} otherwise
     */
    final boolean isStrikethrough() {

        return (getFontStyle() & STRIKETHROUGH) == STRIKETHROUGH;
    }

    /**
     * Test whether the font is boxed.
     *
     * @return {@code true} if boxed, {@code false} otherwise
     */
    final boolean isBoxed() {

        return (getFontStyle() & BOXED) == BOXED;
    }

    /**
     * Test whether the font is hidden.
     *
     * @return {@code true} if hidden, {@code false} otherwise
     */
    public final boolean isHidden() {

        return (getFontStyle() & HIDDEN) == HIDDEN;
    }

    /**
     * Retrieve the font to use for this format.
     *
     * @return the font
     */
    public final Font getFont() {

        Font font = this.style == null ? null : this.style.font;

        if (font == null) {
            // Retrieve the settings to apply to this object. This may involve traversing the tree
            // of objects to get the settings for an ancestor of this object if it has no settings
            // itself.
            final String fName = getFontName();
            final int fStyle = getFontStyle() & (BOLD | ITALIC);
            final int fSize = getFontSize();

            // Now convert the settings into a realized font.
            final BundledFontManager bfm = BundledFontManager.getInstance();
            font = bfm.getFont(fName, (double) fSize, fStyle);

            if (font == null) {
                // Font manager can't do it, so make a substitute
                Log.warning("Can't resolve font: " + fName + ", size=" + fSize + ", style=" + fStyle);
                font = new Font("Dialog", fStyle, fSize);
            }

            if (this.style == null) {
                this.style = new DocObjectStyle();
            }
            this.style.font = font;
        }

        return font;
    }

    /**
     * Get the width of the object when rendered.
     *
     * @return the width (in pixels)
     */
    public final int getWidth() {

        return this.bounds == null ? 0 : this.bounds.width;
    }

    /**
     * Set the width of the object when rendered.
     *
     * @param width the width (in pixels)
     */
    final void setWidth(final int width) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.width = width;
    }

    /**
     * Get the height of the object when rendered.
     *
     * @return the height (in pixels)
     */
    public final int getHeight() {

        return this.bounds == null ? 0 : this.bounds.height;
    }

    /**
     * Set the height of the object when rendered.
     *
     * @param height the height (in pixels)
     */
    final void setHeight(final int height) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.height = height;
    }

    /**
     * Get the X position of top left corner of the object relative to its parent object's location.
     *
     * @return the X position of the object
     */
    public final int getX() {

        return this.bounds == null ? 0 : this.bounds.x;
    }

    /**
     * Set the X location of the top left corner of the object relative to its parent object's location.
     *
     * @param xPos the relative X position of the object
     */
    public final void setX(final int xPos) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.x = xPos;
    }

    /**
     * Get the Y position of top left corner of the object relative to its parent object's location.
     *
     * @return the Y position of the object
     */
    public final int getY() {

        return this.bounds == null ? 0 : this.bounds.y;
    }

    /**
     * Set the Y location of the top left corner of the object relative to its parent object's location.
     *
     * @param yPos the relative Y position of the object
     */
    public final void setY(final int yPos) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.y = yPos;
    }

    /**
     * Get baseline of the object.
     *
     * @return the object's baseline offset
     */
    final int getBaseLine() {

        return this.bounds == null ? 0 : this.bounds.baseLine;
    }

    /**
     * Set baseline of the object.
     *
     * @param theBaseLine the object's new baseline offset
     */
    final void setBaseLine(final int theBaseLine) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.baseLine = theBaseLine;
    }

    /**
     * Get the centerline of the object.
     *
     * @return the object's centerline offset
     */
    final int getCenterLine() {

        return this.bounds == null ? 0 : this.bounds.centerLine;
    }

    /**
     * Set the centerline of the object.
     *
     * @param theCenterLine the object's new centerline offset
     */
    final void setCenterLine(final int theCenterLine) {

        if (this.bounds == null) {
            this.bounds = new DocObjectLayoutBounds();
        }
        this.bounds.centerLine = theCenterLine;
    }

    /**
     * Recompute the size of the object's bounding box, and those of its children.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    protected abstract void doLayout(EvalContext context, ELayoutMode mathMode);

    /**
     * Create a string for a particular indentation level.
     *
     * @param indent the number of spaces to indent
     * @return a string with the requested number of spaces
     */
    static String makeIndent(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(indent);

        for (int i = 0; i < indent; ++i) {
            builder.add("  ");
        }

        return builder.toString();
    }

    /**
     * Generate a set of parameter names referenced by the object or any objects it contains.
     *
     * @return the set of parameter names
     */
    public final Set<String> parameterNames() {

        final Set<String> set = new HashSet<>(10);

        accumulateParameterNames(set);

        return set;
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set to which to add parameter names
     */
    public abstract void accumulateParameterNames(Set<String> set);

    /**
     * Scans a text string for parameter references of the form {name}, and adds those to a set of referenced parameter
     * names.
     *
     * @param toScan the string to scan
     * @param set the set to which to add parameter names
     */
    static void scanStringForParameterReferences(final String toScan, final Collection<? super String> set) {

        final int len = toScan.length();

        int pos = toScan.indexOf('{');
        while (pos >= 0 && pos < len) {
            final int end = toScan.indexOf('}', pos + 1);
            if (end == -1) {
                break;
            }
            if ((int) toScan.charAt(pos + 1) != '\\') {
                if (end > pos + 1) {
                    final String varName = toScan.substring(pos + 1, end);
                    final int bracket = varName.indexOf('[');
                    if (bracket == -1) {
                        set.add(varName);
                    } else {
                        set.add(varName.substring(0, bracket));
                    }
                }
            }
            pos = toScan.indexOf('{', end + 1);
        }
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    protected abstract AbstractDocObjectInst createInstance(EvalContext evalContext);

    /**
     * Generate the XML representation of the object.
     *
     * @param indent the number of spaces to indent the printout
     * @return the XML representation
     */
    public final String toXml(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(512);

        toXml(builder, indent);

        return builder.toString();
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    public abstract void toXml(HtmlBuilder xml, int indent);

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file (the value should be updated if the method writes any files)
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to
     *                     {@code true} if it is {@code false} and the user is asked "Overwrite? [YES] [ALL] [NO]" and
     *                     chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  {@code true} to show answers in any inputs embedded in the document; {@code false} if answers
     *                     should not be shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     * @param context      the evaluation context
     */
    protected abstract void toLaTeX(File dir, int[] fileIndex, boolean[] overwriteAll, HtmlBuilder builder,
                                    boolean showAnswers, char[] mode, EvalContext context);

    /**
     * Print the format attributes to an {@code HtmlBuilder}, in XML format. The format text will include a leading
     * space.
     *
     * @param builder          the {@code HtmlBuilder} to which to write the information
     * @param defaultFontScale the default font scale (if the font scale is not this value, it will be emitted as an
     *                         attribute)
     */
    void printFormat(final HtmlBuilder builder, final float defaultFontScale) {

        if (this.style != null) {
            if (this.style.colorName != null) {
                builder.add(" color='", this.style.colorName, "'");
            }

            if (this.style.fontName != null) {
                builder.add(" fontname='", this.style.fontName, "'");
            }

            if (this.style.fontSize != 0.0f) {
                builder.add(" fontsize='", Float.toString(this.style.fontSize), "'");
            } else if (this.style.fontScale != defaultFontScale) {
                builder.add(" fontsize='", Integer.toString((int) (this.style.fontScale * 100.0f)), "%'");
            }

            if (this.style.fontStyle != null) {
                builder.add(" fontstyle='", makeStyleString(), "'");
            }
        }
    }

    /**
     * Print the object in XML format.
     *
     * @param ps     the {@code PrintStream} to which to print the object
     * @param indent the number of spaces to indent the printout
     */
    public void print(final PrintStream ps, final int indent) {

        ps.print(toXml(indent));
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    protected abstract void printTree(PrintStream ps);

    /**
     * Perform pre-painting operations, including adjusting the graphics transformation.
     *
     * @param grx the {@code Graphics} on which drawing will be done
     */
    final void prePaint(final Graphics grx) {

        grx.translate(getX(), getY());
    }

    /**
     * Perform post-painting operations, which undoes anything done in {@code prePaint}.
     *
     * @param grx the {@code Graphics} on which drawing will be done
     */
    final void postPaint(final Graphics grx) {

        grx.translate(-getX(), -getY());
    }

    /**
     * Draw the object. The object will be drawn at its current location, so if this object is not placed at (0,0) in
     * the {@code Graphics}, a transform should be applied to the {@code Graphics} to shift the object to the proper
     * location before this call is made.
     *
     * @param grx      the {@code Graphics} to draw to
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    protected abstract void paintComponent(Graphics grx, ELayoutMode mathMode);

    /**
     * Performs rendering common to all components. Subclasses should call this method as the first step in their
     * implementation of {@code paintComponent}.
     *
     * @param grx the {@code Graphics} to draw to
     */
    final void innerPaintComponent(final Graphics grx) {

        if (showBoundsFlag) {
            grx.setColor(ColorNames.getColor("powder blue"));
            grx.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        if (showBaselineFlag) {
            grx.setColor(ColorNames.getColor("DarkSeaGreen3"));
            grx.drawLine(0, getBaseLine(), getWidth(), getBaseLine());
        }

        if (showCenterlineFlag) {
            grx.setColor(ColorNames.getColor("coral"));
            grx.drawLine(0, getCenterLine(), getWidth(), getCenterLine());
        }
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public abstract String toString();

    /**
     * Generate a string representation of a style. This will consist of a set of comma-separated style indicators,
     * taken from this list:<br>
     * <br>
     * <b>plain, bold, italic, underline, overline, strikethrough, boxed</b><br>
     * For example: "bold,italic,strikethrough".
     *
     * @return the string representation
     */
    private String makeStyleString() {

        // Called only when it is known that this.style.getFontStyle() is not null

        final int[] styles = {BOLD, ITALIC, UNDERLINE, OVERLINE, STRIKETHROUGH, BOXED};
        final String[] names = {"bold", "italic", "underline", "overline", "strikethrough", "boxed"};

        final int styleInt = this.style.fontStyle.intValue();
        final HtmlBuilder builder = new HtmlBuilder(50);

        if (styleInt == 0) {
            builder.add("plain");
        } else {

            boolean comma = false;
            final int count = styles.length;
            for (int i = 0; i < count; ++i) {

                if ((styleInt & styles[i]) == styles[i]) {

                    if (comma) {
                        builder.add(CoreConstants.COMMA);
                    }

                    builder.add(names[i]);
                    comma = true;
                }
            }
        }

        return builder.toString();
    }

    /**
     * Set the default font to use when no font is specified for a component.
     *
     * @param fontName the default font name
     */
    public static void setDefaultFontName(final String fontName) {

        defaultFormat.fontName = fontName;
    }

    /**
     * Set the default font size to use when no font size is specified for a component.
     *
     * @param fontSize the default font size
     */
    public static void setDefaultFontSize(final int fontSize) {

        defaultFormat.fontSize = (float) fontSize;
    }

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
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int docObjectHashCode() {

        final String thisColor = this.style == null ? null : this.style.colorName;
        final String thisFontName = this.style == null ? null : this.style.fontName;
        final float thisFontSize = this.style == null ? 0.0f : this.style.fontSize;
        final Integer thisFontStyle = this.style == null ? null : this.style.fontStyle;

        return Objects.hashCode(thisColor) + Objects.hashCode(thisFontName)
                + Float.hashCode(thisFontSize) + Objects.hashCode(thisFontStyle);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean docObjectEquals(final AbstractDocObjectTemplate obj) {

        final String thisColor = this.style == null ? null : this.style.colorName;
        final String objColor = obj.style == null ? null : obj.style.colorName;

        final String thisFontName = this.style == null ? null : this.style.fontName;
        final String objFontName = obj.style == null ? null : obj.style.fontName;

        final float thisFontSize = this.style == null ? 0.0f : this.style.fontSize;
        final float objFontSize = obj.style == null ? 0.0f : obj.style.fontSize;

        final Integer thisFontStyle = this.style == null ? null : this.style.fontStyle;
        final Integer objFontStyle = obj.style == null ? null : obj.style.fontStyle;

        return Objects.equals(thisColor, objColor)
                && Objects.equals(thisFontName, objFontName) && thisFontSize == objFontSize
                && Objects.equals(thisFontStyle, objFontStyle);
    }

    /**
     * Generates a string from a string that may include parameter references.
     *
     * @param theContext the evaluation context
     * @return the generated string (empty if this primitive has no content)
     */
    public String generateStringContents(final EvalContext theContext, final String toConvert) {

        String work;
        AbstractVariable var;

        if (toConvert == null) {
            work = CoreConstants.EMPTY;
        } else {
            // Substitute parameter values into text.
            work = toConvert;

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
}
