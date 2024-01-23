package sdmxdl.web.spi;

import internal.sdmxdl.web.spi.NoOpRegistry;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.Persistence;
import sdmxdl.web.WebSources;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.RegistryLoader",
        fallback = NoOpRegistry.class
)
@ThreadSafe
public interface Registry {

    @ServiceId
    @NonNull String getRegistryId();

    @ServiceSorter(reverse = true)
    int getRegistryRank();

    @NonNull WebSources getSources(
            @NonNull List<Persistence> persistences,
            @Nullable Consumer<CharSequence> onEvent,
            @Nullable BiConsumer<CharSequence, IOException> onError);

    @NonNull Collection<String> getRegistryProperties();

    @StaticFactoryMethod
    static @NonNull Registry noOp() {
        return NoOpRegistry.INSTANCE;
    }

    int UNKNOWN_REGISTRY_RANK = -1;

    String REGISTRY_PROPERTY_PREFIX = "sdmxdl.registry";
}
