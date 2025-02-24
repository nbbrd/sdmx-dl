package internal.sdmxdl;

import lombok.NonNull;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public enum NoOpPersistence implements Persistence {

    INSTANCE;

    @Override
    public @NonNull String getPersistenceId() {
        return "NO_OP";
    }

    @Override
    public int getPersistenceRank() {
        return UNKNOWN_PERSISTENCE_RANK;
    }

    @Override
    public @NonNull Set<Class<? extends HasPersistence>> getFormatSupportedTypes() {
        return Collections.emptySet();
    }

    @Override
    public @NonNull <T extends HasPersistence> FileFormat<T> getFormat(@NonNull Class<T> type) {
        return FileFormat.noOp();
    }

    @Override
    public @NonNull Collection<String> getPersistenceProperties() {
        return Collections.emptyList();
    }
}
