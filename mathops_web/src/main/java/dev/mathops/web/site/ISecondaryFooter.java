package dev.mathops.web.site;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;

import java.sql.SQLException;

/**
 * A class that emits a secondary footer.
 */
@FunctionalInterface
public interface ISecondaryFooter {

    /**
     * Emits the HTML for a supplemental footer (within the "footer" element).
     *
     * @param systemData the system data object
     * @param site       the owning site
     * @param htm        the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    void emitSecondaryFooter(SystemData systemData, AbstractSite site, HtmlBuilder htm) throws SQLException;
}
