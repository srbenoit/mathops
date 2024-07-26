package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * An object to check the eligibility of a student to complete a learning target assignment in a standards-based
 * course.
 */
public enum LearningTargetAssignmentEligibilityTester {
    ;

    /**
     * Check a single assignment for eligibility. This method does some initial tests on validity of inputs, then goes
     * through a multistage process to test the eligibility of the student to take the requested exam. Relevant status
     * information is accumulated, including holds on the student record, or the reason the exam cannot be taken at this
     * time.
     *
     * @param cache     the data cache
     * @param reg       the registration record
     * @param stu       the student record
     * @param unit      the unit
     * @param objective the objective
     * @param reasons   a buffer to which to append the reason the exam is unavailable
     * @param holds     a list to which holds are added if present on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean isEligible(final Cache cache, final RawStcourse reg, final RawStudent stu,
                                     final int unit, final int objective, final HtmlBuilder reasons,
                                     final Collection<? super RawAdminHold> holds) throws SQLException {

        boolean result = false;

        // The course must be "open"
        if ("Y".equals(reg.openStatus)) {

            // The student must not have any fatal holds
            if ("F".equals(stu.sevAdminHold)) {
                reasons.add("You have a hold on your account that prevents accessing course content.");
                final List<RawAdminHold> stuHolds = RawAdminHoldLogic.queryByStudent(cache, stu.stuId);
                holds.addAll(stuHolds);
            } else {
                final SystemData systemData = cache.getSystemData();
                final RawCsection csection = systemData.getCourseSection(reg.course, reg.sect, reg.termKey);

                if (csection == null) {
                    reasons.add("Unable to look up data for " + reg.course + " section " + reg.sect + " in "
                            + reg.termKey.longString);
                } else if (csection.pacingStructure == null) {
                    reasons.add("No pacing structure defined for " + reg.course + " section " + reg.sect + " in "
                            + reg.termKey.longString);
                } else {
                    final String pacing = csection.pacingStructure;

                    if (objective == 1) {
                        if (systemData.isRequiredByPacingRules(reg.termKey, pacing, "HW", "SR_M")) {
                            final List<RawSthomework> sthw = getObjectiveHw(cache, reg, unit, objective - 1);
                            if (hasPassed(sthw)) {
                                result = true;
                            } else {
                                reasons.add("Skills Review assignment not yet passed");
                            }
                        } else if (systemData.isRequiredByPacingRules(reg.termKey, pacing, "HW", "SR_A")) {
                            final List<RawSthomework> sthw = getObjectiveHw(cache, reg, unit, objective - 1);
                            if (sthw.isEmpty()) {
                                reasons.add("Skills Review assignment not yet attempted");
                            }
                        } else {
                            result = true;
                        }
                    } else {
                        if (systemData.isRequiredByPacingRules(reg.termKey, pacing, "HW", "HW_M")) {
                            final List<RawSthomework> sthw = getObjectiveHw(cache, reg, unit, objective - 1);
                            if (hasPassed(sthw)) {
                                result = true;
                            } else {
                                reasons.add("Prior learning target assignment not yet passed");
                            }
                        } else if (systemData.isRequiredByPacingRules(reg.termKey, pacing, "HW", "HW_A")) {
                            final List<RawSthomework> sthw = getObjectiveHw(cache, reg, unit, objective - 1);
                            if (sthw.isEmpty()) {
                                reasons.add("Prior learning target assignment not yet attempted");
                            }
                        } else {
                            result = true;
                        }
                    }
                }
            }
        } else {
            reasons.add("Course is not open.");
        }

        return result;
    }

    /**
     * Retrieves homework records for a unit and objective.
     *
     * @param cache     the data cache
     * @param reg       the course registration
     * @param unit      the unit
     * @param objective the objective
     * @return the list of homeworks on record for the specified course, unit, and objective
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawSthomework> getObjectiveHw(final Cache cache, final RawStcourse reg, final int unit,
                                                      final int objective) throws SQLException {
        final Integer unitObj = Integer.valueOf(unit);
        final Integer obj = Integer.valueOf(objective);

        return RawSthomeworkLogic.queryByStudentCourseUnitObjective(cache, reg.stuId, reg.course, unitObj, obj, false);
    }

    /**
     * Checks whether there is at least one passing homework record in a list of homeworks.
     *
     * @param sthw the list of homeworks
     * @return true if there is at least one record marked as passing
     */
    private static boolean hasPassed(final List<RawSthomework> sthw) {

        boolean passed = false;

        for (final RawSthomework test : sthw) {
            if ("Y".equals(test.passed)) {
                passed = true;
                break;
            }
        }

        return passed;
    }
}
