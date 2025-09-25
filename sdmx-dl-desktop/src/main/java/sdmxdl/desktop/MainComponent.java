package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.SdmxCommand;
import internal.sdmxdl.desktop.SdmxUri;
import internal.sdmxdl.desktop.XmlDataSetRef;
import internal.sdmxdl.desktop.XmlDataSourceRef;
import internal.sdmxdl.desktop.util.*;
import lombok.NonNull;
import nbbrd.io.function.IOBiConsumer;
import org.kordamp.ikonli.Ikon;
import sdmxdl.FlowRequest;
import sdmxdl.KeyRequest;
import sdmxdl.desktop.panels.*;
import sdmxdl.ext.Persistence;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.provider.ri.drivers.RiHttpUtils;
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
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static internal.sdmxdl.desktop.Collectors2.getSingle;
import static internal.sdmxdl.desktop.util.Actions.hideWhenDisabled;
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

    public static final String SELECTED_DATA_REF_PROPERTY = "selectedDataRef";

    @lombok.Getter
    private Object selectedDataRef = null;

    private void setSelectedDataRef(Object selectedDataRef) {
        firePropertyChange(SELECTED_DATA_REF_PROPERTY, this.selectedDataRef, this.selectedDataRef = selectedDataRef);
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
        datasetsTree.getSelectionModel().addTreeSelectionListener(ignore -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) datasetsTree.getLastSelectedPathComponent();
            if (node != null) {
                setSelectedDataRef(node.getUserObject());
            }
        });

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

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::editDataSource).build().toAction(this)));
        item.setText("Edit");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::duplicateDataSource).build().toAction(this)));
        item.setText("Duplicate");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::removeDataSource).build().toAction(this)));
        item.setText("Remove");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::openWebsite).predicate(MainComponent::hasWebsite).build().toAction(this)));
        item.setText("Open website");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::openMonitor).predicate(MainComponent::hasMonitor).build().toAction(this)));
        item.setText("Open monitor");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::copyPath).build().toAction(this)));
        item.setText("Copy Path/Reference...");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSourceRef.class).execution(MainComponent::debug).predicate(MainComponent::isDebug).build().toAction(this)));
        item.setText("Debug...");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSetRef.class).execution(MainComponent::openDataSet).predicate((c, ref) -> ref.getKey().isSeries()).build().toAction(this)));
        item.setText("<html><b>Open");

        item = hideWhenDisabled(result.add(DataRefCommand.of(DataSetRef.class).execution(MainComponent::copyPath).build().toAction(this)));
        item.setText("Copy Path/Reference...");

        item = hideWhenDisabled(result.add(DataRefCommand.of(Exception.class).execution(MainComponent::openException).build().toAction(this)));
        item.setText("<html><b>Open");

        item = hideWhenDisabled(result.add(DataRefCommand.of(Exception.class).execution(MainComponent::copyException).build().toAction(this)));
        item.setText("Copy");

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
        Object userObject = getSelectedDataRef();
        if (userObject instanceof DataSetRef) {
            DataSetRef dataSetRef = (DataSetRef) userObject;
            if (dataSetRef.getKey().isSeries()) {
                openDataSet(dataSetRef);
            }
        } else if (userObject instanceof Exception) {
            openException((Exception) userObject);
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
        public void execute(@NonNull MainComponent component) {
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

    @lombok.Builder
    private static final class DataRefCommand<T> extends JCommand<MainComponent> {

        public static <T> Builder<T> of(Class<T> type) {
            return DataRefCommand.<T>builder().type(type);
        }

        private final Class<T> type;

        @lombok.Builder.Default
        private final IOBiConsumer<MainComponent, T> execution = DataRefCommand::noExecution;

        @lombok.Builder.Default
        private final BiPredicate<MainComponent, T> predicate = DataRefCommand::noPredicate;

        @Override
        public boolean isEnabled(@NonNull MainComponent c) {
            if (!type.isInstance(c.getSelectedDataRef())) return false;
            return predicate.test(c, type.cast(c.getSelectedDataRef()));
        }

        @Override
        public void execute(@NonNull MainComponent c) throws IOException {
            execution.acceptWithIO(c, type.cast(c.getSelectedDataRef()));
        }

        @Override
        public JCommand<MainComponent>.@NonNull ActionAdapter toAction(@NonNull MainComponent c) {
            return super.toAction(c).withWeakPropertyChangeListener(c, SELECTED_DATA_REF_PROPERTY);
        }

        private static void noExecution(MainComponent c, Object ref) {
        }

        private static boolean noPredicate(MainComponent c, Object ref) {
            return true;
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
        if (JOptionPane.showOptionDialog(this, panel, "Add datasource",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Add", "Cancel"}, "Add") == 0) {
            getDataSources().addElement(panel.getModel());
        }
    }

    public void removeDataSource(DataSourceRef base) {
        if (JOptionPane.showConfirmDialog(this, "Remove datasource ?") == 0) {
            getDataSources().removeElement(base);
        }
    }

    public void editDataSource(DataSourceRef base) {
        DataSourceRefPanel panel = new DataSourceRefPanel();
        panel.setModel(base);
        if (JOptionPane.showOptionDialog(this, panel, "Edit datasource",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Edit", "Cancel"}, "Add") == 0) {
            getDataSources().removeElement(base);
            getDataSources().addElement(panel.getModel());
        }
    }

    public void duplicateDataSource(DataSourceRef base) {
        DataSourceRefPanel panel = new DataSourceRefPanel();
        panel.setModel(base);
        if (JOptionPane.showOptionDialog(this, panel, "Duplicate datasource",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Duplicate", "Cancel"}, "Add") == 0) {
            getDataSources().addElement(panel.getModel());
        }
    }

    private void openDataSet(DataSetRef dataSetRef) {
        main.addIfAbsent(dataSetRef, DataSetRefRenderer.INSTANCE.asTabFactory(this));
    }

    private void openException(Exception userObject) {
        main.addIfAbsent(userObject, ExceptionRenderer.INSTANCE.asTabFactory(this));
    }

    private void copyException(Exception userObject) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(userObject.toString()), null);
    }

    private boolean hasWebsite(DataSourceRef ref) {
        WebSource source = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        return source != null && source.getWebsite() != null;
    }

    private void openWebsite(DataSourceRef ref) throws IOException {
        WebSource source = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        if (source != null && source.getWebsite() != null) {
            try {
                Desktop.getDesktop().browse(source.getWebsite().toURI());
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }

    private boolean hasMonitor(DataSourceRef ref) {
        WebSource source = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        return source != null && source.getMonitorWebsite() != null;
    }

    private void openMonitor(DataSourceRef ref) throws IOException {
        WebSource source = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        if (source != null && source.getMonitorWebsite() != null) {
            try {
                Desktop.getDesktop().browse(source.getMonitorWebsite().toURI());
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }

    private boolean isDebug(DataSourceRef ref) {
        return ref.isDebug();
    }

    private void debug(DataSourceRef ref) {
        WebSource source = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        new OnDemandMenuBuilder()
                .openFolder("Open cache folder", RiCaching.CACHE_FOLDER_PROPERTY.get(source.getProperties()))
                .openFolder("Open dump folder", RiHttpUtils.DUMP_FOLDER_PROPERTY.get(source.getProperties()))
                .showMenuAsPopup(this);
    }

    private void copyPath(DataSourceRef ref) {
        String source = ref.getSource();
        FlowRequest flowRequest = ref.toFlowRequest();
        new OnDemandMenuBuilder()
                .copyToClipboard("SDMX-DL URI", SdmxUri.fromFlowRequest(source, flowRequest).toString())
                .copyToClipboard("XML reference", XmlDataSourceRef.formatToString(ref))
                .addSeparator()
                .copyToClipboard("List dimensions command", SdmxCommand.listDimensions(source, flowRequest))
                .copyToClipboard("List attributes command", SdmxCommand.listAttributes(source, flowRequest))
                .copyToClipboard("Fetch all keys command", SdmxCommand.fetchKeys(source, KeyRequest.builderOf(flowRequest).build()))
                .showMenuAsPopup(this);
    }

    private void copyPath(DataSetRef ref) {
        String source = ref.getDataSourceRef().getSource();
        KeyRequest keyRequest = ref.toKeyRequest();
        new OnDemandMenuBuilder()
                .copyToClipboard("SDMX-DL URI", SdmxUri.fromKeyRequest(source, keyRequest).toString())
                .copyToClipboard("XML reference", XmlDataSetRef.formatToString(ref))
                .addSeparator()
                .copyToClipboard("Fetch data command", SdmxCommand.fetchData(source, keyRequest))
                .copyToClipboard("Fetch meta command", SdmxCommand.fetchMeta(source, keyRequest))
                .copyToClipboard("Fetch keys command", SdmxCommand.fetchKeys(source, keyRequest))
                .showMenuAsPopup(this);
    }

    public void load() {
        String latest = MainComponent.PREFERENCES.get("LATEST", null);
        if (latest != null) {
            getDataSources().clear();
            try {
                XmlDataSourceRef.LIST_PARSER.parseChars(latest).forEach(getDataSources()::addElement);
            } catch (Exception ex) {
                reportException(ex);
            }
        }
    }

    public void store() {
        try {
            PREFERENCES.put("LATEST", XmlDataSourceRef.LIST_FORMATTER.formatToString(JLists.asList(getDataSources())));
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
