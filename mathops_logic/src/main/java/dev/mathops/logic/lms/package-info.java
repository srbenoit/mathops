/**
 * Business logic related to Learning Management System (LMS) integration.
 *
 * <p>
 * The logic objects in this module store information about LMS configuration and data retrieved from the LMS in a set
 * of tables in the local database.  They also read data from other database tables, but do not update them.
 *
 * <p>Tables owned by (and written by) the LMS logic module:
 * <ul>
 *     <li><b>lms_hosts</b> - one record for every LMS host, with its type and configuration information.</li>
 *     <li><b>lms_users</b> - One record for every LMS user with credentials to support accessing that user's
 *     information within the LMS</li>
 * </ul>
 */
package dev.mathops.logic.lms;
