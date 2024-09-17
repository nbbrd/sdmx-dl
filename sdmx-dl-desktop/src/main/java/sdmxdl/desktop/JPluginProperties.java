package sdmxdl.desktop;

import ec.util.list.swing.JLists;
import lombok.Getter;
import sdmxdl.desktop.panels.PropertyRenderer;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public final class JPluginProperties<T> extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @Getter
    private T model = null;

    public void setModel(T model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    public static final String EXTRACTOR_PROPERTY = "extractor";

    @Getter
    private Function<T, Collection<String>> extractor = null;

    public void setExtractor(Function<T, Collection<String>> extractor) {
        firePropertyChange(EXTRACTOR_PROPERTY, this.extractor, this.extractor = extractor);
    }

    private final JList<String> properties = new JList<>();

    public JPluginProperties() {
        initComponents();
    }

    private void initComponents() {
        properties.setCellRenderer(PropertyRenderer.INSTANCE.asListCellRenderer(properties::repaint));

        setLayout(new BorderLayout());
        add(new JScrollPane(properties), BorderLayout.CENTER);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
        addPropertyChangeListener(EXTRACTOR_PROPERTY, this::onExtractorChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        updateList();
    }

    private void onExtractorChange(PropertyChangeEvent evt) {
        updateList();
    }

    private void updateList() {
        if (model != null && extractor != null) {
            properties.setModel(JLists.modelOf(new ArrayList<>(extractor.apply(model))));
        } else {
            properties.setModel(JLists.emptyModel());
        }
    }
}
