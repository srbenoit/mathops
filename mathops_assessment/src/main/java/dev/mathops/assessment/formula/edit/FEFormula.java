package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A container that holds a single child object and that can generate a {@code Formula} when valid.
 *
 * <p>
 * If the {@code type} field of this object is set to a non-{@code null} value, then it will only accept a child that
 * generates that type. If the {@code type} field is not set, it will accept any type, and will adopt the type of its
 * child.
 */
public final class FEFormula extends AbstractFEObject {

    /** The background when enabled. */
    static final Color ENABLED_BG = Color.WHITE;

    /** The background when enabled. */
    static final Color DISABLED_BG = new Color(245, 245, 245);

    /** The single top-level child. */
    private AbstractFEObject topLevel;

    /** The owning panel - needed to trigger repaints when size or contents change. */
    private final FormulaEditorPanel owner;

    /** The insets. */
    private final Insets insets;

    /** The laid out size. */
    private final Dimension size;

    /** The off-screen image with the rendered formula, including cursor and selection highlight. */
    private BufferedImage offscreen;

    /** A {@code Graphics2D} that will render to the off-screen image. */
    private Graphics2D offscreenG2d;

    /** Listeners to notify when a formula is edited. */
    private final List<IFormulaEditorListener> listeners;

    /**
     * Constructs a new {@code FEFormula}.
     *
     * @param theFontSize the font size for the component
     * @param theOwner    owning panel - needed to trigger repaints when size or contents change
     * @param theInsets   the insets
     */
    public FEFormula(final int theFontSize, final FormulaEditorPanel theOwner, final Insets theInsets) {

        super(theFontSize);

        this.owner = theOwner;
        this.insets = theInsets;

        this.size = new Dimension();
        this.offscreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.offscreenG2d = this.offscreen.createGraphics();
        this.offscreenG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        this.listeners = new ArrayList<>(4);
    }

    /**
     * Adds a listener.
     *
     * @param theListener the listener
     */
    public void addListener(final IFormulaEditorListener theListener) {

        this.listeners.add(theListener);
    }

    /**
     * Sets the top-level object.
     *
     * @param newTopLevel the new top-level object
     * @param storeUndo   true to store an undo state; false to skip
     * @return {@code null} if successful; an error message if the provided object is not of a compatible type
     */
    public String setTopLevel(final AbstractFEObject newTopLevel, final boolean storeUndo) {

        String error = null;

        if (newTopLevel == null) {
            if (this.topLevel != null) {
                this.topLevel.setParent(null);
            }
            this.topLevel = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getAllowedTypes();
            final EType childType = newTopLevel.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> filtered =
                        EType.filter(allowed, newTopLevel.getAllowedTypes());
                isAllowed = !filtered.isEmpty();

                if (!isAllowed) {
                    error = SimpleBuilder.concat("Object of null type cannot be added to this formula");
                    Log.warning(error);
                }
            } else {
                isAllowed = allowed.contains(childType);

                if (!isAllowed) {
                    error = SimpleBuilder.concat("Object of type '", childType, "' cannot be added to this formula");
                    Log.warning(error);
                }
            }

            if (isAllowed) {
                if (this.topLevel != null) {
                    this.topLevel.setParent(null);
                }
                this.topLevel = newTopLevel;
                newTopLevel.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }

        return error;

    }

    /**
     * Gets the top-level object.
     *
     * @return the top-level object
     */
    public AbstractFEObject getTopLevel() {

        return this.topLevel;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. This object adds no steps - it simply
     * defers to its top-level child.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return this.topLevel == null ? 0 : this.topLevel.getNumCursorSteps();
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if a top-level object is present and valid; false if not
     */
    @Override
    public boolean isValid() {

        return this.topLevel != null && this.topLevel.isValid();
    }

    /**
     * Attempts to replace one child with another. For example, replacing an integer constant with a real constant when
     * the user types a '.' character while entering a number.
     *
     * @param currentChild the current child
     * @param newChild     the new (replacement) child
     * @return true if the replacement was allowed (and performed); false if it is not allowed
     */
    @Override
    public boolean replaceChild(final AbstractFEObject currentChild, final AbstractFEObject newChild) {

        final boolean result;

        if (this.topLevel == null) {
            Log.warning("Attempt to replace object when there are no child objects");
            result = false;
        } else if (currentChild == this.topLevel) {
            if (newChild == null) {
                setTopLevel(null, true);
                result = true;
            } else {
                final EnumSet<EType> allowed = getAllowedTypes();
                final EType newType = newChild.getCurrentType();

                if (newType == null) {
                    result = false;
                    final EnumSet<EType> possible = newChild.getAllowedTypes();
                    final EnumSet<EType> filtered = EType.filter(allowed, possible);

                    if (!filtered.isEmpty()) {
                        possible.clear();
                        possible.addAll(filtered);
                        setTopLevel(newChild, true);
                    }
                } else if (allowed.contains(newType)) {
                    setTopLevel(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to add ", newType, " type child to formula when not allowed");
                    result = false;
                }
            }
        } else {
            Log.warning("Attempt to replace object that is not child of this object");
            result = false;
        }

        return result;
    }

    /**
     * Recomputes the current type (does nothing for fixed-type constant values).
     */
    @Override
    public void recomputeCurrentType() {

        final EnumSet<EType> allowedTypes = getAllowedTypes();
        final EnumSet<EType> possible = getPossibleTypes();
        possible.clear();

        if (this.topLevel == null) {
            setCurrentType(null);
            possible.addAll(allowedTypes);
        } else {
            final EType topLevelType = this.topLevel.getCurrentType();
            setCurrentType(topLevelType);

            if (topLevelType == null) {
                final EnumSet<EType> topPossible = this.topLevel.getPossibleTypes();
                final EnumSet<EType> filtered = EType.filter(allowedTypes, topPossible);
                possible.addAll(filtered);
            } else {
                possible.add(topLevelType);
            }
        }
    }

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    @Override
    public void recomputeCursorPositions(final int startPos) {

        setFirstCursorPosition(startPos);

        if (this.topLevel != null) {
            this.topLevel.recomputeCursorPositions(startPos);
        }
    }

    /**
     * Generates a {@code Formula}.
     *
     * @return {@code null} - this object type should be replaced by an appropriate typed formula container before
     *         generating
     */
    @Override
    public Formula generate() {

        Formula result = null;

        if (this.topLevel != null) {
            final AbstractFormulaObject root = this.topLevel.generate();
            if (root != null) {
                result = new Formula(root);
            }
        }

        return result;
    }

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor             cursor position information
     * @param allowedModifications a set that will be populated with the set of allowed modifications at the specified
     *                             position
     */
    @Override
    public void indicateValidModifications(final FECursor fECursor,
                                           final EnumSet<EModification> allowedModifications) {

        if (this.topLevel == null) {
            final EnumSet<EType> allowed = getAllowedTypes();

            allowedModifications.add(EModification.TYPE);

            if (allowed.contains(EType.BOOLEAN)) {
                allowedModifications.add(EModification.INSERT_BOOLEAN);
            }
            if (allowed.contains(EType.INTEGER)) {
                allowedModifications.add(EModification.INSERT_INTEGER);
            }
            if (allowed.contains(EType.REAL)) {
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
            }
            if (allowed.contains(EType.INTEGER_VECTOR)) {
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
            }
            if (allowed.contains(EType.REAL_VECTOR)) {
                allowedModifications.add(EModification.INSERT_REAL_VECTOR);
            }
            if (allowed.contains(EType.STRING)) {
                allowedModifications.add(EModification.INSERT_STRING);
            }
            if (allowed.contains(EType.SPAN)) {
                allowedModifications.add(EModification.INSERT_SPAN);
            }
        } else {
            this.topLevel.indicateValidModifications(fECursor, allowedModifications);
        }
    }

    /**
     * Processes a typed character. If the top-level child is set, the character is simply passed to that object.
     * Otherwise, valid characters are:
     *
     * <ul>
     * <li>A decimal digit; if this object's type is null, INTEGER, or REAL, this triggers the creation of a
     * {@code FEConstantInt} child object with that digit as its text value.
     * <li>A '.' (radix mark); if this object's type is null or REAL, this triggers the creation of a
     * {@code FEConstantReal} child object with that mark as its text value.
     * <li>'+' or '-'; if this object's type is null, INTEGER, or REAL, this triggers the creation of a
     * {@code FEUnaryOper} child object.
     * <li>'"'; if this object's type is null or SPAN, this triggers the creation of a {@code FEConstantSpan} child
     * object with empty content
     * <li>U+22A4; if this object's type is null or BOOLEAN, this triggers the creation of a {@code FEConstantBoolean}
     * child object with TRUE value
     * <li>U+22A5; if this object's type is null or BOOLEAN, this triggers the creation of a {@code FEConstantBoolean}
     * child object with FALSE value
     * <li>'{'; this triggers the creation of a {@code FEVarRef} child object with an empty variable name
     * <li>'['; this triggers the creation of a {@code FEVector} child object with no entries
     * <li>'('; this triggers the creation of a {@code FEGrouping} child object
     * <li>'U+2720' through 'U+274F'; this triggers the creation of a named function
     * <li>'<'; this triggers the creation of a {@code FETest} child object
     * <li>'*'; this triggers the creation of a {@code FEError} child object
     * </ul>
     *
     * @param fECursor the cursor position and selection range
     * @param ch       the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        final String chStr = Character.toString(ch);
        if ((int) ch >= 0x20 && (int) ch < 0x7F) {
            Log.info("Formula container processing '", chStr, "'");
        } else {
            final String chHex = Integer.toHexString(ch);
            Log.info("Formula container processing '\\u", chHex, "'");
        }

        if (this.topLevel == null) {
            final EnumSet<EType> allowed = getAllowedTypes();

            final int fontSize = getFontSize();

            if ((int) ch >= '0' && (int) ch <= '9') {
                if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                    ++fECursor.cursorPosition;
                    final FEConstantInteger constInt = new FEConstantInteger(fontSize);
                    constInt.setText(chStr, false);
                    setTopLevel(constInt, true);
                }
            } else if ((int) ch == '\u03c0' || (int) ch == '\u0435' || (int) ch == '.') {
                if (allowed.contains(EType.REAL)) {
                    ++fECursor.cursorPosition;
                    final FEConstantReal constReal = new FEConstantReal(fontSize);
                    constReal.setText(chStr, false);
                    setTopLevel(constReal, true);
                }
            } else if ((int) ch == '+' || (int) ch == '-') {
                if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                    ++fECursor.cursorPosition;
                    final FEUnaryOper unary = new FEUnaryOper(fontSize,
                            (int) ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                    setTopLevel(unary, true);
                }
            } else if ((int) ch == '{') {
                ++fECursor.cursorPosition;
                final FEVarRef varRef = new FEVarRef(fontSize);
                final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
                varAllowed.clear();
                varAllowed.addAll(allowed);
                setTopLevel(varRef, true);
            } else if ((int) ch == '"') {
                if (allowed.contains(EType.SPAN)) {
                    ++fECursor.cursorPosition;
                    final FEConstantSpan span = new FEConstantSpan(fontSize);
                    setTopLevel(span, true);
                }
            } else if ((int) ch == '\u22A4' || (int) ch == '\u22A5') {
                if (allowed.contains(EType.BOOLEAN)) {
                    ++fECursor.cursorPosition;
                    final FEConstantBoolean boolValue = new FEConstantBoolean(fontSize, (int) ch == '\u22A4');
                    setTopLevel(boolValue, true);
                }
            } else if ((int) ch == '[') {
                if (allowed.contains(EType.REAL_VECTOR) || allowed.contains(EType.INTEGER_VECTOR)) {
                    ++fECursor.cursorPosition;
                    final FEVector vec = new FEVector(fontSize);
                    if (!allowed.contains(EType.REAL_VECTOR)) {
                        vec.getAllowedTypes().remove(EType.REAL_VECTOR);
                    }
                    setTopLevel(vec, true);
                }
            } else if ((int) ch == '(') {
                ++fECursor.cursorPosition;
                final FEGrouping grouping = new FEGrouping(fontSize);
                final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
                groupingAllowed.clear();
                groupingAllowed.addAll(allowed);
                setTopLevel(grouping, true);
            } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
                final EFunction fxn = EFunction.forChar(ch);
                if (fxn != null) {
                    ++fECursor.cursorPosition;
                    final FEFunction function = new FEFunction(fontSize, fxn);
                    setTopLevel(function, true);
                }
            } else if ((int) ch == '<') {
                ++fECursor.cursorPosition;
                final FETest test = new FETest(fontSize);
                final EnumSet<EType> testAllowed = test.getAllowedTypes();
                testAllowed.clear();
                testAllowed.addAll(allowed);
                setTopLevel(test, true);
            } else if ((int) ch == '*') {
                ++fECursor.cursorPosition;
                final FEConstantError error = new FEConstantError(fontSize);
                setTopLevel(error, true);
            }
        } else {
            // Log.info("Passing character " + ch + " to " + this.topLevel.getClass().getSimpleName());
            this.topLevel.processChar(fECursor, ch);
        }
    }

    /**
     * Processes an insert.
     *
     * @param fECursor the cursor position and selection range
     * @param toInsert the object to insert (never {@code null})
     * @return {@code null} on success; an error message on failure
     */
    @Override
    public String processInsert(final FECursor fECursor, final AbstractFEObject toInsert) {

        final String error;

        if (this.topLevel == null) {
            error = setTopLevel(toInsert, true);
        } else {
            error = "Cannot insert when top-level object is already present.";
        }

        return error;
    }

    /**
     * Called on an object whenever any change is made. On child objects, this simply calls the same method on the
     * parent. On the top-level object, this performs a global update, which performs layout and recomputes all cursor
     * positions.
     *
     * <p>
     * This default implementation simply calls the parent {@code update} method. The top-level tree root class should
     * override to perform actual update operations.
     *
     * @param storeUndo true to store an undo state; false to skip
     */
    @Override
    public void update(final boolean storeUndo) {

        // Log.info("Formula being updated");
        layoutFormula();

        // Update all cursor start positions
        if (this.topLevel != null) {
            this.topLevel.recomputeCursorPositions(0);
        }

        this.owner.setMinimumSize(this.size);
        this.owner.setSize(this.size);
        this.owner.revalidate();
        this.owner.repaint();

        for (final IFormulaEditorListener listener : this.listeners) {
            listener.formulaEdited(this);
        }

        if (storeUndo) {
            this.owner.storeUndo();
        }
    }

    /**
     * Performs layout when something changes. Containers should call this method on child components to set their size
     * and centerline heights, then should set the locations of those child components relative to its own bounding
     * rectangle.
     *
     * <p>
     * It is not required that each component react to internal changes by triggering layout - the controlling UI should
     * re-perform layout after any action that could change any aspect of the formula or a display style.
     *
     * @return the size of the laid out formula
     */
    public Dimension layoutFormula() {

        layout(this.offscreenG2d);

        // Make sure size is large enough for cursor (and high enough for text in our font)

        final FontRenderContext frc = this.offscreenG2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        final float lineAscent = lineMetrics.getAscent();
        final float lineDescent = lineMetrics.getDescent();

        final int ascent = Math.round(lineAscent);
        final int descent = Math.round(lineDescent);

        final int minW = this.insets.left + this.insets.right;
        final int minH = this.insets.top + this.insets.bottom + ascent + descent;

        // Create an offscreen image of the proper size

        final int w = Math.max(minW, this.size.width);
        final int h = Math.max(minH, this.size.height);

        final int curWidth = this.offscreen.getWidth();
        final int curHeight = this.offscreen.getHeight();

        if (w != curWidth || h != curHeight) {

            // Log.info("Offscreen resizing to " + w + "x" + h);

            // Dispose of the old Graphics2D before making a new one
            this.offscreenG2d.dispose();

            this.offscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            this.offscreenG2d = this.offscreen.createGraphics();

            this.offscreenG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        return new Dimension(this.size);
    }

    /**
     * Performs layout when something changes. Containers should call this method on child components to set their size
     * and center line heights, then should set the locations of those child components relative to its own bounding
     * rectangle.
     *
     * <p>
     * It is not required that each component react to internal changes by triggering layout - the controlling UI should
     * re-perform layout after any action that could change any aspect of the formula or a display style.
     *
     * @param g2d the {@code Graphics2D} from which a font render context can be obtained
     */
    @Override
    public void layout(final Graphics2D g2d) {

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        final float lineAscent = lineMetrics.getAscent();
        final float lineDescent = lineMetrics.getDescent();

        final int ascent = Math.round(lineAscent);
        final int descent = Math.round(lineDescent);

        int x = 0;
        final int topY;
        final int botY;

        if (this.topLevel == null) {
            // Set bounds to insets width, and insets height plus a reasonable height for font

            setAdvance(0);
            getOrigin().setLocation(this.insets.left, this.insets.top + ascent);
            getBounds().setBounds(-this.insets.left, -ascent - this.insets.top, this.insets.left + this.insets.right,
                    this.insets.top + this.insets.bottom + ascent + descent);

            topY = -ascent;
            botY = descent;
        } else {
            this.topLevel.layout(g2d);
            x += this.topLevel.getAdvance();

            final Rectangle topBounds = this.topLevel.getBounds();
            topY = Math.min(-ascent, topBounds.y);
            botY = Math.max(descent, topBounds.y + topBounds.height);

            // Origins are still at y = 0 on the baseline - shift them down to make text visible
            this.topLevel.translate(this.insets.left, this.insets.top - topY);
        }

        final float[] lineBaselines = lineMetrics.getBaselineOffsets();
        final int center = Math.round(lineBaselines[Font.CENTER_BASELINE]);

        setAdvance(x);
        setCenterAscent(center);
        getOrigin().setLocation(this.insets.left, this.insets.top);
        getBounds().setBounds(0, topY, x, botY - topY);

        this.size.setSize(this.insets.left + this.insets.right + x, this.insets.top + this.insets.bottom + botY - topY);
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    @Override
    public void translate(final int dx, final int dy) {

        if (this.topLevel != null) {
            this.topLevel.translate(dx, dy);
        }

        getOrigin().translate(dx, dy);
    }

    /**
     * Renders the component. This renders all rendered boxes in this component or its descendants.
     *
     * @param g2d    the {@code Graphics2D} to which to render
     * @param cursor the cursor position and selection range
     */
    @Override
    public void render(final Graphics2D g2d, final FECursor cursor) {

        if (this.topLevel != null) {
            this.topLevel.render(g2d, cursor);
        }
    }

    /**
     * Renders the component.
     *
     * @param cursor   the cursor position and selection range
     * @param hasFocus true if the formula ha focus
     * @param bg       the background color
     */
    public void render(final FECursor cursor, final boolean hasFocus, final Color bg) {

        this.offscreenG2d.setColor(bg);
        final int width = this.offscreen.getWidth();
        final int height = this.offscreen.getHeight();
        this.offscreenG2d.fillRect(0, 0, width, height);

        // Draw the top-level object if present
        if (this.topLevel != null) {
            this.topLevel.render(this.offscreenG2d, cursor);
        }

        // Draw the cursor if focused
        if (hasFocus) {
            final Font font = getFont();
            this.offscreenG2d.setFont(font);
            final FontRenderContext frc = this.offscreenG2d.getFontRenderContext();
            this.offscreenG2d.setColor(RenderedBox.CURSOR_COLOR);

            if (this.topLevel == null) {
                final LineMetrics lineMetrics = font.getLineMetrics("0", frc);
                final float lineAscent = lineMetrics.getAscent();
                final float lineDescent = lineMetrics.getDescent();
                final int ascent = Math.round(lineAscent);
                final int descent = Math.round(lineDescent);
                this.offscreenG2d.fillRect(this.insets.left - 1, this.insets.top, 2, ascent + descent);
            } else {
                final List<RenderedBox> charboxes = new ArrayList<>(10);
                this.topLevel.gatherRenderedBoxes(charboxes);
                final int numBoxes = charboxes.size();

                if (numBoxes == 0) {
                    final LineMetrics lineMetrics = font.getLineMetrics("0", frc);
                    final float lineAscent = lineMetrics.getAscent();
                    final float lineDescent = lineMetrics.getDescent();
                    final int ascent = Math.round(lineAscent);
                    final int descent = Math.round(lineDescent);
                    this.offscreenG2d.fillRect(this.insets.left - 1, this.insets.top, 2, ascent + descent);
                } else if (cursor.cursorPosition == 0) {
                    // Draw cursor at start
                    final RenderedBox box = charboxes.getFirst();
                    final Rectangle boxBounds = box.getBounds();
                    final Point boxOrigin = box.getOrigin();
                    this.offscreenG2d.fillRect(boxOrigin.x + boxBounds.x - 1, boxOrigin.y + boxBounds.y, 2,
                            boxBounds.height);
                } else if (cursor.cursorPosition < numBoxes) {
                    // Draw cursor between boxes
                    final RenderedBox left = charboxes.get(cursor.cursorPosition - 1);
                    final RenderedBox right = charboxes.get(cursor.cursorPosition);

                    if (left.isCursorPhobic() || !right.isCursorPhobic()) {
                        final Rectangle rightBounds = right.getBounds();
                        final Point rightOrigin = right.getOrigin();
                        this.offscreenG2d.fillRect(rightOrigin.x + rightBounds.x - 1, rightOrigin.y + rightBounds.y, 2,
                                rightBounds.height);
                    } else {
                        final Rectangle leftBounds = left.getBounds();
                        final Point leftOrigin = left.getOrigin();
                        this.offscreenG2d.fillRect(leftOrigin.x + leftBounds.x + leftBounds.width - 1,
                                leftOrigin.y + leftBounds.y, 2, leftBounds.height);
                    }
                } else {
                    // Draw cursor at end
                    final RenderedBox box = charboxes.get(numBoxes - 1);
                    final Rectangle boxBounds = box.getBounds();
                    final Point boxOrigin = box.getOrigin();
                    this.offscreenG2d.fillRect(boxOrigin.x + boxBounds.x + boxBounds.width - 1,
                            boxOrigin.y + boxBounds.y, 2, boxBounds.height);
                }
            }
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.topLevel != null) {
            this.topLevel.gatherRenderedBoxes(target);
        }

        final int count = target.size();
        Log.info("There are " + count + " rendered boxes");
    }

    /**
     * Paints this object to a {@code Graphics}.
     *
     * @param g the {@code Graphics} to which to draw
     */
    public void paint(final Graphics g) {

        g.drawImage(this.offscreen, 0, 0, null);
    }

    /**
     * Emits a diagnostic representation of this object.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the indentation level
     */
    @Override
    public void emitDiagnostics(final HtmlBuilder builder, final int indent) {

        indent(builder, indent);
        final AbstractFEObject parent = getParent();
        builder.addln(parent == null ? "Formula*:" : "Formula:");

        if (this.topLevel == null) {
            indent(builder, indent + 1);
            builder.addln("(No top-level object)");
        } else {
            this.topLevel.emitDiagnostics(builder, indent + 1);
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEFormula duplicate() {

        final int fontSize = getFontSize();
        final FEFormula dup = new FEFormula(fontSize, this.owner, this.insets);

        dup.getAllowedTypes().clear();
        final EnumSet<EType> allowedTypes = getAllowedTypes();
        dup.getAllowedTypes().addAll(allowedTypes);

        dup.getPossibleTypes().clear();
        final EnumSet<EType> possibleTypes = getPossibleTypes();
        dup.getPossibleTypes().addAll(possibleTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        if (this.topLevel != null) {
            final AbstractFEObject topDup = this.topLevel.duplicate();
            dup.setTopLevel(topDup, false);
        }
        dup.listeners.addAll(this.listeners);

        return dup;
    }

    /**
     * Gets the text representation of the entire formula.
     *
     * @return the text representation
     */
    public String getText() {

        final List<RenderedBox> charBoxes = new ArrayList<>(10);
        this.topLevel.gatherRenderedBoxes(charBoxes);

        final HtmlBuilder htm = new HtmlBuilder(100);

        for (final RenderedBox box : charBoxes) {

            final String txt = box.getText();
            htm.add(txt);
        }

        return htm.toString();
    }
}
