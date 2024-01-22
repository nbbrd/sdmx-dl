package internal.sdmxdl;

import lombok.NonNull;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;

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
    public boolean isFormatSupported(@NonNull Class<? extends HasPersistence> type) {
        return false;
    }

    @Override
    public @NonNull <T extends HasPersistence> FileFormat<T> getFormat(@NonNull Class<T> type) {
        return FileFormat.noOp();
    }
}
