package dev.mathops.db.logic;

import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawSpecialStus;
import dev.mathops.db.rawrecord.RawStudent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A data class containing a student's status with respect to the various Tutorials.
 */
public final class WinterTutorialStatus {

    /** The student record. */
    private final RawStudent student;

    /** All holds the student currently has. */
    final List<RawAdminHold> holds;

    /** All special categories to which the student currently belongs. */
    final List<RawSpecialStus> allSpecials;

    /** The precalculus tutorials (M 1170 through M 1260) for which the student is eligible over Winter break. */
    final Set<String> eligibleTutorials;

    /** Dates the web site is available (from campus_calendar). */
    public DateRangeGroups webSiteAvailability;

    /**
     * Constructs a new {@code WinterTutorialStatus}.
     *
     * @param theStudent the student record
     */
    WinterTutorialStatus(final RawStudent theStudent) {

        this.student = theStudent;
        this.holds = new ArrayList<>(2);
        this.allSpecials = new ArrayList<>(5);
        this.eligibleTutorials = new TreeSet<>();
    }

    /**
     * Gets the student record.
     *
     * @return the student
     */
    public RawStudent getStudent() {

        return this.student;
    }
}
