package dev.mathops.web.websocket.chatdemo;

/**
 * HTML filter utility.
 *
 * @author Craig R. McClanahan
 * @author Tim Tye
 */
enum HTMLFilter {
    ;

    /**
     * Filter the specified message string for characters that are sensitive in HTML. This avoids potential attacks
     * caused by including JavaScript codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     * @return the filtered version of the message
     */
    static String filter(final String message) {

        if (message == null) {
            return null;
        }

        final char[] content = new char[message.length()];
        message.getChars(0, message.length(), content, 0);

        final StringBuilder result = new StringBuilder(content.length + 50);
        for (final char c : content) {
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(c);
            }
        }

        return result.toString();
    }
}
