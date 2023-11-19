/**
 * Data services.  The data system is partitioned in to services, each of which manages some subset of the tables
 * needed.  The service includes the record classes that represent table data, implementation classes to query table
 * data from one or more database engines, and an API to support well-defined and testable operations on that data,
 * plus a suite of test cases.
 *
 * <p>
 * A data service could be deployed within a container to provide a service that could be updated independently of the
 * rest of the application, as long as the API changes in compatible ways.
 */
package dev.mathops.db.svc;
