package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Server;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A "ribbon" that shows the set of all defined servers and databases and allows the user to select (using checkboxes)
 * which databases are "active" for administration.
 */
final class DatabaseRibbon extends JPanel {

    /** The list of tiles in the ribbon. */
    private final List<DatabaseRibbonTile> tiles;

    /**
     * Constructs a new {@code DatabaseRibbon}.
     *
     * @param config      the database configuration
     * @param listener    the listener to notify when the set of selected databases changes
     * @param connections a map from database to its connection
     */
    DatabaseRibbon(final DatabaseConfig config, final ActionListener listener,
                   final Map<Database, DbConnection> connections) {

        super(new StackedBorderLayout());

        final Color background = getBackground();
        final boolean isLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, isLight);

        final Border outline = BorderFactory.createLineBorder(accent);
        setBorder(outline);

        final List<Server> servers = config.getServers();
        final int numServers = servers.size();
        this.tiles = new ArrayList<>(numServers);

        for (final Server server : servers) {
            final DatabaseRibbonTile tile = new DatabaseRibbonTile(server, listener, connections, accent);
            add(tile, StackedBorderLayout.WEST);
            this.tiles.add(tile);
        }
    }

    /**
     * Gets the list of selected database uses.
     *
     * @param databaseUses a list to populate with the selected database uses
     */
    void getSelectedDatabaseUses(final Collection<? super DatabaseUse> databaseUses) {

        databaseUses.clear();
        for (final DatabaseRibbonTile tile : this.tiles) {
            tile.getSelectedDatabaseUses(databaseUses);
        }
    }
}
