package dev.mathops.web.site.help.student;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import jakarta.servlet.ServletRequest;

/**
 * A student's help context, which can include a course, unit, objective, homework session, past exam session, or
 * lecture ID.
 */
final class HelpContext {

    /** The course identifier ('117', '118', '124', '125', '126', 'ELM', 'PCT', 'MPR'). */
    final String course;

    /** The unit number (-1 if none, 0 for the Skills Review unit). */
    final int unit;

    /** The objective number (-1 if none). */
    final int obj;

    /**
     * Constructs a new {@code HelpContext} by parsing parameters from a servlet request.
     *
     * @param req the servlet request
     */
    HelpContext(final ServletRequest req) {

        final String cParam = req.getParameter("c");
        final String uParam = req.getParameter("u");
        final String oParam = req.getParameter("o");

        int theUnit = -1;
        int theObj = -1;

        if ("117".equals(cParam) || "118".equals(cParam) || "124".equals(cParam) || "125".equals(cParam)
                || "126".equals(cParam) || "ELM".equals(cParam) || "PCT".equals(cParam) || "MPR".equals(cParam)) {
            this.course = cParam;

            try {
                theUnit = uParam == null ? -1 : Integer.parseInt(uParam.trim());

                try {
                    theObj = oParam == null ? -1 : Integer.parseInt(oParam.trim());
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                }
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
            }
        } else {
            this.course = null;
        }

        this.unit = theUnit;
        this.obj = theObj;
    }

    /**
     * Generates a query string that encodes the help context.
     *
     * @return the query string
     */
    String makeQueryString() {

        final HtmlBuilder query = new HtmlBuilder(20);

        if (this.course != null) {
            query.add("?c=", this.course);
            if (this.unit != -1) {
                query.add("&u=", Integer.toString(this.unit));
                if (this.obj != -1) {
                    query.add("&o=", Integer.toString(this.obj));
                }
            }
        }

        return query.toString();
    }
}
