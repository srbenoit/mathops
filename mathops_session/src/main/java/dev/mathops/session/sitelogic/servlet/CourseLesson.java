package dev.mathops.session.sitelogic.servlet;

import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gathers the information needed to display a course lesson.
 */
public final class CourseLesson extends LogicBase {

    /** TRUE of the course is a tutorial. */
    private Boolean courseIsTutorial;

    /** The course-unit-lesson configuration. */
    private RawCuobjective courseUnitObjective;

    /** The lesson configuration. */
    private RawLesson lesson;

    /** The lesson components. */
    private List<RawLessonComponent> components;

    /**
     * Constructs a new {@code CourseLesson}.
     *
     * @param theDbProfile the database profile under which this site is accessed
     */
    public CourseLesson(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Gathers the data for a particular course.
     *
     * @param systemData the system data object
     * @param courseId   the ID of the course
     * @param unit       the unit number
     * @param objective  the objective of the lesson
     * @return {@code true} if data was gathered successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public boolean gatherData(final SystemData systemData, final String courseId, final Integer unit,
                              final Integer objective) throws SQLException {

        this.courseIsTutorial = null;
        this.courseUnitObjective = null;
        this.lesson = null;
        this.components = null;

        final TermRec active = systemData.getActiveTerm();

        final boolean ok;

        if (active == null) {
            ok = false;
        } else {
            ok = queryCourseUnitObjective(systemData, courseId, unit, objective, active.term)
                    && queryCourseTutorialStatus(systemData, courseId) && queryLesson(systemData);

            if (ok) {
                this.components = systemData.getLessonComponentsByLesson(this.courseUnitObjective.lessonId);
            }
        }

        return ok;
    }

    /**
     * Queries the course unit objective object and stores it in {@code courseUnitObjective}.
     *
     * @param systemData the system data object
     * @param courseId   the course ID
     * @param unit       the unit number
     * @param objective  the objective number
     * @param term       the term key
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryCourseUnitObjective(final SystemData systemData, final String courseId, final Integer unit,
                                             final Integer objective, final TermKey term) throws SQLException {

        this.courseUnitObjective = systemData.getCourseUnitObjective(courseId, unit, objective, term);

        if (this.courseUnitObjective == null) {
            setErrorText("Course " + courseId + " unit " + unit + " objective " + objective + " not found.");
        }

        return this.courseUnitObjective != null;
    }

    /**
     * Queries the course object and stores it in {@code course}.
     *
     * @param systemData the system data object
     * @param courseId   the course ID
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryCourseTutorialStatus(final SystemData systemData, final String courseId) throws SQLException {

        final RawCourse course = systemData.getCourse(courseId);

        this.courseIsTutorial = course == null ? null : Boolean.valueOf("Y".equals(course.isTutorial));

        if (this.courseIsTutorial == null) {
            setErrorText("Course not found.");
        }

        return this.courseIsTutorial != null;
    }

    /**
     * Queries the lesson object and stores it in {@code lesson}.
     *
     * @param systemData the system data object
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryLesson(final SystemData systemData) throws SQLException {

        final boolean ok;

        this.lesson = systemData.getLesson(this.courseUnitObjective.lessonId);

        if (this.lesson == null) {
            setErrorText("Lesson not found.");
            ok = false;
        } else {
            ok = true;
        }

        return ok;
    }

    /**
     * Tests whether the course is a tutorial.
     *
     * @return TRUE if the course was found an is marked as being a tutorial; FALSE if the course was found as is not
     *         marked as a tutorial; null if the course was not found
     */
    public Boolean getCourseIsTutorial() {

        return this.courseIsTutorial;
    }

    /**
     * Gets the lesson configuration.
     *
     * @return a {@code Lesson}
     */
    public RawLesson getLesson() {

        return this.lesson;
    }

    /**
     * Gets the number of lesson components.
     *
     * @return the number of components
     */
    public int getNumComponents() {

        return this.components.size();
    }

    /**
     * Gets a particular lesson component.
     *
     * @param index the index of the component to retrieve
     * @return the {@code LessonComponent}
     */
    public RawLessonComponent getLessonComponent(final int index) {

        return this.components.get(index);
    }

    /**
     * Gets a copy of the list of lesson components
     *
     * @return a copy of the {@code LessonComponent} list
     */
    public List<RawLessonComponent> getLessonComponents() {

        return new ArrayList<>(this.components);
    }
}
