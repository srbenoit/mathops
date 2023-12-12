package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;

import java.time.LocalDate;

/**
 * A menu that lists the courses a user is registered for, along with links to manage e-texts, and view a recommended
 * progress schedule.
 */
enum TutorialMenu {
    ;

    /**
     * Builds the menu based on the current logged-in user session and appends its HTML representation to an
     * {@code HtmlBuilder}.
     *
     * @param session the session
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     */
    static void buildMenu(final ImmutableSessionInfo session, final PrecalcTutorialSiteLogic logic,
                          final HtmlBuilder htm) {

        htm.addln("<nav class='menu'>");
        htm.sDiv("menubox");

        htm.addln("<a class='ulink' href='home.html'>Home</a>");
        htm.div("vgap0").hr().div("vgap0");
        htm.addln("<a class='ulink' target='_blank' href='/www/media/precalc_tutorial.pdf'>Student Guide</a>");
        htm.div("vgap0");
        htm.addln("<a class='ulink' href='onlinehelp.html'>Getting Help</a>");
        htm.div("vgap0");

        final LocalDate today = session.getNow().toLocalDate();
        if (logic.hasTutorAccess()) {
            htm.div("vgap0");
            htm.sDiv("coursebox");
            htm.sDiv(null, "style='text-align:left;'");

            htm.sH(1, "menu").add("Tutor Access:").eH(1);
            htm.sDiv("startmenu");
            emitAllCourseMenuItems(htm);
            htm.eDiv(); // startmenu

            htm.eDiv(); // alignleft
            htm.eDiv(); // coursebox
        } else if (logic.isEligible(today)) {
            htm.hr();
            htm.sH(1, "menu").add("Placement Information").eH(1);

            htm.addln("<a class='ulink' href='placement_report.html'>My Placement Results</a>");
            htm.div("vgap0");

            final RawCourse eligibleCourse = logic.getEligibleCourse();

            if (eligibleCourse == null) {
                htm.hr();
                htm.sP().add("You have already placed out ouf, or earned credit for, all five courses.").eP();
            } else {
                htm.div("vgap0");
                htm.sDiv("coursebox");
                htm.sDiv(null, "style='text-align:left;'");

                htm.sH(1, "menu").add("Available Tutorial").eH(1);
                htm.sDiv("startmenu");
                emitCourseMenuItem(eligibleCourse, htm);
                htm.eDiv(); // startmenu

                htm.eDiv(); // alignleft
                htm.eDiv(); // coursebox
            }
        } else {
            htm.hr();
            htm.sP().add("You are not eligible for a Precalculus Tutorial at this time.").eP();
        }

        htm.eDiv(); // menubox
        htm.addln("</nav>");
    }

    /**
     * Emits a menu item for each course with the label of the course on the left (which may be simple text or a link to
     * "course.html" depending on locked state) and an optional dot filler and link on the right labeled "My Status" to
     * "course_status.html".
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void emitAllCourseMenuItems(final HtmlBuilder htm) {

        htm.addln("&bull; <strong>Place out of MATH 117</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=M 1170&mode=course'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=M 1170'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");

        htm.addln("&bull; <strong>Place out of MATH 118</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=M 1180&mode=course'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=M 1180'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");

        htm.addln("&bull; <strong>Place out of MATH 124</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=M 1240&mode=course'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=M 1240'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");

        htm.addln("&bull; <strong>Place out of MATH 125</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=M 1250&mode=course'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=M 1250'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");

        htm.addln("&bull; <strong>Place out of MATH 126</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=M 1260&mode=course'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=M 1260'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");
    }

    /**
     * Emits a menu item for a course with the label of the course on the left (which may be simple text or a link to
     * "course.html" depending on locked state) and an optional dot filler and link on the right labeled "My Status" to
     * "course_status.html".
     *
     * @param eligibleCourse the course for which the student is eligible
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     */
    private static void emitCourseMenuItem(final RawCourse eligibleCourse, final HtmlBuilder htm) {

        final String eligCourseNumber = eligibleCourse.course;
        final String label;
        if (RawRecordConstants.M1170.equals(eligCourseNumber)) {
            label = "MATH 117";
        } else if (RawRecordConstants.M1180.equals(eligCourseNumber)) {
            label = "MATH 118";
        } else if (RawRecordConstants.M1240.equals(eligCourseNumber)) {
            label = "MATH 124";
        } else if (RawRecordConstants.M1250.equals(eligCourseNumber)) {
            label = "MATH 125";
        } else {
            label = "MATH 126";
        }

        htm.addln("&bull; <strong>Place out of ", label, "</strong> ");
        htm.sDiv("indent1");
        htm.add("<a class='smallbtn' href='course.html?course=", eligCourseNumber, "'>E-Text</a> ",
                "<a class='smallbtn' href='course_status.html?course=", eligCourseNumber, "'>My Status</a>");
        htm.eDiv(); // indent1
        htm.div("vgap0");
    }
}
