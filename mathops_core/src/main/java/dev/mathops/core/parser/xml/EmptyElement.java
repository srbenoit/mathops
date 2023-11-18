package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * An empty element, characterized by a tag of the form &lt; ... /&gt;.
 *
 * <p>
 * NOTE: Subclasses a serializable class, so must support serialization.
 */
public final class EmptyElement extends AbstractAttributedElementBase {

    /**
     * Constructs a new {@code EmptyElement}.
     *
     * @param tag  the parsed tag name
     * @param span the tag span for the element's tag
     */
    EmptyElement(final String tag, final TagSpan span) {

        super(tag, span);
    }

    /**
     * Generates the print representation of the element.
     *
     * @param indent the number of spaces to indent the element
     * @return the print representation
     */
    @Override
    public String print(final int indent) {

        final HtmlBuilder xml = openAndPrintAttributes(indent);
        xml.closeEmptyElement(true);

        return xml.toString();
    }
}
