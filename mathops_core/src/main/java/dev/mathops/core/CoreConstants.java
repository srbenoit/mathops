package dev.mathops.core;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * System-wide constants.
 */
public enum CoreConstants {
    ;

    /**
     * A static object on which to synchronize single-instance object instance creation. This allows us to avoid
     * creating single-instance objects immediately on class load (through a static member that is initialized to a new
     * instance immediately), and also avoid having each such class define an instance synch object (again, created on
     * class load), just to have something on which to synchronize instance creation.
     * <p>
     * The pattern for use of this object is: <pre>
     * public final class MySingleInstanceClass {
     *
     *     // The single instance
     *     private static MySingleInstanceClass instance = null;
     *
     *     // Private constructor to prevent direct instantiation
     *     private  MySingleInstanceClass() { ... }
     *
     *     // Retrieve the single instance
     *     public static MySingleInstanceClass getInstance() {
     *         synchronized (CoreConstants.INSTANCE_SYNCH) {
     *             if (instance == null) {
     *                 instance = new MySingleInstanceClass();
     *             }
     *
     *             return instance;
     *         }
     *     }
     * }
     * </pre>
     */
    public static final Object INSTANCE_SYNCH = new Object();

    /** Empty string. */
    public static final String EMPTY = "";

    /** String with single space character. */
    public static final String SPC = " ";

    /** The space character. */
    public static final char SPC_CHAR = ' ';

    /** String with single dot character. */
    public static final String DOT = ".";

    /** String with single dot character. */
    public static final String COMMA = ",";

    /** The comma character. */
    public static final char COMMA_CHAR = ',';

    /** String with single slash character. */
    public static final String SLASH = "/";

    /** String with single dash character. */
    public static final String DASH = "-";

    /** String with single colon character. */
    public static final String COLON = ":";

    /** String with single colon character. */
    public static final String QUOTE = "\"";

    /** The standard end-of-line characters. */
    public static final String CRLF = "\r\n";

    /** A commonly used string. */
    public static final String STATIC = "static-method";

    /** Characters from which to make IDs (62). */
    private static final char[] ID_CHARS =
            "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();

    /** Random number generator. */
    private static final Rnd RND = new Rnd(new Seed(System.currentTimeMillis(),
            System.nanoTime() ^ Runtime.getRuntime().freeMemory()));

    /**
     * Generates an ID of a specified length as a sequence of random characters from the ID_CHARS array.
     *
     * @param length the length
     * @return the generated ID
     */
    public static String newId(final int length) {

        final HtmlBuilder str = new HtmlBuilder(length);

        synchronized (RND) {
            for (int i = 0; i < length; ++i) {
                str.add(ID_CHARS[RND.nextInt(ID_CHARS.length)]);
            }
        }

        return str.toString();
    }
}
