package sdmxdl.swing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.WebSource;

public class DemoUtil {

    public static SdmxWebManager getSdmxWebManager() {
        return SdmxWebManager.ofServiceLoader().toBuilder()
                .onEvent(source -> (marker, message) ->
                        System.out.printf(Locale.ROOT, "[%s] (%s) %s%n", source.getId(), marker, message))
                .onError(source -> (marker, message, error) -> System.err.printf(
                        Locale.ROOT, "[%s] (%s) %s: %s%n", source.getId(), marker, message, error.getMessage()))
                .build()
                .warmupAsync();
    }

    public static Icon getIcon(SdmxWebManager manager, WebSource source, int size) {
        return ICONS.computeIfAbsent(
                source.getId() + "@" + size,
                key -> manager.getIcon(source, size)
                        .map(icon -> toIcon(icon, size))
                        .orElseGet(() -> new SdmxLogo(size)));
    }

    private static final Map<String, Icon> ICONS = new ConcurrentHashMap<>();

    private static Icon toIcon(SourceIcon icon, int size) {
        try {
            if (icon.isScalable()) {
                return new FlatSVGIcon(new ByteArrayInputStream(icon.getContent())).derive(size, size);
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(icon.getContent()));
            return image != null
                    ? new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
                    : new SdmxLogo(size);
        } catch (IOException ex) {
            return new SdmxLogo(size);
        }
    }
}
