package sdmxdl.provider.connectors.drivers;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.Connectors.*;

import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmx30Client;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

@DirectImpl
@ServiceProvider
public final class Sdmx30Driver implements Driver {

    private static final String CONNECTORS_SDMX_30 = "CONNECTORS_SDMX30";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport.builder()
            .id(CONNECTORS_SDMX_30)
            .rank(WRAPPED_DRIVER_RANK)
            .connectorOf(GenericRestClientFactory.builder()
                    .supplier((endpoint, p) -> new RestSdmx30Client(ConnectorsRestClient.newProvider(
                            endpoint,
                            NEEDS_CREDENTIALS_PROPERTY.get(p)
                                    ? Provider.AuthenticationMethods.BASIC
                                    : Provider.AuthenticationMethods.NONE,
                            NEEDS_URL_ENCODING_PROPERTY.get(p),
                            SUPPORTS_COMPRESSION_PROPERTY.get(p),
                            SDMXVersion.V3)))
                    .property(NEEDS_CREDENTIALS_PROPERTY)
                    .property(NEEDS_URL_ENCODING_PROPERTY)
                    .property(SUPPORTS_COMPRESSION_PROPERTY)
                    .build())
            .source(WebSource.builder()
                    .id("OECD3")
                    .alias("OECD_SDMXV3")
                    .name("en", "The Organisation for Economic Co-operation and Development")
                    .name("es", "Organización para la Cooperación y el Desarrollo Económicos")
                    .name("fr", "Organisation de coopération et de développement économiques")
                    .name("it", "Organizzazione per la Cooperazione e lo Sviluppo Economico")
                    .driver(CONNECTORS_SDMX_30)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://sdmx.oecd.org/public/rest/v2")
                    .websiteOf("https://data-explorer.oecd.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/OECD")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/oecd")
                    .build())
            .build();
}
