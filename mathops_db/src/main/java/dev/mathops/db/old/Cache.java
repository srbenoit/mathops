package dev.mathops.db.old;

import dev.mathops.commons.log.Log;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.Locale;

/**
 * A container for cached data associated with a single database connection, which is typically a load of a web page or
 * the execution of a batch job or report.
 *
 * <p>
 * Data is often used more than once during construction of a response or execution of a job, and this class prevents
 * doing the same query multiple times (with possibly inconsistent results), while at the same time avoiding bulk
 * queries of all possible data without regard to the particular response being created.
 *
 * <p>
 * This object must be created and accessed by a single thread (typical for a servlet request for a web page) since it
 * has public data members and no synchronization.
 *
 * <p>
 * Rather than try to accommodate every possible query and its results, this class simply provides a generic map from a
 * {@code String} key to {@code RawRecordBase} to store single-record results, and a second map from {@code String} key
 * to {@code List<? extends RawRecordBase>} to store list results.
 *
 * <p>
 * It then becomes essential to uniquely name each possible query result (where the name must include the unique keys
 * used in a query).
 */
public final class Cache {

    /** The database profile that was used to create the cache. */
    public final DbProfile dbProfile;

    /** The database connection to use for all cached data queries. */
    public final DbConnection conn;

    /** The main schema name. */
    public final String mainSchemaName;

    /** The term schema name. */
    public final String termSchemaName;

    /** The single system data instance shared by all student data instances. */
    private final SystemData systemData;

    /**
     * Constructs a new {@code Cache}.
     *
     * @param theDbProfile the database profile that was used to create the cache (this can provide access to other
     *                     schema contexts than the PRIMARY context used here)
     * @param theConn      the database connection to use for all cached data queries
     * @throws SQLException if there is an error accessing the database
     */
    public Cache(final DbProfile theDbProfile, final DbConnection theConn) throws SQLException {

        if (theConn.dbContext.schema.use != ESchemaUse.PRIMARY) {
            throw new IllegalArgumentException("Cache must be used with primary schema connection");
        }

        this.dbProfile = theDbProfile;
        this.conn = theConn;

        // Attempt to determine the schema name for term-related queries

        final DbConfig db = theDbProfile.getDbContext(ESchemaUse.PRIMARY).loginConfig.db;

        final EDbProduct type = db.server.type;
        if (type == EDbProduct.INFORMIX) {
            this.mainSchemaName = "math";
            this.termSchemaName = "math";
        } else if (type == EDbProduct.POSTGRESQL) {
            if (db.use == EDbUse.PROD) {
                this.mainSchemaName = "main";
                final TermRec active = TermLogic.Postgres.INSTANCE.queryActive(this);
                if (active == null) {
                    throw new IllegalArgumentException("No active TermRec found");
                }
                this.termSchemaName = active.term.shortString.toLowerCase(Locale.ROOT);
                Log.info("Using the '", this.termSchemaName, "' schema for term data");
            } else if (db.use == EDbUse.DEV) {
                this.mainSchemaName = "main_d";
                this.termSchemaName = "term_d";
            } else {
                this.mainSchemaName = "main_t";
                this.termSchemaName = "term_t";
            }
        } else {
            throw new IllegalArgumentException("No TERM implementation for " + type + " database");
        }

        this.systemData = new SystemData(this);
    }

    /**
     * Gets the database profile that was used to create this cache.
     *
     * @return the database profile
     */
    public DbProfile getDbProfile() {

        return this.dbProfile;
    }

    /**
     * Gets the system data object.
     *
     * @return the system data object
     */
    public SystemData getSystemData() {

        return this.systemData;
    }
}
