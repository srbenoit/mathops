package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the outline and E-text content for a single standards-based course. This is an outline page with a
 * list of modules, each with its status.
 *
 * <p>
 * It is assumed that this page can only be accessed by someone who has passed the user's exam and has legitimate access
 * to the e-text. This page does not check those conditions.
 */
public enum PageModules {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
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
            presentModulesPage(cache, site, req, resp, session, registration);
        }
    }

    /**
     * Presents the list of course modules.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentModulesPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session,
                                   final RawStcourse registration) throws IOException, SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        final List<RawCsection> csections = RawCsectionLogic.queryByTerm(cache, active.term);
        final List<RawCourse> courses = RawCourseLogic.queryAll(cache);

        RawCsection csection = null;
        for (final RawCsection test : csections) {
            if (registration.course.equals(test.course) && registration.sect.equals(test.sect)) {
                csection = test;
                break;
            }
        }
        RawCourse course = null;
        for (final RawCourse test : courses) {
            if (registration.course.equals(test.course)) {
                course = test;
                break;
            }
        }

        if (csection == null || course == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course.course, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            final String courseId = registration.course;

            // TODO: Make modules data-driven

            if (RawRecordConstants.MATH122.equals(courseId)) {
                presentMATH122Modules(cache, site, req, resp, session, registration, htm);
            } else if (RawRecordConstants.MATH125.equals(courseId)) {
                presentMATH125Modules(cache, site, req, resp, session, registration, htm);
            } else if (RawRecordConstants.MATH126.equals(courseId)) {
                presentMATH126Modules(cache, site, req, resp, session, registration, htm);
            }

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Presents the list of course modules for MATH 122.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMATH122Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

        final String urlCourse = URLEncoder.encode(registration.course, StandardCharsets.UTF_8);

        emitIntroModule(htm, urlCourse);

    }

    /**
     * Presents the list of course modules for MATH 125.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMATH125Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

        final String urlCourse = URLEncoder.encode(registration.course, StandardCharsets.UTF_8);

        emitIntroModule(htm, urlCourse);

        startModule(htm, "Module 1:&nbsp; Angles and Triangles");
        emitModuleItem(htm, "/www/images/etext/skills_review.png", "A brain made of connected shapes",
                "skills_review.html", "Skills Review");

        emitModuleItem(htm, "/www/images/etext/c41-thumb.png", "A wooden architectural feature with angles",
                "topic.html", "Topic 1:&nbsp; <span style='color:#D9782D'>Angles</span>",
                new ModuleItemChecklistEntry("Homeworks", false),
                new ModuleItemChecklistEntry("Complete Learning Targets", false));

        emitModuleItem(htm, "/www/images/etext/triangles_thumb.png", "A structure made of triangles",
                "topic.html", "Topic 2:&nbsp; <span style='color:#D9782D'>Triangles</span>",
                new ModuleItemChecklistEntry("Homeworks", false),
                new ModuleItemChecklistEntry("Complete Learning Targets", false));
        endModule(htm);

        startModule(htm, "Module 2:&nbsp; The Unit Circle and Trigonometric Functions");
        emitModuleItem(htm, "/www/images/etext/skills_review.png", "A brain made of connected shapes",
                "skills_review.html", "Skills Review");
        emitModuleItem(htm, "/www/images/etext/c42-thumb.png",
                "A person standing in a circle painted on pavement with shadow extending from its center",
                "topic.html", "Topic 3:&nbsp; <span style='color:#D9782D'>The Unit Circle</span>");
        emitModuleItem(htm, "/www/images/etext/c43-thumb.png",
                "A woman exercising with a heavy rope in a wave shape ",
                "topic.html", "Topic 4:&nbsp; <span style='color:#D9782D'>The Trigonometric Functions</span>");
        endModule(htm);

        startModule(htm, "Module 3:&nbsp; Transformations, Modeling, and Right Triangle Relationships");
        emitModuleItem(htm, "/www/images/etext/skills_review.png", "A brain made of connected shapes",
                "skills_review.html", "Skills Review");
        emitModuleItem(htm, "/www/images/etext/c44-thumb.png", "Copper strips in wave shapes", "topic.html",
                "Topic 5:&nbsp; <span style='color:#D9782D'>Modeling with Trigonometric Functions</span>");
        emitModuleItem(htm, "/www/images/etext/c46-thumb.png", "A truss made up of right triangles", "topic.html",
                "Topic 6:&nbsp; <span style='color:#D9782D'>Right Triangle Relationships</span>");
        endModule(htm);

        startModule(htm, "Module 4:&nbsp; Solving Problems with Triangles");
        emitModuleItem(htm, "/www/images/etext/skills_review.png", "A brain made of connected shapes",
                "skills_review.html", "Skills Review");
        emitModuleItem(htm, "/www/images/etext/c47-thumb.png",
                "A scene with a glass sphere that inverts the image of what lies behind it", "topic.html",
                "Topic 7:&nbsp; <span style='color:#D9782D'>Inverse Trigonometric Functions</span>");
        emitModuleItem(htm, "/www/images/etext/c48-thumb.png",
                "An indoor space with triangular architecture", "topic.html",
                "Topic 8:&nbsp; <span style='color:#D9782D'>The Law of Sines and the Law of Cosines</span>");
        endModule(htm);
    }

    /**
     * Presents the list of course modules for MATH 126.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMATH126Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

        final String urlCourse = URLEncoder.encode(registration.course, StandardCharsets.UTF_8);

        emitIntroModule(htm, urlCourse);

    }

    /**
     * Emits the course number, title, and section number.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param course   the course record
     * @param csection the course section record
     */
    private static void emitCourseTitleAndSection(final HtmlBuilder htm, final RawCourse course,
                                                  final RawCsection csection) {

        htm.sDiv(null, "style='margin:0 24px; border-bottom:1px solid #C7CDD1;'");
        htm.sH(1, "title");
        if ("Y".equals(csection.courseLabelShown)) {
            htm.add(course.courseLabel);
            htm.add(": ");
        }
        htm.add("<span style='color:#D9782D'>", course.courseName, "</span>");
        htm.br().add("<small>Section ", csection.sect, "</small>");
        htm.eDiv();
    }

    /**
     * Emits the "Introduction" module with the "Start Here" and "How to Successfully Navigate this Course" items.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param urlCourse the course ID in a form that can be used in a URL parameter
     */
    private static void emitIntroModule(final HtmlBuilder htm, final String urlCourse) {

        startModule(htm, "Introduction");

        emitModuleItem(htm, "/www/images/etext/start-thumb.png", "Starting line of race track",
                "start_here.html?course=" + urlCourse, "Start Here",
                new ModuleItemChecklistEntry("Set Account Preferences", true));

        emitModuleItem(htm, "/www/images/etext/navigation-thumb.png", "Man at wheel of ship at sea",
                "navigating.html?course=" + urlCourse, "How to Successfully Navigate this Course",
                new ModuleItemChecklistEntry("Syllabus Quiz", false));

        endModule(htm);
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the module title
     */
    private static void startModule(final HtmlBuilder htm, final String title) {

        htm.addln("<details class='module'>");
        htm.addln("  <summary class='module-summary'>", title, "</summary>");
    }

    /**
     * Emits the HTML to end a module.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endModule(final HtmlBuilder htm) {

        htm.addln("</details>");
    }

    /**
     * Emits a module item.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param thumbImage    the thumbnail image
     * @param thumbImageAlt the ALT text for the thumbnail image
     * @param href          the link reference
     * @param title         the item title
     * @param checklist     an option list of checklist items
     */
    private static void emitModuleItem(final HtmlBuilder htm, final String thumbImage, final String thumbImageAlt,
                                       final String href, final String title,
                                       final ModuleItemChecklistEntry... checklist) {

        htm.sDiv("module-item");

        if (thumbImage != null) {
            htm.addln("<img class='module-thumb' src='", thumbImage, "' alt='", thumbImageAlt, "'/>");
        }

        htm.sDiv("module-title");
        htm.addln("<a class='ulink2' href='", href, "'>", title, "</a>");
        if (checklist != null && checklist.length > 0) {
            htm.br();
            htm.add("<div style='display:inline-block; width:20px; height:2px;'></div>");
            for (final ModuleItemChecklistEntry entry : checklist) {
                htm.add("<img class='module-item-checkbox' src='/www/images/etext/",
                        (entry.checked() ? "box_checked_26.png" : "box_unchecked_26.png"),
                        "'/> ", entry.label());
                htm.add("<div style='display:inline-block; width:15px; height:1px;'></div>");
            }
        }
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * A record with data for a checklist item within a module item.
     *
     * @param label   the label
     * @param checked true if the checkbox is checked (the item has been completed)
     */
    private record ModuleItemChecklistEntry(String label, boolean checked) {
    }
}
