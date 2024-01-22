package sdmxdl.format.protobuf;


import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.format.protobuf.web.WebSources;

import static java.util.stream.Collectors.toList;

@lombok.experimental.UtilityClass
public class ProtobufSources {

    public static SdmxWebSource fromWebSource(sdmxdl.web.WebSource value) {
        SdmxWebSource.Builder result = SdmxWebSource.newBuilder();
        result.setId(value.getId());
        result.putAllNames(value.getNames());
        result.setDriver(value.getDriver());
        result.setEndpoint(value.getEndpoint().toString());
        result.putAllProperties(value.getProperties());
        result.addAllAliases(value.getAliases());
        if (value.getWebsite() != null) result.setWebsite(value.getWebsite().toString());
        if (value.getMonitor() != null) result.setMonitor(value.getMonitor().toString());
        if (value.getMonitorWebsite() != null) result.setMonitorWebsite(value.getMonitorWebsite().toString());
        return result.build();
    }

    public static sdmxdl.web.WebSource toWebSource(SdmxWebSource value) {
        return sdmxdl.web.WebSource
                .builder()
                .id(value.getId())
                .names(value.getNamesMap())
                .driver(value.getDriver())
                .endpointOf(value.getEndpoint())
                .properties(value.getPropertiesMap())
                .aliases(value.getAliasesList())
                .websiteOf(value.hasWebsite() ? value.getWebsite() : null)
                .monitorOf(value.hasMonitor() ? value.getMonitor() : null)
                .monitorWebsiteOf(value.hasMonitorWebsite() ? value.getMonitorWebsite() : null)
                .build();
    }

    public static WebSources fromWebSources(sdmxdl.web.WebSources value) {
        WebSources.Builder result = WebSources.newBuilder();
        result.addAllWebSources(value.getSources().stream().map(ProtobufSources::fromWebSource).collect(toList()));
        return result.build();
    }

    public static sdmxdl.web.WebSources toWebSources(WebSources value) {
        return sdmxdl.web.WebSources
                .builder()
                .sources(value.getWebSourcesList().stream().map(ProtobufSources::toWebSource).collect(toList()))
                .build();
    }
}
