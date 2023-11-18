package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * An element, characterized by a pair of tags of the form &lt; ... &gt; &lt;/ ... &gt;.
 *
 * <p>
 * NOTE: Subclasses a serializable class, so must support serialization.
 */
public final class NonemptyElement extends AbstractAttributedElementBase {

    /** The estimated number of children, for list allocation. */
    private static final int EST_CHILDREN = 10;

    /** The list of children of this element. */
    private final List<INode> children;

    /** The CDATA, character, whitespace, and reference tokens within this element. */
    private final List<IXmlToken> tokens;

    /** The closing tag span of the element. */
    private TagSpan closingTagSpan;

    /**
     * Constructs a new {@code Element}.
     *
     * @param tag  the parsed tag name
     * @param span the tag span for the open tag
     */
    NonemptyElement(final String tag, final TagSpan span) {

        super(tag, span);

        this.children = new ArrayList<>(EST_CHILDREN);
        this.tokens = new ArrayList<>(EST_CHILDREN);
        this.closingTagSpan = null;
    }

    /**
     * Gets the number of children of this element.
     *
     * @return the number of children
     */
    public int getNumChildren() {

        return this.children.size();
    }

    /**
     * Gets a particular child of this element.
     *
     * @param index the index of the child
     * @return the child node
     */
    public INode getChild(final int index) {

        return this.children.get(index);
    }

    /**
     * Gets a list of the children of this element.
     *
     * @return the list of child nodes
     */
    public List<INode> getChildrenAsList() {

        return this.children;
    }

    /**
     * Gets a list of the element children of this element.
     *
     * @return the list of element child nodes
     */
    public List<IElement> getElementChildrenAsList() {

        final List<IElement> result = new ArrayList<>(this.children.size());

        for (final INode child : this.children) {
            if (child instanceof IElement) {
                result.add((IElement) child);
            }
        }

        return result;
    }

    /**
     * Gets a list of the nonempty element children of this element.
     *
     * @return the list of nonempty element child nodes
     */
    public List<NonemptyElement> getNonemptyElementChildren() {

        final List<NonemptyElement> result = new ArrayList<>(this.children.size());

        for (final INode child : this.children) {
            if (child instanceof NonemptyElement) {
                result.add((NonemptyElement) child);
            }
        }

        return result;
    }

    /**
     * Sets the tag span for the closing tag for the element.
     *
     * @param span the closing tag span
     */
    void setClosingTagSpan(final TagSpan span) {

        this.closingTagSpan = span;
    }

    /**
     * Gets the tag span for the closing tag for the element.
     *
     * @return the closing tag span
     */
    public TagSpan getClosingTagSpan() {

        return this.closingTagSpan;
    }

    /**
     * Adds a token to the element.
     *
     * @param token the token
     */
    void addToken(final IXmlToken token) {

        this.tokens.add(token);
    }

    /**
     * Generates the print representation of the element (may recursively call this method on child nodes).
     *
     * @param indent the number of spaces to indent the element
     * @return the print representation
     */
    @Override
    public String print(final int indent) {

        final HtmlBuilder xml = openAndPrintAttributes(indent);
        xml.endOpenElement(true);

        // Append children
        for (final INode child : this.children) {
            xml.add(child.print(indent + 1));
        }

        xml.endNonempty(indent, getTagName(), false);

        return xml.toString();
    }

    /**
     * Recursively accumulates all errors in this node and its descendants into a list. Errors are accumulated by
     * pre-order depth-first traversal.
     *
     * @param allErrors the list into which to accumulate errors
     */
    @Override
    public void accumulateErrors(final List<? super XmlContentError> allErrors) {

        super.accumulateErrors(allErrors);

        for (final INode child : this.children) {
            child.accumulateErrors(allErrors);
        }
    }
}
