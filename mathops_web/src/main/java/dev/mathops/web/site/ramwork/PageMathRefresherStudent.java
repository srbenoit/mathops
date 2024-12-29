package dev.mathops.web.site.ramwork;

import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStlessonAssignLogic;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawStlessonAssign;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that shows the "CSU Math Refresher" entry point for students, with a list of the assigned and
 * active modules.
 */
enum PageMathRefresherStudent {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startEmptyPage(htm, Res.get(Res.SITE_TITLE), true);

            final String stuId = session.getEffectiveUserId();

            htm.sH(1).add("CSU Math Refresher").eH(1);

            // TODO: Query for the list of modules to which this student has been assigned.
            // 'lesson' holds the lessons that can be assigned
            // 'lesson_component' holds the contents of each lesson
            // 'stlesson_assign' holds assignments of lessons to students
            // 'stlesson_component' holds the status associated with each component

            final List<RawStlessonAssign> allAssigns = RawStlessonAssignLogic.queryByStudent(cache, stuId);

            final LocalDateTime now = session.getNow().toLocalDateTime();

            final int count = allAssigns.size();
            final Collection<RawStlessonAssign> closed = new ArrayList<>(count);
            final Collection<RawStlessonAssign> active = new ArrayList<>(count);
            final Collection<RawStlessonAssign> future = new ArrayList<>(count);
            final Map<String, RawLesson> lessons = new HashMap<>(count);

            final SystemData systemData = cache.getSystemData();

            for (final RawStlessonAssign row : allAssigns) {
                final RawLesson lesson = systemData.getLesson(row.lessonId);

                // Make sure row should be shown
                if ((lesson != null) && (row.whenShown == null || row.whenShown.isBefore(now))) {

                    lessons.put(lesson.lessonId, lesson);

                    if (row.whenOpen == null || row.whenOpen.isBefore(now)) {
                        if (row.whenClosed == null || row.whenClosed.isBefore(now)) {
                            if (row.whenHidden != null && row.whenHidden.isAfter(now)) {
                                closed.add(row);
                            }
                        } else {
                            active.add(row);
                        }
                    } else {
                        future.add(row);
                    }
                }
            }

            // Present all "closed" modules with summary data and possible [ review ] actions
            if (!closed.isEmpty()) {
                htm.addln("<details>");
                htm.addln("<summary style='background:#f5f5f5;'>Past Assignments</summary>");
                htm.addln("<ul>");

                for (final RawStlessonAssign row : closed) {
                    final RawLesson lesson = lessons.get(row.lessonId);
                    htm.addln("<li><a href='' class='ulink'>", lesson.descr, "</a></li>");
                }

                htm.addln("</ul>");
                htm.addln("</details>");
            }

            // Present all "active" modules with date windows shown, allow module access
            if (!active.isEmpty()) {
                htm.addln("<details>");
                htm.addln("<summary style='background:#f5f5f5;'>Current Assignments</summary>");
                htm.addln("<ul>");

                for (final RawStlessonAssign row : active) {
                    final RawLesson lesson = lessons.get(row.lessonId);
                    htm.addln("<li><a href='' class='ulink'>", lesson.descr, "</a></li>");
                }

                htm.addln("</ul>");
                htm.addln("</details>");
            }

            // Present all "not yet active" modules, with date windows shown, preview access
            if (!future.isEmpty()) {
                htm.addln("<details>");
                htm.addln("<summary style='background:#f5f5f5;'>Future Assignments</summary>");
                htm.addln("<ul>");

                for (final RawStlessonAssign row : future) {
                    final RawLesson lesson = lessons.get(row.lessonId);
                    htm.addln("<li><a href='' class='ulink'>", lesson.descr, "</a></li>");
                }

                htm.addln("</ul>");
                htm.addln("</details>");
            }

            htm.eDiv();
            Page.endEmptyPage(htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
