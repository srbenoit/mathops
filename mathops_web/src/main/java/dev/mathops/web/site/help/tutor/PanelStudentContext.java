package dev.mathops.web.site.help.tutor;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * Generates the content of the "Student Context" panel.
 */
enum PanelStudentContext {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:white; border:1px solid black; grid-area: scontext;'");

        htm.sDiv(null, "style='background: #1e4d2b; width:100%; height:21px;",
                "font-family:factoria-medium; font-size:15px; padding:2px 0 0 6px; color:white; ",
                "border-bottom:1px solid black;'");
        htm.add("Shared Whiteboard");
        htm.eDiv();

        htm.eDiv();
    }
}
