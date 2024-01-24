package sdmxdl.desktop;

import ec.util.list.swing.JLists;
import internal.sdmxdl.desktop.PropertyFormats;
import internal.sdmxdl.desktop.SdmxRenderers;
import net.miginfocom.swing.MigLayout;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

public final class JDriver extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private Driver model;

    public void setModel(Driver model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField id = new JTextField();
    private final JList<WebSource> sources = new JList<>();
    private final JList<String> properties = new JList<>();

    public JDriver() {
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        id.setEditable(false);
        panel.add(new JLabel("ID"), "skip");
        panel.add(id, "span, growx");

        sources.setCellRenderer(JLists.cellRendererOf((label, value) -> {
            label.setText(SdmxRenderers.webSourceToText(value));
        }));

        panel.add(new JLabel("Sources"), "skip");
        panel.add(new JScrollPane(sources), "span, growx, h 100");

        properties.setCellRenderer(JLists.cellRendererOf((label, property) -> label.setText(PropertyFormats.toText(property))));
        panel.add(new JLabel("Properties"), "skip");
        panel.add(new JScrollPane(properties), "span, growx, h 100");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        id.setText(model.getDriverId());
        sources.setModel(JLists.modelOf(new ArrayList<>(model.getDefaultSources())));
        properties.setModel(JLists.modelOf(new ArrayList<>(model.getDriverProperties())));
    }
}
