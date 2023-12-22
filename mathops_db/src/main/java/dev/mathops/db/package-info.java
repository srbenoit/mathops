/**
 * Provides management of the data store for persistent data.
 * <p>
 * For a given {@code Context}, the singleton {@code ContextMap} can generate an associated {@code DbContext} that
 * contains the schema and driver used for that context.
 *
 * <p>
 * {@code DbContext} objects can then be used as keys in maps that want to track data by database connection. All
 * contexts that use the same {@code DbContext} will see the same data, and can cache and share that data.
 */
package dev.mathops.db;
