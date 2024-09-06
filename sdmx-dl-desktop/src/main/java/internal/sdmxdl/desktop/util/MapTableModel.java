package internal.sdmxdl.desktop.util;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MapTableModel extends AbstractTableModel {
    private final List<Map.Entry<?, ?>> data;
    private final String keyLabel;
    private final String valueLabel;

    public MapTableModel(String keyLabel, String valueLabel) {
        this.data = new ArrayList<>();
        this.keyLabel = keyLabel;
        this.valueLabel = valueLabel;
    }

    public void setData(Map<String, String> map) {
        data.clear();
        map.entrySet().forEach(data::add);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Map.Entry<?, ?> row = data.get(rowIndex);
        return columnIndex == 0 ? row.getKey() : row.getValue();
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? keyLabel : valueLabel;
    }
}
