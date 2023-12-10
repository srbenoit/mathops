package dev.mathops.db.generalized.criteria;

import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;

import java.time.LocalDate;

/**
 *  A criterion that can be used to match a LocalDate field.
 */
public class LocalDateFieldCriterion extends AbstractFieldCriterion {

    /** The match type. */
    final ETemporalMatchType matchType;

    /** The local dates to use in the match process. */
    final LocalDate[] localDates;

    /**
     * Constructs a new {@code LocalDateFieldCriterion}.
     *
     * @param theField the field
     * @param theMatchType the type of match to perform
     * @param theLocalDates the local dates to match using the match type
     */
    public LocalDateFieldCriterion(final Field theField, final ETemporalMatchType theMatchType,
                                   final LocalDate... theLocalDates) {

        super(theField);

        if (theField.getType() != EFieldType.LOCAL_DATE) {
            throw new IllegalArgumentException("A LocalDate field criterion must be used with a Long field");
        }
        if (theMatchType == null) {
            throw new IllegalArgumentException("The match type may not be null");
        }

        if (theLocalDates != null) {
            for (final LocalDate test : theLocalDates) {
                if (test == null) {
                    throw new IllegalArgumentException("Match LocalDate list may not contain null values");
                }
            }
        }

        if (theMatchType != ETemporalMatchType.IS_NULL && theMatchType != ETemporalMatchType.IS_NOT_NULL
            && (theLocalDates == null || theLocalDates.length == 0)) {
            throw new IllegalArgumentException("At least one match local date must be provided.");
        }

        this.matchType = theMatchType;
        this.localDates = theLocalDates == null || theLocalDates.length == 0 ? new LocalDate[0] : theLocalDates.clone();
    }
}
