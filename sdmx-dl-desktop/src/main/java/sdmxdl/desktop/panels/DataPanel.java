package sdmxdl.desktop.panels;

import ec.util.chart.ObsFunction;
import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.GridModel;
import ec.util.grid.swing.JGrid;
import ec.util.grid.swing.XTable;
import ec.util.table.swing.JTables;
import internal.sdmxdl.desktop.util.AccentColorScheme;
import internal.sdmxdl.desktop.util.GridChart;
import internal.sdmxdl.desktop.util.SystemLafColorScheme;
import j2html.tags.DomContent;
import lombok.Getter;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.desktop.SingleSeries;
import sdmxdl.provider.ext.SeriesMeta;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.text.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static internal.sdmxdl.desktop.util.Html4Swing.labelTag;
import static j2html.TagCreator.*;

public final class DataPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @Getter
    private SingleSeries model;

    public void setModel(SingleSeries model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    public static final String NO_MODEL_MESSAGE = "noModelMessage";

    @Getter
    private String noModelMessage = "No data";

    public void setNoModelMessage(String noModelMessage) {
        firePropertyChange(NO_MODEL_MESSAGE, this.noModelMessage, this.noModelMessage = noModelMessage);
    }

    private final JGrid grid = new JGrid();

    private final JTimeSeriesChart chart = new JTimeSeriesChart();

    public DataPanel() {
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
        addPropertyChangeListener(NO_MODEL_MESSAGE, this::onNoModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        if (model != null) {
            grid.setModel(asGridModel(model));
            grid.setDefaultRenderer(Object.class, new CustomCellRenderer(grid, model));
            grid.setCornerRenderer(JTables.cellRendererOf((label, value) -> {
                label.setText(model.getDuration() != null ? html(small(labelTag(model.getDuration().toString(), model.getAccentColor()))).render() : null);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setToolTipText(model.getDuration() + " #" + model.getSeries().getObs().size());
            }));
            chart.setDataset(asChartModel(model));
            chart.setTitle(model.getMeta().getName());
            chart.setObsFormatter(asObsFunction(model));
            chart.setPeriodFormat(getDateFormat(model.getMeta()));
            chart.putClientProperty("fixme_item", model);
            chart.setColorSchemeSupport(SwingColorSchemeSupport.from(new AccentColorScheme(model.getAccentColor())));
        } else {
            grid.setModel(null);
            chart.setDataset(null);
        }
    }

    private void onNoModelChange(PropertyChangeEvent evt) {
        grid.setNoDataRenderer(new XTable.DefaultNoDataRenderer(noModelMessage));
        chart.setNoDataMessage(noModelMessage);
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
                return html(small(labelTag(item.getDsd().getPrimaryMeasureId(), model.getAccentColor()))).render();
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
            this.format = getNumberFormat(item.getMeta());
            this.toolTipFormatter = getHtmlTooltipFormatter(item.getDsd());
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
        Formatter<Obs> obsFormatter = getChartTooltipFormatter(item.getDsd());
        return new ObsFunction<String>() {
            @Override
            public @Nullable String apply(int series, int obs) {
                return obsFormatter.formatAsString(data.get(obs));
            }
        };
    }

    private static DateFormat getDateFormat(SeriesMeta meta) {
        Locale locale = Locale.getDefault(Locale.Category.DISPLAY);
        Duration timeUnit = meta.getTimeUnit();
        if (timeUnit != null) {
            switch (timeUnit.getMinChronoUnit()) {
                case SECONDS:
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
                case MINUTES:
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", locale);
                case HOURS:
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH", locale);
                case DAYS:
                    return new SimpleDateFormat("yyyy-MM-dd", locale);
                case MONTHS:
                    return new SimpleDateFormat("yyyy-MM", locale);
                case YEARS:
                    return new SimpleDateFormat("yyyy", locale);
            }
        }
        return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    }

    private static NumberFormat getNumberFormat(SeriesMeta meta) {
        Locale locale = Locale.getDefault(Locale.Category.DISPLAY);
        return Parser.onInteger()
                .parseValue(meta.getDecimals())
                .map(DataPanel::getValuePattern)
                .map(pattern -> new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale)))
                .map(NumberFormat.class::cast)
                .orElseGet(() -> NumberFormat.getInstance(locale));
    }

    private static String getValuePattern(int decimals) {
        return IntStream.range(0, decimals)
                .mapToObj(i -> "0")
                .collect(Collectors.joining("", "0.", ""));
    }

    private static Formatter<Obs> getChartTooltipFormatter(Structure dsd) {
        Map<String, Attribute> attributes = dsd.getAttributes().stream()
                .filter(attribute -> attribute.getRelationship().equals(AttributeRelationship.OBSERVATION))
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        return obs -> getChartToolTipText(obs, attributes);
    }

    private static String getChartToolTipText(Obs obs, Map<String, Attribute> attributes) {
        return obs.getPeriod().toShortString() + ": " + obs.getValue();
    }

    private static Formatter<Obs> getHtmlTooltipFormatter(Structure dsd) {
        Map<String, Attribute> attributes = dsd.getAttributes().stream()
                .filter(attribute -> attribute.getRelationship().equals(AttributeRelationship.OBSERVATION))
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        return obs -> getToolTipText(obs, attributes);
    }

    private static String getToolTipText(Obs obs, Map<String, Attribute> attributes) {
        return html(
                table(
                        tr(th("Period:").withStyle("text-align:right"), td(text(obs.getPeriod().toShortString()))),
                        tr(th("Value:").withStyle("text-align:right"), td(text(String.valueOf(obs.getValue())))),
                        tr(th("Meta:").withStyle("text-align:right"), td(metaToHtml(obs.getMeta(), attributes)))
                )
        ).render();
    }

    private static DomContent metaToHtml(Map<String, String> meta, Map<String, Attribute> attributes) {
        return table(each(meta.entrySet(), i -> metaToHtml(i, attributes))).withStyle("border-style: solid; border-width: 1px;");
    }

    private static DomContent metaToHtml(Map.Entry<String, String> entry, Map<String, Attribute> attributes) {
        Attribute attribute = attributes.get(entry.getKey());
        return attribute != null
                ? tr(
                td(attribute.getName()).withStyle("text-align=right"),
                td(attribute.isCoded() ? attribute.getCodelist().getCodes().get(entry.getValue()) : entry.getValue())
        )
                : tr(td(entry.getKey()).withStyle("text-align=right"), td(entry.getValue()));
    }
}
