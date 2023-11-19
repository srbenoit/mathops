package dev.mathops.db.reclogic.query;

/**
 * Criteria for an integer field. This class supports an integer value and a comparison operator, which allows the
 * following types of logic:
 *
 * <pre>
 *   WHERE fieldName = 100
 *   WHERE fieldName > 100
 *   WHERE fieldName >= 100
 *   WHERE fieldName < 100
 *   WHERE fieldName <= 100
 *   WHERE fieldName != 100
 * </pre>
 */
public class IntegerCriteria {

    /** The field value. */
    public final int value;

    /** The comparison to use on the field. */
    public final EComparison comparison;

    /**
     * Constructs a new {@code IntegerCriteria}.
     *
     * @param theValue      the value
     * @param theComparison the comparison
     */
    public IntegerCriteria(final int theValue, final EComparison theComparison) {

        this.value = theValue;
        this.comparison = theComparison;
    }
}
