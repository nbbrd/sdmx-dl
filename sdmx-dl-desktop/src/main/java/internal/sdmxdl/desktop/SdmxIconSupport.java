package internal.sdmxdl.desktop;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.MultiResolutionImageSupport;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.HasSdmxProperties;
import sdmxdl.swing.SdmxLogo;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.WebSource;

/**
 * Renders the icons of web sources as Swing icons, using the SDMX logo as fallback.
 * Icons are resolved by the manager without any network access and are cached per manager.
 */
@RequiredArgsConstructor
public final class SdmxIconSupport {

    public static SdmxIconSupport of(HasSdmxProperties<SdmxWebManager> properties) {
        return new SdmxIconSupport(properties::getSdmxManager);
    }

    public static SdmxIconSupport of(SdmxWebManager manager) {
        return new SdmxIconSupport(() -> manager);
    }

    private final Supplier<SdmxWebManager> manager;

    private @Nullable SdmxWebManager cacheOwner = null;

    private ConcurrentMap<IconKey, Icon> cache = new ConcurrentHashMap<>();

    public Icon getIcon(DataSourceRef dataSourceRef, int size, Runnable onUpdate) {
        return getIcon(dataSourceRef.toWebSource(manager.get()), size);
    }

    public Icon getIcon(WebSource source, int size, Runnable onUpdate) {
        return getIcon(source, size);
    }

    public Icon getIcon(@NonNull WebSource source, int size) {
        SdmxWebManager current = manager.get();
        return getCache(current)
                .computeIfAbsent(new IconKey(source, size), key -> toIcon(current.getIcons(source), size));
    }

    private synchronized ConcurrentMap<IconKey, Icon> getCache(SdmxWebManager current) {
        if (cacheOwner != current) {
            cacheOwner = current;
            cache = new ConcurrentHashMap<>();
        }
        return cache;
    }

    @lombok.Value
    private static class IconKey {
        WebSource source;
        int size;
    }

    /**
     * Converts source icons into a Swing icon of the specified size.
     * Raster icons are combined with a double-size variant to look sharp on HiDPI screens.
     *
     * @param icons a non-null list of source icons
     * @param size  a positive size in pixels
     * @return a non-null icon, the SDMX logo if no icon can be rendered
     */
    public static @NonNull Icon toIcon(@NonNull List<SourceIcon> icons, int size) {
        Optional<SourceIcon> base = SourceIcon.findBest(icons, size);
        if (!base.isPresent()) {
            return new SdmxLogo(size);
        }
        try {
            if (base.get().isScalable()) {
                return toSvgIcon(base.get(), size);
            }
            Image image = toImage(base.get(), size);
            SourceIcon hiDpi = SourceIcon.findBest(icons, size * 2).orElse(base.get());
            if (!hiDpi.equals(base.get()) && MultiResolutionImageSupport.isAvailable()) {
                image = MultiResolutionImageSupport.create(0, image, toImage(hiDpi, size * 2));
            }
            return new ImageIcon(image);
        } catch (IOException | RuntimeException ex) {
            return new SdmxLogo(size);
        }
    }

    private static FlatSVGIcon toSvgIcon(SourceIcon icon, int size) throws IOException {
        return new FlatSVGIcon(new ByteArrayInputStream(icon.getContent())).derive(size, size);
    }

    private static Image toImage(SourceIcon icon, int size) throws IOException {
        if (icon.isScalable()) {
            return toSvgIcon(icon, size).getImage();
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(icon.getContent()));
        if (image == null) {
            throw new IOException("Cannot decode PNG icon");
        }
        return image.getWidth() == size && image.getHeight() == size ? image : scale(image, size);
    }

    private static BufferedImage scale(BufferedImage image, int size) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(image, 0, 0, size, size, null);
        } finally {
            g2.dispose();
        }
        return result;
    }
}
