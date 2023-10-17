package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.completion.swing.JAutoCompletion;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.*;
import lombok.NonNull;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import nbbrd.io.Resource;
import net.miginfocom.swing.MigLayout;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.Network;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static internal.sdmxdl.desktop.Actions.onActionPerformed;
import static internal.sdmxdl.desktop.Html4Swing.nameDescription;
import static internal.sdmxdl.desktop.MouseListeners.onDoubleClick;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;

public final class MainComponent extends JComponent implements HasSdmxProperties<SdmxWebManager> {

    @lombok.Getter
    private SdmxWebManager sdmxManager = SdmxWebManager.noOp();

    public void setSdmxManager(@NonNull SdmxWebManager sdmxManager) {
        firePropertyChange(SDMX_MANAGER_PROPERTY, this.sdmxManager, this.sdmxManager = sdmxManager);
    }

    @lombok.Getter
    private Languages languages = Languages.ANY;

    public void setLanguages(@NonNull Languages languages) {
        firePropertyChange(LANGUAGES_PROPERTY, this.languages, this.languages = languages);
    }

    public static final String DATA_SOURCES_PROPERTY = "dataSources";

    @lombok.Getter
    private DefaultListModel<DataSourceRef> dataSources = new DefaultListModel<>();

    public void setDataSources(@NonNull DefaultListModel<DataSourceRef> dataSources) {
        firePropertyChange(DATA_SOURCES_PROPERTY, this.dataSources, this.dataSources = dataSources);
    }

    private final JTree datasetsTree = new JTree();

    private final JList<WebSource> sourcesList = new JList<>();

    private final JList<Driver> driversList = new JList<>();

    private final JEditorTabs main = new JEditorTabs();

    private final JSplitPane splitPane = new JSplitPane();

    private final ListDataListener dataSourcesListener = JLists.dataListenerOf(this::contentsChanged);

    private final FaviconSupport faviconSupport = FaviconSupport.ofServiceLoader()
            .toBuilder()
            .client(this::openConnection)
            .build();

    private final ImageIcon sdmxIcon = new ImageIcon(loadImage());

    private URLConnection openConnection(URL url) throws IOException {
        try {
            WebSource source = WebSource.builder().id("").driver("").endpoint(url.toURI()).build();
            Network network = getSdmxManager().getNetworking().getNetwork(source, null, null);
            return network.getURLConnectionFactory().openConnection(url, Proxy.NO_PROXY);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private Image loadImage() {
        try (InputStream stream = Resource.newInputStream(MainComponent.class, "sdmx-logo.png")) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private final Map<FlowRef, FlowStruct> flowStructs = new HashMap<>();

    public MainComponent() {
        initComponent();
    }

    private Icon getDataSourceIcon(DataSourceRef dataSourceRef, Runnable onUpdate) {
        return getDataSourceIcon(dataSourceRef.getSource(), 16, onUpdate);
    }

    private Icon getDataSourceIcon(String source, int size, Runnable onUpdate) {
        return getDataSourceIcon(getSdmxManager().getSources().get(source), size, onUpdate);
    }

    private Icon getDataSourceIcon(WebSource source, int size, Runnable onUpdate) {
        URL website = source.getWebsite();
        return website != null ? faviconSupport.getOrDefault(FaviconRef.of(DomainName.of(website), size), onUpdate, sdmxIcon) : sdmxIcon;
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
                        label.setIcon(getDataSourceIcon(ref, tree::repaint));
                    } else if (userObject instanceof DataSetRef) {
                        DataSetRef ref = (DataSetRef) userObject;
                        FlowStruct fs = getFlowStruct(ref.getDataSourceRef());
                        label.setText(DataSetRefFormats.toText(ref, fs));
                        label.setToolTipText(DataSetRefFormats.toTooltipText(ref, fs));
                        if (ref.getKey().isSeries()) {
                            label.setIcon(FontIcon.of(MaterialDesign.MDI_CHART_LINE, 16, UIManager.getColor("Tree.icon.leafColor")));
                        } else {
                            label.setIcon(UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon"));
                        }
                    } else if (userObject instanceof SwingWorker) {
                        label.setText("Loading");
                        label.setToolTipText(null);
                        label.setIcon(FontIcon.of(MaterialDesign.MDI_CLOUD_DOWNLOAD, 16, UIManager.getColor("Tree.icon.leafColor")));
                    } else if (userObject instanceof Exception) {
                        label.setText("Error " + userObject.getClass().getSimpleName());
                        label.setToolTipText(((Exception) userObject).getMessage());
                        label.setIcon(FontIcon.of(MaterialDesign.MDI_CLOSE_NETWORK, 16, UIManager.getColor("Tree.icon.leafColor")));
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
        DynamicTree.enable(datasetsTree, new DataNodeFactory(this::getSdmxManager, this::getLanguages), new DefaultMutableTreeNode("root"));
        datasetsTree.addMouseListener(onDoubleClick(this::openCurrentDataSetRef));
        datasetsTree.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        datasetsTree.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentDataSetRef));

        sourcesList.setCellRenderer(JLists.cellRendererOf((label, value) -> {
            label.setText(nameDescription(value.getId(), value.getName(Languages.ANY)).render());
            label.setIcon(getDataSourceIcon(value.getId(), 24, sourcesList::repaint));
            label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }));
        sourcesList.addMouseListener(onDoubleClick(this::openCurrentSource));
        sourcesList.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        sourcesList.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentSource));

        driversList.setCellRenderer(JLists.cellRendererOf((label, value) -> {
            label.setText(value.getDriverId());
            label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }));
        driversList.addMouseListener(onDoubleClick(this::openCurrentDriver));
        driversList.getInputMap().put(getKeyStroke(VK_ENTER, 0), "SELECT_ACTION");
        driversList.getActionMap().put("SELECT_ACTION", onActionPerformed(this::openCurrentDriver));

        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setRightComponent(main);
        splitPane.setResizeWeight(.3);

        JToolWindowBar toolWindowBar = new JToolWindowBar(splitPane);
        addTool(toolWindowBar, "Datasets", MDI_FILE_TREE, new JScrollPane(datasetsTree), newDatasetsToolBar());
        addTool(toolWindowBar, "Sources", MDI_SERVER_NETWORK, new JScrollPane(sourcesList), new JToolBar());
        addTool(toolWindowBar, "Drivers", MDI_CHIP, new JScrollPane(driversList), new JToolBar());

        setLayout(new BorderLayout());
        add(toolWindowBar, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);

        dataSources.addListDataListener(dataSourcesListener);

        addPropertyChangeListener(DATA_SOURCES_PROPERTY, this::onDataSourcesChange);
        addPropertyChangeListener(SDMX_MANAGER_PROPERTY, this::onSdmxWebManagerChange);
    }

    private void addTool(JToolWindowBar toolToolBar, String name, Ikon ikon, Component component, JToolBar toolBar) {
        JTabbedPane tool = new JTabbedPane();
        tool.addTab(name, component);
        tool.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
        toolToolBar.addToolWindow(name, FontIcon.of(ikon, 16, UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key)), tool);
    }

    private JToolBar newDatasetsToolBar() {
        JToolBar result = new JToolBar();
        result.add(Box.createHorizontalGlue());

        result.add(new ButtonBuilder()
                .action(new AddDatasetCommand().toAction(this))
                .ikon(MDI_DATABASE_PLUS)
                .toolTipText("Add dataset")
                .buildButton());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(datasetsTree))
                .ikon(MDI_UNFOLD_MORE)
                .toolTipText("Expand All")
                .buildButton());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(datasetsTree))
                .ikon(MDI_UNFOLD_LESS)
                .toolTipText("Collapse All")
                .buildButton());

        result.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(datasetsTree))
                .ikon(MDI_MENU)
                .toolTipText("Options")
                .buildButton());

        return result;
    }

    private void openCurrentDataSetRef() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) datasetsTree.getLastSelectedPathComponent();
        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject instanceof DataSetRef) {
                DataSetRef dataSetRef = (DataSetRef) userObject;
                if (dataSetRef.getKey().isSeries()) {
                    main.addIfAbsent(dataSetRef,
                            o -> "<html>" + o.getDataSourceRef().getFlow().getId() + "/" + o.getKey(),
                            o -> getDataSourceIcon(o.getDataSourceRef(), main::repaint),
                            o -> {
                                JDataSet result = new JDataSet();
                                result.setSdmxManager(sdmxManager);
                                result.setModel(o);
                                return result;
                            });
                }
            }
        }
    }

    private void openCurrentSource() {
        WebSource source = sourcesList.getSelectedValue();
        if (source != null) {
            main.addIfAbsent(source,
                    o -> "<html>" + o.getId(),
                    o -> getDataSourceIcon(o, 16, main::repaint),
                    o -> {
                        JWebSource result = new JWebSource();
                        result.setModel(o);
                        return result;
                    });
        }
    }

    private void openCurrentDriver() {
        Driver driver = driversList.getSelectedValue();
        if (driver != null) {
            main.addIfAbsent(driver,
                    o -> "<html>" + o.getDriverId(),
                    o -> null,
                    o -> {
                        JDriver result = new JDriver();
                        result.setModel(o);
                        return result;
                    });
        }
    }

    private void onSdmxWebManagerChange(PropertyChangeEvent evt) {
        sourcesList.setModel(JLists.modelOf(getSdmxManager().getSources().values().stream().filter(o -> !o.isAlias()).collect(toList())));
        driversList.setModel(JLists.modelOf(getSdmxManager().getDrivers()));
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
                    publish(FlowStruct.load(getSdmxManager(), getLanguages(), dataSourceRef));
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

    private static final class AddDatasetCommand extends JCommand<MainComponent> {
        @Override
        public void execute(@NonNull MainComponent c) {

            JTextField sourceField = new JTextField("");
            SdmxAutoCompletion sourceCompletion = SdmxAutoCompletion.onWebSource(c.getSdmxManager(), c.getLanguages());
            JAutoCompletion sourceAutoCompletion = new JAutoCompletion(sourceField);
            sourceAutoCompletion.setSource(sourceCompletion.getSource());
            sourceAutoCompletion.getList().setCellRenderer(sourceCompletion.getRenderer());

            JTextField flowField = new JTextField("");
            SdmxAutoCompletion flowCompletion = SdmxAutoCompletion.onDataflow(c.getSdmxManager(), c.getLanguages(), () -> c.getSdmxManager().getSources().get(sourceField.getText()), new ConcurrentHashMap<>());
            JAutoCompletion flowAutoCompletion = new JAutoCompletion(flowField);
            flowAutoCompletion.setSource(flowCompletion.getSource());
            flowAutoCompletion.getList().setCellRenderer(flowCompletion.getRenderer());

            JTextField dimensionsField = new JTextField("");
            dimensionsField.setEnabled(false);

            JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

            addSeparator(panel, "Source");

            panel.add(new JLabel("Provider"), "skip");
            panel.add(sourceField, "span, growx");

            panel.add(new JLabel("Dataflow"), "skip");
            panel.add(flowField, "span, growx");

            addSeparator(panel, "Options");

            panel.add(new JLabel("Dimensions"), "skip");
            panel.add(dimensionsField, "span, growx");

            if (JOptionPane.showOptionDialog(c, panel, "Add dataset",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new Object[]{"Add", "Cancel"}, "Add") == 0) {

                c.getDataSources().addElement(new DataSourceRef(sourceField.getText(), FlowRef.parse(flowField.getText()), emptyList()));
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
}
