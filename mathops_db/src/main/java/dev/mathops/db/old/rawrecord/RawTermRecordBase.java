package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rec.RecBase;

/**
 * The base class for raw records that include "term" and "term_yr" fields.
 */
abstract class RawTermRecordBase extends RecBase {

    /** A field name. */
    static final String FLD_TERM = "term";

    /** A field name. */
    static final String FLD_TERM_YR = "term_yr";

    /** The 'term' and 'term_yr' field values. */
    public TermKey termKey;

    /**
     * Constructs a new {@code RawTermRecordBase}.
     */
    RawTermRecordBase() {

        super();
    }

    /**
     * Constructs a new {@code RawTermRecordBase}.
     *
     * @param theTermKey the term key
     */
    RawTermRecordBase(final TermKey theTermKey) {

        super();

        this.termKey = theTermKey;
    }
}
