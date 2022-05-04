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
import internal.sdmxdl.provider.ri.web.Sdmx21RestQueries;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.sdmxdl.provider.ri.web.Sdmx21RestParsers;
import sdmxdl.format.MediaType;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.SdmxFix;
import sdmxdl.format.ObsParser;
import sdmxdl.format.StandardReportingFormat;
import sdmxdl.format.TimeFormatParser;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;
import sdmxdl.format.DataCursor;

import java.io.IOException;
import java.util.function.Supplier;

import static sdmxdl.provider.SdmxFix.Category.CONTENT;
import static sdmxdl.provider.SdmxFix.Category.MEDIA_TYPE;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class InseeDriver2 implements WebDriver {

    private static final String RI_INSEE = "ri:insee";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(RI_INSEE)
            .rank(NATIVE_RANK)
            .client(InseeRestClient::new)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .defaultDialect(DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("INSEE")
                    .descriptionOf("National Institute of Statistics and Economic Studies")
                    .description("en", "National Institute of Statistics and Economic Studies")
                    .description("fr", "Institut national de la statistique et des études économiques")
                    .driver(RI_INSEE)
                    .endpointOf("https://bdm.insee.fr/series/sdmx")
                    .websiteOf("https://www.insee.fr/fr/statistiques")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INSEE")
                    .build())
            .build();

    @SdmxFix(id = 2, category = CONTENT, cause = "Does not follow sdmx standard codes")
    private static final String DIALECT = "INSEE2017";

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

        InseeRestClient(SdmxWebSource s, WebContext c) throws IOException {
            super(
                    s.getId(),
                    s.getEndpoint().toURL(),
                    c.getLanguages(),
                    OBS_FACTORY,
                    RiHttpUtils.newClient(s, c),
                    new Sdmx21RestQueries(false),
                    new InseeRestParsers(),
                    true
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

    private static final StandardReportingFormat TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .durationOf("P2M")
            .limitPerYear(6)
            .build();

    @VisibleForTesting
    static final TimeFormatParser EXTENDED_PARSER =
            TimeFormatParser.onObservationalTimePeriod()
                    .orElse(TimeFormatParser.onStandardReporting(TWO_MONTH));

    private static final Supplier<ObsParser> OBS_FACTORY = () -> new ObsParser(EXTENDED_PARSER, Parser.onDouble());
}
