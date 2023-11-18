package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.ParsingException;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * An interface implemented by elements.
 */
public interface IElement extends INode {

    /**
     * Gets the tag name for this element.
     *
     * @return the tag name
     */
    String getTagName();

    /**
     * Gets the tag span this element is based on.
     *
     * @return the tag span
     */
    TagSpan getTagSpan();

    /**
     * Tests whether an attribute is present.
     *
     * @param name the attribute name
     * @return {@code true} if the attribute is present, {@code false} if not
     */
    boolean hasAttribute(String name);

    /**
     * Gets an attribute.
     *
     * @param name the attribute name
     * @return the attribute, if present, or {@code null} if not
     */
    Attribute getAttribute(String name);

    /**
     * Gets an attribute that is mandatory. If the attribute is not present, a parsing exception is thrown.
     *
     * @param name the attribute name
     * @return the attribute, if present, or {@code null} if not
     * @throws ParsingException if the attribute is not present
     */
    Attribute getRequiredAttribute(String name) throws ParsingException;

    /**
     * Sets the attribute associated with a name.
     *
     * @param attrib the attribute ({@code null} to remove the attribute from the element)
     * @return the attribute previously associated with the name
     */
    Attribute putAttribute(Attribute attrib);

    /**
     * Gets the set of attribute names.
     *
     * @return the set of attribute names
     */
    Set<String> attributeNames();

    /**
     * Gets a non-required string attribute value. If the attribute is not defined in the element, a {@code null} value
     * is returned.
     *
     * @param name the attribute name
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     */
    String getStringAttr(String name);

    /**
     * Gets a non-required string attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     */
    String getStringAttr(String name, String defValue);

    /**
     * Gets a required string attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name the attribute name
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is missing
     */
    String getRequiredStringAttr(String name) throws ParsingException;

    /**
     * Gets a non-required Integer attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a long
     */
    Integer getIntegerAttr(String name, Integer defValue) throws ParsingException;

    /**
     * Gets a non-required Long attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a long
     */
    Long getLongAttr(String name, Long defValue) throws ParsingException;

    /**
     * Gets a required Integer attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a long
     */
    Integer getRequiredIntegerAttr(String name) throws ParsingException;

    /**
     * Gets a required Long attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a long
     */
    Long getRequiredLongAttr(String name) throws ParsingException;

    /**
     * Gets a non-required double attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a double
     */
    Double getDoubleAttr(String name, Double defValue) throws ParsingException;

    /**
     * Gets a required double attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a double
     */
    Double getRequiredDoubleAttr(String name) throws ParsingException;

    /**
     * Gets a non-required boolean attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a boolean
     */
    Boolean getBooleanAttr(String name, Boolean defValue) throws ParsingException;

    /**
     * Gets a required boolean attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a boolean
     */
    Boolean getRequiredBooleanAttr(String name) throws ParsingException;

    /**
     * Gets a non-required date/time attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a date/time
     */
    LocalDateTime getLocalDateTimeAttr(String name, LocalDateTime defValue) throws ParsingException;
}
