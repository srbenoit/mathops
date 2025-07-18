package dev.mathops.web.host.precalc.course.data;

import dev.mathops.db.old.rawrecord.RawRecordConstants;

/**
 * Static {@code CourseData} objects for Math 125.
 */
public enum Math125 {
    ;

    /** The MATH 125 course. */
    public static final CourseData MATH_125;

    static {
        MATH_125 = buildMath125();
    }

    /**
     * Creates the MATH 125 course.
     *
     * @return the course data container
     */
    private static CourseData buildMath125() {

        final CourseData m125 = new CourseData(RawRecordConstants.MATH125, RawRecordConstants.MATH125,
                "Numerical Trigonometry", "M125");

        // Module 1

        final ModuleData m125m1 = m125.addModule(1, "Angle Measure and Right Triangles", "TR01_SR_HW", "c41-thumb.png");
        m125m1.skillsReview.addExBlock("Unit Conversions")
                .addEx("TR01_SR1_01", "Multi-step Unit Conversion");
        m125m1.skillsReview.addExBlock("Addition and Subtraction with Fractions")
                .addEx("TR01_SR2_01", "Addition of fractions")
                .addEx("TR01_SR2_02", "Addition of fractions including variables")
                .addEx("TR01_SR2_03", "Subtraction of fractions including variables");
        m125m1.skillsReview.addExBlock("Multiplication and Division with Fractions")
                .addEx("TR01_SR3_01", "Multiplication of fractions")
                .addEx("TR01_SR3_02", "Division of fractions");
        m125m1.skillsReview.addExBlock("Evaluating and Manipulating Square Roots")
                .addEx("TR01_SR4_01", "Simplifying a rational expression")
                .addEx("TR01_SR4_02", "Evaluating a rational expression with variable");
        m125m1.skillsReview.addExBlock("Exponents and Distribution")
                .addEx("TR01_SR5_01", "Properties of arithmetic with expressions")
                .addEx("TR01_SR5_02", "Cubing a binomial");

        final LearningTargetData m125m1t1 = m125m1.addLearningTarget(1, 1, "1.1",
                "TR01_ST1_HW",
                "I can classify and work with angles.",
                "interpret and describe degree or radian measure as a quantity of rotation,",
                // "recognize and identify acute, right, obtuse, straight, and reflex angles,",
                "convert between degree and radian units of measure,",
                "recognize congruent angles,",
                "recognize pairs of complementary and supplementary angles,",
                "compute the complement or supplement of an angle in either unit of measure, and",
                "recognize sets of angles that sum to a straight angle, and that their measures sum to half a turn.");

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
                "I can classify and work with triangles.",
                "calculate the measure of any angle in a triangle given the measure of the other two angles,",
                "recognize congruent and similar triangles,",
                "use proportion to calculate side lengths in similar triangles,",
                // "recognize and identify equilateral, isosceles, and right triangles,",
                "divide a general triangle into two right triangles with an altitude, or divide a rectangle into "
                        + "two right triangles with a diagonal,",
                "use the Pythagorean theorem to calculate the length of any edge of a right triangle,",
                "calculate the length of the diagonal of a rectangle.");

        m125m1t2.addExBlock("Calculate a missing angle in a triangle in degrees")
                .addEx("TR01_ST2A_01", "Given two interior degree angles in a triangle, find the third");
        m125m1t2.addExBlock("Calculate a missing angle in a triangle in radians")
                .addEx("TR01_ST2A_02", "Given two interior radian angles in a triangle, find the third");
        m125m1t2.addExBlock("Recognize similar triangles")
                .addEx("TR01_ST2B_01", "Given a drawing containing several triangles, find all that are similar");
        m125m1t2.addExBlock("Use proportion in similar triangles to calculate side lengths")
                .addEx("TR01_ST2C_01",
                        "Given two similar triangles, and a few side lengths, find remaining side lengths");
        m125m1t2.addExBlock("Divide shapes into right triangles")
                .addEx("TR01_ST2E_01", "Divide shapes into right triangles by adding lines.");
        m125m1t2.addExBlock("Calculate side lengths in right triangles using the Pythagorean theorem")
                .addEx("TR01_ST2F_01a", "Find a missing side length in a right triangle");
        m125m1t2.addExBlock("Find the length of the diagonal of a rectangle")
                .addEx("TR01_ST2G_01",
                        "Given a rectangle whose side lengths are given, find the length of its diagonal");

        final LearningTargetData m125m1t3 = m125m1.addLearningTarget(1, 3, "1.3", "TR01_ST3_HW",
                "I can work with angles in standard position in the plane.",
                "locate the terminal ray of an angle with positive or negative measure in standard position,",
                "identify the quadrant in which an angle's terminal ray lies,",
                "identify equivalent/coterminal angles in either degrees or radians,",
                "find an angle equivalent/coterminal to a given angle that lies in a specified degree or radian range,",
                "find the reference angle for a given angle in either degrees or radians,",
                "estimate the angle where two lines intersect, or the signed measure of a given angle in the "
                        + "Cartesian plane in either degrees or radians, and",
                "sketch an approximation of a given angle knowing its measure in either unit.");

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
        m125m1t3.addExBlock("Estimating and sketching angles")
                .addEx("TR01_ST3F_01", "Estimate the measure of an angle in degrees and radians");

        // Module 2

        final ModuleData m125m2 = m125.addModule(2, "The Unit Circle", "TR02_SR_HW", "c42-thumb.png");

        m125m2.skillsReview.addExBlock("Percentages")
                .addEx("TR02_SR1_01a", "Pay cut and pay raise")
                .addEx("TR02_SR1_01c", "Auto depreciation")
                .addEx("TR02_SR1_01d", "Home value growth")
                .addEx("TR02_SR1_01e", "Radioactive decay")
                .addEx("TR02_SR1_01f", "Enrollment growth");
        m125m2.skillsReview.addExBlock("Circle area and perimeter")
                .addEx("TR02_SR2_01", "Calculating circumference, radius, and area");
        m125m2.skillsReview.addExBlock("Square roots")
                .addEx("TR02_SR3_01", "Solve with squares and square roots, when to include +/-");
        m125m2.skillsReview.addExBlock("Angles that sum to 180&deg;")
                .addEx("TR02_SR4_01a", "Find angles, in degrees, in a drawing with intersecting lines")
                .addEx("TR02_SR4_01b", "Find angles, in radians, in a drawing with intersecting lines");

        final LearningTargetData m125m2t1 = m125m2.addLearningTarget(2, 1, "2.1", "TR02_ST1_HW",
                "I can interpret angles in terms of arc length.",
                "interpret radian measure as arc length along the unit circle,",
                "calculate the length of the arc that a given angle subtends on a circle of any radius,",
                "calculate the angle subtended by an arc of any length along a circle of any radius, and",
                "calculate the radius of an arc of a specified length that subtends a specified angle.");

        m125m2t1.addExBlock("Interpret radian measure in terms of arc length along the perimeter of a unit circle")
                .addEx("TR02_ST1A_01", "Relate angle to a fraction of circle's perimeter, then to arc length");
        m125m2t1.addExBlock("Calculate length of arc that subtends a given angle on a circle of any radius")
                .addEx("TR02_ST1B_01", "Find arc length given degree measure and radius")
                .addEx("TR02_ST1B_02", "Find arc length given radian measure and radius");
        m125m2t1.addExBlock("Applications of finding arc length given angle and radius")
                .addEx("TR02_ST1B_APP_01B", "Find distance a bike travels as its tire rotates by a specified angle");
        m125m2t1.addExBlock("Use radius and arc length subtended by an angle to calculate the angle")
                .addEx("TR02_ST1C_01", "Given a radius and arc length, calculate the angle subtended");
        m125m2t1.addExBlock("Applications of finding angle subtended by an arc length")
                .addEx("TR02_ST1C_APP_01",
                        "Find number of turns on a spool needed to wind a specified length of thread");
        m125m2t1.addExBlock("Use arc length and angle subtended to calculate the radius")
                .addEx("TR02_ST1D_01", "Given arc length and angle subtended, calculate the radius");
        m125m2t1.addExBlock("Applications of finding radius given arc length and angle")
                .addEx("TR02_ST1D_APP_01", "Find radius of walkway edge that will use a specified length of edging")
                .addEx("TR02_ST1D_APP_02", "Laying out a shape in cloth to create a garment with desired sizing");

        final LearningTargetData m125m2t2 = m125m2.addLearningTarget(2, 2, "2.2", "TR02_ST2_HW",
                "I can locate points on the unit circle.",
                "construct a right triangle whose side lengths are the x- and y-coordinates of a point on the unit "
                        + "circle,",
                "associate an angle with every point on the unit circle, and identify angles whose associated points "
                        + "have the same x- or y-coordinate,",
                "calculate the x- or y-coordinate of a point on a circle given the other coordinate and the point's "
                        + "quadrant, and",
                "interpret how x- or y-coordinates would change as angle increases through each quadrant.");
        m125m2t2.addExBlock("Constructing right triangles corresponding to points on a circle")
                .addEx("TR02_ST2A_01", "Find point where terminal ray intersects unit circle in any quadrant");
        m125m2t2.addExBlock("Associate an angle with every point on the unit circle, identify angles whose points "
                        + "share x or y coordinates")
                .addEx("TR02_ST2B_01",
                        "Explore relationship between coordinates of points associated with rays having same "
                                + "reference angle ");
        m125m2t2.addExBlock("Generalize relationship between points sharing the same x coordinate")
                .addEx("TR02_ST2B_02",
                        "Find pairs of angles whose terminal rays intersect the unit circle with same x coordinate");
        m125m2t2.addExBlock("Generalize relationship between points sharing the same y coordinate")
                .addEx("TR02_ST2B_03",
                        "Find pairs of angles whose terminal rays intersect the unit circle with same y coordinate");
        m125m2t2.addExBlock("Given one coordinate of a point on an arbitrary circle, calculate the other")
                .addEx("TR02_ST2C_01", "Given radius and one coordinate on a circle, find the missing coordinate");

        final LearningTargetData m125m2t3 = m125m2.addLearningTarget(2, 3, "2.3", "TR02_ST3_HW",
                "I can work with sector area.",
                "describe the relationship between radian measure and sector area,",
                "calculate the area of a sector of a circle of any radius subtended by a given angle,",
                "calculate the measure of the angle that subtends a sector of a circle of any radius with specified "
                        + "area, and",
                "calculate the radius of a sector subtended by a specified angle having specified area.");
        m125m2t3.addExBlock("Interpret the relationship between radian measure and sector area")
                .addEx("TR02_ST3A_01",
                        "Relate angle measure to fraction of circle's area contained, then to sector area");
        m125m2t3.addExBlock("Calculate area of a sector subtended by an angle on a circle of radius R")
                .addEx("TR02_ST3B_01", "Given angle, in degrees, and radius, calculate sector area")
                .addEx("TR02_ST3B_02", "Given angle, in radians, and radius, calculate sector area");
        m125m2t3.addExBlock("Calculate angle subtended by sector with specified area")
                .addEx("TR02_ST3C_01", "Find angle that generates a specified sector area with a given radius");
        m125m2t3.addExBlock("Applications of calculating angle from radius and area")
                .addEx("TR02_ST3C_APP_01",
                        "Determine angle to cut to make small pizza slices same area as larger ones");
        m125m2t3.addExBlock("Calculate radius of sector with specified area subtended by specified angle")
                .addEx("TR02_ST3D_01", "Given an angle and the sector area it contains, find the sector radius");
        m125m2t3.addExBlock("Applications of calculating radius from angle and area")
                .addEx("TR02_ST3D_01", "Given one pizza cut into 8 slices, with a certain area per slice, what size "
                        + "pizza would generate pieces of the same area when cut into 6 pieces?");

        // Module 3

        final ModuleData m125m3 = m125.addModule(3, "The Trigonometric Functions", "TR03_SR_HW", "c43-thumb.png");

        m125m3.skillsReview.addExBlock("Estimating the measure of angles")
                .addEx("TR03_SR1_01", "Match angle to its measure in degrees or radians");
        m125m3.skillsReview.addExBlock("Negative and co-terminal angles")
                .addEx("TR03_SR2_01", "Find positive angle that's co-terminal to a negative angle");
        m125m3.skillsReview.addExBlock("Pythagorean theorem")
                .addEx("TR03_SR3_01", "Find triangle side length using Pythagorean theorem");
        m125m3.skillsReview.addExBlock("Similar triangles")
                .addEx("TR03_SR4_01", "Use proportionality to solve for side length in similar triangle");
        m125m3.skillsReview.addExBlock("Supplementary angles and reference angles")
                .addEx("TR03_SR5_01", "Find angles in all quadrants with reference angle of a given angle.");
        m125m3.skillsReview.addExBlock("The Pythagorean relationship between sine and cosine")
                .addEx("TR03_SR6_01", "Classify statements as true or false");

        final LearningTargetData m125m3t1 = m125m3.addLearningTarget(3, 1, "3.1", "TR03_ST1_HW",
                "I can define and interpret the six trigonometric functions. Specifically, I can:",
                "define each of the six trigonometric functions in terms of ratios of coordinates of points on the "
                        + "unit circle,",
                "express tangent, cotangent, secant, and cosecant in terms of sine and cosine,",
                "interpret each of the six trigonometric functions in terms of these ratios or geometrically, and",
                // "[OPTIONAL] calculate tangent of an angle by calculating slope of a terminal ray, or vice-versa,",
                " interpret cosine and sine as (x, y) components of point undergoing circular motion.");
                // "[OPTIONAL] explore elliptical motion as circular motion scaled along one axis\n");
        m125m3t1.addExBlock("Algebraic and geometric definition of cosine")
                .addEx("TR03_ST1A_01", "Describe what the cosine of an angle represents");
        m125m3t1.addExBlock("Algebraic and geometric definition of sine")
                .addEx("TR03_ST1A_02", "Describe what the sine of an angle represents");
        m125m3t1.addExBlock("Algebraic definitions of the trigonometric functions")
                .addEx("TR03_ST1B_01", "Match trigonometric functions to their algebraic definitions");
        m125m3t1.addExBlock("Geometric meaning of the trig functions")
                .addEx("TR03_ST1C_01", "Relate quantities drawn in a unit circle to the trigonometric functions");
        m125m3t1.addExBlock("Relationship between tangent and slope")
                .addEx("TR03_ST1D_01",
                        "Given point where terminal ray intersects a unit circle, find tangent of the angle and slope "
                                + "of the terminal ray");
        // m125m3t1.addExBlock("Exploration: Cosine and sine as the (x, y) components of circular motion")
        //        .addEx("TR03_ST1E_EXP_01", "Write the equation for a circle with given radius and center")
        //        .addEx("TR03_ST1E_EXP_02", "Model uniform rotation with angle as a function of time")
        //        .addEx("TR03_ST1E_EXP_03", "Write parametric equations for x and y modeling circular motion on a "
        //            + "unit circle")
        //        .addEx("TR03_ST1E_EXP_04", "Write parametric equations for x and y modeling circular motion "
        //            + "around a circle of any radius")
        //        .addEx("TR03_ST1E_EXP_05", "Explore the connection between the parametric equations of circular "
        //            + "motion and the equation of a circle")
        //        .addEx("TR03_ST1E_EXP_06", "Write parametric equations for x and y modeling circular motion "
        //            + "around a circle of any radius and any location in the plane");

        final LearningTargetData m125m3t2 = m125m3.addLearningTarget(3, 2, "3.2", "TR03_ST2_HW",
                "I can graph and interpret graphs of the six trigonometric functions. Specifically, I can:",
                "graph each of the trigonometric functions,",
                "associate quadrants in the plane with intervals in the domain of trigonometric functions,",
                "state the domain and range of each trigonometric function, including points where the function is"
                        + " undefined,",
                "state the period of each trigonometric function,",
                "identify regions (intervals in the domain or quadrants) where each trigonometric "
                        + "function is positive or negative,",
                "identify regions (intervals in the domain or quadrants) where each trigonometric "
                        + "function is increasing or decreasing as angle increases,",
                "classify each trigonometric function as \"even\" or \"odd\", and",
                "state the algebraic relationship that the \"even/odd\" characteristic of each trigonometric "
                        + "function implies.");
        m125m3t2.addExBlock("Identifying graphs of trig functions")
                .addEx("TR03_ST2A_01", "Match graphs to the corresponding trigonometric function ");
        m125m3t2.addExBlock("Domain of each trigonometric function")
                .addEx("TR03_ST2B_01", "Specify the domain where each trigonometric function is defined");
        m125m3t2.addExBlock("Range of each trigonometric function")
                .addEx("TR03_ST2B_02", "Specify the range of each trigonometric function");
        m125m3t2.addExBlock("Period of a function")
                .addEx("TR03_ST2D_01", "Determine the period of each trigonometric function, state the algebraic "
                        + "relationship that period implies");
        m125m3t2.addExBlock("Regions where trigonometric functions are positive or negative, increasing or decreasing")
                .addEx("TR03_ST2F_01",
                        "Identify quadrants in which each trigonometric function is positive or negative and "
                                + "increasing or decreasing")
                .addEx("TR03_ST2F_02",
                        "Identify quadrants in which each trigonometric function is positive")
                .addEx("TR03_ST2F_03",
                        "Identify quadrants in which each trigonometric function is increasing");
        m125m3t2.addExBlock("Even/odd behavior")
                .addEx("TR03_ST2G_01",
                        "Describe what is meant by 'even' and 'odd' functions, and classify trigonometric functions "
                                + "as even or odd");
        m125m3t2.addExBlock("Even/odd relationships and behaviors")
                .addEx("TR03_ST2H_01",
                        "Indicate algebraic relationships that are true based on even/odd nature of functions");

        final LearningTargetData m125m3t3 = m125m3.addLearningTarget(3, 3, "3.3", "TR03_ST3_HW",
                "I can evaluate trigonometric functions in several contexts. Specifically, I can:",
                "recall values of sine and cosine for common angles, and calculating the other four function values,",
                "evaluate any trigonometric function for any angle, in either degrees or radians, using technology,",
                "apply the Pythagorean theorem to calculate sine from cosine or vice versa, and",
                "calculate possible values of various trigonometric functions for a given value of sine or cosine.");
        m125m3t3.addExBlock("Values of sine and cosine for common angles")
                .addEx("TR03_ST3A_01", "Fill in a diagram of (x, y) coordinates at points around a unit circle with "
                        + "cosine and sine values");
        m125m3t3.addExBlock("Evaluating trigonometric functions using technology")
                .addEx("TR03_ST3B_01", "Evaluate trigonometric functions for angles measured in degrees or radians");
        m125m3t3.addExBlock("Calculate possible values for sine or cosine from the other")
                .addEx("TR03_ST3C_01", "Given a value for sine or cosine, find possible values for the other " +
                        "function");
        m125m3t3.addExBlock("Calculate possible values for all trigonometric functions from sine or cosine")
                .addEx("TR03_ST3C_03", "Given a value for sine or cosine, find possible values for all five other "
                        + "trigonometric functions");
        m125m3t3.addExBlock("Calculate possible value or all trigonometric functions from either secant or cosecant")
                .addEx("TR03_ST3C_05", "Given a value for secant or cosecant, find possible values for all five "
                        + "other trigonometric functions");

        // Module 4

        final ModuleData m125m4 = m125.addModule(4, "Transformations of Trigonometric Functions", "TR04_SR_HW",
                "c44-thumb.png");

        m125m4.skillsReview.addExBlock("Evaluation and order of operations")
                .addEx("TR04_SR1_01", "Determine order in which to perform operations to evaluate an expression");
        m125m4.skillsReview.addExBlock("Behavior of sine at zero")
                .addEx("TR04_SR2_01", "Describe the value and behavior of the sine function at angle zero");
        m125m4.skillsReview.addExBlock("Behavior of cosine at zero")
                .addEx("TR04_SR3_01", "Describe the value and behavior of the cosine function at angle zero");
        m125m4.skillsReview.addExBlock("Behavior of secant at zero")
                .addEx("TR04_SR4_01", "Describe the value and behavior of the secant function at angle zero");
        m125m4.skillsReview.addExBlock("Behavior of cosecant at zero")
                .addEx("TR04_SR5_01", "Describe the value and behavior of the cosecant function at angle zero");
        m125m4.skillsReview.addExBlock("Behavior of tangent at zero")
                .addEx("TR04_SR6_01", "Describe the value and behavior of the tangent function at angle zero");
        m125m4.skillsReview.addExBlock("Behavior of cotangent at zero")
                .addEx("TR04_SR7_01", "Describe the value and behavior of the cotangent function at angle zero");
        m125m4.skillsReview.addExBlock("Relationship between sine and cosine and point where terminal ray of "
                        + "angle meets unit circle")
                .addEx("TR04_SR8_01",
                        "Find the (x, y) coordinates of the intersection point of a terminal ray and the unit circle");
        m125m4.skillsReview.addExBlock("Locating points on parametric curves")
                .addEx("TR04_SR9_01", "Given equations for a parametric curve, find the point on the curve "
                        + "corresponding to a specified parameter value");
        m125m4.skillsReview.addExBlock("Interpreting parametric curves")
                .addEx("TR04_SR10_01", "Given a graph of a curve, and three sets of parametric equations, identify "
                        + "the equations that match the graph");

        final LearningTargetData m125m4t1 = m125m4.addLearningTarget(4, 1, "4.1", "TR04_ST1_HW",
                "I can find or interpret shifts and scalings of trigonometric functions, graphically or "
                        + "algebraically. Specifically, I can:",
                "calculate a vertical shift, and describe its relationship with a constant added to the function "
                        + "value,",
                "calculate a horizontal shift, and describe its relationship with a constant subtracted from the "
                        + "function's input variable,",
                "interpret sine and cosine or secant and cosecant as horizontal shifts of one another,",
                "calculate a scaling of amplitude, and describe its relationship with the coefficient on the function,",
                "calculate a scaling of period, and describe its relationship with the coefficient on the input "
                        + "variable.");
        m125m4t1.addExBlock("Finding equations for functions with vertical shifts")
                .addEx("TR04_ST1A_01",
                        "Given the graph of a vertically shifted sine function, write its equation")
                .addEx("TR04_ST1A_02",
                        "Given the graph of a vertically shifted cosine function, write its equation")
                .addEx("TR04_ST1A_03a",
                        "Given the graph of a vertically shifted secant function, write its equation")
                .addEx("TR04_ST1A_03b",
                        "Given the graph of a vertically shifted cosecant function, write its equation")
                .addEx("TR04_ST1A_04a",
                        "Given the graph of a vertically shifted tangent function, write its equation")
                .addEx("TR04_ST1A_04b",
                        "Given the graph of a vertically shifted cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch the graph of a vertically shifted function")
                .addEx("TR04_ST1A_05a", "Sketch the graph of a vertically shifted sine function")
                .addEx("TR04_ST1A_05b", "Sketch the graph of a vertically shifted cosine function")
                .addEx("TR04_ST1A_05c", "Sketch the graph of a vertically shifted secant function")
                .addEx("TR04_ST1A_05d", "Sketch the graph of a vertically shifted cosecant function")
                .addEx("TR04_ST1A_05e", "Sketch the graph of a vertically shifted tangent function")
                .addEx("TR04_ST1A_05f", "Sketch the graph of a vertically shifted cotangent function");
        m125m4t1.addExBlock("Finding equations for functions with horizontal shifts")
                .addEx("TR04_ST1B_01",
                        "Given the graph of a horizontally shifted sine function, write its equation")
                .addEx("TR04_ST1B_02",
                        "Given the graph of a horizontally shifted cosine function, write its equation")
                .addEx("TR04_ST1B_03a",
                        "Given the graph of a horizontally shifted secant function, write its equation")
                .addEx("TR04_ST1B_03b",
                        "Given the graph of a horizontally shifted cosecant function, write its equation")
                .addEx("TR04_ST1B_04a",
                        "Given the graph of a horizontally shifted tangent function, write its equation")
                .addEx("TR04_ST1B_04b",
                        "Given the graph of a horizontally shifted cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch the graph of a horizontally shifted function")
                .addEx("TR04_ST1B_05a", "Sketch the graph of a horizontally shifted sine function")
                .addEx("TR04_ST1B_05b", "Sketch the graph of a horizontally shifted cosine function")
                .addEx("TR04_ST1B_05c", "Sketch the graph of a horizontally shifted secant function")
                .addEx("TR04_ST1B_05d", "Sketch the graph of a horizontally shifted cosecant function")
                .addEx("TR04_ST1B_05e", "Sketch the graph of a horizontally shifted tangent function")
                .addEx("TR04_ST1B_05f", "Sketch the graph of a horizontally shifted cotangent function");
        m125m4t1.addExBlock("Interpret combinations of vertical and horizontal shifts")
                .addEx("TR04_ST1C_01",
                        "Given the graph of sine or cosine with vertical and horizontal shifts, write its equation");
        m125m4t1.addExBlock("Finding equations for functions with vertical scaling")
                .addEx("TR04_ST1D_01",
                        "Given the graph of a vertically scaled sine function, write its equation")
                .addEx("TR04_ST1D_02",
                        "Given the graph of a vertically scaled cosine function, write its equation")
                .addEx("TR04_ST1D_03a",
                        "Given the graph of a vertically scaled secant function, write its equation")
                .addEx("TR04_ST1D_03b",
                        "Given the graph of a vertically scaled cosecant function, write its equation")
                .addEx("TR04_ST1D_04a",
                        "Given the graph of a vertically scaled tangent function, write its equation")
                .addEx("TR04_ST1D_04b",
                        "Given the graph of a vertically scaled cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch the graph of a vertically scaled function")
                .addEx("TR04_ST1D_05a", "Sketch the graph of a vertically scaled sine function")
                .addEx("TR04_ST1D_05b", "Sketch the graph of a vertically scaled cosine function")
                .addEx("TR04_ST1D_05c", "Sketch the graph of a vertically scaled secant function")
                .addEx("TR04_ST1D_05d", "Sketch the graph of a vertically scaled cosecant function")
                .addEx("TR04_ST1D_05e", "Sketch the graph of a vertically scaled tangent function")
                .addEx("TR04_ST1D_05f", "Sketch the graph of a vertically scaled cotangent function");
        m125m4t1.addExBlock("Finding equations for functions with horizontal scaling")
                .addEx("TR04_ST1E_01",
                        "Given the graph of a horizontally scaled sine function, write its equation")
                .addEx("TR04_ST1E_02",
                        "Given the graph of a horizontally scaled cosine function, write its equation")
                .addEx("TR04_ST1E_03a",
                        "Given the graph of a horizontally scaled secant function, write its equation")
                .addEx("TR04_ST1E_03b",
                        "Given the graph of a horizontally scaled cosecant function, write its equation")
                .addEx("TR04_ST1E_04a",
                        "Given the graph of a horizontally scaled tangent function, write its equation")
                .addEx("TR04_ST1E_04b",
                        "Given the graph of a horizontally scaled cotangent function, write its equation");
        m125m4t1.addExBlock("Sketch the graph of a horizontally scaled function")
                .addEx("TR04_ST1E_05a", "Sketch the graph of a horizontally scaled sine function")
                .addEx("TR04_ST1E_05b", "Sketch the graph of a horizontally scaled cosine function")
                .addEx("TR04_ST1E_05c", "Sketch the graph of a horizontally scaled secant function")
                .addEx("TR04_ST1E_05d", "Sketch the graph of a horizontally scaled cosecant function")
                .addEx("TR04_ST1E_05e", "Sketch the graph of a horizontally scaled tangent function")
                .addEx("TR04_ST1E_05f", "Sketch the graph of a horizontally scaled cotangent function");
        m125m4t1.addExBlock("Combinations of vertical and horizontal scaling")
                .addEx("TR04_ST1E_06",
                        "Sketch the graph of a sine function with vertical and horizontal scalings")
                .addEx("TR04_ST1E_07",
                        "Sketch the graph of a cosine function with vertical and horizontal scalings");
        m125m4t1.addExBlock("Exploration: Connection between scaled cosine/sine and circular motion")
                .addEx("TR04_ST1E_EXP_01",
                        "Investigate scaled cosine and sine as a parametric description of uniform motion around a "
                                + "circle of some radius");

        final LearningTargetData m125m4t2 = m125m4.addLearningTarget(4, 2, "4.2", "TR04_ST2_HW",
                "I can find the algebraic form of a trigonometric function from its graph. Specifically, I can:",
                "identify amplitude, period, and shifts from a graph of a scaled and shifted sine or cosine function,",
                "find a scaled and shifted trigonometric function that matches a given graph, in the form "
                        + "y=A \"fxn\"(B (x-h))+k, where fxn is any of the six trigonometric functions,",
                "construct both a sine and cosine function that matches a given graph.");
        m125m4t2.addExBlock("Amplitude and period")
                .addEx("TR04_ST2A_01",
                        "Given a graph, identify its amplitude, period, vertical shift, and horizontal shift");
        m125m4t2.addExBlock("Sketch the graph of a shifted and scaled sine or cosine function")
                .addEx("TR04_ST2B_01", "Given a scaled and shifted sine function, construct its graph")
                .addEx("TR04_ST2B_02", "Given a scaled and shifted cosine function, construct its graph");
        m125m4t2.addExBlock("Interpreting graphs of scaled and shifted sine or cosine functions")
                .addEx("TR04_ST2B_03",
                        "Given the graph of a scaled and shifted sine function, write its equation")
                .addEx("TR04_ST2B_04",
                        "Given the graph of a scaled and shifted cosine function, write its equation");
        m125m4t2.addExBlock("Interpreting graphs of scaled and shifted general secant or cosecant functions")
                .addEx("TR04_ST2B_05", "Given the graph that could be a scaled and shifted secant or cosecant " +
                        "function, find its equation for either function");
        m125m4t2.addExBlock("Graphs that could be expressed using two different trigonometric functions")
                .addEx("TR04_ST2C_01", "Given the graph that could be a scaled and shifted sine or cosine function, "
                        + "find its equation for either function")
                .addEx("TR04_ST2C_02", "Given the graph that could be a scaled and shifted secant or cosecant "
                        + "function, find its equation for either function");

        final LearningTargetData m125m4t3 = m125m4.addLearningTarget(4, 3, "4.3", "TR04_ST3_HW",
                "I can model data or real-world phenomena using sine and cosine functions.",
                "find a model of tabular data in the form y = A sin(B(x-h)) + k or y = A cos(B(x-h)) + k,",
                "find a model from a verbal description in the form y = A sin(B(x-h)) + k or y = A cos(B(x-h)) + k,",
                "interpret models and make predictions of behavior.");
        m125m4t3.addExBlock("Find period and amplitude from tabular data")
                .addEx("TR04_ST3A_01",
                        "Given a table of data that varies sinusoidally, determine the period and amplitude");
        m125m4t3.addExBlock("Find vertical and horizontal shift from tabular data")
                .addEx("TR04_ST3A_02",
                        "Given a table of data that varies sinusoidally, determine the vertical and horizontal shift");
        m125m4t3.addExBlock("Model tabular data using sine or cosine")
                .addEx("TR04_ST3A_03",
                        "Given a table of data that varies sinusoidally, generate a cosine model");
        m125m4t3.addExBlock("Application: Modeling pendulum motion")
                .addEx("TR04_ST3B_APP_01",
                        "From a verbal description of a pendulum's swing, generate a sine model");
        m125m4t3.addExBlock("Application: Modeling weights suspended on springs")
                .addEx("TR04_ST3B_APP_02",
                        "From a verbal description of a weight bouncing on a spring, generate a cosine model");
        m125m4t3.addExBlock("Application: Modeling object bobbing in water")
                .addEx("TR04_ST3B_APP_03",
                        "From a verbal description of a buoy bobbing in waves, generate a sine model");
        m125m4t3.addExBlock("Application: Modeling bungee jumping")
                .addEx("TR04_ST3B_APP_04",
                        "From a verbal description of a bungee jumper, generate a damped cosine model");
        m125m4t3.addExBlock("Model tabular data and make predictions using the model")
                .addEx("TR04_ST3C_01",
                        "Given a table of data that varies sinusoidally, generate a sine model and use that model "
                                + "to make predictions");

        // Module 5

        final ModuleData m125m5 = m125.addModule(5, "Trigonometric Functions in Right Triangles", "TR05_SR_HW",
                "c46-thumb.png");

        m125m5.skillsReview.addExBlock("Reference angles")
                .addEx("TR05_SR1_01", "Given an angle, find its reference angle (Quadrant 2)")
                .addEx("TR05_SR1_02", "Given an angle, find its reference angle (Quadrant 3)")
                .addEx("TR05_SR1_03", "Given an angle, find its reference angle (Quadrant 4)");
        m125m5.skillsReview.addExBlock("Angles sharing same sine value")
                .addEx("TR05_SR2_01", "Find an angle that has the same sine as a given angle");
        m125m5.skillsReview.addExBlock("Angles sharing same cosine value")
                .addEx("TR05_SR3_01", "Find an angle that has the same cosine as a given angle");
        m125m5.skillsReview.addExBlock(
                        "Definitions of trigonometric functions in terms of x, y coordinates and sine/cosine")
                .addEx("TR05_SR4_01",
                        "Match trigonometric functions to their definitions in terms of (x, y) and in terms of sine "
                                + "and cosine");
        m125m5.skillsReview.addExBlock("Similar triangles")
                .addEx("TR05_SR5_01", "Given similar triangles, solve for side lengths using proportionality");

        final LearningTargetData m125m5t1 = m125m5.addLearningTarget(5, 1, "5.1", "TR05_ST1_HW",
                "Given a right triangle, I can express the relationships between side lengths using trigonometric "
                        + "functions.",
                "deduce the \"SOH-CAH-TOA\" relationships from the definitions of sine, cosine, and tangent in the "
                        + "context of a unit circle,",
                "identify the hypotenuse, and which sides are \"adjacent\" and \"opposite\" relative to each acute "
                        + "angle, and",
                "recall and use the \"SOH-CAH-TOA\" relationships to calculate trigonometric function values from "
                        + "triangle side lengths.");
        m125m5t1.addExBlock("Deduce the SOH-CAH-TOA relationships for triangles with hypotenuse 1")
                .addEx("TR05_ST1A_01",
                        "Derive the \"SOH-CAH-TOA\" relationships for right triangles in a unit circle");
        m125m5t1.addExBlock("Scale relations based on similar triangles")
                .addEx("TR05_ST1A_02", "Given arbitrary size right triangle, create similar triangle with "
                        + "hypotenuse 1, and use to scale side length relationships");
        m125m5t1.addExBlock("Opposite and adjacent sides")
                .addEx("TR05_ST1B_01", "Identifying adjacent and opposite sides to an angle");
        m125m5t1.addExBlock("SOH")
                .addEx("TR05_ST1C_01", "Calculate sine and cosecant values using the sine relationship");
        m125m5t1.addExBlock("CAH")
                .addEx("TR05_ST1C_02", "Calculate cosine and secant values using the cosine relationship");
        m125m5t1.addExBlock("TOA")
                .addEx("TR05_ST1C_03", "Calculate tangent and cotangent values using the tangent relationship");

        final LearningTargetData m125m5t2 = m125m5.addLearningTarget(5, 2, "5.2", "TR05_ST2_HW",
                "I can use trigonometric functions to solve for side lengths in right triangles.",
                "find a side length given one other side length and one angle using sine,",
                "find a side length given one other side length and one angle using cosine,",
                "find a side length given one other side length and one angle using tangent,",
                "given one side and one angle in a right triangle, find all sides and angles, and",
                "apply these relationships in real-world contexts.");
        m125m5t2.addExBlock("Apply the sine relationship")
                .addEx("TR05_ST2A_01", "Use the sine relationship to find hypotenuse length")
                .addEx("TR05_ST2A_02", "Use the sine relationship to find opposite side length");
        m125m5t2.addExBlock("Apply the cosine relationship")
                .addEx("TR05_ST2B_01", "Use the cosine relationship to find hypotenuse length")
                .addEx("TR05_ST2B_02", "Use the cosine relationship to find adjacent side length");
        m125m5t2.addExBlock("Apply the tangent relationship")
                .addEx("TR05_ST2C_01", "Use the tangent relationship to find adjacent side length")
                .addEx("TR05_ST2C_02", "Use the tangent relationship to find opposite side length");
        m125m5t2.addExBlock("Apply multiple relationships")
                .addEx("TR05_ST2D_01",
                        "Given a right triangle with one angle and one side known, find unknown angle and lengths");
        m125m5t2.addExBlock("Application: Calculating building height using its shadow")
                .addEx("TR05_ST2D_APP_01a", "Given shadow angle and distance from building, find building height");
        m125m5t2.addExBlock("Application: Locating ship's position using lighthouses")
                .addEx("TR05_ST2D_APP_01c",
                        "Given bearings to two lighthouses whose positions are charted, find the position of a ship");
        m125m5t2.addExBlock("Application: anchoring a power pole")
                .addEx("TR05_ST2D_APP_02a", "Given power pole height, desired angle for support wires, find distance " +
                        "from pole for anchors, and wire length");
        m125m5t2.addExBlock("Application: Supporting a radio tower")
                .addEx("TR05_ST2D_APP_02b",
                        "Find anchor points and support wire angles for two sets of support wires on a radio tower");
        m125m5t2.addExBlock("Application: Supporting a radio tower")
                .addEx("TR05_ST2D_APP_03a",
                        "Given distance to tree and angle of elevation of its top, find its height");
        m125m5t2.addExBlock("Application: Supporting a cell tower")
                .addEx("TR05_ST2D_APP_03b",
                        "Given support wire anchor distance and angle, find height and wire length");
        m125m5t2.addExBlock("Solving a general triangle in the 'angle-side-side' case without the Law of Sines")
                .addEx("TR05_ST2D_APP_05a",
                        "Given a right triangle with one angle and two sides known, find unknown angle and lengths");
        m125m5t2.addExBlock("Application: Cutting tiles to make patterns")
                .addEx("TR05_ST2D_APP_06b", "Given a desired tile pattern, find dimensions and angles for cuts");

        final LearningTargetData m125m5t3 = m125m5.addLearningTarget(5, 3, "5.3", "TR05_ST3_HW",
                "I can apply relationships between right triangle side lengths and trigonometric functions to "
                        + "reference angles corresponding to angles in quadrants 2, 3, and 4.",
                "draw a right triangle using the reference angle in any quadrant, and",
                "use right triangle relationships in these reference triangles to find relationships between side "
                        + "lengths.");
        m125m5t3.addExBlock("Solve triangle with Quadrant 2 angle")
                .addEx("ST46_3_F01_01",
                        "Given angle to hypotenuse in quadrant 2, solve for side lengths in a right triangle");
        m125m5t3.addExBlock("Solve triangle with Quadrant 3 angle")
                .addEx("ST46_3_F02_01",
                        "Given angle to hypotenuse in quadrant 3, solve for side lengths in a right triangle");
        m125m5t3.addExBlock("Solve triangle with Quadrant 4 angle")
                .addEx("ST46_3_F03_01",
                        "Given angle to hypotenuse in quadrant 4, solve for side lengths in a right triangle");

        // Module 6

        final ModuleData m125m6 = m125.addModule(6, "Inverse Trigonometric Functions", "TR06_SR_HW", "c47-thumb.png");

        m125m6.skillsReview
                .addExBlock("Recall the graph, domain, and range of sine and cosine")
                .addEx("TR06_SR1_01", "Sketch graph of sine and recall its domain and range")
                .addEx("TR06_SR1_02", "Sketch graph of cosine and recall its domain and range");
        m125m6.skillsReview
                .addExBlock("Recall the graph, domain, and range of tangent and cotangent")
                .addEx("TR06_SR2_01", "Sketch graph of tangent and recall its domain and range")
                .addEx("TR06_SR2_02", "Sketch graph of cotangent and recall its domain and range");
        m125m6.skillsReview
                .addExBlock("Recall the graph, domain, and range of secant and cosecant")
                .addEx("TR06_SR3_01", "Sketch graph of secant and recall its domain and range")
                .addEx("TR06_SR3_03", "Sketch graph of cosecant and recall its domain and range");
        m125m6.skillsReview
                .addExBlock("Recall that angles with the same x coordinate on unit circle have the same cosine")
                .addEx("TR06_SR4_01", "Find second angle with the same cosine as a given angle");
        m125m6.skillsReview
                .addExBlock("Recall that angles with the same y coordinate on unit circle have the same sine")
                .addEx("TR06_SR5_01", "Find second angle with the same sine as a given angle");
        m125m6.skillsReview
                .addExBlock("Recall that angles with the same slope have the same tangent")
                .addEx("TR06_SR6_01", "Find second angle with the same tangent as a given angle");
        m125m6.skillsReview
                .addExBlock("Angle relationships in right triangles")
                .addEx("TR06_SR7_01", "Express products and quotients of triangle side lengths in terms of "
                        + "trigonometric functions");

        final LearningTargetData m125m6t1 = m125m6.addLearningTarget(6, 1, "6.1", "TR06_ST1_HW",
                "I can work with inverse functions and identify when a function has an inverse.",
                "describe the requirements for a function to be one-to-one, and apply the horizontal line test,",
                "describe the relationships that inverse functions satisfy and the relationships between the domain "
                        + "and range of inverse functions, find an inverse function algebraically,",
                "identify or sketch the graph of an inverse function from the function's graph, and",
                "find a domain restriction of a function that is one-to-one, including restrictions of "
                        + "trigonometric functions.");
        m125m6t1.addExBlock("Recall what a one-to-one function is, and the horizontal line test")
                .addEx("TR06_ST1A_01", "Given a set of graphs, indicate which are one-to-one functions");
        m125m6t1.addExBlock("Recall facts about inverse functions")
                .addEx("TR06_ST1B_01", "Answer a variety of questions about an inverse function.");
        m125m6t1.addExBlock("Given an algebraic function, find its inverse")
                .addEx("TR06_ST1C_01", "Given a function, find the inverse function");
        m125m6t1.addExBlock("Recall the relationship between the graph of a function and its inverse")
                .addEx("TR06_ST1D_01", "Given the graph of a function, sketch the graph of the inverse function");
        m125m6t1.addExBlock("Domain restrictions")
                .addEx("TR06_ST1E_01",
                        "Given a graph of a function, find domain restrictions where the function is one-to-one");
        m125m6t1.addExBlock("Domain restriction of sine")
                .addEx("TR06_ST1E_02", "Find domain restrictions of sine that are one-to-one");
        m125m6t1.addExBlock("Domain restriction of cosine")
                .addEx("TR06_ST1E_03", "Find domain restrictions of cosine that are one-to-one");
        m125m6t1.addExBlock("Domain restriction of tangent and cotangent")
                .addEx("TR06_ST1E_04", "Find domain restrictions of tangent and cotangent that are one-to-one");
        m125m6t1.addExBlock("Domain restriction of secant and cosecant")
                .addEx("TR06_ST1E_05", "Find domain restrictions of secant and cosecant that are one-to-one");

        final LearningTargetData m125m6t2 = m125m6.addLearningTarget(6, 2, "6.2", "TR06_ST2_HW",
                "I can work with inverse trigonometric functions.",
                "state the definitions of the inverse trigonometric functions, along with their domain and range, and "
                        + "identify or sketch their graphs,",
                "interpret values of inverse trigonometric functions as angles in appropriate units, and",
                "evaluate inverse trigonometric functions using technology, and apply them to find all angles that "
                        + "have a specified value of a trigonometric function.");
        m125m6t2.addExBlock("Definition of inverse sine and inverse cosine, domain and range")
                .addEx("TR06_ST2A_01", "Sketch graphs of inverse sine and inverse cosine");
        m125m6t2.addExBlock("Definition of inverse tangent and inverse cotangent, domain and range")
                .addEx("TR06_ST2A_02", "Sketch graphs of inverse tangent and inverse cotangent");
        m125m6t2.addExBlock("Definition of inverse secant and inverse cosecant, domain and range")
                .addEx("TR06_ST2A_03", "Sketch graphs of inverse secant and inverse cosecant");
        m125m6t2.addExBlock("Argument and result types for inverse trigonometric functions")
                .addEx("TR06_ST2B_01", "Describe the type of value that is the input and output of trigonometric "
                        + "functions and inverse trigonometric functions");
        m125m6t2.addExBlock("Evaluate and interpret inverse sine")
                .addEx("TR06_ST2C_01", "Evaluate inverse sine, then find all angles having a specified sine");
        m125m6t2.addExBlock("Evaluate and interpret inverse cosine")
                .addEx("TR06_ST2C_02", "Evaluate inverse cosine, then find all angles having a specified cosine");
        m125m6t2.addExBlock("Evaluate and interpret inverse tangent")
                .addEx("TR06_ST2C_03", "Evaluate inverse tangent, then find all angles having a specified tangent");

        final LearningTargetData m125m6t3 = m125m6.addLearningTarget(6, 3, "6.3", "TR06_ST3_HW",
                "I can apply inverse trigonometric functions to solve problems.",
                "solve for an angle in a right triangle with known side lengths,",
                "find the angle whose terminal ray has a specified slope, and",
                "evaluate compositions of trigonometric functions with their inverse functions.");
        m125m6t3.addExBlock("Solving using inverse sine")
                .addEx("TR06_ST3A_01", "Find angle in triangle given opposite side and hypotenuse");
        m125m6t3.addExBlock("Solving using inverse cosine")
                .addEx("TR06_ST3A_02", "Find angle in triangle given adjacent side and hypotenuse");
        m125m6t3.addExBlock("Solving using inverse tangent")
                .addEx("TR06_ST3A_03", "Find angle in triangle given opposite and adjacent side");

        m125m6t3.addExBlock("Application: Angle of a ladder into a tree-house")
                .addEx("TR06_ST3A_APP_01", "Given platform height and ladder length, at what angle will a ladder sit?");
        m125m6t3.addExBlock("Application: Checking wheelchair ramp for compliance")
                .addEx("TR06_ST3A_APP_02", "Given length of ramp and total rise height, find angle of rise and see "
                        + "if it falls within allowed values");
        m125m6t3.addExBlock("Application: Designing a mechanism for a clock")
                .addEx("TR06_ST3A_APP_03", "Given a pendulum length and desired swing distance, find swing angle in "
                        + "order to design the escapement mechanism");
        m125m6t3.addExBlock("Application: Placing a load with a crane")
                .addEx("TR06_ST3A_APP_04",
                        "Find the angle a crane's boom needs to have to place a load at a specified location");
        m125m6t3.addExBlock("Application: Choosing a security camera")
                .addEx("TR06_ST3A_APP_05","Given the size of a room, find the required view angle so two cameras "
                        + "can cover the entire room");
        m125m6t3.addExBlock("Find angle from slope")
                .addEx("TR06_ST3B_01", "Find angle given slope of terminal ray");
        m125m6t3.addExBlock("Application: Designing a staircase")
                .addEx("TR06_ST3B_APP_01",
                        "Given the length of a staircase and its total rise, find its angle of ascent.");
        m125m6t3.addExBlock("Compositions: trigonometric function after inverse trigonometric function")
                .addEx("TR06_ST3C_01", "Evaluate the sine of an inverse sine");
        m125m6t3.addExBlock("Compositions: inverse trigonometric function after trigonometric function")
                .addEx("TR06_ST3C_02", "Evaluate the inverse sine of a sine")
                .addEx("TR06_ST3C_03", "Evaluate the inverse tangent of a tangent")
                .addEx("TR06_ST3C_04", "Evaluate the inverse secant of a secant");

        // Module 7

        final ModuleData m125m7 = m125.addModule(7, "Triangles, the Law of Sines, and the Law of Cosines", "TR07_SR_HW",
                "c48-thumb.png");

        m125m7.skillsReview
                .addExBlock("Sum of angles in a triangle is 180&deg; or &pi; radians, supplementary angles")
                .addEx("TR07_SR1_01", "Given a drawing, find the sum of several angle measures");
        m125m7.skillsReview.addExBlock("Pythagorean theorem")
                .addEx("TR07_SR2_01", "Given the length of a chord within a circle, find its radius");
        m125m7.skillsReview.addExBlock("SOH-CAH-TOA relationships")
                .addEx("TR07_SR3_01", "Find angles in a kite shape");
        m125m7.skillsReview.addExBlock("Range of inverse sine, cosine, tangent")
                .addEx("TR07_SR4_01", "Find the quadrant containing inverse trigonometric function values");
        m125m7.skillsReview.addExBlock("Domain and inverse sine, cosine, and tangent")
                .addEx("TR07_SR5_01", "State the domains of inverse sine, inverse cosine, and inverse tangent");
        m125m7.skillsReview.addExBlock("Complimentary and supplementary angles")
                .addEx("TR07_SR6_01", "Some short questions about complementary and supplementary angles");
        m125m7.skillsReview.addExBlock("Triangle area")
                .addEx("TR07_SR7_01", "Recall the parameters needed to calculate triangle or parallelogram area");

        final LearningTargetData m125m7t1 = m125m7.addLearningTarget(7, 1, "7.1", "TR07_ST1_HW",
                "I can recall and apply the Law of Sines. Specifically, I can:",
                "recognize situations where the law of sines applies and write the relationship implied by the law "
                        + "of sines in the context of that situation,",
                "solve for unknown angle and side lengths in general triangles when two angles and one side length "
                        + "are known, and",
                "find all solutions for unknown angles and side length in general triangles when one angle and two "
                        + "side lengths are known.");
        m125m7t1.addExBlock("Recall the law of sines")
                .addEx("TR07_ST1A_01", "Given a labeled triangle, write the relationships given by the law of sines");
        m125m7t1.addExBlock("Solve for missing side - AAS")
                .addEx("TR07_ST1B_01", "Solve a triangle given two angles and one side length not between them");
        m125m7t1.addExBlock("Solve for missing side - ASA")
                .addEx("TR07_ST1B_02", "Solve a triangle given two angles and one side length between them");
        m125m7t1.addExBlock("Solve for missing angle - two solutions case")
                .addEx("TR07_ST1C_01", "Solve a triangle given one angle and two side lengths");
        m125m7t1.addExBlock("Solve for missing angle - one solutions case")
                .addEx("TR07_ST1C_02", "Solve a triangle given one angle and two side lengths");
        m125m7t1.addExBlock("Solve for missing angle - zero solutions case")
                .addEx("TR07_ST1C_03", "Solve a triangle given one angle and two side lengths");

        final LearningTargetData m125m7t2 = m125m7.addLearningTarget(7, 2, "7.2", "TR07_ST2_HW",
                "I can recall and apply the law of cosines. Specifically, I can:",
                "recognize situations where the law of cosines applies and write the relationship given by the law "
                        + "of cosines in the context of that situation,",
                "solve for unknown sides and angles in a triangle when two sides and the included angle are known, and",
                "solve for unknown angles in triangles when all three side lengths are known.");
        m125m7t2.addExBlock("Recall the law of cosines")
                .addEx("TR07_ST2A_01",
                        "Given a labeled triangle, write the relationships given by the law of cosines");
        m125m7t2.addExBlock("Exploration: The connection between law of cosines and Pythagorean theorem")
                .addEx("TR07_ST2A_EXP_01",
                        "Apply law of cosines to a right triangle and show that it reduces to the Pythagorean theorem");
        m125m7t2.addExBlock("Solve for missing side - SAS")
                .addEx("TR07_ST2B_01", "Solve a triangle given one angle and the two adjacent side lengths");
        m125m7t2.addExBlock("Solve for angles given all three sides")
                .addEx("TR07_ST2C_01", "Solve a triangle given three side lengths");

        final LearningTargetData m125m7t3 = m125m7.addLearningTarget(7, 3, "7.3", "TR07_ST3_HW",
                "I can solve general triangle problems. Specifically, I can:",
                "choose an appropriate law or relation in the context of a problem, and",
                "correctly apply the selected law or relation and interpret the result in the context of the problem.");

        m125m7t3.addExBlock("Given a right triangle, choose a technique and solve")
                .addEx("TR07_ST3A_01", "One angle and hypotenuse given, solve for the opposite side length")
                .addEx("TR07_ST3A_02", "Two side lengths given, solve for an angle")
                .addEx("TR07_ST3A_03", "Hypotenuse and one side length given, solve for angle between them")
                .addEx("TR07_ST3A_04", "Hypotenuse and one side length given, solve for other side length")
                .addEx("TR07_ST3A_05", "Hypotenuse and one side length given, solve for supplement of angle")
                .addEx("TR07_ST3A_06", "One side length and supplement of one angle given, find other side length");

        m125m7t3.addExBlock("Applications of right triangles")
                .addEx("TR07_ST3A_APP_01", "Desigining trusses for a roof")
                .addEx("TR07_ST3A_APP_02", "Finding roof area that is usable by solar panels")
                .addEx("TR07_ST3A_APP_03", "Find area of a wall for painting")
                .addEx("TR07_ST3A_APP_04", "Find area of turf needed for a landscaping job");

        m125m7t3.addExBlock("Given a general triangle, choose a technique and solve")
                .addEx("TR07_ST3B_01", "Two side lengths and one angle given, find one other angle")
                .addEx("TR07_ST3B_02", "Two side lengths and included angle given, find remaining side length")
                .addEx("TR07_ST3B_03", "Two angles and one side length given, find another side length")
                .addEx("TR07_ST3B_04", "Three side lengths given, find one angle");

        m125m7t3.addExBlock("Applications of general triangles")
                .addEx("TR07_ST3B_APP_01", "Land surveying - find unknown length of property line")
                .addEx("TR07_ST3B_APP_02", "Routing piping around an obstruction")
                .addEx("TR07_ST3B_APP_03", "Fencing a plot of land into three equal areas")
                .addEx("TR07_ST3B_APP_04", "Find area of farm plot to estimate seed and fertilizer needed")
                .addEx("TR07_ST3B_APP_05", "Find the area of a regular pentgon");

        // Module 8

        final ModuleData m125m8 = m125.addModule(8, "Vectors and Trigonometry", "TR08_SR_HW", "c49-thumb.png");

        m125m8.skillsReview.addExBlock("Vectors, components and vector notation")
                .addEx("TR08_SR1_01", "Given two points in the plane, write the components of the vector between them");
        m125m8.skillsReview.addExBlock("Vector arithmetic")
                .addEx("TR08_SR2_01", "Given three vectors, calculate resultants");
        m125m8.skillsReview.addExBlock("The dot product using components")
                .addEx("TR08_SR3_01", "Given two vectors, calculate their dot product using components");
        m125m8.skillsReview.addExBlock("Triangle area ")
                .addEx("TR08_SR4_01", "Find the areas of twp triangles");
        m125m8.skillsReview.addExBlock("Parallelogram area")
                .addEx("TR08_SR5_01", "Find the area of a parallelogram");
        m125m8.skillsReview.addExBlock("Vector length and unit vectors")
                .addEx("TR08_SR6_01", "Find a unit vector in the direction of a given vector");

        final LearningTargetData m125m8t1 = m125m8.addLearningTarget(8, 1, "8.1", "TR08_ST1_HW",
                "I can apply the relationship between dot product and the angle between two vectors.  Specifically, "
                        + "I can:",
                "use the dot product to compute the length of a vector,",
                "use the dot product to compute the angle between two vectors,",
                "compute the dot product between two vectors of known length meeting at a specified angle, and",
                "determine whether two vectors are perpendicular using the dot product.");
        m125m8t1.addExBlock("Compute the length of a vector")
                .addEx("TR08_ST1A_01", "Calculate the length of a vector using the dot product");
        m125m8t1.addExBlock("Compute the angle between two vectors")
                .addEx("TR08_ST1B_01",
                        "Given two vectors, compute their dot product, lengths, and the angle between them");
        m125m8t1.addExBlock("Application: Solving triangles using vertex coordinates")
                .addEx("TR08_ST1B_APP_01", "Given coordinates of vertices of a triangle, final its interior angles");
        m125m8t1.addExBlock("Compute dot product from vector angle and lengths")
                .addEx("TR08_ST1C_01", "Given the lengths and angle between two vectors, find their dot product");
        m125m8t1.addExBlock("Determine whether vectors are perpendicular")
                .addEx("TR08_ST1D_02", "Given a vector and one component of a second vector, find the missing "
                        + "component to make the vectors perpendicular");
        m125m8t1.addExBlock("Exploration: Find vector perpendicular to a line")
                .addEx("TR08_ST1D_EXP_01", "Find a vector perpendicular to a line");

        final LearningTargetData m125m8t2 = m125m8.addLearningTarget(8, 2, "8.2", "TR08_ST2_HW",
                "I can project vectors in specified directions and decompose vectors into components.  Specifically, "
                        + "I can:",
                "find the projection of one vector in the direction of another,",
                "write a vector as the sum of vectors having specified directions, and",
                "find the distance of a point from a line.");
        m125m8t2.addExBlock("Project vector in direction of a unit vector")
                .addEx("TR08_ST2A_01",
                        "Given a vector and a unit vector, find the projection in the unit vector direction");
        m125m8t2.addExBlock("Project vector in direction of general vector")
                .addEx("TR08_ST2A_02",
                        "Given two vectors, find the projection of first in the direction of the second");
        m125m8t2.addExBlock("Decompose vector")
                .addEx("TR08_ST2B_02", "Given three vectors, write the first as the sum of projections in the "
                        + "directions of the other two");
        m125m8t2.addExBlock("Find shortest vector from a point to a line, and distance from point to line")
                .addEx("TR08_ST2C_01",
                        "Find the shortest vector from a point to a line and the distance from the point to the line");

        final LearningTargetData m125m8t3 = m125m8.addLearningTarget(8, 3, "8.3", "TR08_ST3_HW",
                "I can use vectors and trigonometry to analyze applied contexts.",
                "write a force as a resultant sum of forces acting in specified directions,",
                "model tensions in cables using vectors, and",
                "interpret a vector as a speed and using time plus speed to compute distance.");
        m125m8t3.addExBlock("Application: decomposing force vectors")
                .addEx("TR08_ST3A_01",
                        "Given a scenario with a rope dragging an object on a ramp, analyze forces using vectors");
        m125m8t3.addExBlock("Application: cable/rope tension, two ropes holding up load")
                .addEx("TR08_ST3B_01",
                        "Given scenario where multiple ropes hold a load, calculate forces using vectors");
        m125m8t3.addExBlock("Application: bearing and speed, dead reckoning navigation")
                .addEx("TR08_ST3C_01", "Use bearing direction, speed, and time to calculate start and end position");
        m125m8t3.addExBlock("Application: vectors that change over time")
                .addEx("TR08_ST3D_01", "Analyze distance between two moving objects over time");

        return m125;
    }
}
