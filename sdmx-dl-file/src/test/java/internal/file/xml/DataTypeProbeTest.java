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
package internal.file.xml;

import be.nbb.sdmx.facade.samples.SdmxSource;
import internal.file.SdmxDecoder;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DataTypeProbeTest {

    @Test
    public void testDecodeGeneric20() throws IOException {
        assertThat(DataTypeProbe.of().parseReader(SdmxSource.OTHER_GENERIC20::openReader))
                .isEqualTo(SdmxDecoder.DataType.GENERIC20);
    }

    @Test
    public void testDecodeCompact20() throws IOException {
        assertThat(DataTypeProbe.of().parseReader(SdmxSource.OTHER_COMPACT20::openReader))
                .isEqualTo(SdmxDecoder.DataType.COMPACT20);
    }

    @Test
    public void testDecodeGeneric21() throws IOException {
        assertThat(DataTypeProbe.of().parseReader(SdmxSource.OTHER_GENERIC21::openReader))
                .isEqualTo(SdmxDecoder.DataType.GENERIC21);
    }

    @Test
    public void testDecodeCompact21() throws IOException {
        assertThat(DataTypeProbe.of().parseReader(SdmxSource.OTHER_COMPACT21::openReader))
                .isEqualTo(SdmxDecoder.DataType.COMPACT21);
    }
}
