package dev.mathops.web.site.course.data;

import dev.mathops.db.old.rawrecord.RawRecordConstants;

/**
 * Static {@code CourseData} objects for the Math courses.
 */
public enum MathCourses {
    ;

    /** The MATH 125 course. */
    public static final CourseData MATH_125;

    /** The MATH 126 course. */
    public static final CourseData MATH_126;

    static {
        // TODO: Before populating MATH 124, make a copy of the blank structure for 118
        // MATH_124 = buildMath124();

        MATH_125 = buildMath125();
        MATH_126 = buildMath126();
    }

    ///**
    // * Creates the MATH 124 course.
    // *
    // * @return the course data container
    // */
    // private static CourseData buildMath124() {
    //
    // final CourseData m124 = new CourseData("MATH 124",
    // "Logarithmic and Exponential Functions", "m124");
    //
    // // Module 1
    //
    // final ModuleData m124m1 = m124.addModule(1, "xxx", 1, "24M01",
    // "c31-thumb.png");
    // m124m1.skillsReview.addExampleBlock("xxx")
    // .addExample("SR31_01_01",
    // "xxx");
    // m124m1.skillsReview.addExampleBlock("xxx")
    // .addExample("SR31_02_01",
    // "xxx");
    // m124m1.skillsReview.addExampleBlock("xxx")
    // .addExample("SR31_03_01",
    // "xxx");
    // m124m1.skillsReview.addExampleBlock("xxx")
    // .addExample("SR31_04_01",
    // "xxx");
    // m124m1.skillsReview.addExampleBlock("xxx")
    // .addExample("SR31_05_01",
    // "xxx");
    //
    // final LearningTargetData m124m1t1 = m124m1.addLearningTarget(2,"1.1",
    // "24M02",
    // "I can xxx",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F01_01",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F02_01",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F03_01",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F04_01",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F05_01",
    // "xxx");
    // m124m1t1.addExampleBlock("xxx")
    // .addExample("ST31_1_F06_01",
    // "xxx");
    //
    // final LearningTargetData m124m1t2 = m124m1.addLearningTarget(3,"1.2",
    // "24M03",
    // "I can xxx",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F01_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F02_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F03_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F04_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F05_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F06_01",
    // "xxx");
    // m124m1t2.addExampleBlock("xxx")
    // .addExample("ST31_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m1t3 = m124m1.addLearningTarget(4,"1.3",
    // "24M04",
    // "I can xxx",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F01_01",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F02_01",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F03_01",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F04_01",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F05_01",
    // "xxx");
    // m124m1t3.addExampleBlock("xxx")
    // .addExample("ST31_3_F06_01",
    // "xxx");
    //
    // // Module 2
    //
    // final ModuleData m124m2 = m124.addModule(2, "xxx", 5, "24M05",
    // "c32-thumb.png");
    //
    // m124m2.skillsReview.addExampleBlock("xxx")
    // .addExample("SR32_01_01",
    // "xxx");
    // m124m2.skillsReview.addExampleBlock("xxx")
    // .addExample("SR32_02_01",
    // "xxx");
    // m124m2.skillsReview.addExampleBlock("xxx")
    // .addExample("SR32_03_01",
    // "xxx");
    // m124m2.skillsReview.addExampleBlock("xxx")
    // .addExample("SR32_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m2t1 = m124m2.addLearningTarget(6,"2.1",
    // "24M06",
    // "xxx, including:", //
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F01_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F02_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F03_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F04_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F05_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F06_01",
    // "xxx");
    // m124m2t1.addExampleBlock("xxx")
    // .addExample("ST32_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m2t2 = m124m2.addLearningTarget(7,"2.2",
    // "24M07",
    // "xxx, including:",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F01_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F02_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F03_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F04_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F05_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F06_01",
    // "xxx");
    // m124m2t2.addExampleBlock("xxx")
    // .addExample("ST32_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m2t3 = m124m2.addLearningTarget(8,"2.3",
    // "24M08",
    // "xxx, including:",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F01_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F02_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F03_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F04_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F05_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F06_01",
    // "xxx");
    // m124m2t3.addExampleBlock("xxx")
    // .addExample("ST32_3_F07_01",
    // "xxx");
    //
    // // Module 3
    //
    // final ModuleData m124m3 = m124.addModule(3, "xxx", 9, "24M09",
    // "c33-thumb.png");
    //
    // m124m3.skillsReview.addExampleBlock("xxx")
    // .addExample("SR33_01_01",
    // "xxx");
    // m124m3.skillsReview.addExampleBlock("xxx")
    // .addExample("SR33_02_01",
    // "xxx");
    // m124m3.skillsReview.addExampleBlock("xxx")
    // .addExample("SR33_03_01",
    // "xxx");
    // m124m3.skillsReview.addExampleBlock("xxx")
    // .addExample("SR33_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m3t1 = m124m3.addLearningTarget(10,"3.1",
    // "24M10",
    // "xxx, including:",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F01_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F02_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F03_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F04_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F05_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F06_01",
    // "xxx");
    // m124m3t1.addExampleBlock("xxx")
    // .addExample("ST33_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m3t2 = m124m3.addLearningTarget(11,"3.2",
    // "24M11",
    // "xxx, including:",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F01_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F02_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F03_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F04_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F05_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F06_01",
    // "xxx");
    // m124m3t2.addExampleBlock("xxx")
    // .addExample("ST33_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m3t3 = m124m3.addLearningTarget(12,"3.3",
    // "24M12",
    // "xxx, including:",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F01_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F02_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F03_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F04_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F05_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F06_01",
    // "xxx");
    // m124m3t3.addExampleBlock("xxx")
    // .addExample("ST33_3_F07_01",
    // "xxx");
    //
    // // Module 4
    //
    // final ModuleData m124m4 = m124.addModule(4, "xxx", 13, "24M13",
    // "c34-thumb.png");
    //
    // m124m4.skillsReview.addExampleBlock("xxx")
    // .addExample("SR34_01_01",
    // "xxx");
    // m124m4.skillsReview.addExampleBlock("xxx")
    // .addExample("SR34_02_01",
    // "xxx");
    // m124m4.skillsReview.addExampleBlock("xxx")
    // .addExample("SR34_03_01",
    // "xxx");
    // m124m4.skillsReview.addExampleBlock("xxx")
    // .addExample("SR34_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m4t1 = m124m4.addLearningTarget(14,"4.1",
    // "24M14",
    // "xxx, including:",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F01_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F02_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F03_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F04_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F05_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F06_01",
    // "xxx");
    // m124m4t1.addExampleBlock("xxx")
    // .addExample("ST34_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m4t2 = m124m4.addLearningTarget(15,"4.2",
    // "24M15",
    // "xxx, including:",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F01_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F02_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F03_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F04_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F05_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F06_01",
    // "xxx");
    // m124m4t2.addExampleBlock("xxx")
    // .addExample("ST34_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m4t3 = m124m4.addLearningTarget(16,"4.3",
    // "24M16",
    // "xxx, including:",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F01_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F02_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F03_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F04_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F05_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F06_01",
    // "xxx");
    // m124m4t3.addExampleBlock("xxx")
    // .addExample("ST34_3_F07_01",
    // "xxx");
    //
    // // Module 5
    //
    // final ModuleData m124m5 = m124.addModule(5, "xxx", 17, "24M17",
    // "c35-thumb.png");
    //
    // m124m5.skillsReview.addExampleBlock("xxx")
    // .addExample("SR35_01_01",
    // "xxx");
    // m124m5.skillsReview.addExampleBlock("xxx")
    // .addExample("SR35_02_01",
    // "xxx");
    // m124m5.skillsReview.addExampleBlock("xxx")
    // .addExample("SR35_03_01",
    // "xxx");
    // m124m5.skillsReview.addExampleBlock("xxx")
    // .addExample("SR35_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m5t1 = m124m5.addLearningTarget(18,"5.1",
    // "24M18",
    // "xxx, including:",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F01_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F02_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F03_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F04_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F05_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F06_01",
    // "xxx");
    // m124m5t1.addExampleBlock("xxx")
    // .addExample("ST35_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m5t2 = m124m5.addLearningTarget(19,"5.2",
    // "24M19",
    // "xxx, including:",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F01_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F02_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F03_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F04_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F05_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F06_01",
    // "xxx");
    // m124m5t2.addExampleBlock("xxx")
    // .addExample("ST35_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m5t3 = m124m5.addLearningTarget(20,"5.3",
    // "24M20",
    // "xxx, including:",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F01_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F02_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F03_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F04_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F05_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F06_01",
    // "xxx");
    // m124m5t3.addExampleBlock("xxx")
    // .addExample("ST35_3_F07_01",
    // "xxx");
    //
    // // Module 6
    //
    // final ModuleData m124m6 = m124.addModule(6, "xxx", 21, "24M21",
    // "c36-thumb.png");
    //
    // m124m6.skillsReview.addExampleBlock("xxx")
    // .addExample("SR36_01_01",
    // "xxx");
    // m124m6.skillsReview.addExampleBlock("xxx")
    // .addExample("SR36_02_01",
    // "xxx");
    // m124m6.skillsReview.addExampleBlock("xxx")
    // .addExample("SR36_03_01",
    // "xxx");
    // m124m6.skillsReview.addExampleBlock("xxx")
    // .addExample("SR36_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m6t1 = m124m6.addLearningTarget(22,"6.1",
    // "24M22",
    // "xxx, including:",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F01_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F02_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F03_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F04_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F05_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F06_01",
    // "xxx");
    // m124m6t1.addExampleBlock("xxx")
    // .addExample("ST36_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m6t2 = m124m6.addLearningTarget(23,"6.2",
    // "24M23",
    // "xxx, including:",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F01_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F02_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F03_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F04_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F05_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F06_01",
    // "xxx");
    // m124m6t2.addExampleBlock("xxx")
    // .addExample("ST36_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m6t3 = m124m6.addLearningTarget(24,"6.3",
    // "24M24",
    // "xxx, including:",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F01_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F02_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F03_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F04_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F05_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F06_01",
    // "xxx");
    // m124m6t3.addExampleBlock("xxx")
    // .addExample("ST36_3_F07_01",
    // "xxx");
    //
    // // Module 7
    //
    // final ModuleData m124m7 = m124.addModule(7, "xxx", 25, "24M25",
    // "c37-thumb.png");
    //
    // m124m7.skillsReview.addExampleBlock("xxx")
    // .addExample("SR37_01_01",
    // "xxx");
    // m124m7.skillsReview.addExampleBlock("xxx")
    // .addExample("SR37_02_01",
    // "xxx");
    // m124m7.skillsReview.addExampleBlock("xxx")
    // .addExample("SR37_03_01",
    // "xxx");
    // m124m7.skillsReview.addExampleBlock("xxx")
    // .addExample("SR37_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m7t1 = m124m7.addLearningTarget(26,"7.1",
    // "24M26",
    // "xxx, including:",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F01_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F02_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F03_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F04_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F05_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F06_01",
    // "xxx");
    // m124m7t1.addExampleBlock("xxx")
    // .addExample("ST37_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m7t2 = m124m7.addLearningTarget(27,"7.2",
    // "24M27",
    // "xxx, including:",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F01_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F02_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F03_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F04_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F05_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F06_01",
    // "xxx");
    // m124m7t2.addExampleBlock("xxx")
    // .addExample("ST37_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m7t3 = m124m7.addLearningTarget(28,"7.3",
    // "24M28",
    // "xxx, including:",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F01_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F02_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F03_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F04_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F05_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F06_01",
    // "xxx");
    // m124m7t3.addExampleBlock("xxx")
    // .addExample("ST37_3_F07_01",
    // "xxx");
    //
    // // Module 8
    //
    // final ModuleData m124m8 = m124.addModule(8, "xxx", 29, "24M29",
    // "c38-thumb.png");
    //
    // m124m8.skillsReview.addExampleBlock("xxx")
    // .addExample("SR38_01_01",
    // "xxx");
    // m124m8.skillsReview.addExampleBlock("xxx")
    // .addExample("SR38_02_01",
    // "xxx");
    // m124m8.skillsReview.addExampleBlock("xxx")
    // .addExample("SR38_03_01",
    // "xxx");
    // m124m8.skillsReview.addExampleBlock("xxx")
    // .addExample("SR38_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m8t1 = m124m8.addLearningTarget(30,"8.1",
    // "24M30",
    // "xxx, including:",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F01_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F02_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F03_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F04_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F05_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F06_01",
    // "xxx");
    // m124m8t1.addExampleBlock("xxx")
    // .addExample("ST38_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m8t2 = m124m8.addLearningTarget(31,"8.2",
    // "24M31",
    // "xxx, including:",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F01_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F02_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F03_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F04_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F05_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F06_01",
    // "xxx");
    // m124m8t2.addExampleBlock("xxx")
    // .addExample("ST38_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m8t3 = m124m8.addLearningTarget(32,"8.3",
    // "24M32",
    // "xxx, including:",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F01_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F02_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F03_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F04_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F05_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F06_01",
    // "xxx");
    // m124m8t3.addExampleBlock("xxx")
    // .addExample("ST38_3_F07_01",
    // "xxx");
    //
    // // Module 9
    //
    // final ModuleData m124m9 = m124.addModule(9, "xxx", 33, "24M33",
    // "c39-thumb.png");
    //
    // m124m9.skillsReview.addExampleBlock("xxx")
    // .addExample("SR39_01_01",
    // "xxx");
    // m124m9.skillsReview.addExampleBlock("xxx")
    // .addExample("SR39_02_01",
    // "xxx");
    // m124m9.skillsReview.addExampleBlock("xxx")
    // .addExample("SR39_03_01",
    // "xxx");
    // m124m9.skillsReview.addExampleBlock("xxx")
    // .addExample("SR39_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m9t1 = m124m9.addLearningTarget(34,"9.1",
    // "24M34",
    // "xxx, including:",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F01_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F02_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F03_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F04_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F05_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F06_01",
    // "xxx");
    // m124m9t1.addExampleBlock("xxx")
    // .addExample("ST39_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m9t2 = m124m9.addLearningTarget(35,"9.2",
    // "24M35",
    // "xxx, including:",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F01_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F02_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F03_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F04_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F05_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F06_01",
    // "xxx");
    // m124m9t2.addExampleBlock("xxx")
    // .addExample("ST39_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m9t3 = m124m9.addLearningTarget(36,"9.3",
    // "24M36",
    // "xxx, including:",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F01_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F02_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F03_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F04_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F05_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F06_01",
    // "xxx");
    // m124m9t3.addExampleBlock("xxx")
    // .addExample("ST39_3_F07_01",
    // "xxx");
    //
    // // Module 10
    //
    // final ModuleData m124m10 = m124.addModule(10, "xxx", 37, "24M37",
    // "c40-thumb.png");
    //
    // m124m10.skillsReview.addExampleBlock("xxx")
    // .addExample("SR40_01_01",
    // "xxx");
    // m124m10.skillsReview.addExampleBlock("xxx")
    // .addExample("SR40_02_01",
    // "xxx");
    // m124m10.skillsReview.addExampleBlock("xxx")
    // .addExample("SR40_03_01",
    // "xxx");
    // m124m10.skillsReview.addExampleBlock("xxx")
    // .addExample("SR40_04_01",
    // "xxx");
    //
    // final LearningTargetData m124m10t1 = m124m10.addLearningTarget(38,"10.1",
    // "24M38",
    // "xxx, including:",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F01_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F02_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F03_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F04_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F05_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F06_01",
    // "xxx");
    // m124m10t1.addExampleBlock("xxx")
    // .addExample("ST40_1_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m10t2 = m124m10.addLearningTarget(39,"10.2",
    // "24M39",
    // "xxx, including:",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F01_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F02_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F03_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F04_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F05_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F06_01",
    // "xxx");
    // m124m10t2.addExampleBlock("xxx")
    // .addExample("ST40_2_F07_01",
    // "xxx");
    //
    // final LearningTargetData m124m10t3 = m124m10.addLearningTarget(40,"10.3",
    // "24M40",
    // "xxx, including:",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F01_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F02_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F03_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F04_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F05_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F06_01",
    // "xxx");
    // m124m10t3.addExampleBlock("xxx")
    // .addExample("ST40_3_F07_01",
    // "xxx");
    //
    // return m124;
    // }

    /**
     * Creates the MATH 125 course.
     *
     * @return the course data container
     */
    private static CourseData buildMath125() {

        final CourseData m125 = new CourseData(RawRecordConstants.MATH125, "MATH 125", "Numerical Trigonometry",
                "M125");

        // Module 1

        final ModuleData m125m1 = m125.addModule(1, "Angle Measure and Right Triangles",
                "TR01_SR_HW", "c41-thumb.png");
        m125m1.skillsReview.addExampleBlock("Unit Conversions")
                .addEx("TR01_SR1_01", "Multi-step Unit Conversion");
        m125m1.skillsReview.addExampleBlock("Addition and Subtraction with Fractions")
                .addEx("TR01_SR2_01", "Addition of fractions")
                .addEx("TR01_SR2_02", "Addition of fractions including variables")
                .addEx("TR01_SR2_03", "Subtraction of fractions including variables");
        m125m1.skillsReview.addExampleBlock("Multiplication and Division with Fractions")
                .addEx("TR01_SR3_01", "Multiplication of fractions")
                .addEx("TR01_SR3_02", "Division of fractions");
        m125m1.skillsReview.addExampleBlock("Evaluating and Manipulating Square Roots")
                .addEx("TR01_SR4_01", "Simplifying a rational expression")
                .addEx("TR01_SR4_02", "Evaluating a rational expression with variable");
        m125m1.skillsReview.addExampleBlock("Exponents and Distribution")
                .addEx("TR01_SR5_01", "Properties of arithmetic with expressions")
                .addEx("TR01_SR5_02", "Cubing a binomial");

        final LearningTargetData m125m1t1 = m125m1.addLearningTarget(1, 1, "1.1",
                "TR01_ST1_HW",
                "I can classify and work with angles.  Specifically, I can:",
                "interpret degree or radian measure as a quantity of rotation",
                "convert between degree and radian units of measure",
                "recognize congruent angles",
                "recognize pairs of complementary and supplementary angles",
                "compute the complement or supplement of an angle in either unit of measure",
                "recognize sets of angles that sum to a straight angle, and that their measures sum to half a turn");

        m125m1t1.addExBlock("Interpret degree measure as rotation")
                .addEx("TR01_ST1A_01", "Determine how many degrees one line must rotate to fall on another");
        m125m1t1.addExBlock("Interpret radian measure as rotation")
                .addEx("TR01_ST1A_02", "Determine how many radians one line must rotate to fall on another");
        m125m1t1.addExBlock("Interpret angles in terms of portions of full turns")
                .addEx("TR01_ST1A_03", "Determine how many copies of an angle make up one full turn");
        m125m1t1.addExBlock("Unit conversion: degrees to radians")
                .addEx("TR01_ST1C_01", "Convert from degree to radian measure");
        m125m1t1.addExBlock("Unit conversion: radians to degrees")
                .addEx("TR01_ST1C_02", "Convert from radian to degree measure");
        m125m1t1.addExBlock("Recognizing congruent angles")
                .addEx("TR01_ST1D_01", "Identify congruent angles in a diagram");
        m125m1t1.addExBlock("Recognizing complementary and supplementary angles")
                .addEx("TR01_ST1E_01", "Identify pairs of  complementary and supplementary angles in a diagram");
        m125m1t1.addExBlock("Computing complement or supplement of angles")
                .addEx("TR01_ST1F_01", "Calculate complement and supplement in both degrees and radians");

        final LearningTargetData m125m1t2 = m125m1.addLearningTarget(1, 2, "1.2", "TR01_ST2_HW",
                "I can classify and work with triangles.  Specifically, I can:",
                "calculate the measure of any angle in a general triangle given the measure of the other two angles",
                "recognize congruent and similar triangles",
                "use proportion to calculate side lengths in similar triangles",
                "use the Pythagorean theorem to calculate the length of an edge of a right triangle");
        m125m1t2.addExBlock("Calculate a missing angle in a triangle in degrees")
                .addEx("TR01_ST2A_01", "Given two interior degree angles in a triangle, find the third");
        m125m1t2.addExBlock("Calculate a missing angle in a triangle in radians")
                .addEx("TR01_ST2A_02", "Given two interior radian angles in a triangle, find the third");
        m125m1t2.addExBlock("Recognize similar triangles")
                .addEx("TR01_ST2B_01", "Given a drawing containing several triangles, find all that are similar");
        m125m1t2.addExBlock("Use proportion in similar triangles to calculate side lengths")
                .addEx("TR01_ST2C_01",
                        "Given two similar triangles, and a few side lengths, find remaining side lengths");
//        m125m1t2.addExBlock("Divide shapes into right triangles")
//                .addEx("TR01_ST2E_01", "Given several shapes, add lines to divide each into right triangles.");
        m125m1t2.addExBlock("Calculate side lengths in right triangles using the Pythagorean theorem")
                .addEx("TR01_ST2F_01a", "Find a missing side length in a right triangle");
        m125m1t2.addExBlock("Find the length of the diagonal of a rectangle")
                .addEx("TR01_ST2G_01",
                        "Given a rectangle whose side lengths are given, find the length of its diagonal");

        final LearningTargetData m125m1t3 = m125m1.addLearningTarget(1, 3, "1.3", "TR01_ST3_HW",
                "I can work with angles in standard position in the plane.  Specifically, I can:",
                "locate the terminal ray of an angle with positive or negative angle in standard position",
                "identify the quadrant in which an angle's terminal ray lies",
                "identify equivalent/co-terminal angles in either degrees or radians",
                "find an angle equivalent/co-terminal to a given angle that lies in a specified range",
                "find the reference angle for a given angle in either degrees or radians");

        m125m1t3.addExBlock("Locate/sketch angles in standard position and identify quadrants")
                .addEx("TR01_ST3A_01", "Locate terminal rays of angles in any quadrant");
        m125m1t3.addExBlock("Identify co-terminal angles in degrees")
                .addEx("TR01_ST3C_01", "Identify pairs of co-terminal angles");
        m125m1t3.addExBlock("Identify co-terminal angles in radians")
                .addEx("TR01_ST3C_02", "Identify pairs of co-terminal angles");
        m125m1t3.addExBlock("Find a co-terminal angle in a specified degree range")
                .addEx("TR01_ST3D_01", "Find angles co-terminal to a given angle in range 0&deg; - 360&deg;");
        m125m1t3.addExBlock("Find a co-terminal angle in a specified radian range")
                .addEx("TR01_ST3D_02", "Find angles co-terminal to a given angle in range 0 - 2&pi;");
        m125m1t3.addExBlock("Finding reference angles in degrees")
                .addEx("TR01_ST3E_01", "Find the reference angle of an angle in in degrees each quadrant");
        m125m1t3.addExBlock("Finding reference angles in radians")
                .addEx("TR01_ST3E_02", "Find the reference angle of an angle in radians in each quadrant");
//        m125m1t3.addExBlock("Estimating angles")
//                .addEx("TR01_ST3F_01", "Estimate the measure of an angle");

        // Module 2

        final ModuleData m125m2 = m125.addModule(2, "The Unit Circle", "SR42_HW", "c42-thumb.png");

        m125m2.skillsReview.addExampleBlock("Percentages")
                .addEx("SR42_01_01", "Pay cut and pay raise")
                .addEx("SR42_01_03", "Auto depreciation")
                .addEx("SR42_01_04", "Home value growth")
                .addEx("SR42_01_05", "Radioactive decay")
                .addEx("SR42_01_06", "Enrollment growth");
        m125m2.skillsReview.addExampleBlock("Circle area and perimeter")
                .addEx("SR42_02_01", "Calculating circumference, radius, and area");
        m125m2.skillsReview.addExampleBlock("Square roots")
                .addEx("SR42_03_01", "Solve with squares and square roots, when to include +/-");
        m125m2.skillsReview.addExampleBlock("Angles that sum to 180&deg;")
                .addEx("SR42_04_01", "Find angles, in degrees, in a drawing with intersecting lines")
                .addEx("SR42_04_02", "Find angles, in radians, in a drawing with intersecting lines");

        final LearningTargetData m125m2t1 = m125m2.addLearningTarget(2, 1, "2.1", "ST42_1_HW",
                "I can interpret angles in terms of arc length, including:",
                "interpret radian measure as arc length along the unit circle",
                "calculate the length of the arc that a given angle subtends on a circle of any radius",
                "calculate the angle subtended by an arc of any length along a circle of any radius",
                "calculate the radius of an arc of a specified length that subtends a specified angle");
        m125m2t1.addExBlock("Interpret radian measure in terms of arc length along the perimeter of a unit circle")
                .addEx("ST42_1_F01_01", "Relate angle to a fraction of circle's perimeter, then to arc length");
        m125m2t1.addExBlock("Calculate length of arc that subtends a given angle on a circle of any radius")
                .addEx("ST42_1_F02_01", "Find arc length given degree measure and radius")
                .addEx("ST42_1_F02_02", "Find arc length given radian measure and radius");
        m125m2t1.addExBlock("Use radius and arc length subtended by an angle to calculate the angle")
                .addEx("ST42_1_F03_01", "Given a radius and arc length, calculate the angle subtended");
        m125m2t1.addExBlock("Use arc length and angle subtended to calculate the radius")
                .addEx("ST42_1_F04_01", "Given arc length and angle subtended, calculate the radius");
        m125m2t1.addExBlock("Applications of finding arc length given angle and radius")
                .addEx("ST42_1_F05_01", "Find distance a bike travels as its tire rotates by a specified angle");
        m125m2t1.addExBlock("Applications of finding angle subtended by an arc length")
                .addEx("ST42_1_F06_01", "Find number of turns on a spool needed to wind a specified length of thread");
        m125m2t1.addExBlock("Applications of finding radius given arc length and angle")
                .addEx("ST42_1_F07_01", "Find radius of walkway edge that will use a specified length of edging")
                .addEx("ST42_1_F07_02", "Laying out a shape in cloth to create a garment with desired sizing");

        final LearningTargetData m125m2t2 = m125m2.addLearningTarget(2, 2, "2.2", "ST42_2_HW",
                "I can locate points the unit circle, including:",
                "construct a right triangle whose side lengths are the x- and y-coordinates of a point on the unit "
                        + "circle",
                "associate an angle with every point on the unit circle",
                "identify angles whose associated points have the same x- or y-coordinate",
                "calculate the x- or y-coordinate of a point on a circle given the other coordinate and the point's "
                        + "quadrant",
                "interpret how x- or y-coordinates would change as angle increases in each quadrant");
        m125m2t2.addExBlock("Constructing right triangles corresponding to points on a circle")
                .addEx("ST42_2_F01_01", "Find point where terminal ray intersects unit circle in any quadrant");
        m125m2t2.addExBlock("Associate an angle with every point on the unit circle, identify angles whose points "
                        + "share x or y coordinates")
                .addEx("ST42_2_F02_01",
                        "Explore relationship between coordinates of points associated with rays having same "
                                + "reference angle ");
        m125m2t2.addExBlock("Generalize relationship between points sharing the same x coordinate")
                .addEx("ST42_2_F03_01",
                        "Find pairs of angles whose terminal rays intersect the unit circle with same x coordinate");
        m125m2t2.addExBlock("Generalize relationship between points sharing the same y coordinate")
                .addEx("ST42_2_F04_01",
                        "Find pairs of angles whose terminal rays intersect the unit circle with same y coordinate");
        m125m2t2.addExBlock("Given one coordinate of a point on an arbitrary circle, calculate the other")
                .addEx("ST42_2_F05_01", "Given radius and one coordinate on a circle, find the missing coordinate");

        final LearningTargetData m125m2t3 = m125m2.addLearningTarget(2, 3, "2.3", "ST42_3_HW",
                "I can work with sector area, including:",
                "calculate the area of a sector of a circle of any radius subtended by a given angle",
                "calculate the measure of the angle that subtends a sector of a circle of any radius with specified "
                        + "area",
                "calculate the radius of a sector subtended by a specified angle having specified area");
        m125m2t3.addExBlock("Interpret the relationship between radian measure and sector area")
                .addEx("ST42_3_F01_01",
                        "Relate angle measure to fraction of circle's area contained, then to sector area");
        m125m2t3.addExBlock("Calculate area of a sector subtended by an angle on a circle of radius R")
                .addEx("ST42_3_F02_01", "Given angle, in degrees, and radius, calculate sector area")
                .addEx("ST42_3_F02_02", "Given angle, in radians, and radius, calculate sector area");
        m125m2t3.addExBlock("Calculate angle subtended by sector with specified area")
                .addEx("ST42_3_F03_01", "Find angle that generates a specified sector area with a given radius");
        m125m2t3.addExBlock("Calculate radius of sector with specified area subtended by specified angle")
                .addEx("ST42_3_F04_01", "Given an angle and the sector area it contains, find the sector radius");
        m125m2t3.addExBlock("Applications of calculating sector area from radius and angle")
                .addEx("ST42_3_F05_01", "Determine which slice of pizza is larger, with different radii and angles");
        m125m2t3.addExBlock("Applications of calculating angle from radius and area")
                .addEx("ST42_3_F06_01", "Determine angle to cut to make small pizza slices same area as larger ones");
        m125m2t3.addExBlock("Applications of calculating radius from angle and area")
                .addEx("ST42_3_F07_01", "Given pizzas with slices the same area, with different angles, find radius");

        // Module 3

        final ModuleData m125m3 = m125.addModule(3, "The Trigonometric Functions", "SR43_HW", "c43-thumb.png");

        m125m3.skillsReview.addExampleBlock("Estimating the measure of angles")
                .addEx("SR43_01_01", "Match angle to its measure in degrees or radians");
        m125m3.skillsReview.addExampleBlock("Negative and co-terminal angles")
                .addEx("SR43_02_01", "Find positive angle that's co-terminal to a negative angle");
        m125m3.skillsReview.addExampleBlock("Pythagorean theorem")
                .addEx("SR43_03_01", "Find triangle side length using Pythagorean theorem");
        m125m3.skillsReview.addExampleBlock("Similar triangles")
                .addEx("SR43_04_01", "Use proportionality to solve for side length in similar triangle");
        m125m3.skillsReview.addExampleBlock("Supplementary angles and reference angles")
                .addEx("SR43_05_01", "Find angles in all quadrants with reference angle of a given angle.");

        final LearningTargetData m125m3t1 = m125m3.addLearningTarget(3, 1, "3.1", "ST43_1_HW",
                "I can define and interpret the six trigonometric functions, including:",
                "define each of the six trigonometric functions in terms of ratios of coordinates of points on the "
                        + "unit circle",
                "interpret each of the six trigonometric functions in terms of these ratios or geometrically",
                "express tangent, cotangent, secant, and cosecant in terms of sine and cosine",
                "calculate tangent of an angle by calculating slope of a terminal ray, or vice-versa",
                "interpret cosine and sine as (x, y) components of point undergoing circular motion");
        m125m3t1.addExBlock("Algebraic and geometric definition of cosine")
                .addEx("ST43_1_F01_01", "Describe what the cosine of an angle represents");
        m125m3t1.addExBlock("Algebraic and geometric definition of sine")
                .addEx("ST43_1_F02_01", "Describe what the sine of an angle represents");
        m125m3t1.addExBlock("Algebraic definitions of the trigonometric functions")
                .addEx("ST43_1_F03_01", "Match trigonometric functions to their algebraic definitions");
        m125m3t1.addExBlock("Geometric meaning of the trig functions")
                .addEx("ST43_1_F04_01", "Relate quantities drawn in a unit circle to the trigonometric functions");
        m125m3t1.addExBlock("Relationship between tangent and slope")
                .addEx("ST43_1_F05_01",
                        "Given point where terminal ray intersects a unit circle, find tangent of the angle and slope "
                                + "of the terminal ray");
        m125m3t1.addExBlock("Interpretation of sine/cosine in terms of circular motion")
                .addEx("ST43_1_F06_01", "Examine the behavior of sine and cosine as angle increases");

        final LearningTargetData m125m3t2 = m125m3.addLearningTarget(3, 2, "3.2", "ST43_2_HW",
                "I can graph and interpret graphs of the six trigonometric functions, including:",
                "graph each of the trigonometric functions",
                "state the domain and range of each trigonometric function, including points where the function is "
                        + "undefined",
                "state the period of each trigonometric function",
                "classify each trigonometric function as even or odd",
                "state the algebraic relationship that the even/odd characteristic of each trigonometric function "
                        + "implies",
                "identify regions where each trigonometric function is positive or negative",
                "identify regions where each trigonometric function is increasing or decreasing as angle increases");
        m125m3t2.addExBlock("Identifying graphs of trig functions")
                .addEx("ST43_2_F01_01", "Match graphs to the corresponding trigonometric function ");
        m125m3t2.addExBlock("Domain of each trigonometric function")
                .addEx("ST43_2_F02_01", "Specify the domain where each trigonometric function is defined");
        m125m3t2.addExBlock("Range of each trigonometric function")
                .addEx("ST43_2_F03_01", "Specify the range of each trigonometric function");
        m125m3t2.addExBlock("Even/odd behavior")
                .addEx("ST43_2_F04_01",
                        "Describe what is meant by 'even' and 'odd' functions, and classify trigonometric functions "
                                + "as even or odd");
        m125m3t2.addExBlock("Even/odd relationships and behaviors")
                .addEx("ST43_2_F05_01",
                        "Indicate algebraic relationships that are true based on even/odd nature of functions");
        m125m3t2.addExBlock("Regions where trigonometric functions are positive or negative")
                .addEx("ST43_2_F06_01",
                        "Identify quadrants in which each trigonometric function is positive or negative");
        m125m3t2.addExBlock("Regions where trigonometric functions are increasing or decreasing")
                .addEx("ST43_2_F07_01",
                        "Identify quadrants in which each trigonometric function is increasing or decreasing");
        m125m3t2.addExBlock("Period of a function")
                .addEx("ST43_2_F08_01", "Determine the period of each trigonometric function");

        final LearningTargetData m125m3t3 = m125m3.addLearningTarget(3, 3, "3.3", "ST43_3_HW",
                "I can evaluate trigonometric functions in several contexts, including:",
                "recalling values of sine and cosine for common angles, and calculating the other four function values",
                "evaluate any trigonometric function for any angle, in either degrees or radians, using technology",
                "apply the Pythagorean theorem to calculate sine from cosine or vice versa",
                "calculate possible values of various trigonometric functions for a given value of sine or cosine");
        m125m3t3.addExBlock("Values of sine and cosine for common angles")
                .addEx("ST43_3_F01_01", "Fill in a diagram of (x, y) coordinates at points around a unit circle with "
                        + "cosine and sine values");
        m125m3t3.addExBlock("Evaluating trigonometric functions using technology")
                .addEx("ST43_3_F02_01", "Evaluate trigonometric functions for angles measured in degrees or radians");
        m125m3t3.addExBlock("Calculate possible values for sine or cosine from the other")
                .addEx("ST43_3_F03_01", "Given a value for sine or cosine, find possible values for the other " +
                        "function");
        m125m3t3.addExBlock("Calculate possible values for all trigonometric functions from sine or cosine")
                .addEx("ST43_3_F04_01", "Given a value for sine or cosine, find possible values for all five other "
                        + "trigonometric functions");
        m125m3t3.addExBlock("Calculate possible value or all trigonometric functions from either secant or cosecant")
                .addEx("ST43_3_F05_01", "Given a value for secant or cosecant, find possible values for all five "
                        + "other trigonometric functions");

        // Module 4

        final ModuleData m125m4 = m125.addModule(4, "Transformations of Trigonometric Functions", "SR44_HW",
                "c44-thumb.png");

        m125m4.skillsReview.addExampleBlock("Evaluation and order of operations")
                .addEx("SR44_01_01", "Determine order in which to perform operations to evaluate an expression");
        m125m4.skillsReview.addExampleBlock("Behavior of sine at zero")
                .addEx("SR44_02_01", "Describe the value and behavior of the sine function at angle zero");
        m125m4.skillsReview.addExampleBlock("Behavior of cosine at zero")
                .addEx("SR44_03_01", "Describe the value and behavior of the cosine function at angle zero");
        m125m4.skillsReview.addExampleBlock("Behavior of secant at zero")
                .addEx("SR44_04_01", "Describe the value and behavior of the secant function at angle zero");
        m125m4.skillsReview.addExampleBlock("Behavior of cosecant at zero")
                .addEx("SR44_05_01", "Describe the value and behavior of the cosecant function at angle zero");
        m125m4.skillsReview.addExampleBlock("Behavior of tangent at zero")
                .addEx("SR44_06_01", "Describe the value and behavior of the tangent function at angle zero");
        m125m4.skillsReview.addExampleBlock("Behavior of cotangent at zero")
                .addEx("SR44_07_01", "Describe the value and behavior of the cotangent function at angle zero");
        m125m4.skillsReview.addExampleBlock("Relationship between sine and cosine and point where terminal ray of "
                        + "angle meets unit circle")
                .addEx("SR44_08_01",
                        "Find the (x, y) coordinates of the intersection point of a terminal ray and the unit circle");

        final LearningTargetData m125m4t1 = m125m4.addLearningTarget(4, 1, "4.1", "ST44_1_HW",
                "I can find or interpret shifts of trigonometric functions, graphically or algebraically, including:",
                "a vertical shift, and its relationship with a \"+ k\" constant added to the function value",
                "a horizontal shift, and its relationship with a \"- h\" constant subtracted from the function's "
                        + "argument",
                "interpret sine and cosine as horizontal shifts of one another",
                "interpret secant and cosecant as horizontal shifts of one another");
        m125m4t1.addExBlock("Write a sine or cosine functions from a vertically shifted graph")
                .addEx("ST44_1_F01_01",
                        "Given the graph of a vertically shifted sine or cosine function, write its equation");
        m125m4t1.addExBlock("Write a secant or cosecant functions from a vertically shifted graph")
                .addEx("ST44_1_F02_01",
                        "Given the graph of a vertically shifted secant or cosecant function, write its equation");
        m125m4t1.addExBlock("Write a tangent or cotangent functions from a vertically shifted graph")
                .addEx("ST44_1_F03_01",
                        "Given the graph of a vertically shifted tangent or cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch graphs of vertically shifted trigonometric functions")
                .addEx("ST44_1_F04_01",
                        "Given the algebraic form of a trigonometric function with vertical shift, sketch the graph");
        m125m4t1.addExBlock("Write a sine or cosine functions from a horizontally shifted graph")
                .addEx("ST44_1_F05_01",
                        "Given the graph of a horizontally shifted sine or cosine function, write its equation");
        m125m4t1.addExBlock("Write a secant or cosecant functions from a horizontally shifted graph")
                .addEx("ST44_1_F06_01",
                        "Given the graph of a horizontally shifted secant or cosecant function, write its equation");
        m125m4t1.addExBlock("Write a tangent or cotangent functions from a horizontally shifted graph")
                .addEx("ST44_1_F07_01",
                        "Given the graph of a horizontally shifted tangent or cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch graphs of vertically shifted trigonometric functions")
                .addEx("ST44_1_F08_01",
                        "Given the algebraic form of a trigonometric function with horizontal shift, sketch the graph");
        m125m4t1.addExBlock("Combinations of horizontal and vertical shifts")
                .addEx("ST44_1_F09_01",
                        "Given the algebraic form of a trigonometric function with vertical and horizontal shifts, "
                                + "sketch the graph");
        m125m4t1.addExBlock("Interpreting shifted sine or cosine as either function")
                .addEx("ST44_1_F10_01",
                        "Given a graph that could be a shifted sine or cosine, find its equation using either "
                                + "function");

        final LearningTargetData m125m4t2 = m125m4.addLearningTarget(4, 2, "4.2", "ST44_2_HW",
                "I can find or interpret scalings of trigonometric functions, graphically or algebraically, including:",
                "a scaling of amplitude, and its relationship with the coefficient on the function",
                "a scaling of period, and its relationship with the coefficient on the function argument",
                "the relationship between coordinates of a point on a circle of radius R and trigonometric functions " +
                        "of the corresponding angle scaled with coefficient R");
        m125m4t2.addExBlock("Write a sine or cosine functions from a vertically scaled graph")
                .addEx("ST44_2_F01_01",
                        "Given the graph of a vertically scaled sine or cosine function, write its equation");
        m125m4t2.addExBlock("Write a secant or cosecant functions from a vertically scaled graph")
                .addEx("ST44_2_F02_01",
                        "Given the graph of a vertically scaled secant or cosecant function, write its equation");
        m125m4t2.addExBlock("Write a tangent or cotangent functions from a vertically scaled graph")
                .addEx("ST44_2_F03_01",
                        "Given the graph of a vertically scaled tangent or cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch graphs of vertically scaled trigonometric functions")
                .addEx("ST44_2_F04_01",
                        "Given the algebraic form of a trigonometric function with vertical scaling, sketch the graph");
        m125m4t1.addExBlock("Write a sine or cosine functions from a horizontally scaled graph")
                .addEx("ST44_2_F05_01",
                        "Given the graph of a horizontally scaled sine or cosine function, write its equation");
        m125m4t1.addExBlock("Write a secant or cosecant functions from a horizontally scaled graph")
                .addEx("ST44_2_F06_01",
                        "Given the graph of a horizontally scaled secant or cosecant function, write its equation");
        m125m4t1.addExBlock("Write a tangent or cotangent functions from a horizontally scaled graph")
                .addEx("ST44_2_F07_01",
                        "Given the graph of a horizontally scaled tangent or cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch graphs of vertically scaled trigonometric functions")
                .addEx("ST44_2_F08_01",
                        "Given the algebraic form of a trigonometric function with horizontal scaled, sketch the "
                                + "graph");
        m125m4t1.addExBlock("Combinations of horizontal and vertical scalings")
                .addEx("ST44_2_F09_01",
                        "Given the algebraic form of a trigonometric function with vertical and horizontal scalings, "
                                + "sketch the graph");
        m125m4t1.addExBlock("Express coordinates of point on arbitrary circle as scaled sine and cosine functions")
                .addEx("ST44_2_F10_01",
                        "Given an angle and radius, express coordinates of the point its terminal ray meets the unit "
                                + "circle in terms of scaled sine and cosine");

        final LearningTargetData m125m4t3 = m125m4.addLearningTarget(4, 3, "4.3",
                "ST44_3_HW",
                "I can find the algebraic form of a trigonometric function from its graph, including:",
                "find a scaled and shifted trigonometric function that matches a given graph, in the form <i>y=A "
                        + "fxn(B (x-h))+k</i>, where <i>fxn</i> is any of the six trigonometric functions.",
                "given a graph that could be expressed as either sine or cosine, construct a function that matches "
                        + "the graph",
                "given a graph that could be expressed as either secant or cosecant, construct a function that "
                        + "matches the graph");
        m125m4t3.addExBlock("Identify graph of a given shifted and scaled sine or cosine function")
                .addEx("ST44_3_F01_01", "Given a scaled and shifted sine or cosine function, construct its graph");
        m125m4t3.addExBlock("Interpreting graphs of scaled and shifted sine or cosine functions")
                .addEx("ST44_3_F02_01",
                        "Given the graph of a scaled and shifted sine or cosine function, write its equation");
        m125m4t3.addExBlock("Interpreting graphs of scaled and shifted general sine or cosine functions")
                .addEx("ST44_3_F03_01", "Given the graph that could be a scaled and shifted sine or cosine function, "
                        + "find its equation for either function");
        m125m4t3.addExBlock("Interpreting graphs of scaled and shifted general secant or cosecant functions")
                .addEx("ST44_3_F04_01", "Given the graph that could be a scaled and shifted secant or cosecant " +
                        "function, find its equation for either function");
        m125m4t3.addExBlock("Amplitude and period")
                .addEx("ST44_3_F05_01",
                        "Given a graph, identify its amplitude, period, vertical shift, and horizontal shift");

        // Module 5

        final ModuleData m125m5 = m125.addModule(5, "Modeling with Trigonometric Functions", "SR45_HW",
                "c45-thumb.png");

        m125m5.skillsReview.addExampleBlock("Amplitude, period, and shifts")
                .addEx("SR45_01_01",
                        "Given a graph, identify its amplitude, period, vertical shift, and horizontal shift");
        m125m5.skillsReview.addExampleBlock("Common unit circle values")
                .addEx("SR45_02_01", "Given a unit circle with common angles marked, fill in sine and cosine values");
        m125m5.skillsReview.addExampleBlock(
                        "Pythagorean relationship between sine and cosine, definitions of secant/cosecant/tangent")
                .addEx("SR45_03_01", "Identify which of several equations are true");
        m125m5.skillsReview.addExampleBlock("Evaluating parametric curves")
                .addEx("SR45_04_01", "Given two equations defining a parametric curve, find points on that curve");
        m125m5.skillsReview.addExampleBlock("Interpreting parametric curves")
                .addEx("SR45_05_01", "Determine which set of parametric equations matches a given graph");

        final LearningTargetData m125m5t1 = m125m5.addLearningTarget(5, 1, "5.1", "ST45_1_HW",
                "I can model sinusoidal tabular data with shifted and scaled sine or cosine functions, including:",
                "determine the amplitude and period of sinusoidal tabular data",
                "determine the horizontal and vertical shift of sine or cosine needed to model sinusoidal tabular data",
                "write the model in the form <i>y=A sin(B(x-h))+k</i> or <o>y=A cos(B(x-h))+k</i>",
                "interpret the model in terms of behavior of the system being modeled",
                "use the resulting model to make predictions");
        m125m5t1.addExBlock("Extracting amplitude and period from tabular data")
                .addEx("ST45_1_F01_01", "Given a table of data, determine the amplitude and period");
        m125m5t1.addExBlock("Extracting shift data for sine or cosine from tabular data")
                .addEx("ST45_1_F02_01",
                        "Given a table of data, find the horizontal and vertical shifts for sine or cosine");
        m125m5t1.addExBlock("Create a sine or cosine model from tabular data")
                .addEx("ST45_1_F03_01", "Given a table of data, find a cosine or sine model");
        m125m5t1.addExBlock("Interpreting a sine or cosine model and predicting behavior")
                .addEx("ST45_1_F04_01", "given a context, tabular data, and a model, interpret and make predictions");

        final LearningTargetData m125m5t2 = m125m5.addLearningTarget(5, 2, "5.2", "ST45_2_HW",
                "I can model circular or elliptical motion using sine and cosine functions, including:",
                "recognize or write the equation for a circle at arbitrary position in the plane",
                "determine theta as a function of time given an angular frequency.",
                "model the x and y coordinates of circular motion on a unit circle using cosine and sine, "
                        + "respectively, of a function of time that represents theta",
                "model the x, and y coordinates of circular motion on a circle of arbitrary size centered at the "
                        + "origin.",
                "model the x and y coordinates of motion along a circle in arbitrary position.",
                "model (nonuniform) motion on an axis-aligned ellipse using sine and cosine functions.");
        m125m5t2.addExBlock("Equation for a circle")
                .addEx("ST45_2_F01_01", "Given a circle in the plane, write its equation");
        m125m5t2.addExBlock("Express angle as a function of time")
                .addEx("ST45_2_F02_01", "Represent circular motion by expressing angle as a function of time");
        m125m5t2.addExBlock("Parametric representation of motion around unit circle")
                .addEx("ST45_2_F03_01",
                        "Represent circular motion on unit circle using parametric functions for x and y");
        m125m5t2.addExBlock("Parametric representation of motion around general circle")
                .addEx("ST45_2_F04_01",
                        "Represent circular motion on general circle using parametric functions for x and y");
        m125m5t2.addExBlock("Pythagorean relationship between sine and cosine")
                .addEx("ST45_2_F05_01", "Derive Pythagorean relationship between sine and cosine");
        m125m5t2.addExBlock("Parametric representation of motion around circle not at origin")
                .addEx("ST45_2_F06_01", "Represent circular motion on an arbitrary circle in the plane using "
                        + "parametric functions for x and y");
        m125m5t2.addExBlock("Equation for an ellipse")
                .addEx("ST45_2_F07_01", "Write equation for ellipse given center and radii in x and y directions");
        m125m5t2.addExBlock("Parametric definition of ellipse")
                .addEx("ST45_2_F08_01", "Derive equation for ellipse from its parametric definition");
        m125m5t2.addExBlock("Parametric representation of motion around ellipse not at origin")
                .addEx("ST45_2_F09_01", "Represent circular motion on an arbitrary ellipse in the plane using "
                        + "parametric functions for x and y");

        final LearningTargetData m125m5t3 = m125m5.addLearningTarget(5, 3, "5.3", "ST45_3_HW",
                "I can model simple harmonic motion using sine or cosine functions, including:",
                "relate angle theta and time in to a context exhibiting simple harmonic motion",
                "model simple harmonic motion using either sine or cosine",
                "interpret the model and make predictions of behavior");
        m125m5t3.addExBlock("Modeling pendulum swing")
                .addEx("ST45_3_F01_01", "Write a model based on a verbal description of pendulum");
        m125m5t3.addExBlock("Modeling spring-mass motion")
                .addEx("ST45_3_F02_01", "Write a model based on a mass bouncing on a spring");
        m125m5t3.addExBlock("Modeling water waves")
                .addEx("ST45_3_F03_01", "Write a model based on water waves");
        m125m5t3.addExBlock("Interpreting a damped sine or cosine model")
                .addEx("ST45_3_F04_01", "Given a damped sine or cosine model, interpret and make predictions");

        // Module 6

        final ModuleData m125m6 = m125.addModule(6, "Trigonometric Functions in Right Triangles", "SR46_HW",
                "c46-thumb.png");

        m125m6.skillsReview.addExampleBlock("Reference angles")
                .addEx("SR46_01_01", "Given an angle, find its reference angle (Quadrant II)")
                .addEx("SR46_01_02", "Given an angle, find its reference angle (Quadrant III)")
                .addEx("SR46_01_03", "Given an angle, find its reference angle (Quadrant IV)");
        m125m6.skillsReview.addExampleBlock("Angles sharing same sine value")
                .addEx("SR46_02_01", "Find an angle that has the same sine as a given angle");
        m125m6.skillsReview.addExampleBlock("Angles sharing same cosine value")
                .addEx("SR46_03_01", "Find an angle that has the same cosine as a given angle");
        m125m6.skillsReview.addExampleBlock(
                        "Definitions of trigonometric functions in terms of x, y coordinates and sine/cosine")
                .addEx("SR46_04_01",
                        "Match trigonometric functions to their definitions in terms of (x, y) and in terms of sine "
                                + "and cosine");
        m125m6.skillsReview.addExampleBlock("Similar triangles")
                .addEx("SR46_05_01", "Given similar triangles, solve for side lengths using proportionality");

        final LearningTargetData m125m6t1 = m125m6.addLearningTarget(6, 1, "6.1", "ST46_1_HW",
                "Given a right triangle, I can express the relationships between side lengths using trigonometric "
                        + "functions, including:",
                "identify the hypotenuse, and which sides are \"adjacent\" and \"opposite\" relative to each acute"
                        + " angle",
                "recall the \"SOH-CAH-TOA\" relationships",
                "use the \"SOH-CAH-TOA\" relationships to calculate trigonometric function values from triangle side "
                        + "lengths",
                "relate these equations to relationships between x and y coordinates of points on the unit circle");
        m125m6t1.addExBlock("Deduce the SOH-CAH-TOA relationships for triangles with hypotenuse 1")
                .addEx("ST46_1_F01_01", "Derive the \"SOH-CAH-TOA\" relationships for right triangles in a unit " +
                        "circle");
        m125m6t1.addExBlock("Scale relations based on similar triangles")
                .addEx("ST46_1_F02_01", "Given arbitrary size right triangle, create similar triangle with "
                        + "hypotenuse 1, and use to scale side length relationships");
        m125m6t1.addExBlock("Opposite and adjacent sides")
                .addEx("ST46_1_F03_01", "Identifying adjacent and opposite sides to an angle");
        m125m6t1.addExBlock("SOH")
                .addEx("ST46_1_F04_01", "Calculate sine and cosecant values using \"SOH\" relationship");
        m125m6t1.addExBlock("CAH")
                .addEx("ST46_1_F05_01", "Calculate cosine and secant values using \"CAH\" relationship");
        m125m6t1.addExBlock("TOA")
                .addEx("ST46_1_F06_01", "Calculate tangent and cotangent values using \"TOA\" relationship");

        final LearningTargetData m125m6t2 = m125m6.addLearningTarget(6, 2, "6.2", "ST46_2_HW",
                "I can use trigonometric functions to solve for side lengths in right triangles, including:",
                "find a side length given one other side length and one angle using sine",
                "find a side length given one other side length and one angle using cosine",
                "find a side length given one other side length and one angle using tangent",
                "apply these relationships in applied contexts");
        m125m6t2.addExBlock("Apply the \"SOH\" relationship")
                .addEx("ST46_2_F01_01", "Use the \"SOH\" relationship to find hypotenuse length")
                .addEx("ST46_2_F01_02", "Use the \"SOH\" relationship to find opposite side length");
        m125m6t2.addExBlock("Apply the \"CAH\" relationship")
                .addEx("ST46_2_F02_01", "Use the \"CAH\" relationship to find hypotenuse length")
                .addEx("ST46_2_F02_02", "Use the \"CAH\" relationship to find adjacent side length");
        m125m6t2.addExBlock("Apply the \"TOA\" relationship")
                .addEx("ST46_2_F03_01", "Use the \"TOA\" relationship to find adjacent side length")
                .addEx("ST46_2_F03_02", "Use the \"TOA\" relationship to find opposite side length");
        m125m6t2.addExBlock("Application: finding heights from distance and angle")
                .addEx("ST46_2_F04_01", "Given shadow angle and distance from building, find building height");
        m125m6t2.addExBlock("Application: finding distance from height and angle")
                .addEx("ST46_2_F05_01", "Given power pole height, desired angle for support wires, find distance " +
                        "from pole for anchors, and wire length");
        m125m6t2.addExBlock("Application: finding height from angle of elevation")
                .addEx("ST46_2_F06_01", "Given distance to tree and angle of elevation of its top, find its height")
                .addEx("ST46_2_F06_02", "Given support wire anchor distance and angle, find height and wire length");

        final LearningTargetData m125m6t3 = m125m6.addLearningTarget(6, 3, "6.3", "ST46_3_HW",
                "I can apply relationships between right triangle side lengths and trigonometric functions to "
                        + "reference angles corresponding to angles in quadrants II, III, and IV, including:",
                "draw a right triangle using the reference angle in any quadrant",
                "use right triangle relationships in these reference triangles to find relationships between side "
                        + "lengths",
                "given one side and one angle in a right triangle, find all sides and angles");
        m125m6t3.addExBlock("Solve triangle with Quadrant II angle")
                .addEx("ST46_3_F01_01",
                        "Given angle to hypotenuse in quadrant II, solve for side lengths in a right triangle");
        m125m6t3.addExBlock("Solve triangle with Quadrant III angle")
                .addEx("ST46_3_F02_01",
                        "Given angle to hypotenuse in quadrant III, solve for side lengths in a right triangle");
        m125m6t3.addExBlock("Solve triangle with Quadrant IV angle")
                .addEx("ST46_3_F03_01",
                        "Given angle to hypotenuse in quadrant IV, solve for side lengths in a right triangle");
        m125m6t3.addExBlock("Solving right triangles")
                .addEx("ST46_3_F04_01", "Given one acute angle and one side length in a right triangle, solve for " +
                        "all angles and lengths");
        m125m6t3.addExBlock("Solving general triangles")
                .addEx("ST46_3_F05_01", "Given a general triangle, one angle, and two side lengths, find altitude and "
                        + "last side length");
        m125m6t3.addExBlock("Application: tile patterns")
                .addEx("ST46_3_F06_01", "Given a tile pattern, find size of innermost tile");

        // Module 7

        final ModuleData m125m7 = m125.addModule(7, "Inverse Trigonometric Functions", "SR47_HW", "c47-thumb.png");

        m125m7.skillsReview
                .addExampleBlock("Recall the graph, domain, and range of sine and cosine")
                .addEx("SR47_01_01", "Sketch graph of sine and recall its domain and range")
                .addEx("SR47_01_02", "Sketch graph of cosine and recall its domain and range");
        m125m7.skillsReview
                .addExampleBlock("Recall the graph, domain, and range of tangent and cotangent")
                .addEx("SR47_02_01", "Sketch graph of tangent and recall its domain and range")
                .addEx("SR47_02_02", "Sketch graph of cotangent and recall its domain and range");
        m125m7.skillsReview
                .addExampleBlock("Recall the graph, domain, and range of secant and cosecant")
                .addEx("SR47_03_01", "Sketch graph of secant and recall its domain and range")
                .addEx("SR47_03_02", "Sketch graph of cosecant and recall its domain and range");
        m125m7.skillsReview
                .addExampleBlock("Recall that angles with the same x coordinate on unit circle have the same cosine")
                .addEx("SR47_04_01", "Find second angle with the same cosine as a given angle");
        m125m7.skillsReview
                .addExampleBlock("Recall that angles with the same y coordinate on unit circle have the same sine")
                .addEx("SR47_05_01", "Find second angle with the same sine as a given angle");
        m125m7.skillsReview
                .addExampleBlock("Recall that angles with the same slope have the same tangent")
                .addEx("SR47_06_01", "Find second angle with the same tangent as a given angle");

        final LearningTargetData m125m7t1 = m125m7.addLearningTarget(7, 1, "7.1", "ST47_1_HW",
                "I can work identify inverse functions, and determine when a function is invertible, including:",
                "recall the requirements for a function to be one-to-one, and apply the horizontal line test",
                "recall the relationships between the domain and range of inverse functions",
                "recall the relationships that inverse functions satisfy",
                "recall the reflection relationship between the graph of a function and the graph of its inverse, and "
                        + "identify the graph of an inverse function from the function's graph",
                "find a domain restriction of a function that is one-to-one, including restrictions of trigonometric "
                        + "functions");
        m125m7t1.addExBlock("Recall what a one-to-one function looks like, and the horizontal line test")
                .addEx("ST47_1_F01_01", "Given a set of graphs, indicate which are one-to-one functions");
        m125m7t1.addExBlock("Recall facts about inverse functions")
                .addEx("ST47_1_F02_01", "Answer a variety of questions about an inverse function.");
        m125m7t1.addExBlock("Given an algebraic function, find its inverse")
                .addEx("ST47_1_F03_01", "Given a function, find the inverse function");
        m125m7t1.addExBlock("Recall the relationship between the graph of a function and its inverse")
                .addEx("ST47_1_F04_01", "Given the graph of a function, sketch the graph of the inverse function");
        m125m7t1.addExBlock("Domain restrictions")
                .addEx("ST47_1_F05_01",
                        "Given a graph of a function, find domain restrictions where the function is one-to-one");
        m125m7t1.addExBlock("Domain restriction of sine")
                .addEx("ST47_1_F06_01", "Find domain restrictions of sine that are one-to-one");
        m125m7t1.addExBlock("Domain restriction of cosine")
                .addEx("ST47_1_F07_01", "Find domain restrictions of cosine that are one-to-one");
        m125m7t1.addExBlock("Domain restriction of tangent and cotangent")
                .addEx("ST47_1_F08_01", "Find domain restrictions of tangent and cotangent that are one-to-one");
        m125m7t1.addExBlock("Domain restriction of secant and cosecant")
                .addEx("ST47_1_F09_01", "Find domain restrictions of secant and cosecant that are one-to-one");

        final LearningTargetData m125m7t2 = m125m7.addLearningTarget(7, 2, "7.2", "ST47_2_HW",
                "I can work with inverse trigonometric functions, including:",
                "recall their definitions, and state their domain and range",
                "identify or sketch graphs of each inverse function",
                "evaluate inverse function values using technology",
                "interpret values generated by inverse trigonometric functions as angles",
                "find all angles that have a specified value for a trigonometric function");
        m125m7t2.addExBlock("Definition of inverse sine and inverse cosine, domain and range")
                .addEx("ST47_2_F01_01", "Sketch graphs of inverse sine and inverse cosine");
        m125m7t2.addExBlock("Definition of inverse tangent and inverse cotangent, domain and range")
                .addEx("ST47_2_F02_01", "Sketch graphs of inverse tangent and inverse cotangent");
        m125m7t2.addExBlock("Definition of inverse secant and inverse cosecant, domain and range")
                .addEx("ST47_2_F03_01", "Sketch graphs of inverse secant and inverse cosecant");
        m125m7t2.addExBlock("Argument and result types for inverse trigonometric functions")
                .addEx("ST47_2_F04_01", "Describe the type of value that is the input and output of trigonometric "
                        + "functions and inverse trigonometric functions");
        m125m7t2.addExBlock("Evaluate and interpret inverse sine")
                .addEx("ST47_2_F05_01", "Evaluate inverse sine, then find all angles having a specified sine");
        m125m7t2.addExBlock("Evaluate and interpret inverse cosine")
                .addEx("ST47_2_F06_01", "Evaluate inverse cosine, then find all angles having a specified cosine");
        m125m7t2.addExBlock("Evaluate and interpret inverse tangent")
                .addEx("ST47_2_F07_01", "Evaluate inverse tangent, then find all angles having a specified tangent");

        final LearningTargetData m125m7t3 = m125m7.addLearningTarget(7, 3, "7.3", "ST47_3_HW",
                "I can apply inverse trigonometric functions to solve problems, including:",
                "solve for an angle in a right triangle with known side lengths",
                "find the angle whose terminal ray has a specified slope",
                "evaluate compositions of trigonometric and their inverse functions");
        m125m7t3.addExBlock("Solving using inverse sine")
                .addEx("ST47_3_F01_01", "Find angle in triangle given opposite side and hypotenuse");
        m125m7t3.addExBlock("Solving using inverse cosine")
                .addEx("ST47_3_F02_01", "Find angle in triangle given adjacent side and hypotenuse");
        m125m7t3.addExBlock("Solving using inverse tangent")
                .addEx("ST47_3_F03_01", "Find angle in triangle given opposite and adjacent side");
        m125m7t3.addExBlock("Find angle from slope")
                .addEx("ST47_3_F04_01", "Find angle given slope of terminal ray");
        m125m7t3.addExBlock("Compositions: trigonometric function after inverse trigonometric function")
                .addEx("ST47_3_F05_01", "Evaluate the sine of an inverse sine");
        m125m7t3.addExBlock("Compositions: inverse trigonometric function after trigonometric function")
                .addEx("ST47_3_F06_01", "Evaluate the inverse sine of a sine")
                .addEx("ST47_3_F06_03", "Evaluate the inverse tangent of a tangent")
                .addEx("ST47_3_F06_05", "Evaluate the inverse secant of a secant");

        // Module 8

        final ModuleData m125m8 = m125.addModule(8, "Triangles, the Law of Sines, and the Law of Cosines", "SR48_HW",
                "c48-thumb.png");

        m125m8.skillsReview
                .addExampleBlock("Sum of angles in a triangle is 180&deg; or &pi; radians, supplementary angles")
                .addEx("SR48_01_01", "Given a drawing, find the sum of several angle measures");
        m125m8.skillsReview.addExampleBlock("Pythagorean theorem")
                .addEx("SR48_02_01", "Given the length of a chord within a circle, find its radius");
        m125m8.skillsReview.addExampleBlock("SOH-CAH-TOA relationships")
                .addEx("SR48_03_01", "Find angles in a kite shape");
        m125m8.skillsReview.addExampleBlock("Range of inverse sine, cosine, tangent")
                .addEx("SR48_04_01", "Find the quadrant containing inverse trigonometric function values");
        m125m8.skillsReview.addExampleBlock("Domain and inverse sine, cosine, and tangent")
                .addEx("SR48_05_01", "State the domains of inverse sine, inverse cosine, and inverse tangent");

        final LearningTargetData m125m8t1 = m125m8.addLearningTarget(8, 1, "8.1", "ST48_1_HW",
                "I can recall and apply the law of sines, including:",
                "recognize situations where the law of sines applies",
                "write the relationship given by the law of sines for a particular triangle problem",
                "solve for unknown side lengths in general triangles",
                "solve for unknown angles in general triangles",
                "determine when a triangle problem has zero, one, or two solutions, and finding all solutions");
        m125m8t1.addExBlock("Recall the law of sines")
                .addEx("ST48_1_F01_01", "Given a labeled triangle, write the relationships given by the law of sines");
        m125m8t1.addExBlock("Solve for missing side - AAS")
                .addEx("ST48_1_F02_01", "Solve a triangle given two angles and one side length not between them");
        m125m8t1.addExBlock("Solve for missing side - ASA")
                .addEx("ST48_1_F03_01", "Solve a triangle given two angles and one side length between them");
        m125m8t1.addExBlock("Solve for missing angle - two solutions case")
                .addEx("ST48_1_F04_01", "Solve a triangle given one angle and two side lengths");
        m125m8t1.addExBlock("Solve for missing angle - one solutions case")
                .addEx("ST48_1_F05_01", "Solve a triangle given one angle and two side lengths");
        m125m8t1.addExBlock("Solve for missing angle - zero solutions case")
                .addEx("ST48_1_F06_01", "Solve a triangle given one angle and two side lengths");

        final LearningTargetData m125m8t2 = m125m8.addLearningTarget(8, 2, "8.2", "ST48_2_HW",
                "I can recall and apply the law of cosines, including:",
                "recognizing situations where the law of cosines applies",
                "writing the relationship given by the law of cosines for a particular triangle problem",
                "solving for unknown side lengths in general triangles",
                "solving for unknown angles in general triangles");
        m125m8t2.addExBlock("Recall the law of cosines")
                .addEx("ST48_2_F01_01",
                        "Given a labeled triangle, write the relationships given by the law of cosines");
        m125m8t2.addExBlock("Connection between law of cosines and Pythagorean theorem")
                .addEx("ST48_2_F02_01",
                        "Apply law of cosines to a right triangle, and show that it reduces to the Pythagorean "
                                + "theorem");
        m125m8t2.addExBlock("Solve for missing side - SAS")
                .addEx("ST48_2_F03_01", "Solve a triangle given one angle and the two adjacent side lengths");
        m125m8t2.addExBlock("Solve for angles given all three sides")
                .addEx("ST48_2_F04_01", "Solve a triangle given three side lengths");

        final LearningTargetData m125m8t3 = m125m8.addLearningTarget(8, 3, "8.3", "ST48_3_HW",
                "I can solve general triangle problems, including:",
                "choose an appropriate law or relation in the context of a problem",
                "correctly apply the selected law or relation and interpret the results in the context of the problem");
        m125m8t3.addExBlock("Use the law of sines")
                .addEx("ST48_3_F01_01", "Solve a triangle using the law of sines");
        m125m8t3.addExBlock("Use the SOH relationship to solve for a side")
                .addEx("ST48_3_F02_01", "Solve for a side length using the SOH relationship");
        m125m8t3.addExBlock("Use the TOA relationship to solve for angle")
                .addEx("ST48_3_F03_01", "Solve for an angle using the TOA relationship");
        m125m8t3.addExBlock("Use the law of cosines to solve for side")
                .addEx("ST48_3_F04_01", "Solve for a side length using the law of cosines");
        m125m8t3.addExBlock("Use the CAH relationship to solve for angle")
                .addEx("ST48_3_F05_01", "Solve for an angle using the CAH relationship");
        m125m8t3.addExBlock("Use the Pythagorean theorem")
                .addEx("ST48_3_F06_01", "Solve for a side length using the Pythagorean theorem");
        m125m8t3.addExBlock("Use the SOH relationship to solve for angle")
                .addEx("ST48_3_F07_01", "Solve for a supplementary angle using the SOH relationship");
        m125m8t3.addExBlock("Use the law of sines to solve for side")
                .addEx("ST48_3_F08_01", "Solve for a side length using the law of sines");
        m125m8t3.addExBlock("Use the TOA relationship to solve for side")
                .addEx("ST48_3_F09_01", "Solve for a side length using the TOA relationship");
        m125m8t3.addExBlock("Use the law of cosines to solve for angle")
                .addEx("ST48_3_F10_01", "Solve for an angle using the law of cosines");

        // Module 9

        final ModuleData m125m9 = m125.addModule(9, "Vectors and Trigonometry", "SR49_HW", "c49-thumb.png");

        m125m9.skillsReview.addExampleBlock("Vectors, components and vector notation")
                .addEx("SR49_01_01", "Given two points in the plane, write the components of the vector between them");
        m125m9.skillsReview.addExampleBlock("Vector arithmetic")
                .addEx("SR49_02_01", "Given three vectors, calculate resultants");
        m125m9.skillsReview.addExampleBlock("The dot product using components")
                .addEx("SR49_03_01", "Given two vectors, calculate their dot product using components");
        m125m9.skillsReview.addExampleBlock("Triangle area ")
                .addEx("SR49_04_01", "Find the areas of twp triangles");
        m125m9.skillsReview.addExampleBlock("Parallelogram area")
                .addEx("SR49_05_01",
                        "Find the area of a parallelogram");
        m125m9.skillsReview.addExampleBlock("Unit vectors")
                .addEx("SR49_06_01", "Find a unit vector in the direction of a given vector");

        final LearningTargetData m125m9t1 = m125m9.addLearningTarget(9, 1, "9.1", "ST49_1_HW",
                "I can apply the relationship between dot product and the angle between two vectors, including:",
                "using the dot product to compute the angle between two vectors",
                "use the dot product to compute the length of a vector",
                "compute the dot product between two vectors of known length meeting at a specified angle",
                "determine whether two vectors are perpendicular using the dot product");
        m125m9t1.addExBlock("Compute the length of a vector")
                .addEx("ST49_1_F01_01", "Calculate the length of a vector using the dot product");
        m125m9t1.addExBlock("Compute the angle between two vectors")
                .addEx("ST49_1_F02_01",
                        "Given two vectors, compute their dot product, lengths, and the angle between them");
        m125m9t1.addExBlock("Compute dot product from vector angle and lengths")
                .addEx("ST49_1_F03_01", "Given the lengths and angle between two vectors, find their dot product");
        m125m9t1.addExBlock("Determine whether vectors are perpendicular")
                .addEx("ST49_1_F04_01", "Given a vector and one component of a second vector, find the missing "
                        + "component to make the vectors perpendicular");
        m125m9t1.addExBlock("Solving triangles using vertex coordinates")
                .addEx("ST49_1_F05_01", "Given coordinates of vertices of a triangle, final its interior angles");
        m125m9t1.addExBlock("Find vector perpendicular to a line")
                .addEx("ST49_1_F06_01", "Find a vector perpendicular to a line");

        final LearningTargetData m125m9t2 = m125m9.addLearningTarget(9, 2, "9.2", "ST49_2_HW",
                "I can project vectors in specified directions and decompose vectors into components, including:",
                "finding the projection of one vector in the direction of another",
                "writing a vector as the sum of vectors having specified directions",
                "finding the distance of a point from a line");
        m125m9t2.addExBlock("Project vector in direction of a unit vector")
                .addEx("ST49_2_F01_01",
                        "Given a vector and a unit vector, find the projection in the unit vector direction");
        m125m9t2.addExBlock("Project vector in direction of general vector")
                .addEx("ST49_2_F02_01",
                        "Given two vectors, find the projection of first in the direction of the second");
        m125m9t2.addExBlock("Decompose vector")
                .addEx("ST49_2_F03_01", "Given three vectors, write the first as the sum of projections in the "
                        + "directions of the other two");
        m125m9t2.addExBlock("Find shortest vector from a point to a line, and distance from point to line")
                .addEx("ST49_2_F04_01",
                        "Find the shortest vector from a point to a line and the distance from the point to the line");

        final LearningTargetData m125m9t3 = m125m9.addLearningTarget(9, 3, "9.3", "ST49_3_HW",
                "I can use vectors and trigonometry to analyze applied contexts, including:",
                "writing a force as a resultant sum of forces acting in specified directions",
                "interpreting a vector as a speed and using time plus speed to compute distance",
                "modeling tensions in cables using vectors");
        m125m9t3.addExBlock("Application: decomposing force vectors")
                .addEx("ST49_3_F01_01",
                        "Given a scenario with a rope dragging an object on a ramp, analyze forces using vectors");
        m125m9t3.addExBlock("Application: bearing and speed, dead reckoning navigation")
                .addEx("ST49_3_F02_01", "Use bearing direction, speed, and time to calculate start and end position");
        m125m9t3.addExBlock("Application: cable/rope tension, two ropes holding up load")
                .addEx("ST49_3_F03_01",
                        "Given scenario where multiple ropes hold a load, calculate forces using vectors");
        m125m9t3.addExBlock("Application: vectors that change over time")
                .addEx("ST49_3_F04_01", "Analyze distance between two moving objects over time");

        // Module 10

        final ModuleData m125m10 = m125.addModule(10, "Applications of Trigonometric Functions and Triangles",
                "SR50_HW", "c50-thumb.png");

        m125m10.skillsReview.addExampleBlock("SOH-CAH-TOA relationships")
                .addEx("SR50_01_01", "Fill in missing pieces of relationships pertaining to a right triangle");
        m125m10.skillsReview.addExampleBlock("Inverse trigonometric functions")
                .addEx("SR50_02_01",
                        "Indicate quadrants where equations involving inverse trigonometric functions are true");
        m125m10.skillsReview.addExampleBlock("The law of sines")
                .addEx("SR50_03_01", "Calculate side lengths and angles using the law of sines");
        m125m10.skillsReview.addExampleBlock("The law of cosines")
                .addEx("SR50_04_01", "Calculate side length and angle using the law of cosines");
        m125m10.skillsReview.addExampleBlock("Pythagorean theorem")
                .addEx("SR50_05_01", "Calculate side lengths using the Pythagorean theorem");
        m125m10.skillsReview.addExampleBlock("Sector area")
                .addEx("SR50_06_01", "Calculate sector area");
        m125m10.skillsReview.addExampleBlock("Vertical angles, complements, supplements")
                .addEx("SR50_07_01", "Answer some questions regarding complementary and supplementary angles");
        m125m10.skillsReview.addExampleBlock("Triangle and parallelogram area")
                .addEx("SR50_08_01", "On a diagram of a triangle and parallelogram, label what is meant by 'height' "
                        + "and 'base' lengths");

        final LearningTargetData m125m10t1 = m125m10.addLearningTarget(10, 1, "10.1", "ST50_1_HW",
                "I can solve application problems involving right triangles, including:",
                "identifying right triangles in application contexts and creating labeled diagrams of related "
                        + "quantities",
                "identifying applicable relations between quantities in situations involving right triangles",
                "solving right triangle problems for side lengths or angles, and interpreting results in context");
        m125m10t1.addExBlock("Applications of right triangles - SOH case")
                .addEx("ST50_1_F01_01", "For a radio tower supported by wires, calculate anchor point locations, wire "
                        + "lengths, and angles")
                .addEx("ST50_1_F01_02",
                        "For a ladder into a treehouse, find the angle the ladder makes with the ground");
        m125m10t1.addExBlock("Applications of right triangles - SOH case")
                .addEx("ST50_1_F02_01", "Determine whether an ADA wheelchair ramp complies with regulations")
                .addEx("ST50_1_F02_02", "Calculate the angle of swing of a clock pendulum");
        m125m10t1.addExBlock("Applications of right triangles - CAH case")
                .addEx("ST50_1_F03_01",
                        "Calculate angle of crane's boom to place a load a specified distance from its base");
        m125m10t1.addExBlock("Applications of right triangles - TOA case")
                .addEx("ST50_1_F04_01",
                        "For a flight of stairs with given tread depth and riser height, find angle of ascent");
        m125m10t1.addExBlock("Applications of right triangles - TOA case")
                .addEx("ST50_1_F05_01", "Find field of view needed for security camera to cover a room");
        m125m10t1.addExBlock("Applications of right triangles - TOA case")
                .addEx("ST50_1_F06_01", "Find the position of a ship using the bearing to two lighthouses");

        final LearningTargetData m125m10t2 = m125m10.addLearningTarget(10, 2, "10.2", "ST50_2_HW",
                "I can solve application problems involving general triangles, including:",
                "identifying triangles in application contexts and creating labeled diagrams of related quantities",
                "identifying applicable relations between quantities in situations involving general triangles",
                "solving general triangle problems for side lengths or angles, and interpreting results in context");
        m125m10t2.addExBlock("Application: trusses")
                .addEx("ST50_2_F01_01", "Calculate lengths and angles in a truss design");
        m125m10t2.addExBlock("Application: land surveying")
                .addEx("ST50_2_F02_01", "Calculate angles and lengths of the boundary of a plot of land");
        m125m10t2.addExBlock("Application: routing a pipe around an obstacle")
                .addEx("ST50_2_F03_01", "Find pope lengths and joint angles to route a pipe around an obstruction.");
        m125m10t2.addExBlock("Application: dividing field into equal area parts")
                .addEx("ST50_2_F04_01", "Calculate placement of fencing to divide a triangular field");

        final LearningTargetData m125m10t3 = m125m10.addLearningTarget(10, 3, "10.3", "ST50_3_HW",
                "I can calculate areas of regions using trigonometry, including:",
                "calculate the height of a triangle or parallelogram and applying the area formula",
                "break general polygons into triangles and finding areas of each");
        m125m10t3.addExBlock("Application: Solar panel area")
                .addEx("ST50_3_F01_01",
                        "Find the area of a roof that is not in shadow and can be used for solar panels");
        m125m10t3.addExBlock("Application: painting")
                .addEx("ST50_3_F02_01", "Find the area of a wall to calculate amount of paint needed to cover");
        m125m10t3.addExBlock("Application: area of planted field")
                .addEx("ST50_3_F03_01", "Find area of a field to calculate seed and fertilizer amounts");
        m125m10t3.addExBlock("Regular polygons")
                .addEx("ST50_3_F04_01", "Find the area of a regular pentagon");
        m125m10t3.addExBlock("Application: Landscape design")
                .addEx("ST50_3_F05_01", "Given a landscape layout that includes curved walkway, find area of grass");

        return m125;
    }

    /**
     * Creates the MATH 126 course.
     *
     * @return the course data container
     */
    private static CourseData buildMath126() {

        final CourseData m126 = new CourseData(RawRecordConstants.MATH126, "MATH 126", "Analytic Trigonometry", "M126");

        // Module 1

        final ModuleData m126m1 = m126.addModule(1, "Fundamental Trigonometric Identities", "SR51_HW", "c51-thumb.png");
        m126m1.skillsReview.addExampleBlock("Multiplying binomials")
                .addEx("SR51_01_01", "Multiply three binomials");
        m126m1.skillsReview.addExampleBlock("Properties of roots")
                .addEx("SR51_02_01", "Expand a product of factors that involve roots and powers");
        m126m1.skillsReview.addExampleBlock("Imaginary numbers")
                .addEx("SR51_03_01", "Perform arithmetic with the imaginary unit <i>i</i>");
        m126m1.skillsReview.addExampleBlock("Definition of the trigonometric functions")
                .addEx("SR51_04_01", "Write definitions of the trigonometric functions, and identify angles for "
                        + "which they are undefined");
        m126m1.skillsReview.addExampleBlock("Pythagorean theorem")
                .addEx("SR51_05_01", "Solve for side lengths using the Pythagorean theorem");

        final LearningTargetData m126m1t1 = m126m1.addLearningTarget(1, 1, "1.1", "ST51_1_HW",
                "I can work with general identities, including:",
                "explain what makes an equation an identity, in terms of both algebraic and graphical representations.",
                "verify identities", "identify the domain of validity of an identity",
                "apply an identity to change the form of an expression");
        m126m1t1.addExBlock("Definition of an identity")
                .addEx("ST51_1_F01_01", "Indicate which statements are true about identities");
        m126m1t1.addExBlock("Graphical representation of an identity")
                .addEx("ST51_1_F02_01", "Explain what makes an equation an identity, algebraically and graphically");
        m126m1t1.addExBlock("Verifying identities")
                .addEx("ST51_1_F03_01", "Verify a difference of squares identity and infer its domain of validity");
        m126m1t1.addExBlock("Verifying identities")
                .addEx("ST51_1_F04_01",
                        "Verify an identity for products of sums of squares and infer its domain of validity")
                .addEx("ST51_1_F04_02",
                        "Verify an identity for sums of fourth powers and infer its domain of validity");
        m126m1t1.addExBlock("Find domain of validity with square roots")
                .addEx("ST51_1_F05_01", "Verify identities involving <i>i</i> and find their domains of validity");
        m126m1t1.addExBlock("Find domain of validity with imaginary numbers")
                .addEx("ST51_1_F06_01",
                        "Verify an identity involving roots of products and find its domain of validity")
                .addEx("ST51_1_F06_02",
                        "Verify an identity involving roots of quotients and find its domain of validity");
        m126m1t1.addExBlock("Apply identities to change the form of an expression")
                .addEx("ST51_1_F07_01", "Apply an identity to rewrite an expression")
                .addEx("ST51_1_F07_02", "Apply an identity to rewrite an expression");
        m126m1t1.addExBlock("Apply identities to factorize an expression")
                .addEx("ST51_1_F08_01", "Apply an identity to factorize an expression and find its roots")
                .addEx("ST51_1_F08_02", "Apply an identity to factorize an expression and find its roots");

        final LearningTargetData m126m1t2 = m126m1.addLearningTarget(1, 2, "1.2", "ST51_2_HW",
                "I can recall the various forms of the fundamental trigonometric identities, including:",
                "the definitions of tangent, cotangent, secant, and cosecant in terms of sine and cosine",
                "the even/odd identities for all six trigonometric functions, and their graphical interpretation",
                "the cofunction identities for the trigonometric functions, and their graphical interpretation",
                "the Pythagorean identities for the trigonometric functions, and their relationships with right "
                        + "triangles");
        m126m1t2.addExBlock("Definitions of trig functions as identities")
                .addEx("ST51_2_F01_01",
                        "Interpret the definition of tangent as an identity and find its domain of validity")
                .addEx("ST51_2_F01_02",
                        "Interpret the definition of cotangent as an identity and find its domain of validity")
                .addEx("ST51_2_F01_03",
                        "Interpret the definition of secant as an identity and find its domain of validity")
                .addEx("ST51_2_F01_04",
                        "Interpret the definition of cosecant as an identity and find its domain of validity");
        m126m1t2.addExBlock("Even/odd identities for sine and cosine")
                .addEx("ST51_2_F02_01",
                        "Discover the even/odd identities for sine and cosine, and find their domains of validity");
        m126m1t2.addExBlock("Even/odd identities for tangent and cotangent")
                .addEx("ST51_2_F03_01",
                        "Discover the even/odd identities for tangent and cotangent, and find their domains of "
                                + "validity");
        m126m1t2.addExBlock("Even/odd identities for secant and cosecant")
                .addEx("ST51_2_F04_01",
                        "Discover the even/odd identities for secant and cosecant, and find their domains of validity");
        m126m1t2.addExBlock("Cofunction identities for sine and cosine")
                .addEx("ST51_2_F05_01",
                        "Write cofunction identities for sine and cosine, describe them in terms of graphs, and find "
                                + "domains of validity");
        m126m1t2.addExBlock("Cofunction identities for tangent and cotangent")
                .addEx("ST51_2_F06_01",
                        "Write cofunction identities for tangent and cotangent, describe them in terms of graphs, and "
                                + "find domains of validity");
        m126m1t2.addExBlock("Cofunction identities for secant and cosecant")
                .addEx("ST51_2_F07_01",
                        "Write cofunction identities for secant and cosecant, describe them in terms of graphs, and "
                                + "find domains of validity");
        m126m1t2.addExBlock("Pythagorean identity with sine and cosine")
                .addEx("ST51_2_F08_01",
                        "Write the Pythagorean identity for sine and cosine, and find its domain of validity");
        m126m1t2.addExBlock("Pythagorean identity with tangent and secant")
                .addEx("ST51_2_F09_01",
                        "Write the Pythagorean identity for tangent and secant, and find its domain of validity");
        m126m1t2.addExBlock("Pythagorean identity with cotangent and cosecant")
                .addEx("ST51_2_F10_01",
                        "Write the Pythagorean identity for cotangent and cosecant, and find its domain of validity");

        final LearningTargetData m126m1t3 = m126m1.addLearningTarget(1, 3, "1.3", "ST51_3_HW",
                "I can apply fundamental trigonometric identities to rewrite and simplify expression, including",
                "simplifying by using co-function identities",
                "simplifying using even/odd identities",
                "simplifying using Pythagorean identities");
        m126m1t3.addExBlock("Simplifications using definitions")
                .addEx("ST51_3_F01_01",
                        "Use definitions of trigonometric functions as identities to simplify expressions")
                .addEx("ST51_3_F01_02",
                        "Use definitions of trigonometric functions as identities to simplify expressions");
        m126m1t3.addExBlock("Applying even/odd identities")
                .addEx("ST51_3_F02_01", "Use even/odd identities to simplify expressions - sine")
                .addEx("ST51_3_F02_02", "Use even/odd identities to simplify expressions - cosine")
                .addEx("ST51_3_F02_03", "Use even/odd identities to simplify expressions - tangent")
                .addEx("ST51_3_F02_04", "Use even/odd identities to simplify expressions - cotangent")
                .addEx("ST51_3_F02_05", "Use even/odd identities to simplify expressions - secant")
                .addEx("ST51_3_F02_06", "Use even/odd identities to simplify expressions - cosecant");
        m126m1t3.addExBlock("Applying cofunction identities")
                .addEx("ST51_3_F03_01", "Use cofunction identities to simplify expressions - sine")
                .addEx("ST51_3_F03_02", "Use cofunction identities to simplify expressions - cosine")
                .addEx("ST51_3_F03_03", "Use cofunction identities to simplify expressions - tangent")
                .addEx("ST51_3_F03_04", "Use cofunction identities to simplify expressions - cotangent")
                .addEx("ST51_3_F03_05", "Use cofunction identities to simplify expressions - secant")
                .addEx("ST51_3_F03_06", "Use cofunction identities to simplify expressions - cosecant");
        m126m1t3.addExBlock("Applying the Pythagorean identity with sine and cosine")
                .addEx("ST51_3_F04_01", "Use Pythagorean identity for sine and cosine to simplify an expression");
        m126m1t3.addExBlock("Applying the Pythagorean identity with tangent and secant")
                .addEx("ST51_3_F05_01", "Use Pythagorean identities to simplify an expression");

        // Module 2

        final ModuleData m126m2 = m126.addModule(2, "Sum and Difference Identities", "SR52_HW", "c52-thumb.png");

        m126m2.skillsReview.addExampleBlock("Shapes of graphs of the trigonometric functions")
                .addEx("SR52_01_01", "Match graphs to trigonometric functions and recall domain and range of each");
        m126m2.skillsReview.addExampleBlock("Trigonometric functions related to point where angle's terminal ray "
                        + "meets unit circle, and SOH-CAH-TOA relationships")
                .addEx("SR52_02_01", "Given drawing, label quantities represented by each trigonometric function");
        m126m2.skillsReview.addExampleBlock("Add or subtract two rational expressions by finding a common denominator")
                .addEx("SR52_03_01", "Add and subtract rational expressions");
        m126m2.skillsReview.addExampleBlock("Recall values of sine and cosine for common angles")
                .addEx("SR52_04_01", "Recall exact values of sine and cosine for common Quadrant I angles");
        m126m2.skillsReview.addExampleBlock("The distance formula")
                .addEx("SR52_05_01", "Calculate the distance between two points");

        final LearningTargetData m126m2t1 = m126m2.addLearningTarget(2, 1, "2.1", "ST52_1_HW",
                "I can interpret and apply the sum and difference identities, including:",
                "interpret the sum and difference identities for sine and cosine graphically or in the context of " +
                        "rotated right triangles",
                "apply the sum and difference identities to rewrite or simplify expressions ",
                "use the sum or difference identities to verify identities",
                "derive the cofunction and similar identities using the sum and difference identities");
        m126m2t1.addExBlock("Interpret sums and differences of angles graphically")
                .addEx("ST52_1_F01_01", "Given a graph of sine with two angles marked, interpret sums and differences "
                        + "of angles, and describe how values of sine changes in response to changes in either");
        m126m2t1.addExBlock("Application: Right triangles in non-standard positions")
                .addEx("ST52_1_F02_01", "Find lengths in a right triangle in non-standard position");
        m126m2t1.addExBlock("Simplify expression using the sine difference identity")
                .addEx("ST52_1_F03_01", "Use the sine difference of angles identity to simplify an expression");
        m126m2t1.addExBlock("Simplify expression using the cosine sum identity")
                .addEx("ST52_1_F04_01", "Use the cosine sum of angles identity to simplify an expression");
        m126m2t1.addExBlock("Verify identities using the sine and cosine sum and difference identities")
                .addEx("ST52_1_F05_01", "Use the sine sum and difference of angles identities to verify an identity")
                .addEx("ST52_1_F05_02", "Use the sine difference of angles identity to verify an identity");
        m126m2t1.addExBlock("Derive cofunction-like identities")
                .addEx("ST52_1_F06_01", "Use the sine sum of angles identity to derive an identity")
                .addEx("ST52_1_F06_02", "Use the cosine difference of angles identity to derive an identity");

        final LearningTargetData m126m2t2 = m126m2.addLearningTarget(2, 2, "2.2", "ST52_2_HW",
                "I can use sum and difference identities to evaluate expressions, including:",
                "trigonometric functions at angles that are sums or differences of angles for which those function "
                        + "values are known",
                "difference quotients involving trigonometric functions",
                "solve application problems using sum and difference identities");
        m126m2t2.addExBlock("Evaluate expressions using sum and difference identities for sine and cosine")
                .addEx("ST52_2_F01_01",
                        "Compute sine of a value that can be expressed as a difference of common angles")
                .addEx("ST52_2_F01_02",
                        "Compute cosine of a value that can be expressed as a difference of common angles");
        m126m2t2.addExBlock(
                        "Evaluate expressions using sum and difference identities for other trigonometric functions")
                .addEx("ST52_2_F02_01",
                        "Compute tangent of a value that can be expressed as a sum of common angles");
        m126m2t2.addExBlock("Analyze a difference quotient")
                .addEx("ST52_2_F03_01", "Evaluate a difference quotient for several values of its denominator that "
                        + "approach zero and interpret behavior");
        m126m2t2.addExBlock("Solve application problems")
                .addEx("ST52_2_F04_01", "Find distance between tips of two arms that move on a central pivot");

        final LearningTargetData m126m2t3 = m126m2.addLearningTarget(2, 3, "2.3", "ST52_3_HW",
                "I can apply product-to-sum and sum-to-product identities, including:",
                "use these identities to rewrite or simplify expressions",
                "use them to verify identities",
                "solve trigonometric equations using sum to product identities");
        m126m2t3.addExBlock("Product to sum identities")
                .addEx("ST52_3_F01_01", "Rewrite an expression containing a product of sines as a sum")
                .addEx("ST52_3_F01_02", "Rewrite an expression containing a product of cosines as a sum")
                .addEx("ST52_3_F01_03", "Rewrite an expression containing a product of a sine and a cosine as a sum");
        m126m2t3.addExBlock("Sum to product identities")
                .addEx("ST52_3_F02_01", "Rewrite an expression with a sum of sines as a product")
                .addEx("ST52_3_F02_02", "Rewrite an expression with a sum of cosines as a product");
        m126m2t3.addExBlock("Verifying identities using product to sum and sum to product identities")
                .addEx("ST52_3_F03_01", "Verify an identity involving sums of sines and cosines");
        m126m2t3.addExBlock("Factorizing equations using sum to product identities")
                .addEx("ST52_3_F04_01", "Find solutions to an equation involving a sum of sines by converting to a "
                        + "product and factorizing")
                .addEx("ST52_3_F04_02", "Find solutions to an equation involving a sum of cosines by converting to a "
                        + "product and factorizing");

        // Module 3

        final ModuleData m126m3 = m126.addModule(3, "Multiple-Angle and Half-Angle Identities", "SR53_HW",
                "c53-thumb.png");

        m126m3.skillsReview.addExampleBlock("Piecewise Functions")
                .addEx("SR53_01_01", "Graph a piecewise function and evaluate.");
        m126m3.skillsReview.addExampleBlock("Squares of square roots")
                .addEx("SR53_02_01",
                        "Graph square root functions, then their square, and explore relationships between them");
        m126m3.skillsReview.addExampleBlock("Cosine, Sine, and coordinates on circles")
                .addEx("SR53_03_01", "Find coordinates of points on circles using cosine and sine");
        m126m3.skillsReview.addExampleBlock("Pythagorean identity for sine and cosine")
                .addEx("SR53_04_01", "Use the Pythagorean identity to simplify an expression");
        m126m3.skillsReview.addExampleBlock("Definitions of trigonometric functions")
                .addEx("SR53_05_01", "Use definitions of trigonometric functions to expand and simplify an expression");

        final LearningTargetData m126m3t1 = m126m3.addLearningTarget(3, 1, "3.1", "ST53_1_HW",
                "I can interpret and apply double- and multiple-angle identities, including:",
                "interpret double- and multiple-angle identities for sine and cosine graphically or in the context of "
                        + "arrays of right triangles",
                "apply the double- and multiple-angle identities to rewrite and simplify expressions",
                "use the double- and multiple-angle identities to verify identities");
        m126m3t1.addExBlock("Interpret double-angle and multiple-angle quantities graphically")
                .addEx("ST53_1_F01_01", "Examine doubled and multiple angles and how they change as angle changes");
        m126m3t1.addExBlock("Application: arrays of equal angles")
                .addEx("ST53_1_F02_01", "Find point coordinates on a circle in terms of multiples of angles");
        m126m3t1.addExBlock("Using the double-angle identity for sine")
                .addEx("ST53_1_F03_01", "Simplify an expression using the double-angle identity for sine");
        m126m3t1.addExBlock("Using the double-angle identity for cosine")
                .addEx("ST53_1_F04_01", "Simplify an expression using the double-angle identity for cosine");
        m126m3t1.addExBlock("Using the triple-angle identity for cosine")
                .addEx("ST53_1_F05_01", "Simplify an expression using the triple-angle identity for cosine");
        m126m3t1.addExBlock("Using the quadruple-angle identity for sine")
                .addEx("ST53_1_F06_01", "Simplify an expression using the quadruple-angle identity for sine");
        m126m3t1.addExBlock("Using double-and multiple-angle identities to verify identities")
                .addEx("ST53_1_F07_01", "Given appropriate double-angle identities, verify another identity");

        final LearningTargetData m126m3t2 = m126m3.addLearningTarget(3, 2, "3.2", "ST53_2_HW",
                "I can use half-angle identities, including:",
                "interpret half-angle identities for sine and cosine graphically or in the context of bisected angles",
                "apply the half-angle identities to rewrite and simplify expressions",
                "use the half-angle identities to verify identities");
        m126m3t2.addExBlock("Interpret half-angle quantities graphically")
                .addEx("ST53_2_F01_01", "Find point coordinates on a circle in terms halved angles");
        m126m3t2.addExBlock("Using the half-angle identity for sine")
                .addEx("ST53_2_F02_01", "Simplify an expression using the half-angle identity for sine");
        m126m3t2.addExBlock("Using the half-angle identity for cosine")
                .addEx("ST53_2_F03_01", "Simplify an expression using the half-angle identity for cosine");
        m126m3t2.addExBlock("Using half-angle identities to verify identities")
                .addEx("ST53_2_F04_01", "Given appropriate half-angle identities, verify another identity");

        final LearningTargetData m126m3t3 = m126m3.addLearningTarget(3, 3, "3.3", "ST53_3_HW",
                "I can apply double-, multiple-, and half-angle identities, including:",
                "evaluate trigonometric functions at fractions of well-known angles",
                "perform power reduction",
                "solve applied problems involving arrays of equal angles");
        m126m3t3.addExBlock("Evaluate sine and cosine at fractions of angles")
                .addEx("ST53_3_F01_01", "Find exact values of sines of fractions of common angles")
                .addEx("ST53_3_F01_02", "Find exact values of cosines of fractions of common angles");
        m126m3t3.addExBlock("Evaluate sine and cosine at 3/2 of an angle")
                .addEx("ST53_3_F02_01", "Find the exact value for the sine of 3/2 of a common angle")
                .addEx("ST53_3_F02_02", "Find the exact value for the cosine of 3/2 of a common angle");
        m126m3t3.addExBlock("Verify power reduction formulas")
                .addEx("ST53_3_F03_01",
                        "Use half-angle and sum of angle identities to verify the power reduction identities");
        m126m3t3.addExBlock("Perform power reduction")
                .addEx("ST53_3_F04_01", "Rewrite expressions with powers of sine and cosine to reduce exponents");
        m126m3t3.addExBlock("Application: Gear train")
                .addEx("ST53_3_F05_01", "Solve application problem involving gear rotation");
        m126m3t3.addExBlock("Application: Spiral staircase")
                .addEx("ST53_3_F06_01", "Solve application problem involving design of a spiral staircase");

        // Module 4

        final ModuleData m126m4 = m126.addModule(4, "Trigonometric Equations", "SR54_HW", "c54-thumb.png");

        m126m4.skillsReview.addExampleBlock("Cofunction identities")
                .addEx("SR54_01_01", "Recall the cofunction identities");
        m126m4.skillsReview.addExampleBlock("Pythagorean identities")
                .addEx("SR54_02_01", "Recall the Pythagorean trigonometric identities");
        m126m4.skillsReview.addExampleBlock("Right triangle relationships")
                .addEx("SR54_03_01",
                        "Recall the relationship between right triangle side lengths and the sine of an angle")
                .addEx("SR54_03_02",
                        "Recall the relationship between right triangle side lengths and the cosine of an angle")
                .addEx("SR54_03_03",
                        "Recall the relationship between right triangle side lengths and the tangent of an angle");
        m126m4.skillsReview.addExampleBlock("The quadratic formula")
                .addEx("SR54_04_01", "Use the quadratic formula to find the roots of a quadratic")
                .addEx("SR54_04_02", "Use the quadratic formula to find the roots of a quadratic")
                .addEx("SR54_04_03", "Use the quadratic formula to find the roots of a quadratic");
        m126m4.skillsReview.addExampleBlock("Roots of factored equations")
                .addEx("SR54_05_01", "Find roots of an equation where a factored expression is equal to zero");
        m126m4.skillsReview.addExampleBlock("The inverse trigonometric functions")
                .addEx("SR54_06_01", "Sketch the graphs of the inverse trigonometric functions");

        final LearningTargetData m126m4t1 = m126m4.addLearningTarget(4, 1, "4.1", "ST54_1_HW",
                "I can apply identities to find all solutions to trigonometric equations, including:",
                "solve by applying cofunction identities",
                "solve by applying Pythagorean identities",
                "solve by applying half-angle identities",
                "solve by applying double- or multiple-angle identities",
                "solve using the quadratic formula");
        m126m4t1.addExBlock("Solving with double angle formulas")
                .addEx("ST54_1_F01_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F01_02", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F01_03", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F01_04", "Find all solutions in a specified range of a given equation");
        m126m4t1.addExBlock("Solving with half-angle formulas")
                .addEx("ST54_1_F02_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F02_02", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F02_03", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F02_04", "Find all solutions in a specified range of a given equation");
        m126m4t1.addExBlock("Solving with Pythagorean identities")
                .addEx("ST54_1_F03_01", "Find all solutions of a given equation")
                .addEx("ST54_1_F03_02", "Find all solutions of a given equation");
        m126m4t1.addExBlock("Solving using the quadratic formula")
                .addEx("ST54_1_F04_01", "Find all solutions of a given equation")
                .addEx("ST54_1_F04_02", "Find all solutions of a given equation");
        m126m4t1.addExBlock("Choosing an appropriate technique and solving")
                .addEx("ST54_1_F05_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F05_02", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_1_F06_01", "Find all solutions of a given equation")
                .addEx("ST54_1_F06_02", "Find all solutions of a given equation");

        final LearningTargetData m126m4t2 = m126m4.addLearningTarget(4, 2, "4.2", "ST54_2_HW",
                "I can solve trigonometric equations, including:",
                "find angles that satisfy an equation using inverse trigonometric functions",
                "factor and find roots of expressions by finding roots of factors",
                "interpret the context of a problem to choose correct solution when multiple solutions are possible");
        m126m4t2.addExBlock("Solving using inverse trigonometric functions")
                .addEx("ST54_2_F01_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_2_F01_02", "Find all solutions in a specified range of a given equation");
        m126m4t2.addExBlock("Solving by factoring")
                .addEx("ST54_2_F02_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_2_F02_02", "Find all solutions in a specified range of a given equation");
        m126m4t2.addExBlock("Solving using factoring and definitions of trigonometric functions")
                .addEx("ST54_2_F03_01", "Find all solutions in a specified range of a given equation")
                .addEx("ST54_2_F03_02", "Find all solutions in a specified range of a given equation");
        m126m4t2.addExBlock("Applications: sinusoidal models of real-world situations")
                .addEx("ST54_2_F04_01", "A model from Biology")
                .addEx("ST54_2_F04_02", "A model from Ecology");

        final LearningTargetData m126m4t3 = m126m4.addLearningTarget(4, 3, "4.3", "ST54_3_HW",
                "I can evaluate compositions of trigonometric and inverse trigonometric functions, including:",
                "create a drawing of a right triangle to help identify relevant relationships",
                "use SOH-CAH-TOA relationships to express trigonometric function values in terms of side lengths",
                "find exact values of compositions of trigonometric and inverse trigonometric functions");
        m126m4t3.addExBlock("Evaluate compositions of a function after its inverse")
                .addEx("ST54_3_F01_01", "Evaluate tangent of an inverse tangent")
                .addEx("ST54_3_F01_02", "Evaluate sine of an inverse sine")
                .addEx("ST54_3_F01_03", "Evaluate cotangent of an inverse cotangent");
        m126m4t3.addExBlock("Evaluate compositions of an inverse function after the function")
                .addEx("ST54_3_F02_01", "Evaluate inverse cosine of a cosine")
                .addEx("ST54_3_F02_02", "Evaluate inverse sine of a sine")
                .addEx("ST54_3_F02_03", "Evaluate inverse tangent of a tangent");
        m126m4t3.addExBlock("Construct right triangles to help analyze compositions")
                .addEx("ST54_3_F03_01", "Construct a triangle to help evaluate a sine of an inverse tangent")
                .addEx("ST54_3_F03_02", "Construct a triangle to help evaluate a cosine of an inverse tangent")
                .addEx("ST54_3_F03_03", "Construct a triangle to help evaluate a cosine of an inverse sine")
                .addEx("ST54_3_F03_04", "Construct a triangle to help evaluate a tangent of an inverse sine")
                .addEx("ST54_3_F03_05", "Construct a triangle to help evaluate a sine of an inverse cosine")
                .addEx("ST54_3_F03_06", "Construct a triangle to help evaluate a tangent of an inverse cosine");
        m126m4t3.addExBlock("Evaluate compositions of trigonometric functions with different inverse functions")
                .addEx("ST54_3_F04_01", "Evaluate a sine of an inverse tangent")
                .addEx("ST54_3_F04_02", "Evaluate a cosine of an inverse tangent")
                .addEx("ST54_3_F04_03", "valuate a cosine of an inverse sine")
                .addEx("ST54_3_F04_04", "Evaluate a tangent of an inverse sine")
                .addEx("ST54_3_F04_05", "Evaluate a sine of an inverse cosine")
                .addEx("ST54_3_F04_06", "Evaluate a tangent of an inverse cosine");

        // Module 5

        final ModuleData m126m5 = m126.addModule(5, "Applications of Trigonometric Equations", "SR55_HW",
                "c55-thumb.png");

        m126m5.skillsReview.addExampleBlock("Right triangle relationships")
                .addEx("SR55_01_01",
                        "Recall the SOH-CAH-TOA relationships and recognize 'opposite' and 'adjacent' angles");
        m126m5.skillsReview.addExampleBlock("Circular motion")
                .addEx("SR55_02_01",
                        "Write parametric functions describing the x- and y-components of circular motion");
        m126m5.skillsReview.addExampleBlock("Angles with perpendicular terminal rays")
                .addEx("SR55_03_01",
                        "Find two angles whose terminal rays are perpendicular to the terminal ray of a given angle");
        m126m5.skillsReview.addExampleBlock("The distance formula")
                .addEx("SR55_04_01", "Calculate the distance between points on the plane");
        m126m5.skillsReview.addExampleBlock("Area of circles and sectors")
                .addEx("SR55_05_01", "Find circle area and sector areas");
        m126m5.skillsReview.addExampleBlock("Arc length")
                .addEx("SR55_06_01", "Find the length of an outline made up of circular arcs");
        m126m5.skillsReview.addExampleBlock("Triangle area")
                .addEx("SR55_07_01",
                        "Find the area of a hexagon by breaking into triangles and finding all triangle areas");
        m126m5.skillsReview.addExampleBlock("The law of sines")
                .addEx("SR55_08_01", "Use the law of sines to solve for side lengths in a triangle");
        m126m5.skillsReview.addExampleBlock("The law of cosines")
                .addEx("SR55_09_01", "Use the law of cosines to solve for a side length in a triangle");

        final LearningTargetData m126m5t1 = m126m5.addLearningTarget(5, 1, "5.1", "ST55_1_HW",
                "I can solve application problems that involve fixed or varying angles, including:",
                "calculate altitudes or heights and distances using angle of elevation and angle of depression",
                "calculate vertex position along a line that maximizes the angle at that vertex",
                "calculate light paths under refraction using Snell’s law",
                "find orientations of an edge of a shape based on the shape’s orientation");
        m126m5t1.addExBlock("Application: Angles of elevation and depression")
                .addEx("ST55_1_F01_01", "Solve problem using angle of depression")
                .addEx("ST55_1_F01_02", "Solve problem using angle of elevation");
        m126m5t1.addExBlock("Application: Maximizing angles")
                .addEx("ST55_1_F02_01", "Find triangle shape that maximizes an angle");
        m126m5t1.addExBlock("Application: Estimating height")
                .addEx("ST55_1_F03_01", "Estimate tree height using two angle measurements");
        m126m5t1.addExBlock("Application: Refraction of light")
                .addEx("ST55_1_F04_01", "Solve light refraction problem for laser beam passing through glass");

        final LearningTargetData m126m5t2 = m126m5.addLearningTarget(5, 2, "5.2", "ST55_2_HW",
                "I can solve application problems that involve rotation, including:",
                "calculate the position of a point on a rotating object",
                "calculate the position of shadows or projections of points on rotating objects",
                "determine when two rotating objects are aligned or are separated by a given angle");
        m126m5t2.addExBlock("Application: Positions of rotating objects")
                .addEx("ST55_2_F01_01", "Solve Ferris wheel problem");
        m126m5t2.addExBlock("Application: Arrays of equal angles")
                .addEx("ST55_2_F02_01", "Solve problem involving spokes on a bicycle tire");
        m126m5t2.addExBlock("Application: Positions of rotating objects")
                .addEx("ST55_2_F03_01", "Solve problem involving hands of an analog clock");

        final LearningTargetData m126m5t3 = m126m5.addLearningTarget(5, 3, "5.3", "ST55_3_HW",
                "I can solve application problems that involve distances, arc length, or sector area, including:",
                "calculate the distance between two points moving linearly",
                "calculate the distance between points on two rotating objects",
                "use triangle and sector area to find volumes or areas of portions of cylinders or circles",
                "use trigonometric functions to find chord lengths in circles");
        m126m5t3.addExBlock("Application: Distance between moving objects")
                .addEx("ST55_3_F01_01", "Solve problem involving boats sailing in different directions");
        m126m5t3.addExBlock("Application: Mechanical devices with rotating parts")
                .addEx("ST55_3_F02_01", "Solve a problem involving the drive motor of a sewing machine's");
        m126m5t3.addExBlock("Application: Tank volumes")
                .addEx("ST55_3_F03_01", "Solve a problem involving water in a cylindrical tank");
        m126m5t3.addExBlock("Application: Arc and chord length")
                .addEx("ST55_3_F04_01", "Find the length of a chord between the ends of an arc of a given length");

        // Module 6

        final ModuleData m126m6 = m126.addModule(6, "Polar Coordinates", "SR56_HW", "c56-thumb.png");

        m126m6.skillsReview.addExampleBlock("Cosine, Sine, and Point Coordinates")
                .addEx("SR56_01_01", "Express points where a terminal ray meets a circle using cosine and sine");
        m126m6.skillsReview.addExampleBlock("Tangent and inverse tangent")
                .addEx("SR56_02_01", "Find tangent of an angle, two angles with a given tangent");
        m126m6.skillsReview.addExampleBlock("Distance and the Pythagorean Theorem")
                .addEx("SR56_03_01", "Given two points, draw a right triangle with the line segment between as its "
                        + "hypotenuse, express distance between using Pythagorean theorem");
        m126m6.skillsReview.addExampleBlock("Conversion between degree and radian measure")
                .addEx("SR56_04_01", "Convert between degree and radian measure");
        m126m6.skillsReview.addExampleBlock("Equation of a circle")
                .addEx("SR56_05_01", "Given a center and a radius, write the equation of the circle");
        m126m6.skillsReview.addExampleBlock("Circumference and arc length")
                .addEx("SR56_06_01", "Find the Circumference of a circle and the length of an arc given the radius");
        m126m6.skillsReview.addExampleBlock("Co-terminal angles")
                .addEx("SR56_07_01", "Given an angle, find co-terminal angles");

        final LearningTargetData m126m6t1 = m126m6.addLearningTarget(6, 1, "6.1", "ST56_1_HW",
                "I can plot and interpret points represented in polar coordinates, including:",
                "plot points from polar coordinates, including those with negative radius",
                "find multiple polar coordinate representations of points, including representations with both "
                        + "positive and negative radius");
        m126m6t1.addExBlock("Plotting points in polar coordinates")
                .addEx("ST56_1_F01_01", "Given polar coordinates of points, plot them on a polar graph");
        m126m6t1.addExBlock("Plotting points in polar coordinates when radius is negative")
                .addEx("ST56_1_F02_01",
                        "Given polar coordinates of points whose radius is negative, plot them on a polar graph");
        m126m6t1.addExBlock("Multiple polar representations of points")
                .addEx("ST56_1_F03_01",
                        "Given polar coordinates of a point, find other sets of coordinates that represent the point");
        m126m6t1.addExBlock("Plotting points and sketching a curve in polar coordinates")
                .addEx("ST56_1_F04_01",
                        "Plot several points in polar coordinates, sketch a smooth curve through them");
        m126m6t1.addExBlock("Draw curves with constant radius")
                .addEx("ST56_1_F05_01", "Match an equation of constant radius with the corresponding polar graph");
        m126m6t1.addExBlock("Draw curves with constant radius, restricted angle")
                .addEx("ST56_1_F06_01", "Match an equation of constant radius and with a domain restriction on angle "
                        + "with the corresponding polar graph");
        m126m6t1.addExBlock("Draw curves with constant angle")
                .addEx("ST56_1_F07_01", "Match an equation of constant angle with the corresponding polar graph");
        m126m6t1.addExBlock("Draw curves with constant angle, restricted radius")
                .addEx("ST56_1_F08_01", "Match an equation of constant angle and with a domain restriction on radius "
                        + "with the corresponding polar graph");
        m126m6t1.addExBlock("Application: triangulation")
                .addEx("ST56_1_F09_01", "Solve a triangulation problem");

        final LearningTargetData m126m6t2 = m126m6.addLearningTarget(6, 2, "6.2", "ST56_2_HW",
                "I can convert points in the plane between Cartesian and Polar coordinates, including:",
                "find polar coordinates for a point in any quadrant specified in Cartesian coordinates",
                "find Cartesian coordinates of a point specified in polar coordinates");
        m126m6t2.addExBlock("Polar to Cartesian conversion with positive radius")
                .addEx("ST56_2_F01_01",
                        "Given polar coordinates of point with positive radius, final (x, y) coordinates");
        m126m6t2.addExBlock("Polar to Cartesian conversion with negative radius")
                .addEx("ST56_2_F02_01",
                        "Given polar coordinates of point with negative radius, final (x, y) coordinates");
        m126m6t2.addExBlock("Cartesian to polar conversion in Quadrant I")
                .addEx("ST56_2_F03_01", "Given (x, y) coordinates of a point in Quadrant I, find polar coordinates");
        m126m6t2.addExBlock("Cartesian to polar conversion in Quadrant II")
                .addEx("ST56_2_F04_01", "Given (x, y) coordinates of a point in Quadrant II, find polar coordinates");
        m126m6t2.addExBlock("Cartesian to polar conversion in Quadrant III")
                .addEx("ST56_2_F05_01", "Given (x, y) coordinates of a point in Quadrant III, find polar coordinates");
        m126m6t2.addExBlock("Cartesian to polar conversion in Quadrant IV")
                .addEx("ST56_2_F06_01", "Given (x, y) coordinates of a point in Quadrant IV, find polar coordinates");
        m126m6t2.addExBlock("Application: position from distance and bearing")
                .addEx("ST56_2_F07_01", "Solve a problem using distance and bearing");

        final LearningTargetData m126m6t3 = m126m6.addLearningTarget(6, 3, "6.3", "ST56_3_HW",
                "I can use polar coordinates in application contexts, including:",
                "models of position or motion relative to a central point",
                "flat layouts that will form conical shapes",
                "central forces or point sources of sound, light, or signals",
                "color models",
                "transformations of points in the plane");
        m126m6t3.addExBlock("Creating conical shapes from flat materials")
                .addEx("ST56_3_F01_01",
                        "Given a desired conical shape, design a flat pattern that can create that shape");
        m126m6t3.addExBlock("Application: Robotics and prosthetics")
                .addEx("ST56_3_F02_01",
                        "Given a robotic compound arm with specified orientation, find actuator coordinates");
        m126m6t3.addExBlock("Application: Inverse kinematics")
                .addEx("ST56_3_F03_01",
                        "Given a robotic compound arm, find orientations that place actuator at a desired position");
        m126m6t3.addExBlock("Application: Cell tower signal strength")
                .addEx("ST56_3_F04_01",
                        "Given locations of cell towers, find signal strengths at various locations");
        m126m6t3.addExBlock("Transformations of points in the plane")
                .addEx("ST56_3_F05_01", "Perform rotations of points in the plane in polar coordinates");

        // Module 7

        final ModuleData m126m7 = m126.addModule(7, "Polar Functions", "SR57_HW", "c57-thumb.png");

        m126m7.skillsReview.addExampleBlock("xxx")
                .addEx("SR57_01_01",
                        "xxx");
        m126m7.skillsReview.addExampleBlock("xxx")
                .addEx("SR57_02_01",
                        "xxx");
        m126m7.skillsReview.addExampleBlock("xxx")
                .addEx("SR57_03_01",
                        "xxx");
        m126m7.skillsReview.addExampleBlock("xxx")
                .addEx("SR57_04_01",
                        "xxx");

        final LearningTargetData m126m7t1 = m126m7.addLearningTarget(7, 1, "7.1", "ST57_1_HW",
                "xxx, including:",
                "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F01_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F02_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F03_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F04_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F05_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F06_01",
                        "xxx");
        m126m7t1.addExBlock("xxx")
                .addEx("ST57_1_F07_01",
                        "xxx");

        final LearningTargetData m126m7t2 = m126m7.addLearningTarget(7, 2, "7.2", "ST57_2_HW",
                "xxx, including:",
                "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F01_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F02_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F03_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F04_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F05_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F06_01",
                        "xxx");
        m126m7t2.addExBlock("xxx")
                .addEx("ST57_2_F07_01",
                        "xxx");

        final LearningTargetData m126m7t3 = m126m7.addLearningTarget(7, 3, "7.3", "ST57_3_HW",
                "xxx, including:",
                "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F01_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F02_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F03_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F04_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F05_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F06_01",
                        "xxx");
        m126m7t3.addExBlock("xxx")
                .addEx("ST57_3_F07_01",
                        "xxx");

        // Module 8

        final ModuleData m126m8 = m126.addModule(8, "Imaginary and Complex Numbers", "SR58_HW", "c58-thumb.png");

        m126m8.skillsReview.addExampleBlock("xxx")
                .addEx("SR58_01_01",
                        "xxx");
        m126m8.skillsReview.addExampleBlock("xxx")
                .addEx("SR58_02_01",
                        "xxx");
        m126m8.skillsReview.addExampleBlock("xxx")
                .addEx("SR58_03_01",
                        "xxx");
        m126m8.skillsReview.addExampleBlock("xxx")
                .addEx("SR58_04_01",
                        "xxx");

        final LearningTargetData m126m8t1 = m126m8.addLearningTarget(8, 1, "8.1", "ST58_1_HW",
                "xxx, including:",
                "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F01_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F02_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F03_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F04_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F05_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F06_01",
                        "xxx");
        m126m8t1.addExBlock("xxx")
                .addEx("ST58_1_F07_01",
                        "xxx");

        final LearningTargetData m126m8t2 = m126m8.addLearningTarget(8, 2, "8.2", "ST58_2_HW",
                "xxx, including:",
                "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F01_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F02_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F03_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F04_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F05_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F06_01",
                        "xxx");
        m126m8t2.addExBlock("xxx")
                .addEx("ST58_2_F07_01",
                        "xxx");

        final LearningTargetData m126m8t3 = m126m8.addLearningTarget(8, 3, "8.3", "ST58_3_HW",
                "xxx, including:",
                "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F01_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F02_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F03_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F04_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F05_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F06_01",
                        "xxx");
        m126m8t3.addExBlock("xxx")
                .addEx("ST58_3_F07_01",
                        "xxx");

        // Module 9

        final ModuleData m126m9 = m126.addModule(9, "3D Cartesian Coordinates", "SR59_HW", "c59-thumb.png");

        m126m9.skillsReview.addExampleBlock("xxx")
                .addEx("SR59_01_01",
                        "xxx");
        m126m9.skillsReview.addExampleBlock("xxx")
                .addEx("SR59_02_01",
                        "xxx");
        m126m9.skillsReview.addExampleBlock("xxx")
                .addEx("SR59_03_01",
                        "xxx");
        m126m9.skillsReview.addExampleBlock("xxx")
                .addEx("SR59_04_01",
                        "xxx");

        final LearningTargetData m126m9t1 = m126m9.addLearningTarget(9, 1, "9.1", "ST59_1_HW",
                "xxx, including:",
                "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F01_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F02_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F03_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F04_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F05_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F06_01",
                        "xxx");
        m126m9t1.addExBlock("xxx")
                .addEx("ST59_1_F07_01",
                        "xxx");

        final LearningTargetData m126m9t2 = m126m9.addLearningTarget(9, 2, "9.2", "ST59_2_HW",
                "xxx, including:",
                "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F01_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F02_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F03_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F04_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F05_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F06_01",
                        "xxx");
        m126m9t2.addExBlock("xxx")
                .addEx("ST59_2_F07_01",
                        "xxx");

        final LearningTargetData m126m9t3 = m126m9.addLearningTarget(9, 3, "9.3", "ST59_3_HW",
                "xxx, including:",
                "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F01_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F02_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F03_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F04_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F05_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F06_01",
                        "xxx");
        m126m9t3.addExBlock("xxx")
                .addEx("ST59_3_F07_01",
                        "xxx");

        // Module 10

        final ModuleData m126m10 = m126.addModule(10, "Cylindrical and Spherical Coordinates", "SR60_HW",
                "c60-thumb.png");

        m126m10.skillsReview.addExampleBlock("xxx")
                .addEx("SR60_01_01",
                        "xxx");
        m126m10.skillsReview.addExampleBlock("xxx")
                .addEx("SR60_02_01",
                        "xxx");
        m126m10.skillsReview.addExampleBlock("xxx")
                .addEx("SR60_03_01",
                        "xxx");
        m126m10.skillsReview.addExampleBlock("xxx")
                .addEx("SR60_04_01",
                        "xxx");

        final LearningTargetData m126m10t1 = m126m10.addLearningTarget(10, 1, "10.1", "ST60_1_HW",
                "xxx, including:",
                "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F01_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F02_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F03_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F04_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F05_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F06_01",
                        "xxx");
        m126m10t1.addExBlock("xxx")
                .addEx("ST60_1_F07_01",
                        "xxx");

        final LearningTargetData m126m10t2 = m126m10.addLearningTarget(10, 2, "10.2", "ST60_2_HW",
                "xxx, including:",
                "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F01_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F02_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F03_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F04_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F05_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F06_01",
                        "xxx");
        m126m10t2.addExBlock("xxx")
                .addEx("ST60_2_F07_01",
                        "xxx");

        final LearningTargetData m126m10t3 = m126m10.addLearningTarget(10, 3, "10.3", "ST60_3_HW",
                "xxx, including:",
                "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F01_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F02_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F03_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F04_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F05_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F06_01",
                        "xxx");
        m126m10t3.addExBlock("xxx")
                .addEx("ST60_3_F07_01",
                        "xxx");

        return m126;
    }
}
