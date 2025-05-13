package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStpaceSummaryLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to calculate the total points from passing review exams on time.
 */
public enum CalcStPaceTotal {
    ;

    /**
     * Finds the best passing scores for all proctored exams in a course and calculates their total.
     *
     * @param cache the data cache
     * @param reg   the course registration for which to construct the student pace summary
     * @return the total score if successful;
     */
    static Integer calculate(final Cache cache, final RawStcourse reg) {

        Integer result = null;

        try {
            final List<RawStpaceSummary> rows = RawStpaceSummaryLogic.queryByStudentCourseSectionTerm(cache, reg.stuId,
                    reg.course, reg.sect, reg.termKey);
            final Map<Integer, Integer> unitScores = buildUnitScores(rows);

            int total = 0;
            for (final Integer value : unitScores.values()) {
                total += value.intValue();
            }
            result = Integer.valueOf(total);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student pace summary records for ", reg.stuId, " in ", reg.course, ex);
        }

        return result;
    }

    /**
     * Builds a map from unit number to the highest score in that unit.
     *
     * @param rows the pace summary rows
     * @return a map from unit number to highest proctored exam score
     */
    private static Map<Integer, Integer> buildUnitScores(final Iterable<RawStpaceSummary> rows) {

        final Map<Integer, Integer> unitScores = new HashMap<>(5);

        for (final RawStpaceSummary row : rows) {
            final Integer existing = unitScores.get(row.msUnit);
            if (existing == null || existing.intValue() < row.rePoints.intValue()) {
                unitScores.put(row.msUnit, row.rePoints);
            }
        }

        return unitScores;
    }
}