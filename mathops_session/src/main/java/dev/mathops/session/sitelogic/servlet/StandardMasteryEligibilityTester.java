package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ResolvedStandardMilestones;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.StudentUnitMasteryRec;
import dev.mathops.db.old.reclogic.StudentUnitMasteryLogic;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * An object to check the eligibility of a student to master standards in a course. For a given student course, this
 * returns a list of the standard exams for which the student is eligible to demonstrate mastery but has not yet done
 * so.
 */
public enum StandardMasteryEligibilityTester {
    ;

    /**
     * Check a single exam for eligibility. This method does some initial tests on validity of inputs, then goes through
     * a multistage process to test the eligibility of the student to take the requested exam. Relevant status
     * information is accumulated, including holds on the student record, or the reason the exam cannot be taken at this
     * time.
     *
     * @param cache      the data cache
     * @param now        the date/time to consider as "now"
     * @param reg        the registration record
     * @param stu        the student record
     * @param milestones the resolved milestones
     * @param unit       the unit
     * @param objective  the objective
     * @param reasons    a buffer to which to append the reason the exam is unavailable
     * @param holds      a list to which holds are added if present on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean isStandardEligible(final Cache cache, final ZonedDateTime now,
                                             final RawStcourse reg, final RawStudent stu,
                                             final ResolvedStandardMilestones milestones,
                                             final int unit, final int objective, final HtmlBuilder reasons,
                                             final Collection<RawAdminHold> holds) throws SQLException {

        boolean result = false;

        // The course must be "open"
        if ("Y".equals(reg.openStatus)) {

            // The student must not have any fatal holds
            if ("F".equals(stu.sevAdminHold)) {
                reasons.add("You have a hold on your account that prevents taking exams.");
                holds.addAll(RawAdminHoldLogic.queryByStudent(cache, stu.stuId));
            } else if (unit >= 1 && unit <= 10) {
                // TODO: The course deadline must not be in the past

                final StudentUnitMasteryLogic masteryLogic = StudentUnitMasteryLogic.get(cache);

                // The student must be eligible to demonstrate mastery on the standard
                StudentUnitMasteryRec unitMastery =
                        masteryLogic.query(cache, stu.stuId, reg.course, Integer.valueOf(unit));

                if (unitMastery == null) {
                    // TODO: Should actually query work record and make sure we're inserting accurate data

                    unitMastery = new StudentUnitMasteryRec(stu.stuId, reg.course,
                            Integer.valueOf(unit), Integer.valueOf(0), null, null, null, null);
                    masteryLogic.insert(cache, unitMastery);
                }

                if (objective == 1) {
                    if (unitMastery.s1Status == null) {
                        reasons.add("Learning target assignment not completed");
                    } else {
                        result = true;
                    }
                } else if (objective == 2) {
                    if (unitMastery.s2Status == null) {
                        reasons.add("Learning target assignment not completed");
                    } else {
                        result = true;
                    }
                } else if (objective == 3) {
                    if (unitMastery.s3Status == null) {
                        reasons.add("Learning target assignment not completed");
                    } else {
                        result = true;
                    }
                } else {
                    reasons.add("Invalid learning target number.");
                }
            } else {
                reasons.add("Invalid unit.");
            }
        } else {
            reasons.add("Course is not open.");
        }

        return result;
    }
}
