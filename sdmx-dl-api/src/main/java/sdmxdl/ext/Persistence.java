package sdmxdl.ext;

import internal.sdmxdl.NoOpPersistence;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.HasPersistence;

import java.util.Collection;
import java.util.Set;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Persistence {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getPersistenceId();

    @ServiceSorter(reverse = true)
    int getPersistenceRank();

    @NonNull Set<Class<? extends HasPersistence>> getFormatSupportedTypes();

    @NonNull <T extends HasPersistence> FileFormat<T> getFormat(@NonNull Class<T> type);

    @NonNull Collection<String> getPersistenceProperties();

    int UNKNOWN_PERSISTENCE_RANK = -1;

    String PERSISTENCE_PROPERTY_PREFIX = "sdmxdl.persistence";

    @StaticFactoryMethod
    static @NonNull Persistence noOp() {
        return NoOpPersistence.INSTANCE;
    }
}
