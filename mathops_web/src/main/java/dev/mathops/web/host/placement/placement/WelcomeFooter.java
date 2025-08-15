package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ISecondaryFooter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Emits a secondary footer.
 */
public final class WelcomeFooter implements ISecondaryFooter {

    /**
     * Constructs a new {@code WelcomeFooter}.
     */
    WelcomeFooter() {

        // No action
    }

    /**
     * Emits the HTML for a supplemental footer (within the "footer" element).
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void emitSecondaryFooter(final Cache cache, final AbstractSite site,
                                    final HtmlBuilder htm) throws SQLException {

        final List<RawCampusCalendar> calendarDays = cache.getSystemData().getCampusCalendars();

        final TermRec active = cache.getSystemData().getActiveTerm();

        htm.sDiv("secfooter");
        htm.sDiv("container");
        htm.sDiv("secfootertext");

        htm.sDiv("secfootercol1");
        htm.sH(2).add("Math Placement Contact Information").eH(2);

        htm.sP().add("<span style='display:inline-block;min-width:60px;'>Email: </span>",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>")
                .br().add("<span style='display:inline-block;min-width:60px;'>",
                        "Phone: </span>(970) 491-5761").br()
                .add("<span style='display:inline-block;min-width:130px;'>Precalculus Center:</span> ")
                .add("<a class='ulink' href='https://map.concept3d.com/?id=748#!m/122443' ",
                        "target='_blank'>Weber Building</a>, room 137").eP();

        // Show holidays
        final List<String> days = AbstractPageSite.makeHolidayList(calendarDays);

        htm.sH(2).add("Closures").eH(2);

        htm.sP();
        htm.addln("The Precalculus Center will be closed ");
        final int numDays = days.size();
        if (numDays > 0) {
            boolean comma = false;

            for (int i = 0; i < numDays; ++i) {
                final String display = days.get(i);

                if (comma) {
                    if (days.size() == 2) {
                        htm.add(" and ");
                    } else if (i == days.size() - 1) {
                        htm.add(", and ");
                    } else {
                        htm.add(", ");
                    }
                }

                htm.add(display);

                comma = true;
            }

            htm.addln(days.size() == 1 ? " (University holiday) and " : " (University holidays) and ");
        }
        htm.addln("during Finals Week.");
        htm.eP();

        htm.eDiv(); // secfootercol1

        htm.sDiv("secfootercol2");
        htm.sH(2).add("Hours of Operation").eH(2);

        if (active != null && !calendarDays.isEmpty()) {
            RawCampusCalendar start1 = null;
            RawCampusCalendar end1 = null;
            RawCampusCalendar start2 = null;
            RawCampusCalendar end2 = null;
            RawCampusCalendar walkin = null;
            RawCampusCalendar start1x = null;
            RawCampusCalendar end1x = null;

            for (final RawCampusCalendar test : calendarDays) {
                if (RawCampusCalendar.DT_DESC_START_DATE_1.equals(test.dtDesc)) {
                    start1 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_1.equals(test.dtDesc)) {
                    end1 = test;
                } else if (RawCampusCalendar.DT_DESC_START_DATE_2.equals(test.dtDesc)) {
                    start2 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_2.equals(test.dtDesc)) {
                    end2 = test;
                } else if (RawCampusCalendar.DT_DESC_WALKIN_PLACEMENT.equals(test.dtDesc)) {
                    walkin = test;
                } else if (RawCampusCalendar.DT_DESC_START_DATE_1_NEXT.equals(test.dtDesc)) {
                    start1x = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_1_NEXT.equals(test.dtDesc)) {
                    end1x = test;
                }
            }

            htm.sP("tight").add("<span style='color:white'>").add(active.term.longString).add("</span>").eP();

            if (start1 != null && end1 != null) {
                final LocalDate start1Date = start1.campusDt;
                final LocalDate end1Date = end1.campusDt;

                htm.sP("indent0");
                if (start1Date.getYear() == end1Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start1Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start1Date));
                }
                htm.addln(" - ", TemporalUtils.FMT_MDY.format(end1Date));

                htm.sSpan("smallish");

                final int count = start1.numTimes();

                if (count > 0) {
                    htm.br().add("&nbsp; ", start1.openTime1, " - ", start1.closeTime1, ", ", start1.weekdays1);
                }
                if (count > 1) {
                    htm.br().add("&nbsp; ", start1.openTime2, " - ", start1.closeTime2, ", ", start1.weekdays2);
                }
                if (count > 2) {
                    htm.br().add("&nbsp; ", start1.openTime3, " - ", start1.closeTime3, ", ", start1.weekdays3);
                }

                htm.eSpan();

                htm.eP();
            }

            if (start2 != null && end2 != null) {
                final LocalDate start2Date = start2.campusDt;
                final LocalDate end2Date = end2.campusDt;

                htm.sP("indent0");
                if (start2Date.getYear() == end2Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start2Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start2Date));
                }
                htm.addln(" - ", TemporalUtils.FMT_MDY.format(end2Date));

                htm.sSpan("smallish");

                final int count = start2.numTimes();

                if (count > 0) {
                    htm.br().add("&nbsp; ", start2.openTime1, " - ", start2.closeTime1, ", ", start2.weekdays1);
                }
                if (count > 1) {
                    htm.br().add("&nbsp; ", start2.openTime2, " - ", start2.closeTime2, ", ", start2.weekdays2);
                }
                if (count > 2) {
                    htm.br().add("&nbsp; ", start2.openTime3, " - ", start2.closeTime3, ", ", start2.weekdays3);
                }

                htm.eSpan();
                htm.eP();
            }

            if (walkin != null) {
                htm.sP("tight").add("<span style='color:white'>Walk-in Math Placement Day</span>").eP();

                htm.sP("indent0");
                htm.add(TemporalUtils.FMT_MDY.format(walkin.campusDt));

                htm.sSpan("smallish");

                final int count = walkin.numTimes();

                if (count > 0) {
                    htm.br().add("&nbsp; ", walkin.openTime1, " - ", walkin.closeTime1, ", ", walkin.weekdays1);
                }
                if (count > 1) {
                    htm.br().add("&nbsp; ", walkin.openTime2, " - ", walkin.closeTime2, ", ", walkin.weekdays2);
                }
                if (count > 2) {
                    htm.br().add("&nbsp; ", walkin.openTime3, " - ", walkin.closeTime3, ", ", walkin.weekdays3);
                }

                htm.eSpan();
                htm.eP();
            }

            if (start1x != null && end1x != null) {
                htm.eDiv();

                final TermRec nextTerm = cache.getSystemData().getActiveTerm();
                htm.sP("tight").add("<em>").add(nextTerm.term.longString).add("</em>").eP();

                final LocalDate start1xDate = start1x.campusDt;
                final LocalDate end1xDate = end1x.campusDt;

                htm.sP("indent0");
                if (start1xDate.getYear() == end1xDate.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start1xDate));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start1xDate));
                }
                htm.addln(" - ", TemporalUtils.FMT_MDY.format(end1xDate));

                htm.sSpan("smallish");

                final int count = start1x.numTimes();

                if (count > 0) {
                    htm.br().add("&nbsp; ", start1x.openTime1, " - ", start1x.closeTime1, ", ", start1x.weekdays1);
                }
                if (count > 1) {
                    htm.br().add("&nbsp; ", start1x.openTime2, " - ", start1x.closeTime2, ", ", start1x.weekdays2);
                }
                if (count > 2) {
                    htm.br().add("&nbsp; ", start1x.openTime3, " - ", start1x.closeTime3, ", ", start1x.weekdays3);
                }

                htm.eSpan();
                htm.eP();
            }
        }

        htm.eDiv(); // secfootercol2

        htm.eDiv(); // secfootertext
        htm.eDiv(); // container
        htm.eDiv(); // secfooter
    }
}
