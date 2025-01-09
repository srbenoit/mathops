package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A utility class to work with lesson component records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawLessonComponentLogic {
    ;

    /** Label for overview video. */
    private static final String OVVID = "Instructor Lecture by Dr. Paul Kennedy";

    /** Label for overview video. */
    private static final String OVVID1 = "Instructor Lecture by Dr. Paul Kennedy, Part 1";

    /** Label for overview video. */
    private static final String OVVID2 = "Instructor Lecture by Dr. Paul Kennedy, Part 2";

    /** Label for overview video. */
    private static final String OVVID3 = "Instructor Lecture by Dr. Paul Kennedy, Part 3";

    /** Label for overview video. */
    private static final String OVVIDPRE = "Instructor Lecture by Dr. Paul Kennedy";

    /** Label for overview video. */
    private static final String OVVID101 = "Instructor Lecture";

    /** Label for overview PDF. */
    private static final String OVPDF = "Lecture Notes";

    /** Label for overview PDF. */
    private static final String OVPDF1 = "Lecture Notes, Part 1";

    /** Label for overview PDF. */
    private static final String OVPDF2 = "Lecture Notes, Part 2";

    /** Label for overview PDF. */
    private static final String OVPDF3 = "Printed Review Materials";

    /** Label for example a. */
    private static final String EXA = "Example (a)";

    /** Label for example b. */
    private static final String EXB = "Example (b)";

    /** Label for example c. */
    private static final String EXC = "Example (c)";

    /** Label for example 1. */
    private static final String EX1 = "Example 1";

    /** Label for example 2. */
    private static final String EX2 = "Example 2";

    /** Label for example 3. */
    private static final String EX3 = "Example 3";

    /** Label for example 4. */
    private static final String EX4 = "Example 4";

    /** Label for example 5. */
    private static final String EX5 = "Example 5";

    /** Label for example 6. */
    private static final String EX6 = "Example 6";

    /** Label for example 7. */
    private static final String EX7 = "Example 7";

    /** Label for example 8. */
    private static final String EX8 = "Example 8";

    /** Label for exercise 1. */
    private static final String TT1 = "Try This 1";

    /** Label for exercise 2. */
    private static final String TT2 = "Try This 2";

    /** Label for exercise 3. */
    private static final String TT3 = "Try This 3";

    /** Label for exercise 4. */
    private static final String TT4 = "Try This 4";

    /** Label for exercise 5. */
    private static final String TT5 = "Try This 5";

    /** Label for exercise 6. */
    private static final String TT6 = "Try This 6";

    /** Label for exercise 7. */
    private static final String TT7 = "Try This 7";

    /** Label for exercise 8. */
    private static final String TT8 = "Try This 8";

    /** Label. */
    private static final String OV = "Overview";

    /** Label. */
    private static final String O1_1 = "Objective 1.1";

    /** Label. */
    private static final String O1_2 = "Objective 1.2";

    /** Label. */
    private static final String O1_3 = "Objective 1.3";

    /** Label. */
    private static final String O1_4 = "Objective 1.4";

    /** Label. */
    private static final String O1_5 = "Objective 1.5";

    /** Label. */
    private static final String O1_6 = "Objective 1.6";

    /** Label. */
    private static final String O1_7 = "Objective 1.7";

    /** Label. */
    private static final String O1_8 = "Objective 1.8";

    /** Label. */
    private static final String O1_9 = "Objective 1.9";

    /** Label. */
    private static final String O1_10 = "Objective 1.10";

    /** Label. */
    private static final String O2_1 = "Objective 2.1";

    /** Label. */
    private static final String O2_2 = "Objective 2.2";

    /** Label. */
    private static final String O2_3 = "Objective 2.3";

    /** Label. */
    private static final String O2_4 = "Objective 2.4";

    /** Label. */
    private static final String O2_5 = "Objective 2.5";

    /** Label. */
    private static final String O2_6 = "Objective 2.6";

    /** Label. */
    private static final String O2_7 = "Objective 2.7";

    /** Label. */
    private static final String O2_8 = "Objective 2.8";

    /** Label. */
    private static final String O2_9 = "Objective 2.9";

    /** Label. */
    private static final String O2_10 = "Objective 2.10";

    /** Label. */
    private static final String O3_1 = "Objective 3.1";

    /** Label. */
    private static final String O3_2 = "Objective 3.2";

    /** Label. */
    private static final String O3_3 = "Objective 3.3";

    /** Label. */
    private static final String O3_4 = "Objective 3.4";

    /** Label. */
    private static final String O3_5 = "Objective 3.5";

    /** Label. */
    private static final String O3_6 = "Objective 3.6";

    /** Label. */
    private static final String O3_7 = "Objective 3.7";

    /** Label. */
    private static final String O3_8 = "Objective 3.8";

    /** Label. */
    private static final String O3_9 = "Objective 3.9";

    /** Label. */
    private static final String O3_10 = "Objective 3.10";

    /** Label. */
    private static final String O4_1 = "Objective 4.1";

    /** Label. */
    private static final String O4_2 = "Objective 4.2";

    /** Label. */
    private static final String O4_3 = "Objective 4.3";

    /** Label. */
    private static final String O4_4 = "Objective 4.4";

    /** Label. */
    private static final String O4_5 = "Objective 4.5";

    /** Label. */
    private static final String O4_6 = "Objective 4.6";

    /** Label. */
    private static final String O4_7 = "Objective 4.7";

    /** Label. */
    private static final String O4_8 = "Objective 4.8";

    /** Label. */
    private static final String O4_9 = "Objective 4.9";

    /** Label. */
    private static final String O4_10 = "Objective 4.10";

    /** A commonly used string. */
    private static final String SR_MAT = "Skills Review materials";

    /** The course ID of the model currently being constructed. */
    private static String courseId;

    /** The lesson ID of the model currently being constructed. */
    private static String lessonId;

    /** The unit number of the model currently being constructed. */
    private static int unit;

    /** The objective number of the model currently being constructed. */
    private static int objective;

    /** The sequence number of the model currently being constructed. */
    private static int seqNumber;

    /** The map of hard-coded lesson components. */
    private static final Map<String, Map<Integer, RawLessonComponent>> lessonComponents;

    static {
        final Collection<RawLessonComponent> lst = new ArrayList<>(4000);

        build117(lst);
        build1170(lst);
        build118(lst);
        build1180(lst);
        build124(lst);
        build1240(lst);
        build125(lst);
        build1250(lst);
        build126(lst);
        build1260(lst);

        //  ELM Tutorial
        courseId = RawRecordConstants.M100T;

        // 1:
        startLesson("M100T.11", 1, 1);
        lst.add(mkLH2(null, O1_1, "Recognize and Identify Number Systems"));
        lst.add(mkVid("MT11OV", OVVID101));
        lst.add(mkPdf("MT11OV", OVPDF));
        lst.add(mkETT("MT11E1", EX1));
        lst.add(mkETT("MT11E2", EX2));
        lst.add(mkETT("MT11T1", TT1));
        lst.add(mkETT("MT11T2", TT2));

        startLesson("M100T.12", 1, 2);
        lst.add(mkLH2(null, O1_2, "Identify Prime and Composite Numbers"));
        lst.add(mkVid("MT12OV", OVVID101));
        lst.add(mkPdf("MT12OV", OVPDF));
        lst.add(mkETT("MT12E1", EX1));
        lst.add(mkETT("MT12E2", EX2));
        lst.add(mkETT("MT12T1", TT1));
        lst.add(mkETT("MT12T2", TT2));

        startLesson("M100T.13", 1, 3);
        lst.add(mkLH2(null, O1_3, "Find the Greatest Common Divisor"));
        lst.add(mkVid("MT13OV", OVVID101));
        lst.add(mkPdf("MT13OV", OVPDF));
        lst.add(mkETT("MT13E1", EX1));
        lst.add(mkETT("MT13E2", EX2));
        lst.add(mkETT("MT13T1", TT1));
        lst.add(mkETT("MT13T2", TT2));

        startLesson("M100T.14", 1, 4);
        lst.add(mkLH2(null, O1_4, "Find the Least Common Denominator"));
        lst.add(mkVid("MT14OV", OVVID101));
        lst.add(mkPdf("MT14OV", OVPDF));
        lst.add(mkETT("MT14E1", EX1));
        lst.add(mkETT("MT14E2", EX2));
        lst.add(mkETT("MT14T1", TT1));
        lst.add(mkETT("MT14T2", TT2));

        startLesson("M100T.15", 1, 5);
        lst.add(mkLH2(null, O1_5, "Order Rational Numbers"));
        lst.add(mkVid("MT15OV", OVVID101));
        lst.add(mkPdf("MT15OV", OVPDF));
        lst.add(mkETT("MT15E1", EX1));
        lst.add(mkETT("MT15E2", EX2));
        lst.add(mkETT("MT15T1", TT1));
        lst.add(mkETT("MT15T2", TT2));

        startLesson("M100T.16", 1, 6);
        lst.add(mkLH2(null, O1_6, //
                "Understanding Positive and Negative Number Operations"));
        lst.add(mkVid("MT16OV", OVVID101));
        lst.add(mkPdf("MT16OV", OVPDF));
        lst.add(mkETT("MT16E1", EX1));
        lst.add(mkETT("MT16E2", EX2));
        lst.add(mkETT("MT16T1", TT1));
        lst.add(mkETT("MT16T2", TT2));

        startLesson("M100T.17", 1, 7);
        lst.add(mkLH2(null, O1_7, "Solve Conversions Problems"));
        lst.add(mkVid("MT17OV", OVVID101));
        lst.add(mkPdf("MT17OV", OVPDF));
        lst.add(mkETT("MT17E1", EX1));
        lst.add(mkETT("MT17E2", EX2));
        lst.add(mkETT("MT17T1", TT1));
        lst.add(mkETT("MT17T2", TT2));

        startLesson("M100T.18", 1, 8);
        lst.add(mkLH2(null, O1_8, "Solve Precentage Increase or Decrease Problems"));
        lst.add(mkVid("MT18OV", OVVID101));
        lst.add(mkPdf("MT18OV", OVPDF));
        lst.add(mkETT("MT18E1", EX1));
        lst.add(mkETT("MT18E2", EX2));
        lst.add(mkETT("MT18T1", TT1));
        lst.add(mkETT("MT18T2", TT2));

        startLesson("M100T.19", 1, 9);
        lst.add(mkLH2(null, O1_9, "Use Exponential Rules to Evaluate Expressions"));
        lst.add(mkVid("MT19OV", OVVID101));
        lst.add(mkPdf("MT19OV", OVPDF));
        lst.add(mkETT("MT19E1", EX1));
        lst.add(mkETT("MT19E2", EX2));
        lst.add(mkETT("MT19T1", TT1));
        lst.add(mkETT("MT19T2", TT2));

        startLesson("M100T.110", 1, 10);
        lst.add(mkLH2(null, O1_10, "Simplify Radical Expressions"));
        lst.add(mkVid("MT1AOV", OVVID101));
        lst.add(mkPdf("MT1AOV", OVPDF));
        lst.add(mkETT("MT1AE1", EX1));
        lst.add(mkETT("MT1AE2", EX2));
        lst.add(mkETT("MT1AT1", TT1));
        lst.add(mkETT("MT1AT2", TT2));

        // 2:
        startLesson("M100T.21", 2, 1);
        lst.add(mkLH2(null, O2_1, "Apply the Counting Principle"));
        lst.add(mkVid("MT21OV", OVVID101));
        lst.add(mkPdf("MT21OV", OVPDF));
        lst.add(mkETT("MT21E1", EX1));
        lst.add(mkETT("MT21E2", EX2));
        lst.add(mkETT("MT21T1", TT1));
        lst.add(mkETT("MT21T2", TT2));

        startLesson("M100T.22", 2, 2);
        lst.add(mkLH2(null, O2_2, "Understanding the Terminology of Algebra"));
        lst.add(mkVid("MT22OV", OVVID101));
        lst.add(mkPdf("MT22OV", OVPDF));
        lst.add(mkETT("MT22E1", EX1));
        lst.add(mkETT("MT22E2", EX2));
        lst.add(mkETT("MT22T1", TT1));
        lst.add(mkETT("MT22T2", TT2));

        startLesson("M100T.23", 2, 3);
        lst.add(mkLH2(null, O2_3, "Simplify Algebraic Expressions"));
        lst.add(mkVid("MT23OV", OVVID101));
        lst.add(mkPdf("MT23OV", OVPDF));
        lst.add(mkETT("MT23E1", EX1));
        lst.add(mkETT("MT23E2", EX2));
        lst.add(mkETT("MT23T1", TT1));
        lst.add(mkETT("MT23T2", TT2));

        startLesson("M100T.24", 2, 4);
        lst.add(mkLH2(null, O2_4, "Solve Literal Equations"));
        lst.add(mkVid("MT24OV", OVVID101));
        lst.add(mkPdf("MT24OV", OVPDF));
        lst.add(mkETT("MT24E1", EX1));
        lst.add(mkETT("MT24E2", EX2));
        lst.add(mkETT("MT24T1", TT1));
        lst.add(mkETT("MT24T2", TT2));

        startLesson("M100T.25", 2, 5);
        lst.add(mkLH2(null, O2_5, "Multiply Binomial Expressions"));
        lst.add(mkVid("MT25OV", OVVID101));
        lst.add(mkPdf("MT25OV", OVPDF));
        lst.add(mkETT("MT25E1", EX1));
        lst.add(mkETT("MT25E2", EX2));
        lst.add(mkETT("MT25T1", TT1));
        lst.add(mkETT("MT25T2", TT2));

        startLesson("M100T.26", 2, 6);
        lst.add(mkLH2(null, O2_6, "Factor Quadratic Equations"));
        lst.add(mkVid("MT26OV", OVVID101));
        lst.add(mkPdf("MT26OV", OVPDF));
        lst.add(mkETT("MT26E1", EX1));
        lst.add(mkETT("MT26E2", EX2));
        lst.add(mkETT("MT26T1", TT1));
        lst.add(mkETT("MT26T2", TT2));

        startLesson("M100T.27", 2, 7);
        lst.add(mkLH2(null, O2_7, "Solve Linear Word Problems"));
        lst.add(mkVid("MT27OV", OVVID101));
        lst.add(mkPdf("MT27OV", OVPDF));
        lst.add(mkETT("MT27E1", EX1));
        lst.add(mkETT("MT27E2", EX2));
        lst.add(mkETT("MT27T1", TT1));
        lst.add(mkETT("MT27T2", TT2));

        startLesson("M100T.28", 2, 8);
        lst.add(mkLH2(null, O2_8, "Use Function Notation"));
        lst.add(mkVid("MT28OV", OVVID101));
        lst.add(mkPdf("MT28OV", OVPDF));
        lst.add(mkETT("MT28E1", EX1));
        lst.add(mkETT("MT28E2", EX2));
        lst.add(mkETT("MT28T1", TT1));
        lst.add(mkETT("MT28T2", TT2));

        startLesson("M100T.29", 2, 9);
        lst.add(mkLH2(null, O2_9, "Determine the Slope of a Line (Graphical Approach)"));
        lst.add(mkVid("MT29OV", OVVID101));
        lst.add(mkPdf("MT29OV", OVPDF));
        lst.add(mkETT("MT29E1", EX1));
        lst.add(mkETT("MT29E2", EX2));
        lst.add(mkETT("MT29T1", TT1));
        lst.add(mkETT("MT29T2", TT2));

        startLesson("M100T.210", 2, 10);
        lst.add(mkLH2(null, O2_10, "Graph Lines (by Graphing Intersects)"));
        lst.add(mkVid("MT2AOV", OVVID101));
        lst.add(mkPdf("MT2AOV", OVPDF));
        lst.add(mkETT("MT2AE1", EX1));
        lst.add(mkETT("MT2AE2", EX2));
        lst.add(mkETT("MT2AT1", TT1));
        lst.add(mkETT("MT2AT2", TT2));

        // 3:
        startLesson("M100T.31", 3, 1);
        lst.add(mkLH2(null, O3_1, "Solve Direct and Indirect Variation Problems"));
        lst.add(mkVid("MT31OV", OVVID101));
        lst.add(mkPdf("MT31OV", OVPDF));
        lst.add(mkETT("MT31E1", EX1));
        lst.add(mkETT("MT31E2", EX2));
        lst.add(mkETT("MT31T1", TT1));
        lst.add(mkETT("MT31T2", TT2));

        startLesson("M100T.32", 3, 2);
        lst.add(mkLH2(null, O3_2, "Solve Absolute Value Inequalities"));
        lst.add(mkVid("MT32OV", OVVID101));
        lst.add(mkPdf("MT32OV", OVPDF));
        lst.add(mkETT("MT32E1", EX1));
        lst.add(mkETT("MT32E2", EX2));
        lst.add(mkETT("MT32T1", TT1));
        lst.add(mkETT("MT32T2", TT2));

        startLesson("M100T.33", 3, 3);
        lst.add(mkLH2(null, O3_3, "Identify Rational Functions"));
        lst.add(mkVid("MT33OV", OVVID101));
        lst.add(mkPdf("MT33OV", OVPDF));
        lst.add(mkETT("MT33E1", EX1));
        lst.add(mkETT("MT33E2", EX2));
        lst.add(mkETT("MT33T1", TT1));
        lst.add(mkETT("MT33T2", TT2));

        startLesson("M100T.34", 3, 4);
        lst.add(mkLH2(null, O3_4, "Determine the Domain and Range of Rational Functions"));
        lst.add(mkVid("MT34OV", OVVID101));
        lst.add(mkPdf("MT34OV", OVPDF));
        lst.add(mkETT("MT34E1", EX1));
        lst.add(mkETT("MT34E2", EX2));
        lst.add(mkETT("MT34T1", TT1));
        lst.add(mkETT("MT34T2", TT2));

        startLesson("M100T.35", 3, 5);
        lst.add(mkLH2(null, O3_5, "Identify Radical Functions"));
        lst.add(mkVid("MT35OV", OVVID101));
        lst.add(mkPdf("MT35OV", OVPDF));
        lst.add(mkETT("MT35E1", EX1));
        lst.add(mkETT("MT35E2", EX2));
        lst.add(mkETT("MT35T1", TT1));
        lst.add(mkETT("MT35T2", TT2));

        startLesson("M100T.36", 3, 6);
        lst.add(mkLH2(null, O3_6, "Determine the Domain and Range of Radical Functions"));
        lst.add(mkVid("MT36OV", OVVID101));
        lst.add(mkPdf("MT36OV", OVPDF));
        lst.add(mkETT("MT36E1", EX1));
        lst.add(mkETT("MT36E2", EX2));
        lst.add(mkETT("MT36T1", TT1));
        lst.add(mkETT("MT36T2", TT2));

        startLesson("M100T.37", 3, 7);
        lst.add(mkLH2(null, O3_7, "Apply the Pythagorean Relationship"));
        lst.add(mkVid("MT37OV", OVVID101));
        lst.add(mkPdf("MT37OV", OVPDF));
        lst.add(mkETT("MT37E1", EX1));
        lst.add(mkETT("MT37E2", EX2, false, true));
        lst.add(mkETT("MT37T1", TT1, false, true));
        lst.add(mkETT("MT37T2", TT1, false, true));

        startLesson("M100T.38", 3, 8);
        lst.add(mkLH2(null, O3_8, "Determine the Area of Polygons"));
        lst.add(mkVid("MT38OV", OVVID101));
        lst.add(mkPdf("MT38OV", OVPDF));
        lst.add(mkETT("MT38E1", EX1));
        lst.add(mkETT("MT38E2", EX2));
        lst.add(mkETT("MT38T1", TT1));
        lst.add(mkETT("MT38T2", TT2));

        startLesson("M100T.39", 3, 9);
        lst.add(mkLH2(null, O3_9, "Solve Similar Triangles Problems"));
        lst.add(mkVid("MT39OV", OVVID101));
        lst.add(mkPdf("MT39OV", OVPDF));
        lst.add(mkETT("MT39E1", EX1));
        lst.add(mkETT("MT39E2", EX2));
        lst.add(mkETT("MT39T1", TT1));
        lst.add(mkETT("MT39T2", TT2));

        // 4:
        startLesson("M100T.41", 4, 1);
        lst.add(mkLH2(null, O4_1, "Solve Literal Equations"));
        lst.add(mkVid("MT41OV", OVVID101));
        lst.add(mkPdf("MT41OV", OVPDF));
        lst.add(mkETT("MT41E1", EX1));
        lst.add(mkETT("MT41E2", EX2));
        lst.add(mkETT("MT41T1", TT1));
        lst.add(mkETT("MT41T2", TT2));

        startLesson("M100T.42", 4, 2);
        lst.add(mkLH2(null, O4_2, "Multiply Polynomials"));
        lst.add(mkVid("MT42OV", OVVID101));
        lst.add(mkPdf("MT42OV", OVPDF));
        lst.add(mkETT("MT42E1", EX1));
        lst.add(mkETT("MT42E2", EX2));
        lst.add(mkETT("MT42T1", TT1));
        lst.add(mkETT("MT42T2", TT2));

        startLesson("M100T.43", 4, 3);
        lst.add(mkLH2(null, O4_3, "Factor Polynomials"));
        lst.add(mkVid("MT43OV", OVVID101));
        lst.add(mkPdf("MT43OV", OVPDF));
        lst.add(mkETT("MT43E1", EX1));
        lst.add(mkETT("MT43E2", EX2));
        lst.add(mkETT("MT43T1", TT1));
        lst.add(mkETT("MT43T2", TT2));

        startLesson("M100T.44", 4, 4);
        lst.add(mkLH2(null, O4_4, "Determine the Slope of a Line (Algabraic Approach)"));
        lst.add(mkVid("MT44OV", OVVID101));
        lst.add(mkPdf("MT44OV", OVPDF));
        lst.add(mkETT("MT44E1", EX1));
        lst.add(mkETT("MT44E2", EX2));
        lst.add(mkETT("MT44T1", TT1));
        lst.add(mkETT("MT44T2", TT2));

        startLesson("M100T.45", 4, 5);
        lst.add(mkLH2(null, O4_5, "Write the Slope-Intercept Form of a Line"));
        lst.add(mkVid("MT45OV", OVVID101));
        lst.add(mkPdf("MT45OV", OVPDF));
        lst.add(mkETT("MT45E1", EX1));
        lst.add(mkETT("MT45E2", EX2));
        lst.add(mkETT("MT45T1", TT1));
        lst.add(mkETT("MT45T2", TT2));

        startLesson("M100T.46", 4, 6);
        lst.add(mkLH2(null, O4_6, "Determine the Equation of Parallel Lines"));
        lst.add(mkVid("MT46OV", OVVID101));
        lst.add(mkPdf("MT46OV", OVPDF));
        lst.add(mkETT("MT46E1", EX1));
        lst.add(mkETT("MT46E2", EX2));
        lst.add(mkETT("MT46T1", TT1));
        lst.add(mkETT("MT46T2", TT2));

        startLesson("M100T.47", 4, 7);
        lst.add(mkLH2(null, O4_7, "Determine the Equation of Perpendicular Lines"));
        lst.add(mkVid("MT47OV", OVVID101));
        lst.add(mkPdf("MT47OV", OVPDF));
        lst.add(mkETT("MT47E1", EX1));
        lst.add(mkETT("MT47E2", EX2));
        lst.add(mkETT("MT47T1", TT1));
        lst.add(mkETT("MT47T2", TT2));

        startLesson("M100T.48", 4, 8);
        lst.add(mkLH2(null, O4_8, "Add and Subtract Rational Expressions"));
        lst.add(mkVid("MT48OV", OVVID101));
        lst.add(mkPdf("MT48OV", OVPDF));
        lst.add(mkETT("MT48E1", EX1));
        lst.add(mkETT("MT48E2", EX2));
        lst.add(mkETT("MT48T1", TT1));
        lst.add(mkETT("MT48T2", TT2));

        startLesson("M100T.49", 4, 9);
        lst.add(mkLH2(null, O4_9, "Multiply and Divide Rational Expressions"));
        lst.add(mkVid("MT49OV", OVVID101));
        lst.add(mkPdf("MT49OV", OVPDF));
        lst.add(mkETT("MT49E1", EX1));
        lst.add(mkETT("MT49E2", EX2));
        lst.add(mkETT("MT49T1", TT1));
        lst.add(mkETT("MT49T2", TT2));

        startLesson("M100T.410", 4, 10);
        lst.add(mkLH2(null, O4_10, "Solve Rational Equations"));
        lst.add(mkVid("MT4AOV", OVVID101));
        lst.add(mkPdf("MT4AOV", OVPDF));
        lst.add(mkETT("MT4AE1", EX1));
        lst.add(mkETT("MT4AE2", EX2));
        lst.add(mkETT("MT4AT1", TT1));
        lst.add(mkETT("MT4AT2", TT2));

        startLesson("M100T.411", 4, 11);
        lst.add(mkLH2(null, "Objective 4.11", "Solve Radical Equations"));
        lst.add(mkVid("MT4BOV", OVVID101));
        lst.add(mkPdf("MT4BOV", OVPDF));
        lst.add(mkETT("MT4BE1", EX1));
        lst.add(mkETT("MT4BE2", EX2));
        lst.add(mkETT("MT4BT1", TT1));
        lst.add(mkETT("MT4BT2", TT2));

        // MPE Review
        courseId = "M 100R";
        final String prefix = courseId.replace("M ", "M");

        // 1: Algebra
        startLesson(prefix + ".11", 1, 1);
        // lst.add(mkPrePdf("algebra", OVPDF3));
        lst.add(mkLH2("Section 1: Algebra", "Topic 1", "Polynomial Manipulation"));
        lst.add(mkPdf("A1", OVPDF3));
        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("Section 1: Algebra", "Topic 2", "Solving One Linear Equation in One Unknown"));
        lst.add(mkPdf("A2", OVPDF3));
        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("Section 1: Algebra", "Topic 3", "Mathematical Models"));
        lst.add(mkPdf("A3", OVPDF3));
        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("Section 1: Algebra", "Topic 4", "Inequalities/Absolute Values"));
        lst.add(mkPdf("A4", OVPDF3));
        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("Section 1: Algebra", "Topic 5", "Graphing on the Plane"));
        lst.add(mkPdf("A5", OVPDF3));
        startLesson(prefix + ".16", 1, 6);
        lst.add(mkLH2("Section 1: Algebra", "Topic 6", "Lines in the Plane"));
        lst.add(mkPdf("A6", OVPDF3));
        startLesson(prefix + ".17", 1, 7);
        lst.add(mkLH2("Section 1: Algebra", "Topic 7", "Systems of Linear Equations"));
        lst.add(mkPdf("A7", OVPDF3));
        startLesson(prefix + ".18", 1, 8);
        lst.add(mkLH2("Section 1: Algebra", "Topic 8", "Distance Formula"));
        lst.add(mkPdf("A8", OVPDF3));
        startLesson(prefix + ".19", 1, 9);
        lst.add(mkLH2("Section 1: Algebra", "Topic 9", "Equation of a Circle"));
        lst.add(mkPdf("A9", OVPDF3));
        startLesson(prefix + ".110", 1, 10);
        lst.add(mkLH2("Section 1: Algebra", "Topic 10", "Piecewise Functions"));
        lst.add(mkPdf("A10", OVPDF3));
        startLesson(prefix + ".111", 1, 11);
        lst.add(mkLH2("Section 1: Algebra", "Topic 11", "Solving Polynomials by Factoring"));
        lst.add(mkPdf("A11", OVPDF3));
        startLesson(prefix + ".112", 1, 12);
        lst.add(mkLH2("Section 1: Algebra", "Topic 12", "Complex Numbers"));
        lst.add(mkPdf("A12", OVPDF3));
        startLesson(prefix + ".113", 1, 13);
        lst.add(mkLH2("Section 1: Algebra", "Topic 13", "Quadratic Formula"));
        lst.add(mkPdf("A13", OVPDF3));
        startLesson(prefix + ".114", 1, 14);
        lst.add(mkLH2("Section 1: Algebra", "Topic 14", "Completing the Square"));
        lst.add(mkPdf("A14", OVPDF3));
        startLesson(prefix + ".115", 1, 15);
        lst.add(mkLH2("Section 1: Algebra", "Topic 15", "Constructing Polynomials"));
        lst.add(mkPdf("A15", OVPDF3));
        startLesson(prefix + ".116", 1, 16);
        lst.add(mkLH2("Section 1: Algebra", "Topic 16", "Algebraic Fractions"));
        lst.add(mkPdf("A16", OVPDF3));
        startLesson(prefix + ".117", 1, 17);
        lst.add(mkLH2("Section 1: Algebra", "Topic 17", "Integer Exponents"));
        lst.add(mkPdf("A17", OVPDF3));
        startLesson(prefix + ".118", 1, 18);
        lst.add(mkLH2("Section 1: Algebra", "Topic 18", "Rational Exponents and Radicals"));
        lst.add(mkPdf("A18", OVPDF3));
        startLesson(prefix + ".119", 1, 19);
        lst.add(mkLH2("Section 1: Algebra", "Topic 19", "Solving Radical Equations"));
        lst.add(mkPdf("A19", OVPDF3));
        startLesson(prefix + ".120", 1, 20);
        lst.add(mkLH2("Section 1: Algebra", "Topic 20", "Power Functions"));
        lst.add(mkPdf("A20", OVPDF3));

        // 2: Trigonometry
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPrePdf("trigonometry", OVPDF3));
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 1", "Angles"));
        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 2", "Arc Length and Area of a Sector"));
        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 3", "The Trigonometric Functions"));
        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 4", "Solving Right Triangles"));
        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 5", "Solving Oblique Triangles"));
        startLesson(prefix + ".26", 2, 6);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 6", "Narrative Problems"));
        startLesson(prefix + ".27", 2, 7);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 7", "Graphing Trigonometric Functions"));
        startLesson(prefix + ".28", 2, 8);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 8", "Inverse Trigonometric Functions"));
        startLesson(prefix + ".29", 2, 9);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 9", "Basic Trigonometric Identities"));
        startLesson(prefix + ".210", 2, 10);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 10", "Negative-Angle and Cofunction Identities"));
        startLesson(prefix + ".211", 2, 11);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 11", "Sum and Difference Identities"));
        startLesson(prefix + ".212", 2, 12);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 12", "Double-Angle and Half-Angle Identities"));
        startLesson(prefix + ".213", 2, 13);
        lst.add(mkLH2("Section 2: Trigonometry", "Topic 13", "Trigonometric Equations"));

        // 3: Functions
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPrePdf("logarithms", OVPDF3));
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 1", "Functions and Graphs"));
        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 2", "Exponential Functions"));
        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 3", "Basic Operations on Functions"));
        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 4", "Composition and Inverse Functions"));
        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 5", "Logarithmic Functions"));
        startLesson(prefix + ".36", 3, 6);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 6", "Solving Equations"));
        startLesson(prefix + ".37", 3, 7);
        lst.add(mkLH2("Section 3: Functions and Graphs", "Topic 7", "Mathematical Models"));

        lessonComponents = new HashMap<>(200);

        for (final RawLessonComponent lcomp : lst) {
            final String lessId = lcomp.lessonId;
            final Integer seq = Integer.valueOf(lcomp.seqNbr.intValue());

            final Map<Integer, RawLessonComponent> inner = lessonComponents.computeIfAbsent(lessId,
                    s -> new TreeMap<>());

            if (inner.get(seq) != null) {
                Log.warning("Duplicate ", lessId, CoreConstants.DOT, seq);
            }

            inner.put(seq, lcomp);
        }
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawLessonComponent record) {

        Log.warning("Unable to insert lesson_component record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawLessonComponent record) {

        Log.warning("Unable to delete lesson_component record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawLessonComponent> queryAll(final Cache cache) {

        final List<RawLessonComponent> result = new ArrayList<>(0);

        Log.warning("Unable to query all lesson_component records: no underlying table.");

        return result;
    }

    /**
     * Queries all components for a particular lesson. Results are ordered by sequence number.
     *
     * @param theLessonId the ID of the lesson to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    public static List<RawLessonComponent> queryByLesson(final String theLessonId) {

        final List<RawLessonComponent> result;
        final Map<Integer, RawLessonComponent> inner = lessonComponents.get(theLessonId);

        if (inner == null) {
            result = new ArrayList<>(0);
        } else {
            result = new ArrayList<>(inner.values());
        }

        return result;
    }

    /**
     * Builds lesson components for MATH 117.
     *
     * @param lst the list to which to add components
     */
    private static void build117(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M117;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Add and subtract fractions."));
        lst.add(mkETT("2P1a", EXA));
        lst.add(mkETT("2P1b", EXB));
        lst.add(mkETT("2P1c", EXC));
        lst.add(mkSEC("2", "Multiply and divide fractions."));
        lst.add(mkETT("2P2a", EXA));
        lst.add(mkETT("2P2b", EXB));
        lst.add(mkETT("2P2c", EXC));
        lst.add(mkSEC("3", "Solve equations."));
        lst.add(mkETT("2P3a", EXA));
        lst.add(mkETT("2P3b", EXB));
        lst.add(mkSEC("4", "Use interval notion to describe each situation."));
        lst.add(mkETT("2P4a", EXA));
        lst.add(mkETT("2P4b", EXB));
        lst.add(mkETT("2P4c", EXC));
        lst.add(mkSEC("5", "Solve inequalities using addition and subtraction."));
        lst.add(mkETT("2P5a", EXA));
        lst.add(mkETT("2P5b", EXB));
        lst.add(mkSEC("6", "Solve inequalities using multiplication and division."));
        lst.add(mkETT("2P6a", EXA));
        lst.add(mkETT("2P6b", EXB));
        lst.add(mkETT("2P6c", EXC));
        lst.add(mkSEC("7", "Multiply binomials."));
        lst.add(mkETT("2P7a", EXA));
        lst.add(mkETT("2P7b", EXB));
        lst.add(mkETT("2P7c", EXC));
        lst.add(mkSEC("8", "Factor."));
        lst.add(mkETT("2P8a", EXA));
        lst.add(mkETT("2P8b", EXB));
        lst.add(mkETT("2P8c", EXC));
        lst.add(mkSEC("9", "Simplify expressions with exponents."));
        lst.add(mkETT("2P9a", EXA));
        lst.add(mkETT("2P9b", EXB));
        lst.add(mkSEC("10", "Simplify radicals."));
        lst.add(mkETT("2P10a", EXA));
        lst.add(mkETT("2P10b", EXB));

        // 1: Linear Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkLH2("1: Linear Functions", O1_1, "Generalize linear equations from tables and graphs"));
        lst.add(mkVid("211OV", OVVID));
        lst.add(mkPdf("211OV", OVPDF));
        lst.add(mkVid("IV1.1.1", "Supplemental: Using Verbal Descriptions to Write Linear Equations"));
        lst.add(mkVid("IV1.1.2", "Supplemental: Using Tables to Determine Linear Equations"));
        lst.add(mkVid("IV1.1.3", "Supplemental: Using Graphs to Determine Linear Equations"));
        lst.add(mkVid("TV1.1", "Technology Supplement: Using Technology to Verify Linear Functions"));

        lst.add(mkSEC("1.1.1", "Given a verbal description, write a linear equation."));
        lst.add(mkETT("2111E1", EX1));
        lst.add(mkETT("2111E2", EX2));
        lst.add(mkETT("2111T1", TT1));
        lst.add(mkETT("2111T2", TT2));
        lst.add(mkSEC("1.1.2", "Given a table, write a linear equation."));
        lst.add(mkETT("2112E1", EX1));
        lst.add(mkETT("2112E2", EX2));
        lst.add(mkETT("2112E3", EX3));
        lst.add(mkETT("2112T1", TT1));
        lst.add(mkETT("2112T2", TT2));
        lst.add(mkETT("2112T3", TT3));
        lst.add(mkSEC("1.1.3", "Given a graph, write a linear equation."));
        lst.add(mkETT("2113E1", EX1));
        lst.add(mkETT("2113E2", EX2));
        lst.add(mkETT("2113T1", TT1));
        lst.add(mkETT("2113T2", TT2));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Linear Functions", O1_2, "Use function notation"));
        lst.add(mkVid("212OV", OVVID));
        lst.add(mkPdf("212OV", OVPDF));
        lst.add(mkVid("IV1.2.1", "Supplemental: Identity Functions"));
        lst.add(mkVid("IV1.2.2", "Supplemental: Evaluating Functions"));
        lst.add(mkVid("IV1.2.3", "Supplemental: Interpreting Functions from Graphs"));
        lst.add(mkVid("TV1.2", "Technology Supplement: Evaluating Functions on the Calculator"));

        lst.add(mkSEC("1.2.1", "Define and identify functions."));
        lst.add(mkETT("2121E1", EX1));
        lst.add(mkETT("2121E2", EX2));
        lst.add(mkETT("2121E3", EX3));
        lst.add(mkETT("2121T1", TT1));
        lst.add(mkETT("2121T2", TT2));
        lst.add(mkETT("2121T3", TT3));
        lst.add(mkSEC("1.2.2", "Evaluate and graph functions."));
        lst.add(mkETT("2122E1", EX1));
        lst.add(mkETT("2122E2", EX2));
        lst.add(mkETT("2122T1", TT1));
        lst.add(mkETT("2122T2", TT2));
        lst.add(mkSEC("1.2.3", "Interpret functions and their graphs."));
        lst.add(mkETT("2123E1", EX1));
        lst.add(mkETT("2123E2", EX2));
        lst.add(mkETT("2123E3", EX3));
        lst.add(mkETT("2123T1", TT1));
        lst.add(mkETT("2123T2", TT2));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Linear Functions", O1_3, "Determine the equation of the line given conditions"));
        lst.add(mkVid("213OV", OVVID));
        lst.add(mkPdf("213OV", OVPDF));
        lst.add(mkVid("IV1.3.1", "Supplemental: Slope-Intercept Form"));
        lst.add(mkVid("IV1.3.2", "Supplemental: Point-Slope Form"));
        lst.add(mkVid("IV1.3.3", "Supplemental: Parallel and Perpendicular Lines"));
        lst.add(mkVid("TV1.3", "Technology Supplement: Graphing Linear Equations"));

        lst.add(mkSEC("1.3.1", "Determine the equation of the line given conditions."));
        lst.add(mkETT("2131E1", EX1));
        lst.add(mkETT("2131E2", EX2));
        lst.add(mkETT("2131E3", EX3));
        lst.add(mkETT("2131T1", TT1));
        lst.add(mkETT("2131T2", TT2));
        lst.add(mkETT("2131T3", TT3));
        lst.add(mkSEC("1.3.2", "Use point-slope to determine the equation of the line."));
        lst.add(mkETT("2132E1", EX1));
        lst.add(mkETT("2132E2", EX2));
        lst.add(mkETT("2132T1", TT1));
        lst.add(mkETT("2132T2", TT2));
        lst.add(mkSEC("1.3.3", "Find equations of parallel and perpendicular lines."));
        lst.add(mkETT("2133E1", EX1));
        lst.add(mkETT("2133E2", EX2));
        lst.add(mkETT("2133T1", TT1));
        lst.add(mkETT("2133T2", TT2));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Linear Functions", O1_4, "Solve equations and inequalities"));
        lst.add(mkVid("214OV", OVVID));
        lst.add(mkPdf("214OV", OVPDF));
        lst.add(mkVid("IV1.4.1", "Supplemental: Solving Equations"));
        lst.add(mkVid("IV1.4.2", "Supplemental: Solving Inequalities"));
        lst.add(mkVid("IV1.4.3", "Supplemental: Solving Literal Equations and Inequalities"));
        lst.add(mkVid("TV1.4", "Technology Supplement: Solving Linear Inequalities with Technology"));

        lst.add(mkSEC("1.4.1", "Solve linear equations."));
        lst.add(mkETT("2141E1", EX1));
        lst.add(mkETT("2141E2", EX2));
        lst.add(mkETT("2141E3", EX3));
        lst.add(mkETT("2141E4", EX4));
        lst.add(mkETT("2141T1", TT1));
        lst.add(mkETT("2141T2", TT2));
        lst.add(mkETT("2141T3", TT3));
        lst.add(mkETT("2141T4", TT4));
        lst.add(mkSEC("1.4.2", "Solve linear inequalities."));
        lst.add(mkETT("2142E1", EX1));
        lst.add(mkETT("2142E2", EX2));
        lst.add(mkETT("2142E3", EX3));
        lst.add(mkETT("2142T1", TT1));
        lst.add(mkETT("2142T2", TT2));
        lst.add(mkSEC("1.4.3", "Solve literal equations."));
        lst.add(mkETT("2143E1", EX1));
        lst.add(mkETT("2143E2", EX2));
        lst.add(mkETT("2143E3", EX3));
        lst.add(mkETT("2143T1", TT1));
        lst.add(mkETT("2143T2", TT2));
        lst.add(mkETT("2143T3", TT3));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Linear Functions", O1_5, "Model with linear functions"));
        lst.add(mkVid("215OV", OVVID));
        lst.add(mkPdf("215OV", OVPDF));
        lst.add(mkVid("IV1.5.1", "Supplemental: Determining if a Data Set is Linear"));
        lst.add(mkVid("IV1.5.3p1", "Supplemental: Solve Applications of Linear Equations and Inequalities (part 1)"));
        lst.add(mkVid("IV1.5.3p2", "Supplemental: Solve Applications of Linear Equations and Inequalities (part 2)"));
        lst.add(mkVid("IV1.5.3p3", "Supplemental: Solve Applications of Linear Equations and Inequalities (part 3)"));
        lst.add(mkVid("TV1.5", "Technology Supplement: Linear Regression"));

        lst.add(mkSEC("1.5.1", "Determine if a data set is linear."));
        lst.add(mkETT("2151E1", EX1));
        lst.add(mkETT("2151E2", EX2));
        lst.add(mkETT("2151T1", TT1));
        lst.add(mkETT("2151T2", TT2));
        lst.add(mkSEC("1.5.2", "Given a data set determine a trend line and make predictions."));
        lst.add(mkETT("2152E1", EX1));
        lst.add(mkETT("2152T1", TT1));

        // 2: Absolute Value and Piecewise-Defined Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_1, "Graph absolute functions"));
        lst.add(mkVid("221OV", OVVID));
        lst.add(mkPdf("221OV", OVPDF));
        lst.add(mkVid("IV2.1.1", "Supplemental: Sketching Graphs of Absolute Value Functions"));
        lst.add(mkVid("IV2.1.2", "Supplemental: Determining the Equation from the Graph"));
        lst.add(mkVid("TV2.1", "Technology Supplement: Graphing and Evaluating Absolute Value Functions"));

        lst.add(mkSEC("2.1.1", "Given an absolute value function, sketch its graph."));
        lst.add(mkETT("2211E1", EX1));
        lst.add(mkETT("2211E2", EX2));
        lst.add(mkETT("2211T1", TT1));
        lst.add(mkETT("2211T2", TT2));
        lst.add(mkSEC("2.1.2", "Given the graph of an absolute value function, determine its equation."));
        lst.add(mkETT("2212E1", EX1));
        lst.add(mkETT("2212E2", EX2));
        lst.add(mkETT("2212T1", TT1));
        lst.add(mkETT("2212T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_2, "Graph piecewise defined functions"));
        lst.add(mkVid("222OV", OVVID));
        lst.add(mkPdf("222OV", OVPDF));
        lst.add(mkVid("IV2.2.1", "Supplemental: Evaluating Piecewise-Defined Functions"));
        lst.add(mkVid("IV2.1.2", "Supplemental: Determining the Function from the Graph"));
        lst.add(mkVid("TV2.2", "Technology Supplement: Graphing and Evaluating Piecewise-Defined Functions"));

        lst.add(mkSEC("2.2.1", "Graph and interpret piecewise-defined functions."));
        lst.add(mkETT("2221E1", EX1));
        lst.add(mkETT("2221E2", EX2));
        lst.add(mkETT("2221T1", TT1));
        lst.add(mkETT("2221T2", TT2));
        lst.add(mkSEC("2.2.2", "Given a piecewise-defined function determine the rules and domains."));
        lst.add(mkETT("2222E1", EX1));
        lst.add(mkETT("2222E2", EX2));
        lst.add(mkETT("2222T1", TT1));
        lst.add(mkETT("2222T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_3, "Solve absolute value equations"));
        lst.add(mkVid("223OV", OVVID));
        lst.add(mkPdf("223OV", OVPDF));
        lst.add(mkVid("IV2.3.1", "Supplemental: Solving Absolute Value Equations |u| = a"));
        lst.add(mkVid("IV2.3.2", "Supplemental: Solving Absolute Value Equations |u| = linear function"));
        lst.add(mkVid("TV2.3", "Technology Supplement: Finding Intersections of Linear and Absolute Value Functions"));

        lst.add(mkSEC("2.3.1", "Develop and apply methods to solve absolute value equations of the form "
                               + "<em>|u| = a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2231E1", EX1));
        lst.add(mkETT("2231E2", EX2));
        lst.add(mkETT("2231T1", TT1));
        lst.add(mkETT("2231T2", TT2));
        lst.add(mkSEC("2.3.2", "Develop and apply methods to solve absolute value equations of the form "
                               + "<em>|u| = v</em> where <em>v</em> is an expression in <em>x</em>."));
        lst.add(mkETT("2232E1", EX1));
        lst.add(mkETT("2232E2", EX2));
        lst.add(mkETT("2232T1", TT1));
        lst.add(mkETT("2232T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_4, "Solve absolute value inequalities"));
        lst.add(mkVid("224OV", OVVID));
        lst.add(mkPdf("224OV", OVPDF));
        lst.add(mkVid("IV2.4.1", "Supplemental: Solving Absolute Value Inequalities |u| < a"));
        lst.add(mkVid("IV2.4.2", "Supplemental: Solving Absolute Value Inequalities |u| > a"));
        lst.add(mkVid("TV2.4", "Technology Supplement: Solving Absolute Value Inequalities"));

        lst.add(mkSEC("2.4.1", "Develop and apply methods to solve absolute value inequalities of the form "
                               + "<em>|u| < a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2241E1", EX1));
        lst.add(mkETT("2241E2", EX2));
        lst.add(mkETT("2241T1", TT1));
        lst.add(mkETT("2241T2", TT2));
        lst.add(mkSEC("2.4.2", "Develop and apply methods to solve absolute value inequalities of the form "
                               + "<em>|u| > a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2242E1", EX1));
        lst.add(mkETT("2242E2", EX2));
        lst.add(mkETT("2242T1", TT1));
        lst.add(mkETT("2242T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_5,
                "Model with absolute value and piecewise defined functions"));
        lst.add(mkVid("225OV", OVVID));
        // lst.add(mkPdf("225OV", OVPDF));
        lst.add(mkVid("IV2.5.1", "Supplemental: Modeling with Absolute Value Functions"));
        lst.add(mkVid("IV2.5.2", "Supplemental: Modeling with Piecewise-Defined Functions"));
        lst.add(mkVid("TV2.5", "Technology Supplement: Verifying Models"));

        lst.add(mkSEC("2.5.1", "Model and interpret situations with absolute value functions."));
        lst.add(mkETT("2251E1", EX1));
        lst.add(mkETT("2251E2", EX2));
        lst.add(mkETT("2251T1", TT1));
        lst.add(mkETT("2251T2", TT2));
        lst.add(mkSEC("2.5.2", "Model and interpret situations with piecewise-defined functions."));
        lst.add(mkETT("2252E1", EX1));
        lst.add(mkETT("2252E2", EX2));
        lst.add(mkETT("2252T1", TT1));
        lst.add(mkETT("2252T2", TT2));

        // 3: Quadratic Relations and Functions
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_1, "Graph quadratic functions"));
        lst.add(mkVid("231OV", OVVID));
        lst.add(mkPdf("231OV", OVPDF));
        lst.add(mkVid("IV3.1.1", "Supplemental: Finding the Vertex from a Graph"));
        lst.add(mkVid("IV3.1.2", "Supplemental: Finding the Vertex by Completing the Square"));
        lst.add(mkVid("IV3.1.3", "Supplemental: Solving Quadratic Applications"));
        lst.add(mkVid("TV3.1", "Technology Supplement: Graphing Quadratic Functions"));

        lst.add(mkSEC("3.1.1", "Given the graph of a quadratic function, determine its equation."));
        lst.add(mkETT("2311E1", EX1));
        lst.add(mkETT("2311E2", EX2));
        lst.add(mkETT("2311T1", TT1));
        lst.add(mkETT("2311T2", TT2));
        lst.add(mkSEC("3.1.2", "Write and graph quadratic functions in vertex form and determine the domain, range, "
                               + "and where increasing or decreasing."));
        lst.add(mkETT("2312E1", EX1));
        lst.add(mkETT("2312E2", EX2));
        lst.add(mkETT("2312T1", TT1));
        lst.add(mkETT("2312T2", TT2));
        lst.add(mkSEC("3.1.3", "Solve quadratic applications."));
        lst.add(mkETT("2313E1", EX1));
        lst.add(mkETT("2313E2", EX2));
        lst.add(mkETT("2313T1", TT1));
        lst.add(mkETT("2313T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_2, "Solve quadratic equations"));
        lst.add(mkVid("232OV", OVVID));
        lst.add(mkPdf("232OV", OVPDF));
        lst.add(mkVid("IV3.2.1", "Supplemental: Solve Quadratics by Factoring or Grouping"));
        lst.add(mkVid("IV3.2.2", "Supplemental: Completing the Square and the Quadratic Formula"));
        lst.add(mkVid("TV3.2", "Technology Supplement: Verifying Factors and Complex Zeros"));

        lst.add(mkSEC("3.2.1", "Solve quadratic equations by factoring and using technology."));
        lst.add(mkETT("2321E1", EX1));
        lst.add(mkETT("2321E2", EX2));
        lst.add(mkETT("2321T1", TT1));
        lst.add(mkETT("2321T2", TT2));
        lst.add(mkSEC("3.2.2", "Solve quadratic equations by completing the square and with the quadratic formula."));
        lst.add(mkETT("2322E1", EX1));
        lst.add(mkETT("2322E2", EX2));
        lst.add(mkETT("2322T1", TT1));
        lst.add(mkETT("2322T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_3, "Solve quadratic inequalities"));
        lst.add(mkVid("233OV", OVVID));
        lst.add(mkPdf("233OV", OVPDF));
        lst.add(mkSEC("3.3.1", "Develop and apply methods to solve quadratic inequalities."));
        lst.add(mkETT("2331E1", EX1));
        lst.add(mkETT("2331E2", EX2));
        lst.add(mkETT("2331E3", EX3));
        lst.add(mkETT("2331T1", TT1));
        lst.add(mkETT("2331T2", TT2));
        lst.add(mkETT("2331T3", TT3));
        lst.add(mkSEC("3.3.2", "Solve quadratic inequality applications."));
        lst.add(mkETT("2332E1", EX1));
        lst.add(mkETT("2332E2", EX2));
        lst.add(mkETT("2332T1", TT1));
        lst.add(mkETT("2332T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_4, "Graph circles and ellipses"));
        lst.add(mkVid("234OV", OVVID));
        lst.add(mkPdf("234OV", OVPDF));
        lst.add(mkSEC("3.4.1", "Determine the distance between two points."));
        lst.add(mkETT("2341E1", EX1));
        lst.add(mkETT("2341E2", EX2));
        lst.add(mkETT("2341T1", TT1));
        lst.add(mkETT("2341T2", TT2));
        lst.add(mkSEC("3.4.2", "Graph and interpret graphs of the form "
                               + "(x - h)<sup>2</sup> + (y - k)<sup>2</sup> = r<sup>2</sup>."));
        lst.add(mkETT("2342E1", EX1));
        lst.add(mkETT("2342E2", EX2));
        lst.add(mkETT("2342T1", TT1));
        lst.add(mkETT("2342T2", TT2));
        lst.add(mkSEC("3.4.3", "Graph and interpret graphs of the form "
                               + "[(x - h)<sup>2</sup> / a<sup>2</sup>] + [(y - k)<sup>2</sup> / b<sup>2</sup>] = 1."));
        lst.add(mkETT("2343E1", EX1));
        lst.add(mkETT("2343E2", EX2));
        lst.add(mkETT("2343T1", TT1));
        lst.add(mkETT("2343T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_5, "Model with quadratic functions and relations"));
        lst.add(mkVid("235OV", OVVID));
        lst.add(mkPdf("235OV", OVPDF));
        lst.add(mkVid("IV3.5.1", "Supplemental: Solve Applications Involving Quadratic Functions"));
        lst.add(mkVid("IV3.5.2", "Supplemental: Average Rate of Change and Instantaneous Velocity"));
        lst.add(mkVid("TV3.5", "Technology Supplement: Verifying Results of Application Problems"));

        lst.add(mkSEC("3.5.1", "Solve applications."));
        lst.add(mkETT("2351E1", EX1));
        lst.add(mkETT("2351E2", EX2));
        lst.add(mkETT("2351T1", TT1));
        lst.add(mkETT("2351T2", TT2));
        lst.add(mkSEC("3.5.2", "Determine average and instantaneous rates of change."));
        lst.add(mkETT("2352E1", EX1));
        lst.add(mkETT("2352E2", EX2));
        lst.add(mkETT("2352T1", TT1));
        lst.add(mkETT("2352T2", TT2));
        lst.add(mkSEC("3.5.3", "Model and interpret situations with quadratic functions."));
        lst.add(mkETT("2353E1", EX1));
        lst.add(mkETT("2353E2", EX2));
        lst.add(mkETT("2353T1", TT1));
        lst.add(mkETT("2353T2", TT2));

        // 4: Systems of Equations and Inequalities
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_1, "Solve systems of linear equations"));
        lst.add(mkVid("241OV", OVVID));
        lst.add(mkPdf("241OV", OVPDF));
        lst.add(mkVid("IV4.1.1", "Supplemental: Solving Systems with Substitution"));
        lst.add(mkVid("IV4.1.2", "Supplemental: Solving Systems with Elimination"));
        lst.add(mkVid("TV4.1", "Technology Supplement: Verifying Solutions to Systems Graphically"));

        lst.add(mkSEC("4.1.1", "Solve systems using substitution."));
        lst.add(mkETT("2411E1", EX1));
        lst.add(mkETT("2411E2", EX2));
        lst.add(mkETT("2411T1", TT1));
        lst.add(mkETT("2411T2", TT2));
        lst.add(mkSEC("4.1.2", "Solve systems using elimination."));
        lst.add(mkETT("2412E1", EX1));
        lst.add(mkETT("2412E2", EX2));
        lst.add(mkETT("2412E3", EX3));
        lst.add(mkETT("2412T1", TT1));
        lst.add(mkETT("2412T2", TT2));
        lst.add(mkETT("2412T3", TT3));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_2, "Solve systems using matrices"));
        lst.add(mkVid("242OV", OVVID));
        lst.add(mkPdf("242OV", OVPDF));
        lst.add(mkVid("IV4.2.1", "Supplemental: Solving Systems using Row Transformations"));
        lst.add(mkVid("IV4.2.2", "Supplemental: Reduced Row Echelon Form"));
        lst.add(mkVid("TV4.2", "Technology Supplement: Solving Sustems using Matrices and RREF"));

        lst.add(mkSEC("4.2.1", "Solve two-variable systems using row reduction."));
        lst.add(mkETT("2421E1", EX1));
        lst.add(mkETT("2421E2", EX2));
        lst.add(mkETT("2421E3", EX3));
        lst.add(mkETT("2421T1", TT1));
        lst.add(mkETT("2421T2", TT2));
        lst.add(mkETT("2421T3", TT3));
        lst.add(mkSEC("4.2.2", "Solve multi-variable systems using matrices."));
        lst.add(mkETT("2422E1", EX1));
        lst.add(mkETT("2422E2", EX2));
        lst.add(mkETT("2422T1", TT1));
        lst.add(mkETT("2422T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_3, "Solve systems of linear inequalities"));
        lst.add(mkVid("243OV", OVVID));
        lst.add(mkPdf("243OV", OVPDF));
        lst.add(mkSEC("4.3.1", "Solve linear inequalities."));
        lst.add(mkETT("2431E1", EX1));
        lst.add(mkETT("2431E2", EX2));
        lst.add(mkETT("2431T1", TT1));
        lst.add(mkETT("2431T2", TT2));
        lst.add(mkSEC("4.3.2", "Solve systems of linear inequalities."));
        lst.add(mkETT("2432E1", EX1));
        lst.add(mkETT("2432E2", EX2));
        lst.add(mkETT("2432T1", TT1));
        lst.add(mkETT("2432T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_4,
                "Solve systems with other functions and relations"));
        lst.add(mkVid("244OV", OVVID));
        lst.add(mkPdf("244OV", OVPDF));
        lst.add(mkSEC("4.4.1", "Solve nonlinear systems of equations."));
        lst.add(mkETT("2441E1", EX1));
        lst.add(mkETT("2441E2", EX2));
        lst.add(mkETT("2441E3", EX3));
        lst.add(mkETT("2441T1", TT1));
        lst.add(mkETT("2441T2", TT2));
        lst.add(mkETT("2441T3", TT3));
        lst.add(mkSEC("4.4.2", "Solve nonlinear systems of inequalities."));
        lst.add(mkETT("2442E1", EX1));
        lst.add(mkETT("2442E2", EX2));
        lst.add(mkETT("2442T1", TT1));
        lst.add(mkETT("2442T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_5, "Model with systems"));
        lst.add(mkVid("245OV", OVVID));
        // lst.add(mkPdf("245OV", OVPDF));
        lst.add(mkSEC("4.5.1", "Model with systems I."));
        lst.add(mkETT("2451E1", EX1));
        lst.add(mkETT("2451E2", EX2));
        lst.add(mkETT("2451T1", TT1));
        lst.add(mkETT("2451T2", TT2));
        lst.add(mkSEC("4.5.2", "Model with systems II."));
        lst.add(mkETT("2452E1", EX1));
        lst.add(mkETT("2452E2", EX2));
        lst.add(mkETT("2452T1", TT1));
        lst.add(mkETT("2452T2", TT2));
    }

    /**
     * Builds lesson components for MATH 1170.
     *
     * @param lst the list to which to add components
     */
    private static void build1170(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M1170;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Add and subtract fractions."));
        lst.add(mkETT("2P1a", EXA));
        lst.add(mkETT("2P1b", EXB));
        lst.add(mkETT("2P1c", EXC));
        lst.add(mkSEC("2", "Multiply and divide fractions."));
        lst.add(mkETT("2P2a", EXA));
        lst.add(mkETT("2P2b", EXB));
        lst.add(mkETT("2P2c", EXC));
        lst.add(mkSEC("3", "Solve equations."));
        lst.add(mkETT("2P3a", EXA));
        lst.add(mkETT("2P3b", EXB));
        lst.add(mkSEC("4", "Use interval notion to describe each situation."));
        lst.add(mkETT("2P4a", EXA));
        lst.add(mkETT("2P4b", EXB));
        lst.add(mkETT("2P4c", EXC));
        lst.add(mkSEC("5", "Solve inequalities using addition and subtraction."));
        lst.add(mkETT("2P5a", EXA));
        lst.add(mkETT("2P5b", EXB));
        lst.add(mkSEC("6", "Solve inequalities using multiplication and division."));
        lst.add(mkETT("2P6a", EXA));
        lst.add(mkETT("2P6b", EXB));
        lst.add(mkETT("2P6c", EXC));
        lst.add(mkSEC("7", "Multiply binomials."));
        lst.add(mkETT("2P7a", EXA));
        lst.add(mkETT("2P7b", EXB));
        lst.add(mkETT("2P7c", EXC));
        lst.add(mkSEC("8", "Factor."));
        lst.add(mkETT("2P8a", EXA));
        lst.add(mkETT("2P8b", EXB));
        lst.add(mkETT("2P8c", EXC));
        lst.add(mkSEC("9", "Simplify expressions with exponents."));
        lst.add(mkETT("2P9a", EXA));
        lst.add(mkETT("2P9b", EXB));
        lst.add(mkSEC("10", "Simplify radicals."));
        lst.add(mkETT("2P10a", EXA));
        lst.add(mkETT("2P10b", EXB));

        // 1: Linear Functions
        startLesson(prefix + ".11", 1, 1);

        lst.add(mkPreVid("211OV", OVVIDPRE));
        lst.add(mkPrePdf("211OV", OVPDF));
        lst.add(mkPreVid("IV1.1.1", "Supplemental: Using Verbal Descriptions to Write Linear Equations"));
        lst.add(mkPreVid("IV1.1.2", "Supplemental: Using Tables to Determine Linear Equations"));
        lst.add(mkPreVid("IV1.1.3", "Supplemental: Using Graphs to Determine Linear Equations"));
        lst.add(mkPreVid("TV1.1", "Technology Supplement: Using Technology to Verify Linear Functions"));

        lst.add(mkLH2("1: Linear Functions", O1_1, "Generalize linear equations from tables and graphs"));
        lst.add(mkSEC("1.1.1", "Given a verbal description, write a linear equation."));
        lst.add(mkETT("2111E1", EX1));
        lst.add(mkETT("2111E2", EX2));
        lst.add(mkETT("2111T1", TT1));
        lst.add(mkETT("2111T2", TT2));
        lst.add(mkSEC("1.1.2", "Given a table, write a linear equation."));
        lst.add(mkETT("2112E1", EX1));
        lst.add(mkETT("2112E2", EX2));
        lst.add(mkETT("2112E3", EX3));
        lst.add(mkETT("2112T1", TT1));
        lst.add(mkETT("2112T2", TT2));
        lst.add(mkETT("2112T3", TT3));
        lst.add(mkSEC("1.1.3", "Given a graph, write a linear equation."));
        lst.add(mkETT("2113E1", EX1));
        lst.add(mkETT("2113E2", EX2));
        lst.add(mkETT("2113T1", TT1));
        lst.add(mkETT("2113T2", TT2));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkPreVid("212OV", OVVIDPRE));
        lst.add(mkPrePdf("212OV", OVPDF));
        lst.add(mkPreVid("IV1.2.1", "Supplemental: Identity Functions"));
        lst.add(mkPreVid("IV1.2.2", "Supplemental: Evaluating Functions"));
        lst.add(mkPreVid("IV1.2.3", "Supplemental: Interpreting Functions from Graphs"));
        lst.add(mkPreVid("TV1.2", "Technology Supplement: Evaluating Functions on the Calculator"));

        lst.add(mkLH2("1: Linear Functions", O1_2, "Use function notation"));
        lst.add(mkSEC("1.2.1", "Define and identify functions."));
        lst.add(mkETT("2121E1", EX1));
        lst.add(mkETT("2121E2", EX2));
        lst.add(mkETT("2121E3", EX3));
        lst.add(mkETT("2121T1", TT1));
        lst.add(mkETT("2121T2", TT2));
        lst.add(mkETT("2121T3", TT3));
        lst.add(mkSEC("1.2.2", "Evaluate and graph functions."));
        lst.add(mkETT("2122E1", EX1));
        lst.add(mkETT("2122E2", EX2));
        lst.add(mkETT("2122T1", TT1));
        lst.add(mkETT("2122T2", TT2));
        lst.add(mkSEC("1.2.3", "Interpret functions and their graphs."));
        lst.add(mkETT("2123E1", EX1));
        lst.add(mkETT("2123E2", EX2));
        lst.add(mkETT("2123E3", EX3));
        lst.add(mkETT("2123T1", TT1));
        lst.add(mkETT("2123T2", TT2));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkPreVid("213OV", OVVIDPRE));
        lst.add(mkPrePdf("213OV", OVPDF));
        lst.add(mkPreVid("IV1.3.1", "Supplemental: Slope-Intercept Form"));
        lst.add(mkPreVid("IV1.3.2", "Supplemental: Point-Slope Form"));
        lst.add(mkPreVid("IV1.3.3", "Supplemental: Parallel and Perpendicular Lines"));
        lst.add(mkPreVid("TV1.3", "Technology Supplement: Graphing Linear Equations"));

        lst.add(mkLH2("1: Linear Functions", O1_3, "Use slope-intercept to determine the equation of the line"));
        lst.add(mkSEC("1.3.1", "Use slope-intercept to determine the equation of the line."));
        lst.add(mkETT("2131E1", EX1));
        lst.add(mkETT("2131E2", EX2));
        lst.add(mkETT("2131E3", EX3));
        lst.add(mkETT("2131T1", TT1));
        lst.add(mkETT("2131T2", TT2));
        lst.add(mkETT("2131T3", TT3));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Linear Functions", O1_4, "Use point-slope to determine the equation of the line"));
        lst.add(mkSEC("1.4.1", "Use point-slope to determine the equation of the line."));
        lst.add(mkETT("2132E1", EX1));
        lst.add(mkETT("2132E2", EX2));
        lst.add(mkETT("2132T1", TT1));
        lst.add(mkETT("2132T2", TT2));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Linear Functions", O1_5, "Find equations of parallel and perpendicular lines."));
        lst.add(mkSEC("1.5.1", "Find equations of parallel and perpendicular lines."));
        lst.add(mkETT("2132E1", EX1));
        lst.add(mkETT("2132E2", EX2));
        lst.add(mkETT("2132T1", TT1));
        lst.add(mkETT("2132T2", TT2));

        startLesson(prefix + ".16", 1, 6);
        lst.add(mkPreVid("214OV", OVVIDPRE));
        lst.add(mkPrePdf("214OV", OVPDF));
        lst.add(mkPreVid("IV1.4.1", "Supplemental: Solving Equations"));
        lst.add(mkPreVid("IV1.4.2", "Supplemental: Solving Inequalities"));
        lst.add(mkPreVid("IV1.4.3", "Supplemental: Solving Literal Equations and Inequalities"));
        lst.add(mkPreVid("TV1.4", "Technology Supplement: Solving Linear Inequalities with Technology"));

        lst.add(mkLH2("1: Linear Functions", O1_6, "Solve linear equations"));
        lst.add(mkSEC("1.6.1", "Solve linear equations."));
        lst.add(mkETT("2141E1", EX1));
        lst.add(mkETT("2141E2", EX2));
        lst.add(mkETT("2141E3", EX3));
        lst.add(mkETT("2141E4", EX4));
        lst.add(mkETT("2141T1", TT1));
        lst.add(mkETT("2141T2", TT2));
        lst.add(mkETT("2141T3", TT3));
        lst.add(mkETT("2141T4", TT4));

        startLesson(prefix + ".17", 1, 7);
        lst.add(mkLH2("1: Linear Functions", O1_7, "Solve linear inequalities"));
        lst.add(mkSEC("1.7.1", "Solve linear inequalities."));
        lst.add(mkETT("2142E1", EX1));
        lst.add(mkETT("2142E2", EX2));
        lst.add(mkETT("2142E3", EX3));
        lst.add(mkETT("2142T1", TT1));
        lst.add(mkETT("2142T2", TT2));

        startLesson(prefix + ".18", 1, 8);
        lst.add(mkLH2("1: Linear Functions", O1_8, "Solve literal equations"));
        lst.add(mkSEC("1.8.1", "Solve literal equations."));
        lst.add(mkETT("2143E1", EX1));
        lst.add(mkETT("2143E2", EX2));
        lst.add(mkETT("2143E3", EX3));
        lst.add(mkETT("2143T1", TT1));
        lst.add(mkETT("2143T2", TT2));
        lst.add(mkETT("2143T3", TT3));

        startLesson(prefix + ".19", 1, 9);
        lst.add(mkPreVid("215OV", OVVIDPRE));
        lst.add(mkPrePdf("215OV", OVPDF));
        lst.add(mkPreVid("IV1.5.1", "Supplemental: Determining if a Data Set is Linear"));
        lst.add(mkPreVid("IV1.5.3p1", "Supplemental: Applications of Linear Equations and Inequalities (part 1)"));
        lst.add(mkPreVid("IV1.5.3p2", "Supplemental: Applications of Linear Equations and Inequalities (part 2)"));
        lst.add(mkPreVid("IV1.5.3p3", "Supplemental: Applications of Linear Equations and Inequalities (part 3)"));
        lst.add(mkPreVid("TV1.5", "Technology Supplement: Linear Regression"));

        lst.add(mkLH2("1: Linear Functions", O1_9, "Model with linear functions"));
        lst.add(mkSEC("1.9.1", "Determine if a data set is linear."));
        lst.add(mkETT("2151E1", EX1));
        lst.add(mkETT("2151E2", EX2));
        lst.add(mkETT("2151T1", TT1));
        lst.add(mkETT("2151T2", TT2));

        startLesson(prefix + ".110", 1, 10);
        lst.add(mkLH2("1: Linear Functions", O1_10, "Given a data set determine a trend line and make predictions"));
        lst.add(mkSEC("1.10.1", "Given a data set determine a trend line and make predictions."));
        lst.add(mkETT("2152E1", EX1));
        lst.add(mkETT("2152T1", TT1));

        // 2: Absolute Value and Piecewise-Defined Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPreVid("221OV", OVVIDPRE));
        lst.add(mkPrePdf("221OV", OVPDF));
        lst.add(mkPreVid("IV2.1.1", "Supplemental: Sketching Graphs of Absolute Value Functions"));
        lst.add(mkPreVid("IV2.1.2", "Supplemental: Determining the Equation from the Graph"));

        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_1,
                "Given an absolute value function, sketch its graph"));
        lst.add(mkSEC("2.1.1", "Given an absolute value function, sketch its graph."));
        lst.add(mkETT("2211E1", EX1));
        lst.add(mkETT("2211E2", EX2));
        lst.add(mkETT("2211T1", TT1));
        lst.add(mkETT("2211T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_2,
                "Given the graph of an absolute value function, determine its equation"));
        lst.add(mkSEC("2.2.1", "Given the graph of an absolute value function, determine its equation."));
        lst.add(mkETT("2212E1", EX1));
        lst.add(mkETT("2212E2", EX2));
        lst.add(mkETT("2212T1", TT1));
        lst.add(mkETT("2212T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkPreVid("222OV", OVVIDPRE));
        lst.add(mkPrePdf("222OV", OVPDF));
        lst.add(mkPreVid("IV2.2.1", "Supplemental: Evaluating Piecewise-Defined Functions"));
        lst.add(mkPreVid("IV2.1.2", "Supplemental: Determining the Function from the Graph"));

        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_3, "Graph piecewise defined functions"));
        lst.add(mkSEC("2.3.1", "Graph and interpret piecewise-defined functions."));
        lst.add(mkETT("2221E1", EX1));
        lst.add(mkETT("2221E2", EX2));
        lst.add(mkETT("2221T1", TT1));
        lst.add(mkETT("2221T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_4,
                "Given a piecewise-defined function determine the rules and domains"));
        lst.add(mkSEC("2.4.1", "Given a piecewise-defined function determine the rules and domains."));
        lst.add(mkETT("2222E1", EX1));
        lst.add(mkETT("2222E2", EX2));
        lst.add(mkETT("2222T1", TT1));
        lst.add(mkETT("2222T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkPreVid("223OV", OVVIDPRE));
        lst.add(mkPrePdf("223OV", OVPDF));
        lst.add(mkPreVid("IV2.3.1", "Supplemental: Solving Absolute Value Equations |u| = a"));
        lst.add(mkPreVid("IV2.3.2", "Supplemental: Solving Absolute Value Equations |u| = linear function"));

        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_5,
                "Develop and apply methods to solve absolute value equations of the form "
                + "<em>|u| = a</em> where <em>a > 0</em>"));
        lst.add(mkSEC("2.5.1", "Develop and apply methods to solve absolute value equations of the form "
                               + "<em>|u| = a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2231E1", EX1));
        lst.add(mkETT("2231E2", EX2));
        lst.add(mkETT("2231T1", TT1));
        lst.add(mkETT("2231T2", TT2));

        startLesson(prefix + ".26", 2, 6);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_6,
                "Develop and apply methods to solve absolute value equations of the form "
                + "<em>|u| = v</em> where <em>v</em> is an expression in <em>x</em>."));
        lst.add(mkSEC("2.6.1",
                "Develop and apply methods to solve absolute value equations of the form "
                + "<em>|u| = v</em> where <em>v</em> is an expression in <em>x</em>."));
        lst.add(mkETT("2232E1", EX1));
        lst.add(mkETT("2232E2", EX2));
        lst.add(mkETT("2232T1", TT1));
        lst.add(mkETT("2232T2", TT2));

        startLesson(prefix + ".27", 2, 7);
        lst.add(mkPreVid("224OV", OVVIDPRE));
        lst.add(mkPrePdf("224OV", OVPDF));
        lst.add(mkPreVid("IV2.4.1", "Supplemental: Solving Absolute Value Inequalities |u| < a"));
        lst.add(mkPreVid("IV2.4.2", "Supplemental: Solving Absolute Value Inequalities |u| > a"));

        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_7,
                "Develop and apply methods to solve absolute value inequalities of the form "
                + "<em>|u| < a</em> where <em>a > 0</em>."));
        lst.add(mkSEC("2.7.1", "Develop and apply methods to solve absolute value inequalities of the form "
                               + "<em>|u| < a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2241E1", EX1));
        lst.add(mkETT("2241E2", EX2));
        lst.add(mkETT("2241T1", TT1));
        lst.add(mkETT("2241T2", TT2));

        startLesson(prefix + ".28", 2, 8);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_8,
                "Develop and apply methods to solve absolute value inequalities of the form "
                + "<em>|u| > a</em> where <em>a > 0</em>"));
        lst.add(mkSEC("2.8.1", "Develop and apply methods to solve absolute value inequalities of the form "
                               + "<em>|u| > a</em> where <em>a > 0</em>."));
        lst.add(mkETT("2242E1", EX1));
        lst.add(mkETT("2242E2", EX2));
        lst.add(mkETT("2242T1", TT1));
        lst.add(mkETT("2242T2", TT2));

        startLesson(prefix + ".29", 2, 9);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_9,
                "Model and interpret situations with absolute value functions"));
        lst.add(mkSEC("2.9.1", "Model and interpret situations with absolute value functions."));
        lst.add(mkETT("2251E1", EX1));
        lst.add(mkETT("2251E2", EX2));
        lst.add(mkETT("2251T1", TT1));
        lst.add(mkETT("2251T2", TT2));

        startLesson(prefix + ".210", 2, 10);
        lst.add(mkLH2("2: Absolute Value and Piecewise-Defined Functions", O2_10,
                "Model and interpret situations with piecewise-defined functions"));
        lst.add(mkSEC("2.10.1", "Model and interpret situations with piecewise-defined functions."));
        lst.add(mkETT("2252E1", EX1));
        lst.add(mkETT("2252E2", EX2));
        lst.add(mkETT("2252T1", TT1));
        lst.add(mkETT("2252T2", TT2));

        // 3: Quadratic Relations and Functions
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPreVid("231OV", OVVIDPRE));
        lst.add(mkPrePdf("231OV", OVPDF));
        lst.add(mkPreVid("IV3.1.1", "Supplemental: Finding the Vertex from a Graph"));
        lst.add(mkPreVid("IV3.1.2", "Supplemental: Finding the Vertex by Completing the Square"));
        lst.add(mkPreVid("IV3.1.3", "Supplemental: Solving Quadratic Applications"));
        lst.add(mkPreVid("TV3.1", "Technology Supplement: Graphing Quadratic Functions"));

        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_1,
                "Given the graph of a quadratic function, determine its equation"));
        lst.add(mkSEC("3.1.1", "Given the graph of a quadratic function, determine its equation."));
        lst.add(mkETT("2311E1", EX1));
        lst.add(mkETT("2311E2", EX2));
        lst.add(mkETT("2311T1", TT1));
        lst.add(mkETT("2311T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_2,
                "Write and graph quadratic functions in vertex form and determine the domain, range, and where "
                + "increasing or decreasing."));
        lst.add(mkSEC("3.2.1", "Write and graph quadratic functions in vertex form and determine the domain, range, "
                               + "and where increasing or decreasing."));
        lst.add(mkETT("2312E1", EX1));
        lst.add(mkETT("2312E2", EX2));
        lst.add(mkETT("2312T1", TT1));
        lst.add(mkETT("2312T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_3, "Solve quadratic applications"));
        lst.add(mkSEC("3.3.1", "Solve quadratic applications."));
        lst.add(mkETT("2313E1", EX1));
        lst.add(mkETT("2313E2", EX2));
        lst.add(mkETT("2313T1", TT1));
        lst.add(mkETT("2313T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkPreVid("232OV", OVVIDPRE));
        lst.add(mkPrePdf("232OV", OVPDF));
        lst.add(mkPreVid("IV3.2.1", "Supplemental: Solve Quadratics by Factoring or Grouping"));
        lst.add(mkPreVid("IV3.2.2", "Supplemental: Completing the Square and the Quadratic Formula"));
        lst.add(mkPreVid("TV3.2", "Technology Supplement: Verifying Factors and Complex Zeros"));

        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_4, "Solve quadratic equations"));
        lst.add(mkSEC("3.4.1", "Solve quadratic equations by factoring and using technology."));
        lst.add(mkETT("2321E1", EX1));
        lst.add(mkETT("2321E2", EX2));
        lst.add(mkETT("2321T1", TT1));
        lst.add(mkETT("2321T2", TT2));
        lst.add(mkSEC("3.4.2", "Solve quadratic equations by completing the square and with the quadratic formula."));
        lst.add(mkETT("2322E1", EX1));
        lst.add(mkETT("2322E2", EX2));
        lst.add(mkETT("2322T1", TT1));
        lst.add(mkETT("2322T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkPreVid("233OV", OVVIDPRE));
        lst.add(mkPrePdf("233OV", OVPDF));
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_5, "Solve quadratic inequalities"));
        lst.add(mkSEC("3.5.1", "Develop and apply methods to solve quadratic inequalities."));
        lst.add(mkETT("2331E1", EX1));
        lst.add(mkETT("2331E2", EX2));
        lst.add(mkETT("2331E3", EX3));
        lst.add(mkETT("2331T1", TT1));
        lst.add(mkETT("2331T2", TT2));
        lst.add(mkETT("2331T3", TT3));
        lst.add(mkSEC("3.5.2", "Solve quadratic inequality applications."));
        lst.add(mkETT("2332E1", EX1));
        lst.add(mkETT("2332E2", EX2));
        lst.add(mkETT("2332T1", TT1));
        lst.add(mkETT("2332T2", TT2));

        startLesson(prefix + ".36", 3, 6);
        lst.add(mkPreVid("234OV", OVVIDPRE));
        lst.add(mkPrePdf("234OV", OVPDF));
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_6, "Determine the distance between two points"));
        lst.add(mkSEC("3.6.1", "Determine the distance between two points."));
        lst.add(mkETT("2341E1", EX1));
        lst.add(mkETT("2341E2", EX2));
        lst.add(mkETT("2341T1", TT1));
        lst.add(mkETT("2341T2", TT2));

        startLesson(prefix + ".37", 3, 7);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_6, "Graph and interpret graphs of the form "
                                                                    + "(x - h)<sup>2</sup> + (y - k)<sup>2</sup> = " +
                                                                    "r<sup>2</sup>"));
        lst.add(mkSEC("3.7.1", "Graph and interpret graphs of the form "
                               + "(x - h)<sup>2</sup> + (y - k)<sup>2</sup> = r<sup>2</sup>."));
        lst.add(mkETT("2342E1", EX1));
        lst.add(mkETT("2342E2", EX2));
        lst.add(mkETT("2342T1", TT1));
        lst.add(mkETT("2342T2", TT2));

        startLesson(prefix + ".38", 3, 8);
        lst.add(mkPreVid("235OV", OVVIDPRE));
        lst.add(mkPrePdf("235OV", OVPDF));
        lst.add(mkPreVid("IV3.5.1", "Supplemental: Solve Applications Involving Quadratic Functions"));
        lst.add(mkPreVid("IV3.5.2", "Supplemental: Average Rate of Change and Instantaneous Velocity"));
        lst.add(mkPreVid("TV3.5", "Technology Supplement: Verifying Results of Application Problems"));

        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_8, "Model with quadratic functions and relations"));
        lst.add(mkSEC("3.8.1", "Solve applications."));
        lst.add(mkETT("2351E1", EX1));
        lst.add(mkETT("2351E2", EX2));
        lst.add(mkETT("2351T1", TT1));
        lst.add(mkETT("2351T2", TT2));

        startLesson(prefix + ".39", 3, 9);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_9,
                "Determine average and instantaneous rates of change"));
        lst.add(mkSEC("3.9.1", "Determine average and instantaneous rates of change."));
        lst.add(mkETT("2352E1", EX1));
        lst.add(mkETT("2352E2", EX2));
        lst.add(mkETT("2352T1", TT1));
        lst.add(mkETT("2352T2", TT2));

        startLesson(prefix + ".310", 3, 10);
        lst.add(mkLH2("3: Quadratic Relations and Functions", O3_10,
                "Determine average and instantaneous rates of change"));
        lst.add(mkSEC("3.10.1", "Model and interpret situations with quadratic functions."));
        lst.add(mkETT("2353E1", EX1));
        lst.add(mkETT("2353E2", EX2));
        lst.add(mkETT("2353T1", TT1));
        lst.add(mkETT("2353T2", TT2));

        // 4: Systems of Equations and Inequalities
        lst.add(mkPreVid("241OV", OVVIDPRE));
        lst.add(mkPrePdf("241OV", OVPDF));
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkPreVid("IV4.1.1", "Supplemental: Solving Systems with Substitution"));
        lst.add(mkPreVid("IV4.1.2", "Supplemental: Solving Systems with Elimination"));
        lst.add(mkPreVid("TV4.1", "Technology Supplement: Verifying Solutions to Systems Graphically"));

        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_1, "Solve systems of linear equations"));
        lst.add(mkSEC("4.1.1", "Solve systems using substitution."));
        lst.add(mkETT("2411E1", EX1));
        lst.add(mkETT("2411E2", EX2));
        lst.add(mkETT("2411T1", TT1));
        lst.add(mkETT("2411T2", TT2));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_2, "Solve systems of linear equations"));
        lst.add(mkSEC("4.2.1", "Solve systems using elimination."));
        lst.add(mkETT("2412E1", EX1));
        lst.add(mkETT("2412E2", EX2));
        lst.add(mkETT("2412E3", EX3));
        lst.add(mkETT("2412T1", TT1));
        lst.add(mkETT("2412T2", TT2));
        lst.add(mkETT("2412T3", TT3));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkPreVid("242OV", OVVIDPRE));
        lst.add(mkPrePdf("242OV", OVPDF));
        lst.add(mkPreVid("IV4.2.1", "Supplemental: Solving Systems using Row Transformations"));
        lst.add(mkPreVid("IV4.2.2", "Supplemental: Reduced Row Echelon Form"));
        lst.add(mkPreVid("TV4.2", "Technology Supplement: Solving Sustems using Matrices and RREF"));

        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_3, "Solve systems using matrices"));
        lst.add(mkSEC("4.3.1", "Solve two-variable systems using row reduction."));
        lst.add(mkETT("2421E1", EX1));
        lst.add(mkETT("2421E2", EX2));
        lst.add(mkETT("2421E3", EX3));
        lst.add(mkETT("2421T1", TT1));
        lst.add(mkETT("2421T2", TT2));
        lst.add(mkETT("2421T3", TT3));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_4, "Solve systems using matrices"));
        lst.add(mkSEC("4.4.1", "Solve multi-variable systems using matrices."));
        lst.add(mkETT("2422E1", EX1));
        lst.add(mkETT("2422E2", EX2));
        lst.add(mkETT("2422T1", TT1));
        lst.add(mkETT("2422T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkPreVid("243OV", OVVIDPRE));
        lst.add(mkPrePdf("243OV", OVPDF));
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_5, "Solve systems of linear inequalities"));
        lst.add(mkSEC("4.5.1", "Solve linear inequalities."));
        lst.add(mkETT("2431E1", EX1));
        lst.add(mkETT("2431E2", EX2));
        lst.add(mkETT("2431T1", TT1));
        lst.add(mkETT("2431T2", TT2));

        startLesson(prefix + ".46", 4, 6);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_6, "Solve systems of linear inequalities"));
        lst.add(mkSEC("4.6.2", "Solve systems of linear inequalities."));
        lst.add(mkETT("2432E1", EX1));
        lst.add(mkETT("2432E2", EX2));
        lst.add(mkETT("2432T1", TT1));
        lst.add(mkETT("2432T2", TT2));

        startLesson(prefix + ".47", 4, 7);
        lst.add(mkPreVid("244OV", OVVIDPRE));
        lst.add(mkPrePdf("244OV", OVPDF));
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_7,
                "Solve systems with other functions and relations"));
        lst.add(mkSEC("4.7.1", "Solve nonlinear systems of equations."));
        lst.add(mkETT("2441E1", EX1));
        lst.add(mkETT("2441E2", EX2));
        lst.add(mkETT("2441E3", EX3));
        lst.add(mkETT("2441T1", TT1));
        lst.add(mkETT("2441T2", TT2));
        lst.add(mkETT("2441T3", TT3));

        startLesson(prefix + ".48", 4, 8);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_8,
                "Solve systems with other functions and relations"));
        lst.add(mkSEC("4.8.1", "Solve nonlinear systems of inequalities."));
        lst.add(mkETT("2442E1", EX1));
        lst.add(mkETT("2442E2", EX2));
        lst.add(mkETT("2442T1", TT1));
        lst.add(mkETT("2442T2", TT2));

        startLesson(prefix + ".49", 4, 9);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_9, "Model with systems"));
        lst.add(mkSEC("4.9.1", "Model with systems I."));
        lst.add(mkETT("2451E1", EX1));
        lst.add(mkETT("2451E2", EX2));
        lst.add(mkETT("2451T1", TT1));
        lst.add(mkETT("2451T2", TT2));

        startLesson(prefix + ".410", 4, 10);
        lst.add(mkLH2("4: Systems of Equations and Inequalities", O4_10, "Model with systems"));
        lst.add(mkSEC("4.10.1", "Model with systems II."));
        lst.add(mkETT("2452E1", EX1));
        lst.add(mkETT("2452E2", EX2));
        lst.add(mkETT("2452T1", TT1));
        lst.add(mkETT("2452T2", TT2));
    }

    /**
     * Builds lesson components for MATH 118.
     *
     * @param lst the list to which to add components
     */
    private static void build118(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M118;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Multiply and factor polynomials."));
        lst.add(mkETT("3P1a", EXA));
        lst.add(mkETT("3P1b", EXB));
        lst.add(mkSEC("2", "Add and subtract rational expressions."));
        lst.add(mkETT("3P2a", EXA));
        lst.add(mkETT("3P2b", EXB));
        lst.add(mkSEC("3", "Multiply rational expressions."));
        lst.add(mkETT("3P3a", EXA));
        lst.add(mkETT("3P3b", EXB));
        lst.add(mkSEC("4", "Simplify expressions with integer exponents."));
        lst.add(mkETT("3P4a", EXA));
        lst.add(mkETT("3P4b", EXB));
        lst.add(mkSEC("5", "Simplify radicals."));
        lst.add(mkETT("3P5a", EXA));
        lst.add(mkETT("3P5b", EXB));
        lst.add(mkSEC("6", "Simplify expressions with rational exponents."));
        lst.add(mkETT("3P6a", EXA));
        lst.add(mkETT("3P6b", EXB));
        lst.add(mkSEC("7", "Determine the equation of a line in standard form."));
        lst.add(mkETT("3P7a", EXA));
        lst.add(mkETT("3P7b", EXB));
        lst.add(mkSEC("8", "Solve quadratic equations."));
        lst.add(mkETT("3P8a", EXA));
        lst.add(mkETT("3P8b", EXB));
        lst.add(mkETT("3P8c", EXC));
        lst.add(mkSEC("9", "Determine the vertex for each quadratic."));
        lst.add(mkETT("3P9a", EXA));
        lst.add(mkETT("3P9b", EXB));
        lst.add(mkSEC("10", "Solve systems of equations."));
        lst.add(mkETT("3P10a", EXA));
        lst.add(mkETT("3P10b", EXB));

        // 1: Polynomial Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkLH2("1: Polynomial Functions", O1_1, "Identify and graph polynomial functions"));
        lst.add(mkVid("311OV", OVVID));
        lst.add(mkPdf("311OV", OVPDF));

        lst.add(mkVid("IV1.1.1", "Supplemental: Polynomial Functions"));
        lst.add(mkVid("IV1.1.2", "Supplemental: Evaluating Polynomial Functions"));
        lst.add(mkVid("IV1.1.3", "Supplemental: Interpreting Polynomial Functions"));
        lst.add(mkVid("TV1.1", "Technology Supplement: Evaluating polynomials using the calculator"));

        lst.add(mkSEC("1.1.1", "Determine if a function is a polynomial function."));
        lst.add(mkETT("3111E1", EX1));
        lst.add(mkETT("3111E2", EX2));
        lst.add(mkETT("3111E3", EX3));
        lst.add(mkETT("3111T1", TT1));
        lst.add(mkETT("3111T2", TT2));
        lst.add(mkETT("3111T3", TT3));
        lst.add(mkSEC("1.1.2", "Evaluate polynomial functions."));
        lst.add(mkETT("3112E1", EX1));
        lst.add(mkETT("3112E2", EX2));
        lst.add(mkETT("3112E3", EX3));
        lst.add(mkETT("3112T1", TT1));
        lst.add(mkETT("3112T2", TT2));
        lst.add(mkETT("3112T3", TT3));
        lst.add(mkSEC("1.1.3", "Graph polynomial functions."));
        lst.add(mkETT("3113E1", EX1));
        lst.add(mkETT("3113E2", EX2));
        lst.add(mkETT("3113T1", TT1));
        lst.add(mkETT("3113T2", TT2));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Polynomial Functions", O1_2, "Build polynomial functions"));
        lst.add(mkVid("312OV", OVVID));
        lst.add(mkPdf("312OV", OVPDF));

        lst.add(mkVid("IV1.2.1", "Supplemental: Writing Polynomials in Factored Form"));
        lst.add(mkVid("IV1.2.2", "Supplemental: Build Polynomials in Factored Form"));
        lst.add(mkVid("TV1.2", "Technology Supplement: Verfying zeros with the calculator"));

        lst.add(mkSEC("1.2.1", "Graph polynomials in factored form."));
        lst.add(mkETT("3121E1", EX1));
        lst.add(mkETT("3121E2", EX2));
        lst.add(mkETT("3121T1", TT1));
        lst.add(mkETT("3121T2", TT2));
        lst.add(mkSEC("1.2.2", "Construct a polynomial with given zeros."));
        lst.add(mkETT("3122E1", EX1));
        lst.add(mkETT("3122E2", EX2));
        lst.add(mkETT("3122T1", TT1));
        lst.add(mkETT("3122T2", TT2));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Polynomial Functions", O1_3, "Determine zeros of polynomial functions"));
        lst.add(mkVid("313OV", OVVID));
        lst.add(mkPdf("313OV", OVPDF));

        lst.add(mkVid("IV1.3.1", "Supplemental: Factoring Polynomials by Grouping"));
        lst.add(mkVid("IV1.3.2.1", "Supplemental: Factoring Polynomials by Finding Rational Zeros"));
        lst.add(mkVid("IV1.3.2.2", "Supplemental: Factoring Polynomials Using Long or Synthetic Division"));
        lst.add(mkVid("TV1.3", "Technology Supplement: Verfying zeros with the calculator"));

        lst.add(mkSEC("1.3.1", "Factor polynomials to determine zeros."));
        lst.add(mkETT("3131E1", EX1));
        lst.add(mkETT("3131E2", EX2));
        lst.add(mkETT("3131T1", TT1));
        lst.add(mkETT("3131T2", TT2));
        lst.add(mkSEC("1.3.2", "Find all zeros of a given polynomial function."));
        lst.add(mkETT("3132E1", EX1));
        lst.add(mkETT("3132E2", EX2));
        lst.add(mkETT("3132E3", EX3));
        lst.add(mkETT("3132E4", EX4));
        lst.add(mkETT("3132T1", TT1));
        lst.add(mkETT("3132T2", TT2));
        lst.add(mkETT("3132T3", TT3));
        lst.add(mkETT("3132T4", TT4));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Polynomial Functions", O1_4, "Solve problems with polynomials"));
        lst.add(mkVid("314OV", OVVID));
        lst.add(mkPdf("314OV", OVPDF));

        lst.add(mkVid("IV1.4.1", "Supplemental: Solving Polynomial Equations and Inequalities"));
        lst.add(mkVid("IV1.4.2", "Supplemental: Finding Average Rate of Change"));
        lst.add(mkVid("TV1.4",
                "Technology Supplement: Finding Intersection of Lines and Local Min and Max"));

        lst.add(mkSEC("1.4.1", "Solve polynomial equations and inequalities."));
        lst.add(mkETT("3141E1", EX1));
        lst.add(mkETT("3141E2", EX2));
        lst.add(mkETT("3141T1", TT1));
        lst.add(mkETT("3141T2", TT2));
        lst.add(mkSEC("1.4.2", "Solve max/min problems."));
        lst.add(mkETT("3142E1", EX1));
        lst.add(mkETT("3142E2", EX2));
        lst.add(mkETT("3142T1", TT1));
        lst.add(mkSEC("1.4.3", "Compute average and instantaneous rate of change."));
        lst.add(mkETT("3143E1", EX1));
        lst.add(mkETT("3143E2", EX2));
        lst.add(mkETT("3143E3", EX3));
        lst.add(mkETT("3143E4", EX4));
        lst.add(mkETT("3143T1", TT1));
        lst.add(mkETT("3143T2", TT2));
        lst.add(mkETT("3143T3", TT3));
        lst.add(mkETT("3143T4", TT4));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Polynomial Functions", O1_5, "Determine polynomial functions for data sets."));
        lst.add(mkVid("315OV", OVVID));
        lst.add(mkPdf("315OV", OVPDF));

        lst.add(mkVid("IV1.5.1", "Supplemental: Using Differences to find Polynomials, Part 1"));
        lst.add(mkVid("IV1.5.2", "Supplemental: Using Differences to find Polynomials, Part 2"));
        lst.add(mkVid("IV1.5.3", "Supplemental: Polynomial Regression"));
        lst.add(mkVid("TV1.5", "Technology Supplement: Finding Differences with the Calculator"));

        lst.add(mkSEC("1.5.1", "Given polynomial data, determine the degree and the model."));
        lst.add(mkETT("3151E1", EX1));
        lst.add(mkETT("3151E2", EX2));
        lst.add(mkETT("3151E3", EX3));
        lst.add(mkETT("3151T1", TT1));
        lst.add(mkETT("3151T2", TT2));
        lst.add(mkSEC("1.5.2", "Use regression to determine polynomial models."));
        lst.add(mkETT("3152E1", EX1));
        lst.add(mkETT("3152E2", EX2));
        lst.add(mkETT("3152T1", TT1));
        lst.add(mkETT("3152T2", TT2));

        // 2: Rational Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkLH2("2: Rational Functions", O2_1, "Determine local behavior of rational functions"));
        lst.add(mkVid("321OV", OVVID));
        lst.add(mkPdf("321OV", OVPDF));

        lst.add(mkVid("IV2.1.1", "Supplemental: Evaluating Rational Functions"));
        lst.add(mkVid("IV2.1.2", "Supplemental: Graphing Rational Functions"));
        lst.add(mkVid("TV2.1", "Technology Supplement: Verifying Intercepts, Zeros, Asymptotes"));

        lst.add(mkSEC("2.1.1", "Evaluate rational functions."));
        lst.add(mkETT("3211E1", EX1));
        lst.add(mkETT("3211E2", EX2));
        lst.add(mkETT("3211T1", TT1));
        lst.add(mkETT("3211T2", TT2));
        lst.add(mkSEC("2.1.2", "Graph rational functions."));
        lst.add(mkETT("3212E1", EX1));
        lst.add(mkETT("3212E2", EX2));
        lst.add(mkETT("3212T1", TT1));
        lst.add(mkETT("3212T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Rational Functions", O2_2,
                "Determine asymptotic and end-behavior properties of rational functions"));
        lst.add(mkVid("322OV", OVVID));
        lst.add(mkPdf("322OV", OVPDF));

        lst.add(mkVid("IV2.2.1", "Supplemental: Horizontal Asymptotes, Part 1"));
        lst.add(mkVid("IV2.2.2", "Supplemental: Horizontal Asymptotes, Part 2"));
        lst.add(mkVid("IV2.2.3", "Supplemental: Slant/Oblique Asymptotes"));
        lst.add(mkVid("TV2.2", "Technology Supplement: End-Behavior of Rational Functions"));

        lst.add(mkSEC("2.2.1", "Determine horizontal asymptotes and sketch a graph."));
        lst.add(mkETT("3221E1", EX1));
        lst.add(mkETT("3221E2", EX2));
        lst.add(mkETT("3221T1", TT1));
        lst.add(mkETT("3221T2", TT2));
        lst.add(mkSEC("2.2.2", "Determine oblique asymptotes and end behavior."));
        lst.add(mkETT("3222E1", EX1));
        lst.add(mkETT("3222E2", EX2));
        lst.add(mkETT("3222T1", TT1));
        lst.add(mkETT("3222T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("2: Rational Functions", O2_3, "Build rational functions"));
        lst.add(mkVid("323OV", OVVID));
        lst.add(mkPdf("323OV", OVPDF));

        lst.add(mkVid("IV2.3.1", "Supplemental: Building Rational Functions, Part 1"));
        lst.add(mkVid("IV2.3.2", "Supplemental: Building Rational Functions, Part 2"));
        lst.add(mkVid("IV2.3.3", "Supplemental: Building Rational Functions, Part 3"));
        lst.add(mkVid("TV2.3", "Technology Supplement: Verifying Rational Functions"));

        lst.add(mkSEC("2.3.1", "Construct rational functions with horizontal asymptotes."));
        lst.add(mkETT("3231E1", EX1));
        lst.add(mkETT("3231E2", EX2));
        lst.add(mkETT("3231T1", TT1));
        lst.add(mkETT("3231T2", TT2));
        lst.add(mkSEC("2.3.2", "Construct rational functions with slant asymptotes."));
        lst.add(mkETT("3232E1", EX1));
        lst.add(mkETT("3232E2", EX2));
        lst.add(mkETT("3232T1", TT1));
        lst.add(mkETT("3232T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Rational Functions", O2_4, "Solve rational equations and inequalities"));
        lst.add(mkVid("324OV", OVVID));
        lst.add(mkPdf("324OV", OVPDF));

        lst.add(mkVid("IV2.4.1", "Supplemental: Solving Rational Equations"));
        lst.add(mkVid("IV2.4.2", "Supplemental: Solving Rational Inequalities"));
        lst.add(mkVid("TV2.4", "Technology Supplement: Verifying Solutions to Rational Equations"));

        lst.add(mkSEC("2.4.1", "Solve rational equations."));
        lst.add(mkETT("3241E1", EX1));
        lst.add(mkETT("3241E2", EX2));
        lst.add(mkETT("3241T1", TT1));
        lst.add(mkETT("3241T2", TT2));
        lst.add(mkSEC("2.4.2", "Solve rational inequalities."));
        lst.add(mkETT("3242E1", EX1));
        lst.add(mkETT("3242E2", EX2));
        lst.add(mkETT("3242T1", TT1));
        lst.add(mkETT("3242T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Rational Functions", O2_5, "Model with rational functions"));
        lst.add(mkVid("325OV", OVVID));
        // lst.add(mkPdf("325OV", OVPDF));

        lst.add(mkVid("IV2.5.1", "Supplemental: Solving Mixture Problems with Rational Equations"));
        lst.add(mkVid("IV2.5.2", "Supplemental: Solving Population Problems with Rational Equations"));
        lst.add(mkVid("TV2.5", "Technology Supplement: Solving Rational Equations with the Calculator"));

        lst.add(mkSEC("2.5.1", "Solve application problems I."));
        lst.add(mkETT("3251E1", EX1));
        lst.add(mkETT("3251E2", EX2));
        lst.add(mkETT("3251T1", TT1));
        lst.add(mkETT("3251T2", TT2));
        lst.add(mkSEC("2.5.2", "Solve application problems II."));
        lst.add(mkETT("3252E1", EX1));
        lst.add(mkETT("3252E2", EX2));
        lst.add(mkETT("3252T1", TT1));
        lst.add(mkETT("3252T2", TT2));

        // 3: Radical Functions and Equations
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_1, "Graph square root functions"));
        lst.add(mkVid("331OV", OVVID));
        lst.add(mkPdf("331OV", OVPDF));

        lst.add(mkVid("IV3.1.1", "Supplemental: Evaluating Square Root Functions"));
        lst.add(mkVid("IV3.1.2", "Supplemental: Graphing Square Root Functions"));
        lst.add(mkVid("TV3.1", "Technology Supplement: Graphs and Zeros of Square Root Functions"));

        lst.add(mkSEC("3.1.1", "Evaluate square root functions."));
        lst.add(mkETT("3311E1", EX1));
        lst.add(mkETT("3311E2", EX2));
        lst.add(mkETT("3311T1", TT1));
        lst.add(mkETT("3311T2", TT2));
        lst.add(mkSEC("3.1.2", "Graph and interpret square root functions."));
        lst.add(mkETT("3312E1", EX1));
        lst.add(mkETT("3312E2", EX2));
        lst.add(mkETT("3312E3", EX3));
        lst.add(mkETT("3312T1", TT1));
        lst.add(mkETT("3312T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_2, "Graph other root functions"));
        lst.add(mkVid("332OV", OVVID));
        lst.add(mkPdf("332OV", OVPDF));

        lst.add(mkVid("IV3.2.1", "Supplemental: Evaluating Radical Functions"));
        lst.add(mkVid("IV3.2.2", "Supplemental: Graphing Radical Functions"));
        lst.add(mkVid("TV3.2", "Technology Supplement: Graphs of Radical Functions"));

        lst.add(mkSEC("3.2.1", "Evaluate radical functions."));
        lst.add(mkETT("3321E1", EX1));
        lst.add(mkETT("3321E2", EX2));
        lst.add(mkETT("3321T1", TT1));
        lst.add(mkETT("3321T2", TT2));
        lst.add(mkSEC("3.2.2", "Graph and interpret radical functions."));
        lst.add(mkETT("3322E1", EX1));
        lst.add(mkETT("3322E2", EX2));
        lst.add(mkETT("3322T1", TT1));
        lst.add(mkETT("3322T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_3, "Solve radical equations"));
        lst.add(mkVid("333OV", OVVID));
        lst.add(mkPdf("333OV", OVPDF));

        lst.add(mkVid("IV3.3.1", "Supplemental: Solving Radical Equations, Part 1"));
        lst.add(mkVid("IV3.3.2", "Supplemental: Solving Radical Equations, Part 2"));
        lst.add(mkVid("TV3.3", "Technology Supplement: Verifying Solutions to Radical Equations"));

        lst.add(mkSEC("3.3.1", "Solve square root equations."));
        lst.add(mkETT("3331E1", EX1));
        lst.add(mkETT("3331E2", EX2));
        lst.add(mkETT("3331T1", TT1));
        lst.add(mkETT("3331T2", TT2));
        lst.add(mkSEC("3.3.2", "Solve other radical equations."));
        lst.add(mkETT("3332E1", EX1));
        lst.add(mkETT("3332E2", EX2));
        lst.add(mkETT("3332T1", TT1));
        lst.add(mkETT("3332T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_4, "Solve radical inequalities"));
        lst.add(mkVid("334OV", OVVID));
        lst.add(mkPdf("334OV", OVPDF));

        lst.add(mkVid("IV3.4.1", "Supplemental: Solving Radical Inequalities, Part 1"));
        lst.add(mkVid("IV3.4.2", "Supplemental: Solving Radical Inequalities, Part 2"));
        lst.add(mkVid("TV3.4", "Technology Supplement:Solving Radical Inequalities with the Calculator"));

        lst.add(mkSEC("3.4.1", "Solve square root inequalities."));
        lst.add(mkETT("3341E1", EX1));
        lst.add(mkETT("3341E2", EX2));
        lst.add(mkETT("3341T1", TT1));
        lst.add(mkETT("3341T2", TT2));
        lst.add(mkSEC("3.4.2", "Solve other radical inequalities."));
        lst.add(mkETT("3342E1", EX1));
        lst.add(mkETT("3342E2", EX2));
        lst.add(mkETT("3342T1", TT1));
        lst.add(mkETT("3342T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_5, "Model with radical functions"));
        lst.add(mkVid("335OV", OVVID));
        // lst.add(mkPdf("335OV", OVPDF));

        lst.add(mkVid("IV3.5.1", "Supplemental: Solving Pendulum Problems using Radical Functions"));
        lst.add(mkVid("IV3.5.2", "Supplemental: Solving Minimizing Problems using Radical Functions"));
        lst.add(mkVid("TV3.5", "Supplemental: Solving Pendulum Problems with the Calculator"));

        lst.add(mkSEC("3.5.1", "Solve radical applications I."));
        lst.add(mkETT("3351E1", EX1));
        lst.add(mkETT("3351E2", EX2));
        lst.add(mkETT("3351T1", TT1));
        lst.add(mkETT("3351T2", TT2));
        lst.add(mkSEC("3.5.2", "Solve radical applications II."));
        lst.add(mkETT("3352E1", EX1));
        lst.add(mkETT("3352E2", EX2));
        lst.add(mkETT("3352T1", TT1));
        lst.add(mkETT("3352T2", TT2));

        // 4: Power Functions, Operations and Systems
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_1, "Evaluate power functions"));
        lst.add(mkVid("341OV", OVVID));
        lst.add(mkPdf("341OV", OVPDF));

        lst.add(mkVid("IV4.1.1", "Supplemental: Evaluating Power Functions, Part 1"));
        lst.add(mkVid("IV4.1.2", "Supplemental: Evaluating Power Functions, Part 2"));
        lst.add(mkVid("TV4.1", "Supplemental: Evaluating Power Functions with the Calculator"));

        lst.add(mkSEC("4.1.1", "Evaluate power functions I."));
        lst.add(mkETT("3411E1", EX1));
        lst.add(mkETT("3411E2", EX2));
        lst.add(mkETT("3411T1", TT1));
        lst.add(mkETT("3411T2", TT2));
        lst.add(mkSEC("4.1.2", "Evaluate power functions II."));
        lst.add(mkETT("3412E1", EX1));
        lst.add(mkETT("3412E2", EX2));
        lst.add(mkETT("3412T1", TT1));
        lst.add(mkETT("3412T2", TT2));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_2, "Graph power functions"));
        lst.add(mkVid("342OV", OVVID));
        lst.add(mkPdf("342OV", OVPDF));

        lst.add(mkVid("IV4.2.1", "Supplemental: Graphing Power Functions, Part 1"));
        lst.add(mkVid("IV4.2.2", "Supplemental: Graphing Power Functions, Part 2"));
        lst.add(mkVid("TV4.2", "Supplemental: Graphing Power Functions with the Calculator"));

        lst.add(mkSEC("4.2.1", "Graph  power functions I."));
        lst.add(mkETT("3421E1", EX1));
        lst.add(mkETT("3421E2", EX2));
        lst.add(mkETT("3421T1", TT1));
        lst.add(mkETT("3421T2", TT2));
        lst.add(mkSEC("4.2.2", "Graph  power functions II."));
        lst.add(mkETT("3422E1", EX1));
        lst.add(mkETT("3422E2", EX2));
        lst.add(mkETT("3422T1", TT1));
        lst.add(mkETT("3422T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_3,
                "Solve equations and inequalities with power functions"));
        lst.add(mkVid("343OV", OVVID));
        lst.add(mkPdf("343OV", OVPDF));

        lst.add(mkVid("IV4.3.1", "Supplemental: Solving Equations with Power Functions"));
        lst.add(mkVid("IV4.3.2", "Supplemental: Solving Inequalities with Power Functions"));
        lst.add(mkVid("TV4.3", "Supplemental: Verifying Inequalities Involving Power Functions"));

        lst.add(mkSEC("4.3.1", "Solve equations with power functions."));
        lst.add(mkETT("3431E1", EX1));
        lst.add(mkETT("3431E2", EX2));
        lst.add(mkETT("3431T1", TT1));
        lst.add(mkETT("3431T2", TT2));
        lst.add(mkSEC("4.3.2", "Solve inequalities with power functions."));
        lst.add(mkETT("3432E1", EX1));
        lst.add(mkETT("3432E2", EX2));
        lst.add(mkETT("3432T1", TT1));
        lst.add(mkETT("3432T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_4, "Solve power function applications"));
        lst.add(mkVid("344OV", OVVID));
        lst.add(mkPdf("344OV", OVPDF));

        lst.add(mkVid("TV4.4", "Supplemental: Power Regression"));

        lst.add(mkSEC("4.4.1", "Solve applications with power functions."));
        lst.add(mkETT("3441E1", EX1));
        lst.add(mkETT("3441E2", EX2));
        lst.add(mkETT("3441T1", TT1));
        lst.add(mkETT("3441T2", TT2));
        lst.add(mkSEC("4.4.2", "Solve applications with power regression."));
        lst.add(mkETT("3442E1", EX1));
        lst.add(mkETT("3442E2", EX2));
        lst.add(mkETT("3442T1", TT1));
        lst.add(mkETT("3442T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_5, "Solve systems with nonlinear functions"));
        lst.add(mkVid("345OV", OVVID));
        // lst.add(mkPdf("345OV", OVPDF));

        lst.add(mkVid("IV4.5.1", "Supplemental: Solving Systems of Nonlinear Equations Algebraically"));
        lst.add(mkVid("IV4.5.2", "Supplemental: Solving Systems of Nonlinear Equations Graphically"));
        lst.add(mkVid("TV4.5", "Supplemental: Solving Systems of Nonlinear Equations with Technology"));

        lst.add(mkSEC("4.5.1", "Solve systems with nonlinear functions algabraically."));
        lst.add(mkETT("3451E1", EX1));
        lst.add(mkETT("3451T1", TT1));
        lst.add(mkETT("3451T2", TT2));
        lst.add(mkSEC("4.5.2", "Solve systems with nonlinear functions graphically."));
        lst.add(mkETT("3452E1", EX1));
        lst.add(mkETT("3452E2", EX2));
        lst.add(mkETT("3452T1", TT1));
        lst.add(mkETT("3452T2", TT2));
    }

    /**
     * Builds lesson components for MATH 1180.
     *
     * @param lst the list to which to add components
     */
    private static void build1180(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M1180;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Multiply and factor polynomials."));
        lst.add(mkETT("3P1a", EXA));
        lst.add(mkETT("3P1b", EXB));
        lst.add(mkSEC("2", "Add and subtract rational expressions."));
        lst.add(mkETT("3P2a", EXA));
        lst.add(mkETT("3P2b", EXB));
        lst.add(mkSEC("3", "Multiply rational expressions."));
        lst.add(mkETT("3P3a", EXA));
        lst.add(mkETT("3P3b", EXB));
        lst.add(mkSEC("4", "Simplify expressions with integer exponents."));
        lst.add(mkETT("3P4a", EXA));
        lst.add(mkETT("3P4b", EXB));
        lst.add(mkSEC("5", "Simplify radicals."));
        lst.add(mkETT("3P5a", EXA));
        lst.add(mkETT("3P5b", EXB));
        lst.add(mkSEC("6", "Simplify expressions with rational exponents."));
        lst.add(mkETT("3P6a", EXA));
        lst.add(mkETT("3P6b", EXB));
        lst.add(mkSEC("7", "Determine the equation of a line in standard form."));
        lst.add(mkETT("3P7a", EXA));
        lst.add(mkETT("3P7b", EXB));
        lst.add(mkSEC("8", "Solve quadratic equations."));
        lst.add(mkETT("3P8a", EXA));
        lst.add(mkETT("3P8b", EXB));
        lst.add(mkETT("3P8c", EXC));
        lst.add(mkSEC("9", "Determine the vertex for each quadratic."));
        lst.add(mkETT("3P9a", EXA));
        lst.add(mkETT("3P9b", EXB));
        lst.add(mkSEC("10", "Solve systems of equations."));
        lst.add(mkETT("3P10a", EXA));
        lst.add(mkETT("3P10b", EXB));

        // 1: Polynomial Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkPreVid("311OV", OVVIDPRE));
        lst.add(mkPrePdf("311OV", OVPDF));

        lst.add(mkPreVid("IV1.1.1", "Supplemental: Polynomial Functions"));
        lst.add(mkPreVid("IV1.1.2", "Supplemental: Evaluating Polynomial Functions"));
        lst.add(mkPreVid("IV1.1.3", "Supplemental: Interpreting Polynomial Functions"));
        lst.add(mkPreVid("TV1.1", "Technology Supplement: Evaluating polynomials using the calculator"));

        lst.add(mkLH2("1: Polynomial Functions", O1_1, "Identify and graph polynomial functions"));
        lst.add(mkSEC("1.1.1", "Determine if a function is a polynomial function."));
        lst.add(mkETT("3111E1", EX1));
        lst.add(mkETT("3111E2", EX2));
        lst.add(mkETT("3111E3", EX3));
        lst.add(mkETT("3111T1", TT1));
        lst.add(mkETT("3111T2", TT2));
        lst.add(mkETT("3111T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Polynomial Functions", O1_2, "Identify and graph polynomial functions"));
        lst.add(mkSEC("1.2.1", "Evaluate polynomial functions."));
        lst.add(mkETT("3112E1", EX1));
        lst.add(mkETT("3112E2", EX2));
        lst.add(mkETT("3112E3", EX3));
        lst.add(mkETT("3112T1", TT1));
        lst.add(mkETT("3112T2", TT2));
        lst.add(mkETT("3112T3", TT3));
        lst.add(mkSEC("1.2.2", "Graph polynomial functions."));
        lst.add(mkETT("3113E1", EX1));
        lst.add(mkETT("3113E2", EX2));
        lst.add(mkETT("3113T1", TT1));
        lst.add(mkETT("3113T2", TT2));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkPreVid("312OV", OVVIDPRE));
        lst.add(mkPrePdf("312OV", OVPDF));

        lst.add(mkPreVid("IV1.2.1", "Supplemental: Writing Polynomials in Factored Form"));
        lst.add(mkPreVid("IV1.2.2", "Supplemental: Build Polynomials in Factored Form"));
        lst.add(mkPreVid("TV1.2", "Technology Supplement: Verfying zeros with the calculator"));

        lst.add(mkLH2("1: Polynomial Functions", O1_3, "Build polynomial functions"));
        lst.add(mkSEC("1.3.1", "Graph polynomials in factored form."));
        lst.add(mkETT("3121E1", EX1));
        lst.add(mkETT("3121E2", EX2));
        lst.add(mkETT("3121T1", TT1));
        lst.add(mkETT("3121T2", TT2));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Polynomial Functions", O1_4, "Build polynomial functions"));
        lst.add(mkSEC("1.4.1", "Construct a polynomial with given zeros."));
        lst.add(mkETT("3122E1", EX1));
        lst.add(mkETT("3122E2", EX2));
        lst.add(mkETT("3122T1", TT1));
        lst.add(mkETT("3122T2", TT2));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkPreVid("313OV", OVVIDPRE));
        lst.add(mkPrePdf("313OV", OVPDF));

        lst.add(mkPreVid("IV1.3.1", "Supplemental: Factoring Polynomials by Grouping"));
        lst.add(mkPreVid("IV1.3.2.1", "Supplemental: Factoring Polynomials by Finding Rational Zeros"));
        lst.add(mkPreVid("IV1.3.2.2", "Supplemental: Factoring Polynomials Using Long or Synthetic Division"));
        lst.add(mkPreVid("TV1.3", "Technology Supplement: Verfying zeros with the calculator"));

        lst.add(mkLH2("1: Polynomial Functions", O1_5, "Determine zeros of polynomial functions"));
        lst.add(mkSEC("1.5.1", "Factor polynomials to determine zeros."));
        lst.add(mkETT("3131E1", EX1));
        lst.add(mkETT("3131E2", EX2));
        lst.add(mkETT("3131T1", TT1));
        lst.add(mkETT("3131T2", TT2));
        lst.add(mkSEC("1.5.2", "Find all zeros of a given polynomial function."));
        lst.add(mkETT("3132E1", EX1));
        lst.add(mkETT("3132E2", EX2));
        lst.add(mkETT("3132E3", EX3));
        lst.add(mkETT("3132E4", EX4));
        lst.add(mkETT("3132T1", TT1));
        lst.add(mkETT("3132T2", TT2));
        lst.add(mkETT("3132T3", TT3));
        lst.add(mkETT("3132T4", TT4));

        startLesson(prefix + ".16", 1, 6);
        lst.add(mkPreVid("314OV", OVVIDPRE));
        lst.add(mkPrePdf("314OV", OVPDF));

        lst.add(mkPreVid("IV1.4.1", "Supplemental: Solving Polynomial Equations and Inequalities"));
        lst.add(mkPreVid("IV1.4.2", "Supplemental: Finding Average Rate of Change"));
        lst.add(mkPreVid("TV1.4", "Technology Supplement: Finding Intersection of Lines and Local Min and Max"));

        lst.add(mkLH2("1: Polynomial Functions", O1_6, "Solve problems with polynomials"));
        lst.add(mkSEC("1.6.1", "Solve polynomial equations and inequalities."));
        lst.add(mkETT("3141E1", EX1));
        lst.add(mkETT("3141E2", EX2));
        lst.add(mkETT("3141T1", TT1));
        lst.add(mkETT("3141T2", TT2));

        startLesson(prefix + ".17", 1, 7);
        lst.add(mkLH2("1: Polynomial Functions", O1_7, "Solve problems with polynomials"));
        lst.add(mkSEC("1.7.1", "Solve max/min problems."));
        lst.add(mkETT("3142E1", EX1));
        lst.add(mkETT("3142E2", EX2));
        lst.add(mkETT("3142T1", TT1));

        startLesson(prefix + ".18", 1, 8);
        lst.add(mkLH2("1: Polynomial Functions", O1_8, "Solve problems with polynomials"));
        lst.add(mkSEC("1.8.1", "Compute average and instantaneous rate of change."));
        lst.add(mkETT("3143E1", EX1));
        lst.add(mkETT("3143E2", EX2));
        lst.add(mkETT("3143E3", EX3));
        lst.add(mkETT("3143E4", EX4));
        lst.add(mkETT("3143T1", TT1));
        lst.add(mkETT("3143T2", TT2));
        lst.add(mkETT("3143T3", TT3));
        lst.add(mkETT("3143T4", TT4));

        startLesson(prefix + ".19", 1, 9);
        lst.add(mkPreVid("315OV", OVVIDPRE));
        lst.add(mkPrePdf("315OV", OVPDF));

        lst.add(mkPreVid("IV1.5.1", "Supplemental: Using Differences to find Polynomials, Part 1"));
        lst.add(mkPreVid("IV1.5.2", "Supplemental: Using Differences to find Polynomials, Part 2"));
        lst.add(mkPreVid("IV1.5.3", "Supplemental: Polynomial Regression"));
        lst.add(mkPreVid("TV1.5", "Technology Supplement: Finding Differences with the Calculator"));

        lst.add(mkLH2("1: Polynomial Functions", O1_9, "Determine polynomial functions for data sets."));
        lst.add(mkSEC("1.9.1", "Given polynomial data, determine the degree and the model."));
        lst.add(mkETT("3151E1", EX1));
        lst.add(mkETT("3151E2", EX2));
        lst.add(mkETT("3151E3", EX3));
        lst.add(mkETT("3151T1", TT1));
        lst.add(mkETT("3151T2", TT2));

        startLesson(prefix + ".110", 1, 10);
        lst.add(mkLH2("1: Polynomial Functions", O1_10, "Determine polynomial functions for data sets."));
        lst.add(mkSEC("1.10.1", "Use regression to determine polynomial models."));
        lst.add(mkETT("3152E1", EX1));
        lst.add(mkETT("3152E2", EX2));
        lst.add(mkETT("3152T1", TT1));
        lst.add(mkETT("3152T2", TT2));

        // 2: Rational Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPreVid("321OV", OVVIDPRE));
        lst.add(mkPrePdf("321OV", OVPDF));

        lst.add(mkPreVid("IV2.1.1", "Supplemental: Evaluating Rational Functions"));
        lst.add(mkPreVid("IV2.1.2", "Supplemental: Graphing Rational Functions"));
        lst.add(mkPreVid("TV2.1", "Technology Supplement: Verifying Intercepts, Zeros, Asymptotes"));

        lst.add(mkLH2("2: Rational Functions", O2_1, "Determine local behavior of rational functions"));
        lst.add(mkSEC("2.1.1", "Evaluate rational functions."));
        lst.add(mkETT("3211E1", EX1));
        lst.add(mkETT("3211E2", EX2));
        lst.add(mkETT("3211T1", TT1));
        lst.add(mkETT("3211T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Rational Functions", O2_2, "Determine local behavior of rational functions"));
        lst.add(mkSEC("2.2.1", "Graph rational functions."));
        lst.add(mkETT("3212E1", EX1));
        lst.add(mkETT("3212E2", EX2));
        lst.add(mkETT("3212T1", TT1));
        lst.add(mkETT("3212T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkPreVid("322OV", OVVIDPRE));
        lst.add(mkPrePdf("322OV", OVPDF));

        lst.add(mkPreVid("IV2.2.1", "Supplemental: Horizontal Asymptotes, Part 1"));
        lst.add(mkPreVid("IV2.2.2", "Supplemental: Horizontal Asymptotes, Part 2"));
        lst.add(mkPreVid("IV2.2.3", "Supplemental: Slant/Oblique Asymptotes"));
        lst.add(mkPreVid("TV2.2", "Technology Supplement: End-Behavior of Rational Functions"));

        lst.add(mkLH2("2: Rational Functions", O2_3,
                "Determine asymptotic and end-behavior properties of rational functions"));
        lst.add(mkSEC("2.3.1", "Determine horizontal asymptotes and sketch a graph."));
        lst.add(mkETT("3221E1", EX1));
        lst.add(mkETT("3221E2", EX2));
        lst.add(mkETT("3221T1", TT1));
        lst.add(mkETT("3221T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Rational Functions", O2_4,
                "Determine asymptotic and end-behavior properties of rational functions"));
        lst.add(mkSEC("2.4.1", "Determine oblique asymptotes and end behavior."));
        lst.add(mkETT("3222E1", EX1));
        lst.add(mkETT("3222E2", EX2));
        lst.add(mkETT("3222T1", TT1));
        lst.add(mkETT("3222T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkPreVid("323OV", OVVIDPRE));
        lst.add(mkPrePdf("323OV", OVPDF));

        lst.add(mkPreVid("IV2.3.1", "Supplemental: Building Rational Functions, Part 1"));
        lst.add(mkPreVid("IV2.3.2", "Supplemental: Building Rational Functions, Part 2"));
        lst.add(mkPreVid("IV2.3.3", "Supplemental: Building Rational Functions, Part 3"));
        lst.add(mkPreVid("TV2.3", "Technology Supplement: Verifying Rational Functions"));

        lst.add(mkLH2("2: Rational Functions", O2_5, "Build rational functions"));
        lst.add(mkSEC("2.5.1", "Construct rational functions with horizontal asymptotes."));
        lst.add(mkETT("3231E1", EX1));
        lst.add(mkETT("3231E2", EX2));
        lst.add(mkETT("3231T1", TT1));
        lst.add(mkETT("3231T2", TT2));

        startLesson(prefix + ".26", 2, 6);
        lst.add(mkLH2("2: Rational Functions", O2_6, "Build rational functions"));
        lst.add(mkSEC("2.6.1", "Construct rational functions with slant asymptotes."));
        lst.add(mkETT("3232E1", EX1));
        lst.add(mkETT("3232E2", EX2));
        lst.add(mkETT("3232T1", TT1));
        lst.add(mkETT("3232T2", TT2));

        startLesson(prefix + ".27", 2, 7);
        lst.add(mkPreVid("324OV", OVVIDPRE));
        lst.add(mkPrePdf("324OV", OVPDF));

        lst.add(mkPreVid("IV2.4.1", "Supplemental: Solving Rational Equations"));
        lst.add(mkPreVid("IV2.4.2", "Supplemental: Solving Rational Inequalities"));
        lst.add(mkPreVid("TV2.4", "Technology Supplement: Verifying Solutions to Rational Equations"));

        lst.add(mkLH2("2: Rational Functions", O2_7, "Solve rational equations and inequalities"));
        lst.add(mkSEC("2.7.1", "Solve rational equations."));
        lst.add(mkETT("3241E1", EX1));
        lst.add(mkETT("3241E2", EX2));
        lst.add(mkETT("3241T1", TT1));
        lst.add(mkETT("3241T2", TT2));

        startLesson(prefix + ".28", 2, 8);
        lst.add(mkLH2("2: Rational Functions", O2_8, "Solve rational equations and inequalities"));
        lst.add(mkSEC("2.8.1", "Solve rational inequalities."));
        lst.add(mkETT("3242E1", EX1));
        lst.add(mkETT("3242E2", EX2));
        lst.add(mkETT("3242T1", TT1));
        lst.add(mkETT("3242T2", TT2));

        startLesson(prefix + ".29", 2, 9);
        lst.add(mkLH2("2: Rational Functions", O2_9, "Model with rational functions"));

        lst.add(mkPreVid("325OV", OVVID));

        lst.add(mkPreVid("IV2.5.1", "Supplemental: Solving Mixture Problems with Rational Equations"));
        lst.add(mkPreVid("IV2.5.2", "Supplemental: Solving Population Problems with Rational Equations"));
        lst.add(mkPreVid("TV2.5", "Technology Supplement: Solving Rational Equations with the Calculator"));

        lst.add(mkSEC("2.9.1", "Solve application problems I."));
        lst.add(mkETT("3251E1", EX1));
        lst.add(mkETT("3251E2", EX2));
        lst.add(mkETT("3251T1", TT1));
        lst.add(mkETT("3251T2", TT2));

        startLesson(prefix + ".210", 2, 10);
        lst.add(mkLH2("2: Rational Functions", O2_10, "Model with rational functions"));
        lst.add(mkSEC("2.10.1", "Solve application problems II."));
        lst.add(mkETT("3252E1", EX1));
        lst.add(mkETT("3252E2", EX2));
        lst.add(mkETT("3252T1", TT1));
        lst.add(mkETT("3252T2", TT2));

        // 3: Radical Functions and Equations
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPreVid("331OV", OVVIDPRE));
        lst.add(mkPrePdf("331OV", OVPDF));

        lst.add(mkPreVid("IV3.1.1", "Supplemental: Evaluating Square Root Functions"));
        lst.add(mkPreVid("IV3.1.2", "Supplemental: Graphing Square Root Functions"));
        lst.add(mkPreVid("TV3.1", "Technology Supplement: Graphs and Zeros of Square Root Functions"));

        lst.add(mkLH2("3: Radical Functions and Equations", O3_1, "Graph square root functions"));
        lst.add(mkSEC("3.1.1", "Evaluate square root functions."));

        lst.add(mkETT("3311E1", EX1));
        lst.add(mkETT("3311E2", EX2));
        lst.add(mkETT("3311T1", TT1));
        lst.add(mkETT("3311T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_2, "Graph square root functions"));
        lst.add(mkSEC("3.2.1", "Graph and interpret square root functions."));
        lst.add(mkETT("3312E1", EX1));
        lst.add(mkETT("3312E2", EX2));
        lst.add(mkETT("3312E3", EX3));
        lst.add(mkETT("3312T1", TT1));
        lst.add(mkETT("3312T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkPreVid("332OV", OVVIDPRE));
        lst.add(mkPrePdf("332OV", OVPDF));

        lst.add(mkPreVid("IV3.2.1", "Supplemental: Evaluating Radical Functions"));
        lst.add(mkPreVid("IV3.2.2", "Supplemental: Graphing Radical Functions"));
        lst.add(mkPreVid("TV3.2", "Technology Supplement: Graphs of Radical Functions"));

        lst.add(mkLH2("3: Radical Functions and Equations", O3_3, "Graph other root functions"));
        lst.add(mkSEC("3.3.1", "Evaluate radical functions."));
        lst.add(mkETT("3321E1", EX1));
        lst.add(mkETT("3321E2", EX2));
        lst.add(mkETT("3321T1", TT1));
        lst.add(mkETT("3321T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_4, "Graph other root functions"));
        lst.add(mkSEC("3.4.1", "Graph and interpret radical functions."));
        lst.add(mkETT("3322E1", EX1));
        lst.add(mkETT("3322E2", EX2));
        lst.add(mkETT("3322T1", TT1));
        lst.add(mkETT("3322T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkPreVid("333OV", OVVIDPRE));
        lst.add(mkPrePdf("333OV", OVPDF));

        lst.add(mkPreVid("IV3.3.1", "Supplemental: Solving Radical Equations, Part 1"));
        lst.add(mkPreVid("IV3.3.2", "Supplemental: Solving Radical Equations, Part 2"));
        lst.add(mkPreVid("TV3.3", "Technology Supplement: Verifying Solutions to Radical Equations"));

        lst.add(mkLH2("3: Radical Functions and Equations", O3_5, "Solve radical equations"));
        lst.add(mkSEC("3.5.1", "Solve square root equations."));
        lst.add(mkETT("3331E1", EX1));
        lst.add(mkETT("3331E2", EX2));
        lst.add(mkETT("3331T1", TT1));
        lst.add(mkETT("3331T2", TT2));

        startLesson(prefix + ".36", 3, 6);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_6, "Solve radical equations"));
        lst.add(mkSEC("3.6.1", "Solve other radical equations."));
        lst.add(mkETT("3332E1", EX1));
        lst.add(mkETT("3332E2", EX2));
        lst.add(mkETT("3332T1", TT1));
        lst.add(mkETT("3332T2", TT2));

        startLesson(prefix + ".37", 3, 7);
        lst.add(mkPreVid("334OV", OVVIDPRE));
        lst.add(mkPrePdf("334OV", OVPDF));

        lst.add(mkPreVid("IV3.4.1", "Supplemental: Solving Radical Inequalities, Part 1"));
        lst.add(mkPreVid("IV3.4.2", "Supplemental: Solving Radical Inequalities, Part 2"));
        lst.add(mkPreVid("TV3.4", "Technology Supplement:Solving Radical Inequalities with the Calculator"));

        lst.add(mkLH2("3: Radical Functions and Equations", O3_7, "Solve radical inequalities"));
        lst.add(mkSEC("3.7.1", "Solve square root inequalities."));
        lst.add(mkETT("3341E1", EX1));
        lst.add(mkETT("3341E2", EX2));
        lst.add(mkETT("3341T1", TT1));
        lst.add(mkETT("3341T2", TT2));

        startLesson(prefix + ".38", 3, 8);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_8, "Solve radical inequalities"));
        lst.add(mkSEC("3.8.1", "Solve other radical inequalities."));
        lst.add(mkETT("3342E1", EX1));
        lst.add(mkETT("3342E2", EX2));
        lst.add(mkETT("3342T1", TT1));
        lst.add(mkETT("3342T2", TT2));

        startLesson(prefix + ".39", 3, 9);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_9, "Model with radical functions"));
        lst.add(mkSEC("3.9.1", "Solve radical applications I."));

        lst.add(mkPreVid("335OV", OVVID));
        lst.add(mkPreVid("IV3.5.1", "Supplemental: Solving Pendulum Problems using Radical Functions"));
        lst.add(mkPreVid("IV3.5.2", "Supplemental: Solving Minimizing Problems using Radical Functions"));
        lst.add(mkPreVid("TV3.5", "Supplemental: Solving Pendulum Problems with the Calculator"));

        lst.add(mkETT("3351E1", EX1));
        lst.add(mkETT("3351E2", EX2));
        lst.add(mkETT("3351T1", TT1));
        lst.add(mkETT("3351T2", TT2));

        startLesson(prefix + ".310", 3, 10);
        lst.add(mkLH2("3: Radical Functions and Equations", O3_10, "Model with radical functions"));
        lst.add(mkSEC("3.10.1", "Solve radical applications II."));
        lst.add(mkETT("3352E1", EX1));
        lst.add(mkETT("3352E2", EX2));
        lst.add(mkETT("3352T1", TT1));
        lst.add(mkETT("3352T2", TT2));

        // 4: Power Functions, Operations and Systems
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkPreVid("341OV", OVVIDPRE));
        lst.add(mkPrePdf("341OV", OVPDF));

        lst.add(mkPreVid("IV4.1.1", "Supplemental: Evaluating Power Functions, Part 1"));
        lst.add(mkPreVid("IV4.1.2", "Supplemental: Evaluating Power Functions, Part 2"));
        lst.add(mkPreVid("TV4.1", "Supplemental: Evaluating Power Functions with the Calculator"));

        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_1, "Evaluate power functions"));
        lst.add(mkSEC("4.1.1", "Evaluate power functions I."));
        lst.add(mkETT("3411E1", EX1));
        lst.add(mkETT("3411E2", EX2));
        lst.add(mkETT("3411T1", TT1));
        lst.add(mkETT("3411T2", TT2));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_2, "Evaluate power functions"));
        lst.add(mkSEC("4.2.1", "Evaluate power functions II."));
        lst.add(mkETT("3412E1", EX1));
        lst.add(mkETT("3412E2", EX2));
        lst.add(mkETT("3412T1", TT1));
        lst.add(mkETT("3412T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkPreVid("342OV", OVVIDPRE));
        lst.add(mkPrePdf("342OV", OVPDF));

        lst.add(mkPreVid("IV4.2.1", "Supplemental: Graphing Power Functions, Part 1"));
        lst.add(mkPreVid("IV4.2.2", "Supplemental: Graphing Power Functions, Part 2"));
        lst.add(mkPreVid("TV4.2", "Supplemental: Graphing Power Functions with the Calculator"));

        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_3, "Graph power functions"));
        lst.add(mkSEC("4.3.1", "Graph  power functions I."));
        lst.add(mkETT("3421E1", EX1));
        lst.add(mkETT("3421E2", EX2));
        lst.add(mkETT("3421T1", TT1));
        lst.add(mkETT("3421T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_4, "Graph power functions"));
        lst.add(mkSEC("4.4.1", "Graph power functions II."));
        lst.add(mkETT("3422E1", EX1));
        lst.add(mkETT("3422E2", EX2));
        lst.add(mkETT("3422T1", TT1));
        lst.add(mkETT("3422T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkPreVid("343OV", OVVIDPRE));
        lst.add(mkPrePdf("343OV", OVPDF));

        lst.add(mkPreVid("IV4.3.1", "Supplemental: Solving Equations with Power Functions"));
        lst.add(mkPreVid("IV4.3.2", "Supplemental: Solving Inequalities with Power Functions"));
        lst.add(mkPreVid("TV4.3", "Supplemental: Verifying Inequalities Involving Power Functions"));

        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_5,
                "Solve equations and inequalities with power functions"));
        lst.add(mkSEC("4.5.1", "Solve equations with power functions."));
        lst.add(mkETT("3431E1", EX1));
        lst.add(mkETT("3431E2", EX2));
        lst.add(mkETT("3431T1", TT1));
        lst.add(mkETT("3431T2", TT2));

        startLesson(prefix + ".46", 4, 6);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_6,
                "Solve equations and inequalities with power functions"));
        lst.add(mkSEC("4.6.1", "Solve inequalities with power functions."));
        lst.add(mkETT("3432E1", EX1));
        lst.add(mkETT("3432E2", EX2));
        lst.add(mkETT("3432T1", TT1));
        lst.add(mkETT("3432T2", TT2));

        startLesson(prefix + ".47", 4, 7);
        lst.add(mkPreVid("344OV", OVVIDPRE));
        lst.add(mkPrePdf("344OV", OVPDF));

        lst.add(mkPreVid("TV4.4", "Supplemental: Power Regression"));

        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_7, "Solve power function applications"));
        lst.add(mkSEC("4.7.1", "Solve applications with power functions."));
        lst.add(mkETT("3441E1", EX1));
        lst.add(mkETT("3441E2", EX2));
        lst.add(mkETT("3441T1", TT1));
        lst.add(mkETT("3441T2", TT2));

        startLesson(prefix + ".48", 4, 8);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_8, "Solve power function applications"));
        lst.add(mkSEC("4.8.1", "Solve applications with power regression."));
        lst.add(mkETT("3442E1", EX1));
        lst.add(mkETT("3442E2", EX2));
        lst.add(mkETT("3442T1", TT1));
        lst.add(mkETT("3442T2", TT2));

        startLesson(prefix + ".49", 4, 9);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_9, "Solve systems with nonlinear functions"));
        lst.add(mkSEC("4.9.1", "Solve systems with nonlinear functions algebraically."));
        lst.add(mkETT("3451E1", EX1));
        lst.add(mkETT("3451T1", TT1));
        lst.add(mkETT("3451T2", TT2));

        startLesson(prefix + ".410", 4, 10);
        lst.add(mkLH2("4: Power Functions, Operations and Systems", O4_10, "Solve systems with nonlinear functions"));
        lst.add(mkSEC("4.10.1", "Solve systems with nonlinear functions graphically."));
        lst.add(mkETT("3452E1", EX1));
        lst.add(mkETT("3452E2", EX2));
        lst.add(mkETT("3452T1", TT1));
        lst.add(mkETT("3452T2", TT2));
    }

    /**
     * Builds lesson components for MATH 124.
     *
     * @param lst the list to which to add components
     */
    private static void build124(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M124;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Multiply and factor polynomials."));
        lst.add(mkETT("4P1a", EXA));
        lst.add(mkETT("4P1b", EXB));
        lst.add(mkSEC("2", "Solve quadratic equations."));
        lst.add(mkETT("4P2a", EXA));
        lst.add(mkETT("4P2b", EXB));
        lst.add(mkETT("4P2c", EXC));
        lst.add(mkSEC("3", "Determine the vertex for a quadratic."));
        lst.add(mkETT("4P3a", EXA));
        lst.add(mkETT("4P3b", EXB));
        lst.add(mkSEC("4", "Evaluate functions."));
        lst.add(mkETT("4P4a", EXA));
        lst.add(mkETT("4P4b", EXB));
        lst.add(mkETT("4P4c", EXC));
        lst.add(mkSEC("5", "Determine the equation of a line in standard form."));
        lst.add(mkETT("4P5a", EXA));
        lst.add(mkETT("4P5b", EXB));
        lst.add(mkSEC("6", "Evaluate exponents."));
        lst.add(mkETT("4P6a", EXA));
        lst.add(mkETT("4P6b", EXB));
        lst.add(mkSEC("7", "Products and powers of exponents."));
        lst.add(mkETT("4P7a", EXA));
        lst.add(mkETT("4P7b", EXB));
        lst.add(mkSEC("8", "Simplify expressions with integer exponents."));
        lst.add(mkETT("4P8a", EXA));
        lst.add(mkETT("4P8b", EXB));
        lst.add(mkSEC("9", "Simplify expressions with rational exponents."));
        lst.add(mkETT("4P9a", EXA));
        lst.add(mkETT("4P9b", EXB));
        lst.add(mkSEC("10", "Linear regression."));
        lst.add(mkETT("4P10", EXA));

        // 1: Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkLH2("1: Functions", O1_1, "Evaluate and graph relations and functions"));
        lst.add(mkVid("411OV", OVVID));
        lst.add(mkPdf("411OV", OVPDF));

        lst.add(mkVid("IV1.1.1", "Supplemental: Relations and Functions"));
        lst.add(mkVid("IV1.1.2", "Supplemental: Evaluating and Graphing Functions"));
        lst.add(mkVid("IV1.1.3", "Supplemental: Findind Domain an Range of Functions"));
        lst.add(mkVid("TV1.1", "Technology Supplement: Evaluating and finding domain and range using the calculator"));

        lst.add(mkSEC("1.1.1", "Determine whether or not a relation is a function."));
        lst.add(mkETT("4111E1", EX1));
        lst.add(mkETT("4111E2", EX2));
        lst.add(mkETT("4111E3", EX3));
        lst.add(mkETT("4111T1", TT1));
        lst.add(mkETT("4111T2", TT2));
        lst.add(mkETT("4111T3", TT3));
        lst.add(mkSEC("1.1.2", "Build a table and graph for each function."));
        lst.add(mkETT("4112E1", EX1));
        lst.add(mkETT("4112E2", EX2));
        lst.add(mkETT("4112T1", TT1));
        lst.add(mkETT("4112T2", TT2));
        lst.add(mkETT("4112T3", TT3));
        lst.add(mkSEC("1.1.3", "Identify the domain and range of a function."));
        lst.add(mkETT("4113E1", EX1));
        lst.add(mkETT("4113E2", EX2));
        lst.add(mkETT("4113E3", EX3));
        lst.add(mkETT("4113T1", TT1));
        lst.add(mkETT("4113T2", TT2));
        lst.add(mkETT("4113T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Functions", O1_2, "Evaluate functions to determine average rates of change"));
        lst.add(mkVid("412OV", OVVID));
        lst.add(mkPdf("412OV", OVPDF));

        lst.add(mkVid("IV1.2.1", "Supplemental: Computing Average Rages of Change"));
        lst.add(mkVid("IV1.2.2part1", "Supplemental: Rate of change of difference quotients, part 1"));
        lst.add(mkVid("IV1.2.2part2", "Supplemental: Rate of change of difference quotients, part 2"));
        lst.add(mkVid("TV1.2", "Technology Supplement: Evaluating functions at particular x values"));

        lst.add(mkSEC("1.2.1", "Given a function determine the average rate of change between two points."));
        lst.add(mkETT("4121E1", EX1));
        lst.add(mkETT("4121E2", EX2));
        lst.add(mkETT("4121T1", TT1));
        lst.add(mkETT("4121T2", TT2));
        lst.add(mkETT("4121T3", TT3));
        lst.add(mkSEC("1.2.2", "Given a function, compute and simplify <em>[f(x + h) - f(x)] / h</em>."));
        lst.add(mkVid("4122E0", "Introductory Lesson"));
        lst.add(mkETT("4122E1", EX1));
        lst.add(mkETT("4122E2", EX2));
        lst.add(mkETT("4122T1", TT1));
        lst.add(mkETT("4122T2", TT2));
        lst.add(mkETT("4122T3", TT3));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Functions", O1_3, "Solve equations related to functions"));
        lst.add(mkVid("413OV", OVVID));
        lst.add(mkPdf("413OV", OVPDF));
        lst.add(mkSEC("1.3.1", "Given an output, solve for the input algebraically."));
        lst.add(mkETT("4131E1", EX1));
        lst.add(mkETT("4131E2", EX2));
        lst.add(mkETT("4131T1", TT1));
        lst.add(mkETT("4131T2", TT2));
        lst.add(mkSEC("1.3.2", "Given an output, solve for the input graphically."));
        lst.add(mkETT("4132E1", EX1));
        lst.add(mkETT("4132E2", EX2));
        lst.add(mkETT("4132T1", TT1));
        lst.add(mkETT("4132T2", TT2));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Functions", O1_4, "Perform operations on functions"));
        lst.add(mkVid("414OV", OVVID));
        lst.add(mkPdf("414OV", OVPDF));
        lst.add(mkSEC("1.4.1", "Perform basic operations on functions."));
        lst.add(mkETT("4141E1", EX1));
        lst.add(mkETT("4141E2", EX2));
        lst.add(mkETT("4141T1", TT1));
        lst.add(mkETT("4141T2", TT2));
        lst.add(mkSEC("1.4.2", "Given two functions, <em>f</em> and <em>g</em>, find and compare "
                               + "<em>(f <small>o</small> g)(x)</em> and <em>(g <small>o</small> f)(x)</em>."));
        lst.add(mkETT("4142E1", EX1));
        lst.add(mkETT("4142E2", EX2));
        lst.add(mkETT("4142T1", TT1));
        lst.add(mkETT("4142T2", TT2));
        lst.add(mkSEC("1.4.3", "Given a function write it as the composition of two functions."));
        lst.add(mkETT("4143E1", EX1));
        lst.add(mkETT("4143E2", EX2));
        lst.add(mkETT("4143T1", TT1));
        lst.add(mkETT("4143T2", TT2));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Functions", O1_5, "Given a function, find its inverse"));
        lst.add(mkVid("415OV", OVVID));
        lst.add(mkPdf("415OV", OVPDF));

        lst.add(mkVid("IV1.5.1", "Supplemental: Inverses of Relations"));
        lst.add(mkVid("IV1.5.2", "Supplemental: Inverse Functions"));
        lst.add(mkVid("IV1.5.3", "Supplemental: Determine the Inverse of a Function"));
        lst.add(mkVid("TV1.2", "Technology Supplement: Graphing a function and its inverse"));

        lst.add(mkSEC("1.5.1", "Identify the inverse of a function numerically and graphically."));
        lst.add(mkETT("4151E1", EX1));
        lst.add(mkETT("4151T1", TT1));
        lst.add(mkSEC("1.5.2", "Given a table, graph or rule for a function, determine whether or not its "
                               + "inverse is a function."));
        lst.add(mkETT("4152E1", EX1));
        lst.add(mkETT("4152E2", EX2));
        lst.add(mkETT("4152E3", EX3));
        lst.add(mkETT("4152T1", TT1));
        lst.add(mkETT("4152T2", TT2));
        lst.add(mkETT("4152T3", TT3));
        lst.add(mkSEC("1.5.3", "Given <em>f</em>, determine <em>f<sup>-1</sup></em> and compose "
                               + "<em>f</em> with <em>f<sup>-1</sup></em> and <em>f<sup>-1</sup></em> with <em>f</em>" +
                               "."));
        lst.add(mkETT("4153E1", EX1));
        lst.add(mkETT("4153E2", EX2));
        lst.add(mkETT("4153T1", TT1));
        lst.add(mkETT("4153T2", TT2));
        lst.add(mkETT("4153T3", TT3));

        // 2: Introduction to Exponential and Logarithmic Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_1, "Write and evaluate functions of the form <em>y = ab<sup>x</sup></em>."));
        lst.add(mkVid("421OV", OVVID));
        lst.add(mkPdf("421OV", OVPDF));

        lst.add(mkVid("IV2.1.1part1", "Supplemental: Writing and Evaluating Exponential Functions"));
        lst.add(mkVid("IV2.1.1part2", "Supplemental: Computing Compound Interest"));
        lst.add(mkVid("IV2.1.2", "Supplemental: Determine Exponential Model from a Table"));
        lst.add(mkVid("TV2.1", "Technology Supplement: Exponential Models and Models of Growth and Decay"));

        lst.add(mkSEC("2.1.1",
                "Given a growth or decay situation write an exponential function to evaluate it for a given input."));
        lst.add(mkETT("4211E1", EX1));
        lst.add(mkETT("4211E2", EX2));
        lst.add(mkETT("4211E3", EX3));
        lst.add(mkETT("4211T1", TT1));
        lst.add(mkETT("4211T2", TT2));
        lst.add(mkETT("4211T3", TT3));
        lst.add(mkSEC("2.1.2",
                "Given a table of data, determine an exponential function and evaluate it for a given input."));
        lst.add(mkETT("4212E1", EX1));
        lst.add(mkETT("4212E2", EX2));
        lst.add(mkETT("4212T1", TT1));
        lst.add(mkETT("4212T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_2, "Graph and interpret graphs of functions of the form <em>y = ab<sup>x</sup></em>."));
        lst.add(mkVid("422OV", OVVID));
        lst.add(mkPdf("422OV", OVPDF));

        lst.add(mkVid("IV2.2.1", "Supplemental: Graphing Exponential Functions"));
        lst.add(mkVid("IV2.2.2", "Supplemental: Determine Exponential Function from a Graph"));
        lst.add(mkVid("IV2.2.3", "Supplemental: Solving Exponential Equations Algebraically and Graphically"));
        lst.add(mkVid("TV2.2", "Technology Supplement: Verifying Exponenial Models"));

        lst.add(mkSEC("2.2.1", "Given an exponential function, identify its graph."));
        lst.add(mkETT("4221E1", EX1));
        lst.add(mkETT("4221E2", EX2));
        lst.add(mkETT("4221E3", EX3));
        lst.add(mkETT("4221T1", TT1));
        lst.add(mkETT("4221T2", TT2));
        lst.add(mkETT("4221T3", TT3));
        lst.add(mkSEC("2.2.2", "Given the graph of an exponential function, identify the equation."));
        lst.add(mkVid("4222E0", "Introductory Lesson"));
        lst.add(mkETT("4222E1", EX1));
        lst.add(mkETT("4222E2", EX2));
        lst.add(mkETT("4222E3", EX3));
        lst.add(mkETT("4222T1", TT1));
        lst.add(mkETT("4222T2", TT2));
        lst.add(mkETT("4222T3", TT3));
        lst.add(mkSEC("2.2.3", "Solve exponential equations numerically and graphically."));
        lst.add(mkETT("4223E1", EX1));
        lst.add(mkETT("4223E2", EX2));
        lst.add(mkETT("4223E3", EX3));
        lst.add(mkETT("4223T1", TT1));
        lst.add(mkETT("4223T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_3, "Write and evaluate functions of the form <em>y =</em> log<sub><em>b</em></sub><em>x</em>."));
        lst.add(mkVid("423OV", OVVID));
        lst.add(mkPdf("423OV", OVPDF));
        lst.add(mkSEC("2.3.1",
                "Given an exponential function, determine its inverse function and build a table and graph."));
        lst.add(mkETT("4231E1", EX1));
        lst.add(mkETT("4231E2", EX2));
        lst.add(mkETT("4231T1", TT1));
        lst.add(mkETT("4231T2", TT2));
        lst.add(mkSEC("2.3.2", "Compute logarithms."));
        lst.add(mkETT("4232E1", EX1));
        lst.add(mkETT("4232E2", EX2));
        lst.add(mkETT("4232E3", EX3));
        lst.add(mkETT("4232E4", EX4));
        lst.add(mkETT("4232E5", EX5));
        lst.add(mkETT("4232E6", EX6));
        lst.add(mkETT("4232T1", TT1));
        lst.add(mkETT("4232T2", TT2));
        lst.add(mkETT("4232T3", TT3));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_4,
                "Interpret graphs of functions of the form <em>y</em> = log<sub><em>b</em></sub><em>x</em> and "
                + "solve equations."));
        lst.add(mkVid("424OV", OVVID));
        lst.add(mkPdf("424OV", OVPDF));
        lst.add(mkSEC("2.4.1", "Given the graph of a logarithmic function identify the equation."));
        lst.add(mkETT("4241E1", EX1));
        lst.add(mkETT("4241E2", EX2));
        lst.add(mkETT("4241T1", TT1));
        lst.add(mkETT("4241T2", TT2));
        lst.add(mkSEC("2.4.2", "Solve basic logarithmic equations algabraically."));
        lst.add(mkETT("4242E1", EX1));
        lst.add(mkETT("4242E2", EX2));
        lst.add(mkETT("4242E3", EX3));
        lst.add(mkETT("4242T1", TT1));
        lst.add(mkETT("4242T2", TT2));
        lst.add(mkETT("4242T3", TT3));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_5, "Interpreting <em>y = ae<sup>x</sup></em> and <em>y = </em> ln <em>x</em>."));
        lst.add(mkVid("425OV", OVVID));
        lst.add(mkPdf("425OV", OVPDF));
        lst.add(mkSEC("2.5.1", "Graph <em>y = ae<sup>x</sup></em> and <em>y =</em> ln <em>x</em>."));
        lst.add(mkETT("4251E1", EX1));
        lst.add(mkETT("4251E2", EX2));
        lst.add(mkSEC("2.5.2", "Solve problems using the model <em>y = ae<sup>x</sup></em>."));
        lst.add(mkETT("4252E1", EX1));
        lst.add(mkETT("4252E2", EX2));
        lst.add(mkETT("4252E3", EX3));
        lst.add(mkETT("4252T1", TT1));
        lst.add(mkETT("4252T2", TT2));

        // 3: Properties of Logarithms and Applications
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkLH2("3: Properties of Logarithms and Applications", O3_1,
                "Translate between exponential and logarithmic forms."));
        lst.add(mkVid("431OV", OVVID));
        lst.add(mkPdf("431OV", OVPDF));

        lst.add(mkVid("IV3.1.1", "Supplemental: Properties of Logarithms, Part 1"));
        lst.add(mkVid("IV3.1.2", "Supplemental: Properties of Logarithms, Part 2"));
        lst.add(mkVid("TV3.1", "Technology Supplement: Simplifying Logarithmic Expressions"));

        lst.add(mkSEC("3.1.1", "Simplify <em>b</em><sup>log<sub><em>b</em></sub> <em>u</em></sup>."));
        lst.add(mkETT("4311E1", EX1));
        lst.add(mkETT("4311E2", EX2));
        lst.add(mkETT("4311E3", EX3));
        lst.add(mkETT("4311T1", TT1));
        lst.add(mkETT("4311T2", TT2));
        lst.add(mkETT("4311T3", TT3));
        lst.add(mkSEC("3.1.2", "Simplify log<sub><em>b</em></sub> <em>b<sup>u</sup></em>."));
        lst.add(mkETT("4312E1", EX1));
        lst.add(mkETT("4312E2", EX2));
        lst.add(mkETT("4312E3", EX3));
        lst.add(mkETT("4312T1", TT1));
        lst.add(mkETT("4312T2", TT2));
        lst.add(mkETT("4312T3", TT3));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Properties of Logarithms and Applications", O3_2,
                "Develop and apply properties of logarithms I."));
        lst.add(mkVid("432OV", OVVID));
        lst.add(mkPdf("432OV", OVPDF));

        lst.add(mkVid("IV3.2.1", "Supplemental: Product Rule of Logarithms"));
        lst.add(mkVid("IV3.2.2", "Supplemental: Quotient Rule of Logarithms"));
        lst.add(mkVid("TV3.2", "Technology Supplement: Using the Calculator with the Quotient Rule of Logarithms"));

        lst.add(mkSEC("3.2.1", "Develop the product and quotient rules."));
        lst.add(mkETT("4321E1", EX1));
        lst.add(mkETT("4321E2", EX2));
        lst.add(mkSEC("3.2.2", "Apply the product and quotient rules."));
        lst.add(mkETT("4322E1", EX1));
        lst.add(mkETT("4322E2", EX2));
        lst.add(mkETT("4322E3", EX3));
        lst.add(mkETT("4322E4", EX4));
        lst.add(mkETT("4322E5", EX5));
        lst.add(mkETT("4322E6", EX6));
        lst.add(mkETT("4322T1", TT1));
        lst.add(mkETT("4322T2", TT2));
        lst.add(mkSEC("3.2.3", "Develop and apply the power rule."));
        lst.add(mkETT("4323E1", EX1));
        lst.add(mkETT("4323E2", EX2));
        lst.add(mkETT("4323E3", EX3));
        lst.add(mkETT("4323E4", EX4));
        lst.add(mkETT("4323E5", EX5));
        lst.add(mkETT("4323E6", EX6));
        lst.add(mkETT("4323T1", TT1));
        lst.add(mkETT("4323T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Properties of Logarithms and Applications", O3_3,
                "Develop and apply properties of logarithms II."));
        lst.add(mkVid("433OV", OVVID));
        lst.add(mkPdf("433OV", OVPDF));
        lst.add(mkSEC("3.3.1", "Use properties to rewrite logarithms."));
        lst.add(mkETT("4331E1", EX1));
        lst.add(mkETT("4331E2", EX2));
        lst.add(mkETT("4331E3", EX3));
        lst.add(mkETT("4331T1", TT1));
        lst.add(mkETT("4331T2", TT2));
        lst.add(mkETT("4331T3", TT3));
        lst.add(mkSEC("3.3.2", "Use properties to rewrite expressions as single logarithms."));
        lst.add(mkETT("4332E1", EX1));
        lst.add(mkETT("4332E2", EX2));
        lst.add(mkETT("4332E3", EX3));
        lst.add(mkETT("4332T1", TT1));
        lst.add(mkETT("4332T2", TT2));
        lst.add(mkETT("4332T3", TT3));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Properties of Logarithms and Applications", O3_4,
                "Derive and apply the base-changing formula."));
        lst.add(mkVid("434OV", OVVID));
        lst.add(mkPdf("434OV", OVPDF));
        lst.add(mkSEC("3.4.1", "Compute logarithms using the base changing formula."));
        lst.add(mkETT("4341E1", EX1));
        lst.add(mkETT("4341E2", EX2));
        lst.add(mkETT("4341E3", EX3));
        lst.add(mkETT("4341T1", TT1));
        lst.add(mkETT("4341T2", TT2));
        lst.add(mkETT("4341T3", TT3));
        lst.add(mkSEC("3.4.2", "Build tables and graphs of logarithmic functions in other bases."));
        lst.add(mkETT("4342E1", EX1));
        lst.add(mkETT("4342E2", EX2));
        lst.add(mkETT("4342T1", TT1));
        lst.add(mkETT("4342T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Properties of Logarithms and Applications", O3_5,
                "Convert between the forms <em>y = ab<sup>x</sup></em> and <em>y = ae<sup>kx</sup></em>."));
        lst.add(mkVid("435OV", OVVID));
        lst.add(mkPdf("435OV", OVPDF));
        lst.add(mkSEC("3.5.1",
                "Convert from the form <em>y = ab<sup>x</sup></em> to <em>y = ae<sup>kx</sup> and compare."));
        lst.add(mkETT("4351E1", EX1));
        lst.add(mkETT("4351E2", EX2));
        lst.add(mkETT("4351T1", TT1));
        lst.add(mkETT("4351T2", TT2));
        lst.add(mkSEC("3.5.2",
                "Convert from the form <em>y = ae<sup>kx</sup> to <em>y = ab<sup>x</sup></em> and interpret."));
        lst.add(mkETT("4352E1", EX1));
        lst.add(mkETT("4352E2", EX2));
        lst.add(mkETT("4352T1", TT1));
        lst.add(mkETT("4352T2", TT2));

        // 4: Modeling with Exponentials and Logarithms
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_1,
                "Solve exponential equations and their applications."));
        lst.add(mkVid("441OV", OVVID));
        lst.add(mkPdf("441OV", OVPDF));

        lst.add(mkVid("IV4.1.1", "Supplemental: Solving Equations of the Form b<sup>u</sup> = b<sup>v</sup>"));
        lst.add(mkVid("IV4.1.2", "Supplemental: Solving Equations Using Logarithms"));
        lst.add(mkVid("TV4.1", "Technology Supplement: Solving Exponential Equations Using the Calculator"));

        lst.add(mkSEC("4.1.1", "Simplify and solve equations of the form <em>b<sup>u</sup> = b<sup>v</sup></em>."));
        lst.add(mkETT("4411E1", EX1));
        lst.add(mkETT("4411E2", EX2));
        lst.add(mkETT("4411T1", TT1));
        lst.add(mkETT("4411T2", TT2));
        lst.add(mkSEC("4.1.2", "Solve exponential equations using logarithms or technology."));
        lst.add(mkETT("4412E1", EX1));
        lst.add(mkETT("4412E2", EX2, true, false));
        lst.add(mkETT("4412E3", EX3));
        lst.add(mkETT("4412T1", TT1));
        lst.add(mkETT("4412T2", TT2));
        lst.add(mkETT("4412T3", TT3));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_2,
                "Solve logarithmic equations and their applications."));
        lst.add(mkVid("442OV", OVVID));
        lst.add(mkPdf("442OV", OVPDF));

        lst.add(mkVid("IV4.2.1", "Supplemental: Solving Equations of the Form log<sub>b</sub> u = log<sub>b</sub> v"));
        lst.add(mkVid("IV4.2.2", "Supplemental: Solving Equations Using the Definition of Logarithm"));
        lst.add(mkVid("TV4.2", "Technology Supplement: Solving Logarithmic Equations Using the Calculator"));

        // TODO: Here
        // TODO: Here
        // TODO: Here
        // TODO: Here
        // TODO: Here

        lst.add(mkSEC("4.2.1", "Simplify and solve equations of the form "
                               + "log<sub><em>b</em></sub><em>u</em> = log<sub><em>b</em></sub><em>v</em>."));
        lst.add(mkETT("4421E1", EX1));
        lst.add(mkETT("4421E2", EX2));
        lst.add(mkETT("4421T1", TT1));
        lst.add(mkETT("4421T2", TT2));
        lst.add(mkSEC("4.2.2", "Solve equations of the form log<sub><em>b</em></sub><em>u</em> = <em>v</em>."));
        lst.add(mkETT("4422E1", EX1));
        lst.add(mkETT("4422E2", EX2));
        lst.add(mkETT("4422T1", TT1));
        lst.add(mkETT("4422T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_3,
                "Model real world situations with exponential models."));
        lst.add(mkVid("443OV", OVVID));
        lst.add(mkPdf("443OV", OVPDF));
        lst.add(mkSEC("4.3.1", "Solve growth and decay problems."));
        lst.add(mkETT("4431E1", EX1));
        lst.add(mkETT("4431E2", EX2));
        lst.add(mkETT("4431E3", EX3));
        lst.add(mkETT("4431T1", TT1));
        lst.add(mkETT("4431T2", TT2));
        lst.add(mkETT("4431T3", TT3));
        lst.add(mkSEC("4.3.2", "Fit an exponential function to data and solve related problems."));
        lst.add(mkETT("4432E1", EX1));
        lst.add(mkETT("4432T1", TT1));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_4,
                "Model real world situations with logarithmic models."));
        lst.add(mkVid("444OV", OVVID));
        lst.add(mkPdf("444OV", OVPDF));
        lst.add(mkSEC("4.4.1", "Solve problems involving decibels."));
        lst.add(mkETT("4441E1", EX1));
        lst.add(mkETT("4441E2", EX2));
        lst.add(mkETT("4441T1", TT1));
        lst.add(mkETT("4441T2", TT2));
        lst.add(mkSEC("4.4.2", "Solve problems involving pH."));
        lst.add(mkETT("4442E1", EX1));
        lst.add(mkETT("4442E2", EX2));
        lst.add(mkETT("4442T1", TT1));
        lst.add(mkETT("4442T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_5, "Solve logistic growth problems."));
        lst.add(mkVid("445OV", OVVID));
        lst.add(mkPdf("445OV", OVPDF));
        lst.add(mkSEC("4.5.1", "Solve problems using logistic models."));
        lst.add(mkETT("4451E1", EX1));
        lst.add(mkETT("4451E2", EX2));
        lst.add(mkETT("4451E3", EX3));
        lst.add(mkETT("4451T1", TT1));
        lst.add(mkETT("4451T2", TT2));
        lst.add(mkETT("4451T3", TT3));
        lst.add(mkSEC("4.5.2", "Determine a logistic model for a situation and solve related problems."));
        lst.add(mkETT("4452E1", EX1));
        lst.add(mkETT("4452E2", EX2));
        lst.add(mkETT("4452E3", EX3));
        lst.add(mkETT("4452T1", TT1));
        lst.add(mkETT("4452T2", TT2));
        lst.add(mkETT("4452T3", TT3));
    }

    /**
     * Builds lesson components for MATH 1240.
     *
     * @param lst the list to which to add components
     */
    private static void build1240(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M1240;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Multiply and factor polynomials."));
        lst.add(mkETT("4P1a", EXA));
        lst.add(mkETT("4P1b", EXB));
        lst.add(mkSEC("2", "Solve quadratic equations."));
        lst.add(mkETT("4P2a", EXA));
        lst.add(mkETT("4P2b", EXB));
        lst.add(mkETT("4P2c", EXC));
        lst.add(mkSEC("3", "Determine the vertex for a quadratic."));
        lst.add(mkETT("4P3a", EXA));
        lst.add(mkETT("4P3b", EXB));
        lst.add(mkSEC("4", "Evaluate functions."));
        lst.add(mkETT("4P4a", EXA));
        lst.add(mkETT("4P4b", EXB));
        lst.add(mkETT("4P4c", EXC));
        lst.add(mkSEC("5", "Determine the equation of a line in standard form."));
        lst.add(mkETT("4P5a", EXA));
        lst.add(mkETT("4P5b", EXB));
        lst.add(mkSEC("6", "Evaluate exponents."));
        lst.add(mkETT("4P6a", EXA));
        lst.add(mkETT("4P6b", EXB));
        lst.add(mkSEC("7", "Products and powers of exponents."));
        lst.add(mkETT("4P7a", EXA));
        lst.add(mkETT("4P7b", EXB));
        lst.add(mkSEC("8", "Simplify expressions with integer exponents."));
        lst.add(mkETT("4P8a", EXA));
        lst.add(mkETT("4P8b", EXB));
        lst.add(mkSEC("9", "Simplify expressions with rational exponents."));
        lst.add(mkETT("4P9a", EXA));
        lst.add(mkETT("4P9b", EXB));
        lst.add(mkSEC("10", "Linear regression."));
        lst.add(mkETT("4P10", EXA));

        // 1: Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkPreVid("411OV", OVVIDPRE));
        lst.add(mkPrePdf("411OV", OVPDF));

        lst.add(mkPreVid("IV1.1.1", "Supplemental: Relations and Functions"));
        lst.add(mkPreVid("IV1.1.2", "Supplemental: Evaluating and Graphing Functions"));
        lst.add(mkPreVid("IV1.1.3", "Supplemental: Findind Domain an Range of Functions"));
        lst.add(mkPreVid("TV1.1",
                "Technology Supplement: Evaluating and finding domain and range using the calculator"));

        lst.add(mkLH2("1: Functions", O1_1,
                "Evaluate and graph relations and functions, identify the domain and range"));
        lst.add(mkSEC("1.1.1", "Determine whether or not a relation is a function."));
        lst.add(mkETT("4111E1", EX1));
        lst.add(mkETT("4111E2", EX2));
        lst.add(mkETT("4111E3", EX3));
        lst.add(mkETT("4111T1", TT1));
        lst.add(mkETT("4111T2", TT2));
        lst.add(mkETT("4111T3", TT3));
        lst.add(mkSEC("1.1.2", "Build a table and graph for each function."));
        lst.add(mkETT("4112E1", EX1));
        lst.add(mkETT("4112E2", EX2));
        lst.add(mkETT("4112T1", TT1));
        lst.add(mkETT("4112T2", TT2));
        lst.add(mkETT("4112T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Functions", O1_2, "Identify the domain and range"));
        lst.add(mkSEC("1.2.1", "Identify the domain and range of a function."));
        lst.add(mkETT("4113E1", EX1));
        lst.add(mkETT("4113E2", EX2));
        lst.add(mkETT("4113E3", EX3));
        lst.add(mkETT("4113T1", TT1));
        lst.add(mkETT("4113T2", TT2));
        lst.add(mkETT("4113T3", TT3));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkPreVid("412OV", OVVIDPRE));
        lst.add(mkPrePdf("412OV", OVPDF));

        lst.add(mkPreVid("IV1.2.1", "Supplemental: Computing Average Rages of Change"));
        lst.add(mkPreVid("IV1.2.2part1", "Supplemental: Rate of change of difference quotients, part 1"));
        lst.add(mkPreVid("IV1.2.2part2", "Supplemental: Rate of change of difference quotients, part 2"));
        lst.add(mkPreVid("TV1.2", "Technology Supplement: Evaluating functions at particular x values"));

        lst.add(mkLH2("1: Functions", O1_3, "Evaluate functions to determine average rates of change."));
        lst.add(mkSEC("1.3.1", "Given a function determine the average rate of change between two points."));
        lst.add(mkETT("4121E1", EX1));
        lst.add(mkETT("4121E2", EX2));
        lst.add(mkETT("4121T1", TT1));
        lst.add(mkETT("4121T2", TT2));
        lst.add(mkETT("4121T3", TT3));
        lst.add(mkSEC("1.3.2", "Given a function, compute and simplify <em>[f(x + h) - f(x)] / h</em>."));
        lst.add(mkETT("4122E1", EX1));
        lst.add(mkETT("4122E2", EX2));
        lst.add(mkETT("4122T1", TT1));
        lst.add(mkETT("4122T2", TT2));
        lst.add(mkETT("4122T3", TT3));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkPreVid("413OV", OVVIDPRE));
        lst.add(mkPrePdf("413OV", OVPDF));
        lst.add(mkLH2("1: Functions", O1_4, "Given an output solve for the input algebraically."));
        lst.add(mkSEC("1.4.1", "Given an output, solve for the input algebraically."));
        lst.add(mkETT("4131E1", EX1));
        lst.add(mkETT("4131E2", EX2));
        lst.add(mkETT("4131T1", TT1));
        lst.add(mkETT("4131T2", TT2));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Functions", O1_5, "Given an output solve for the input graphically."));
        lst.add(mkSEC("1.5.1", "Given an output, solve for the input graphically."));
        lst.add(mkETT("4132E1", EX1));
        lst.add(mkETT("4132E2", EX2));
        lst.add(mkETT("4132T1", TT1));
        lst.add(mkETT("4132T2", TT2));

        startLesson(prefix + ".16", 1, 6);
        lst.add(mkPreVid("414OV", OVVIDPRE));
        lst.add(mkPrePdf("414OV", OVPDF));
        lst.add(mkLH2("1: Functions", O1_6, "Perform operations on functions."));
        lst.add(mkSEC("1.6.1", "Perform basic operations on functions."));
        lst.add(mkETT("4141E1", EX1));
        lst.add(mkETT("4141E2", EX2));
        lst.add(mkETT("4141T1", TT1));
        lst.add(mkETT("4141T2", TT2));

        startLesson(prefix + ".17", 1, 7);
        lst.add(mkLH2("1: Functions", O1_7, "Given two functions, <em>f</em> and <em>g</em>, find and compare "
                                            + "<em>(f <small>o</small> g)(x)</em> and <em>(g <small>o</small> f)(x)" +
                                            "</em>."));
        lst.add(mkSEC("1.7.1", "Given two functions, <em>f</em> and <em>g</em>, find and compare "
                               + "<em>(f <small>o</small> g)(x)</em> and <em>(g <small>o</small> f)(x)</em>."));
        lst.add(mkETT("4142E1", EX1));
        lst.add(mkETT("4142E2", EX2));
        lst.add(mkETT("4142T1", TT1));
        lst.add(mkETT("4142T2", TT2));

        startLesson(prefix + ".18", 1, 8);
        lst.add(mkLH2("1: Functions", O1_8, "Given a function write it as the composition of two functions."));
        lst.add(mkSEC("1.8.1", "Given a function write it as the composition of two functions."));
        lst.add(mkETT("4143E1", EX1));
        lst.add(mkETT("4143E2", EX2));
        lst.add(mkETT("4143T1", TT1));
        lst.add(mkETT("4143T2", TT2));

        startLesson(prefix + ".19", 1, 9);
        lst.add(mkPreVid("415OV", OVVIDPRE));
        lst.add(mkPrePdf("415OV", OVPDF));

        lst.add(mkPreVid("IV1.5.1", "Supplemental: Inverses of Relations"));
        lst.add(mkPreVid("IV1.5.2", "Supplemental: Inverse Functions"));
        lst.add(mkPreVid("IV1.5.3", "Supplemental: Determine the Inverse of a Function"));
        lst.add(mkPreVid("TV1.2", "Technology Supplement: Graphing a function and its inverse"));

        lst.add(mkLH2("1: Functions", O1_9, "Given a table, graph or rule for a function, identify the inverse "
                                            + "numerically and graphically. Determine whether or not its inverse is a" +
                                            " function."));
        lst.add(mkSEC("1.9.1", "Identify the inverse of a function numerically and graphically."));
        lst.add(mkETT("4151E1", EX1));
        lst.add(mkETT("4151T1", TT1));
        lst.add(mkSEC("1.9.2",
                "Given a table, graph or rule for a function, determine whether or not its inverse is a function."));
        lst.add(mkETT("4152E1", EX1));
        lst.add(mkETT("4152E2", EX2));
        lst.add(mkETT("4152E3", EX3));
        lst.add(mkETT("4152T1", TT1));
        lst.add(mkETT("4152T2", TT2));
        lst.add(mkETT("4152T3", TT3));

        startLesson(prefix + ".110", 1, 10);
        lst.add(mkLH2("1: Functions", O1_10, "Given <em>f</em>, determine <em>f<sup>-1</sup></em> and compose "
                                             + "<em>f</em> with <em>f<sup>-1</sup></em> and <em>f<sup>-1</sup></em> " +
                                             "with <em>f</em>."));
        lst.add(mkSEC("1.10.1", "Given <em>f</em>, determine <em>f<sup>-1</sup></em> and compose "
                                + "<em>f</em> with <em>f<sup>-1</sup></em> and <em>f<sup>-1</sup></em> with " +
                                "<em>f</em>."));
        lst.add(mkETT("4153E1", EX1));
        lst.add(mkETT("4153E2", EX2));
        lst.add(mkETT("4153T1", TT1));
        lst.add(mkETT("4153T2", TT2));
        lst.add(mkETT("4153T3", TT3));

        // 2: Introduction to Exponential and Logarithmic Functions
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPreVid("421OV", OVVIDPRE));
        lst.add(mkPrePdf("421OV", OVPDF));

        lst.add(mkPreVid("IV2.1.1part1", "Supplemental: Writing and Evaluating Exponential Functions"));
        lst.add(mkPreVid("IV2.1.1part2", "Supplemental: Computing Compound Interest"));
        lst.add(mkPreVid("IV2.1.2", "Supplemental: Determine Exponential Model from a Table"));
        lst.add(mkPreVid("TV2.1", "Technology Supplement: Exponential Models and Models of Growth and Decay"));

        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_1,
                "Given a growth or decay situation write an exponential function and evaluate it for a given input."));
        lst.add(mkSEC("2.1.1",
                "Given a growth or decay situation write an exponential function to evaluate it for a given input."));
        lst.add(mkETT("4211E1", EX1));
        lst.add(mkETT("4211E2", EX2));
        lst.add(mkETT("4211E3", EX3));
        lst.add(mkETT("4211T1", TT1));
        lst.add(mkETT("4211T2", TT2));
        lst.add(mkETT("4211T3", TT3));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkPreVid("421OV", OVVIDPRE));
        lst.add(mkPrePdf("421OV", OVPDF));
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_2,
                "Given a table of data determine an exponential function and evaluate it for a given input."));
        lst.add(mkSEC("2.2.1",
                "Given a table of data, determine an exponential function and evaluate it for a given input."));
        lst.add(mkETT("4212E1", EX1));
        lst.add(mkETT("4212E2", EX2));
        lst.add(mkETT("4212T1", TT1));
        lst.add(mkETT("4212T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkPreVid("422OV", OVVIDPRE));
        lst.add(mkPrePdf("422OV", OVPDF));

        lst.add(mkPreVid("IV2.2.1", "Supplemental: Graphing Exponential Functions"));
        lst.add(mkPreVid("IV2.2.2", "Supplemental: Determine Exponential Function from a Graph"));
        lst.add(mkPreVid("IV2.2.3", "Supplemental: Solving Exponential Equations Algebraically and Graphically"));
        lst.add(mkPreVid("TV2.2", "Technology Supplement: Verifying Exponenial Models"));

        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_3,
                "Graph and interpret graphs of functions of the form <em>y = ab<sup>x</sup></em>."));
        lst.add(mkSEC("2.3.1", "Given an exponential function, identify its graph."));
        lst.add(mkETT("4221E1", EX1));
        lst.add(mkETT("4221E2", EX2));
        lst.add(mkETT("4221E3", EX3));
        lst.add(mkETT("4221T1", TT1));
        lst.add(mkETT("4221T2", TT2));
        lst.add(mkETT("4221T3", TT3));
        lst.add(mkSEC("2.3.2", "Given the graph of an exponential function, identify the equation."));
        lst.add(mkPreVid("4222E0", "Introductory Lesson"));
        lst.add(mkETT("4222E1", EX1));
        lst.add(mkETT("4222E2", EX2));
        lst.add(mkETT("4222E3", EX3));
        lst.add(mkETT("4222T1", TT1));
        lst.add(mkETT("4222T2", TT2));
        lst.add(mkETT("4222T3", TT3));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_4, "Solve exponential equations numerically and graphically."));
        lst.add(mkSEC("2.4.1", "Solve exponential equations numerically and graphically."));
        lst.add(mkETT("4223E1", EX1));
        lst.add(mkETT("4223E2", EX2));
        lst.add(mkETT("4223E3", EX3));
        lst.add(mkETT("4223T1", TT1));
        lst.add(mkETT("4223T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkPreVid("423OV", OVVIDPRE));
        lst.add(mkPrePdf("423OV", OVPDF));
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions",
                O2_5, "Given an exponential function determine its inverse function and build a table and graph."));
        lst.add(mkSEC("2.5.1",
                "Given an exponential function, determine its inverse function and build a table and graph."));
        lst.add(mkETT("4231E1", EX1));
        lst.add(mkETT("4231E2", EX2));
        lst.add(mkETT("4231T1", TT1));
        lst.add(mkETT("4231T2", TT2));

        startLesson(prefix + ".26", 2, 6);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_6, "Compute logarithms."));
        lst.add(mkSEC("2.6.1", "Compute logarithms."));
        lst.add(mkETT("4232E1", EX1));
        lst.add(mkETT("4232E2", EX2));
        lst.add(mkETT("4232E3", EX3));
        lst.add(mkETT("4232E4", EX4));
        lst.add(mkETT("4232E5", EX5));
        lst.add(mkETT("4232E6", EX6));
        lst.add(mkETT("4232T1", TT1));
        lst.add(mkETT("4232T2", TT2));
        lst.add(mkETT("4232T3", TT3));

        startLesson(prefix + ".27", 2, 7);
        lst.add(mkPreVid("424OV", OVVIDPRE));
        lst.add(mkPrePdf("424OV", OVPDF));
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_7,
                "Given the graph of a logarithmic function identify the equation."));
        lst.add(mkSEC("2.7.1", "Given the graph of a logarithmic function identify the equation."));
        lst.add(mkETT("4241E1", EX1));
        lst.add(mkETT("4241E2", EX2));
        lst.add(mkETT("4241T1", TT1));
        lst.add(mkETT("4241T2", TT2));

        startLesson(prefix + ".28", 2, 8);
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_8,
                "Solve basic logarithmic equations algabraically."));
        lst.add(mkSEC("2.8.1", "Solve basic logarithmic equations algabraically."));
        lst.add(mkETT("4242E1", EX1));
        lst.add(mkETT("4242E2", EX2));
        lst.add(mkETT("4242E3", EX3));
        lst.add(mkETT("4242T1", TT1));
        lst.add(mkETT("4242T2", TT2));
        lst.add(mkETT("4242T3", TT3));

        startLesson(prefix + ".29", 2, 9);
        lst.add(mkPreVid("425OV", OVVIDPRE));
        lst.add(mkPrePdf("425OV", OVPDF));
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_9,
                "Graph <em>y = ae<sup>x</sup></em> and <em>y</em> = ln <em>x</em>."));
        lst.add(mkSEC("2.9.1", "Graph <em>y = ae<sup>x</sup></em> and <em>y</em> = ln <em>x</em>."));
        lst.add(mkETT("4251E1", EX1));
        lst.add(mkETT("4251E2", EX2));

        startLesson(prefix + ".210", 2, 10);
        lst.add(mkPreVid("425OV", OVVIDPRE));
        lst.add(mkPrePdf("425OV", OVPDF));
        lst.add(mkLH2("2: Introduction to Exponential and Logarithmic Functions", O2_10,
                "Solve problems using the model <em>y = ae<sup>x</sup></em>."));
        lst.add(mkSEC("2.10.1", "Solve problems using the model <em>y = ae<sup>x</sup></em>."));
        lst.add(mkETT("4252E1", EX1));
        lst.add(mkETT("4252E2", EX2));
        lst.add(mkETT("4252E3", EX3));
        lst.add(mkETT("4252T1", TT1));
        lst.add(mkETT("4252T2", TT2));

        // 3: Properties of Logarithms and Exponential Functions
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPreVid("431OV", OVVIDPRE));
        lst.add(mkPrePdf("431OV", OVPDF));

        lst.add(mkPreVid("IV3.1.1", "Supplemental: Properties of Logarithms, Part 1"));
        lst.add(mkPreVid("IV3.1.2", "Supplemental: Properties of Logarithms, Part 2"));
        lst.add(mkPreVid("TV3.1", "Technology Supplement: Simplifying Logarithmic Expressions"));

        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions)", O3_1,
                "Simplify <em>b</em><sup>log<sub><em>b</em></sub> <em>u</em></sup>."));
        lst.add(mkSEC("3.1.1", "Simplify <em>b</em><sup>log<sub><em>b</em></sub> <em>u</em></sup>."));
        lst.add(mkETT("4311E1", EX1));
        lst.add(mkETT("4311E2", EX2));
        lst.add(mkETT("4311E3", EX3));
        lst.add(mkETT("4311T1", TT1));
        lst.add(mkETT("4311T2", TT2));
        lst.add(mkETT("4311T3", TT3));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_2,
                "Simplify log<sub><em>b</em></sub> <em>b<sup>u</sup></em>."));
        lst.add(mkSEC("3.2.1", "Simplify log<sub><em>b</em></sub> <em>b<sup>u</sup></em>."));
        lst.add(mkETT("4312E1", EX1));
        lst.add(mkETT("4312E2", EX2));
        lst.add(mkETT("4312E3", EX3));
        lst.add(mkETT("4312T1", TT1));
        lst.add(mkETT("4312T2", TT2));
        lst.add(mkETT("4312T3", TT3));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkPreVid("432OV", OVVIDPRE));
        lst.add(mkPrePdf("432OV", OVPDF));

        lst.add(mkPreVid("IV3.2.1", "Supplemental: Product Rule of Logarithms"));
        lst.add(mkPreVid("IV3.2.2", "Supplemental: Quotient Rule of Logarithms"));
        lst.add(mkPreVid("TV3.2", "Technology Supplement: Using the Calculator with the Quotient Rule of Logarithms"));

        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_3,
                "Develop and apply the product and quotient rules."));
        lst.add(mkSEC("3.3.1", "Develop the product and quotient rules."));
        lst.add(mkETT("4321E1", EX1));
        lst.add(mkETT("4321E2", EX2));
        lst.add(mkSEC("3.3.2", "Apply the product and quotient rules."));
        lst.add(mkETT("4322E1", EX1));
        lst.add(mkETT("4322E2", EX2));
        lst.add(mkETT("4322E3", EX3));
        lst.add(mkETT("4322E4", EX4));
        lst.add(mkETT("4322E5", EX5));
        lst.add(mkETT("4322E6", EX6));
        lst.add(mkETT("4322T1", TT1));
        lst.add(mkETT("4322T2", TT2));

        startLesson(prefix + ".34", 4, 4);
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_4,
                "Develop and apply the power rule."));
        lst.add(mkSEC("3.4.1", "Develop and apply the power rule."));
        lst.add(mkETT("4323E1", EX1));
        lst.add(mkETT("4323E2", EX2));
        lst.add(mkETT("4323E3", EX3));
        lst.add(mkETT("4323E4", EX4));
        lst.add(mkETT("4323E5", EX5));
        lst.add(mkETT("4323E6", EX6));
        lst.add(mkETT("4323T1", TT1));
        lst.add(mkETT("4323T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkPreVid("433OV", OVVIDPRE));
        lst.add(mkPrePdf("433OV", OVPDF));
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_5,
                "Use properties to rewrite logarithms."));
        lst.add(mkSEC("3.5.1", "Use properties to rewrite logarithms."));
        lst.add(mkETT("4331E1", EX1));
        lst.add(mkETT("4331E2", EX2));
        lst.add(mkETT("4331E3", EX3));
        lst.add(mkETT("4331T1", TT1));
        lst.add(mkETT("4331T2", TT2));
        lst.add(mkETT("4331T3", TT3));

        startLesson(prefix + ".36", 3, 6);
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_6,
                "Use properties to rewrite expressions as single logarithms."));
        lst.add(mkSEC("3.6.1", "Use properties to rewrite expressions as single logarithms."));
        lst.add(mkETT("4332E1", EX1));
        lst.add(mkETT("4332E2", EX2));
        lst.add(mkETT("4332E3", EX3));
        lst.add(mkETT("4332T1", TT1));
        lst.add(mkETT("4332T2", TT2));
        lst.add(mkETT("4332T3", TT3));

        startLesson(prefix + ".37", 3, 7);
        lst.add(mkPreVid("434OV", OVVIDPRE));
        lst.add(mkPrePdf("434OV", OVPDF));
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_7,
                "Compute logarithms using the base changing formulas."));
        lst.add(mkSEC("3.7.1", "Compute logarithms using the base changing formula."));
        lst.add(mkETT("4341E1", EX1));
        lst.add(mkETT("4341E2", EX2));
        lst.add(mkETT("4341E3", EX3));
        lst.add(mkETT("4341T1", TT1));
        lst.add(mkETT("4341T2", TT2));
        lst.add(mkETT("4341T3", TT3));

        startLesson(prefix + ".38", 3, 8);
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_8,
                "Build tables and graphs of logarithmic functions in other bases."));
        lst.add(mkSEC("3.8.1", "Build tables and graphs of logarithmic functions in other bases."));
        lst.add(mkETT("4342E1", EX1));
        lst.add(mkETT("4342E2", EX2));
        lst.add(mkETT("4342T1", TT1));
        lst.add(mkETT("4342T2", TT2));

        startLesson(prefix + ".39", 3, 9);
        lst.add(mkPreVid("435OV", OVVIDPRE));
        lst.add(mkPrePdf("435OV", OVPDF));
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_9,
                "Convert from the form <em>y = ab<sup>x</sup></em> to <em>y = ae<sup>kx</sup> and compare."));
        lst.add(mkSEC("3.9.1",
                "Convert from the form <em>y = ab<sup>x</sup></em> to <em>y = ae<sup>kx</sup> and compare."));
        lst.add(mkETT("4351E1", EX1));
        lst.add(mkETT("4351E2", EX2));
        lst.add(mkETT("4351T1", TT1));
        lst.add(mkETT("4351T2", TT2));

        startLesson(prefix + ".310", 3, 10);
        lst.add(mkLH2("3: Properties of Logarithms and Exponential Functions", O3_10,
                "Convert from the form <em>y = ae<sup>kx</sup> to <em>y = ab<sup>x</sup></em> and interpret."));
        lst.add(mkSEC("3.10.1",
                "Convert from the form <em>y = ae<sup>kx</sup> to <em>y = ab<sup>x</sup></em> and interpret."));
        lst.add(mkETT("4352E1", EX1));
        lst.add(mkETT("4352E2", EX2));
        lst.add(mkETT("4352T1", TT1));
        lst.add(mkETT("4352T2", TT2));

        // 4: Modeling with Exponential and Logarithmic functions
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkPreVid("441OV", OVVIDPRE));
        lst.add(mkPrePdf("441OV", OVPDF));

        lst.add(mkPreVid("IV4.1.1", "Supplemental: Solving Equations of the Form b<sup>u</sup> = b<sup>v</sup>"));
        lst.add(mkPreVid("IV4.1.2", "Supplemental: Solving Equations Using Logarithms"));
        lst.add(mkPreVid("TV4.1", "Technology Supplement: Solving Exponential Equations Using the Calculator"));

        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_1,
                "Simplify and solve equations of the form <em>b<sup>u</sup> = b<sup>v</sup></em>."));
        lst.add(mkSEC("4.1.1", "Simplify and solve equations of the form <em>b<sup>u</sup> = b<sup>v</sup></em>."));
        lst.add(mkETT("4411E1", EX1));
        lst.add(mkETT("4411E2", EX2));
        lst.add(mkETT("4411T1", TT1));
        lst.add(mkETT("4411T2", TT2));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_2,
                "Solve exponential equations using logarithms or technology."));
        lst.add(mkSEC("4.2.1", "Solve exponential equations using logarithms or technology."));
        lst.add(mkETT("4412E1", EX1));
        lst.add(mkETT("4412E2", EX2, true, false));
        lst.add(mkETT("4412E3", EX3));
        lst.add(mkETT("4412T1", TT1));
        lst.add(mkETT("4412T2", TT2));
        lst.add(mkETT("4412T3", TT3));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkPreVid("442OV", OVVIDPRE));
        lst.add(mkPrePdf("442OV", OVPDF));

        lst.add(mkPreVid("IV4.2.1",
                "Supplemental: Solving Equations of the Form log<sub>b</sub> u = log<sub>b</sub> v"));
        lst.add(mkPreVid("IV4.2.2", "Supplemental: Solving Equations Using the Definition of Logarithm"));
        lst.add(mkPreVid("TV4.2", "Technology Supplement: Solving Logarithmic Equations Using the Calculator"));

        // TODO: Here
        // TODO: Here
        // TODO: Here
        // TODO: Here
        // TODO: Here

        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_3,
                "Simplify and solve equations of the form "
                + "log<sub><em>b</em></sub><em>u</em> = log<sub><em>b</em></sub><em>v</em>."));
        lst.add(mkSEC("4.3.1", "Simplify and solve equations of the form "
                               + "log<sub><em>b</em></sub><em>u</em> = log<sub><em>b</em></sub><em>v</em>."));
        lst.add(mkETT("4421E1", EX1));
        lst.add(mkETT("4421E2", EX2));
        lst.add(mkETT("4421T1", TT1));
        lst.add(mkETT("4421T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_4,
                "Solve equations of the form log<sub><em>b</em></sub><em>u</em> = <em>v</em>."));
        lst.add(mkSEC("4.4.1", "Solve equations of the form log<sub><em>b</em></sub><em>u</em> = <em>v</em>."));
        lst.add(mkETT("4422E1", EX1));
        lst.add(mkETT("4422E2", EX2));
        lst.add(mkETT("4422T1", TT1));
        lst.add(mkETT("4422T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkPreVid("443OV", OVVIDPRE));
        lst.add(mkPrePdf("443OV", OVPDF));
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_5,
                "Solve growth and decay problems."));
        lst.add(mkSEC("4.5.1", "Solve growth and decay problems."));
        lst.add(mkETT("4431E1", EX1));
        lst.add(mkETT("4431E2", EX2));
        lst.add(mkETT("4431E3", EX3));
        lst.add(mkETT("4431T1", TT1));
        lst.add(mkETT("4431T2", TT2));
        lst.add(mkETT("4431T3", TT3));

        startLesson(prefix + ".46", 4, 6);
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_6,
                "Fit an exponential function to data and solve related problems."));
        lst.add(mkSEC("4.6.1", "Fit an exponential function to data and solve related problems."));
        lst.add(mkETT("4432E1", EX1));
        lst.add(mkETT("4432T1", TT1));

        startLesson(prefix + ".47", 4, 7);
        lst.add(mkPreVid("444OV", OVVIDPRE));
        lst.add(mkPrePdf("444OV", OVPDF));
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_7,
                "Solve problems involving decibels."));
        lst.add(mkSEC("4.7.1", "Solve problems involving decibels."));
        lst.add(mkETT("4441E1", EX1));
        lst.add(mkETT("4441E2", EX2));
        lst.add(mkETT("4441T1", TT1));
        lst.add(mkETT("4441T2", TT2));

        startLesson(prefix + ".48", 4, 8);
        lst.add(mkLH2("4: Properties of Logarithms and Exponential Functions", O4_8, "Solve problems involving pH."));
        lst.add(mkSEC("4.8.1", "Solve problems involving pH."));
        lst.add(mkETT("4442E1", EX1));
        lst.add(mkETT("4442E2", EX2));
        lst.add(mkETT("4442T1", TT1));
        lst.add(mkETT("4442T2", TT2));

        startLesson(prefix + ".49", 4, 9);
        lst.add(mkLH2("4: Modeling with Exponentials and Logarithms", O4_9, "Solve logistic growth problems."));
        lst.add(mkVid("445OV", OVVID));
        lst.add(mkPdf("445OV", OVPDF));
        lst.add(mkSEC("4.9.1", "Solve problems using logistic models."));
        lst.add(mkETT("4451E1", EX1));
        lst.add(mkETT("4451E2", EX2));
        lst.add(mkETT("4451E3", EX3));
        lst.add(mkETT("4451T1", TT1));
        lst.add(mkETT("4451T2", TT2));
        lst.add(mkETT("4451T3", TT3));
        lst.add(mkSEC("4.9.2", "Determine a logistic model for a situation and solve related problems."));
        lst.add(mkETT("4452E1", EX1));
        lst.add(mkETT("4452E2", EX2));
        lst.add(mkETT("4452E3", EX3));
        lst.add(mkETT("4452T1", TT1));
        lst.add(mkETT("4452T2", TT2));
        lst.add(mkETT("4452T3", TT3));
    }

    /**
     * Builds lesson components for MATH 125.
     *
     * @param lst the list to which to add components
     */
    private static void build125(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M125;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Compute angles in triangles."));
        lst.add(mkETT("5P1a", EXA));
        lst.add(mkETT("5P1b", EXB));
        lst.add(mkSEC("2", "Simplify radicals."));
        lst.add(mkETT("5P2a", EXA));
        lst.add(mkETT("5P2b", EXB));
        lst.add(mkSEC("3", "Solving similar triangles."));
        lst.add(mkETT("5P3a", EXA));
        lst.add(mkETT("5P3b", EXB));
        lst.add(mkSEC("4", "Determine the equation of a circle."));
        lst.add(mkETT("5P4a", EXA));
        lst.add(mkETT("5P4b", EXB));
        lst.add(mkSEC("5", "Compose functions."));
        lst.add(mkETT("5P5a", EXA));
        lst.add(mkETT("5P5b", EXB));
        lst.add(mkSEC("6", "Transform functions I."));
        lst.add(mkETT("5P6a", EXA));
        lst.add(mkETT("5P6b", EXB));
        lst.add(mkSEC("7", "Transform functions II."));
        lst.add(mkETT("5P7a", EXA));
        lst.add(mkETT("5P7b", EXB));
        lst.add(mkSEC("8", "Compute circumference."));
        lst.add(mkETT("5P8a", EXA));
        lst.add(mkETT("5P8b", EXB));
        lst.add(mkSEC("9", "Compute area of a circle."));
        lst.add(mkETT("5P9a", EXA));
        lst.add(mkETT("5P9b", EXB));
        lst.add(mkSEC("10", "Solve equations."));
        lst.add(mkETT("5P10a", EXA));
        lst.add(mkETT("5P10b", EXB));

        // 1: Introduction to Trigonometric Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_1, "Review right triangles"));
        lst.add(mkVid("511OV", OVVID));
        lst.add(mkPdf("511OV", OVPDF));
        lst.add(mkTip("12511Tips"));
        lst.add(mkSEC("1.1.1", "Solve problems using the Pythagorean Theorem."));
        lst.add(mkETT("5111E1", EX1));
        lst.add(mkETT("5111E2", EX2));
        lst.add(mkETT("5111E3", EX3));
        lst.add(mkETT("5111T1", TT1));
        lst.add(mkETT("5111T2", TT2));
        lst.add(mkETT("5111T3", TT3));
        lst.add(mkSEC("1.1.2", "Solve problems using 45-45-90 right triangles."));
        lst.add(mkVid("5112E0", "Introductory Lesson"));
        lst.add(mkETT("5112E1", EX1));
        lst.add(mkETT("5112E2", EX2));
        lst.add(mkETT("5112E3", EX3));
        lst.add(mkETT("5112T1", TT1));
        lst.add(mkETT("5112T2", TT2));
        lst.add(mkETT("5112T3", TT3));
        lst.add(mkSEC("1.1.3", "Solve problems using 30-60-90 right triangles."));
        lst.add(mkVid("5113E0", "Introductory Lesson"));
        lst.add(mkETT("5113E1", EX1));
        lst.add(mkETT("5113E2", EX2));
        lst.add(mkETT("5113E3", EX3));
        lst.add(mkETT("5113T1", TT1));
        lst.add(mkETT("5113T2", TT2));
        lst.add(mkETT("5113T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_2,
                "Compute with basic trigonometric functions"));
        lst.add(mkVid("512OV", OVVID));
        lst.add(mkPdf("512OV", OVPDF));
        lst.add(mkTip("12512Tips"));
        lst.add(mkSEC("1.2.1", "Determine basic trigonometric function values for right triangles."));
        lst.add(mkETT("5121E1", EX1));
        lst.add(mkETT("5121E2", EX2));
        lst.add(mkETT("5121E3", EX3));
        lst.add(mkETT("5121E4", EX4));
        lst.add(mkETT("5121T1", TT1));
        lst.add(mkETT("5121T2", TT2));
        lst.add(mkETT("5121T3", TT3));
        lst.add(mkETT("5121T4", TT4));
        lst.add(mkSEC("1.2.2", "Determine basic trigonometric function values for special right triangles."));
        lst.add(mkPdf("5122summary", "Summary"));
        lst.add(mkETT("5122E1", EX1));
        lst.add(mkETT("5122E2", EX2));
        lst.add(mkETT("5122E3", EX3));
        lst.add(mkETT("5122E4", EX4));
        lst.add(mkETT("5122T1", TT1));
        lst.add(mkETT("5122T2", TT2));
        lst.add(mkETT("5122T3", TT3));
        lst.add(mkETT("5122T4", TT4));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_3, "Solve simple triangles"));
        lst.add(mkVid("513OV", OVVID));
        lst.add(mkPdf("513OV", OVPDF));
        lst.add(mkTip("12513Tips"));
        lst.add(mkSEC("1.3.1", "Use basic trigonometric functions to solve right triangle problems."));
        lst.add(mkETT("5131E1", EX1));
        lst.add(mkETT("5131E2", EX2));
        lst.add(mkETT("5131E3", EX3));
        lst.add(mkETT("5131T1", TT1));
        lst.add(mkETT("5131T2", TT2));
        lst.add(mkETT("5131T3", TT3));
        lst.add(mkSEC("1.3.2", "Use inverses to solve right triangle problems."));
        lst.add(mkETT("5132E1", EX1));
        lst.add(mkETT("5132E2", EX2));
        lst.add(mkETT("5132E3", EX3));
        lst.add(mkETT("5132T1", TT1));
        lst.add(mkETT("5132T2", TT2));
        lst.add(mkETT("5132T3", TT3));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_4, "Work with reference angles"));
        lst.add(mkVid("514OV", OVVID));
        lst.add(mkPdf("514OV", OVPDF));
        lst.add(mkTip("12514Tips"));
        lst.add(mkSEC("1.4.1", "Determine basic trigonometric function values using reference angles."));
        lst.add(mkETT("5141E1", EX1));
        lst.add(mkETT("5141E2", EX2));
        lst.add(mkETT("5141E3", EX3));
        lst.add(mkETT("5141E4", EX4));
        lst.add(mkETT("5141E5", EX5));
        lst.add(mkETT("5141T1", TT1));
        lst.add(mkETT("5141T2", TT2));
        lst.add(mkETT("5141T3", TT3));
        lst.add(mkETT("5141T4", TT4));
        lst.add(mkETT("5141T5", TT5));
        lst.add(mkSEC("1.4.2", "Determine trigonometric function values using reference angles."));
        lst.add(mkETT("5142E1", EX1));
        lst.add(mkETT("5142E2", EX2));
        lst.add(mkETT("5142E3", EX3));
        lst.add(mkETT("5142E4", EX4));
        lst.add(mkETT("5142E5", EX5));
        lst.add(mkETT("5142T1", TT1));
        lst.add(mkETT("5142T2", TT2));
        lst.add(mkETT("5142T3", TT3));
        lst.add(mkETT("5142T4", TT4));
        lst.add(mkETT("5142T5", TT5));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_5, "Solve trigonometric applications"));
        lst.add(mkVid("515OV", OVVID));
        // lst.add(mkPdf("515OV", OVPDF));
        lst.add(mkTip("12515Tips"));
        lst.add(mkSEC("1.5.1", "Solve angle of elevation problems."));
        lst.add(mkETT("5151E1", EX1));
        lst.add(mkETT("5151E2", EX2));
        lst.add(mkETT("5151T1", TT1));
        lst.add(mkETT("5151T2", TT2));
        lst.add(mkSEC("1.5.2", "Solve angle of depression problems."));
        lst.add(mkETT("5152E1", EX1));
        lst.add(mkETT("5152E2", EX2));
        lst.add(mkETT("5152T1", TT1));
        lst.add(mkETT("5152T2", TT2));
        lst.add(mkSEC("1.5.3", "Solve other applications."));
        lst.add(mkETT("5153E1", EX1));
        lst.add(mkETT("5153T1", TT1));

        // 2: Law of Sines and Law of Cosines
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_1,
                "Derive and identify the Law of Sines and the Law of Cosines"));
        lst.add(mkVid("521OV", OVVID));
        lst.add(mkPdf("521OV", OVPDF));
        lst.add(mkTip("12521Tips"));
        lst.add(mkSEC("2.1.1", "Derive and identify the Law of Sines."));
        lst.add(mkETT("5211E1", EX1));
        lst.add(mkETT("5211E2", EX2));
        lst.add(mkETT("5211T1", TT1));
        lst.add(mkETT("5211T2", TT2));
        lst.add(mkSEC("2.1.2", "Derive and identify the Law of Cosines."));
        lst.add(mkETT("5212E1", EX1));
        lst.add(mkETT("5212E2", EX2));
        lst.add(mkETT("5212T1", TT1));
        lst.add(mkETT("5212T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_2, "Apply the Law of Sines I: Two angles"));
        lst.add(mkVid("522OV", OVVID));
        lst.add(mkPdf("522OV", OVPDF));
        lst.add(mkTip("12522Tips"));
        lst.add(mkSEC("2.2.1", "Solve triangles given two angles (AAS)."));
        lst.add(mkETT("5221E1", EX1));
        lst.add(mkETT("5221E2", EX2));
        lst.add(mkETT("5221T1", TT1));
        lst.add(mkETT("5221T2", TT2));
        lst.add(mkSEC("2.2.2", "Solve triangles given two angles (ASA)."));
        lst.add(mkETT("5222E1", EX1));
        lst.add(mkETT("5222E2", EX2));
        lst.add(mkETT("5222T1", TT1));
        lst.add(mkETT("5222T2", TT2));
        lst.add(mkSEC("2.2.3", "Solve applications with Law of Sines."));
        lst.add(mkETT("5223E1", EX1));
        lst.add(mkETT("5223E2", EX2));
        lst.add(mkETT("5223T1", TT1));
        lst.add(mkETT("5223T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_3,
                "Apply the Law of Sines II: Two Sides: The Ambiguous Case"));
        lst.add(mkVid("523OV", OVVID));
        lst.add(mkPdf("523OV", OVPDF));
        lst.add(mkTip("12523Tips"));
        lst.add(mkSEC("2.3.1", "Solve the ambiguous case with two solutions."));
        lst.add(mkETT("5231E1", EX1));
        lst.add(mkETT("5231E2", EX2));
        lst.add(mkETT("5231T1", TT1));
        lst.add(mkETT("5231T2", TT2));
        lst.add(mkSEC("2.3.2", "Solve the ambiguous case with one solution."));
        lst.add(mkETT("5232E1", EX1));
        lst.add(mkETT("5232E2", EX2));
        lst.add(mkETT("5232T1", TT1));
        lst.add(mkETT("5232T2", TT2));
        lst.add(mkSEC("2.3.3", "Solve the ambiguous case with no solution."));
        lst.add(mkETT("5233E1", EX1));
        lst.add(mkETT("5233E2", EX2));
        lst.add(mkETT("5233T1", TT1));
        lst.add(mkETT("5233T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_4, "Apply the Law of Cosines"));
        lst.add(mkVid("524OV", OVVID));
        lst.add(mkPdf("524OV", OVPDF));
        lst.add(mkTip("12524Tips"));
        lst.add(mkSEC("2.4.1", "Solve for a missing side (SAS)."));
        lst.add(mkETT("5241E1", EX1));
        lst.add(mkETT("5241E2", EX2));
        lst.add(mkETT("5241T1", TT1));
        lst.add(mkETT("5241T2", TT2));
        lst.add(mkSEC("2.4.2", "Solve for a missing angle (SSS)."));
        lst.add(mkETT("5242E1", EX1));
        lst.add(mkETT("5242E2", EX2));
        lst.add(mkETT("5242T1", TT1));
        lst.add(mkETT("5242T2", TT2));
        lst.add(mkSEC("2.4.3", "Solve applications with Law of Cosines."));
        lst.add(mkETT("5243E1", EX1));
        lst.add(mkETT("5243E2", EX2));
        lst.add(mkETT("5243T1", TT1));
        lst.add(mkETT("5243T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_5, "Solve Problems Involving Vectors"));
        lst.add(mkVid("525OV", OVVID1));
        lst.add(mkVid("525OV2", OVVID2));
        lst.add(mkVid("525OV3", OVVID3));
        lst.add(mkPdf("525OV", OVPDF));
        // lst.add(mkTip("12525Tips"));

        lst.add(mkSEC("2.5.1", "Given a vector, compute its magnitude, direction, and find resultant vectors."));
        lst.add(mkETT("5251E1", EX1));
        lst.add(mkETT("5251E2", EX2));
        lst.add(mkETT("5251T1", TT1));
        lst.add(mkETT("5251T2", TT2));
        lst.add(mkSEC("2.5.2", "Solve applications of vectors in the <b>i</b>, <b>j</b> form."));
        lst.add(mkETT("5252E1", EX1));
        lst.add(mkETT("5252E2", EX2));
        lst.add(mkETT("5252T1", TT1));
        lst.add(mkETT("5252T2", TT2));
        lst.add(mkSEC("2.5.3", "Compute the dot product and angle between two vectors"));
        lst.add(mkETT("5253E1", EX1));
        lst.add(mkETT("5253E2", EX2));
        lst.add(mkETT("5253T1", TT1, true, false));
        lst.add(mkETT("5253T2", TT2));

        // 3: Unit Circle and Radian Measure
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_1, "Construct the unit circle"));
        lst.add(mkVid("531OV", OVVID));
        lst.add(mkPdf("531OV", OVPDF));
        lst.add(mkTip("12531Tips"));
        lst.add(mkSEC("3.1.1", "Convert basic angles to radians."));
        lst.add(mkETT("5311E1", EX1));
        lst.add(mkETT("5311E2", EX2));
        lst.add(mkETT("5311T1", TT1));
        lst.add(mkETT("5311T2", TT2));
        lst.add(mkSEC("3.1.2", "Convert angles."));
        lst.add(mkETT("5312E1", EX1));
        lst.add(mkETT("5312E2", EX2));
        lst.add(mkETT("5312T1", TT1));
        lst.add(mkETT("5312T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_2, "Label points on the unit circle"));
        lst.add(mkVid("532OV", OVVID));
        lst.add(mkPdf("532OV", OVPDF));
        lst.add(mkTip("12532Tips"));
        lst.add(mkSEC("3.2.1", "Identify points on the unit circle."));
        lst.add(mkETT("5321E1", EX1));
        lst.add(mkETT("5321E2", EX2));
        lst.add(mkETT("5321T1", TT1));
        lst.add(mkETT("5321T2", TT2));
        lst.add(mkSEC("3.2.2", "Determine sine and cosine values from the unit circle."));
        lst.add(mkETT("5322E1", EX1));
        lst.add(mkETT("5322E2", EX2));
        lst.add(mkETT("5322T1", TT1));
        lst.add(mkETT("5322T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_3, "Compute with radian measure"));
        lst.add(mkVid("533OV", OVVID));
        lst.add(mkPdf("533OV", OVPDF));
        lst.add(mkTip("12533Tips"));
        lst.add(mkSEC("3.3.1", "Given an angle in radians compute trigonometric function values."));
        lst.add(mkETT("5331E1", EX1));
        lst.add(mkETT("5331E2", EX2));
        lst.add(mkETT("5331E3", EX3));
        lst.add(mkETT("5331E4", EX4));
        lst.add(mkETT("5331T1", TT1));
        lst.add(mkETT("5331T2", TT2));
        lst.add(mkETT("5331T3", TT3));
        lst.add(mkETT("5331T4", TT4));
        lst.add(mkETT("5331T5", TT5));
        lst.add(mkSEC("3.3.2", "Given a trigonometric value determine the angle in radians and degrees."));
        lst.add(mkETT("5332E1", EX1));
        lst.add(mkETT("5332E2", EX2));
        lst.add(mkETT("5332E3", EX3));
        lst.add(mkETT("5332E4", EX4));
        lst.add(mkETT("5332E5", EX5));
        lst.add(mkETT("5332E6", EX6));
        lst.add(mkETT("5332T1", TT1));
        lst.add(mkETT("5332T2", TT2));
        lst.add(mkETT("5332T3", TT3));
        lst.add(mkETT("5332T4", TT4));
        lst.add(mkETT("5332T5", TT5));
        lst.add(mkETT("5332T6", TT6));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_4, "Applications"));
        lst.add(mkVid("534OV", OVVID));
        lst.add(mkPdf("534OV", OVPDF));
        lst.add(mkTip("12534Tips"));
        lst.add(mkSEC("3.4.1", "Compute arc length."));
        lst.add(mkETT("5341E1", EX1));
        lst.add(mkETT("5341E2", EX2));
        lst.add(mkETT("5341T1", TT1));
        lst.add(mkETT("5341T2", TT2));
        lst.add(mkSEC("3.4.2", "Compute area of a sector."));
        lst.add(mkETT("5342E1", EX1));
        lst.add(mkETT("5342E2", EX2));
        lst.add(mkETT("5342T1", TT1));
        lst.add(mkETT("5342T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_5, "Solve problems with complex numbers."));
        lst.add(mkVid("535OV", OVVID1));
        lst.add(mkVid("535OV2", OVVID2));
        lst.add(mkPdf("535OV", OVPDF));
        lst.add(mkSEC("3.5.1", "Perform operations on complex numbers."));
        lst.add(mkETT("5351E1", EX1));
        lst.add(mkETT("5351T1", TT1));
        lst.add(mkSEC("3.5.2",
                "Given a complex number, determine relationships between its coordinates, direction, and magnitude."));
        lst.add(mkETT("5352E1", EX1));
        lst.add(mkETT("5352T1", TT1));
        lst.add(mkSEC("3.5.3", "Convert between rectangular and trigonometric forms."));
        lst.add(mkETT("5353E1", EX1));
        lst.add(mkETT("5353T1", TT1));

        // 4: Periodic Functions and Applications
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_1,
                "Translate <em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>"));
        lst.add(mkVid("541OV", OVVID));
        lst.add(mkPdf("541OV", OVPDF1));
        lst.add(mkPdf("541OV2", OVPDF2));
        lst.add(mkTip1("12541Tips1"));
        lst.add(mkTip2("12541Tips2"));
        lst.add(mkSEC("4.1.1", "Graph sine, cosine, and tangent."));
        lst.add(mkETT("5411E1", EX1));
        lst.add(mkETT("5411E2", EX2));
        lst.add(mkETT("5411E3", EX3));
        lst.add(mkETT("5411T1", TT1));
        lst.add(mkETT("5411T2", TT2));
        lst.add(mkETT("5411T3", TT3));

        lst.add(mkSEC("4.1.2", "Graph and identify vertical and horizontal shifts of "
                               + "<em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>."));
        lst.add(mkETT("5412E1", EX1));
        lst.add(mkETT("5412E2", EX2));
        lst.add(mkETT("5412E3", EX3));
        lst.add(mkETT("5412E4", EX4));
        lst.add(mkETT("5412E5", EX5));
        lst.add(mkETT("5412E6", EX6));
        lst.add(mkETT("5412E7", EX7));
        lst.add(mkETT("5412E8", EX8));
        lst.add(mkETT("5412T1", TT1));
        lst.add(mkETT("5412T2", TT2));
        lst.add(mkETT("5412T3", TT3));
        lst.add(mkETT("5412T4", TT4));
        lst.add(mkETT("5412T5", TT5));
        lst.add(mkETT("5412T6", TT6));
        lst.add(mkETT("5412T7", TT7));
        lst.add(mkETT("5412T8", TT8));
        lst.add(mkSEC("4.1.3", "Graph and identify translations of <em>y</em> = sin <em>x</em> and "
                               + "<em>y</em> = cos <em>x</em>."));
        lst.add(mkETT("5413E1", EX1));
        lst.add(mkETT("5413E2", EX2));
        lst.add(mkETT("5413E3", EX3));
        lst.add(mkETT("5413T1", TT1));
        lst.add(mkETT("5413T2", TT2));
        lst.add(mkETT("5413T3", TT3));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_2,
                "Transform <em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>"));
        lst.add(mkVid("542OV", OVVID));
        lst.add(mkPdf("542OV", OVPDF));
        lst.add(mkTip("12542Tips"));
        lst.add(mkSEC("4.2.1", "Graph and identify amplitude transformations of the form "
                               + "<em>y = a</em> sin <em>x</em> and <em>y = a</em> cos <em>x</em>."));
        lst.add(mkETT("5421E1", EX1));
        lst.add(mkETT("5421E2", EX2));
        lst.add(mkETT("5421E3", EX3));
        lst.add(mkETT("5421E4", EX4));
        lst.add(mkETT("5421E5", EX5));
        lst.add(mkETT("5421E6", EX6));
        lst.add(mkETT("5421T1", TT1));
        lst.add(mkETT("5421T2", TT2));
        lst.add(mkETT("5421T3", TT3));
        lst.add(mkETT("5421T4", TT4));
        lst.add(mkETT("5421T5", TT5));
        lst.add(mkETT("5421T6", TT6));
        lst.add(mkSEC("4.2.2", "Graph and identify period transformations of the form "
                               + "<em>y = a</em> sin <em>bx</em> and <em>y = a</em> cos <em>bx</em>."));
        lst.add(mkETT("5422E1", EX1));
        lst.add(mkETT("5422E2", EX2));
        lst.add(mkETT("5422E3", EX3));
        lst.add(mkETT("5422E4", EX4));
        lst.add(mkETT("5422E5", EX5));
        lst.add(mkETT("5422E6", EX6));
        lst.add(mkETT("5422T1", TT1));
        lst.add(mkETT("5422T2", TT2));
        lst.add(mkETT("5422T3", TT3));
        lst.add(mkETT("5422T4", TT4));
        lst.add(mkETT("5422T5", TT5));
        lst.add(mkETT("5422T6", TT6));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_3, "Combine transformations"));
        lst.add(mkVid("543OV", OVVID));
        lst.add(mkPdf("543OV", OVPDF));
        lst.add(mkTip("12543Tips"));
        lst.add(mkSEC("4.3.1",
                "Graph and identify transformations of the form <em>y = a</em> sin <em>b(x-h) + k</em>."));
        lst.add(mkETT("5431E1", EX1));
        lst.add(mkETT("5431E2", EX2));
        lst.add(mkETT("5431E3", EX3));
        lst.add(mkETT("5431T1", TT1));
        lst.add(mkETT("5431T2", TT2));
        lst.add(mkETT("5431T3", TT3));
        lst.add(mkSEC("4.3.2",
                "Graph and identify transformations of the form <em>y = a</em> cos <em>b(x-h) + k</em>."));
        lst.add(mkETT("5432E1", EX1));
        lst.add(mkETT("5432E2", EX2));
        lst.add(mkETT("5432E3", EX3));
        lst.add(mkETT("5432T1", TT1));
        lst.add(mkETT("5432T2", TT2));
        lst.add(mkETT("5432T3", TT3));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_4,
                "Graph and identify graphs of other trigonometric functions"));
        lst.add(mkVid("544OV", OVVID));
        lst.add(mkPdf("544OV", OVPDF));
        lst.add(mkTip("12544Tips"));
        lst.add(mkSEC("4.4.1", "Graph and identify transformations of <em>y</em> = tan <em>x</em>."));
        lst.add(mkETT("5441E1", EX1));
        lst.add(mkETT("5441E2", EX2));
        lst.add(mkETT("5441T1", TT1));
        lst.add(mkETT("5441T2", TT2));
        lst.add(mkSEC("4.4.2", "Graph and identify transformations of <em>y</em> = sec <em>x</em>, "
                               + "<em>y</em> = csc <em>x</em>, and <em>y</em> = cot <em>x</em>."));
        lst.add(mkETT("5442E1", EX1));
        lst.add(mkETT("5442E2", EX2));
        lst.add(mkETT("5442E3", EX3));
        lst.add(mkETT("5442T1", TT1));
        lst.add(mkETT("5442T2", TT2));
        lst.add(mkETT("5442T3", TT3));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_5,
                "Model real world situations with periodic functions"));
        lst.add(mkVid("545OV", OVVID));
        // lst.add(mkPdf("545OV", OVPDF));
        lst.add(mkTip("12545Tips"));
        lst.add(mkSEC("4.5.1", "Identify a periodic function of a data set."));
        lst.add(mkETT("5451E1", EX1));
        lst.add(mkETT("5451E2", EX2));
        lst.add(mkETT("5451T1", TT1));
        lst.add(mkETT("5451T2", TT2));
        lst.add(mkSEC("4.5.2", "Solve problems using periodic functions."));
        lst.add(mkETT("5452E1", EX1));
        lst.add(mkETT("5452E2", EX2));
        lst.add(mkETT("5452E3", EX3));
        lst.add(mkETT("5452T1", TT1));
        lst.add(mkETT("5452T2", TT2));
        lst.add(mkETT("5452T3", TT3));
    }

    /**
     * Builds lesson components for MATH 1250.
     *
     * @param lst the list to which to add components
     */
    private static void build1250(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M1250;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Compute angles in triangles."));
        lst.add(mkETT("5P1a", EXA));
        lst.add(mkETT("5P1b", EXB));
        lst.add(mkSEC("2", "Simplify radicals."));
        lst.add(mkETT("5P2a", EXA));
        lst.add(mkETT("5P2b", EXB));
        lst.add(mkSEC("3", "Solving similar triangles."));
        lst.add(mkETT("5P3a", EXA));
        lst.add(mkETT("5P3b", EXB));
        lst.add(mkSEC("4", "Determine the equation of a circle."));
        lst.add(mkETT("5P4a", EXA));
        lst.add(mkETT("5P4b", EXB));
        lst.add(mkSEC("5", "Compose functions."));
        lst.add(mkETT("5P5a", EXA));
        lst.add(mkETT("5P5b", EXB));
        lst.add(mkSEC("6", "Transform functions I."));
        lst.add(mkETT("5P6a", EXA));
        lst.add(mkETT("5P6b", EXB));
        lst.add(mkSEC("7", "Transform functions II."));
        lst.add(mkETT("5P7a", EXA));
        lst.add(mkETT("5P7b", EXB));
        lst.add(mkSEC("8", "Compute circumference."));
        lst.add(mkETT("5P8a", EXA));
        lst.add(mkETT("5P8b", EXB));
        lst.add(mkSEC("9", "Compute area of a circle."));
        lst.add(mkETT("5P9a", EXA));
        lst.add(mkETT("5P9b", EXB));
        lst.add(mkSEC("10", "Solve equations."));
        lst.add(mkETT("5P10a", EXA));
        lst.add(mkETT("5P10b", EXB));

        // 1: Introduction to Trigonometric Functions
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkPreVid("511OV", OVVIDPRE));
        lst.add(mkPrePdf("511OV", OVPDF));
        lst.add(mkTip("12511Tips"));
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_1,
                "Solve problems using the Pythagorean Theorem."));
        lst.add(mkSEC("1.1.1", "Solve problems using the Pythagorean Theorem."));
        lst.add(mkETT("5111E1", EX1));
        lst.add(mkETT("5111E2", EX2));
        lst.add(mkETT("5111E3", EX3));
        lst.add(mkETT("5111T1", TT1));
        lst.add(mkETT("5111T2", TT2));
        lst.add(mkETT("5111T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_2,
                "Solve problems using 45-45-90 right triangles."));
        lst.add(mkSEC("1.2.1", "Solve problems using 45-45-90 right triangles."));
        lst.add(mkETT("5112E1", EX1));
        lst.add(mkETT("5112E2", EX2));
        lst.add(mkETT("5112E3", EX3));
        lst.add(mkETT("5112T1", TT1));
        lst.add(mkETT("5112T2", TT2));
        lst.add(mkETT("5112T3", TT3));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_3,
                "Solve problems using 30-60-90 right triangles."));
        lst.add(mkSEC("1.3.1", "Solve problems using 30-60-90 right triangles."));
        lst.add(mkETT("5113E1", EX1));
        lst.add(mkETT("5113E2", EX2));
        lst.add(mkETT("5113E3", EX3));
        lst.add(mkETT("5113T1", TT1));
        lst.add(mkETT("5113T2", TT2));
        lst.add(mkETT("5113T3", TT3));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkPreVid("512OV", OVVIDPRE));
        lst.add(mkPrePdf("512OV", OVPDF));
        lst.add(mkTip("12512Tips"));
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_4,
                "Determine basic trigonometric function values for right triangles."));
        lst.add(mkSEC("1.4.1", "Determine basic trigonometric function values for right triangles."));
        lst.add(mkETT("5121E1", EX1));
        lst.add(mkETT("5121E2", EX2));
        lst.add(mkETT("5121E3", EX3));
        lst.add(mkETT("5121E4", EX4));
        lst.add(mkETT("5121T1", TT1));
        lst.add(mkETT("5121T2", TT2));
        lst.add(mkETT("5121T3", TT3));
        lst.add(mkETT("5121T4", TT4));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_5,
                "Determine basic trigonometric function values for special right triangles."));
        lst.add(mkSEC("1.5.1", "Determine basic trigonometric function values for special right triangles."));
        lst.add(mkPdf("5122summary", "Summary"));
        lst.add(mkETT("5122E1", EX1));
        lst.add(mkETT("5122E2", EX2));
        lst.add(mkETT("5122E3", EX3));
        lst.add(mkETT("5122E4", EX4));
        lst.add(mkETT("5122T1", TT1));
        lst.add(mkETT("5122T2", TT2));
        lst.add(mkETT("5122T3", TT3));
        lst.add(mkETT("5122T4", TT4));

        startLesson(prefix + ".16", 1, 6);
        lst.add(mkPreVid("513OV", OVVIDPRE));
        lst.add(mkPrePdf("513OV", OVPDF));
        lst.add(mkTip("12513Tips"));
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_6,
                "Use basic trigonometric functions to solve right triangle problems."));
        lst.add(mkSEC("1.6.1", "Use basic trigonometric functions to solve right triangle problems."));
        lst.add(mkETT("5131E1", EX1));
        lst.add(mkETT("5131E2", EX2));
        lst.add(mkETT("5131E3", EX3));
        lst.add(mkETT("5131T1", TT1));
        lst.add(mkETT("5131T2", TT2));
        lst.add(mkETT("5131T3", TT3));

        startLesson(prefix + ".17", 1, 7);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_7,
                "Use inverses to solve right triangle problems."));
        lst.add(mkSEC("1.7.1", "Use inverses to solve right triangle problems."));
        lst.add(mkETT("5132E1", EX1));
        lst.add(mkETT("5132E2", EX2));
        lst.add(mkETT("5132E3", EX3));
        lst.add(mkETT("5132T1", TT1));
        lst.add(mkETT("5132T2", TT2));
        lst.add(mkETT("5132T3", TT3));

        startLesson(prefix + ".18", 1, 8);
        lst.add(mkPreVid("514OV", OVVIDPRE));
        lst.add(mkPrePdf("514OV", OVPDF));
        lst.add(mkTip("12514Tips"));
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_8,
                "Determine basic trigonometric function values using reference angles."));
        lst.add(mkSEC("1.8.1", "Determine basic trigonometric function values using reference angles."));
        lst.add(mkETT("5141E1", EX1));
        lst.add(mkETT("5141E2", EX2));
        lst.add(mkETT("5141E3", EX3));
        lst.add(mkETT("5141E4", EX4));
        lst.add(mkETT("5141E5", EX5));
        lst.add(mkETT("5141T1", TT1));
        lst.add(mkETT("5141T2", TT2));
        lst.add(mkETT("5141T3", TT3));
        lst.add(mkETT("5141T4", TT4));
        lst.add(mkETT("5141T5", TT5));

        startLesson(prefix + ".19", 1, 9);
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_9,
                "Determine trigonometric function values using reference angles."));
        lst.add(mkSEC("1.9.1", "Determine trigonometric function values using reference angles."));
        lst.add(mkETT("5142E1", EX1));
        lst.add(mkETT("5142E2", EX2));
        lst.add(mkETT("5142E3", EX3));
        lst.add(mkETT("5142E4", EX4));
        lst.add(mkETT("5142E5", EX5));
        lst.add(mkETT("5142T1", TT1));
        lst.add(mkETT("5142T2", TT2));
        lst.add(mkETT("5142T3", TT3));
        lst.add(mkETT("5142T4", TT4));
        lst.add(mkETT("5142T5", TT5));

        startLesson(prefix + ".110", 1, 10);
        lst.add(mkTip("12515Tips"));
        lst.add(mkLH2("1: Introduction to Trigonometric Functions", O1_10, "Solve trigonometric applications."));
        lst.add(mkSEC("1.10.1", "Solve angle of elevation problems."));
        lst.add(mkETT("5151E1", EX1));
        lst.add(mkETT("5151E2", EX2));
        lst.add(mkETT("5151T1", TT1));
        lst.add(mkETT("5151T2", TT2));
        lst.add(mkSEC("1.10.2", "Solve angle of depression problems."));
        lst.add(mkETT("5152E1", EX1));
        lst.add(mkETT("5152E2", EX2));
        lst.add(mkETT("5152T1", TT1));
        lst.add(mkETT("5152T2", TT2));
        lst.add(mkSEC("1.10.3", "Solve other applications."));
        lst.add(mkETT("5153E1", EX1));
        lst.add(mkETT("5153T1", TT1));

        // 2: Law of Sines and Law of Cosines
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPreVid("521OV", OVVIDPRE));
        lst.add(mkPrePdf("521OV", OVPDF));
        lst.add(mkTip("12521Tips"));
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_1, "Derive and identify the Law of Sines."));
        lst.add(mkSEC("2.1.1", "Derive and identify the Law of Sines."));
        lst.add(mkETT("5211E1", EX1));
        lst.add(mkETT("5211E2", EX2));
        lst.add(mkETT("5211T1", TT1));
        lst.add(mkETT("5211T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_2, "Derive and identify the Law of Cosines."));
        lst.add(mkSEC("2.2.1", "Derive and identify the Law of Cosines."));
        lst.add(mkETT("5212E1", EX1));
        lst.add(mkETT("5212E2", EX2));
        lst.add(mkETT("5212T1", TT1));
        lst.add(mkETT("5212T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkPreVid("522OV", OVVIDPRE));
        lst.add(mkPrePdf("522OV", OVPDF));
        lst.add(mkTip("12522Tips"));
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_3, "Apply the Law of Sines I: Two angles"));
        lst.add(mkSEC("2.3.1", "Solve triangles given two angles (AAS)."));
        lst.add(mkETT("5221E1", EX1));
        lst.add(mkETT("5221E2", EX2));
        lst.add(mkETT("5221T1", TT1));
        lst.add(mkETT("5221T2", TT2));
        lst.add(mkSEC("2.3.2", "Solve triangles given two angles (ASA)."));
        lst.add(mkETT("5222E1", EX1));
        lst.add(mkETT("5222E2", EX2));
        lst.add(mkETT("5222T1", TT1));
        lst.add(mkETT("5222T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkPreVid("523OV", OVVIDPRE));
        lst.add(mkPrePdf("523OV", OVPDF));
        lst.add(mkTip("12523Tips"));
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_4,
                "Apply the Law of Sines II: Two Sides: The Ambiguous Case"));
        lst.add(mkSEC("2.4.1", "Solve the ambiguous case with two solutions."));
        lst.add(mkETT("5231E1", EX1));
        lst.add(mkETT("5231E2", EX2));
        lst.add(mkETT("5231T1", TT1));
        lst.add(mkETT("5231T2", TT2));
        lst.add(mkSEC("2.4.2", "Solve the ambiguous case with one solution."));
        lst.add(mkETT("5232E1", EX1));
        lst.add(mkETT("5232E2", EX2));
        lst.add(mkETT("5232T1", TT1));
        lst.add(mkETT("5232T2", TT2));
        lst.add(mkSEC("2.4.3", "Solve the ambiguous case with no solution."));
        lst.add(mkETT("5233E1", EX1));
        lst.add(mkETT("5233E2", EX2));
        lst.add(mkETT("5233T1", TT1));
        lst.add(mkETT("5233T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_5, "Solve applications with Law of Sines."));
        lst.add(mkSEC("2.5.1", "Solve applications with Law of Sines."));
        lst.add(mkETT("5223E1", EX1));
        lst.add(mkETT("5223E2", EX2));
        lst.add(mkETT("5223T1", TT1));
        lst.add(mkETT("5223T2", TT2));

        startLesson(prefix + ".26", 2, 6);
        lst.add(mkPreVid("524OV", OVVIDPRE));
        lst.add(mkPrePdf("524OV", OVPDF));
        lst.add(mkTip("12524Tips"));
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_6, "Apply the Law of Cosines."));
        lst.add(mkSEC("2.6.1", "Solve for a missing side."));
        lst.add(mkETT("5241E1", EX1));
        lst.add(mkETT("5241E2", EX2));
        lst.add(mkETT("5241T1", TT1));
        lst.add(mkETT("5241T2", TT2));
        lst.add(mkSEC("2.6.2", "Solve for a missing angle."));
        lst.add(mkETT("5242E1", EX1));
        lst.add(mkETT("5242E2", EX2));
        lst.add(mkETT("5242T1", TT1));
        lst.add(mkETT("5242T2", TT2));

        startLesson(prefix + ".27", 2, 7);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_7, "Solve applications with Law of Cosines."));
        lst.add(mkSEC("2.7.1", "Solve applications with Law of Cosines."));
        lst.add(mkETT("5243E1", EX1));
        lst.add(mkETT("5243E2", EX2));
        lst.add(mkETT("5243T1", TT1));
        lst.add(mkETT("5243T2", TT2));

        startLesson(prefix + ".28", 2, 8);
        lst.add(mkPreVid("525OV", OVVID1));
        lst.add(mkPreVid("525OV2", OVVID2));
        lst.add(mkPreVid("525OV3", OVVID3));
        lst.add(mkPrePdf("525OV", OVPDF));
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_8,
                "Given a vector, compute its magnitude, direction, and scalar product."));
        lst.add(mkSEC("2.8.1", "Given a vector, compute its magnitude, direction, and find resultant vectors."));
        lst.add(mkETT("5251E1", EX1));
        lst.add(mkETT("5251E2", EX2));
        lst.add(mkETT("5251T1", TT1));
        lst.add(mkETT("5251T2", TT2));

        startLesson(prefix + ".29", 2, 9);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_9, "Perform operations on vectors."));
        lst.add(mkSEC("2.9.1", "Solve applications of vectors in the <b>i</b>, <b>j</b> form."));
        lst.add(mkETT("5252E1", EX1));
        lst.add(mkETT("5252E2", EX2));
        lst.add(mkETT("5252T1", TT1));
        lst.add(mkETT("5252T2", TT2));

        startLesson(prefix + ".210", 2, 10);
        lst.add(mkLH2("2: Law of Sines and Law of Cosines", O2_10,
                "Compute the dot product and angle between two vectors."));
        lst.add(mkSEC("2.10.1", "Compute the dot product and angle between two vectors."));
        lst.add(mkETT("5253E1", EX1));
        lst.add(mkETT("5253E2", EX2));
        lst.add(mkETT("5253T1", TT1, true, false));
        lst.add(mkETT("5253T2", TT2));

        // 3: Unit Circle and Radian Measure
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPreVid("531OV", OVVIDPRE));
        lst.add(mkPrePdf("531OV", OVPDF));
        lst.add(mkTip("12531Tips"));
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_1, "Construct the unit circle."));
        lst.add(mkSEC("3.1.1", "Convert basic angles to radians."));
        lst.add(mkETT("5311E1", EX1));
        lst.add(mkETT("5311E2", EX2));
        lst.add(mkETT("5311T1", TT1));
        lst.add(mkETT("5311T2", TT2));
        lst.add(mkSEC("3.1.2", "Convert angles."));
        lst.add(mkETT("5312E1", EX1));
        lst.add(mkETT("5312E2", EX2));
        lst.add(mkETT("5312T1", TT1));
        lst.add(mkETT("5312T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkPreVid("532OV", OVVIDPRE));
        lst.add(mkPrePdf("532OV", OVPDF));
        lst.add(mkTip("12532Tips"));
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_2, "Identify points on the unit circle."));
        lst.add(mkSEC("3.2.1", "Identify points on the unit circle."));
        lst.add(mkETT("5321E1", EX1));
        lst.add(mkETT("5321E2", EX2));
        lst.add(mkETT("5321T1", TT1));
        lst.add(mkETT("5321T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_3,
                "Determine sine and cosine values from the unit circle."));
        lst.add(mkSEC("3.3.1", "Determine sine and cosine values from the unit circle."));
        lst.add(mkETT("5322E1", EX1));
        lst.add(mkETT("5322E2", EX2));
        lst.add(mkETT("5322T1", TT1));
        lst.add(mkETT("5322T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkPreVid("533OV", OVVIDPRE));
        lst.add(mkPrePdf("533OV", OVPDF));
        lst.add(mkTip("12533Tips"));
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_4,
                "Given an angle in radians compute trigonometric function values."));
        lst.add(mkSEC("3.4.1", "Given an angle in radians compute trigonometric function values."));
        lst.add(mkETT("5331E1", EX1));
        lst.add(mkETT("5331E2", EX2));
        lst.add(mkETT("5331E3", EX3));
        lst.add(mkETT("5331E4", EX4));
        lst.add(mkETT("5331T1", TT1));
        lst.add(mkETT("5331T2", TT2));
        lst.add(mkETT("5331T3", TT3));
        lst.add(mkETT("5331T4", TT4));
        lst.add(mkETT("5331T5", TT5));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_5,
                "Given a trigonometric value determine the angle in radians and degrees."));
        lst.add(mkSEC("3.5.1", "Given a trigonometric value determine the angle in radians and degrees."));
        lst.add(mkETT("5332E1", EX1));
        lst.add(mkETT("5332E2", EX2));
        lst.add(mkETT("5332E3", EX3));
        lst.add(mkETT("5332E4", EX4));
        lst.add(mkETT("5332E5", EX5));
        lst.add(mkETT("5332E6", EX6));
        lst.add(mkETT("5332T1", TT1));
        lst.add(mkETT("5332T2", TT2));
        lst.add(mkETT("5332T3", TT3));
        lst.add(mkETT("5332T4", TT4));
        lst.add(mkETT("5332T5", TT5));
        lst.add(mkETT("5332T6", TT6));

        startLesson(prefix + ".36", 3, 6);
        lst.add(mkPreVid("534OV", OVVIDPRE));
        lst.add(mkPrePdf("534OV", OVPDF));
        lst.add(mkTip("12534Tips"));
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_6, "Compute arc length."));
        lst.add(mkSEC("3.6.1", "Compute arc length."));
        lst.add(mkETT("5341E1", EX1));
        lst.add(mkETT("5341E2", EX2));
        lst.add(mkETT("5341T1", TT1));
        lst.add(mkETT("5341T2", TT2));

        startLesson(prefix + ".37", 3, 7);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_7, "Compute area of a sector."));
        lst.add(mkSEC("3.7.1", "Compute area of a sector."));
        lst.add(mkETT("5342E1", EX1));
        lst.add(mkETT("5342E2", EX2));
        lst.add(mkETT("5342T1", TT1));
        lst.add(mkETT("5342T2", TT2));

        startLesson(prefix + ".38", 3, 8);
        lst.add(mkPreVid("535OV", OVVIDPRE));
        lst.add(mkPreVid("535OV2", OVVID2));
        lst.add(mkPdf("535OV", OVPDF));
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_8, "Perform operations on complex numbers."));
        lst.add(mkSEC("3.8.1", "Perform operations on complex numbers."));
        lst.add(mkETT("5351E1", EX1));
        lst.add(mkETT("5351T1", TT1));

        startLesson(prefix + ".39", 3, 9);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_9,
                "Given a complex number, determine relationships between its coordinates, direction, and magnitude."));
        lst.add(mkSEC("3.9.1",
                "Given a complex number, determine relationships between its coordinates, direction, and magnitude."));
        lst.add(mkETT("5352E1", EX1));
        lst.add(mkETT("5352T1", TT1));

        startLesson(prefix + ".310", 3, 10);
        lst.add(mkLH2("3: Unit Circle and Radian Measure", O3_10,
                "Convert between rectangular and trigonometric forms."));
        lst.add(mkSEC("3.10.1", "Convert between rectangular and trigonometric forms."));
        lst.add(mkETT("5353E1", EX1));
        lst.add(mkETT("5353T1", TT1));

        // 4: Periodic Functions and Applications
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkPreVid("541OV", OVVIDPRE));
        lst.add(mkPrePdf("541OV", OVPDF1));
        lst.add(mkPrePdf("541OV2", OVPDF2));
        lst.add(mkTip1("12541Tips1"));
        lst.add(mkTip2("12541Tips2"));
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_1, "Graph sine, cosine, and tangent."));
        lst.add(mkSEC("4.1.1", "Graph sine, cosine, and tangent."));
        lst.add(mkETT("5411E1", EX1));
        lst.add(mkETT("5411E2", EX2));
        lst.add(mkETT("5411E3", EX3));
        lst.add(mkETT("5411T1", TT1));
        lst.add(mkETT("5411T2", TT2));
        lst.add(mkETT("5411T3", TT3));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_2,
                "Graph and identify vertical and horizontal shifts of "
                + "<em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>."));
        lst.add(mkSEC("4.2.1", "Graph and identify vertical and horizontal shifts of "
                               + "<em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>."));
        lst.add(mkETT("5412E1", EX1));
        lst.add(mkETT("5412E2", EX2));
        lst.add(mkETT("5412E3", EX3));
        lst.add(mkETT("5412E4", EX4));
        lst.add(mkETT("5412E5", EX5));
        lst.add(mkETT("5412E6", EX6));
        lst.add(mkETT("5412E7", EX7));
        lst.add(mkETT("5412E8", EX8));
        lst.add(mkETT("5412T1", TT1));
        lst.add(mkETT("5412T2", TT2));
        lst.add(mkETT("5412T3", TT3));
        lst.add(mkETT("5412T4", TT4));
        lst.add(mkETT("5412T5", TT5));
        lst.add(mkETT("5412T6", TT6));
        lst.add(mkETT("5412T7", TT7));
        lst.add(mkETT("5412T8", TT8));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_3,
                "Graph and identify translations of <em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>."));
        lst.add(mkSEC("4.3.1",
                "Graph and identify translations of <em>y</em> = sin <em>x</em> and <em>y</em> = cos <em>x</em>."));
        lst.add(mkETT("5413E1", EX1));
        lst.add(mkETT("5413E2", EX2));
        lst.add(mkETT("5413E3", EX3));
        lst.add(mkETT("5413T1", TT1));
        lst.add(mkETT("5413T2", TT2));
        lst.add(mkETT("5413T3", TT3));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkPreVid("542OV", OVVIDPRE));
        lst.add(mkPrePdf("542OV", OVPDF1));
        lst.add(mkTip("12542Tips"));
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_4,
                "Graph and identify amplitude transformations of the form "
                + "<em>y = a</em> sin <em>x</em> and <em>y = a</em> cos <em>x</em>."));
        lst.add(mkSEC("4.4.1", "Graph and identify amplitude transformations of the form "
                               + "<em>y = a</em> sin <em>x</em> and <em>y = a</em> cos <em>x</em>."));
        lst.add(mkETT("5421E1", EX1));
        lst.add(mkETT("5421E2", EX2));
        lst.add(mkETT("5421E3", EX3));
        lst.add(mkETT("5421E4", EX4));
        lst.add(mkETT("5421E5", EX5));
        lst.add(mkETT("5421E6", EX6));
        lst.add(mkETT("5421T1", TT1));
        lst.add(mkETT("5421T2", TT2));
        lst.add(mkETT("5421T3", TT3));
        lst.add(mkETT("5421T4", TT4));
        lst.add(mkETT("5421T5", TT5));
        lst.add(mkETT("5421T6", TT6));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_5,
                "Graph and identify period transformations of the form "
                + "<em>y = a</em> sin <em>bx</em> and <em>y = a</em> cos <em>bx</em>."));
        lst.add(mkSEC("4.5.1", "Graph and identify period transformations of the form "
                               + "<em>y = a</em> sin <em>bx</em> and <em>y = a</em> cos <em>bx</em>."));
        lst.add(mkETT("5422E1", EX1));
        lst.add(mkETT("5422E2", EX2));
        lst.add(mkETT("5422E3", EX3));
        lst.add(mkETT("5422E4", EX4));
        lst.add(mkETT("5422E5", EX5));
        lst.add(mkETT("5422E6", EX6));
        lst.add(mkETT("5422T1", TT1));
        lst.add(mkETT("5422T2", TT2));
        lst.add(mkETT("5422T3", TT3));
        lst.add(mkETT("5422T4", TT4));
        lst.add(mkETT("5422T5", TT5));
        lst.add(mkETT("5422T6", TT6));

        startLesson(prefix + ".46", 4, 6);
        lst.add(mkPreVid("543OV", OVVIDPRE));
        lst.add(mkPrePdf("543OV", OVPDF));
        lst.add(mkTip("12543Tips"));
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_6,
                "Graph and identify transformations of the form <em>y = a</em> sin <em>b(x-h) + k</em>."));
        lst.add(mkSEC("4.6.1",
                "Graph and identify transformations of the form <em>y = a</em> sin <em>b(x-h) + k</em>."));
        lst.add(mkETT("5431E1", EX1));
        lst.add(mkETT("5431E2", EX2));
        lst.add(mkETT("5431E3", EX3));
        lst.add(mkETT("5431T1", TT1));
        lst.add(mkETT("5431T2", TT2));
        lst.add(mkETT("5431T3", TT3));

        startLesson(prefix + ".47", 4, 7);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_7,
                "Graph and identify transformations of the form <em>y = a</em> cos <em>b(x-h) + k</em>."));
        lst.add(mkSEC("4.7.1",
                "Graph and identify transformations of the form <em>y = a</em> cos <em>b(x-h) + k</em>."));
        lst.add(mkETT("5432E1", EX1));
        lst.add(mkETT("5432E2", EX2));
        lst.add(mkETT("5432E3", EX3));
        lst.add(mkETT("5432T1", TT1));
        lst.add(mkETT("5432T2", TT2));
        lst.add(mkETT("5432T3", TT3));

        startLesson(prefix + ".48", 4, 8);
        lst.add(mkPreVid("544OV", OVVID));
        lst.add(mkPrePdf("544OV", OVPDF));
        lst.add(mkTip("12544Tips"));
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_8,
                "Graph and identify transformations of <em>y</em> = tan <em>x</em>."));
        lst.add(mkSEC("4.8.1", "Graph and identify transformations of <em>y</em> = tan <em>x</em>."));
        lst.add(mkETT("5441E1", EX1));
        lst.add(mkETT("5441E2", EX2));
        lst.add(mkETT("5441T1", TT1));
        lst.add(mkETT("5441T2", TT2));

        startLesson(prefix + ".49", 4, 9);
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_9, "Graph and identify transformations of "
                                                                      + "<em>y</em> = sec <em>x</em>, <em>y</em> = " +
                                                                      "csc <em>x</em>, and <em>y</em> = cot " +
                                                                      "<em>x</em>."));
        lst.add(mkSEC("4.9.1", "Graph and identify transformations of "
                               + "<em>y</em> = sec <em>x</em>, <em>y</em> = csc <em>x</em>, and <em>y</em> = cot " +
                               "<em>x</em>."));
        lst.add(mkETT("5442E1", EX1));
        lst.add(mkETT("5442E2", EX2));
        lst.add(mkETT("5442E3", EX3));
        lst.add(mkETT("5442T1", TT1));
        lst.add(mkETT("5442T2", TT2));
        lst.add(mkETT("5442T3", TT3));

        startLesson(prefix + ".410", 4, 10);
        lst.add(mkTip("12545Tips"));
        lst.add(mkLH2("4: Periodic Functions and Applications", O4_10, "Model real world situations with periodic " +
                                                                       "functions."));
        lst.add(mkSEC("4.10.1", "Identify a periodic function of a data set."));
        lst.add(mkETT("5451E1", EX1));
        lst.add(mkETT("5451E2", EX2));
        lst.add(mkETT("5451T1", TT1));
        lst.add(mkETT("5451T2", TT2));
        lst.add(mkSEC("4.10.2", "Solve problems using periodic functions."));
        lst.add(mkETT("5452E1", EX1));
        lst.add(mkETT("5452E2", EX2));
        lst.add(mkETT("5452E3", EX3));
        lst.add(mkETT("5452T1", TT1));
        lst.add(mkETT("5452T2", TT2));
        lst.add(mkETT("5452T3", TT3));
    }

    /**
     * Builds lesson components for MATH 126.
     *
     * @param lst the list to which to add components
     */
    private static void build126(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M126;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Identify special products."));
        lst.add(mkETT("6P1a", EXA));
        lst.add(mkETT("6P1b", EXB));
        lst.add(mkSEC("2", "Solve equations."));
        lst.add(mkETT("6P2a", EXA));
        lst.add(mkETT("6P2b", EXB));
        lst.add(mkSEC("3", "Apply the Pythagorean Theorem."));
        lst.add(mkETT("6P3a", EXA));
        lst.add(mkETT("6P3b", EXB));
        lst.add(mkSEC("4", "Compose functions."));
        lst.add(mkETT("6P4a", EXA));
        lst.add(mkETT("6P4b", EXB));
        lst.add(mkSEC("5", "Common unit angles."));
        lst.add(mkETT("6P5a", EXA));
        lst.add(mkETT("6P5b", EXB));
        lst.add(mkSEC("6", "Identify transformations."));
        lst.add(mkETT("6P6a", EXA));
        lst.add(mkETT("6P6b", EXB));
        lst.add(mkSEC("7", "Convert angles."));
        lst.add(mkETT("6P7a", EXA));
        lst.add(mkETT("6P7b", EXB));
        lst.add(mkSEC("8", "Multiply and factor polynomials."));
        lst.add(mkETT("6P8a", EXA));
        lst.add(mkETT("6P8b", EXB));
        lst.add(mkSEC("9", "Simplify radicals."));
        lst.add(mkETT("6P9a", EXA));
        lst.add(mkETT("6P9b", EXB));
        lst.add(mkSEC("10", "Solve systems of equations."));
        lst.add(mkETT("6P10a", EXA));
        lst.add(mkETT("6P10a", EXB));

        // 1: Inverses
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkLH2("1: Inverses", O1_1, "Review of common trigonometric functions"));
        lst.add(mkVid("611OV", OVVID));
        lst.add(mkPdf("611OV", OVPDF));
        lst.add(mkTip("12611Tips"));
        lst.add(mkSEC("1.1.1", "Determine values of trigonometric functions for special angles."));
        lst.add(mkETT("6111E1", EX1));
        lst.add(mkETT("6111E2", EX2));
        lst.add(mkETT("6111E3", EX3));
        lst.add(mkETT("6111E4", EX4));
        lst.add(mkETT("6111E5", EX5));
        lst.add(mkETT("6111T1", TT1));
        lst.add(mkETT("6111T2", TT2));
        lst.add(mkETT("6111T3", TT3));
        lst.add(mkETT("6111T4", TT4));
        lst.add(mkETT("6111T5", TT5));
        lst.add(mkSEC("1.1.2", "Detemine values of trigonometric functions for angles."));
        lst.add(mkETT("6112E1", EX1));
        lst.add(mkETT("6112E2", EX2));
        lst.add(mkETT("6112E3", EX3));
        lst.add(mkETT("6112T1", TT1));
        lst.add(mkETT("6112T2", TT2));
        lst.add(mkETT("6112T3", TT3));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Inverses", O1_2, "Graph inverse trigonometric functions"));
        lst.add(mkVid("612OV", OVVID));
        lst.add(mkPdf("612OV", OVPDF));
        lst.add(mkTip("12612Tips"));
        lst.add(mkSEC("1.2.1",
                "Determine the domain and range, and graph for <em>y</em> = sin<sup>-1</sup> (<em>x</em>)."));
        lst.add(mkETT("6121E1", EX1));
        lst.add(mkETT("6121T1", TT1));
        lst.add(mkETT("6121T2", TT2));
        lst.add(mkSEC("1.2.2", "Determine the domain and range, and graph for "
                               + "<em>y</em> = cos<sup>-1</sup> (<em>x</em>) and <em>y</em> = tan<sup>-1</sup> " +
                               "(<em>x</em>)."));
        lst.add(mkETT("6122E1", EX1));
        lst.add(mkETT("6122T1", TT1));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkLH2("1: Inverses", O1_3, "Determine angles from trigonometric values"));
        lst.add(mkVid("613OV", OVVID));
        lst.add(mkPdf("613OV", OVPDF));
        lst.add(mkTip("12613Tips"));
        lst.add(mkSEC("1.3.1", "Determine values for inverse functions."));
        lst.add(mkETT("6131E1", EX1));
        lst.add(mkETT("6131E2", EX2));
        lst.add(mkETT("6131E3", EX3));
        lst.add(mkETT("6131E4", EX4));
        lst.add(mkETT("6131E5", EX5));
        lst.add(mkETT("6131E6", EX6));
        lst.add(mkETT("6131T1", TT1));
        lst.add(mkETT("6131T2", TT2));
        lst.add(mkETT("6131T3", TT3));
        lst.add(mkETT("6131T4", TT4));
        lst.add(mkETT("6131T5", TT5));
        lst.add(mkETT("6131T6", TT6));
        lst.add(mkSEC("1.3.2", "Determine solutions to equations."));
        lst.add(mkETT("6132E1", EX1));
        lst.add(mkETT("6132E2", EX2));
        lst.add(mkETT("6132E3", EX3));
        lst.add(mkETT("6132E4", EX4));
        lst.add(mkETT("6132E5", EX5));
        lst.add(mkETT("6132E6", EX6));
        lst.add(mkETT("6132T1", TT1));
        lst.add(mkETT("6132T2", TT2));
        lst.add(mkETT("6132T3", TT3));
        lst.add(mkETT("6132T4", TT4));
        lst.add(mkETT("6132T5", TT5));
        lst.add(mkETT("6132T6", TT6));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Inverses", O1_4, "Use composition to find values"));
        lst.add(mkVid("614OV", OVVID));
        lst.add(mkPdf("614OV", OVPDF));
        lst.add(mkTip("12614Tips"));
        lst.add(mkSEC("1.4.1", "Use composition to find exact values."));
        lst.add(mkETT("6141E1", EX1));
        lst.add(mkETT("6141E2", EX2));
        lst.add(mkETT("6141T1", TT1));
        lst.add(mkETT("6141T2", TT2));
        lst.add(mkSEC("1.4.2", "Use composition to explore inverses."));
        lst.add(mkETT("6142E1", EX1));
        lst.add(mkETT("6142E2", EX2));
        lst.add(mkETT("6142E3", EX3));
        lst.add(mkETT("6142E4", EX4));
        lst.add(mkETT("6142T1", TT1));
        lst.add(mkETT("6142T2", TT2));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkLH2("1: Inverses", O1_5, "Solve applications"));
        lst.add(mkVid("615OV", OVVID));
        // lst.add(mkPdf("615OV", OVPDF));
        lst.add(mkTip("12615Tips"));
        lst.add(mkSEC("1.5.1", "Solve angle of elevation problems."));
        lst.add(mkETT("6151E1", EX1));
        lst.add(mkETT("6151T1", TT1));
        lst.add(mkSEC("1.5.2", "Solve other applications."));
        lst.add(mkETT("6152E1", EX1));
        lst.add(mkETT("6152T1", TT1));

        // 2: Fundamental Identities
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkLH2("2: Fundamental Identities", O2_1, "Develop and apply basic identities"));
        lst.add(mkVid("621OV", OVVID));
        lst.add(mkPdf("621OV", OVPDF));
        lst.add(mkTip("12621Tips"));
        lst.add(mkSEC("2.1.1", "Verify identities."));
        lst.add(mkETT("6211E1", EX1));
        lst.add(mkETT("6211E2", EX2));
        lst.add(mkETT("6211T1", TT1));
        lst.add(mkETT("6211T2", TT2));
        lst.add(mkSEC("2.1.2", "Determine the domain of vailidity for trigonometric identities."));
        lst.add(mkETT("6212E1", EX1));
        lst.add(mkETT("6212E2", EX2));
        lst.add(mkETT("6212T1", TT1));
        lst.add(mkETT("6212T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Fundamental Identities", O2_2, "Develop and apply cofunction and odd-even identities"));
        lst.add(mkVid("622OV", OVVID));
        lst.add(mkPdf("622OV", OVPDF));
        lst.add(mkTip("12622Tips"));
        lst.add(mkSEC("2.2.1", "Determine cofunction identities."));
        lst.add(mkETT("6221E1", EX1));
        lst.add(mkETT("6221E2", EX2));
        lst.add(mkETT("6221T1", TT1));
        lst.add(mkETT("6221T2", TT2));
        lst.add(mkSEC("2.2.2", "Determine odd-even identities."));
        lst.add(mkETT("6222E1", EX1));
        lst.add(mkETT("6222E2", EX2));
        lst.add(mkETT("6222T1", TT1));
        lst.add(mkETT("6222T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkLH2("2: Fundamental Identities", O2_3, "Develop and apply Pythagorean identities"));
        lst.add(mkVid("623OV", OVVID));
        lst.add(mkPdf("623OV", OVPDF));
        lst.add(mkTip("12623Tips"));
        lst.add(mkSEC("2.3.1", "Develop the Pythagorean identities."));
        lst.add(mkETT("6231E1", EX1));
        lst.add(mkETT("6231E2", EX2));
        lst.add(mkETT("6231T1", TT1));
        lst.add(mkSEC("2.3.2", "Apply the Pythagorean identities."));
        lst.add(mkETT("6232E1", EX1));
        lst.add(mkETT("6232E2", EX2));
        lst.add(mkETT("6232T1", TT1));
        lst.add(mkETT("6232T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Fundamental Identities", O2_4, "Verify identities"));
        lst.add(mkVid("624OV", OVVID));
        lst.add(mkPdf("624OV", OVPDF));
        lst.add(mkTip("12624Tips"));
        lst.add(mkSEC("2.4.1", "Prove elementary identities."));
        lst.add(mkETT("6241E1", EX1));
        lst.add(mkETT("6241E2", EX2));
        lst.add(mkETT("6241T1", TT1));
        lst.add(mkSEC("2.4.2", "Prove identities."));
        lst.add(mkETT("6242E1", EX1));
        lst.add(mkETT("6242E2", EX2));
        lst.add(mkETT("6242E3", EX3));
        lst.add(mkETT("6242T1", TT1));
        lst.add(mkETT("6242T2", TT2));
        lst.add(mkETT("6242T3", TT3));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkLH2("2: Fundamental Identities", O2_5, "Solve equations"));
        lst.add(mkVid("625OV", OVVID));
        lst.add(mkPdf("625OV", OVPDF));
        lst.add(mkTip("12625Tips"));
        lst.add(mkSEC("2.5.1", "Solve trigonometric equations."));
        lst.add(mkETT("6251E1", EX1));
        lst.add(mkETT("6251E2", EX2));
        lst.add(mkETT("6251T1", TT1));
        lst.add(mkETT("6251T2", TT2));
        lst.add(mkSEC("2.5.2", "Use Pythagorean identities to solve equations."));
        lst.add(mkETT("6252E1", EX1));
        lst.add(mkETT("6252E2", EX2));
        lst.add(mkETT("6252T1", TT1));
        lst.add(mkETT("6252T2", TT2));

        // 3: Sum and Difference Identities
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_1, "Develop and apply the Cosine sum identitiy"));
        lst.add(mkVid("631OV", OVVID));
        lst.add(mkPdf("631OV", OVPDF));
        lst.add(mkTip("12631Tips"));
        lst.add(mkSEC("3.1.1", "Derive identities from the cosine sum identity."));
        lst.add(mkVid("6311", OV));
        lst.add(mkETT("6311E1", EX1));
        lst.add(mkETT("6311T1", TT1));
        lst.add(mkSEC("3.1.2", "Find exact values with the cosine sum identity."));
        lst.add(mkETT("6312E1", EX1));
        lst.add(mkETT("6312E2", EX2));
        lst.add(mkETT("6312T1", TT1));
        lst.add(mkETT("6312T2", TT2));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_2,
                "Develop and apply the cosine of a difference identity"));
        lst.add(mkVid("632OV", OVVID));
        lst.add(mkPdf("632OV", OVPDF));
        lst.add(mkTip("12632Tips"));
        lst.add(mkSEC("3.2.1", "Derive identities from the cosine of a difference identity."));
        lst.add(mkVid("6321", OV));
        lst.add(mkETT("6321E1", EX1));
        lst.add(mkETT("6321T1", TT1));
        lst.add(mkSEC("3.2.2", "Find exact values with the cosine of a difference identity."));
        lst.add(mkETT("6322E1", EX1));
        lst.add(mkETT("6322E2", EX2));
        lst.add(mkETT("6322T1", TT1));
        lst.add(mkETT("6322T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_3, "Develop and apply the sine sum identity"));
        lst.add(mkVid("633OV", OVVID));
        lst.add(mkPdf("633OV", OVPDF));
        lst.add(mkTip("12633Tips"));
        lst.add(mkSEC("3.3.1", "Derive identities from the sine sum identity."));
        lst.add(mkVid("6331", OV));
        lst.add(mkETT("6331E1", EX1));
        lst.add(mkETT("6331T1", TT1));
        lst.add(mkSEC("3.3.2", "Find exact values with the sine sum identity."));
        lst.add(mkETT("6332E1", EX1));
        lst.add(mkETT("6332E2", EX2));
        lst.add(mkETT("6332T1", TT1));
        lst.add(mkETT("6332T2", TT2));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_4,
                "Develop and apply the sine of a difference identity"));
        lst.add(mkVid("634OV", OVVID));
        lst.add(mkPdf("634OV", OVPDF));
        lst.add(mkTip("12634Tips"));
        lst.add(mkSEC("3.4.1", "Derive identities with the sine of a difference identity."));
        lst.add(mkVid("6341", OV));
        lst.add(mkETT("6341E1", EX1));
        lst.add(mkETT("6341T1", TT1));
        lst.add(mkSEC("3.4.2", "Find exact values with the sine of a difference identity."));
        lst.add(mkETT("6342E1", EX1));
        lst.add(mkETT("6342E2", EX2));
        lst.add(mkETT("6342T1", TT1));
        lst.add(mkETT("6342T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_5,
                "Develop and apply the tangent of sums and differences"));
        lst.add(mkVid("635OV", OVVID));
        lst.add(mkPdf("635OV", OVPDF));
        lst.add(mkTip("12635Tips"));
        lst.add(mkSEC("3.5.1", "Derive identities from the tangent sum and difference identities."));
        lst.add(mkVid("6351", OV));
        lst.add(mkETT("6351E1", EX1));
        lst.add(mkETT("6351T1", TT1));
        lst.add(mkSEC("3.5.2", "Find exact values with the tangent sum and difference identities."));
        lst.add(mkETT("6352E1", EX1));
        lst.add(mkETT("6352T1", TT1));

        // 4: Identities and Equations
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkLH2("4: Identities and Equations", O4_1, "Derive and apply the double angle identity for sine"));
        lst.add(mkVid("641OV", OVVID));
        lst.add(mkPdf("641OV", OVPDF));
        lst.add(mkTip("12641Tips"));
        lst.add(mkSEC("4.1.1", "Derive identities using the double angle identity for sine."));
        lst.add(mkETT("6411E1", EX1));
        lst.add(mkETT("6411T1", TT1));
        lst.add(mkSEC("4.1.2", "Solve problems with the double angle identity for sine."));
        lst.add(mkETT("6412E1", EX1));
        lst.add(mkETT("6412E2", EX2));
        lst.add(mkETT("6412T1", TT1));
        lst.add(mkETT("6412T2", TT2));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Identities and Equations", O4_2,
                "Derive and apply the double angle identity for cosine and tangent"));
        lst.add(mkVid("642OV", OVVID));
        lst.add(mkPdf("642OV", OVPDF));
        lst.add(mkTip("12642Tips"));
        lst.add(mkSEC("4.2.1", "Derive the double angle identity for cosine and tangent."));
        lst.add(mkETT("6421E1", EX1));
        lst.add(mkETT("6421E2", EX2));
        lst.add(mkETT("6421T1", TT1));
        lst.add(mkETT("6421T2", TT2));
        lst.add(mkSEC("4.2.2", "Solve problems with the double angle identity for cosine and tangent."));
        lst.add(mkETT("6422E1", EX1));
        lst.add(mkETT("6422E2", EX2));
        lst.add(mkETT("6422T1", TT1));
        lst.add(mkETT("6422T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkLH2("4: Identities and Equations", O4_3, "Derive and apply the half angle identities"));
        lst.add(mkVid("643OV", OVVID));
        lst.add(mkPdf("643OV", OVPDF));
        lst.add(mkTip("12643Tips"));
        lst.add(mkSEC("4.3.1", "Derive the half angle formulas."));
        lst.add(mkETT("6431E1", EX1));
        lst.add(mkETT("6431T1", TT1));
        lst.add(mkSEC("4.3.2", "Solve problems using the half angle identities."));
        lst.add(mkETT("6432E1", EX1));
        lst.add(mkETT("6432E2", EX2));
        lst.add(mkETT("6432T1", TT1));
        lst.add(mkETT("6432T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Identities and Equations", O4_4, "Solve equations"));
        lst.add(mkVid("644OV", OVVID));
        lst.add(mkPdf("644OV", OVPDF));
        lst.add(mkTip("12644Tips"));
        lst.add(mkSEC("4.4.1", "Solve equations using the double angles."));
        lst.add(mkETT("6441E1", EX1));
        lst.add(mkETT("6441E2", EX2));
        lst.add(mkETT("6441T1", TT1));
        lst.add(mkETT("6441T2", TT2));
        lst.add(mkSEC("4.4.2", "Solve equations using half angles."));
        lst.add(mkETT("6442E1", EX1));
        lst.add(mkETT("6442E2", EX2));
        lst.add(mkETT("6442T1", TT1));
        lst.add(mkETT("6442T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkLH2("4: Identities and Equations", O4_5, "Solve equations using identities"));
        lst.add(mkVid("645OV", OVVID));
        lst.add(mkPdf("645OV", OVPDF));
        lst.add(mkTip("12645Tips"));
        lst.add(mkSEC("4.5.1", "Solve equations I."));
        lst.add(mkETT("6451E1", EX1));
        lst.add(mkETT("6451E2", EX2));
        lst.add(mkETT("6451T1", TT1));
        lst.add(mkETT("6451T2", TT2));
        lst.add(mkSEC("4.5.2", "Solve equations II."));
        lst.add(mkETT("6452E1", EX1));
        lst.add(mkETT("6452T1", TT1));
    }

    /**
     * Builds lesson components for MATH 1260.
     *
     * @param lst the list to which to add components
     */
    private static void build1260(final Collection<? super RawLessonComponent> lst) {

        final String crs = RawRecordConstants.M1260;
        courseId = crs;
        final String prefix = crs.replace("M ", "M");

        // 0: Skills review
        startLesson(prefix + ".01", 0, 1);
        lst.add(mkLH1(SR_MAT));
        lst.add(mkSEC("1", "Identify special products."));
        lst.add(mkETT("6P1a", EXA));
        lst.add(mkETT("6P1b", EXB));
        lst.add(mkSEC("2", "Solve equations."));
        lst.add(mkETT("6P2a", EXA));
        lst.add(mkETT("6P2b", EXB));
        lst.add(mkSEC("3", "Apply the Pythagorean Theorem."));
        lst.add(mkETT("6P3a", EXA));
        lst.add(mkETT("6P3b", EXB));
        lst.add(mkSEC("4", "Compose functions."));
        lst.add(mkETT("6P4a", EXA));
        lst.add(mkETT("6P4b", EXB));
        lst.add(mkSEC("5", "Common unit angles."));
        lst.add(mkETT("6P5a", EXA));
        lst.add(mkETT("6P5b", EXB));
        lst.add(mkSEC("6", "Identify transformations."));
        lst.add(mkETT("6P6a", EXA));
        lst.add(mkETT("6P6b", EXB));
        lst.add(mkSEC("7", "Convert angles."));
        lst.add(mkETT("6P7a", EXA));
        lst.add(mkETT("6P7b", EXB));
        lst.add(mkSEC("8", "Multiply and factor polynomials."));
        lst.add(mkETT("6P8a", EXA));
        lst.add(mkETT("6P8b", EXB));
        lst.add(mkSEC("9", "Simplify radicals."));
        lst.add(mkETT("6P9a", EXA));
        lst.add(mkETT("6P9b", EXB));
        lst.add(mkSEC("10", "Solve systems of equations."));
        lst.add(mkETT("6P10a", EXA));
        lst.add(mkETT("6P10a", EXB));

        // 1: Inverses
        startLesson(prefix + ".11", 1, 1);
        lst.add(mkPreVid("611OV", OVVIDPRE));
        lst.add(mkPrePdf("611OV", OVPDF));
        lst.add(mkTip("12611Tips"));
        lst.add(mkLH2("1: Inverses", O1_1, "Determine values of trigonometric functions for special angles."));
        lst.add(mkSEC("1.1.1", "Determine values of trigonometric functions for special angles."));
        lst.add(mkETT("6111E1", EX1));
        lst.add(mkETT("6111E2", EX2));
        lst.add(mkETT("6111E3", EX3));
        lst.add(mkETT("6111E4", EX4));
        lst.add(mkETT("6111E5", EX5));
        lst.add(mkETT("6111T1", TT1));
        lst.add(mkETT("6111T2", TT2));
        lst.add(mkETT("6111T3", TT3));
        lst.add(mkETT("6111T4", TT4));
        lst.add(mkETT("6111T5", TT5));

        startLesson(prefix + ".12", 1, 2);
        lst.add(mkLH2("1: Inverses", O1_2, "Detemine values of trigonometric functions for angles."));
        lst.add(mkSEC("1.2.1", "Detemine values of trigonometric functions for angles."));
        lst.add(mkETT("6112E1", EX1));
        lst.add(mkETT("6112E2", EX2));
        lst.add(mkETT("6112E3", EX3));
        lst.add(mkETT("6112T1", TT1));
        lst.add(mkETT("6112T2", TT2));
        lst.add(mkETT("6112T3", TT3));

        startLesson(prefix + ".13", 1, 3);
        lst.add(mkPreVid("612OV", OVVID));
        lst.add(mkPrePdf("612OV", OVPDF));
        lst.add(mkTip("12612Tips"));
        lst.add(mkLH2("1: Inverses", O1_3,
                "Determine the domain and range, and graph for <em>y</em> = sin<sup>-1</sup> (<em>x</em>)."));
        lst.add(mkSEC("1.3.1",
                "Determine the domain and range, and graph for <em>y</em> = sin<sup>-1</sup> (<em>x</em>)."));
        lst.add(mkETT("6121E1", EX1));
        lst.add(mkETT("6121T1", TT1));
        lst.add(mkETT("6121T2", TT2));

        startLesson(prefix + ".14", 1, 4);
        lst.add(mkLH2("1: Inverses", O1_4, "Determine the domain and range, and graph for "
                                           + "<em>y</em> = cos<sup>-1</sup> (<em>x</em>) and <em>y</em> = " +
                                           "tan<sup>-1</sup> (<em>x</em>)."));
        lst.add(mkSEC("1.4.1", "Determine the domain and range, and graph for "
                               + "<em>y</em> = cos<sup>-1</sup> (<em>x</em>) and <em>y</em> = tan<sup>-1</sup> " +
                               "(<em>x</em>)."));
        lst.add(mkETT("6122E1", EX1));
        lst.add(mkETT("6122T1", TT1));

        startLesson(prefix + ".15", 1, 5);
        lst.add(mkPreVid("613OV", OVVID));
        lst.add(mkPrePdf("613OV", OVPDF));
        lst.add(mkTip("12613Tips"));
        lst.add(mkLH2("1: Inverses", O1_5, "Determine values for inverse functions."));
        lst.add(mkSEC("1.5.1", "Determine values for inverse functions."));
        lst.add(mkETT("6131E1", EX1));
        lst.add(mkETT("6131E2", EX2));
        lst.add(mkETT("6131E3", EX3));
        lst.add(mkETT("6131E4", EX4));
        lst.add(mkETT("6131E5", EX5));
        lst.add(mkETT("6131E6", EX6));
        lst.add(mkETT("6131T1", TT1));
        lst.add(mkETT("6131T2", TT2));
        lst.add(mkETT("6131T3", TT3));
        lst.add(mkETT("6131T4", TT4));
        lst.add(mkETT("6131T5", TT5));
        lst.add(mkETT("6131T6", TT6));

        startLesson(prefix + ".16", 1, 6);
        lst.add(mkLH2("1: Inverses", O1_6, "Determine solutions to equations."));
        lst.add(mkSEC("1.3.2", "Determine solutions to equations."));
        lst.add(mkETT("6132E1", EX1));
        lst.add(mkETT("6132E2", EX2));
        lst.add(mkETT("6132E3", EX3));
        lst.add(mkETT("6132E4", EX4));
        lst.add(mkETT("6132E5", EX5));
        lst.add(mkETT("6132E6", EX6));
        lst.add(mkETT("6132T1", TT1));
        lst.add(mkETT("6132T2", TT2));
        lst.add(mkETT("6132T3", TT3));
        lst.add(mkETT("6132T4", TT4));
        lst.add(mkETT("6132T5", TT5));
        lst.add(mkETT("6132T6", TT6));

        startLesson(prefix + ".17", 1, 7);
        lst.add(mkPreVid("614OV", OVVID));
        lst.add(mkPrePdf("614OV", OVPDF));
        lst.add(mkTip("12614Tips"));
        lst.add(mkLH2("1: Inverses", O1_7, "Use composition to find exact values."));
        lst.add(mkSEC("1.7.1", "Use composition to find exact values."));
        lst.add(mkETT("6141E1", EX1));
        lst.add(mkETT("6141E2", EX2));
        lst.add(mkETT("6141T1", TT1));
        lst.add(mkETT("6141T2", TT2));

        startLesson(prefix + ".18", 1, 8);
        lst.add(mkLH2("1: Inverses", O1_8, "Use composition to explore inverses."));
        lst.add(mkSEC("1.8.1", "Use composition to explore inverses."));
        lst.add(mkETT("6142E1", EX1));
        lst.add(mkETT("6142E2", EX2));
        lst.add(mkETT("6142E3", EX3));
        lst.add(mkETT("6142E4", EX4));
        lst.add(mkETT("6142T1", TT1));
        lst.add(mkETT("6142T2", TT2));

        startLesson(prefix + ".19", 1, 9);
        lst.add(mkTip("12615Tips"));
        lst.add(mkLH2("1: Inverses", O1_9, "Solve angle of elevation problems."));
        lst.add(mkSEC("1.9.1", "Solve angle of elevation problems."));
        lst.add(mkETT("6151E1", EX1));
        lst.add(mkETT("6151T1", TT1));

        startLesson(prefix + ".110", 1, 10);
        lst.add(mkLH2("1: Inverses", O1_10, "Solve other applications."));
        lst.add(mkSEC("1.10.1", "Solve other applications."));
        lst.add(mkETT("6152E1", EX1));
        lst.add(mkETT("6152T1", TT1));

        // 2: Fundamental Identities
        startLesson(prefix + ".21", 2, 1);
        lst.add(mkPreVid("621OV", OVVIDPRE));
        lst.add(mkPrePdf("621OV", OVPDF));
        lst.add(mkTip("12621Tips"));
        lst.add(mkLH2("2: Fundamental Identities", O2_1, "Verify identities."));
        lst.add(mkSEC("2.1.1", "Verify identities."));
        lst.add(mkETT("6211E1", EX1));
        lst.add(mkETT("6211E2", EX2));
        lst.add(mkETT("6211T1", TT1));
        lst.add(mkETT("6211T2", TT2));

        startLesson(prefix + ".22", 2, 2);
        lst.add(mkLH2("2: Fundamental Identities", O2_2,
                "Determine the domain of vailidity for trigonometric identities."));
        lst.add(mkSEC("2.2.1", "Determine the domain of vailidity for trigonometric identities."));
        lst.add(mkETT("6212E1", EX1));
        lst.add(mkETT("6212E2", EX2));
        lst.add(mkETT("6212T1", TT1));
        lst.add(mkETT("6212T2", TT2));

        startLesson(prefix + ".23", 2, 3);
        lst.add(mkPreVid("622OV", OVVIDPRE));
        lst.add(mkPrePdf("622OV", OVPDF));
        lst.add(mkTip("12622Tips"));
        lst.add(mkLH2("2: Fundamental Identities", O2_3, "Determine cofunction identities."));
        lst.add(mkSEC("2.3.1", "Determine cofunction identities."));
        lst.add(mkETT("6221E1", EX1));
        lst.add(mkETT("6221E2", EX2));
        lst.add(mkETT("6221T1", TT1));
        lst.add(mkETT("6221T2", TT2));

        startLesson(prefix + ".24", 2, 4);
        lst.add(mkLH2("2: Fundamental Identities", O2_4, "Determine odd-even identities."));
        lst.add(mkSEC("2.4.1", "Determine odd-even identities."));
        lst.add(mkETT("6222E1", EX1));
        lst.add(mkETT("6222E2", EX2));
        lst.add(mkETT("6222T1", TT1));
        lst.add(mkETT("6222T2", TT2));

        startLesson(prefix + ".25", 2, 5);
        lst.add(mkPreVid("623OV", OVVIDPRE));
        lst.add(mkPrePdf("623OV", OVPDF));
        lst.add(mkTip("12623Tips"));
        lst.add(mkLH2("2: Fundamental Identities", O2_5, "Develop the Pythagorean identities."));
        lst.add(mkSEC("2.5.1", "Develop the Pythagorean identities."));
        lst.add(mkETT("6231E1", EX1));
        lst.add(mkETT("6231E2", EX2));
        lst.add(mkETT("6231T1", TT1));

        startLesson(prefix + ".26", 2, 6);
        lst.add(mkLH2("2: Fundamental Identities", O2_6, "Apply the Pythagorean identities."));
        lst.add(mkSEC("2.6.1", "Apply the Pythagorean identities."));
        lst.add(mkETT("6232E1", EX1));
        lst.add(mkETT("6232E2", EX2));
        lst.add(mkETT("6232T1", TT1));
        lst.add(mkETT("6232T2", TT2));

        startLesson(prefix + ".27", 2, 7);
        lst.add(mkPreVid("624OV", OVVIDPRE));
        lst.add(mkPrePdf("624OV", OVPDF));
        lst.add(mkTip("12624Tips"));
        lst.add(mkLH2("2: Fundamental Identities", O2_7, "Prove elementary identities."));
        lst.add(mkSEC("2.7.1", "Prove elementary identities."));
        lst.add(mkETT("6241E1", EX1));
        lst.add(mkETT("6241E2", EX2));
        lst.add(mkETT("6241T1", TT1));

        startLesson(prefix + ".28", 2, 8);
        lst.add(mkLH2("2: Fundamental Identities", O2_8, "Prove identities."));
        lst.add(mkSEC("2.8.1", "Prove identities."));
        lst.add(mkETT("6242E1", EX1));
        lst.add(mkETT("6242E2", EX2));
        lst.add(mkETT("6242E3", EX3));
        lst.add(mkETT("6242T1", TT1));
        lst.add(mkETT("6242T2", TT2));
        lst.add(mkETT("6242T3", TT3));

        startLesson(prefix + ".29", 2, 9);
        lst.add(mkPreVid("625OV", OVVIDPRE));
        lst.add(mkPrePdf("625OV", OVPDF));
        lst.add(mkTip("12625Tips"));
        lst.add(mkLH2("2: Fundamental Identities", O2_9, "Solve trigonometric equations."));
        lst.add(mkSEC("2.9.1", "Solve trigonometric equations."));
        lst.add(mkETT("6251E1", EX1));
        lst.add(mkETT("6251E2", EX2));
        lst.add(mkETT("6251T1", TT1));
        lst.add(mkETT("6251T2", TT2));

        startLesson(prefix + ".210", 2, 10);
        lst.add(mkLH2("2: Fundamental Identities", O2_10, "Use Pythagorean identities to solve equations."));
        lst.add(mkSEC("2.10.1", "Use Pythagorean identities to solve equations."));
        lst.add(mkETT("6252E1", EX1));
        lst.add(mkETT("6252E2", EX2));
        lst.add(mkETT("6252T1", TT1));
        lst.add(mkETT("6252T2", TT2));

        // 3: Sum and Difference Identities
        startLesson(prefix + ".31", 3, 1);
        lst.add(mkPreVid("631OV", OVVIDPRE));
        lst.add(mkPrePdf("631OV", OVPDF));
        lst.add(mkTip("12631Tips"));
        lst.add(mkLH2("3: Sum and Difference Identities", O3_1, "Derive identities from the cosine sum identity."));
        lst.add(mkSEC("3.1.1", "Derive identities from the cosine sum identity."));
        lst.add(mkETT("6311E1", EX1));
        lst.add(mkETT("6311T1", TT1));

        startLesson(prefix + ".32", 3, 2);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_2, "Find exact values with the cosine sum identity."));
        lst.add(mkSEC("3.2.1", "Find exact values with the cosine sum identity."));
        lst.add(mkETT("6312E1", EX1));
        lst.add(mkETT("6312E2", EX2));
        lst.add(mkETT("6312T1", TT1));
        lst.add(mkETT("6312T2", TT2));

        startLesson(prefix + ".33", 3, 3);
        lst.add(mkPreVid("632OV", OVVIDPRE));
        lst.add(mkPrePdf("632OV", OVPDF));
        lst.add(mkTip("12632Tips"));
        lst.add(mkLH2("3: Sum and Difference Identities", O3_3,
                "Derive identities from the cosine of a difference identity."));
        lst.add(mkSEC("3.3.1", "Derive identities from the cosine of a difference identity."));
        lst.add(mkETT("6321E1", EX1));
        lst.add(mkETT("6321T1", TT1));

        startLesson(prefix + ".34", 3, 4);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_4,
                "Find exact values with the cosine of a difference identity."));
        lst.add(mkSEC("3.4.1", "Find exact values with the cosine of a difference identity."));
        lst.add(mkETT("6322E1", EX1));
        lst.add(mkETT("6322E2", EX2));
        lst.add(mkETT("6322T1", TT1));
        lst.add(mkETT("6322T2", TT2));

        startLesson(prefix + ".35", 3, 5);
        lst.add(mkPreVid("633OV", OVVIDPRE));
        lst.add(mkPrePdf("633OV", OVPDF));
        lst.add(mkTip("12633Tips"));
        lst.add(mkLH2("3: Sum and Difference Identities", O3_5, "Derive identities from the sine sum identity."));
        lst.add(mkSEC("3.5.1", "Derive identities from the sine sum identity."));
        lst.add(mkETT("6331E1", EX1));
        lst.add(mkETT("6331T1", TT1));

        startLesson(prefix + ".36", 3, 6);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_6, "Find exact values with the sine sum identity."));
        lst.add(mkSEC("3.6.1", "Find exact values with the sine sum identity."));
        lst.add(mkETT("6332E1", EX1));
        lst.add(mkETT("6332E2", EX2));
        lst.add(mkETT("6332T1", TT1));
        lst.add(mkETT("6332T2", TT2));

        startLesson(prefix + ".37", 3, 7);
        lst.add(mkPreVid("634OV", OVVID));
        lst.add(mkPrePdf("634OV", OVPDF));
        lst.add(mkTip("12634Tips"));
        lst.add(mkLH2("3: Sum and Difference Identities", O3_7,
                "Derive identities with the sine of a difference identity."));
        lst.add(mkSEC("3.7.1", "Derive identities with the sine of a difference identity."));
        lst.add(mkETT("6341E1", EX1));
        lst.add(mkETT("6341T1", TT1));

        startLesson(prefix + ".38", 3, 8);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_8,
                "Find exact values with the sine of a difference identity."));
        lst.add(mkSEC("3.8.1", "Find exact values with the sine of a difference identity."));
        lst.add(mkETT("6342E1", EX1));
        lst.add(mkETT("6342E2", EX2));
        lst.add(mkETT("6342T1", TT1));
        lst.add(mkETT("6342T2", TT2));

        startLesson(prefix + ".39", 3, 9);
        lst.add(mkPreVid("635OV", OVVID));
        lst.add(mkPrePdf("635OV", OVPDF));
        lst.add(mkTip("12635Tips"));
        lst.add(mkLH2("3: Sum and Difference Identities", O3_9,
                "Derive identities from the tangent sum and difference identities."));
        lst.add(mkSEC("3.9.1", "Derive identities from the tangent sum and difference identities."));
        lst.add(mkETT("6351E1", EX1));
        lst.add(mkETT("6351T1", TT1));

        startLesson(prefix + ".310", 3, 10);
        lst.add(mkLH2("3: Sum and Difference Identities", O3_10,
                "Find exact values with the tangent sum and difference identities."));
        lst.add(mkSEC("3.10.1", "Find exact values with the tangent sum and difference identities."));
        lst.add(mkETT("6352E1", EX1));
        lst.add(mkETT("6352T1", TT1));

        // 4: Identities and Equations
        startLesson(prefix + ".41", 4, 1);
        lst.add(mkPreVid("641OV", OVVIDPRE));
        lst.add(mkPrePdf("641OV", OVPDF));
        lst.add(mkTip("12641Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_1,
                "Derive identities using the double angle identity for sine."));
        lst.add(mkSEC("4.1.1", "Derive identities using the double angle identity for sine."));
        lst.add(mkETT("6411E1", EX1));
        lst.add(mkETT("6411T1", TT1));

        startLesson(prefix + ".42", 4, 2);
        lst.add(mkLH2("4: Identities and Equations", O4_2, "Solve problems with the double angle identity for sine."));
        lst.add(mkSEC("4.2.1", "Solve problems with the double angle identity for sine."));
        lst.add(mkETT("6412E1", EX1));
        lst.add(mkETT("6412E2", EX2));
        lst.add(mkETT("6412T1", TT1));
        lst.add(mkETT("6412T2", TT2));

        startLesson(prefix + ".43", 4, 3);
        lst.add(mkPreVid("642OV", OVVIDPRE));
        lst.add(mkPrePdf("642OV", OVPDF));
        lst.add(mkTip("12642Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_3, "Derive the double angle identity for cosine and tangent."));
        lst.add(mkSEC("4.3.1", "Derive the double angle identity for cosine and tangent."));
        lst.add(mkETT("6421E1", EX1));
        lst.add(mkETT("6421E2", EX2));
        lst.add(mkETT("6421T1", TT1));
        lst.add(mkETT("6421T2", TT2));

        startLesson(prefix + ".44", 4, 4);
        lst.add(mkLH2("4: Identities and Equations", O4_4,
                "Solve problems with the double angle identity for cosine and tangent."));
        lst.add(mkSEC("4.4.1", "Solve problems with the double angle identity for cosine and tangent."));
        lst.add(mkETT("6422E1", EX1));
        lst.add(mkETT("6422E2", EX2));
        lst.add(mkETT("6422T1", TT1));
        lst.add(mkETT("6422T2", TT2));

        startLesson(prefix + ".45", 4, 5);
        lst.add(mkPreVid("643OV", OVVID));
        lst.add(mkPrePdf("643OV", OVPDF));
        lst.add(mkTip("12643Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_5, "Derive the half angle formulas."));
        lst.add(mkSEC("4.5.1", "Derive the half angle formulas."));
        lst.add(mkETT("6431E1", EX1));
        lst.add(mkETT("6431T1", TT1));

        startLesson(prefix + ".46", 4, 6);
        lst.add(mkLH2("4: Identities and Equations", O4_6, "Solve problems using the half angle identities."));
        lst.add(mkSEC("4.6.1", "Solve problems using the half angle identities."));
        lst.add(mkETT("6432E1", EX1));
        lst.add(mkETT("6432E2", EX2));
        lst.add(mkETT("6432T1", TT1));
        lst.add(mkETT("6432T2", TT2));

        startLesson(prefix + ".47", 4, 7);
        lst.add(mkPreVid("644OV", OVVID));
        lst.add(mkPrePdf("644OV", OVPDF));
        lst.add(mkTip("12644Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_7, "Solve equations using the double angles."));
        lst.add(mkSEC("4.7.1", "Solve equations using the double angles."));
        lst.add(mkETT("6441E1", EX1));
        lst.add(mkETT("6441E2", EX2));
        lst.add(mkETT("6441T1", TT1));
        lst.add(mkETT("6441T2", TT2));

        startLesson(prefix + ".48", 4, 8);
        lst.add(mkLH2("4: Identities and Equations", O4_8, "Solve equations using half angles."));
        lst.add(mkSEC("4.4.2", "Solve equations using half angles."));
        lst.add(mkETT("6442E1", EX1));
        lst.add(mkETT("6442E2", EX2));
        lst.add(mkETT("6442T1", TT1));
        lst.add(mkETT("6442T2", TT2));

        startLesson(prefix + ".49", 4, 9);
        lst.add(mkPreVid("645OV", OVVID));
        lst.add(mkPrePdf("645OV", OVPDF));
        lst.add(mkTip("12645Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_9, "Solve equations I."));
        lst.add(mkSEC("4.9.1", "Solve equations I."));
        lst.add(mkETT("6451E1", EX1));
        lst.add(mkETT("6451E2", EX2));
        lst.add(mkETT("6451T1", TT1));
        lst.add(mkETT("6451T2", TT2));

        startLesson(prefix + ".410", 4, 10);
        lst.add(mkPreVid("645OV", OVVID));
        lst.add(mkPrePdf("645OV", OVPDF));
        lst.add(mkTip("12645Tips"));
        lst.add(mkLH2("4: Identities and Equations", O4_10, "Solve equations II."));
        lst.add(mkSEC("4.10.1", "Solve equations II."));
        lst.add(mkETT("6452E1", EX1));
        lst.add(mkETT("6452T1", TT1));
    }

    /**
     * Sets the fields to start a new lesson.
     *
     * @param theLessonId  the lesson ID
     * @param theUnit      the unit
     * @param theObjective the objective
     */
    private static void startLesson(final String theLessonId, final int theUnit, final int theObjective) {

        lessonId = theLessonId;
        unit = theUnit;
        objective = theObjective;
        seqNumber = 1;
    }

    /**
     * Builds a component representing a 1-line lesson heading.
     *
     * @param title the heading title
     * @return the new component
     */
    private static RawLessonComponent mkLH1(final String title) {

        final String xml = SimpleBuilder.concat("<h2>", title, "</h2>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "LH", xml);

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a 2-line lesson heading.
     *
     * @param title     the heading title
     * @param subtitle1 the subtitle first part (bold)
     * @param subtitle2 the subtitle second part (plain)
     * @return the new component
     */
    private static RawLessonComponent mkLH2(final String title, final String subtitle1, final String subtitle2) {

        final HtmlBuilder xml = new HtmlBuilder(100);

        xml.addln("<div class='indent11 inset'>");
        if (title != null) {
            xml.addln("<h2>", title, "</h2>");
        }

        xml.add("<h3>");
        if (subtitle1 != null) {
            xml.add("<strong>", subtitle1, "</strong>");
            if (subtitle2 != null) {
                xml.add(" : ");
            }
        }
        if (subtitle2 != null) {
            xml.add(subtitle2);
        }
        xml.addln("</h3>");
        xml.addln("</div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "LH",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a section heading.
     *
     * @param number the section number
     * @param title  the section title
     * @return the new component
     */
    private static RawLessonComponent mkSEC(final String number, final String title) {

        final HtmlBuilder xml = new HtmlBuilder(100);

        xml.addln("<div class='gap2'>&nbsp;</div>");
        xml.addln("<div class='indent1'><strong>", number, "</strong> : ", title, "</div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "SH",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a section heading.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @return the new component
     */
    private static RawLessonComponent mkETT(final String mediaId, final String title) {

        return mkETT(mediaId, title, true, true);
    }

    /**
     * Builds a component representing an example or exercise video.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @param doVideo {@code true} to include the video link; {@code false} to omit
     * @param doPdf   {@code true} to include the PDF link; {@code false} to omit
     * @return the new component
     */
    private static RawLessonComponent mkETT(final String mediaId, final String title,
                                            final boolean doVideo, final boolean doPdf) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln("<div class='indent1'>");
        xml.addln("<div class='ex'>");
        if (doVideo) {
            xml.addln(" <img src='/images/video.png' alt=''/>");
            xml.addln(" <a class='linkbtn' href='video.html?course=", courseId, "&media-id=", mediaId, "&unit=",
                    Integer.toString(unit), "&lesson=", Integer.toString(objective), "&mode=%%MODE%%'>", title,
                    " Video</a>");
        }
        xml.addln("</div>");

        xml.addln("<div class='sol'>");
        if (doPdf) {
            xml.addln(" <img src='/images/pdf.png' alt='' "
                      + "style='padding-right:3px;'/>");
            xml.addln(" <a class='linkbtn' target='_blank' href='https://nibbler.math.colostate.edu/media/",
                    courseId.replace(CoreConstants.SPC, CoreConstants.EMPTY), "/pdf/", mediaId, ".pdf'>", title,
                    " Solution</a>");
        }
        xml.addln("</div>");

        xml.addln("<div class='clear'></div>");
        xml.addln("</div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "EX",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a pre-lesson media link which will appear in the course outline.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @return the new component
     */
    private static RawLessonComponent mkPreVid(final String mediaId, final String title) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln(" <img src='/images/video.png' alt=''/><a class='linkbtn' href='video.html?course=", courseId,
                "&media-id=", mediaId, "&unit=", Integer.toString(unit), "&lesson=", Integer.toString(objective),
                "&mode=%%MODE%%'>", title, "</a>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "PREMED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a media link.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @return the new component
     */
    private static RawLessonComponent mkVid(final String mediaId, final String title) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln("<div class='vid'>");
        xml.addln(" <img src='/images/video.png' alt=''/><a class='linkbtn' href='video.html?course=", courseId,
                "&media-id=", mediaId, "&unit=", Integer.toString(unit), "&lesson=", Integer.toString(objective),
                "&mode=%%MODE%%'> ", title, "</a>");
        xml.addln("</div>");
        xml.addln("<div class='clear'></div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "MED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a media link.
     *
     * @param mediaId the ID of the media
     * @return the new component
     */
    private static RawLessonComponent mkTip(final String mediaId) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln("<div class='vid'>");
        xml.addln(" <img src='/images/video.png' alt=''/><a class='linkbtn' href='video.html?course=", courseId,
                "&media-id=", mediaId, "&unit=", Integer.toString(unit), "&lesson=", Long.valueOf(objective),
                "&mode=%%MODE%%'>Helpful Tips</a>");
        xml.addln("</div>");
        xml.addln("<div class='clear'></div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "MED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a media link.
     *
     * @param mediaId the ID of the media
     * @return the new component
     */
    private static RawLessonComponent mkTip1(final String mediaId) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln("<div class='vid'>");
        xml.addln(" <img src='/images/video.png' alt=''/><a class='linkbtn' href='video.html?course=", courseId,
                "&media-id=", mediaId, "&unit=", Integer.toString(unit), "&lesson=", Integer.toString(objective),
                "&mode=%%MODE%%'>Helpful Tips, Part 1</a>");
        xml.addln("</div>");
        xml.addln("<div class='clear'></div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "MED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a media link.
     *
     * @param mediaId the ID of the media
     * @return the new component
     */
    private static RawLessonComponent mkTip2(final String mediaId) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln("<div class='vid'>");
        xml.addln(" <img src='/images/video.png' alt=''/><a class='linkbtn' href='video.html?course=", courseId,
                "&media-id=", mediaId, "&unit=", Integer.toString(unit), "&lesson=", Integer.toString(objective),
                "&mode=%%MODE%%'>Helpful Tips, Part 2</a>");
        xml.addln("</div>");
        xml.addln("<div class='clear'></div>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "MED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a pre-lesson media link which will appear in the course outline.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @return the new component
     */
    private static RawLessonComponent mkPrePdf(final String mediaId, final String title) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.addln(" <img src='/images/pdf.png' alt='' style='padding-right:3px;'/>");
        xml.addln(" <a class='linkbtn' target='_blank' href='https://nibbler.math.colostate.edu/media/",
                courseId.replace(CoreConstants.SPC, CoreConstants.EMPTY), "/pdf/", mediaId, ".pdf'>", title, "</a>");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "PREMED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

    /**
     * Builds a component representing a media link.
     *
     * @param mediaId the ID of the media
     * @param title   the section title
     * @return the new component
     */
    private static RawLessonComponent mkPdf(final String mediaId, final String title) {

        final HtmlBuilder xml = new HtmlBuilder(200);

        xml.sDiv("ex");
        xml.addln(" <img src='/images/pdf.png' alt='' style='padding-right:3px;'/>");
        xml.addln(" <a class='linkbtn' target='_blank' href='https://nibbler.math.colostate.edu/media/",
                courseId.replace(CoreConstants.SPC, CoreConstants.EMPTY), "/pdf/", mediaId, ".pdf'>", title, "</a>");
        xml.eDiv();
        xml.div("clear");

        final RawLessonComponent comp = new RawLessonComponent(lessonId, Integer.valueOf(seqNumber), "MED",
                xml.toString());

        ++seqNumber;

        return comp;
    }

//    /**
//     * Builds a component representing a link to a PDF file.
//     *
//     * @param url the URL to the file (includes the .pdf extension)
//     * @param title the label for the file link
//     * @return the new component
//     */
//     private static LessonComponent mkPdfLink(final String url, final String title) {
//
//     final HtmlBuilder xml = new HtmlBuilder(200);
//
//     xml.addln("<div class='ex'>");
//     xml.addln(" <img src='/images/pdf.png' alt='' style='padding-right:3px;'/>");
//     xml.addln(" <a class='linkbtn' href='", url, "'>",
//     title, "</a>");
//     xml.addln("</div>");
//     xml.addln("<div class='clear'></div>");
//
//     final LessonComponent comp = new LessonComponent(lessonId, Integer.valueOf(seqNumber), //
//     "MED", xml.toString());
//
//     ++seqNumber;
//
//     return comp;
//     }
}
