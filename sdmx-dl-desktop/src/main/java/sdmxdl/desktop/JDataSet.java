package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import internal.sdmxdl.desktop.util.BrowseCommand;
import internal.sdmxdl.desktop.util.ButtonBuilder;
import internal.sdmxdl.desktop.util.NoOpCommand;
import lombok.Getter;
import sdmxdl.DataRepository;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;

public final class JDataSet extends JComponent implements HasModel<DataSetRef> {

    @Getter
    private DataSetRef model;

    public void setModel(DataSetRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JSeriesDataPanel dataPanel = new JSeriesDataPanel();

    private final JSeriesMetaPanel metaPanel = new JSeriesMetaPanel();

    private final JTable idTable = new JTable();

    private final JValueAsText<DataRepository> dsdTextArea = new JValueAsText<>();

    public JDataSet() {
        initComponents();
    }

    private void initComponents() {
        JToolBar contentToolBar = new JToolBar();
        contentToolBar.add(Box.createHorizontalGlue());

        contentToolBar.add(new ButtonBuilder()
                .action(BrowseCommand.ofURL(JDataSet::getWebsite)
                        .toAction(this)
                        .withWeakPropertyChangeListener(this, MODEL_PROPERTY))
                .ikon(MDI_WEB)
                .toolTipText("Open web site")
                .build());

        contentToolBar.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(this))
                .ikon(MDI_EXPORT)
                .toolTipText("Export")
                .build());

        contentToolBar.add(new ButtonBuilder()
                .action(NoOpCommand.INSTANCE.toAction(this))
                .ikon(MDI_REFRESH)
                .toolTipText("Refresh")
                .build());

        JTabbedPane content = new JTabbedPane();
        content.addTab("Data", dataPanel);
        content.addTab("Meta", metaPanel);
        content.addTab("ID", new JScrollPane(idTable));
        content.addTab("DSD", dsdTextArea);
        content.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, contentToolBar);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, content);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        reportLoading();
        new SwingWorker<SingleSeries, Void>() {
            @Override
            protected SingleSeries doInBackground() throws Exception {
                return SingleSeries.load(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(), model);
            }

            @Override
            protected void done() {
                try {
                    displayData(get());
                } catch (InterruptedException | ExecutionException ex) {
                    reportError(ex);
                }
            }
        }.execute();
    }

    private void reportLoading() {
        // TODO
    }

    private void displayData(SingleSeries item) {
        dataPanel.setModel(item);
        metaPanel.setModel(item);
        idTable.setModel(asIdTableModel(item));
        dsdTextArea.setModel(DataRepository.builder().structure(item.getDsd()).build());
    }

    private void reportError(Exception ex) {
        ex.printStackTrace();
        // TODO
    }

    private TableModel asIdTableModel(SingleSeries item) {
        DefaultTableModel result = new DefaultTableModel();
        result.addColumn("Name");
        result.addColumn("Value");
        result.addRow(new Object[]{"Source", getModel().getDataSourceRef().getSource()});
        result.addRow(new Object[]{"Flow", getModel().getDataSourceRef().getFlow()});
        result.addRow(new Object[]{"Key", item.getSeries().getKey()});
        return result;
    }

    private static URL getWebsite(JDataSet c) {
        DataSetRef model = c.getModel();
        if (model == null) return null;
        WebSource source = Sdmxdl.INSTANCE.getSdmxManager().getSources().get(model.getDataSourceRef().getSource());
        if (source == null) return null;
        return source.getWebsite();
    }
}
