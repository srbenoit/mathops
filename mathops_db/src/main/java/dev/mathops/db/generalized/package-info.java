/**
 * Classes that support generalized "table" and "record" constructions, with insert, query, update, and delete
 * capability.
 *
 * <p>
 * The goal of this package is to abstract away the idea of a JDBC connection that is specific to RDBMS. We want a
 * more general "connection" interface that could encapsulate a JDBC connection for database types that use JDBC,
 * or a system-specific connection object for others, with methods to perform needed operations using generalized
 * record objects.
 *
 * <p>
 * In this context, a "table" is an immutable object defined within a named "schema" by a unique table name plus a list
 * of field definitions.  A field definition consists of the field's data type, type-specific constraints, the field's
 * nullability, and the field's role within records: primary key, sort key, or datum.  Schemas are logical partitioning
 * of sets of tables; for databases that support schemas, they can be used directly, and for those that do not, the
 * tuple of schema name and table name can be used as a unique name under which to store table data.
 *
 * <p>
 * A "record" is an immutable object that consists of a reference to a table plus a map from that table's field
 * definitions to typed field values.  Record objects can be serialized and restored losslessly in a human-readable
 * text format.
 *
 * <p>
 * A "query criteria" object specifies a list of field constraints that can be used to generate a "WHERE" clause of an
 * SQL statement, or the equivalent for non-SQL databases.
 *
 * <p>
 * Finally, an "updated values" object carries new values for update operations.
 *
 * <p>
 * A particular implementation, specific to a database product and table structure, will have a single implementation
 * class that can perform all queries, inserts, deletes, and updates using these general objects.  The database
 * configuration file will identify the class name of this implementation class for each configured data store.
 */
package dev.mathops.db.generalized;
