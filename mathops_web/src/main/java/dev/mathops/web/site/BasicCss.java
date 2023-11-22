package dev.mathops.web.site;

/**
 * A basic stylesheet for web pages.
 */
public final class BasicCss extends CssStylesheet {

    /** Object on which to synchronize creation of singleton instance. */
    private static final Object INSTANCE_SYNCH = new Object();

    /** The menu and login bar background. */
    private static final String MENU_BG = "#f1ecd9";

    /** Highlighted menu background. */
    private static final String HEADING_BG = "#f0fff0";

    /** Background for folders within the menu. */
    private static final String FOLDER_BG = "#f1e7c0";

    /** Outline color for folders. */
    private static final String FOLDER_OUTLINE = "#b5ae91";

    /** A commonly used dark color. */
    private static final String DARK_TEXT_COLOR = "#004f39";

    /** The singleton instance. */
    private static BasicCss instance;

    /**
     * Constructs a new {@code BasicCss}.
     */
    private BasicCss() {

        super("geneva,helvetica,arial,\"lucida sans\",sans-serif");

        // TODO: Load these from a database table

        addFontsToStyle("body");
        addStyle("body", "font-size", "11pt");
        addStyle("body", "color", "#333");
        addStyle("body", "margin", "0");
        addStyle("body", "padding", "0");
        addStyle("body", "border-width", "0");
        addStyle("body", "background-color", "#cdcdbf");
        addStyle("body", "overflow", "hidden");
        addStyle("body", "width", "100%");
        addStyle("body", "height", "100%");
        addStyle("body", "background-image",
                "url('/images/menu200_stem.png')");
        addStyle("body", "background-repeat",
                "no-repeat");
        addStyle("body", "background-attachment",
                "fixed");
        addStyle("body", "background-position", "0 85px");

        addStyle("input[type=\"text\"]", "font-size",
                "10pt");
        addStyle("input[type=\"password\"]", "font-size",
                "10pt");

        addStyle(".pagetitle", "margin-top", "18px");
        addStyle(".pagetitle", "margin-left", "10px");
        addStyle(".pagetitle", "color", "#004f39");
        addStyle(".pagetitle", "font-family",
                "minion_probold_cond");
        addStyle(".pagetitle", "font-size", "52px");
        addStyle(".pagetitle", "line-height", "53px");

        addStyle("p", "margin-top", "3pt");
        addStyle("p", "margin-bottom", "7pt");

        addStyle("hr", "margin", "3pt 0 3pt 0");
        addStyle("hr", "padding", "0");
        addStyle("hr", "color", DARK_TEXT_COLOR);
        addStyle("hr", "background-color", DARK_TEXT_COLOR);
        addStyle("hr", "height", "2px");
        addStyle("hr", "border", "none");

        addFontsToStyle(".welcome");
        addStyle(".welcome", "text-align", "center");
        addStyle(".welcome", "padding",
                "0 20px 5px 20px");
        addStyle(".welcome", "border-width", "0 0 1px 0");
        addStyle(".welcome", "border-style", "solid");
        addStyle(".welcome", "border-color", "#88aa88");

        addFontsToStyle(".copyright");
        addStyle(".copyright", "height", "11pt");
        addStyle(".copyright", "width", "100%");
        addStyle(".copyright", "padding-top", "10pt");
        addStyle(".copyright", "color", "#888888");
        addStyle(".copyright", "font-size", "8pt");
        addStyle(".copyright", "font-weight", "bold");
        addStyle(".copyright", "text-align", "center");
        addStyle(".copyright", "white-space", "nowrap");

        addStyle(".footer", "padding-top", "20pt");
        addStyle(".footer", "text-align", "center");
        addStyle(".footer", "width", "300pt");

        addStyle(".bullets", "padding-top", "8pt;");

        addStyle(".center", "text-align", "center");

        addStyle(".error", "text-align", "center");
        addStyle(".error", "font-weight", "bold");
        addStyle(".error", "color", "#640000");
        addStyle(".error", "padding-top", "3pt");

        addStyle("div.left", "float", "left");
        addStyle("div.left", "white-space", "nowrap");
        addStyle("div.left", "text-align", "left");

        addStyle("div.right", "float", "right");
        addStyle("div.right", "white-space", "nowrap");
        addStyle("div.right", "text-align", "right");

        addStyle("div.clear", "margin", "0");
        addStyle("div.clear", "clear", "both");
        addStyle("div.clear", "font-size", "0");
        addStyle("div.clear", "line-height", "0");

        addStyle("a:link", "color", "#004400");

        addStyle("a:visited", "color", "#004400");

        addStyle("a:hover", "background-color", DARK_TEXT_COLOR);
        addStyle("a:hover", "color", "white");

        addStyle("a img", "border", "0");
        addStyle("a img", "text-decoration", "none");

        addStyle("a:active", "background-color", DARK_TEXT_COLOR);
        addStyle("a:active", "color", "white");

        addStyle("a:focus", "outline", "none");
        addStyle("a:focus", "outline-style", "none");

        addStyle(".menu", "display", "block");
        addStyle(".menu", "margin", "0");
        addStyle(".menu", "padding", "1.4pt");
        addStyle(".menu", "padding-left", "3pt");
        addStyle(".menu", "padding-right", "3pt");
        addStyle(".menu", "background-color", MENU_BG);

        addStyle("a.menu:link", "background-color", MENU_BG);
        addStyle("a.menu:link", "color", "#004400");
        addStyle("a.menu:link", "text-decoration",
                "none");

        addStyle("a.menu:visited", "background-color", MENU_BG);
        addStyle("a.menu:visited", "color", "#004400");
        addStyle("a.menu:visited", "text-decoration",
                "none");

        addStyle("a.menu:hover", "background-color", DARK_TEXT_COLOR);
        addStyle("a.menu:hover", "color", "white");
        addStyle("a.menu:hover", "text-decoration",
                "none");

        addStyle("a.menu:active", "background-color", DARK_TEXT_COLOR);
        addStyle("a.menu:active", "color", "white");
        addStyle("a.menu:active", "text-decoration",
                "none");

        addStyle(".menu2", "display", "block");
        addStyle(".menu2", "margin", "0");
        addStyle(".menu2", "padding", "1.4pt");
        addStyle(".menu2", "padding-left", "3pt");
        addStyle(".menu2", "padding-right", "3pt");
        addStyle(".menu2", "background-color", FOLDER_BG);

        addStyle("a.menu2:link", "background-color", FOLDER_BG);
        addStyle("a.menu2:link", "color", "#004400");
        // addStyle("a.menu2:link", "text-decoration", "none");

        addStyle("a.menu2:visited", "background-color", FOLDER_BG);
        addStyle("a.menu2:visited", "color", "#004400");
        addStyle("a.menu2:visited", "text-decoration",
                "none");

        addStyle("a.menu2:hover", "background-color", DARK_TEXT_COLOR);
        addStyle("a.menu2:hover", "color", "white");
        addStyle("a.menu2:hover", "text-decoration",
                "none");

        addStyle("a.menu2:active", "background-color", DARK_TEXT_COLOR);
        addStyle("a.menu2:active", "color", "white");
        addStyle("a.menu2:active", "text-decoration",
                "none");

        addStyle(".label", "margin", "0");
        addStyle(".label", "padding-bottom", "1pt");
        addStyle(".label", "padding-top", "1pt");

        addStyle(".gap", "height", "6pt");
        addStyle(".gap2", "height", "10pt");
        addStyle(".gap3", "height", "14pt");

        addStyle(".loginbar", "position", "absolute");
        addStyle(".loginbar", "width", "100%");
        addStyle(".loginbar", "min-width", "800px");
        addStyle(".loginbar", "left", "0");
        addStyle(".loginbar", "font-size", "15px");
        addStyle(".loginbar", "margin", "0");
        addStyle(".loginbar", "padding", "0");
        addStyle(".loginbar", "border-style", "solid");
        addStyle(".loginbar", "border-color", DARK_TEXT_COLOR);
        addStyle(".loginbar", "border-width",
                "1px 0 1px 0");
        addStyle(".loginbar", "background-image",
                "url('/images/loginbar28_bg.png')");

        addStyle(".loginuser", "margin", "0");
        addStyle(".loginuser", "padding",
                "4pt 9px 0 9px");
        addStyle(".loginuser", "color", DARK_TEXT_COLOR);
        addStyle(".logout", "margin", "0");
        addStyle(".logout", "padding", "4px 9px 0 9px");
        addStyle(".logout", "color", DARK_TEXT_COLOR);
        addStyle(".date", "color", DARK_TEXT_COLOR);
        addStyle(".date", "background-image",
                "url('/images/clock28_bg.png')");
        addStyle(".date", "margin", "0");
        addStyle(".date", "padding", "5px 9px 0 9px");
        addStyle(".date", "color", "black");
        addStyle(".date", "border-style", "solid");
        addStyle(".date", "border-color", DARK_TEXT_COLOR);
        addStyle(".date", "border-width", "0 0 0 1px");

        addStyle(".rolebar", "position", "absolute");
        addStyle(".rolebar", "width", "100%");
        addStyle(".rolebar", "min-width", "800px");
        addStyle(".rolebar", "left", "0");
        addStyle(".rolebar", "font-size", "14px");
        addStyle(".rolebar", "margin", "0");
        addStyle(".rolebar", "padding", "0");
        addStyle(".rolebar", "border-style", "solid");
        addStyle(".rolebar", "border-color", "#640000");
        addStyle(".rolebar", "border-width", "0 0 1px 0");
        addStyle(".rolebar", "background-color",
                "#ececec");

        addStyle(".role", "margin", "0");
        addStyle(".role", "padding", "4px 9px 0 9px");
        addStyle(".role", "color", "#640000");

        addStyle(".roletab", "color", "#640000");
        addStyle(".roletab", "background-color",
                "#cdcddd");
        addStyle(".roletab", "margin", "0");
        addStyle(".roletab", "padding", "3px 9px 0 9px");
        addStyle(".roletab", "border-style", "solid");
        addStyle(".roletab", "border-color", "#640000");
        addStyle(".roletab", "border-width",
                "0 1px 0 1px");

        addStyle(".roleform", "display", "inline");
        addStyle(".roleform", "margin", "0");
        addStyle(".roleform", "padding", "0");

        addStyle(".fullpanel", "font-size", "11pt");
        addStyle(".fullpanel", "position", "absolute");
        addStyle(".fullpanel", "bottom", "0");
        addStyle(".fullpanel", "left", "0");
        addStyle(".fullpanel", "width", "100%");
        addStyle(".fullpanel", "margin", "0");
        addStyle(".fullpanel", "padding", "12px 0 0 0");
        addStyle(".fullpanel", "background-color",
                "#ffffff");
        addStyle(".fullpanel", "overflow", "auto");
        addStyle(".fullpanel", "min-width", "800px");

        addStyle(".panel", "font-size", "11pt");
        addStyle(".panel", "position", "absolute");
        addStyle(".panel", "bottom", "0");
        addStyle(".panel", "top", "0");
        addStyle(".panel", "left", "220px");
        addStyle(".panel", "right", "0");
        addStyle(".panel", "padding", "3pt");
        addStyle(".panel", "background-color", "#ffffff");
        addStyle(".panel", "overflow", "auto");
        addStyle(".panel", "min-width", "500pt");

        addStyle(".menupanel", "font-size", "11pt");
        addStyle(".menupanel", "position", "absolute");
        addStyle(".menupanel", "bottom", "0");
        addStyle(".menupanel", "left", "0");
        addStyle(".menupanel", "width", "100%");
        addStyle(".menupanel", "min-width", "800px");
        addStyle(".menupanel", "background-color",
                "#ffffff");
        addStyle(".menupanel", "overflow", "auto");
        addStyle(".menupanel", "padding", "0");
        addStyle(".menupanel", "margin", "0");

        addStyle(".menuback", "position", "absolute");
        addStyle(".menuback", "top", "0");
        addStyle(".menuback", "left", "0");
        addStyle(".menuback", "bottom", "0");
        addStyle(".menuback", "text-align", "center");
        addStyle(".menuback", "width", "219px");
        addStyle(".menuback", "margin", "0");
        addStyle(".menuback", "padding", "0");
        addStyle(".menuback", "background-color", MENU_BG);
        addStyle(".menuback", "border-width",
                "0 1px 0 0");
        addStyle(".menuback", "border-style", "solid");
        addStyle(".menuback", "border-color", DARK_TEXT_COLOR);

        addStyle(".menubox", "width", "219px");
        addStyle(".menubox", "background-color", MENU_BG);
        addStyle(".menubox", "margin", "0");
        addStyle(".menubox", "padding", "3pt 0 0 0");

        addStyle(".small", "font-size", "10pt");
        addStyle(".small", "color", DARK_TEXT_COLOR);
        addStyle(".smallred", "font-size", "9pt");
        addStyle(".smallred", "color", "#640000");
        addStyle(".smallred", "margin", "0");
        addStyle(".smallred", "padding", "0");

        addStyle(".button", "font-size", "11pt");
        addStyle(".gobutton", "font-size", "9pt");
        addStyle(".gobutton", "color", "#640000");
        addStyle(".gobutton", "padding", "0 2px 0 2px");
        addStyle(".gobutton", "margin", "0");

        addStyle(".maintbox", "margin", "10pt");
        addStyle(".maintbox", "padding", "6pt");
        addStyle(".maintbox", "border-width", "2px");
        addStyle(".maintbox", "border-style", "solid");
        addStyle(".maintbox", "border-color", "black");
        addStyle(".maintbox", "background", MENU_BG);

        addStyle("img", "border", "0");

        addStyle("fieldset", "border",
                "solid #01684D 3px");
        addStyle("fieldset", "background-color",
                "#fffff6");
        addStyle("fieldset", "margin", "10pt");
        addStyle("fieldset", "padding-bottom", "0");
        addStyle("legend", "font-weight", "bold");
        addStyle("legend", "padding-left", "8pt");
        addStyle("legend", "padding-right", "8pt");
        addStyle("legend", "padding-top", "2pt");
        addStyle("legend", "padding-bottom", "2pt");
        addStyle("legend", "border", "solid #01684D 1px");
        addStyle("legend", "background-color", "#f6fff6");

        addStyle(".largerbox", "font-size", "12pt");
        addStyle(".largerbox", "padding", "3pt");
        addStyle(".largerbox", "margin-left", "10pt");
        addStyle(".largerbox", "margin-right", "10pt");
        addStyle(".largerbox", "background-color",
                "#f6fff6");
        addStyle(".largerbox", "border",
                "solid #01684D 1px");

        addStyle(".larger", "font-size", "12pt");

        addStyle(".bar", "background-color", DARK_TEXT_COLOR);
        addStyle(".bar", "font-family", "sans-serif");
        addStyle(".bar", "font-size", "12pt");
        addStyle(".bar", "color", "#f0f0d0");
        addStyle(".bar", "font-weight", "bold");
        addStyle(".bar", "padding", "2pt 9pt 1pt 9pt");
        addStyle(".bar", "margin", "0");
        addStyle(".bar", "margin-top", "6pt");
        addStyle(".bar", "margin-bottom", "6pt");

        addStyle(".indent1", "padding-left", "8pt");
        addStyle(".indent11", "padding-left", "8pt");
        addStyle(".indent11", "padding-right", "8pt");

        addStyle(".indent2", "padding-left", "16pt");
        addStyle(".indent22", "padding-left", "16pt");
        addStyle(".indent22", "padding-right", "16pt");

        addStyle(".indent3", "padding-left", "24pt");
        addStyle(".indent31", "padding-left", "8pt");
        addStyle(".indent31", "padding-right", "8pt");
        addStyle(".indent33", "padding-left", "24pt");
        addStyle(".indent33", "padding-right", "24pt");

        addStyle(".indent4", "padding-left", "33pt");
        addStyle(".indent41", "padding-left", "32pt");
        addStyle(".indent41", "padding-right", "8pt");
        addStyle(".indent44", "padding-left", "32pt");
        addStyle(".indent44", "padding-right", "32pt");

        addStyle("ul.boxlist", "margin", "0");
        addStyle("ul.boxlist", "margin-top", "6pt");
        addStyle("ul.boxlist", "padding", "0");
        addStyle("ul.boxlist", "padding-left", "13pt");

        addStyle("li.boxlist", "list-style", "none");
        addStyle("li.boxlist", "padding", "0");
        addStyle("li.boxlist", "padding-left", "20pt");
        addStyle("li.boxlist", "padding-right", "8pt");
        addStyle("li.boxlist", "margin", "0");
        addStyle("li.boxlist", "margin-bottom", "7pt");
        addStyle("li.boxlist", "padding-top", "1px");
        addStyle("li.boxlist", "background-repeat",
                "no-repeat");
        addStyle("li.boxlist", "background-image",
                "url('/images/yellow-lozenge-16.png')");
        addStyle("li.boxlist", "background-position",
                "0 0.2em");

        addStyle("li.bullet", "list-style", "none");
        addStyle("li.bullet", "padding", "0");
        addStyle("li.bullet", "padding-left", "15pt");
        addStyle("li.bullet", "padding-right", "8pt");
        addStyle("li.bullet", "margin", "0");
        addStyle("li.bullet", "margin-bottom", "6pt");
        addStyle("li.bullet", "background-repeat",
                "no-repeat");
        addStyle("li.bullet", "background-image",
                "url('/images/bullet.png')");
        addStyle("li.bullet", "background-position",
                "0 0.2em");

        addStyle(".gray", "color", "#888");
        addStyle(".green", "color", "#007400");
        addStyle(".blue", "color", "#000074");
        addStyle(".red", "color", "#740000");
        addStyle(".orange", "color", "#CC7000");
        addStyle(".redred", "color", "#CC0000");

        addStyle(".lightblue", "background-color",
                "#efefff");
        addStyle(".medblue", "background-color",
                "#d0d0ff");
        addStyle(".lightgreen", "background-color",
                "#e0ffe0");
        addStyle(".medgreen", "background-color",
                "#ccffcc");
        addStyle(".lightgold", "background-color",
                "#ffffd8");
        addStyle(".lighttan", "background-color",
                "#fff0e0");
        addStyle(".lightgray", "background-color",
                "#f2f2f2");

        addStyle(".tlightblue", "background-color",
                "#efefff");
        addStyle(".tlightblue", "border-width",
                "1px 0 0 0");
        addStyle(".tlightblue", "border-color", "#777");
        addStyle(".tlightblue", "border-style", "solid");
        addStyle(".tlightblue", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tmedblue", "background-color",
                "#d0d0ff");
        addStyle(".tmedblue", "border-width",
                "1px 0 0 0");
        addStyle(".tmedblue", "border-color", "#777");
        addStyle(".tmedblue", "border-style", "solid");
        addStyle(".tmedblue", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tlightgreen", "background-color",
                "#e0ffe0");
        addStyle(".tlightgreen", "border-width",
                "1px 0 0 0");
        addStyle(".tlightgreen", "border-color", "#777");
        addStyle(".tlightgreen", "border-style", "solid");
        addStyle(".tlightgreen", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tmedgreen", "background-color",
                "#ccffcc");
        addStyle(".tmedgreen", "border-width",
                "1px 0 0 0");
        addStyle(".tmedgreen", "border-color", "#777");
        addStyle(".tmedgreen", "border-style", "solid");
        addStyle(".tmedgreen", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tlightgold", "background-color",
                "#ffffd8");
        addStyle(".tlightgold", "border-width",
                "1px 0 0 0");
        addStyle(".tlightgold", "border-color", "#777");
        addStyle(".tlightgold", "border-style", "solid");
        addStyle(".tlightgold", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tlighttan", "background-color",
                "#fff0e0");
        addStyle(".tlighttan", "border-width",
                "1px 0 0 0");
        addStyle(".tlighttan", "border-color", "#777");
        addStyle(".tlighttan", "border-style", "solid");
        addStyle(".tlighttan", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tlightgray", "background-color",
                "#f2f2f2");
        addStyle(".tlightgray", "border-width",
                "1px 0 0 0");
        addStyle(".tlightgray", "border-color", "#777");
        addStyle(".tlightgray", "border-style", "solid");
        addStyle(".tlightgray", "padding",
                ".1em .5em .1em .5em");

        addStyle(".blightgold", "background-color",
                "#ffffd8");
        addStyle(".blightgold", "border-width",
                "0 0 1px 0");
        addStyle(".blightgold", "border-color", "#777");
        addStyle(".blightgold", "border-style", "solid");
        addStyle(".blightgold", "padding",
                ".1em .5em .1em .5em");
        addStyle(".blighttan", "background-color",
                "#fff0e0");
        addStyle(".blighttan", "border-width",
                "0 0 1px 0");
        addStyle(".blighttan", "border-color", "#777");
        addStyle(".blighttan", "border-style", "solid");
        addStyle(".blighttan", "padding",
                ".1em .5em .1em .5em");
        addStyle(".blightgray", "background-color",
                "#f2f2f2");
        addStyle(".blightgray", "border-width",
                "0 0 1px 0");
        addStyle(".blightgray", "border-color", "#777");
        addStyle(".blightgray", "border-style", "solid");
        addStyle(".blightgray", "padding",
                ".1em .5em .1em .5em");

        addStyle(".nlightgreen", "background-color",
                "#e0ffe0");
        addStyle(".nlightgreen", "padding",
                ".1em .5em .1em .5em");
        addStyle(".nlightblue", "background-color",
                "#efefff");
        addStyle(".nlightblue", "padding",
                ".1em .5em .1em .5em");
        addStyle(".nlightgold", "background-color",
                "#ffffd8");
        addStyle(".nlightgold", "padding",
                ".1em .5em .1em .5em");
        addStyle(".nlighttan", "background-color",
                "#fff0e0");
        addStyle(".nlighttan", "padding",
                ".1em .5em .1em .5em");
        addStyle(".nlightgray", "background-color",
                "#f2f2f2");
        addStyle(".nlightgray", "padding",
                ".1em .5em .1em .5em");

        addStyle(".tblightblue", "background-color",
                "#efefff");
        addStyle(".tblightblue", "border-width",
                "1px 0 1px 0");
        addStyle(".tblightblue", "border-color", "#777");
        addStyle(".tblightblue", "border-style", "solid");
        addStyle(".tblightblue", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tbmedblue", "background-color",
                "#d0d0ff");
        addStyle(".tbmedblue", "border-width",
                "1px 0 1px 0");
        addStyle(".tbmedblue", "border-color", "#777");
        addStyle(".tbmedblue", "border-style", "solid");
        addStyle(".tbmedblue", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tblightgreen", "background-color",
                "#e0ffe0");
        addStyle(".tblightgreen", "border-width",
                "1px 0 1px 0");
        addStyle(".tblightgreen", "border-color", "#777");
        addStyle(".tblightgreen", "border-style",
                "solid");
        addStyle(".tblightgreen", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tbmedgreen", "background-color",
                "#ccffcc");
        addStyle(".tbmedgreen", "border-width",
                "1px 0 1px 0");
        addStyle(".tbmedgreen", "border-color", "#777");
        addStyle(".tbmedgreen", "border-style", "solid");
        addStyle(".tbmedgreen", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tblightgold", "background-color",
                "#ffffd8");
        addStyle(".tblightgold", "border-width",
                "1px 0 1px 0");
        addStyle(".tblightgold", "border-color", "#777");
        addStyle(".tblightgold", "border-style", "solid");
        addStyle(".tblightgold", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tblighttan", "background-color",
                "#fff0e0");
        addStyle(".tblighttan", "border-width",
                "1px 0 1px 0");
        addStyle(".tblighttan", "border-color", "#777");
        addStyle(".tblighttan", "border-style", "solid");
        addStyle(".tblighttan", "padding",
                ".1em .5em .1em .5em");
        addStyle(".tblightgray", "background-color",
                "#f2f2f2");
        addStyle(".tblightgray", "border-width",
                "1px 0 1px 0");
        addStyle(".tblightgray", "border-color", "#777");
        addStyle(".tblightgray", "border-style", "solid");
        addStyle(".tblightgray", "padding",
                ".1em .5em .1em .5em");

        addStyle(".boxed", "background-color", "#efefff");
        addStyle(".boxed", "padding", "3pt 4pt 3pt 4pt");
        addStyle(".boxed", "border", "1px solid #004f39");
        addStyle(".boxed", "line-height", "1.4em");

        addStyle(".boxed2", "background-color",
                "#efefff");
        addStyle(".boxed2", "padding", "4pt 5pt 4pt 5pt");
        addStyle(".boxed2", "border",
                "4px double #004f39");
        addStyle(".boxed2", "line-height", "1.4em");

        addStyle(".errorbox", "background-color",
                "#f9f9eb");
        addStyle(".errorbox", "padding",
                "3pt 12pt 3pt 12pt");
        addStyle(".errorbox", "border", "3px solid #800");
        addStyle(".errorbox", "line-height", "1.4em");

        addStyle(".tablebox", "background-color",
                "#f9f9eb");
        addStyle(".tablebox", "border",
                "1px solid #01684D");
        addStyle(".tablebox", "margin-left", "auto");
        addStyle(".tablebox", "margin-right", "auto");

        addStyle(".scoretable", "background-color",
                "#f9f9eb");
        addStyle(".scoretable", "border",
                "3px double #b5ae91");
        addStyle(".scoretable", "padding", "3pt");
        addStyle(".scoretable", "border-collapse",
                "separate");

        addStyle(".scoreh", "padding", "1pt 5pt 1pt 5pt");
        addStyle(".scoreh", "background-color",
                "#f9f9eb");
        addStyle(".scoreh", "text-align", "right");
        addStyle(".scoreh", "font-weight", "bold");
        addStyle(".scoreh", "border-color", "#b5ae91");
        addStyle(".scoreh", "border-style", "solid");
        addStyle(".scoreh", "border-width", "0 1px 0 0");
        addStyle(".scoreh", "min-width", "60pt");

        addStyle(".scored", "padding", "1pt 5pt 1pt 5pt");
        addStyle(".scored", "background-color",
                "#f9f9eb");
        addStyle(".scored", "min-width", "60pt");

        addStyle(".pacetable", "background-color",
                "#f9f9eb");
        addStyle(".pacetable", "border",
                "3px double #b5ae91");
        addStyle(".pacetable", "padding", "3pt");
        addStyle(".pacetable", "border-collapse",
                "separate");

        addStyle(".paceh", "padding", "1pt 5pt 1pt 5pt");
        addStyle(".paceh", "background-color", "#f9f9eb");
        addStyle(".paceh", "text-align", "left");
        addStyle(".paceh", "font-weight", "bold");
        addStyle(".paceh", "border-color", "#b5ae91");
        addStyle(".paceh", "border-style", "solid");
        addStyle(".paceh", "border-width", "0 0 1px 0");
        addStyle(".paceh", "min-width", "60pt");

        addStyle(".paced", "padding", "1pt 5pt 1pt 5pt");
        addStyle(".paced", "background-color", "#f9f9eb");
        addStyle(".paced", "min-width", "60pt");

        addFontsToStyle("h1");
        addStyle("h1", "margin", "0");
        addStyle("h1", "padding-top", "2pt");
        addStyle("h1", "color", "#000");
        addStyle("h1", "padding-bottom", "9pt");
        addStyle("h1", "font-size", "13pt");
        addStyle("h1 span", "font-size", "13pt");
        addStyle("h1 a", "font-size", "13pt");

        addStyle("h1.menu", "display", "block");
        addStyle("h1.menu", "color", DARK_TEXT_COLOR);
        addStyle("h1.menu", "margin", "0 0 2pt 0");
        addStyle("h1.menu", "padding", "2pt 0 2pt 5pt");
        addStyle("h1.menu", "font-size", "11pt");
        addStyle("h1.menu", "background-color", HEADING_BG);
        addStyle("h1.menu", "font-weight", "bold");
        addStyle("h1.menu", "text-align", "left");
        addStyle("h1.menu", "border-width",
                "2px 0 1px 0");
        addStyle("h1.menu", "border-color", DARK_TEXT_COLOR);
        addStyle("h1.menu", "border-style",
                "solid none solid none");

        addFontsToStyle("h2");
        addStyle("h2", "margin", "0");
        addStyle("h2", "padding-top", "2pt");
        addStyle("h2", "color", "#000");
        addStyle("h2", "padding-bottom", "9pt");
        addStyle("h2", "font-size", "14pt");
        addStyle("h2 span", "font-size", "14pt");
        addStyle("h2 a", "font-size", "14pt");

        // addStyle("h2.menu", "display", "block");   
        // addStyle("h2.menu", "color"), DARK_TEXT_COLOR);
        // 
        // addStyle("h2.menu", "margin", "2pt 2px 0 2px");
        // 
        // addStyle("h2.menu", "padding", "1pt 7pt 2pt 5pt");
        // 
        // addStyle("h2.menu", "font-size", "10pt");   
        // addStyle("h2.menu", "background-color"), FOLDER_BG);
        // 
        // addStyle("h2.menu", "font-weight", "bold");   
        // addStyle("h2.menu", "text-align", "left");   
        // addStyle("h2.menu", "border-width", "1px 1px 0 1px");
        // 
        // addStyle("h2.menu", "border-style", "inset");   
        // addStyle("h2.menu", "border-color"), FOLDER_OUTLINE);
        // 
        // addStyle("h2.menu", "border-radius", "6pt 6pt 0 0");
        // 
        // addStyle("h2.menu", "float", "left");   

        addStyle("h2.menu", "display", "block");
        addStyle("h2.menu", "color", DARK_TEXT_COLOR);
        addStyle("h2.menu", "margin", "2pt 0 0 2pt");
        addStyle("h2.menu", "padding", "0");
        addStyle("h2.menu", "font-size", "10pt");
        addStyle("h2.menu", "font-weight", "bold");
        addStyle("h2.menu", "font-style", "italic");
        addStyle("h2.menu", "text-align", "left");

        addStyle(".startmenu", "clear", "both");
        addStyle(".startmenu", "margin", "0 2px 0 2px");
        addStyle(".startmenu", "padding", "0");
        addStyle(".startmenu", "height", "0");
        addStyle(".startmenu", "border-width",
                "1px 0 0 0");
        addStyle(".startmenu", "border-style", "inset");
        addStyle(".startmenu", "border-color", FOLDER_OUTLINE);

        addStyle(".endmenu", "clear", "both");
        addStyle(".endmenu", "margin", "0 2px 0 2px");
        addStyle(".endmenu", "padding", "0");
        addStyle(".endmenu", "height", "0");
        addStyle(".endmenu", "border-width", "0 0 1px 0");
        addStyle(".endmenu", "border-style", "inset");
        addStyle(".endmenu", "border-color", FOLDER_OUTLINE);

        addFontsToStyle("h3");
        addStyle("h3", "margin", "0");
        addStyle("h3", "padding-top", "2pt");
        addStyle("h3", "color", "#000");
        addStyle("h3", "padding-bottom", "5pt");
        addStyle("h3", "font-size", "11pt");
        addStyle("h3 span", "font-size", "11pt");
        addStyle("h3 a", "font-size", "11pt");

        addStyle(".h3", "color", "#002000");
        addStyle(".h3", "font-size", "11pt");
        addStyle(".h3", "font-weight", "bold");
        addStyle(".h3", "margin", "0");
        addStyle(".h3", "padding-bottom", "1pt");

        addFontsToStyle("h4");
        addStyle("h4", "margin", "0");
        addStyle("h4", "padding-top", "2pt");
        addStyle("h4", "color", "#000");
        addStyle("h4", "padding-bottom", "2pt");
        addStyle("h4", "font-size", "11pt");
        addStyle("h4 span", "font-size", "11pt");
        addStyle("h4 a", "font-size", "11pt");

        addFontsToStyle("h5");
        addStyle("h5", "margin-top", "0");
        addStyle("h5", "margin-bottom", "0");
        addStyle("h5", "padding-top", "0");
        addStyle("h5", "color", "#000");
        addStyle("h5", "font-size", "11pt");

        addStyle(".formlabel", "min-width", "11em");
        addStyle(".formlabel", "text-align", "right");
        addStyle(".formlabel", "float", "left");
        addStyle(".formlabel", "padding-right", "1em");
        addStyle(".formlabel", "line-height", "2em");
        addStyle(".formlabel", "font-weight", "bold");
        addStyle(".formlabel", "white-space", "nowrap");

        addStyle(".formfield", "float", "left");
        addStyle(".formfield", "line-height", "2em");

        addStyle(".formline", "padding-top", "1pt");
        addStyle(".formline", "padding-bottom", "1pt");

        addStyle(".dim", "color", "#7F7F7F");

        addStyle(".smalldim", "color", "#7F7F7F");
        addStyle(".smalldim", "font-size", "10pt");
        addStyle(".smalldim", "font-weight", "normal");

        addStyle(".dots", "width", "213px");
        addStyle(".dots", "margin", "0 2px 0 2px");
        addStyle(".dots", "padding", "0");
        addStyle(".dots", "background-image",
                "url('/images/elipsis.gif')");
        addStyle(".dots", "background-repeat",
                "repeat-x");
        addStyle(".dots", "border-width", "0 1px 0 1px");
        addStyle(".dots", "border-style", "inset");
        addStyle(".dots", "border-color", FOLDER_OUTLINE);
        addStyle(".dots", "background-color", FOLDER_BG);

        addStyle(".nodots", "width", "213px");
        addStyle(".nodots", "margin", "0 2px 0 2px");
        addStyle(".nodots", "padding", "0");
        addStyle(".nodots", "border-width",
                "0 1px 0 1px");
        addStyle(".nodots", "border-style", "inset");
        addStyle(".nodots", "border-color", FOLDER_OUTLINE);
        addStyle(".nodots", "background-color", FOLDER_BG);

        addStyle(".title", "display", "block");
        addStyle(".title", "font-size", "13pt");
        addStyle(".title", "font-weight", "bold");
        addStyle(".title", "margin", "0");
        addStyle(".title", "margin-bottom", "3pt");
        addStyle(".title", "margin-top", "7pt");
        addStyle(".title", "color", "#f0f0d0");
        addStyle(".title", "background-color", DARK_TEXT_COLOR);
        addStyle(".title", "text-align", "left");
        addStyle(".title", "padding", "2pt 6pt 0 6pt");
        addStyle(".title", "border-width", "1px");
        addStyle(".title", "border-color", "black");
        addStyle(".title", "border-style",
                "solid none solid none");

        addStyle("div.vline", "float", "left");
        addStyle("div.vline", "padding", "0 2pt 0 2pt");
        addStyle("div.vline", "border-left",
                "1px solid #004f39");

        addStyle("div.vlines", "background-color",
                "#efefff");
        addStyle("div.vlines", "float", "left");
        addStyle("div.vlines", "margin-left", "2pt");
        addStyle("div.vlines", "margin-right", "0");
        addStyle("div.vlines", "margin-top", "2pt");
        addStyle("div.vlines", "margin-bottom", "2pt");
        addStyle("div.vlines", "padding",
                "2pt 5pt 4pt 5pt");
        addStyle("div.vlines", "border-style", "solid");
        addStyle("div.vlines", "border-color", DARK_TEXT_COLOR);
        addStyle("div.vlines", "border-width",
                "0 1px 0 1px");

        addStyle("div.aslines", "float", "left");
        addStyle("div.aslines", "margin-left", "2pt");
        addStyle("div.aslines", "margin-right", "0");
        addStyle("div.aslines", "margin-top", "1pt");
        addStyle("div.aslines", "margin-bottom", "1pt");
        addStyle("div.aslines", "padding", "0 3pt 0 3pt");

        addStyle("div.nav", "margin-top", "3pt");
        addStyle("div.nav", "float", "left");

        addStyle(".exambtn", "margin-left", "32pt");
        addStyle(".exambtn", "margin-top", "15pt");
        addStyle(".exambtn", "margin-bottom", "11pt");

        addStyle(".exambtnlow", "margin-left", "32pt");
        addStyle(".exambtnlow", "margin-top", "7pt");
        addStyle(".exambtnlow", "margin-bottom", "10pt");

        addStyle(".reviewbtn", "margin-left", "32pt");
        addStyle(".reviewbtn", "margin-top", "7pt");
        addStyle(".reviewbtn", "margin-bottom", "7pt");

        addStyle(".vid", "margin", "0");
        addStyle(".vid", "margin-left", "24pt");
        addStyle(".vid", "width", "600pt");
        addStyle(".vid", "float", "left");

        addStyle(".ex", "margin", "0");
        addStyle(".ex", "margin-left", "24pt");
        addStyle(".ex", "width", "150pt");
        addStyle(".ex", "float", "left");

        addStyle(".sol", "margin", "0");
        addStyle(".sol", "margin-left", "24pt");
        addStyle(".sol", "float", "left");

        addStyle("table", "border-collapse", "collapse");
        addStyle("table", "border-spacing", "0");

        addStyle("tr", "margin", "0");
        addStyle("tr.alternate:nth-child(even)", "background",
                "honeydew");

        addFontsToStyle("td");
        addStyle("td", "font-size", "11pt");
        addStyle("td", "margin", "0");
        addStyle("td", "padding", "1pt 2pt 1pt 2pt");
        addStyle("td.bl", "border-style", "solid");
        addStyle("td.bl", "border-color", "#007400");
        addStyle("td.bl", "border-width", "0 0 0 1px");
        addStyle("td.br", "border-style", "solid");
        addStyle("td.br", "border-color", "#007400");
        addStyle("td.br", "border-width", "0 1px 0 0");
        addStyle("td.blr", "border-style", "solid");
        addStyle("td.blr", "border-color", "#007400");
        addStyle("td.blr", "border-width", "0 1px 0 1px");
        addStyle("td.open", "padding", "2pt 4pt 2pt 4pt");

        addStyle("td.c", "text-align", "center");

        addStyle("td.tight", "text-align", "center");
        addStyle("td.tight", "padding", "0");
        addStyle("td.tightbl", "text-align", "center");
        addStyle("td.tightbl", "padding", "0");
        addStyle("td.tightbl", "border-style", "solid");
        addStyle("td.tightbl", "border-color", "#007400");
        addStyle("td.tightbl", "border-width",
                "0 0 0 1px");
        addStyle("td.tightbr", "text-align", "center");
        addStyle("td.tightbr", "padding", "0");
        addStyle("td.tightbr", "border-style", "solid");
        addStyle("td.tightbr", "border-color", "#007400");
        addStyle("td.tightbr", "border-width",
                "0 1px 0 0");
        addStyle("td.tightblr", "text-align", "center");
        addStyle("td.tightblr", "padding", "0");
        addStyle("td.tightblr", "border-style", "solid");
        addStyle("td.tightblr", "border-color",
                "#007400");
        addStyle("td.tightblr", "border-width",
                "0 1px 0 1px");

        addFontsToStyle("th");
        addStyle("th", "font-size", "11pt");
        addStyle("th", "font-weight", "bold");
        addStyle("th", "text-align", "center");
        addStyle("th", "vertical-align", "bottom");
        addStyle("th", "margin", "0");
        addStyle("th", "padding", "0 2pt 0 2pt");

        addStyle("th.bl", "border-style", "solid");
        addStyle("th.bl", "border-color", "#007400");
        addStyle("th.bl", "border-width", "0 0 0 1px");
        addStyle("th.br", "border-style", "solid");
        addStyle("th.br", "border-color", "#007400");
        addStyle("th.br", "border-width", "0 1px 0 0");
        addStyle("th.blr", "border-style", "solid");
        addStyle("th.blr", "border-color", "#007400");
        addStyle("th.blr", "border-width", "0 1px 0 1px");

        addStyle(".outerlogindiv", "text-align",
                "center");
        addStyle(".outerlogindiv", "background-color",
                "#e0ffe0");
        addStyle(".outerlogindiv", "padding", "2pt");
        addStyle(".outerlogindiv", "border",
                "1px solid #aaddaa");

        addStyle(".innerlogindiv", "margin", "0 auto");
        addStyle(".innerlogindiv", "padding", "0");
        addStyle(".innerlogindiv", "width", "200pt");
        addStyle(".innerlogindiv", "text-align",
                "left ! important");

        addStyle(".loginform", "margin", "0");
        addStyle(".loginform", "padding", "0");

        addStyle(".ename_label_div", "margin-top", "3pt");
        addStyle(".ename_label_div", "float", "left");
        addStyle(".ename_label_div", "clear", "left");
        addStyle(".ename_label_div", "width", "75pt");

        addStyle(".ename_input_div", "margin-top", "1pt");
        addStyle(".ename_input_div", "float", "left");
        addStyle(".ename_input_div", "width", "115pt");

        addStyle(".ename", "width", "105pt");

        addStyle(".ename_label", "font-weight", "bold");

        addStyle(".epassword_label_div", "margin-top",
                "3pt");
        addStyle(".epassword_label_div", "float", "left");
        addStyle(".epassword_label_div", "clear", "left");
        addStyle(".epassword_label_div", "width", "75pt");

        addStyle(".epassword_input_div", "margin-top",
                "1pt");
        addStyle(".epassword_input_div", "float", "left");
        addStyle(".epassword_input_div", "width",
                "115pt");

        addStyle(".epassword", "width", "110pt");

        addStyle(".epassword_label", "font-weight",
                "bold");

        addStyle(".openforgoteidlink_div", "margin-top",
                "3pt");
        addStyle(".openforgoteidlink_div", "clear",
                "left");
        addStyle(".openforgoteidlink_div", "text-align",
                "center");
        addStyle(".openforgoteidlink_div", "font-size",
                "80%");

        addStyle(".authenticate_form_submit_div", "margin-top",
                "10pt");
        addStyle(".authenticate_form_submit_div", "text-align",
                "center");

        addStyle(".authenticate_form_locl", "text-align",
                "center");
        addStyle(".authenticate_form_locl", "background-color",
                "#DDDDFF");
        addStyle(".authenticate_form_locl", "padding",
                "2pt");
        addStyle(".authenticate_form_locl", "border",
                "1px solid #AAAADD");

        addStyle(".authenticate_form_div", "margin",
                "0 auto");
        addStyle(".authenticate_form_div", "padding",
                "0");
        addStyle(".authenticate_form_div", "width",
                "200pt");
        addStyle(".authenticate_form_div", "text-align",
                "left ! important");

        addStyle(".local_login_div", "padding-bottom",
                "11pt");

        addStyle(".username_label_div", "margin-top",
                "3pt");
        addStyle(".username_label_div", "float", "left");
        addStyle(".username_label_div", "clear", "left");
        addStyle(".username_label_div", "width", "75pt");

        addStyle(".username_input_div", "margin-top",
                "1pt");
        addStyle(".username_input_div", "float", "left");
        addStyle(".username_input_div", "width", "115pt");

        addStyle(".username", "width", "110pt");

        addStyle(".username_label", "font-weight",
                "bold");

        addStyle(".password_label_div", "margin-top",
                "3pt");
        addStyle(".password_label_div", "float", "left");
        addStyle(".password_label_div", "clear", "left");
        addStyle(".password_label_div", "width", "75pt");

        addStyle(".password_input_div", "margin-top",
                "1pt");
        addStyle(".password_input_div", "float", "left");
        addStyle(".password_input_div", "width", "115pt");

        addStyle(".password", "width", "110pt");

        addStyle(".password_label", "font-weight",
                "bold");

        addStyle(".hidden", "display", "none");
        addStyle(".visible", "display", "inline");
        addStyle(".right", "text-align", "right");

        addStyle("a.button", "font-size", "1.3em");
        addStyle("a.button", "border-radius", "9px");
        addStyle("a.button", "padding", "5px 15px");
        addStyle("a.button", "color", "white");
        addStyle("a.button", "background-color",
                "#015915 !important");
        addStyle("a.button", "box-shadow",
                "3px 3px 3px #999");
        addStyle("a.button", "border", "1px solid #031");
        addStyle("a.button", "font-weight", "bold");
        addStyle("a.button", "text-decoration", "none");
        addStyle("a.button", "vertical-align",
                "baseline");

        addStyle("a.btn", "-webkit-border-radius", "0");
        addStyle("a.btn", "-moz-border-radius", "0");
        addStyle("a.btn", "display", "inline-block");
        addStyle("a.btn", "border-radius", "0px");
        addStyle("a.btn", "font-family",
                "prox-regular,sans-serif");
        addStyle("a.btn", "color", "#ffffff");
        addStyle("a.btn", "font-size", "20px");
        addStyle("a.btn", "line-height", "1.2em");
        addStyle("a.btn", "background", "#1e4d2b");
        addStyle("a.btn", "padding", "10px 20px");
        addStyle("a.btn", "margin", "10px 15px");
        addStyle("a.btn", "min-width", "250px");
        addStyle("a.btn", "text-decoration", "none");
        addStyle("a.btn", "white-space", "nowrap");
        addStyle("a.btn", "white-space", "nowrap");
        addStyle("a.btn", "white-space", "nowrap");

        addStyle("a.btn:hover", "background", "#257742");
        addStyle("a.btn:hover", " text-decoration",
                "none");

        // Styles used by the "HtmlGenerator" class to embed a view of POJO state in web pages.

        addStyle("a.objhtml", "text-decoration", "none");

        addStyle("ul.objhtml ul", "border-style",
                "ridge");
        addStyle("ul.objhtml ul", "border-color",
                "#ddddee");
        addStyle("ul.objhtml ul", "border-width",
                "0 0 0 2px");

        addStyle("ul.objhtml, .objhtml li", "list-style",
                "none");
        addStyle("ul.objhtml, .objhtml li", "margin",
                "0");
        addStyle("ul.objhtml, .objhtml li", "padding",
                "1px 4px");
        addStyle("ul.objhtml, .objhtml li", "cursor",
                "pointer");

        addStyle(".objhtml ul", "display", "none");
        addStyle(".objhtml ul", "padding-left", "10px");

        addStyle(".objhtml > li", "display", "block");
        addStyle(".objhtml > li", "background",
                "#f4f4ff");
        addStyle(".objhtml > li", "margin-bottom", "1px");

        addStyle(".objhtml span", "display", "block");
        addStyle(".objhtml span", "padding", "2px");

        addStyle(".objhtml .hasChildren > .expanded", "background",
                "#ddddee");
        addStyle(".objhtml .hasChildren > .expanded a", "color",
                "#fff");

        addStyle(".objhtml .noChildren", "padding-left",
                "26px");

        addStyle(".objicon", "display", "inline-block");

        addStyle(".objicon:before", "content",
                "\"+\"");
        addStyle(".objicon:before", "display",
                "inline-block");
        addStyle(".objicon:before", "min-width",
                "20px");
        addStyle(".objicon:before", "text-align",
                "center");

        addStyle(".expanded > .objicon:before",
                "content", "\"-\"");

        addStyle(".show-effect", "display",
                "block!important");

        addStyle(".sr-only", "position",
                "absolute");
        addStyle(".sr-only", "width",
                "1px");
        addStyle(".sr-only", "height",
                "1px");
        addStyle(".sr-only", "magin",
                "-1px");
        addStyle(".sr-only", "padding",
                "0");
        addStyle(".sr-only", "overflow",
                "hidden");
        addStyle(".sr-only", "clip",
                "rect(0, 0, 0, 0)");
        addStyle(".sr-only", "border",
                "0");

        addStyle(".no-sr", "speak",
                "none");
    }

    /**
     * Gets the singleton {@code BasicCss} instance.
     *
     * @return the instance
     */
    public static BasicCss getInstance() {

        synchronized (INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new BasicCss();
            }

            return instance;
        }
    }
}
