package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.newProvider;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.custom.ILO;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

@DirectImpl
@ServiceProvider
public final class IloDriver implements Driver {

    private static final String CONNECTORS_ILO = "CONNECTORS_ILO";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_ILO)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(SpecificRestClientFactory.builder()
                    .supplier(endpoint ->
                            new ILO(newProvider(endpoint, Provider.AuthenticationMethods.NONE, false, true)))
                    .build())
            .source(WebSource.builder()
                    .id("ILO")
                    .name("en", "International Labour Organization")
                    .driver(CONNECTORS_ILO)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://sdmx.ilo.org/rest")
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ilo")
                    .build())
            .build();
}
