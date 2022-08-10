package sdmxdl.format.protobuf;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.MonitorReports;
import sdmxdl.format.protobuf.web.MonitorStatus;

import java.util.stream.Collectors;

import static sdmxdl.format.protobuf.WellKnownTypes.fromInstant;

@lombok.experimental.UtilityClass
public class ProtobufMonitors {

    public static FileParser<sdmxdl.web.MonitorReports> getFileParser() {
        return FileParser.onParsingStream(resource -> toMonitorReports(MonitorReports.parseFrom(resource)));
    }

    public static FileFormatter<sdmxdl.web.MonitorReports> getFileFormatter() {
        return FileFormatter.onFormattingStream((value, resource) -> fromMonitorReports(value).writeTo(resource));
    }

    public static MonitorReports fromMonitorReports(sdmxdl.web.MonitorReports value) {
        return MonitorReports
                .newBuilder()
                .setUriScheme(value.getUriScheme())
                .addAllReports(value.getReports().stream().map(ProtobufMonitors::fromMonitorReport)::iterator)
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static sdmxdl.web.MonitorReports toMonitorReports(MonitorReports value) {
        return sdmxdl.web.MonitorReports
                .builder()
                .uriScheme(value.getUriScheme())
                .reports(value.getReportsList().stream().map(ProtobufMonitors::toMonitorReport).collect(Collectors.toList()))
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
