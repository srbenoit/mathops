package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EPaletteSymbol;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocSymbolPaletteInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.INode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A palette that allows the user to click buttons to enter symbols, such as "pi", "theta", etc. This control can be
 * embedded in a paragraph or span. It does not appear in a "print" version of a document, and should behave as if a key
 * was pressed in terms of the currently focused input and insertion caret.
 */
public final class DocSymbolPalette extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1699751835183065245L;

    /** Padding on the left and right end. */
    private static final int LEFT_RIGHT_PADDING = 10;

    /** Padding on the top and bottom end. */
    private static final int TOP_BOTTOM_PADDING = 5;

    /** The keys. */
    public final List<Key> keys;

    /**
     * Construct a new {@code DocSymbolPalette} object.
     *
     * @param theSymbols the symbols the palette will support
     */
    DocSymbolPalette(final EPaletteSymbol... theSymbols) {

        super();

        if (theSymbols == null || theSymbols.length == 0) {
            this.keys = new ArrayList<>(2);
        } else {
            this.keys = new ArrayList<>(theSymbols.length);

            for (final EPaletteSymbol sym : theSymbols) {
                this.keys.add(new Key(sym));
            }
        }
    }

    /**
     * Sets the rendering scale.
     *
     * @param theScale the scale
     */
    @Override
    public void setScale(final float theScale) {

        super.setScale(theScale);

        for (final Key key : this.keys) {
            key.label.setScale(theScale);
        }
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocSymbolPalette deepCopy() {

        final DocSymbolPalette copy = new DocSymbolPalette();

        copy.copyObjectFromContainer(this);

        for (final Key key : this.keys) {
            copy.keys.add(key.deepCopy());
        }

        return copy;
    }

    /**
     * Set an attribute value used in drawing.
     *
     * @param name     the name of the attribute
     * @param theValue the attribute value
     * @param elem     an element to which to log errors
     * @return true if the attribute was valid; false otherwise
     */
    boolean setAttribute(final String name, final String theValue, final INode elem) {

        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else if ("symbols".equals(name)) {
            this.keys.clear();

            // Value is a comma-separated list of EPaletteSymbol labels
            final String[] split = theValue.split(CoreConstants.COMMA);

            ok = true;
            for (final String s : split) {
                final EPaletteSymbol sym = EPaletteSymbol.forLabel(s);
                if (sym == null) {
                    elem.logError("Invalid 'symbols' value (" + theValue + ") on symbol palette");
                    ok = false;
                    break;
                }
                this.keys.add(new Key(sym));
            }
        } else {
            elem.logError("Unsupported attribute '" + name + "' on arc primitive");
        }

        return ok;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        // No action
    }

    /**
     * Recompute the size of the object's bounding box. This method is empty since this component will not do its own
     * layout. It is simply a container for objects that should flow into the larger paragraph of which this is a part.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        for (final Key key : this.keys) {
            key.label.doLayout(context, mathMode);
        }

        // Find max center height above baseline (this is the "true centerline" as distance above
        // baseline)
        int maxCenter = 0;
        for (final Key key : this.keys) {
            final int center = key.label.getBaseLine() - key.label.getCenterLine();

            if (center > maxCenter) {
                maxCenter = center;
            }
        }

        // Compute maximum height of any object - this will become the new baseline height for the
        // whole span.
        int maxHeight = 0;
        int maxTotalHeight = 0;
        for (final Key key : this.keys) {
            final int height = key.label.getBaseLine();
            maxTotalHeight = Math.max(maxTotalHeight, key.label.getHeight());

            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        // Store the baseline and centerline offsets
        setBaseLine(TOP_BOTTOM_PADDING + maxHeight);
        setCenterLine(TOP_BOTTOM_PADDING + maxHeight - maxCenter);

        // Generate the correct Y values for all objects, tracking the bottom-most point to use as
        // bounds of this object.
        int x = LEFT_RIGHT_PADDING;
        int y = TOP_BOTTOM_PADDING;

        int objY;

        final float scale = this.getScale();
        final int keyPadding = Math.round(10.0f * scale);
        final int keySpacing = Math.round(4.0f * scale);

        x += keySpacing;

        for (final Key key : this.keys) {
            key.bounds.setLocation(x, TOP_BOTTOM_PADDING);

            objY = TOP_BOTTOM_PADDING + maxHeight - key.label.getBaseLine();

            key.label.setX(x + keyPadding);
            key.label.setY(objY);

            final int buttonH = key.label.getHeight();
            if ((objY + buttonH) > y) {
                y = objY + buttonH;
            }

            key.bounds.setSize(key.label.getWidth() + 2 * keyPadding, maxTotalHeight);

            x += key.label.getWidth() + keySpacing + 2 * keyPadding;
        }

        x += LEFT_RIGHT_PADDING;
        y += TOP_BOTTOM_PADDING;

        setWidth(x);
        setHeight(y);
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        for (final Key key : this.keys) {
            final Rectangle bounds = key.bounds;
            final int rad = bounds.height / 2;

            grx.setColor(Color.LIGHT_GRAY);
            grx.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width, bounds.height, rad, rad);
            grx.setColor(new Color(255, 255, 230));
            grx.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, rad, rad);
            grx.setColor(Color.BLACK);
            grx.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, rad, rad);

            key.label.paintComponent(grx, mathMode);
        }

        postPaint(grx);
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
    @Override
    public DocSymbolPaletteInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final int count = this.keys.size();
        final EPaletteSymbol[] symbols = new EPaletteSymbol[count];
        for (int i = 0; i < count; ++i) {
            symbols[i] = this.keys.get(i).symbol;
        }

        return new DocSymbolPaletteInst(objStyle, null, symbols);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<symbol-palette symbols=\"");
        appendSymbolList(xml);
        xml.add("\"/>");
    }

    /**
     * Appends a comma-separated list of symbols to an {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     */
    private void appendSymbolList(final HtmlBuilder builder) {

        final int count = this.keys.size();
        if (count > 0) {
            builder.add(this.keys.getFirst().symbol.label);
            for (int i = 1; i < count; ++i) {
                builder.add(CoreConstants.COMMA_CHAR);
                builder.add(this.keys.get(i).symbol.label);
            }
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
     * @param context      the evaluation context
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        // No action - this control is not emitted in print form
    }

    /**
     * Generate a {@code String} representation of the palette (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(50);

        buf.add("[Symbol palette: {");
        appendSymbolList(buf);
        buf.add("}]");

        return buf.toString();
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>SymbolPalette</li>");
    }

    /**
     * Default implementation to test whether a mouse press should be consumed.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return false, meaning the mouse press is not consumed
     */
    @Override
    public boolean consumeMousePress(final int xCoord, final int yCoord,
                                     final EvalContext context) {

        boolean consume = false;

        if (xCoord >= 0 && xCoord < getWidth() && yCoord >= 0 && yCoord < getHeight()) {

            for (final Key key : this.keys) {
                if (key.bounds.contains(xCoord, yCoord)) {

                    AbstractDocObjectTemplate parent = getParent();
                    while (parent != null) {
                        if (parent instanceof final DocColumn col) {
                            // Log.info("Sending " + key.symbol.label + " to owning column");
                            consume = true;
                            col.processKey(key.symbol.character, 0, 0, context);
                            break;
                        }
                        parent = parent.getParent();
                    }

                }
            }
        }

        return consume;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.keys.hashCode();
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
        } else if (obj instanceof final DocSymbolPalette palette) {
            equal = this.keys.equals(palette.keys);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * A "key" used to enter a symbol.
     */
    public static final class Key {

        /** The symbol. */
        public final EPaletteSymbol symbol;

        /** The key label. */
        public final DocText label;

        /** The bounding rectangle. */
        final Rectangle bounds;

        /**
         * Constructs a new {@code Key}.
         *
         * @param theSymbol the symbol
         */
        Key(final EPaletteSymbol theSymbol) {

            this(theSymbol, new DocText(Character.toString(theSymbol.character)), new Rectangle());
        }

        /**
         * Constructs a new {@code Key}.
         *
         * @param theSymbol the symbol
         * @param theLabel  the label
         * @param theBounds the bounding rectangle
         */
        private Key(final EPaletteSymbol theSymbol, final DocText theLabel,
                    final Rectangle theBounds) {

            this.symbol = theSymbol;
            this.label = theLabel;
            this.bounds = theBounds;
        }

        /**
         * Constructs a deep copy of the key.
         *
         * @return the copy
         */
        Key deepCopy() {

            return new Key(this.symbol, this.label.deepCopy(), new Rectangle(this.bounds));
        }
    }
}
