package dev.mathops.web.site.placement.main;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The Math Placement site landing page, which does not require a login (but if the user is logged in, the display may
 * be customized).
 */
enum PagePlacement {
    ;

    /** A commonly used string. */
    private static final String EXT_LINK = "<span title='Links to an external site'>" //
            + "<img style='width:12px;margin-left:3px;position:relative;top:-2px;' "
            + "src='/images/external-link-24.png'/></span>";

    /**
     * Processes a GET request of this form.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startOrdinaryPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, true, true);

        // This page should include EVERYTHING that the user can see without logging in, with
        // JavaScript navigation to get to each part.

        htm.sDiv("buttons", "id='buttons'");

        htm.addln("<nav class='buttons' id='nav'>");
        htm.addln("<button id='btn1' class='btnlit' onClick='nav1()'>",
                "Math Placement</button>");
        htm.addln("<button id='btn4' class='btn' onClick='nav4()'>",
                "More Information</button>");
        htm.addln("<button id='btn2' class='fatbtn' onClick='nav2()'>",
                "<b>Get Started</b></button>");
        htm.addln("<button id='btn3' class='btn' onClick='nav3()'>",
                "Explore Math Courses</button>");
        htm.addln("</nav>");

        htm.sDiv("messages", "id='messages'");
        emitMessages(htm);
        htm.eDiv(); // messages

        htm.eDiv(); // buttons (end left-side navigation and message area)

        // Right-side panel

        htm.sDiv("text", "id='text'");

        htm.sDiv(null, "id='block1'", "style='display:block;'");
        emitPlacementProcess(htm);
        htm.eDiv(); // block1

        htm.sDiv(null, "id='block2'", "style='display:none;'");
        emitGetStarted(htm);
        htm.eDiv(); // block2

        htm.sDiv(null, "id='block3'", "style='display:none;'");
        ExploreCourses.emitExploreMathCourses(htm);
        htm.eDiv(); // block3

        htm.sDiv(null, "id='block4'", "style='display:none;'");
        emitMoreInformation(htm);
        htm.eDiv(); // block4

        htm.sDiv(null, "id='block5'", "style='display:none;'");
        SelfAssess.emitSelfAssess(htm);
        htm.eDiv(); // block5

        htm.sDiv(null, "id='block6'", "style='display:none;'");
        emitChallenge(htm);
        htm.eDiv(); // block6

        htm.sDiv(null, "id='block7'", "style='display:none;'");
        emitReview(htm);
        htm.eDiv(); // block7

        htm.sDiv(null, "id='block8'", "style='display:none;'");
        OnCampusSchedule.emitPlacementSchedule(htm);
        htm.eDiv(); // block8

        htm.eDiv(); // text (end of right-side panel)

        htm.sDiv("textcover", "id='cover'").eDiv();

        emitPopups(htm);

        emitPlacementScripts(htm);
        SelfAssess.emitScripts(htm);
        MPPage.emitScripts(htm);
        Page.endOrdinaryPage(cache, site, site.getFooter(), htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits messages that can be clicked to show popups.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitMessages(final HtmlBuilder htm) {

        htm.sDiv("messagehdr").add("<span class='msgCount'>2</span> Messages").eDiv();

        htm.sDiv("message", "id='msg1' onClick='navmsg1()'");
        htm.sDiv("left");
        htm.addln("<img src='/images/mail-unread-new.png'/> &nbsp;");
        htm.eDiv();

        htm.sSpan("subject").add("Regisration Deadlines").eSpan();

        htm.div("clear");
        htm.sSpan("body").add("(Click to read)").eSpan();
        htm.eDiv();

        htm.sDiv("message", "id='msg2' onClick='navmsg2()'");
        htm.sDiv("left");
        htm.addln("<img src='/images/mail-unread-new.png'/> &nbsp;");
        htm.eDiv();

        htm.sSpan("subject").add("Upcoming Maintenance").eSpan();

        htm.div("clear");
        htm.sSpan("body").add("(Click to read)").eSpan();
        htm.eDiv();
    }

    /**
     * Emits (initially hidden) popups to display message contents.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPopups(final HtmlBuilder htm) {

        htm.sDiv("popup", "id='pop1'");
        htm.sDiv("msgWindow");
        htm.add("<input type='button' onClick='closemsg()' value='Close'/>");
        htm.eDiv(); // msgWindow
        htm.sDiv("msgHeader");
        htm.add("Subject: <strong>Registration Deadlines</strong>");
        htm.eDiv(); // msgHeader

        htm.sDiv("msgBody");

        Page.emitFile(htm, "placement_reg_deadlines.html");

        htm.eDiv(); // msgBody
        htm.eDiv(); // popup

        htm.sDiv("popup", "id='pop2'");
        htm.sDiv("msgWindow");
        htm.add("<input type='button' onClick='closemsg()' value='Close'/>");
        htm.eDiv(); // msgWindow
        htm.sDiv("msgHeader");
        htm.add("Subject: <strong>Upcoming Maintenance</strong>");
        htm.eDiv(); // msgHeader
        htm.sDiv("msgBody");

        Page.emitFile(htm, "placement_sch_maintenance.html");

        htm.eDiv(); // msgBody
        htm.eDiv(); // popup
    }

    /**
     * Emits the contents of the block that describes the Math Placement process.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPlacementProcess(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Math Placement").eH(1);
        htm.eDiv(); // center

        htm.div("vgap");

        // Logged in
        htm.sDiv("shaded2left");
        htm.sP()
                .add("The Math Placement process identifies the mathematics course(s) ",
                        "that best match your mathematical preparation with your academic goals. We want ",
                        "to support you in being successful in this placement process.")
                .eP()//
                .div("vgap0");

        htm.sP().add("The process has three steps...").eP();
        htm.eDiv(); // shaded2left

        htm.sDiv("center");

        htm.add("<ul class='stepnum', id='orangesteps' aria-hidden='true'>");
        htm.add(" <li class='dim' aria-hidden='true'>1</li>");
        htm.add(" <li class='dim' aria-hidden='true'>2</li>");
        htm.add(" <li class='dim' aria-hidden='true'>3</li>");
        htm.add("</ul>");

        htm.add("<nav class='plcsteps'>");
        htm.add("<div class='plcsteps' id='first'>",
                "<span class='sr-only'>Step 1:</span>", //
                "Make a Math Plan<br/>to see if you need<br/>Math Placement.</div>");

        htm.add("<div class='plcsteps'>",
                "<span class='sr-only'>Step 2:</span>Use our interactive<br/>", //
                "materials to review<br/>and practice.</div>");

        htm.add("<div class='plcsteps' id='last'>",
                "<span class='sr-only'>Step 3:</span>", //
                "Complete the<br/>Math Placement Tool,<br>determine next steps.</div>");

        htm.add("</nav>");

        htm.eDiv(); // center

        htm.div("vgap");
        htm.sDiv("center");
        htm.add("<iframe class='video' ",
                "src='https://www.youtube.com/embed/wXugzCNuo3E' ",
                "title='YouTube video player' frameborder='0' ",
                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; ",
                "gyroscope; picture-in-picture' allowfullscreen></iframe>");
        htm.eDiv();

        htm.div("vgap");
        htm.sDiv("shaded");
        htm.sDiv("center");
        htm.add("<button class='btn' onClick='nav2()'>",
                "<strong>Get Started</strong></button>");
        htm.eDiv();
        htm.eDiv();

        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
        htm.div("vgap2");
    }

    /**
     * Emits the contents of the block that shows the "get started" messaging.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitGetStarted(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Get Started").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("center");

        htm.sDiv("shaded2left",
                "style='display:inline-block; max-width:800px;'");
        htm.sP().add("To begin, you will need a <strong>NetID</strong>.").eP();

        htm.sP().add("NetID is the official CSU login used for all University systems.").eP();

        htm.sP().add("Creating your NetID establishes your official CSU email address.").eP();
        htm.eDiv(); // shaded2left

        htm.div("vgap2");

        htm.addln("<a class='fatbtn' href='secure/shibboleth.html'>",
                "Log in using your NetID</a><br>");

        htm.div("vgap2");

        htm.sDiv("center shaded2",
                "style='display:inline-block; width:300px;'");
        htm.sP().add("Don't have your NetID yet?").eP();
        htm.addln("<a class='smallbtn' href='https://eid.colostate.edu/' target='_blank'>",
                "Create your NetID</a>");
        htm.eDiv(); // center shaded2

        htm.eDiv(); // center
    }

    /**
     * Emits the contents of the block that shows more placement-related information (without login).
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitMoreInformation(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("General Math Placement Infomation").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("shaded2");
        htm.sDiv("column1");
        htm.sH(2).add("What math is required for").eH(2);
        htm.sP("indent").add("<ul class='loose'>") //
                .add("<li><a class='question' target='_blank' ",
                        "href='https://www.math.colostate.edu/placement/Math_Requirements.pdf'>",
                        "specific majors? ", EXT_LINK, "</a></li>")
                .add("<li><a class='question' target='_blank' ",
                        "href='https://catalog.colostate.edu/general-catalog/",
                        "all-university-core-curriculum/mathematics-requirement/'>",
                        "all majors at CSU?", EXT_LINK, "</a></li>")
                .add("<li><a class='question' target='_blank' ",
                        "href=https://admissions.colostate.edu/requirementinmathematics/'>",
                        "admission as a transfer student?", EXT_LINK, "</a></li></ul>")
                .eP();

        htm.div("vgap0");
        htm.sH(2).add("How do I").eH(2);

        htm.sP("indent").add("<ul class='loose'>")
                .add("<li><a class='question' target='_blank' target='_blank' ",
                        "href='https://registrar.colostate.edu/transferring-your-examination-test-credit/'>",
                        "transfer my AP or IB test credit?", EXT_LINK, "</a></li>")
                .add("<li><a class='question' onClick='nav5()'>",
                        "know if I need to complete Math Placement?</a></li>")
                .add("<li><a class='question' onclick='nav7()'>",
                        "prepare for the Math Placement Tool?</a></li>")
                .add("<li><a class='question' onClick='nav6()'>",
                        "challenge a course for credit?</a></li>")
                .add("</ul>").eP();
        htm.div("vgap0");
        htm.eDiv(); // column1

        htm.sDiv("column2");
        htm.sH(2).add("Take me to").eH(2);

        htm.sP("indent").add("<ul class='loose'>")
                .add("<li><a class='question' onclick='nav8()'>",
                        "on-campus Math Placement schedules.</a></li>")
                .add("<li><a class='question' onClick='nav2()'>",
                        "the Math Placement Tool.</a></li>")
                .add("<li><a class='question' href='/elm-tutorial/' target='_blank'>",
                        "the Entry-Level Mathematics Tutorial.", EXT_LINK, "</a></li>")
                .add("<li><a class='question' href='/precalc-tutorial/' target='_blank'>",
                        "the Precalculus Tutorial.", EXT_LINK, "</a></li>")
                .eP();

        htm.eDiv(); // column2
        htm.eDiv(); // shaded2

        htm.div("vgap");

        addCommonQuestions(htm);
    }

    /**
     * Emits challenge exam information.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitChallenge(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Challenging Precalculus Courses for Credit").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("shaded2");

        htm.div("vgap");
        htm.sH(3).add("Challenging Courses through the Precalculus Center").eH(3);

        htm.sP("indent");
        htm.addln("The Precalculus Center offers challenge exams for MATH 117, MATH 118, ",
                "MATH 124, MATH 125, and MATH 126.  These exams must be taken in the Precalculus ",
                "Center's testing area (Weber 138).  Each exam can earn credit in the corresponding ",
                "course.");
        htm.eP();

        htm.sP("indent");
        htm.addln("Each course challenge exam consists of 20 questions, and students are allowed ",
                "75 minutes to complete the exam.  Scoring 16 or higher will earn credit in the ",
                "challenged course.").eP();

        htm.sP("indent");
        htm.addln("Scratch paper and an on-screen TI-84 calculator are provided. Reference ",
                "materials or a personal calculator may not be used during a course challenge exam. ",
                "You only need your CSU ID card and a pencil.");
        htm.eP();

        htm.div("vgap");
        htm.sH(3).add("Is there a fee for challenging a course?").eH(3);

        htm.sP("indent");
        htm.addln("There is a $20 fee for taking a course challenge exam, as described in the ",
                "<a class='ulink' target='_blank' href='https://catalog.colostate.edu/",
                "general-catalog/academic-standards/registration/'>University Catalog</a>. This fee ",
                "will be billed to your student account at the time the exam is taken, regardless ",
                "of whether you successfully earn course credit on the exam.");
        htm.eP();

        htm.div("vgap");
        htm.sH(3).add("Am I eligible to challenge a precalculus course?").eH(3);

        htm.sP("indent");
        htm.addln("To be eligible to take a course challenge exam, a student...");
        htm.eP();

        htm.sDiv("indent");
        htm.addln("<ul>");
        htm.addln("<li> must be eligible to take the course being challenged (meaning all course ",
                "prerequisites must be satisfied);</li>");
        htm.addln("<li> must not be currently enrolled in the course being challenged; and</li>");
        htm.addln("<li> must not have already challenge the course (only one attempt ",
                "per course challenge exam is allowed).</li>");
        htm.addln("</ul>");
        htm.eDiv(); // indent

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln("<button class='smallbtn' onClick='nav4()'>",
                "<strong>Back to General Math Placement Infomation</strong></button>");
        htm.eDiv(); // center

        htm.eDiv(); // shaded2
    }

    /**
     * Emits review information.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitReview(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Review for the Math Placement Tool").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("shaded2");

        htm.div("vgap");
        htm.sH(3).add("Online Review Materials").eH(3);

        htm.sP("indent");
        htm.addln("Our goal is to support you to be successful in the placement process. One ",
                "way to be successful in this process is to study and review before completing the ",
                "Math Placement Tool.");
        htm.eP();

        htm.sP("indent");
        htm.addln("There may be questions that you do not know that are outside of your current ",
                "mathematical preparation - that is ok! Remember, the goal is to find the course(s) ",
                "that best match your mathematical preparation and your academic goals.");
        htm.eP();

        htm.sP("indent");
        htm.addln("To access our online review materials, log in, and then click on the ",
                "[ Review Materials ] button.").eP();

        htm.div("vgap");

        htm.sDiv("center");
        htm.addln("<button class='btn' onClick='nav2()'>",
                "<strong>Get Started</strong></button>");
        htm.eDiv(); // center

        htm.sDiv("center");
        htm.addln("<button class='smallbtn' onClick='nav4()'>",
                "<strong>Back to General Math Placement Infomation</strong></button>");
        htm.eDiv(); // center

        htm.eDiv(); // shaded2
    }

    /**
     * Generates the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void addCommonQuestions(final HtmlBuilder htm) {

        htm.sH(3, "shaded").add("Common Placement Questions").eH(3);

        htm.sDiv("indent0");

        htm.addln("<button class='accordion'>",
                "Do I have to go through the Math Placement Process?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("In general, most entering first-year students will need to complete the ",
                "Math Placement Tool. Students whose degree program allows them to satisfy their ",
                "mathematics requirement with <b>MATH 101, Math in the Social Sciences</b>, ",
                "<b>MATH 105, Patterns of Phenomena</b>, or <b>STAT 100: Statistical Literacy</b> ",
                "can do so without completing the Math Placement Tool. Students wishing to take a ",
                "math course other than MATH 101, MATH 105, or STAT 100 will need to complete the ",
                "Math Placement Tool. This requirement is waived for students who either scored 3, 4 ",
                "or 5 on an Advanced Placement Calculus Exam (either AB or BC) or have transfer ",
                "credit in a math course at the level of College Algebra or above.").eP();
        htm.sP("center").add("<button class='btn' onclick='nav5()'>",
                "Do I need Math Placement?</button>").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "How should I prepare for the Math Placement Tool?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP()
                .add("You should review and prepare before completing the Math Placement Tool, even ",
                        "if you are currently taking a mathematics course.  The Mathematics Department ",
                        "has prepared <a onclick='nav7()'>interactive review materials and practice ",
                        "questions</a> for all sections of the tool.")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "How do I arrange for testing accommodations like extended time?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP()
                .add("The Math Placement Tool is a web-based tool with a timer.  If you have had ",
                        "accommodations in the past, you will want to contact the ",
                        "<a href='https://disabilitycenter.colostate.edu/' target='_blank'>",
                        "Student Disability Center ", EXT_LINK,
                        "</a> in the TILT Building and meet with an accommodations specialist. The ",
                        "specialist will review your documentation and provide a memo that helps your ",
                        "instructors know what accommodations are appropriate.")
                .eP();
        htm.sP()
                .add("<b>If you are an incoming student</b>, you are welcome to contact the ",
                        "Precalculus Office (Weber 137; (970) 491-5761) and ask to speak with a director. ",
                        "The director will talk with you about the accommodations you have had in the ",
                        "past, and if you are eligible, can get you set up with extended time for the ",
                        "Math Placement Tool.")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "What if I don't place into any courses beyond MATH 101 / MATH 105 / STAT 100?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("All students are eligible for <b>MATH 101, Math in the Social Sciences</b>, ",
                "<b>MATH 105, Patterns of Phenomena</b>, and <b>STAT 100 - Statistical Literacy</b>. ",
                "The other entry-level math courses have enforced prerequisites that require you to ",
                "demonstrate a greater level of preparation on the Math Placement Tool than is ",
                "required for these three courses.");
        htm.sP().add("<b>Be sure to check the requirements for your degree program in the ",
                "University Catalog to verify that MATH 101, MATH 105, or STAT 100 is the right ",
                "course for you.</b> Many academic programs do not accept these coures toward ",
                "degree requirements. Also, they do NOT satisfy the prerequisites for any other ",
                "mathematics course at the University (such as <b>MATH 117, College Algebra I</b>). ",
                "If you are eligible for only MATH 101, MATH 105, and STAT 100 and need to take a ",
                "different math class, you can use your second Math Placement Tool attempt, or you ",
                "can complete the <a href='/elm-tutorial/' target='_blank'>ELM Tutorial ", EXT_LINK,
                "</a> to become eligible for MATH 117.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "Can I complete the Math Placement Tool multiple times?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP()
                .add("Yes. You are allowed to complete the Math Placement Tool twice. <b>Incoming ",
                        "students</b> can complete one attempt remotely (without proctoring required) ",
                        "prior to their term of admission.")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "When can I complete the Math Placement Tool?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("The Math Placement Tool can be completed on a <b>walk-in</b> basis in the ",
                "Precalculus Center (Weber 138) <b>whenever classes are in session</b>.").eP();
        htm.sP().add("<b>Incoming students</b> can complete the Math Placement Tool ",
                "remotely (without proctoring required) prior to their term of admission.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "How do I get my Math Placement results?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("Placement results are available <b>immediately</b> after the Math Placement ",
                "Tool is completed. You can re-check your placement results and see your available ",
                "placement options at any time by logging ",
                "in to the this web site with the [ Get Started ] button.").eP();
        htm.sP().add("The Mathematics Department cannot give results over the telephone because ",
                "of federal law regarding privacy of information. Questions concerning exam results ",
                "should be directed to the Precalculus Center (Weber 137; (970) 491-5761).").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "How long is the Math Placement Tool?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP()
                .add("The Math Placement Tool consists of 50 multiple choice and multiple response ",
                        "questions. The time limit is 2 hours and 20 minutes.")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "What material does Math Placement cover?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("Math Placement covers pre-algebra and algebra, trigonometry, and ",
                "logarithmic and exponential functions.</p>");
        htm.sP().add("The Mathematics Department has prepared <a onclick='nav7()'>interactive ",
                        "review materials and practice questions</a> for all topics covered by Math Placement.")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "Can I use a calculator on the Math Placement Tool? What else will I need?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP()
                .add("Yes, you are expected to use a calculator. If you complete the Math Placement ",
                        "Tool in the Precalculus Center, you must use an on-screen TI-84 calculator ",
                        "provided on the testing computers in the Precalculus Center.  You will also ",
                        "need a pencil or pen, and your CSU RamCard (ID card). <b>Reference materials ",
                        "are not permitted.</b>")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "Is there a charge for completing the Math Placement Tool?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().add("A one-time processing fee of $15 is charged to your student account the ",
                "first time you complete the Math Placement Tool. There is no charge for your second ",
                "attempt, or if you take advantage of other placement activities like the ELM ",
                "Tutorial or Precalculus Tutorial.").eP();
        htm.eDiv(); // accordionpanel

        htm.addln("<button class='accordion'>",
                "Can I earn challenge credit for Precalculus courses?", //
                "</button>");
        htm.sDiv("accordionpanel");
        htm.sP().addln("Yes - the Department of Mathematics offers Challenge Exams for these ",
                "Precalculus courses:").eP();
        htm.addln(" <ul style='margin-left:10pt'>");
        htm.addln(" <li>MATH 117 - College Algebra in Context I</li>");
        htm.addln(" <li>MATH 118 - College Algebra in Context II</li>");
        htm.addln(" <li>MATH 124 - Logarithmic and Exponential Functions</li>");
        htm.addln(" <li>MATH 125 - Numerical Trigonometry</li>");
        htm.addln(" <li>MATH 126 - Analytic Trigonometry</li>");
        htm.addln(" </ul>");
        htm.sP().addln(
                        "These exams can be taken in the Precalculus center.  Students must have satisfied ",
                        "the prerequisites for a course in order to challenge that course, and a course ",
                        "may not be challenged while a student is enrolled in that course. Challenging a ",
                        "course may be done one time, and there is a $20 processing fee charged for each ",
                        "challenge exam.  If you have questions about challenging a Precalculus course, ",
                        "please speak with the staff in the Precalculus Center (Weber 137; (970) 491-5761).")
                .eP();
        htm.eDiv(); // accordionpanel

        htm.eDiv(); // indent0

        htm.div("vgap");

        htm.sP("shaded2")
                .add("Didn't find your answer?  Send an email to ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                        "precalc_math@colostate.edu</a>.")
                .eP();
    }

    /**
     * Emits JavaScript scripts.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPlacementScripts(final HtmlBuilder htm) {

        htm.addln("<script>");

        htm.addln("var acc = document.getElementsByClassName('accordion');");
        htm.addln("var i;");
        htm.addln("for (i = 0; i < acc.length; i++) {");
        htm.addln("  acc[i].addEventListener(\"click\", function() {");
        htm.addln("    this.classList.toggle(\"accordionactive\");");
        htm.addln("    var panel = this.nextElementSibling;");
        htm.addln("    if (panel.style.display === \"block\") {");
        htm.addln("      panel.style.display = \"none\";");
        htm.addln("    } else {");
        htm.addln("      panel.style.display = \"block\";");
        htm.addln("    }");
        htm.addln("  });");
        htm.addln("}");

        htm.addln("function hideBlocks() {");
        htm.addln("  document.getElementById('block1').style.display='none';");
        htm.addln("  document.getElementById('block2').style.display='none';");
        htm.addln("  document.getElementById('block3').style.display='none';");
        htm.addln("  document.getElementById('block4').style.display='none';");
        htm.addln("  document.getElementById('block5').style.display='none';");
        htm.addln("  document.getElementById('block6').style.display='none';");
        htm.addln("  document.getElementById('block7').style.display='none';");
        htm.addln("  document.getElementById('block8').style.display='none';");
        htm.addln("  document.getElementById('btn1').className='btn';");
        htm.addln("  document.getElementById('btn2').className='fatbtn';");
        htm.addln("  document.getElementById('btn3').className='btn';");
        htm.addln("  document.getElementById('btn4').className='btn';");
        htm.addln("}");

        htm.addln("function nav1() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block1').style.display='block';");
        htm.addln("  document.getElementById('btn1').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav2() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block2').style.display='block';");
        htm.addln("  document.getElementById('btn2').className='fatbtnlit';");
        htm.addln("}");

        htm.addln("function nav3() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block3').style.display='block';");
        htm.addln("  document.getElementById('btn3').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav4() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block4').style.display='block';");
        htm.addln("  document.getElementById('btn4').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav5() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block5').style.display='block';");
        htm.addln("  document.getElementById('btn4').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav6() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block6').style.display='block';");
        htm.addln("  document.getElementById('btn4').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav7() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block7').style.display='block';");
        htm.addln("  document.getElementById('btn4').className='btnlit';");
        htm.addln("}");

        htm.addln("function nav8() {");
        htm.addln("  closemsg();");
        htm.addln("  hideBlocks();");
        htm.addln("  document.getElementById('block8').style.display='block';");
        htm.addln("  document.getElementById('btn4').className='btnlit';");
        htm.addln("}");

        htm.addln("function navmsg1() {");
        htm.addln("  document.getElementById('msg1').className='messagelit';");
        htm.addln("  document.getElementById('msg2').className='message';");
        htm.addln("  document.getElementById('pop1').style.display='block';");
        htm.addln("  document.getElementById('pop2').style.display='none';");
        htm.addln("  document.getElementById('text').className='textdim';");
        htm.addln("  document.getElementById('text').style.pointerEvents='none';");
        htm.addln("  document.getElementById('cover').style.display='block';");
        htm.addln("}");

        htm.addln("function navmsg2() {");
        htm.addln("  document.getElementById('msg1').className='message';");
        htm.addln("  document.getElementById('msg2').className='messagelit';");
        htm.addln("  document.getElementById('pop1').style.display='none';");
        htm.addln("  document.getElementById('pop2').style.display='block';");
        htm.addln("  document.getElementById('text').className='textdim';");
        htm.addln("  document.getElementById('text').style.pointerEvents='none';");
        htm.addln("  document.getElementById('cover').style.display='block';");
        htm.addln("}");

        htm.addln("function closemsg() {");
        htm.addln("  document.getElementById('msg1').className='message';");
        htm.addln("  document.getElementById('msg2').className='message';");
        htm.addln("  document.getElementById('pop1').style.display='none';");
        htm.addln("  document.getElementById('pop2').style.display='none';");
        htm.addln("  document.getElementById('text').className='text';");
        htm.addln("  document.getElementById('text').style.pointerEvents='auto';");
        htm.addln("  document.getElementById('cover').style.display='none';");
        htm.addln("}");

        htm.addln("</script>");
    }
}
