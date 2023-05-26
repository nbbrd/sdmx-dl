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

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.TckUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

@lombok.experimental.UtilityClass
public class FileFormatProviderAssert {

    public void assertCompliance(FileFormatProvider provider) {
        TckUtil.run(s -> assertCompliance(s, provider));
    }

    public void assertCompliance(SoftAssertions s, FileFormatProvider provider) throws Exception {
        checkDataRepository(s, provider.getDataRepositoryFormat());
        checkMonitorReports(s, provider.getMonitorReportsFormat());
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

    private static <T> void assertValid(SoftAssertions s, FileFormat<T> fileFormat, T data) throws IOException {
        s.assertThat(storeLoad(fileFormat.getParser(), fileFormat.getFormatter(), data))
                .isEqualTo(data)
                .isNotSameAs(data);
    }

    private static <T> T storeLoad(FileParser<T> parser, FileFormatter<T> formatter, T data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            formatter.formatStream(data, output);
            try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                return parser.parseStream(input);
            }
        }
    }
}
