package sdmxdl.desktop;

import ec.util.chart.ObsFunction;
import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.GridModel;
import ec.util.grid.swing.JGrid;
import internal.sdmxdl.desktop.GridChart;
import internal.sdmxdl.desktop.ObsFormats;
import internal.sdmxdl.desktop.SeriesMetaFormats;
import internal.sdmxdl.desktop.SystemLafColorScheme;
import lombok.NonNull;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import sdmxdl.Obs;
import sdmxdl.ext.Registry;
import sdmxdl.web.SdmxWebManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class JDataSet extends JComponent implements HasSdmxProperties<SdmxWebManager> {

    @lombok.Getter
    private SdmxWebManager sdmxManager = SdmxWebManager.noOp();

    public void setSdmxManager(@NonNull SdmxWebManager sdmxManager) {
        firePropertyChange(SDMX_MANAGER_PROPERTY, this.sdmxManager, this.sdmxManager = sdmxManager);
    }

    @lombok.Getter
    private Registry registry = Registry.noOp();

    public void setRegistry(@NonNull Registry registry) {
        firePropertyChange(REGISTRY_PROPERTY, this.registry, this.registry = registry);
    }

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private DataSetRef model;

    public void setModel(DataSetRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextArea textArea = new JTextArea();

    private final JGrid grid = new JGrid();

    private final JTimeSeriesChart chart = new JTimeSeriesChart();

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grid, chart);
        splitPane.setResizeWeight(.3);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, splitPane);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        reportLoading();
        new SwingWorker<SingleSeries, Void>() {
            @Override
            protected SingleSeries doInBackground() throws Exception {
                return SingleSeries.load(getSdmxManager(), getRegistry(), model);
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
        textArea.setText("Loading");
    }

    private void displayData(SingleSeries item) {
        grid.setModel(asGridModel(item));
        grid.setDefaultRenderer(Object.class, new CustomCellRenderer(grid, item));
        chart.setDataset(asChartModel(item));
        chart.setTitle(item.getMeta().getName());
        chart.setObsFormatter(asObsFunction(item));
        chart.setPeriodFormat(SeriesMetaFormats.getDateFormat(item.getMeta()));
        chart.putClientProperty("fixme_item", item);
    }

    private void reportError(Exception ex) {
        ex.printStackTrace();
        textArea.setText(ex.getMessage());
    }

    private GridModel asGridModel(SingleSeries item) {
        List<Obs> data = item.getSeries().getObsList();
        DateTimeFormatter dateTimeFormatter = SeriesMetaFormats.getDateTimeFormatter(item.getMeta());
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
                return dateTimeFormatter.format(data.get(rowIndex).getPeriod());
            }
        };
    }

    private IntervalXYDataset asChartModel(SingleSeries item) {
        TimeSeriesCollection result = new TimeSeriesCollection();
        result.setXPosition(TimePeriodAnchor.MIDDLE);
        TimeSeries ts = new TimeSeries(item.getSeries().getKey().toString());
        for (Obs obs : item.getSeries().getObs()) {
            ts.add(new TimeSeriesDataItem(new FixedMillisecond(Timestamp.valueOf(obs.getPeriod())), obs.getValue()));
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

//    private static final class CustomTooltip extends JLabel {
//
//        private Popup popup;
//
//        public CustomTooltip() {
//            this.popup = null;
//            setEnabled(false);
//        }
//
//        public void enable(final JTimeSeriesChart chart) {
//            updateColors(chart);
//            chart.addPropertyChangeListener(evt -> {
//                switch (evt.getPropertyName()) {
//                    case JTimeSeriesChart.TOOLTIP_TRIGGER_PROPERTY:
//                        if (popup != null) {
//                            popup.hide();
//                        }
//                        break;
//                    case JTimeSeriesChart.HOVERED_OBS_PROPERTY:
//                        if (isEnabled() && chart.getTooltipTrigger() != TimeSeriesChart.DisplayTrigger.SELECTION) {
//                            updateCustomTooltip(chart, chart.getObsExistPredicate().apply(chart.getHoveredObs()));
//                        }
//                        break;
//                    case JTimeSeriesChart.SELECTED_OBS_PROPERTY:
//                        if (isEnabled() && chart.getTooltipTrigger() != TimeSeriesChart.DisplayTrigger.HOVERING) {
//                            updateCustomTooltip(chart, chart.getObsExistPredicate().apply(chart.getSelectedObs()));
//                        }
//                        break;
//                    case JTimeSeriesChart.COLOR_SCHEME_SUPPORT_PROPERTY:
//                        updateColors(chart);
//                        break;
//                }
//            });
//        }
//
//        private void updateColors(JTimeSeriesChart chart) {
//            ColorSchemeSupport<? extends Color> csc = chart.getColorSchemeSupport();
//            setOpaque(true);
//            setBackground(csc.getBackColor());
//            setForeground(csc.getTextColor());
//            setBorder(createCompoundBorder(createLineBorder(csc.getGridColor(), 1), createEmptyBorder(5, 5, 5, 5)));
//        }
//
//        private void updateCustomTooltip(JTimeSeriesChart chart, boolean visible) {
//            if (popup != null) {
//                popup.hide();
//            }
//            if (visible) {
//                Point p = MouseInfo.getPointerInfo().getLocation();
//                popup = PopupFactory.getSharedInstance().getPopup(chart, getCustomTooltip(chart), p.x + 5, p.y + 5);
//                popup.show();
//            }
//        }
//
//        private Component getCustomTooltip(JTimeSeriesChart chart) {
//            ObsIndex o = chart.getHoveredObs();
//            String serie = chart.getSeriesFormatter().apply(o.getSeries());
//            String value = chart.getValueFormatter().apply(o);
//            String period = chart.getPeriodFormatter().apply(o);
//            boolean forecast = chart.getDashPredicate().apply(o);
//            Color color = chart.getColorSchemeSupport().getLineColor(o.getSeries());
////            setText("<html><b>" + serie + "</b><br>" + period + ": " + value);
//            SingleSeries fixmeItem = (SingleSeries) chart.getClientProperty("fixme_item");
//            setText(ObsFormats.getHtmlTooltipFormatter(fixmeItem.getDsd()).formatAsString(fixmeItem.getSeries().getObsList().get(o.getObs())));
//            return this;
//        }
//    }

}
