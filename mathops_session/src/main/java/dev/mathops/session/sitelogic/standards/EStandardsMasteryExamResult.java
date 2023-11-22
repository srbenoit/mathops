package dev.mathops.session.sitelogic.standards;

/**
 * Possible outcomes of an attempt to create a standards mastery assessment.
 */
public enum EStandardsMasteryExamResult {

    /** Success. */
    SUCCESS,

    /** No item groups need to be mastered. */
    NO_ITEM_GROUPS_NEED_MASTERY,

    /** A mastery group exists that has no items available. */
    MASTERY_GROUP_WITH_NO_ITEMS,

    /** An item ID was configured as a mastery item but does not exist. */
    NO_ITEM_WITH_ITEM_ID,
}
