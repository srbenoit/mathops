package dev.mathops.assessment.formula;

import dev.mathops.commons.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The contents of an XML source file. It contains a copy of the entire file, as a {@code String}, and a list of errors
 * that were encountered while parsing the content.<br>
 * <br>
 * This class contains several methods for dealing with the XML contents of the file. These include validating the XML
 * header, finding the location of a particular tag in the file, finding the next tag following a certain position,
 * regardless of that tag's type, and locating attribute values within tags.<br>
 * <br>
 * Note that none of the searching methods in this class return their results as {@code String}s or {@code Object}s.
 * Rather, they simply return positions and ranges of characters within the XML source file. This is to allow parsers to
 * parse data "in-place", preserving the global position of all errors that occur. This allows a "compiler-style" view
 * of the parsed file with all errors displayed to the user with their exact location indicated, rather than general
 * error messages that force the user to search for the problem.<br>
 * <br>
 * <b>Usage</b><br>
 * To use this class, a caller would first identify where the source XML is coming from. This can be a {@code File}, a
 * {@code URL}, or a {@code String}. If the source is a {@code String}, there is a constructor to install the XML
 * directly in a new {@code XmlSource} object. If the source is a {@code File} or {@code URL}, a new {@code XmlSource}
 * object is created with the default constructor, then one of the load methods is called to read the XML from the
 * external resource.<br>
 * <br>
 * Once XML has been loaded, the caller may optionally call the {@code validateXmlHeader} method to test whether the
 * header in the XML file is valid and matches the expected document type and version. Any problems in the XML header
 * are logged in the errors list.<br>
 * <br>
 * When this class has the XML loaded, its various search methods can be used to navigate through the tags and
 * attributes in the XML. All of these methods accept as input some search range that defines the region of the document
 * in which to search, and all produce set of character positions as output, indicating the location of the item if
 * found.<br>
 * <br>
 * The {@code searchForTag} method finds the first instance of a particular tag in a search region, returning the start
 * and end positions of both the opening and closing tag.<br>
 * <br>
 * The {@code searchForNextTag} method finds the first tag in a search region regardless of that tag's mName.<br>
 * <br>
 * Finally, the {@code searchForAttribute} method searches for a {@code name='value'} or {@code name="value"} attribute
 * definition in a search region, which is most likely the extents of an opening tag.<br>
 * <br>
 * <b>Error Reporting</b><br>
 * All operations performed by this class that can result in an error condition will log those errors as
 * {@code ParseError} objects stored in the contained {@code ErrorList}. Any method that can return an error condition
 * will log an error to this list before doing so. Every attempt is made to include in these error reports an accurate
 * location or context for the error. See the {@code ParseError} class for more information.
 */
final class FormulaSource {

    /** Return code from search methods if target was found. */
    private static final int FOUND = 1;

    /** Return code from search methods if target was not found. */
    private static final int NOT_FOUND = 2;

    /** Return code from search methods if an error occurred. */
    private static final int ERROR = 3;

    /** The size of the read buffer used to improve read performance. */
    private static final int READ_BUFFER_SIZE = 256;

    /** The string that marks the start of a comment. */
    private static final char[] COMMENT_START = "<!--".toCharArray();

    /** The string that marks the end of a comment. */
    private static final char[] COMMENT_END = "-->".toCharArray();

    /** The string that marks the beginning of a closing tag. */
    private static final char[] END_TAG_START = "</".toCharArray();

    /** The string that marks the beginning of a closing tag. */
    private static final char[] EMPTY_TAG_END = "/>".toCharArray();

    /** The origin of the data (URL, filename, other). */
    private String origin;

    /** The XML source, as loaded from a file/stream/URL. */
    private String xml;

    /** A list of messages associated with parsing of the XML. */
    private final List<MessageInt> messages;

    /**
     * Constructs a new {@code XmlSource} with no XML.
     */
    FormulaSource() {

        this.messages = new ArrayList<>(10);
    }

    /**
     * Sets the XML content of the file from a {@code String}.
     *
     * @param xmlContent the source XML as a {@code String}
     * @return {@code true} if the {@code String} contained no illegal characters for XML, {@code false} if it contained
     *         illegal characters (XML will not be stored if illegal characters are present)
     */
    boolean setXml(final String xmlContent) {

        final boolean result;

        if (xmlContent == null) {
            this.origin = null;
            this.xml = null;

            result = true;
        } else {
            this.origin = "String";
            this.xml = xmlContent;

            result = validateXmlCharacters();
        }

        return result;
    }

    /**
     * Gets the origin of the XML.
     *
     * @return the origin of the data (URL, filename, other)
     */
    public String getOrigin() {

        return this.origin;
    }

    /**
     * Gets the source XML.
     *
     * @return the XML as a {@code String} ({@code null} if empty)
     */
    public String getXml() {

        return this.xml;
    }

    /**
     * Loads the XML source from a {@code File}. Any errors that occur will be recorded in the errors list.
     *
     * @param file the {@code File} from which to load the XML source
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean loadFromFile(final File file) {

        boolean result = false;

        this.origin = file.getAbsolutePath();

        try (final InputStream input = new FileInputStream(file)) {
            result = loadFromStream(input);
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Loads the XML source from an {@code InputStream}. This is a utility method called by the methods that load XML
     * from a {@code File} or a {@code URL}, and may be called by other loader methods in the future. Any errors that
     * occur will be recorded in the errors list.<br>
     * <br>
     * Note that this method does not take into consideration the encoding specification (if any) in the <?xml ... ?>
     * tag. It assumes that the document is encoded in UTF-8 or UTF-16 (normal ASCII text is a subset of UTF-8).
     *
     * @param input the {@code InputStream} from which to load the source
     * @return {@code true} if successful, {@code false} otherwise
     */
    private boolean loadFromStream(final InputStream input) {

        boolean result = false;
        final byte[] buffer = new byte[READ_BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        this.xml = null;

        // Read the XML from the stream, storing it into the byte array
        try {
            int len = input.read(buffer);

            while (len > 0) {
                baos.write(buffer, 0, len);
                len = input.read(buffer);
            }

            // Convert the byte array to a char array (this may convert multi-byte Unicode values
            // into single characters, so the result may have fewer characters than bytes in the
            // source file).
            this.xml = baos.toString(StandardCharsets.UTF_8);

            // Now, we scrutinize the XML that we read to ensure each character is a valid XML
            // character. (no control characters except whitespace).
            result = validateXmlCharacters();
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Searches a specified range of the XML file the first occurrence of a particular tag, skipping any comments we
     * find (if the comment has a tag in it, it must be ignored), then go on to find the matching end tag. Any errors
     * are logged as they are encountered.
     *
     * @param searchRange a 2-element integer array, in which element [0] holds the position at which we should start
     *                    searching, and [1] holds the end of the range we should search
     * @param result      a 4-element integer array - on successful return, [0] holds the position of the beginning of
     *                    the tag; if the tag is a container tag, [1] holds the position of the end of the opening tag,
     *                    [2] holds the position of the beginning of the closing tag, and [3] holds the position of the
     *                    end of the closing tag; if the tag is an empty tag, [1], [2] and [3] all point to the end of
     *                    the tag
     * @param tagName     the name of the tag to search for
     * @param isMandatory {@code true} if it is an error not to locate the tag; {@code false} if the tag is not required
     *                    to be there
     * @return one of FOUND, NOT_FOUND or ERROR
     */
    private int searchForTag(final int[] searchRange, final int[] result, final String tagName,
                             final boolean isMandatory) {

        int lookingFor = 0;
        int currentPos = searchRange[0];
        boolean inTag = false;
        char quote;

        // Scan the file looking for the tag, ignoring comments
        while (currentPos < searchRange[1] && lookingFor < 4) {

            // Skip runs of whitespace
            currentPos = skipWhitespace(currentPos, searchRange);

            if (currentPos >= searchRange[1]) {
                break;
            }

            final char chr = this.xml.charAt(currentPos);

            if (chr == '<') {

                if (inTag) {
                    final int min = Math.max(0, currentPos - 20);
                    final int max = Math.min(this.xml.length() - 1, currentPos + 20);
                    return parseError(Res.fmt(Res.NESTED_LEFT_BRACKET, this.origin, this.xml.substring(min, max)),
                            currentPos);
                }

                // See if this is the beginning of a comment, and if so, search immediately for the
                // end of the comment.
                if (equals(COMMENT_START, currentPos, searchRange)) {
                    currentPos += 4;

                    while (currentPos < searchRange[1]) {

                        if (equals(COMMENT_END, currentPos, searchRange)) {
                            currentPos += 3;
                            break;
                        }

                        ++currentPos;
                    }
                } else {

                    // Not a comment, so record position of '<'
                    result[lookingFor] = currentPos;

                    if (equals(END_TAG_START, currentPos, searchRange)) {

                        // We are entering a closing tag like </...>
                        inTag = true;
                        currentPos += (this.xml.charAt(currentPos + 1) == 0) ? 5 : 2;

                        // See if looking for closing tag
                        if (lookingFor == 2) {

                            // Permit whitespace between '</' and tag name
                            currentPos = skipWhitespace(currentPos, searchRange);

                            // See if the tag name is what we're looking for
                            if (equals(tagName, currentPos, searchRange)) {
                                currentPos += tagName.length();
                                currentPos = skipWhitespace(currentPos, searchRange);

                                // If it matches, see that we didn't just match
                                // the first part of a longer tag name.
                                if ((currentPos < searchRange[1])
                                        && (this.xml.charAt(currentPos) == '>')) {
                                    result[3] = currentPos;
                                    lookingFor = 4; // Found closure tag
                                }
                            }
                        }
                    } else {

                        // We are entering an opening tag like <...>
                        inTag = true;
                        ++currentPos;

                        // See if looking for opening tag
                        if (lookingFor == 0) {

                            // Permit whitespace between '<' and tag name
                            currentPos = skipWhitespace(currentPos, searchRange);

                            // See if the tag name is what we're looking for
                            if (equals(tagName, currentPos, searchRange)) {
                                currentPos += tagName.length();

                                // If it matches, see that we didn't just match
                                // the first part of a longer tag name.
                                if ((currentPos < searchRange[1]) && (isXmlWhitespace(this.xml.charAt(currentPos))
                                        || (this.xml.charAt(currentPos) == '>')
                                        || (this.xml.charAt(currentPos) == '/'))) {
                                    lookingFor = 1; // Found opening tag
                                }
                            }
                        }
                    }
                }
            } else if (inTag) {

                if (chr == '>') {

                    if (lookingFor == 1 && this.xml.charAt(currentPos - 1) == '/') {

                        // Opening tag closed with '/>' so it's an empty tag.
                        result[1] = currentPos;
                        result[2] = currentPos;
                        result[3] = currentPos;
                        lookingFor = 4;
                    } else if (lookingFor == 1 || lookingFor == 3) {

                        // Found closure, so move on.
                        result[lookingFor] = currentPos;
                        ++lookingFor;
                    }

                    inTag = false;
                } else if (chr == '\'' || chr == '"') {

                    // Entering a quoted region, seek immediately to its end.
                    quote = chr;
                    ++currentPos;

                    while (currentPos < searchRange[1]) {

                        if (this.xml.charAt(currentPos) == quote) {
                            ++currentPos;
                            break;
                        }

                        ++currentPos;
                    }
                } else {
                    ++currentPos;
                }
            } else {
                ++currentPos;
            }
        }

        final int code;

        if (lookingFor == 4) {
            code = FOUND;
        } else if (isMandatory) {
            final String errorMsg = switch (lookingFor) {
                case 0 -> Res.fmt(Res.MISSING_TAG, this.origin, tagName);
                case 1, 2 -> Res.fmt(Res.MISSING_CLOSURE, this.origin, tagName);
                default -> Res.fmt(Res.MISSING_END_CLOSURE, this.origin, tagName);
            };

            code = parseError(errorMsg, searchRange[0], searchRange[1]);
        } else {
            code = NOT_FOUND;
        }

        return code;
    }

    /**
     * Locates the next tag in the XML file. The position of the ends of the located tag are returned, as is the name of
     * the tag. Any intervening comments are skipped while searching for the tag.
     *
     * @param searchRange a 2-element integer array, in which element [0] holds the position at which we should start
     *                    searching, and [1] holds the end of the range we should search
     * @param result      a 4-element integer array - on successful return, [0] holds the position of the beginning of
     *                    the tag; if the tag is a container tag, [1] holds the position of the end of the opening tag,
     *                    [2] holds the position of the beginning of the closing tag, and [3] holds the position of the
     *                    end of the closing tag; if the tag is an empty tag, [1], [2] and [3] all point to the end of
     *                    the tag
     * @param tagName     a 1-element {@code String} array that will receive the name of the tag if found
     * @return one of FOUND, NOT_FOUND or ERROR
     */
    public int searchForNextTag(final int[] searchRange, final int[] result,
                                final String[] tagName) {

        // If empty search field, just indicate no tags found.
        if (searchRange[0] >= searchRange[1]) {
            return NOT_FOUND;
        }

        int currentPos = searchRange[0];
        boolean hit = false;

        // Find the first '<' symbol that is not a comment. If we are unable to find any, there are
        // no tags to find, so return NOT_FOUND. Also, if there are any non-whitespace characters
        // before the first '<', return everything from there to the first tag with the name
        // "#text".
        while (currentPos < searchRange[1]) {

            // Skip whitespace
            currentPos = skipWhitespace(currentPos, searchRange);
            if (currentPos == searchRange[1]) {
                break;
            }

            // Test for a comment, and skip if it is.
            if (this.xml.charAt(currentPos) == '<') {
                result[0] = currentPos;

                if (equals(COMMENT_START, currentPos, searchRange)) {
                    currentPos += 4;

                    while (currentPos < searchRange[1]) {

                        if (equals(COMMENT_END, currentPos, searchRange)) {
                            currentPos += 3;
                            break;
                        }

                        ++currentPos;
                    }

                    continue;
                }

                break;
            }

            // If we get here, it must be text. Hunt for next '<' or end of buffer to find the
            // end of the text.
            result[0] = searchRange[0];
            result[1] = indexOf('<', currentPos);

            if ((result[1] == -1) || (result[1] >= searchRange[1])) {
                result[1] = searchRange[1] - 1;
            } else {
                // Back up to character just before '<'
                --result[1];
            }

            // Return the text we found;
            result[2] = result[1];
            result[3] = result[1];
            tagName[0] = "#text";

            return FOUND;
        }

        // If nothing found, just indicate no tags.
        if (currentPos >= searchRange[1]) {
            return NOT_FOUND;
        }

        // We now find the beginning of the name of the tag. First, we skip any leading whitespace
        // between the '<' and the name, and we validate that the name starts with a legal XML
        // name-start character.
        currentPos = result[0] + 1;
        currentPos = skipWhitespace(currentPos, searchRange);

        if ((!Character.isLetter(this.xml.charAt(currentPos))) && this.xml.charAt(currentPos) != '_'
                && this.xml.charAt(currentPos) != ':') {
            return parseError(Res.fmt(Res.BAD_NAME_START_CHAR, this.origin), currentPos);
        }

        // Now we find the end of the name (name ends at first whitespace or '/' or '>' character).
        // We also validate that the name contains only valid characters: letters, numbers, '_',
        // '-', ':', '.'
        int endPos;
        for (endPos = currentPos; endPos < searchRange[1]; ++endPos) {

            if (isXmlWhitespace(this.xml.charAt(endPos)) || this.xml.charAt(endPos) == '/'
                    || this.xml.charAt(endPos) == '>') {
                break;
            }

            if (Character.isLetter(this.xml.charAt(endPos))
                    || Character.isDigit(this.xml.charAt(endPos)) || this.xml.charAt(endPos) == '_'
                    || this.xml.charAt(endPos) == '-' || this.xml.charAt(endPos) == '.'
                    || this.xml.charAt(endPos) == ':') {
                continue;
            }

            return parseError(Res.fmt(Res.BAD_NAME_CHAR, this.origin), endPos);
        }

        // Finally, see that we found a name of length > 0, and did not run to the end of the
        // region of the document in the process.
        if ((endPos == searchRange[1]) || (endPos == currentPos)) {
            return parseError(Res.fmt(Res.CANT_GET_TAG_NAME, this.origin), result[0], endPos);
        }

        // Store the resulting name.
        tagName[0] = this.xml.substring(currentPos, endPos);

        // Now we need to determine the style of tag: <.../> or <...> </...>. We can test whether
        // the tag is <.../> by just searching for "/>" and ensuring there are no '<' or '>'
        // characters before that.
        endPos = indexOf(EMPTY_TAG_END, result[0] + 1);

        if (endPos != -1 && endPos < searchRange[1]) {
            currentPos = indexOf('<', result[0] + 1);

            if (currentPos == -1 || currentPos > endPos) {

                // No intervening '<', so test now for '>'
                currentPos = indexOf('>', result[0] + 1);

                if ((currentPos == -1) || (currentPos > endPos)) {

                    // Success, return what we found (Skip '/' to point at '>')
                    result[1] = endPos + 1;
                    result[2] = result[1];
                    result[3] = result[1];

                    return FOUND;
                }
            }
        }

        // It must be a <...> .. */...> tag. Find the end of the opening
        result[1] = indexOf('>', result[0] + 1);

        if (result[1] == -1 || result[1] >= searchRange[1]) {
            return parseError(Res.fmt(Res.OPEN_NO_RIGHT_BRACKET, this.origin, tagName[0]),
                    result[0]);
        }

        // For a <...> .. */...> tag, we cannot simply search for the closing symbol, since this
        // tag may contain other tags of the same type. Instead, we scan forward, tracking how many
        // '<', '/>', and '</' we have hit, and only reacting when a closure is on the same level
        // and matches the name of the opening tag.
        int tagNestingLevel = 0;

        for (currentPos = result[0]; currentPos < searchRange[1]; ++currentPos) {
            currentPos = skipWhitespace(currentPos, searchRange);

            if (this.xml.charAt(currentPos) == '<') {

                // If this is a comment, immediately skip over it
                if (equals(COMMENT_START, currentPos, searchRange)) {
                    endPos = indexOf(COMMENT_END, currentPos + 4);

                    if (endPos == -1) {
                        return parseError(Res.fmt(Res.UNTERM_COMMENT, this.origin), currentPos);
                    }

                    currentPos = endPos + 1;
                    hit = false;
                } else if (equals(END_TAG_START, currentPos, searchRange)) {
                    result[2] = currentPos;
                    currentPos += (this.xml.charAt(currentPos + 1) == 0) ? 4 : 1;
                    --tagNestingLevel;
                    hit = true;
                } else {
                    ++tagNestingLevel;
                    hit = false;
                }
            } else {
                if (this.xml.charAt(currentPos) == '/') {

                    if (equals(EMPTY_TAG_END, currentPos, searchRange)) {
                        --tagNestingLevel;
                        ++currentPos;
                    }
                } else if (hit && (tagNestingLevel == 0)) {

                    // This could be a closing tag - test the name
                    endPos = indexOf('>', currentPos);

                    if (endPos == -1) {
                        return parseError(Res.fmt(Res.CLOSE_NO_RIGHT_BRACKET, this.origin),
                                result[2]);
                    }

                    if (tagName[0].equals(this.xml.substring(currentPos, endPos))) {

                        // Success - return what we found
                        result[3] = endPos;
                        return FOUND;
                    }
                }
                hit = false;
            }
        }

        return parseError(Res.fmt(Res.MISSING_CLOSE_TAG, this.origin, tagName[0]), result[0], indexOf('>', result[0]));
    }

    /**
     * Searches a specified range of the XML file the first occurrence of a particular tag, skipping any comments we
     * find (if the comment has a tag in it, it must be ignored), go on to find the matching end tag, and then verify
     * that the tag found is empty (of the form &lt;tagName --- /&gt;). Any errors are logged as they are encountered.
     *
     * @param searchRange a 2-element integer array, in which element [0] holds the position at which we should start
     *                    searching, and [1] holds the end of the range we should search
     * @param result      a 4-element integer array - on successful return, [0] holds the position of the beginning of
     *                    the tag, [1], [2] and [3] all point to the end of the tag
     * @param tagName     the tag to search for
     * @param isMandatory {@code true} if it is an error not to locate the tag; {@code false} if the tag is not required
     *                    to be there
     * @return one of FOUND, NOT_FOUND or ERROR
     */
    public int searchForEmptyTag(final int[] searchRange, final int[] result, final String tagName,
                                 final boolean isMandatory) {

        int code = searchForTag(searchRange, result, tagName, isMandatory);

        if ((code == FOUND) && (result[1] != result[2])) {
            code = parseError(Res.fmt(Res.BAD_EMPTY_TAG_FORM, this.origin, tagName), result[0],
                    result[1]);
        }

        return code;
    }

    /**
     * Searches a specified range of the XML file the first occurrence of a particular tag, skipping any comments we
     * find (if the comment has a tag in it, it must be ignored), go on to find the matching end tag, and then verify
     * that the tag found is of the nonempty style (of the form &lt;tagName&gt; ... &lt;/tagName&gt;)
     * <p>
     * Note that this does not guarantee that there is anything between the opening and closing tags. Any errors are
     * logged as they are encountered.
     *
     * @param searchRange a 2- element integer array, in which element [0] holds the position at which we should start
     *                    searching, and [1] holds the end of the range we should search
     * @param result      a 4-element integer array - on successful return, [0] holds the position of the beginning of
     *                    the tag, [1] holds the position of the end of the opening tag, [2] holds the position of the
     *                    beginning of the closing tag,and[3] holds the position of the end of the closing tag
     * @param tagName     the tag to search for
     * @param isMandatory {@code true} if it is an error not to locate the tag; {@code false}if the tag is not required
     *                    to be there
     * @return one of FOUND, NOT_FOUND or ERROR
     */
    public int searchForNonemptyTag(final int[] searchRange, final int[] result,
                                          final String tagName, final boolean isMandatory) {

        int code = searchForTag(searchRange, result, tagName, isMandatory);

        if ((code == FOUND) && (result[1] == result[2])) {
            code = parseError(Res.fmt(Res.BAD_NONEMPTY_TAG_FORM, this.origin, tagName), result[0],
                    result[1]);
        }

        return code;
    }

    /**
     * Given a search range that represents the extents of a tag, searches for a given attribute (attribute name is
     * case-sensitive). If the attribute is found, its start and end position is returned. On any errors, an error will
     * be logged.
     *
     * @param searchRange   a 2-element integer array in which element [0] is the start position of the range to search,
     *                      and [1] is the end of the range to search
     * @param result        a 2-element integer array - on successful return, element [0] holds the start position of
     *                      the attribute value (the character immediately following the opening quote), and element [1]
     *                      holds the end of the value (the position of the closing quote)
     * @param attributeName the name of the attribute to search for
     * @param isMandatory   {@code true} if failure to locate the attribute is an error
     * @param canBeEmpty    {@code true} if empty values are permitted; {@code false} otherwise
     * @return one of FOUND, NOT_FOUND or ERROR
     */
    public int searchForAttribute(final int[] searchRange, final int[] result, final String attributeName,
                                  final boolean isMandatory, final boolean canBeEmpty) {

        int currentPos;
        boolean searching = true;
        char quoteChar = 0;
        String tagName = null;
        final int nameLen = attributeName.length();

        // Scan for the name, preceded by nothing other than whitespace, and followed by either
        // whitespace or '=', and ignoring things in quotes
        for (currentPos = searchRange[0]; currentPos <= (searchRange[1] - nameLen); ++currentPos) {

            if (isXmlWhitespace(this.xml.charAt(currentPos)) && (tagName == null)) {
                tagName = this.xml.substring(searchRange[0], currentPos);
            }

            // If we're entering a " quote, go to its end.
            if (this.xml.charAt(currentPos) == '\"') {
                ++currentPos;

                while (currentPos <= (searchRange[1] - nameLen) && this.xml.charAt(currentPos) != '\"') {
                    ++currentPos;
                }
            }

            // If we're entering a ' quote, go to its end.
            if (this.xml.charAt(currentPos) == '\'') {
                ++currentPos;

                while (currentPos <= (searchRange[1] - nameLen) && this.xml.charAt(currentPos) != '\'') {
                    ++currentPos;
                }
            }

            // Check for the name at this position
            if (equals(attributeName, currentPos, searchRange) && (currentPos == searchRange[0]
                    || isXmlWhitespace(this.xml.charAt(currentPos - 1)))) {

                // See if character following name is whitespace or '='
                final char currentChar = this.xml.charAt(currentPos + nameLen);

                if (currentChar == '=' || isXmlWhitespace(currentChar)) {

                    // Found the name.
                    currentPos += nameLen;
                    searching = false;

                    break;
                }
            }
        }

        // Attribute was not present, but this is an error only if mandatory.
        if (searching) {
            if (isMandatory) {
                return parseError(Res.fmt(Res.MISSING_ATTRIB, this.origin, attributeName, tagName), searchRange[0],
                        searchRange[1]);
            }

            return NOT_FOUND;
        }

        // Now find the '=', skipping intervening whitespace
        searching = true;

        for (; currentPos < searchRange[1]; ++currentPos) {
            final char currentChar = this.xml.charAt(currentPos);

            if (currentChar == '=') {
                searching = false;
                ++currentPos;

                break;
            } else if (!isXmlWhitespace(currentChar)) {
                // This is a case like <tag name FOO='1'>, where the "name" attribute has no '='
                // before a new attribute starts.
                return parseError(Res.fmt(Res.BAD_ATTRIB_CHAR, this.origin, tagName), currentPos);
            }
        }

        if (searching) {
            // This is a case like <tag name>, where the '=' is missing.
            return parseError(Res.fmt(Res.ATTRIB_NO_EQ, this.origin, attributeName, tagName), currentPos);
        }

        // Now find the opening ' or ", skipping intervening whitespace
        searching = true;

        for (; currentPos < searchRange[1]; ++currentPos) {
            final char currentChar = this.xml.charAt(currentPos);

            if ((currentChar == '\'') || (currentChar == '\"')) {
                searching = false;
                ++currentPos;

                // Record type of quote being used
                quoteChar = currentChar;

                // Output: first character after quote
                result[0] = currentPos;

                break;
            } else if (!isXmlWhitespace(currentChar)) {
                // This is a case like <tag name=x'1'> or <tag name=1>.
                return parseError(Res.fmt(Res.BAD_ATTRIB_CHAR, this.origin, tagName), currentPos);
            }
        }

        if (searching) {
            // This is a case like <tag name=1>, where the quotes are missing.
            return parseError(Res.fmt(Res.ATTRIB_NO_QUOT, this.origin, tagName), currentPos);
        }

        // Now find the trailing quote (must match type of quote used above)
        searching = true;

        for (; currentPos < searchRange[1]; ++currentPos) {
            final char currentChar = this.xml.charAt(currentPos);

            if (currentChar == quoteChar) {
                searching = false;

                // Output: position of closing quote
                result[1] = currentPos;

                break;
            }
        }

        if (searching) {
            // This is a case like <tag name='1>
            return parseError(Res.fmt(Res.ATTRIB_NO_CLOSE_QUOT, this.origin, tagName), result[0] - 1);
        }

        if ((!canBeEmpty) && (result[0] == result[1])) {
            // Attribute is empty, like <tag name=''>, but empty is disallowed.
            return parseError(Res.fmt(Res.EMPTY_ATTRIB_VALUE, this.origin, attributeName, tagName), result[0] - 1);
        }

        return FOUND;
    }

    /**
     * Locates the position of the next occurrence of a particular {@code String} in the XML data.
     *
     * @param test     the string to search for
     * @param startPos the position at which to begin searching
     * @return the index of the next matching character sequence (if found), or -1 if the sequence was not found
     */
    private int indexOf(final char[] test, final int startPos) {

        int index = -1;

        final int len = this.xml.length();
        final int testLen = test.length;

        for (int i = startPos; i < len; ++i) {
            int diff;
            for (diff = 0; diff < testLen; ++diff) {
                if (this.xml.charAt(i + diff) != test[diff]) {
                    break;
                }
            }

            if (diff == testLen) {
                // All characters matched, so return a hit.
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * Locates the position of the next occurrence of a particular {@code String} in the XML data.
     *
     * @param test     the string to search for
     * @param startPos the position at which to begin searching
     * @return the index of the next matching character sequence (if found), or -1 if the sequence was not found
     */
    int indexOf(final char test, final int startPos) {

        int index = -1;

        final int len = this.xml.length();
        for (int i = startPos; i < len; ++i) {

            if (this.xml.charAt(i) == test) {
                index = i;

                break;
            }
        }

        return index;
    }

    /**
     * Checks whether or not the sequence of characters starting at a particular position matches a given character
     * array.
     *
     * @param test        the character sequence to test for
     * @param startPos    the position at which to check for the string
     * @param searchRange the range of positions to search
     * @return {@code true} if the string matched; {@code false} otherwise
     */
    private boolean equals(final CharSequence test, final int startPos, final int[] searchRange) {

        int match;

        final int len = test.length();
        for (match = 0; match < len; ++match) {
            if (((startPos + match) >= searchRange[1]) || (this.xml.charAt(startPos + match) != test.charAt(match))) {
                break;
            }
        }

        return match == test.length();
    }

    /**
     * Checks whether or not the sequence of characters starting at a particular position matches a given character
     * array.
     *
     * @param test        the character sequence to test for
     * @param startPos    the position at which to check for the string
     * @param searchRange the range of positions to search
     * @return {@code true} if the string matched; {@code false} otherwise
     */
    private boolean equals(final char[] test, final int startPos, final int[] searchRange) {

        int diff;

        final int testLen = test.length;
        for (diff = 0; diff < testLen; ++diff) {
            if (((startPos + diff) >= searchRange[1]) || (this.xml.charAt(startPos + diff) != test[diff])) {
                break;
            }
        }

        return diff == testLen;
    }

    /**
     * Examines the XML stored in {@code this.mXml} and test whether each character is legal for XML.
     *
     * @return {@code true} if the XML contains only legal characters; {@code false} otherwise
     */
    private boolean validateXmlCharacters() {

        int line = 0;
        int col = 0;
        boolean result = true;

        // Here, we scrutinize the XML to ensure each character is legal in XML (no control
        // characters except whitespace).
        final int len = this.xml.length();
        for (int i = 0; i < len; ++i) {
            ++col;

            if (this.xml.charAt(i) == 0) {
                continue;
            }

            if (Character.isISOControl(this.xml.charAt(i))) {

                if (this.xml.charAt(i) == '\n') {
                    ++line;
                    col = 0;
                } else if ((this.xml.charAt(i) != '\r') && (this.xml.charAt(i) != '\t')) {
                    Log.warning(Res.fmt(Res.BAD_XML_CHAR, this.origin, Integer.toString(this.xml.charAt(i)),
                            Integer.toString(line + 1), Integer.toString(col)));
                    this.xml = null;
                    result = false;

                    break;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a character is XML whitespace, which is defined as space, tab, carriage return or line feed. This
     * differs from the Java definition of whitespace, which includes several other characters (such as vertical tab).
     *
     * @param chr the character to be tested
     * @return {@code true} if the character is whitespace; {@code false} otherwise
     */
    private static boolean isXmlWhitespace(final char chr) {

        return chr == ' ' || chr == '\t' || chr == '\n' || chr == '\r' || chr == 0;
    }

    /**
     * Skips a run of whitespace, and finds the position of the next non- whitespace character.
     *
     * @param start       the start position to search (need not be whitespace)
     * @param searchRange the range of positions to search
     * @return the index of the next non-whitespace character, or the index of the end of the buffer if none found
     */
    private int skipWhitespace(final int start, final int[] searchRange) {

        int pos = start;

        while (pos < searchRange[1] && isXmlWhitespace(this.xml.charAt(pos))) {
            ++pos;
        }

        return pos;
    }

    /**
     * Records a parse error to the message list.
     *
     * @param msg the parse error message
     * @param pos the parse error position
     * @return {@code ERROR}
     */
    private int parseError(final String msg, final int pos) {

        Log.warning(msg);
        this.messages.add(new FormulaParseError(msg, pos));

        return ERROR;
    }

    /**
     * Records a parse error to the message list.
     *
     * @param msg   the parse error message
     * @param start the parse error start position
     * @param end   the parse error end position
     * @return {@code ERROR}
     */
    int parseError(final String msg, final int start, final int end) {

        Log.warning(msg);
        this.messages.add(new FormulaParseError(msg, start, end));

        Log.warning(new Exception(this.origin + "[" + start + ":" + end + "]" + msg + ": "
                + this.xml.substring(start, end)));

        return ERROR;
    }

    /**
     * Gets the message list.
     *
     * @return the message list
     */
    public List<MessageInt> getMessageList() {

        return this.messages;
    }
}
