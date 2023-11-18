package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * An interface for objects that can be serialized as XML elements. Such types should also include a public constructor
 * or static factory method that accepts an {@code IElement} and parses the generated content back into its original
 * value.
 */
@FunctionalInterface
public interface IXmlObj {

    /**
     * Appends the XML representation of the object to an {@code IXmlBuilder}.
     *
     * @param indent the number of spaces by which to indent the content
     * @param xml    the {@code IXmlBuilder} to which to append
     */
    void appendXml(int indent, HtmlBuilder xml);
}
