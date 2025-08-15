package dev.mathops.web.site;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.sql.SQLException;

/**
 * A base class for web pages.
 */
public enum Page {
    ;

    /** Show no bars. */
    public static final int NO_BARS = 0x00;

    /** Show the user/date bar (could be combined with '|' with ADMIN_BAR). */
    public static final int USER_DATE_BAR = 0x01;

    /** Show the admin bar (could be combined with '|' with USER_DATE_BAR). */
    public static final int ADMIN_BAR = 0x02;

    /** The MIME type text/html. */
    public static final String MIME_TEXT_HTML = "text/html";

    /** The MIME type text/xml. */
    public static final String MIME_TEXT_XML = "text/xml";

    /** The MIME type text/csv. */
    public static final String MIME_TEXT_CSV = "text/csv";

    /** The MIME type text/json. */
    public static final String MIME_APP_JSON = "application/json";

    /**
     * Appends the start of the HTML page (the DOCTYPE declaration, opening of the &lt;html&gt; element, &lt;head&gt;
     * block, opening of &lt;body&gt;, and two surrounding &lt;div&gt;s) to an {@code HtmlBuilder}.
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param title          the title (to be followed by " | Department of Mathematics")
     * @param allowIndex     {@code true} to allow search engines to index the page
     * @param includeScripts {@code true} to include scripts to resize header/footer
     */
    public static void startPage(final HtmlBuilder htm, final String title, final boolean allowIndex,
                                 final boolean includeScripts) {

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");

        if (!allowIndex) {
            htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        }

//        htm.addln("<script async src='https://www.googletagmanager.com/gtag/js?id=G-JTNEG80W4C'></script>")
//                .addln("<script>")
//                .addln("window.dataLayer = window.dataLayer || [];")
//                .addln("function gtag(){dataLayer.push(arguments);}")
//                .addln("gtag('js', new Date());")
//                .addln("gtag('config', 'G-JTNEG80W4C');")
//                .addln("</script>");

        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <meta name='viewport' content='width=device-width, initial-scale=1'>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")

                .addln(" <link rel='icon' type='image/x-icon' href='/www/images/favicon.ico'>")
                .addln(" <title>", title, " | Department of Mathematics</title>");

        if (includeScripts) {
            emitScripts(htm);
        }
        htm.addln("</head>");
    }

    /**
     * Appends the start of the HTML page (the DOCTYPE declaration, opening of the &lt;html&gt; element, &lt;head&gt;
     * block, opening of &lt;body&gt;, and two surrounding &lt;div&gt;s) to an {@code HtmlBuilder}.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param title      the title (to be followed by " | Department of Mathematics")
     * @param allowIndex {@code true} to allow search engines to index the page
     */
    private static void startPageNoFooter(final HtmlBuilder htm, final String title, final boolean allowIndex) {

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");

        if (!allowIndex) {
            htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        }

        htm.addln("<script async src='https://www.googletagmanager.com/gtag/js?id=G-JTNEG80W4C'></script>")
                .addln("<script>")
                .addln("window.dataLayer = window.dataLayer || [];")
                .addln("function gtag(){dataLayer.push(arguments);}")
                .addln("gtag('js', new Date());")
                .addln("gtag('config', 'G-JTNEG80W4C');")
                .addln("</script>")

                .addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <meta name='viewport' content='width=device-width, initial-scale=1'>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")

                .addln(" <link rel='icon' type='image/x-icon' href='/www/images/favicon.ico'>")
                .addln(" <title>", title, " | Department of Mathematics</title>");

        emitScriptsNoFooter(htm);
        htm.addln("</head>");
    }

    /**
     * Emits the contents of a file
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param filename the name of the file to emit
     */
    public static void emitFile(final HtmlBuilder htm, final String filename) {

        final File file = new File("/opt/zircon/html/" + filename);
        final String content = FileLoader.loadFileAsString(file, true);

        if (content != null) {
            htm.add(content);
        }
    }

    /**
     * Emits the header (CSU responsive brand logo and department title).
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param subtitle     an optional subtitle
     * @param subtitleLink the URL to which the subtitle should link
     * @param maximizeUrl  to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                     maximizing
     */
    private static void emitHeader(final HtmlBuilder htm, final String subtitle,
                                   final String subtitleLink, final String maximizeUrl) {

        htm.addln("<header role='banner' id='page_banner'>");

        htm.sDiv("brandbar");
        htm.sDiv("container");
        htm.addln("<section id='BrandLogo'>");
        htm.sDiv("responsiveLogoContainer");
        htm.sDiv("screenLG", "id='responsiveLogo'");
        htm.sDiv();

        htm.sH(1);
        htm.addln("<a href='https://www.colostate.edu'>");
        htm.addln("<span class='sr-only'>", Res.get(Res.LOGO_TEXT_ONLY), "</span>");
        htm.addln("<svg xmlns='http://www.w3.org/2000/svg' viewBox='5 5 979 115' ",
                "enable-background='new 5 5 979 115'>");
        htm.addln("<title>", Res.get(Res.LOGO_TEXT_ONLY), "</title>");
        htm.addln("<defs>");
        htm.addln(" <g id='signatureword'>");
        htm.addln("  <g id='signaturetop'>");
        htm.addln("   <path d='m159.1 42.2h-12.8c-5.7 0-8.8 2.8-8.8 8v",
                "22.6c0 5.2 3.1 8 8.8 8h12.8c5.7 0 8.8-2.8 8.8-8v-6h-8.5v5.6c0 .5-.1 1.",
                "1-1.4 1.1h-10.8c-1.1 0-1.3-.5-1.3-1.1v-21.8c0-.6.2-1.1 1.3-1.1h10.8c1.",
                "3 0 1.4.6 1.4 1.1v5.6h8.5v-6c0-5.1-3.2-8-8.8-8'></path>");
        htm.addln("   <path d='m198.4 42.2h-13.5c-5.7 0-8.8 2.8-8.8 8v",
                "22.6c0 5.2 3.1 8 8.8 8h13.5c5.7 0 8.8-2.8 8.8-8v-22.6c0-5.1-3.2-8-8.8-",
                "8m.3 8.4v21.9c0 .5-.1 1.1-1.4 1.1h-11.5c-1.1 0-1.3-.5-1.3-1.1v-21.9c0-",
                ".6.2-1.1 1.3-1.1h11.5c1.3 0 1.4.5 1.4 1.1'></path>");
        htm.addln("   <path d='m223.9 42.2h-8.5v38.6h26.5v-7.3h-18z'>",
                "</path>");
        htm.addln("   <path d='m270.8 42.2h-13.5c-5.7 0-8.8 2.8-8.8 8v",
                "22.6c0 5.2 3.1 8 8.8 8h13.5c5.7 0 8.8-2.8 8.8-8v-22.6c0-5.1-3.1-8-8.8-",
                "8m.3 8.4v21.9c0 .5-.1 1.1-1.4 1.1h-11.5c-1.1 0-1.3-.5-1.3-1.1v-21.9c0-",
                ".6.2-1.1 1.3-1.1h11.5c1.3 0 1.4.5 1.4 1.1'></path>");
        htm.addln("   <path d='m317.9 58.5v-8.3c0-5.2-3.1-8-8.8-8h-21.",
                "3v38.6h8.5v-14.3h6.8l7.4 14.3h9.3l-7.6-14.6c3.7-.9 5.7-3.6 5.7-7.7m-21",
                ".6-9.1h11.9c1.1 0 1.3.5 1.3 1.1v7.8c0 .6-.2 1.1-1.3 1.1h-11.9v-10'>",
                "</path>");
        htm.addln("   <path d='m339.2 42.2l-14.2 38.6h8.9l3.1-9.1h13.3",
                "l3.1 9.1h9.3l-14.2-38.6h-9.3m8.7 22.2h-8.5l4.2-12.6 4.3 12.6'></path>");
        htm.addln("   <path d='m391 42.2h-21.8v38.6h21.8c5.7 0 8.8-2.8",
                " 8.8-8v-22.6c0-5.1-3.1-8-8.8-8m-13.2 7.3h12.3c1.1 0 1.3.5 1.3 1.1v21.9",
                "c0 .6-.2 1.1-1.3 1.1h-12.3v-24.1'></path>");
        htm.addln("   <path d='m430.2 42.2h-13.5c-5.7 0-8.8 2.8-8.8 8v",
                "22.6c0 5.2 3.1 8 8.8 8h13.5c5.7 0 8.8-2.8 8.8-8v-22.6c0-5.1-3.1-8-8.8-",
                "8m.4 8.4v21.9c0 .6-.2 1.1-1.4 1.1h-11.5c-1.1 0-1.3-.5-1.3-1.1v-21.9c0-",
                ".6.2-1.1 1.3-1.1h11.5c1.3 0 1.4.5 1.4 1.1'></path>");
        htm.addln("   <path d='m481.7 58.7l-10.6-2.1c-1.9-.4-2-.8-2-1.",
                "8v-4.3c0-.5.1-1.1 1.4-1.1h9.9c1.1 0 1.3.5 1.3 1.1v4.5h8.4v-4.7c0-5.2-3",
                ".1-8-8.8-8h-11.8c-5.7 0-8.8 2.8-8.8 8v5.4c0 5.6 4.3 7.3 8.9 8.2l10.6 2",
                ".2c1.8.4 2 .7 2 1.7v4.8c0 .7-.2 1.1-1.3 1.1h-10.7c-1.1 0-1.3-.5-1.3-1.",
                "1v-4.9h-8.4v5.1c0 5.2 3.1 8 8.8 8h12.5c5.7 0 8.8-2.8 8.8-8v-6c0-5.6-4.",
                "3-7.2-8.9-8.1'></path>");
        htm.addln("   <path d='m527.4 42.2h-31v7.2h11.3v31.4h8.5v-31.4",
                "h11.2z'></path>");
        htm.addln("   <path d='m540.4 42.2l-14.2 38.6h8.9l3.1-9.1h13.3",
                "l3.1 9.1h9.3l-14.2-38.6h-9.3m8.7 22.2h-8.5l4.2-12.6 4.3 12.6'></path>");
        htm.addln("   <path d='m562.6 49.4h11.3v31.4h8.4v-31.4h11.3v-7",
                ".2h-31z'></path>");
        htm.addln("   <path d='m599.5 80.8h27.9v-7.3h-19.4v-8.8h16v-7.",
                "2h-16v-8h19.4v-7.3h-27.9z'></path>");
        htm.addln("  </g>");
        htm.addln("  <g id='signaturebottom'>");
        htm.addln("   <path d='m672 72.4c0 .6-.2 1.1-1.3 1.1h-11.3c-1.",
                "2 0-1.4-.5-1.4-1.1v-30.2h-8.5v30.6c0 5.2 3.1 8 8.8 8h13.3c5.7 0 8.8-2.",
                "8 8.8-8v-30.6h-8.4v30.2'></path>");
        htm.addln("   <path d='m713.2 66.9l-16-24.7h-8.5v38.6h8.1v-25.",
                "4l16.5 25.4h8v-38.6h-8.1z'></path>");
        htm.addln("   <path d='m729.5 42.2h8.5v38.6h-8.5z'></path>");
        htm.addln("   <path d='m762.7 70.5l-8.8-28.3h-9.2l13.4 38.6h9.",
                "1l13.4-38.6h-9z'></path>");
        htm.addln("   <path d='m787.1 80.8h28v-7.3h-19.5v-8.8h16.1v-7.",
                "2h-16.1v-8h19.5v-7.3h-28z'></path>");
        htm.addln("   <path d='m853.1 58.5v-8.3c0-5.2-3.1-8-8.8-8h-21.",
                "3v38.6h8.5v-14.3h6.8l7.4 14.3h9.3l-7.6-14.6c3.6-.9 5.7-3.6 5.7-7.7m-21",
                ".7-9.1h11.9c1.1 0 1.3.5 1.3 1.1v7.8c0 .6-.2 1.1-1.3 1.1h-11.9v-10'>",
                "</path>");
        htm.addln("   <path d='m882 58.7l-10.6-2.1c-1.9-.4-2-.8-2-1.8v",
                "-4.3c0-.5.1-1.1 1.4-1.1h9.9c1.1 0 1.3.5 1.3 1.1v4.5h8.4v-4.7c0-5.2-3.1",
                "-8-8.8-8h-11.8c-5.7 0-8.8 2.8-8.8 8v5.4c0 5.6 4.3 7.3 8.9 8.2l10.6 2.2",
                "c1.8.4 2 .7 2 1.7v4.8c0 .7-.2 1.1-1.3 1.1h-10.7c-1.1 0-1.3-.5-1.3-1.1v",
                "-4.9h-8.4v5.1c0 5.2 3.1 8 8.8 8h12.5c5.7 0 8.8-2.8 8.8-8v-6c0-5.6-4.3-",
                "7.2-8.9-8.1'></path>");
        htm.addln("   <path d='m898.8 42.2h8.5v38.6h-8.5z'></path>");
        htm.addln("   <path d='m913.2 49.4h11.2v31.4h8.5v-31.4h11.2v-7",
                ".2h-30.9z'></path>");
        htm.addln("   <path d='m974.3 42.2l-9 16.8-9-16.8h-9.3l14 25.1",
                "v13.5h8.5v-13.5l14-25.1z'></path>");
        htm.addln("  </g>");
        htm.addln(" </g>");
        htm.addln("</defs>");

        htm.addln("<g id='signaturehead'>");
        htm.addln(" <g fill='#cbc46e'>");
        htm.addln("  <path d='m62.5 117.6c-30.4 0-55.1-24.7-55.1-55.1 ",
                "0-30.4 24.7-55.1 55.1-55.1 30.4 0 55.1 24.7 55.1 55.1 0 30.4-24.7 55.1",
                "-55.1 55.1'></path>");
        htm.addln("  <path d='M62.5,120C30.8,120,5,94.2,5,62.5C5,30.8,",
                "30.8,5,62.5,5C94.2,5,120,30.8,120,62.5 C120,94.2,94.2,120,62.5,120z'>",
                "</path>");
        htm.addln(" </g>");
        htm.addln(" <path fill='#004c23' d='m62.5 7.4c-30.4 0-55.1 24.",
                "7-55.1 55.1 0 30.4 24.7 55.1 55.1 55.1 30.4 0 55.1-24.7 55.1-55.1 0-30",
                ".4-24.7-55.1-55.1-55.1'></path>");
        htm.addln(" <g fill='#fff'>");
        htm.addln("  <path d='m53.8 75.5c1.5-2.3 3.1-7.5 2.2-9.6-1.5-.",
                "6-3.3-1.8-4.4-3.4-1.1-1.6-1.7-3.3-3.6-3.8-1.3-.5-2.7-.2-3.7.4-1.7 1-2.",
                "1 3.3-2.3 5.1-.1 3.8.6 9.3 2.2 12.8 1 2.6 3.2 6.9 6 11-.8-6.5 2.8-10.8",
                " 3.6-12.5m-4.9-8.3c-.7-1.3-2.6-1-3.5-1.6-1.4-.9-1.4-2.9 0-4.2 1.8-1.6 ",
                "6.3 3.5 5.3 7.6-.5.1-.7-.1-1.8-1.8'></path>");
        htm.addln("  <path d='m39.3 51.5c-2 .3-2.3 3.2-.9 5-1.4-.2-3.4",
                "-2-8.3-.5-1.7.5-3.6 1.2-3.8 3.4-.2 1.6 2 3.3 3.4 4.3 1.4 1.1 3.7 1.2 4",
                ".8 2.9 1 1.5 1.7 3.8 2 5.8.4 4 .4 8.9 3.1 12.2.5-1.9-.9-6.3-.9-11.9 0-",
                "2.1-.8-4.5-2.1-6 2.3-.2 2.3.1 2.5-.6.2-.7-.2-1.5-.7-2.1-1.6-1.9-4-.9-6",
                ".1-1.6-1.4-.5-3.5-1.2-3.6-3.1-.1-1.3 4-1.6 4-1.6 3.3.3 2.9 2.1 5.2 2.3",
                " 1.9.2 2.6-1.8 3.2-3 1-2.1 2-6.1-1.8-5.5'></path>");
        htm.addln("  <path d='m62.6 72.1c0 0 0 0 0 0-1.9 0-2.8 1.5-4.5",
                " 3.3-2.9 3.1-8.8 10.9-4.5 14.6 1.9 1.6 4.4 2.8 7.8 1.7.9-.6.4-2.3-.1-3",
                ".1-1.6-2.1-4.1-1.3-4.8-2.3-.7-1-.2-1.8.2-2.3.6-.7 1.6-.6 2.4-.3 1.5.3 ",
                "2.4.9 3.4 1.8v.1c0 0 0 0 0 0v-.1c.9-.9 1.9-1.5 3.4-1.8.8-.2 1.8-.4 2.3",
                ".3.4.5.9 1.3.2 2.3-.7 1-3.2.2-4.8 2.3-.4.8-1 2.5-.1 3.1 3.5 1 5.9-.1 7",
                ".8-1.7 4.3-3.7-1.6-11.5-4.5-14.6-1.4-1.8-2.4-3.3-4.2-3.3'></path>");
        htm.addln("  <path d='m95.4 55.9c-4.8-1.5-6.9.4-8.3.5 1.5-1.7 ",
                "1.2-4.7-.9-5-3.8-.6-2.7 3.5-1.8 5.4.6 1.2 1.3 3.2 3.2 3 2.3-.2 1.9-2 5",
                ".2-2.3 0 0 4.1.3 4 1.6-.2 1.9-2.3 2.6-3.7 3.1-2 .7-4.4-.3-6.1 1.6-.5.6",
                "-.9 1.4-.7 2.1.2.7.3.4 2.5.6-1.3 1.6-2 4-2 6-.1 5.6-1.5 10-.9 11.9 2.7",
                "-3.3 2.7-8.2 3.1-12.2.3-2.1 1-4.3 2-5.8 1.2-1.8 3.4-1.8 4.8-2.9 1.4-1 ",
                "3.5-2.7 3.4-4.3-.3-2.1-2.2-2.7-3.8-3.3'></path>");
        htm.addln("  <path d='m62.5 10.4c-28.7 0-52.1 23.4-52.1 52.1 0",
                " 28.7 23.4 52.1 52.1 52.1 28.7 0 52.1-23.4 52.1-52.1 0-28.7-23.4-52.1-",
                "52.1-52.1m30.8 90.8c0 0 0 0 0 0m1.9-2.6c-1.6.6-3.2.9-4.9.1-1.3-.6-2.1-",
                "1.8-2.3-3.2-.3-2.1.9-4.5 1.9-6.2 1.4-2.3 4.6-6.4 8.3-9.2 5.2-3.9 8.9-5",
                " 7.5-5.8-.9-.6-5.9 2.1-6.9 2.6-5.6 2.9-13.3 11.2-14.2 17.3-.7 4.4 4 7.",
                "7 7.7 7.2-12.1 9.9-25.4 10.3-29.9 10.4-4.4-.1-17.8-.4-29.9-10.4 3.7.5 ",
                "8.4-2.8 7.7-7.2-.9-6.2-8.6-14.5-14.1-17.3-1-.5-5.9-3.2-6.9-2.6-1.4.9 2",
                ".3 2 7.5 5.8 3.7 2.7 6.9 6.8 8.2 9.2 1.1 1.6 2.2 4.1 1.9 6.2-.2 1.4-1.",
                "1 2.7-2.3 3.2-1.6.7-3.3.5-4.9-.1-2.5-.9-4.5-2.8-6.2-5.2-3.3-4.4-6.5-9.",
                "5-8.1-14.9 1.4 3.1 10.2 16 14.1 16.2 4 .2-3.9-10.5-9.4-15-1.3-1.1-2.8-",
                "2.3-3.1-4.4-.2-1.5.5-2.4 2-2.5 2.2-.1 7.9 2.9 7.9 2.8-9.9-17.7 5.2-26.",
                "6-1-30.5-2.2-1.4-6.8 0-9.3.4 23.2-13.7 27.3 0 34.3 6.3 1.1 1 6 0 7-1.2",
                " 1.2-1.5 3.7-6.9.9-10.4-4-5-20.1-19.5-35.1-7.8 7.8-11.4 24.2-19.6 38.9",
                "-19.1 14.6-.4 31 7.8 38.8 19.1-15.1-11.7-31.1 2.8-35.1 7.8-2.8 3.5-.3 ",
                "8.9.9 10.4 1 1.2 5.9 2.2 7 1.2 7-6.3 11.1-20 34.3-6.3-2.5-.4-7.2-1.8-9",
                ".3-.4-6.3 3.9 8.8 12.8-1 30.5.1.1 5.8-2.8 7.9-2.8 1.4.1 2.2 1 2 2.5-.3",
                " 2-1.8 3.3-3.1 4.4-5.5 4.5-13.4 15.2-9.4 15 3.9-.2 12.8-13.1 14.1-16.2",
                "-1.6 5.4-4.9 10.5-8.1 14.9-1.8 2.4-3.8 4.3-6.3 5.2'></path>");
        htm.addln("  <path d='m80.6 59c-1-.6-2.5-.8-3.7-.4-1.9.5-2.5 2",
                ".3-3.6 3.8-1.1 1.6-2.9 2.8-4.4 3.4-.9 2.2.7 7.3 2.2 9.6.9 1.8 4.4 6 3.",
                "6 12.4 2.8-4.1 5-8.4 6-11 1.6-3.5 2.4-9.1 2.2-12.8-.1-1.7-.6-4-2.3-5m-",
                "1 6.6c-.9.5-2.8.3-3.5 1.6-1 1.7-1.3 2-1.7 1.8-1-4 3.5-9.2 5.3-7.6 1.3 ",
                "1.2 1.4 3.3-.1 4.2'></path>");
        htm.addln("  <path d='m68.2 92.7c-1.9.2-4 .6-5.6.6-1.7 0-3.8-.",
                "4-5.6-.6-.3 0-.5.2-.5.2-1.1 2.4 5.2 3.7 6.1 3.6.9 0 7.3-1.3 6.2-3.6 0 ",
                "0-.3-.2-.6-.2'></path>");
        htm.addln(" </g>");
        htm.addln("</g>");
        htm.addln("<g id='signaturewordMD' visibility='hidden' ",
                "fill='#fff' transform='translate(10,5) scale(.71)'>");
        htm.addln(" <use xmlns:xlink='http://www.w3.org/1999/xlink' ",
                "xlink:href='#signaturetop' transform='translate(43 -9)'></use>");
        htm.addln(" <use xmlns:xlink='http://www.w3.org/1999/xlink' ",
                "xlink:href='#signaturebottom' transform='translate(-469 48)'></use>");
        htm.addln("</g>");
        htm.addln("<g id='signaturewordLG' visibility='hidden' fill='#fff'>");
        htm.addln(" <use xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='#signaturetop'></use>");
        htm.addln(" <use xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='#signaturebottom'></use>");
        htm.addln("</g>");
        htm.addln("<g id='signaturecsu' visibility='hidden' fill='#fff'>");
        htm.addln(" <path d='m178.6 25.2h-22.2c-9.8 0-15.3 4.9-15.3 13",
                ".9v39.2c0 8.9 5.4 13.9 15.3 13.9h22.2c9.8 0 15.3-4.9 15.3-13.9v-10.5h-",
                "14.7v9.8c0 .9-.2 1.9-2.4 1.9h-18.7c-2 0-2.3-.9-2.3-1.9v-37.9c0-1 .3-1.",
                "9 2.3-1.9h18.7c2.2 0 2.4 1 2.4 1.9v9.8h14.7v-10.5c-.1-8.9-5.5-13.8-15.",
                "3-13.8'></path>");
        htm.addln(" <path d='m240.4 53.7l-18.4-3.7c-3.3-.7-3.5-1.3-3.5",
                "-3.1v-7.5c0-1 .2-2 2.4-2h17.1c1.9 0 2.3.8 2.3 2v7.7h14.5v-8.1c0-8.9-5.",
                "4-13.9-15.3-13.9h-20.5c-9.8 0-15.3 4.9-15.3 13.9v9.4c0 9.7 7.4 12.6 15",
                ".4 14.2l18.4 3.7c3.2.7 3.5 1.2 3.5 3v8.4c0 1.2-.4 2-2.3 2h-18.5c-1.9 0",
                "-2.3-.8-2.3-2v-8.4h-14.5v8.9c0 8.9 5.4 13.9 15.3 13.9h21.7c9.8 0 15.3-",
                "4.9 15.3-13.9v-10.3c.1-10.3-7.9-12.7-15.3-14.2'></path>");
        htm.addln(" <path d='m303.7 25.2v52.4c0 1-.3 1.9-2.3 1.9h-19.6",
                "c-2.2 0-2.3-1-2.3-1.9v-52.4h-14.7v53c0 8.9 5.4 13.9 15.3 13.9h23c9.8 0",
                " 15.3-4.9 15.3-13.9v-53h-14.7'></path>");
        htm.addln(" </g>");
        htm.addln("</svg>");
        htm.addln("</a>");
        htm.eH(1);
        htm.eDiv();
        htm.eDiv(); // screenLG

        htm.sDiv(null, "id='responsiveLogoSubsytem'");
        htm.sH(2).add("<a href='https://www.math.colostate.edu'>",
                "<span class='hidebelow400'>Department&nbsp;of </span>Mathematics</a>").eH(2);
        htm.eDiv();

        htm.eDiv(); // responsiveLogoContainer
        htm.addln("</section>");
        htm.eDiv(); // container

        if (maximizeUrl != null) {
            htm.sDiv(null, "style='position:absolute; top:64px; right:5px;'")
                    .add("<a style='font-size:24px; color:white;' href='", maximizeUrl, "'>\u21EB</a>").eDiv();
        }

        htm.eDiv(); // brandbar

        if (subtitle != null) {
            htm.sDiv("subtitlebar");
            htm.sDiv("container");
            if (subtitleLink != null) {
                htm.add("<a href='", subtitleLink, "'>");
            }
            htm.add(subtitle);
            if (subtitleLink != null) {
                htm.add("</a>");
            }
            htm.eDiv(); // container
            htm.eDiv(); // subtitlebar
        }

        htm.addln(" </header>");
    }

    /**
     * Emits the maximized header (CSU responsive brand logo and department title).
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param minimizeUrl to allow minimizing, the URL to which the minimize button should link; null to prevent
     *                    minimizing
     */
    private static void emitMaxHeader(final HtmlBuilder htm, final String minimizeUrl) {

        htm.addln("<header role='banner' id='page_banner'>");
        htm.sDiv("brandbarmax");

        if (minimizeUrl != null) {
            htm.sDiv(null, "style='position:absolute; top:0; right:5px;'")
                    .add("<a style='font-size:24px; color:white;' href='", minimizeUrl, "'>\u21E9</a>").eDiv();
        }

        htm.eDiv(); // brandbarmax

        htm.addln("</header>");
    }

    /**
     * Emits a thin bar that shows the logged-in user's name (and the effective name and user ID, if "acting as"), and
     * the current date. A future version will support controls (when the actual user is an administrator) to alter the
     * date.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the session
     */
    private static void emitUserDateBar(final HtmlBuilder htm, final ImmutableSessionInfo session) {

        if (session != null) {
            htm.sDiv("userdatebar");

            htm.sDiv("left");
            if (session.actAsUserId == null) {
                htm.add("Logged in as ", session.screenName);
            } else {
                htm.add("Acting as ", session.getEffectiveScreenName(), " (", session.getEffectiveUserId(), ")");
            }
            htm.eDiv(); // left

            htm.sDiv("right");
            htm.add("<strong>", TemporalUtils.FMT_WMDY.format(session.getNow().toLocalDate()), "</strong>");
            htm.eDiv(); // right

            htm.div("clear");

            htm.eDiv();
        }
    }

    /**
     * When the logged-in user is an administrator, this method can be called after emitting the header to emit a bar
     * with controls to allow the administrator to "act as" or "become" a particular student to see what they would see
     * if they logged into the site.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param session     the session
     * @param showDetails true to show details (right-justified in top bar)
     */
    private static void emitAdministratorBar(final HtmlBuilder htm, final ImmutableSessionInfo session,
                                             final boolean showDetails) {

        if (session != null) {
            htm.sDiv("rolebar");

            if (showDetails) {
                // Administrator can access "details"
                htm.sDiv("details");
                htm.add("<a href='admindetails.html'>", Res.get(Res.ROLE_DETAILS), "</a>");
                htm.eDiv();
            }

            htm.sDiv("roletab").add(Res.get(Res.ROLE_ADMIN)).eDiv();

            htm.sDiv("role");

            htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
            htm.addln(Res.get(Res.ROLE_ACT_AS));
            htm.addln(" <input class='small' type='text' size='8' name='act-as-stu-id' data-lpignore='true'>");
            htm.addln(" <input class='gobutton' type='submit' value='>'>");
            htm.addln(" <input style='display:none;' id='targetUrl1' name='target' value=''>");
            htm.addln("</form>");

            if (session.actAsUserId != null) {
                htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
                htm.addln(" <input type='hidden' name='act-as-stu-id' value='", session.userId, "'>");
                htm.addln(" <input class='gobutton' type='submit' value='", Res.get(Res.ROLE_STOP_ACTING), "'>");
                htm.addln(" <input style='display:none;' id='targetUrl2' name='target' value=''>");
                htm.addln("</form>");
            }

            htm.sDiv("becomeuser");

            htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
            htm.addln(Res.get(Res.ROLE_BECOME));
            htm.addln(" <input class='smallred' type='text' size='8' name='become-stu-id' data-lpignore='true'>");
            htm.addln(" <input class='gobutton' type='submit' value='>'>");
            htm.addln(" <input style='display:none;' id='targetUrl3' name='target' value=''>");
            htm.addln("</form>");

            htm.eDiv(); // becomeuser

            htm.eDiv(); // role

            htm.addln("<script>");
            htm.addln("function setUrls() {");
            htm.addln("  document.getElementById('targetUrl1').value = window.location.href;");
            if (session.actAsUserId != null) {
                htm.addln("  if (document.getElementById('targetUrl2')) {");
                htm.addln("    document.getElementById('targetUrl2').value = window.location.href;");
                htm.addln("  }");
            }
            htm.addln("  if (document.getElementById('targetUrl3')) {");
            htm.addln("   document.getElementById('targetUrl3').value = window.location.href;");
            htm.addln("  }");
            htm.addln("}");
            htm.addln("window.addEventListener(\"load\", setUrls, false);");
            htm.addln("</script>");

            htm.div("clear");

            htm.eDiv(); // rolebar
        }
    }

    /**
     * When the logged-in user is an adviser, this method can be called after emitting the header to emit a bar with
     * controls to allow the adviser to "act as" or "become" a particular student to see what they would see if they
     * logged into the site.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the session
     */
    private static void emitAdviserBar(final HtmlBuilder htm, final ImmutableSessionInfo session) {

        if (session != null) {
            htm.sDiv("rolebar");

            htm.sDiv("roletab").add(Res.get(Res.ROLE_ADVISER)).eDiv();

            htm.sDiv("role");

            htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
            htm.addln(Res.get(Res.ADVISER_ACT_AS));
            htm.addln(" <input class='small' type='text' size='8' name='act-as-stu-id' data-lpignore='true'>");
            htm.addln(" <input class='gobutton' type='submit' value='>'>");
            htm.addln(" <input style='display:none;' id='targetUrl1' name='target' value=''>");
            htm.addln("</form>");

            htm.addln("<script>");
            htm.addln(" document.getElementById('targetUrl1').value=\"/welcome/welcome.html\";");
            htm.addln("</script>");

            if (session.actAsUserId != null) {
                htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
                htm.addln(" <input type='hidden' name='act-as-stu-id' value='", session.userId, "'>");
                htm.addln(" <input class='gobutton' type='submit' value='", Res.get(Res.ROLE_STOP_ACTING), "'>");
                htm.addln(" <input style='display:none;' id='targetUrl2' name='target' value=''>");
                htm.addln("</form>");

                htm.addln("<script>");
                htm.addln(" document.getElementById('targetUrl2').value = window.location.href;");
                htm.addln("</script>");
            }

            if (session.role.canActAs(ERole.ADMINISTRATOR)) {
                htm.sDiv("becomeuser");

                htm.addln("<form class='roleform' action='rolecontrol.html' method='post'>");
                htm.addln(Res.get(Res.ROLE_BECOME));
                htm.addln(" <input class='smallred' type='text' size='8' name='become-stu-id' data-lpignore='true'>");
                htm.addln(" <input class='gobutton' type='submit' value='>'>");
                htm.addln(" <input style='display:none;' id='targetUrl3' name='target' value=''>");
                htm.addln("</form>");

                htm.addln("<script>");
                htm.addln(" document.getElementById('targetUrl3').value = window.location.href;");
                htm.addln("</script>");

                htm.eDiv(); // becomeuser
            }

            htm.eDiv(); // role

            htm.div("clear");

            htm.eDiv(); // rolebar
        }
    }

    /**
     * Emits the footer (Contact, OEO, Privacy, Disclaimer and Copyright).
     *
     * @param cache     the data cache
     * @param site      the owning site
     * @param secondary an optional secondary footer
     * @param htm       the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitFooter(final Cache cache, final AbstractSite site, final ISecondaryFooter secondary,
                                   final HtmlBuilder htm) throws SQLException {

        if (secondary == null) {
            htm.addln("<footer class='empty' id='page_footer'>");
        } else {
            htm.addln("<footer id='page_footer'>");
            secondary.emitSecondaryFooter(cache, site, htm);
        }

        htm.sDiv("bottom-footer");
        htm.sDiv("container");

        htm.sDiv("two-thirds", "id='footerText'");

        htm.addln("<ul>");
        htm.addln(" <li><a href='https://www.colostate.edu/contact/'>", Res.get(Res.FOOTER_CONTACT), "</a></li>");
        htm.addln(" <li><a href='https://www.colostate.edu/equal-opportunity'>", Res.get(Res.FOOTER_OEO), "</a></li>");
        htm.addln(" <li><a href='https://www.colostate.edu/privacy'>", Res.get(Res.FOOTER_PRIVACY), "</a></li>");
        htm.addln(" <li><a href='https://www.colostate.edu/disclaimer'>", Res.get(Res.FOOTER_DISCLAIMER), "</a></li>");
        htm.addln("</ul>");

        htm.sDiv("copyright", "title='Site last updated " + site.buildDatetime + "'")
                .add(Res.get(Res.FOOTER_COPYRIGHT)).eDiv();

        htm.eDiv(); // two-thirds

        htm.sDiv("one-third", "id='footerLogo'");
        htm.sDiv("bottomlogo");
        htm.addln("<a href='https://www.colostate.edu/'><img id='bottomlogo' ",
                "src='/www/images/csu-logo-oneline.svg' alt=''></a>");
        htm.eDiv(); // bottomlogo
        htm.eDiv(); // one-third

        htm.eDiv(); // container
        htm.eDiv(); // bottom-footer

        htm.addln("</footer>");
    }

    /**
     * Emits scripts called by elements on the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScripts(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln(" function resized() {");
        htm.addln("   w = document.documentElement.clientWidth;");

        // header breakpoints
        htm.addln("   logo = document.getElementById('responsiveLogo');");
        htm.addln("   logo2 = document.getElementById('bottomlogo');");
        htm.addln("   lg = document.getElementById('signaturewordLG');");
        htm.addln("   md = document.getElementById('signaturewordMD');");
        htm.addln("   sm = document.getElementById('signaturecsu');");

        htm.addln("   if (w >= 800) {");
        htm.addln("     logo.className='screenLG';");
        htm.addln("     lg.style.visibility='visible';");
        htm.addln("     md.style.visibility='hidden';");
        htm.addln("     sm.style.visibility='hidden';");
        htm.addln("     if (logo2) {");
        htm.addln("       if (w >= 1200) {");
        htm.addln("         logo2.style.width='350px';");
        htm.addln("         logo2.style.height='57.84px';");
        htm.addln("         logo2.style.marginTop='-5.5px';");
        htm.addln("       } else {");
        htm.addln("         logo2.style.width='283.33px';");
        htm.addln("         logo2.style.height='46.83px';");
        htm.addln("         logo2.style.marginTop='0';");
        htm.addln("       }");
        htm.addln("     }");
        htm.addln("   } else {");
        htm.addln("     lg.style.visibility='hidden';");
        htm.addln("     if (w >= 620) {");
        htm.addln("       logo.className='screenMD';");
        htm.addln("       md.style.visibility='visible';");
        htm.addln("       sm.style.visibility='hidden';");
        htm.addln("     } else {");
        htm.addln("       logo.className='screenSM';");
        htm.addln("       md.style.visibility='hidden';");
        htm.addln("       sm.style.visibility='visible';");
        htm.addln("     }");
        htm.addln("   }");

        // Footer breakpoints
        htm.addln("   ft = document.getElementById('footerText');");
        htm.addln("   fl = document.getElementById('footerLogo');");
        htm.addln("   if (w >= 900) {");
        htm.addln("     ft.className='two-thirds';");
        htm.addln("     fl.style.display='inline-block';");
        htm.addln("   } else {");
        htm.addln("     ft.className='three-thirds';");
        htm.addln("     fl.style.display='none';");
        htm.addln("   }");

        htm.addln(" }");
        htm.addln("</script>");
    }

    /**
     * Emits scripts called by elements on the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScriptsNoFooter(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln(" function resized() {");
        htm.addln("   w = document.documentElement.clientWidth;");

        // header breakpoints
        htm.addln("   logo = document.getElementById('responsiveLogo');");
        htm.addln("   logo2 = document.getElementById('bottomlogo');");
        htm.addln("   lg = document.getElementById('signaturewordLG');");
        htm.addln("   md = document.getElementById('signaturewordMD');");
        htm.addln("   sm = document.getElementById('signaturecsu');");

        htm.addln("   if (w >= 800) {");
        htm.addln("     logo.className='screenLG';");
        htm.addln("     lg.style.visibility='visible';");
        htm.addln("     md.style.visibility='hidden';");
        htm.addln("     sm.style.visibility='hidden';");
        htm.addln("     if (logo2) {");
        htm.addln("       if (w >= 1200) {");
        htm.addln("         logo2.style.width='350px';");
        htm.addln("         logo2.style.height='57.84px';");
        htm.addln("         logo2.style.marginTop='-5.5px';");
        htm.addln("       } else {");
        htm.addln("         logo2.style.width='283.33px';");
        htm.addln("         logo2.style.height='46.83px';");
        htm.addln("         logo2.style.marginTop='0';");
        htm.addln("       }");
        htm.addln("     }");
        htm.addln("   } else {");
        htm.addln("     lg.style.visibility='hidden';");
        htm.addln("     if (w >= 620) {");
        htm.addln("       logo.className='screenMD';");
        htm.addln("       md.style.visibility='visible';");
        htm.addln("       sm.style.visibility='hidden';");
        htm.addln("     } else {");
        htm.addln("       logo.className='screenSM';");
        htm.addln("       md.style.visibility='hidden';");
        htm.addln("       sm.style.visibility='visible';");
        htm.addln("     }");
        htm.addln("   }");
        htm.addln(" }");
        htm.addln("</script>");
    }

    /**
     * Appends the end of the HTML page (the closure of the two surrounding &lt;div&gt;s and the &lt;body&gt; and
     * &lt;html&gt; elements) to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    public static void endPage(final HtmlBuilder htm) {

        htm.addln("</html>");
    }

    /**
     * Writes the start of an ordinary page, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param showBars     bit flags indicating which bars to show
     * @param maximizeUrl  to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                     maximizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void startOrdinaryPage(final HtmlBuilder htm, final String title, final ImmutableSessionInfo session,
                                         final boolean allowIndex, final int showBars, final String maximizeUrl,
                                         final boolean showDetails, final boolean includeLayer) {

        startOrdinaryPage(htm, title, session, allowIndex, null, null, showBars, maximizeUrl, showDetails,
                includeLayer);
    }

    /**
     * Writes the start of an ordinary page, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param subtitle     an optional subtitle
     * @param subtitleLink the URL to which the subtitle should link
     * @param showBars     bit flags indicating which bars to show
     * @param maximizeUrl  to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                     maximizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void startOrdinaryPage(final HtmlBuilder htm, final String title, final ImmutableSessionInfo session,
                                         final boolean allowIndex, final String subtitle, final String subtitleLink,
                                         final int showBars, final String maximizeUrl, final boolean showDetails,
                                         final boolean includeLayer) {

        final boolean isAdmin = session != null && session.role.canActAs(ERole.ADMINISTRATOR);
        final boolean isAdviser = session != null && session.role.canActAs(ERole.ADVISER);

        startPage(htm, title, allowIndex, true);

        htm.addln("<body onload='resized();' onresize='resized();'>");
        htm.addln(" <a class='sr-only' href='#maincontent'>", Res.get(Res.SKIP_TO_MAIN), "</a>");

        emitHeader(htm, subtitle, subtitleLink, maximizeUrl);

        final boolean showAdmin = (showBars & ADMIN_BAR) == ADMIN_BAR;

        if (subtitle == null) {
            if (isAdmin && showAdmin) {
                emitAdministratorBar(htm, session, showDetails);
                htm.sDiv("page-wrapper-admin", "id='page_wrapper'");
            } else if (isAdviser && showAdmin) {
                emitAdviserBar(htm, session);
                htm.sDiv("page-wrapper-admin", "id='page_wrapper'");
            } else {
                htm.sDiv("page-wrapper", "id='page_wrapper'");
            }
        } else if (isAdmin && showAdmin) {
            emitAdministratorBar(htm, session, showDetails);
            htm.sDiv("page-wrapper-admin2", "id='page_wrapper'");
        } else if (isAdviser && showAdmin) {
            emitAdviserBar(htm, session);
            htm.sDiv("page-wrapper-admin2", "id='page_wrapper'");
        } else {
            htm.sDiv("page-wrapper2", "id='page_wrapper'");
        }

        final boolean showUser = (showBars & USER_DATE_BAR) == USER_DATE_BAR;

        if (showUser) {
            emitUserDateBar(htm, session);
        }

        if (includeLayer) {
            htm.sDiv(showUser || showAdmin ? "layeru" : "layer");
        }
        htm.sDiv(null, "id='maincontent'");
    }

    /**
     * Writes the start of an ordinary page with no footer, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param showBars     bit flags indicating which bars to show
     * @param maximizeUrl  to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                     maximizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void startNofooterPage(final HtmlBuilder htm, final String title, final ImmutableSessionInfo session,
                                         final boolean allowIndex, final int showBars, final String maximizeUrl,
                                         final boolean showDetails, final boolean includeLayer) {

        startNofooterPage(htm, title, session, allowIndex, null, null, showBars, maximizeUrl, showDetails,
                includeLayer);
    }

    /**
     * Writes the start of an ordinary page with no footer, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param subtitle     an optional subtitle
     * @param subtitleLink the URL to which the subtitle should link
     * @param showBars     bit flags indicating which bars to show
     * @param maximizeUrl  to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                     maximizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    private static void startNofooterPage(final HtmlBuilder htm, final String title, final ImmutableSessionInfo session,
                                          final boolean allowIndex, final String subtitle, final String subtitleLink,
                                          final int showBars, final String maximizeUrl, final boolean showDetails,
                                          final boolean includeLayer) {

        final boolean isAdmin = session != null && session.role.canActAs(ERole.ADMINISTRATOR);
        final boolean isAdviser = session != null && session.role.canActAs(ERole.ADVISER);

        startPageNoFooter(htm, title, allowIndex);

        htm.addln("<body onload='resized();' onresize='resized();'>");
        htm.addln(" <a class='sr-only' href='#maincontent'>", Res.get(Res.SKIP_TO_MAIN), "</a>");

        emitHeader(htm, subtitle, subtitleLink, maximizeUrl);

        final boolean showAdmin = (showBars & ADMIN_BAR) == ADMIN_BAR;

        if (subtitle == null) {
            if (isAdmin && showAdmin) {
                emitAdministratorBar(htm, session, showDetails);
                htm.sDiv("page-wrapper-admin", "id='page_wrapper'");
            } else if (isAdviser && showAdmin) {
                emitAdviserBar(htm, session);
                htm.sDiv("page-wrapper-admin", "id='page_wrapper'");
            } else {
                htm.sDiv("page-wrapper", "id='page_wrapper'");
            }
        } else if (isAdmin && showAdmin) {
            emitAdministratorBar(htm, session, showDetails);
            htm.sDiv("page-wrapper-admin2", "id='page_wrapper'");
        } else if (isAdviser && showAdmin) {
            emitAdviserBar(htm, session);
            htm.sDiv("page-wrapper-admin2", "id='page_wrapper'");
        } else {
            htm.sDiv("page-wrapper2", "id='page_wrapper'");
        }

        final boolean showUser = (showBars & USER_DATE_BAR) == USER_DATE_BAR;

        if (showUser) {
            emitUserDateBar(htm, session);
        }

        if (includeLayer) {
            htm.sDiv(showUser ? "layeru" : "layer");
        }
        htm.sDiv(null, "id='maincontent'");
    }

    /**
     * Writes the start of an ordinary maximized page, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param showBars     bit flags indicating which bars to show
     * @param minimizeUrl  to allow minimizing, the URL to which the minimize button should link; null to prevent
     *                     minimizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void startOrdinaryMaxPage(final HtmlBuilder htm, final String title,
                                            final ImmutableSessionInfo session, final boolean allowIndex,
                                            final int showBars, final String minimizeUrl, final boolean showDetails,
                                            final boolean includeLayer) {

        startOrdinaryMaxPage(htm, title, session, allowIndex, null, showBars, minimizeUrl, showDetails, includeLayer);
    }

    /**
     * Writes the start of an ordinary maximized page, including the opening of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param session      the user session; {@code null} if not logged in
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param subtitle     an optional subtitle
     * @param showBars     bit flags indicating which bars to show
     * @param minimizeUrl  to allow minimizing, the URL to which the minimize button should link; null to prevent
     *                     minimizing
     * @param showDetails  true to show details in the admin bar (if present)
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    private static void startOrdinaryMaxPage(final HtmlBuilder htm, final String title,
                                             final ImmutableSessionInfo session, final boolean allowIndex,
                                             final String subtitle,
                                             final int showBars, final String minimizeUrl, final boolean showDetails,
                                             final boolean includeLayer) {

        final boolean isAdmin = session != null && session.role.canActAs(ERole.ADMINISTRATOR);
        final boolean isAdviser = session != null && session.role.canActAs(ERole.ADVISER);

        startPage(htm, title, allowIndex, false);

        htm.addln("<body>");
        htm.addln(" <a class='sr-only' href='#maincontent'>",
                Res.get(Res.SKIP_TO_MAIN), "</a>");

        emitMaxHeader(htm, minimizeUrl);

        if (subtitle == null) {
            if (isAdmin && (showBars & ADMIN_BAR) == ADMIN_BAR) {
                emitAdministratorBar(htm, session, showDetails);
                htm.sDiv("page-wrapper-max-admin", "id='page_wrapper'");
            } else if (isAdviser && (showBars & ADMIN_BAR) == ADMIN_BAR) {
                emitAdviserBar(htm, session);
                htm.sDiv("page-wrapper-max-admin", "id='page_wrapper'");
            } else {
                htm.sDiv("page-wrapper-max", "id='page_wrapper'");
            }
        } else if (isAdmin && (showBars & ADMIN_BAR) == ADMIN_BAR) {
            emitAdministratorBar(htm, session, showDetails);
            htm.sDiv("page-wrapper-max-admin2", "id='page_wrapper'")
                    .addln();
        } else if (isAdviser && (showBars & ADMIN_BAR) == ADMIN_BAR) {
            emitAdviserBar(htm, session);
            htm.sDiv("page-wrapper-max-admin2", "id='page_wrapper'");
        } else {
            htm.sDiv("page-wrapper-max2", "id='page_wrapper'");
        }

        if ((showBars & USER_DATE_BAR) == USER_DATE_BAR) {
            emitUserDateBar(htm, session);
        }

        if (includeLayer) {
            htm.sDiv("layermax");
        }
        htm.sDiv(null, "id='maincontent'");
    }

    /**
     * Writes the end of an ordinary page, including the closing of the "maincontent" div.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param includeLayer true to include the "layer" div that snaps widths
     * @throws SQLException if there is an error accessing the database
     */
    public static void endOrdinaryPage(final Cache cache, final AbstractSite site, final HtmlBuilder htm,
                                       final boolean includeLayer) throws SQLException {

        endOrdinaryPage(cache, site, null, htm, includeLayer);
    }

    /**
     * Writes the end of an ordinary page, including the closing of the "maincontent" div.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param secondary    an optional secondary footer
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param includeLayer true to include the "layer" div that snaps widths
     * @throws SQLException if there is an error accessing the database
     */
    public static void endOrdinaryPage(final Cache cache, final AbstractSite site, final ISecondaryFooter secondary,
                                       final HtmlBuilder htm, final boolean includeLayer)
            throws SQLException {

        htm.eDiv(); // maincontent
        if (includeLayer) {
            htm.eDiv(); // layer
        }
        htm.eDiv(); // page-wrapper
        emitFooter(cache, site, secondary, htm);
        htm.addln("</body>");
        endPage(htm);
    }

    /**
     * Writes the end of an ordinary page with no footer, including the closing of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void endNoFooterPage(final HtmlBuilder htm, final boolean includeLayer) {

        htm.eDiv(); // maincontent
        if (includeLayer) {
            htm.eDiv(); // layer
        }
        htm.eDiv(); // page-wrapper
        htm.addln("</body>");
        endPage(htm);
    }

    /**
     * Writes the end of an ordinary maximized page, including the closing of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void endOrdinaryMaxPage(final HtmlBuilder htm, final boolean includeLayer) {

        htm.eDiv(); // maincontent
        if (includeLayer) {
            htm.eDiv(); // layer
        }
        htm.eDiv(); // page-wrapper
        htm.addln("</body>");
        endPage(htm);
    }

    /**
     * Writes the start of an empty page (with no headers or footers, but with a page wrapper that sizes just like the
     * content area of an ordinary page).
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the page title
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void startEmptyPage(final HtmlBuilder htm, final String title, final boolean includeLayer) {

        startPage(htm, title, false, false);

        htm.addln("<body>");
        htm.addln(" <a class='sr-only' href='#maincontent'>", Res.get(Res.SKIP_TO_MAIN), "</a>");

        htm.sDiv("page-wrapper-empty", "id='page_wrapper'");
        if (includeLayer) {
            htm.sDiv("layer");
        }
        htm.sDiv(null, "id='maincontent'");
    }

    /**
     * Writes the end of an empty page, including the closing of the "maincontent" div.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param includeLayer true to include the "layer" div that snaps widths
     */
    public static void endEmptyPage(final HtmlBuilder htm, final boolean includeLayer) {

        htm.eDiv(); // maincontent
        if (includeLayer) {
            htm.eDiv(); // layer
        }
        htm.eDiv(); // page-wrapper

        htm.addln("</body>");
        endPage(htm);
    }
}
