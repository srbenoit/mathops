package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.core.CoreConstants;
import dev.mathops.web.site.CssStylesheet;

/**
 * The singleton CSS stylesheet for the lesson pages.
 */
final class LessonCss extends CssStylesheet {

    /** The singleton instance. */
    private static LessonCss instance;

    /**
     * Constructs a new {@code LessonCss}.
     */
    private LessonCss() {

        super("geneva,helvetica,arial,\"lucida sans\",sans-serif");

        // TODO: Load these from a database table

        addStyle(".chapter-head", "width", "100%");

        addStyle(".chapter-image", "float", "right");
        addStyle(".chapter-image", "padding", "10px;");

        addStyle(".chapter-heading", "background-color", "#EEE");
        addStyle(".chapter-heading", "border-top", "1px solid #C77");
        addStyle(".chapter-heading", "border-bottom", "1px solid #C77");
        addStyle(".chapter-heading", "min-width", "640px");

        addStyle(".chapter-number", "font-family", "STIXGeneralRegular");
        addStyle(".chapter-number", "float", "left");
        addStyle(".chapter-number", "padding", "10px");
        addStyle(".chapter-number", "height", "70pt");
        addStyle(".chapter-number", "line-height", "70pt");
        addStyle(".chapter-number", "font-size", "50pt");

        addStyle(".chapter-title", "float", "right");
        addStyle(".chapter-title", "padding", "6pt");

        addStyle(".chapter-title-1-only", "text-align", "right");
        addStyle(".chapter-title-1-only", "height", "70pt");
        addStyle(".chapter-title-1-only", "line-height", "80pt");
        addStyle(".chapter-title-1-only", "font-size", "40pt");
        addStyle(".chapter-title-1-only", "font-family", "STIXGeneralRegular");

        addStyle(".chapter-title-1-of-2", "text-align", "right");
        addStyle(".chapter-title-1-of-2", "height", "50pt");
        addStyle(".chapter-title-1-of-2", "line-height", "70pt");
        addStyle(".chapter-title-1-of-2", "font-size", "40pt");
        addStyle(".chapter-title-1-of-2", "font-family", "STIXGeneralRegular");

        addStyle(".chapter-title-2-of-2", "text-align", "right");
        addStyle(".chapter-title-2-of-2", "height", "30pt");
        addStyle(".chapter-title-2-of-2", "line-height", "42pt");
        addStyle(".chapter-title-2-of-2", "font-size", "24pt");
        addStyle(".chapter-title-2-of-2", "font-family", "STIXGeneralRegular");

        addStyle(".prerequisites", "padding", "10px");
        addStyle(".prerequisites", "padding-left", "170px");
        addStyle(".prerequisites", "font-size", "13pt");
        addStyle(".prerequisites", "min-width", "370px");
        addStyle(".prerequisites", "font-family", "PTSansNarrowBold");

        addStyle(".prerequisitelist", "padding", "0");
        addStyle(".prerequisitelist", "padding-left", "30px");
        addStyle(".prerequisitelist", "margin", "0");
        addStyle(".prerequisitelist", "list-style-type", "disc");

        addStyle(".prerequisite", "padding", "0");
        addStyle(".prerequisite", "font-size", "13pt");
        addStyle(".prerequisite", "font-family", "PTSansNarrowRegular");

        addStyle(".objectives", "padding", "10px");
        addStyle(".objectives", "padding-left", "170px");
        addStyle(".objectives", "font-size", "13pt");
        addStyle(".objectives", "min-width", "370px");
        addStyle(".objectives", "font-family", "PTSansNarrowBold");

        addStyle(".objectivelist", "padding", "0");
        addStyle(".objectivelist", "padding-left", "30px");
        addStyle(".objectivelist", "margin", "0");
        addStyle(".objectivelist", "list-style-type", "disc");

        addStyle(".objective", "padding", "0");
        addStyle(".objective", "font-size", "13pt");
        addStyle(".objective", "font-family", "PTSansNarrowRegular");

        addStyle(".textrefs", "padding", "10px");
        addStyle(".textrefs", "padding-left", "170px");
        addStyle(".textrefs", "font-size", "13pt");
        addStyle(".textrefs", "min-width", "370px");
        addStyle(".textrefs", "font-family", "PTSansNarrowBold");

        addStyle(".textreflist", "padding", "0");
        addStyle(".textreflist", "padding-left", "30px");
        addStyle(".textreflist", "margin", "0");
        addStyle(".textreflist", "list-style-type", "disc");

        addStyle(".textref", "padding", "0");
        addStyle(".textref", "font-size", "13pt");
        addStyle(".textref", "font-family", "PTSansNarrowRegular");

        addStyle(".summary-text", "padding", "20px");
        addStyle(".summary-text", "padding-left", "150px");
        addStyle(".summary-text", "font-size", "16pt");
        addStyle(".summary-text", "min-width", "390px");
        addStyle(".summary-text", "font-family", "STIXGeneralItalic");

        addStyle(".left-margin-div", "width", "180px");
        addStyle(".left-margin-div", "margin-left", "10px");
        addStyle(".left-margin-div", "float", "left");

        addStyle(".left-margin-img", "text-decoration", "none");

        addStyle(".section", "margin", "5px");
        addStyle(".section", "padding-left", "90px");
        addStyle(".section", "margin-top", "30px");
        addStyle(".section", "font-size", "20pt");
        addStyle(".section", "font-family", "PTSansNarrowBold");
        addStyle(".section", "min-width", "450px");
        addStyle(".section", "background", "#ddf");

        addStyle(".subsection", "margin", "5px");
        addStyle(".subsection", "padding-left", "100px");
        addStyle(".subsection", "min-width", "440px");
        addStyle(".subsection", "margin-top", "30px");
        addStyle(".subsection", "font-size", "19pt");
        addStyle(".subsection", "font-family", "PTSansNarrowBold");

        addStyle(".subsubsection", "margin", "5px");
        addStyle(".subsubsection", "padding-left", "110px");
        addStyle(".subsubsection", "min-width", "430px");
        addStyle(".subsubsection", "margin-top", "30px");
        addStyle(".subsubsection", "font-size", "17pt");
        addStyle(".subsubsection", "font-family", "PTSansNarrowBold");

        addStyle(".subsubsubsection", "margin", "5px");
        addStyle(".subsubsubsection", "padding-left", "120px");
        addStyle(".subsubsubsection", "min-width", "420px");
        addStyle(".subsubsubsection", "margin-top", "30px");
        addStyle(".subsubsubsection", "font-size", "16pt");
        addStyle(".subsubsubsection", "font-family", "PTSansNarrowBold");

        addStyle(".paragraph", "padding", "10px");
        addStyle(".paragraph", "padding-left", "120px");
        addStyle(".paragraph", "min-width", "420px");
        addStyle(".paragraph", "font-size", "14pt");
        addStyle(".paragraph", "font-family", "STIXGeneralRegular");

        addStyle(".par-display-tex", "padding", "10px");
        addStyle(".par-display-tex", "text-align", "center");
        addStyle(".par-display-tex", "font-size", "14pt");
        addStyle(".par-display-tex", "font-family", "STIXGeneralRegular");

        addStyle(".display-tex", "padding", "10px");
        addStyle(".display-tex", "text-align", "center");
        addStyle(".display-tex", "padding-left", "120px");
        addStyle(".display-tex", "min-width", "420px");
        addStyle(".display-tex", "font-size", "14pt");
        addStyle(".display-tex", "font-family", "STIXGeneralRegular");

        addStyle(".grid", "width", "100%");
        addStyle(".grid", "min-width", "420px");
        addStyle(".grid", "padding", "10px");
        addStyle(".grid", "padding-right", "30px");
        addStyle(".grid", "padding-left", "140px");

        addStyle(".pgrid", "width", "100%");
        addStyle(".pgrid", "padding", "10px");

        addStyle("ul.list", "margin", "0");
        addStyle("ul.list", "padding", "5pt");
        addStyle("ul.list", "padding-right", "30px");
        addStyle("ul.list", "padding-left", "160px");

        addStyle("ul.par-list", "margin", "0");
        addStyle("ul.par-list", "padding", "5pt");
        addStyle("ul.par-list", "padding-right", "30px");
        addStyle("ul.par-list", "padding-left", "30px");

        addStyle("ol", "margin", "0");
        addStyle("ol", "padding", "5pt");
        addStyle("ol", "padding-right", "30px");
        addStyle("ol", "padding-left", "160px");

        addStyle("ol.par-list", "margin", "0");
        addStyle("ol.par-list", "padding", "5pt");
        addStyle("ol.par-list", "padding-right", "30px");
        addStyle("ol.par-list", "padding-left", "30px");

        addStyle(".question", "padding", "10px");
        addStyle(".question", "padding-right", "30px");
        addStyle(".question", "padding-left", "140px");
        addStyle(".question", "min-width", "400px");
        addStyle(".question", "font-size", "14pt");
        addStyle(".question", "font-family", "STIXGeneralRegular");

        addStyle(".table", "padding", "10px");
        addStyle(".table", "padding-right", "30px");
        addStyle(".table", "padding-left", "140px");
        addStyle(".table", "min-width", "400px");

        addStyle(".theorem ", "margin-left", "40px");
        addStyle(".theorem ", "margin-right", "30px");
        addStyle(".theorem ", "min-width", "400px");
        addStyle(".theorem ", "padding", "10px");
        addStyle(".theorem ", "padding-right", "20px");
        addStyle(".theorem ", "padding-left", "20px");
        addStyle(".theorem ", "font-size", "14pt");
        addStyle(".theorem ", "font-family", "STIXGeneralRegular");
        addStyle(".theorem ", "border", "1px solid blue");

        addStyle(".todo", "margin", "5px");
        addStyle(".todo", "margin-left", "220px");
        addStyle(".todo", "min-width", "420px");
        addStyle(".todo", "padding-left", "3pt");
        addStyle(".todo", "padding-right", "3pt");
        addStyle(".todo", "font-size", "12pt");
        addStyle(".todo", "font-family", "STIXGeneralRegular");
        addStyle(".todo", "color", "blue");
        addStyle(".todo", "border", "solid 1px blue");

        addStyle("th", "font-size", "14pt");
        addStyle("th", "font-family", "STIXGeneralBold");

        addStyle("td", "font-size", "14pt");
        addStyle("td", "font-family", "STIXGeneralRegular");

        addStyle(".plot", "margin-left", "220px");
        addStyle(".plot", "min-width", "420px");
        addStyle(".plot", "text-align", "center");

        addStyle(".list-item", "font-size", "14pt");
        addStyle(".list-item", "font-family", "STIXGeneralRegular");

        addStyle(".caption", "padding-left", "7pt");
        addStyle(".caption", "padding-right", "7pt");
        addStyle(".caption", "text-align", "center");
        addStyle(".caption", "font-size", "12pt");
        addStyle(".caption", "font-family", "STIXGeneralRegular");

        addStyle(".par-plot", "text-align", "center");

        addStyle(".examples", "margin-left", "20px");
        addStyle(".examples", "margin-right", "20px");
        addStyle(".examples", "min-width", "420px");
        addStyle(".examples", "padding", "10px");
        addStyle(".examples", "padding-right", "20px");
        addStyle(".examples", "padding-left", "20px");
        addStyle(".examples", "font-size", "14pt");
        addStyle(".examples", "font-family", "STIXGeneralRegular");
        addStyle(".examples", "border", "3px ridge #8888AA");

        addStyle(".examples-title", "font-size", "14pt");
        addStyle(".examples-title", "font-family", "PTSansNarrowBold");
        addStyle(".examples-title", "color", CoreConstants.EMPTY);

        addStyle(".example", "padding", "0");
        addStyle(".example", "margin", "0");
        addStyle(".example", "padding-left", "30px");
        addStyle(".example", "font-size", "12pt");
        addStyle(".example", "font-family", "STIXGeneralRegular");

        addStyle(".exercise", "padding", "0");
        addStyle(".exercise", "margin", "0");
        addStyle(".exercise", "padding-left", "30px");
        addStyle(".exercise", "font-size", "12pt");
        addStyle(".exercise", "font-family", "STIXGeneralRegular");

        addStyle(".solution", "font-size", "12pt");
        addStyle(".solution", "font-family", "STIXGeneralRegular");

        addStyle(".media-link", "font-size", "12pt");
        addStyle(".media-link", "font-family", "STIXGeneralRegular");

        addStyle(".assignment", "margin-left", "200px");
        addStyle(".assignment", "padding", "40px");

        addStyle(".exam", "margin-left", "200px");
        addStyle(".exam", "padding", "40px");
    }

    /**
     * Gets the singleton {@code LessonCss} instance.
     *
     * @return the instance
     */
    static LessonCss getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new LessonCss();
            }
        }

        return instance;
    }
}
