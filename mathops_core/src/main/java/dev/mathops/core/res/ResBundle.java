package dev.mathops.core.res;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * A resource bundle that uses {@code String} keys and messages and can store messages for multiple {@code Locale}s.
 *
 * <p>
 * Subclasses need only define a number of arrays of key/value pairs, one for each supported locale, then add them to
 * the messages map on instantiation. The intent is that each package within a project have its own "Res" subclass of
 * this class that holds the localized messages used in that package. Such a subclass would typically look like the
 * following:
 *
 * <pre>
 * final class Res extends ResBundle {
 *
 *     private static int index;
 *     public static final String MSG_KEY_1 = key(index++);
 *     public static final String MSG_KEY_2 = key(index++);
 *
 *     private static final String[][] EN_US = {
 *         {MSG_KEY_1, "The message in the default language"},
 *         {MSG_KEY_2, "A message {0} with {1} parameters"},
 *     };
 *
 *     private static Res instance = new Res();
 *
 *
 *     private Res() {
 *         super(Locale.US, EN_US);
 *     }
 *
 *     public static String get(String key) {
 *         return instance.getMsg(key);
 *     }
 *
 *     public static String fmt(String key, Object... arguments) {
 *         return instance.formatMsg(key, arguments);
 *     }
 * }
 * </pre>
 *
 * <p>
 * Within package code, messages are accessed as in the following example:
 *
 * <pre>
 * Log.info(Res.get(Res.MSG_KEY_1));
 * Log.info(Res.fmt(Res.MSG_KEY_2, "equipped", Integer.valueOf(2)));
 * </pre>
 * <p>
 * NOTE: It is good practice to give package-specific resource classes "package" access (as in the example above) rather
 * than "public", so they won't be inadvertently used (or automatically included by an IDE!) in classes outside the
 * package.
 */
public class ResBundle extends ResBundleBase {

    /**
     * Constructs a new {@code ResBundle}.
     *
     * @param locale   the locale under which to register the first block of messages
     * @param messages an array with dimensions [N][2] of {@code String} key/message pairs
     */
    protected ResBundle(final Locale locale, final String[][] messages) {

        super();

        addMessages(locale, messages);
    }

    /**
     * Gets the message with a specified key using the current default locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    public final String getMsg(final String key) {

        return getMsg(key, Locale.getDefault());
    }

    /**
     * Retrieves the message with a specified key using the current default locale, then uses a {@code MessageFormat} to
     * format that message pattern with a collection of arguments.
     *
     * @param key       the message key
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    public final String formatMsg(final String key, final Object... arguments) {

        return formatMsg(key, Locale.getDefault(), arguments);
    }

    /**
     * Retrieves the message with a specified key using a specified locale, then uses a {@code MessageFormat} to format
     * that message pattern with a collection of arguments.
     *
     * @param key       the message key
     * @param locale    the desired {@code Locale}
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    private String formatMsg(final String key, final Locale locale, final Object... arguments) {

        return MessageFormat.format(getMsg(key, locale), arguments);
    }

    /**
     * Given a message index, returns an {@code String} key unique to that index.
     *
     * @param index the index
     * @return the {@code String} key
     */
    protected static String key(final int index) {

        return Integer.toString(index);
    }
}
