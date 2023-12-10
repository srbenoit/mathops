package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

import java.time.LocalTime;

/**
 *  A criterion that can be used to match a LocalTime field.
 */
public class LocalTimeFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ETemporalMatchType matchType;

    /** The local time to use in the match process. */
    final LocalTime[] localTimes;

    /**
     * Constructs a new {@code LocalTimeFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theLocalTimes the local times to match using the match type
     */
    public LocalTimeFieldCriterion(final Field theField, final ETemporalMatchType theMatchType,
                                   final LocalTime... theLocalTimes) {

        super(theField);

        if (theField.getType() != EFieldType.LOCAL_TIME) {
            throw new IllegalArgumentException("A LocalTime field criterion must be used with a Long field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theLocalTimes != null) {
            for (final LocalTime test : theLocalTimes) {
                if (test == null) {
                    throw new IllegalArgumentException("Match LocalTime list may not contain null values");
                }
            }
        }

        if (theMatchType != ETemporalMatchType.IS_NULL && theMatchType != ETemporalMatchType.IS_NOT_NULL
            && (theLocalTimes == null || theLocalTimes.length == 0)) {
            throw new IllegalArgumentException("At least one match local time must be provided.");
        }

        this.matchType = theMatchType;
        this.localTimes = theLocalTimes == null || theLocalTimes.length == 0 ? new LocalTime[0] : theLocalTimes.clone();
    }
}
