package dev.mathops.db.reclogic.query;

import java.time.LocalDate;

/**
 * Criteria for a date field. This class supports a date value and a comparison operator, which allows the following
 * types of logic:
 *
 * <pre>
 *   WHERE fieldName = date(2023,12,31)
 *   WHERE fieldName > date(2023,12,31)
 *   WHERE fieldName >= date(2023,12,31)
 *   WHERE fieldName < date(2023,12,31)
 *   WHERE fieldName <= date(2023,12,31)
 *   WHERE fieldName != date(2023,12,31)
 * </pre>
 */
public class DateCriteria {

    /** The field value. */
    public final LocalDate value;

    /** The comparison to use on the field. */
    public final EComparison comparison;

    /**
     * Constructs a new {@code DateCriteria}.
     *
     * @param theValue      the value
     * @param theComparison the comparison
     */
    public DateCriteria(final LocalDate theValue, final EComparison theComparison) {

        this.value = theValue;
        this.comparison = theComparison;
    }
}
