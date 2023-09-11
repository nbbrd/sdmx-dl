package sdmxdl.format.protobuf;


import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.web.WebSource;

@lombok.experimental.UtilityClass
public class ProtobufSources {

    public static SdmxWebSource fromWebSource(WebSource value) {
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

    public static WebSource toWebSource(SdmxWebSource value) {
        return WebSource
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
}
