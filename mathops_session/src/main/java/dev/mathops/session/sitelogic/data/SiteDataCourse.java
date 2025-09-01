package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.schema.legacy.RawCuobjective;
import dev.mathops.db.schema.legacy.RawCusection;
import dev.mathops.db.schema.legacy.RawPacingStructure;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A container for the course-oriented data relating to the courses in which a student is enrolled or visiting, and
 * which are included in the current context for a {@code SiteData} object.
 */
public final class SiteDataCourse {

    /** A zero-length array used to create other arrays. */
    private static final Integer[] ZERO_LEN_INT_ARR = new Integer[0];

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The configuration of each course/section, keyed on course ID. */
    private final Map<String, Map<String, SiteDataCfgCourse>> courseConfigs;

    /** The ordered set of unit numbers for each course, keyed on course ID. */
    private final Map<String, SortedSet<Integer>> units;

    /** The configuration of each course unit keyed on course ID, unit number. */
    private final Map<String, Map<Integer, SiteDataCfgUnit>> courseUnitConfigs;

    /** The ordered list of objective numbers for each unit, keyed on course ID, unit number. */
    private final Map<String, Map<Integer, SortedSet<Integer>>> objectives;

    /** The configuration of each objective keyed on course ID, unit number, objective number. */
    private final Map<String, Map<Integer, Map<Integer, SiteDataCfgObjective>>> objectiveConfigs;

    /**
     * Constructs a new {@code SiteDataCourse}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataCourse(final SiteData theOwner) {

        this.owner = theOwner;

        this.courseConfigs = new TreeMap<>();
        this.units = new TreeMap<>();
        this.courseUnitConfigs = new TreeMap<>();
        this.objectives = new TreeMap<>();
        this.objectiveConfigs = new TreeMap<>();
    }

    /**
     * Adds a course ID and section number to this object (if it does not already exist), querying the basic
     * configuration data for that course section as needed. Called as the student's registrations and visiting course
     * records are loaded.
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @param termKey    the term in which the course was taken
     * @return the generated (or cached) {@code SiteDataCfgCourse} object
     * @throws SQLException if there is an error accessing the database
     */
    SiteDataCfgCourse addCourse(final Cache cache, final String courseId, final String sectionNum,
                                final TermKey termKey) throws SQLException {

        final Map<String, SiteDataCfgCourse> map = this.courseConfigs.computeIfAbsent(courseId, s -> new TreeMap<>());

        SiteDataCfgCourse cfg = map.get(sectionNum);

        if (cfg == null) {
            // final long t0 = System.currentTimeMillis();

            cfg = new SiteDataCfgCourse(cache, courseId, sectionNum, termKey, this.owner);
            // final long t1 = System.currentTimeMillis();

            if (cfg.course == null) {
                Log.warning("Unable to create information for ", courseId, ", sect ", sectionNum, " for term ",
                        termKey);
                cfg = null;
            } else {
                final RawPacingStructure pacingStructure = cfg.pacingStructure;
                if (pacingStructure != null && "Y".equals(pacingStructure.requireLicensed)
                        && "N".equals(this.owner.studentData.getStudent().licensed)) {

                    cfg.mustTakeUsersExam = true;
                }

                // final long t2 = System.currentTimeMillis();

                if (loadCourseUnits(cache, courseId, sectionNum, termKey)) {
                    map.put(sectionNum, cfg);
                } else {
                    cfg = null;
                }
            }

            // final long t3 = System.currentTimeMillis();
            // Log.info(" COURSE 1: " + (t1 - t0));
            // Log.info(" COURSE 2: " + (t2 - t1));
            // Log.info(" COURSE 3: " + (t3 - t2));
        }

        return cfg;
    }

    /**
     * Loads the list of units in the course, and populates the map of {@code SiteDataCfgUnit} objects.
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @return the generated (or cached) {@code SiteDataCfgCourse} object
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadCourseUnits(final Cache cache, final String courseId, final String sectionNum,
                                    final TermKey termKey) throws SQLException {

        boolean success = true;

        final SystemData systemData = cache.getSystemData();
        final List<RawCusection> courseSectionUnits = systemData.getCourseUnitSections(courseId, sectionNum, termKey);

        for (final RawCusection cusect : courseSectionUnits) {

            final SiteDataCfgUnit data = new SiteDataCfgUnit(cache, cusect);

            final Integer unit = Integer.valueOf(cusect.unit.intValue());

            if (!loadCourseUnitObjectives(cache, courseId, unit, termKey)) {
                success = false;
                break;
            }

            final SortedSet<Integer> set = this.units.computeIfAbsent(courseId, s -> new TreeSet<>());
            set.add(unit);

            final Map<Integer, SiteDataCfgUnit> map = this.courseUnitConfigs.computeIfAbsent(courseId,
                    s -> new TreeMap<>());
            map.put(unit, data);
        }

        return success;
    }

    /**
     * Loads the list of units in the course, and populates the map of {@code SiteDataCfgUnit} objects.
     *
     * @param cache    the data cache
     * @param courseId the course ID
     * @param unit     the unit number
     * @return the generated (or cached) {@code SiteDataCfgCourse} object
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadCourseUnitObjectives(final Cache cache, final String courseId,
                                             final Integer unit, final TermKey termKey) throws SQLException {

        boolean success = true;

        final List<RawCuobjective> courseUnitObjectives =
                cache.getSystemData().getCourseUnitObjectives(courseId, unit, termKey);

        for (final RawCuobjective cuobj : courseUnitObjectives) {
            final SiteDataCfgObjective data = new SiteDataCfgObjective(cache, cuobj);

            if (data.courseUnitObjective == null) {
                success = false;
                break;
            }

            final Map<Integer, SortedSet<Integer>> map = this.objectives.computeIfAbsent(courseId,
                    s -> new TreeMap<>());

            final SortedSet<Integer> set = map.computeIfAbsent(unit, k -> new TreeSet<>());
            set.add(cuobj.objective);

            final Map<Integer, Map<Integer, SiteDataCfgObjective>> map2 =
                    this.objectiveConfigs.computeIfAbsent(courseId, s -> new TreeMap<>());

            final Map<Integer, SiteDataCfgObjective> map3 = map2.computeIfAbsent(unit, k -> new TreeMap<>());

            map3.put(cuobj.objective, data);
        }

        return success;
    }

    public Map<String, Map<String, SiteDataCfgCourse>> getCourses() {

        return this.courseConfigs;
    }

    /**
     * Gets the course configuration data for a course section.
     *
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @return the {@code SiteDataCfgCourse}
     */
    public SiteDataCfgCourse getCourse(final String courseId, final String sectionNum) {

        final SiteDataCfgCourse result;

        final Map<String, SiteDataCfgCourse> map = this.courseConfigs.get(courseId);
        if (map == null) {
            result = null;
        } else {
            result = map.get(sectionNum);
        }

        return result;
    }

    /**
     * Gets the course configuration data for a course section.
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @param termKey    the term key
     * @return the {@code SiteDataCfgCourse}
     * @throws SQLException if there is an error accessing the database
     */
    SiteDataCfgCourse getCourse(final Cache cache, final String courseId, final String sectionNum,
                                final TermKey termKey) throws SQLException {

        SiteDataCfgCourse result;

        final Map<String, SiteDataCfgCourse> map = this.courseConfigs.get(courseId);
        if (map == null) {
            result = null;
        } else {
            result = map.get(sectionNum);
        }

        if (result == null) {
            result = addCourse(cache, courseId, sectionNum, termKey);
        }

        return result;
    }

    /**
     * Gets the maximum unit number for a course.
     *
     * @param courseId the course ID
     * @return the maximum unit number; {@code null} if no units for course
     */
    public Integer getMaxUnit(final String courseId) {

        Integer max = null;

        final Map<Integer, SiteDataCfgUnit> map = this.courseUnitConfigs.get(courseId);

        if (map == null) {
            Log.warning("Unable to determine max unit of ", courseId);
        } else {
            for (final Integer val : map.keySet()) {
                if (max == null || max.intValue() < val.intValue()) {
                    max = val;
                }
            }
        }

        return max;
    }

    /**
     * Gets the list of units loaded for a course.
     *
     * @param courseId the course ID
     * @return the ordered list of units
     */
    public Integer[] getUnitsForCourse(final String courseId) {

        final SortedSet<Integer> set = this.units.get(courseId);
        final Integer[] result;

        if (set == null) {
            result = ZERO_LEN_INT_ARR;
        } else {
            result = set.toArray(ZERO_LEN_INT_ARR);
        }

        return result;
    }

    /**
     * Gets the course unit configuration data for a course unit.
     *
     * @param courseId the course ID
     * @param unit     the unit number
     * @return the {@code SiteDataCfgUnit}
     */
    public SiteDataCfgUnit getCourseUnit(final String courseId, final Integer unit) {

        final SiteDataCfgUnit result;

        final Map<Integer, SiteDataCfgUnit> map = this.courseUnitConfigs.get(courseId);
        if (map == null) {
            result = null;
        } else {
            result = map.get(unit);
        }

        return result;
    }

    /**
     * Gets the list of objectives loaded for a course unit.
     *
     * @param courseId the course ID
     * @param unit     the unit number
     * @return the ordered list of objectives
     */
    Integer[] getObjectivesForUnit(final String courseId, final Integer unit) {

        final Map<Integer, SortedSet<Integer>> map = this.objectives.get(courseId);
        final Integer[] result;

        if (map == null) {
            result = ZERO_LEN_INT_ARR;
        } else {
            final SortedSet<Integer> set = map.get(unit);
            if (set == null) {
                result = null;
            } else {
                result = set.toArray(ZERO_LEN_INT_ARR);
            }
        }

        return result;
    }

    /**
     * Gets the course unit objective configuration data for a course unit objective.
     *
     * @param courseId  the course ID
     * @param unit      the unit number
     * @param objective the objective number
     * @return the {@code SiteDataCfgObjective}
     */
    SiteDataCfgObjective getCourseUnitObjective(final String courseId, final Integer unit,
                                                final Integer objective) {

        final SiteDataCfgObjective result;

        final Map<Integer, Map<Integer, SiteDataCfgObjective>> map1 =
                this.objectiveConfigs.get(courseId);

        if (map1 == null) {
            result = null;
        } else {
            final Map<Integer, SiteDataCfgObjective> map2 = map1.get(unit);

            if (map2 == null) {
                result = null;
            } else {
                result = map2.get(objective);
            }
        }

        return result;
    }
}
