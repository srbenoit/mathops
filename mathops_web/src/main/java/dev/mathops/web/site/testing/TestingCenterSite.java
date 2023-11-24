package dev.mathops.web.site.testing;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The site that handles testing center applications.
 */
public final class TestingCenterSite extends AbstractSite {

    /**
     * Constructs a new {@code TestingCenterSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public TestingCenterSite(final WebSiteProfile theSiteProfile,
                             final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being in
     */
    @Override
    public void init(final ServletConfig config) {

        // No action
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        if ("checkin.jnlp".equals(subpath)) {
            doCheckinJnlp(req, resp);
        } else if ("checkin3.jnlp".equals(subpath)) {
            doCheckin3Jnlp(req, resp);
        } else if ("checkout.jnlp".equals(subpath)) {
            doCheckoutJnlp(req, resp);
        } else if ("testing-station.jnlp".equals(subpath)) {
            doTestingStation(req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else {
            Log.warning(req.getRequestURI(), " (", subpath,
                    ") not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        Log.warning(req.getRequestURI(), " (", subpath,
                ") not found");
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Generates the Java Web Start launch descriptor to start the checkin app.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void doCheckinJnlp(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(1000);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");
        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, this.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Checkin Application</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Proctored Testing Center Checkin Station.</description>");
        htm.addln("  </information>");

        htm.addln(" <security>");
        htm.addln(" <all-permissions/>");
        htm.addln(" </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/ifxjdbc.jar'/>");
        htm.addln("    <jar href='/www/jars/postgresql-42.6.0.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.checkin.CheckinApp'>");
        htm.addln("    <argument>1</argument>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the Java Web Start launch descriptor to start the checkout app for testing center 3.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void doCheckin3Jnlp(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(1000);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");
        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, this.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Checkin Application</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Proctored Testing Center Checkin Station.</description>");
        htm.addln("  </information>");

        htm.addln(" <security>");
        htm.addln(" <all-permissions/>");
        htm.addln(" </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/ifxjdbc.jar'/>");
        htm.addln("    <jar href='/www/jars/postgresql-42.6.0.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.checkin.CheckinApp'>");
        htm.addln("    <argument>3</argument>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the Java Web Start launch descriptor to start the checkout app.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void doCheckoutJnlp(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(1000);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");
        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, this.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Checkout Application</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Proctored Testing Center Checkout Station.</description>");
        htm.addln("  </information>");

        htm.addln(" <security>");
        htm.addln(" <all-permissions/>");
        htm.addln(" </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/ifxjdbc.jar'/>");
        htm.addln("    <jar href='/www/jars/postgresql-42.6.0.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.checkout.CheckoutApp'>");
        htm.addln("    <argument>1</argument>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the Java Web Start launch descriptor to start the testing station app.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void doTestingStation(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(1000);
        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final String port = Integer.toString(req.getServerPort());

        htm.addln("<?xml version='1.0' encoding='utf-8'?>");
        htm.addln("<jnlp spec='6.0+' codebase='", scheme, "://",
                host, CoreConstants.COLON, port, this.siteProfile.path, "'>");

        htm.addln("  <information>");
        htm.addln("    <title>Testing Station</title>");
        htm.addln("    <vendor>Colorado State University</vendor>");
        htm.addln("    <homepage href='index.html'/>");
        htm.addln("    <description>Proctored Testing Center Station.</description>");
        htm.addln("  </information>");

        htm.addln(" <security>");
        htm.addln(" <all-permissions/>");
        htm.addln(" </security>");

        htm.addln("  <update check='always' policy='always'/>");

        htm.addln("  <resources>");
        htm.addln("    <j2se version='1.8+'/>");
        htm.addln("    <jar href='/www/jars/bls8.jar'/>");
        htm.addln("    <jar href='/www/jars/minfonts3.jar'/>");
        htm.addln("    <jar href='/www/jars/jwabbit.jar'/>");
        htm.addln("    <property name='jnlp.packEnabled' value='true'/>");
        htm.addln("  </resources>");

        htm.addln("  <application-desc ",
                "main-class='edu.colostate.math.app.teststation.TestStationApp'>");
        htm.addln("    <argument>1</argument>");
        htm.addln("  </application-desc>");
        htm.addln("</jnlp>");

        resp.setDateHeader("Expires", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());

        sendReply(req, resp, "application/x-java-jnlp-file",
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
