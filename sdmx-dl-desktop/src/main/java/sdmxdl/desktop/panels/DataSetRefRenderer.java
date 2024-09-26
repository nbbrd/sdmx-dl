package sdmxdl.desktop.panels;

import internal.sdmxdl.desktop.util.*;
import j2html.tags.DomContent;
import j2html.tags.Text;
import sdmxdl.DataRepository;
import sdmxdl.Dimension;
import sdmxdl.desktop.*;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static j2html.TagCreator.*;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;

public enum DataSetRefRenderer implements Renderer<DataSetRef> {

    INSTANCE;

    @Override
    public String toText(DataSetRef value, Runnable onUpdate) {
        FlowStruct flowStruct = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(value.getDataSourceRef(), onUpdate);
        return flowStruct != null
                ? getDimension(value, flowStruct).getCodelist().getCodes().get(value.getKey().get(value.getDimensionIndex()))
                : getKeyText(value);
    }

    @Override
    public Icon toIcon(DataSetRef value, Runnable onUpdate) {
        return value.getKey().isSeries() ? Ikons.of(MDI_CHART_LINE, 16, WebSourceRenderer.getColor(value.getDataSourceRef().toWebSource(Sdmxdl.INSTANCE.getSdmxManager()).getConfidentiality())) : null;
        //UIManager.getIcon(expanded ? "Tree.openIcon" : "Tree.closedIcon")
    }

    @Override
    public String toTooltip(DataSetRef value, Runnable onUpdate) {
        FlowStruct flowStruct = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(value.getDataSourceRef(), onUpdate);
        return html(
                table(
                        tr(th("Key:").withStyle("text-align:right"), td(getKeyText(value))),
                        tr(th("Dimension:").withStyle("text-align:right"), td(htmlDimension(value, flowStruct)))
                )
        ).render();
    }

    @Override
    public String toHeaderText(DataSetRef value, Runnable onUpdate) {
        return value.getDataSourceRef().toFlowRef().getId() + "/" + value.getKey();
    }

    @Override
    public Icon toHeaderIcon(DataSetRef value, Runnable onUpdate) {
        return Sdmxdl.INSTANCE.getIconSupport().getIcon(value.getDataSourceRef(), 16, onUpdate);
    }

    @Override
    public JDocument<DataSetRef> toView(MainComponent main, DataSetRef value) {
        JDocument<DataSetRef> result = new JDocument<>();
        result.setModel(value);
        result.addComponent("...", new JLabel("loading"));

        result.addToolBarItem(new ButtonBuilder()
                .action(BrowseCommand.ofURL(DataSetRefRenderer::getWebsite)
                        .toAction(result)
                        .withWeakPropertyChangeListener(result, JDocument.MODEL_PROPERTY))
                .ikon(MDI_WEB)
                .toolTipText("Open web site")
                .build());

        result.addToolBarItem(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(result))
                .ikon(MDI_EXPORT)
                .toolTipText("Export")
                .build());

        result.addToolBarItem(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(result))
                .ikon(MDI_REFRESH)
                .toolTipText("Refresh")
                .build());

        new SwingWorker<SingleSeries, Void>() {
            @Override
            protected SingleSeries doInBackground() throws Exception {
                return SingleSeries.load(Sdmxdl.INSTANCE.getSdmxManager(), value, WebSourceRenderer.getColor(value.getDataSourceRef().toWebSource(Sdmxdl.INSTANCE.getSdmxManager()).getConfidentiality()));
            }

            @Override
            protected void done() {
                try {
                    SingleSeries singleSeries = get();
                    result.clearComponents();
                    DataPanel dataPanel = new DataPanel();
                    dataPanel.setModel(singleSeries);
                    result.addComponent("Data", dataPanel);
                    MetaPanel metaPanel = new MetaPanel();
                    metaPanel.setModel(singleSeries);
                    result.addComponent("Meta", metaPanel);
                    JTable idTable = new JTable();
                    idTable.setModel(asIdTableModel(value, singleSeries));
                    result.addComponent("ID", idTable);
                    JValueAsText<DataRepository> dsdTextArea = new JValueAsText<>();
                    dsdTextArea.setModel(DataRepository.builder().structure(singleSeries.getDsd()).build());
                    result.addComponent("DSD", dsdTextArea);
                } catch (InterruptedException | ExecutionException ex) {
//                    reportError(ex.getCause());
                }
            }
        }.execute();

        return result;
    }

    private TableModel asIdTableModel(DataSetRef ref, SingleSeries item) {
        DefaultTableModel result = new DefaultTableModel();
        result.addColumn("Name");
        result.addColumn("Value");
        result.addRow(new Object[]{"Source", ref.getDataSourceRef().getSource()});
        result.addRow(new Object[]{"Catalog", ref.getDataSourceRef().getCatalog()});
        result.addRow(new Object[]{"Flow", ref.getDataSourceRef().getFlow()});
        result.addRow(new Object[]{"Key", item.getSeries().getKey()});
        result.addRow(new Object[]{"Languages", ref.getDataSourceRef().getLanguages()});
        result.addRow(new Object[]{"Dimensions", ref.getDataSourceRef().getDimensions().toString()});
        return result;
    }

    private static URL getWebsite(JDocument<DataSetRef> c) {
        DataSetRef model = c.getModel();
        if (model == null) return null;
        WebSource source = model.getDataSourceRef().toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        if (source == null) return null;
        return source.getWebsite();
    }

    private static Dimension getDimension(DataSetRef ref, FlowStruct fs) {
        return fs.getStructure().getDimensionList().get(ref.getDimensionIndex());
    }

    private static String getKeyText(DataSetRef ref) {
        return ref.getKey().toString();
    }

    private static DomContent htmlDimension(DataSetRef ref, FlowStruct fs) {
        return fs != null
                ? htmlDimension(getDimension(ref, fs))
                : htmlDimension(ref);
    }

    private static DomContent htmlDimension(Dimension dimension) {
        return each(text(dimension.getId()), br(), text(dimension.getName()));
    }

    private static Text htmlDimension(DataSetRef ref) {
        return text(String.valueOf(ref.getDimensionIndex()));
    }
}
