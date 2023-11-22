package dev.mathops.web.site.course;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawCourse;
import dev.mathops.db.rawrecord.RawCsection;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.rec.StudentCourseMasteryRec;
import dev.mathops.db.reclogic.StudentCourseMasteryLogic;

import java.sql.SQLException;

/**
 * Generates the content of the web page that displays the outline of a standards-based course tailored to a student's
 * position and status in the course.
 */
enum PageStdsCourse {
    ;

    /**
     * Show the content of the course status page.
     *
     * @param cache    the data cache
     * @param course   the course
     * @param reg      the registration record
     * @param csection the course section record
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    static void masteryCoursePanel(final Cache cache, final RawCourse course, final RawStcourse reg,
                                   final RawCsection csection, final HtmlBuilder htm) {

        final String section = csection.sect;

        htm.sH(2, "title");
        if ("Y".equals(csection.courseLabelShown)) {
            htm.add(course.courseLabel);
            htm.add(": ");
        }
        htm.add("<span style='color:#D9782D'>", course.courseName, "</span>");
        if (section != null) {
            htm.br().add("<small>Section ", section, "</small>");
        }
        htm.eH(2).hr();

        emitMasteryCourseStatus(cache, reg, csection, htm);

        htm.addln("<a href='course_text.html?course=", course.course,
                "&mode=course'><img style='width:210px;margin-left:10px;' ",
                "src='/www/images/etext/textbook.png'/></a><br/>");

        htm.addln("<a style='width:202px;margin-left:10px;text-align:center;' ",
                "class='smallbtn' href='course_text.html?course=", course.course,
                "&mode=course'>", "Open Textbook", "</a>");

        // TODO: Schedule and next steps...
    }

    /**
     * Emits a display of the student's status in the course.
     *
     * @param cache    the data cache
     * @param reg      the registration record
     * @param csection the course section record
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    private static void emitMasteryCourseStatus(final Cache cache, final RawStcourse reg, final RawCsection csection,
                                                final HtmlBuilder htm) {

        Log.info("Status reg: " + reg.course, CoreConstants.SPC, reg.sect, CoreConstants.SPC, reg.paceOrder);

        if ("Y".equals(reg.iInProgress)) {
            // TODO: Incomplete status
        } else if (reg.paceOrder == null) {
            htm.sP();
            htm.addln("Unable to determine class schedule.");
            htm.eP();
        } else {
            try {
                final StudentCourseMasteryLogic masteryLogic = StudentCourseMasteryLogic.get(cache);
                StudentCourseMasteryRec courseMastery = masteryLogic.query(cache, reg.stuId, reg.course);
                if (courseMastery == null) {
                    // TODO: Query work record and build a correct row here.
                    courseMastery = new StudentCourseMasteryRec(reg.stuId, reg.course, Integer.valueOf(0),
                            Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), null, null);
                    masteryLogic.insert(cache, courseMastery);
                }

                final int targetsFirstHalf = courseMastery.nbrMasteredH1.intValue();
                final int targetsSecondHalf = courseMastery.nbrMasteredH2.intValue();
                final int targetsReachedTotal = targetsFirstHalf + targetsSecondHalf;
                final int totalPoints = courseMastery.score.intValue();
                final int minPassingPoints;
                if (csection.dMinScore != null) {
                    minPassingPoints = csection.dMinScore.intValue();
                } else if (csection.cMinScore != null) {
                    minPassingPoints = csection.cMinScore.intValue();
                } else if (csection.bMinScore != null) {
                    minPassingPoints = csection.bMinScore.intValue();
                } else {
                    minPassingPoints = csection.aMinScore.intValue();
                }

                htm.sDiv("hours");
                htm.addln("To pass this course, you must reach at least <b>24</b> learning targets ",
                        "(at least <b>12</b> from the first half of the course, and at least ",
                        "<b>12</b> from the second half).  Your grade will then be based on total ",
                        "points earned.");
                htm.eDiv();

                htm.sP();
                htm.addln("Learning Targets Reached: <b>" + targetsReachedTotal
                        + "</b> (out of 30 total)").br();

                // Bar chart showing number of standards mastered in each half of the course

                htm.addln("<svg width='444' height='37'>");
                htm.addln("<defs>");
                htm.addln(" <linearGradient id='grad1' x1='0%' y1='0%' x2='0%' y2='100%'>");
                htm.addln("  <stop offset='0%' style='stop-color:rgb(217, 120, 45)'/>");
                htm.addln("  <stop offset='35%' style='stop-color:rgb(244,218,198)'/>");
                htm.addln("  <stop offset='100%' style='stop-color:rgb(193, 104, 35)'/>");
                htm.addln(" </linearGradient>");
                htm.addln("</defs>");

                htm.addln("  <rect x='0.5' y='0.5' width='214' height='17' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln("  <rect x='1.5' y='1.5' width='213' height='16' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");
                htm.addln("  <rect x='2.5' y='2.5' width='211' height='14' ",
                        "style='fill:rgb(250,250,235);stroke-width:1px;stroke:black;'/>");

                htm.addln("  <rect x='228.5' y='0.5' width='214' height='17' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln("  <rect x='229.5' y='1.5' width='213' height='16' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");
                htm.addln("  <rect x='230.5' y='2.5' width='211' height='14' ",
                        "style='fill:rgb(250,250,235);stroke-width:1px;stroke:black;'/>");

                htm.addln("  <rect x='171' y='3' width='42' height='13' ",
                        "style='fill:rgb(120,255,120);stroke-width:0px;'/>");
                htm.addln("  <rect x='399' y='3' width='42' height='13' ",
                        "style='fill:rgb(120,255,120);stroke-width:0px;'/>");

                if (targetsFirstHalf > 0) {
                    final int w = 14 * targetsFirstHalf;
                    final String wStr = Integer.toString(w);
                    htm.addln("  <rect x='3' y='3' width='", wStr, "' height='13' ",
                            "style='fill:url(#grad1);stroke-width:0px;'/>");
                }

                if (targetsSecondHalf > 0) {
                    final int w = 14 * targetsSecondHalf;
                    final String wStr = Integer.toString(w);
                    htm.addln("  <rect x='231' y='3' width='", wStr, "' height='13' ",
                            "style='fill:url(#grad1);stroke-width:0px;'/>");
                }

                for (int i = 1; i < 15; ++i) {
                    final String x1 = Float.toString((float) (2 + 14 * i) - 0.5f);
                    if (i < targetsFirstHalf) {
                        htm.addln("  <line x1='", x1, "' y1='2.5' x2='", x1,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(80,80,80);'/>");
                    } else {
                        htm.addln("  <line x1='", x1, "' y1='2.5' x2='", x1,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(180,180,180);'/>");
                    }

                    final String x2 = Float.toString((float) (230 + 14 * i) - 0.5f);
                    if (i < targetsSecondHalf) {
                        htm.addln("  <line x1='", x2, "' y1='2.5' x2='", x2,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(80,80,80);'/>");
                    } else {
                        htm.addln("  <line x1='", x2, "' y1='2.5' x2='", x2,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(180,180,180);'/>");
                    }
                }

                htm.addln("  <text x='12' y='34' style='font-size:16px;'>"
                        + targetsFirstHalf + " in first half</text>");

                htm.addln("  <text x='241' y='34' style='font-size:16px;'>"
                        + targetsSecondHalf + " in second half</text>");

                htm.addln("</svg>");
                htm.eP();

                // Bar chart showing point total

                htm.sP();
                htm.addln("Current point total in course: <strong>" + totalPoints
                        + "</strong> (out of 170 possible)").br();

                final int aRangeBottom = 2 + 440 * 153 / 170;
                final int aRangeWidth = 441 - aRangeBottom;
                final int aRangeText = aRangeBottom + aRangeWidth / 2 - 4;

                final int bRangeBottom = 2 + 440 * 136 / 170;
                final int bRangeWidth = aRangeBottom - bRangeBottom;
                final int bRangeText = bRangeBottom + bRangeWidth / 2 - 4;

                final int cRangeBottom = 2 + 440 * minPassingPoints / 170;
                final int cRangeWidth = bRangeBottom - cRangeBottom;
                final int cRangeText = cRangeBottom + cRangeWidth / 2 - 4;

                final int progressWidth = 440 * totalPoints / 170;
                htm.addln("<svg width='444' height='24'>");
                htm.addln("<defs>");
                htm.addln(" <linearGradient id='grad1' x1='0%' y1='0%' x2='0%' y2='100%'>");
                htm.addln("  <stop offset='0%' style='stop-color:rgb(190,190,233)'/>");
                htm.addln("  <stop offset='26%' style='stop-color:rgb(216,216,245)'/>");
                htm.addln("  <stop offset='100%' style='stop-color:rgb(100,100,217)'/>");
                htm.addln(" </linearGradient>");
                htm.addln("</defs>");

                htm.addln("  <rect x='" + aRangeBottom + "' y='2' width='" + aRangeWidth
                        + "' height='20' style='fill:rgb(150,255,150);stroke-width:0;'/>");

                htm.addln("  <rect x='" + bRangeBottom + "' y='2' width='" + bRangeWidth
                        + "' height='20' style='fill:rgb(150,235,150);stroke-width:0;'/>");

                htm.addln("  <rect x='" + cRangeBottom + "' y='2' width='" + cRangeWidth
                        + "' height='20' style='fill:rgb(150,215,150);stroke-width:0;'/>");

                htm.addln("  <rect x='2' y='2' width='" + progressWidth
                        + "' height='20' style='fill:url(#grad1);fill-opacity=0.3;stroke:none;'/>");

                htm.addln(" <rect x='0.5' y='0.5' width='442' height='22' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln(" <rect x='1.5' y='1.5' width='441' height='20' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");

                htm.addln("  <rect x='2.5' y='2.5' width='439' height='19' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(0,0,0)'/>");
                htm.addln("  <text x='" + aRangeText + "' y='18' style='font-size:16px;'>A</text>");
                htm.addln("  <text x='" + bRangeText + "' y='18' style='font-size:16px;'>B</text>");
                htm.addln("  <text x='" + cRangeText + "' y='18' style='font-size:16px;'>C</text>");
                htm.addln("</svg>");
                htm.eP();

                htm.div("vgap0");
            } catch (final SQLException ex) {
                Log.warning(ex);
                htm.sP();
                htm.addln("Unable to determine class schedule.");
                htm.eP();
            }
        }
    }
}
