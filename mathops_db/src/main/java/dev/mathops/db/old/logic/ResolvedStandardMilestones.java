package dev.mathops.db.old.logic;

import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.db.rec.StuStandardMilestoneRec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A container for all standard milestones for a student, after "student standard milestone" overrides have been
 * applied.
 */
public final class ResolvedStandardMilestones {

    /** The set of all resolved milestones. */
    private final List<ResolvedStandardMilestone> resolved;

    /**
     * Constructs a new {@code ResolvedStandardMilestones}.
     *
     * @param originals the original milestones for the student's pace and pace track.
     * @param overrides any student milestone overrides for the student's pace and pace track.
     */
    public ResolvedStandardMilestones(final Collection<StandardMilestoneRec> originals,
                                      final Iterable<StuStandardMilestoneRec> overrides) {

        super();

        this.resolved = new ArrayList<>(originals.size());

        for (final StandardMilestoneRec orig : originals) {
            StuStandardMilestoneRec override = null;
            for (final StuStandardMilestoneRec over : overrides) {
                if (over.paceIndex.equals(orig.paceIndex) && over.unit.equals(orig.unit)
                        && over.objective.equals(orig.objective) && over.msType.equals(orig.msType)) {
                    override = over;
                    break;
                }
            }

            this.resolved.add(new ResolvedStandardMilestone(orig, override));
        }
    }

    /**
     * Gets the resolved milestone with a specified milestone number.
     *
     * @param paceIndex the pace index
     * @param unit      the unit
     * @param objective the objective
     * @param msType    the milestone type
     * @return the resolved milestone
     */
    public ResolvedStandardMilestone getResolvedMilestone(final int paceIndex, final int unit,
                                                          final int objective, final String msType) {

        ResolvedStandardMilestone result = null;

        for (final ResolvedStandardMilestone test : this.resolved) {
            if (test.original.paceIndex.intValue() == paceIndex
                    && test.original.unit.intValue() == unit
                    && test.original.objective.intValue() == objective
                    && test.original.msType.equals(msType)) {
                result = test;
                break;
            }
        }

        return result;
    }
}
