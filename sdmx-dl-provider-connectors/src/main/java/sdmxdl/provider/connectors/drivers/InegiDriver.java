package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.newProvider;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.custom.INEGI;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

@DirectImpl
@ServiceProvider
public final class InegiDriver implements Driver {

    private static final String CONNECTORS_INEGI = "CONNECTORS_INEGI";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_INEGI)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(SpecificRestClientFactory.builder()
                    .supplier(endpoint ->
                            new INEGI(newProvider(endpoint, Provider.AuthenticationMethods.NONE, false, true)))
                    .build())
            .source(WebSource.builder()
                    .id("INEGI")
                    .name("en", "Instituto Nacional de Estadistica y Geografia")
                    .driver(CONNECTORS_INEGI)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://sdmx.snieg.mx/ServiceV6/rest")
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/inegi")
                    .build())
            .build();
}
