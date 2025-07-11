package dev.mathops.web.host.precalc.course.data;

import dev.mathops.db.old.rawrecord.RawRecordConstants;

/**
 * Static {@code CourseData} objects for the Math 126 course.
 */
public enum Math126 {
    ;

    /** The MATH 126 course. */
    public static final CourseData MATH_126;

    static {
        MATH_126 = buildMath126();
    }

    /**
     * Creates the MATH 126 course.
     *
     * @return the course data container
     */
    private static CourseData buildMath126() {

        final CourseData m126 = new CourseData(RawRecordConstants.MATH126, RawRecordConstants.MATH126,
                "Analytic Trigonometry", "M126");

        // Module 1

        final ModuleData m126m1 = m126.addModule(1, "Fundamental Trigonometric Identities", "TR09_SR_HW",
                "c51-thumb.png");
        m126m1.skillsReview.addExBlock("Multiplying binomials")
                .addEx("TR09_SR1_01", "Multiply three binomials");
        m126m1.skillsReview.addExBlock("Properties of roots")
                .addEx("TR09_SR2_01", "Expand a product of factors that involve roots and powers");
        m126m1.skillsReview.addExBlock("Imaginary numbers")
                .addEx("TR09_SR3_01", "Perform arithmetic with the imaginary unit <i>i</i>");
        m126m1.skillsReview.addExBlock("Definition of the trigonometric functions")
                .addEx("TR09_SR4_01", "Write definitions of the trigonometric functions, and identify angles for "
                        + "which they are undefined");
        m126m1.skillsReview.addExBlock("Pythagorean theorem")
                .addEx("TR09_SR5_01", "Solve for side lengths using the Pythagorean theorem");

        final LearningTargetData m126m1t1 = m126m1.addLearningTarget(1, 1, "1.1", "TR09_ST1_HW",
                "I can work with general identities, including:",
                "explain what makes an equation an identity, in terms of both algebraic and graphical representations.",
                "verify identities", "identify the domain of validity of an identity",
                "apply an identity to change the form of an expression");
        m126m1t1.addExBlock("Definition of an identity")
                .addEx("TR09_ST1A_01a", "Indicate which statements are true about identities");
        m126m1t1.addExBlock("Graphical representation of an identity")
                .addEx("TR09_ST1A_02a", "Explain what makes an equation an identity, algebraically and graphically");
        m126m1t1.addExBlock("Verifying identities")
                .addEx("TR09_ST1B_01a", "Verify a difference of squares identity and infer its domain of validity");
        m126m1t1.addExBlock("Verifying identities")
                .addEx("TR09_ST1B_02a",
                        "Verify an identity for products of sums of squares and infer its domain of validity")
                .addEx("TR09_ST1B_02b",
                        "Verify an identity for sums of fourth powers and infer its domain of validity");
        m126m1t1.addExBlock("Find domain of validity with square roots")
                .addEx("TR09_ST1B_03a", "Verify identities involving <i>i</i> and find their domains of validity");
        m126m1t1.addExBlock("Find domain of validity with imaginary numbers")
                .addEx("TR09_ST1B_04a",
                        "Verify an identity involving roots of products and find its domain of validity")
                .addEx("TR09_ST1B_04b",
                        "Verify an identity involving roots of quotients and find its domain of validity");
        m126m1t1.addExBlock("Apply identities to change the form of an expression")
                .addEx("TR09_ST1C_01a", "Apply an identity to rewrite an expression")
                .addEx("TR09_ST1C_01b", "Apply an identity to rewrite an expression");
        m126m1t1.addExBlock("Apply identities to factorize an expression")
                .addEx("TR09_ST1C_02a", "Apply an identity to factorize an expression and find its roots")
                .addEx("TR09_ST1C_02b", "Apply an identity to factorize an expression and find its roots");

        final LearningTargetData m126m1t2 = m126m1.addLearningTarget(1, 2, "1.2", "TR09_ST2_HW",
                "I can recall the various forms of the fundamental trigonometric identities, including:",
                "the definitions of tangent, cotangent, secant, and cosecant in terms of sine and cosine",
                "the even/odd identities for all six trigonometric functions, and their graphical interpretation",
                "the cofunction identities for the trigonometric functions, and their graphical interpretation",
                "the Pythagorean identities for the trigonometric functions, and their relationships with right "
                        + "triangles");
        m126m1t2.addExBlock("Definitions of trig functions as identities")
                .addEx("TR09_ST2A_01a",
                        "Interpret the definition of tangent as an identity and find its domain of validity")
                .addEx("TR09_ST2A_02a",
                        "Interpret the definition of cotangent as an identity and find its domain of validity")
                .addEx("TR09_ST2A_03a",
                        "Interpret the definition of secant as an identity and find its domain of validity")
                .addEx("TR09_ST2A_04a",
                        "Interpret the definition of cosecant as an identity and find its domain of validity");
        m126m1t2.addExBlock("Even/odd identities for sine and cosine")
                .addEx("TR09_ST2B_01a",
                        "Discover the even/odd identities for sine and cosine, and find their domains of validity");
        m126m1t2.addExBlock("Even/odd identities for tangent and cotangent")
                .addEx("TR09_ST2B_02a",
                        "Discover the even/odd identities for tangent and cotangent, and find their domains of "
                                + "validity");
        m126m1t2.addExBlock("Even/odd identities for secant and cosecant")
                .addEx("TR09_ST2B_03a",
                        "Discover the even/odd identities for secant and cosecant, and find their domains of validity");
        m126m1t2.addExBlock("Cofunction identities for sine and cosine")
                .addEx("TR09_ST2C_01a",
                        "Write cofunction identities for sine and cosine, describe them in terms of graphs, and find "
                                + "domains of validity");
        m126m1t2.addExBlock("Cofunction identities for tangent and cotangent")
                .addEx("TR09_ST2C_02a",
                        "Write cofunction identities for tangent and cotangent, describe them in terms of graphs, and "
                                + "find domains of validity");
        m126m1t2.addExBlock("Cofunction identities for secant and cosecant")
                .addEx("TR09_ST2C_03a",
                        "Write cofunction identities for secant and cosecant, describe them in terms of graphs, and "
                                + "find domains of validity");
        m126m1t2.addExBlock("Pythagorean identity with sine and cosine")
                .addEx("TR09_ST2D_01a",
                        "Write the Pythagorean identity for sine and cosine, and find its domain of validity");
        m126m1t2.addExBlock("Pythagorean identity with tangent and secant")
                .addEx("TR09_ST2D_02a",
                        "Write the Pythagorean identity for tangent and secant, and find its domain of validity");
        m126m1t2.addExBlock("Pythagorean identity with cotangent and cosecant")
                .addEx("TR09_ST2D_03a",
                        "Write the Pythagorean identity for cotangent and cosecant, and find its domain of validity");

        final LearningTargetData m126m1t3 = m126m1.addLearningTarget(1, 3, "1.3", "TR09_ST3_HW",
                "I can apply fundamental trigonometric identities to rewrite and simplify expression, including",
                "simplifying by using co-function identities",
                "simplifying using even/odd identities",
                "simplifying using Pythagorean identities");
        m126m1t3.addExBlock("Simplifications using definitions")
                .addEx("TR09_ST3A_01a",
                        "Use definitions of trigonometric functions as identities to simplify expressions")
                .addEx("TR09_ST3A_02a",
                        "Use definitions of trigonometric functions as identities to simplify expressions");
        m126m1t3.addExBlock("Applying even/odd identities")
                .addEx("TR09_ST3B_01a", "Use even/odd identities to simplify expressions - sine")
                .addEx("TR09_ST3B_02a", "Use even/odd identities to simplify expressions - cosine")
                .addEx("TR09_ST3B_03a", "Use even/odd identities to simplify expressions - tangent")
                .addEx("TR09_ST3B_04a", "Use even/odd identities to simplify expressions - cotangent")
                .addEx("TR09_ST3B_05a", "Use even/odd identities to simplify expressions - secant")
                .addEx("TR09_ST3B_06a", "Use even/odd identities to simplify expressions - cosecant");
        m126m1t3.addExBlock("Applying cofunction identities")
                .addEx("TR09_ST3C_01a", "Use cofunction identities to simplify expressions - sine")
                .addEx("TR09_ST3C_02a", "Use cofunction identities to simplify expressions - cosine")
                .addEx("TR09_ST3C_03a", "Use cofunction identities to simplify expressions - tangent")
                .addEx("TR09_ST3C_04a", "Use cofunction identities to simplify expressions - cotangent")
                .addEx("TR09_ST3C_05a", "Use cofunction identities to simplify expressions - secant")
                .addEx("TR09_ST3C_06a", "Use cofunction identities to simplify expressions - cosecant");
        m126m1t3.addExBlock("Applying the Pythagorean identity with sine and cosine")
                .addEx("TR09_ST3D_01a", "Use Pythagorean identity for sine and cosine to simplify an expression");
        m126m1t3.addExBlock("Applying the Pythagorean identity with tangent and secant")
                .addEx("TR09_ST3D_02a", "Use Pythagorean identities to simplify an expression");

        // Module 2

        final ModuleData m126m2 = m126.addModule(2, "Sum and Difference Identities", "TR10_SR_HW", "c52-thumb.png");

        m126m2.skillsReview.addExBlock("Shapes of graphs of the trigonometric functions")
                .addEx("TR10_SR1_01", "Match graphs to trigonometric functions and recall domain and range of each");
        m126m2.skillsReview.addExBlock("Trigonometric functions related to point where angle's terminal ray "
                        + "meets unit circle, and SOH-CAH-TOA relationships")
                .addEx("TR10_SR2_01", "Given drawing, label quantities represented by each trigonometric function");
        m126m2.skillsReview.addExBlock("Add or subtract two rational expressions by finding a common denominator")
                .addEx("TR10_SR3_01", "Add and subtract rational expressions");
        m126m2.skillsReview.addExBlock("Recall values of sine and cosine for common angles")
                .addEx("TR10_SR4_01", "Recall exact values of sine and cosine for common Quadrant I angles");
        m126m2.skillsReview.addExBlock("The distance formula")
                .addEx("TR10_SR5_01", "Calculate the distance between two points");

        final LearningTargetData m126m2t1 = m126m2.addLearningTarget(2, 1, "2.1", "TR10_ST1_HW",
                "I can interpret and apply the sum and difference identities, including:",
                "interpret the sum and difference identities for sine and cosine graphically or in the context of " +
                        "rotated right triangles",
                "apply the sum and difference identities to rewrite or simplify expressions ",
                "use the sum or difference identities to verify identities",
                "derive the cofunction and similar identities using the sum and difference identities");
        m126m2t1.addExBlock("Interpret sums and differences of angles graphically")
                .addEx("TR10_ST1A_01", "Given a graph of sine with two angles marked, interpret sums and differences "
                        + "of angles, and describe how values of sine changes in response to changes in either");
        m126m2t1.addExBlock("Application: Right triangles in non-standard positions")
                .addEx("TR10_ST1A_02", "Find lengths in a right triangle in non-standard position");
        m126m2t1.addExBlock("Simplify expression using the sine difference identity")
                .addEx("TR10_ST1B_01", "Use the sine difference of angles identity to simplify an expression");
        m126m2t1.addExBlock("Simplify expression using the cosine sum identity")
                .addEx("TR10_ST1B_02", "Use the cosine sum of angles identity to simplify an expression");
        m126m2t1.addExBlock("Verify identities using the sine and cosine sum and difference identities")
                .addEx("TR10_ST1C_01", "Use the sine sum and difference of angles identities to verify an identity")
                .addEx("TR10_ST1C_02", "Use the sine difference of angles identity to verify an identity");
        m126m2t1.addExBlock("Derive cofunction-like identities")
                .addEx("TR10_ST1D_01", "Use the sine sum of angles identity to derive an identity")
                .addEx("TR10_ST1D_02", "Use the cosine difference of angles identity to derive an identity");

        final LearningTargetData m126m2t2 = m126m2.addLearningTarget(2, 2, "2.2", "TR10_ST2_HW",
                "I can use sum and difference identities to evaluate expressions, including:",
                "trigonometric functions at angles that are sums or differences of angles for which those function "
                        + "values are known",
                "difference quotients involving trigonometric functions",
                "solve application problems using sum and difference identities");
        m126m2t2.addExBlock("Evaluate expressions using sum and difference identities for sine and cosine")
                .addEx("TR10_ST2A_01",
                        "Compute sine of a value that can be expressed as a difference of common angles")
                .addEx("TR10_ST2A_02",
                        "Compute cosine of a value that can be expressed as a difference of common angles");
        m126m2t2.addExBlock(
                        "Evaluate expressions using sum and difference identities for other trigonometric functions")
                .addEx("TR10_ST2A_03",
                        "Compute tangent of a value that can be expressed as a sum of common angles");
        m126m2t2.addExBlock("Analyze a difference quotient")
                .addEx("TR10_ST2B_01", "Evaluate a difference quotient for several values of its denominator that "
                        + "approach zero and interpret behavior");
        m126m2t2.addExBlock("Solve application problems")
                .addEx("TR10_ST2C_01", "Find distance between tips of two arms that move on a central pivot");

        final LearningTargetData m126m2t3 = m126m2.addLearningTarget(2, 3, "2.3", "TR10_ST3_HW",
                "I can apply product-to-sum and sum-to-product identities, including:",
                "use these identities to rewrite or simplify expressions",
                "use them to verify identities",
                "solve trigonometric equations using sum to product identities");
        m126m2t3.addExBlock("Product to sum identities")
                .addEx("TR10_ST3A_01a", "Rewrite an expression containing a product of sines as a sum")
                .addEx("TR10_ST3A_01b", "Rewrite an expression containing a product of cosines as a sum")
                .addEx("TR10_ST3A_01c", "Rewrite an expression containing a product of a sine and a cosine as a sum");
        m126m2t3.addExBlock("Sum to product identities")
                .addEx("TR10_ST3A_02a", "Rewrite an expression with a sum of sines as a product")
                .addEx("TR10_ST3A_02b", "Rewrite an expression with a sum of cosines as a product");
        m126m2t3.addExBlock("Verifying identities using product to sum and sum to product identities")
                .addEx("TR10_ST3B_01", "Verify an identity involving sums of sines and cosines");
        m126m2t3.addExBlock("Factorizing equations using sum to product identities")
                .addEx("TR10_ST3C_01a", "Find solutions to an equation involving a sum of sines by converting to a "
                        + "product and factorizing")
                .addEx("TR10_ST3C_01b", "Find solutions to an equation involving a sum of cosines by converting to a "
                        + "product and factorizing");

        // Module 3

        final ModuleData m126m3 = m126.addModule(3, "Multiple-Angle and Half-Angle Identities", "TR11_SR_HW",
                "c53-thumb.png");

        m126m3.skillsReview.addExBlock("Piecewise Functions")
                .addEx("TR11_SR1_01", "Graph a piecewise function and evaluate.");
        m126m3.skillsReview.addExBlock("Squares of square roots")
                .addEx("TR11_SR2_01",
                        "Graph square root functions, then their square, and explore relationships between them");
        m126m3.skillsReview.addExBlock("Cosine, Sine, and coordinates on circles")
                .addEx("TR11_SR3_01", "Find coordinates of points on circles using cosine and sine");
        m126m3.skillsReview.addExBlock("Pythagorean identity for sine and cosine")
                .addEx("TR11_SR4_01", "Use the Pythagorean identity to simplify an expression");
        m126m3.skillsReview.addExBlock("Definitions of trigonometric functions")
                .addEx("TR11_SR5_01",
                        "Use definitions of trigonometric functions to expand and simplify an expression");

        final LearningTargetData m126m3t1 = m126m3.addLearningTarget(3, 1, "3.1", "TR11_ST1_HW",
                "I can interpret and apply double- and multiple-angle identities, including:",
                "interpret double- and multiple-angle identities for sine and cosine graphically or in the context of "
                        + "arrays of right triangles",
                "apply the double- and multiple-angle identities to rewrite and simplify expressions",
                "use the double- and multiple-angle identities to verify identities");
        m126m3t1.addExBlock("Interpret double-angle and multiple-angle quantities graphically")
                .addEx("TR11_ST1A_01", "Examine doubled and multiple angles and how they change as angle changes");
        m126m3t1.addExBlock("Application: arrays of equal angles")
                .addEx("TR11_ST1A_02", "Find point coordinates on a circle in terms of multiples of angles");
        m126m3t1.addExBlock("Using the double-angle identity for sine")
                .addEx("TR11_ST1B_01", "Simplify an expression using the double-angle identity for sine");
        m126m3t1.addExBlock("Using the double-angle identity for cosine")
                .addEx("TR11_ST1B_02", "Simplify an expression using the double-angle identity for cosine");
        m126m3t1.addExBlock("Using the triple-angle identity for cosine")
                .addEx("TR11_ST1B_03", "Simplify an expression using the triple-angle identity for cosine");
        m126m3t1.addExBlock("Using the quadruple-angle identity for sine")
                .addEx("TR11_ST1B_04", "Simplify an expression using the quadruple-angle identity for sine");
        m126m3t1.addExBlock("Using double-and multiple-angle identities to verify identities")
                .addEx("TR11_ST1C_01", "Given appropriate double-angle identities, verify another identity");

        final LearningTargetData m126m3t2 = m126m3.addLearningTarget(3, 2, "3.2", "TR11_ST2_HW",
                "I can use half-angle identities, including:",
                "interpret half-angle identities for sine and cosine graphically or in the context of bisected angles",
                "apply the half-angle identities to rewrite and simplify expressions",
                "use the half-angle identities to verify identities");
        m126m3t2.addExBlock("Interpret half-angle quantities graphically")
                .addEx("TR11_ST2A_01", "Find point coordinates on a circle in terms halved angles");
        m126m3t2.addExBlock("Using the half-angle identity for sine")
                .addEx("TR11_ST2B_01", "Simplify an expression using the half-angle identity for sine");
        m126m3t2.addExBlock("Using the half-angle identity for cosine")
                .addEx("TR11_ST2C_01", "Simplify an expression using the half-angle identity for cosine");
        m126m3t2.addExBlock("Using half-angle identities to verify identities")
                .addEx("TR11_ST2C_02", "Given appropriate half-angle identities, verify another identity");

        final LearningTargetData m126m3t3 = m126m3.addLearningTarget(3, 3, "3.3", "TR11_ST3_HW",
                "I can apply double-, multiple-, and half-angle identities, including:",
                "evaluate trigonometric functions at fractions of well-known angles",
                "perform power reduction",
                "solve applied problems involving arrays of equal angles");
        m126m3t3.addExBlock("Evaluate sine and cosine at fractions of angles")
                .addEx("TR11_ST3A_01a", "Find exact values of sines of fractions of common angles")
                .addEx("TR11_ST3A_01b", "Find exact values of cosines of fractions of common angles");
        m126m3t3.addExBlock("Evaluate sine and cosine at 3/2 of an angle")
                .addEx("TR11_ST3A_02a", "Find the exact value for the sine of 3/2 of a common angle")
                .addEx("TR11_ST3A_02b", "Find the exact value for the cosine of 3/2 of a common angle");
        m126m3t3.addExBlock("Perform power reduction")
                .addEx("TR11_ST3B_01", "Rewrite expressions with powers of sine and cosine to reduce exponents");
        m126m3t3.addExBlock("Application: Gear train")
                .addEx("TR11_ST3C_01", "Solve application problem involving gear rotation");
        m126m3t3.addExBlock("Application: Spiral staircase")
                .addEx("TR11_ST3C_02", "Solve application problem involving design of a spiral staircase");

        // Module 4

        final ModuleData m126m4 = m126.addModule(4, "Trigonometric Equations", "TR12_SR_HW", "c54-thumb.png");

        m126m4.skillsReview.addExBlock("Cofunction identities")
                .addEx("TR12_SR1_01", "Recall the cofunction identities");
        m126m4.skillsReview.addExBlock("Pythagorean identities")
                .addEx("TR12_SR2_01", "Recall the Pythagorean trigonometric identities");
        m126m4.skillsReview.addExBlock("Right triangle relationships")
                .addEx("TR12_SR3_01",
                        "Recall the relationship between right triangle side lengths and the sine of an angle")
                .addEx("TR12_SR3_02",
                        "Recall the relationship between right triangle side lengths and the cosine of an angle")
                .addEx("TR12_SR3_03",
                        "Recall the relationship between right triangle side lengths and the tangent of an angle");
        m126m4.skillsReview.addExBlock("The quadratic formula")
                .addEx("TR12_SR4_01", "Use the quadratic formula to find the roots of a quadratic")
                .addEx("TR12_SR4_02", "Use the quadratic formula to find the roots of a quadratic")
                .addEx("TR12_SR4_03", "Use the quadratic formula to find the roots of a quadratic");
        m126m4.skillsReview.addExBlock("Roots of factored equations")
                .addEx("TR12_SR5_01", "Find roots of an equation where a factored expression is equal to zero");
        m126m4.skillsReview.addExBlock("The inverse trigonometric functions")
                .addEx("TR12_SR6_01", "Sketch the graphs of the inverse trigonometric functions");

        final LearningTargetData m126m4t1 = m126m4.addLearningTarget(4, 1, "4.1", "TR12_ST1_HW",
                "I can apply identities to find all solutions to trigonometric equations. Specifically, I can:",
                "solve by applying cofunction identities",
                "solve by applying Pythagorean identities",
                "solve by applying half-angle identities",
                "solve by applying double- or multiple-angle identities",
                "solve using the quadratic formula",
                "choose an appropriate technique for a given equation and apply to solve");
        m126m4t1.addExBlock("Solving with Pythagorean identities")
                .addEx("TR12_ST1B_01", "Find all solutions of a given equation")
                .addEx("TR12_ST1B_02", "Find all solutions of a given equation");
        m126m4t1.addExBlock("Solving with half-angle identities")
                .addEx("TR12_ST1C_01", "Find all solutions iof a given equation")
                .addEx("TR12_ST1C_02", "Find all solutions iof a given equation")
                .addEx("TR12_ST1C_03", "Find all solutions iof a given equation")
                .addEx("TR12_ST1C_04", "Find all solutions iof a given equation");
        m126m4t1.addExBlock("Solving with double- or multiple-angle identities")
                .addEx("TR12_ST1D_01", "Find all solutions in a specified interval of a given equation")
                .addEx("TR12_ST1D_02", "Find all solutions in a specified interval of a given equation")
                .addEx("TR12_ST1D_03", "Find all solutions in a specified interval of a given equation")
                .addEx("TR12_ST1D_04", "Find all solutions in a specified interval of a given equation");
        m126m4t1.addExBlock("Solving using the quadratic formula")
                .addEx("TR12_ST1E_01", "Find all solutions of a given equation")
                .addEx("TR12_ST1E_02", "Find all solutions of a given equation");
        m126m4t1.addExBlock("Choosing an appropriate technique and solving")
                .addEx("TR12_ST1F_01", "Find all solutions in a specified interval of a given equation")
                .addEx("TR12_ST1F_02", "Find all solutions in a specified interval of a given equation")
                .addEx("TR12_ST1F_03", "Find all solutions of a given equation")
                .addEx("TR12_ST1F_04", "Find all solutions of a given equation");

        final LearningTargetData m126m4t2 = m126m4.addLearningTarget(4, 2, "4.2", "TR12_ST2_HW",
                "I can solve trigonometric equations. Specifically, I can:",
                "find angles that satisfy an equation using inverse trigonometric functions",
                "factor and find roots of expressions by finding roots of factors",
                "interpret the context of a problem to choose correct solution when multiple solutions are possible");
        m126m4t2.addExBlock("Solving using inverse trigonometric functions")
                .addEx("TR12_ST2A_01", "Find all solutions in a specified range of a given equation")
                .addEx("TR12_ST2A_02", "Find all solutions in a specified range of a given equation");
        m126m4t2.addExBlock("Solving by factoring")
                .addEx("TR12_ST2B_01", "Find all solutions in a specified range of a given equation")
                .addEx("TR12_ST2B_02", "Find all solutions in a specified range of a given equation")
                .addEx("TR12_ST2B_03", "Find all solutions in a specified range of a given equation")
                .addEx("TR12_ST2B_04", "Find all solutions in a specified range of a given equation");
        m126m4t2.addExBlock("Applications: sinusoidal models of real-world situations")
                .addEx("TR12_ST2C_01", "A model from Biology")
                .addEx("TR12_ST2C_02", "A model from Ecology");

        final LearningTargetData m126m4t3 = m126m4.addLearningTarget(4, 3, "4.3", "TR12_ST3_HW",
                "I can evaluate compositions of trigonometric and inverse trigonometric functions. Specifically, I "
                        + "can:",
                "evaluate compositions of trigonometric functions and their inverses",
                "create a drawing of a right triangle to help identify relevant relationships between trigonometric "
                        + "function values and side lengths",
                "find exact values of compositions of trigonometric and inverse trigonometric functions");
        m126m4t3.addExBlock("Evaluate compositions of a function after its inverse")
                .addEx("TR12_ST3A_01", "Evaluate tangent of an inverse tangent")
                .addEx("TR12_ST3A_02", "Evaluate sine of an inverse sine")
                .addEx("TR12_ST3A_03", "Evaluate cotangent of an inverse cotangent");
        m126m4t3.addExBlock("Evaluate compositions of an inverse function after the function")
                .addEx("TR12_ST3A_04", "Evaluate inverse cosine of a cosine")
                .addEx("TR12_ST3A_05", "Evaluate inverse sine of a sine")
                .addEx("TR12_ST3A_06", "Evaluate inverse tangent of a tangent");
        m126m4t3.addExBlock("Construct right triangles to help analyze compositions")
                .addEx("TR12_ST3B_01", "Construct a triangle to help evaluate a sine of an inverse tangent")
                .addEx("TR12_ST3B_02", "Construct a triangle to help evaluate a cosine of an inverse tangent")
                .addEx("TR12_ST3B_03", "Construct a triangle to help evaluate a cosine of an inverse sine")
                .addEx("TR12_ST3B_04", "Construct a triangle to help evaluate a tangent of an inverse sine")
                .addEx("TR12_ST3B_05", "Construct a triangle to help evaluate a sine of an inverse cosine")
                .addEx("TR12_ST3B_06", "Construct a triangle to help evaluate a tangent of an inverse cosine");
        m126m4t3.addExBlock("Evaluate compositions of trigonometric functions with different inverse functions")
                .addEx("TR12_ST3C_01", "Evaluate a sine of an inverse tangent")
                .addEx("TR12_ST3C_02", "Evaluate a cosine of an inverse tangent")
                .addEx("TR12_ST3C_03", "valuate a cosine of an inverse sine")
                .addEx("TR12_ST3C_04", "Evaluate a tangent of an inverse sine")
                .addEx("TR12_ST3C_05", "Evaluate a sine of an inverse cosine")
                .addEx("TR12_ST3C_06", "Evaluate a tangent of an inverse cosine");

        // Module 5

        final ModuleData m126m5 = m126.addModule(5, "Applications of Trigonometric Equations", "TR13_SR_HW",
                "c55-thumb.png");

        m126m5.skillsReview.addExBlock("Right triangle relationships")
                .addEx("TR13_SR1_01",
                        "Recall the SOH-CAH-TOA relationships and recognize 'opposite' and 'adjacent' angles");
        m126m5.skillsReview.addExBlock("Circular motion")
                .addEx("TR13_SR2_01",
                        "Write parametric functions describing the x- and y-components of circular motion");
        m126m5.skillsReview.addExBlock("Angles with perpendicular terminal rays")
                .addEx("TR13_SR3_01",
                        "Find two angles whose terminal rays are perpendicular to the terminal ray of a given angle");
        m126m5.skillsReview.addExBlock("The distance formula")
                .addEx("TR13_SR1_01", "Calculate the distance between points on the plane");
        m126m5.skillsReview.addExBlock("Area of circles and sectors")
                .addEx("TR13_SR5_01", "Find circle area and sector areas");
        m126m5.skillsReview.addExBlock("Arc length")
                .addEx("TR13_SR6_01", "Find the length of an outline made up of circular arcs");
        m126m5.skillsReview.addExBlock("Triangle area")
                .addEx("TR13_SR7_01",
                        "Find the area of a hexagon by breaking into triangles and finding all triangle areas");
        m126m5.skillsReview.addExBlock("The law of sines")
                .addEx("TR13_SR8_01", "Use the law of sines to solve for side lengths in a triangle");
        m126m5.skillsReview.addExBlock("The law of cosines")
                .addEx("TR13_SR9_01", "Use the law of cosines to solve for a side length in a triangle");

        final LearningTargetData m126m5t1 = m126m5.addLearningTarget(5, 1, "5.1", "TR13_ST1_HW",
                "I can solve application problems that involve fixed or varying angles. Specifically, I can:",
                "calculate altitudes or heights and distances using angle of elevation and angle of depression",
                "calculate vertex position along a line that maximizes the angle at that vertex",
                "calculate light paths under refraction using Snell’s law");
        m126m5t1.addExBlock("Application: Angles of elevation and depression")
                .addEx("TR13_ST1A_APP_01", "Estimate tree height using two angle measurements")
                .addEx("TR13_ST1A_APP_02", "Estimate airplane's distance to runway")
                .addEx("TR13_ST1A_APP_03", "Determine height where wire attaches to tower");
        m126m5t1.addExBlock("Application: Maximizing angles")
                .addEx("TR13_ST1B_APP_01", "Find triangle shape that maximizes an angle");
        m126m5t1.addExBlock("Application: Refraction of light")
                .addEx("TR13_ST1C_APP_01", "Solve light refraction problem for laser beam passing through glass");

        final LearningTargetData m126m5t2 = m126m5.addLearningTarget(5, 2, "5.2", "TR13_ST2_HW",
                "I can solve application problems that involve rotation. Specifically, I can:",
                "calculate the position of a point on a rotating object, or the position of shadows or projections "
                        + "of points on rotating objects",
                "determine when two rotating objects are aligned or are separated by a given angle");
        m126m5t2.addExBlock("Application: Positions of rotating objects")
                .addEx("TR13_ST2A_APP_01", "Solve Ferris wheel problem");
        m126m5t2.addExBlock("Application: Arrays of equal angles")
                .addEx("TR13_ST2A_ APP_02", "Solve problem involving spokes on a bicycle tire");
        m126m5t2.addExBlock("Application: Positions of rotating objects")
                .addEx("TR13_ST2B_APP_01", "Solve problem involving hands of an analog clock");

        final LearningTargetData m126m5t3 = m126m5.addLearningTarget(5, 3, "5.3", "TR13_ST3_HW",
                "I can solve application problems that involve distances, arc length, or sector area. Specifically, "
                        + "I can:",
                "calculate the distance between two points moving linearly",
                "calculate the distance between points on two rotating objects",
                "use triangle and sector area to find volumes or areas of portions of cylinders or circles",
                "use trigonometric functions to find chord lengths in circles");
        m126m5t3.addExBlock("Application: Distance between moving objects")
                .addEx("TR13_ST3A_APP_01", "Solve problem involving boats sailing in different directions");
        m126m5t3.addExBlock("Application: Mechanical devices with rotating parts")
                .addEx("TR13_ST3B_APP_01", "Solve a problem involving the drive mechanism of a sewing machine's");
        m126m5t3.addExBlock("Application: Tank volumes")
                .addEx("TR13_ST3C_APP_01", "Solve a problem involving water in a cylindrical tank");
        m126m5t3.addExBlock("Application: Arc and chord length")
                .addEx("TR13_ST3D_APP_01", "Find the length of a chord between the ends of an arc of a given length");

        // Module 6

        final ModuleData m126m6 = m126.addModule(6, "Polar Coordinates", "TR14_SR_HW", "c56-thumb.png");

        m126m6.skillsReview.addExBlock("Cosine, Sine, and Point Coordinates")
                .addEx("TR14_SR1_01", "Express points where a terminal ray meets a circle using cosine and sine");
        m126m6.skillsReview.addExBlock("Tangent and inverse tangent")
                .addEx("TR14_SR2_01", "Find tangent of an angle, two angles with a given tangent");
        m126m6.skillsReview.addExBlock("Distance and the Pythagorean Theorem")
                .addEx("TR14_SR3_01", "Given two points, draw a right triangle with the line segment between as its "
                        + "hypotenuse, express distance between using Pythagorean theorem");
        m126m6.skillsReview.addExBlock("Conversion between degree and radian measure")
                .addEx("TR14_SR4_01", "Convert between degree and radian measure");
        m126m6.skillsReview.addExBlock("Equation of a circle")
                .addEx("TR14_SR5_01", "Given a center and a radius, write the equation of the circle");
        m126m6.skillsReview.addExBlock("Circumference and arc length")
                .addEx("TR14_SR6_01", "Find the Circumference of a circle and the length of an arc given the radius");
        m126m6.skillsReview.addExBlock("Co-terminal angles")
                .addEx("TR14_SR7_01", "Given an angle, find co-terminal angles");

        final LearningTargetData m126m6t1 = m126m6.addLearningTarget(6, 1, "6.1", "TR14_ST1_HW",
                "I can plot and interpret points represented in polar coordinates. Specifically, I can:",
                "plot points from polar coordinates, including those with negative radius",
                "find multiple polar coordinate representations of points, including representations with both "
                        + "positive and negative radius",
                "interpret a graph on a polar coordinate system as the set of points whose polar coordinates satisfy "
                        + "an equation or inequality");
        m126m6t1.addExBlock("Plotting points in polar coordinates")
                .addEx("TR14_ST1A_01", "Given polar coordinates of points, plot them on a polar graph")
                .addEx("TR14_ST1A_02",
                        "Given polar coordinates of points whose radius is negative, plot them on a polar graph");
        m126m6t1.addExBlock("Multiple polar representations of points")
                .addEx("TR14_ST1B_01",
                        "Given polar coordinates of a point, find other sets of coordinates that represent the point");
        m126m6t1.addExBlock("Draw curves with constant radius")
                .addEx("TR14_ST1C_01", "Sketch a polar equation with constant radius")
                .addEx("TR14_ST1C_02", "Sketch a polar equation with constant radius and domain restriction on angle");
        m126m6t1.addExBlock("Draw curves with constant angle")
                .addEx("TR14_ST1C_03", "Sketch a polar equation with constant angle")
                .addEx("TR14_ST1C_04", "Sketch a polar equation with constant angle and domain restriction on radius");

        final LearningTargetData m126m6t2 = m126m6.addLearningTarget(6, 2, "6.2", "TR14_ST2_HW",
                "I can convert points in the plane between Cartesian and Polar coordinates. Specifically, I can:",
                "find Cartesian coordinates of a point specified in polar coordinates",
                "find polar coordinates for a point in any quadrant specified in Cartesian coordinates");
        m126m6t2.addExBlock("Polar to Cartesian coordinate conversions")
                .addEx("TR14_ST2A_01",
                        "Given polar coordinates of point with positive radius, final (x, y) coordinates")
                .addEx("TR14_ST2A_02",
                        "Given polar coordinates of point with negative radius, final (x, y) coordinates");
        m126m6t2.addExBlock("Cartesian to polar coordiante conversions")
                .addEx("TR14_ST2B_01", "Given (x, y) coordinates of a point in Quadrant 1, find polar coordinates")
                .addEx("TR14_ST2B_02", "Given (x, y) coordinates of a point in Quadrant 2, find polar coordinates")
                .addEx("TR14_ST2B_03", "Given (x, y) coordinates of a point in Quadrant 3, find polar coordinates")
                .addEx("TR14_ST2B_04", "Given (x, y) coordinates of a point in Quadrant 4, find polar coordinates");

        final LearningTargetData m126m6t3 = m126m6.addLearningTarget(6, 3, "6.3", "TR14_ST3_HW",
                "I can use polar coordinates in application contexts. Specifically, I can:",
                "model position or motion relative to a central point using polar coordinates",
                "model flat layouts that will form conical shapes",
                "model central forces or point sources of sound, light, or signals",
                "model color using components in polar coordinates",
                "model transformations of points in the plane");
        m126m6t3.addExBlock("Application: Calculating with distance and bearing")
                .addEx("TR14_ST3A_APP_01",
                        "Given distance and bearing to two points, find their coordinates and separation distance");
        m126m6t3.addExBlock("Application: Cell tower signal strength")
                .addEx("TR14_ST3C_APP_01",
                        "Given locations of cell towers, find signal strengths at various locations");
        m126m6t3.addExBlock("Transformations of points in the plane")
                .addEx("TR14_ST3E_01", "Perform rotations of points in the plane in polar coordinates");
        m126m6t3.addExBlock("Application: Robotics and prosthetics")
                .addEx("TR14_ST3E_APP_01",
                        "Given a robotic compound arm with specified orientation, find actuator coordinates");

        // Module 7

        final ModuleData m126m7 = m126.addModule(7, "Polar Functions", "TR15_SR_HW", "c57-thumb.png");

        m126m7.skillsReview.addExBlock("Conversion between polar and Cartesian coordinates")
                .addEx("TR15_SR1_01",
                        "Convert a Polar point to Cartesian, and a Cartesian point to polar");
        m126m7.skillsReview.addExBlock("The distance formula")
                .addEx("TR15_SR2_01",
                        "Find the distance between two points on a plane");
        m126m7.skillsReview.addExBlock("Parametric curves")
                .addEx("TR15_SR3_01",
                        "Given parametric equations for a curve, for what parameter value is the point at a "
                                + "specified location?");
        m126m7.skillsReview.addExBlock("Arc length")
                .addEx("TR15_SR4_01",
                        "Given radius and arc angle, find arc length");
        m126m7.skillsReview.addExBlock("Sector area")
                .addEx("TR15_SR5_01",
                        "Given radius and arc angle, find sector area");
        m126m7.skillsReview.addExBlock("The graphs of sine and cosine")
                .addEx("TR15_SR6_01",
                        "Sketch and describe graphs of sine and cosine");

        final LearningTargetData m126m7t1 = m126m7.addLearningTarget(7, 1, "7.1", "TR15_ST1_HW",
                "I can evaluate and graph polar functions and interpret polar data. Specifically, I can:",
                "evaluate, graph, and interpret functions that define radius (both positive and negative) in terms of "
                        + "angle",
                "evaluate, graph, and interpret functions that define angle in terms of radius",
                "evaluate, graph, and interpret functions that define angle and radius in terms of a parameter",
                "interpret scenarios where varying quantities can be interpreted as radius and angle");
        m126m7t1.addExBlock("Functions that define radius in terms of angle")
                .addEx("TR15_ST1A_01",
                        "Given a function, positive radius of angle, plot several points")
                .addEx("TR15_ST1A_02",
                        "Given a function, signed radius of angle, plot several points");
        m126m7t1.addExBlock("Functions that define angle in terms of radius")
                .addEx("TR15_ST1B_01",
                        "Given a function angle of radius, plot several points");
        m126m7t1.addExBlock("Parametric polar curves")
                .addEx("TR15_ST1C_01",
                        "Given radius and angle as functions of t, plot several ");
        m126m7t1.addExBlock("Applications where radius varies with angle")
                .addEx("TR15_ST1D_APP_01",
                        "An application in mapping and distances.");
        m126m7t1.addExBlock("Applications where angle varies with radius")
                .addEx("TR15_ST1D_APP_02",
                        "An application in flight-path of a Frisbee");
        m126m7t1.addExBlock("Applications where angle and radius vary parametrically")
                .addEx("TR15_ST1D_APP_03",
                        "An application with sonar range-finding.");

        final LearningTargetData m126m7t2 = m126m7.addLearningTarget(7, 2, "7.2", "TR15_ST2_HW",
                "I can plot and interpret polar equations and inequalities, and polar functions where radius is given "
                        + "by a constant, linear, or trigonometric function of angle. Specifically, I can:",
                "express the boundary, interior, or exterior of circles in polar form and create plots from these "
                        + "forms",
                "express spirals in polar form and create plots from these forms",
                "express the boundary or interior of rose curves, lemniscates, and cardioids in polar form and "
                        + "create plots from these forms");
        m126m7t2.addExBlock("Circles and circular arcs ")
                .addEx("TR15_ST2A_01",
                        "Write the polar equation of a circle of radius R, and use domain restriction to form a "
                                + "circular arc")
                .addEx("TR15_ST2A_02",
                        "Pick the inequality that defines the interior of a circle");
        m126m7t2.addExBlock("Spirals")
                .addEx("TR15_ST2B_01",
                        "Given the graph of a spiral, find the equation R=A*theta, and identify the graph of "
                                + "R=-A theta");
        m126m7t2.addExBlock("A tour of some polar curves and regions")
                .addEx("TR15_ST2C_01",
                        "Rose curves")
                .addEx("TR15_ST2C_02",
                        "Cardioid curves")
                .addEx("TR15_ST2C_03",
                        "Lemniscates")
                .addEx("TR15_ST2C_04",
                        "Region within a lemniscate");

        final LearningTargetData m126m7t3 = m126m7.addLearningTarget(7, 3, "7.3", "TR15_ST3_HW",
                "I can construct polar functions whose graphs have desired properties or model real-world forms. "
                        + "Specifically, I can:",
                "define polar functions that represent spirals with desired spacing between arms and desired "
                        + "orientation",
                "define polar functions that represent rose curves and petals of rose curves with desired size and "
                        + "orientation");
        m126m7t3.addExBlock("Designing and modeling with spirals")
                .addEx("TR15_ST3A_01",
                        "Given a desired spiral shape, construct the polar function");
        m126m7t3.addExBlock("Modeling spiral forms in nature")
                .addEx("TR15_ST3A_APP_01 ",
                        "Given a photo of a galaxy, model with spirals");
        m126m7t3.addExBlock("Designing and modeling with rose curves")
                .addEx("TR15_ST3B_01",
                        "Given desired rose curve, construct the polar function")
                .addEx("TR15_ST3B_02",
                        "Given a single petal of a rose curve, construct the polar function, domain restriction")
                .addEx("TR15_ST3B_03",
                        "Given a desired orientation of a rose curve or petal, construct the polar function");
        m126m7t3.addExBlock("Modeling rose curve shapes in nature")
                .addEx("TR15_ST3B_APP_01",
                        "Given a photo of a flower, model petals with rose curves");

        // Module 8

        final ModuleData m126m8 = m126.addModule(8, "Imaginary and Complex Numbers", "TR16_SR_HW", "c58-thumb.png");

        m126m8.skillsReview.addExBlock("Vectors in the plane")
                .addEx("TR16_SR1_01",
                        "Sketch vectors in the plane, and write the components of vectors shown");
        m126m8.skillsReview.addExBlock("Vector addition")
                .addEx("TR16_SR2_01",
                        "Perform vector addition and interpret geometrically");
        m126m8.skillsReview.addExBlock("Binomial multiplication with the distributive property")
                .addEx("TR16_SR3_01",
                        "Perform binomial multiplication with variables");
        m126m8.skillsReview.addExBlock("Converting between Cartesian and Polar coordinates")
                .addEx("TR16_SR4_01",
                        "Convert a Cartesian point into polar coordinates, and a polar point into Cartesian");
        m126m8.skillsReview.addExBlock("Co-terminal angles")
                .addEx("TR16_SR5_01",
                        "Find the angle in a given interval co-terminal to a given angle");
        m126m8.skillsReview.addExBlock("Exponent rules")
                .addEx("TR16_SR6_01",
                        "multiply exponentials by adding exponents, raise exponential to power by multiplying "
                                + "exponents");
        m126m8.skillsReview.addExBlock("Negative and fractional powers")
                .addEx("TR16_SR7_01",
                        "raising a number to a negative power by raising reciprocal to positive power, take an N-th "
                                + "root by raising to power 1/N");

        final LearningTargetData m126m8t1 = m126m8.addLearningTarget(8, 1, "8.1", "TR16_ST1_HW",
                "I can perform arithmetic with complex numbers in their standard form. Specifically, I can:",
                "simplifying powers of i",
                "adding and subtracting complex numbers, and interpreting these operations geometrically in the "
                        + "complex plane",
                "finding complex conjugates of complex numbers",
                "multiplying complex numbers by real or complex numbers and simplifying the result",
                "dividing complex numbers using complex conjugates");
        m126m8t1.addExBlock("Powers of i")
                .addEx("TR16_ST1A_01",
                        "Given several powers of i, rewrite with no exponents");
        m126m8t1.addExBlock("Adding and subtracting complex numbers, the relationship to vector arithmetic")
                .addEx("TR16_ST1B_01",
                        "Plot complex numbers in the plane, identify numbers plotted in the complex plane, and " +
                                 "recognize that the real numbers are complex numbers that lie on the real axis")
                .addEx("TR16_ST1B_02",
                        "Add two complex numbers, and interpret as vector addition in the plane")
                .addEx("TR16_ST1B_03",
                        "Subtract two complex numbers and interpret subtraction geometrically as addition of a "
                                + "negated vector");
        m126m8t1.addExBlock("Complex conjugates")
                .addEx("TR16_ST1C_01",
                        "Find complex conjugates of complex numbers");
        m126m8t1.addExBlock("Complex multiplication")
                .addEx("TR16_ST1D_01",
                        "Multiply complex numbers");
        m126m8t1.addExBlock("Complex division")
                .addEx("TR16_ST1E_01",
                        "Divide complex numbers");

        final LearningTargetData m126m8t2 = m126m8.addLearningTarget(8, 2, "8.2", "TR16_ST2_HW",
                "I can convert complex numbers between standard / Cartesian and trigonometric / polar forms and "
                        + "work with complex numbers in trigonometric form. Specifically, I can:",
                "convert a complex number from standard for into trigonometric / polar form",
                "convert a complex number from trigonometric / polar form into standard form",
                "scale complex numbers in trigonometric / polar form and interpret geometrically",
                "recognize or find equivalent representations of complex numbers in trigonometric / polar form");
        m126m8t2.addExBlock("Converting complex numbers into trigonometric / polar form")
                .addEx("TR16_ST2A_01",
                        "Convert a complex number into trigonometric / polar form")
                .addEx("TR16_ST2A_02",
                        "Given a set of lettered points in the plane, and a set of complex numbers in "
                                + "trigonometric / polar form, match them");
        m126m8t2.addExBlock("Converting complex numbers from trigonometric / polar form to standard / Cartesian form")
                .addEx("TR16_ST2B_01",
                        "Given a polar form complex number, convert to standard / Cartesian");
        m126m8t2.addExBlock("Scalar multiplication of complex numbers")
                .addEx("TR16_ST2C_01",
                        "Multiply a complex number by a scalar in Cartesian and polar forms");
        m126m8t2.addExBlock("Equivalent representations of complex numbers in trigonometric / polar form")
                .addEx("TR16_ST2D_01",
                        "Find equivalent representations of complex numners in trigonometric / polar form");

        final LearningTargetData m126m8t3 = m126m8.addLearningTarget(8, 3, "8.3", "TR16_ST3_HW",
                "I can convert complex numbers between standard or trigonometric and exponential forms and work "
                        + "with complex numbers in exponential form. Specifically, I can:",
                "convert a complex number from standard or trigonometric form into exponential form",
                "convert a complex number from exponential form into standard form or trigonometric form",
                "multiply two complex numbers given in exponential form",
                "divide two complex numbers given in exponential form",
                "raise complex numbers to powers using exponential form",
                "find roots of complex numbers given in exponential form");
        m126m8t3.addExBlock("Expressing complex numbers in exponential form")
                .addEx("TR16_ST3A_01",
                        "Given complex numbers in trigonometric form or standard form, convert to exponential form")
                .addEx("TR16_ST3A_02",
                        "Given a set of lettered points in the plane, and a set of complex numbers in exponential "
                                + "form, match them");
        m126m8t3.addExBlock("Converting complex numbers from exponential form to other forms")
                .addEx("TR16_ST3B_01",
                        "Convert complex numbers from exponential to trigonometric and standard forms");
        m126m8t3.addExBlock("Complex multiplication in exponential form")
                .addEx("TR16_ST3C_01",
                        "Multiply complex numbers in exponential form");
        m126m8t3.addExBlock("Complex division in exponential form, complex reciprocals")
                .addEx("TR16_ST3D_01",
                        "Divide complex numbers in exponential form")
                .addEx("TR16_ST3D_02",
                        "Compute the reciprocal of a complex number");
        m126m8t3.addExBlock("Powers of complex numbers")
                .addEx("TR16_ST3E_01",
                        "Calculate a large power of a complex number using exponential form");
        m126m8t3.addExBlock("Roots of complex numbers")
                .addEx("TR16_ST3F_01",
                        "Compute a root of a complex number using exponential form");

        return m126;
    }
}
