package internal.sdmxdl.web.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSources;
import sdmxdl.web.spi.Registry;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum NoOpRegistry implements Registry {

    INSTANCE;

    @Override
    public @NonNull String getRegistryId() {
        return "NO_OP";
    }

    @Override
    public int getRegistryRank() {
        return UNKNOWN_REGISTRY_RANK;
    }

    @Override
    public @NonNull WebSources getSources(
            @NonNull List<Persistence> persistences,
            @Nullable Consumer<CharSequence> onEvent,
            @Nullable BiConsumer<CharSequence, IOException> onError) {
        return WebSources.EMPTY;
    }

    @Override
    public @NonNull Collection<String> getRegistryProperties() {
        return Collections.emptyList();
    }
}
