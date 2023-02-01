package internal.sdmxdl.desktop;

import ec.util.various.swing.JCommand;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

public final class JToolWindowBar extends JComponent {

    public static final String SELECTION_PROPERTY = "selection";
    private static final int NO_SELECTION = -1;

    @lombok.Getter
    private int selection = NO_SELECTION;

    public void setSelection(int selection) {
        firePropertyChange(SELECTION_PROPERTY, this.selection, this.selection = selection);
    }

    private final JToolBar bar = new JToolBar();

    private final List<Component> windows = new ArrayList<>();

    private final JSplitPane splitPane;

    private int dividerSize;

    private int dividerLocation;

    public JToolWindowBar(JSplitPane splitPane) {
        initComponents();
        this.splitPane = splitPane;
        this.dividerSize = splitPane.getDividerSize();
        this.dividerLocation = splitPane.getDividerLocation();
    }

    private void initComponents() {
        bar.setOrientation(JToolBar.VERTICAL);
        setLayout(new BorderLayout());
        add(bar, BorderLayout.CENTER);
        addPropertyChangeListener(SELECTION_PROPERTY, this::onSelectionChange);
    }

    private void onSelectionChange(PropertyChangeEvent evt) {
        int oldValue = (int) evt.getOldValue();
        int newValue = (int) evt.getNewValue();
        if (newValue != NO_SELECTION) {
            splitPane.setLeftComponent(windows.get(newValue));
            if (oldValue == NO_SELECTION) {
                splitPane.setDividerSize(dividerSize);
                splitPane.setDividerLocation(dividerLocation);
            }
        } else {
            dividerSize = splitPane.getDividerSize();
            dividerLocation = splitPane.getDividerLocation();
            splitPane.setDividerSize(0);
            splitPane.setDividerLocation(0);
            splitPane.setLeftComponent(null);
        }
    }

    public void addToolWindow(String name, Icon icon, Component window) {
        int index = windows.size();
        bar.add(newButton(index, name, icon));
        windows.add(window);
        if (windows.size() == 1) {
            setSelection(index);
        }
    }

    private JToggleButton newButton(int index, String name, Icon icon) {
        JToggleButton result = new JToggleButton(
                new ShowToolCommand(index)
                        .toAction(this)
                        .withWeakPropertyChangeListener(this, SELECTION_PROPERTY)
        );
        result.setToolTipText(name);
        result.setIcon(icon);
        return result;
    }

    @lombok.AllArgsConstructor
    private static final class ShowToolCommand extends JCommand<JToolWindowBar> {

        private final int index;

        @Override
        public boolean isSelected(@NonNull JToolWindowBar c) {
            return c.getSelection() == index;
        }

        @Override
        public void execute(@NonNull JToolWindowBar c) {
            c.setSelection(isSelected(c) ? NO_SELECTION : index);
        }
    }
}
