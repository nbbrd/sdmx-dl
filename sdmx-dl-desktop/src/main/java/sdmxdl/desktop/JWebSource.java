package sdmxdl.desktop;

import ec.util.table.swing.JTables;
import internal.sdmxdl.desktop.BrowseCommand;
import internal.sdmxdl.desktop.ButtonBuilder;
import internal.sdmxdl.desktop.PropertyFormats;
import net.miginfocom.swing.MigLayout;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.formdev.flatlaf.FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT;

public final class JWebSource extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private WebSource model;

    public void setModel(WebSource model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField id = new JTextField();
    private final JTable names = new JTable();
    private final JTextField driver = new JTextField();
    private final JTextField endpoint = new JTextField();
    private final JTable properties = new JTable();
    private final JTextField aliases = new JTextField();
    private final JTextField website = new JTextField();
    private final JTextField monitor = new JTextField();

    public JWebSource() {
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        id.setEditable(false);
        panel.add(new JLabel("ID"), "skip");
        panel.add(id, "span, growx");

        aliases.setEditable(false);
        panel.add(new JLabel("Aliases"), "skip");
        panel.add(aliases, "span, growx");

        panel.add(new JLabel("Names"), "skip");
        panel.add(new JScrollPane(names), "span, growx, h 100");

        driver.setEditable(false);
        panel.add(new JLabel("Driver"), "skip");
        panel.add(driver, "span, growx");

        endpoint.setEditable(false);
        panel.add(new JLabel("Endpoint"), "skip");
        panel.add(endpoint, "span, growx");

        panel.add(new JLabel("Properties"), "skip");
        panel.add(new JScrollPane(properties), "span, growx, h 100");

        website.setEditable(false);
        website.putClientProperty(
                TEXT_FIELD_TRAILING_COMPONENT,
                new ButtonBuilder()
                        .action(BrowseCommand.ofURL(JWebSource::getWebsite)
                                .toAction(this)
                                .withWeakPropertyChangeListener(this, MODEL_PROPERTY))
                        .ikon(MaterialDesign.MDI_LAUNCH)
                        .buildButton()
        );
        panel.add(new JLabel("Website"), "skip");
        panel.add(website, "span, growx");

        monitor.setEditable(false);
        monitor.putClientProperty(
                TEXT_FIELD_TRAILING_COMPONENT,
                new ButtonBuilder()
                        .action(BrowseCommand.ofURL(JWebSource::getMonitorWebsite)
                                .toAction(this)
                                .withWeakPropertyChangeListener(this, MODEL_PROPERTY))
                        .ikon(MaterialDesign.MDI_LAUNCH)
                        .buildButton()
        );
        panel.add(new JLabel("Monitor"), "skip");
        panel.add(monitor, "span, growx");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        id.setText(model.getId());
        names.setModel(new MapTableModel(model.getNames(), "Language", "Name"));
        JTables.setWidthAsPercentages(names, .2, .8);
        driver.setText(model.getDriver());
        endpoint.setText(model.getEndpoint().toString());
        properties.setModel(new MapTableModel(model.getProperties(), "Name", "Value"));
        properties.getColumnModel().getColumn(0).setCellRenderer(JTables.cellRendererOf((label, value) -> {
            if (value instanceof String) {
                label.setText(PropertyFormats.toText((String) value));
            }
        }));
        JTables.setWidthAsPercentages(properties, .6, .4);
        aliases.setText(String.join(", ", model.getAliases()));
        website.setText(Objects.toString(model.getWebsite()));
        monitor.setText(Objects.toString(model.getMonitor()));
    }

    private static final class MapTableModel extends AbstractTableModel {
        private final List<Map.Entry<String, String>> data;
        private final String keyLabel;
        private final String valueLabel;

        public MapTableModel(Map<String, String> map, String keyLabel, String valueLabel) {
            this.data = map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            this.keyLabel = keyLabel;
            this.valueLabel = valueLabel;
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
            Map.Entry<String, String> row = data.get(rowIndex);
            return columnIndex == 0 ? row.getKey() : row.getValue();
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? keyLabel : valueLabel;
        }
    }

    private static URL getWebsite(JWebSource c) {
        WebSource source = c.getModel();
        if (source == null) return null;
        return source.getWebsite();
    }

    private static URL getMonitorWebsite(JWebSource c) {
        WebSource model = c.getModel();
        if (model == null) return null;
        return model.getMonitorWebsite();
    }
}
