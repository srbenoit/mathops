package dev.mathops.db.reclogic.iface;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.rec.RecBase;
import dev.mathops.db.reclogic.query.DateCriteria;
import dev.mathops.db.reclogic.query.DateTimeCriteria;
import dev.mathops.db.reclogic.query.IntegerCriteria;
import dev.mathops.db.reclogic.query.StringCriteria;

/**
 * An interface implemented by record logic implementations that access PostgreSQL.
 *
 * @param <T> the record type
 */
public interface IPostgresRecLogic<T extends RecBase> extends IRecLogic<T> {

    /**
     * Appends a query match clause for a string value. If the string value starts with "LIKE(" and ends with ")", a
     * "LIKE" operation is performed on the string with these removed.
     *
     * @param stmt      the {@code HtmlBuilder} to which to append
     * @param where     the " WHERE " or " AND " prefix to emit before the clause (if any)
     * @param fieldName the field name
     * @param criteria  the query criteria (null if none)
     * @return the new prefix ({@code where} if no clause was emitted, " AND " if a clause was emitted)
     */
    default String stringWhere(final HtmlBuilder stmt, final String where,
                               final String fieldName, final StringCriteria criteria) {

        String newWhere = where;

        if (criteria != null) {
            switch (criteria.comparison) {

                case EQUAL_IGNORE_CASE:
                    stmt.add(where, "LOWER(", fieldName, ")=LOWER(", sqlStringValue(criteria.value), ")");
                    break;

                case LIKE:
                    stmt.add(where, fieldName, "LIKE(", sqlStringValue(criteria.value),
                            ")");
                    break;

                case LIKE_IGNORE_CASE:
                    stmt.add(where, "LOWER(", fieldName, ") LIKE(LOWER(", sqlStringValue(criteria.value), "))");
                    break;

                case EQUAL:
                default:
                    stmt.add(where, fieldName, "=", sqlStringValue(criteria.value));
                    break;
            }

            newWhere = AND;
        }

        return newWhere;
    }

    /**
     * Appends a query match clause for an integer value.
     *
     * @param stmt      the {@code HtmlBuilder} to which to append
     * @param where     the " WHERE " or " AND " prefix to emit before the clause (if any)
     * @param fieldName the field name
     * @param criteria  the query criteria (null if none)
     * @return the new prefix ({@code where} if no clause was emitted, " AND " if a clause was emitted)
     */
    default String integerWhere(final HtmlBuilder stmt, final String where,
                                final String fieldName, final IntegerCriteria criteria) {

        String newWhere = where;

        if (criteria != null) {
            switch (criteria.comparison) {

                case UNEQUAL:
                    stmt.add(where, fieldName, "!=", sqlIntegerValue(criteria.value));
                    break;

                case GREATER_THAN:
                    stmt.add(where, fieldName, ">", sqlIntegerValue(criteria.value));
                    break;

                case GREATER_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, ">=", sqlIntegerValue(criteria.value));
                    break;

                case LESS_THAN:
                    stmt.add(where, fieldName, "<", sqlIntegerValue(criteria.value));
                    break;

                case LESS_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, "<=", sqlIntegerValue(criteria.value));
                    break;

                case EQUAL:
                default:
                    stmt.add(where, fieldName, "=", sqlIntegerValue(criteria.value));
                    break;
            }

            newWhere = AND;
        }

        return newWhere;
    }

    /**
     * Appends a query match clause for a date value.
     *
     * @param stmt      the {@code HtmlBuilder} to which to append
     * @param where     the " WHERE " or " AND " prefix to emit before the clause (if any)
     * @param fieldName the field name
     * @param criteria  the query criteria (null if none)
     * @return the new prefix ({@code where} if no clause was emitted, " AND " if a clause was emitted)
     */
    default String dateWhere(final HtmlBuilder stmt, final String where, final String fieldName,
                             final DateCriteria criteria) {

        String newWhere = where;

        if (criteria != null) {
            switch (criteria.comparison) {

                case UNEQUAL:
                    stmt.add(where, fieldName, "!=", sqlDateValue(criteria.value));
                    break;

                case GREATER_THAN:
                    stmt.add(where, fieldName, ">", sqlDateValue(criteria.value));
                    break;

                case GREATER_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, ">=", sqlDateValue(criteria.value));
                    break;

                case LESS_THAN:
                    stmt.add(where, fieldName, "<", sqlDateValue(criteria.value));
                    break;

                case LESS_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, "<=", sqlDateValue(criteria.value));
                    break;

                case EQUAL:
                default:
                    stmt.add(where, fieldName, "=", sqlDateValue(criteria.value));
                    break;
            }

            newWhere = AND;
        }

        return newWhere;
    }

    /**
     * Appends a query match clause for a date value.
     *
     * @param stmt      the {@code HtmlBuilder} to which to append
     * @param where     the " WHERE " or " AND " prefix to emit before the clause (if any)
     * @param fieldName the field name
     * @param criteria  the query criteria (null if none)
     * @return the new prefix ({@code where} if no clause was emitted, " AND " if a clause was emitted)
     */
    default String dateWhere(final HtmlBuilder stmt, final String where, final String fieldName,
                             final DateTimeCriteria criteria) {

        String newWhere = where;

        if (criteria != null) {
            switch (criteria.comparison) {
                case UNEQUAL:
                    stmt.add(where, fieldName, "!=", sqlDateValue(criteria.value.toLocalDate()));
                    break;

                case GREATER_THAN:
                    stmt.add(where, fieldName, ">", sqlDateValue(criteria.value.toLocalDate()));
                    break;

                case GREATER_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, ">=", sqlDateValue(criteria.value.toLocalDate()));
                    break;

                case LESS_THAN:
                    stmt.add(where, fieldName, "<", sqlDateValue(criteria.value.toLocalDate()));
                    break;

                case LESS_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, "<=", sqlDateValue(criteria.value.toLocalDate()));
                    break;

                case EQUAL:
                default:
                    stmt.add(where, fieldName, "=", sqlDateValue(criteria.value.toLocalDate()));
                    break;
            }

            newWhere = AND;
        }

        return newWhere;
    }

    /**
     * Appends a query match clause for a date/time value.
     *
     * @param stmt      the {@code HtmlBuilder} to which to append
     * @param where     the " WHERE " or " AND " prefix to emit before the clause (if any)
     * @param fieldName the field name
     * @param criteria  the query criteria (null if none)
     * @return the new prefix ({@code where} if no clause was emitted, " AND " if a clause was emitted)
     */
    default String dateTimeWhere(final HtmlBuilder stmt, final String where,
                                 final String fieldName, final DateTimeCriteria criteria) {

        String newWhere = where;

        if (criteria != null) {
            switch (criteria.comparison) {

                case UNEQUAL:
                    stmt.add(where, fieldName, "!=", sqlDateTimeValue(criteria.value));
                    break;

                case GREATER_THAN:
                    stmt.add(where, fieldName, ">", sqlDateTimeValue(criteria.value));
                    break;

                case GREATER_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, ">=", sqlDateTimeValue(criteria.value));
                    break;

                case LESS_THAN:
                    stmt.add(where, fieldName, "<", sqlDateTimeValue(criteria.value));
                    break;

                case LESS_THAN_OR_EQUAL:
                    stmt.add(where, fieldName, "<=", sqlDateTimeValue(criteria.value));
                    break;

                case EQUAL:
                default:
                    stmt.add(where, fieldName, "=", sqlDateTimeValue(criteria.value));
                    break;
            }

            newWhere = AND;
        }

        return newWhere;
    }
}
