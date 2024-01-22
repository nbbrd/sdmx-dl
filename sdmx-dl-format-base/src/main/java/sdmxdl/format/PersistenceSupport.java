package sdmxdl.format;

import lombok.NonNull;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.format.design.ServiceSupport;

import java.util.function.Predicate;

@ServiceSupport
@lombok.Builder(toBuilder = true)
public final class PersistenceSupport implements Persistence {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_PERSISTENCE_RANK;

    @lombok.Builder.Default
    private final Predicate<Class<? extends HasPersistence>> support = Persistence.noOp()::isFormatSupported;
    ;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Factory factory = Persistence.noOp()::getFormat;

    @Override
    public @NonNull String getPersistenceId() {
        return id;
    }

    @Override
    public int getPersistenceRank() {
        return rank;
    }

    @Override
    public boolean isFormatSupported(@NonNull Class<? extends HasPersistence> type) {
        return support.test(type);
    }

    @Override
    public @NonNull <T extends HasPersistence> FileFormat<T> getFormat(@NonNull Class<T> type) {
        return factory.create(type);
    }

    @FunctionalInterface
    public interface Factory {
        <T extends HasPersistence> @NonNull FileFormat<T> create(@NonNull Class<T> type);
    }
}
