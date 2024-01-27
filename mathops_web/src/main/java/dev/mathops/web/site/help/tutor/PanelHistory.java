package dev.mathops.web.site.help.tutor;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * Generates the content of the "History" panel.
 */
enum PanelHistory {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final HtmlBuilder htm) {

        htm.sDiv(null, "style='border:1px solid black; background: GhostWhite; grid-area: notes;'");

        htm.sDiv(null, "style='background: #1e4d2b; width:100%; height:21px;",
                "font-family:factoria-medium; font-size:15px; padding:2px 0 0 6px; color:white; ",
                "border-bottom:1px solid black;'");
        htm.add("History and Notes");
        htm.eDiv();

        // History list - scrollable
        htm.sDiv(null, "style='overflow:scroll; padding:0 8px; font-size:14px; height:calc(100% - 21px);'");

        htm.sP().add("<b>01/01/2020</b>: (SRB) helped student with finding polynomial that fits ",
                "data. Student often transposes numbers when entering them into calculator.");
        htm.sP().add("<b>01/02/2020</b>: (JJK) Student had misconception about canceling in ",
                "fractions. Worked on several examples.");
        htm.eDiv();

        htm.eDiv();
    }
}
