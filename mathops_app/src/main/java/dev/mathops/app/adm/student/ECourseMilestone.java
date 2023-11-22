package dev.mathops.app.adm.student;

/**
 * Course status milestones that can be used when selecting populations.
 */
public enum ECourseMilestone {

    /** Enrolled in the course, but has not yet satisfied prerequisites. */
    NEEDS_PREREQS("Enroll", false),

    /** Enrolled and has prerequisites, has not yet started. */
    HAS_PRERQ("Prereq", false),

    /** Has started the course but not yet finished Skills Review exam. */
    STARTED("Start", false),

    /** Has passed the Skills Review Exam but not the Unit 1 Review Exam. */
    PASSED_SR("SR", false),

    /** Has passed the Unit 1 Review Exam but not the Unit 1 Exam. */
    PASSED_UNIT_1_REVIEW("R1", true),

    /** Has passed the Unit 1 Exam but not the Unit 2 Review Exam. */
    PASSED_UNIT_1_EXAM("U1", false),

    /** Has passed the Unit 2 Review Exam but not the Unit 2 Exam. */
    PASSED_UNIT_2_REVIEW("R2", true),

    /** Has passed the Unit 2 Exam but not the Unit 3 Review Exam. */
    PASSED_UNIT_2_EXAM("U2", false),

    /** Has passed the Unit 3 Review Exam but not the Unit 3 Exam. */
    PASSED_UNIT_3_REVIEW("R3", true),

    /** Has passed the Unit 3 Exam but not the Unit 4 Review Exam. */
    PASSED_UNIT_3_EXAM("U3", false),

    /** Has passed the Unit 4 Review Exam but not the Unit 4 Exam. */
    PASSED_UNIT_4_REVIEW("R4", true),

    /** Has passed the Unit 4 Exam but not the Final Exam. */
    PASSED_UNIT_4_EXAM("U4", false),

    /** Has passed the Final Exam but does not yet have 54 points. */
    PASSED_FINAL_EXAM_NOT_54("Final", true),

    /** Has passed the course with a C grade. */
    PASSED_COURSE_WITH_C("C", false),

    /** Has passed the course with a B grade. */
    PASSED_COURSE_WITH_B("B", false),

    /** Has passed the course with an A grade. */
    PASSED_COURSE_WITH_A("A", false),

    /** Has forfeit the course. */
    FORFEIT("Forfeit", false),

    ;

    /** The label. */
    public final String label;

    /** True if milestone has a due date. */
    public final boolean hasDueDate;

    /**
     * Constructs a new {@code ECourseStatusMilestone}.
     *
     * @param theLabel the label
     * @param dueDate  true if the milestone has a due date
     */
    ECourseMilestone(final String theLabel, final boolean dueDate) {

        this.label = theLabel;
        this.hasDueDate = dueDate;
    }
}
