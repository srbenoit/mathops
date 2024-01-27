package dev.mathops.web.site.admin.genadmin.reports;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A report of course exam activity.
 */
public enum PageCourseExamsReport {
    ;

    /**
     * Appends an SVG histogram of placement submissions for the last 7 days by 2-hour block (84 columns).
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     */
    static void appendHistogram(final Cache cache, final HtmlBuilder htm) {

        LocalDate today = LocalDate.now();

        final List<List<RawStexam>> history = new ArrayList<>(7);

        try {
            RawStexamLogic.getHistory(cache, history, 7, today, RawRecordConstants.M100U,
                    RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                    RawRecordConstants.M125, RawRecordConstants.M126);

            // Graph will show 84 columns, 3 units wide each, so 252 units inside a 1-unit border
            htm.addln("<svg width='100%' height='100%' viewBox='0 0 254 127' preserveAspectRatio='xMinYMid'>");

            // Alternating colors to delineate days of the week
            htm.addln(" <rect x='1' y='1' width='36' height='125' fill='#f0f2da' stroke='none'/>");
            htm.addln(" <rect x='37' y='1' width='36' height='125' fill='#ffffff' stroke='none'/>");
            htm.addln(" <rect x='73' y='1' width='36' height='125' fill='#f0f2da' stroke='none'/>");
            htm.addln(" <rect x='109' y='1' width='36' height='125' fill='#ffffff' stroke='none'/>");
            htm.addln(" <rect x='145' y='1' width='36' height='125' fill='#f0f2da' stroke='none'/>");
            htm.addln(" <rect x='181' y='1' width='36' height='125' fill='#ffffff' stroke='none'/>");
            htm.addln(" <rect x='217' y='1' width='36' height='125' fill='#f0f2da' stroke='none'/>");

            // Day-of-week labels
            today = today.minus(Period.ofDays(6));
            for (int i = 0; i < 7; ++i) {
                final String lbl = today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                final int x = 36 * i + 7;
                htm.addln(" <text x='", Integer.toString(x), "' y='13' font-size='10' fill='#080'>", lbl, "</text>");
                today = today.plus(Period.ofDays(1));
            }

            // Border...
            htm.addln(" <rect x='0' y='0' width='254' height='127' fill='none' stroke='black' stroke-width='1'/>");

            // Current hour...
            final int nowColumn = LocalTime.now().getHour() / 2;
            final float nowX = 1.5f + (float) (3 * (72 + nowColumn));
            htm.addln(" <rect x='", Float.toString(nowX),
                    "' y='1' width='2' height='125' fill='#caccb8' stroke='none'/>");

            final int numColumns = 7 * 12;
            final int[] columns = new int[numColumns];
            for (int day = 0; day < 7; ++day) {
                final List<RawStexam> daily = history.get(day);
                for (int hour = 0; hour < 24; hour += 2) {
                    final int col = day * 12 + hour / 2;
                    for (final RawStexam attempt : daily) {
                        final LocalDateTime fin = attempt.getFinishDateTime();
                        if (fin != null) {
                            final int attemptHour = fin.getHour();
                            if (attemptHour >= hour && attemptHour < hour + 2) {
                                columns[col]++;
                            }
                        }
                    }
                }
            }

            int max = 0;
            for (int i = 0; i < numColumns; ++i) {
                max = Math.max(max, columns[i]);
            }

            if (max == 0) {
                htm.addln(" <text x='10' y='22' font-size='10' fill='black'>No activity.</text>");
            } else {
                final int step;
                if (max <= 20) {
                    max = (max + 4) / 5 * 5;
                    step = 5;
                } else if (max <= 50) {
                    max = (max + 9) / 10 * 10;
                    step = 10;
                } else if (max <= 100) {
                    max = (max + 19) / 20 * 20;
                    step = 20;
                } else if (max <= 200) {
                    max = (max + 39) / 40 * 40;
                    step = 40;
                } else if (max <= 500) {
                    max = (max + 99) / 100 * 100;
                    step = 100;
                } else if (max <= 1000) {
                    max = (max + 199) / 200 * 200;
                    step = 200;
                } else {
                    max = (max + 499) / 500 * 500;
                    step = 500;
                }

                // Horizontal level lines with vertical scale labeling
                for (int i = step; i < max; i += step) {
                    final float y = 126.0f - 125.0f * (float) i / (float) max;
                    final float texty = y - 2.0f;
                    htm.addln(" <line fill='none' stroke='#666' stroke-width='.5' x1='1' x2='252' y1='",
                            Float.toString(y), "' y2='", Float.toString(y), "'/>");
                    htm.addln(" <text x='4' y='", Float.toString(texty), "' font-size='10' fill='black'>",
                            Integer.toString(i), "</text>");
                }

                final float dx = 252.0f / (float) numColumns;
                for (int i = 0; i < numColumns; ++i) {
                    final float leftx = 1.5f + (float) i * dx;
                    final float height = 125.0f * (float) columns[i] / (float) max;
                    final float top = 126.0f - height;

                    htm.addln(" <rect x='", Float.toString(leftx), "' y='", Float.toString(top), "' width='2' height='",
                            Float.toString(height), "' fill='#1e4d2b' stroke='none'/>");
                }
            }
        } catch (final SQLException ex) {
            htm.addln(" <text x='10' y='22' font-size='12' fill='red'>", ex.getMessage(), "</text>");
        }

        htm.addln("</svg>");
    }

    /**
     * Presents the report on ELM activity.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.MONITOR_SYSTEM, htm);

        htm.addln("<h1 class='bar'>Report: Course Exam Activity</h1>");

        final LocalDate today = LocalDate.now();

        final List<List<RawStexam>> history = new ArrayList<>(7);

        try {
            RawStexamLogic.getHistory(cache, history, 7, today, RawRecordConstants.M100U,
                    RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                    RawRecordConstants.M125, RawRecordConstants.M126);

            LocalDate dt = today;

            for (int i = 6; i >= 0; --i) {
                final List<RawStexam> list = history.get(i);

                htm.sH(3).add(dt.format(TemporalUtils.FMT_WMDY), " (", Integer.toString(list.size()), " exams)").eH(3);

                htm.addln("<table class='report'>");
                htm.add("<tr>");
                htm.add("<th>Course</th>");
                htm.add("<th>Student ID</th>");
                htm.add("<th>Serial #</th>");
                htm.add("<th>Unit</th>");
                htm.add("<th>Exam</th>");
                htm.add("<th>Started</th>");
                htm.add("<th>Finished</th>");
                htm.add("<th>Type</th>");
                htm.add("<th>Score</th>");
                htm.add("<th>Passed</th>");
                htm.add("<th>Source</th>");
                htm.add("</tr>");

                for (final RawStexam row : list) {
                    final LocalDateTime start = row.getStartDateTime();
                    final LocalDateTime fin = row.getFinishDateTime();

                    htm.add("<tr>");
                    htm.add("<td>", row.course, "</td>");
                    htm.add("<td>", row.stuId, "</td>");
                    htm.add("<td>", row.serialNbr, "</td>");
                    htm.add("<td>", row.unit, "</td>");
                    htm.add("<td>", row.version, "</td>");
                    htm.add("<td>", start == null ? "N/A" : start.format(TemporalUtils.FMT_HM_A), "</td>");
                    htm.add("<td>", fin == null ? "N/A" : fin.format(TemporalUtils.FMT_HM_A), "</td>");
                    htm.add("<td>", RawExamLogic.getExamTypeName(row.examType), "</td>");
                    htm.add("<td>", row.examScore, "</td>");
                    htm.add("<td>", row.passed, "</td>");
                    htm.add("<td>", row.examSource, "</td>");
                    htm.add("</tr>");
                }
                htm.addln("</table>");
                dt = dt.minusDays(1L);
            }
        } catch (final SQLException ex) {
            htm.sP().add(ex.getMessage()).eP();
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
