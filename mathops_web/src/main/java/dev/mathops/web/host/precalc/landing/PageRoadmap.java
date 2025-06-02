package dev.mathops.web.host.precalc.landing;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with the "Placement Roadmap".  This shows the placement opportunities and the courses that placement
 * can make a student eligible for.
 */
enum PageRoadmap {
    ;

    /** Labels for eligibility blocks: */
    static final String[] PLACED_OUT_ELIGIBLE_FOR = {
            "Eligible for:",
            "Placed out of MATH 117, eligible for:",
            "Placed out of MATH 118, eligible for:",
            "Placed out of MATH 124, eligible for:",
            "Placed out of MATH 125, eligible for:",
            "Placed out of MATH 126, eligible for:"
    };

    /** Courses for which each level of placement makes the student eligible */
    static final String[][] ELIGIBLE_COURSES = {
            {"MATH 117, MATH 120"},
            {"MATH 118", "CHEM 107, ECON 202, ECON 204, STAT 301"},
            {"MATH 124, MATH 125", "CHEM 105, CHEM 111, CHEM 117, BZ 220"},
            {"CHEM 113, PH 121, CS 152, CS 163"},
            {"MATH 126, MATH 155"},
            {"MATH 156, MATH 160", "PH 141"},
    };

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final LandingSite site, final ServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startOrdinaryPage(htm, "Math Placement Roadmap", null, false, Page.ADMIN_BAR, null, false, true);

        htm.sH(1).add("Math Placement Roadmap").eH(1);

        htm.sP();
        htm.add("<b>Math Placement</b> is a process by which students become eligible for CSU courses that require ",
                "some level of preparation in Algebra or Trigonometry.");
        htm.eP();

        htm.sP("indent");
        htm.addln("<figure style='width:90%; height:66%; min-width:500px; max-width:704px; text-align:left;'>");
        htm.addln("<svg version='1.1' role='img' viewBox='0 0 600 440' font-size='14' ",
                "style='width:100%; height:50%; max-width:702px; font-family:open-sans-regular,sans-serif;'>");
        htm.addln("  <title>A graphic with boxes and arrows showing the courses a student can become eligible for ",
                "with each each level of Math Placement.</title>");
        htm.addln("  <defs>");
        htm.addln("    <marker id='A' viewBox='0 0 10 10' refX='0' refY='5' markerUnits='strokeWidth' ",
                "markerWidth='4' markerHeight='3' orient='auto'>");
        htm.addln(" <path d='M 0 0 L 10 5 L 0 10 z'/>");
        htm.addln("    </marker>");
        htm.addln("  </defs>");

        // Drawing outline
        htm.addln("  <rect x='0' y='0' width='600' height='420' stroke='gray' fill='rgb(235,235,235)' ",
                "stroke-width='1'/>");

        final int boxWidth = 86;
        final int boxHeight = 50;
        final int boxDx = 40;
        final int boxDy = 70;

        for (int i = 0; i < 6; ++i) {

            final int leftX = 10 + boxDx * i;
            final int topY = 10 + boxDy * i;

            // Draw five arrows to connect boxes in a cascade
            if (i < 5) {
                final int startX = leftX + 15;
                final int startY = topY + boxHeight;
                final int vert = boxDy - boxHeight / 2;
                final int horiz = boxDx - 27;

                htm.addln("  <path d='M " + startX + " " + startY + " v " + vert + " h " + horiz + "' fill='none' ",
                        "stroke='black' stroke-width='4' marker-end='url(#A)'/>");
            }

            // Draw arrows to the right of each box with a vertical line
            final int startX = leftX + boxWidth;
            final int lineX = startX + 35;
            final int midY = topY + boxHeight / 2;

            htm.addln("  <path d='M " + startX + " " + midY + " h 23' fill='none' stroke='black' stroke-width='4' ",
                    "marker-end='url(#A)'/>");
            htm.addln("  <path d='M " + lineX + " " + topY + " v " + boxHeight +
                      "' fill='none' stroke='rgb(25,111,67)' stroke-width='1'/>");

            // Boxes
            htm.addln("  <rect x='" + leftX + "' y='" + topY + "' width='" + boxWidth + "' height='" + boxHeight
                      + "' stroke='rgb(25,111,67)' fill='rgb(255,255,255)' stroke-width='1'/>");

            final int textLeft = leftX + 8;
            final int line1 = topY + 20;
            final int line2 = topY + 40;
            final int level = i + 1;
            htm.addln("  <text x='" + textLeft + "' y='" + line1 + "'>Placement</text>");
            htm.addln("  <text x='" + textLeft + "' y='" + line2 + "'>Level " + level + "</text>");

            // Eligibility result
            final int textX = lineX + 6;
            final int textY = topY + 11;
            htm.addln("  <text x='" + textX + "' y='" + textY + "' style='font-style:italic;'>",
                    PLACED_OUT_ELIGIBLE_FOR[i], "</text>");
            final int textX2 = textX + 5;
            int y = textY + 20;
            for (final String s : ELIGIBLE_COURSES[i]) {
                htm.addln("  <text x='" + textX2 + "' y='" + y + "' font-size='13' style='font-weight:bold;'>",
                        s, "</text>");
                y += 20;
            }
        }
        htm.addln("</svg>");

        htm.addln("  <figcaption style='margin-left:20pt; margin-right:20pt;'>");
        htm.addln("  Levels of <b>Math Placement</b>. Completing level 1 makes a student eligible for <b>MATH 117</b> ",
                "or <b>MATH 120</b>. Subsequent levels allow students to skip over any of the one-credit Precalculus ",
                "courses. Students who earn all of these placement results are eligible to register for any of the ",
                "Calculus courses offered by the Math department.");
        htm.addln("  </figcaption>");
        htm.addln("</figure>");
        htm.eP();

        htm.div("vgap");

        htm.sH(2).add("Skills Measured at each Placement Level").eH(2);

        htm.sP().add("<b>Placement Level 1</b> tests the following skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Working with intervals and inequalities</li>");
        htm.addln("  <li>Evaluating expressions, order of operations</li>");
        htm.addln("  <li>Manipulating expressions: expanding, combining like terms, factoring, canceling</li>");
        htm.addln("  <li>Using exponent rules for positive integer exponents</li>");
        htm.addln("  <li>Working with fractions: reciprocals, multiply/divide, add/subtract</li>");
        htm.addln("  <li>Interpreting a solution to an equation or inequality, solving by isolating variable</li>");
        htm.addln("  <li>Manipulating equations or inequalities to create equivalent statements</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Placement Level 2</b> tests the following additional skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Performing unit conversions</li>");
        htm.addln("  <li>Working with percentages, percent increase or decrease</li>");
        htm.addln("  <li>Using exponent rules for negative and fractional exponents, roots</li>");
        htm.addln("  <li>Reading and interpreting graphs: interpreting axes, interpreting and plotting points</li>");
        htm.addln("  <li>Interpreting graphs of functions: input and output variables, increasing/decreasing</li>");
        htm.addln("  <li>Interpreting linear functions: calculate slope, interpret as rate of change, solve ",
                "graphically or algebraically</li>");
        htm.addln("  <li>Finding roots (real and complex) of quadratic functions</li>");
        htm.addln("  <li>Interpreting graphs of quadratic functions: finding minimum or maximum, solve graphically ",
                "or algebraically</li>");
        htm.addln("  <li>Evaluating compositions of functions, inverse function relationships (including graphs of ",
                "inverse functions)</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Placement Level 3</b> tests the following additional skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Interpreting piecewise-defined functions</li>");
        htm.addln("  <li>Working with polynomials (graph, find roots, solve them graphically or algebraically)</li>");
        htm.addln("  <li>Working with rational functions, identify vertical asymptotes, end behavior</li>");
        htm.addln("  <li>Working with proportionality and variation, inverse proportionality</li>");
        htm.addln("  <li>Solving systems of linear equations using elimination or graphically</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Placement Level 4</b> tests the following additional skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Using properties and rules of exponents in expressions with a variable in the exponent</li>");
        htm.addln("  <li>Graphing and interpreting graphs of exponential functions</li>");
        htm.addln("  <li>Using properties and rules of logarithms</li>");
        htm.addln("  <li>Graphing and interpreting graphs of logarithmic functions</li>");
        htm.addln("  <li>Knowing that any nonzero base to power 0 is 1, and log (with any base) of 1 is zero</li>");
        htm.addln("  <li>Using inverse relationship between logarithmic and exponential functions</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Placement Level 5</b> tests the following additional skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Converting between units of angle measure</li>");
        htm.addln("  <li>Finding angles in diagrams using vertical, complementary, supplementary relationships</li>");
        htm.addln("  <li>Identifying angles in quadrants in standard position in the plane</li>");
        htm.addln("  <li>Identifying similar triangles and solve for side lengths</li>");
        htm.addln("  <li>Using relationships between radius, angle, arc length, sector area to solve problems</li>");
        htm.addln("  <li>Computing values of trigonometric functions of degree or radian angles</li>");
        htm.addln("  <li>Using right triangle relationships to solve right triangle problems</li>");
        htm.addln("  <li>Using inverse trigonometric functions to solve trigonometric equations</li>");
        htm.addln("  <li>Using the laws of sines and cosines to solve general triangle problems</li>");
        htm.addln("</ul>");

        htm.sP().add("<b>Placement Level 6</b> tests the following additional skills:").eP();
        htm.addln("<ul>");
        htm.addln("  <li>Verifying an identity</li>");
        htm.addln("  <li>Using Pythagorean identities to rewrite expressions and solve equations</li>");
        htm.addln("  <li>Using sum and difference identities to rewrite expressions and solve equations</li>");
        htm.addln("  <li>Using half -and double- angle identities to rewrite expressions and solve equations</li>");
        htm.addln("  <li>Converting points between Cartesian and Polar coordinates</li>");
        htm.addln("  <li>Performing complex arithmetic (addition, multiplication)</li>");
        htm.addln("</ul>");

        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
