package dev.mathops.db.old.logic;

import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.db.rec.StuStandardMilestoneRec;

import java.time.LocalDate;

/**
 * A resolved milestone, which stores the original {@code StandardMilestoneRec} record and a
 * {@code StudentStandardMilestoneRec} record if found.
 */
public final class ResolvedStandardMilestone {

    /** The original milestone. */
    final StandardMilestoneRec original;

    /** The student override, {@code null} if none. */
    final StuStandardMilestoneRec override;

    /**
     * Constructs a new {@code ResolvedStandardMilestone}.
     *
     * @param theOriginal the original milestone
     * @param theOverride the student override, {@code null} if none
     */
    ResolvedStandardMilestone(final StandardMilestoneRec theOriginal, final StuStandardMilestoneRec theOverride) {

        this.original = theOriginal;
        this.override = theOverride;
    }

    /**
     * Gets the resolved milestone date.
     *
     * @return the date
     */
    public LocalDate getMilestoneDate() {

        return this.override == null ? this.original.msDate : this.override.msDate;
    }

    /**
     * Tests whether this milestone has been overridden.
     *
     * @return true if overridden
     */
    public boolean isOverridden() {

        return this.override != null;
    }
}
