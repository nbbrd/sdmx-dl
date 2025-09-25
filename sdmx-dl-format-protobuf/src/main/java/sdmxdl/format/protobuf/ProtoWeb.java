package sdmxdl.format.protobuf;


import sdmxdl.format.protobuf.web.*;

import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static sdmxdl.format.protobuf.WellKnownTypes.fromInstant;

@lombok.experimental.UtilityClass
public class ProtoWeb {

    public static WebSource fromWebSource(sdmxdl.web.WebSource value) {
        WebSource.Builder result = WebSource.newBuilder();
        result.setId(value.getId());
        result.putAllNames(value.getNames());
        result.setDriver(value.getDriver());
        result.setConfidentiality(ProtoApi.fromConfidentiality(value.getConfidentiality()));
        result.setEndpoint(value.getEndpoint().toString());
        result.putAllProperties(value.getProperties());
        result.addAllAliases(value.getAliases());
        if (value.getWebsite() != null) result.setWebsite(value.getWebsite().toString());
        if (value.getMonitor() != null) result.setMonitor(value.getMonitor().toString());
        if (value.getMonitorWebsite() != null) result.setMonitorWebsite(value.getMonitorWebsite().toString());
        return result.build();
    }

    public static sdmxdl.web.WebSource toWebSource(WebSource value) {
        return sdmxdl.web.WebSource
                .builder()
                .id(value.getId())
                .names(value.getNamesMap())
                .driver(value.getDriver())
                .confidentiality(ProtoApi.toConfidentiality(value.getConfidentiality()))
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
        result.addAllWebSources(value.getSources().stream().map(ProtoWeb::fromWebSource).collect(toList()));
        return result.build();
    }

    public static sdmxdl.web.WebSources toWebSources(WebSources value) {
        return sdmxdl.web.WebSources
                .builder()
                .sources(value.getWebSourcesList().stream().map(ProtoWeb::toWebSource).collect(toList()))
                .build();
    }

    public static MonitorReports fromMonitorReports(sdmxdl.web.MonitorReports value) {
        return MonitorReports
                .newBuilder()
                .setUriScheme(value.getUriScheme())
                .addAllReports(value.getReports().stream().map(ProtoWeb::fromMonitorReport)::iterator)
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static sdmxdl.web.MonitorReports toMonitorReports(MonitorReports value) {
        return sdmxdl.web.MonitorReports
                .builder()
                .uriScheme(value.getUriScheme())
                .reports(value.getReportsList().stream().map(ProtoWeb::toMonitorReport).collect(Collectors.toList()))
                .creationTime(WellKnownTypes.toInstant(value.getCreationTime()))
                .expirationTime(WellKnownTypes.toInstant(value.getExpirationTime()))
                .build();
    }

    public static MonitorReport fromMonitorReport(sdmxdl.web.MonitorReport value) {
        MonitorReport.Builder result = MonitorReport
                .newBuilder()
                .setSource(value.getSource())
                .setStatus(fromMonitorStatus(value.getStatus()));
        if (value.getUptimeRatio() != null) result.setUptimeRatio(value.getUptimeRatio());
        if (value.getAverageResponseTime() != null) result.setAverageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    public static sdmxdl.web.MonitorReport toMonitorReport(MonitorReport value) {
        sdmxdl.web.MonitorReport.Builder result = sdmxdl.web.MonitorReport
                .builder()
                .source(value.getSource())
                .status(toMonitorStatus(value.getStatus()));
        if (value.hasUptimeRatio()) result.uptimeRatio(value.getUptimeRatio());
        if (value.hasAverageResponseTime()) result.averageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    public static MonitorStatus fromMonitorStatus(sdmxdl.web.MonitorStatus value) {
        return MonitorStatus.valueOf(value.name());
    }

    public static sdmxdl.web.MonitorStatus toMonitorStatus(MonitorStatus value) {
        return sdmxdl.web.MonitorStatus.valueOf(value.name());
    }
}
