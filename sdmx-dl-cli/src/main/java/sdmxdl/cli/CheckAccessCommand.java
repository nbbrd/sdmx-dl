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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import sdmxdl.Connection;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "access")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckAccessCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Access> getTable() {
        return CsvTable
                .builderOf(Access.class)
                .columnOf("Source", Access::getSource, Formatter.onString())
                .columnOf("Accessible", Access::isAccessible, o -> Boolean.TRUE.equals(o) ? "YES" : "NO")
                .columnOf("DurationInMillis", Access::getDuration, CheckAccessCommand::formatDuration)
                .columnOf("ErrorMessage", Access::getCause, Formatter.onString())
                .build();
    }

    private Stream<Access> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        Stream<String> sources = web.isAllSources() ? WebSourcesOptions.getAllSourceNames(manager) : web.getSources().stream();
        return sort.applySort(web.applyParallel(sources).map(sourceName -> Access.of(manager, sourceName)), BY_SOURCE);
    }

    private static String formatDuration(Duration o) {
        return o != null ? String.valueOf(o.toMillis()) : null;
    }

    private static final Comparator<Access> BY_SOURCE = Comparator.comparing(Access::getSource);

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    @lombok.Value
    private static class Access {

        static @NonNull Access of(@NonNull SdmxWebManager manager, @NonNull String source) {
            try (Connection conn = manager.getConnection(source)) {
                Clock clock = Clock.systemDefaultZone();
                Instant start = clock.instant();
                conn.testConnection();
                return success(source, Duration.between(start, clock.instant()));
            } catch (IOException ex) {
                return failure(source, ex);
            }
        }

        static @NonNull Access success(@NonNull String source, @NonNull Duration duration) {
            return new Access(source, duration, null);
        }

        static @NonNull Access failure(@NonNull String source, @NonNull IOException cause) {
            return new Access(source, null, cause.getMessage());
        }

        @lombok.NonNull
        String source;

        @Nullable
        Duration duration;

        @Nullable
        String cause;

        public boolean isAccessible() {
            return cause == null;
        }
    }
}
