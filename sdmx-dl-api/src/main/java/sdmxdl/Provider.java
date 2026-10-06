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
     * database reference string value and truncated to {@link DatabasesRequest#getEffectiveMaxResults()}.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofDatabases(java.util.Collection)} and returned best match
     * first, limited to {@link DatabasesRequest#getEffectiveMaxResults()} results.
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
     * flow reference string value and truncated to {@link FlowsRequest#getEffectiveMaxResults()}.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofFlows(java.util.Collection)} and returned best match first,
     * limited to {@link FlowsRequest#getEffectiveMaxResults()} results.
     *
     * <p>When {@link FlowsRequest#isPlainText()} is {@code true} and/or
     * {@link FlowsRequest#getTruncate()} is set, each flow's description is
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
        return !request.isPlainText() && request.getTruncate() == HasDescription.NO_DESCRIPTION_LIMIT
                ? UnaryOperator.identity()
                : flow -> flow.toBuilder()
                        .description(flow.getDescription(request.isPlainText(), request.getTruncate()))
                        .build();
    }

    /**
     * Lists dimensions of the structure associated with the requested flow.
     *
     * <p>When {@link DimensionsRequest#getQuery()} is empty, entries are returned sorted by
     * index and truncated to {@link DimensionsRequest#getEffectiveMaxResults()}.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofDimensions(java.util.Collection)} and returned best match
     * first, limited to {@link DimensionsRequest#getEffectiveMaxResults()} results.
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
     * component id and truncated to {@link AttributesRequest#getEffectiveMaxResults()}.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofAttributes(java.util.Collection)} and returned best match
     * first, limited to {@link AttributesRequest#getEffectiveMaxResults()} results.
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
     * order and truncated to {@link CodesRequest#getEffectiveMaxResults()}.
     *
     * <p>When a non-empty query is provided, entries are ranked by relevance using
     * {@link sdmxdl.web.Search#ofCodes(java.util.Map)} and returned best match first, limited
     * to {@link CodesRequest#getEffectiveMaxResults()} results.
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
     * <p>The requested dimension is resolved as described in {@link AvailabilityRequest}: by id,
     * then by zero-based index, or as the first wildcard dimension of the key when empty. The
     * resolved dimension must be a wildcard in the key.
     *
     * <p>Codes are returned sorted by id and mapped to their label as defined in the
     * dimension's codelist. When a code has no matching label, its value is {@code null}.
     *
     * <p>A partial key (fewer dimensions than the structure) is expanded with trailing
     * wildcards before being sent to the connection; see {@link Key#normalize(Structure)}.
     *
     * @param request dimension/key-level request parameters (non-null)
     * @return non-null availability of the resolved dimension (possibly without codes)
     * @throws IOException if the requested dimension cannot be resolved or is not a wildcard
     *                      in the key, or if metadata/data retrieval fails due to I/O issues
     */
    public @NonNull Availability listAvailability(@NonNull AvailabilityRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Structure structure =
                    connection.getMeta(request.getDatabase(), request.getFlow()).getStructure();
            Key key = request.getKey().normalize(structure);
            int index = resolveDimensionIndex(structure, key, request.getDimension());
            Dimension dimension = structure.getDimensions().get(index);

            Map<String, String> labels = dimension.getCodes();
            Availability.Builder result = Availability.builder().dimension(dimension.getId());
            for (String code :
                    connection.getAvailableDimensionCodes(request.getDatabase(), request.getFlow(), key, index)) {
                result.code(code, labels.get(code));
            }
            return result.build();
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
     * <p>A partial key (fewer dimensions than the structure) is expanded with trailing
     * wildcards before being sent to the connection; see {@link Key#normalize(Structure)}.
     *
     * @param request key/data query parameters (non-null)
     * @return non-null dataset containing matching observations/series
     * @throws IOException if data retrieval fails due to I/O issues
     */
    public @NonNull DataSet getData(@NonNull DataRequest request) throws IOException {
        try (Connection connection = manager.getConnection(source, request.getLanguages())) {
            Query query = request.toQuery();
            if (!Key.ALL.equals(query.getKey())) {
                Structure structure = connection
                        .getMeta(request.getDatabase(), request.getFlow())
                        .getStructure();
                query = query.toBuilder()
                        .key(query.getKey().normalize(structure))
                        .build();
            }
            return connection.getData(request.getDatabase(), request.getFlow(), query);
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
     * Resolves the dimension requested by {@link AvailabilityRequest#getDimension()} to its
     * position in the structure, and checks that it is a wildcard in the key.
     *
     * @param structure the structure of the flow
     * @param key the normalized key constraint
     * @param dimension a dimension id, a zero-based index or an empty value for the first wildcard
     * @return the zero-based position of the resolved dimension
     * @throws IOException if the dimension cannot be resolved or is not a wildcard in the key
     */
    private static int resolveDimensionIndex(Structure structure, Key key, String dimension) throws IOException {
        List<Dimension> dimensions = structure.getDimensions();
        if (dimension.equals(AvailabilityRequest.FIRST_WILDCARD_DIMENSION)) {
            for (int i = 0; i < dimensions.size(); i++) {
                if (isWildcard(key, i)) {
                    return i;
                }
            }
            throw new IOException("Cannot find a wildcard dimension in key '" + key + "'");
        }
        int result = indexOfDimension(dimensions, dimension);
        if (result == -1) {
            throw new IOException("Cannot find dimension '" + dimension + "'");
        }
        if (!isWildcard(key, result)) {
            throw new IOException("Expecting dimension '"
                    + dimensions.get(result).getId() + "' to be a wildcard in key '" + key + "'");
        }
        return result;
    }

    private static int indexOfDimension(List<Dimension> dimensions, String dimension) {
        for (int i = 0; i < dimensions.size(); i++) {
            if (dimensions.get(i).getId().equals(dimension)) {
                return i;
            }
        }
        try {
            int result = Integer.parseInt(dimension);
            return result >= 0 && result < dimensions.size() ? result : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static boolean isWildcard(Key key, int index) {
        return Key.ALL.equals(key) || index >= key.size() || key.isWildcard(index);
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
