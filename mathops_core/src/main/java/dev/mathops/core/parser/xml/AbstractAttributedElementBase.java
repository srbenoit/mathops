package dev.mathops.core.parser.xml;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Adds management of attributes to an {@code AbstractElementBase}.
 */
public abstract class AbstractAttributedElementBase implements IElement {

    /** String representation of true. */
    private static final String TRUE = "true";

    /** String representation of false. */
    private static final String FALSE = "false";

    /** The parsed tag name. */
    private final String tagName;

    /** The tag span of the element. */
    private final TagSpan tagSpan;

    /** Errors associated with the element. */
    private List<XmlContentError> errors;

    /** A map from attribute name to attribute. */
    private final Map<String, Attribute> attributes;

    /**
     * Constructs a new {@code AbstractElementBase}.
     *
     * @param tag  the parsed tag name
     * @param span the tag span for the element's tag
     */
    AbstractAttributedElementBase(final String tag, final TagSpan span) {

        this.tagName = tag;
        this.tagSpan = span;

        this.attributes = new TreeMap<>();
    }

    /**
     * Gets the tag name for this element.
     *
     * @return the tag name
     */
    @Override
    public final String getTagName() {

        return this.tagName;
    }

    /**
     * Gets the start position of the element tag.
     *
     * @return the start position
     */
    @Override
    public final int getStart() {

        return this.tagSpan.getStart();
    }

    /**
     * Gets the end position of the element tag.
     *
     * @return the end position
     */
    @Override
    public final int getEnd() {

        return this.tagSpan.getEnd();
    }

    /**
     * Gets the line number of the start of the span.
     *
     * @return the line number (the first line in the file is line 1)
     */
    @Override
    public final int getLineNumber() {

        return this.tagSpan.getLineNumber();
    }

    /**
     * Gets the column within its line of the start of the span.
     *
     * @return the column (the first character in the line is column 1)
     */
    @Override
    public final int getColumn() {

        return this.tagSpan.getColumn();
    }

    /**
     * Gets the tag span this element is based on.
     *
     * @return the tag span
     */
    @Override
    public final TagSpan getTagSpan() {

        return this.tagSpan;
    }

    /**
     * Logs an error message to the element.
     *
     * @param msg the message
     */
    @Override
    public final void logError(final String msg) {

        if (this.errors == null) {
            this.errors = new ArrayList<>(2);
        }
        this.errors.add(new XmlContentError(this, msg));
        // Log.warning(msg);
    }

    /**
     * Gets the list of errors.
     *
     * @return the list of errors; {@code null} if no errors have been logged
     */
    @Override
    public final List<XmlContentError> getErrors() {

        return this.errors;
    }

    /**
     * Recursively accumulates all errors in this node and its descendants into a list. Errors are accumulated by
     * pre-order depth-first traversal.
     *
     * @param allErrors the list into which to accumulate errors
     */
    @Override
    public void accumulateErrors(final List<? super XmlContentError> allErrors) {

        if (this.errors != null) {
            allErrors.addAll(this.errors);
        }
    }

    /**
     * Tests whether an attribute is present.
     *
     * @param name the attribute name
     * @return {@code true} if the attribute is present, {@code false} if not
     */
    @Override
    public final boolean hasAttribute(final String name) {

        return this.attributes.containsKey(name);
    }

    /**
     * Gets an attribute.
     *
     * @param name the attribute name
     * @return the attribute, if present, or {@code null} if not
     */
    @Override
    public final Attribute getAttribute(final String name) {

        return this.attributes.get(name);
    }

    /**
     * Gets an attribute that is mandatory. If the attribute is not present, a parsing exception is thrown.
     *
     * @param name the attribute name
     * @return the attribute, if present, or {@code null} if not
     * @throws ParsingException if the attribute is not present
     */
    @Override
    public final Attribute getRequiredAttribute(final String name) throws ParsingException {

        final Attribute attr = this.attributes.get(name);

        if (attr == null) {
            throw new ParsingException(this.tagSpan, Res.fmt(Res.MISS_ATTR, name, this.tagName));
        }

        return attr;
    }

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
    @Override
    public final Boolean getBooleanAttr(final String name, final Boolean defValue)
            throws ParsingException {

        final Attribute attr = getAttribute(name);
        final Boolean result;

        if (attr == null) {
            result = defValue;
        } else {
            final String value = attr.value;
            if (TRUE.equalsIgnoreCase(value)) {
                result = Boolean.TRUE;
            } else if (FALSE.equalsIgnoreCase(value)) {
                result = Boolean.FALSE;
            } else {
                throw new ParsingException(attr, Res.get(Res.BOOLEAN_ERR));
            }
        }

        return result;
    }

    /**
     * Gets a required boolean attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a boolean
     */
    @Override
    public final Boolean getRequiredBooleanAttr(final String name) throws ParsingException {

        final Attribute attr = getRequiredAttribute(name);
        final Boolean result;

        final String value = attr.value;
        if (TRUE.equalsIgnoreCase(value)) {
            result = Boolean.TRUE;
        } else if (FALSE.equalsIgnoreCase(value)) {
            result = Boolean.FALSE;
        } else {
            throw new ParsingException(attr, Res.get(Res.BOOLEAN_ERR));
        }

        return result;
    }

    /**
     * Gets a non-required string attribute value. If the attribute is not defined in the element, a {@code null} value
     * is returned.
     *
     * @param name the attribute name
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     */
    @Override
    public final String getStringAttr(final String name) {

        return getStringAttr(name, null);
    }

    /**
     * Gets a non-required string attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     */
    @Override
    public final String getStringAttr(final String name, final String defValue) {

        final Attribute theAttrib = this.attributes.get(name);

        return (theAttrib == null) ? defValue : theAttrib.value;
    }

    /**
     * Gets a required string attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name the attribute name
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is missing
     */
    @Override
    public final String getRequiredStringAttr(final String name) throws ParsingException {

        return getRequiredAttribute(name).value;
    }

    /**
     * Gets a non-required Integer attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as an integer
     */
    @Override
    public final Integer getIntegerAttr(final String name, final Integer defValue)
            throws ParsingException {

        final Attribute attr = getAttribute(name);
        final Integer result;

        if (attr == null) {
            result = defValue;
        } else {
            try {
                result = Integer.valueOf(attr.value);
            } catch (final NumberFormatException ex) {
                throw new ParsingException(attr, Res.get(Res.INT_ERR), ex);
            }
        }

        return result;
    }

    /**
     * Gets a non-required Long attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as an integer
     */
    @Override
    public final Long getLongAttr(final String name, final Long defValue) throws ParsingException {

        final Attribute attr = getAttribute(name);
        final Long result;

        if (attr == null) {
            result = defValue;
        } else {
            try {
                result = Long.valueOf(attr.value);
            } catch (final NumberFormatException ex) {
                throw new ParsingException(attr, Res.get(Res.INT_ERR), ex);
            }
        }

        return result;
    }

    /**
     * Gets a required Integer attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as an integer
     */
    @Override
    public final Integer getRequiredIntegerAttr(final String name) throws ParsingException {

        final Attribute attr = getRequiredAttribute(name);
        final Integer result;

        try {
            result = Integer.valueOf(attr.value);
        } catch (final NumberFormatException ex) {
            throw new ParsingException(attr, Res.get(Res.INT_ERR), ex);
        }

        return result;
    }

    /**
     * Gets a required Long attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as an integer
     */
    @Override
    public final Long getRequiredLongAttr(final String name) throws ParsingException {

        final Attribute attr = getRequiredAttribute(name);
        final Long result;

        try {
            result = Long.valueOf(attr.value);
        } catch (final NumberFormatException ex) {
            throw new ParsingException(attr, Res.get(Res.INT_ERR), ex);
        }

        return result;
    }

    /**
     * Gets a non-required real attribute value. If the attribute is not defined in the element, a default value is
     * returned.
     *
     * @param name     the attribute name
     * @param defValue the default value to return if the attribute is not present
     * @return the resulting value (will return {@code null} only of the default value is {@code null} and the attribute
     *         is not present)
     * @throws ParsingException if the value is present but could not be parsed as a double
     */
    @Override
    public final Double getDoubleAttr(final String name, final Double defValue) throws ParsingException {

        final Attribute attr = getAttribute(name);
        final Double result;

        if (attr == null) {
            result = defValue;
        } else {
            try {
                result = Double.valueOf(attr.value);
            } catch (final NumberFormatException ex) {
                throw new ParsingException(attr, Res.get(Res.FLOAT_ERR), ex);
            }
        }

        return result;
    }

    /**
     * Gets a required real attribute value.
     *
     * @param name the attribute name
     * @return the resulting value (will never return {@code null})
     * @throws ParsingException if the value is missing or could not be parsed as a double
     */
    @Override
    public final Double getRequiredDoubleAttr(final String name) throws ParsingException {

        final Attribute attr = getRequiredAttribute(name);
        final Double result;

        try {
            result = Double.valueOf(attr.value);
        } catch (final NumberFormatException ex) {
            throw new ParsingException(attr, Res.get(Res.LONG_ERR), ex);
        }

        return result;
    }

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
    @Override
    public final LocalDateTime getLocalDateTimeAttr(final String name, final LocalDateTime defValue)
            throws ParsingException {

        final Attribute attr = getAttribute(name);
        final LocalDateTime result;

        if (attr == null) {
            result = defValue;
        } else {
            final String value = attr.value;
            try {
                result = LocalDateTime.parse(value);
            } catch (final DateTimeParseException ex) {
                throw new ParsingException(ex);
            }
        }

        return result;
    }

    /**
     * Sets the attribute associated with a name.
     *
     * @param attrib the attribute ({@code null} to remove the attribute from the element)
     * @return the attribute previously associated with the name
     */
    @Override
    public final Attribute putAttribute(final Attribute attrib) {

        return this.attributes.put(attrib.name, attrib);
    }

    /**
     * Gets the set of attribute names.
     *
     * @return the set of attribute names
     */
    @Override
    public final Set<String> attributeNames() {

        return Collections.unmodifiableSet(this.attributes.keySet());
    }

    /**
     * Creates a new {@code HtmlBuilder}, then prints the opening of a tag and the list of attributes to a
     * {@code HtmlBuilder}.
     *
     * @param indent the number of spaces to indent the tag
     * @return the generated {@code HtmlBuilder}
     */
    final HtmlBuilder openAndPrintAttributes(final int indent) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.openElement(indent, this.tagName);

        for (final String name : attributeNames()) {
            xml.add(CoreConstants.SPC, name, "='", XmlEscaper.escape(getAttribute(name).value), "'");
        }

        return xml;
    }
}
