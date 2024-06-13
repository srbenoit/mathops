package dev.mathops.db.old.logic.mathplan;

/**
 * A container for status of Math Placement relative to a Math Plan.
 */
public class MathPlanPlacementStatus {

    /** True if student has completed the Math Plan and plan indicates placement is needed. */
    public final boolean isPlacementNeeded;

    /** True if placement requirement has been satisfied (even if Math Plan indicates placement is not needed). */
    public final boolean isPlacementComplete;

    /**
     * If placement requirement has been satisfied, how.  One of the following:
     * <ul>
     *     <li>Math Placement Tool Completed</li>
     *     <li>Placement requirement satisfied by test score</li>
     *     <li>Placement requirement satisfied by transfer credit</li>
     *     <li>Placement requirement satisfied by course credit</li>
     * </ul>
     */
    public final EHowSatisfiedPlacement howSatisfied;

    /**
     * Constructs a new {@code MathPlanPlacementStatus}
     *
     * @param placementNeeded    true if student has completed the Math Plan and plan indicates placement is needed
     * @param placementSatisfied true if placement requirement has been satisfied (even if Math Plan indicates placement
     *                           is not needed)
     * @param how                f placement requirement has been satisfied, how.  One of the following
     */
    public MathPlanPlacementStatus(final boolean placementNeeded, final boolean placementSatisfied,
                                   final EHowSatisfiedPlacement how) {

        this.isPlacementNeeded = placementNeeded;
        this.isPlacementComplete = placementSatisfied;
        this.howSatisfied = how;
    }
}
