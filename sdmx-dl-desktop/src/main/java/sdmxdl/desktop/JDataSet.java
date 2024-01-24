package sdmxdl.desktop;

import com.formdev.flatlaf.FlatClientProperties;
import ec.util.chart.ObsFunction;
import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.GridModel;
import ec.util.grid.swing.JGrid;
import ec.util.table.swing.JTables;
import internal.sdmxdl.desktop.ObsFormats;
import internal.sdmxdl.desktop.SeriesMetaFormats;
import internal.sdmxdl.desktop.util.*;
import lombok.Getter;
import lombok.NonNull;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import sdmxdl.Attribute;
import sdmxdl.Languages;
import sdmxdl.Obs;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.*;

public final class JDataSet extends JComponent implements HasSdmxProperties<SdmxWebManager> {

    @Getter
    private SdmxWebManager sdmxManager = SdmxWebManager.noOp();

    public void setSdmxManager(@NonNull SdmxWebManager sdmxManager) {
        firePropertyChange(SDMX_MANAGER_PROPERTY, this.sdmxManager, this.sdmxManager = sdmxManager);
    }

    @Getter
    private Languages languages = Languages.ANY;

    public void setLanguages(@NonNull Languages languages) {
        firePropertyChange(LANGUAGES_PROPERTY, this.languages, this.languages = languages);
    }

    public static final String MODEL_PROPERTY = "model";

    @Getter
    private DataSetRef model;

    public void setModel(DataSetRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JGrid grid = new JGrid();

    private final JTimeSeriesChart chart = new JTimeSeriesChart();

    private final JTable metaTable = new JTable();

    private final JTable idTable = new JTable();

//    private final CustomTooltip customTooltip = new CustomTooltip();

    public JDataSet() {
        initComponents();
    }

    private void initComponents() {
        grid.setPreferredSize(new Dimension(350, 10));
        grid.setRowSelectionAllowed(true);
        grid.setColumnSelectionAllowed(true);

        chart.setPreferredSize(new Dimension(350, 10));
        chart.setElementVisible(TimeSeriesChart.Element.LEGEND, false);
        chart.setElementVisible(TimeSeriesChart.Element.CROSSHAIR, true);
        chart.setElementVisible(TimeSeriesChart.Element.TOOLTIP, true);
        chart.setCrosshairOrientation(TimeSeriesChart.CrosshairOrientation.BOTH);
        chart.setCrosshairTrigger(TimeSeriesChart.DisplayTrigger.SELECTION);
        chart.setLineThickness(2);

//        customTooltip.enable(chart);
//        customTooltip.setEnabled(true);

        chart.setColorSchemeSupport(SwingColorSchemeSupport.from(new SystemLafColorScheme()));

        GridChart.enableSync(grid, chart);

        JSplitPane dataPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grid, chart);
        dataPane.setResizeWeight(.3);

        JScrollPane metaPane = new JScrollPane(metaTable);

        JScrollPane idPane = new JScrollPane(idTable);

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
        content.addTab("Data", dataPane);
        content.addTab("Meta", metaPane);
        content.addTab("ID", idPane);
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
                return SingleSeries.load(getSdmxManager(), getLanguages(), model);
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
        grid.setModel(asGridModel(item));
        grid.setDefaultRenderer(Object.class, new CustomCellRenderer(grid, item));
        grid.setCornerRenderer(JTables.cellRendererOf((label, value) -> {
            label.setText(item.getDuration() != null ? item.getDuration().toString() : null);
            label.setHorizontalAlignment(JLabel.CENTER);
        }));
        chart.setDataset(asChartModel(item));
        chart.setTitle(item.getMeta().getName());
        chart.setObsFormatter(asObsFunction(item));
        chart.setPeriodFormat(SeriesMetaFormats.getDateFormat(item.getMeta()));
        chart.putClientProperty("fixme_item", item);
        metaTable.setModel(asMetaTableModel(item));
        idTable.setModel(asIdTableModel(item));
    }

    private void reportError(Exception ex) {
        ex.printStackTrace();
        // TODO
    }

    private GridModel asGridModel(SingleSeries item) {
        List<Obs> data = item.getSeries().getObsList();
        return new AbstractGridModel() {
            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data.get(rowIndex);
            }

            @Override
            public String getColumnName(int column) {
                return item.getDsd().getPrimaryMeasureId();
            }

            @Override
            public String getRowName(int rowIndex) {
                return data.get(rowIndex).getPeriod().getStartAsShortString();
            }
        };
    }

    private IntervalXYDataset asChartModel(SingleSeries item) {
        TimeSeriesCollection result = new TimeSeriesCollection();
        result.setXPosition(TimePeriodAnchor.MIDDLE);
        TimeSeries ts = new TimeSeries(item.getSeries().getKey().toString());
        for (Obs obs : item.getSeries().getObs()) {
            ts.add(new TimeSeriesDataItem(new FixedMillisecond(Timestamp.valueOf(obs.getPeriod().getStart())), obs.getValue()));
        }
        result.addSeries(ts);
        return result;
    }

    private TableModel asMetaTableModel(SingleSeries item) {
        DefaultTableModel result = new DefaultTableModel();
        result.addColumn("Name");
        result.addColumn("Value");
        Map<String, Attribute> attributeById = item.getDsd().getAttributes().stream().collect(toMap(Attribute::getId, identity()));
        item.getSeries().getMeta().forEach((k, v) -> {
            Attribute attribute = attributeById.get(k);
            result.addRow(attribute != null
                    ? new Object[]{attribute.getName(), attribute.isCoded() ? attribute.getCodelist().getCodes().getOrDefault(v, v) : v}
                    : new Object[]{k, v});
        });
        return result;
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

    private static final class CustomCellRenderer implements TableCellRenderer {

        private final NumberFormat format;
        private final Formatter<Obs> toolTipFormatter;
        private final TableCellRenderer delegate;

        public CustomCellRenderer(JGrid grid, SingleSeries item) {
            this.format = SeriesMetaFormats.getNumberFormat(item.getMeta());
            this.toolTipFormatter = ObsFormats.getHtmlTooltipFormatter(item.getDsd());
            this.delegate = grid.getDefaultRenderer(Object.class);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Obs obs = (Obs) value;
            String formattedValue = format.format(obs.getValue());
            Component result = delegate.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
            if (result instanceof JLabel) {
                JLabel label = (JLabel) result;
                label.setToolTipText(toolTipFormatter.formatAsString(obs));
                label.setHorizontalAlignment(JLabel.TRAILING);
            }
            return result;
        }
    }

    private static ObsFunction<String> asObsFunction(SingleSeries item) {
        List<Obs> data = item.getSeries().getObsList();
        Formatter<Obs> obsFormatter = ObsFormats.getChartTooltipFormatter(item.getDsd());
        return new ObsFunction<String>() {
            @Override
            public @Nullable String apply(int series, int obs) {
                return obsFormatter.formatAsString(data.get(obs));
            }
        };
    }

    private static URL getWebsite(JDataSet c) {
        DataSetRef model = c.getModel();
        if (model == null) return null;
        WebSource source = c.getSdmxManager().getSources().get(model.getDataSourceRef().getSource());
        if (source == null) return null;
        return source.getWebsite();
    }
}
