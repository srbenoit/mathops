package dev.mathops.web.host.placement.placement;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * The Math Placement site landing page, which does not require a login (but if the user is logged in, the display may
 * be customized).
 */
enum ExploreCourses {
    ;

    /**
     * Emits the contents of the block that shows 100-level math courses.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitExploreMathCourses(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("100-Level Math Courses").eH(1);
        htm.eDiv();

        htm.div("vgap");

        htm.sH(3, "shaded").add("General Math Courses").eH(3);

        htm.sDiv("indent");
        htm.addln("<button class='accordion'>MATH 101: Math in the Social Sciences (3 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 101 is designed to increase students' understanding and appreciation of the importance and ",
                "usefulness of mathematics by showing how discrete mathematics can be used in planning and decision ",
                "making.  Topics include voting theory, fair division, optimization, and probability. MATH 101 ",
                "emphasizes cooperative group learning and active participation.").eP();
        htm.sP().add("MATH 101 satisfies the mathematics requirement of the All-University Core Curriculum but does ",
                "not satisfy the prerequisites for any courses that use mathematics (such as economics, physical ",
                "sciences, and statistics). MATH 101 does not satisfy the prerequisite for MATH 117.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 105: Patterns of Phenomena (3 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 105 engages students in the exploration of mathematical ideas and modes of thought and ",
                "their application in the arts and humanities. Representative topics include symmetry, levels of ",
                "infinity, the fourth dimension, contortions of space, chaos and fractals.  (Prior knowledge of ",
                "these topics is not assumed.)").sP();
        htm.sP().add("MATH 105 satisfies the mathematics requirement of the All-University Core Curriculum but does ",
                "not satisfy the prerequisites for any courses that use mathematics (such as economics, physical ",
                "sciences, and statistics). MATH 105 does not satisfy the prerequisite for MATH 117.").eP();
        htm.eDiv(); // accordionpanel
        htm.eDiv(); // indent

        htm.div("vgap2");

        htm.sH(3, "shaded").add("Pre-Calculus Courses").eH(3);

        htm.sDiv("indent");
        htm.addln("<button class='accordion'>MATH 117: College Algebra in Context I (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("In MATH 117, concepts and skills traditionally identified with college algebra are ",
                "presented in a learning environment that emphasizes active student involvement in investigating, ",
                "interpreting, applying, and communicating mathematical ideas.").eP();
        htm.sP().add("Topics include the idea of a mathematical model, linear functions, quadratic functions and ",
                        "equations, systems of equations and inequalities, piecewise-defined functions, and absolute " +
                        "value.")
                .eP();
        htm.sP().add("A Texas Instruments TI-84 graphing calculator is required.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 118: College Algebra in Context II (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("In MATH 118, concepts and skills traditionally identified with college algebra are presented ",
                "in a learning environment that emphasizes active student involvement in investigating, interpreting, ",
                "applying, and communicating mathematical ideas.").eP();
        htm.sP().add("Topics include polynomials and polynomial equations, rational functions, radical and root ",
                "functions, and fractional exponents.").eP();
        htm.sP().add("A Texas Instruments TI-84 graphing calculator is required.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 120: College Algebra (3 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 120 is a face-to-face course in Precalculus that examines ideas of quantity, variable, ",
                "rate of change, and formula that are necessary for succeeding in and learning precalculus and ",
                "calculus.  Equivalent to MATH 117, 118, and 124.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 124: Logarithmic and Exponential Functions (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 124 examines functions more carefully, including inverse functions. Topics include ",
                "definitions and graphs of exponential functions, definition of logarithmic functions as the ",
                "inverses of the exponential functions, properties of logarithmic functions, techniques for solving ",
                "exponential and logarithmic equations, and mathematical models involving logarithmic or exponential ",
                "functions.").eP();
        htm.sP().add("A scientific calculator is required for investigations of these topics. A ",
                "Texas Instruments TI-84 is strongly recommended.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 125: Numerical Trigonometry (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 125 is the first of two courses designed to help students acquire conceptual understanding ",
                "and computational proficiency with traditional topics from plane trigonometry. Content includes ",
                "definitions and graphs of the six trigonometric functions, techniques for solving right and oblique ",
                "triangles, the unit circle and radian measure, and periodic functions.").eP();
        htm.sP().add("Students are required to use a scientific calculator in their investigations of these topics. ",
                "A Texas Instruments TI-84 is recommended.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 126: Analytic Trigonometry (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 126 is the second of two courses designed to help students acquire conceptual ",
                "understanding and computational proficiency with traditional topics from plane trigonometry. ",
                "Content includes the inverse trigonometric functions, trigonometric identities, and solving ",
                "trigonometric equations.").eP();
        htm.sP().add("Students are required to use a scientific calculator in their investigations of these topics. ",
                "A Texas Instruments TI-84 is recommended.").eP();
        htm.eDiv(); // accordionpanel

//        htm.addln("<button class='accordion'>MATH 127: Precalculus (4 credits)</button>");
//        htm.sDiv("accordionpanel");
//        htm.sP().add("MATH 127 is a face-to-face course in Precalculus that examines ideas of quantity, variable, ",
//                "rate of change, and formula that are necessary for succeeding in and learning precalculus and ",
//                "calculus.").eP();
//        htm.sP().add("This course satisfies the prerequisite for MATH 155, MATH 156, MATH 157, or MATH 160.").eP();
//        htm.eDiv(); // accordionpanel
        htm.eDiv(); // indent

        htm.div("vgap2");

        htm.sH(3, "shaded").add("Calculus Courses").eH(3);

        htm.sDiv("indent");
        htm.addln("<button class='accordion'>MATH 141: Calculus in Management Sciences (3 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 141 is a Calculus course designed for students pursuing Business degrees or with an ",
                "interest in Business.  Topics covered include analytic geometry, limits, equilibrium of supply and ",
                "demand, differentiation, integration, applications of the derivative, integral.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 155: Calculus for Biological Scientists I (4 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 155 is a Calculus course focused on applications in Biology and the Life Sciences.  ",
                "Students study and quantify change in natural systems. Biological examples motivate mathematical ",
                "concepts, which in turn lead students to ask new questions about biology.").eP();
        htm.sP().add("MATH 155 is the first course in a two-course Calculus sequence. The second course is ",
                "MATH 255.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 156: Mathematics for Computational Science I (4 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 156 is a Calculus course focused on applications in computational science.  Students ",
                "study sets, relations, number systems, functions, sequences and series, and concepts of ",
                "differential and integral calculus.").eP();
        htm.sP().add("MATH 155 is the first course in a two-course Calculus sequence. ",
                "The second course is MATH 256.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 157/159: One Year Calculus I (3 credits each semester)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 157 and 159 is a one-year sequence that covers the content of MATH ",
                "160, going into greater depth in many areas, and at a more modest pace.  It can be ",
                "taken by students who struggle with the pace of MATH 160, or those who want a deeper ",
                "understanding of Calculus topics.").eP();
        htm.sP().add("MATH 157 / 159 can act as the first course in a three-course Calculus ",
                "sequence. The follow-on courses are MATH 161 and MATH 261.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 160: Calculus for Physical Scientists I (4 credits)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 160 is a Calculus course designed for science and engineering majors. ",
                "Topics covered include Limits, continuity, differentiation, and integration of ",
                "elementary functions, and applications.").eP();
        htm.sP().add("MATH 160 is the first course in a three-course Calculus sequence. ",
                "The follow-on courses are MATH 161 and MATH 261.").eP();
        htm.eDiv(); // accordionpanel
        htm.eDiv(); // indent

        htm.div("vgap2");

        htm.sH(3, "shaded").add("Mathematical Algorithms Courses").eH(3);

        htm.sDiv("indent");
        htm.addln("<button class='accordion'>MATH 151: Mathematical Algorithms in Matlab I (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 151 introduces ideas of mathematical algorithms in the context of the Matlab environment. ",
                "Topics covered include statements, expressions and variable assignments, scripts, control statements ",
                "and logical statements. Applications include Newton's method, Simpson's rule, and recursion.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 152: Mathematical Algorithms in Maple (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 152 introduces ideas of mathematical algorithms in the context of the Maple environment. ",
                "Topics covered include iteration and recursion, control and logical statements, expressions, ",
                "functions, data types, binary numbers, symbolic manipulation of terms.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>MATH 158: Mathematical Algorithms in C (1 credit)</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("MATH 158 introduces ideas of mathematical algorithms in the context of the C programming ",
                "language.  Topics covered include compilers, expressions, variable types, control statements, ",
                "pointers, and logical statements.  Applications include plotting, the secant method, the trapezoid ",
                "rule, and recursion.").eP();
        htm.eDiv(); // accordionpanel
        htm.eDiv(); // indent
    }
}
