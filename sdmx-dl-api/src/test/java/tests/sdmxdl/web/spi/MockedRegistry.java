package tests.sdmxdl.web.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;
import sdmxdl.web.spi.Registry;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@lombok.Builder(toBuilder = true)
public class MockedRegistry implements Registry {

    @lombok.Builder.Default
    private final String id = "MOCKED_REGISTRY";

    @lombok.Builder.Default
    private final int rank = Registry.UNKNOWN_REGISTRY_RANK;

    @lombok.Singular
    private final List<WebSource> sources;

    @lombok.Singular
    private final List<String> properties;

    @Override
    public @NonNull String getRegistryId() {
        return id;
    }

    @Override
    public int getRegistryRank() {
        return rank;
    }

    @Override
    public @NonNull WebSources getSources(@NonNull List<Persistence> persistences, @Nullable Consumer<CharSequence> onEvent, @Nullable BiConsumer<CharSequence, IOException> onError) {
        return WebSources.builder().sources(sources).build();
    }

    @Override
    public @NonNull Collection<String> getRegistryProperties() {
        return properties;
    }
}
