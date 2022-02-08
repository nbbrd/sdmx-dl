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
import sdmxdl.DataStructure;
import sdmxdl.samples.RepoSamples;

import java.net.HttpURLConnection;

import static internal.sdmxdl.connectors.Connectors.*;
import static it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory.createRestException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static sdmxdl.samples.RepoSamples.*;

/**
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
        assertThat(toFlow(fromFlow(RepoSamples.FLOW)))
                .isEqualTo(RepoSamples.FLOW);
    }

    @Test
    public void testStructureRef() {
        assertThat(toStructureRef(fromStructureRef(STRUCT_REF)))
                .isEqualTo(STRUCT_REF);
    }

    @Test
    public void testDimension() {
        assertThat(toDimension(fromDimension(DIM1)))
                .isEqualTo(DIM1);
    }

    @Test
    public void testAttribute() {
        assertThat(toAttribute(fromAttribute(RepoSamples.NOT_CODED_ATTRIBUTE)))
                .isEqualTo(RepoSamples.NOT_CODED_ATTRIBUTE);
    }

    @Test
    public void testStructure() {
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
                .describedAs("Non-contiguous positions fail in connectors")
                .isThrownBy(() -> toStructure(fromStructure(RepoSamples.STRUCT)));

        DataStructure contiguousPositions = RepoSamples.STRUCT
                .toBuilder()
                .clearDimensions()
                .dimension(DIM1)
                .dimension(DIM2.toBuilder().position(2).build())
                .dimension(DIM3.toBuilder().position(3).build())
                .build();

        assertThat(toStructure(fromStructure(contiguousPositions)))
                .isEqualTo(contiguousPositions);
    }
}
