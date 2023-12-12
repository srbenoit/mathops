package dev.mathops.db.old.reclogic.query;

import java.time.LocalDateTime;

/**
 * Criteria for a date/time field (which is assumed to store year to second). This class supports a date/time value and
 * a comparison operator, which allows the following types of logic:
 *
 * <pre>
 *   WHERE fieldName = datetime(2023,12,31,11,59,50)
 *   WHERE fieldName > datetime(2023,12,31,11,59,50)
 *   WHERE fieldName >= date(2023,12,31,11,59,50)
 *   WHERE fieldName < datetime(2023,12,31,11,59,50)
 *   WHERE fieldName <= datetime(2023,12,31,11,59,50)
 *   WHERE fieldName != datetime(2023,12,31,11,59,50)
 * </pre>
 */
public class DateTimeCriteria {

    /** The field value. */
    public final LocalDateTime value;

    /** The comparison to use on the field. */
    public final EComparison comparison;

    /**
     * Constructs a new {@code DateTimeCriteria}.
     *
     * @param theValue      the value
     * @param theComparison the comparison
     */
    public DateTimeCriteria(final LocalDateTime theValue, final EComparison theComparison) {

        this.value = theValue;
        this.comparison = theComparison;
    }
}
