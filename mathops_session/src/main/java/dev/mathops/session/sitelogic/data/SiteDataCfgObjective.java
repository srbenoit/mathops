package dev.mathops.session.sitelogic.data;

import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rec.AssignmentRec;

import java.sql.SQLException;

/**
 * A container for the configuration records associated with an objective within one unit of a course.
 */
final class SiteDataCfgObjective {

    /** The course unit objective record . */
    final RawCuobjective courseUnitObjective;

    /** The homework associated with the course/unit/objective. */
    private final AssignmentRec homework;

    /**
     * Constructs a new {@code SiteDataCfgObjective}.
     *
     * @param systemData       the system data object
     * @param theCourseUnitObj the {@code CCourseUnitObjective} model for the objective
     * @throws SQLException if there was an error accessing the database
     */
    SiteDataCfgObjective(final SystemData systemData, final RawCuobjective theCourseUnitObj) throws SQLException {

        this.homework = systemData.getActiveAssignment(theCourseUnitObj.course, theCourseUnitObj.unit,
                theCourseUnitObj.objective, "HW");

        if (this.homework == null) {
            this.courseUnitObjective = null;
        } else {
            this.courseUnitObjective = theCourseUnitObj;
        }
    }

    /**
     * Gets the homework for the objective.
     *
     * @return the list homework
     */
    public AssignmentRec getHomework() {

        return this.homework;
    }
}
