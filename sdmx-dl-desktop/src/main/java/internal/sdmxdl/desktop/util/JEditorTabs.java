package internal.sdmxdl.desktop.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;

import static com.formdev.flatlaf.FlatClientProperties.*;

public final class JEditorTabs extends JComponent {

    private final JTabbedPane main = new JTabbedPane();

    public JEditorTabs() {
        initComponents();
    }

    private void initComponents() {
        main.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        main.putClientProperty(TABBED_PANE_TAB_CLOSE_CALLBACK, (IntConsumer) main::removeTabAt);
        main.putClientProperty(TABBED_PANE_TAB_TYPE, TABBED_PANE_TAB_TYPE_CARD);
        main.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        setLayout(new BorderLayout());
        add(main, BorderLayout.CENTER);
    }

    public <ID> void addIfAbsent(ID id, TabFactory<ID> tabFactory) {
        String title = tabFactory.getTitle(id, this);
        int index = main.indexOfTab(title);
        if (index != -1) {
            main.setSelectedIndex(index);
        } else {
            main.addTab(title, tabFactory.getIcon(id, this), tabFactory.getComponent(id, this));
            main.setSelectedIndex(main.getTabCount() - 1);
        }
    }

    public interface TabFactory<ID> {

        String getTitle(ID id, JEditorTabs source);

        Icon getIcon(ID id, JEditorTabs source);

        Component getComponent(ID id, JEditorTabs source);
    }

    @lombok.Builder
    public static final class TabFactorySupport<ID> implements TabFactory<ID> {

        BiFunction<? super ID, ? super JEditorTabs, ? extends String> titleFactory;
        BiFunction<? super ID, ? super JEditorTabs, ? extends Icon> iconFactory;
        BiFunction<? super ID, ? super JEditorTabs, ? extends Component> componentFactory;

        @Override
        public String getTitle(ID id, JEditorTabs source) {
            return titleFactory.apply(id, source);
        }

        @Override
        public Icon getIcon(ID id, JEditorTabs source) {
            return iconFactory.apply(id, source);
        }

        @Override
        public Component getComponent(ID id, JEditorTabs source) {
            return componentFactory.apply(id, source);
        }
    }
}
