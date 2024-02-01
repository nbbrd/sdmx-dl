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
package tests.sdmxdl.ext;

import internal.util.PersistenceLoader;
import nbbrd.design.MightBeGenerated;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.DataRepository;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSources;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.TckUtil;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Set;

import static java.util.Collections.emptyList;

@lombok.experimental.UtilityClass
public class PersistenceAssert {

    @MightBeGenerated
    private static final ExtensionPoint<Persistence> EXTENSION_POINT = ExtensionPoint
            .<Persistence>builder()
            .id(Persistence::getPersistenceId)
            .idPattern(PersistenceLoader.ID_PATTERN)
            .rank(Persistence::getPersistenceRank)
            .rankLowerBound(Persistence.UNKNOWN_PERSISTENCE_RANK)
            .properties(ignore -> emptyList())
            .propertiesPrefix("")
            .build();

    public void assertCompliance(Persistence persistence) {
        TckUtil.run(s -> assertCompliance(s, persistence));
    }

    public void assertCompliance(SoftAssertions s, Persistence persistence) throws Exception {
        EXTENSION_POINT.assertCompliance(s, persistence);

        Set<Class<? extends HasPersistence>> supportedTypes = persistence.getFormatSupportedTypes();
        s.assertThat(supportedTypes).isNotNull();

        checkDataRepository(s, persistence.getFormat(DataRepository.class), supportedTypes.contains(DataRepository.class));
        checkMonitorReports(s, persistence.getFormat(MonitorReports.class), supportedTypes.contains(MonitorReports.class));
        checkWebSources(s, persistence.getFormat(WebSources.class), supportedTypes.contains(WebSources.class));
    }

    private void checkDataRepository(SoftAssertions s, FileFormat<DataRepository> fileFormat, boolean supported) throws IOException {
        FileFormatAssert.assertCompliance(s, fileFormat, RepoSamples.EMPTY_REPO, supported);

        DataRepository nullFlowDescription = RepoSamples.EMPTY_REPO
                .toBuilder()
                .flow(RepoSamples.FLOW.toBuilder().description(null).build())
                .build();
        FileFormatAssert.assertCompliance(s, fileFormat, nullFlowDescription, supported);

        DataRepository nullTimeDimensionId = RepoSamples.EMPTY_REPO
                .toBuilder()
                .structure(RepoSamples.STRUCT.toBuilder().timeDimensionId(null).build())
                .build();
        FileFormatAssert.assertCompliance(s, fileFormat, nullTimeDimensionId, supported);

        DataRepository normal = RepoSamples.REPO
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        FileFormatAssert.assertCompliance(s, fileFormat, normal, supported);
    }

    private void checkMonitorReports(SoftAssertions s, FileFormat<MonitorReports> fileFormat, boolean supported) throws IOException {
        FileFormatAssert.assertCompliance(s, fileFormat, RepoSamples.EMPTY_REPORTS, supported);

        sdmxdl.web.MonitorReports normal = RepoSamples.REPORTS
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        FileFormatAssert.assertCompliance(s, fileFormat, normal, supported);
    }

    private void checkWebSources(SoftAssertions s, FileFormat<WebSources> fileFormat, boolean supported) throws IOException {
        FileFormatAssert.assertCompliance(s, fileFormat, WebSources.EMPTY, supported);
        FileFormatAssert.assertCompliance(s, fileFormat, WebSources.builder()
                .source(RepoSamples.BASIC_SOURCE)
                .source(RepoSamples.FULL_SOURCE)
                .build(), supported);
    }
}
