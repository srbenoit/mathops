package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

import java.time.LocalDateTime;

/**
 *  A criterion that can be used to match a LocalDateTime field.
 */
public class LocaDateTimeFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ETemporalMatchType matchType;

    /** The local time to use in the match process. */
    final LocalDateTime[] localDateTimes;

    /**
     * Constructs a new {@code LocaDateTimeFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theLocalDateTimes the local date/times to match using the match type
     */
    public LocaDateTimeFieldCriterion(final Field theField, final ETemporalMatchType theMatchType,
                                      final LocalDateTime... theLocalDateTimes) {

        super(theField);

        if (theField.getType() != EFieldType.LOCAL_DATE_TIME) {
            throw new IllegalArgumentException("A LocalDateTime field criterion must be used with a Long field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theLocalDateTimes != null) {
            for (final LocalDateTime test : theLocalDateTimes) {
                if (test == null) {
                    throw new IllegalArgumentException("Match LocalDateTime list may not contain null values");
                }
            }
        }

        if (theMatchType != ETemporalMatchType.IS_NULL && theMatchType != ETemporalMatchType.IS_NOT_NULL
            && (theLocalDateTimes == null || theLocalDateTimes.length == 0)) {
            throw new IllegalArgumentException("At least one match local date/time must be provided.");
        }

        this.matchType = theMatchType;
        this.localDateTimes = theLocalDateTimes == null || theLocalDateTimes.length == 0 ? new LocalDateTime[0]
                : theLocalDateTimes.clone();
    }
}
