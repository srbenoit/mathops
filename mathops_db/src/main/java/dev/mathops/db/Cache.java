package dev.mathops.db;

import dev.mathops.core.log.Log;
import dev.mathops.db.cfg.DbConfig;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.EDbInstallationType;
import dev.mathops.db.cfg.EDbUse;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rec.RecBase;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /** The anlyt schema name. */
    private final String anlytSchemaName;

    /** Cached single records. */
    private final Map<String, RecBase> singleRecords;

    /** Cached lists of records. */
    private final Map<String, List<? extends RecBase>> listsOfRecords;

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

        this.singleRecords = new HashMap<>(10);
        this.listsOfRecords = new HashMap<>(10);

        // Attempt to determine the schema name for term-related queries

        final DbConfig db = theDbProfile.getDbContext(ESchemaUse.PRIMARY).loginConfig.db;

        final EDbInstallationType type = db.server.type;
        if (type == EDbInstallationType.INFORMIX) {
            this.mainSchemaName = "math";
            this.termSchemaName = "math";
            this.anlytSchemaName = "math";
        } else if (type == EDbInstallationType.POSTGRESQL) {
            if (db.use == EDbUse.PROD) {
                this.mainSchemaName = "main";
                this.anlytSchemaName = "anlyt";
                final TermRec active = TermLogic.Postgres.INSTANCE.queryActive(this);
                if (active == null) {
                    throw new IllegalArgumentException("No active TermRec found");
                }
                this.termSchemaName = active.term.shortString.toLowerCase(Locale.ROOT);
                Log.info("Using the '", this.termSchemaName, "' schema for term data");
            } else if (db.use == EDbUse.DEV) {
                this.mainSchemaName = "main_d";
                this.termSchemaName = "term_d";
                this.anlytSchemaName = "anlyt";
            } else {
                this.mainSchemaName = "main_t";
                this.termSchemaName = "term_t";
                this.anlytSchemaName = "anlyt_t";
            }
        } else {
            throw new IllegalArgumentException("No TERM implementation for " + type + " database");
        }
    }

    /**
     * Gets the database profile that was used to create this cache.
     *
     * @return the database profile
     */
    public DbProfile getDbProfile() {

        return this.dbProfile;
    }

//    /**
//     * Gets the database use for this cache.
//     *
//     * @return the database use (PROD, DEV, TEST, etc.)
//     */
//    public EDbUse getDbUse() {
//
//        return this.dbProfile.getDbContext(ESchemaUse.PRIMARY).loginConfig.db.use;
//    }

    /**
     * Stores a record in the cache.
     *
     * @param key    the key
     * @param record the record
     */
    public void storeRecord(final String key, final RecBase record) {

        this.singleRecords.put(key, record);
    }

    /**
     * Gets a cached single record as a specified record type.
     *
     * @param <T> the record type
     * @param key the key
     * @param cls the record type's class
     * @return the record; null if none found (or if the record found was not of the expected class)
     */
    @SuppressWarnings("unchecked")
    public <T extends RecBase> T getRecord(final String key, final Class<T> cls) {

        final RecBase value = this.singleRecords.get(key);
        final T result;

        if (value == null) {
            result = null;
        } else if (value.getClass().equals(cls)) {
            result = (T) value;
        } else {
            Log.warning("Attempt to query for record of class ", cls.getSimpleName(),
                    " but cache had record of class ", value.getClass().getSimpleName());
            result = null;
        }

        return result;
    }

//    /**
//     * Removes a record from the cache.
//     *
//     * @param key the key of the record to remove
//     */
//     public void removeRecord(final String key) {
//
//     this.singleRecords.remove(key);
//     }

    /**
     * Clones a list of records, and then stores the clone in the cache.
     *
     * @param key  the key
     * @param list the list of records (this list can be changed without affecting the cache)
     */
    public void cloneAndStoreList(final String key, final List<? extends RecBase> list) {

        this.listsOfRecords.put(key, new ArrayList<>(list));
    }

    /**
     * Gets a cached list of records as a list of records of a specified record type.
     *
     * @param <T> the record type
     * @param key the key
     * @param cls the record type's class
     * @return a clone of the cached list of records if found; null if none found (or if the list found contained a
     *         record that was not of the expected class)
     */
    @SuppressWarnings("unchecked")
    public <T extends RecBase> List<T> getList(final String key, final Class<T> cls) {

        final List<? extends RecBase> list = this.listsOfRecords.get(key);
        final List<T> result;

        if (list == null) {
            result = null;
        } else if (list.isEmpty()) {
            result = new ArrayList<>(0);
        } else if (list.get(0).getClass().equals(cls)) {
            result = new ArrayList<>((Collection<? extends T>) list);
        } else {
            Log.warning("Attempt to query for list of records of class ", cls.getSimpleName(),
                    " but cache had list with record of class ", list.get(0).getClass().getSimpleName());
            result = null;
        }

        return result;
    }

//    /**
//     * Removes a list from the cache.
//     *
//     * @param key the key of the list to remove
//     */
//    void removeList(final String key) {
//
//        this.listsOfRecords.remove(key);
//    }
}
