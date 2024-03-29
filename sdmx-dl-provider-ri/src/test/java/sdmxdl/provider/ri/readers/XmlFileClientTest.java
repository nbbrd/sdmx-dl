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
package sdmxdl.provider.ri.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.*;
import sdmxdl.file.FileSource;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.file.FileConnection;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileInfo;
import tests.sdmxdl.api.ConnectionAssert;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Languages.ANY;

/**
 * @author Philippe Charles
 */
public class XmlFileClientTest {

    @Test
    public void testCompactData21(@TempDir Path temp) throws IOException {
        File compact21 = temp.resolve("compact21").toFile();
        SdmxXmlSources.OTHER_COMPACT21.copyTo(compact21);

        FileSource source = sourceOf(compact21);
        FileClient x = new XmlFileClient(source, ANY, DECODER, null, null);

        FileInfo info = x.decode();

        assertThat(info.getDataType()).isEqualTo(XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21);
        assertThat(info.getStructure().getDimensions()).hasSize(7);

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (Stream<Series> o = x.loadData(info, DataRef.of(source.asDataflowRef(), Query.ALL))) {
            assertThat(o)
                    .singleElement()
                    .satisfies(series -> {
                        assertThat(series.getKey()).isEqualTo(key);
//                        assertThat(series.getFreq()).isEqualTo(Frequency.ANNUAL);
                        assertThat(series.getObs())
                                .hasSize(57)
                                .element(0)
                                .satisfies(obs -> {
                                    assertThat(obs.getPeriod()).hasToString("1960-01-01T00:00/P1Y");
                                    assertThat(obs.getValue()).isEqualTo(92.0142);
                                });
                        assertThat(series.getObs())
                                .element(56)
                                .satisfies(obs -> {
                                    assertThat(obs.getPeriod()).hasToString("2016-01-01T00:00/P1Y");
                                    assertThat(obs.getValue()).isEqualTo(386.5655);
                                });
                    });
        }

        ConnectionAssert.assertCompliance(
                () -> new FileConnection(x, FLOW),
                ConnectionAssert.Sample
                        .builder()
                        .validFlow(source.asDataflowRef())
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(key)
                        .invalidKey(Key.of("zzz"))
                        .build()
        );
    }

    public static FileSource sourceOf(File compact21) {
        return FileSource.builder().data(compact21).build();
    }

    public static final Decoder DECODER = new XmlDecoder(null);
    public static final Flow FLOW = Flow.builder().ref(FlowRef.parse("data")).structureRef(StructureRef.parse("xyz")).name("label").build();
}
