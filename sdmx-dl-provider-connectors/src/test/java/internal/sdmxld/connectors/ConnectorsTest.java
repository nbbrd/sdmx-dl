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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sdmxdl.Attribute;
import sdmxdl.AttributeRelationship;
import sdmxdl.DataStructure;
import sdmxdl.Dataflow;
import tests.sdmxdl.api.RepoSamples;

import java.net.HttpURLConnection;

import static internal.sdmxdl.provider.connectors.Connectors.*;
import static it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory.createRestException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tests.sdmxdl.api.RepoSamples.*;

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
        // description field not supported in Connectors
        Dataflow dataflowWithoutDescription = FLOW.toBuilder().description(null).build();
        assertThat(toFlow(fromFlow(dataflowWithoutDescription)))
                .isEqualTo(dataflowWithoutDescription);
    }

    @Test
    public void testStructureRef() {
        assertThat(toStructureRef(fromStructureRef(STRUCT_REF)))
                .isEqualTo(STRUCT_REF);
    }

    @Disabled("CodeList is no more embedded")
    @Test
    public void testDimension() {
        assertThat(toDimension(fromDimension(DIM1)))
                .isEqualTo(DIM1);
    }

    @Test
    public void testAttribute() {
        // attribute relationship not supported in Connectors
        Attribute notCodedAttributeWithoutRelationship = NOT_CODED_ATTRIBUTE.toBuilder().relationship(AttributeRelationship.UNKNOWN).build();
        assertThat(toAttribute(fromAttribute(notCodedAttributeWithoutRelationship)))
                .isEqualTo(notCodedAttributeWithoutRelationship);
    }

    @Disabled("CodeList is no more embedded")
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
                .clearAttributes()
                .attribute(NOT_CODED_ATTRIBUTE.toBuilder().relationship(AttributeRelationship.UNKNOWN).build())
                .attribute(CODED_ATTRIBUTE.toBuilder().relationship(AttributeRelationship.UNKNOWN).build())
                .build();

        assertThat(toStructure(fromStructure(contiguousPositions)))
                .isEqualTo(contiguousPositions);
    }
}
