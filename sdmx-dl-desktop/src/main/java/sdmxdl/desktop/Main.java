package sdmxdl.desktop;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLightLaf;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import internal.sdmxdl.desktop.Config;
import lombok.NonNull;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;
import sdmxdl.DataflowRef;
import sdmxdl.ext.Registry;

import javax.swing.*;
import java.awt.*;

import static java.util.Collections.emptyList;

@lombok.experimental.UtilityClass
public class Main {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .lookAndFeel(FlatLightLaf.class.getName())
                .content(Main::create)
                .size(900, 600)
                .title("sdmx-dl")
                .launch();
    }

    static JComponent create() {
        MainComponent mainComponent = new MainComponent();
        mainComponent.setSdmxManager(Config.loadManager());
        mainComponent.setRegistry(Registry.ofServiceLoader());

        JToolBar toolBar = new JToolBar();

        JButton demoButton = new JButton(new DemoCommand().toAction(mainComponent));
        demoButton.setIcon(FontIcon.of(MaterialDesign.MDI_AUTO_FIX, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key)));
        demoButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_AUTO_FIX, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREY.key)));
        demoButton.setToolTipText("Add demo data sources");
        toolBar.add(demoButton);

        JButton addButton = new JButton(new AddCommand().toAction(mainComponent));
        addButton.setIcon(FontIcon.of(MaterialDesign.MDI_DATABASE_PLUS, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key)));
        addButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_DATABASE_PLUS, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREY.key)));
        addButton.setToolTipText("Add data source");
        toolBar.add(addButton);

        toolBar.addSeparator();

        JButton exportButton = new JButton(new ExportCommand().toAction(mainComponent));
        exportButton.setIcon(FontIcon.of(MaterialDesign.MDI_EXPORT, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key)));
        exportButton.setRolloverIcon(FontIcon.of(MaterialDesign.MDI_EXPORT, 24, UIManager.getColor(FlatIconColors.ACTIONS_GREY.key)));
        exportButton.setToolTipText("Export data set");
        toolBar.add(exportButton);

        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(toolBar, BorderLayout.NORTH);
        result.add(mainComponent, BorderLayout.CENTER);
        return result;
    }

    private static final class DemoCommand extends JCommand<MainComponent> {
        @Override
        public void execute(@NonNull MainComponent c) {
            c.getDataSources().addElement(new DataSourceRef("ECB", DataflowRef.parse("EXR"), emptyList()));
            c.getDataSources().addElement(new DataSourceRef("RNG", DataflowRef.parse("RNG"), emptyList()));
        }
    }

    private static final class AddCommand extends JCommand<MainComponent> {

        @Override
        public boolean isEnabled(@NonNull MainComponent component) {
            return false;
        }

        @Override
        public void execute(@NonNull MainComponent c) {
        }
    }

    private static final class ExportCommand extends JCommand<MainComponent> {

        @Override
        public boolean isEnabled(@NonNull MainComponent component) {
            return false;
        }

        @Override
        public void execute(@NonNull MainComponent c) {
        }
    }
}