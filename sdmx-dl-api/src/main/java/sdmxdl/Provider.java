package sdmxdl;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.web.Search;

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
     * <p>When {@link DatabasesRequest#getQuery()} is empty, entries are returned sorted by
     * database reference string value and truncated to {@link DatabasesRequest#getMaxResults()}
     * when a positive limit is set.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofDatabases(java.util.Collection)} and returned best match
     * first, limited to {@link DatabasesRequest#getMaxResults()} results.
     *
     * @param request source-level request parameters (non-null)
     * @return non-null list of databases (possibly empty), sorted or ranked depending on the query
     * @throws IOException if database discovery fails due to I/O issues
     */
    public @NonNull List<Database> listDatabases(@NonNull DatabasesRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Collection<Database> result = connection.getDatabases();
            return request.getQuery().isEmpty()
                    ? result.stream()
                            .sorted(comparing(HasReference::getRef))
                            .limit(max(request))
                            .collect(toList())
                    : Search.ofDatabases(result).search(request.getQuery(), max(request)).stream()
                            .map(Search.Result::getItem)
                            .collect(toList());
        }
    }

    /**
     * Lists flows available in the requested database context.
     *
     * <p>When {@link FlowsRequest#getQuery()} is empty, entries are returned sorted by
     * flow reference string value and truncated to {@link FlowsRequest#getMaxResults()}
     * when a positive limit is set.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofFlows(java.util.Collection)} and returned best match first,
     * limited to {@link FlowsRequest#getMaxResults()} results.
     *
     * @param request database-level request parameters (non-null)
     * @return non-null list of flows (possibly empty), sorted or ranked depending on the query
     * @throws IOException if flow discovery fails due to I/O issues
     */
    public @NonNull List<Flow> listFlows(@NonNull FlowsRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Collection<Flow> result = connection.getFlows(request.getDatabase());
            return request.getQuery().isEmpty()
                    ? result.stream()
                            .sorted(comparing(HasReference::getRef))
                            .limit(max(request))
                            .collect(toList())
                    : Search.ofFlows(result).search(request.getQuery(), max(request)).stream()
                            .map(Search.Result::getItem)
                            .collect(toList());
        }
    }

    /**
     * Lists dimensions of the structure associated with the requested flow.
     *
     * <p>When {@link DimensionsRequest#getQuery()} is empty, entries are returned sorted by
     * index and truncated to {@link DimensionsRequest#getMaxResults()} when a positive
     * limit is set.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofDimensions(java.util.Collection)} and returned best match
     * first, limited to {@link DimensionsRequest#getMaxResults()} results.
     *
     * @param request component-level request parameters (non-null)
     * @return non-null list of dimensions (possibly empty), sorted or ranked depending on the query
     * @throws IOException if structure retrieval fails due to I/O issues
     */
    public @NonNull List<Dimension> listDimensions(@NonNull DimensionsRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            List<Dimension> result = connection
                    .getMeta(request.getDatabase(), request.getFlow())
                    .getStructure()
                    .getDimensions();
            return request.getQuery().isEmpty()
                    ? result.stream().limit(max(request)).collect(toList())
                    : Search.ofDimensions(result).search(request.getQuery(), max(request)).stream()
                            .map(Search.Result::getItem)
                            .collect(toList());
        }
    }

    /**
     * Lists attributes of the structure associated with the requested flow.
     *
     * <p>When {@link AttributesRequest#getQuery()} is empty, entries are returned sorted by
     * component id and truncated to {@link AttributesRequest#getMaxResults()} when a positive
     * limit is set.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofAttributes(java.util.Collection)} and returned best match
     * first, limited to {@link AttributesRequest#getMaxResults()} results.
     *
     * @param request component-level request parameters (non-null)
     * @return non-null list of attributes (possibly empty), sorted or ranked depending on the query
     * @throws IOException if structure retrieval fails due to I/O issues
     */
    public @NonNull List<Attribute> listAttributes(@NonNull AttributesRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Set<Attribute> result = connection
                    .getMeta(request.getDatabase(), request.getFlow())
                    .getStructure()
                    .getAttributes();
            return request.getQuery().isEmpty()
                    ? result.stream()
                            .sorted(comparing(Component::getId))
                            .limit(max(request))
                            .collect(toList())
                    : Search.ofAttributes(result).search(request.getQuery(), max(request)).stream()
                            .map(Search.Result::getItem)
                            .collect(toList());
        }
    }

    /**
     * Lists codes of the codelist associated with the requested concept (a dimension or
     * attribute id) within the structure of the requested flow.
     *
     * <p>When no dimension or attribute matches {@link CodesRequest#getConcept()}, or when
     * the matching component is not coded, an empty map is returned.
     *
     * <p>When {@link CodesRequest#getQuery()} is empty, entries are returned in codelist
     * order and truncated to {@link CodesRequest#getMaxResults()} when a positive limit is
     * set.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofCodes(java.util.Map)} and returned best match first, limited
     * to {@link CodesRequest#getMaxResults()} results.
     *
     * @param request concept-level request parameters (non-null)
     * @return non-null map of code id to code label (possibly empty), ordered or ranked depending on the query
     * @throws IOException if structure retrieval fails due to I/O issues
     */
    public @NonNull Map<String, String> listCodes(@NonNull CodesRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Map<String, String> result = loadComponent(connection, request);
            return request.getQuery().isEmpty()
                    ? result.entrySet().stream()
                            .limit(max(request))
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new))
                    : Search.ofCodes(result).search(request.getQuery(), max(request)).stream()
                            .map(Search.Result::getItem)
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        }
    }

    /**
     * Retrieves structural metadata (dimensions, attributes, etc.) for a flow.
     *
     * @param request flow-level request parameters (non-null)
     * @return non-null metadata set for the targeted flow
     * @throws IOException if metadata retrieval fails due to I/O issues
     */
    public @NonNull MetaSet getMeta(@NonNull MetaRequest request) throws IOException {
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
    public @NonNull DataSet getData(@NonNull DataRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            return connection.getData(request.getDatabase(), request.getFlow(), request.toQuery());
        }
    }

    private static int max(HasLimit request) {
        return request.getMaxResults() > 0 ? request.getMaxResults() : Integer.MAX_VALUE;
    }

    private static Map<String, String> loadComponent(Connection connection, CodesRequest request) throws IOException {
        Structure dsd =
                connection.getMeta(request.getDatabase(), request.getFlow()).getStructure();
        return Stream.concat(dsd.getDimensions().stream(), dsd.getAttributes().stream())
                .filter(component -> component.getId().equals(request.getConcept()))
                .map(Component::getCodes)
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find concept '" + request.getConcept() + "'"));
    }
}
