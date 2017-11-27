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
package internal.file;

import internal.file.xml.StaxSdmxDecoder;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import static be.nbb.sdmx.facade.LanguagePriorityList.ANY;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.file.SdmxFileSet;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.tck.ConnectionAssert;
import be.nbb.sdmx.facade.xml.stream.Stax;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class SdmxFileConnectionImplTest {

    @Test
    @SuppressWarnings("null")
    public void testFile() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSet files = SdmxFileSet.builder().data(compact21).build();

        SdmxFileConnectionImpl.Resource r = new SdmxDecoderResource(files, ANY, factoryWithoutNamespace, decoder, Optional.empty());
        SdmxFileConnectionImpl conn = new SdmxFileConnectionImpl(r, dataflow);

        assertThat(conn.getDataflowRef()).isEqualTo(files.asDataflowRef());
        assertThat(conn.getFlow()).isEqualTo(conn.getFlow(files.asDataflowRef()));
        assertThat(conn.getStructure()).isEqualTo(conn.getStructure(files.asDataflowRef()));
        assertThatNullPointerException().isThrownBy(() -> conn.getCursor(null));
        assertThatNullPointerException().isThrownBy(() -> conn.getStream(null));
        try (Stream<Series> stream = conn.getStream(DataQuery.of(Key.ALL, false))) {
            assertThat(stream).containsExactly(conn.getStream(DataQuery.of(Key.ALL, false)).toArray(Series[]::new));
        }
    }

    @Test
    public void testCompactData21() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSet files = SdmxFileSet.builder().data(compact21).build();

        SdmxFileConnectionImpl.Resource r = new SdmxDecoderResource(files, ANY, factoryWithoutNamespace, decoder, Optional.empty());
        SdmxFileConnectionImpl conn = new SdmxFileConnectionImpl(r, dataflow);

        assertThat(conn.getFlows()).hasSize(1);
        assertThat(conn.getStructure(files.asDataflowRef()).getDimensions()).hasSize(7);

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (DataCursor o = conn.getCursor(files.asDataflowRef(), DataQuery.of(Key.ALL, false))) {
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

        ConnectionAssert.assertCompliance(() -> new SdmxFileConnectionImpl(r, dataflow), files.asDataflowRef());
    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private final XMLInputFactory factoryWithoutNamespace = Stax.getInputFactoryWithoutNamespace();
    private final SdmxDecoder decoder = new StaxSdmxDecoder(Stax.getInputFactory(), factoryWithoutNamespace);
    private final Dataflow dataflow = Dataflow.of(DataflowRef.parse("data"), DataStructureRef.parse("xyz"), "label");
}
