package internal.sdmxdl.desktop.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class JDocument<T> extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private T model;

    public void setModel(T model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JToolBar toolBar = new JToolBar();

    private final JTabbedPane content = new JTabbedPane();

    private final List<Consumer<T>> callbacks = new ArrayList<>();

    public JDocument() {
        initComponents();
    }

    private void initComponents() {
        toolBar.add(Box.createHorizontalGlue());
        content.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(content));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        callbacks.forEach(callback -> callback.accept(model));
    }

    public <C extends JComponent> void addComponent(String title, C component) {
        content.add(title, component);
    }

    public <C extends JComponent> void addComponent(String title, C component, BiConsumer<C, T> onModelChange) {
        content.add(title, component);
        onModelChange.accept(component, model);
        callbacks.add(model -> onModelChange.accept(component, model));
    }

    public void addToolBarItem(JComponent item) {
        toolBar.add(item);
    }
}
