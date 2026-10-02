package sdmxdl.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

/**
 * Preview of the script that reproduces a request, with the selection of its target and options.
 * <p>
 * The script is regenerated each time a property changes and is exposed via the bound
 * {@link #SCRIPT_PROPERTY}. The CLI launcher is not editable in the panel; set it through
 * {@link #setOptions(ScriptOptions)}.
 */
public final class ScriptPreviewPanel extends JComponent {

    private static final Color ERROR_COLOR = new Color(0xC6, 0x28, 0x28);

    // ==================== Properties ====================
    public static final String MANAGER_PROPERTY = "manager";

    @lombok.Getter
    private @NonNull ScriptManager manager = ScriptManager.noOp();

    public void setManager(@NonNull ScriptManager manager) {
        firePropertyChange(MANAGER_PROPERTY, this.manager, this.manager = manager);
    }

    public static final String SOURCE_ID_PROPERTY = "sourceId";

    @lombok.Getter
    private @Nullable String sourceId = null;

    public void setSourceId(@Nullable String sourceId) {
        firePropertyChange(SOURCE_ID_PROPERTY, this.sourceId, this.sourceId = sourceId);
    }

    public static final String REQUEST_PROPERTY = "request";

    @lombok.Getter
    private @Nullable Request request = null;

    public void setRequest(@Nullable Request request) {
        firePropertyChange(REQUEST_PROPERTY, this.request, this.request = request);
    }

    public static final String TARGET_PROPERTY = "target";

    @lombok.Getter
    private @Nullable ScriptTarget target = null;

    public void setTarget(@Nullable ScriptTarget target) {
        firePropertyChange(TARGET_PROPERTY, this.target, this.target = target);
    }

    public static final String OPTIONS_PROPERTY = "options";

    @lombok.Getter
    private @NonNull ScriptOptions options = ScriptOptions.DEFAULT;

    public void setOptions(@NonNull ScriptOptions options) {
        firePropertyChange(OPTIONS_PROPERTY, this.options, this.options = options);
    }

    public static final String VIEWER_FACTORY_PROPERTY = "viewerFactory";

    @lombok.Getter
    private @NonNull Supplier<ScriptViewer> viewerFactory = ScriptViewer::auto;

    public void setViewerFactory(@NonNull Supplier<ScriptViewer> viewerFactory) {
        firePropertyChange(VIEWER_FACTORY_PROPERTY, this.viewerFactory, this.viewerFactory = viewerFactory);
    }

    public static final String SCRIPT_PROPERTY = "script";

    @lombok.Getter
    private @Nullable Script script = null;

    private void setScript(@Nullable Script script) {
        firePropertyChange(SCRIPT_PROPERTY, this.script, this.script = script);
    }

    // ==================== Components ====================
    private final DefaultComboBoxModel<ScriptTarget> targetModel = new DefaultComboBoxModel<>();
    private final JComboBox<ScriptTarget> targetCombo = new JComboBox<>(targetModel);
    private final JTextField outputFileField = new JTextField();
    private final JLabel restEndpointLabel = new JLabel("REST endpoint:");
    private final JTextField restEndpointField = new JTextField();
    private final DefaultTableModel propertiesModel = new PropertiesTableModel();
    private final JScrollPane propertiesPane = new JScrollPane(new JTable(propertiesModel));
    private final JLabel statusLabel = new JLabel();
    private final JPanel viewerPanel = new JPanel(new BorderLayout());
    private ScriptViewer viewer;
    private boolean updating = false;

    public ScriptPreviewPanel() {
        initComponents();
    }

    private void initComponents() {
        targetCombo.addActionListener(e -> {
            if (!updating) {
                setTarget((ScriptTarget) targetCombo.getSelectedItem());
            }
        });
        onTextChange(outputFileField, this::onOptionsEdit);
        onTextChange(restEndpointField, this::onOptionsEdit);
        propertiesModel.addTableModelListener(e -> onOptionsEdit());
        propertiesPane.setPreferredSize(new java.awt.Dimension(200, 80));

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JLabel targetLabel = new JLabel("Target:");
        JLabel outputFileLabel = new JLabel("Output file:");
        addRow(optionsPanel, 0, targetLabel, targetCombo);
        addRow(optionsPanel, 1, outputFileLabel, outputFileField);
        addRow(optionsPanel, 2, restEndpointLabel, restEndpointField);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.insets = new Insets(2, 0, 2, 0);
        optionsPanel.add(propertiesPane, c);
        // keeps the width of the label column stable when some rows are hidden
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 4;
        filler.insets = new Insets(0, 0, 0, 8);
        optionsPanel.add(new WidthFiller(targetLabel, outputFileLabel, restEndpointLabel), filler);

        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        viewer = viewerFactory.get();
        viewerPanel.add(viewer.getComponent(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(optionsPanel, BorderLayout.NORTH);
        add(viewerPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        addPropertyChangeListener(this::onPropertyChange);
        refresh();
    }

    private static void addRow(JPanel panel, int row, JComponent label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(2, 0, 2, 8);
        panel.add(label, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(2, 0, 2, 0);
        panel.add(field, c);
    }

    private void onPropertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case MANAGER_PROPERTY:
            case SOURCE_ID_PROPERTY:
            case REQUEST_PROPERTY:
            case TARGET_PROPERTY:
            case OPTIONS_PROPERTY:
                refresh();
                break;
            case VIEWER_FACTORY_PROPERTY:
                onViewerFactoryChange();
                break;
            default:
                break;
        }
    }

    private void onViewerFactoryChange() {
        viewerPanel.remove(viewer.getComponent());
        viewer = viewerFactory.get();
        viewerPanel.add(viewer.getComponent(), BorderLayout.CENTER);
        viewerPanel.revalidate();
        viewerPanel.repaint();
        viewer.setScript(script);
    }

    private void refresh() {
        if (updating) {
            return;
        }
        updating = true;
        try {
            List<ScriptTarget> targets = getSupportedTargets();
            if (!targets.isEmpty() && (target == null || !targets.contains(target))) {
                // updating is set, so this change only updates the field and notifies listeners
                setTarget(targets.get(0));
            }
            targetModel.removeAllElements();
            targets.forEach(targetModel::addElement);
            targetModel.setSelectedItem(target);
            updateOptionFields();
        } finally {
            updating = false;
        }
        generate();
    }

    private List<ScriptTarget> getSupportedTargets() {
        Request current = request;
        return current == null
                ? java.util.Collections.emptyList()
                : manager.getTargets().stream()
                        .filter(item -> manager.isSupported(item, current.getClass()))
                        .sorted(Comparator.comparing(ScriptTarget::toString))
                        .collect(Collectors.toList());
    }

    private void updateOptionFields() {
        setTextIfChanged(outputFileField, options.getOutputFile() != null ? options.getOutputFile() : "");
        setTextIfChanged(restEndpointField, options.getRestEndpoint().toString());
        boolean rest = target != null && ScriptTarget.REST_TRANSPORT.equals(target.getTransport());
        restEndpointLabel.setVisible(rest);
        restEndpointField.setVisible(rest);

        List<String> names = target != null
                ? manager.getPropertyNames(target).stream().sorted().collect(Collectors.toList())
                : java.util.Collections.emptyList();
        propertiesModel.setRowCount(0);
        names.forEach(name -> propertiesModel.addRow(
                new Object[] {name, options.getProperties().getOrDefault(name, "")}));
        propertiesPane.setVisible(!names.isEmpty());
        revalidate();
    }

    private void onOptionsEdit() {
        if (updating) {
            return;
        }
        ScriptOptions.Builder result = options.toBuilder();
        String outputFile = outputFileField.getText().trim();
        result.outputFile(outputFile.isEmpty() ? null : outputFile);
        try {
            String restEndpoint = restEndpointField.getText().trim();
            if (restEndpoint.isEmpty()) {
                showStatus("REST endpoint is required", true);
                return;
            }
            result.restEndpoint(URI.create(restEndpoint));
        } catch (IllegalArgumentException ex) {
            showStatus("Invalid REST endpoint: " + ex.getMessage(), true);
            return;
        }
        Map<String, String> properties = new LinkedHashMap<>(options.getProperties());
        for (int row = 0; row < propertiesModel.getRowCount(); row++) {
            String name = (String) propertiesModel.getValueAt(row, 0);
            Object value = propertiesModel.getValueAt(row, 1);
            if (value == null || value.toString().isEmpty()) {
                properties.remove(name);
            } else {
                properties.put(name, value.toString());
            }
        }
        result.clearProperties().properties(properties);
        setOptions(result.build());
    }

    private void generate() {
        String currentSourceId = sourceId;
        Request currentRequest = request;
        ScriptTarget currentTarget = target;
        if (currentSourceId == null || currentRequest == null || currentTarget == null) {
            updateScript(null);
            showStatus(currentRequest != null && currentSourceId != null ? "No script target available" : "", true);
            return;
        }
        try {
            Script result = manager.generate(currentTarget, currentSourceId, currentRequest, options);
            updateScript(result);
            showStatus(String.join(" ", result.getWarnings()), false);
        } catch (IllegalArgumentException ex) {
            updateScript(null);
            showStatus(ex.getMessage(), true);
        }
    }

    private void updateScript(@Nullable Script script) {
        setScript(script);
        viewer.setScript(script);
    }

    private void showStatus(@Nullable String text, boolean error) {
        boolean visible = text != null && !text.isEmpty();
        statusLabel.setText(visible ? text : "");
        statusLabel.setForeground(error ? ERROR_COLOR : null);
        statusLabel.setVisible(visible);
    }

    // ==================== Dialog ====================

    /**
     * Shows this panel in a modal dialog with copy and save actions.
     *
     * @param parent the parent component of the dialog
     * @param title  the title of the dialog
     */
    public void showDialog(@Nullable Component parent, @NonNull String title) {
        Window owner = parent instanceof Window
                ? (Window) parent
                : parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = owner instanceof Dialog
                ? new JDialog((Dialog) owner, title, true)
                : new JDialog((Frame) owner, title, true);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());
        JButton saveButton = new JButton("Save as…");
        saveButton.addActionListener(e -> saveToFile(dialog));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        Runnable updateButtons = () -> {
            copyButton.setEnabled(script != null);
            saveButton.setEnabled(script != null);
        };
        updateButtons.run();
        java.beans.PropertyChangeListener scriptListener = evt -> updateButtons.run();
        addPropertyChangeListener(SCRIPT_PROPERTY, scriptListener);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                removePropertyChangeListener(SCRIPT_PROPERTY, scriptListener);
            }
        });
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(closeButton);
        dialog.setSize(720, 560);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void copyToClipboard() {
        if (script != null) {
            StringSelection content = new StringSelection(script.getContent());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, content);
        }
    }

    private void saveToFile(Component parent) {
        Script current = script;
        if (current == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(
                java.nio.file.Paths.get(getDefaultFileName(current)).toFile());
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.exists()
                && JOptionPane.showConfirmDialog(
                                parent,
                                "File '" + file.getName() + "' already exists. Overwrite it?",
                                "Save as",
                                JOptionPane.YES_NO_OPTION)
                        != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(file.toPath(), current.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Save as", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getDefaultFileName(Script current) {
        String baseName = sourceId != null ? sourceId.replaceAll("[^A-Za-z0-9._-]", "_") + "_script" : "script";
        return baseName + "." + current.getFileExtension();
    }

    private static void setTextIfChanged(JTextField field, String text) {
        if (!field.getText().equals(text)) {
            field.setText(text);
        }
    }

    private static void onTextChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    /**
     * Invisible component as wide as the widest of some components, even if they are hidden.
     */
    private static final class WidthFiller extends JComponent {

        private final JComponent[] components;

        WidthFiller(JComponent... components) {
            this.components = components;
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            int width = 0;
            for (JComponent component : components) {
                width = Math.max(width, component.getPreferredSize().width);
            }
            return new java.awt.Dimension(width, 0);
        }

        @Override
        public java.awt.Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    private static final class PropertiesTableModel extends DefaultTableModel {

        PropertiesTableModel() {
            super(new Object[] {"Property", "Value"}, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }
    }
}
