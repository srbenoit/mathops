package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.DbContext;
import dev.mathops.app.ops.snapin.AbstractSnapIn;

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
     * @param theContext     the database context
     * @param theLiveContext the live database context
     * @param theCache       the data cache
     * @param theFrame       the owning frame
     */
    public CanvasSnapIn(final DbContext theContext, final DbContext theLiveContext, final Cache theCache,
                        final JFrame theFrame) {

        super(theContext, theLiveContext, theCache);

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
