package dev.mathops.db.table.primary;

import dev.mathops.db.generalized.EFieldRole;
import dev.mathops.db.generalized.EFieldType;
import dev.mathops.db.generalized.Field;
import dev.mathops.db.generalized.QueryCriteria;
import dev.mathops.db.generalized.Table;
import dev.mathops.db.generalized.constraint.IntegerRangeConstraint;
import dev.mathops.db.generalized.criteria.ENumericMatchType;
import dev.mathops.db.generalized.criteria.IntegerFieldCriterion;

/**
 * The "Term" table within the "Term and Course" domain.
 */
public final class TermTable extends Table {

    /** The single instance. */
    public static final TermTable INSTANCE = new TermTable();

    /**
     * Term IDs, which are six-digit integers of the form "202330" (Spring 2023), "202360" (Summer 2023), or
     * "202390" (Fall, 2023).
     */
    private static final Field F_TERM_ID = new Field("term_id", EFieldType.INTEGER, EFieldRole.PARTITION_KEY,
            new IntegerRangeConstraint(100000, 999999));

    /**
     * The first day of the term.  The start/end date ranges of terms should form a collection of non-overlapping
     * intervals that include all days; there should be no days that are not part of a term.
     */
    private static final Field F_START_DATE = new Field("start_date", EFieldType.LOCAL_DATE, EFieldRole.NOT_NULL);

    /** The last day of the term. */
    private static final Field F_END_DATE = new Field("end_date", EFieldType.LOCAL_DATE, EFieldRole.NOT_NULL);

    /**
     * The academic year, which is a four-digit integer of the form "2324", which would indicate the 2023/2024 academic
     * year, which includes Fall 2023, Spring 2024, and Summer 2024.
     */
    private static final Field F_ACADEMIC_YEAR = new Field("academic_year", EFieldType.INTEGER, EFieldRole.NOT_NULL,
            new IntegerRangeConstraint(1000, 9999));

    /**
     * The active index.  0 for the currently active term; +1 for the next term, +2 for the term after the +1 term,
     * -1 for the prior term, -2 for the term prior to the prior term, etc.  At each transition between terms, all
     * records are updated to increment their active index.
     */
    private static final Field F_ACTIVE_INDEX = new Field("active_index", EFieldType.LOCAL_DATE, EFieldRole.NOT_NULL);

    /** The last day students may drop the course. */
    private static final Field F_DROP_DEADLINE = new Field("d_deadline", EFieldType.LOCAL_DATE, EFieldRole.NOT_NULL);

    /** The last day students can withdraw from the course. */
    private static final Field F_WITHDRAW_DEADLINE = new Field("w_deadline", EFieldType.LOCAL_DATE,
            EFieldRole.NOT_NULL);

    /**
     * Constructs a new {@code TermTable}.
     */
    private TermTable() {

        super("main", "term", F_TERM_ID, F_START_DATE, F_END_DATE, F_ACADEMIC_YEAR, F_ACTIVE_INDEX,
                F_DROP_DEADLINE, F_WITHDRAW_DEADLINE);
    }

    /**
     * Creates a {@code QueryCriteria} object that matches on a specified term ID.
     *
     * @param theTermId the term ID for which to query
     * @return the query criteria
     */
    public QueryCriteria mkTermIdQuery(final int theTermId) {

        final Integer activeIndexObj = Integer.valueOf(theTermId);

        return new QueryCriteria(this, new IntegerFieldCriterion(F_TERM_ID, ENumericMatchType.EXACT_IN,
                activeIndexObj));
    }

    /**
     * Creates a {@code QueryCriteria} object that matches on a specified active index.
     *
     * @param theActiveIndex the active index for which to query
     * @return the query criteria
     */
    public QueryCriteria mkActiveIndexQuery(final int theActiveIndex) {

        final Integer activeIndexObj = Integer.valueOf(theActiveIndex);

        return new QueryCriteria(this, new IntegerFieldCriterion(F_ACTIVE_INDEX, ENumericMatchType.EXACT_IN,
                activeIndexObj));
    }
}
