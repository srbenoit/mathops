package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawLesson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to work with lesson records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawLessonLogic {
    ;

    /** A commonly used string. */
    private static final String SR_MAT = "Skills Review materials";

    /** The map of hard-coded lessons. */
    private static final Map<String, RawLesson> lessons;

    static {
        final Collection<RawLesson> tempList = new ArrayList<>(200);

        try {
            // MATH 100T
            tempList.add(mkCourseModel("M100T.11",
                    "Recognize and Identify Number Systems"));
            tempList.add(mkCourseModel("M100T.12",
                    "Identify Prime and Composite Numbers"));
            tempList.add(mkCourseModel("M100T.13",
                    "Find the Greatest Common Divisor"));
            tempList.add(mkCourseModel("M100T.14",
                    "Find the Least Common Denominator"));
            tempList.add(mkCourseModel("M100T.15",
                    "Order Rational Numbers"));
            tempList.add(mkCourseModel("M100T.16",
                    "Understanding Positive and Negative Number Operations"));
            tempList.add(mkCourseModel("M100T.17",
                    "Solve Conversions Problems"));
            tempList.add(mkCourseModel("M100T.18",
                    "Solve Precentage Increase or Decrease Problems"));
            tempList.add(mkCourseModel("M100T.19",
                    "Use Exponential Rules to Evaluate Expressions"));
            tempList.add(mkCourseModel("M100T.110",
                    "Simplify Radical Expressions"));

            tempList.add(mkCourseModel("M100T.21",
                    "Apply the Counting Principle"));
            tempList.add(mkCourseModel("M100T.22",
                    "Understanding the Terminology of Algebra"));
            tempList.add(mkCourseModel("M100T.23",
                    "Simplify Algebraic Expressions"));
            tempList.add(mkCourseModel("M100T.24",
                    "Solve Literal Equations"));
            tempList.add(mkCourseModel("M100T.25",
                    "Multiply Binomial Expressions"));
            tempList.add(mkCourseModel("M100T.26",
                    "Factor Quadratic Equations"));
            tempList.add(mkCourseModel("M100T.27",
                    "Solve Linear Word Problems"));
            tempList.add(mkCourseModel("M100T.28",
                    "Use Function Notation"));
            tempList.add(mkCourseModel("M100T.29",
                    "Determine the Slope of a Line (Graphical Approach)"));
            tempList.add(mkCourseModel("M100T.210",
                    "Graph Lines (by Graphing Intersects)"));

            tempList.add(mkCourseModel("M100T.31",
                    "Solve Direct and Indirect Variation Problems"));
            tempList.add(mkCourseModel("M100T.32",
                    "Solve Absolute Value Inequalities"));
            tempList.add(mkCourseModel("M100T.33",
                    "Identify Rational Functions"));
            tempList.add(mkCourseModel("M100T.34",
                    "Determine the Domain and Range of Rational Functions"));
            tempList.add(mkCourseModel("M100T.35",
                    "Identify Radical Functions"));
            tempList.add(mkCourseModel("M100T.36",
                    "Determine the Domain and Range of Radical Functions"));
            tempList.add(mkCourseModel("M100T.37",
                    "Apply the Pythagorean Relationship"));
            tempList.add(mkCourseModel("M100T.38",
                    "Determine the Area of Polygons"));
            tempList.add(mkCourseModel("M100T.39",
                    "Solve Similar Triangles Problems"));

            tempList.add(mkCourseModel("M100T.41",
                    "Solve Literal Equations"));
            tempList.add(mkCourseModel("M100T.42",
                    "Multiply Polynomials"));
            tempList.add(mkCourseModel("M100T.43",
                    "Factor Polynomials"));
            tempList.add(mkCourseModel("M100T.44",
                    "Determine the Slope of a Line (Algabraic Approach)"));
            tempList.add(mkCourseModel("M100T.45",
                    "Write the Slope-Intercept Form of a Line"));
            tempList.add(mkCourseModel("M100T.46",
                    "Determine the Equation of Parallel Lines"));
            tempList.add(mkCourseModel("M100T.47",
                    "Determine the Equation of Perpendicular Lines"));
            tempList.add(mkCourseModel("M100T.48",
                    "Add and Subtract Rational Expressions"));
            tempList.add(mkCourseModel("M100T.49",
                    "Multiply and Divide Rational Expressions"));
            tempList.add(mkCourseModel("M100T.410",
                    "Solve Rational Equations"));
            tempList.add(mkCourseModel("M100T.411",
                    "Solve Radical Equations"));

            // M 100R
            tempList.add(mkCourseModel("M100R.11",
                    "Polynomial Manipulation"));
            tempList.add(mkCourseModel("M100R.12",
                    "Solving One Linear Equation in One Unknown"));
            tempList.add(mkCourseModel("M100R.13",
                    "Mathematical Models"));
            tempList.add(mkCourseModel("M100R.14",
                    "Inequalities/Absolute Values"));
            tempList.add(mkCourseModel("M100R.15",
                    "Graphing on the Plane"));
            tempList.add(mkCourseModel("M100R.16",
                    "Lines in the Plane"));
            tempList.add(mkCourseModel("M100R.17",
                    "Systems of Linear Equations"));
            tempList.add(mkCourseModel("M100R.18",
                    "Distance Formula"));
            tempList.add(mkCourseModel("M100R.19",
                    "Equation of a Circle"));
            tempList.add(mkCourseModel("M100R.110",
                    "Piecewise Functions"));
            tempList.add(mkCourseModel("M100R.111",
                    "Solving Polynomials by Factoring"));
            tempList.add(mkCourseModel("M100R.112",
                    "Complex Numbers"));
            tempList.add(mkCourseModel("M100R.113",
                    "Quadratic Formula"));
            tempList.add(mkCourseModel("M100R.114",
                    "Completing the Square"));
            tempList.add(mkCourseModel("M100R.115",
                    "Constructing Polynomials"));
            tempList.add(mkCourseModel("M100R.116",
                    "Algebraic Fractions"));
            tempList.add(mkCourseModel("M100R.117",
                    "Integer Exponents"));
            tempList.add(mkCourseModel("M100R.118",
                    "Rational Exponents and Radicals"));
            tempList.add(mkCourseModel("M100R.119",
                    "Solving Radical Equations"));
            tempList.add(mkCourseModel("M100R.120",
                    "Power Functions"));

            tempList.add(mkCourseModel("M100R.21",
                    "Angles"));
            tempList.add(mkCourseModel("M100R.22",
                    "Arc Length and Area of a Sector"));
            tempList.add(mkCourseModel("M100R.23",
                    "The Trigonometric Functions"));
            tempList.add(mkCourseModel("M100R.24",
                    "Solving Right Triangles"));
            tempList.add(mkCourseModel("M100R.25",
                    "Solving Oblique Triangles"));
            tempList.add(mkCourseModel("M100R.26",
                    "Narrative Problems"));
            tempList.add(mkCourseModel("M100R.27",
                    "Graphing Trigonometric Functions"));
            tempList.add(mkCourseModel("M100R.28",
                    "Inverse Trigonometric Functions"));
            tempList.add(mkCourseModel("M100R.29",
                    "Basic Trigonometric Identities"));
            tempList.add(mkCourseModel("M100R.210",
                    "Negative-Angle and Cofunction Identities"));
            tempList.add(mkCourseModel("M100R.211",
                    "Sum and Difference Identities"));
            tempList.add(mkCourseModel("M100R.212",
                    "Double-Angle and Half-Angle Identities"));
            tempList.add(mkCourseModel("M100R.213",
                    "Trigonometric Equations"));

            tempList.add(mkCourseModel("M100R.31",
                    "Functions and Graphs"));
            tempList.add(mkCourseModel("M100R.32",
                    "Exponential Functions"));
            tempList.add(mkCourseModel("M100R.33",
                    "Basic Operations on Functions"));
            tempList.add(mkCourseModel("M100R.34",
                    "Composition and Inverse Functions"));
            tempList.add(mkCourseModel("M100R.35",
                    "Logarithmic Functions"));
            tempList.add(mkCourseModel("M100R.36",
                    "Solving Equations"));
            tempList.add(mkCourseModel("M100R.37",
                    "Mathematical Models"));

            // MATH 160R
            tempList.add(mkCourseModel("M160R.11",
                    "Algebra and Trigonometry Review"));
            tempList.add(mkCourseModel("M160R.12",
                    "Limits"));
            tempList.add(mkCourseModel("M160R.13",
                    "Continuity"));

            tempList.add(mkCourseModel("M160R.21",
                    "Derivative Function"));
            tempList.add(mkCourseModel("M160R.22",
                    "Derivative Rules"));
            tempList.add(mkCourseModel("M160R.23",
                    "Extreme Values and Critical Points"));

            tempList.add(mkCourseModel("M160R.31",
                    "Antiderivatives and the Indefinite Integral"));
            tempList.add(mkCourseModel("M160R.32",
                    "Finite Sums and the Definite Integral"));
            tempList.add(mkCourseModel("M160R.33",
                    "Substitution"));

            // MATH 117
            tempList.add(mkCourseModel("M117.01", SR_MAT));

            tempList.add(mkCourseModel("M117.11",
                    "Generalize Linear Equations from Tables and Graphs"));
            tempList.add(mkCourseModel("M117.12",
                    "Use function Notation"));
            tempList.add(mkCourseModel("M117.13",
                    "Determine the equation of a line given conditions"));
            tempList.add(mkCourseModel("M117.14",
                    "Solve equations and inequalities"));
            tempList.add(mkCourseModel("M117.15",
                    "Model with linear functions"));

            tempList.add(mkCourseModel("M117.21",
                    "Graph absolute functions"));
            tempList.add(mkCourseModel("M117.22",
                    "Graph piecewise defined functions"));
            tempList.add(mkCourseModel("M117.23",
                    "Solve absolute value equations"));
            tempList.add(mkCourseModel("M117.24",
                    "Solve absolute value inequalities"));
            tempList.add(mkCourseModel("M117.25",
                    "Model with absolute value and piecewise defined functions"));

            tempList.add(mkCourseModel("M117.31",
                    "Graph quadratic functions"));
            tempList.add(mkCourseModel("M117.32",
                    "Solve quadratic equations"));
            tempList.add(mkCourseModel("M117.33",
                    "Solve quadratic inequalities"));
            tempList.add(mkCourseModel("M117.34",
                    "Graph circles and ellipses"));
            tempList.add(mkCourseModel("M117.35",
                    "Model with quadratic functions and relations"));

            tempList.add(mkCourseModel("M117.41",
                    "Solve systems of linear equations"));
            tempList.add(mkCourseModel("M117.42",
                    "Solve systems using matrices"));
            tempList.add(mkCourseModel("M117.43",
                    "Solve systems of linear inequalities"));
            tempList.add(mkCourseModel("M117.44",
                    "Solve systems with other functions and relations"));
            tempList.add(mkCourseModel("M117.45",
                    "Model with systems"));

            // MATH 118
            tempList.add(mkCourseModel("M118.01", SR_MAT));

            tempList.add(mkCourseModel("M118.11",
                    "Identify and graph polynomial functions"));
            tempList.add(mkCourseModel("M118.12",
                    "Build polynomial functions"));
            tempList.add(mkCourseModel("M118.13",
                    "Determine zeros of polynomial functions"));
            tempList.add(mkCourseModel("M118.14",
                    "Solve problems with polynomials"));
            tempList.add(mkCourseModel("M118.15",
                    "Determine polynomial functions for data sets"));

            tempList.add(mkCourseModel("M118.21",
                    "Determine local behavior of rational functions"));
            tempList.add(mkCourseModel("M118.22",
                    "Determine asymptotic and end-behavior properties of "
                            + "rational functions"));
            tempList.add(mkCourseModel("M118.23",
                    "Build rational functions"));
            tempList.add(mkCourseModel("M118.24",
                    "Solve rational equations and inequalities"));
            tempList.add(mkCourseModel("M118.25",
                    "Model with rational functions"));

            tempList.add(mkCourseModel("M118.31",
                    "Graph square root functions"));
            tempList.add(mkCourseModel("M118.32",
                    "Graph other root functions"));
            tempList.add(mkCourseModel("M118.33",
                    "Solve radical equations"));
            tempList.add(mkCourseModel("M118.34",
                    "Solve radical inequalities"));
            tempList.add(mkCourseModel("M118.35",
                    "Model with radical functions"));

            tempList.add(mkCourseModel("M118.41",
                    "Evaluate power functions"));
            tempList.add(mkCourseModel("M118.42",
                    "Graph power functions"));
            tempList.add(mkCourseModel("M118.43",
                    "Solve equations and inequalities with power functions"));
            tempList.add(mkCourseModel("M118.44",
                    "Solve power function applications"));
            tempList.add(mkCourseModel("M118.45",
                    "Solve systems with nonlinear functions"));

            // MATH 124
            tempList.add(mkCourseModel("M124.01", SR_MAT));

            tempList.add(mkCourseModel("M124.11",
                    "Evaluate and graph relations and functions"));
            tempList.add(mkCourseModel("M124.12",
                    "Evaluate functions and determine average rates of change"));
            tempList.add(mkCourseModel("M124.13",
                    "Solve equations related to functions"));
            tempList.add(mkCourseModel("M124.14",
                    "Perform operations on functions"));
            tempList.add(mkCourseModel("M124.15",
                    "Given a function, find its inverse"));

            tempList.add(mkCourseModel("M124.21",
                    "Write and evaluate functions of the form "
                            + "<em>y = ab<sup>x</sup></em>"));
            tempList.add(mkCourseModel("M124.22",
                    "Graph and interpret graphs of functions of the form "
                            + "<em>y = ab<sup>x</sup></em>"));
            tempList.add(mkCourseModel("M124.23",
                    "Write and evaluate functions of the form "
                            + "<em>y</em> = log <sub><em>b</em></sub><em>x</em>"));
            tempList.add(mkCourseModel("M124.24",
                    "Interpret graphs of functions of the form "
                            + "<em>y</em> = log<sub><em>b</em></sub><em>x</em> "
                            + "and solve equations"));
            tempList.add(mkCourseModel("M124.25",
                    "Interpreting <em>y = ae<sup>x</sup></em> and "
                            + "<em>y</em> = ln <em>x</em>"));

            tempList.add(mkCourseModel("M124.31",
                    "Translate between logarithmic and exponential forms"));
            tempList.add(mkCourseModel("M124.32",
                    "Develop and apply properties of logarithms I"));
            tempList.add(mkCourseModel("M124.33",
                    "Develop and apply properties of logarithms II"));
            tempList.add(mkCourseModel("M124.34",
                    "Derive and apply the base-changing formula"));
            tempList.add(mkCourseModel("M124.35",
                    "Convert between the forms <em>y = ab<sup>x</sup></em> and "
                            + "<em>y = ae<sup>kx</sup></em>"));

            tempList.add(mkCourseModel("M124.41",
                    "Solve exponential equations and their applications"));
            tempList.add(mkCourseModel("M124.42",
                    "Solve logarithmic equations and their applications"));
            tempList.add(mkCourseModel("M124.43",
                    "Model real-world situations with exponential models"));
            tempList.add(mkCourseModel("M124.44",
                    "Model real-world situations with logarithmic models"));
            tempList.add(mkCourseModel("M124.45",
                    "Solve logistic growth problems"));

            // MATH 125
            tempList.add(mkCourseModel("M125.01", SR_MAT));

            tempList.add(mkCourseModel("M125.11",
                    "Review right triangles"));
            tempList.add(mkCourseModel("M125.12",
                    "Compute with basic trigonometric functions"));
            tempList.add(mkCourseModel("M125.13",
                    "Solve simple triangles"));
            tempList.add(mkCourseModel("M125.14",
                    "Work with reference angles"));
            tempList.add(mkCourseModel("M125.15",
                    "Solve trigonometric applications"));

            tempList.add(mkCourseModel("M125.21",
                    "Derive and identify the law of sines and the law of cosines"));
            tempList.add(mkCourseModel("M125.22",
                    "Apply the law of sines I: two angles"));
            tempList.add(mkCourseModel("M125.23",
                    "Apply the law of sines II: two sides - the ambiguous case"));
            tempList.add(mkCourseModel("M125.24",
                    "Apply the law of cosines I"));
            tempList.add(mkCourseModel("M125.25",
                    "Solve problems involving vectors"));

            tempList.add(mkCourseModel("M125.31",
                    "Construct the unit circle"));
            tempList.add(mkCourseModel("M125.32",
                    "Label points on the unit circle"));
            tempList.add(mkCourseModel("M125.33",
                    "Compute with radian measure"));
            tempList.add(mkCourseModel("M125.34",
                    "Applications"));
            tempList.add(mkCourseModel("M125.35",
                    "Solve problems with complex numbers"));

            tempList.add(mkCourseModel("M125.41",
                    "Translate <em>y</em> = sin <em>x</em> and "
                            + "<em>y</em> = cos <em>x</em>"));
            tempList.add(mkCourseModel("M125.42",
                    "Transform <em>y</em> = sin <em>x</em> and "
                            + "<em>y</em> = cos <em>x</em>"));
            tempList.add(mkCourseModel("M125.43",
                    "Combine transformations"));
            tempList.add(mkCourseModel("M125.44",
                    "Graph and identify graphs of other trigonometric functions"));
            tempList.add(mkCourseModel("M125.45",
                    "Model real-world situations with periodic functions"));

            // MATH 126
            tempList.add(mkCourseModel("M126.01", SR_MAT));

            tempList.add(mkCourseModel("M126.11",
                    "Review of common trigonometric functions"));
            tempList.add(mkCourseModel("M126.12",
                    "Graph inverse trigonometric functions"));
            tempList.add(mkCourseModel("M126.13",
                    "Determine angles from trigonometric values"));
            tempList.add(mkCourseModel("M126.14",
                    "Use composition to find values"));
            tempList.add(mkCourseModel("M126.15",
                    "Solve applications"));

            tempList.add(mkCourseModel("M126.21",
                    "Develop and apply basic identities"));
            tempList.add(mkCourseModel("M126.22",
                    "Develop and apply cofunction and odd-even identities"));
            tempList.add(mkCourseModel("M126.23",
                    "Develop and apply Pythagorean identities"));
            tempList.add(mkCourseModel("M126.24",
                    "Verify identities"));
            tempList.add(mkCourseModel("M126.25",
                    "Solve equations"));

            tempList.add(mkCourseModel("M126.31",
                    "Develop and apply the cosine sum identity"));
            tempList.add(mkCourseModel("M126.32",
                    "Develop and apply the cosine of a difference identity"));
            tempList.add(mkCourseModel("M126.33",
                    "Develop and apply the sine sum identity"));
            tempList.add(mkCourseModel("M126.34",
                    "Develop and apply the sine of a difference identity"));
            tempList.add(mkCourseModel("M126.35",
                    "Develop and apply the tangent of sums and differences"));

            tempList.add(mkCourseModel("M126.41",
                    "Derive and apply the double angle identity for sine"));
            tempList.add(mkCourseModel("M126.42",
                    "Derive and apply the double angle identity for cosine and tangent"));
            tempList.add(mkCourseModel("M126.43",
                    "Derive and apply the half-angle identities"));
            tempList.add(mkCourseModel("M126.44",
                    "Solve equations"));
            tempList.add(mkCourseModel("M126.45",
                    "Solve equations using identities"));

            // MATH 1170
            tempList.add(mkCourseModel("M1170.01", SR_MAT));

            tempList.add(mkCourseModel("M1170.11",
                    "Generalize linear equations from tables and graphs"));
            tempList.add(mkCourseModel("M1170.12",
                    "Use function notation"));
            tempList.add(mkCourseModel("M1170.13",
                    "Use slope-intercept to determine the equation of the line"));
            tempList.add(mkCourseModel("M1170.14",
                    "Use point-slope to determine the equation of the line"));
            tempList.add(mkCourseModel("M1170.15",
                    "Find equations of parallel and perpendicular lines"));
            tempList.add(mkCourseModel("M1170.16",
                    "Solve linear equations"));
            tempList.add(mkCourseModel("M1170.17",
                    "Solve linear inequalities"));
            tempList.add(mkCourseModel("M1170.18",
                    "Solve literal equations"));
            tempList.add(mkCourseModel("M1170.19",
                    "Determine if a data set is linear"));
            tempList.add(mkCourseModel("M1170.110",
                    "Given a data set determine a trend line and make predictions"));

            tempList.add(mkCourseModel("M1170.21",
                    "Given an absolute value function, sketch its graph"));
            tempList.add(mkCourseModel("M1170.22",
                    "Given the graph of an absolute value function, determine "
                            + "its equation"));
            tempList.add(mkCourseModel("M1170.23",
                    "Graph and interpret piecewise-defined functions"));
            tempList.add(mkCourseModel("M1170.24",
                    "Given a piecewise-defined function determine the rules "
                            + "and domains"));
            tempList.add(mkCourseModel("M1170.25",
                    "Develop and apply methods to solve absolute value equations "
                            + "of the form <em>|u| = a</em> where a > 0"));
            tempList.add(mkCourseModel("M1170.26",
                    "Develop and apply methods to solve absolute value equations "
                            + "of the form <em>|u| = v</em> where <em>v</em> is an "
                            + "expression in <em>x</em>"));
            tempList.add(mkCourseModel("M1170.27",
                    "Develop and apply methods to solve absolute value inequalities "
                            + "of the form <em>|u| &lt; a</em> where <em>a &gt; 0</em>"));
            tempList.add(mkCourseModel("M1170.28",
                    "Develop and apply methods to solve absolute value inequalities "
                            + "of the form <em>|u| &gt; a</em> where <em>a &gt; 0</em>"));
            tempList.add(mkCourseModel("M1170.29",
                    "Model and interpret situations with absolute value functions"));
            tempList.add(mkCourseModel("M1170.210",
                    "Model and interpret situations with piecewise-defined functions"));

            tempList.add(mkCourseModel("M1170.31",
                    "Given the graph of a quadratic function, determine its equation"));
            tempList.add(mkCourseModel("M1170.32",
                    "Write and graph quadratic functions in vertex form and determine the "
                            + "domain, range, and where increasing or decreasing."));
            tempList.add(mkCourseModel("M1170.33",
                    "Solve quadratic applications"));
            tempList.add(mkCourseModel("M1170.34",
                    "Solve quadratic equations by factoring, completing the square, "
                            + "with the quadratic formula, and using technology"));
            tempList.add(mkCourseModel("M1170.35",
                    "Solve quadratic inequalities"));
            tempList.add(mkCourseModel("M1170.36",
                    "Determine the distance between two points"));
            tempList.add(mkCourseModel("M1170.37",
                    "Graph and interpret graphs of the form"));
            tempList.add(mkCourseModel("M1170.38",
                    "Solve quadratic applications"));
            tempList.add(mkCourseModel("M1170.39",
                    "Determine average and instantaneous rates of change"));
            tempList.add(mkCourseModel("M1170.310",
                    "Model and interpret situations with quadratic functions"));

            tempList.add(mkCourseModel("M1170.41",
                    "Solve systems of linear equations using substitution"));
            tempList.add(mkCourseModel("M1170.42",
                    "Solve systems of linear equations using elimination"));
            tempList.add(mkCourseModel("M1170.43",
                    "Solve two variable systems using row reduction"));
            tempList.add(mkCourseModel("M1170.44",
                    "Solve multi-variable systems using matrices"));
            tempList.add(mkCourseModel("M1170.45",
                    "Solve linear inequalities"));
            tempList.add(mkCourseModel("M1170.46",
                    "Solve systems of linear inequalities"));
            tempList.add(mkCourseModel("M1170.47",
                    "Solve nonlinear systems of equations"));
            tempList.add(mkCourseModel("M1170.48",
                    "Solve nonlinear systems of inequalities"));
            tempList.add(mkCourseModel("M1170.49",
                    "Model with systems I"));
            tempList.add(mkCourseModel("M1170.410",
                    "Model with systems II"));

            // MATH 1180
            tempList.add(mkCourseModel("M1180.01", SR_MAT));

            tempList.add(mkCourseModel("M1180.11",
                    "Determine if a function is a polynomial function"));
            tempList.add(mkCourseModel("M1180.12",
                    "Evaluate and graph polynomial functions"));
            tempList.add(mkCourseModel("M1180.13",
                    "Graph polynomials in factored form"));
            tempList.add(mkCourseModel("M1180.14",
                    "Construct a polynomial function with given zeros"));
            tempList.add(mkCourseModel("M1180.15",
                    "Determine zeros of polynomial functions"));
            tempList.add(mkCourseModel("M1180.16",
                    "Solve polynomial equations and inequalities"));
            tempList.add(mkCourseModel("M1180.17",
                    "Solve max/min problems"));
            tempList.add(mkCourseModel("M1180.18",
                    "Compute average rate of change"));
            tempList.add(mkCourseModel("M1180.19",
                    "Given polynomial data, determine the degree and the model"));
            tempList.add(mkCourseModel("M1180.110",
                    "Use regression to determine polynomial models"));

            tempList.add(mkCourseModel("M1180.21",
                    "Evaluate rational functions"));
            tempList.add(mkCourseModel("M1180.22",
                    "Graph rational functions"));
            tempList.add(mkCourseModel("M1180.23",
                    "Determine horizontal asymptotes"));
            tempList.add(mkCourseModel("M1180.24",
                    "Determine oblique asymptotes and end behavior"));
            tempList.add(mkCourseModel("M1180.25",
                    "Construct rational functions with horizontal asymptotes"));
            tempList.add(mkCourseModel("M1180.26",
                    "Construct rational functions with slant asymptotes"));
            tempList.add(mkCourseModel("M1180.27",
                    "Solve rational equations"));
            tempList.add(mkCourseModel("M1180.28",
                    "Solve rational inequalities"));
            tempList.add(mkCourseModel("M1180.29",
                    "Solve application problems I"));
            tempList.add(mkCourseModel("M1180.210",
                    "Solve application problems II"));

            tempList.add(mkCourseModel("M1180.31",
                    "Evaluate square root functions"));
            tempList.add(mkCourseModel("M1180.32",
                    "Graph and interpret square root functions"));
            tempList.add(mkCourseModel("M1180.33",
                    "Evaluate radical functions"));
            tempList.add(mkCourseModel("M1180.34",
                    "Graph and interpret radical functions"));
            tempList.add(mkCourseModel("M1180.35",
                    "Solve square root equations"));
            tempList.add(mkCourseModel("M1180.36",
                    "Solve other radical equations"));
            tempList.add(mkCourseModel("M1180.37",
                    "Solve square root inequalities"));
            tempList.add(mkCourseModel("M1180.38",
                    "Solve other radical inequalities"));
            tempList.add(mkCourseModel("M1180.39",
                    "Solve radical applications I"));
            tempList.add(mkCourseModel("M1180.310",
                    "Solve radical applications II"));

            tempList.add(mkCourseModel("M1180.41",
                    "Evaluate power functions I"));
            tempList.add(mkCourseModel("M1180.42",
                    "Evaluate power functions II"));
            tempList.add(mkCourseModel("M1180.43",
                    "Graph power functions I"));
            tempList.add(mkCourseModel("M1180.44",
                    "Graph power function II"));
            tempList.add(mkCourseModel("M1180.45",
                    "Solve equations with power functions"));
            tempList.add(mkCourseModel("M1180.46",
                    "Solve inequalities with power functions"));
            tempList.add(mkCourseModel("M1180.47",
                    "Solve applications with power functions"));
            tempList.add(mkCourseModel("M1180.48",
                    "Solve applications with power regression"));
            tempList.add(mkCourseModel("M1180.49",
                    "Solve systems with nonlinear functions algebraically"));
            tempList.add(mkCourseModel("M1180.410",
                    "Solve systems with nonlinear functions graphically"));

            // MATH 1240
            tempList.add(mkCourseModel("M1240.01", SR_MAT));

            tempList.add(mkCourseModel("M1240.11",
                    "Evaluate and graph relations and functions, identify the domain "
                            + "and range"));
            tempList.add(mkCourseModel("M1240.12",
                    "Identify the domain and range"));
            tempList.add(mkCourseModel("M1240.13",
                    "Evaluate functions to determine average rates of change."));
            tempList.add(mkCourseModel("M1240.14",
                    "Given an output solve for the input algebraically."));
            tempList.add(mkCourseModel("M1240.15",
                    "Given an output solve for the input graphically."));
            tempList.add(mkCourseModel("M1240.16",
                    "Perform operations on functions."));
            tempList.add(mkCourseModel("M1240.17",
                    "Given two functions, <em>f</em> and <em>g</em>, find and compare "
                            + "<em>(f <small>o</small> g)(x)</em> and "
                            + "<em>(g <small>o</small> f)(x)</em>."));
            tempList.add(mkCourseModel("M1240.18",
                    "Given a function write it as the composition of two functions."));
            tempList.add(mkCourseModel("M1240.19",
                    "Given a table, graph or rule for a function, identify the inverse "
                            + "numerically and graphically. Determine whether or not its "
                            + "inverse is a function."));
            tempList.add(mkCourseModel("M1240.110",
                    "Given <em>f</em>, determine <em>f<sup>-1</sup></em> and compose "
                            + "<em>f</em> with <em>f<sup>-1</sup></em> and "
                            + "<em>f<sup>-1</sup></em> with <em>f</em>."));

            tempList.add(mkCourseModel("M1240.21",
                    "Given a growth or decay situation write an exponential function "
                            + "and evaluate it for a given input."));
            tempList.add(mkCourseModel("M1240.22",
                    "Given a table of exponential data determine an exponential "
                            + "function and evaluate it for a given input."));
            tempList.add(mkCourseModel("M1240.23",
                    "Graph and interpret graphs of functions of the form "
                            + "<em>y = ab<sup>x</sup></em>."));
            tempList.add(mkCourseModel("M1240.24",
                    "Solve exponential equations numerically and graphically."));
            tempList.add(mkCourseModel("M1240.25",
                    "Given an exponential function determine its inverse function and "
                            + "build a table and graph."));
            tempList.add(mkCourseModel("M1240.26",
                    "Compute logarithms."));
            tempList.add(mkCourseModel("M1240.27",
                    "Given the graph of a logarithmic function identify the equation."));
            tempList.add(mkCourseModel("M1240.28",
                    "Solve basic logarithmic equations algabraically."));
            tempList.add(mkCourseModel("M1240.29",
                    "Graph <em>y = ae<sup>x</sup></em> and <em>y =</em> ln <em>x</em>."));
            tempList.add(mkCourseModel("M1240.210",
                    "Solve problems using the model <em>y = ae<sup>x</sup></em>."));

            tempList.add(mkCourseModel("M1240.31",
                    "Simplify <em>b</em><sup>log<sub><em>b</em></sub> "
                            + "<em>u</em></sup>."));
            tempList.add(mkCourseModel("M1240.32",
                    "Simplify log<sub><em>b</em></sub> <em>b<sup>u</sup></em>."));
            tempList.add(mkCourseModel("M1240.33",
                    "Develop and apply the product and quotient rules."));
            tempList.add(mkCourseModel("M1240.34",
                    "Develop and apply the power rule."));
            tempList.add(mkCourseModel("M1240.35",
                    "Use properties to rewrite logarithms."));
            tempList.add(mkCourseModel("M1240.36",
                    "Use properties to rewrite expressions as single logarithms."));
            tempList.add(mkCourseModel("M1240.37",
                    "Compute logarithms using the base changing formulas."));
            tempList.add(mkCourseModel("M1240.38",
                    "Build tables and graphs of logarithmic functions in other bases."));
            tempList.add(mkCourseModel("M1240.39",
                    "Convert from the form <em>y = ab<sup>x</sup></em> "
                            + "to <em>y = ae<sup>kx</sup> and compare."));
            tempList.add(mkCourseModel("M1240.310",
                    "Convert from the form <em>y = ae<sup>kx</sup> to "
                            + "<em>y = ab<sup>x</sup></em> and interpret."));

            tempList.add(mkCourseModel("M1240.41",
                    "Simplify and solve equations of the form "
                            + "<em>b<sup>u</sup> = b<sup>v</sup></em>."));
            tempList.add(mkCourseModel("M1240.42",
                    "olve exponential equations using logarithms or technology."));
            tempList.add(mkCourseModel("M1240.43",
                    "Simplify and solve equations of the form "
                            + "log<sub><em>b</em></sub><em>u</em> = "
                            + "log<sub><em>b</em></sub><em>v</em>."));
            tempList.add(mkCourseModel("M1240.44",
                    "Solve equations of the form "
                            + "log<sub><em>b</em></sub><em>u</em> = <em>v</em>."));
            tempList.add(mkCourseModel("M1240.45",
                    "Solve growth and decay problems."));
            tempList.add(mkCourseModel("M1240.46",
                    "Fit an exponential function to data and solve related problems."));
            tempList.add(mkCourseModel("M1240.47",
                    "Solve problems involving decibels."));
            tempList.add(mkCourseModel("M1240.48",
                    "Solve problems involving pH."));
            tempList.add(mkCourseModel("M1240.49",
                    "Solve logistic growth problems."));

            // MATH 1250
            tempList.add(mkCourseModel("M1250.01", SR_MAT));

            tempList.add(mkCourseModel("M1250.11",
                    "Solve problems using the Pythagorean Theorem."));
            tempList.add(mkCourseModel("M1250.12",
                    "Solve problems using 45-45-90 right triangles."));
            tempList.add(mkCourseModel("M1250.13",
                    "Solve problems using 30-60-90 right triangles."));
            tempList.add(mkCourseModel("M1250.14",
                    "Determine basic trigonometric function values for "
                            + "right triangles."));
            tempList.add(mkCourseModel("M1250.15",

                    "Determine basic trigonometric function values for special "
                            + "right triangles."));
            tempList.add(mkCourseModel("M1250.16",
                    "Use basic trigonometric functions to solve right triangle "
                            + "problems."));
            tempList.add(mkCourseModel("M1250.17",
                    "Use inverses to solve right triangle problems."));
            tempList.add(mkCourseModel("M1250.18",
                    "Determine basic trigonometric function values using "
                            + "reference angles."));
            tempList.add(mkCourseModel("M1250.19",
                    "Determine trigonometric function values using reference angles."));
            tempList.add(mkCourseModel("M1250.110",
                    "Solve trigonometric applications."));

            tempList.add(mkCourseModel("M1250.21",
                    "Derive and identify the Law of Sines."));
            tempList.add(mkCourseModel("M1250.22",
                    "Derive and identify the Law of Cosines."));
            tempList.add(mkCourseModel("M1250.23",
                    "Apply the Law of Sines I: Two angles"));
            tempList.add(mkCourseModel("M1250.24",
                    "Apply the Law of Sines II: Two Sides: The Ambiguous Case"));
            tempList.add(mkCourseModel("M1250.25",
                    "Solve applications with Law of Sines."));
            tempList.add(mkCourseModel("M1250.26",
                    "Apply the Law of Cosines."));
            tempList.add(mkCourseModel("M1250.27",
                    "Solve applications with Law of Cosines."));
            tempList.add(mkCourseModel("M1250.28",
                    "Given a vector, compute its magnitude, direction, and scalar "
                            + "product."));
            tempList.add(mkCourseModel("M1250.29",
                    "Perform operations on vectors."));
            tempList.add(mkCourseModel("M1250.210",
                    "Compute the dot product and angle between two vectors."));

            tempList.add(mkCourseModel("M1250.31",
                    "Construct the unit circle."));
            tempList.add(mkCourseModel("M1250.32",
                    "Identify points on the unit circle."));
            tempList.add(mkCourseModel("M1250.33",
                    "Determine sine and cosine values from the unit circle."));
            tempList.add(mkCourseModel("M1250.34",
                    "Given an angle in radians compute trigonometric function values."));
            tempList.add(mkCourseModel("M1250.35",
                    "Given a trigonometric value determine the angle in radians "
                            + "and degrees."));
            tempList.add(mkCourseModel("M1250.36",
                    "Compute arc length."));
            tempList.add(mkCourseModel("M1250.37",
                    "Compute area of a sector."));
            tempList.add(mkCourseModel("M1250.38",
                    "Perform operations on complex numbers."));
            tempList.add(mkCourseModel("M1250.39",
                    "Given a complex number, determine relationships between its "
                            + "coordinates, direction, and magnitude."));
            tempList.add(mkCourseModel("M1250.310",
                    "Convert between rectangular and trigonometric forms."));

            tempList.add(mkCourseModel("M1250.41",
                    "Graph sine, cosine, and tangent."));
            tempList.add(mkCourseModel("M1250.42",
                    "Graph and identify vertical and horizontal shifts of "
                            + "<em>y</em> = sin <em>x</em> and "
                            + "<em>y</em> = cos <em>x</em>."));
            tempList.add(mkCourseModel("M1250.43",
                    "Graph and identify translations of <em>y</em> = sin <em>x</em> and "
                            + "<em>y</em> = cos <em>x</em>."));
            tempList.add(mkCourseModel("M1250.44",
                    "Graph and identify amplitude transformations of the form "
                            + "<em>y = a</em> sin <em>x</em> and "
                            + "<em>y = a</em> cos <em>x</em>."));
            tempList.add(mkCourseModel("M1250.45",
                    "Graph and identify period transformations of the form "
                            + "<em>y = a</em> sin <em>bx</em> and "
                            + "<em>y = a</em> cos <em>bx</em>."));
            tempList.add(mkCourseModel("M1250.46",
                    "Graph and identify transformations of the form "
                            + "<em>y = a</em> sin <em>b(x-h) + k</em>."));
            tempList.add(mkCourseModel("M1250.47",
                    "Graph and identify transformations of the form "
                            + "<em>y = a</em> cos <em>b(x-h) + k</em>."));
            tempList.add(mkCourseModel("M1250.48",
                    "Graph and identify transformations of "
                            + "<em>y</em> = tan <em>x</em>."));
            tempList.add(mkCourseModel("M1250.49",
                    "Graph and identify transformations of <em>y</em> = sec <em>x</em>, "
                            + "<em>y</em> = csc <em>x</em>, and "
                            + "<em>y</em> = cot <em>x</em>."));
            tempList.add(mkCourseModel("M1250.410",
                    "Model real world situations with periodic functions."));

            // MATH 1260
            tempList.add(mkCourseModel("M1260.01", SR_MAT));

            tempList.add(mkCourseModel("M1260.11",
                    "Determine values of trigonometric functions for special angles."));
            tempList.add(mkCourseModel("M1260.12",
                    "Detemine values of trigonometric functions for angles."));
            tempList.add(mkCourseModel("M1260.13",
                    "Determine the domain and range, and graph for "
                            + "<em>y</em> = sin<sup>-1</sup> (<em>x</em>)."));
            tempList.add(mkCourseModel("M1260.14",
                    "Determine the domain and range, and graph for "
                            + "<em>y</em> = cos<sup>-1</sup> (<em>x</em>) and "
                            + "<em>y</em> = tan<sup>-1</sup> (<em>x</em>)."));
            tempList.add(mkCourseModel("M1260.15",
                    "Determine values for inverse functions."));
            tempList.add(mkCourseModel("M1260.16",
                    "Determine solutions to equations."));
            tempList.add(mkCourseModel("M1260.17",
                    "Use composition to find exact values."));
            tempList.add(mkCourseModel("M1260.18",
                    "Use composition to explore inverses."));
            tempList.add(mkCourseModel("M1260.19",
                    "Solve angle of elevation problems."));
            tempList.add(mkCourseModel("M1260.110",
                    "Solve other applications."));

            tempList.add(mkCourseModel("M1260.21",
                    "Verify identities."));
            tempList.add(mkCourseModel("M1260.22",
                    "Determine the domain of vailidity for trigonometric identities."));
            tempList.add(mkCourseModel("M1260.23",
                    "Determine cofunction identities."));
            tempList.add(mkCourseModel("M1260.24",
                    "Determine odd-even identities."));
            tempList.add(mkCourseModel("M1260.25",
                    "Develop the Pythagorean identities."));
            tempList.add(mkCourseModel("M1260.26",
                    "Apply the Pythagorean identities."));
            tempList.add(mkCourseModel("M1260.27",
                    "Prove elementary identities."));
            tempList.add(mkCourseModel("M1260.28",
                    "Prove identities."));
            tempList.add(mkCourseModel("M1260.29",
                    "Solve trigonometric equations."));
            tempList.add(mkCourseModel("M1260.210",
                    "Use Pythagorean identities to solve equations."));

            tempList.add(mkCourseModel("M1260.31",
                    "Derive identities from the cosine sum identity."));
            tempList.add(mkCourseModel("M1260.32",
                    "Find exact values with the cosine sum identity."));
            tempList.add(mkCourseModel("M1260.33",
                    "Derive identities from the cosine of a difference identity."));
            tempList.add(mkCourseModel("M1260.34",
                    "Find exact values with the cosine of a difference identity."));
            tempList.add(mkCourseModel("M1260.35",
                    "Derive identities from the sine sum identity."));
            tempList.add(mkCourseModel("M1260.36",
                    "Find exact values with the sine sum identity."));
            tempList.add(mkCourseModel("M1260.37",
                    "Derive identities with the sine of a difference identity."));
            tempList.add(mkCourseModel("M1260.38",
                    "Find exact values with the sine of a difference identity."));
            tempList.add(mkCourseModel("M1260.39",
                    "Derive identities from the tangent sum and difference identities."));
            tempList.add(mkCourseModel("M1260.310",
                    "Find exact values with the tangent sum and difference identities."));

            tempList.add(mkCourseModel("M1260.41",
                    "Derive identities using the double angle identity for sine."));
            tempList.add(mkCourseModel("M1260.42",
                    "Solve problems with the double angle identity for sine."));
            tempList.add(mkCourseModel("M1260.43",
                    "Derive the double angle identity for cosine and tangent."));
            tempList.add(mkCourseModel("M1260.44",
                    "Solve problems with the double angle identity for cosine "
                            + "and tangent."));
            tempList.add(mkCourseModel("M1260.45",
                    "Derive the half angle formulas."));
            tempList.add(mkCourseModel("M1260.46",
                    "Solve problems using the half angle identities."));
            tempList.add(mkCourseModel("M1260.47",
                    "Solve equations using the double angles."));
            tempList.add(mkCourseModel("M1260.48",
                    "Solve equations using half angles."));
            tempList.add(mkCourseModel("M1260.49",
                    "Solve equations I."));
            tempList.add(mkCourseModel("M1260.410",
                    "Solve equations II."));

            // MATH 100L (Calculus Review)
            tempList.add(mkCourseModel("M160R.11",
                    "Algebra and Trigonometry Review"));
            tempList.add(mkCourseModel("M160R.12",
                    "Limits"));
            tempList.add(mkCourseModel("M160R.13",
                    "Continuity"));

            tempList.add(mkCourseModel("M160R.21",
                    "Derivative Function"));
            tempList.add(mkCourseModel("M160R.22",
                    "Derivative Rules"));
            tempList.add(mkCourseModel("M160R.23",
                    "Extreme Values and Critical Points"));

            tempList.add(mkCourseModel("M160R.31",
                    "Antiderivatives and the Indefinite Integral"));
            tempList.add(mkCourseModel("M160R.32",
                    "Finite Sums and the Definite Integral"));
            tempList.add(mkCourseModel("M160R.33",
                    "Substitution"));

            // MATH 160
            tempList.add(mkCourseModel("M160.11",
                    "Average rate of change"));
            tempList.add(mkCourseModel("M160.12",
                    "Instantaneous rate of change, slope of a curve at a point"));
            tempList.add(mkCourseModel("M160.13",
                    "Limits"));
            tempList.add(mkCourseModel("M160.14",
                    "Dealing with zero denominators"));
            tempList.add(mkCourseModel("M160.15",
                    "The sandwich theorem"));
            tempList.add(mkCourseModel("M160.16",
                    "Formal definition of a limit"));
            tempList.add(mkCourseModel("M160.17",
                    "One-sided limits"));
            tempList.add(mkCourseModel("M160.18",
                    "Limts at infinity - asymptotes"));

            tempList.add(mkCourseModel("M160.21",
                    "Definition of a continuous function"));
            tempList.add(mkCourseModel("M160.22",
                    "Properties of continuous functions"));

            tempList.add(mkCourseModel("M160.31",
                    "Definition of the derivative"));
            tempList.add(mkCourseModel("M160.32",
                    "The derivative as a function"));
            tempList.add(mkCourseModel("M160.33",
                    "Differentiability"));
            tempList.add(mkCourseModel("M160.34",
                    "Derivatives of constants, powers of <em>x</em>"));
            tempList.add(mkCourseModel("M160.35",
                    "Derivatives of sums and differences"));
            tempList.add(mkCourseModel("M160.36",
                    "Derivatives of products and quotients"));
            tempList.add(mkCourseModel("M160.37",
                    "Second and higher derivatives"));
            tempList.add(mkCourseModel("M160.38",
                    "Derivatives of trigonometric functions"));
            tempList.add(mkCourseModel("M160.39",
                    "The chain rule"));
            tempList.add(mkCourseModel("M160.310",
                    "Implicit differentiation"));
            tempList.add(mkCourseModel("M160.311",
                    "Related rates"));
            tempList.add(mkCourseModel("M160.312",
                    "Differentials"));
            tempList.add(mkCourseModel("M160.313",
                    "Estimating using linearizations and sensitivity"));

            tempList.add(mkCourseModel("M160.41",
                    "Finding extrema and critical points of functions"));
            tempList.add(mkCourseModel("M160.42",
                    "The mean-value theorem"));
            tempList.add(mkCourseModel("M160.43",
                    "Showing increasing/decreasing behavior of a function"));
            tempList.add(mkCourseModel("M160.44",
                    "Concavity and inflection points"));
            tempList.add(mkCourseModel("M160.45",
                    "Curve sketching"));
            tempList.add(mkCourseModel("M160.46",
                    "Optimization"));
            tempList.add(mkCourseModel("M160.47",
                    "Newton's method"));
            tempList.add(mkCourseModel("M160.48",
                    "Antiderivatives"));

            tempList.add(mkCourseModel("M160.51",
                    "Estimating area using rectangles"));
            tempList.add(mkCourseModel("M160.52",
                    "Signed area and average values"));
            tempList.add(mkCourseModel("M160.53",
                    "Sigma notation and Riemann sums"));
            tempList.add(mkCourseModel("M160.54",
                    "Definite integrals"));
            tempList.add(mkCourseModel("M160.55",
                    "Rules for definite integrals"));
            tempList.add(mkCourseModel("M160.56",
                    "The fundamental theorem of Calculus"));
            tempList.add(mkCourseModel("M160.57",
                    "Indefinite integrals"));
            tempList.add(mkCourseModel("M160.58",
                    "Substitution methods"));
            tempList.add(mkCourseModel("M160.59",
                    "Integrals of even and odd functions"));
            tempList.add(mkCourseModel("M160.510",
                    "Area between curves"));

            tempList.add(mkCourseModel("M160.61",
                    "Volumes using cross sections"));
            tempList.add(mkCourseModel("M160.62",
                    "The disc and washer methods"));
            tempList.add(mkCourseModel("M160.63",
                    "The shell method"));
            tempList.add(mkCourseModel("M160.64",
                    "Average values"));
            tempList.add(mkCourseModel("M160.65",
                    "Arc lengths"));
            tempList.add(mkCourseModel("M160.66",
                    "Surface area of surfaces of revolution"));
            tempList.add(mkCourseModel("M160.67",
                    "Work and fluid forces"));
            tempList.add(mkCourseModel("M160.68",
                    "Moments and centers of math"));

            // MATH 161
            tempList.add(mkCourseModel("Math.Calc.TransFxn.01",
                    "Inverse functions"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.02",
                    "Derivatives of inverse functions"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.03",
                    "Inverse trigonometric functions and their derivatives"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.04",
                    "The natural logarithm"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.05",
                    "The exponential functions"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.06",
                    "Indeterminant forms and L'H&ocirc;pital's rule"));
            tempList.add(mkCourseModel("Math.Calc.TransFxn.07",
                    "Relative rates of growth"));

            tempList.add(mkCourseModel("Math.Calc.IntegTech.01",
                    "Integration by parts"));
            tempList.add(mkCourseModel("Math.Calc.IntegTech.02",
                    "Trigonometric integrals"));
            tempList.add(mkCourseModel("Math.Calc.IntegTech.03",
                    "Trigonometric substitutions"));
            tempList.add(mkCourseModel("Math.Calc.IntegTech.04",
                    "Integration of rational functions using partial fractions"));
            tempList.add(mkCourseModel("Math.Calc.IntegTech.05",
                    "Strategies for integration"));
            tempList.add(mkCourseModel("Math.Calc.IntegTech.06",
                    "Integral tables and computer algebra systems"));

            tempList.add(mkCourseModel("Math.Calc.DEIntro.01",
                    "Modeling with differential equations"));
            tempList.add(mkCourseModel("Math.Calc.DEIntro.02",
                    "Solutions, slope fields, and Euler's method"));
            tempList.add(mkCourseModel("Math.Calc.DEIntro.03",
                    "First-order linear differential equations"));
            tempList.add(mkCourseModel("Math.Calc.DEIntro.04",
                    "Separable differential equations"));
            tempList.add(mkCourseModel("Math.Calc.DEIntro.05",
                    "Exponential growth and decay"));

            tempList.add(mkCourseModel("Math.Calc.ImpropInt.01",
                    "Improper integrals"));
            tempList.add(mkCourseModel("Math.Calc.ImpropInt.02",
                    "Convergence of improper integrals"));

            tempList.add(mkCourseModel("Math.Calc.SeqSer.01",
                    "Sequences"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.02",
                    "Convergence of sequences"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.03",
                    "Infinite series"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.04",
                    "Convergence of infinite series"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.05",
                    "Geometric series"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.06",
                    "The integral test"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.07",
                    "Comparison tests for series"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.08",
                    "The ratio and root tests"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.09",
                    "Alternating series and absolute convergence"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.10",
                    "Power series and power series convergence"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.11",
                    "Working with power series"));
            tempList.add(mkCourseModel("Math.Calc.SeqSer.12",
                    "Binomial series"));

            tempList.add(mkCourseModel("Math.Calc.TaylorSer.01",
                    "Taylor and Maclaurin series"));
            tempList.add(mkCourseModel("Math.Calc.TaylorSer.02",
                    "Commonly occurring Taylor series"));
            tempList.add(mkCourseModel("Math.Calc.TaylorSer.03",
                    "Building new series from old"));
            tempList.add(mkCourseModel("Math.Calc.TaylorSer.04",
                    "Taylor's formula - error estimates"));
            tempList.add(mkCourseModel("Math.Calc.TaylorSer.05",
                    "Differential equations with Taylor series"));

            tempList.add(mkCourseModel("Math.Calc.ParamPol.01",
                    "Parametrizations of plane curves"));
            tempList.add(mkCourseModel("Math.Calc.ParamPol.02",
                    "Calculus with parametric curves"));
            tempList.add(mkCourseModel("Math.Calc.ParamPol.03",
                    "Polar coordinates"));
            tempList.add(mkCourseModel("Math.Calc.ParamPol.04",
                    "Areas and lengths in polar coordinates"));
            tempList.add(mkCourseModel("Math.Calc.ParamPol.05",
                    "Conic sections in Cartesian and polar coordinates"));

            tempList.add(mkCourseModel("Math.Calc.Complex.01",
                    "Complex numbers and the complex plane"));
            tempList.add(mkCourseModel("Math.Calc.Complex.02",
                    "Euler's formula and De Moivre's theorem"));
            tempList.add(mkCourseModel("Math.Calc.Complex.03",
                    "The fundamental theorem of algebra and complex roots"));

            tempList.add(mkRefreshModel("Math.Calc.Refresh.01.Differentiation",
                    "Integration"));
            tempList.add(mkRefreshModel("Math.Calc.Refresh.02.Integration",
                    "Integration"));
        } catch (final IllegalArgumentException ex) {
            Log.warning("Static data for lesson has validation error", ex);
        }

        lessons = new HashMap<>(tempList.size());

        for (final RawLesson lesson : tempList) {
            lessons.put(lesson.lessonId, lesson);
        }

        lessons.put("C41S1", new RawLesson("C41S1", "C", CoreConstants.EMPTY));
        lessons.put("C41S2", new RawLesson("C41S2", "C", CoreConstants.EMPTY));
        lessons.put("C41S3", new RawLesson("C41S3", "C", CoreConstants.EMPTY));
        lessons.put("C42S1", new RawLesson("C42S1", "C", CoreConstants.EMPTY));
        lessons.put("C42S2", new RawLesson("C42S2", "C", CoreConstants.EMPTY));
        lessons.put("C42S3", new RawLesson("C42S3", "C", CoreConstants.EMPTY));
        lessons.put("C43S1", new RawLesson("C43S1", "C", CoreConstants.EMPTY));
        lessons.put("C43S2", new RawLesson("C43S2", "C", CoreConstants.EMPTY));
        lessons.put("C43S3", new RawLesson("C43S3", "C", CoreConstants.EMPTY));
        lessons.put("C44S1", new RawLesson("C44S1", "C", CoreConstants.EMPTY));
        lessons.put("C44S2", new RawLesson("C44S2", "C", CoreConstants.EMPTY));
        lessons.put("C44S3", new RawLesson("C44S3", "C", CoreConstants.EMPTY));
        lessons.put("C45S1", new RawLesson("C45S1", "C", CoreConstants.EMPTY));
        lessons.put("C45S2", new RawLesson("C45S2", "C", CoreConstants.EMPTY));
        lessons.put("C45S3", new RawLesson("C45S3", "C", CoreConstants.EMPTY));
        lessons.put("C46S1", new RawLesson("C46S1", "C", CoreConstants.EMPTY));
        lessons.put("C46S2", new RawLesson("C46S2", "C", CoreConstants.EMPTY));
        lessons.put("C46S3", new RawLesson("C46S3", "C", CoreConstants.EMPTY));
        lessons.put("C47S1", new RawLesson("C47S1", "C", CoreConstants.EMPTY));
        lessons.put("C47S2", new RawLesson("C47S2", "C", CoreConstants.EMPTY));
        lessons.put("C47S3", new RawLesson("C47S3", "C", CoreConstants.EMPTY));
        lessons.put("C48S1", new RawLesson("C48S1", "C", CoreConstants.EMPTY));
        lessons.put("C48S2", new RawLesson("C48S2", "C", CoreConstants.EMPTY));
        lessons.put("C48S3", new RawLesson("C48S3", "C", CoreConstants.EMPTY));
        lessons.put("C49S1", new RawLesson("C49S1", "C", CoreConstants.EMPTY));
        lessons.put("C49S2", new RawLesson("C49S2", "C", CoreConstants.EMPTY));
        lessons.put("C49S3", new RawLesson("C49S3", "C", CoreConstants.EMPTY));
        lessons.put("C50S1", new RawLesson("C50S1", "C", CoreConstants.EMPTY));
        lessons.put("C50S2", new RawLesson("C50S2", "C", CoreConstants.EMPTY));
        lessons.put("C50S3", new RawLesson("C50S3", "C", CoreConstants.EMPTY));
        lessons.put("C45EX", new RawLesson("C45EX", "C", CoreConstants.EMPTY));
        lessons.put("C50EX", new RawLesson("C50EX", "C", CoreConstants.EMPTY));

        lessons.put("C51S1", new RawLesson("C51S1", "C", CoreConstants.EMPTY));
        lessons.put("C51S2", new RawLesson("C51S2", "C", CoreConstants.EMPTY));
        lessons.put("C51S3", new RawLesson("C51S3", "C", CoreConstants.EMPTY));
        lessons.put("C52S1", new RawLesson("C52S1", "C", CoreConstants.EMPTY));
        lessons.put("C52S2", new RawLesson("C52S2", "C", CoreConstants.EMPTY));
        lessons.put("C52S3", new RawLesson("C52S3", "C", CoreConstants.EMPTY));
        lessons.put("C53S1", new RawLesson("C53S1", "C", CoreConstants.EMPTY));
        lessons.put("C53S2", new RawLesson("C53S2", "C", CoreConstants.EMPTY));
        lessons.put("C53S3", new RawLesson("C53S3", "C", CoreConstants.EMPTY));
        lessons.put("C54S1", new RawLesson("C54S1", "C", CoreConstants.EMPTY));
        lessons.put("C54S2", new RawLesson("C54S2", "C", CoreConstants.EMPTY));
        lessons.put("C54S3", new RawLesson("C54S3", "C", CoreConstants.EMPTY));
        lessons.put("C55S1", new RawLesson("C55S1", "C", CoreConstants.EMPTY));
        lessons.put("C55S2", new RawLesson("C55S2", "C", CoreConstants.EMPTY));
        lessons.put("C55S3", new RawLesson("C55S3", "C", CoreConstants.EMPTY));
        lessons.put("C56S1", new RawLesson("C56S1", "C", CoreConstants.EMPTY));
        lessons.put("C56S2", new RawLesson("C56S2", "C", CoreConstants.EMPTY));
        lessons.put("C56S3", new RawLesson("C56S3", "C", CoreConstants.EMPTY));
        lessons.put("C57S1", new RawLesson("C57S1", "C", CoreConstants.EMPTY));
        lessons.put("C57S2", new RawLesson("C57S2", "C", CoreConstants.EMPTY));
        lessons.put("C57S3", new RawLesson("C57S3", "C", CoreConstants.EMPTY));
        lessons.put("C58S1", new RawLesson("C58S1", "C", CoreConstants.EMPTY));
        lessons.put("C58S2", new RawLesson("C58S2", "C", CoreConstants.EMPTY));
        lessons.put("C58S3", new RawLesson("C58S3", "C", CoreConstants.EMPTY));
        lessons.put("C59S1", new RawLesson("C59S1", "C", CoreConstants.EMPTY));
        lessons.put("C59S2", new RawLesson("C59S2", "C", CoreConstants.EMPTY));
        lessons.put("C59S3", new RawLesson("C59S3", "C", CoreConstants.EMPTY));
        lessons.put("C60S1", new RawLesson("C60S1", "C", CoreConstants.EMPTY));
        lessons.put("C60S2", new RawLesson("C60S2", "C", CoreConstants.EMPTY));
        lessons.put("C60S3", new RawLesson("C60S3", "C", CoreConstants.EMPTY));
        lessons.put("C55EX", new RawLesson("C55EX", "C", CoreConstants.EMPTY));
        lessons.put("C60EX", new RawLesson("C60EX", "C", CoreConstants.EMPTY));
    }

    /**
     * Builds a model with fixed fields.
     *
     * @param lessonId the lesson ID
     * @param title    the lesson title
     * @return the new model
     */
    private static RawLesson mkCourseModel(final String lessonId, final String title) {

        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID may not be null");
        }

        return new RawLesson(lessonId, "C", title);
    }

    /**
     * Builds a refresher model with fixed fields.
     *
     * @param lessonId the lesson ID
     * @param title    the lesson title
     * @return the new model
     */
    private static RawLesson mkRefreshModel(final String lessonId, final String title) {

        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID may not be null");
        }

        return new RawLesson(lessonId, "R", title);
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawLesson record) {

        Log.warning("Unable to insert lesson record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawLesson record) {

        Log.warning("Unable to delete lesson record: no underlying table.");

        return false;
    }

    /**
     * Queries for a single lesson.
     *
     * @param lessonId the lesson ID
     * @return the lesson; null if none found
     */
    public static RawLesson query(final String lessonId) {

        return lessons.get(lessonId);
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawLesson> queryAll(final Cache cache) {

        final Collection<RawLesson> values = lessons.values();

        return new ArrayList<>(values);
    }
}
