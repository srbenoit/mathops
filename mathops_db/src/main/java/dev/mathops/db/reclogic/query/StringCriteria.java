package dev.mathops.db.reclogic.query;

/**
 * Criteria for a string field. This class supports a string value and a comparison operator, which allows the following
 * types of logic:
 *
 * <pre>
 *   WHERE fieldName = 'Hello'
 *   WHERE lower(fieldName) = lower('Hello')
 *   WHERE fieldName LIKE('He%')
 *   WHERE lower(fieldName) LIKE(lower('He%'))
 * </pre>
 */
public class StringCriteria {

    /** The field value. */
    public final String value;

    /** The comparison to use on the field. */
    public final EStringComparison comparison;

    /**
     * Constructs a new {@code StringCriteria}.
     *
     * @param theValue      the value
     * @param theComparison the comparison
     */
    public StringCriteria(final String theValue, final EStringComparison theComparison) {

        this.value = theValue;
        this.comparison = theComparison;
    }
}
