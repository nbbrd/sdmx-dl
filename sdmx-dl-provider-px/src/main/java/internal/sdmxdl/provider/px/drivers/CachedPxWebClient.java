package internal.sdmxdl.provider.px.drivers;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.provider.Marker;
import sdmxdl.provider.TypedId;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@lombok.AllArgsConstructor
public final class CachedPxWebClient implements PxWebClient {

    @lombok.NonNull
    private final PxWebClient delegate;

    @lombok.NonNull
    private final Cache<DataRepository> cache;

    @lombok.NonNull
    private final URI endpoint;

    @lombok.NonNull
    private final Duration ttl;

    @lombok.Getter(lazy = true)
    private final TypedId<PxConfig> idOfConfig = initIdOfConfig(endpoint);

    @lombok.Getter(lazy = true)
    private final TypedId<List<Database>> idOfDatabases = initIdOfDatabases(endpoint);

    @lombok.Getter(lazy = true)
    private final TypedId<List<Flow>> idOfTables = initIdOfTables(endpoint);

    @lombok.Getter(lazy = true)
    private final TypedId<Structure> idOfMeta = initIdOfMeta(endpoint);

    public static TypedId<PxConfig> initIdOfConfig(URI base) {
        return TypedId.of(base,
                repo -> PxConfig.JSON_PARSER.asParser().parse(repo.getName()),
                config -> DataRepository.builder().name(PxConfig.JSON_FORMATTER.asFormatter().formatValueAsString(config).orElse("")).build()
        ).with("config");
    }

    private static TypedId<List<Database>> initIdOfDatabases(URI base) {
        return TypedId.of(base,
                DataRepository::getDatabases,
                databases -> DataRepository.builder().databases(databases).build()
        ).with("databases");
    }

    private static TypedId<List<Flow>> initIdOfTables(URI base) {
        return TypedId.of(base,
                DataRepository::getFlows,
                flows -> DataRepository.builder().flows(flows).build()
        ).with("tables");
    }

    private static TypedId<Structure> initIdOfMeta(URI base) {
        return TypedId.of(base,
                repo -> repo.getStructures().stream().findFirst().orElse(null),
                struct -> DataRepository.builder().structure(struct).build()
        ).with("meta");
    }

    @Override
    public @NonNull Marker getMarker() {
        return delegate.getMarker();
    }

    @Override
    public @NonNull URI ping() throws IOException {
        return delegate.ping();
    }

    @Override
    public @NonNull PxConfig getConfig() throws IOException {
        return getIdOfConfig()
                .load(cache, delegate::getConfig, ignore -> ttl);
    }

    @Override
    public @NonNull List<Database> getDataBases() throws IOException {
        return getIdOfDatabases()
                .load(cache, delegate::getDataBases, ignore -> ttl);
    }

    @Override
    public @NonNull List<Flow> getTables(@NonNull String dbId) throws IOException {
        return getIdOfTables()
                .with(dbId)
                .load(cache, () -> delegate.getTables(dbId), ignore -> ttl);
    }

    @Override
    public @NonNull Structure getMeta(@NonNull String dbId, @NonNull String tableId) throws IOException, IllegalArgumentException {
        return getIdOfMeta()
                .with(dbId)
                .with(tableId)
                .load(cache, () -> delegate.getMeta(dbId, tableId), ignore -> ttl);
    }

    @Override
    public @NonNull DataCursor getData(@NonNull String dbId, @NonNull String tableId, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
        return delegate.getData(dbId, tableId, dsd, key);
    }
}
