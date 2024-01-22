package sdmxdl.ext;

import internal.sdmxdl.NoOpPersistence;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.HasPersistence;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Persistence {

    @ServiceId
    @NonNull String getPersistenceId();

    @ServiceSorter(reverse = true)
    int getPersistenceRank();

    boolean isFormatSupported(@NonNull Class<? extends HasPersistence> type);

    @NonNull <T extends HasPersistence> FileFormat<T> getFormat(@NonNull Class<T> type);

    int UNKNOWN_PERSISTENCE_RANK = -1;

    @StaticFactoryMethod
    static @NonNull Persistence noOp() {
        return NoOpPersistence.INSTANCE;
    }
}
