package sdmxdl;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import lombok.AccessLevel;
import lombok.NonNull;

/**
 * Contract for data providers that expose SDMX-related resources and data.
 *
 * <p>A provider is bound to a specific {@link Source} type and is responsible for
 * discovering metadata (databases, flows, structure) and retrieving data according to
 * request objects.
 *
 * @param <SOURCE> concrete source type handled by this provider
 */
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Provider<SOURCE extends Source> {

    private final @NonNull SdmxManager<SOURCE> manager;

    private final @NonNull SOURCE source;

    /**
     * Returns the source descriptor associated with this provider.
     *
     * @return non-null source metadata/configuration
     */
    public @NonNull SOURCE getSource() {
        return this.source;
    }

    /**
     * Performs a connectivity check against the underlying remote/local source.
     *
     * <p>If a specific endpoint was tested, it may be returned.
     *
     * @return optional URI of the tested endpoint when available
     * @throws IOException if the connectivity check fails due to I/O issues
     */
    public @NonNull Optional<URI> testConnection() throws IOException {
        try (Connection connection = manager.getConnection(source, Languages.ANY)) {
            return connection.testConnection();
        }
    }

    /**
     * Lists the features supported by this provider.
     *
     * <p>Features are returned in natural order.
     *
     * @return non-null sorted set of supported features
     * @throws IOException if capabilities cannot be retrieved due to I/O issues
     */
    public @NonNull SortedSet<Feature> getSupportedFeatures() throws IOException {
        try (Connection connection = manager.getConnection(source, Languages.ANY)) {
            return new TreeSet<>(connection.getSupportedFeatures());
        }
    }

    /**
     * Lists databases available for the given source request.
     *
     * <p>Entries are returned sorted by database reference string value.
     *
     * @param request source-level request parameters (non-null)
     * @return non-null sorted list of databases (possibly empty)
     * @throws IOException if database discovery fails due to I/O issues
     */
    public @NonNull List<Database> listDatabases(@NonNull SourceRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            return connection.getDatabases().stream()
                    .sorted(Comparator.comparing(o -> o.getRef().toString()))
                    .collect(toList());
        }
    }

    /**
     * Lists flows available in the requested database context.
     *
     * <p>Entries are returned sorted by flow reference string value.
     *
     * @param request database-level request parameters (non-null)
     * @return non-null sorted list of flows (possibly empty)
     * @throws IOException if flow discovery fails due to I/O issues
     */
    public @NonNull List<Flow> listFlows(@NonNull DatabaseRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            return connection.getFlows(request.getDatabase()).stream()
                    .sorted(Comparator.comparing(o -> o.getRef().toString()))
                    .collect(toList());
        }
    }

    /**
     * Retrieves structural metadata (dimensions, attributes, etc.) for a flow.
     *
     * @param request flow-level request parameters (non-null)
     * @return non-null metadata set for the targeted flow
     * @throws IOException if metadata retrieval fails due to I/O issues
     */
    public @NonNull MetaSet getMeta(@NonNull FlowRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            return connection.getMeta(request.getDatabase(), request.getFlow());
        }
    }

    /**
     * Retrieves data matching the given key request.
     *
     * @param request key/data query parameters (non-null)
     * @return non-null dataset containing matching observations/series
     * @throws IOException if data retrieval fails due to I/O issues
     */
    public @NonNull DataSet getData(@NonNull KeyRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            return connection.getData(request.getDatabase(), request.getFlow(), request.toQuery());
        }
    }
}
