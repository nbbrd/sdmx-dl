package sdmxdl.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import ec.util.various.swing.BasicSwingLauncher;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@lombok.experimental.UtilityClass
public class Main {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .lookAndFeel(FlatLightLaf.class.getName())
                .content(Main::create)
                .size(900, 600)
                .title("sdmx-dl")
                .icons(Main::getIcons)
                .launch();
    }

    private static List<Image> getIcons() {
        return IntStream.of(256, 128, 64, 48, 32, 16)
                .mapToObj(size -> new FlatSVGIcon("sdmxdl/desktop/SDMX_logo.svg", size, size))
                .map(FlatSVGIcon::getImage)
                .collect(toList());
    }

    static JComponent create() {
        Sdmxdl.INSTANCE.setSdmxManager(loadManager());
        MainComponent result = new MainComponent();
        result.load();

        result.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(result);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        result.store();
                        frame.dispose();
                    }
                });
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        return result;
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
                .build()
                .warmupAsync();
    }

    private static EventListener printEvent(WebSource source) {
        return (marker, message) -> {
            SwingUtilities.invokeLater(() -> Sdmxdl.INSTANCE.getEventList().addElement(new Event(source.getId(), marker, message.toString())));
            System.out.println("[" + source.getId() + "] (" + marker + ") " + message);
        };
    }

    private static ErrorListener printError(WebSource source) {
        return (marker, message, error) -> {
            System.err.println("[" + source.getId() + "] (" + marker + ") " + message + ": " + error.getMessage());
            error.printStackTrace(System.err);
        };
    }

    private static void printRegistryEvent(String marker, CharSequence message) {
        System.out.println("[REG] (" + marker + ") " + message);
    }

    private static void printRegistryError(String marker, CharSequence message, IOException error) {
        System.err.println("[REG] (" + marker + ") " + message + ": " + error.getMessage());
    }
}