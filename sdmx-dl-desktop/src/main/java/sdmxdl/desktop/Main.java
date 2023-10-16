package sdmxdl.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import ec.util.various.swing.BasicSwingLauncher;
import internal.sdmxdl.desktop.DesktopWebFactory;

import javax.swing.*;

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
        mainComponent.setSdmxManager(DesktopWebFactory.loadManager());
        return mainComponent;
    }
}