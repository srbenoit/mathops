package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The base class for document objects that may contain child objects.
 */
public abstract class AbstractDocContainer extends AbstractDocObjectTemplate {

    /** An empty list to return when the object has no children. */
    private static final List<AbstractDocObjectTemplate> EMPTY_CHILD_LIST =
            Collections.unmodifiableList(new ArrayList<>(0));

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1478175485170603071L;

    /** The tag of the object in the XML file. */
    public String tag;

    /** The set of contained children components. */
    private List<AbstractDocObjectTemplate> children;

    /**
     * Construct a new {@code AbstractDocContainer}.
     */
    AbstractDocContainer() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public abstract AbstractDocContainer deepCopy();

    /**
     * Copy information from a source {@code DocObject} object, including all underlying {@code DocFormattable}
     * information.
     *
     * @param source the {@code DocObject} from which to copy data
     */
    final void copyObjectFromContainer(final AbstractDocContainer source) {

        copyObjectFrom(source);

        this.tag = source.tag;
    }

    /**
     * Sets the rendering scale.
     *
     * @param theScale the scale
     */
    @Override
    public void setScale(final float theScale) {

        super.setScale(theScale);

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                child.setScale(theScale);
            }
        }
    }

    /**
     * This method uncaches the font for an object. It should be used whenever an object's parentage changes, or when a
     * parent's font attributes change. It descends through the tree uncaching fonts of all children.
     */
    @Override
    public final void uncacheFont() {

        super.uncacheFont();

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                child.uncacheFont();
            }
        }
    }

    /**
     * Add a new object.
     *
     * @param comp the {@code DocObject} being added
     */
    public void add(final AbstractDocObjectTemplate comp) {

        comp.setParent(this);
        comp.uncacheFont();

        if (this.children == null) {
            this.children = new ArrayList<>(1);
        }
        this.children.add(comp);
        comp.setScale(getScale());
    }

    /**
     * Get the number of children this object contains.
     *
     * @return the number of children
     */
    public final int numChildren() {

        return this.children == null ? 0 : this.children.size();
    }

    /**
     * Get an iterator over the object children.
     *
     * @return an iterator over the children
     */
    public final List<AbstractDocObjectTemplate> getChildren() {

        return this.children == null ? EMPTY_CHILD_LIST : this.children;
    }

//    /**
//     * Gets the first child.
//     *
//     * @return the first child (null if there are no children)
//     */
//    public final AbstractDocObjectTemplate getFirstChild() {
//
//        return this.children == null || this.children.isEmpty() ? null : this.children.get(0);
//    }

    /**
     * Gets the last child.
     *
     * @return the last child (null if there are no children)
     */
    final AbstractDocObjectTemplate getLastChild() {

        return this.children == null || this.children.isEmpty() ? null : this.children.getLast();
    }

    /**
     * Removes all children.
     */
    final void clearChildren() {

        this.children = null;
    }

    /**
     * Recompute the size of the object's bounding box, and those of its children. This base class method simply calls
     * {@code doLayout} on all children. It will be up to overriding subclasses to set the locations of the children
     * relative to each other.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                child.doLayout(context, mathMode);
            }
        }
    }

    /**
     * Add any parameter names referenced by children of this object to a set of names.
     *
     * @param set the set of parameter names
     */
    final void accumulateChildrenParameterNames(final Set<String> set) {

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                child.accumulateParameterNames(set);
            }
        }
    }

    /**
     * Print the list of contained object tree in HTML format, as a set of nested ordered list tags. The caller is
     * assumed to have created a list item tag that this will print within.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    final void printTreeContents(final PrintStream ps) {

        if (this.children != null) {
            ps.print("<ol>");

            for (final AbstractDocObjectTemplate child : this.children) {
                child.printTree(ps);
            }

            ps.println("</ol>");
        }
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child.isVisible()) {
                    builder.add(child.toString());
                }
            }
        }

        return builder.toString();
    }

    /**
     * Handler for mouse clicks. Mouse clicks are propagated to all children, with the coordinates being adjusted to the
     * child's frame. Input objects should gain/lose focus based on clicks, and other reactive objects can respond to
     * them as needed.
     *
     * @param xCoord     the X coordinate (in the object's coordinate system)
     * @param yCoord     the Y coordinate (in the object's coordinate system)
     * @param clickCount 1 for single click, 2 for double-click, and so on
     * @param context    the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    public boolean processMouseClick(final int xCoord, final int yCoord, final int clickCount,
                                     final EvalContext context) {

        boolean repaint = false;

        // Propagate clicks to all children
        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer childContainer) {
                    final int childx = child.getX();
                    final int childy = child.getY();

                    if (childContainer.processMouseClick(xCoord - childx, yCoord - childy, clickCount, context)) {
                        repaint = true;
                    }
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null) {
                        final int childx = span.getX();
                        final int childy = span.getY();

                        if (span.processMouseClick(xCoord - childx, yCoord - childy, clickCount, context)) {
                            repaint = true;
                        }
                    }
                }
            }
        }

        return repaint;
    }

    /**
     * Handler for mouse press actions. Mouse presses are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the beginning of drag sequences.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    public boolean processMousePress(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;

        // Give children the chance to consume the click without passing it to other components
        if (this.children != null && !consumeMousePress(xCoord, yCoord, context)) {

            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer container) {
                    final int childx = child.getX();
                    final int childy = child.getY();

                    if (container.processMousePress(xCoord - childx, yCoord - childy, context)) {
                        repaint = true;
                    }
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null) {
                        final int childx = span.getX();
                        final int childy = span.getY();

                        if (span.processMousePress(xCoord - childx, yCoord - childy, context)) {
                            repaint = true;
                        }
                    }
                }
            }
        }

        return repaint;
    }

    /**
     * Default implementation to test whether a mouse press should be consumed.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return false, meaning the mouse press is not consumed
     */
    boolean consumeMousePress(final int xCoord, final int yCoord, final EvalContext context) {

        boolean consumed = false;

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer container) {
                    final int childx = child.getX();
                    final int childy = child.getY();

                    if (container.consumeMousePress(xCoord - childx, yCoord - childy, context)) {
                        consumed = true;
                        break;
                    }
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null) {
                        final int childx = span.getX();
                        final int childy = span.getY();

                        if (span.consumeMousePress(xCoord - childx, yCoord - childy, context)) {
                            consumed = true;
                            break;
                        }
                    }
                }
            }
        }

        return consumed;
    }

    /**
     * Handler for mouse release actions. Mouse releases are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the end of drag sequences.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    public boolean processMouseRelease(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;

        // Propagate releases to all children
        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer container) {
                    final int childx = child.getX();
                    final int childy = child.getY();

                    if (container.processMouseRelease(xCoord - childx, yCoord - childy, context)) {
                        repaint = true;
                    }
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null) {
                        final int childx = span.getX();
                        final int childy = span.getY();

                        if (span.processMouseRelease(xCoord - childx, yCoord - childy, context)) {
                            repaint = true;
                        }
                    }
                }
            }
        }

        return repaint;
    }

    /**
     * Handler for key presses. Keys are propagated to all children, but only children who have focus should react to
     * them.
     *
     * @param keyChar   the key character
     * @param keyCode   the key code
     * @param modifiers modifiers (CTRL, ALT, SHIFT, etc.) to the key press
     * @param context   the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    boolean processKey(final char keyChar, final int keyCode, final int modifiers, final EvalContext context) {

        boolean repaint = false;

        // Propagate keys to all children
        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer container
                        && container.processKey(keyChar, keyCode, modifiers, context)) {
                    repaint = true;
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null && span.processKey(keyChar, keyCode, modifiers, context)) {
                        repaint = true;
                    }
                }
            }
        }

        return repaint;
    }

    /**
     * Handler for mouse drag events. Mouse drags are propagated to all children, with the coordinates being adjusted to
     * the child's frame. Input objects should support selection of a range of objects.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    public boolean processMouseDrag(final int xCoord, final int yCoord, final EvalContext context) {

        int childx;
        int childy;
        boolean repaint = false;

        // Propagate drags to all children
        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof AbstractDocContainer) {
                    childx = child.getX();
                    childy = child.getY();

                    if (((AbstractDocContainer) child).processMouseDrag(xCoord - childx,
                            yCoord - childy, context)) {
                        repaint = true;
                    }
                } else if (child instanceof final DocParameterReference ref) {
                    final DocSimpleSpan span = ref.getLaidOutContents();
                    if (span != null) {
                        childx = span.getX();
                        childy = span.getY();

                        if (span.processMouseDrag(xCoord - childx, yCoord - childy, context)) {
                            repaint = true;
                        }
                    }
                }
            }
        }

        return repaint;
    }

    /**
     * Gather all the DocInput objects contained in the object, including the object itself.
     *
     * @param inputs the list to which to add found inputs
     */
    public final void accumulateInputs(final List<AbstractDocInput> inputs) {

        if (this instanceof final AbstractDocInput inp) {
            inputs.add(inp);
        }

        if (this.children != null) {
            for (final AbstractDocObjectTemplate child : this.children) {
                if (child instanceof final AbstractDocContainer contain) {
                    contain.accumulateInputs(inputs);
                }
            }
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int innerHashCode() {

        return docObjectHashCode() + Objects.hashCode(this.tag)
                + Objects.hashCode(this.children);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean innerEquals(final AbstractDocContainer obj) {

        return docObjectEquals(obj)
                && Objects.equals(this.tag, obj.tag)
                && Objects.equals(this.children, obj.children);
    }
}
