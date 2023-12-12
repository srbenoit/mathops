package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawStexam;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A utility class to test whether a student is eligible for the proctored exam in a precalculus tutorial
 */
public enum PrecalcExamEligibility {
    ;

    /**
     * Tests whether the student is eligible for the proctored precalculus tutorial exam.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course ID
     * @return the eligibility
     * @throws SQLException if there is an error accessing the database
     */
    public static EEligibility isEligible(final Cache cache, final String stuId,
                                          final String course) throws SQLException {

        final EEligibility result;

        final List<RawStexam> stexams = RawStexamLogic.getExams(cache, stuId, course, //
                Integer.valueOf(4), false, "R", "U");

        LocalDate latestPassedRE = null;
        Integer latestPassedRETime = null;
        boolean hasPassedUnit = false;

        for (final RawStexam test : stexams) {
            if ("R".equals(test.examType)
                    && "Y".equals(test.passed)) {
                if (latestPassedRE == null || latestPassedRETime == null
                        || test.examDt.isAfter(latestPassedRE)
                        || (test.examDt.equals(latestPassedRE)
                        && test.finishTime.intValue() > latestPassedRETime.intValue())) {
                    latestPassedRE = test.examDt;
                    latestPassedRETime = test.finishTime;
                }
            } else if ("U".equals(test.examType) && "Y".equals(test.passed)) {
                hasPassedUnit = true;
            }
        }

        if (hasPassedUnit) {
            result = EEligibility.ELIGIBLE_BUT_ALREADY_PASSED;
        } else if (latestPassedRE == null || latestPassedRETime == null) {
            result = EEligibility.INELIGIBLE_RE4_NOT_PASSED;
        } else {
            int failUESinceLatestRE = 0;

            for (final RawStexam test : stexams) {
                if ("U".equals(test.examType) && "N".equals(test.passed)) {

                    if (test.examDt.isAfter(latestPassedRE) || (test.examDt.equals(latestPassedRE)
                            && test.finishTime.intValue() > latestPassedRETime.intValue())) {
                        ++failUESinceLatestRE;
                    }
                }
            }

            if (failUESinceLatestRE >= 2) {
                result = EEligibility.INELIGIBLE_MUST_REPASS_RE4;
            } else {
                result = EEligibility.ELIGIBLE;
            }
        }

        return result;
    }
}
