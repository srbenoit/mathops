package dev.mathops.web.site.admin.genadmin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Pages to launch utility programs.
 */
enum PageUtilities {
    ;

    /**
     * Generates the page with a list of links to launch available utilities.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doUtilitiesPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.UTILITIES, htm);

        htm.addln("<h1>Utilities</h1>");

        htm.addln("<ul>");
        htm.addln("  <li><a href='problemtester.jnlp'>Problem Authoring Tool</a></li>");
        htm.addln("  <li><a href='examtester.jnlp'>Exam Testing Tool</a></li>");
        htm.addln("  <li><a href='examprinter.jnlp'>Exam Printing Tool</a></li>");
        htm.addln("  <li><a href='instructiontester.jnlp'>Instructional Materials Tester</a></li>");
        htm.addln("  <li><a href='glyphviewer.jnlp'>Bundled Font Glyph Viewer</a></li>");
        htm.addln("  <li><a href='keyconfig.jnlp'>Calculator Profile Builder</a></li>");
        htm.addln("  <li><a href='pwdhash.jnlp'>Password Hash Generator</a></li>");
        htm.addln("  <li><a href='renamedirs.jnlp'>Rename Exam Directories</a></li>");
        htm.addln("  <li><a href='jwabbit.jnlp'>JWabbit calculator</a><br/>");
        htm.addln("    ROMS: ");
        htm.addln("    <a href='/www/jars/TI-73.rom'>73</a>");
        htm.addln("  | <a href='/www/jars/TI-82.rom'>82</a>");
        htm.addln("  | <a href='/www/jars/TI-83Plus.rom'>83+</a>");
        htm.addln("  | <a href='/www/jars/TI-83PSE.rom'>83+SE</a>");
        htm.addln("  | <a href='/www/jars/TI-84P.rom'>84+</a>");
        htm.addln("  | <a href='/www/jars/TI-84PSE.rom'>84+SE</a>");
        htm.addln("  | <a href='/www/jars/TI-84PCSE.rom'>84+CSE</a>");
        htm.addln("  | <a href='/www/jars/TI-85.rom'>85</a>");
        htm.addln("  | <a href='/www/jars/TI-86.rom'>86</a></li>");
        htm.addln("</ul>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the XML authoring tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doXmlAuthor(final AdminSite site, final ServletRequest req,
                            final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>XML Authoring Tool</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Problem Authoring Tool</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <jfx:javafx-runtime version='2.2+' ",
                "href='http://javadl.sun.com/webapps/download/GetFile/javafx-latest/",
                "windows-i586/javafx2.jnlp'/>");
        htm.addln("    <j2se version='1.7+'/>");
        htm.addln("    <jar href='/www/jars/xmlauthor.jar'/>");
        htm.addln("    <jar href='/www/jars/jfxrt.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='com.javafx.main.NoJavaFXFallback' name='XmlAuthor'>");
        htm.addln("    <param name='requiredFXVersion' value='2.2+'/>");
        htm.addln("  </application-desc>");
        htm.addln("  <jfx:javafx-desc  width='0' height='0' ",
                "main-class='edu.colostate.math.doc.author.XmlAuthor' name='XmlAuthor'/>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the problem authoring tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doProblemTester(final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Problem Authoring Tool</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Problem Authoring Tool</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.instruction.problem.ui.ProblemTester'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis() - (long) (86400 * 1000 * 7));
        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the exam testing tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doExamTester(final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Exam Testing Tool</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Exam Testing Tool</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.localtesting.LocalTestingApp'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis() - (long) (86400 * 1000 * 7));
        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the exam printing tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doExamPrinter(final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Exam Printing Tool</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Exam Printing Tool</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.examprinter.ExamPrinterApp'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the instructional materials testing tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doInstructionTester(final AdminSite site, final ServletRequest req,
                                    final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Instructional Materials Testing Tool</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Instructional Materials Testing Tool</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.instruction.problem.",
                "ui.InstructionTester'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the bundled font glyph viewer tool.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doGlyphViewer(final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Bundled Font Glyph Viewer</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Bundled Font Glyph Viewer</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc "
                + "main-class='edu.colostate.math.font.GlyphViewer'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the calculator profile builder.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doKeyConfig(final AdminSite site, final ServletRequest req,
                            final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Calculator Key Configuration</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Calculator Key Configuration</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.txn.document.KeyConfigApp'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the password hash generator.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doPasswordHash(final AdminSite site, final ServletRequest req,
                               final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Calculator Key Configuration</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Calculator Key Configuration</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.passwordhash.PasswordHash'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the calculator.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doRenameDirs(final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Rename Directories</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Renames Exam and Homework Directories</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.txn.object.RenameDirs'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the JNLP file to launch the calculator.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doJWabbit(final AdminSite site, final ServletRequest req,
                          final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(500);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");

        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, site.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>JWabbit Emulator</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>The JWabbit TI calculator emulator</description>");
        htm.addln("  </information>");

        htm.addln("  <security>");
        htm.addln("    <all-permissions/>");
        htm.addln("  </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.7+'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='jwabbit.Launcher'>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        AbstractSite.sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
