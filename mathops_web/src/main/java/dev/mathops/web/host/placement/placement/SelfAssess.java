package dev.mathops.web.host.placement.placement;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * Handles the "self-assess" (Do I Need Math Placement?) interactive flowchart that is part of the main Math Placement
 * site. This is not a separate page, but is called by {@code PagePlacement} to emit this portion of that page's
 * content.
 */
enum SelfAssess {
    ;

    /** Destination for link to transfer information page. */
    private static final String XFER_HREF =
            "https://registrar.colostate.edu/what-credits-will-transfer/";

    /** A common element attribute. */
    private static final String DISPLAY_NONE = "style='display:none;'";

    /** A common string. */
    private static final String STUDY = //
            "<span class='ulink' onclick='nav2()'>Review and Practice</span>";

    /** A common string. */
    private static final String NO_ACTION_NEEDED = //
            "You do not need to complete the Math Placement Tool.";

    /**
     * Emits the contents of the block that shows more placement-related information (without login).
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitSelfAssess(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Do I Need Math Placement?").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("shaded2");

        htm.sP("tightbelow");
        htm.addln("Most incoming students should review and complete the <b>Math Placement ",
                "Tool</b> before Ram Orientation so that they can");
        htm.eP();
        htm.addln("<ul class='tight'>");
        htm.addln("<li class='bullet'>register for appropriate courses during orientation.</li>");
        htm.addln("<li class='bullet'>develop a plan with their major adviser or academic support ",
                "coordinator for completing the appropriate math in their first year at CSU.</li>");
        htm.addln("</ul>");

        htm.sP().add("This interactive flowchart will help you determine whether or not you need ",
                "to complete the Math Placement Tool.").eP();

        htm.sP()
                .add("You should also check <a class='ulink' target='_blank' ",
                        "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                        "<b>Math Requirements for Specific Majors</b></a> (PDF).")
                .eP();

        emitCenteredDiv(htm, "<button class='smallbtn' onClick='nav4()'>",
                "<strong>Back to General Math Placement Infomation</strong></button>");

        htm.eDiv(); // shaded2

        htm.div("vgap");

        htm.sDiv("shaded");

        //
        // (Q1)
        //

        htm.sDiv();
        emitNumberedQuestion(htm, 1,
                "Have you completed at least one Mathematics or Statistics course at a ",
                "college <strong>in Colorado</strong> with a grade of C&minus; or higher?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "Y");
        emitNButton(htm, "N");
        htm.eDiv(); // center
        htm.eDiv();

        //
        // (Q2) when "Y" on (Q1)
        //

        htm.sDiv(null, "id='saQ2Y'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 2, "Are any of these courses at the 100 level or higher?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YY");
        emitNButton(htm, "YN");
        htm.eDiv(); // center

        htm.eDiv(); // id=saQ2Y

        //
        // (Q3) when "Y" on (Q1), "Y" on (Q2)
        //

        htm.sDiv(null, "id='saQ3YY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 3,
                "Will you have your score/transcript before your orientation date?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YYY");
        emitNButton(htm, "YYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ3YY

        //
        // (Q4) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3)
        //

        htm.sDiv(null, "id='saQ4YYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 4,
                "Does your transfer credit appear in <a class='ulink' target='_blank' ",
                "href='https://ramweb.colostate.edu/'><b>RAMweb</b></a> under ",
                "<b>Menu &rarr; Records &rarr; Transfer Credits</b>?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YYYY");
        emitNButton(htm, "YYYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ4YYY

        //
        // (Q5) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saQ5YYYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 5,
                "Does your transfer credit satisfy the math requirement for your major, or the ",
                "prerequisites needed for your major so you can register for the correct class at ",
                "Orientation?");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YYYYY");
        emitNButton(htm, "YYYYN");
        emitXButton(htm, "YYYYX");
        htm.eDiv();

        htm.eDiv(); // id=saQ5YYYY

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "Y" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYYY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYYY

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "N" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYYN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYYN

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "X" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYYX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYYX

        //
        // (Q5) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saQ5YYYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 5,
                "Do you expect your transfer credit to satisfy the math requirement for your major, ",
                "or the prerequisites needed for your major so you can register for the correct class ",
                "at Orientation?");
        emitCenteredDiv(htm, "<a class='ulink' target='_blank' ",
                "style='display:inline-block;margin:.5em;' href='", XFER_HREF,
                "'>Go to the Registrar's Transfer Evaluation web page</a>");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YYYNY");
        emitNButton(htm, "YYYNN");
        emitXButton(htm, "YYYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ5YYYN

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "Y" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm,
                "Bring a copy (unofficial is OK) of your scores/transcript to Ram Orientation ",
                "and ask for an official copy to be sent to CSU.");
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYNY

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "N" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYNN

        //
        // (R6) when "Y" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "X" on (Q5)
        //

        htm.sDiv(null, "id='saR6YYYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YYYNX

        //
        // (Q4) when "Y" on (Q1), "Y" on (Q2), "N" on (Q3)
        //

        htm.sDiv(null, "id='saQ4YYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 4,
                "Is math critical to progress in your major beginning with your first semester? ",
                "Check <a class='ulink' target='_blank' ",
                "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                "<b>Math Requirements for Specific Majors</b></a> (PDF).");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YYNY");
        emitNButton(htm, "YYNN");
        emitXButton(htm, "YYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ4YYN

        //
        // (R5) when "Y" on (Q1), "Y" on (Q2), "N" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saR5YYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We strongly recommend that you ", STUDY,
                " and complete the Math Placement Tool. The appropriate placement result will allow ",
                "you to register for necessary classes (chemistry, physics, economics, etc.) at Ram ",
                "Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YYNY

        //
        // (R5) when "Y" on (Q1), "Y" on (Q2), "N" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saR5YYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "Please have an official transcript/score report sent to CSU as soon as ",
                "possible.  In your first semester, please talk with your adviser about how to ",
                "fulfill the CSU requirement for math.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YYNN

        //
        // (R5) when "Y" on (Q1), "Y" on (Q2), "N" on (Q3), "X" on (Q4)
        //

        htm.sDiv(null, "id='saR5YYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY, " and complete the Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YYNX

        //
        // (Q3) when "Y" on (Q1), "N" on (Q2)
        //

        htm.sDiv(null, "id='saQ3YN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 3,
                "Are any of these courses <b>MAT 055</b>, <b>MAT 093</b>, or <b>MAT 099</b>?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNY");
        emitNButton(htm, "YNN");
        htm.eDiv();

        htm.eDiv(); // id=saQ3YN

        //
        // (Q4) when "Y" on (Q1), "N" on (Q2), "Y" on (Q3)
        //

        htm.sDiv(null, "id='saQ4YNY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 4, "Does this course appear in <a class='ulink' target='_blank' ",
                "href='https://ramweb.colostate.edu/'><b>RAMweb</b></a> under <b>Menu &rarr; Records ",
                "&rarr; Transfer Credits</b>? (it would appear as MATH-002)");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNYY");
        emitNButton(htm, "YNYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ4YNY

        //
        // (R5) when "Y" on (Q1), "N" on (Q2), "Y" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saR5YNYY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm,
                "You may register for <b>MATH 117, College Algebra I</b> without completing the ",
                "Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YNYY

        //
        // (R5) when "Y" on (Q1), "N" on (Q2), "Y" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saR5YNYN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm,
                "Bring a copy (unofficial is OK) of your transcript to Ram Orientation and ask ",
                "for an override to register for <b>MATH 117, College Algebra I</b> without ",
                "completing the Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YNYN

        //
        // (Q4) when "Y" on (Q1), "N" on (Q2), "N" on (Q3)
        //

        htm.sDiv(null, "id='saQ4YNN'", DISPLAY_NONE);

        htm.div("vgap");
        htm.sDiv("advice");
        htm.addln("You are eligible to register for <b>MATH 101, Math in the Social Sciences</b>, ",
                        "<b>MATH 105, Patterns of Phenomena</b>, <b>STAT 100: Statistical Literacy</b>, ",
                        "<b>STAT 201: General Statistics</b>, and <b>STAT 204: Statistics With Business " +
                        "Applications</b>.")
                .br().br();
        emitCheckMajorReqs(htm);
        htm.eDiv().br();

        emitNumberedQuestion(htm, 4,
                "Is <b>MATH 101</b>, <b>MATH 105</b>, <b>STAT 100</b>, <b>STAT 201</b>, or <b>STAT 204</b> ",
                "appropriate for your degree program at CSU?");
        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNY");
        emitNButton(htm, "YNNN");
        htm.eDiv();

        htm.eDiv(); // id=saQ4YNN

        //
        // (R5) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saR5YNNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "You may register for <b>MATH 101, Math in the Social Sciences</b>, ",
                "<b>MATH 105, Patterns of Phenomena</b>, <b>STAT 100: Statistical Literacy</b>, ",
                "<b>STAT 201: General Statistics</b>, and <b>STAT 204: Statistics With Business Applications</b> ",
                "without completing the Math Placement Tool.");
        htm.sP("center");
        htm.addln("Please note: <em style='color:red'><b>MATH 101</b>, <b>MATH 105</b>, <b>STAT 100</b>, ",
                "<b>STAT 201</b> and <b>STAT 204</b> do not satisfy the prerequisites for <b>MATH 117</b>.</em>");
        htm.eP();
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5YNNY

        //
        // (Q5) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saQ5YNNN'", DISPLAY_NONE);

        htm.sP("center");
        htm.addln("Please note: <em style='color:red'><b>MATH 101</b>, <b>MATH 105</b>, <b>STAT 100</b>, ",
                "<b>STAT 201</b> and <b>STAT 204</b> do not satisfy the prerequisites for <b>MATH 117</b>.</em>");
        htm.eP();

        emitNumberedQuestion(htm, 5,
                "Do you expect to earn <strong>any other</strong> college credit in ",
                "<strong>Mathematics</strong> or <strong>Statistics</strong> through a college ",
                "course or an AP/IB (or similar) exam?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNY");
        emitNButton(htm, "YNNNN");
        emitXButton(htm, "YNNNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ5YNNN

        //
        // (Q6) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5)
        //

        htm.sDiv(null, "id='saQ6YNNNY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 6,
                "Will you have your score/transcript before your orientation date?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNYY");
        emitNButton(htm, "YNNNYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ6YNNNY

        //
        // (Q7) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6)
        //

        htm.sDiv(null, "id='saQ7YNNNYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 7,
                "Does your transfer credit appear in <a class='ulink' target='_blank' ",
                "href='https://ramweb.colostate.edu/'><b>RAMweb</b></a> under <b>Menu &rarr; Records ",
                "&rarr; Transfer Credits</b>?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNYYY");
        emitNButton(htm, "YNNNYYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ7YNNNYY

        //
        // (Q8) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "Y" on (Q7)
        //

        htm.sDiv(null, "id='saQ8YNNNYYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 8,
                "Does your transfer credit satisfy the math requirement for your major, or the ",
                "prerequisites needed for your major so you can register for the correct class at ",
                "Orientation?");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNYYYY");
        emitNButton(htm, "YNNNYYYN");
        emitXButton(htm, "YNNNYYYX");
        htm.eDiv();

        htm.eDiv(); // id=saQ8YNNNYYY

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "Y" on (Q7), "Y" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYYY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYYY

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "Y" on (Q7), "N" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYYN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYYN

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "Y" on (Q7), "X" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYYX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYYX

        //
        // (Q8) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "N" on (Q7)
        //

        htm.sDiv(null, "id='saQ8YNNNYYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 8,
                "Do you expect your transfer credit to satisfy the math requirement for your major, ",
                "or the prerequisites needed for your major so you can register for the correct class ",
                "at Orientation?");
        emitCenteredDiv(htm, "<a class='ulink' target='_blank' ",
                "style='display:inline-block;margin:.5em;' href='", XFER_HREF,
                "'>Go to the Registrar's Transfer Evaluation web page</a>");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNYYNY");
        emitNButton(htm, "YNNNYYNN");
        emitXButton(htm, "YNNNYYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ8YNNNYYN

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "N" on (Q7), "Y" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "Bring a copy (unofficial is OK) of your scores/transcript to Ram ",
                "Orientation and ask for an official copy to be sent to CSU.");
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYNY

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "N" on (Q7), "N" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYNN

        //
        // (R9) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "Y" on (Q6),
        // "N" on (Q7), "X" on (Q8)
        //

        htm.sDiv(null, "id='saR9YNNNYYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR9YNNNYYNX

        //
        // (Q7) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "N" on (Q6)
        //

        htm.sDiv(null, "id='saQ7YNNNYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 7,
                "Is math critical to progress in your major beginning with your first semester? ",
                "Check <a class='ulink' target='_blank'",
                "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                "<b>Math Requirements for Specific Majors</b></a> (PDF).");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "YNNNYNY");
        emitNButton(htm, "YNNNYNN");
        emitXButton(htm, "YNNNYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ7YNNNYN

        //
        // (R8) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "N" on (Q6),
        // "Y" on (Q7)
        //

        htm.sDiv(null, "id='saR8YNNNYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We strongly recommend that you ", STUDY,
                " and complete the Math Placement Tool. The appropriate placement result will allow ",
                "you to register for necessary classes (chemistry, physics, economics, etc.) at Ram ",
                "Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR8YNNNYNY

        //
        // (R8) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "N" on (Q6),
        // "N" on (Q7)
        //

        htm.sDiv(null, "id='saR8YNNNYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "Please have an official transcript/score report sent to CSU as soon as ",
                "possible.  In your first semester, please talk with your adviser about how to ",
                "fulfill the CSU requirement for math.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR8YNNNYNN

        //
        // (R8) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "Y" on (Q5), "N" on (Q6),
        // "X" on (Q7)
        //

        htm.sDiv(null, "id='saR8YNNNYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY, " and complete the Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR8YNNNYNX

        //
        // (R6) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "N" on (Q5)
        //

        htm.sDiv(null, "id='saR6YNNNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We strongly recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YNNNN

        //
        // (R6) when "Y" on (Q1), "N" on (Q2), "N" on (Q3), "N" on (Q4), "X" on (Q5)
        //

        htm.sDiv(null, "id='saR6YNNNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "If you are unsure if you will receive a sufficient score or grade ",
                "to earn college credit, we strongly recommend you ", STUDY,
                " and complete the Math Placement Tool.  The appropriate placement result will ",
                "allow you to register for necessary classes (chemistry, physics, economics, etc...) ",
                "at Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6YNNNX

        //
        // (Q2) when "N" on (Q1)
        //

        htm.sDiv(null, "id='saQ2N'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 2, "Do you expect to earn college credit in <strong>Mathematics/",
                "Statistics</strong> through a college course or an AP/IB (or similar) test?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NY");
        emitNButton(htm, "NN");
        emitXButton(htm, "NX");
        htm.eDiv();

        htm.eDiv(); // id=saQ2N

        //
        // (Q3) when "N" on (Q1), "Y" on (Q2)
        //

        htm.sDiv(null, "id='saQ3NY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 3,
                "Will you have your score/transcript before your orientation date?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NYY");
        emitNButton(htm, "NYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ3NY

        //
        // (Q4) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3)
        //

        htm.sDiv(null, "id='saQ4NYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 4,
                "Does your transfer credit appear in <a href='https://ramweb.colostate.edu/'>",
                "<b>RAMweb</b></a> under <b>Menu &rarr; Records &rarr; Transfer Credits</b>?");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NYYY");
        emitNButton(htm, "NYYN");
        htm.eDiv();

        htm.eDiv(); // id=saQ4NYY

        //
        // (Q5) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saQ5NYYY'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 5,
                "Does your transfer credit satisfy the math requirement for your major, or the ",
                "prerequisites needed for your major so you can register for the correct class at ",
                "Orientation?");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NYYYY");
        emitNButton(htm, "NYYYN");
        emitXButton(htm, "NYYYX");
        htm.eDiv();

        htm.eDiv(); // id=saQ5NYYY

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "Y" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYYY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYYY

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "N" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYYN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYYN

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "Y" on (Q4), "X" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYYX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYYX

        //
        // (Q5) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saQ5NYYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 5,
                "Do you expect your transfer credit to satisfy the math requirement for your major, ",
                "or the prerequisites needed for your major so you can register for the correct ",
                "class at Orientation?</br> &nbsp; &nbsp; <a class='ulink' target='_blank' ",
                "style='display:inline-block;margin:.5em;' href='", XFER_HREF,
                "'>Go to the Registrar's Transfer Evaluation web page</a>");
        emitPrereqNotes(htm);

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NYYNY");
        emitNButton(htm, "NYYNN");
        emitXButton(htm, "NYYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ5NYYN

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "Y" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "Bring a copy (unofficial is OK) of your scores/transcript to Ram ",
                "Orientation and ask for an official copy to be sent to CSU.");
        emitCenteredDiv(htm, NO_ACTION_NEEDED);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYNY

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "N" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYNN

        //
        // (R6) when "N" on (Q1), "Y" on (Q2), "Y" on (Q3), "N" on (Q4), "X" on (Q5)
        //

        htm.sDiv(null, "id='saR6NYYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitCheckMajorReqs(htm);
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR6NYYNX

        //
        // (Q4) when "N" on (Q1), "Y" on (Q2), "N" on (Q3)
        //

        htm.sDiv(null, "id='saQ4NYN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 4,
                "Is math critical to progress in your major beginning with your first semester? ",
                "Check <a  class='ulink' target='_blank' ",
                "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                "<b>Math Requirements for Specific Majors</b></a> (PDF).");

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NYNY");
        emitNButton(htm, "NYNN");
        emitXButton(htm, "NYNX");
        htm.eDiv();

        htm.eDiv(); // id=saQ4NYN

        //
        // (R5) when "N" on (Q1), "Y" on (Q2), "N" on (Q3), "Y" on (Q4)
        //

        htm.sDiv(null, "id='saR5NYNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We strongly recommend that you ", STUDY,
                " and complete the Math Placement Tool. The appropriate placement result will allow ",
                "you to register for necessary classes (chemistry, physics, economics, etc.) at Ram ",
                "Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5NYNY

        //
        // (R5) when "N" on (Q1), "Y" on (Q2), "N" on (Q3), "N" on (Q4)
        //

        htm.sDiv(null, "id='saR5NYNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "Please have an official transcript/score report sent to CSU as soon as ",
                "possible.  In your first semester, please talk with your adviser about how to ",
                "fulfill the CSU requirement for math.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5NYNN

        //
        // (R5) when "N" on (Q1), "Y" on (Q2), "N" on (Q3), "X" on (Q4)
        //

        htm.sDiv(null, "id='saR5NYNX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY, " and complete the Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR5NYNX

        //
        // (Q3) when "N" on (Q1), "N" on (Q2)
        //

        htm.sDiv(null, "id='saQ3NN'", DISPLAY_NONE);

        emitNumberedQuestion(htm, 3,
                "Does your major accept <strong>any</strong> 3 credits of AUCC Core Mathematics, including ",
                "<b>MATH 101</b>, <b>MATH 105</b>, <b>STAT 100</b>, <b>STAT 201</b> and <b>STAT 204</b>?");

        htm.sP("center");
        htm.addln("Check <a class='ulink' target='_blank' ",
                "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                "<b>Math Requirements for Specific Majors</b></a> (PDF).");
        htm.eP();

        htm.div("vgap");
        htm.sDiv("center");
        emitYButton(htm, "NNY");
        emitNButton(htm, "NNN");
        htm.eDiv();

        htm.eDiv(); // id=saQ3NN

        //
        // (R4) when "N" on (Q1), "N" on (Q2), "Y" on (Q3)
        //

        htm.sDiv(null, "id='saR4NNY'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "You are eligible to register for <b>MATH 101: Math in the Social Sciences</b>, ",
                "<b>MATH 105: Patterns of Phenomena</b>, <b>STAT 100: Statistical Literacy</b>, ",
                "<b>STAT 201: General Statistics</b>, or <b>STAT 204: Statistics With Business Applications</b> ",
                "without completing the Math Placement Tool.");
        htm.div("vgap");
        emitDiv(htm, "Please note: <em style='color:red'><b>MATH 101</b>, <b>MATH 105</b>, <b>STAT 100</b>, ",
                "<b>STAT 201</b>, and <b>STAT 204</b> do not satisfy the prerequisites for <b>MATH 117</b>.</em>");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR4NNY

        //
        // (R4) when "N" on (Q1), "N" on (Q2), "N" on (Q3)
        //

        htm.sDiv(null, "id='saR4NNN'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY,
                " and complete the Math Placement Tool prior to Ram Orientation.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR4NNN

        //
        // (R3) when "N" on (Q1), "X" on (Q2)
        //

        htm.sDiv(null, "id='saR3NX'", DISPLAY_NONE);

        emitResultsHeader(htm); // opens 'advice' div
        emitDiv(htm, "We recommend that you ", STUDY, " and complete the Math Placement Tool.");
        htm.eDiv(); // advice

        htm.eDiv(); // id=saR3NX

        htm.eDiv(); // shaded

        htm.div("vgap0");
        htm.div(null, "id='saBottom'");
    }

    /**
     * Emits a numbered question.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param number   the question number
     * @param question the question text
     */
    private static void emitNumberedQuestion(final HtmlBuilder htm, final int number,
                                             final String... question) {

        htm.hr("question").sDiv("left");
        htm.sSpan("circledNum").add(Integer.toString(number)).eSpan();
        htm.eDiv();

        htm.sP("question");
        for (final String q : question) {
            htm.add(q);
        }
        htm.eP();
        htm.div("clear");
    }

    /**
     * Emits the header for the results section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitResultsHeader(final HtmlBuilder htm) {

        htm.div("vgap2");
        htm.addln("<div class='advice'>");
        htm.addln("<div class='center'><strong>What I Should Do:</strong></div>");
        htm.div("vgap");
    }

    /**
     * Emits a div element with text.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param text the text
     */
    private static void emitDiv(final HtmlBuilder htm, final String... text) {

        htm.sDiv();
        for (final String s : text) {
            htm.add(s);
        }
        htm.addln();
        htm.eDiv();
    }

    /**
     * Emits a centered div element with text.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param text the text
     */
    private static void emitCenteredDiv(final HtmlBuilder htm, final String... text) {

        htm.sDiv("center");
        for (final String s : text) {
            htm.add(s);
        }
        htm.addln();
        htm.eDiv();
    }

    /**
     * Emits a recommendation to check the math requirements for specific majors.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitCheckMajorReqs(final HtmlBuilder htm) {

        emitDiv(htm, "Check <a class='ulink' target='_blank' ",
                "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                "<b>Math Requirements for Specific Majors</b></a> (PDF). You may also refer to your ",
                "department's website for your program of study and/or the <a class='ulink' ",
                "target='_blank' href='https://catalog.colostate.edu/general-catalog/'>",
                "General Catalog</a>.");
    }

    /**
     * Emits a "Yes" button.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param cmd the button command - the ID of the generated button is "sa" followed by this command, and the onClick
     *            handler is "saGo" followed by this command
     */
    private static void emitYButton(final HtmlBuilder htm, final String cmd) {

        htm.add(" <button class='btn' id='sa", cmd, "' onclick='saGo",
                cmd, "()'>Yes</button>");
    }

    /**
     * Emits a "No" button.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param cmd the button command - the ID of the generated button is "sa" followed by this command, and the onClick
     *            handler is "saGo" followed by this command
     */
    private static void emitNButton(final HtmlBuilder htm, final String cmd) {

        htm.add(" <button class='btn' id='sa", cmd, "' onclick='saGo",
                cmd, "()'>No</button>");
    }

    /**
     * Emits an "I Don't Know" button.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param cmd the button command - the ID of the generated button is "sa" followed by this command, and the onClick
     *            handler is "saGo" followed by this command
     */
    private static void emitXButton(final HtmlBuilder htm, final String cmd) {

        htm.add(" <button class='btn' id='sa", cmd, "' onclick='saGo",
                cmd, "()'>I Don't Know</button>");
    }

    /**
     * Emits a block with notes on prerequisites.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPrereqNotes(final HtmlBuilder htm) {

        emitDiv(htm,
                "Please Note: The prerequisites for <b>MATH 156 (Mathematics for Computational ",
                "Science I)</b> and <b>MATH 160 (Calculus for Physical Scientists I)</b> require a grade ",
                "of B or higher in both <b>MATH 124 (Logarithmic and Exponential Functions)</b> and ",
                "<b>MATH 126 (Analytic Trigonometry)</b>.");

        // emitDiv(htm,
        // "(Note: The prerequisites for <b>MATH 161 Calculus for Physical Scientists II</b> are "
        // + "both <b>MATH 160 Calculus for Physical Scientists I</b> and <b>MATH 124 Logarithmic "
        // + "and Exponential Functions</b>.)");
    }

    /**
     * Emits JavaScript scripts.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitScripts(final HtmlBuilder htm) {

        htm.addln("<script>");

        htm.addln("function saHideAll() {");
        htm.addln("  document.getElementById('saQ2Y').style.display='none';");
        htm.addln("  document.getElementById('saQ3YY').style.display='none';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='none';");
        htm.addln("  document.getElementById('saQ5YYYY').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYYY').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYYN').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYYX').style.display='none';");
        htm.addln("  document.getElementById('saQ5YYYN').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYNY').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYNN').style.display='none';");
        htm.addln("  document.getElementById('saR6YYYNX').style.display='none';");
        htm.addln("  document.getElementById('saQ4YYN').style.display='none';");
        htm.addln("  document.getElementById('saR5YYNY').style.display='none';");
        htm.addln("  document.getElementById('saR5YYNN').style.display='none';");
        htm.addln("  document.getElementById('saR5YYNX').style.display='none';");
        htm.addln("  document.getElementById('saQ3YN').style.display='none';");
        htm.addln("  document.getElementById('saQ4YNY').style.display='none';");
        htm.addln("  document.getElementById('saR5YNYY').style.display='none';");
        htm.addln("  document.getElementById('saR5YNYN').style.display='none';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='none';");
        htm.addln("  document.getElementById('saR5YNNY').style.display='none';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='none';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='none';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='none';");
        htm.addln("  document.getElementById('saQ8YNNNYYY').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYYY').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYYN').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYYX').style.display='none';");
        htm.addln("  document.getElementById('saQ8YNNNYYN').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYNY').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYNN').style.display='none';");
        htm.addln("  document.getElementById('saR9YNNNYYNX').style.display='none';");
        htm.addln("  document.getElementById('saR8YNNNYNY').style.display='none';");
        htm.addln("  document.getElementById('saR8YNNNYNN').style.display='none';");
        htm.addln("  document.getElementById('saR8YNNNYNX').style.display='none';");
        htm.addln("  document.getElementById('saQ7YNNNYN').style.display='none';");
        htm.addln("  document.getElementById('saR6YNNNN').style.display='none';");
        htm.addln("  document.getElementById('saR6YNNNX').style.display='none';");
        htm.addln("  document.getElementById('saQ2N').style.display='none';");
        htm.addln("  document.getElementById('saQ3NY').style.display='none';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='none';");
        htm.addln("  document.getElementById('saQ5NYYY').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYYY').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYYN').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYYX').style.display='none';");
        htm.addln("  document.getElementById('saQ5NYYN').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYNY').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYNN').style.display='none';");
        htm.addln("  document.getElementById('saR6NYYNX').style.display='none';");
        htm.addln("  document.getElementById('saR5NYNY').style.display='none';");
        htm.addln("  document.getElementById('saR5NYNN').style.display='none';");
        htm.addln("  document.getElementById('saR5NYNX').style.display='none';");
        htm.addln("  document.getElementById('saQ4NYN').style.display='none';");
        htm.addln("  document.getElementById('saQ3NN').style.display='none';");
        htm.addln("  document.getElementById('saR4NNY').style.display='none';");
        htm.addln("  document.getElementById('saR4NNN').style.display='none';");
        htm.addln("  document.getElementById('saR3NX').style.display='none';");
        htm.addln("}");

        // Question 1: "Y" or "N"

        htm.addln("function saGoY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saY').className='btnlit';");
        htm.addln("  document.getElementById('saN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saYY').className='btn';");
        htm.addln("  document.getElementById('saYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saY').className='btn';");
        htm.addln("  document.getElementById('saN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saNY').className='btn';");
        htm.addln("  document.getElementById('saNN').className='btn';");
        htm.addln("  document.getElementById('saNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 2Y: YY or YN

        htm.addln("function saGoYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYY').className='btnlit';");
        htm.addln("  document.getElementById('saYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saYYY').className='btn';");
        htm.addln("  document.getElementById('saYYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYY').className='btn';");
        htm.addln("  document.getElementById('saYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saYNY').className='btn';");
        htm.addln("  document.getElementById('saYNN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 3YY: YYY or YYN

        htm.addln("function saGoYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYY').className='btnlit';");
        htm.addln("  document.getElementById('saYYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYY').className='btn';");
        htm.addln("  document.getElementById('saYYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYN').style.display='block';");
        htm.addln("  document.getElementById('saYYNY').className='btn';");
        htm.addln("  document.getElementById('saYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4YYY: YYYY or YYYN

        htm.addln("function saGoYYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYY').className='btnlit';");
        htm.addln("  document.getElementById('saYYYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYY').style.display='block';");
        htm.addln("  document.getElementById('saYYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYYN').className='btn';");
        htm.addln("  document.getElementById('saYYYYX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYN').style.display='block';");
        htm.addln("  document.getElementById('saYYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 5YYYY: YYYYN or YYYYN or YYYYX

        htm.addln("function saGoYYYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYYY').className='btnlit';");
        htm.addln("  document.getElementById('saYYYYN').className='btn';");
        htm.addln("  document.getElementById('saYYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYYY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYYN').className='btnlit';");
        htm.addln("  document.getElementById('saYYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYYN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYYYX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYYY').className='btn';");
        htm.addln("  document.getElementById('saYYYYN').className='btn';");
        htm.addln("  document.getElementById('saYYYYX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYYX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 5YYYN: YYYNN or YYYNN or YYYNX

        htm.addln("function saGoYYYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYNY').className='btnlit';");
        htm.addln("  document.getElementById('saYYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYNY').className='btn';");
        htm.addln("  document.getElementById('saYYYNN').className='btnlit';");
        htm.addln("  document.getElementById('saYYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYYNY').className='btn';");
        htm.addln("  document.getElementById('saYYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5YYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6YYYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4YYN: YYNY or YYNN or YYNX

        htm.addln("function saGoYYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYNY').className='btnlit';");
        htm.addln("  document.getElementById('saYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYN').style.display='block';");
        htm.addln("  document.getElementById('saR5YYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYNY').className='btn';");
        htm.addln("  document.getElementById('saYYNN').className='btnlit';");
        htm.addln("  document.getElementById('saYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYN').style.display='block';");
        htm.addln("  document.getElementById('saR5YYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYYNY').className='btn';");
        htm.addln("  document.getElementById('saYYNN').className='btn';");
        htm.addln("  document.getElementById('saYYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YY').style.display='block';");
        htm.addln("  document.getElementById('saQ4YYN').style.display='block';");
        htm.addln("  document.getElementById('saR5YYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 3YN: YNY or YNN

        htm.addln("function saGoYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNY').className='btnlit';");
        htm.addln("  document.getElementById('saYNN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNY').style.display='block';");
        htm.addln("  document.getElementById('saYNYY').className='btn';");
        htm.addln("  document.getElementById('saYNYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNY').className='btn';");
        htm.addln("  document.getElementById('saYNN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saYNNY').className='btn';");
        htm.addln("  document.getElementById('saYNNN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4YNY: YNYY or YNYN

        htm.addln("function saGoYNYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNYY').className='btnlit';");
        htm.addln("  document.getElementById('saYNYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNY').style.display='block';");
        htm.addln("  document.getElementById('saR5YNYY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNYY').className='btn';");
        htm.addln("  document.getElementById('saYNYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNY').style.display='block';");
        htm.addln("  document.getElementById('saR5YNYN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4YNN: YNNY or YNNN

        htm.addln("function saGoYNNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saR5YNNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNY').className='btn';");
        htm.addln("  document.getElementById('saYNNN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saYNNNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 5YNNN: YNNNY or YNNNN or YNNNX

        htm.addln("function saGoYNNNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saYNNNYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNN').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saR6YNNNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saR6YNNNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 6YNNNY: YNNNYY or YNNNYN

        htm.addln("function saGoYNNNYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saYNNNYYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYN').style.display='block';");
        htm.addln("  document.getElementById('saYNNNYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 7YNNNYY: 8YNNNYYY or 8YNNNYYN

        htm.addln("function saGoYNNNYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYYN').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYY').style.display='block';");
        htm.addln("  document.getElementById('saYNNNYYYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYN').style.display='block';");
        htm.addln("  document.getElementById('saYNNNYYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 8YNNNYYY: 9YNNNYYYY or 9YNNNYYYN or 9YNNNYYYX

        htm.addln("function saGoYNNNYYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYYY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYYYN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYY').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYYY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYN').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYY').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYYN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYYYX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYYY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYYX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYY').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYYX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 8YNNNYYN: 9YNNNYYNY or 9YNNNYYNN or 9YNNNYYNX

        htm.addln("function saGoYNNNYYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYNY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYN').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNN').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYN').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYY').style.display='block';");
        htm.addln("  document.getElementById('saQ8YNNNYYN').style.display='block';");
        htm.addln("  document.getElementById('saR9YNNNYYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 7YNNNYN: 8YNNNYNY or 8YNNNYNN or 8YNNNYNX

        htm.addln("function saGoYNNNYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYNY').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYN').style.display='block';");
        htm.addln("  document.getElementById('saR8YNNNYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNN').className='btnlit';");
        htm.addln("  document.getElementById('saYNNNYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYN').style.display='block';");
        htm.addln("  document.getElementById('saR8YNNNYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoYNNNYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saYNNNYNY').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNN').className='btn';");
        htm.addln("  document.getElementById('saYNNNYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2Y').style.display='block';");
        htm.addln("  document.getElementById('saQ3YN').style.display='block';");
        htm.addln("  document.getElementById('saQ4YNN').style.display='block';");
        htm.addln("  document.getElementById('saQ5YNNN').style.display='block';");
        htm.addln("  document.getElementById('saQ6YNNNY').style.display='block';");
        htm.addln("  document.getElementById('saQ7YNNNYN').style.display='block';");
        htm.addln("  document.getElementById('saR8YNNNYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 2N: NY or NN or NX

        htm.addln("function saGoNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNY').className='btnlit';");
        htm.addln("  document.getElementById('saNN').className='btn';");
        htm.addln("  document.getElementById('saNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saNYY').className='btn';");
        htm.addln("  document.getElementById('saNYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNY').className='btn';");
        htm.addln("  document.getElementById('saNN').className='btnlit';");
        htm.addln("  document.getElementById('saNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NN').style.display='block';");
        htm.addln("  document.getElementById('saNNY').className='btn';");
        htm.addln("  document.getElementById('saNNN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNY').className='btn';");
        htm.addln("  document.getElementById('saNN').className='btn';");
        htm.addln("  document.getElementById('saNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saR3NX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 3NY: NYY or NYN

        htm.addln("function saGoNYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYY').className='btnlit';");
        htm.addln("  document.getElementById('saNYN').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saNYYY').className='btn';");
        htm.addln("  document.getElementById('saNYYN').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYY').className='btn';");
        htm.addln("  document.getElementById('saNYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYN').style.display='block';");
        htm.addln("  document.getElementById('saNYNY').className='btn';");
        htm.addln("  document.getElementById('saNYNN').className='btn';");
        htm.addln("  document.getElementById('saNYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4NYY: NYYY or NYYN

        htm.addln("function saGoNYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYY').className='btnlit';");
        htm.addln("  document.getElementById('saNYYN').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYY').style.display='block';");
        htm.addln("  document.getElementById('saNYYYY').className='btn';");
        htm.addln("  document.getElementById('saNYYYN').className='btn';");
        htm.addln("  document.getElementById('saNYYYX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYY').className='btn';");
        htm.addln("  document.getElementById('saNYYN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYN').style.display='block';");
        htm.addln("  document.getElementById('saNYYNY').className='btn';");
        htm.addln("  document.getElementById('saNYYNN').className='btn';");
        htm.addln("  document.getElementById('saNYYNX').className='btn';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 5NYYY: NYYYY or NYYYN or NYYYX

        htm.addln("function saGoNYYYY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYYY').className='btnlit';");
        htm.addln("  document.getElementById('saNYYYN').className='btn';");
        htm.addln("  document.getElementById('saNYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYYY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYYYN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYYY').className='btn';");
        htm.addln("  document.getElementById('saNYYYN').className='btnlit';");
        htm.addln("  document.getElementById('saNYYYX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYYN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYYYX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYYY').className='btn';");
        htm.addln("  document.getElementById('saNYYYN').className='btn';");
        htm.addln("  document.getElementById('saNYYYX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYY').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYYX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 5NYYN: NYYNY or NYYNN or NYYNX

        htm.addln("function saGoNYYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYNY').className='btnlit';");
        htm.addln("  document.getElementById('saNYYNN').className='btn';");
        htm.addln("  document.getElementById('saNYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYNY').className='btn';");
        htm.addln("  document.getElementById('saNYYNN').className='btnlit';");
        htm.addln("  document.getElementById('saNYYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYYNY').className='btn';");
        htm.addln("  document.getElementById('saNYYNN').className='btn';");
        htm.addln("  document.getElementById('saNYYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYY').style.display='block';");
        htm.addln("  document.getElementById('saQ5NYYN').style.display='block';");
        htm.addln("  document.getElementById('saR6NYYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 4NYN: NYNY or NYNN or NYNX

        htm.addln("function saGoNYNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYNY').className='btnlit';");
        htm.addln("  document.getElementById('saNYNN').className='btn';");
        htm.addln("  document.getElementById('saNYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYN').style.display='block';");
        htm.addln("  document.getElementById('saR5NYNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYNY').className='btn';");
        htm.addln("  document.getElementById('saNYNN').className='btnlit';");
        htm.addln("  document.getElementById('saNYNX').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYN').style.display='block';");
        htm.addln("  document.getElementById('saR5NYNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNYNX() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNYNY').className='btn';");
        htm.addln("  document.getElementById('saNYNN').className='btn';");
        htm.addln("  document.getElementById('saNYNX').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NY').style.display='block';");
        htm.addln("  document.getElementById('saQ4NYN').style.display='block';");
        htm.addln("  document.getElementById('saR5NYNX').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        // Question 3NN: NNY or NNN

        htm.addln("function saGoNNY() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNNY').className='btnlit';");
        htm.addln("  document.getElementById('saNNN').className='btn';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NN').style.display='block';");
        htm.addln("  document.getElementById('saR4NNY').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("function saGoNNN() {");
        htm.addln("  saHideAll();");
        htm.addln("  document.getElementById('saNNY').className='btn';");
        htm.addln("  document.getElementById('saNNN').className='btnlit';");
        htm.addln("  document.getElementById('saQ2N').style.display='block';");
        htm.addln("  document.getElementById('saQ3NN').style.display='block';");
        htm.addln("  document.getElementById('saR4NNN').style.display='block';");
        htm.addln("  document.getElementById('saBottom').scrollIntoView(false);");
        htm.addln("}");

        htm.addln("</script>");
    }
}
