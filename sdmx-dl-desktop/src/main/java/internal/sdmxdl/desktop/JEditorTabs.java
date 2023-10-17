package internal.sdmxdl.desktop;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;
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

        setLayout(new BorderLayout());
        add(main, BorderLayout.CENTER);
    }

    public <ID> void addIfAbsent(ID id,
                                 Function<? super ID, ? extends String> titleFactory,
                                 Function<? super ID, ? extends Icon> iconFactory,
                                 Function<? super ID, ? extends Component> componentFactory
    ) {
        String title = titleFactory.apply(id);
        int index = main.indexOfTab(title);
        if (index != -1) {
            main.setSelectedIndex(index);
        } else {
            main.addTab(title, iconFactory.apply(id), componentFactory.apply(id));
            main.setSelectedIndex(main.getTabCount() - 1);
        }
    }
}
