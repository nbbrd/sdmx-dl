/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.dialects.drivers;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.Resource;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.http.ext.InterceptingDecorator;
import nbbrd.io.http.ext.InterceptingFunction;
import nbbrd.io.http.ext.ThrowingStatusException;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.IntProperty;
import nbbrd.io.text.LongProperty;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.EventListener;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.format.MessageFooter;
import sdmxdl.format.ObsParser;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.RiRestClient;
import sdmxdl.provider.ri.drivers.Sdmx21RestErrors;
import sdmxdl.provider.ri.drivers.Sdmx21RestParsers;
import sdmxdl.provider.ri.drivers.Sdmx21RestQueries;
import sdmxdl.provider.ri.http.HttpDecoration;
import sdmxdl.provider.ri.http.HttpDecorationSupport;
import sdmxdl.provider.ri.http.HttpFactory;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static java.util.Collections.singletonList;
import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.SdmxFix.Category.PROTOCOL;
import static sdmxdl.provider.SdmxFix.Category.QUERY;
import static sdmxdl.provider.ri.drivers.Sdmx21RestParsers.withCharset;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class EstatDialectDriver implements Driver {

    private static final String DIALECTS_ESTAT = "DIALECTS_ESTAT";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_ESTAT)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(EstatDialectDriver::newClient))
            .propertiesOf(ESTAT_HTTP_FACTORY.getFactoryProperties())
            .source(WebSource
                    .builder()
                    .id("ESTAT")
                    .alias("EUROSTAT")
                    .name("en", "Eurostat")
                    .name("de", "Eurostat")
                    .name("fr", "Eurostat")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://ec.europa.eu/eurostat/data/database")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/estat")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ESTAT_COMEXT")
                    .name("en", "Eurostat - International trade in goods statistics (ITGS)")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://ec.europa.eu/eurostat/api/comext/dissemination/sdmx/2.1")
                    .websiteOf("https://ec.europa.eu/eurostat/web/international-trade-in-goods/overview")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT_COMEXT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/estat_comext")
                    .build())
            .source(WebSource
                    .builder()
                    .id("EC_DG_COMP")
                    .name("en", "European Commission - Directorate General for Competition")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/comp/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=comp")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_COMP")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_comp")
                    .build())
            .source(WebSource
                    .builder()
                    .id("EC_DG_ECFIN")
                    .name("en", "European Commission - Directorate-General for Economic and Financial Affairs")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/ecfin/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://webgate.ec.europa.eu/ecfin/redisstat/databrowser/explore")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_ECFIN")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_ecfin")
                    .build())
            .source(WebSource
                    .builder()
                    .id("EC_DG_EMPL")
                    .name("en", "European Commission - Directorate General for Employment, Social Affairs and inclusion")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/empl/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=empl")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_EMPL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_empl")
                    .build())
            .source(WebSource
                    .builder()
                    .id("EC_DG_GROW")
                    .name("en", "European Commission - Directorate General for Internal Market, Industry, Entrepreneurship and SMEs")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/grow/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=grow")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_GROW")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_grow")
                    .build())
            .source(WebSource
                    .builder()
                    .id("EC_DG_TAXUD")
                    .name("en", "European Commission - Directorate-General for Taxation and Customs Union")
                    .driver(DIALECTS_ESTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/taxation_customs/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://webgate.ec.europa.eu/taxation_customs/redisstat/databrowser/explore")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_TAXUD")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_taxud")
                    .build())
            .build();

    private static RestClient newClient(WebSource s, Languages languages, WebContext c) {
        return new RiRestClient(
                HasMarker.of(s),
                s.getEndpoint(),
                languages,
                ObsParser::newDefault,
                ESTAT_HTTP_FACTORY.create(s, c),
                Sdmx21RestQueries.DEFAULT,
                Sdmx21RestParsers.DEFAULT,
                Sdmx21RestErrors.DEFAULT,
                ESTAT_FEATURES
        );
    }

    @SdmxFix(id = 4, category = QUERY, cause = "Data key parameter does not support 'all' keyword")
    private static final Set<Feature> ESTAT_FEATURES = EnumSet.of(Feature.DATA_QUERY_DETAIL);

    private static final HttpFactory ESTAT_HTTP_FACTORY =
            new AsyncDecoration().decorate(
                    HttpManager.getHttpFactory()
            );

    private static final class AsyncDecoration implements HttpDecoration {

        @PropertyDefinition
        public static final IntProperty ASYNC_MAX_RETRIES_PROPERTY =
                IntProperty.of(DRIVER_PROPERTY_PREFIX + ".asyncMaxRetries", 10);

        @PropertyDefinition
        public static final LongProperty ASYNC_SLEEP_TIME_PROPERTY =
                LongProperty.of(DRIVER_PROPERTY_PREFIX + ".asyncSleepTime", 6000);

        @lombok.experimental.Delegate
        private final HttpDecoration support = HttpDecorationSupport
                .builder()
                .name("async")
                .property(ASYNC_MAX_RETRIES_PROPERTY)
                .property(ASYNC_SLEEP_TIME_PROPERTY)
                .superFactory(AsyncDecoration::decorate)
                .build();

        private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
            return new InterceptingDecorator(
                    d.create(s, c),
                    getInterceptor(
                            ASYNC_SLEEP_TIME_PROPERTY.get(s.getProperties()),
                            ASYNC_MAX_RETRIES_PROPERTY.get(s.getProperties()),
                            c.getEventListener(s)
                    )
            );
        }

        private static InterceptingFunction getInterceptor(long asyncSleepTime, int asyncMaxRetries, EventListener onEvent) {
            return (client, request, response) -> checkCodesInMessageFooter(client, response, asyncSleepTime, asyncMaxRetries, onEvent);
        }


        @SdmxFix(id = 3, category = PROTOCOL, cause = "Some response codes are located in the message footer")
        private static HttpResponse checkCodesInMessageFooter(HttpClient client, HttpResponse result, long asyncSleepTime, int asyncMaxRetries, EventListener onEvent) throws IOException {
            if (result.getContentType().isCompatible(SDMX_GENERIC_XML)) {
                MessageFooter messageFooter = parseMessageFooter(result);
                Optional<URI> asyncURI = getAsyncURI(messageFooter);
                if (asyncURI.isPresent()) {
                    if (onEvent != null) {
                        onEvent.accept("ESTAT", "Async request " + asyncURI.get());
                    }
                    return requestAsync(client, asyncURI.get(), asyncSleepTime, asyncMaxRetries);
                }
                throw getResponseException(messageFooter);
            }
            return result;
        }

        private static final MediaType SDMX_GENERIC_XML = MediaType.parse("application/vnd.sdmx.generic+xml; version=2.1");

        private static MessageFooter parseMessageFooter(HttpResponse result) throws IOException {
            return withCharset(SdmxXmlStreams.messageFooter21(ANY), result.getContentType().getCharset())
                    .parseStream(result::getBody);
        }

        private static ThrowingStatusException getResponseException(MessageFooter messageFooter) {
            return new ThrowingStatusException(messageFooter.getCode(), String.join(System.lineSeparator(), messageFooter.getTexts()));
        }

        private static Optional<URI> getAsyncURI(MessageFooter messageFooter) {
            return messageFooter.getCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE
                    ? messageFooter.getTexts().stream().map(Parser.onURI()::parse).filter(Objects::nonNull).findFirst()
                    : Optional.empty();
        }

        private static HttpResponse requestAsync(HttpClient client, URI url, long sleepTimeInMillis, int retries) throws IOException {
            HttpRequest request = HttpManager.newHttpRequest(url, singletonList(MediaType.ANY_TYPE), ANY);
            for (int i = 1; i <= retries; i++) {
                sleep(sleepTimeInMillis);
                try {
                    return new AsyncResponse(client.send(request));
                } catch (ThrowingStatusException ex) {
                    if (ex.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                        throw ex;
                    }
                }
            }
            throw new IOException("Asynchronous max retries reached");
        }

        private static void sleep(long timeInMillis) throws IOException {
            try {
                Thread.sleep(timeInMillis);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        @lombok.AllArgsConstructor
        private static final class AsyncResponse implements HttpResponse {

            @lombok.NonNull
            private final HttpResponse zipResponse;

            @Override
            public @NonNull MediaType getContentType() {
                return XmlMediaTypes.GENERIC_DATA_21;
            }

            @Override
            public long getContentLength() throws IOException {
                return zipResponse.getContentLength();
            }

            @Override
            public @NonNull HttpHeaders getHeaders() throws IOException {
                return zipResponse.getHeaders();
            }

            @Override
            public int getStatusCode() throws IOException {
                return zipResponse.getStatusCode();
            }

            @Override
            public @NonNull String getReasonPhrase() throws IOException {
                return zipResponse.getReasonPhrase();
            }

            @Override
            public @NonNull InputStream getBody() throws IOException {
                ZipInputStream result = new ZipInputStream(zipResponse.getBody());
                try {
                    result.getNextEntry();
                    return result;
                } catch (Throwable ex) {
                    Resource.ensureClosed(ex, zipResponse);
                    throw ex;
                }
            }

            @Override
            public void close() throws IOException {
                zipResponse.close();
            }
        }
    }
}
