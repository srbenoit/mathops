package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.schema.legacy.RawResource;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present recent resource activity.
 */
final class JTableResourceActivity extends AbstractAdmTable<ResourceActivityRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2306531843733300270L;

    /**
     * Constructs a new {@code JTableResourceActivity}.
     */
    JTableResourceActivity() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(6);

        result.add(new LocalTableColumn("Date/Time", 200));
        result.add(new LocalTableColumn("Activity", 100));
        result.add(new LocalTableColumn("Student ID", 100));
        result.add(new LocalTableColumn("Resource ID", 100));
        result.add(new LocalTableColumn("Resource type", 180));

        return result;
    }

    /**
     * Installs data in the table.
     *
     * @param data    the data to install
     * @param minRows the minimum number of rows to show in the table
     * @return the number of records in the table after the insert
     */
    @Override
    protected int installData(final List<? extends ResourceActivityRow> data, final int minRows) {

        final String[] row = new String[5];

        for (final ResourceActivityRow record : data) {

            row[0] = record.activityDateTime == null ? CoreConstants.EMPTY
                    : TemporalUtils.FMT_MDY_AT_HM_A.format(record.activityDateTime);
            row[1] = valueToString(record.activityType);
            row[2] = valueToString(record.studentId);
            row[3] = valueToString(record.resourceId);
            row[4] = typeToString(record.resourceType);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }

    /**
     * Generates a string representation of a resource type.
     *
     * @param type the resource type
     * @return the string representation
     */
    private static String typeToString(final String type) {

        return switch (type) {
            case RawResource.TYPE_INHOUSE_CALC -> "TI-84 calculator";
            case RawResource.TYPE_OFFICE_CALC -> "Office TI-84 calculator";
            case RawResource.TYPE_RENTAL_CALC -> "Rental TI-84 calculator";
            case RawResource.TYPE_RENTAL_MANUAL -> "TI-84 calculator manual";
            case RawResource.TYPE_INHOUSE_IPAD -> "iPad tablet";
            case RawResource.TYPE_INHOUSE_NOTEBOOK -> "Windows notebook";
            case RawResource.TYPE_INHOUSE_TEXT -> "Textbook";
            case RawResource.TYPE_OVERNIGHT_TEXT -> "Overnight textbook";
            case RawResource.TYPE_INHOUSE_HEADSET -> "Headphones";
            case RawResource.TYPE_INHOUSE_LOCK -> "Padlock";
            case RawResource.TYPE_TUTOR_TABLET -> "Tutor Tablet";
            case null, default -> "*** Unknown resource type";
        };
    }
}
