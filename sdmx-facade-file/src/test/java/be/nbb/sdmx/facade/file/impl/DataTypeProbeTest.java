/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.sdmx.facade.file.impl;

import be.nbb.sdmx.facade.file.SdmxDecoder;
import be.nbb.sdmx.facade.samples.SdmxSource;
import java.io.IOException;
import org.junit.Test;
import java.io.Reader;
import javax.xml.stream.XMLInputFactory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DataTypeProbeTest {

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    @Test
    public void testDecodeGeneric20() throws IOException {
        try (Reader stream = SdmxSource.OTHER_GENERIC20.openReader()) {
            assertThat(DataTypeProbe.probeDataType(factory, stream)).isEqualTo(SdmxDecoder.DataType.GENERIC20);
        }
    }

    @Test
    public void testDecodeCompact20() throws IOException {
        try (Reader stream = SdmxSource.OTHER_COMPACT20.openReader()) {
            assertThat(DataTypeProbe.probeDataType(factory, stream)).isEqualTo(SdmxDecoder.DataType.COMPACT20);
        }
    }

    @Test
    public void testDecodeGeneric21() throws IOException {
        try (Reader stream = SdmxSource.OTHER_GENERIC21.openReader()) {
            assertThat(DataTypeProbe.probeDataType(factory, stream)).isEqualTo(SdmxDecoder.DataType.GENERIC21);
        }
    }

    @Test
    public void testDecodeCompact21() throws IOException {
        try (Reader stream = SdmxSource.OTHER_COMPACT21.openReader()) {
            assertThat(DataTypeProbe.probeDataType(factory, stream)).isEqualTo(SdmxDecoder.DataType.COMPACT21);
        }
    }
}
