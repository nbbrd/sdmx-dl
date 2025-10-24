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
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.http.URLQueryBuilder;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.StandardReportingFormat;
import sdmxdl.format.time.TimeFormats;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.*;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.function.Supplier;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;
import static sdmxdl.provider.SdmxFix.Category.*;
import static sdmxdl.provider.ri.drivers.RiHttpUtils.RI_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class InseeDialectDriver implements Driver {

    private static final String DIALECTS_INSEE = "DIALECTS_INSEE";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_INSEE)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(InseeRestClient::new))
            .properties(RI_CONNECTION_PROPERTIES)
            .propertyOf(NO_COMMA_ENCODING_PROPERTY)
            .source(WebSource
                    .builder()
                    .id("INSEE")
                    .name("en", "National Institute of Statistics and Economic Studies")
                    .name("fr", "Institut national de la statistique et des études économiques")
                    .driver(DIALECTS_INSEE)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://bdm.insee.fr/series/sdmx")
                    .websiteOf("https://www.insee.fr/fr/statistiques")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INSEE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/insee")
                    .propertyOf(NO_COMMA_ENCODING_PROPERTY, true)
                    .build())
            .build();

    @SdmxFix(id = 3, category = CONTENT, cause = "Some dimension/code ids are invalid")
    private static Dimension fixDimensionId(Dimension dimension) {
        String id = dimension.getId();
        return dimension.getId().endsWith("6")
                ? dimension.toBuilder().id(id.substring(0, id.length() - 1)).build()
                : dimension;
    }

    @SdmxFix(id = 4, category = CONTENT, cause = "Some codes are missing in dsd even when requested with 'references=children'")
    private static Dimension fixDimensionCodes(Dimension dimension, IOFunction<CodelistRef, Codelist> codelistProvider) throws IOException {
        Codelist codelist = dimension.getCodelist();
        return codelist.getCodes().isEmpty()
                ? dimension.toBuilder().codelist(codelistProvider.applyWithIO(codelist.getRef())).build()
                : dimension;
    }

    @SdmxFix(id = 5, category = MEDIA_TYPE, cause = "Default media type is compact instead of generic")
    private static final MediaType DATA_TYPE = XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21;

    @SdmxFix(id = 6, category = QUERY, cause = "Since October 2025, encoded comma in URL triggers HTTP 500 error")
    @PropertyDefinition
    private static final BooleanProperty NO_COMMA_ENCODING_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".noCommaEncoding", false);

    private final static class InseeRestClient extends RiRestClient {

        InseeRestClient(WebSource s, Languages languages, WebContext c) throws IOException {
            super(
                    HasMarker.of(s),
                    s.getEndpoint().toURL(),
                    languages,
                    OBS_FACTORY,
                    RiHttpUtils.newClient(s, c),
                    NO_COMMA_ENCODING_PROPERTY.get(s.getProperties())
                            ? InseeRestQueries.NO_COMMA_ENCODING
                            : InseeRestQueries.DEFAULT,
                    InseeRestParsers.INSTANCE,
                    Sdmx21RestErrors.DEFAULT,
                    EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_DETAIL)
            );
        }

        @Override
        public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
            Structure dsd = super.getStructure(ref);
            Structure.Builder result = dsd.toBuilder().clearDimensions();
            for (Dimension dimension : dsd.getDimensions()) {
                result.dimension(fixDimensionCodes(fixDimensionId(dimension), super::getCodelist));
            }
            return result.build();
        }
    }

    private static final class InseeRestQueries extends Sdmx21RestQueries {

        public static final InseeRestQueries DEFAULT = new InseeRestQueries(false);
        public static final InseeRestQueries NO_COMMA_ENCODING = new InseeRestQueries(true);

        private final boolean noCommaEncoding;

        private InseeRestQueries(boolean noCommaEncoding) {
            super(false);
            this.noCommaEncoding = noCommaEncoding;
        }

        @Override
        protected URLQueryBuilder onData(URL endpoint, String resourcePath, FlowRef flowRef, Key key, String providerRef) {
            URLQueryBuilder result = super.onData(endpoint, resourcePath, flowRef, key, providerRef);
            if (noCommaEncoding) {
                String fixedURL = result.toString().replace("%2C", ",");
                try {
                    return URLQueryBuilder.of(new URL(fixedURL));
                } catch (MalformedURLException e) {
                    return result;
                }
            }
            return result;
        }
    }

    private static final class InseeRestParsers extends Sdmx21RestParsers {

        public static final InseeRestParsers INSTANCE = new InseeRestParsers();

        @Override
        public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull Structure dsd, @NonNull Supplier<ObsParser> dataFactory) {
            return super.getDataParser(DATA_TYPE, dsd, dataFactory);
        }
    }

    @VisibleForTesting
    static final StandardReportingFormat REPORTING_TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .duration(Duration.parse("P2M"))
            .limitPerYear(6)
            .build();

    @VisibleForTesting
    static final Parser<ObservationalTimePeriod> EXTENDED_TIME_PARSER =
            TimeFormats.getObservationalTimePeriod(IGNORE_ERROR)
                    .orElse(TimeFormats.onReportingFormat(REPORTING_TWO_MONTH, IGNORE_ERROR));

    private static final Supplier<ObsParser> OBS_FACTORY = () -> new ObsParser(EXTENDED_TIME_PARSER, Parser.onDouble());
}
