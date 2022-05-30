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
package _test.sdmxdl.connectors.samples;

import org.junit.jupiter.api.Test;
import sdmxdl.*;

import java.io.IOException;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class ParsersTest {

    @Test
    public void test() throws Exception {
        assertThat(ConnectorsResource.nbb()).isEqualTo(removeAttributeRelationship(FacadeResource.nbb()));
        assertThat(ConnectorsResource.ecb()).isEqualTo(removeAttributeRelationship(FacadeResource.ecb()));
    }

    private DataRepository removeAttributeRelationship(DataRepository repo) {
        return repo
                .toBuilder()
                .clearStructures()
                .structures(repo.getStructures().stream().map(this::removeAttributeRelationship).collect(toList()))
                .build();
    }

    private DataStructure removeAttributeRelationship(DataStructure dsd) {
        return dsd
                .toBuilder()
                .clearAttributes()
                .attributes(dsd.getAttributes().stream().map(this::removeAttributeRelationship).collect(toSet()))
                .build();
    }

    private Attribute removeAttributeRelationship(Attribute attribute) {
        return attribute.toBuilder().relationship(AttributeRelationship.UNKNOWN).build();
    }

    @Test
    public void testEcbContent() throws IOException {
        assertThat(FacadeResource.ecb()).satisfies(repository -> {
            assertThat(repository.getName())
                    .isEqualTo("ECB");

            assertThat(repository.getStructures())
                    .hasSize(1)
                    .element(0)
                    .satisfies(dsd -> {
                        assertThat(dsd.getRef())
                                .isEqualTo(DataStructureRef.of("ECB", "ECB_AME1", "1.0"));
                        assertThat(dsd.getDimensions())
                                .hasSize(7)
                                .element(0)
                                .satisfies(dimension -> {
                                    assertThat(dimension.getId())
                                            .isEqualTo("FREQ");
                                    assertThat(dimension.getLabel())
                                            .isEqualTo("Frequency");
                                    assertThat(dimension.getPosition())
                                            .isEqualTo(1);
                                    assertThat(dimension.getCodelist()).satisfies(codelist -> {
                                        assertThat(codelist.getRef())
                                                .isEqualTo(CodelistRef.of("ECB", "CL_FREQ", "1.0"));
                                        assertThat(codelist.getCodes())
                                                .hasSize(10)
                                                .containsEntry("A", "Annual");
                                    });
                                });
                        assertThat(dsd.getAttributes())
                                .hasSize(11)
                                .element(1)
                                .satisfies(attribute -> {
                                    assertThat(attribute.getId())
                                            .isEqualTo("OBS_STATUS");
                                    assertThat(attribute.getLabel())
                                            .isEqualTo("Observation status");
                                    assertThat(attribute.getCodelist()).satisfies(codelist -> {
                                        assertThat(codelist.getRef())
                                                .isEqualTo(CodelistRef.of("ECB", "CL_OBS_STATUS", "1.0"));
                                        assertThat(codelist.getCodes())
                                                .hasSize(17)
                                                .containsEntry("A", "Normal value");
                                    });
                                });
                        assertThat(dsd.getTimeDimensionId())
                                .isEqualTo("TIME_PERIOD");
                        assertThat(dsd.getPrimaryMeasureId())
                                .isEqualTo("OBS_VALUE");
                        assertThat(dsd.getLabel())
                                .isEqualTo("AMECO");
                    });

            assertThat(repository.getFlows())
                    .hasSize(3)
                    .element(0)
                    .extracting(Dataflow::getRef)
                    .isEqualTo(DataflowRef.of("ECB", "AME", "1.0"));

            assertThat(repository.getDataSets())
                    .hasSize(1);
        });
    }

    @Test
    public void testNbbContent() throws IOException {
        assertThat(FacadeResource.nbb()).satisfies(repository -> {
            assertThat(repository.getName())
                    .isEqualTo("NBB");

            assertThat(repository.getStructures())
                    .hasSize(1)
                    .element(0)
                    .satisfies(dsd -> {
                        assertThat(dsd.getRef())
                                .isEqualTo(DataStructureRef.of("NBB", "TEST_DATASET", "latest"));
                        assertThat(dsd.getDimensions())
                                .hasSize(3)
                                .element(0)
                                .satisfies(dimension -> {
                                    assertThat(dimension.getId())
                                            .isEqualTo("SUBJECT");
                                    assertThat(dimension.getLabel())
                                            .isEqualTo("Sujet");
                                    assertThat(dimension.getPosition())
                                            .isEqualTo(1);
                                    assertThat(dimension.getCodelist()).satisfies(codelist -> {
                                        assertThat(codelist.getRef())
                                                .isEqualTo(CodelistRef.of("NBB", "CL_TEST_DATASET_SUBJECT", "latest"));
                                        assertThat(codelist.getCodes())
                                                .hasSize(1)
                                                .containsEntry("LOCSTL04", "Corrigé de l'amplitude (ICA)");
                                    });
                                });
                        assertThat(dsd.getAttributes())
                                .hasSize(2)
                                .element(0)
                                .satisfies(attribute -> {
                                    assertThat(attribute.getId())
                                            .isEqualTo("OBS_STATUS");
                                    assertThat(attribute.getLabel())
                                            .isEqualTo("Observation Status");
                                    assertThat(attribute.getCodelist()).satisfies(codelist -> {
                                        assertThat(codelist.getRef())
                                                .isEqualTo(CodelistRef.of("NBB", "CL_TEST_DATASET_OBS_STATUS", "latest"));
                                        assertThat(codelist.getCodes())
                                                .hasSize(7)
                                                .containsEntry("D", "Définitif");
                                    });
                                });
                        assertThat(dsd.getTimeDimensionId())
                                .isEqualTo("TIME");
                        assertThat(dsd.getPrimaryMeasureId())
                                .isEqualTo("OBS_VALUE");
                        assertThat(dsd.getLabel())
                                .isEqualTo("Mon premier dataset");
                    });

            assertThat(repository.getFlows())
                    .hasSize(1)
                    .element(0)
                    .extracting(Dataflow::getRef)
                    .isEqualTo(DataflowRef.of("NBB", "TEST_DATASET", "latest"));

            assertThat(repository.getDataSets())
                    .hasSize(1);
        });
    }
}
