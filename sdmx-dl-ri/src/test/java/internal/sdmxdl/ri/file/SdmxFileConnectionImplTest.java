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
package internal.sdmxdl.ri.file;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sdmxdl.*;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.tck.SdmxConnectionAssert;
import sdmxdl.tck.file.SdmxFileConnectionAssert;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.LanguagePriorityList.ANY;

/**
 * @author Philippe Charles
 */
public class SdmxFileConnectionImplTest {

    @Test
    public void testCompliance() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource source = sourceOf(compact21);
        SdmxFileConnectionImpl.Resource r = new SdmxDecoderResource(source, ANY, DECODER, null);
        DataflowRef valid = DATAFLOW.getRef();
        DataflowRef invalid = DataflowRef.parse("invalid");

        SdmxFileConnectionAssert.assertCompliance(
                () -> new SdmxFileConnectionImpl(r, DATAFLOW),
                SdmxFileConnectionAssert.Sample
                        .builder()
                        .connection(SdmxConnectionAssert.Sample.builder().valid(valid).invalid(invalid).build())
                        .build()
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testFile() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource source = sourceOf(compact21);
        SdmxFileConnectionImpl.Resource r = new SdmxDecoderResource(source, ANY, DECODER, null);

        SdmxFileConnectionImpl conn = new SdmxFileConnectionImpl(r, DATAFLOW);

        assertThat(conn.getDataflowRef()).isEqualTo(source.asDataflowRef());
        assertThat(conn.getFlow()).isEqualTo(conn.getFlow(source.asDataflowRef()));
        assertThat(conn.getStructure()).isEqualTo(conn.getStructure(source.asDataflowRef()));
        assertThatNullPointerException().isThrownBy(() -> conn.getDataCursor(Key.ALL, null));
        assertThatNullPointerException().isThrownBy(() -> conn.getDataStream(Key.ALL, null));
        try (Stream<Series> stream = conn.getDataStream(Key.ALL, DataFilter.ALL)) {
            assertThat(stream).containsExactly(conn.getDataStream(Key.ALL, DataFilter.ALL).toArray(Series[]::new));
        }
    }

    @Test
    public void testCompactData21() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource source = sourceOf(compact21);
        SdmxFileConnectionImpl.Resource r = new SdmxDecoderResource(source, ANY, DECODER, null);

        SdmxFileConnectionImpl conn = new SdmxFileConnectionImpl(r, DATAFLOW);

        assertThat(conn.getFlows()).hasSize(1);
        assertThat(conn.getStructure(source.asDataflowRef()).getDimensions()).hasSize(7);

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (DataCursor o = conn.getDataCursor(source.asDataflowRef(), Key.ALL, DataFilter.ALL)) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(key);
            assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
            int indexObs = -1;
            while (o.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertThat(o.getObsPeriod()).isEqualTo("1960-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(92.0142);
                        break;
                    case 56:
                        assertThat(o.getObsPeriod()).isEqualTo("2016-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(386.5655);
                        break;
                }
            }
            assertThat(indexObs).isEqualTo(56);
            assertThat(o.nextSeries()).isFalse();
        }

        SdmxConnectionAssert.assertCompliance(
                () -> new SdmxFileConnectionImpl(r, DATAFLOW),
                SdmxConnectionAssert.Sample.builder().valid(source.asDataflowRef()).build()
        );
    }

    public static SdmxFileSource sourceOf(File compact21) {
        return SdmxFileSource.builder().data(compact21).build();
    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    public static final SdmxDecoder DECODER = new StaxSdmxDecoder();
    public static final Dataflow DATAFLOW = Dataflow.of(DataflowRef.parse("data"), DataStructureRef.parse("xyz"), "label");
}
