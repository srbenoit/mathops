package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by CourseRecommendations

    /** A resource key. */
    static final String NO_COURSE_GROUP_WITH_CODE = key(index++);

    /** A resource key. */
    static final String COURSE_NOT_DEFINED = key(index++);

    // Used by CourseGroup

    /** A resource key. */
    static final String SELECT_ONE_COURSE_FROM = key(index++);

    /** A resource key. */
    static final String SELECT_CREDITS_FROM = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {NO_COURSE_GROUP_WITH_CODE, "No course group with group code {0}"},
            {COURSE_NOT_DEFINED, "Course {0} not defined"},

            {SELECT_ONE_COURSE_FROM, "Select one course from"},
            {SELECT_CREDITS_FROM, "Select {0} or more credits from"},

            //
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
