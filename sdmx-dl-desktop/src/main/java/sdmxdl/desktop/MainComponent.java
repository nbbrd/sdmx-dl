package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.XmlDataSourceRef;
import internal.sdmxdl.desktop.util.*;
import lombok.NonNull;
import org.kordamp.ikonli.Ikon;
import sdmxdl.desktop.panels.*;
import sdmxdl.ext.Persistence;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static internal.sdmxdl.desktop.Collectors2.getSingle;
import static internal.sdmxdl.desktop.util.Actions.onActionPerformed;
import static internal.sdmxdl.desktop.util.JTrees.toDefaultMutableTreeNode;
import static internal.sdmxdl.desktop.util.MouseListeners.onDoubleClick;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.stream.Collectors.toList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;

public final class MainComponent extends JComponent {

    public static final String DATA_SOURCES_PROPERTY = "dataSources";

    @lombok.Getter
    private DefaultListModel<DataSourceRef> dataSources = new DefaultListModel<>();

    public void setDataSources(@NonNull DefaultListModel<DataSourceRef> dataSources) {
        firePropertyChange(DATA_SOURCES_PROPERTY, this.dataSources, this.dataSources = dataSources);
    }

    private final JTree datasetsTree = new JTree();

    private final JList<WebSource> sourcesList = new JList<>();

    private final JTree pluginsTree = new JTree();

    private final JEditorTabs main = new JEditorTabs();

    private final JSplitPane splitPane = new JSplitPane();

    private final ListDataListener dataSourcesListener = JLists.dataListenerOf(this::contentsChanged);

    public MainComponent() {
        initComponent();
    }

    private void initComponent() {
        datasetsTree.setRootVisible(false);
        datasetsTree.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(datasetsTree);
        datasetsTree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof DataSourceRef) {
                        DataSourceRefRenderer.INSTANCE.render(label, (DataSourceRef) userObject, tree::repaint);
                    } else if (userObject instanceof DataSetRef) {
                        DataSetRefRenderer.INSTANCE.render(label, (DataSetRef) userObject, tree::repaint);
                    } else if (userObject instanceof SwingWorker) {
                        SwingWorkerRenderer.INSTANCE.render(label, (SwingWorker<?, ?>) userObject, tree::repaint);
                    } else if (userObject instanceof Exception) {
                        ExceptionRenderer.INSTANCE.render(label, (Exception) userObject, tree::repaint);
                    }
                }
                return label;
            }
        });
        DynamicTree.enable(datasetsTree, new DataNodeFactory(Sdmxdl.INSTANCE::getSdmxManager), new DefaultMutableTreeNode("root"));
        datasetsTree.addMouseListener(onDoubleClick(this::openCurrentDataSetRef));
        datasetsTree.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        datasetsTree.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentDataSetRef));
        datasetsTree.setComponentPopupMenu(newDatasetsMenu().getPopupMenu());

        sourcesList.setCellRenderer(WebSourceRenderer.INSTANCE.asListCellRenderer(sourcesList::repaint));
        sourcesList.addMouseListener(onDoubleClick(e -> sourcesList.getActionMap().get("SELECT_ACTION").actionPerformed(null)));
        sourcesList.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        sourcesList.getActionMap().put("SELECT_ACTION",
                new OpenCurrentSourceCommand().toAction(this)
                        .withWeakListSelectionListener(sourcesList.getSelectionModel()));
        sourcesList.setComponentPopupMenu(newSourcesMenu().getPopupMenu());

        pluginsTree.setCellRenderer(JTrees.cellRendererOf(Object.class, (label, value) -> renderPlugin(label, value, pluginsTree::repaint)));
        pluginsTree.addMouseListener(onDoubleClick(this::openCurrentPlugin));
        pluginsTree.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        pluginsTree.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentPlugin));

        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setRightComponent(main);
        splitPane.setResizeWeight(.25);

        JToolWindowBar toolWindowBar = new JToolWindowBar(splitPane);
        addTool(toolWindowBar, "Datasets", MDI_FILE_TREE, new JScrollPane(datasetsTree), newDatasetsToolBar());
        addTool(toolWindowBar, "Sources", MDI_SERVER_NETWORK, new JScrollPane(sourcesList), newSourcesToolBar());
        addTool(toolWindowBar, "Plugins", MDI_CHIP, new JScrollPane(pluginsTree), new JToolBar());

        setLayout(new BorderLayout());
        add(toolWindowBar, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);

        dataSources.addListDataListener(dataSourcesListener);

        addPropertyChangeListener(DATA_SOURCES_PROPERTY, this::onDataSourcesChange);

        onSdmxWebManagerChange(null);
    }

    private void addTool(JToolWindowBar toolToolBar, String name, Ikon ikon, Component component, JToolBar toolBar) {
        JTabbedPane tool = new JTabbedPane();
        tool.addTab(name, component);
        tool.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
        toolToolBar.addToolWindow(name, Ikons.of(ikon, 24, FlatIconColors.ACTIONS_GREYINLINE.key), tool);
    }

    private JMenu newDatasetsMenu() {
        JMenu result = new JMenu();

        JMenuItem item;

        item = result.add(NoOpCommand.INSTANCE.toAction(datasetsTree));
        item.setText("Pin dataset");

        item = result.add(NoOpCommand.INSTANCE.toAction(datasetsTree));
        item.setText("Open DSD");

        item = result.add(NoOpCommand.INSTANCE.toAction(datasetsTree));
        item.setText("Open web site");

        item = result.add(NoOpCommand.INSTANCE.toAction(datasetsTree));
        item.setText("Automate");

        return result;
    }

    private JMenu newSourcesMenu() {
        JMenu result = new JMenu();

        JMenuItem item;

        item = result.add(sourcesList.getActionMap().get("SELECT_ACTION"));
        item.setText("<html><b>Open</b>");
        item.setAccelerator(getKeyStroke(VK_ENTER, 0));

        item = result.add(new AddDatasetCommand(() -> DataSourceRef.builder().source(getSelectedSource(sourcesList)).build()).toAction(this));
        item.setText("Add dataset");

        item = result.add(BrowseCommand.ofURL(MainComponent::getSelectedWebsite).toAction(sourcesList).withWeakListSelectionListener(sourcesList.getSelectionModel()));
        item.setText("Open website");

        item = result.add(BrowseCommand.ofURL(MainComponent::getSelectedMonitorWebsite).toAction(sourcesList).withWeakListSelectionListener(sourcesList.getSelectionModel()));
        item.setText("Open monitor");

        return result;
    }

    private JToolBar newDatasetsToolBar() {
        JToolBar result = new JToolBar();
        result.add(Box.createHorizontalGlue());

        result.add(new ButtonBuilder()
                .action(new AddDatasetCommand(() -> DataSourceRef.builder().source("").build()).toAction(this))
                .ikon(MDI_DATABASE_PLUS)
                .toolTipText("Add dataset")
                .build());

        result.add(new ButtonBuilder()
                .action(Actions.onActionPerformed(() -> JTrees.expandOrCollapseAll(datasetsTree, true)))
                .ikon(MDI_UNFOLD_MORE)
                .toolTipText("Expand All")
                .build());

        result.add(new ButtonBuilder()
                .action(Actions.onActionPerformed(() -> JTrees.expandOrCollapseAll(datasetsTree, false)))
                .ikon(MDI_UNFOLD_LESS)
                .toolTipText("Collapse All")
                .build());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(datasetsTree))
                .ikon(MDI_MENU)
                .toolTipText("Options")
                .build());

        return result;
    }

    private JToolBar newSourcesToolBar() {
        JToolBar result = new JToolBar();
        result.add(Box.createHorizontalGlue());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(sourcesList))
                .ikon(MDI_FILTER_VARIANT)
                .toolTipText("Filter")
                .build());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(sourcesList))
                .ikon(MDI_MENU)
                .toolTipText("Options")
                .build());

        return result;
    }

    private void openCurrentDataSetRef() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) datasetsTree.getLastSelectedPathComponent();
        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject instanceof DataSetRef) {
                DataSetRef dataSetRef = (DataSetRef) userObject;
                if (dataSetRef.getKey().isSeries()) {
                    main.addIfAbsent(dataSetRef, DataSetRefRenderer.INSTANCE.asTabFactory(this));
                }
            } else if (userObject instanceof Exception) {
                main.addIfAbsent((Exception) userObject, ExceptionRenderer.INSTANCE.asTabFactory(this));
            }
        }
    }

    private void openCurrentPlugin() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) pluginsTree.getLastSelectedPathComponent();
        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject instanceof Driver) {
                main.addIfAbsent((Driver) userObject, DriverRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof Authenticator) {
                main.addIfAbsent((Authenticator) userObject, AuthenticatorRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof Monitor) {
                main.addIfAbsent((Monitor) userObject, MonitorRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof Persistence) {
                main.addIfAbsent((Persistence) userObject, PersistenceRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof Registry) {
                main.addIfAbsent((Registry) userObject, RegistryRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof WebCaching) {
                main.addIfAbsent((WebCaching) userObject, WebCachingRenderer.INSTANCE.asTabFactory(this));
            } else if (userObject instanceof Networking) {
                main.addIfAbsent((Networking) userObject, NetworkingRenderer.INSTANCE.asTabFactory(this));
            }
        }
    }

    private void renderPlugin(JLabel label, Object value, Runnable onUpdate) {
        if (value instanceof Driver) {
            DriverRenderer.INSTANCE.render(label, (Driver) value, onUpdate);
        } else if (value instanceof Authenticator) {
            AuthenticatorRenderer.INSTANCE.render(label, (Authenticator) value, onUpdate);
        } else if (value instanceof Monitor) {
            MonitorRenderer.INSTANCE.render(label, (Monitor) value, onUpdate);
        } else if (value instanceof Persistence) {
            PersistenceRenderer.INSTANCE.render(label, (Persistence) value, onUpdate);
        } else if (value instanceof Registry) {
            RegistryRenderer.INSTANCE.render(label, (Registry) value, onUpdate);
        } else if (value instanceof WebCaching) {
            WebCachingRenderer.INSTANCE.render(label, (WebCaching) value, onUpdate);
        } else if (value instanceof Networking) {
            NetworkingRenderer.INSTANCE.render(label, (Networking) value, onUpdate);
        }
    }

    private void onSdmxWebManagerChange(PropertyChangeEvent ignore) {
        SdmxWebManager manager = Sdmxdl.INSTANCE.getSdmxManager();
        sourcesList.setModel(JLists.modelOf(manager.getSources().values().stream().filter(o -> !o.isAlias()).collect(toList())));
        DefaultMutableTreeNode plugins = new DefaultMutableTreeNode();
        plugins.add(manager.getDrivers().stream().collect(toDefaultMutableTreeNode("Drivers")));
        plugins.add(manager.getAuthenticators().stream().collect(toDefaultMutableTreeNode("Authenticators")));
        plugins.add(manager.getMonitors().stream().collect(toDefaultMutableTreeNode("Monitors")));
        plugins.add(manager.getPersistences().stream().collect(toDefaultMutableTreeNode("Persistences")));
        plugins.add(new DefaultMutableTreeNode(manager.getRegistry()));
        plugins.add(new DefaultMutableTreeNode(manager.getCaching()));
        plugins.add(new DefaultMutableTreeNode(manager.getNetworking()));
        pluginsTree.setModel(new DefaultTreeModel(plugins));
        JTrees.expandOrCollapseAll(pluginsTree, true);
    }

    private void onDataSourcesChange(PropertyChangeEvent evt) {
        DefaultListModel<?> oldValue = (DefaultListModel<?>) evt.getOldValue();
        if (oldValue != null) {
            oldValue.removeListDataListener(dataSourcesListener);
        }
        DefaultListModel<?> newValue = (DefaultListModel<?>) evt.getNewValue();
        if (newValue != null) {
            newValue.addListDataListener(dataSourcesListener);
        }
        contentsChanged(null);
    }

    private void contentsChanged(ListDataEvent e) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) datasetsTree.getModel().getRoot();
        root.removeAllChildren();
        JLists.stream(dataSources).forEach(dataSourceRef -> root.add(new DynamicTree.CustomNode(dataSourceRef, false)));
        datasetsTree.setModel(new DefaultTreeModel(root));
    }

    private static final class OpenCurrentSourceCommand extends JCommand<MainComponent> {

        @Override
        public boolean isEnabled(@NonNull MainComponent component) {
            return component.sourcesList.getSelectedValuesList().size() == 1;
        }

        @Override
        public void execute(@NonNull MainComponent component) throws Exception {
            component.main.addIfAbsent(
                    component.sourcesList.getSelectedValue(),
                    WebSourceRenderer.INSTANCE.asTabFactory(component)
            );
        }
    }

    @lombok.AllArgsConstructor
    private static final class AddDatasetCommand extends JCommand<MainComponent> {

        private final @NonNull Supplier<DataSourceRef> base;

        @Override
        public void execute(@NonNull MainComponent c) {
            c.addDataSource(base.get());
        }
    }

    private static String getSelectedSource(JList<WebSource> x) {
        return getSingle(x.getSelectedValuesList()).map(WebSource::getId).orElse(null);
    }

    private static URL getSelectedWebsite(JList<WebSource> x) {
        return getSingle(x.getSelectedValuesList()).map(WebSource::getWebsite).orElse(null);
    }

    private static URL getSelectedMonitorWebsite(JList<WebSource> x) {
        return getSingle(x.getSelectedValuesList()).map(WebSource::getMonitorWebsite).orElse(null);
    }

    public void addDataSource(DataSourceRef base) {
        DataSourceRefPanel panel = new DataSourceRefPanel();
        panel.setModel(base);
        if (JOptionPane.showOptionDialog(this, panel, "Add dataset",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Add", "Cancel"}, "Add") == 0) {
            getDataSources().addElement(panel.getModel());
        }
    }

    public void load() {
        String latest = MainComponent.PREFERENCES.get("LATEST", null);
        if (latest != null) {
            getDataSources().clear();
            try {
                XmlDataSourceRef.PARSER.parseChars(latest).forEach(getDataSources()::addElement);
            } catch (Exception ex) {
                reportException(ex);
            }
        }
    }

    public void store() {
        try {
            PREFERENCES.put("LATEST", XmlDataSourceRef.FORMATTER.formatToString(JLists.asList(getDataSources())));
        } catch (Exception ex) {
            reportException(ex);
        }
    }

    private static void reportException(Exception ex) {
        ExceptionPanel panel = new ExceptionPanel();
        panel.setPreferredSize(new Dimension(500, 300));
        panel.setException(ex);
        JOptionPane.showMessageDialog(null, panel, ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
    }

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(MainComponent.class).node(MainComponent.class.getSimpleName());
}
