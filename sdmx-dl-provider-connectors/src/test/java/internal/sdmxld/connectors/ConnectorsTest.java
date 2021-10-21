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
package internal.sdmxld.connectors;

import org.junit.jupiter.api.Test;
import sdmxdl.*;

import java.net.HttpURLConnection;

import static internal.sdmxdl.connectors.Connectors.*;
import static it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory.createRestException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class ConnectorsTest {

    @Test
    public void testIsNoResultMatchingQuery() {
        assertThat(isNoResultMatchingQuery(createRestException(HttpURLConnection.HTTP_NOT_FOUND, null, null))).isTrue();
        assertThat(isNoResultMatchingQuery(createRestException(HttpURLConnection.HTTP_BAD_REQUEST, null, null))).isFalse();
    }

    @Test
    public void testFlow() {
        Dataflow o = Dataflow.of(DataflowRef.parse("flow1"), DataStructureRef.parse("struct1"), "label0");
        assertThat(toFlow(fromFlow(o))).isEqualTo(o);
    }

    @Test
    public void testStructureRef() {
        DataStructureRef o = DataStructureRef.parse("struct1");
        assertThat(toStructureRef(fromStructureRef(o))).isEqualTo(o);
    }

    @Test
    public void testDimension() {
        Dimension o = Dimension.builder().id("1234").label("label1").position(1).code("hello", "world").build();
        assertThat(toDimension(fromDimension(o))).isEqualTo(o);
    }

    @Test
    public void testStructure() {
        DataStructure o = DataStructure
                .builder()
                .ref(DataStructureRef.parse("struct1"))
                .dimension(Dimension.builder().id("1234").label("label1").position(1).code("hello", "world").build())
                .label("label2")
                .primaryMeasureId("OBS_VALUE")
                .timeDimensionId("obs_period")
                .build();
        assertThat(toStructure(fromStructure(o))).isEqualTo(o);
    }
}
