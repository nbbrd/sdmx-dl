package sdmxdl.format.protobuf;


import sdmxdl.format.protobuf.web.SdmxWebSource;

@lombok.experimental.UtilityClass
public class ProtobufSources {

    public static SdmxWebSource fromWebSource(sdmxdl.web.SdmxWebSource value) {
        SdmxWebSource.Builder result = SdmxWebSource.newBuilder();
        result.setId(value.getId());
        result.putAllNames(value.getNames());
        result.setDriver(value.getDriver());
        if (value.getDialect() != null) result.setDialect(value.getDialect());
        result.setEndpoint(value.getEndpoint().toString());
        result.putAllProperties(value.getProperties());
        result.addAllAliases(value.getAliases());
        if (value.getWebsite() != null) result.setWebsite(value.getWebsite().toString());
        if (value.getMonitor() != null) result.setMonitor(value.getMonitor().toString());
        if (value.getMonitorWebsite() != null) result.setMonitorWebsite(value.getMonitorWebsite().toString());
        return result.build();
    }

    public static sdmxdl.web.SdmxWebSource toWebSource(SdmxWebSource value) {
        return sdmxdl.web.SdmxWebSource
                .builder()
                .id(value.getId())
                .names(value.getNamesMap())
                .driver(value.getDriver())
                .dialect(value.hasDialect() ? value.getDialect() : null)
                .endpointOf(value.getEndpoint())
                .properties(value.getPropertiesMap())
                .aliases(value.getAliasesList())
                .websiteOf(value.hasWebsite() ? value.getWebsite() : null)
                .monitorOf(value.hasMonitor() ? value.getMonitor() : null)
                .monitorWebsiteOf(value.hasMonitorWebsite() ? value.getMonitorWebsite() : null)
                .build();
    }
}
