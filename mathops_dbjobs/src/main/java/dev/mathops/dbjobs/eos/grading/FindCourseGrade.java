package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;

/**
 * A utility class to determine the grade in a course based on the grading scale.
 */
public enum FindCourseGrade {
    ;

    /**
     * Finds the best passing scores for all proctored exams in a course and calculates their total.
     *
     * @param cache       the data cache
     * @param reg         the course registration for which to construct the student pace summary
     * @param totalPoints the total points scored
     * @return the course grade if successful; null if not
     */
    static String calculate(final Cache cache, final RawStcourse reg, final int totalPoints) {

        String grade = null;

        if ("F".equals(reg.courseGrade) || "I".equals(reg.courseGrade)) {
            grade = reg.courseGrade;
        } else {
            final TermKey key = "Y".equals(reg.iInProgress) ? reg.iTermKey : reg.termKey;

            try {
                final RawCsection section = RawCsectionLogic.query(cache, key, reg.course, reg.sect);
                if (section == null) {
                    Log.warning("No course section record found for ", reg.course, " section ", reg.sect, " in ", key);
                } else if (section.aMinScore != null && totalPoints >= section.aMinScore.intValue()) {
                    grade = "A";
                } else if (section.bMinScore != null && totalPoints >= section.bMinScore.intValue()) {
                    grade = "B";
                } else if (section.cMinScore != null && totalPoints >= section.cMinScore.intValue()) {
                    grade = "C";
                } else if (section.dMinScore != null && totalPoints >= section.dMinScore.intValue()) {
                    grade = "D";
                } else {
                    grade = "U";
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query course section record for ", reg.course, " section ", reg.sect, " in ",
                        key, ex);
            }
        }

        return grade;
    }
}