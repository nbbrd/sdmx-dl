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
package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.newProvider;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.custom.EUROSTAT;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class EurostatDriver implements Driver {

    private static final String CONNECTORS_EUROSTAT = "CONNECTORS_EUROSTAT";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_EUROSTAT)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(SpecificRestClientFactory.builder()
                    .supplier(endpoint ->
                            new EUROSTAT(newProvider(endpoint, Provider.AuthenticationMethods.NONE, false, true)))
                    .build())
            .source(WebSource.builder()
                    .id("ESTAT")
                    .alias("EUROSTAT")
                    .name("en", "Eurostat")
                    .name("de", "Eurostat")
                    .name("fr", "Eurostat")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://ec.europa.eu/eurostat/data/database")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/estat")
                    .build())
            .source(WebSource.builder()
                    .id("ESTAT_COMEXT")
                    .alias("EUROSTAT_COMEXT")
                    .name("en", "Eurostat - International trade in goods statistics (ITGS)")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://ec.europa.eu/eurostat/api/comext/dissemination/sdmx/2.1")
                    .websiteOf("https://ec.europa.eu/eurostat/web/international-trade-in-goods/overview")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT_COMEXT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/estat_comext")
                    .build())
            .source(WebSource.builder()
                    .id("EC_DG_COMP")
                    .alias("EUROSTAT_COMP")
                    .name("en", "European Commission - Directorate General for Competition")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/comp/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=comp")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_COMP")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_comp")
                    .build())
            .source(WebSource.builder()
                    .id("EC_DG_ECFIN")
                    .alias("AMECO")
                    .name("en", "European Commission - Directorate-General for Economic and Financial Affairs")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/ecfin/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://webgate.ec.europa.eu/ecfin/redisstat/databrowser/explore")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_ECFIN")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_ecfin")
                    .build())
            .source(WebSource.builder()
                    .id("EC_DG_EMPL")
                    .alias("EUROSTAT_EMPL")
                    .name(
                            "en",
                            "European Commission - Directorate General for Employment, Social Affairs and inclusion")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/empl/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=empl")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_EMPL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_empl")
                    .build())
            .source(WebSource.builder()
                    .id("EC_DG_GROW")
                    .alias("EUROSTAT_GROW")
                    .name(
                            "en",
                            "European Commission - Directorate General for Internal Market, Industry, Entrepreneurship and SMEs")
                    .driver(CONNECTORS_EUROSTAT)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://webgate.ec.europa.eu/grow/redisstat/api/dissemination/sdmx/2.1")
                    .websiteOf("https://data.europa.eu/data/datasets?catalog=grow")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/EC_DG_GROW")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ec_dg_grow")
                    .build())
            .build();
}
