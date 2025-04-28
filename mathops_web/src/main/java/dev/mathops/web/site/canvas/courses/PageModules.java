package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.db.Cache;
import dev.mathops.db.course.MetadataCourseModule;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
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

import java.io.File;
import java.io.IOException;
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
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentModulesPage(cache, site, req, resp, session, registration, course);
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
     * @param course       the course object
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentModulesPage(final Cache cache, final CanvasSite site, final ServletRequest req,
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

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            htm.sH(2).add("Modules").eH(2);
            htm.hr();

            // Locate "media root" which is typically /opt/public/media
            final File wwwPath = PathList.getInstance().get(EPath.WWW_PATH);
            final File publicPath = wwwPath.getParentFile();
            final File mediaRoot = new File(publicPath, "media");

            emitCourseModules(cache, htm, registration, course, mediaRoot);

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Emits all course modules, including an introductory module.
     *
     * @param cache        the data cache
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param registration the student's registration record
     * @param course       the course object
     * @param mediaRoot    the root media directory relative to which the module path is specified
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseModules(final Cache cache, final HtmlBuilder htm, final RawStcourse registration,
                                          final StandardsCourseRec course, final File mediaRoot) throws SQLException {

        emitIntroModule(htm);

        final MainData mainData = cache.getMainData();
        final List<StandardsCourseModuleRec> modules = mainData.getStandardsCourseModules(course.courseId);

        for (final StandardsCourseModuleRec module : modules) {
            final MetadataCourseModule meta = new MetadataCourseModule(mediaRoot, module.modulePath,
                    module.moduleNbr);

            if (meta.isValid()) {
                emitModule(htm, module, meta);
            }
        }
    }

    /**
     * Emits the "Introduction" module with the "Start Here" and "How to Successfully Navigate this Course" items.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitIntroModule(final HtmlBuilder htm) {

        startModule(htm, null, "Introduction");

        emitChecklistModuleItem(htm, "/www/images/etext/start-thumb.png", "Starting line of race track",
                "start_here.html", "Start Here",
                new ModuleItemChecklistEntry("Set Account Preferences", true));

        emitChecklistModuleItem(htm, "/www/images/etext/navigation-thumb.png", "Man at wheel of ship at sea",
                "navigating.html", "How to Successfully Navigate this Course",
                new ModuleItemChecklistEntry("Syllabus Quiz", false));

        endModule(htm);
    }

    /**
     * Emits a single topic module.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param module the metadata describing the topic within the course
     * @param meta   loaded module metadata
     */
    private static void emitModule(final HtmlBuilder htm, final StandardsCourseModuleRec module,
                                   final MetadataCourseModule meta) {

        final String heading = "Module " + module.moduleNbr;
        startModule(htm, heading, meta.title);

        // The top-level Topic Module object in the web page has three items:
        // - E-Text Chapter: [title]
        // - Module Homework Assignments (with completion status)
        // - Learning Target Exams (with completion status)

        // The topic module with ID "M01" lives at "M01/module.html"
        final String modulePath = "M" + module.moduleNbr + "/module.html";

        if (meta.thumbnailFile == null) {
            emitChapterItem(htm, null, null, modulePath, meta.title);
        } else {
            final String imageUrl = "/media/" + meta.moduleRelPath + "/" + meta.thumbnailFile;
            emitChapterItem(htm, imageUrl, meta.thumbnailAltText, modulePath, meta.title);
        }

        // Required assignments, with status
        final String assignmentsPath = "M" + module.moduleNbr + "/assignments.html";
        emitChecklistModuleItem(htm, "/www/images/etext/required_assignment_thumb.png", "A student doing homework.",
                assignmentsPath, "Module Homework Assignments",
                new ModuleItemChecklistEntry("Assignment 1", false),
                new ModuleItemChecklistEntry("Assignment 2", false),
                new ModuleItemChecklistEntry("Assignment 3", false));

        // Learning Target Exams, with status
        final String examsPath = "M" + module.moduleNbr + "/targets.html";
        emitChecklistModuleItem(htm, "/www/images/etext/target_thumb.png", "A dartboard with several magnetic darts",
                examsPath, "Module Learning Targets",
                new ModuleItemChecklistEntry("Learning Target 1", false),
                new ModuleItemChecklistEntry("Learning Target 2", false),
                new ModuleItemChecklistEntry("Learning Target 3", false));

        endModule(htm);
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param heading the heading
     * @param title   the title
     */
    private static void startModule(final HtmlBuilder htm, final String heading, final String title) {

        htm.addln("<details class='module'>");
        htm.add("  <summary class='module-summary'>");

        if (heading == null) {
            htm.add(title);
        } else {
            htm.add(heading, ": <span style='color:#D9782D'>", title, "</span>");
        }

        htm.addln("</summary>");
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
     * Emits a module item with a heading and title.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param thumbImage    the thumbnail image
     * @param thumbImageAlt the ALT text for the thumbnail image
     * @param href          the link reference
     * @param title         the item title
     */
    private static void emitChapterItem(final HtmlBuilder htm, final String thumbImage, final String thumbImageAlt,
                                        final String href, final String title) {

        htm.sDiv("module-item");

        if (thumbImage != null) {
            htm.addln("<a href='", href, "'><img class='module-thumb' src='", thumbImage, "' alt='", thumbImageAlt,
                    "'/></a>");
        }

        htm.sDiv("module-title");
        if (title == null) {
            htm.addln("<span style='color:#D9782D'><a class='ulink2' href='", href, "'>Textbook Chapter</a></span>");
        } else {
            htm.addln("Textbook Chapter:").br();
            htm.addln("<div style='color:#D9782D; margin-top:4px; margin-left:20px'>",
                    "<a class='ulink2' href='", href, "'>", title, "</a></div>");
        }
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits a module item with a checklist.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param thumbImage    the thumbnail image
     * @param thumbImageAlt the ALT text for the thumbnail image
     * @param href          the link reference
     * @param title         the item title
     * @param checklist     an option list of checklist items
     */
    private static void emitChecklistModuleItem(final HtmlBuilder htm, final String thumbImage,
                                                final String thumbImageAlt, final String href, final String title,
                                                final ModuleItemChecklistEntry... checklist) {

        htm.sDiv("module-item");

        if (thumbImage != null) {
            htm.addln("<a href='", href, "'><img class='module-thumb' src='", thumbImage, "' alt='", thumbImageAlt,
                    "'/></a>");
        }

        htm.sDiv("module-title");
        htm.addln("<a class='ulink2' href='", href, "'>", title, "</a>");
        if (checklist != null && checklist.length > 0) {
            htm.br();
            for (final ModuleItemChecklistEntry entry : checklist) {
                htm.add("<div style='display:inline-block; width:20px; height:2px;'></div>");
                htm.add("<img class='module-item-checkbox' src='/www/images/etext/",
                        (entry.checked() ? "box_checked_18.png" : "box_unchecked_18.png"),
                        "'/>", entry.label());
                htm.br();
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
