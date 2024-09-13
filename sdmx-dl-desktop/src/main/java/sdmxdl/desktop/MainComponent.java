package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.completion.swing.JAutoCompletion;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.DataSetRefFormats;
import internal.sdmxdl.desktop.DataSourceRefFormats;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.*;
import lombok.NonNull;
import net.miginfocom.swing.MigLayout;
import org.kordamp.ikonli.Ikon;
import sdmxdl.FlowRef;
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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static internal.sdmxdl.desktop.Collectors2.getSingle;
import static internal.sdmxdl.desktop.util.Actions.onActionPerformed;
import static internal.sdmxdl.desktop.util.JTrees.toDefaultMutableTreeNode;
import static internal.sdmxdl.desktop.util.MouseListeners.onDoubleClick;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;
import static sdmxdl.desktop.Renderer.*;
import static sdmxdl.desktop.Sdmxdl.lookupWebSource;

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

    private final Map<FlowRef, FlowStruct> flowStructs = new HashMap<>();

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
                        DataSourceRef ref = (DataSourceRef) userObject;
                        FlowStruct fs = getFlowStruct(ref);
                        label.setText(DataSourceRefFormats.toText(ref, fs));
                        label.setToolTipText(DataSourceRefFormats.toTooltipText(ref, fs));
                        label.setIcon(Sdmxdl.INSTANCE.getIconSupport().getIcon(ref, 16, tree::repaint));
                    } else if (userObject instanceof DataSetRef) {
                        DataSetRef ref = (DataSetRef) userObject;
                        FlowStruct fs = getFlowStruct(ref.getDataSourceRef());
                        label.setText(DataSetRefFormats.toText(ref, fs));
                        label.setToolTipText(DataSetRefFormats.toTooltipText(ref, fs));
                        if (ref.getKey().isSeries()) {
                            label.setIcon(Ikons.of(MDI_CHART_LINE, 16, Renderer.getColor(lookupWebSource(ref).getConfidentiality())));
                        } else {
                            label.setIcon(UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon"));
                        }
                    } else if (userObject instanceof SwingWorker) {
                        SWING_WORKER_RENDERER.render(label, (SwingWorker<?, ?>) userObject);
                        label.setToolTipText(null);
                    } else if (userObject instanceof Exception) {
                        ERROR_RENDERER.render(label, (Exception) userObject);
                        label.setToolTipText(((Exception) userObject).getMessage());
                    }
                }
                return label;
            }

            private FlowStruct getFlowStruct(DataSourceRef ref) {
                return flowStructs.entrySet()
                        .stream()
                        .filter(entry -> ref.getFlow().contains(entry.getKey()))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(null);
            }
        });
        DynamicTree.enable(datasetsTree, new DataNodeFactory(Sdmxdl.INSTANCE::getSdmxManager), new DefaultMutableTreeNode("root"));
        datasetsTree.addMouseListener(onDoubleClick(this::openCurrentDataSetRef));
        datasetsTree.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        datasetsTree.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentDataSetRef));
        datasetsTree.setComponentPopupMenu(newDatasetsMenu().getPopupMenu());

        sourcesList.setCellRenderer(WEB_SOURCE_RENDERER.asListCellRenderer(sourcesList::repaint));
        sourcesList.addMouseListener(onDoubleClick(e -> sourcesList.getActionMap().get("SELECT_ACTION").actionPerformed(null)));
        sourcesList.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        sourcesList.getActionMap().put("SELECT_ACTION",
                new OpenCurrentSourceCommand().toAction(this)
                        .withWeakListSelectionListener(sourcesList.getSelectionModel()));
        sourcesList.setComponentPopupMenu(newSourcesMenu().getPopupMenu());

        pluginsTree.setCellRenderer(JTrees.cellRendererOf(Object.class, this::renderPlugin));
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

        item = result.add(new AddDatasetCommand().toAction(this));
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
                .action(new AddDatasetCommand().toAction(this))
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
                    main.addIfAbsent(dataSetRef, DATA_SET_REF_RENDERER.asTabFactory());
                }
            }
        }
    }

    private void openCurrentPlugin() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) pluginsTree.getLastSelectedPathComponent();
        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject instanceof Driver) {
                main.addIfAbsent((Driver) userObject, DRIVER_RENDERER.asTabFactory());
            } else if (userObject instanceof Authenticator) {
                main.addIfAbsent((Authenticator) userObject, AUTHENTICATOR_RENDERER.asTabFactory());
            } else if (userObject instanceof Monitor) {
                main.addIfAbsent((Monitor) userObject, MONITOR_RENDERER.asTabFactory());
            } else if (userObject instanceof Persistence) {
                main.addIfAbsent((Persistence) userObject, PERSISTENCE_RENDERER.asTabFactory());
            } else if (userObject instanceof Registry) {
                main.addIfAbsent((Registry) userObject, REGISTRY_RENDERER.asTabFactory());
            } else if (userObject instanceof WebCaching) {
                main.addIfAbsent((WebCaching) userObject, WEB_CACHING_RENDERER.asTabFactory());
            } else if (userObject instanceof Networking) {
                main.addIfAbsent((Networking) userObject, NETWORKING_RENDERER.asTabFactory());
            }
        }
    }

    private void renderPlugin(JLabel label, Object value) {
        if (value instanceof Driver) {
            DRIVER_RENDERER.render(label, (Driver) value);
        } else if (value instanceof Authenticator) {
            AUTHENTICATOR_RENDERER.render(label, (Authenticator) value);
        } else if (value instanceof Monitor) {
            MONITOR_RENDERER.render(label, (Monitor) value);
        } else if (value instanceof Persistence) {
            PERSISTENCE_RENDERER.render(label, (Persistence) value);
        } else if (value instanceof Registry) {
            REGISTRY_RENDERER.render(label, (Registry) value);
        } else if (value instanceof WebCaching) {
            WEB_CACHING_RENDERER.render(label, (WebCaching) value);
        } else if (value instanceof Networking) {
            NETWORKING_RENDERER.render(label, (Networking) value);
        } else {
            label.setIcon(Ikons.of(MDI_CUBE_OUTLINE, 16, UIConstants.TREE_ICON_LEAF_COLOR));
        }
    }

    private void onSdmxWebManagerChange(PropertyChangeEvent evt) {
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
        flowStructs.clear();
        new SwingWorker<Void, FlowStruct>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (DataSourceRef dataSourceRef : JLists.asList(dataSources)) {
                    publish(FlowStruct.load(Sdmxdl.INSTANCE.getSdmxManager(), dataSourceRef));
                }
                return null;
            }

            @Override
            protected void process(List<FlowStruct> chunks) {
                chunks.forEach(chunk -> flowStructs.put(chunk.getFlow().getRef(), chunk));
                datasetsTree.repaint();
            }
        }.execute();
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
                    WEB_SOURCE_RENDERER.asTabFactory()
            );
        }
    }

    private static final class AddDatasetCommand extends JCommand<MainComponent> {
        @Override
        public void execute(@NonNull MainComponent c) {

            JTextField sourceField = new JTextField("");
            SdmxAutoCompletion sourceCompletion = SdmxAutoCompletion.onWebSource(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages());
            JAutoCompletion sourceAutoCompletion = new JAutoCompletion(sourceField);
            sourceAutoCompletion.setSource(sourceCompletion.getSource());
            sourceAutoCompletion.getList().setCellRenderer(sourceCompletion.getRenderer());

            JTextField catalogField = new JTextField("");
            SdmxAutoCompletion catalogCompletion = SdmxAutoCompletion.onCatalog(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                    () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                    new ConcurrentHashMap<>());
            JAutoCompletion catalogAutoCompletion = new JAutoCompletion(catalogField);
            catalogAutoCompletion.setSource(catalogCompletion.getSource());
            catalogAutoCompletion.getList().setCellRenderer(catalogCompletion.getRenderer());

            JTextField flowField = new JTextField("");
            SdmxAutoCompletion flowCompletion = SdmxAutoCompletion.onDataflow(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                    () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                    () -> catalogField.getText(),
                    new ConcurrentHashMap<>());
            JAutoCompletion flowAutoCompletion = new JAutoCompletion(flowField);
            flowAutoCompletion.setSource(flowCompletion.getSource());
            flowAutoCompletion.getList().setCellRenderer(flowCompletion.getRenderer());

            JTextField dimensionsField = new JTextField("");
            dimensionsField.setEnabled(false);

            JTextField languagesField = new JTextField(Sdmxdl.INSTANCE.getLanguages().toString());
            languagesField.setEnabled(false);

            JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

            addSeparator(panel, "Source");

            panel.add(new JLabel("Provider"), "skip");
            panel.add(sourceField, "span, growx");

            panel.add(new JLabel("Catalog"), "skip");
            panel.add(catalogField, "span, growx");

            panel.add(new JLabel("Dataflow"), "skip");
            panel.add(flowField, "span, growx");

            addSeparator(panel, "Options");

            panel.add(new JLabel("Dimensions"), "skip");
            panel.add(dimensionsField, "span, growx");

            panel.add(new JLabel("Languages"), "skip");
            panel.add(languagesField, "span, growx");

            if (JOptionPane.showOptionDialog(c, panel, "Add dataset",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new Object[]{"Add", "Cancel"}, "Add") == 0) {

                c.getDataSources().addElement(new DataSourceRef(sourceField.getText(), catalogField.getText(), FlowRef.parse(flowField.getText()), emptyList(), Sdmxdl.INSTANCE.getLanguages()));
            }
        }

        static final Color LABEL_COLOR = new Color(0, 70, 213);

        private void addSeparator(JPanel panel, String text) {
            JLabel l = new JLabel(text);
            l.setForeground(LABEL_COLOR);

            panel.add(l, "gapbottom 1, span, split 2, aligny center");
            panel.add(new JSeparator(), "gapleft rel, growx");
        }
    }

    private static URL getSelectedWebsite(JList<WebSource> x) {
        return getSingle(x.getSelectedValuesList()).map(WebSource::getWebsite).orElse(null);
    }

    private static URL getSelectedMonitorWebsite(JList<WebSource> x) {
        return getSingle(x.getSelectedValuesList()).map(WebSource::getMonitorWebsite).orElse(null);
    }
}
