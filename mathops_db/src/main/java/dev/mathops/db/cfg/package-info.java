/**
 * Database configuration management.
 *
 * <p>
 * Informix uses separate databases for PROD, DEV, and TEST, each with the same set of table names.  A separate set of
 * "archive" databases holds prior-term data.
 *
 * <p>
 * PostgreSQL uses a single database with multiple schemas, some of which support PROD, some DEV, and some TEST.
 *
 * <p>
 * Configuration objects to support either structure include:
 *
 * <dl>
 *     <dt>{@code SchemaConfig}[String id, ESchema schema]</dt>
 *     <dd>A schema (well-defined set of tables).</dd>
 *
 *     <dt>{@code ServerConfig}[String id, String host, int port, EDbProduct type]</dt>
 *     <dd>An installation of a database product on a server.</dd>
 *
 *     <dt>{@code DbConfig}[String name] (owned by {@code ServerConfig})</dt>
 *     <dd>A database that exists within the installation.</dd>
 *
 *     <dt>{@code DbSchemaConfig}[String schemaName, ESchema schema, EDbUse use] (owned by {@code DbConfig})</dt>
 *     <dd>Associates a schema and use with a schema name in a database (or with no schema name in cases where the
 *         database consists of a single schema).</dd>
 *
 *     <dt>{@code LoginConfig}[String id, String user, String password] (owned by {@code DbConfig})</dt>
 *     <dd>A login to a database.</dd>
 * </dl>
 *
 * <p>
 * Schemas that are defined include:
 * <ul>
 *     <li><b>Legacy</b> (The legacy schema from Informix - can support PROD, DEV, or TEST)</li>
 *     <li><b>MathOps</b> (The mathOps (system) schema - can support PROD, DEV, or TEST)</li>
 *     <li><b>Main</b> (The main schema - can support PROD, DEV, or TEST)</li>
 *     <li><b>Extern</b> (The external data schema - can support PROD, DEV, or TEST)</li>
 *     <li><b>Analytics</b> (The analytics schema - can support PROD, DEV, or TEST)</li>
 *     <li><b>Term</b> (The term schema - can support PROD, DEV, or TEST)</li>
 * </ul>
 */
package dev.mathops.db.cfg;
