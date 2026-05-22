package sdmxdl.web.spi;

import internal.sdmxdl.web.spi.NoOpRegistry;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSources;

import java.util.Collection;
import java.util.List;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpRegistry.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Registry {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getRegistryId();

    @ServiceSorter(reverse = true)
    int getRegistryRank();

    @NonNull WebSources getSources(
            @NonNull List<Persistence> persistences,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);

    @NonNull Collection<String> getRegistryPropertyNames();

    @StaticFactoryMethod
    static @NonNull Registry noOp() {
        return NoOpRegistry.INSTANCE;
    }

    int UNKNOWN_REGISTRY_RANK = -1;

    String REGISTRY_PROPERTY_PREFIX = "sdmxdl.registry";
}
