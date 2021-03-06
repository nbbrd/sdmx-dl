/*
 * Copyright 2018 National Bank of Belgium
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

import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.ProxyOptions;
import lombok.AccessLevel;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "status")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckStatusCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Status> getTable() {
        return CsvTable
                .builderOf(Status.class)
                .columnOf("Source", Status::getSource, Formatter.onString())
                .columnOf("Status", Status::getStatus, Formatter.onObjectToString())
                .columnOf("UptimeRatio", Status::getUptimeRatio, Formatter.onObjectToString())
                .columnOf("AverageResponseTime", Status::getAverageResponseTime, Formatter.onObjectToString())
                .columnOf("ErrorMessage", Status::getCause, Formatter.onString())
                .build();
    }

    private Stream<Status> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        ProxyOptions.warmupProxySelector(manager.getProxySelector());
        Stream<String> sources = web.isAllSources() ? WebSourcesOptions.getAllSourceNames(manager) : web.getSources().stream();
        return sort.applySort(web.applyParallel(sources).map(sourceName -> Status.of(manager, sourceName)), BY_SOURCE);
    }

    private static final Comparator<Status> BY_SOURCE = Comparator.comparing(Status::getSource);

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    @lombok.Value
    private static class Status {

        static Status of(SdmxWebManager manager, String sourceName) {
            SdmxWebSource source = manager.getSources().get(sourceName);
            if (source == null) {
                return failure(sourceName, "Cannot find source");
            }
            if (source.getMonitor() == null) {
                return failure(sourceName, "No monitor defined");
            }
            try {
                return success(sourceName, manager.getMonitorReport(source));
            } catch (IOException ex) {
                return failure(sourceName, ex);
            }
        }

        static @NonNull Status success(@NonNull String source, @NonNull SdmxWebMonitorReport report) {
            return new Status(source, report, null);
        }

        static @NonNull Status failure(@NonNull String source, @NonNull IOException cause) {
            return new Status(source, SdmxWebMonitorReport.EMPTY, cause.getMessage());
        }

        static @NonNull Status failure(@NonNull String source, @NonNull String cause) {
            return new Status(source, SdmxWebMonitorReport.EMPTY, cause);
        }

        @lombok.NonNull
        String source;

        @lombok.NonNull
        @lombok.experimental.Delegate
        SdmxWebMonitorReport report;

        @Nullable
        String cause;
    }
}
