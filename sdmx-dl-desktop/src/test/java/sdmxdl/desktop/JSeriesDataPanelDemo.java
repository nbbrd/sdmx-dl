package sdmxdl.desktop;

import ec.util.various.swing.BasicSwingLauncher;
import internal.sdmxdl.desktop.util.AccentColors;
import sdmxdl.desktop.panels.DataPanel;
import tests.sdmxdl.api.RepoSamples;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class JSeriesDataPanelDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(() -> {
                    DataPanel panel = new DataPanel();
                    AbstractAction load = new AbstractAction("Load") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            panel.setModel(new SingleSeries(DataSetRef.builder().build(), Collections.emptyList(), RepoSamples.STRUCT, RepoSamples.S1, AccentColors.DARK_BLUE));
                        }
                    };
                    AbstractAction clear = new AbstractAction("Clear") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            panel.setModel(null);
                        }
                    };

                    JPanel result = new JPanel(new BorderLayout());
                    JToolBar toolBar = new JToolBar();
                    toolBar.add(load);
                    toolBar.add(clear);
                    result.add(toolBar, BorderLayout.NORTH);
                    result.add(panel, BorderLayout.CENTER);
                    return result;
                })
                .launch();
    }
}
