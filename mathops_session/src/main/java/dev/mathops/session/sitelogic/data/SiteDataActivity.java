package dev.mathops.session.sitelogic.data;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A container for the course activity-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataActivity {

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The student exam records, keyed on course ID, unit, ordered by date/time. */
    private final Map<String, Map<Integer, List<RawStexam>>> studentExams;

    /** The student homework records, keyed on course ID, unit, objective, ordered by date/time. */
    private final Map<String, Map<Integer, Map<Integer, List<RawSthomework>>>> studentHomework;

    /**
     * Constructs a new {@code SiteDataActivity}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataActivity(final SiteData theOwner) {

        this.owner = theOwner;
        this.studentExams = new TreeMap<>();
        this.studentHomework = new TreeMap<>();
    }

    /**
     * Gets all student exams for a particular course.
     *
     * @param courseId the course ID
     * @return the list of exams, ordered by date/time
     */
    public List<RawStexam> getStudentExams(final String courseId) {

        final List<RawStexam> result = new ArrayList<>(100);

        final Map<Integer, List<RawStexam>> map = this.studentExams.get(courseId);

        if (map != null) {
            for (final List<RawStexam> rawStexams : map.values()) {
                result.addAll(rawStexams);
            }
        }

        return result;
    }

    /**
     * Gets all student exams for a particular course and unit.
     *
     * @param courseId the course ID
     * @param unit     the unit
     * @return the list of exams, ordered by date/time
     */
    public List<RawStexam> getStudentExams(final String courseId, final Integer unit) {

        final List<RawStexam> result;

        final Map<Integer, List<RawStexam>> map = this.studentExams.get(courseId);

        if (map == null) {
            result = new ArrayList<>(0);
        } else {
            final List<RawStexam> list = map.get(unit);
            result = Objects.requireNonNullElseGet(list, () -> new ArrayList<>(0));
        }

        return result;
    }

    /**
     * Gets all student submitted homework assignments for a particular course, unit, and objective.
     *
     * @param courseId  the course ID
     * @param unit      the unit
     * @param objective the objective
     * @return the list of submitted homework, ordered by date/time
     */
    List<RawSthomework> getStudentHomework(final String courseId, final Integer unit, final Integer objective) {

        final List<RawSthomework> result;

        final Map<Integer, Map<Integer, List<RawSthomework>>> map1 = this.studentHomework.get(courseId);

        if (map1 == null) {
            result = new ArrayList<>(0);
        } else {
            final Map<Integer, List<RawSthomework>> map2 = map1.get(unit);

            if (map2 == null) {
                result = new ArrayList<>(0);
            } else {
                final List<RawSthomework> list = map2.get(objective);
                result = Objects.requireNonNullElseGet(list, () -> new ArrayList<>(0));
            }
        }

        return result;
    }

//    /**
//     * Searches for the earliest passing exam date for a specified course, unit, and exam type.
//     *
//     * @param course   the course
//     * @param unit     the unit
//     * @param examType the exam type
//     * @return the earliest date of a passed exam; null if none passed
//     */
//    public LocalDate findEarliestPassingExam(final String course, final Integer unit, final String examType) {
//
//        LocalDate result = null;
//
//        final Map<Integer, List<RawStexam>> courseExams = this.studentExams.get(course);
//        if (courseExams != null) {
//            final List<RawStexam> unitExams = courseExams.get(unit);
//
//            // Exams are sorted by date/time, so we can stop as soon as we find one.
//            for (final RawStexam stexam : unitExams) {
//                if ("Y".equals(stexam.passed) && stexam.examType.equals(examType)) {
//                    result = stexam.examDt;
//                    break;
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Searches for the earliest attempted exam date for a specified course, unit, and exam type.
//     *
//     * @param course   the course
//     * @param unit     the unit
//     * @param examType the exam type
//     * @return the earliest date of a passed exam; null if none passed
//     */
//    public LocalDate findEarliestAttempt(final String course, final Integer unit,
//                                         final String examType) {
//
//        LocalDate result = null;
//
//        final Map<Integer, List<RawStexam>> courseExams = this.studentExams.get(course);
//        if (courseExams != null) {
//            final List<RawStexam> unitExams = courseExams.get(unit);
//
//            // Exams are sorted by date/time, so we can stop as soon as we find one.
//            for (final RawStexam stexam : unitExams) {
//                if (stexam.examType.equals(examType)) {
//                    result = stexam.examDt;
//                    break;
//                }
//            }
//        }
//
//        return result;
//    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, all pace track rules, the {@code SiteDataContext} object, the {@code SiteDataStudent} object, the
     * {@code SuteDataProfile} object, and the {@code SiteDateRegistration} object, and the {@code SiteDateCourse}
     * object will have been populated in the process.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final Cache cache) throws SQLException {

        // We assume that only current, relevant exam/homework/lesson date is retained, so there
        // is no harm in simply querying all of it!
        return loadExams(cache) && loadHomework(cache);
    }

    /**
     * Loads and organizes all submitted exams for the student.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadExams(final Cache cache) throws SQLException {

        final boolean success = true;

        final List<RawStexam> allExams =
                RawStexamLogic.queryByStudent(cache, this.owner.studentData.getStudent().stuId, true);

        for (final RawStexam exam : allExams) {
            final Map<Integer, List<RawStexam>> map = this.studentExams.computeIfAbsent(exam.course,
                    s -> new TreeMap<>());
            final List<RawStexam> list = map.computeIfAbsent(exam.unit, k -> new LinkedList<>());
            list.add(exam);
        }

        return success;
    }

    /**
     * Loads and organizes all submitted homework for the student.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadHomework(final Cache cache) throws SQLException {

        final boolean success = true;

        final List<RawSthomework> allHw = RawSthomeworkLogic.queryByStudent(cache,
                this.owner.studentData.getStudent().stuId, false);

        for (final RawSthomework hw : allHw) {
            final String courseId = hw.course;
            final Integer unit = hw.unit;
            final Integer obj = hw.objective;

            final Map<Integer, Map<Integer, List<RawSthomework>>> map1 =
                    this.studentHomework.computeIfAbsent(courseId, s -> new TreeMap<>());
            final Map<Integer, List<RawSthomework>> map2 = map1.computeIfAbsent(unit, k -> new TreeMap<>());
            final List<RawSthomework> list = map2.computeIfAbsent(obj, k -> new LinkedList<>());
            list.add(hw);
        }

        return success;
    }
}
