package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to calculate the total points from unit and final exams for a single registration.
 */
public enum CalcExamTotal {
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
            final List<RawStexam> exams = RawStexamLogic.queryByStudentCourse(cache, reg.stuId, reg.course, false);
            final Map<Integer, Integer> unitScores = buildUnitScores(exams);

            int total = 0;
            for (final Integer value : unitScores.values()) {
                total += value.intValue();
            }
            result = Integer.valueOf(total);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student exam records for ", reg.stuId, " in ", reg.course, ex);
        }

        return result;
    }

    /**
     * Builds a map from unit number to the highest score in that unit.
     *
     * @param exams the list of exams
     * @return a map from unit number to highest proctored exam score
     */
    private static Map<Integer, Integer> buildUnitScores(final Iterable<RawStexam> exams) {

        final Map<Integer, Integer> unitScores = new HashMap<>(5);

        for (final RawStexam exam : exams) {
            if ("Y".equals(exam.passed) && exam.examScore != null && exam.unit != null) {
                final String type = exam.examType;
                if ("U".equals(type) || "UE".equals(type) || "M".equals(type) || "ME".equals(type)
                    || "F".equals(type) || "FE".equals(type)) {

                    final Integer existing = unitScores.get(exam.unit);
                    if (existing == null || existing.intValue() < exam.examScore.intValue()) {
                        unitScores.put(exam.unit, exam.examScore);
                    }
                }
            }
        }

        return unitScores;
    }
}