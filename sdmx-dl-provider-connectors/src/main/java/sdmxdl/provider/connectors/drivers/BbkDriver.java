package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.newProvider;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.custom.BBK;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

@DirectImpl
@ServiceProvider
public final class BbkDriver implements Driver {

    private static final String CONNECTORS_BBK = "CONNECTORS_BBK";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_BBK)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(SpecificRestClientFactory.builder()
                    .supplier(endpoint ->
                            new BBK(newProvider(endpoint, Provider.AuthenticationMethods.NONE, false, true)))
                    .build())
            .source(WebSource.builder()
                    .id("BBK")
                    .name("en", "Deutsche Bundesbank")
                    .name("de", "Deutsche Bundesbank")
                    .driver(CONNECTORS_BBK)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.statistiken.bundesbank.de/rest")
                    .websiteOf("https://www.bundesbank.de/en/statistics/time-series-databases")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BBK")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/bbk")
                    .build())
            .build();
}
