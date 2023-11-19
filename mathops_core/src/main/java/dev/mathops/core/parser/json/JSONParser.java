package dev.mathops.core.parser.json;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A parser that attempts to construct a {@code JSONObject} from a string.
 */
public enum JSONParser {
    ;

    /** A zero-length array of objects. */
    private static final Object[] ZERO_LEN_OBJ_ARR = new Object[0];

    /**
     * Attempts to parse a JSON string into an object.
     *
     * <pre>
     * json
     *     element
     * </pre>
     *
     * @param str the string to parse
     * @return the parsed object (a {@code JSONObject}, object array, String, Double, Boolean, or null)
     * @throws ParsingException if parsing failed
     */
    public static Object parseJSON(final String str) throws ParsingException {

        if (str.isEmpty()) {
            throw new ParsingException(0, 0, "JSON data to parse was empty");
        }

        final char[] chars = str.toCharArray();
        final int[] range = {0, 0, chars.length};

        final Object result = scanElement(chars, range);

        if (range[1] != chars.length) {
            throw new ParsingException(range[1], chars.length, "Unexpected characters at end of JSON data");
        }

        return result;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "value" has been accumulated.
     *
     * <pre>
     * value
     *     object
     *     array
     *     string
     *     number
     *     "true"
     *     "false"
     *     "null"
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned value (a {@code JSONObject}, object array, String, Double, Boolean, or null)
     * @throws ParsingException if parsing failed
     */
    private static Object scanValue(final char[] chars, final int[] range) throws ParsingException {

        final char ch = chars[range[1]];

        final Object result;

        if (ch == '{') {
            result = scanObject(chars, range);
        } else if (ch == '[') {
            result = scanArray(chars, range);
        } else if (ch == '"') {
            result = scanString(chars, range);
        } else {
            final int pos = range[1];
            final int end = range[2];

            if (end >= pos + 4 && chars[pos] == 't' && chars[pos + 1] == 'r'
                    && chars[pos + 2] == 'u' && chars[pos + 3] == 'e') {
                result = Boolean.TRUE;
                range[1] += 4;
            } else if (end >= pos + 5 && chars[pos] == 'f' && chars[pos + 1] == 'a'
                    && chars[pos + 2] == 'l' && chars[pos + 3] == 's' && chars[pos + 4] == 'e') {
                result = Boolean.FALSE;
                range[1] += 5;
            } else if (end >= pos + 4 && chars[pos] == 'n' && chars[pos + 1] == 'u'
                    && chars[pos + 2] == 'l' && chars[pos + 3] == 'l') {
                result = NullValue.INSTANCE;
                range[1] += 4;
            } else {
                result = scanNumber(chars, range);
            }
        }

        // Log.info("Scanned " + result);

        return result;
    }

    /**
     * Attempts to scan forward in a JSON character array until an "object" has been accumulated.
     *
     * <pre>
     * object
     *     '{' ws '}'
     *     '{' members '}'
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}, where 'current' is the position of the leading
     *              open brace
     * @return the parsed {@code JSONObject}
     * @throws ParsingException if parsing failed
     */
    private static JSONObject scanObject(final char[] chars, final int[] range) throws ParsingException {

        ++range[1];
        scanWs(chars, range);

        if (range[1] == range[2]) {
            throw new ParsingException(range[1] - 1, range[2], "Missing closing '}' after object.");
        }

        final int start = range[1];

        final JSONObject object = new JSONObject();

        if (chars[range[1]] == '}') {
            // Empty object
            ++range[1];
        } else {
            scanMembers(chars, range, object);

            if (range[1] < range[2] && chars[range[1]] == '}') {
                // Proper termination
                ++range[1];
            } else {
                final String context = new String(chars, start, range[1]);
                Log.warning(context);
                throw new ParsingException(range[1] - 1, range[2], "Missing closing '}' after object.");
            }
        }

        return object;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "members" has been accumulated.
     *
     * <pre>
     * members
     *     member
     *     member ',' members
     * </pre>
     *
     * @param chars  the character array
     * @param range  the range over which to scan {start, current, end}
     * @param target the object to which to add parsed members
     * @throws ParsingException if parsing failed
     */
    private static void scanMembers(final char[] chars, final int[] range, final JSONObject target)
            throws ParsingException {

        scanMember(chars, range, target);

        while (range[1] < range[2] && chars[range[1]] == CoreConstants.COMMA_CHAR) {
            ++range[1];
            scanMember(chars, range, target);
        }
    }

    /**
     * Attempts to scan forward in a JSON character array until a "member" has been accumulated.
     *
     * <pre>
     * member
     *     ws string ws ':' element
     * </pre>
     *
     * @param chars  the character array
     * @param range  the range over which to scan {start, current, end}
     * @param target the object to which to add parsed members
     * @throws ParsingException if parsing failed
     */
    private static void scanMember(final char[] chars, final int[] range, final JSONObject target)
            throws ParsingException {

        scanWs(chars, range);
        if (range[1] == range[2]) {
            throw new ParsingException(range[1] - 1, range[2], "JSON data ended when expecting string member name");
        }

        final String name = scanString(chars, range);

        scanWs(chars, range);

        if (range[1] == range[2]) {
            throw new ParsingException(range[1] - 1, range[2],
                    "JSON data ended when expecting ':' after object member name.");
        }
        if (chars[range[1]] != ':') {
            throw new ParsingException(range[1], range[1] + 1, "Expected ':' after object member name.");
        }
        ++range[1];

        target.setProperty(name, scanElement(chars, range));
    }

    /**
     * Attempts to scan forward in a JSON character array until an "array" has been accumulated.
     *
     * <pre>
     * array
     *     '[' elements ']'
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}, where 'current' is the position of the leading
     *              open bracket
     * @return the parsed array
     * @throws ParsingException if parsing failed
     */
    private static Object[] scanArray(final char[] chars, final int[] range)
            throws ParsingException {

        ++range[1];
        scanWs(chars, range);
        if (range[1] == range[2]) {
            throw new ParsingException(range[1] - 1, range[2], "JSON data ended within array.");
        }

        final Object[] array;

        if (chars[range[1]] == ']') {
            // Empty array
            array = ZERO_LEN_OBJ_ARR;
            ++range[1];
        } else {
            array = scanElements(chars, range);

            if (range[1] == range[2]) {
                throw new ParsingException(range[1] - 1, range[2], "Missing closing ']' after array.");
            }

            if (chars[range[1]] == ']') {
                ++range[1];
            } else {
                throw new ParsingException(range[1] - 1, range[2], "Missing closing ']' after object.");
            }
        }

        return array;
    }

    /**
     * Attempts to scan forward in a JSON character array until an "elements" has been accumulated.
     *
     * <pre>
     * elements
     *     element
     *     element ',' elements
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}, where 'current' is the position of the leading
     *              open bracket
     * @return the parsed array of elements
     * @throws ParsingException if parsing failed
     */
    private static Object[] scanElements(final char[] chars, final int[] range)
            throws ParsingException {

        final Object elem = scanElement(chars, range);

        final Collection<Object> list = new ArrayList<>(10);
        list.add(elem);

        while (range[1] < range[2] && chars[range[1]] == CoreConstants.COMMA_CHAR) {
            ++range[1];
            list.add(scanElement(chars, range));
        }

        return list.toArray();
    }

    /**
     * Attempts to scan forward in a JSON character array until an "element" has been accumulated.
     *
     * <pre>
     * element
     *     ws value ws
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned element (on successful return, range[1] will hold the position after the scanned element)
     * @throws ParsingException if parsing failed
     */
    private static Object scanElement(final char[] chars, final int[] range)
            throws ParsingException {

        scanWs(chars, range);
        if (range[1] == range[2]) {
            throw new ParsingException(range[1], chars.length, "JSON data ended before value found");
        }

        final Object result = scanValue(chars, range);

        scanWs(chars, range);

        return result;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "string" has been accumulated.
     *
     * <pre>
     * string
     *     '"' characters '"'
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned string value
     * @throws ParsingException if parsing failed
     */
    private static String scanString(final char[] chars, final int[] range) throws ParsingException {

        final int start = range[1];

        if (chars[range[1]] != '"') {
            throw new ParsingException(range[1], range[1] + 1, "Expected opening quotation mark on string.");
        }

        ++range[1];
        final String result = scanCharacters(chars, range);

        if (range[1] == range[2]) {
            throw new ParsingException(range[1] - 1, range[2],
                    "JSON data ended when expecting closing quote of string: ["
                            + new String(chars, start, range[1] + 1) + "]");
        }
        if (chars[range[1]] != '"') {
            throw new ParsingException(range[1], range[1] + 1,
                    "Expected closing quotation mark on string: [" + new String(chars, start, range[1] + 1) + "]");
        }
        ++range[1];

        return result;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "characters" has been accumulated.
     *
     * <pre>
     * characters
     *     ""
     *     character characters
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned string value
     * @throws ParsingException if parsing failed
     */
    private static String scanCharacters(final char[] chars, final int[] range)
            throws ParsingException {

        final String result;

        if (chars[range[1]] == '"') {
            result = CoreConstants.EMPTY;
        } else {
            final StringBuilder target = new StringBuilder(20);
            final int end = range[2];

            while (range[1] < end) {
                final char ch = scanCharacter(chars, range);
                if (ch == 0) {
                    break;
                }
                target.append(ch);
            }

            result = target.toString();
        }

        return result;

    }

    /**
     * Attempts to scan forward in a JSON character array until a "character" has been accumulated.
     *
     * <pre>
     * character
     *     '0020' . '10FFFF' - '"' - '\'
     *     '\' escape
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned character value, 0 if none
     * @throws ParsingException if parsing failed
     */
    private static char scanCharacter(final char[] chars, final int[] range)
            throws ParsingException {

        char ch = chars[range[1]];

        if (ch == '"') {
            ch = 0;
        } else if (ch == '\\') {
            ++range[1];
            ch = scanEscape(chars, range);
        } else if (ch < 0x20) {
            ch = 0;
        } else {
            ++range[1];
        }

        return ch;
    }

    /**
     * Attempts to scan forward in a JSON character array until an "escape" has been accumulated.
     *
     * <pre>
     * escape
     *     '"'
     *     '\'
     *     '/'
     *     'b'
     *     'f'
     *     'n'
     *     'r'
     *     't'
     *     'u' hex hex hex hex
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned character value, 0 if none
     * @throws ParsingException if parsing failed
     */
    private static char scanEscape(final char[] chars, final int[] range) throws ParsingException {

        final int pos = range[1];

        if (pos == range[2]) {
            throw new ParsingException(pos - 1, range[2], "JSON data ended within character escape.");
        }

        char ch = chars[pos];

        if (ch == 'u') {
            if (pos + 4 >= range[2]) {
                throw new ParsingException(pos - 1, range[2], "Invalid unicode escape in string.");
            }
            ++range[1];

            final int h1 = scanHex(chars, range);
            ++range[1];
            final int h2 = scanHex(chars, range);
            ++range[1];
            final int h3 = scanHex(chars, range);
            ++range[1];
            final int h4 = scanHex(chars, range);
            ++range[1];

            if (h1 == -1 || h2 == -1 || h3 == -1 || h4 == -1) {
                throw new ParsingException(pos, pos + 5, "Invalid unicode escape in string.");
            }

            final int uncode = (h1 << 12) + (h2 << 8) + (h3 << 4) + h4;
            ch = (char) uncode;
        } else if (ch == 'n') {
            ch = '\n';
            ++range[1];
        } else if (ch == 'r') {
            ch = '\r';
            ++range[1];
        } else if (ch == 't') {
            ch = '\t';
            ++range[1];
        } else if (ch == 'f') {
            ch = '\f';
            ++range[1];
        } else if (ch == 'b') {
            ch = '\b';
            ++range[1];
        } else if (ch == '"' || ch == '\\' || ch == '/') {
            ++range[1];
        } else {
            throw new ParsingException(pos - 1, pos, "Invalid escape in string.");
        }

        return ch;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "hex" has been accumulated.
     *
     * <pre>
     * hex
     *     digit
     *     'A' . 'F'
     *     'a' . 'f'
     * </pre>
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     * @return the scanned hex character value (0 to 15)
     * @throws ParsingException if parsing failed
     */
    private static int scanHex(final char[] chars, final int[] range) throws ParsingException {

        final int result;

        final char ch = chars[range[1]];

        if (ch >= '0' && ch <= '9') {
            result = ch - '0';
        } else if (ch >= 'a' && ch <= 'f') {
            result = ch - 'a' + 10;
        } else if (ch >= 'A' && ch <= 'F') {
            result = ch - 'A' + 10;
        } else {
            throw new ParsingException(range[1], range[1] + 1, "Invalid hex character");
        }

        return result;
    }

    /**
     * Attempts to scan forward in a JSON character array until a "number" has been accumulated.
     *
     * <pre>
     * number
     *     integer fraction exponent
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     * @return the parsed number
     * @throws ParsingException if parsing failed
     */
    private static Double scanNumber(final char[] chars, final int[] range)
            throws ParsingException {

        final int start = range[1];

        // The scanners below just validate format and skip characters, we use Java's Double
        // class to parse the result at the end
        scanInteger(chars, range);
        scanFraction(chars, range);
        scanExponent(chars, range);

        final int end = range[1];

        return Double.valueOf(new String(chars, start, end - start));
    }

    /**
     * Attempts to scan forward in a JSON character array until an "integer" has been accumulated.
     *
     * <pre>
     * integer
     *     digit
     *     onenine digits
     *     '-' digit
     *     '-' onenine digits
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     * @throws ParsingException if parsing failed
     */
    private static void scanInteger(final char[] chars, final int[] range) throws ParsingException {

        final int start = range[1];

        char ch = chars[range[1]];

        if (ch == '-') {
            ++range[1];
            if (range[1] == range[2]) {
                throw new ParsingException(start, range[2], "JSON data ended within number");
            }
            ch = chars[range[1]];
        }

        if (ch == '0') {
            ++range[1];
        } else if (ch >= '1' && ch <= '9') {
            ++range[1];

            ch = chars[range[1]];
            while (isDigit(ch)) {
                ++range[1];
                ch = chars[range[1]];
            }
        } else {
            throw new ParsingException(start, range[1] + 1, "Invalid number");
        }
    }

    /**
     * Attempts to scan forward in a JSON character array until a "digits" has been accumulated.
     *
     * <pre>
     * digits
     *     digit
     *     digit digits
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     * @throws ParsingException if parsing failed
     */
    private static void scanDigits(final char[] chars, final int[] range) throws ParsingException {

        if (isDigit(chars[range[1]])) {
            ++range[1];

            while (range[1] < range[2] && isDigit(chars[range[1]])) {
                ++range[1];
            }
        } else {
            throw new ParsingException(range[1], range[1] + 1, "Invalid digit");
        }
    }

    /**
     * Attempts to scan forward in a JSON character array until a "fraction" has been accumulated.
     *
     * <pre>
     * fraction
     *     ""
     *     '.' digits
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     * @throws ParsingException if parsing failed
     */
    private static void scanFraction(final char[] chars, final int[] range)
            throws ParsingException {

        final int start = range[1];

        char ch = chars[range[1]];

        if (ch == '.') {
            ++range[1];
            if (range[1] == range[2]) {
                throw new ParsingException(start, range[2], "JSON data ended within number");
            }

            ch = chars[range[1]];
            if (isDigit(ch)) {
                while (range[1] < range[2] && isDigit(chars[range[1]])) {
                    ++range[1];
                }
            } else {
                throw new ParsingException(start, range[2], "JSON data ended within number");
            }
        }
    }

    /**
     * Attempts to scan forward in a JSON character array until an "exponent" has been accumulated.
     *
     * <pre>
     * exponent
     *     ""
     *     'E' sign digits
     *     'e' sign digits
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     * @throws ParsingException if parsing failed
     */
    private static void scanExponent(final char[] chars, final int[] range)
            throws ParsingException {

        final char ch = chars[range[1]];

        if (ch == 'E' || ch == 'e') {
            ++range[1];
            scanSign(chars, range);
            scanDigits(chars, range);
        }
    }

    /**
     * Attempts to scan forward in a JSON character array until a "sign" has been accumulated.
     *
     * <pre>
     * sign
     *     ""
     *     '+'
     *     '-'
     * </pre>
     *
     * @param chars the character array to parse
     * @param range the range over which to scan {start, current, end}
     */
    private static void scanSign(final char[] chars, final int[] range) {

        final char ch = chars[range[1]];

        if (ch == '+' || ch == '-') {
            ++range[1];
        }
    }

    /**
     * Scans past any whitespace in a character array. On return, range[1] will hold the position of the next
     * non-whitespace character found, or the value of range[2] if none was found.
     *
     * @param chars the character array
     * @param range the range over which to scan {start, current, end}
     */
    private static void scanWs(final char[] chars, final int[] range) {

        while (range[1] < range[2] && isWhitespace(chars[range[1]])) {
            ++range[1];
        }
    }

    /**
     * Tests whether a character is a JSON whitespace.
     *
     * @param ch the character
     * @return true if whitespace; false if not
     */
    private static boolean isWhitespace(final char ch) {

        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    /**
     * Tests whether a character is a digit.
     *
     * @param ch the character
     * @return true if digit; false if not
     */
    private static boolean isDigit(final char ch) {

        return ch >= '0' && ch <= '9';
    }
}
