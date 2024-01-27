package dev.mathops.session.txn.messages;

import dev.mathops.commons.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class for all messages.
 */
public abstract class AbstractMessageBase {

    /** A zero-length array used when creating other arrays. */
    private static final String[] ZERO_LEN_STRING_ARR = new String[0];

    /** A zero-length array used when creating other arrays. */
    private static final Serializable[] ZERO_LEN_SER_ARR = new Serializable[0];

    /**
     * Constructs a new {@code AbstractMessageBase}.
     */
    protected AbstractMessageBase() {

        // No action
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    public abstract String toXml();

    /**
     * Generates the String XML representation of the message.
     *
     * @return the String XML representation
     */
    @Override
    public final String toString() {

        return toXml();
    }

    /**
     * Tells the message to free any resources allocated to it. The message will assume no other methods will be called
     * after this one. Messages that override this method should call {@code super.die} to ensure the superclass
     * references get cleaned up.
     */
    void die() {

        // No action
    }

    /**
     * Verifies that the XML is contained in a proper XML tag pair and extracts the message data.
     *
     * @param xml the XML data from which to extract the message
     * @param tag the tag name - the message content will be embedded between &lt;tag&gt; and &lt;/tag&gt; tags
     * @return the message content, if valid
     * @throws IllegalArgumentException if the XML data is not valid
     */
    static String extractMessage(final char[] xml, final String tag)
            throws IllegalArgumentException {

        final String str = String.valueOf(xml);

        // Verify the envelope.
        String search = "<" + tag + ">";
        int start = str.indexOf(search);

        if (start == -1) {
            throw new IllegalArgumentException(Res.fmt(Res.BAD_MESSAGE_TYPE, tag));
        }

        start += search.length();

        search = "</" + tag + ">";

        final int end = str.indexOf(search, start);

        if (end == -1) {
            throw new IllegalArgumentException(Res.fmt(Res.NO_CLOSING_TAG, tag));
        }

        return str.substring(start, end);
    }

    /**
     * Searches the message content for a particular field and extracts its value.
     *
     * @param message   the message content
     * @param fieldName the name of the field to search for
     * @return the located value, or {@code null} if the value was not found
     */
    static String extractField(final String message, final String fieldName) {

        final String search1 = "<" + fieldName + ">";
        final int start = message.indexOf(search1);
        String field = null;

        if (start != -1) {
            final String search2 = "</" + fieldName + ">";
            final int end = message.indexOf(search2, start + search1.length());

            if (end == -1) {
                throw new IllegalArgumentException(Res.fmt(Res.NO_CLOSING_TAG, fieldName));
            }

            field = message.substring(start + search1.length(), end).trim();
        }

        return field;
    }

    /**
     * Searches the message content for any number of a particular field and extracts its list of values.
     *
     * @param message   the message content
     * @param fieldName the name of the field to search (repeatedly) for
     * @return the array of located values, or {@code null} if the value was not found
     */
    static String[] extractFieldList(final String message, final String fieldName) {

        final List<String> list = new ArrayList<>(5);
        int pos = 0;

        final int len = message.length();
        while (pos < len) {
            String search = "<" + fieldName + ">";
            int start = message.indexOf(search, pos);

            if (start == -1) {
                break;
            }

            start += search.length();
            search = "</" + fieldName + ">";

            final int end = message.indexOf(search, start);

            if (end == -1) {
                throw new IllegalArgumentException(Res.fmt(Res.NO_CLOSING_TAG, fieldName));
            }

            list.add(message.substring(start, end).trim());
            pos = end + search.length();
        }

        return list.isEmpty() ? null : list.toArray(ZERO_LEN_STRING_ARR);
    }

    /**
     * Given an XML string that consists of a series of values, enclosed in type tags, extracts the values. For example,
     * a series of values may look like:
     *
     * <pre>
     * &lt;int&gt;4&lt;/int&gt;&lt;long&gt;12345&lt;/long&gt;&lt;string&gt;Hello&lt;/string&gt;
     * &lt;double&gt;1.2&lt;/double&gt;
     * </pre>
     * <p>
     * This would result in the return of an Object[4], containing an Integer, Long, String and Double in its four
     * positions.
     *
     * @param message the message content
     * @return the array of located values, or {@code null} if none
     */
    static Serializable[] extractValueList(final String message) {

        int pos = 0;
        final List<Serializable> list = new ArrayList<>(5);

        final int len = message.length();
        while (pos < len) {

            // Find next tag and extract its name
            int start = message.indexOf('<', pos);

            if (start == -1) {
                break;
            }

            final int end = message.indexOf('>', start + 1);

            if (end == -1) {
                break;
            }

            final String tag = message.substring(start + 1, end);

            // Find the end tag and extract the contained value
            start = message.indexOf("</" + tag + ">", end + 1);

            if (start == -1) {
                break;
            }

            final String value = message.substring(end + 1, start);

            // Parse value and store in list
            switch (tag) {
                case "int", "long" -> {
                    try {
                        list.add(Long.valueOf(value));
                    } catch (final NumberFormatException ex) {
                        Log.severe(Res.get(Res.CANT_EXTRACT_INTEGER), ex);
                    }
                }
                case "double" -> {
                    try {
                        list.add(Double.valueOf(value));
                    } catch (final NumberFormatException ex) {
                        Log.severe(Res.get(Res.CANT_EXTRACT_DOUBLE), ex);
                    }
                }
                case "string" -> list.add(value);
            }

            pos = start + tag.length() + 3;
        }

        return list.isEmpty() ? null : list.toArray(ZERO_LEN_SER_ARR);
    }
}
