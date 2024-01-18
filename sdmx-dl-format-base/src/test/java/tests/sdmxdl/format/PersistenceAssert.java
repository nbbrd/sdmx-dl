/*
 * Copyright 2016 National Bank of Belgium
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
package tests.sdmxdl.format;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.DataRepository;
import sdmxdl.format.WebSources;
import sdmxdl.format.spi.FileFormat;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.TckUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

@lombok.experimental.UtilityClass
public class PersistenceAssert {

    public void assertCompliance(Persistence formatting) {
        TckUtil.run(s -> assertCompliance(s, formatting));
    }

    public void assertCompliance(SoftAssertions s, Persistence formatting) throws Exception {
        checkDataRepository(s, formatting.getRepositoryFormat());
        checkMonitorReports(s, formatting.getMonitorFormat());
        checkWebSources(s, formatting.getSourcesFormat());
    }

    private void checkDataRepository(SoftAssertions s, FileFormat<DataRepository> fileFormat) throws IOException {
        assertValid(s, fileFormat, RepoSamples.EMPTY_REPO);

        DataRepository nullFlowDescription = RepoSamples.EMPTY_REPO
                .toBuilder()
                .flow(RepoSamples.FLOW.toBuilder().description(null).build())
                .build();
        assertValid(s, fileFormat, nullFlowDescription);

        DataRepository nullTimeDimensionId = RepoSamples.EMPTY_REPO
                .toBuilder()
                .structure(RepoSamples.STRUCT.toBuilder().timeDimensionId(null).build())
                .build();
        assertValid(s, fileFormat, nullTimeDimensionId);

        DataRepository normal = RepoSamples.REPO
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(s, fileFormat, normal);
    }

    private void checkMonitorReports(SoftAssertions s, FileFormat<MonitorReports> fileFormat) throws IOException {
        assertValid(s, fileFormat, RepoSamples.EMPTY_REPORTS);

        sdmxdl.web.MonitorReports normal = RepoSamples.REPORTS
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(s, fileFormat, normal);
    }

    private void checkWebSources(SoftAssertions s, FileFormat<WebSources> fileFormat) throws IOException {
        assertValid(s, fileFormat, WebSources.builder().build());
        assertValid(s, fileFormat, WebSources.builder()
                .source(RepoSamples.BASIC_SOURCE)
                .source(RepoSamples.FULL_SOURCE)
                .build());
    }

    private static <T> void assertValid(SoftAssertions s, FileFormat<T> fileFormat, T data) throws IOException {
        if (fileFormat.isFormattingSupported()) {
            if (fileFormat.isParsingSupported()) {
                // path
                s.assertThat(storeLoadPath(fileFormat, data))
                        .isEqualTo(data)
                        .isNotSameAs(data);
                // stream
                s.assertThat(storeLoadStream(fileFormat, data))
                        .isEqualTo(data)
                        .isNotSameAs(data);
            } else {
                // path
                Path tmpFile = Files.createTempFile("store", "load");
                try {
                    s.assertThatCode(() -> fileFormat.formatPath(data, tmpFile)).doesNotThrowAnyException();
                    s.assertThatIOException().isThrownBy(() -> fileFormat.parsePath(tmpFile));
                } finally {
                    Files.deleteIfExists(tmpFile);
                }
                // stream
                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    s.assertThatCode(() -> fileFormat.formatStream(data, output)).doesNotThrowAnyException();
                    try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                        s.assertThatIOException().isThrownBy(() -> fileFormat.parseStream(input));
                    }
                }
            }
        }
    }

    private static <T> T storeLoadPath(FileFormat<T> fileFormat, T data) throws IOException {
        Path tmpFile = Files.createTempFile("store", "load");
        try {
            fileFormat.formatPath(data, tmpFile);
            return fileFormat.parsePath(tmpFile);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    private static <T> T storeLoadStream(FileFormat<T> fileFormat, T data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            fileFormat.formatStream(data, output);
            try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                return fileFormat.parseStream(input);
            }
        }
    }
}
