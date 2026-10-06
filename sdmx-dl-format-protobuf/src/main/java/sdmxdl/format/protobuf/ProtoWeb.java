package sdmxdl.format.protobuf;

import static java.util.stream.Collectors.toList;
import static sdmxdl.format.protobuf.WellKnownTypes.fromInstant;

import java.util.stream.Collectors;
import sdmxdl.AccessReport;
import sdmxdl.format.protobuf.web.*;
import sdmxdl.web.*;

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
        if (value.getMonitorWebsite() != null)
            result.setMonitorWebsite(value.getMonitorWebsite().toString());
        return result.build();
    }

    public static WebSource toWebSource(WebSourceDto value) {
        return WebSource.builder()
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
        result.addAllWebSources(
                value.getSources().stream().map(ProtoWeb::fromWebSource).collect(toList()));
        return result.build();
    }

    public static WebSources toWebSources(WebSourcesDto value) {
        return WebSources.builder()
                .sources(value.getWebSourcesList().stream()
                        .map(ProtoWeb::toWebSource)
                        .collect(toList()))
                .build();
    }

    public static MonitorReportsDto fromMonitorReports(MonitorReports value) {
        return MonitorReportsDto.newBuilder()
                .setUriScheme(value.getUriScheme())
                .addAllReports(value.getReports().stream().map(ProtoWeb::fromMonitorReport)::iterator)
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static MonitorReports toMonitorReports(MonitorReportsDto value) {
        return MonitorReports.builder()
                .uriScheme(value.getUriScheme())
                .reports(value.getReportsList().stream()
                        .map(ProtoWeb::toMonitorReport)
                        .collect(Collectors.toList()))
                .creationTime(WellKnownTypes.toInstant(value.getCreationTime()))
                .expirationTime(WellKnownTypes.toInstant(value.getExpirationTime()))
                .build();
    }

    public static MonitorReportDto fromMonitorReport(MonitorReport value) {
        MonitorReportDto.Builder result = MonitorReportDto.newBuilder()
                .setSource(value.getSource())
                .setStatus(fromMonitorStatus(value.getStatus()));
        if (value.getUptimeRatio() != null) result.setUptimeRatio(value.getUptimeRatio());
        if (value.getAverageResponseTime() != null) result.setAverageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    public static MonitorReport toMonitorReport(MonitorReportDto value) {
        MonitorReport.Builder result =
                MonitorReport.builder().source(value.getSource()).status(toMonitorStatus(value.getStatus()));
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

    public static HealthCheckDto fromHealthCheck(HealthCheck value) {
        return HealthCheckDto.valueOf(value.name());
    }

    public static HealthCheck toHealthCheck(HealthCheckDto value) {
        return HealthCheck.valueOf(value.name());
    }

    public static HealthReportDto.Verdict fromHealthVerdict(HealthVerdict value) {
        return HealthReportDto.Verdict.valueOf(value.name());
    }

    public static AccessReportDto fromAccessReport(AccessReport value) {
        AccessReportDto.Builder result = AccessReportDto.newBuilder()
                .setReachable(value.isReachable())
                .setAccessible(value.isAccessible())
                .setDuration(value.getDuration().toMillis());
        if (value.getUri() != null) result.setUri(value.getUri().toString());
        if (value.getStatusCode() != null) result.setStatusCode(value.getStatusCode());
        if (value.getErrorMessage() != null) result.setErrorMessage(value.getErrorMessage());
        return result.build();
    }

    public static HealthReportDto fromHealthReport(HealthReport value) {
        HealthReportDto.Builder result = HealthReportDto.newBuilder()
                .setSource(value.getSource())
                .setVerdict(fromHealthVerdict(value.getVerdict()));
        if (value.getMonitor() != null) result.setMonitor(fromMonitorReport(value.getMonitor()));
        if (value.getMonitorError() != null) result.setMonitorError(value.getMonitorError());
        if (value.getAccess() != null) result.setAccess(fromAccessReport(value.getAccess()));
        return result.build();
    }
}
