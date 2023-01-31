package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import ec.util.list.swing.JLists;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.DynamicTree;
import lombok.NonNull;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import nbbrd.io.Resource;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;
import sdmxdl.DataflowRef;
import sdmxdl.ext.Registry;
import sdmxdl.web.SdmxWebManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public final class MainComponent extends JComponent implements HasSdmxProperties<SdmxWebManager> {

    @lombok.Getter
    private SdmxWebManager sdmxManager = SdmxWebManager.noOp();

    public void setSdmxManager(@NonNull SdmxWebManager sdmxManager) {
        firePropertyChange(SDMX_MANAGER_PROPERTY, this.sdmxManager, this.sdmxManager = sdmxManager);
    }

    @lombok.Getter
    private Registry registry = NO_OP_REGISTRY;

    public void setRegistry(@NonNull Registry registry) {
        firePropertyChange(REGISTRY_PROPERTY, this.registry, this.registry = registry);
    }

    public static final String DATA_SOURCES_PROPERTY = "dataSources";

    @lombok.Getter
    private DefaultListModel<DataSourceRef> dataSources = new DefaultListModel<>();

    public void setDataSources(@NonNull DefaultListModel<DataSourceRef> dataSources) {
        firePropertyChange(DATA_SOURCES_PROPERTY, this.dataSources, this.dataSources = dataSources);
    }

    private final JTree dataSets = new JTree();

    private final JTabbedPane main = new JTabbedPane();

    private final ListDataListener dataSourcesListener = JLists.dataListenerOf(this::contentsChanged);

    private final FaviconSupport faviconSupport = FaviconSupport.ofServiceLoader()
            .toBuilder()
            .client(url -> getSdmxManager().getNetwork().getURLConnectionFactory().openConnection(url, Proxy.NO_PROXY))
            .build();

    private final ImageIcon sdmxIcon = new ImageIcon(loadImage());

    private Image loadImage() {
        try (InputStream stream = Resource.getResourceAsStream(MainComponent.class, "sdmx-logo.png").orElseThrow(RuntimeException::new)) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private final Map<DataflowRef, FlowStruct> flowStructs = new HashMap<>();

    public MainComponent() {
        initComponent();
    }

    private Icon getDataSourceIcon(DataSourceRef dataSourceRef, Runnable onUpdate) {
        URL website = getSdmxManager().getSources().get(dataSourceRef.getSource()).getWebsite();
        return website != null ? faviconSupport.getOrDefault(FaviconRef.of(DomainName.of(website), 16), onUpdate, sdmxIcon) : sdmxIcon;
    }

    private void initComponent() {
        dataSets.setRootVisible(false);
        dataSets.setShowsRootHandles(true);
        dataSets.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof DataSourceRef) {
                        DataSourceRef dataSourceRef = (DataSourceRef) userObject;
                        FlowStruct fs = getFlowStruct(dataSourceRef);
                        label.setText(fs != null ? fs.getDataflow().getName() : dataSourceRef.getFlow().toString());
                        label.setToolTipText(null);
                        label.setIcon(getDataSourceIcon(dataSourceRef, tree::repaint));
                    } else if (userObject instanceof DataSetRef) {
                        DataSetRef dataSetRef = (DataSetRef) userObject;
                        FlowStruct fs = getFlowStruct(dataSetRef.getDataSourceRef());
                        label.setText(fs != null ? fs.getDataStructure().getDimensionList().get(dataSetRef.getDimensionIndex()).getCodelist().getCodes().get(dataSetRef.getKey().get(dataSetRef.getDimensionIndex())) : dataSetRef.getKey().toString());
                        label.setToolTipText(null);
                        if (dataSetRef.getKey().isSeries()) {
                            label.setIcon(FontIcon.of(MaterialDesign.MDI_CHART_LINE, 16, UIManager.getColor("Tree.icon.leafColor")));
                        } else {
                            label.setIcon(UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon"));
                        }
                    } else if (userObject instanceof SwingWorker) {
                        label.setText("Loading");
                        label.setToolTipText(null);
                        label.setIcon(FontIcon.of(MaterialDesign.MDI_CLOUD_DOWNLOAD, 16, UIManager.getColor("Tree.icon.leafColor")));
                    } else if (userObject instanceof Exception) {
                        label.setText("Error of type " + userObject.getClass());
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
        DynamicTree.enable(dataSets, new DataNodeFactory(this::getSdmxManager), new DefaultMutableTreeNode("root"));
        dataSets.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openCurrentDataSetRef();
                }
            }
        });

        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        dataSets.getInputMap().put(enterKeyStroke, "SELECT_ACTION");
        dataSets.getActionMap().put("SELECT_ACTION", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCurrentDataSetRef();
            }
        });


        main.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        main.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (IntConsumer) main::removeTabAt);

        JTabbedPane side = new JTabbedPane();
        JToolBar sideToolBar = new JToolBar();
        sideToolBar.add(Box.createHorizontalGlue());

        JButton expandButton = sideToolBar.add(new ExpandAllCommand().toAction(dataSets));
        expandButton.setIcon(FontIcon.of(MaterialDesign.MDI_UNFOLD_MORE, 16, UIManager.getColor("Button.disabledText")));
        expandButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_UNFOLD_MORE, 16, UIManager.getColor("Button.foreground")));
        expandButton.setToolTipText("Expand All");

        JButton collapseButton = sideToolBar.add(new CollapseAllCommand().toAction(dataSets));
        collapseButton.setIcon(FontIcon.of(MaterialDesign.MDI_UNFOLD_LESS, 16, UIManager.getColor("Button.disabledText")));
        collapseButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_UNFOLD_LESS, 16, UIManager.getColor("Button.foreground")));
        collapseButton.setToolTipText("Collapse All");

        JButton optionsButton = sideToolBar.add(new OptionsCommand().toAction(dataSets));
        optionsButton.setIcon(FontIcon.of(MaterialDesign.MDI_MENU, 16, UIManager.getColor("Button.disabledText")));
        optionsButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_MENU, 16, UIManager.getColor("Button.foreground")));
        optionsButton.setToolTipText("Options");

        side.addTab("Datasets", new JScrollPane(dataSets));
        side.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, sideToolBar);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, side, main);
        splitPane.setResizeWeight(.25);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        dataSources.addListDataListener(dataSourcesListener);

        addPropertyChangeListener(DATA_SOURCES_PROPERTY, this::onDataSourcesChange);
        addPropertyChangeListener(SDMX_MANAGER_PROPERTY, this::onSdmxWebManagerChange);
    }

    private void openCurrentDataSetRef() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) dataSets.getLastSelectedPathComponent();
        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject instanceof DataSetRef) {
                DataSetRef dataSetRef = (DataSetRef) userObject;
                if (dataSetRef.getKey().isSeries()) {
                    String title = "<html>" + dataSetRef.getDataSourceRef().getFlow().getId() + "/" + dataSetRef.getKey();
                    int index = main.indexOfTab(title);
                    if (index != -1) {
                        main.setSelectedIndex(index);
                    } else {
                        JDataSet result = new JDataSet();
                        result.setSdmxManager(sdmxManager);
                        result.setRegistry(registry);
                        result.setModel(dataSetRef);
                        main.addTab(title, getDataSourceIcon(dataSetRef.getDataSourceRef(), main::repaint), result);
                        main.setSelectedComponent(result);
                    }
                }
            }
        }
    }

    private void onSdmxWebManagerChange(PropertyChangeEvent evt) {

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
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dataSets.getModel().getRoot();
        root.removeAllChildren();
        JLists.stream(dataSources).forEach(dataSourceRef -> root.add(new DynamicTree.CustomNode(dataSourceRef, false)));
        dataSets.setModel(new DefaultTreeModel(root));
        flowStructs.clear();
        new SwingWorker<Void, FlowStruct>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (DataSourceRef dataSourceRef : JLists.asList(dataSources)) {
                    publish(FlowStruct.load(getSdmxManager(), dataSourceRef));
                }
                return null;
            }

            @Override
            protected void process(List<FlowStruct> chunks) {
                chunks.forEach(chunk -> flowStructs.put(chunk.getDataflow().getRef(), chunk));
                dataSets.repaint();
            }
        }.execute();
    }

    private static final class ExpandAllCommand extends JCommand<JTree> {

        @Override
        public void execute(@NonNull JTree component) {
        }
    }

    private static final class CollapseAllCommand extends JCommand<JTree> {

        @Override
        public void execute(@NonNull JTree component) {
        }
    }

    private static final class OptionsCommand extends JCommand<JTree> {

        @Override
        public void execute(@NonNull JTree component) {
        }
    }
}
