package internal.sdmxdl.desktop.util;

import ec.util.chart.impl.AndroidColorScheme;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public final class SystemLafColorScheme extends AndroidColorScheme.AndroidDarkColorScheme {
    
    @Override
    public int getPlotColor() {
        return UIManager.getColor("Table.selectionForeground").getRGB();
    }

    @Override
    public int getBackColor() {
        return UIManager.getColor("Panel.background").getRGB();
    }

    @Override
    public @NonNull List<Integer> getLineColors() {
        return Collections.singletonList(UIManager.getColor("Table.selectionBackground").getRGB());
    }

    @Override
    public int getAxisColor() {
        Color result = UIManager.getColor("Table.cellFocusColor");
        return result != null ? result.getRGB() : super.getAxisColor();
    }

    @Override
    public int getGridColor() {
        return UIManager.getColor("Table.gridColor").getRGB();
    }

    @Override
    public int getTextColor() {
        return UIManager.getColor("Panel.foreground").getRGB();
    }
}
