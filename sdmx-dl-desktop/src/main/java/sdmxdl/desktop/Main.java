package sdmxdl.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import ec.util.various.swing.BasicSwingLauncher;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import java.io.IOException;

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
        mainComponent.setSdmxManager(loadManager());
        return mainComponent;
    }

    private static SdmxWebManager loadManager() {
        System.setProperty("enableRngDriver", "true");
        System.setProperty("enableFileDriver", "true");
        System.setProperty("enablePxWebDriver", "true");
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(Main::printEvent)
                .onError(Main::printError)
                .onRegistryEvent(Main::printRegistryEvent)
                .onRegistryError(Main::printRegistryError)
                .build();
    }

    private static void printEvent(WebSource source, String marker, CharSequence message) {
        System.out.println("[" + source.getId() + "] (" + marker + ") " + message);
    }

    private static void printError(WebSource source, String marker, CharSequence message, IOException error) {
        System.err.println("[" + source.getId() + "] (" + marker + ") " + message + ": " + error.getMessage());
    }

    private static void printRegistryEvent(CharSequence message) {
        System.out.println("[REG] (" + "" + ") " + message);
    }

    private static void printRegistryError(CharSequence message, IOException error) {
        System.err.println("[REG] (" + "" + ") " + message + ": " + error.getMessage());
    }
}