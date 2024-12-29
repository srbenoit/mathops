package dev.mathops.db.old.rawrecord;

import dev.mathops.db.enums.EProctoringOption;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A raw "csection" record.
 */
public final class RawCsection extends RawTermRecordBase implements Comparable<RawCsection> {

    /** The table name. */
    public static final String TABLE_NAME = "csection";

    /** A field name. */
    public static final String FLD_COURSE = "course";

    /** A field name. */
    public static final String FLD_SECT = "sect";

    /** A field name. */
    public static final String FLD_SECTION_ID = "section_id";

    /** A field name. */
    public static final String FLD_ARIES_START_DT = "aries_start_dt";

    /** A field name. */
    public static final String FLD_ARIES_END_DT = "aries_end_dt";

    /** A field name. */
    public static final String FLD_START_DT = "start_dt";

    /** A field name. */
    public static final String FLD_EXAM_DELETE_DT = "exam_delete_dt";

    /** A field name. */
    public static final String FLD_INSTRN_TYPE = "instrn_type";

    /** A field name. */
    public static final String FLD_INSTRUCTOR = "instructor";

    /** A field name. */
    public static final String FLD_CAMPUS = "campus";

    /** A field name. */
    public static final String FLD_PACING_STRUCTURE = "pacing_structure";

    /** A field name. */
    public static final String FLD_MTG_DAYS = "mtg_days";

    /** A field name. */
    public static final String FLD_CLASSROOM_ID = "classroom_id";

    /** A field name. */
    public static final String FLD_LST_STCRS_CREAT_DT = "lst_stcrs_creat_dt";

    /** A field name. */
    public static final String FLD_GRADING_STD = "grading_std";

    /** A field name. */
    public static final String FLD_A_MIN_SCORE = "a_min_score";

    /** A field name. */
    public static final String FLD_B_MIN_SCORE = "b_min_score";

    /** A field name. */
    public static final String FLD_C_MIN_SCORE = "c_min_score";

    /** A field name. */
    public static final String FLD_D_MIN_SCORE = "d_min_score";

    /** A field name. */
    public static final String FLD_SURVEY_ID = "survey_id";

    /** A field name. */
    public static final String FLD_COURSE_LABEL_SHOWN = "course_label_shown";

    /** A field name. */
    public static final String FLD_DISPLAY_SCORE = "display_score";

    /** A field name. */
    public static final String FLD_DISPLAY_GRADE_SCALE = "display_grade_scale";

    /** A field name. */
    public static final String FLD_COUNT_IN_MAX_COURSES = "count_in_max_courses";

    /** A field name. */
    public static final String FLD_ONLINE = "online";

    /** A field name. */
    public static final String FLD_BOGUS = "bogus";

    /** A field name. */
    public static final String FLD_CANVAS_ID = "canvas_id";

    /** A field name. */
    public static final String FLD_SUBTERM = "subterm";

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'section_id' field value. */
    public String sectionId;

    /** The 'aries_start_dt' field value. */
    public LocalDate ariesStartDt;

    /** The 'aries_end_dt' field value. */
    public LocalDate ariesEndDt;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /** The 'exam_delete_dt' field value. */
    public LocalDate examDeleteDt;

    /** The 'instrn_type' field value. */
    public String instrnType;

    /** The 'instructor' field value. */
    public String instructor;

    /** The 'campus' field value. */
    public String campus;

    /** The 'pacing_structure' field value. */
    public String pacingStructure;

    /** The 'mtg_days' field value. */
    public String mtgDays;

    /** The 'classroom_id' field value. */
    public String classroomId;

    /** The 'lst_stcrs_creat_dt' field value. */
    public LocalDate lstStcrsCreatDt;

    /** The 'grading_std' field value. */
    public String gradingStd;

    /** The 'a_min_score' field value. */
    public Integer aMinScore;

    /** The 'b_min_score' field value. */
    public Integer bMinScore;

    /** The 'c_min_score' field value. */
    public Integer cMinScore;

    /** The 'd_min_score' field value. */
    public Integer dMinScore;

    /** The 'survey_id' field value. */
    public String surveyId;

    /** The 'course_label_shown' field value. */
    public String courseLabelShown;

    /** The 'display_score' field value. */
    public String displayScore;

    /** The 'display_grade_scale' field value. */
    public String displayGradeScale;

    /** The 'count_in_max_courses' field value. */
    public String countInMaxCourses;

    /** The 'online' field value. */
    public String online;

    /** The 'bogus' field value. */
    public String bogus;

    /** The 'canvas_id' field value. */
    public String canvasId;

    /** The 'subterm' field value. */
    public String subterm;

    /**
     * Constructs a new {@code RawCsection}.
     */
    public RawCsection() {

        super();
    }

    /**
     * Constructs a new {@code RawCsection}.
     *
     * @param theTermKey           the term key
     * @param theCourse            the course
     * @param theSect              the sect
     * @param theSectionId         the section ID
     * @param theAriesStartDt      the Aries start date
     * @param theAriesEndDt        the Aries end date
     * @param theStartDt           the start date
     * @param theExamDeleteDt      the exam delete date
     * @param theInstrnType        the instruction type
     * @param theInstructor        the instructor
     * @param theCampus            the campus
     * @param thePacingStructure   the pacing structure
     * @param theMtgDays           the days the class meets
     * @param theClassroomId       the classroom ID
     * @param theLstStcrsCreatDt   the last 'stcourse' create date
     * @param theGradingStd        the grading standard
     * @param theAMinScore         the minimum score for an A
     * @param theBMinScore         the minimum score for an B, null if B not given
     * @param theCMinScore         the minimum score for an C, null if C not given
     * @param theDMinScore         the minimum score for an D, null if D not given
     * @param theSurveyId          the survey ID
     * @param theCourseLabelShown  "Y" if the course label should be shown
     * @param theDisplayScore      "Y" if student score should be shown
     * @param theDisplayGradeScale "Y" if the grading scale should be shown
     * @param theCountInMaxCourses "Y" if the course should count toward max open courses
     * @param theOnline            "Y" if the course is online
     * @param theBogus             "Y" if the course is "bogus", and does not appear in the catalog
     * @param theCanvasId          the ID of the Canvas curse that corresponds to this section, if any
     * @param theSubterm           the subterm this section occupies ("FULL" or "LATE" so far)
     */
    public RawCsection(final TermKey theTermKey, final String theCourse, final String theSect,
                       final String theSectionId, final LocalDate theAriesStartDt, final LocalDate theAriesEndDt,
                       final LocalDate theStartDt, final LocalDate theExamDeleteDt, final String theInstrnType,
                       final String theInstructor, final String theCampus, final String thePacingStructure,
                       final String theMtgDays, final String theClassroomId, final LocalDate theLstStcrsCreatDt,
                       final String theGradingStd, final Integer theAMinScore, final Integer theBMinScore,
                       final Integer theCMinScore, final Integer theDMinScore, final String theSurveyId,
                       final String theCourseLabelShown, final String theDisplayScore,
                       final String theDisplayGradeScale, final String theCountInMaxCourses,
                       final String theOnline, final String theBogus, final String theCanvasId,
                       final String theSubterm) {

        super(theTermKey);

        this.course = theCourse;
        this.sect = theSect;
        this.sectionId = theSectionId;
        this.ariesStartDt = theAriesStartDt;
        this.ariesEndDt = theAriesEndDt;
        this.startDt = theStartDt;
        this.examDeleteDt = theExamDeleteDt;
        this.instrnType = theInstrnType;
        this.instructor = theInstructor;
        this.campus = theCampus;
        this.pacingStructure = thePacingStructure;
        this.mtgDays = theMtgDays;
        this.classroomId = theClassroomId;
        this.lstStcrsCreatDt = theLstStcrsCreatDt;
        this.gradingStd = theGradingStd;
        this.aMinScore = theAMinScore;
        this.bMinScore = theBMinScore;
        this.cMinScore = theCMinScore;
        this.dMinScore = theDMinScore;
        this.surveyId = theSurveyId;
        this.courseLabelShown = theCourseLabelShown;
        this.displayScore = theDisplayScore;
        this.displayGradeScale = theDisplayGradeScale;
        this.countInMaxCourses = theCountInMaxCourses;
        this.online = theOnline;
        this.bogus = theBogus;
        this.canvasId = theCanvasId;
        this.subterm = theSubterm;
    }

    /**
     * Extracts a "csection" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCsection fromResultSet(final ResultSet rs) throws SQLException {

        final RawCsection result = new RawCsection();

        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.sectionId = getStringField(rs, FLD_SECTION_ID);
        result.ariesStartDt = getDateField(rs, FLD_ARIES_START_DT);
        result.ariesEndDt = getDateField(rs, FLD_ARIES_END_DT);
        result.startDt = getDateField(rs, FLD_START_DT);
        result.examDeleteDt = getDateField(rs, FLD_EXAM_DELETE_DT);
        result.instrnType = getStringField(rs, FLD_INSTRN_TYPE);
        result.instructor = getStringField(rs, FLD_INSTRUCTOR);
        result.campus = getStringField(rs, FLD_CAMPUS);
        result.pacingStructure = getStringField(rs, FLD_PACING_STRUCTURE);
        result.mtgDays = getStringField(rs, FLD_MTG_DAYS);
        result.classroomId = getStringField(rs, FLD_CLASSROOM_ID);
        result.lstStcrsCreatDt = getDateField(rs, FLD_LST_STCRS_CREAT_DT);
        result.gradingStd = getStringField(rs, FLD_GRADING_STD);
        result.aMinScore = getIntegerField(rs, FLD_A_MIN_SCORE);
        result.bMinScore = getIntegerField(rs, FLD_B_MIN_SCORE);
        result.cMinScore = getIntegerField(rs, FLD_C_MIN_SCORE);
        result.dMinScore = getIntegerField(rs, FLD_D_MIN_SCORE);
        result.surveyId = getStringField(rs, FLD_SURVEY_ID);
        result.courseLabelShown = getStringField(rs, FLD_COURSE_LABEL_SHOWN);
        result.displayScore = getStringField(rs, FLD_DISPLAY_SCORE);
        result.displayGradeScale = getStringField(rs, FLD_DISPLAY_GRADE_SCALE);
        result.countInMaxCourses = getStringField(rs, FLD_COUNT_IN_MAX_COURSES);
        result.online = getStringField(rs, FLD_ONLINE);
        result.bogus = getStringField(rs, FLD_BOGUS);
        result.canvasId = getStringField(rs, FLD_CANVAS_ID);
        result.subterm = getStringField(rs, FLD_SUBTERM);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawCsection o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);
            if (result == 0) {
                result = compareAllowingNull(this.sect, o.sect);
            }
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECTION_ID, this.sectionId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ARIES_START_DT, this.ariesStartDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_ARIES_END_DT, this.ariesEndDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DELETE_DT, this.examDeleteDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_INSTRN_TYPE, this.instrnType);
        htm.add(DIVIDER);
        appendField(htm, FLD_INSTRUCTOR, this.instructor);
        htm.add(DIVIDER);
        appendField(htm, FLD_CAMPUS, this.campus);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACING_STRUCTURE, this.pacingStructure);
        htm.add(DIVIDER);
        appendField(htm, FLD_MTG_DAYS, this.mtgDays);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLASSROOM_ID, this.classroomId);
        htm.add(DIVIDER);
        appendField(htm, FLD_LST_STCRS_CREAT_DT, this.lstStcrsCreatDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_GRADING_STD, this.gradingStd);
        htm.add(DIVIDER);
        appendField(htm, FLD_A_MIN_SCORE, this.aMinScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_B_MIN_SCORE, this.bMinScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_C_MIN_SCORE, this.cMinScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_D_MIN_SCORE, this.dMinScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_SURVEY_ID, this.surveyId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_LABEL_SHOWN, this.courseLabelShown);
        htm.add(DIVIDER);
        appendField(htm, FLD_DISPLAY_SCORE, this.displayScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_DISPLAY_GRADE_SCALE, this.displayGradeScale);
        htm.add(DIVIDER);
        appendField(htm, FLD_COUNT_IN_MAX_COURSES, this.countInMaxCourses);
        htm.add(DIVIDER);
        appendField(htm, FLD_ONLINE, this.online);
        htm.add(DIVIDER);
        appendField(htm, FLD_BOGUS, this.bogus);
        htm.add(DIVIDER);
        appendField(htm, FLD_CANVAS_ID, this.canvasId);
        htm.add(DIVIDER);
        appendField(htm, FLD_SUBTERM, this.subterm);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.sectionId)
                + Objects.hashCode(this.ariesStartDt)
                + Objects.hashCode(this.ariesEndDt)
                + Objects.hashCode(this.startDt)
                + Objects.hashCode(this.examDeleteDt)
                + Objects.hashCode(this.instrnType)
                + Objects.hashCode(this.instructor)
                + Objects.hashCode(this.campus)
                + Objects.hashCode(this.pacingStructure)
                + Objects.hashCode(this.mtgDays)
                + Objects.hashCode(this.classroomId)
                + Objects.hashCode(this.lstStcrsCreatDt)
                + Objects.hashCode(this.gradingStd)
                + Objects.hashCode(this.aMinScore)
                + Objects.hashCode(this.bMinScore)
                + Objects.hashCode(this.cMinScore)
                + Objects.hashCode(this.dMinScore)
                + Objects.hashCode(this.surveyId)
                + Objects.hashCode(this.courseLabelShown)
                + Objects.hashCode(this.displayScore)
                + Objects.hashCode(this.displayGradeScale)
                + Objects.hashCode(this.countInMaxCourses)
                + Objects.hashCode(this.online)
                + Objects.hashCode(this.bogus)
                + Objects.hashCode(this.canvasId)
                + Objects.hashCode(this.subterm);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawCsection rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.sectionId, rec.sectionId)
                    && Objects.equals(this.ariesStartDt, rec.ariesStartDt)
                    && Objects.equals(this.ariesEndDt, rec.ariesEndDt)
                    && Objects.equals(this.startDt, rec.startDt)
                    && Objects.equals(this.examDeleteDt, rec.examDeleteDt)
                    && Objects.equals(this.instrnType, rec.instrnType)
                    && Objects.equals(this.instructor, rec.instructor)
                    && Objects.equals(this.campus, rec.campus)
                    && Objects.equals(this.pacingStructure, rec.pacingStructure)
                    && Objects.equals(this.mtgDays, rec.mtgDays)
                    && Objects.equals(this.classroomId, rec.classroomId)
                    && Objects.equals(this.lstStcrsCreatDt, rec.lstStcrsCreatDt)
                    && Objects.equals(this.gradingStd, rec.gradingStd)
                    && Objects.equals(this.aMinScore, rec.aMinScore)
                    && Objects.equals(this.bMinScore, rec.bMinScore)
                    && Objects.equals(this.cMinScore, rec.cMinScore)
                    && Objects.equals(this.dMinScore, rec.dMinScore)
                    && Objects.equals(this.surveyId, rec.surveyId)
                    && Objects.equals(this.courseLabelShown, rec.courseLabelShown)
                    && Objects.equals(this.displayScore, rec.displayScore)
                    && Objects.equals(this.displayGradeScale, rec.displayGradeScale)
                    && Objects.equals(this.countInMaxCourses, rec.countInMaxCourses)
                    && Objects.equals(this.online, rec.online)
                    && Objects.equals(this.bogus, rec.bogus)
                    && Objects.equals(this.canvasId, rec.canvasId)
                    && Objects.equals(this.subterm, rec.subterm);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Gets the top matter associated with a course and section.
     *
     * @param courseId the course ID
     * @return the top matter (null if none)
     */
    public static String getTopmatter(final String courseId) {

        String topmatter = null;

        if (RawRecordConstants.M125.equals(courseId) || RawRecordConstants.M126.equals(courseId)
                || RawRecordConstants.M1250.equals(courseId)
                || RawRecordConstants.M1260.equals(courseId)) {
            topmatter = "<strong class='red'>REMEMBER: when working with angles, always check the mode setting on "
                    + "your calculator.</strong>";
        }

        return topmatter;
    }

    /**
     * Gets the proctoring options available for a particular course section.
     *
     * @param csection the course section
     * @return the list of proctoring options; null if none
     */
    public static List<EProctoringOption> getProctoringOptions(final RawCsection csection) {

        final String course = csection.course;
        final String sect = csection.sect;

        // FIXME: For now, we base some data on first digit of section number!
        final char sectChar0 = sect == null || sect.isEmpty() ? 0 : sect.charAt(0);

        List<EProctoringOption> proctoringOptions = null;

        final boolean isPrecalcCourse = RawRecordConstants.M117.equals(course) || RawRecordConstants.M118.equals(course)
                || RawRecordConstants.M124.equals(course) || RawRecordConstants.M125.equals(course)
                || RawRecordConstants.M126.equals(course);

        if (sectChar0 == '8' || sectChar0 == '4') {
            if (isPrecalcCourse) {
                proctoringOptions = new ArrayList<>(6);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.DIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.HUMAN);
                proctoringOptions.add(EProctoringOption.HONORLOCK);
            }
        } else if (sectChar0 == '0') {
            if (isPrecalcCourse) {
                proctoringOptions = new ArrayList<>(6);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.DIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.HUMAN);
                proctoringOptions.add(EProctoringOption.RESPONDUS);
            }
        } else if (sectChar0 == '1') {
            if (RawRecordConstants.M100T.equals(course)) {
                // ELM Tutorial
                proctoringOptions = new ArrayList<>(5);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.PROCTOR_U_STUDENT);
                proctoringOptions.add(EProctoringOption.HUMAN);
            } else if (RawRecordConstants.M100P.equals(course)
                    || RawRecordConstants.M1170.equals(course)
                    || RawRecordConstants.M1180.equals(course)
                    || RawRecordConstants.M1240.equals(course)
                    || RawRecordConstants.M1250.equals(course)
                    || RawRecordConstants.M1260.equals(course)) {
                // Placement Tool or Precalculus Tutorial
                proctoringOptions = new ArrayList<>(5);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.PROCTOR_U_STUDENT);
                proctoringOptions.add(EProctoringOption.HUMAN);
            }
        }

        return proctoringOptions;
    }
}
