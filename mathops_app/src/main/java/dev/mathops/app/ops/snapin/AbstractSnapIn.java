package dev.mathops.app.ops.snapin;

import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Facet;

/**
 * The base class for a snap-in.
 */
public abstract class AbstractSnapIn {

    /** The database schema. */
    private final Facet schema;

    /** The live database schema. */
    private final Facet liveSchema;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code AbstractSnapIn}.
     *
     * @param theSchema     the database schema
     * @param theLiveSchema the live database schema
     * @param theCache     the data cache
     */
    protected AbstractSnapIn(final Facet theSchema, final Facet theLiveSchema, final Cache theCache) {

        this.schema = theSchema;
        this.liveSchema = theLiveSchema;
        this.cache = theCache;
    }

    /**
     * Gets the database schema.
     *
     * @return the schema
     */
    public final Facet getSchema() {

        return this.schema;
    }

    /**
     * Gets the live database schema.
     *
     * @return the live schema
     */
    public final Facet getLiveSchema() {

        return this.liveSchema;
    }

    /**
     * Gets the data cache.
     *
     * @return the cache
     */
    public final Cache getCache() {

        return this.cache;
    }

    /**
     * Gets the snap-in title.
     *
     * @return the title
     */
    public abstract String getTitle();

    /**
     * Gets the thumbnail panel.
     *
     * @return the thumbnail panel
     */
    public abstract AbstractThumbnailButton getThumbnail();

    /**
     * Gets the dashboard tile panel.
     *
     * @return the dashboard tile panel
     */
    public abstract AbstractDashboardPanel getDashboardTile();

    /**
     * Gets the full-window panel.
     *
     * @return the full-window panel
     */
    public abstract AbstractFullPanel getFull();
}
