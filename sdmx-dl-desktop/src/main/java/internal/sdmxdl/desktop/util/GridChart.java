package internal.sdmxdl.desktop.util;

import ec.util.chart.ObsIndex;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.grid.CellIndex;
import ec.util.grid.swing.JGrid;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class GridChart {

    private GridChart() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void enableSync(JGrid grid, JTimeSeriesChart chart) {
        PropertyChangeListener listener = new PropertyChangeListener() {
            boolean updating = false;

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (updating) {
                    return;
                }
                updating = true;
                switch (evt.getPropertyName()) {
                    case JGrid.HOVERED_CELL_PROPERTY:
                        chart.setHoveredObs(toObsIndex(grid.getHoveredCell()));
                        break;
                    case JGrid.SELECTED_CELL_PROPERTY:
                        chart.setSelectedObs(toObsIndex(grid.getSelectedCell()));
                        break;
                    case JTimeSeriesChart.HOVERED_OBS_PROPERTY:
                        grid.setHoveredCell(toCellIndex(chart.getHoveredObs()));
                        break;
                    case JTimeSeriesChart.SELECTED_OBS_PROPERTY:
                        grid.setSelectedCell(toCellIndex(chart.getSelectedObs()));
                        break;
                }
                updating = false;
            }

            private ObsIndex toObsIndex(CellIndex index) {
                return ObsIndex.valueOf(index.getColumn(), index.getRow());
            }

            private CellIndex toCellIndex(ObsIndex index) {
                return CellIndex.valueOf(index.getObs(), index.getSeries());
            }
        };

        grid.addPropertyChangeListener(listener);
        chart.addPropertyChangeListener(listener);
    }
}
