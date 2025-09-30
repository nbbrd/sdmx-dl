package sdmxdl.format.protobuf;


import sdmxdl.format.protobuf.web.*;
import sdmxdl.web.*;

import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static sdmxdl.format.protobuf.WellKnownTypes.fromInstant;

@lombok.experimental.UtilityClass
public class ProtoWeb {

    public static WebSourceDto fromWebSource(WebSource value) {
        WebSourceDto.Builder result = WebSourceDto.newBuilder();
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

    public static WebSource toWebSource(WebSourceDto value) {
        return WebSource
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

    public static WebSourcesDto fromWebSources(WebSources value) {
        WebSourcesDto.Builder result = WebSourcesDto.newBuilder();
        result.addAllWebSources(value.getSources().stream().map(ProtoWeb::fromWebSource).collect(toList()));
        return result.build();
    }

    public static WebSources toWebSources(WebSourcesDto value) {
        return WebSources
                .builder()
                .sources(value.getWebSourcesList().stream().map(ProtoWeb::toWebSource).collect(toList()))
                .build();
    }

    public static MonitorReportsDto fromMonitorReports(MonitorReports value) {
        return MonitorReportsDto
                .newBuilder()
                .setUriScheme(value.getUriScheme())
                .addAllReports(value.getReports().stream().map(ProtoWeb::fromMonitorReport)::iterator)
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static MonitorReports toMonitorReports(MonitorReportsDto value) {
        return MonitorReports
                .builder()
                .uriScheme(value.getUriScheme())
                .reports(value.getReportsList().stream().map(ProtoWeb::toMonitorReport).collect(Collectors.toList()))
                .creationTime(WellKnownTypes.toInstant(value.getCreationTime()))
                .expirationTime(WellKnownTypes.toInstant(value.getExpirationTime()))
                .build();
    }

    public static MonitorReportDto fromMonitorReport(MonitorReport value) {
        MonitorReportDto.Builder result = MonitorReportDto
                .newBuilder()
                .setSource(value.getSource())
                .setStatus(fromMonitorStatus(value.getStatus()));
        if (value.getUptimeRatio() != null) result.setUptimeRatio(value.getUptimeRatio());
        if (value.getAverageResponseTime() != null) result.setAverageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    public static MonitorReport toMonitorReport(MonitorReportDto value) {
        MonitorReport.Builder result = MonitorReport
                .builder()
                .source(value.getSource())
                .status(toMonitorStatus(value.getStatus()));
        if (value.hasUptimeRatio()) result.uptimeRatio(value.getUptimeRatio());
        if (value.hasAverageResponseTime()) result.averageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    public static MonitorStatusDto fromMonitorStatus(MonitorStatus value) {
        return MonitorStatusDto.valueOf(value.name());
    }

    public static MonitorStatus toMonitorStatus(MonitorStatusDto value) {
        return MonitorStatus.valueOf(value.name());
    }
}
