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
package internal.sdmxdl.provider.ri.web.drivers;

import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.sdmxdl.provider.ri.web.Sdmx21RestParsers;
import internal.sdmxdl.provider.ri.web.Sdmx21RestQueries;
import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import internal.util.http.HttpResponseException;
import internal.util.http.ext.InterceptingClient;
import lombok.NonNull;
import nbbrd.io.Resource;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.IntProperty;
import nbbrd.io.text.LongProperty;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.format.MessageFooter;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.Marker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipInputStream;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static internal.sdmxdl.provider.ri.web.Sdmx21RestParsers.withCharset;
import static java.util.Collections.singletonList;
import static sdmxdl.LanguagePriorityList.ANY;
import static sdmxdl.ext.spi.Dialect.SDMX21_DIALECT;
import static sdmxdl.provider.SdmxFix.Category.PROTOCOL;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class EurostatDriver2 implements WebDriver {

    public static final IntProperty ASYNC_MAX_RETRIES_PROPERTY =
            IntProperty.of("asyncMaxRetries", 10);

    public static final LongProperty ASYNC_SLEEP_TIME_PROPERTY =
            LongProperty.of("asyncSleepTime", 6000);

    private static final String RI_EUROSTAT = "ri:estat";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .id(RI_EUROSTAT)
            .rank(NATIVE_RANK)
            .connector(RestConnector.of(EurostatDriver2::newClient))
            .supportedProperties(RI_CONNECTION_PROPERTIES)
            .supportedPropertyOf(ASYNC_MAX_RETRIES_PROPERTY)
            .supportedPropertyOf(ASYNC_SLEEP_TIME_PROPERTY)
            .defaultDialect(SDMX21_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .id("ESTAT")
                    .alias("EUROSTAT")
                    .name("en", "Eurostat")
                    .name("de", "Eurostat")
                    .name("fr", "Eurostat")
                    .driver(RI_EUROSTAT)
                    .endpointOf("https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://ec.europa.eu/eurostat/data/database")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/estat")
                    .build())
            .build();

    private static RestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return new RiRestClient(
                Marker.of(s),
                s.getEndpoint().toURL(),
                c.getLanguages(),
                ObsParser::newDefault,
                getHttpClient(s, c),
                new Sdmx21RestQueries(false),
                new Sdmx21RestParsers(),
                false
        );
    }

    private static HttpClient getHttpClient(SdmxWebSource s, WebContext c) {
        int asyncMaxRetries = ASYNC_MAX_RETRIES_PROPERTY.get(s.getProperties());
        long asyncSleepTime = ASYNC_SLEEP_TIME_PROPERTY.get(s.getProperties());
        return new InterceptingClient(
                RiHttpUtils.newClient(RiHttpUtils.newContext(s, c)),
                (client, request, response) -> checkCodesInMessageFooter(client, response, asyncSleepTime, asyncMaxRetries)
        );
    }

    @SdmxFix(id = 3, category = PROTOCOL, cause = "Some response codes are located in the message footer")
    private static HttpResponse checkCodesInMessageFooter(HttpClient client, HttpResponse result, long asyncSleepTime, int asyncMaxRetries) throws IOException {
        if (result.getContentType().isCompatible(SDMX_GENERIC_XML)) {
            MessageFooter messageFooter = parseMessageFooter(result);
            Optional<URL> asyncURL = getAsyncURL(messageFooter);
            if (asyncURL.isPresent()) {
                return requestAsync(client, asyncURL.get(), asyncSleepTime, asyncMaxRetries);
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

    private static HttpResponseException getResponseException(MessageFooter messageFooter) {
        return new HttpResponseException(messageFooter.getCode(), String.join(System.lineSeparator(), messageFooter.getTexts()));
    }

    private static Optional<URL> getAsyncURL(MessageFooter messageFooter) {
        return messageFooter.getCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE
                ? messageFooter.getTexts().stream().map(Parser.onURL()::parse).filter(Objects::nonNull).findFirst()
                : Optional.empty();
    }

    private static HttpResponse requestAsync(HttpClient client, URL url, long sleepTimeInMillis, int retries) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, singletonList(MediaType.ANY_TYPE), ANY);
        for (int i = 1; i <= retries; i++) {
            sleep(sleepTimeInMillis);
            try {
                return new AsyncResponse(client.send(request));
            } catch (HttpResponseException ex) {
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
