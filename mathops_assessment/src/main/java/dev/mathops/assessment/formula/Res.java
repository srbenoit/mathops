package dev.mathops.assessment.formula;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by AbstractFormulaObjectBase

    /** A resource key. */
    static final String NULL_CHILD_NOT_ALLOWED = key(index++);

    // Used by FormulaSource

    /** A resource key. */
    static final String NESTED_LEFT_BRACKET = key(index++);

    /** A resource key. */
    static final String MISSING_TAG = key(index++);

    /** A resource key. */
    static final String MISSING_CLOSURE = key(index++);

    /** A resource key. */
    static final String MISSING_END_CLOSURE = key(index++);

    /** A resource key. */
    static final String BAD_NAME_START_CHAR = key(index++);

    /** A resource key. */
    static final String BAD_NAME_CHAR = key(index++);

    /** A resource key. */
    static final String CANT_GET_TAG_NAME = key(index++);

    /** A resource key. */
    static final String OPEN_NO_RIGHT_BRACKET = key(index++);

    /** A resource key. */
    static final String UNTERM_COMMENT = key(index++);

    /** A resource key. */
    static final String CLOSE_NO_RIGHT_BRACKET = key(index++);

    /** A resource key. */
    static final String MISSING_CLOSE_TAG = key(index++);

    /** A resource key. */
    static final String BAD_EMPTY_TAG_FORM = key(index++);

    /** A resource key. */
    static final String BAD_NONEMPTY_TAG_FORM = key(index++);

    /** A resource key. */
    static final String MISSING_ATTRIB = key(index++);

    /** A resource key. */
    static final String BAD_ATTRIB_CHAR = key(index++);

    /** A resource key. */
    static final String ATTRIB_NO_EQ = key(index++);

    /** A resource key. */
    static final String ATTRIB_NO_QUOT = key(index++);

    /** A resource key. */
    static final String ATTRIB_NO_CLOSE_QUOT = key(index++);

    /** A resource key. */
    static final String EMPTY_ATTRIB_VALUE = key(index++);

    /** A resource key. */
    static final String BAD_XML_CHAR = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {NULL_CHILD_NOT_ALLOWED, "Null child of formula object not allowed",},

            {NESTED_LEFT_BRACKET, "{0}: '<' is nested within another tag: [{1}]"},
            {MISSING_TAG, "{0}: Unable to locate <{1}> tag."},
            {MISSING_CLOSURE, "{0}: Unable to locate </{1}> ending tag (some other tag missing its closure?)"},
            {MISSING_END_CLOSURE, "{0}: Unable to locate closure of </{1}> ending tag."},
            {BAD_NAME_START_CHAR, "{0}: Invalid character for starting a tag name."},
            {BAD_NAME_CHAR, "{0}: Invalid character for a tag name."},
            {CANT_GET_TAG_NAME, "{0}: Unable to determine the name of this tag."},
            {OPEN_NO_RIGHT_BRACKET, "{0}: Opening tag for {1} has no '>' termination."},
            {UNTERM_COMMENT, "{0}: Unterminated comment."},
            {CLOSE_NO_RIGHT_BRACKET, "{0}: Closing tag has no '>' termination."},
            {MISSING_CLOSE_TAG, "{0}: Unable to find </{1}> closing tag."},
            {BAD_EMPTY_TAG_FORM, "{0}: {1} tag should be of this form: <{1}.../>"},
            {BAD_NONEMPTY_TAG_FORM, "{0}: {1} tag should be of this form: <{1}>...</{1}>"},
            {MISSING_ATTRIB, "{0}: Missing ''{1}'' attribute on {2} tag."},
            {BAD_ATTRIB_CHAR, "{0}: Invalid character in attribute specification for {1} tag."},
            {ATTRIB_NO_EQ, "{0}: Missing '=' following {1} attribute in {2} tag."},
            {ATTRIB_NO_QUOT, "{0}: Attribute value not enclosed in ' or \" quotes in {1} tag."},
            {ATTRIB_NO_CLOSE_QUOT, "{0}: Unable to locate closing quote in {1} tag."},
            {EMPTY_ATTRIB_VALUE, "{0}: Empty ''{1}'' attribute in {2} tag."},
            {BAD_XML_CHAR, "{0}: Found a character ({1}) that is not legal in XML:\n Line {2}, column {3}"},

    };

    /** The singleton instance. */
    private static final Res instance = new Res();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Res() {

        super(Locale.US, EN_US);
    }

    /**
     * Gets the message with a specified key using the current locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    static String get(final String key) {

        return instance.getMsg(key);
    }

    /**
     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format that message pattern with
     * a collection of arguments.
     *
     * @param key       the message key
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    static String fmt(final String key, final Object... arguments) {

        return instance.formatMsg(key, arguments);
    }
}
