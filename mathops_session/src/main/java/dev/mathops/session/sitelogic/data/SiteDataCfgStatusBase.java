package dev.mathops.session.sitelogic.data;

/**
 * A base class for status configurations that store eligibility and a reason if not eligible.
 */
class SiteDataCfgStatusBase {

    /** True if student is currently eligible for the item. */
    public boolean eligible;

    /**
     * Constructs a new {@code AbstractSiteDataCfgStatus}.
     */
    SiteDataCfgStatusBase() {

        this.eligible = false;
    }
}
