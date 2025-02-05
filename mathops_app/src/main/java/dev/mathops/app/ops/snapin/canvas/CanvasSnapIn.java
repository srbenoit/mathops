package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.db.Cache;
import dev.mathops.app.ops.snapin.AbstractSnapIn;
import dev.mathops.db.cfg.Facet;

import javax.swing.JFrame;

/**
 * A snap-in that allows management of Canvas courses associated with active-term courses.
 */
public final class CanvasSnapIn extends AbstractSnapIn {

    /** The thumbnail panel. */
    private final CanvasThumbnail thumbnail;

    /** The dashboard panel. */
    private final CanvasDashboard dashboardTile;

    /** The full-window panel. */
    private final CanvasFull full;

    /**
     * Constructs a new {@code CanvasSnapIn}.
     *
     * @param theSchema     the database schema
     * @param theLiveSchema the live database schema
     * @param theCache      the data cache
     * @param theFrame      the owning frame
     */
    public CanvasSnapIn(final Facet theSchema, final Facet theLiveSchema, final Cache theCache,
                        final JFrame theFrame) {

        super(theSchema, theLiveSchema, theCache);

        this.thumbnail = new CanvasThumbnail();
        this.dashboardTile = new CanvasDashboard();
        this.full = new CanvasFull(theFrame);
    }

    /**
     * Gets the snap-in title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return ("Canvas");
    }

    /**
     * Gets the thumbnail panel.
     *
     * @return the thumbnail panel
     */
    @Override
    public CanvasThumbnail getThumbnail() {

        return this.thumbnail;
    }

    /**
     * Gets the dashboard tile panel.
     *
     * @return the dashboard tile panel
     */
    @Override
    public CanvasDashboard getDashboardTile() {

        return this.dashboardTile;
    }

    /**
     * Gets the full-window panel.
     *
     * @return the full-window panel
     */
    @Override
    public CanvasFull getFull() {

        return this.full;
    }
}
