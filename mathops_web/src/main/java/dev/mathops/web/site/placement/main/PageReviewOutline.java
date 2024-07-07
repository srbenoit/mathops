package dev.mathops.web.site.placement.main;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Generates the content of the web page that displays the outline of a course tailored to a student's position and
 * status in the course.
 */
enum PageReviewOutline {
    ;

    /** Zero-length array used in construction of other arrays. */
    private static final RawCuobjective[] ZERO_LEN_CUOBJ_ARR = new RawCuobjective[0];

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session != null) {
            logStudentAccess(cache, session);
        }

        final HtmlBuilder htm = MPPage.startReviewPage2(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        final TermRec active = cache.getSystemData().getActiveTerm();

        final RawCunit[] units = queryUnits(cache, active.term);
        final RawCuobjective[][] objectives = queryObjectives(cache);
        final RawLesson[][] lessons = queryLessons(cache, objectives);
        final RawLessonComponent[][][] components = queryComponents(cache, objectives);

        final int numObj = objectives.length;
        for (int i = 0; i < numObj; ++i) {
            if (units[i] != null && objectives[i] != null) {
                doUnitHeading(units[i], htm);
                doUnitLessons(units[i], objectives[i], lessons[i], components[i], htm);

                htm.div("vgap");
                htm.hr();
            }
        }

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Logs student access to the site - used to drive a checkmark in the welcome site as part of the placement
     * process.
     *
     * @param cache   the data cache
     * @param session the user session
     * @throws SQLException if there is an error accessing the database
     */
    private static void logStudentAccess(final Cache cache, final ImmutableSessionInfo session) throws SQLException {

        final String studentId = session.getEffectiveUserId();

        if (studentId != null && session.actAsUserId == null) {
            // If we don't have a record of this user accessing the review site, create one.

            final List<RawStmathplan> responses = RawStmathplanLogic.queryLatestByStudentPage(cache, studentId,
                    MathPlanConstants.REVIEWED_PROFILE);

            if (responses.isEmpty()) {
                final RawStudent stu = RawStudentLogic.query(cache, studentId, false);

                if (stu != null) {
                    final String aplnTermStr = stu.aplnTerm == null ? null : stu.aplnTerm.shortString;

                    final LocalDateTime when = session.getNow().toLocalDateTime();

                    final RawStmathplan log = new RawStmathplan(studentId, stu.pidm, aplnTermStr,
                            MathPlanConstants.REVIEWED_PROFILE, when.toLocalDate(), Integer.valueOf(1), "Y",
                            Integer.valueOf(TemporalUtils.minuteOfDay(when)), Long.valueOf(session.loginSessionTag));

                    RawStmathplanLogic.INSTANCE.insert(cache, log);
                }

            }
        }
    }

    /**
     * Queries the course unit objectives for the review course.
     *
     * @param cache     the data cache
     * @param activeKey the active term key
     * @return an array of course unit objectives, indexed by [unit][objective]
     * @throws SQLException if there is an error accessing the database
     */
    private static RawCunit[] queryUnits(final Cache cache, final TermKey activeKey)
            throws SQLException {

        final List<RawCunit> all = cache.getSystemData().getCourseUnits("M 100R", activeKey);

        int max = 0;
        for (final RawCunit unit : all) {
            max = Math.max(max, unit.unit.intValue());
        }

        final RawCunit[] result = new RawCunit[max + 1];
        for (final RawCunit unit : all) {
            result[unit.unit.intValue()] = unit;
        }

        return result;
    }

    /**
     * Queries the course unit objectives for the review course.
     *
     * @param cache the data cache
     * @return an array of course unit objectives, indexed by [unit][objective]
     * @throws SQLException if there is an error accessing the database
     */
    private static RawCuobjective[][] queryObjectives(final Cache cache) throws SQLException {

        final RawCuobjective[][] result;

        final SystemData systemData = cache.getSystemData();
        final RawCourse course = systemData.getCourse("M 100R");
        final TermRec active = systemData.getActiveTerm();

        if (course == null) {
            Log.warning("Failed to query course ", "M 100R");
            result = null;
        } else if (active == null) {
            Log.warning("Failed to query active term");
            result = null;
        } else {
            final int numUnits = course.nbrUnits.intValue();
            result = new RawCuobjective[numUnits + 1][];

            for (int i = 0; i <= numUnits; ++i) {
                final List<RawCuobjective> all = systemData.getCourseUnitObjectives("M 100R", Integer.valueOf(i),
                        active.term);

                result[i] = all.toArray(ZERO_LEN_CUOBJ_ARR);
                Arrays.sort(result[i]);
            }
        }

        return result;
    }

    /**
     * Queries the lessons for the review course.
     *
     * @param cache      the data cache
     * @param objectives the objectives
     * @return an array of course unit objectives, indexed by [unit][objective]
     */
    private static RawLesson[][] queryLessons(final Cache cache, final RawCuobjective[][] objectives) {

        final int numObj = objectives.length;
        final RawLesson[][] result = new RawLesson[numObj][];

        final SystemData systemData = cache.getSystemData();

        for (int i = 0; i < numObj; ++i) {
            if (objectives[i] != null) {
                final int innerLen = objectives[i].length;
                result[i] = new RawLesson[innerLen];

                for (int j = 0; j < innerLen; ++j) {
                    final String lessonId = objectives[i][j].lessonId;

                    result[i][j] = systemData.getLesson(lessonId);
                    if (result[i][j] == null) {
                        Log.warning("Unable to look up lesson ", lessonId);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Queries the lesson components for the review course.
     *
     * @param cache      the data cache
     * @param objectives the objectives
     * @return an array of course unit objectives, indexed by [unit][objective][]
     */
    private static RawLessonComponent[][][] queryComponents(final Cache cache, final RawCuobjective[][] objectives) {

        final int numObj = objectives.length;
        final RawLessonComponent[][][] result = new RawLessonComponent[numObj][][];

        final SystemData systemData = cache.getSystemData();

        for (int i = 0; i < numObj; ++i) {
            if (objectives[i] != null) {
                final int innerLen = objectives[i].length;
                result[i] = new RawLessonComponent[innerLen][];

                for (int j = 0; j < innerLen; ++j) {
                    final String lessonId = objectives[i][j].lessonId;

                    final List<RawLessonComponent> list = systemData.getLessonComponentsByLesson(lessonId);

                    int max = 0;
                    for (final RawLessonComponent test : list) {
                        max = Math.max(max, test.seqNbr.intValue());
                    }

                    result[i][j] = new RawLessonComponent[max + 1];

                    for (final RawLessonComponent test : list) {
                        result[i][j][test.seqNbr.intValue()] = test;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Present the header in the course outline for a unit.
     *
     * @param unit the unit
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitHeading(final RawCunit unit, final HtmlBuilder htm) {

        htm.sDiv(null, "style='float:left;padding-right:10px;'");
        htm.addln(" <img src='/images/orange2.png' alt=''/>");
        htm.eDiv();

        htm.sDiv();
        htm.add(" <h3><span class='green' style='position:relative;top:3px;'>");
        htm.add("Section ", unit.unit, ": ");
        if (unit.unitDesc != null) {
            htm.add(unit.unitDesc);
        }
        htm.addln("</span></h3>").eDiv();
    }

    /**
     * Present the list of lessons in a unit.
     *
     * @param unit       the unit
     * @param objectives the objectives
     * @param lessons    the lessons
     * @param components the lesson components
     * @param htm        the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitLessons(final RawCunit unit, final RawCuobjective[] objectives,
                                      final RawLesson[] lessons, final RawLessonComponent[][] components,
                                      final HtmlBuilder htm) {

        htm.sDiv("inset2");
        htm.addln("<table>");

        final int numObj = objectives.length;
        for (int i = 0; i < numObj; ++i) {

            boolean hasPre = false;

            final int innerLen = components[i].length;
            for (int j = 0; j < innerLen; ++j) {
                if (components[i][j] != null && "PREMED".equals(components[i][j].type)) {
                    hasPre = true;
                    break;
                }
            }

            if (hasPre) {
                for (int j = 0; j < innerLen; ++j) {
                    if (components[i][j] != null && "PREMED".equals(components[i][j].type)) {
                        final String xml = components[i][j].xmlData;
                        htm.add("<tr><td colspan='2' class='open' style='white-space:nowrap;'>");
                        htm.add(xml.replace("%%MODE%%", "practice"));
                        htm.addln("</td></tr>");
                    }
                }

                htm.add("<tr><td style='height:4px;' colspan='2'></td></tr>");
            }

            htm.addln("<tr>");

            htm.add("<td class='open' ", "style='text-align:right;font-family:factoria-medium,sans-serif;'>");
            htm.add(objectives[i].objective, ":&nbsp;");

            htm.add("</td><td style='white-space:nowrap;'>");
            htm.add("<a class='linkbtn' href='review_lesson.html?course=M 100R&unit=", unit.unit, "&lesson=",
                    objectives[i].objective, "'>", lessons[i].descr, "</a></td>");
            htm.addln("</tr>");
        }

        htm.addln("</table>");
        htm.eDiv();
    }
}
