/*
 * Copyright 2017 National Bank of Belgium
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

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.RiRestClient;
import sdmxdl.provider.ri.drivers.Sdmx21RestErrors;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.util.EnumSet;
import java.util.Set;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class DotStatDialectDriver implements Driver {

    private static final String DIALECTS_DOTSTAT = "DIALECTS_DOTSTAT";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_DOTSTAT)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(DotStatDialectDriver::newClient))
            .propertiesOf(HttpManager.getHttpFactory().getFactoryProperties())
            .source(WebSource
                    .builder()
                    .id("UKDS")
                    .name("en", "UK Data Service")
                    .driver(DIALECTS_DOTSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://stats2.digitalresources.jisc.ac.uk/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats2.digitalresources.jisc.ac.uk/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UKDS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ukds")
                    .build())
            .build();

    private static RestClient newClient(WebSource s, Languages languages, WebContext c) {
        return new RiRestClient(
                HasMarker.of(s),
                s.getEndpoint(),
                languages,
                ObsParser::newDefault,
                HttpManager.getHttpFactory().create(s, c),
                DotStatRestQueries.DEFAULT,
                DotStatRestParsers.DEFAULT,
                Sdmx21RestErrors.DEFAULT,
                DOTSTAT_FEATURES
        );
    }

    @SdmxFix(id = 1, category = QUERY, cause = "Data detail parameter not supported")
    private static final Set<Feature> DOTSTAT_FEATURES = EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);
}
