package dev.mathops.db.old.logic;

import dev.mathops.db.old.rawrecord.RawAdminHold;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A data class containing a student's status with respect to the various Tutorials.
 */
public final class PrecalcTutorialStatus {

    /** All holds the student currently has. */
    final List<RawAdminHold> holds;

    /**
     * Flag indicating student has completed the math placement tool and is eligible for one or more of the Precalculus
     * Tutorials (either now or in the future).
     */
    public boolean eligibleForPrecalcTutorial;

    /**
     * List of course IDs (in 'M 1170' format) of courses whose corresponding Precalculus Tutorial has been completed.
     */
    public final Set<String> completedPrecalcTutorials;

    /**
     * The course ID (in 'M 1170' format) of the course whose corresponding Precalculus Tutorial is currently available
     * to the student.
     */
   public String nextPrecalcTutorial;

    /**
     * The course (M 1170 through M 1260) for which the student is currently eligible to take the precalculus exam (has
     * passed the unit 4 review).
     */
    public final Set<String> eligiblePrecalcExamCourses;

    /** Dates the website is available. */
    public DateRangeGroups webSiteAvailability;

    /**
     * Constructs a new {@code PrecalcTutorialStatus}.
     */
    PrecalcTutorialStatus() {

        this.holds = new ArrayList<>(2);
        this.completedPrecalcTutorials = new HashSet<>(5);
        this.eligiblePrecalcExamCourses = new HashSet<>(5);
    }
}
