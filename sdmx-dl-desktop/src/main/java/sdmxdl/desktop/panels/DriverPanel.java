package sdmxdl.desktop.panels;

import ec.util.list.swing.JLists;
import internal.sdmxdl.desktop.util.NoOpCommand;
import net.miginfocom.swing.MigLayout;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import static internal.sdmxdl.desktop.util.MouseListeners.onDoubleClick;
import static java.awt.event.KeyEvent.VK_ENTER;
import static javax.swing.KeyStroke.getKeyStroke;

public final class DriverPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private Driver model;

    public void setModel(Driver model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField id = new JTextField();
    private final JList<WebSource> sourcesList = new JList<>();
    private final JList<String> properties = new JList<>();

    public DriverPanel() {
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        id.setEditable(false);
        panel.add(new JLabel("ID"), "skip");
        panel.add(id, "span, growx");

        sourcesList.setCellRenderer(WebSourceRenderer.INSTANCE.asListCellRenderer(sourcesList::repaint));
        sourcesList.addMouseListener(onDoubleClick(e -> sourcesList.getActionMap().get("SELECT_ACTION").actionPerformed(null)));
        sourcesList.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        sourcesList.getActionMap().put("SELECT_ACTION", NoOpCommand.INSTANCE.toAction(this));
        panel.add(new JLabel("Sources"), "skip");
        panel.add(new JScrollPane(sourcesList), "span, growx, h 100");

        properties.setCellRenderer(PropertyRenderer.INSTANCE.asListCellRenderer(properties::repaint));
        panel.add(new JLabel("Properties"), "skip");
        panel.add(new JScrollPane(properties), "span, growx, h 100");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            id.setText(model.getDriverId());
            sourcesList.setModel(JLists.modelOf(new ArrayList<>(model.getDefaultSources())));
            properties.setModel(JLists.modelOf(new ArrayList<>(model.getDriverProperties())));
        }
    }
}
