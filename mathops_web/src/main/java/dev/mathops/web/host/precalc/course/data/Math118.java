package dev.mathops.web.host.precalc.course.data;

import dev.mathops.db.old.rawrecord.RawRecordConstants;

/**
 * Static {@code CourseData} objects for the Math 118 course.
 */
public enum Math118 {
    ;

    /** The MATH 118 course. */
    public static final CourseData MATH_118;

    static {
        MATH_118 = buildMath118();
    }

    /**
     * Creates the MATH 118 course.
     *
     * @return the course data container
     */
    private static CourseData buildMath118() {

        final CourseData m118 = new CourseData(RawRecordConstants.MATH118, RawRecordConstants.MATH118,
                "College Algebra in Context II", "M118");

        // Module 1

        final ModuleData m118m1 = m118.addModule(1, "xxx", "xxx", "c31-thumb.png");
        m118m1.skillsReview.addExBlock("xxx")
                .addEx("SR31_01_01",
                        "xxx");

        final LearningTargetData m118m1t1 = m118m1.addLearningTarget(1, 1, "1.1",
                "xxx",
                "I can xxx",
                "xxx");

        m118m1t1.addExBlock("xxx")
                .addEx("ST31_1_F01_01",
                        "xxx");

        final LearningTargetData m118m1t2 = m118m1.addLearningTarget(1, 2, "1.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m1t2.addExBlock("xxx")
                .addEx("ST31_2_F01_01",
                        "xxx");

        final LearningTargetData m118m1t3 = m118m1.addLearningTarget(1, 3, "1.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m1t3.addExBlock("xxx")
                .addEx("ST31_3_F01_01",
                        "xxx");

        // Module 2

        final ModuleData m118m2 = m118.addModule(2, "xxx", "xxx", "c32-thumb.png");

        m118m2.skillsReview.addExBlock("xxx")
                .addEx("SR32_01_01",
                        "xxx");

        final LearningTargetData m118m2t1 = m118m2.addLearningTarget(2, 1, "2.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m2t1.addExBlock("xxx")
                .addEx("ST32_1_F01_01",
                        "xxx");

        final LearningTargetData m118m2t2 = m118m2.addLearningTarget(2, 2, "2.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m2t2.addExBlock("xxx")
                .addEx("ST32_2_F01_01",
                        "xxx");

        final LearningTargetData m118m2t3 = m118m2.addLearningTarget(2, 3, "2.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m2t3.addExBlock("xxx")
                .addEx("ST32_3_F01_01",
                        "xxx");

        // Module 3

        final ModuleData m118m3 = m118.addModule(3, "xxx", "xxx", "c33-thumb.png");

        m118m3.skillsReview.addExBlock("xxx")
                .addEx("SR33_01_01",
                        "xxx");

        final LearningTargetData m118m3t1 = m118m3.addLearningTarget(3, 1, "3.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m3t1.addExBlock("xxx")
                .addEx("ST33_1_F01_01",
                        "xxx");

        final LearningTargetData m118m3t2 = m118m3.addLearningTarget(3, 2, "3.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m3t2.addExBlock("xxx")
                .addEx("ST33_2_F01_01",
                        "xxx");

        final LearningTargetData m118m3t3 = m118m3.addLearningTarget(3, 3, "3.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m3t3.addExBlock("xxx")
                .addEx("ST33_3_F01_01",
                        "xxx");

        // Module 4

        final ModuleData m118m4 = m118.addModule(4, "xxx", "xxx", "c34-thumb.png");

        m118m4.skillsReview.addExBlock("xxx")
                .addEx("SR34_01_01",
                        "xxx");

        final LearningTargetData m118m4t1 = m118m4.addLearningTarget(4, 1, "4.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m4t1.addExBlock("xxx")
                .addEx("ST34_1_F01_01",
                        "xxx");

        final LearningTargetData m118m4t2 = m118m4.addLearningTarget(4, 2, "4.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m4t2.addExBlock("xxx")
                .addEx("ST34_2_F01_01",
                        "xxx");

        final LearningTargetData m118m4t3 = m118m4.addLearningTarget(4, 3, "4.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m4t3.addExBlock("xxx")
                .addEx("ST34_3_F01_01",
                        "xxx");

        // Module 5

        final ModuleData m118m5 = m118.addModule(5, "xxx", "xxx", "c35-thumb.png");

        m118m5.skillsReview.addExBlock("xxx")
                .addEx("SR35_01_01",
                        "xxx");

        final LearningTargetData m118m5t1 = m118m5.addLearningTarget(5, 1, "5.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m5t1.addExBlock("xxx")
                .addEx("ST35_1_F01_01",
                        "xxx");

        final LearningTargetData m118m5t2 = m118m5.addLearningTarget(5, 2, "5.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m5t2.addExBlock("xxx")
                .addEx("ST35_2_F01_01",
                        "xxx");

        final LearningTargetData m118m5t3 = m118m5.addLearningTarget(5, 3, "5.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m5t3.addExBlock("xxx")
                .addEx("ST35_3_F01_01",
                        "xxx");

        // Module 6

        final ModuleData m118m6 = m118.addModule(6, "xxx", "xxx", "c36-thumb.png");

        m118m6.skillsReview.addExBlock("xxx")
                .addEx("SR36_01_01",
                        "xxx");

        final LearningTargetData m118m6t1 = m118m6.addLearningTarget(6, 1, "6.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m6t1.addExBlock("xxx")
                .addEx("ST36_1_F01_01",
                        "xxx");

        final LearningTargetData m118m6t2 = m118m6.addLearningTarget(6, 2, "6.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m6t2.addExBlock("xxx")
                .addEx("ST36_2_F01_01",
                        "xxx");

        final LearningTargetData m118m6t3 = m118m6.addLearningTarget(6, 3, "6.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m6t3.addExBlock("xxx")
                .addEx("ST36_3_F01_01",
                        "xxx");

        // Module 7

        final ModuleData m118m7 = m118.addModule(7, "xxx", "xxx", "c37-thumb.png");

        m118m7.skillsReview.addExBlock("xxx")
                .addEx("SR37_01_01",
                        "xxx");

        final LearningTargetData m118m7t1 = m118m7.addLearningTarget(7, 1, "7.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m7t1.addExBlock("xxx")
                .addEx("ST37_1_F01_01",
                        "xxx");

        final LearningTargetData m118m7t2 = m118m7.addLearningTarget(7, 2, "7.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m7t2.addExBlock("xxx")
                .addEx("ST37_2_F01_01",
                        "xxx");

        final LearningTargetData m118m7t3 = m118m7.addLearningTarget(7, 3, "7.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m7t3.addExBlock("xxx")
                .addEx("ST37_3_F01_01",
                        "xxx");

        // Module 8

        final ModuleData m118m8 = m118.addModule(8, "xxx", "xxx", "c38-thumb.png");

        m118m8.skillsReview.addExBlock("xxx")
                .addEx("SR38_01_01",
                        "xxx");

        final LearningTargetData m118m8t1 = m118m8.addLearningTarget(8, 1, "8.1",
                "xxx",
                "I can xxx",
                "xxx");
        m118m8t1.addExBlock("xxx")
                .addEx("ST38_1_F01_01",
                        "xxx");

        final LearningTargetData m118m8t2 = m118m8.addLearningTarget(8, 2, "8.2",
                "xxx",
                "I can xxx",
                "xxx");
        m118m8t2.addExBlock("xxx")
                .addEx("ST38_2_F01_01",
                        "xxx");

        final LearningTargetData m118m8t3 = m118m8.addLearningTarget(8, 3, "8.3",
                "xxx",
                "I can xxx",
                "xxx");
        m118m8t3.addExBlock("xxx")
                .addEx("ST38_3_F01_01",
                        "xxx");

        return m118;
    }
}
