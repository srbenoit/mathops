package dev.mathops.session.sitelogic.data;

import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for the configuration records associated with a unit within a course.
 */
public final class SiteDataCfgUnit {

    /** The course unit record. */
    public final RawCunit courseUnit;

    /** The course section unit record . */
    public final RawCusection courseSectionUnit;

    /** The exams associated with the course/unit . */
    private final List<RawExam> exams;

    /**
     * Constructs a new {@code SiteDataCfgUnit}.
     *
     * @param cache        the data cache
     * @param theCusection the {@code RawCusection} model for the unit
     * @throws SQLException if there was an error accessing the database
     */
    SiteDataCfgUnit(final Cache cache, final RawCusection theCusection) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        this.courseSectionUnit = theCusection;
        this.courseUnit = systemData.getCourseUnit(theCusection.course, theCusection.unit, theCusection.termKey);
        this.exams = systemData.getActiveExamByCourseUnit(theCusection.course, theCusection.unit);
    }

    /**
     * Gets the list of exams for the unit.
     *
     * @return the list of exams
     */
    public List<RawExam> getExams() {

        return new ArrayList<>(this.exams);
    }
}
