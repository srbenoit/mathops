package dev.mathops.app.ops.snapin;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbContext;

/**
 * The base class for a snap-in.
 */
public abstract class AbstractSnapIn {

    /** The database context. */
    private final DbContext context;

    /** The live database context. */
    private final DbContext liveContext;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code AbstractSnapIn}.
     *
     * @param theContext     the database context
     * @param theLiveContext the live database context
     * @param theCache       the data cache
     */
    protected AbstractSnapIn(final DbContext theContext, final DbContext theLiveContext, final Cache theCache) {

        this.context = theContext;
        this.liveContext = theLiveContext;
        this.cache = theCache;
    }

    /**
     * Gets the database context.
     *
     * @return the context
     */
    public final DbContext getContext() {

        return this.context;
    }

    /**
     * Gets the live database context.
     *
     * @return the live context
     */
    public final DbContext getLiveContext() {

        return this.liveContext;
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
