/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.cli;

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.AccessReport;
import sdmxdl.web.HealthCheck;
import sdmxdl.web.HealthReport;
import sdmxdl.web.HealthVerdict;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebHealthRequest;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "health")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckHealthCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.Option(
            names = {"-c", "--checks"},
            paramLabel = "<check>",
            split = ",",
            defaultValue = "MONITOR",
            descriptionKey = "cli.sdmx.checks")
    private List<HealthCheck> checks;

    @CommandLine.Option(
            names = {"--fail-on-issue"},
            defaultValue = "false",
            descriptionKey = "cli.sdmx.failOnIssue")
    private boolean failOnIssue;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Integer call() throws Exception {
        List<HealthReport> reports = getReports();
        getTable().write(csv, sort.applySort(reports.stream(), BY_SOURCE));
        return failOnIssue && reports.stream().anyMatch(report -> report.getVerdict() != HealthVerdict.OK)
                ? CommandLine.ExitCode.SOFTWARE
                : CommandLine.ExitCode.OK;
    }

    private List<HealthReport> getReports() throws IOException {
        SdmxWebManager manager = web.loadManager();
        WebHealthRequest.Builder request =
                WebHealthRequest.builder().checks(getChecks()).parallel(!web.isNoParallel());
        if (!web.isAllSources()) {
            request.sources(web.getSources());
        }
        return manager.checkHealth(request.build());
    }

    private Set<HealthCheck> getChecks() {
        return checks.isEmpty() ? WebHealthRequest.DEFAULT_CHECKS : EnumSet.copyOf(checks);
    }

    private CsvTable<HealthReport> getTable() {
        Set<HealthCheck> set = getChecks();
        CsvTable.Builder<HealthReport> result = CsvTable.builderOf(HealthReport.class)
                .columnOf("Source", HealthReport::getSource)
                .columnOf("Verdict", HealthReport::getVerdict, Formatter.onObjectToString());
        if (set.contains(HealthCheck.MONITOR)) {
            result.columnOf("Status", monitor(MonitorReport::getStatus), Formatter.onObjectToString())
                    .columnOf("UptimeRatio", monitor(MonitorReport::getUptimeRatio), Formatter.onObjectToString())
                    .columnOf(
                            "AverageResponseTime",
                            monitor(MonitorReport::getAverageResponseTime),
                            Formatter.onObjectToString())
                    .columnOf("MonitorError", HealthReport::getMonitorError);
        }
        if (set.contains(HealthCheck.ACCESS)) {
            result.columnOf("Reachable", access(AccessReport::isReachable), CheckHealthCommand::formatBoolean)
                    .columnOf("Accessible", access(AccessReport::isAccessible), CheckHealthCommand::formatBoolean)
                    .columnOf("StatusCode", access(AccessReport::getStatusCode), Formatter.onObjectToString())
                    .columnOf(
                            "DurationInMillis",
                            access(report ->
                                    report.isReachable() ? report.getDuration().toMillis() : null),
                            Formatter.onObjectToString())
                    .columnOf("URI", access(AccessReport::getUri), Formatter.onObjectToString())
                    .columnOf("AccessError", access(AccessReport::getErrorMessage));
        }
        return result.build();
    }

    private static <T> Function<HealthReport, T> monitor(Function<MonitorReport, T> getter) {
        return report -> report.getMonitor() != null ? getter.apply(report.getMonitor()) : null;
    }

    private static <T> Function<HealthReport, T> access(Function<AccessReport, T> getter) {
        return report -> report.getAccess() != null ? getter.apply(report.getAccess()) : null;
    }

    private static String formatBoolean(Boolean value) {
        return value != null ? (value ? "YES" : "NO") : null;
    }

    private static final Comparator<HealthReport> BY_SOURCE = Comparator.comparing(HealthReport::getSource);
}
