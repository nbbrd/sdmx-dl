package sdmxdl.desktop;

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
import internal.sdmxdl.desktop.util.GridChart;
import internal.sdmxdl.desktop.util.SystemLafColorScheme;
import lombok.Getter;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import sdmxdl.Obs;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.List;

public final class JSeriesDataPanel extends JComponent implements HasModel<SingleSeries> {

    @Getter
    private SingleSeries model;

    public void setModel(SingleSeries model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JGrid grid = new JGrid();

    private final JTimeSeriesChart chart = new JTimeSeriesChart();

    public JSeriesDataPanel() {
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
        chart.setColorSchemeSupport(SwingColorSchemeSupport.from(new SystemLafColorScheme()));

        GridChart.enableSync(grid, chart);

        JSplitPane dataPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grid, chart);
        dataPane.setResizeWeight(.3);

        grid.addPropertyChangeListener(JGrid.MODEL_PROPERTY, evt -> {
            dataPane.setDividerLocation(grid.getColumnModel().getTotalColumnWidth() * 2
                    + UIManager.getInt("ScrollBar.width"));
        });

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, dataPane);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            grid.setModel(asGridModel(model));
            grid.setDefaultRenderer(Object.class, new CustomCellRenderer(grid, model));
            grid.setCornerRenderer(JTables.cellRendererOf((label, value) -> {
                label.setText(model.getDuration() != null ? model.getDuration().toString() : null);
                label.setHorizontalAlignment(JLabel.CENTER);
            }));
            chart.setDataset(asChartModel(model));
            chart.setTitle(model.getMeta().getName());
            chart.setObsFormatter(asObsFunction(model));
            chart.setPeriodFormat(SeriesMetaFormats.getDateFormat(model.getMeta()));
            chart.putClientProperty("fixme_item", model);
        } else {
            grid.setModel(null);
            chart.setDataset(null);
        }
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
}
