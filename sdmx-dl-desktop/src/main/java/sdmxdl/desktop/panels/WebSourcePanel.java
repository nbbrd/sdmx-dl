package sdmxdl.desktop.panels;

import ec.util.table.swing.JTables;
import internal.sdmxdl.desktop.util.BrowseCommand;
import internal.sdmxdl.desktop.util.ButtonBuilder;
import internal.sdmxdl.desktop.util.MapTableModel;
import internal.sdmxdl.desktop.util.NoOpCommand;
import net.miginfocom.swing.MigLayout;
import sdmxdl.Confidentiality;
import sdmxdl.web.WebSource;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.Objects;

import static com.formdev.flatlaf.FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT;
import static java.util.Collections.emptyMap;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_EXPORT;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_LAUNCH;

public final class WebSourcePanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    public static final String BROWSE_WEBSITE_ACTION = "browseWebsite";

    public static final String BROWSE_MONITOR_ACTION = "browseMonitor";

    public static final String EXPORT_MODEL_ACTION = "exportModel";

    @lombok.Getter
    private WebSource model;

    public void setModel(WebSource model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField id = new JTextField();
    private final JComboBox<Confidentiality> confidentiality = new JComboBox<>(Confidentiality.values());
    private final JTable names = new JTable();
    private final JTextField driver = new JTextField();
    private final JTextField endpoint = new JTextField();
    private final JTable properties = new JTable();
    private final JTextField aliases = new JTextField();
    private final JTextField website = new JTextField();
    private final JTextField monitor = new JTextField();

    public WebSourcePanel() {
        initComponents();
    }

    private void initComponents() {
        ActionMap am = getActionMap();

        am.put(BROWSE_WEBSITE_ACTION,
                BrowseCommand.ofURL(WebSourcePanel::getWebsiteOrNull)
                        .toAction(this)
                        .withWeakPropertyChangeListener(this, MODEL_PROPERTY));

        am.put(BROWSE_MONITOR_ACTION,
                BrowseCommand.ofURL(WebSourcePanel::getMonitorWebsiteOrNull)
                        .toAction(this)
                        .withWeakPropertyChangeListener(this, MODEL_PROPERTY));

        am.put(EXPORT_MODEL_ACTION,
                NoOpCommand.INSTANCE
                        .toAction(this)
                        .withWeakPropertyChangeListener(this, MODEL_PROPERTY));

        id.setEditable(false);
        confidentiality.setEnabled(false);
        names.setModel(new MapTableModel("Language", "Name"));
        JTables.setWidthAsPercentages(names, .2, .8);
        aliases.setEditable(false);
        website.setEditable(false);
        website.putClientProperty(
                TEXT_FIELD_TRAILING_COMPONENT,
                new ButtonBuilder()
                        .action(am.get(BROWSE_WEBSITE_ACTION))
                        .ikon(MDI_LAUNCH)
                        .toolTipText("Browse website")
                        .build()
        );
        driver.setEditable(false);
        endpoint.setEditable(false);
        properties.setModel(new MapTableModel("Name", "Value"));
        properties.getColumnModel().getColumn(0).setCellRenderer(PropertyRenderer.INSTANCE.asTableCellRenderer(properties::repaint));
        JTables.setWidthAsPercentages(properties, .6, .4);
        monitor.setEditable(false);
        monitor.putClientProperty(
                TEXT_FIELD_TRAILING_COMPONENT,
                new ButtonBuilder()
                        .action(am.get(BROWSE_MONITOR_ACTION))
                        .ikon(MDI_LAUNCH)
                        .toolTipText("Browse monitor")
                        .build()
        );

        JButton extract = new ButtonBuilder()
                .action(am.get(EXPORT_MODEL_ACTION))
                .ikon(MDI_EXPORT)
                .toolTipText("Export source...")
                .build();

        JPanel panel = new JPanel(new MigLayout(
                "ins 20",
                "[para][][]",
                ""));

        panel.add(new JLabel("Description"), "gapbottom 1, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, growx");

        panel.add(new JLabel("ID"), "skip, align left, sizegroup lbl");
        panel.add(id, "growx, w 150,  sizegroup ids");
        panel.add(new JLabel("Confidentiality"), "skip, align left");
        panel.add(confidentiality, "w 70, sizegroup ids");
        panel.add(extract, "align trailing, wrap");

        panel.add(new JLabel("Aliases"), "skip, align left, sizegroup lbl");
        panel.add(aliases, "span, growx");

        panel.add(new JLabel("Names"), "skip, align left, sizegroup lbl");
        panel.add(new JScrollPane(names), "span, growx, h 100");

        panel.add(new JLabel("Website"), "skip, align left, sizegroup lbl");
        panel.add(website, "span, growx, wrap 20px");

        panel.add(new JLabel("Connection"), "gapbottom 1, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, growx");

        panel.add(new JLabel("Driver"), "skip, align left, sizegroup lbl");
        panel.add(driver, "span, growx");

        panel.add(new JLabel("Endpoint"), "skip, align left, sizegroup lbl");
        panel.add(endpoint, "span, growx");

        panel.add(new JLabel("Properties"), "skip, align left, sizegroup lbl");
        panel.add(new JScrollPane(properties), "span, growx, h 100");

        panel.add(new JLabel("Monitor"), "skip, align left, sizegroup lbl");
        panel.add(monitor, "span, growx");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            id.setText(model.getId());
            confidentiality.setSelectedItem(model.getConfidentiality());
            ((MapTableModel) names.getModel()).setData(model.getNames());
            driver.setText(model.getDriver());
            endpoint.setText(model.getEndpoint().toString());
            ((MapTableModel) properties.getModel()).setData(model.getProperties());
            aliases.setText(String.join(", ", model.getAliases()));
            website.setText(Objects.toString(model.getWebsite()));
            monitor.setText(Objects.toString(model.getMonitor()));
        } else {
            id.setText("");
            confidentiality.setSelectedItem(Confidentiality.RESTRICTED);
            ((MapTableModel) names.getModel()).setData(emptyMap());
            driver.setText("");
            endpoint.setText("");
            ((MapTableModel) properties.getModel()).setData(emptyMap());
            aliases.setText("");
            website.setText("");
            monitor.setText("");
        }
    }

    private static URL getWebsiteOrNull(WebSourcePanel c) {
        WebSource source = c.getModel();
        return source == null ? null : source.getWebsite();
    }

    private static URL getMonitorWebsiteOrNull(WebSourcePanel c) {
        WebSource source = c.getModel();
        return source == null ? null : source.getMonitorWebsite();
    }
}
