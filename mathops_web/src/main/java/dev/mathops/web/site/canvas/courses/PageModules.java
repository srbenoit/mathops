package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
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
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final Metadata metadata) throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MetadataCourse metaCourse = metadata.getCourse(registration.course);
            if (metaCourse == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentModulesPage(cache, site, req, resp, session, registration, metaCourse);
            }
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
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentModulesPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session,
                                   final RawStcourse registration, final MetadataCourse metaCourse)
            throws IOException, SQLException {

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

        if (csection == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            htm.sH(2).add("Modules").eH(2);
            htm.hr();

            final String urlCourse = URLEncoder.encode(registration.course, StandardCharsets.UTF_8);

            emitIntroModule(htm, urlCourse);

            for (final MetadataModule metaModule : metaCourse.modules) {

                startModule(htm, metaModule.title);

                emitModuleItem(htm, "/www/images/etext/skills_review.png", "A brain made of connected shapes",
                        "review_M01.html", "Skills Review");

                for (final MetadataTopic metaTopic : metaModule.topics) {

                    emitModuleItem(htm, "/www/images/etext/c41-thumb.png", "A wooden architectural feature with angles",
                            "topic_M01_T01.html",
                            "Topic 1:&nbsp; <span style='color:#D9782D'>" + metaTopic.title + "</span>",
                            new ModuleItemChecklistEntry("Homeworks", false),
                            new ModuleItemChecklistEntry("Complete Learning Targets", false));
                }

                endModule(htm);
            }

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
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
