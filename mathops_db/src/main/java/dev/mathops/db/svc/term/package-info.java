/**
 * The "term" data service.
 *
 * <p>
 * This service manages the TERM, TERM_WEEK, PACE_TRACK_RULE, RULE_SET, and RULE_SET_RULE tables.
 *
 * <p>
 * This service can provide access to all terms, not just the current term.
 *
 * <p>
 * This is an "internal" service that does no authentication or authorization validation.  It is intended to be deployed
 * as part of a monolithic application, or within a container that is only accessible from trusted code, in such a way
 * that it does not expose its interface over a network.
 */
package dev.mathops.db.svc.term;
