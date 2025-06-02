package dev.mathops.web.host.course.help.student;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * Generates the content of the "Tutor Video" panel.
 */
enum PanelTutorVideo {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black;'");

        htm.sDiv(null, "style='background:#C8C372; width:100%; height:20px; border-bottom:1px solid black;'");
        htm.sDiv(null, "style='font-family:factoria-medium; font-size:14px; padding:2px 0 0 6px; color:black;'");
        htm.add("Course Assistant");
        htm.eDiv();
        htm.eDiv();

        htm.sDiv("videobox");
        htm.addln("<video autoplay='true' id='tutorcam' ",
                "style='background-color: #333; width:100%; height:100%;'></video>");
        htm.eDiv();

        // Video controls
        htm.sDiv("controlsbox");
        htm.addln("<button>Stop</button>");
        htm.addln("<button>Mute</button>");
        htm.eDiv();

        htm.eDiv();
    }
}
