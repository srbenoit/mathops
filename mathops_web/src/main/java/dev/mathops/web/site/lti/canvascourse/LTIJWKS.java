package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

/**
 * A dispatcher class that examines the payload of a validate launch callback to determine the page to generate, then
 * dispatches the request to the appropriate page generator.
 */
public enum LTIJWKS {
    ;

    /**
     * A handler for GET requests for the JSON Web Key Set for the LTI tool.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException if there is an error writing the response
     */
    public static void doGet(final Cache cache, final LtiSite site, final HttpServletRequest req,
                             final HttpServletResponse resp) throws IOException {

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String value = req.getParameter(name);
            Log.info("JWKS param '", name, "' = ", value);
        }

        // TODO: We will need the client ID to select the correct key set...  it is included int he request?
    }
}
