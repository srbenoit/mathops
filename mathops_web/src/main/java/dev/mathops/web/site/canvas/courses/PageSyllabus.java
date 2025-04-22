package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This page shows the "Syllabus" content.
 */
public enum PageSyllabus {
    ;

    /**
     * Starts the page that shows the status of all assignments and grades.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentSyllabus(cache, site, req, resp, session, registration, course);
            }
        }
    }

    /**
     * Presents the "Syllabus" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param course       the course object
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentSyllabus(final Cache cache, final CanvasSite site, final ServletRequest req,
                                final HttpServletResponse resp, final ImmutableSessionInfo session,
                                final RawStcourse registration, final StandardsCourseRec course)
            throws IOException, SQLException {

        final TermData termData = cache.getTermData();
        final StandardsCourseSectionRec section = termData.getStandardsCourseSection(registration.course,
                registration.sect);

        if (section == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            final SystemData systemData = cache.getSystemData();
            final TermRec active = systemData.getActiveTerm();

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.SYLLABUS);

            htm.sDiv("flexmain");

            emitCommonSyllabusHeader(htm, active, section);

            if ("Y".equals(section.online)) {
                final String sect = section.sect;

                if (sect.startsWith("8") || sect.startsWith("4")) {
                    emitCEOnlineSyllabus(htm, active, section);
                } else {
                    emitRIOnlineSyllabus(htm, active, section);
                }
            } else {
                emitFaceToFaceSyllabus(htm, active, section);
            }

            emitCommonSyllabusTrailer(htm);

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Emits the last part of the syllabus that is common between online and face-to-face sections.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitCommonSyllabusHeader(final HtmlBuilder htm, final TermRec active,
                                                 final StandardsCourseSectionRec section) {

        htm.sH(2).add("Syllabus - ", active.term.longString).eH(2);
        htm.hr();

        htm.sTable("grades indent");
        if (csection.instructor != null) {
            htm.sTr().sTh().add("Instructor: ").eTh().sTd().add(csection.instructor).eTd().eTr();
        }
        htm.sTr().sTh().add("Office: ").eTh().sTd().add("Precalculus Center (Weber 137)").eTd().eTr();
        htm.sTr().sTh().add("Email: ").eTh().sTd().add("precalc_math@colostate.edu").eTd().eTr();
        htm.sTr().sTh().add("Class Times: ").eTh().sTd().add("TBD").eTd().eTr();
        htm.sTr().sTh().add("Prerequisites: ").eTh().sTd().add("MATH 120 or MATH 124").eTd().eTr();
        htm.eTable();
        htm.hr();

        htm.sH(3).add("Course Description").eH(3);

        // TODO: Make this data-driven

        htm.sDiv("indent");
        if ("MATH 125".equals(csection.course)) {
            htm.sP().add("""
                    This course develops foundational Trigonometry beginning with angles and their relationships
                    and units of measure, then examining triangles geometrically, then focusing on right triangles and
                    Pythagorean relationship. We introduce the unit circle and its relationship with right triangles,
                    use this context to develop the trigonometric functions, then transform these functions to generate
                    models and explore applications.   We develop the right triangle side-angle relationships, introduce
                    inverse trigonometric functions, and develop the laws of sines and cosines, and finish the course
                    with an emphasis on solving application problems with these tools.""").eP();
        } else if ("MATH 126".equals(csection.course)) {
            htm.sP().add("""
                    This course develops analytic Trigonometry, starting with a survey of trigonometric identities
                    that will be useful in later Math courses and in applications.  We explore solutions to
                    trigonometric equations, and introduce polar coordinates and polar functions.  Finally, we develop
                    complex numbers and their trigonometric and polar representations, and then present application
                    contexts to practice problem solving.""").eP();
        } else {
            htm.sP().add("See the course catalog for a description of the material covered.").eP();
        }
        htm.eDiv(); // indent

        htm.sH(3).add("Textbook").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                This course uses an online textbook and an associated online homework system, which requires a nominal
                access code fee ($20).  The course participates in the CSU Bookstore's <b>First-day Access</b> program,
                in which the fee for this access code is automatically billed to your student account.  You must not
                "opt out" of this access code, or you will be unable to access the assignments needed to complete the
                course.""").eP();

        htm.sP().add("""
                There is no required physical textbook.  If you prefer to have a physical book as a study aid,
                we recommend the following as good Trigonometry references:""").eP();
        htm.addln("<ul>");
        htm.addln("  <li><em>Trigonometry, 11th ed.</em> (Ron Larson) ISBN: 9780357455210</li>");
        htm.addln("  <li><em>Algebra and Trigonometry, 4th ed.</em> (James Stewart, et. al.) ",
                "ISBN-13: 9781305071742</li>");
        htm.addln("</ul>");
        htm.eDiv(); // indent
    }

    private static void emitCEOnlineSyllabus(final HtmlBuilder htm, final TermRec active,
                                             final StandardsCourseSectionRec section) {

        htm.sH(3).add("Class Meetings and Delivery Mode").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                This is a "distance" section of the course.  You can complete all course requirements remotely.  There
                is no requirement to be on-campus for any aspect of the course.""").eP();

        htm.sP().add("""
                Course materials are presented online as pre-recorded videos, PDF documents, and web pages.  There are
                no "synchronous" lectures that require attendance at a specific date or time.""").eP();
        htm.eDiv(); // indent

        htm.sH(3).add("Homework Assignments and Exams").eH(3);

        htm.sDiv("indent");

        htm.sP().add("""
                All courses exams are available through an online proctoring system.  You must have a computer with a
                webcam and you will be asked to share your screen during the exam.  Exam sessions are recorded and
                reviewed by CSU personnel, but remain confidential and are not made available to anyone other than
                proctors who review videos to ensure no unauthorized assistance was used.  These videos are deleted
                after each semester.""").eP();
        htm.eDiv(); // indent
    }

    private static void emitRIOnlineSyllabus(final HtmlBuilder htm, final TermRec active,
                                             final StandardsCourseSectionRec section) {

        htm.sH(3).add("Class Meetings and Delivery Mode").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                This is a "hybrid" section of the course.  You can access all course materials online, but course
                exams must be taken on-campus.  This course cannot be completed without being on-campus to take course
                exams.  If you need to take the course in a completely remote format, you would need to register
                through CSU Online rather than through AriesWEB.""").eP();

        htm.sP().add("""
                Course materials are presented online as pre-recorded videos, PDF documents, and web pages.  There are
                no class meetings or "synchronous" lectures that require attendance at a specific date or time.
                """).eP();
        htm.eDiv(); // indent

        htm.sH(3).add("Homework Assignments and Exams").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                All courses exams will be taken in the Precalculus Center's testing area (Weber 138).  No appointment
                is needed, you can walk in and attempt an exam any time the center is open.  See the
                <a href='course.html'>Home</a> page for the hours of operation of the testing center.""");
        htm.eDiv(); // indent
    }

    private static void emitFaceToFaceSyllabus(final HtmlBuilder htm, final TermRec active,
                                               final StandardsCourseSectionRec section) {

        htm.sH(3).add("Class Meetings and Delivery Mode").eH(3);

        // TODO: How to get this information from data?

        htm.sDiv("indent");
        htm.sP().add("""
                This is a "face-to-face" section of the course.  You are expected to attend all scheduled class
                sessions.  This course cannot be completed without being on-campus to take course exams.  If you need
                to take the course in a completely remote format, you would need to register through CSU Online rather
                than through AriesWEB.""").eP();

        htm.sP().add("""
                Course materials will be presented in class.  Supplemental materials will also be provided online as
                pre-recorded videos, PDF documents, and web pages.
                """).eP();
        htm.eDiv(); // indent

        htm.sH(3).add("Homework Assignments and Exams").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                All exams in the course will be taken in the Precalculus Center's testing area (Weber 138).  No
                appointment is needed, you can walk in and attempt an exam any time the center is open.  See the
                <a href='course.html'>Home</a> page for the hours of operation of the testing center.""");
        htm.eDiv(); // indent
    }

    /**
     * Emits the last part of the syllabus that is common between online and face-to-face sections.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitCommonSyllabusTrailer(final HtmlBuilder htm) {

        htm.sDiv("indent");
        htm.sP().addln("<b>Learning Targets and Homework Assignments</b>");

        htm.addln("<ul>");
        htm.addln("  <li>The class is divided into eight modules.</li>");
        htm.addln("""
                <li>Each module has three learning targets, for a total of 24 learning targets in the course.</li>""");
        htm.addln("""
                <li>Every learning target has a short homework assignment that must be passed in order to unlock the
                learning target's questions on the course mastery exam.  You have unlimited attempts on these homework
                assignments.  They have no deadline and do not count toward your course grade, but they are necessary
                to unlock exam questions, which do have deadlines and do count toward the course grade.</li>""");
        htm.addln("</ul>");

        htm.sP().addln("<b>Course Mastery Exam</b>");

        htm.addln("<ul>");
        htm.addln("""
                <li>The Course Mastery Exam consists of 48 questions - two questions for each learning target.</li>""");
        htm.addln("""
                <li>You have unlimited attempts on the Course Mastery Exam, through the last day of classes.</li>""");
        htm.addln("""
                <li>As you complete learning target homework assignments, the corresponding questions on the mastery
                exam are unlocked, and you become eligible to attempt those questions.</li>""");
        htm.addln("""
                <li>Each module defines a due date for its questions on the mastery exam.  If you answer a question
                for a learning target correctly on or before the module due date, you receive <b>5 points</b>
                for that question.  If the question answered correctly, but only after the due date, you receive
                <b>4 points</b>.</li>""");
        htm.addln("""
                <li>If you have 8 or more learning targets whose questions are unlocked but not yet mastered, you will
                be required to master some of these learning targets to get this number below 8 before you can move
                to the next module in the course.  This is to prevent the situation where someone waits until the end
                of the semester to try mastering all the learning targets in the course.</li>""");
        htm.addln("</ul>");

        htm.sP().add("To pass the course, you must:").eP();
        htm.addln("<ul>");
        htm.addln("<li>Answer all Course Mastery Exam questions correctly for at least <b>18</b> learning targets, ",
                "AND</li>");
        htm.addln("<li>Answer all Course Mastery Exam questions correctly for all learning targets marked ",
                "<b>essential</b>.</li>");
        htm.addln("</ul>");
        htm.sP().add("""
                If you have met these two requirements, your course grade will be based on your point total.""").eP();

        htm.sTable("grades indent");
        htm.sTr().sTh().add("Point Range").eTh().sTh().add("Percentage").eTh().sTh().add("Grade Earned").eTh().eTr();
        htm.sTr().sTd().add("216 to 240: ").eTd().sTd().add("90% - 100%").eTd().sTd().add("A").eTd().eTr();
        htm.sTr().sTd().add("192 to 215: ").eTd().sTd().add("80% - 89.9%").eTd().sTd().add("B").eTd().eTr();
        htm.sTr().sTd().add("168 to 191: ").eTd().sTd().add("70% - 79.9%").eTd().sTd().add("C").eTd().eTr();
        htm.sTr().sTd().add("144 to 167: ").eTd().sTd().add("60% - 69.9%").eTd().sTd().add("D").eTd().eTr();
        htm.eTable();

        htm.sP().add("""
                If you have not met the requirements for completing the course, a <b>U</b> grade will be submitted.
                A grade of U does not affect your grade point average.""").eP();

        htm.sP().add("""
                A grade of <b>F</b> may be submitted if 3 or fewer learning targets are completed to encourage
                participation and engagement in the course.""").eP();

        htm.sP().add("""
                A grade of incomplete (I) will be considered only when circumstances beyond the student's control or
                that could not be anticipated prevented the student from completing the course requirements.""").eP();
        htm.eDiv(); // indent

        htm.sH(3).add("Policies on Absences").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                Absences due to university events (unscheduled closures due to weather, travel for University events
                like athletic competitions, travel for band, stock judging, etc.) are excused and due dates that
                conflict with the absence will be adjusted to allow students to complete work without loss of points.
                """).eP();

        htm.sP().add("""
                All students may request an extension of up to 2 days on any exam due date without documentation due
                to outside factors like minor illnesses, conflicts with exams in other courses, or family or personal
                emergencies.  No details need to be provided.  To request an extension of longer than 2 days, some
                form of documentation of the situation, or a letter of support from Student Case Management, the
                SAFE Center, the Student Disability Center, a doctor, or some other campus entity.
                """).eP();
        htm.eDiv(); // indent

        htm.sH(3).add("Accommodations").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                Students who have arrangements with the Student Disability Center for accommodations for exams
                or assignments should let the instructor know as soon as possible when the course begins so
                appropriate arrangements can be made.""");
        htm.eDiv(); // indent

        htm.sH(3).add("Collaboration, Cooperation, and Academic Integrity").eH(3);

        htm.sDiv("indent");
        htm.sP().add("""
                A University education is a pursuit of knowledge, and must be based on truth and integrity. Academic
                dishonesty undermines this foundation and diminishes the value of a University education, and is not
                acceptable.  You can expect your instructors to act with integrity and honesty and they will expect
                academic integrity and honesty from you as well.""").eP();

        htm.sP().add("""
                Collaboration and group discussion are encouraged and a useful tool to help you learn the material in
                the course.  However, homework assignments and exams MUST represent your own work to provide an accurate
                assessment of your understanding of the course materials.""").eP();

        htm.sP().add("""
                Exams are to be completed in a proctored setting with only allowed notes, and no other references, and
                without collaboration, unless the exam instructions specifically permit or provide some additional
                material.  Exams may include an honor pledge to affirm you have neither given nor received unauthorized
                assistance on the exam.""").eP();

        htm.sP().add("""
                Concerns about the course or any of the instructor's decisions that affect your participation and/or
                performance in the course should be discussed first with the instructor. Concerns regarding the course
                may also be discussed with Steve Benoit, the Director of Foundational Mathematics.""");

        htm.sP().add("""
                The University policies on Academic Integrity and on Grading and Grade Appeals and other applicable
                policies are published under ""Students' Rights" and "Students' Responsibilities" in Section 1.6 of the
                current CSU General Catalog, (http://catalog.colostate.edu).""").eP();
        htm.eDiv(); // indent
    }
}
