package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AbstractAdminTable;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.db.old.rawrecord.RawResource;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present all outstanding resources.
 */
final class JTableOutstandingResource extends AbstractAdminTable<OutstandingResourceRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6306057646798849263L;

    /**
     * Constructs a new {@code JTableOutstandingResource}.
     */
    JTableOutstandingResource() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<AdminTableColumn> makeColumns() {

        final List<AdminTableColumn> result = new ArrayList<>(8);

        result.add(new AdminTableColumn("Student ID", 100));
        result.add(new AdminTableColumn("Student Name", 180));
        result.add(new AdminTableColumn("Resource ID", 80));
        result.add(new AdminTableColumn("Resource type", 180));
        result.add(new AdminTableColumn("When Borrowed", 210));
        result.add(new AdminTableColumn("When Due", 100));

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
    protected int installData(final List<? extends OutstandingResourceRow> data, final int minRows) {

        final String[] row = new String[6];

        for (final OutstandingResourceRow record : data) {

            row[0] = valueToString(record.studentId);
            row[1] = valueToString(record.studentName);
            row[2] = valueToString(record.resourceId);
            row[3] = typeToString(record.resourceType);
            row[4] = record.loanDateTime == null ? CoreConstants.EMPTY
                    : TemporalUtils.FMT_MDY_AT_HM_A.format(record.loanDateTime);
            row[5] = record.dueDate == null ? CoreConstants.EMPTY : FMT_MDY.format(record.dueDate);

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

        final String result;

        if (RawResource.TYPE_INHOUSE_CALC.equals(type)) {
            result = "TI-84 calculator";
        } else if (RawResource.TYPE_OFFICE_CALC.equals(type)) {
            result = "Office TI-84 calculator";
        } else if (RawResource.TYPE_RENTAL_CALC.equals(type)) {
            result = "Rental TI-84 calculator";
        } else if (RawResource.TYPE_RENTAL_MANUAL.equals(type)) {
            result = "TI-84 calculator manual";
        } else if (RawResource.TYPE_INHOUSE_IPAD.equals(type)) {
            result = "iPad tablet";
        } else if (RawResource.TYPE_INHOUSE_NOTEBOOK.equals(type)) {
            result = "Windows notebook";
        } else if (RawResource.TYPE_INHOUSE_TEXT.equals(type)) {
            result = "Textbook";
        } else if (RawResource.TYPE_OVERNIGHT_TEXT.equals(type)) {
            result = "Overnight textbook";
        } else if (RawResource.TYPE_INHOUSE_HEADSET.equals(type)) {
            result = "Headphones";
        } else if (RawResource.TYPE_INHOUSE_LOCK.equals(type)) {
            result = "Padlock";
        } else if (RawResource.TYPE_TUTOR_TABLET.equals(type)) {
            result = "Tutor Tablet";
        } else {
            result = "*** Unknown resource type";
        }

        return result;
    }
}
