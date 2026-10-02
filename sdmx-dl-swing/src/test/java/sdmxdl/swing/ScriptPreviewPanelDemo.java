package sdmxdl.swing;

import javax.swing.SwingUtilities;
import nbbrd.design.Demo;
import sdmxdl.DataRequest;
import sdmxdl.script.ScriptManager;

public class ScriptPreviewPanelDemo {

    @Demo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();

            ScriptPreviewPanel panel = new ScriptPreviewPanel();
            panel.setManager(ScriptManager.ofServiceLoader());
            panel.setSourceId("ECB");
            panel.setRequest(DataRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M.CHF+USD.EUR.SP00.A")
                    .build());
            panel.showDialog(null, "Generate script");
            System.exit(0);
        });
    }
}
