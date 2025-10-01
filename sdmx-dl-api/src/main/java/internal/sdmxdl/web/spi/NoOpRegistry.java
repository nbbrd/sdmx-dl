package internal.sdmxdl.web.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSources;
import sdmxdl.web.spi.Registry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {
        return WebSources.EMPTY;
    }

    @Override
    public @NonNull Collection<String> getRegistryProperties() {
        return Collections.emptyList();
    }
}
