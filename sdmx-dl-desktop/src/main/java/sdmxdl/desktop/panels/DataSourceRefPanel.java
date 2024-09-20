package sdmxdl.desktop.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIconColors;
import ec.util.completion.swing.JAutoCompletion;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.Ikons;
import nbbrd.design.MightBePromoted;
import nbbrd.io.text.Formatter;
import net.miginfocom.swing.MigLayout;
import sdmxdl.CatalogRef;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.Sdmxdl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_MENU_DOWN;

public final class DataSourceRefPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private DataSourceRef model;

    public void setModel(DataSourceRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextField sourceField = new JTextField();
    private final JTextField catalogField = new JTextField();
    private final JTextField flowField = new JTextField();
    private final JTextField dimensionsField = new JTextField();
    private final JTextField languagesField = new JTextField();
    private final JCheckBox debugBox = new JCheckBox();
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

        JButton catalogButton = new JButton(Ikons.of(MDI_MENU_DOWN, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        catalogField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, catalogButton);
        SdmxAutoCompletion catalogCompletion = SdmxAutoCompletion.onCatalog(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                new ConcurrentHashMap<>());
        JAutoCompletion catalogAutoCompletion = new JAutoCompletion(catalogField);
        catalogAutoCompletion.setSource(catalogCompletion.getSource());
        catalogAutoCompletion.getList().setCellRenderer(catalogCompletion.getRenderer());

        JButton flowButton = new JButton(Ikons.of(MDI_MENU_DOWN, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        flowField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, flowButton);
        SdmxAutoCompletion flowCompletion = SdmxAutoCompletion.onDataflow(Sdmxdl.INSTANCE.getSdmxManager(), Sdmxdl.INSTANCE.getLanguages(),
                () -> Sdmxdl.INSTANCE.getSdmxManager().getSources().get(sourceField.getText()),
                () -> CatalogRef.parse(catalogField.getText()),
                new ConcurrentHashMap<>());
        JAutoCompletion flowAutoCompletion = new JAutoCompletion(flowField);
        flowAutoCompletion.setSource(flowCompletion.getSource());
        flowAutoCompletion.getList().setCellRenderer(flowCompletion.getRenderer());

        dimensionsField.setEnabled(false);

        JPanel panel = new JPanel(new MigLayout("ins 20", "[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        addSeparator(panel, "Source");

        panel.add(new JLabel("Provider"), "skip");
        panel.add(sourceField, "span, growx");

        panel.add(new JLabel("Catalog"), "skip");
        panel.add(catalogField, "span, growx");

        panel.add(new JLabel("Dataflow"), "skip");
        panel.add(flowField, "span, growx");

        addSeparator(panel, "Options");

        panel.add(new JLabel("Dimensions"), "skip");
        panel.add(dimensionsField, "span, growx");

        panel.add(new JLabel("Languages"), "skip");
        panel.add(languagesField, "span, growx");

        panel.add(new JLabel("Debug"), "skip");
        panel.add(debugBox, "span, growx");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(panel));

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
        sourceField.getDocument().addDocumentListener((DocumentAdapter) ignore -> updateModel());
        catalogField.getDocument().addDocumentListener((DocumentAdapter) ignore -> updateModel());
        flowField.getDocument().addDocumentListener((DocumentAdapter) ignore -> updateModel());
        dimensionsField.getDocument().addDocumentListener((DocumentAdapter) ignore -> updateModel());
        languagesField.getDocument().addDocumentListener((DocumentAdapter) ignore -> updateModel());
        debugBox.addChangeListener(ignore -> updateModel());
    }

    private void onModelChange(PropertyChangeEvent event) {
        if (!updating) {
            DataSourceRef newModel = (DataSourceRef) event.getNewValue();
            sourceField.setText(newModel.getSource());
            catalogField.setText(newModel.getCatalog().toString());
            flowField.setText(newModel.getFlow());
            dimensionsField.setText(Formatter.onStringList(DataSourceRefPanel::join).formatAsString(newModel.getDimensions()));
            languagesField.setText(newModel.getLanguages().toString());
            debugBox.setSelected(newModel.isDebug());
        }
    }

    private void updateModel() {
        updateModel(dataSourceRef -> dataSourceRef
                .toBuilder()
                .source(sourceField.getText())
                .catalog(CatalogRef.parse(catalogField.getText()))
                .flow(flowField.getText())
//                .dimensions(Parser.onStringList(JDataSourceRef::split).parse(dimensionsField.getText()))
                .languages(languagesField.getText().isEmpty() ? Languages.ANY : Languages.parse(languagesField.getText()))
                .debug(debugBox.isSelected())
                .build());
    }

    @MightBePromoted
    private void updateModel(UnaryOperator<DataSourceRef> operator) {
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

    private interface DocumentAdapter extends DocumentListener {

        @Override
        default void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }
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