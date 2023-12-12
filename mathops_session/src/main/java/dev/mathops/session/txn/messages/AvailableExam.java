package dev.mathops.session.txn.messages;

import dev.mathops.db.old.rawrecord.RawExam;

/**
 * An available exam.
 */
public class AvailableExam {

    /** The exam being tested. */
    public RawExam exam;

    /** The factor by which to adjust exam time limits. */
    public Float timelimitFactor;

    /**
     * Constructs a new {@code AvailableExam}.
     */
    public AvailableExam() {

        // No action
    }
}
