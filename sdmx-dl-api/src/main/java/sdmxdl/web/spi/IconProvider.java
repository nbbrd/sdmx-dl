package sdmxdl.web.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.web.SourceIcon;

/**
 * Provides the bundled icons targeted by URIs such as {@code sdmx-dl:icon/<name>}.
 * <p>
 * Icon names match {@code [A-Za-z0-9_-]+}. A name may have several variants
 * (one scalable icon and/or raster icons of different sizes).
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, loaderName = "internal.{{canonicalName}}Loader")
@ThreadSafe
public interface IconProvider {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getIconProviderId();

    @ServiceSorter(reverse = true)
    int getIconProviderRank();

    /**
     * Gets the names of the icons provided by this provider.
     *
     * @return a non-null collection of names
     */
    @NonNull Collection<String> getIconNames();

    /**
     * Gets all the variants of an icon.
     *
     * @param name a non-null icon name
     * @return a non-null list of icons, empty if the name is unknown
     * @throws IOException if the icons cannot be loaded
     */
    @NonNull List<SourceIcon> getIcons(@NonNull String name) throws IOException;

    int UNKNOWN_ICON_PROVIDER_RANK = -1;
}
