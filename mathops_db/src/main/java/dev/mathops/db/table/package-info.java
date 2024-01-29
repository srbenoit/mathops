/**
 * Definitions of generalized system tables.  Table definitions are organized into the "primary" schema, which is
 * designed to support the math system, a "live" schema, which represents live University data (over which this system
 * has no control), and a "store" schema, which represents a University data store of data that is not necessarily live,
 * but is updated frequently, and which may be more efficient to access than the live data.
 *
 * <p>
 * A table definition should include factory methods to create records, query criteria objects for all implemented
 * queries, and updated value objects for implemented updates, all with well-typed argument lists.
 */
package dev.mathops.db.table;
