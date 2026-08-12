package sdmxdl.provider.px.drivers;

import internal.sdmxdl.provider.px.drivers.*;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.NonNegative;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.RateLimiter;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.*;
import sdmxdl.provider.ri.http.CookieDecoration;
import sdmxdl.provider.ri.http.HttpFactory;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.ri.http.RateLimitingDecoration;
import sdmxdl.provider.web.ConnectionFactory;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static nbbrd.io.Resource.newInputStream;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.commaSeparatedProperty;

@DirectImpl
@ServiceProvider
public final class PxWebDriver implements Driver {

    @VisibleForTesting
    static final String PX_PXWEB = "PX_PXWEB";

    @PropertyDefinition
    static final Property<List<String>> VERSIONS_PROPERTY =
            commaSeparatedProperty(DRIVER_PROPERTY_PREFIX + ".versions", emptyList());

    @PropertyDefinition
    static final Property<List<String>> LANGUAGES_PROPERTY =
            commaSeparatedProperty(DRIVER_PROPERTY_PREFIX + ".languages", emptyList());

    @PropertyDefinition
    static final BooleanProperty ENABLE_PROPERTY =
            BooleanProperty.of("enablePxWebDriver", false);

    /**
     * Strategy used to list the tables (flows) of a database.
     * <ul>
     *     <li>{@link #FLAT}: single fast query ({@code ?query=*&filter=*}); works only on
     *     servers that support the search endpoint and keep a fresh index.</li>
     *     <li>{@link #TREE}: reliable but slower folder-tree navigation.</li>
     *     <li>{@link #AUTO}: try {@code FLAT} first and fall back to {@code TREE} when the
     *     search endpoint is unsupported (e.g. HTTP 400) or returns nothing.</li>
     * </ul>
     * Note that {@code AUTO} cannot detect a <em>stale</em> search index (HTTP 200 with
     * outdated entries); such sources must be pinned to {@code TREE}.
     */
    @VisibleForTesting
    public enum TableListing {
        AUTO, FLAT, TREE
    }

    @PropertyDefinition
    static final Property<TableListing> TABLE_LISTING_PROPERTY =
            Property.of(DRIVER_PROPERTY_PREFIX + ".tableListing", TableListing.AUTO, Parser.onEnum(TableListing.class), nbbrd.io.text.Formatter.onEnum());

    static final String DEFAULT_VERSION = "v1";

    static final String VERSION_VARIABLE = UriTemplate.getVariable("version");

    static final String DEFAULT_LANG = "en";

    static final String LANGUAGE_VARIABLE = UriTemplate.getVariable("lang");

    /**
     * Upper bound on how long a request may wait for a rate-limiting permit before failing.
     */
    static final Duration RATE_LIMIT_MAX_WAIT = Duration.ofSeconds(120);

    /**
     * Fallback used when the server-declared rate limit cannot be determined; it never
     * throttles proactively and only reacts to {@code 429} responses.
     */
    @VisibleForTesting
    static final RateLimiter FALLBACK_RATE_LIMITER = RateLimiter.unlimitedAdaptive(RATE_LIMIT_MAX_WAIT);

    /**
     * Builds a fixed rate limiter from the server-declared configuration ({@code maxCalls} per
     * {@code timeWindow} seconds), falling back to a non-throttling limiter when the configuration
     * is invalid.
     */
    @VisibleForTesting
    static @NonNull RateLimiter toRateLimiter(@NonNull PxConfig config) {
        if (config.getMaxCalls() <= 0 || config.getTimeWindow() <= 0) {
            return FALLBACK_RATE_LIMITER;
        }
        return RateLimiter.fixed(
                config.getMaxCalls() / (double) config.getTimeWindow(),
                config.getMaxCalls(),
                RATE_LIMIT_MAX_WAIT);
    }

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(PX_PXWEB)
            .rank(NATIVE_DRIVER_RANK)
            .availability(ENABLE_PROPERTY::get)
            .connector(new PxWebConnectionFactory())
            .sources(IOSupplier.unchecked(PxWebDriver::loadDefaultSources).get())
            .build();

    private static List<WebSource> loadDefaultSources() throws IOException {
        Map<String, Websites.Website> websiteByHost = Websites.PARSER.parseResource(PxWebDriver.class, "websites.csv", UTF_8);
        try (InputStream stream = newInputStream(PxWebDriver.class, "api.json")) {
            return PxWebSourcesFormat.INSTANCE.parseStream(stream)
                    .getSources()
                    .stream()
                    .map(source -> applyWebsite(source, websiteByHost.get(source.getEndpoint().getHost())))
                    .collect(toList());
        }
    }

    private static WebSource applyWebsite(WebSource source, Websites.Website website) {
        if (website == null) {
            return source;
        }
        WebSource.Builder result = source.toBuilder().website(website.getUrl());
        if (website.getListing() != null) {
            result.propertyOf(TABLE_LISTING_PROPERTY, website.getListing());
        }
        if (website.isCookie()) {
            result.propertyOf(CookieDecoration.COOKIE_PROPERTY, true);
        }
        return result.build();
    }

    @VisibleForTesting
    static final class PxWebConnectionFactory implements ConnectionFactory {

        public final HttpFactory httpFactory = HttpManager.getHttpFactory();

        @Override
        public @NonNull List<BaseProperty> getConnectionProperties() {
            // Hide the rate-limiting toggle: this driver forces it on and supplies a
            // server-declared per-host limiter internally (see connect).
            List<BaseProperty> httpProperties = httpFactory.getHttpClientProperties()
                    .stream()
                    .filter(property -> !property.getKey().equals(RateLimitingDecoration.RATE_LIMITING_PROPERTY.getKey()))
                    .collect(toList());
            return PropertiesSupport.merge(
                    httpProperties,
                    VERSIONS_PROPERTY,
                    LANGUAGES_PROPERTY,
                    TABLE_LISTING_PROPERTY,
                    CACHE_TTL_PROPERTY
            );
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
            Marker marker = HasMarker.of(source);
            URI endpoint = getFullEndpoint(source, languages);
            TableListing configuredListing = TABLE_LISTING_PROPERTY.get(source.getProperties());
            TableListing listing = configuredListing != null ? configuredListing : TableListing.AUTO;

            Cache<DataRepository> cache = context.getDriverCache(source);
            URI cacheBase = getCachedClientBaseURI(source, languages);
            Duration ttl = Duration.ofMillis(CACHE_TTL_PROPERTY.get(source.getProperties()));

            // Build the client through the shared HTTP pipeline so that rate limiting is applied
            // in the correct order (before 429 responses are turned into exceptions).
            HttpClient httpClient = httpFactory.createHttpClient(source, context);

            // Enforce the server-declared limit (see PxWeb config endpoint) by registering it as
            // the per-host limiter used by the shared rate-limiting decoration.
            String host = endpoint.getHost();
            if (host != null) {
                RateLimitingDecoration.putRateLimiterIfAbsent(host, resolveRateLimiter(cache, cacheBase, ttl, httpClient, endpoint));
            }

            PxWebClient client = new CachedPxWebClient(
                    new DefaultPxWebClient(marker, endpoint, httpClient, listing),
                    cache,
                    cacheBase,
                    ttl
            );

            return new PxWebConnection(client);
        }

        // Resolves the server-declared rate limit from the driver cache, fetching and caching it
        // once when absent; falls back to a non-throttling limiter when it cannot be determined.
        private static RateLimiter resolveRateLimiter(Cache<DataRepository> cache, URI cacheBase, Duration ttl, HttpClient httpClient, URI endpoint) {
            try {
                PxConfig config = CachedPxWebClient.initIdOfConfig(cacheBase)
                        .load(cache, () -> DefaultPxWebClient.fetchConfig(httpClient, endpoint), ignore -> ttl);
                return toRateLimiter(config);
            } catch (IOException ex) {
                return FALLBACK_RATE_LIMITER;
            }
        }

        @VisibleForTesting
        static @NonNull URI getFullEndpoint(@NonNull WebSource source, @NonNull Languages languages) throws IOException {
            Map<String, String> variables = new HashMap<>();
            variables.put(VERSION_VARIABLE, resolveVersion(source));
            variables.put(LANGUAGE_VARIABLE, resolveLanguage(source, languages));
            try {
                return UriTemplate.expand(source.getEndpoint(), variables);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }

        private static String resolveVersion(WebSource source) {
            List<String> versions = VERSIONS_PROPERTY.get(source.getProperties());
            return versions != null && !versions.isEmpty() ? versions.get(0) : DEFAULT_VERSION;
        }

        private static String resolveLanguage(WebSource source, Languages requested) {
            List<String> availableLanguages = LANGUAGES_PROPERTY.get(source.getProperties());
            String language = availableLanguages != null ? lookupLanguage(availableLanguages, requested) : null;
            return language != null ? language : DEFAULT_LANG;
        }

        @VisibleForTesting
        static @Nullable String lookupLanguage(@NonNull Collection<String> available, @NonNull Languages requested) {
            String result = requested.lookupTag(available);
            return result != null ? result : available.stream().findFirst().orElse(null);
        }

        @VisibleForTesting
        static URI getCachedClientBaseURI(WebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:pxweb"), TypedId.getUniqueID(source), resolveLanguage(source, languages));
        }
    }

    @lombok.AllArgsConstructor
    private static final class PxWebConnection implements Connection {

        @NonNull
        private final PxWebClient client;

        @Override
        public @NonNull Optional<URI> testConnection() throws IOException {
            return Optional.of(client.ping());
        }

        @Override
        public @NonNull Collection<sdmxdl.Database> getDatabases() throws IOException {
            return client.getDataBases();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            checkDatabase(database);
            return client.getTables(database.getId());
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            String tablePath = PxConverter.flowRefToTablePath(flowRef);
            return MetaSet
                    .builder()
                    .flow(ConnectionSupport.getFlowFromFlows(database, flowRef, this, client))
                    .structure(client.getMeta(database.getId(), tablePath))
                    .build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            String tablePath = PxConverter.flowRefToTablePath(flowRef);
            Structure dsd = client.getMeta(database.getId(), tablePath);
            DataCursor dataCursor = client.getData(database.getId(), tablePath, dsd, query.getKey());
            return query.execute(dataCursor.asCloseableStream());
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @Override
        public void close() {
        }

        private void checkDatabase(DatabaseRef database) throws IOException {
            if (database.equals(DatabaseRef.NO_DATABASE)) {
                throw new IOException("Database reference is required");
            }
        }
    }
}
