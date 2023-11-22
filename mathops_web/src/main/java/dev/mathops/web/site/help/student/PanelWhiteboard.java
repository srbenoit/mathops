package dev.mathops.web.site.help.student;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * Generates the content of the "Whiteboard" panel.
 */
enum PanelWhiteboard {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the context title
     */
    static void emitPanel(final HtmlBuilder htm, final String title) {

        htm.sDiv(null, "style='background:white; border:1px solid black; grid-column: span 2;'");

        htm.sDiv(null, "style='background:#C8C372; width:100%; height:20px; border-bottom:1px solid black;'");
        htm.sDiv(null, "style='font-family:factoria-medium; font-size:14px; padding:2px 0 0 6px; color:black;'");
        htm.add("Whiteboard: ", title);
        htm.eDiv();
        htm.eDiv();

        htm.eDiv();
    }
}
