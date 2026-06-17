package sdmxdl.swing;

import com.formdev.flatlaf.FlatLightLaf;
import nbbrd.design.Demo;
import sdmxdl.web.WebFlowRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static sdmxdl.swing.FlowSearchPanel.SELECTION_PROPERTY;

public class FlowSearchPanelDemo {

    @Demo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            FlowSearchPanel panel = new FlowSearchPanel();
            panel.setSourceIconProvider((src, repaint) -> DemoUtil.getFavicon(src, repaint, 32));
            panel.setManager(DemoUtil.getSdmxWebManager());

            JLabel selectionLabel = new JLabel("selection: <none>");
            selectionLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
            selectionLabel.setFont(selectionLabel.getFont().deriveFont(Font.PLAIN, 11f));
            panel.addPropertyChangeListener(SELECTION_PROPERTY, evt -> {
                WebFlowRequest ref = (WebFlowRequest) evt.getNewValue();
                selectionLabel.setText(ref == null
                        ? "selection: <none>"
                        : "selection: source=" + ref.getSource()
                          + "  database=" + ref.getRequest().getDatabase()
                          + "  flow=" + ref.getRequest().getFlow());
            });

            JPanel content = new JPanel(new BorderLayout(0, 4));
            content.setBorder(new EmptyBorder(8, 8, 8, 8));
            content.add(panel, BorderLayout.CENTER);
            content.add(selectionLabel, BorderLayout.SOUTH);

            JFrame frame = new JFrame("MegaFlowSearchPanel — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(content);
            frame.setSize(700, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
