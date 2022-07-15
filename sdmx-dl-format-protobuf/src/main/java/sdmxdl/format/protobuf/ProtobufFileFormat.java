package sdmxdl.format.protobuf;

import lombok.NonNull;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.MonitorReports;
import sdmxdl.format.protobuf.web.MonitorStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.stream.Collectors;

public abstract class ProtobufFileFormat<T> implements FileParser<T>, FileFormatter<T> {

    public static final ProtobufFileFormat<sdmxdl.web.MonitorReports> MONITOR = new ProtobufFileFormat<sdmxdl.web.MonitorReports>() {

        @Override
        public void formatStream(@NonNull sdmxdl.web.MonitorReports value, @NonNull OutputStream resource) throws IOException {
            fromMonitorReports(value).writeTo(resource);
        }

        @Override
        public @NonNull sdmxdl.web.MonitorReports parseStream(@NonNull InputStream resource) throws IOException {
            return toMonitorReports(MonitorReports.parseFrom(resource));
        }
    };

    private ProtobufFileFormat() {
    }

    private static MonitorReports fromMonitorReports(sdmxdl.web.MonitorReports value) {
        return MonitorReports
                .newBuilder()
                .setUriScheme(value.getUriScheme())
                .addAllReports(value.getReports().stream().map(ProtobufFileFormat::fromMonitorReport)::iterator)
                .setCreationTime(value.getCreationTime().toString())
                .setExpirationTime(value.getExpirationTime().toString())
                .build();
    }

    private static sdmxdl.web.MonitorReports toMonitorReports(MonitorReports value) {
        return sdmxdl.web.MonitorReports
                .builder()
                .uriScheme(value.getUriScheme())
                .reports(value.getReportsList().stream().map(ProtobufFileFormat::toMonitorReport).collect(Collectors.toList()))
                .creationTime(Instant.parse(value.getCreationTime()))
                .expirationTime(Instant.parse(value.getExpirationTime()))
                .build();
    }

    private static MonitorReport fromMonitorReport(sdmxdl.web.MonitorReport value) {
        MonitorReport.Builder result = MonitorReport
                .newBuilder()
                .setSource(value.getSource())
                .setStatus(fromMonitorStatus(value.getStatus()));
        if (value.getUptimeRatio() != null) result.setUptimeRatio(value.getUptimeRatio());
        if (value.getAverageResponseTime() != null) result.setAverageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    private static sdmxdl.web.MonitorReport toMonitorReport(MonitorReport value) {
        sdmxdl.web.MonitorReport.Builder result = sdmxdl.web.MonitorReport
                .builder()
                .source(value.getSource())
                .status(toMonitorStatus(value.getStatus()));
        if (value.hasUptimeRatio()) result.uptimeRatio(value.getUptimeRatio());
        if (value.hasAverageResponseTime()) result.averageResponseTime(value.getAverageResponseTime());
        return result.build();
    }

    private static MonitorStatus fromMonitorStatus(sdmxdl.web.MonitorStatus value) {
        return MonitorStatus.valueOf(value.name());
    }

    private static sdmxdl.web.MonitorStatus toMonitorStatus(MonitorStatus value) {
        return sdmxdl.web.MonitorStatus.valueOf(value.name());
    }
}
