/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.util.file;

import _test.sdmxdl.util.XRepoFileClient;
import org.junit.jupiter.api.Test;
import sdmxdl.DataQuery;
import sdmxdl.Series;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.SdmxConnectionAssert;
import tests.sdmxdl.file.SdmxFileConnectionAssert;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SdmxFileConnectionImplTest {

    @Test
    public void testCompliance() throws IOException {
        SdmxFileConnectionAssert.assertCompliance(
                () -> new SdmxFileConnectionImpl(new XRepoFileClient(RepoSamples.REPO), RepoSamples.FLOW),
                SdmxFileConnectionAssert.Sample
                        .builder()
                        .connection(SdmxConnectionAssert.Sample
                                .builder()
                                .validFlow(RepoSamples.FLOW_REF)
                                .invalidFlow(RepoSamples.BAD_FLOW_REF)
                                .validKey(RepoSamples.K1)
                                .invalidKey(RepoSamples.INVALID_KEY)
                                .build())
                        .build()
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testFile() throws IOException {
        SdmxFileClient r = new XRepoFileClient(RepoSamples.REPO);

        SdmxFileConnectionImpl conn = new SdmxFileConnectionImpl(r, RepoSamples.FLOW);

        assertThat(conn.getDataflowRef()).isEqualTo(RepoSamples.FLOW_REF);
        assertThat(conn.getFlow()).isEqualTo(conn.getFlow(RepoSamples.FLOW_REF));
        assertThat(conn.getStructure()).isEqualTo(conn.getStructure(RepoSamples.FLOW_REF));

        try (Stream<Series> stream = conn.getDataStream(conn.getDataflowRef(), DataQuery.ALL)) {
            assertThat(stream).containsExactly(conn.getDataStream(conn.getDataflowRef(), DataQuery.ALL).toArray(Series[]::new));
        }
    }
}
