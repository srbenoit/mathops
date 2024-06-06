package dev.mathops.session.sitelogic.standards;

import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawItemLogic;
import dev.mathops.db.old.rawlogic.RawStdItemLogic;
import dev.mathops.db.old.rawrecord.RawItem;
import dev.mathops.db.old.rawrecord.RawStdItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * An assessment with a collection if items selected from item groups attached to standards that a student has not yet
 * mastered. On submission, for every item that is correct, the associated item group should be marked as mastered on
 * the student's "ststd" record for the corresponding standard.
 */
public final class StandardsMasteryExam {

    /** Map from unit number to a map from standard ID to a list of mastery group numbers. */
    private final Map<Integer, Map<String, List<Integer>>> unitStandards;

    /** Map from unit number to a map from standard ID to a list of mastery group numbers. */
    private final Map<Integer, Map<String, List<StandardsMasteryItem>>> unitItems;

    /**
     * Constructs a new {@code StandardsMasteryExam}.
     */
    StandardsMasteryExam() {

        this.unitStandards = new LinkedHashMap<>(4);
        this.unitItems = new LinkedHashMap<>(4);
    }

    /**
     * Adds an item group to the exam.
     *
     * @param unit  the unit
     * @param stdId the ID of the standard
     * @param group the group number
     */
    void addItemGroup(final Integer unit, final String stdId, final int group) {

        final Map<String, List<Integer>> inner = this.unitStandards.computeIfAbsent(unit, k -> new LinkedHashMap<>(8));

        final List<Integer> list = inner.computeIfAbsent(stdId, s -> new ArrayList<>(3));

        list.add(Integer.valueOf(group));
    }

    /**
     * Attempt to realize the exam.
     *
     * @param cache the data cache
     * @return the result
     */
    public EStandardsMasteryExamResult realize(final Cache cache) {

        final RandomGenerator rnd = new Random(System.currentTimeMillis());

        EStandardsMasteryExamResult result = EStandardsMasteryExamResult.SUCCESS;

        this.unitItems.clear();

        outer:
        for (final Map.Entry<Integer, Map<String, List<Integer>>> entry : this.unitStandards
                .entrySet()) {

            final Integer unit = entry.getKey();
            final Map<String, List<Integer>> standardsWithinUnit = entry.getValue();

            final Map<String, List<StandardsMasteryItem>> itemsMap = new LinkedHashMap<>(standardsWithinUnit.size());
            this.unitItems.put(unit, itemsMap);

            for (final Map.Entry<String, List<Integer>> stdEntry : standardsWithinUnit.entrySet()) {

                final String stdId = stdEntry.getKey();
                final List<Integer> itemGroups = stdEntry.getValue();

                final List<StandardsMasteryItem> itemList = new ArrayList<>(itemGroups.size());
                itemsMap.put(stdId, itemList);

                for (final Integer groupNum : itemGroups) {
                    final List<RawStdItem> stdItems = RawStdItemLogic.queryByMasteryGroup(cache, stdId, groupNum);
                    if (stdItems.isEmpty()) {
                        result = EStandardsMasteryExamResult.MASTERY_GROUP_WITH_NO_ITEMS;
                        break outer;
                    }

                    int index = 0;
                    if (stdItems.size() > 1) {
                        index = rnd.nextInt(stdItems.size());
                    }

                    final RawStdItem selectedItem = stdItems.get(index);

                    final RawItem item = RawItemLogic.query(cache, selectedItem.itemId);
                    if (item == null) {
                        result = EStandardsMasteryExamResult.NO_ITEM_WITH_ITEM_ID;
                        break outer;
                    }

                    final StandardsMasteryItem examItem = new StandardsMasteryItem(selectedItem);

                }
            }
        }

        return result;
    }
}
