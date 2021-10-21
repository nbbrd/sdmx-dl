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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.*;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.samples.RepoSamples;
import sdmxdl.samples.SdmxSource;
import sdmxdl.tck.SdmxConnectionAssert;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileConnectionImpl;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.LanguagePriorityList.ANY;

/**
 * @author Philippe Charles
 */
public class XmlFileClientTest {

    @Test
    public void testCompactData21(@TempDir Path temp) throws IOException {
        File compact21 = temp.resolve("compact21").toFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource source = sourceOf(compact21);
        SdmxFileClient r = new XmlFileClient(source, ANY, DECODER, null, SdmxFileListener.noOp());

        SdmxFileInfo info = r.decode();

        assertThat(info.getDataType()).isEqualTo(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
        assertThat(info.getStructure().getDimensions()).hasSize(7);

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (DataCursor o = r.loadData(info, source.asDataflowRef(), Key.ALL, DataFilter.FULL)) {
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
                SdmxConnectionAssert.Sample
                        .builder()
                        .validFlow(source.asDataflowRef())
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(key)
                        .invalidKey(Key.of("zzz"))
                        .build()
        );
    }

    public static SdmxFileSource sourceOf(File compact21) {
        return SdmxFileSource.builder().data(compact21).build();
    }

    public static final SdmxDecoder DECODER = new XmlDecoder(SdmxFileListener.noOp());
    public static final Dataflow DATAFLOW = Dataflow.of(DataflowRef.parse("data"), DataStructureRef.parse("xyz"), "label");
}
