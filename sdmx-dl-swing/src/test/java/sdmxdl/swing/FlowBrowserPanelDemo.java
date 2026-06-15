package sdmxdl.swing;

import nbbrd.design.Demo;
import sdmxdl.DatabaseRef;
import sdmxdl.Flow;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class FlowBrowserPanelDemo {

    @Demo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();

            FlowSelectionPanel selector = new FlowSelectionPanel();

            JLabel modelLabel = new JLabel("model: <none>");
            modelLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
            modelLabel.setFont(modelLabel.getFont().deriveFont(Font.PLAIN, 11f));
            selector.addPropertyChangeListener(FlowSelectionPanel.MODEL_PROPERTY, evt -> {
                WebFlowRequest ref = (WebFlowRequest) evt.getNewValue();
                modelLabel.setText(ref == null
                        ? "model: <none>"
                        : "model: source=" + ref.getSource()
                          + "  database=" + ref.getRequest().getDatabase()
                          + "  flow=" + ref.getRequest().getFlow());
            });

            JPanel content = new JPanel(new BorderLayout(0, 4));
            content.setBorder(new EmptyBorder(12, 12, 12, 12));
            content.add(selector, BorderLayout.NORTH);
            content.add(modelLabel, BorderLayout.CENTER);

            JFrame frame = new JFrame("FlowSelectorPanel — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(content);
            frame.pack();
            frame.setMinimumSize(new java.awt.Dimension(450, frame.getHeight()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * A reusable Swing component that displays the currently selected SDMX dataflow
     * and allows the user to pick a new one through a step-by-step browser dialog:
     * <ol>
     *   <li><b>Sources</b> – searchable list of all non-alias {@link WebSource} entries.</li>
     *   <li><b>Databases</b> – sub-databases for the chosen source
     *       (skipped automatically when none exist).</li>
     *   <li><b>Flows</b> – searchable list of {@link Flow} objects; double-clicking
     *       a flow confirms the selection.</li>
     * </ol>
     *
     * <p>The selected flow is exposed through the bound {@link #MODEL_PROPERTY} as a
     * {@link WebFlowRequest} that carries the source id, database ref, and flow ref.</p>
     *
     */
    private static final class FlowSelectionPanel extends JComponent {

        public static final String MODEL_PROPERTY = "model";

        @lombok.Getter
        private WebFlowRequest model;

        public void setModel(WebFlowRequest model) {
            firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
        }

        private final JLabel summaryLabel = new JLabel();
        private final JButton browseButton = new JButton();

        private final SdmxWebManager manager = DemoUtil.getSdmxWebManager();
        private final Languages languages = Languages.ANY;

        public FlowSelectionPanel() {
            initComponents();
        }

        private void initComponents() {
            summaryLabel.setText("🗄 No flow selected");

            browseButton.setText("Browse… ›");
            browseButton.addActionListener(e -> showBrowseDialog());

            setLayout(new BorderLayout(4, 0));
            setBorder(new EmptyBorder(2, 2, 2, 2));
            add(summaryLabel, BorderLayout.CENTER);
            add(browseButton, BorderLayout.EAST);

            addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
        }

        private void onModelChange(PropertyChangeEvent evt) {
            WebFlowRequest ref = (WebFlowRequest) evt.getNewValue();
            if (ref == null || ref.getSource().isEmpty()) {
                summaryLabel.setText("🗄 No flow selected");
            } else {
                StringBuilder sb = new StringBuilder(ref.getSource());
                if (!ref.getRequest().getDatabase().equals(DatabaseRef.NO_DATABASE)) {
                    sb.append(" / ").append(ref.getRequest().getDatabase());
                }
                sb.append(" / ").append(ref.getRequest().getFlow());
                summaryLabel.setText(sb.toString());
                WebSource source = manager.getSources().get(ref.getSource());
                summaryLabel.setIcon(source != null ? DemoUtil.getFavicon(source, summaryLabel::repaint, 16) : null);
            }
        }

        private void showBrowseDialog() {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = owner instanceof Frame
                    ? new JDialog((Frame) owner, "Select Flow", true)
                    : new JDialog((Dialog) owner, "Select Flow", true);

            FlowBrowserPanel browser = new FlowBrowserPanel();
            browser.setSourceIconProvider((src, repaint) -> DemoUtil.getFavicon(src, repaint, 32));
            browser.setLanguages(languages);
            browser.setManager(manager);

            JButton selectButton = new JButton("Select");
            selectButton.setEnabled(false);
            JButton cancelButton = new JButton("Cancel");

            browser.addPropertyChangeListener(FlowBrowserPanel.SELECTION_PROPERTY, evt -> selectButton.setEnabled(browser.getSelection() != null));

            selectButton.addActionListener(e -> {
                WebFlowRequest selection = browser.getSelection();
                if (selection != null) {
                    setModel(selection);
                    dialog.dispose();
                }
            });
            cancelButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
            buttonPanel.add(cancelButton);
            buttonPanel.add(selectButton);

            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(browser, BorderLayout.CENTER);
            dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            dialog.setSize(600, 480);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }
}
