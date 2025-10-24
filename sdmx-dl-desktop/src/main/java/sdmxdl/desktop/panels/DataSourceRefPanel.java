package sdmxdl.desktop.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.completion.swing.JAutoCompletion;
import ec.util.list.swing.JLists;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.Ikons;
import nbbrd.design.MightBePromoted;
import nbbrd.io.text.Formatter;
import net.miginfocom.swing.MigLayout;
import sdmxdl.DatabaseRef;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.desktop.Toggle;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.sdmxdl.desktop.util.Documents.documentListenerOf;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_MENU_DOWN;

public final class DataSourceRefPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private DataSourceRef model;

    public void setModel(DataSourceRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField sourceField = new JTextField();
    private final JTextField databaseField = new JTextField();
    private final JTextField flowField = new JTextField();
    private final JTextField dimensionsField = new JTextField();
    private final JTextField languagesField = new JTextField();
    private final JCheckBox debugBox = new JCheckBox();
    private final JComboBox<Toggle> curlBackendBox = new JComboBox<>(Toggle.values());
    private boolean updating = false;

    public DataSourceRefPanel() {
        initComponents();
    }

    private void initComponents() {
        JButton sourceButton = new JButton(Ikons.of(MDI_MENU_DOWN, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        sourceField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, sourceButton);
        SdmxAutoCompletion sourceCompletion = SdmxAutoCompletion.onWebSource(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages());
        JAutoCompletion sourceAutoCompletion = new JAutoCompletion(sourceField);
        sourceAutoCompletion.setSource(sourceCompletion.getSource());
        sourceAutoCompletion.getList().setCellRenderer(sourceCompletion.getRenderer());

        JButton databaseButton = new JButton(Ikons.of(MDI_MENU_DOWN, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        databaseField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, databaseButton);
        SdmxAutoCompletion databaseCompletion = SdmxAutoCompletion.onDatabase(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                new ConcurrentHashMap<>());
        JAutoCompletion databaseAutoCompletion = new JAutoCompletion(databaseField);
        databaseAutoCompletion.setSource(databaseCompletion.getSource());
        databaseAutoCompletion.getList().setCellRenderer(databaseCompletion.getRenderer());

        JButton flowButton = new JButton(Ikons.of(MDI_MENU_DOWN, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        flowField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, flowButton);
        SdmxAutoCompletion flowCompletion = SdmxAutoCompletion.onFlow(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                () -> DatabaseRef.parse(databaseField.getText()),
                new ConcurrentHashMap<>());
        JAutoCompletion flowAutoCompletion = new JAutoCompletion(flowField);
        flowAutoCompletion.setSource(flowCompletion.getSource());
        flowAutoCompletion.getList().setCellRenderer(flowCompletion.getRenderer());

        dimensionsField.setEnabled(false);

        JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        addSeparator(panel, "Source");

        panel.add(new JLabel("Provider"), "skip");
        panel.add(sourceField, "span, growx");

        panel.add(new JLabel("Database"), "skip");
        panel.add(databaseField, "span, growx");

        panel.add(new JLabel("Dataflow"), "skip");
        panel.add(flowField, "span, growx");

        addSeparator(panel, "Options");

        panel.add(new JLabel("Dimensions"), "skip");
        panel.add(dimensionsField, "span, growx");

        panel.add(new JLabel("Languages"), "skip");
        panel.add(languagesField, "span, growx");

        panel.add(new JLabel("Debug"), "skip");
        panel.add(debugBox, "span, growx");

        panel.add(new JLabel("CURL backend"), "skip");
        panel.add(curlBackendBox, "span, growx");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
        sourceField.getDocument().addDocumentListener(documentListenerOf(this::updateModel));
        databaseField.getDocument().addDocumentListener(documentListenerOf(this::updateModel));
        flowField.getDocument().addDocumentListener(documentListenerOf(this::updateModel));
        dimensionsField.getDocument().addDocumentListener(documentListenerOf(this::updateModel));
        languagesField.getDocument().addDocumentListener(documentListenerOf(this::updateModel));
        debugBox.addChangeListener(this::updateModel);
        curlBackendBox.getModel().addListDataListener(JLists.dataListenerOf(this::updateModel));
    }

    private void onModelChange(PropertyChangeEvent event) {
        if (!updating) {
            DataSourceRef newModel = (DataSourceRef) event.getNewValue();
            sourceField.setText(newModel.getSource());
            databaseField.setText(newModel.getDatabase().toString());
            flowField.setText(newModel.getFlow());
            dimensionsField.setText(Formatter.onStringList(DataSourceRefPanel::join).formatAsString(newModel.getDimensions()));
            languagesField.setText(newModel.getLanguages().toString());
            debugBox.setSelected(newModel.isDebug());
            curlBackendBox.setSelectedItem(newModel.getCurlBackend());
        }
    }

    private void updateModel(Object ignore) {
        updateModel2(dataSourceRef -> dataSourceRef
                .toBuilder()
                .source(sourceField.getText())
                .database(DatabaseRef.parse(databaseField.getText()))
                .flow(flowField.getText())
//                .dimensions(Parser.onStringList(JDataSourceRef::split).parse(dimensionsField.getText()))
                .languages(languagesField.getText().isEmpty() ? Languages.ANY : Languages.parse(languagesField.getText()))
                .debug(debugBox.isSelected())
                .curlBackend((Toggle) curlBackendBox.getSelectedItem())
                .build());
    }

    @MightBePromoted
    private void updateModel2(UnaryOperator<DataSourceRef> operator) {
        updating = true;
        setModel(operator.apply(getModel()));
        updating = false;
    }

    private static void addSeparator(JPanel panel, String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(0, 70, 213));

        panel.add(l, "gapbottom 1, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, growx");
    }

    @MightBePromoted
    private static Stream<String> split(CharSequence text) {
        return Stream.of(text.toString().split(",", -1));
    }

    @MightBePromoted
    private static String join(Stream<CharSequence> stream) {
        return stream.collect(Collectors.joining(","));
    }
}
