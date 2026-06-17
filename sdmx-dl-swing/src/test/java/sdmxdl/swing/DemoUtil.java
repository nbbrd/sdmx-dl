package sdmxdl.swing;

import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import sdmxdl.Confidentiality;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import java.net.URL;
import java.util.Locale;

public class DemoUtil {

    public static SdmxWebManager getSdmxWebManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(source -> (marker, message) ->
                        System.out.printf(Locale.ROOT, "[%s] (%s) %s%n", source.getId(), marker, message))
                .onError(source -> (marker, message, error) ->
                        System.err.printf(Locale.ROOT, "[%s] (%s) %s: %s%n", source.getId(), marker, message, error.getMessage()))
                .build()
                .warmupAsync();
    }

    public static Icon getFavicon(WebSource source, Runnable onUpdate, int size) {
        URL website = source.getWebsite();
        return website != null && !isForbidden(source.getConfidentiality())
                ? FAVICONS.getOrDefault(FaviconRef.of(DomainName.of(website), size), onUpdate, getDefaultIcon(size))
                : getDefaultIcon(size);
    }

    private static boolean isForbidden(Confidentiality confidentiality) {
        return confidentiality.compareTo(MAX_CONFIDENTIALITY) > 0;
    }

    private static final Confidentiality MAX_CONFIDENTIALITY = Confidentiality.PUBLIC;

    private static final FaviconSupport FAVICONS = FaviconSupport.ofServiceLoader();
    private static final Icon DEFAULT_ICON_16 = new SdmxLogo(16);
    private static final Icon DEFAULT_ICON_32 = new SdmxLogo(32);

    private static Icon getDefaultIcon(int size) {
        switch (size) {
            case 16:
                return DEFAULT_ICON_16;
            case 32:
                return DEFAULT_ICON_32;
            default:
                return new SdmxLogo(size);
        }
    }
}
