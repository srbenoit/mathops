package dev.mathops.web.site.ramwork;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates the page that shows the "CSU Math Refresher" library of lessons, and allows a course administrator to
 * assign modules to students.
 */
enum PageMathRefresherLibrary {
    ;

    /**
     * Generates the page.
     *
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException {

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startEmptyPage(htm, Res.get(Res.SITE_TITLE), true);

            htm.sH(1).add("CSU Math Refresher").eH(1);

            htm.sP().add("This system provides a library of &quot;Math Refresher&quot; lessons ",
                    "that can be inserted as assignments in a Canvas course.  Each lesson ",
                    "provides a review of a math topic and a few practice problems to help ",
                    "refresh student skills when they are needed in another course.").eP();

            htm.sP().add("Browse lessons below, then integrate selected lessons within a Canvas ",
                    "course using the 'CSU Math Refresher' Canvas application.  Lessons will ",
                    "appear as assignments that will automatically integrate with the Canvas ",
                    "gradebook.").eP().hr();

            // TODO: Query a list of refresher topics, and a list of lessons within each topic.

            emitAlgebraTopics(htm);

            htm.div("vgap");

            emitTrigonometryTopics(htm);

            //

            htm.div("vgap");
            htm.sH(2, "smaller").add("Calculus").eH(2);

            //

            htm.div("vgap");
            htm.sH(2, "smaller").add("Linear Algebra").eH(2);

            //

            htm.div("vgap");
            htm.sH(2, "smaller").add("Differential Equations").eH(2);

            //

            htm.eDiv();
            Page.endEmptyPage(htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Emits a list of Algebra topics.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitAlgebraTopics(final HtmlBuilder htm) {

        startTopic(htm, "Algebra");

        startSubtopic(htm, "Number Systems");
        emitLesson(htm, "al_00_01nainrare", "Intetger, Rational and Real Numbers");
        emitLesson(htm, "al_00_02imagcomp", "Imaginary and Complex Numbers");
        emitLesson(htm, "al_00_03scinotat", "Scientific Notation");
        emitLesson(htm, "al_00_04factoria", "Factorials");
        endSubtopic(htm);

        startSubtopic(htm, "Functions and Graphs");
        emitLesson(htm, "al_01_01cartplan", "The Cartesian Plane");
        emitLesson(htm, "al_01_02fxnnotat", "Functions and Function Notation");
        emitLesson(htm, "al_01_03fxngraph", "Graphs of Functions");
        emitLesson(htm, "al_01_04combinfx", "Combining and Composing Functions");
        emitLesson(htm, "al_01_05reflsymm", "Reflections and Symmetry in Graphs");
        emitLesson(htm, "al_01_06xlatescl", "Translating and Scaling Graphs");
        emitLesson(htm, "al_01_07grphdesm", "Creating Graphs with Desmos");
        emitLesson(htm, "al_01_08grphxcel", "Creating Graphs with Excel");
        emitLesson(htm, "al_01_09grphti84", "Creating Graphs with the TI-84");
        endSubtopic(htm);

        startSubtopic(htm, "Lines and Linear Functions");
        emitLesson(htm, "al_02_01linslope", "Lines and Slope");
        emitLesson(htm, "al_02_02linequat", "Equations of Lines");
        emitLesson(htm, "al_02_03lingraph", "Graphing Linear Functions");
        emitLesson(htm, "al_02_04linparal", "Equations of Parallel Lines");
        emitLesson(htm, "al_02_05linperpe", "Equations of Perpendicular Lines");
        emitLesson(htm, "al_02_07lineslve", "Solving Linear Equations");
        emitLesson(htm, "al_02_07lineslvi", "Solving Linear Inequalities");
        emitLesson(htm, "al_02_08lininter", "Intersections of Lines");
        emitLesson(htm, "al_02_09propovar", "Proportionalty and Variation");
        emitLesson(htm, "al_02_09invpropo", "Inverse Proportionalty");
        emitLesson(htm, "al_02_10constroc", "Modeling Constant Rate of Change");
        emitLesson(htm, "al_02_11lineregr", "Linear Regression");
        emitLesson(htm, "al_02_12interpol", "Interpolation and Extrapolation");
        endSubtopic(htm);

        startSubtopic(htm, "Quadratic Functions");
        emitLesson(htm, "al_03_01quadfunc", "Quadratic Functions");
        emitLesson(htm, "al_03_02complsqu", "Factoring and Completing the Square");
        emitLesson(htm, "al_03_03quadslve", "Solving Quadratic Equations");
        emitLesson(htm, "al_03_04quadslvi", "Solving Quadratic Inequalities");
        emitLesson(htm, "al_03_05quadintl", "Intersection of Line and Quadratic");
        emitLesson(htm, "al_03_06quadintq", "Intersection of Two Quadratics");
        emitLesson(htm, "al_03_07lininter", "Complex Solutions to Quadratic Equations");
        emitLesson(htm, "al_03_08quadopti", "Optimization of Quadratic Functions");
        emitLesson(htm, "al_03_09quadregr", "Quadratic Regression");
        endSubtopic(htm);

        startSubtopic(htm, "Polynomial Functions");
        emitLesson(htm, "al_04_01quadfunc", "Polynomial Functions");
        emitLesson(htm, "al_04_02complsqu", "Factoring Polynomials");
        emitLesson(htm, "al_04_03polyslve", "Solving Polynomial Equations");
        emitLesson(htm, "al_04_04polyslvi", "Solving Polynomial Inequalities");
        emitLesson(htm, "al_04_05polyinte", "Intersections of Polynomials");
        emitLesson(htm, "al_04_06polyendb", "End-Behavior of Polynomial Functions");
        emitLesson(htm, "al_04_07polyregr", "Polynomial Regression");
        endSubtopic(htm);

        startSubtopic(htm, "Piecewise Functions");
        emitLesson(htm, "al_05_01piecfunc", "Piecewise Functions");
        emitLesson(htm, "al_05_02piecline", "Piecewise Linear Functions");
        emitLesson(htm, "al_05_03absvalue", "The Absolute Value Function");
        emitLesson(htm, "al_05_04pieclini", "Piecewise Linear Inequalities");
        emitLesson(htm, "al_05_05piecmodl", "Piecewise Linear Models of Data");
        endSubtopic(htm);

        startSubtopic(htm, "Rational Functions");
        emitLesson(htm, "al_06_01ratifunc", "Rational Functions");
        emitLesson(htm, "al_06_02plongdiv", "Polynomial Long Division");
        emitLesson(htm, "al_06_03partfrac", "Partial Fractions");
        emitLesson(htm, "al_06_04ratislve", "Solving Rational Equations");
        emitLesson(htm, "al_06_05ratislvi", "Solving Rational Inequalities");
        emitLesson(htm, "al_06_06vertasym", "Vertical Asymptotes");
        emitLesson(htm, "al_06_07horiasym", "Horizontal Asymptotes");
        emitLesson(htm, "al_06_08oblqasym", "Oblique/Slant Asymptotes");
        endSubtopic(htm);

        startSubtopic(htm, "Root and Power Functions");
        emitLesson(htm, "al_07_01exproots", "Properties of Exponents and Roots");
        emitLesson(htm, "al_07_02exroslve", "Solving Equations with Roots, Exponents.");
        emitLesson(htm, "al_07_03exroslvi", "Solving Inequalities with Roots, Exponents.");
        emitLesson(htm, "al_07_04powrfunc", "Power Functions");
        emitLesson(htm, "al_07_05rootfunc", "Root Functions");
        endSubtopic(htm);

        startSubtopic(htm, "Systems of Equations");
        emitLesson(htm, "al_08_01systequn", "Systems of Equations");
        emitLesson(htm, "al_08_02systineq", "Sytems of Inequalities");
        emitLesson(htm, "al_08_03systslvg", "Solving Systems Graphically.");
        emitLesson(htm, "al_08_04systslvs", "Solving Systems by Substitution");
        emitLesson(htm, "al_08_05systslve", "Solving Systems by Elimination");
        emitLesson(htm, "al_08_06gausjord", "The Gauss-Jordan Method");
        emitLesson(htm, "al_08_07systslvt", "Solving Systems with Technology");
        endSubtopic(htm);

        startSubtopic(htm, "Geometry");
        emitLesson(htm, "al_09_01anglemea", "Angles and Angle Measure");
        emitLesson(htm, "al_09_02triangle", "Triangles");
        emitLesson(htm, "al_09_03polygons", "Polygons");
        emitLesson(htm, "al_09_04circelip", "Circles and Ellipses");
        emitLesson(htm, "al_09_05thredvol", "Tyree-Dimensions Volumes");
        emitLesson(htm, "al_09_06intrelli", "Intersections of Ellipses");
        endSubtopic(htm);

        startSubtopic(htm, "Vectors");
        emitLesson(htm, "al_10_01vecarith", "Vectors and Vector Arithmetic");
        emitLesson(htm, "al_10_02vecdotpr", "Dot Product (without trig).");
        emitLesson(htm, "al_10_03vecproje", "Vector Projection");
        emitLesson(htm, "al_10_04vecdecom", "Vector Decomposition");
        endSubtopic(htm);

        startSubtopic(htm, "Curves");
        emitLesson(htm, "al_11_01curvimpl", "Implicit Curves");
        emitLesson(htm, "al_11_02curvpara", "Parametric Curves");
        emitLesson(htm, "al_11_03pamatriz", "Finding Parametrizations of Curves");
        emitLesson(htm, "al_11_04intrimpl", "Intersections of Implicit Curves");
        emitLesson(htm, "al_11_05intrpara", "Intersections of Parametric Curves");
        endSubtopic(htm);

        startSubtopic(htm, "Discrete Growth and Decay");
        emitLesson(htm, "al_12_01curvimpl", "Discrete Growth and Decay");
        emitLesson(htm, "al_12_02compintr", "Compound Interest");
        emitLesson(htm, "al_12_03loanmort", "Loans and Mortgages");
        endSubtopic(htm);

        startSubtopic(htm, "Exponential Functions");
        emitLesson(htm, "al_13_01expofunc", "Exponential Functions");
        emitLesson(htm, "al_13_02natuexpo", "The Natural Exponential Function");
        emitLesson(htm, "al_13_03expobase", "Changing Base of Exponential");
        emitLesson(htm, "al_13_04contgrde", "Continuous Growth and Decay");
        emitLesson(htm, "al_13_05logigrow", "Logistic Growth");
        emitLesson(htm, "al_13_06normdist", "The Normal Distribution");
        emitLesson(htm, "al_13_07exporegr", "Exponential Regression");
        endSubtopic(htm);

        startSubtopic(htm, "Logarithmic Functions");
        emitLesson(htm, "al_14_01expofunc", "Logarithmic Functions");
        emitLesson(htm, "al_14_02natuexpo", "Properties of Logarithms");
        emitLesson(htm, "al_14_03expobase", "Changing Base of Logarithm");
        emitLesson(htm, "al_14_04contgrde", "Simplifying Logarithmic Expressions");
        emitLesson(htm, "al_14_05logaslve", "Solving Logarithmic Equations");
        emitLesson(htm, "al_14_06loggraph", "Log-Linear and Log-Log Graphs");
        emitLesson(htm, "al_14_07logaregr", "Logarithmic Regression");
        endSubtopic(htm);

        endTopic(htm);
    }

    /**
     * Emits a list of Trigonometry topics.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitTrigonometryTopics(final HtmlBuilder htm) {

        startTopic(htm, "Trigonometry");

        startSubtopic(htm, "Angle Measure");
        emitLesson(htm, "tr_01_01", "Angle Measure");
        emitLesson(htm, "tr_01_02", "Angles in the Cartesian Plane");
        emitLesson(htm, "tr_01_03", "Angle (Modular) Aruthmetic");
        emitLesson(htm, "tr_01_04", "Right Triangles");
        emitLesson(htm, "tr_01_05", "The Pythagorean Theorem");
        endSubtopic(htm);

        startSubtopic(htm, "The Unit Circle");
        emitLesson(htm, "tr_02_01", "The Unit Circle");
        emitLesson(htm, "tr_02_02", "Radian Measure and Arc Length");
        emitLesson(htm, "tr_02_03", "Points on the Unit Circle");
        emitLesson(htm, "tr_02_04", "Area of a Sector");
        endSubtopic(htm);

        startSubtopic(htm, "Sine and Cosine");
        emitLesson(htm, "tr_03_01", "The Sine and Cosine Functions");
        emitLesson(htm, "tr_03_02", "Sine and Cosine of Common Angles");
        emitLesson(htm, "tr_03_03", "Amplitude and Period");
        emitLesson(htm, "tr_03_04", "Circular Motion and Period");
        emitLesson(htm, "tr_03_05", "Phase Shift, Relationship between Sine and Cosine");
        emitLesson(htm, "tr_03_06", "Vertical and Horizontal Shifts");
        emitLesson(htm, "tr_03_07", "Scaling Amplitude and Period");
        emitLesson(htm, "tr_03_08", "Determine Sine or Consine from a Graph");
        emitLesson(htm, "tr_03_09", "Coordinates of Points on a Circle");
        emitLesson(htm, "tr_03_10", "Simple Harmonic Motion");
        endSubtopic(htm);

        startSubtopic(htm, "Tangent");
        emitLesson(htm, "tr_04_01", "The Tangent Function");
        emitLesson(htm, "tr_04_02", "SOH-CAH-TOA Relationships in Right Triangles");
        endSubtopic(htm);

        startSubtopic(htm, "Reciprocal Functions");
        emitLesson(htm, "tr_05_01", "The Secant and Cosecant Functions");
        emitLesson(htm, "tr_05_02", "The Cotangent Function");
        endSubtopic(htm);

        startSubtopic(htm, "Inverse Trigonometric Functions");
        emitLesson(htm, "tr_06_01", "Inverse Sine");
        emitLesson(htm, "tr_06_02", "Inverse Cosine");
        emitLesson(htm, "tr_06_03", "Inverse Tangent");
        emitLesson(htm, "tr_06_04", "Inverse Cotangent");
        emitLesson(htm, "tr_06_05", "Inverse Secant");
        emitLesson(htm, "tr_06_06", "Inverse Cosecant");
        endSubtopic(htm);

        startSubtopic(htm, "Solving Triangles");
        emitLesson(htm, "tr_07_01", "Solving Right Triangles");
        emitLesson(htm, "tr_07_02", "The Law of Sines");
        emitLesson(htm, "tr_07_03", "The Law of Cosines");
        endSubtopic(htm);

        startSubtopic(htm, "Trigonometry and Vectors");
        emitLesson(htm, "tr_08_01", "Cosine and the Dot Product");
        emitLesson(htm, "tr_08_02", "Cross Product and Parallelogram Area");
        emitLesson(htm, "tr_08_03", "The Angle between Vectors");
        emitLesson(htm, "tr_08_04", "Vector Projections with Trigonometry");
        emitLesson(htm, "tr_08_05", "Triangle Area");
        endSubtopic(htm);

        startSubtopic(htm, "Trigonometric Identities");
        emitLesson(htm, "tr_09_01", "Even/Odd Identities");
        emitLesson(htm, "tr_09_02", "Cofunction Identities");
        emitLesson(htm, "tr_09_03", "Pythagorean Identities");
        emitLesson(htm, "tr_09_04", "Sum and Difference Identities");
        emitLesson(htm, "tr_09_05", "Product-to-Sum and Sum-to-Product Identities");
        emitLesson(htm, "tr_09_06", "Double-Angle Identities");
        emitLesson(htm, "tr_09_07", "Multiple-Angle Identities");
        emitLesson(htm, "tr_09_08", "Half-Angle Identities");
        endSubtopic(htm);

        startSubtopic(htm, "The Complex Plane");
        emitLesson(htm, "tr_10_01", "The Complex Plane");
        emitLesson(htm, "tr_10_02", "Complex Arithmetic");
        emitLesson(htm, "tr_10_03", "Trigonometric Form of Complex Numbers");
        emitLesson(htm, "tr_10_04", "Exponential Form of Complex Numbers");
        emitLesson(htm, "tr_10_05", "Complex Arithmetic with Trigonometric and Exponential Forms");
        endSubtopic(htm);

        startSubtopic(htm, "Polar Coordinates");
        emitLesson(htm, "tr_11_01", "Polar Coordinates");
        emitLesson(htm, "tr_11_02", "Converting between Cartesian and Polar Coordinates");
        emitLesson(htm, "tr_11_03", "Parabolas and Hyperbolas in Polar Coordinates");
        emitLesson(htm, "tr_11_04", "Ellipses in Polar Coordinates");
        emitLesson(htm, "tr_11_05", "Polar Functions and Graphs");
        endSubtopic(htm);

        endTopic(htm);
    }

    /**
     * Emits the opening of a topic header.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param topicName the topic name
     */
    private static void startTopic(final HtmlBuilder htm, final String topicName) {

        htm.addln(" <details open>")
                .add("  <summary class='topic'>")
                .add(topicName) //
                .addln("</summary>");
        htm.add("  ").sDiv("indent").addln();
    }

    /**
     * Emits the closing of a topic header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endTopic(final HtmlBuilder htm) {

        htm.add("  ").eDiv().addln("</details>");
    }

    /**
     * Emits the opening of a subtopic header.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param subtopicName the subtopic name
     */
    private static void startSubtopic(final HtmlBuilder htm, final String subtopicName) {

        htm.addln("  <details>")
                .add("    <summary class='subtopic'>")
                .add(subtopicName) //
                .addln("</summary>");
        htm.add("    ").sDiv("indent").addln();
    }

    /**
     * Emits the closing of a topic header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endSubtopic(final HtmlBuilder htm) {

        htm.add("    ").eDiv().addln("  </details>");
        htm.add("  ").div("vgap0").addln();
    }

    /**
     * Emits a lesson link.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param id    the lesson ID, used to create the link
     * @param title the link title
     */
    private static void emitLesson(final HtmlBuilder htm, final String id, final String title) {

        htm.addln("      <a class='ulink smaller' href='lesson_",
                id, ".html'>", title, "</a>").br();
    }
}
