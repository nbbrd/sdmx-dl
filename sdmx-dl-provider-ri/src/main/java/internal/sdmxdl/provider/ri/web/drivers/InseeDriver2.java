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

import internal.sdmxdl.provider.ri.web.*;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.StandardReportingFormat;
import sdmxdl.format.time.TimeFormats;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.Marker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.util.EnumSet;
import java.util.function.Supplier;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;
import static sdmxdl.provider.SdmxFix.Category.CONTENT;
import static sdmxdl.provider.SdmxFix.Category.MEDIA_TYPE;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class InseeDriver2 implements WebDriver {

    private static final String RI_INSEE = "ri:insee";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .id(RI_INSEE)
            .rank(NATIVE_RANK)
            .connector(RestConnector.of(InseeRestClient::new))
            .supportedProperties(RI_CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .id("INSEE")
                    .name("en", "National Institute of Statistics and Economic Studies")
                    .name("fr", "Institut national de la statistique et des études économiques")
                    .driver(RI_INSEE)
                    .endpointOf("https://bdm.insee.fr/series/sdmx")
                    .websiteOf("https://www.insee.fr/fr/statistiques")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INSEE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/insee")
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

    private final static class InseeRestClient extends RiRestClient {

        InseeRestClient(SdmxWebSource s, LanguagePriorityList languages, WebContext c) throws IOException {
            super(
                    Marker.of(s),
                    s.getEndpoint().toURL(),
                    languages,
                    OBS_FACTORY,
                    RiHttpUtils.newClient(s, c),
                    new Sdmx21RestQueries(false),
                    new InseeRestParsers(),
                    Sdmx21RestErrors.DEFAULT,
                    EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_DETAIL)
            );
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
            DataStructure dsd = super.getStructure(ref);
            DataStructure.Builder result = dsd.toBuilder().clearDimensions();
            for (Dimension dimension : dsd.getDimensions()) {
                result.dimension(fixDimensionCodes(fixDimensionId(dimension), super::getCodelist));
            }
            return result.build();
        }
    }

    private static final class InseeRestParsers extends Sdmx21RestParsers {

        @Override
        public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull Supplier<ObsParser> dataFactory) {
            return super.getDataParser(DATA_TYPE, dsd, dataFactory);
        }
    }

    @VisibleForTesting
    static final StandardReportingFormat REPORTING_TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .durationOf("P2M")
            .limitPerYear(6)
            .build();

    @VisibleForTesting
    static final Parser<ObservationalTimePeriod> EXTENDED_TIME_PARSER =
            TimeFormats.getObservationalTimePeriod(IGNORE_ERROR)
                    .orElse(TimeFormats.onReportingFormat(REPORTING_TWO_MONTH, IGNORE_ERROR));

    private static final Supplier<ObsParser> OBS_FACTORY = () -> new ObsParser(EXTENDED_TIME_PARSER, Parser.onDouble());
}
