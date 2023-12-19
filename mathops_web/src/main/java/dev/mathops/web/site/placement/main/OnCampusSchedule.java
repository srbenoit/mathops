package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * The Math Placement site landing page, which does not require a login (but if the user is logged in, the display may
 * be customized).
 */
enum OnCampusSchedule {
    ;

    /**
     * Emits review information.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPlacementSchedule(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Math Placement Tool - On-Campus Schedule").eH(1);
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("shaded2");

        htm.div("vgap");
        htm.sH(3).add("General Information about the Math Placement Tool").eH(3);

        htm.sP("indent");
        htm.addln("The Math Placement Tool is offered on a \"walk-in\" basis in the Precalculus ",
                "Center (Weber 138) when classes are in session. This tool consists of 50 questions ",
                "covering topics in algebra, trigonometry, logarithms, and exponential functions. ",
                "Students have two hours and twenty minutes to complete the tool. An on-screen TI-84 ",
                "calculator is provided when completing the tool in the Precalculus Center. Reference ",
                "materials may not be used.");
        htm.eP();

        htm.sP("indent");
        htm.addln("The Math Placement Tool may be completed twice. Placement results are ",
                "available immediately. Incoming students can complete the Tool one time remotely ",
                "(with no proctoring required) prior to their term of admission.");
        htm.eP();

        htm.sP("indent");
        htm.addln("Students who have already completed the Math Placement Tool and do not place ",
                "into the math course required by their degree program may study and retry the Math ",
                "Placement Tool or may complete the Entry-Level Mathematics (ELM) Tutorial and take ",
                "the ELM Exam instead. The ELM Exam can be used to place into <b>MATH 117</b> only.");
        htm.eP();

        htm.div("vgap2");
        htm.sH(3).add("Do I need to complete the Math Placement Tool?").eH(3);

        htm.sP("indent");
        htm.addln("In general, most entering first-year students should complete the Math ",
                "Placement Tool to register for MATH 117 or above. This requirement is waived for ",
                "students who have scored 3, 4, or 5 on an AP Calculus Exam (either AB or BC), passed ",
                "an appropriate IB Exam, or have transfer credit in a math course at the level of ",
                "College Algebra or above. Transfer students who have not transferred any mathematics ",
                "courses to Colorado State may need to begin with Math Placement.");
        htm.eP();

        htm.sP("center");
        htm.addln("<a class='btn' onclick='nav5()'>",
                "Do I need Math Placement?</a>");
        htm.eP();

        htm.div("vgap2");
        htm.sH(3).add("Is there a fee for the Math Placement Tool?").eH(3);

        htm.sP("indent");
        htm.addln("A CSU processing fee of $15 is automatically charged to your student account ",
                "the first time you complete the Math Placement Tool. This is a one-time fee and ",
                "covers subsequent Math Placement attempts, the ELM Tutorial, and the Precalculus ",
                "Tutorial. If you need to complete the ELM Tutorial and take the ELM Exam, you will ",
                "not be charged again.");
        htm.eP();

        htm.div("vgap");
        htm.sH(3).add("Can I challenge a Precalculus course for credit?").eH(3);

        htm.sP("indent");
        htm.addln("Yes - there are course challenge exams in MATH 117, MATH 118, MATH 124, ",
                "MATH 125, and MATH 126.  These exams must be taken in the Precalculus Center ",
                "(Weber 138).  You may only challenge a course once, and there is a $20 fee for each ",
                "course challenged.  Passing a course challenge exam with a score of 16/20 or higher ",
                "earns credit for that course.  You may not challenge a course while enrolled in ",
                "that course, or if you have not yet satisfied the prerequisites for the course being ",
                "challenged.");
        htm.eP();

        htm.div("vgap2");
        htm.sH(3).add("What do I need to bring?").eH(3);

        htm.sP("indent");
        htm.addln(
                "To complete the Math Placement Tool, ELM Exam, or a course challenge exam in the ",
                "Precalculus Center, you need to bring a pencil or pen and identification that ",
                "includes your photo and student ID number.  Currently enrolled students are required ",
                "to bring their CSU RamCard. Incoming students who do not yet have a RamCard should ",
                "bring a photo ID (eg. driver's license) and stop in the Precalculus Center Office ",
                "(Weber 136) before entering the testing area. The testing computers in the ",
                "Precalculus Center will provide an on-screen TI-84 calculator.");
        htm.eP();

        htm.div("vgap");

        htm.sP("advice");
        htm.addln("For \"walk-in\" exams, plan to arrive at the Precalculus Center well before ",
                "the closing time listed below to be certain you will have the full time allowed for ",
                "the Math Placement Tool or ELM Exam.").br().br();
        htm.addln("The Precalculus Center testing area doors close 15 minutes prior to the ",
                "Center's closing time, and no new placement sessions or exams may be started after ",
                "doors are closed. All work must be submitted by the Center's closing time.");
        htm.eP();

        htm.div("vgap2");
        htm.sDiv("center");

        // TODO: Get the following from data, like the web site footer does

        htm.sTable("sched");
        htm.sTr().add("<th colspan='3'>Spring, 2024 - Precalculus Center, Weber 138").eTh().eTr();

        htm.sTr().sTd("c1")
                .add("Friday,&nbsp;January&nbsp;12").eTd()//
                .sTd("c2").add("1:00 pm - 4:00 pm").eTd() //
                .sTd("c3")
                .add("Math Placement Tool").br() //
                .add("ELM Exam").br() //
                .add("Precalculus Tutorial Exams").br() //
                .add("Course challenge exams").eTd().eTr();

        htm.sTr().sTd("c1")
                .add("Tuesday,&nbsp;January&nbsp;16&nbsp;- Friday,&nbsp;May&nbsp;3").br()
                .add("(<b>Closed</b> March 11 - 15)").eTd()//
                .sTd("c2").add("10:00 am - 8:00 pm Monday-Thursday<br>10:00 am - 4:00 pm Friday").eTd() //
                .sTd("c3")
                .add("Math Placement Tool").br() //
                .add("ELM Exam").br() //
                .add("Precalculus Tutorial Exams").br() //
                .add("Course challenge exams").eTd().eTr();

        htm.eTable();

        htm.div("vgap2");

        htm.sTable("sched");
        htm.sTr().add("<th colspan='3'>SUMMER, 2024 - Precalculus Center, Weber 138").eTh().eTr();

        htm.sTr().sTd("c1")
                .add("Monday,&nbsp;May&nbsp;13&nbsp;- Friday,&nbsp;August&nbsp;2").br()
                .add("(<b>Closed</b> May 27, June 19, and July 4").eTd()//
                .sTd("c2").add("10:00 am - 2:00 pm Monday-Friday").br() //
                .sTd("c3")
                .add("Math Placement Tool").br() //
                .add("ELM Exam").br() //
                .add("Precalculus Tutorial Exams").br() //
                .add("Course challenge exams").eTd().eTr();

        htm.eTable();

        htm.eDiv(); // center

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln("<button class='smallbtn' onClick='nav4()'>",
                "<strong>Back to General Math Placement Infomation</strong></button>");
        htm.eDiv(); // center

        htm.eDiv(); // shaded2
    }
}
