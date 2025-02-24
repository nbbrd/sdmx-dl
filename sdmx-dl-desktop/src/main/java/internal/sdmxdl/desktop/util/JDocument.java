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

    private final JTabbedPane tabs = new JTabbedPane();

    private final List<Item<T>> items = new ArrayList<>();

    public JDocument() {
        initComponents();
    }

    private void initComponents() {
        toolBar.add(Box.createHorizontalGlue());
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
//        content.setTabPlacement(JTabbedPane.BOTTOM);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        items.forEach(item -> item.onModelChange.accept(model));
    }

    private void updateContent() {
        tabs.removeAll();
        removeAll();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                add(items.get(0).component, BorderLayout.CENTER);
                break;
            default:
                add(tabs, BorderLayout.CENTER);
                items.forEach(item -> tabs.addTab(item.title, item.component));
                break;
        }
    }

    public <C extends JComponent> void addComponent(String title, C component) {
        items.add(new Item<>(title, component, JDocument::doNothing));
        updateContent();
    }

    public <C extends JComponent> void addComponent(String title, C component, BiConsumer<C, T> onModelChange) {
        items.add(new Item<>(title, component, value -> onModelChange.accept(component, value)));
        updateContent();
    }

    public void clearComponents() {
        items.clear();
        updateContent();
    }

    public void addToolBarItem(JComponent item) {
        toolBar.add(item);
    }

    @lombok.AllArgsConstructor
    private static class Item<T> {
        String title;
        JComponent component;
        Consumer<T> onModelChange;
    }

    private static <T> void doNothing(T ignore) {
    }
}
