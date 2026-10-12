package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.newProvider;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.custom.WB;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

@DirectImpl
@ServiceProvider
public final class WbDriver implements Driver {

    private static final String CONNECTORS_WB = "CONNECTORS_WB";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_WB)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(SpecificRestClientFactory.builder()
                    .supplier(
                            endpoint -> new WB(newProvider(endpoint, Provider.AuthenticationMethods.NONE, false, true)))
                    .build())
            .source(WebSource.builder()
                    .id("WB")
                    .name("en", "World Bank")
                    .driver(CONNECTORS_WB)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wb")
                    .build())
            .build();
}
