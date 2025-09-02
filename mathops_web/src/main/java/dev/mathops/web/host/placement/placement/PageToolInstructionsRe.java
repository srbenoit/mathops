package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.DateRange;
import dev.mathops.db.logic.DateRangeGroups;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.placement.PlacementStatus;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates the content of a page with instructions for the remote exam.
 */
enum PageToolInstructionsRe {
    ;

    /**
     * Generates the page of information on a remote, unproctored placement attempt.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStudent stu = RawStudentLogic.query(cache, stuId, false);

        final PlacementLogic logic = new PlacementLogic(cache, stuId, stu == null ? null : stu.aplnTerm,
                session.getNow());
        final PlacementStatus status = logic.status;

        final HtmlBuilder htm = MPPage.startPage3(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        final DateRangeGroups unproc = status.unproctoredDateRanges;

        if (status.allowedToUseUnproctored) {
            htm.sP().addln("Incoming students and CSU Online students may complete the Math Placement ",
                    "Tool one time from a remote, unproctored location.").eP();

            final List<DateRange> future = unproc.future;

            if (status.availableUnproctoredIds.isEmpty()) {
                htm.sP().addln(status.whyUnproctoredUnavailable).eP();

                if (!future.isEmpty()) {
                    htm.sP().addln("The Math Placement Tool will be available on the following days (except ",
                            "from 6am - 9am (Mountain time zone) daily):");

                    htm.sP("indent22");
                    for (final DateRange r : future) {
                        htm.addln(TemporalUtils.FMT_MDY.format(r.start), " &nbsp; through &nbsp; ",
                                TemporalUtils.FMT_MDY.format(r.end)).br();
                    }
                    htm.eP();

                    htm.sP("indent22").addln("These dates are based on the U.S. Mountain Time Zone.").eP();
                }

                htm.sP().addln("If you have any questions, send an email to",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                        "precalc_math@colostate.edu</a>.").eP();
            } else {
                htm.sDiv("center");
                htm.sTable("plan-table", "style='display:inline-table;'");
                htm.sTr();
                htm.sTh().add("Format:").eTh();
                htm.sTd().add("50 items, multiple-choice / multiple selection").eTd();
                htm.eTr();
                htm.sTr();
                htm.sTh().add("Time limit:").eTh();
                if (stu != null && stu.timelimitFactor != null) {
                    final float factor = stu.timelimitFactor.floatValue();

                    if (factor > 1.49f && factor < 1.51f) {
                        htm.sTd().add("2 hours, 20 minutes<br/>",
                                "(Adjusted to 3 hours, 30 minutes by accommodation)").eTd();
                    } else if (factor > 1.99f && factor < 2.01f) {
                        htm.sTd().add("2 hours, 20 minutes<br/>",
                                "(Adjusted to 4 hours, 40 minutes by accommodation)").eTd();
                    } else if (factor > 2.49f && factor < 2.51f) {
                        htm.sTd().add("2 hours, 20 minutes<br/>",
                                "(Adjusted to 5 hours, 50 minutes by accommodation)").eTd();
                    } else if (factor > 2.99f && factor < 3.01f) {
                        htm.sTd().add("2 hours, 20 minutes<br/>", "(Adjusted to 7 hours by accommodation)")
                                .eTd();
                    } else {
                        final int minutes = Math.round(140.0f * factor);
                        if (minutes <= 140) {
                            htm.sTd().add("2 hours, 20 minutes").eTd();
                        } else {
                            htm.sTd().add("2 hours, 20 minutes<br/>", "(Adjusted to ",
                                    Integer.toString(minutes), " minutes by accommodation)").eTd();
                        }
                    }
                } else {
                    htm.sTd().add("2 hours, 20 minutes").eTd();
                }
                htm.eTr();
                htm.sTr();
                htm.sTh().add("Calculator:").eTh();
                htm.sTd().add("Personal graphing calculator allowed").eTd();
                htm.eTr();
                htm.sTr();
                htm.sTh().add("References:").eTh();
                htm.sTd().add("Not allowed").eTd();
                htm.eTr();
                htm.sTr();
                htm.sTh("highlight").add("Fee:").eTh();
                htm.sTd("highlight").add("<a class='ulink' href='tool_fees.html'>",
                        "Tell me about the Math Placement fee</a>").eTd();
                htm.eTr();
                htm.eTable();
                htm.eDiv(); // center
                htm.div("vgap");

                htm.addln("<ul>");

                htm.addln("<li>",
                        "To place out of a course, you must demonstrate proficiency in all of its ",
                        "prerequisite courses during the same attempt of the Math Placement Tool.  That ",
                        "means you should not skip the algebra portion if you want to place out of ",
                        "trigonometry or logarithmic &amp; exponential functions.", //
                        "</li>");
                htm.div("vgap");

                htm.addln("<li>",
                        "The unproctored Math Placement Tool will be available on the following days (except ",
                        "from 6am - 9am (Mountain time zone) daily):");
                htm.div("vgap");

                if (unproc.current != null) {
                    htm.sDiv("indent");
                    htm.addln(unproc.current);
                    htm.eDiv();
                }
                for (final DateRange r : future) {
                    htm.addln(r).br();
                }
                htm.div("vgap");
                htm.addln("These dates are based on the U.S. Mountain Time Zone.");

                htm.addln("</li>");
                htm.div("vgap");

                htm.addln("<li>Results are available immediately after completing the Math Placement Tool.</li>");
                htm.div("vgap");

                htm.addln("<li>Report any problems via email to ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>.",
                        "</li>");
                htm.div("vgap");

                htm.addln("</ul>");

                htm.div("vgap");

                htm.sDiv("advice");
                htm.sDiv("left");
                htm.addln("<img width='90pt' height='90pt' src='/images/dialog-warning-2.png'/>");
                htm.eDiv(); // left

                htm.sDiv(null, "style='margin-left:80pt;'");
                htm.addln("You may only complete the Math Placement Tool one time in a non-proctored ",
                        "setting. You should review and study before using the tool.");
                htm.eDiv();

                htm.sDiv(null, "style='margin:8pt 20pt 0 100pt;'");
                htm.addln("<a class='smallbtn' href='review.html'>Use our interactive review materials.</a>");
                htm.eDiv();

                htm.sDiv(null, "style='margin-left:80pt;margin-top:8pt;'");
                htm.addln("Depending on your major, your results could directly affect your ability ",
                        "to register for important classes in your degree program.");
                htm.eDiv();

                htm.div("clear");
                htm.eDiv(); // advice

                htm.div("vgap2");

                htm.sDiv("center");
                htm.addln("<form style='display:inline;margin:20pt' method='get' action='tool_start_re.html'>");
                htm.addln(" <input type='submit' class='btn' value='Complete the Math Placement Tool...'/>");
                htm.addln("</form>");
            }
        } else {
            // Should never get to this page if not eligible
            htm.addln("Based on the term for which you have applied for admission, you are ",
                    "not eligible to complete the unproctored Math Placement Tool.").eP();
        }

        htm.eDiv(); // indent11

        htm.div("vgap");

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
