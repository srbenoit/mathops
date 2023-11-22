package dev.mathops.session.sitelogic.data;

import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawCuobjective;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.reclogic.AssignmentLogic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for the configuration records associated with an objective within one unit of a course.
 */
final class SiteDataCfgObjective {

    /** The course unit objective record . */
    final RawCuobjective courseUnitObjective;

    /** The homework associated with the course/unit/objective. */
    private final List<AssignmentRec> homeworks;

    /**
     * Constructs a new {@code SiteDataCfgObjective}.
     *
     * @param cache            the data cache
     * @param theCourseUnitObj the {@code CCourseUnitObjective} model for the objective
     * @throws SQLException if there was an error accessing the database
     */
    SiteDataCfgObjective(final Cache cache, final RawCuobjective theCourseUnitObj) throws SQLException {

        this.homeworks = AssignmentLogic.get(cache).queryActiveByCourseUnitObjective(cache,
                theCourseUnitObj.course, theCourseUnitObj.unit, theCourseUnitObj.objective, "HW");

        if (this.homeworks == null) {
            this.courseUnitObjective = null;
        } else {
            this.courseUnitObjective = theCourseUnitObj;
        }
    }

    /**
     * Gets the list of homeworks for the objective.
     *
     * @return the list of homeworks
     */
    public List<AssignmentRec> getHomeworks() {

        return new ArrayList<>(this.homeworks);
    }
}
