package dev.mathops.web.site;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;

import java.sql.SQLException;

/**
 * A class that emits a secondary footer.
 */
@FunctionalInterface
public interface ISecondaryFooter {

    /**
     * Emits the HTML for a supplemental footer (within the "footer" element).
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    void emitSecondaryFooter(Cache cache, AbstractSite site, HtmlBuilder htm) throws SQLException;
}
