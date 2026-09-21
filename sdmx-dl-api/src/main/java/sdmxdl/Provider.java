package sdmxdl;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import sdmxdl.web.Search;

/**
 * Facade that binds a {@link SdmxManager} to a specific {@link Source} and exposes
 * SDMX-related resources and data through simple request/response methods.
 *
 * <p>A provider is bound to a specific {@link Source} type and is responsible for
 * discovering metadata (databases, flows, structure) and retrieving data according to
 * request objects.
 *
 * <p>Every method opens a short-lived {@link Connection} via {@link
 * SdmxManager#getConnection(Source, Languages)}, uses it to satisfy the request, then closes
 * it (even if an exception occurs), so instances of this class are cheap to keep around and
 * do not need to be closed themselves.
 *
 * <p>Instances are typically obtained through {@link SdmxManager#using(Source)} rather than
 * constructed directly.
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
                            .limit(request.getEffectiveMaxResults())
                            .collect(toList())
                    : Search.ofDatabases(result).search(request.getQuery(), request.getEffectiveMaxResults()).stream()
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
     * <p>When {@link FlowsRequest#isPlainDescription()} is {@code true} and/or
     * {@link FlowsRequest#getMaxDescriptionLength()} is set, each flow's description is
     * cleaned (markup stripped) and/or truncated accordingly; see {@link HasDescription}.
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
                            .limit(request.getEffectiveMaxResults())
                            .map(flowTransformer(request))
                            .collect(toList())
                    : Search.ofFlows(result).search(request.getQuery(), request.getEffectiveMaxResults()).stream()
                            .map(Search.Result::getItem)
                            .map(flowTransformer(request))
                            .collect(toList());
        }
    }

    /**
     * Builds a function that applies the description-related options of the given request
     * (plain text conversion and/or truncation) to a flow.
     *
     * <p>Returns the identity function when neither option is active, to avoid rebuilding
     * every flow unnecessarily.
     *
     * @param request the flow-level request carrying description options
     * @return a function transforming a flow's description according to the request
     */
    private static UnaryOperator<Flow> flowTransformer(FlowsRequest request) {
        return !request.isPlainDescription() && request.getMaxDescriptionLength() == HasDescription.NO_DESCRIPTION_LIMIT
                ? UnaryOperator.identity()
                : flow -> flow.toBuilder()
                        .description(
                                flow.getDescription(request.isPlainDescription(), request.getMaxDescriptionLength()))
                        .build();
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
                    ? result.stream()
                            .limit(request.getEffectiveMaxResults())
                            .map(Provider::removeCodes)
                            .collect(toList())
                    : Search.ofDimensions(result).search(request.getQuery(), request.getEffectiveMaxResults()).stream()
                            .map(Search.Result::getItem)
                            .map(Provider::removeCodes)
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
                            .limit(request.getEffectiveMaxResults())
                            .map(Provider::removeCodes)
                            .collect(toList())
                    : Search.ofAttributes(result).search(request.getQuery(), request.getEffectiveMaxResults()).stream()
                            .map(Search.Result::getItem)
                            .map(Provider::removeCodes)
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
                            .limit(request.getEffectiveMaxResults())
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new))
                    : Search.ofCodes(result).search(request.getQuery(), request.getEffectiveMaxResults()).stream()
                            .map(Search.Result::getItem)
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        }
    }

    /**
     * Lists the codes that are actually available for the requested dimension, given the
     * requested key constraints, within the structure of the requested flow.
     *
     * <p>Entries are returned sorted by code id and mapped to their label as defined in the
     * dimension's codelist. When a code has no matching label, its value is {@code null}.
     *
     * @param request dimension/key-level request parameters (non-null)
     * @return non-null sorted map of available code id to code label (possibly empty)
     * @throws IOException if the requested dimension cannot be found, or if metadata/data
     *                      retrieval fails due to I/O issues
     */
    public @NonNull SortedMap<String, String> listAvailability(@NonNull AvailabilityRequest request)
            throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Dimension dimension =
                    connection.getMeta(request.getDatabase(), request.getFlow()).getStructure().getDimensions().stream()
                            .filter(o -> o.getId().equals(request.getDimension()))
                            .findFirst()
                            .orElseThrow(
                                    () -> new IOException("Cannot find dimension '" + request.getDimension() + "'"));

            Map<String, String> codes = dimension.getCodes();
            // NB: a plain Collectors.toMap(..., TreeMap::new) would throw a NullPointerException
            // as soon as a returned code has no matching label (its Map.merge call rejects null
            // values), so the map is built manually to keep the label as null in that case.
            SortedMap<String, String> result = new TreeMap<>();
            for (String code : connection.getAvailableDimensionCodes(
                    request.getDatabase(), request.getFlow(), request.getKey(), dimension.getIndex())) {
                result.putIfAbsent(code, codes.get(code));
            }
            return result;
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

    /**
     * Finds, in the structure of the requested flow, the codelist (as an id-to-label map) of
     * the dimension or attribute whose id matches {@link CodesRequest#getConcept()}.
     *
     * @param connection the connection used to retrieve the flow's structure
     * @param request the concept-level request identifying the target component
     * @return non-null map of code id to code label for the matching component
     * @throws IOException if no dimension or attribute matches the requested concept, or if
     *                      structure retrieval fails due to I/O issues
     */
    private static Map<String, String> loadComponent(Connection connection, CodesRequest request) throws IOException {
        Structure dsd =
                connection.getMeta(request.getDatabase(), request.getFlow()).getStructure();
        return Stream.concat(dsd.getDimensions().stream(), dsd.getAttributes().stream())
                .filter(component -> component.getId().equals(request.getConcept()))
                .map(Component::getCodes)
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find concept '" + request.getConcept() + "'"));
    }

    /**
     * Returns a copy of the given dimension with its codelist's codes cleared, or the same
     * dimension unchanged when it has no codelist.
     *
     * @param dimension the dimension to strip codes from
     * @return the dimension without its codelist's codes
     */
    @MightBePromoted
    static Dimension removeCodes(Dimension dimension) {
        Codelist codelist = dimension.getCodelist();
        return codelist != null
                ? dimension.toBuilder()
                        .codelist(codelist.toBuilder().clearCodes().build())
                        .build()
                : dimension;
    }

    /**
     * Returns a copy of the given attribute with its codelist's codes cleared, or the same
     * attribute unchanged when it has no codelist.
     *
     * @param attribute the attribute to strip codes from
     * @return the attribute without its codelist's codes
     */
    @MightBePromoted
    static Attribute removeCodes(Attribute attribute) {
        Codelist codelist = attribute.getCodelist();
        return codelist != null
                ? attribute.toBuilder()
                        .codelist(codelist.toBuilder().clearCodes().build())
                        .build()
                : attribute;
    }
}
