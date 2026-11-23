package tests.sdmxdl.web.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.spi.IconProvider;

@lombok.Builder(toBuilder = true)
public final class MockedIconProvider implements IconProvider {

    @lombok.Builder.Default
    private final String id = "MOCKED";

    @lombok.Builder.Default
    private final int rank = UNKNOWN_ICON_PROVIDER_RANK;

    @lombok.Singular
    private final Map<String, List<SourceIcon>> icons;

    @lombok.Singular
    private final Collection<String> failures;

    @Override
    public @NonNull String getIconProviderId() {
        return id;
    }

    @Override
    public int getIconProviderRank() {
        return rank;
    }

    @Override
    public @NonNull Collection<String> getIconNames() {
        return icons.keySet();
    }

    @Override
    public @NonNull List<SourceIcon> getIcons(@NonNull String name) throws IOException {
        if (failures.contains(name)) {
            throw new IOException("Boom");
        }
        return icons.getOrDefault(name, Collections.emptyList());
    }
}
