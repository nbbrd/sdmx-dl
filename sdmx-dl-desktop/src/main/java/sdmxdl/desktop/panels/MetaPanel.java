package sdmxdl.desktop.panels;

import ec.util.table.swing.JTables;
import lombok.Getter;
import sdmxdl.Attribute;
import sdmxdl.desktop.SingleSeries;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import static ec.util.table.swing.JTables.cellRendererOf;
import static internal.sdmxdl.desktop.util.Html4Swing.labelTag;
import static j2html.TagCreator.html;
import static j2html.TagCreator.small;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class MetaPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @Getter
    private SingleSeries model;

    public void setModel(SingleSeries model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTable table = new JTable();

    public MetaPanel() {
        initComponents();
    }

    private void initComponents() {
        DefaultTableModel result = new DefaultTableModel();
        result.addColumn("Attribute ID");
        result.addColumn("Attribute name");
        result.addColumn("Code ID");
        result.addColumn("Code text");
        table.setModel(result);
        table.getColumnModel().getColumn(0).setCellRenderer(cellRendererOf(this::renderID));
        table.getColumnModel().getColumn(2).setCellRenderer(cellRendererOf(this::renderID));
        JTables.setWidthAsPercentages(table, 0.15, 0.3, 0.10, 0.45);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(table));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void renderID(JLabel label, String value) {
        label.setHorizontalAlignment(JLabel.TRAILING);
        label.setText(value != null ? html(small(labelTag(value, model.getAccentColor()))).render() : null);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            table.setModel(asMetaTableModel(model));
        } else {
            table.setModel(new DefaultTableModel());
        }
    }

    private TableModel asMetaTableModel(SingleSeries item) {
        DefaultTableModel result = (DefaultTableModel) table.getModel();
        result.getDataVector().clear();
        Map<String, Attribute> attributeById = item.getDsd().getAttributes().stream().collect(toMap(Attribute::getId, identity()));
        item.getSeries().getMeta().forEach((key, value) -> {
            Attribute attribute = attributeById.get(key);
            result.addRow(attribute != null
                    ? new Object[]
                    {
                            attribute.getId(),
                            attribute.getName(),
                            attribute.isCoded() ? value : null,
                            attribute.isCoded() ? attribute.getCodelist().getCodes().getOrDefault(value, value) : value,
                    }
                    : new Object[]{null, key, null, value});
        });
        return result;
    }
}
