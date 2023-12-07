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
 * In this context, a "table" is an immutable object defined by a unique name plus a map from a string field name to
 * field definition.  A field definition consists of the field's data type, type-specific constraints, the field's
 * nullability, and the field's role within records: primary key, sort key, or datum.
 *
 * <p>
 * A "record" is an immutable object that consists of a reference to a table plus a map from that table's field
 * definitions to typed field values.  Record objects can be serialized and restored losslessly in a human-readable
 * text format.
 *
 * <p>
 * There will also exist "query criteria" objects with the same structure as "record" objects, but that are only
 * populated with the field values on which to match, and which may contain operators like "greater than", "less than",
 * "between", etc.
 *
 * <p>
 * Finally, there will exist "updated values" objects, with the same structure as "record" objects to carry new values
 * for update operations.
 *
 * <p>
 * A particular implementation, specific to a database product and table structure, will have a single implementation
 * class that can perform all queries, inserts, deletes, and updates using these general objects.  The database
 * configuration file will identify the class name of this implementation class for each configured data store.
 */
package dev.mathops.db.generalized;
