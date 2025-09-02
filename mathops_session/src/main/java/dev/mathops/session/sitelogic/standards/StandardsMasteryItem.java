package dev.mathops.session.sitelogic.standards;

import dev.mathops.db.schema.legacy.rec.RawStdItem;

/**
 * An assessment with a collection if items selected from item groups attached to standards that a student has not yet
 * mastered. On submission, for every item that is correct, the associated item group should be marked as mastered on
 * the student's "ststd" record for the corresponding standard.
 */
class StandardsMasteryItem {

    /** The standard item record. */
    private final RawStdItem stdItem;

    /**
     * Constructs a new {@code StandardsMasteryExam}.
     *
     * @param theStdItem the standard item record
     */
    StandardsMasteryItem(final RawStdItem theStdItem) {

        this.stdItem = theStdItem;
    }

}
