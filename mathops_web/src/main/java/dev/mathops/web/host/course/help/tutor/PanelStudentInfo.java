package dev.mathops.web.host.course.help.tutor;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.websocket.help.livehelp.LiveHelpSession;

/**
 * Generates the content of the "Student Information" panel.
 */
enum PanelStudentInfo {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param lhsess the live help queue entry
     */
    static void emitPanel(final HtmlBuilder htm, final LiveHelpSession lhsess) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area: sinfo;'");

        htm.sDiv(null, "style='background: #1e4d2b; width:100%; height:21px;",
                "font-family:factoria-medium; font-size:15px; padding:2px 0 0 6px; color:white; ",
                "border-bottom:1px solid black;'");
        htm.add("Student Information");
        htm.eDiv();

        htm.sP().add("<table style='width:100%; padding:0; margin:0;'>");
        htm.sP().add("<tr><th style='width:65px;font-size:14px;background:GhostWhite;'>Name:</th>",
                "<td style='font-size:14px;background:GhostWhite;'>",
                lhsess.initiatingStudent.screenName, "</td></tr>");
        htm.sP().add(
                "<tr><th style='width:65px;font-size:14px;background:GhostWhite;'>Student&nbsp;ID:</th>",
                "<td style='font-size:14px;background:GhostWhite;'>",
                lhsess.initiatingStudent.studentId, "</td></tr>");
        htm.sP().add(
                "<tr><th style='width:65px;font-size:14px;background:GhostWhite;'>Courses:</th>",
                "<td style='font-size:14px;background:GhostWhite;'>???</td></tr>");
        htm.sP().add(
                "<tr><th style='width:65px;font-size:14px;background:GhostWhite;'>Campus:</th>",
                "<td style='font-size:14px;background:GhostWhite;'>",
                "On-Campus (not CSU Online)</td></tr>");
        htm.sP().add("</table>");

        htm.eDiv();
    }
}
