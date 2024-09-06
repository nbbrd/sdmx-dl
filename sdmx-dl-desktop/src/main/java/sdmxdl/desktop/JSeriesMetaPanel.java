package sdmxdl.desktop;

import lombok.Getter;
import sdmxdl.Attribute;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class JSeriesMetaPanel extends JComponent implements HasModel<SingleSeries> {

    @Getter
    private SingleSeries model;

    public void setModel(SingleSeries model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTable table = new JTable();

    public JSeriesMetaPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(table));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            table.setModel(asMetaTableModel(model));
        } else {
            table.setModel(new DefaultTableModel());
        }
    }

    private TableModel asMetaTableModel(SingleSeries item) {
        DefaultTableModel result = new DefaultTableModel();
        result.addColumn("Name");
        result.addColumn("Value");
        Map<String, Attribute> attributeById = item.getDsd().getAttributes().stream().collect(toMap(Attribute::getId, identity()));
        item.getSeries().getMeta().forEach((k, v) -> {
            Attribute attribute = attributeById.get(k);
            result.addRow(attribute != null
                    ? new Object[]{attribute.getName(), attribute.isCoded() ? attribute.getCodelist().getCodes().getOrDefault(v, v) : v}
                    : new Object[]{k, v});
        });
        return result;
    }
}
