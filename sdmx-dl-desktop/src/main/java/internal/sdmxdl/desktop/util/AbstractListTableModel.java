package internal.sdmxdl.desktop.util;

import ec.util.list.swing.JLists;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

public abstract class AbstractListTableModel<ROW> extends AbstractTableModel {

    private final ListDataListener listener = JLists.dataListenerOf(ignore -> fireTableDataChanged());

    @lombok.Getter
    private ListModel<ROW> list = new DefaultListModel<>();

    public void setList(ListModel<ROW> list) {
        this.list.removeListDataListener(listener);
        this.list = list;
        list.addListDataListener(listener);
        fireTableStructureChanged();
    }

    public AbstractListTableModel() {
    }

    public AbstractListTableModel(ListModel<ROW> list) {
        setList(list);
    }

    @Override
    public int getRowCount() {
        return list.getSize();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getColumnValue(list.getElementAt(rowIndex), columnIndex);
    }

    abstract protected Object getColumnValue(ROW row, int columnIndex);
}
