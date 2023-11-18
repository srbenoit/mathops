package dev.mathops.core.parser.xml;

/**
 * The interface nodes that have content (CData and Comment).
 */
interface IContentNode extends INode {

    /**
     * Gets the content.
     *
     * @return the content
     */
    String getContent();
}
