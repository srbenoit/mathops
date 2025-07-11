package dev.mathops.app.sim.swing;

import dev.mathops.commons.log.Log;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;

/**
 * A cell editor that presents a String as a JButton.
 */
public final class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,
        ActionListener, MouseListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6129090436846896119L;

    private final JTable table;
    private final Action action;
    private final int mnemonic = 0;
    private final Border originalBorder;
    private Border focusBorder;

    private final JButton editButton;
    private Object editorValue = null;
    private boolean isButtonColumnEditor = false;

    /**
     * Create the ButtonColumn to be used as a renderer and editor. The renderer and editor will automatically be
     * installed on the TableColumn of the specified column.
     *
     * @param theTable  the table containing the button renderer/editor
     * @param theAction the Action to be invoked when the button is invoked
     * @param theColumn the column to which the button renderer/editor is added
     */
    public ButtonColumn(final JTable theTable, final Action theAction, final int theColumn) {

        this.table = theTable;
        this.action = theAction;

        this.editButton = new JButton();
        this.editButton.setFocusPainted(false);
        this.editButton.addActionListener(this);
        this.originalBorder = this.editButton.getBorder();

        setFocusBorder(new LineBorder(Color.BLUE));

        final TableColumnModel columnModel = theTable.getColumnModel();
        columnModel.getColumn(theColumn).setCellRenderer(this);
        columnModel.getColumn(theColumn).setCellEditor(this);
        theTable.addMouseListener(this);
    }

    /**
     * Get foreground color of the button when the cell has focus
     *
     * @return the foreground color
     */
    public Border getFocusBorder() {

        return this.focusBorder;
    }

    /**
     * The foreground color of the button when the cell has focus
     *
     * @param theFocusBorder the foreground color
     */
    public void setFocusBorder(final Border theFocusBorder) {

        this.focusBorder = theFocusBorder;
        this.editButton.setBorder(theFocusBorder);
    }

//    public int getMnemonic() {
//
//        return this.mnemonic;
//    }
//
//    /**
//     * The mnemonic to activate the button when the cell has focus
//     *
//     * @param mnemonic the mnemonic
//     */
//    public void setMnemonic(final int mnemonic) {
//
//        this.mnemonic = mnemonic;
//        this.editButton.setMnemonic(mnemonic);
//    }

    @Override
    public Component getTableCellEditorComponent(final JTable theTable, final Object value, final boolean isSelected,
                                                 final int row, final int column) {

        if (value == null) {
            this.editButton.setText("");
            this.editButton.setIcon(null);
        } else if (value instanceof Icon) {
            this.editButton.setText("");
            this.editButton.setIcon((Icon) value);
        } else {
            this.editButton.setText(value.toString());
            this.editButton.setIcon(null);
        }

        this.editorValue = value;

        return this.editButton;
    }

    @Override
    public Object getCellEditorValue() {

        return this.editorValue;
    }

    public Component getTableCellRendererComponent(final JTable theTable, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {

//        if (isSelected) {
//            this.editButton.setForeground(table.getSelectionForeground());
//            this.editButton.setBackground(table.getSelectionBackground());
//        } else {
        this.editButton.setForeground(theTable.getForeground());
        this.editButton.setBackground(UIManager.getColor("Button.background"));
//        }

        if (hasFocus) {
            this.editButton.setBorder(this.focusBorder);
        } else {
            this.editButton.setBorder(this.originalBorder);
        }

        //		editButton.setText( (value == null) ? "" : value.toString() );
        if (value == null) {
            this.editButton.setText("");
            this.editButton.setIcon(null);
        } else if (value instanceof Icon) {
            this.editButton.setText("");
            this.editButton.setIcon((Icon) value);
        } else {
            this.editButton.setText(value.toString());
            this.editButton.setIcon(null);
        }

        return this.editButton;
    }

    /*
     *	The button has been pressed. Stop editing and invoke the custom Action
     */
    public void actionPerformed(final ActionEvent e) {

        Log.info("Button");

        final int row = this.table.convertRowIndexToModel(this.table.getEditingRow());
        fireEditingStopped();

        final ActionEvent event = new ActionEvent(this.table, ActionEvent.ACTION_PERFORMED, "" + row);
        this.action.actionPerformed(event);
    }

    /*
     *  When the mouse is pressed the editor is invoked. If you then then drag
     *  the mouse to another cell before releasing it, the editor is still
     *  active. Make sure editing is stopped when the mouse is released.
     */
    public void mousePressed(final MouseEvent e) {

        if (this.table.isEditing() && this.table.getCellEditor() == this) {
            this.isButtonColumnEditor = true;
        }
    }

    public void mouseReleased(final MouseEvent e) {

        if (this.isButtonColumnEditor && this.table.isEditing()) {
            this.table.getCellEditor().stopCellEditing();
        }

        this.isButtonColumnEditor = false;
    }

    public void mouseClicked(final MouseEvent e) {
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }
}
