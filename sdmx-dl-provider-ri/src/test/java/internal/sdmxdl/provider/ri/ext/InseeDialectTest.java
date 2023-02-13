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
package internal.sdmxdl.provider.ri.ext;

import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.provider.ext.SeriesMetaFactory;
import tests.sdmxdl.ext.DialectAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class InseeDialectTest {

    @Test
    public void testCompliance() {
        DialectAssert.assertDialectCompliance(new InseeDialect());
    }

    @Test
    public void testFreqParser() {
        Key.Builder key = Key.builder(dsd);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "A").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.ANNUAL);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "T").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.QUARTERLY);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "M").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.MONTHLY);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "B").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.MONTHLY);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "S").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.HALF_YEARLY);
        assertThat(InseeDialect.getFreqFactory(dsd).get(Series.builder().key(key.put("FREQ", "X").build()).build()).getTimeUnit()).isEqualTo(SeriesMetaFactory.UNDEFINED);
    }

    // https://bdm.insee.fr/series/sdmx/codelist/FR1/CL_PERIODICITE/1.0
    private final Codelist cl_periodicite = Codelist
            .builder()
            .ref(CodelistRef.of("FR1", "CL_PERIODICITE", "1.0"))
            .code("A", "Annuelle")
            .code("B", "Bimestrielle")
            .code("M", "Mensuelle")
            .code("S", "Semestrielle")
            .code("T", "Trimestrielle")
            .build();

    // https://bdm.insee.fr/series/sdmx/datastructure/FR1/IPI-2010/1.0?references=children
    private final DataStructure dsd = DataStructure
            .builder()
            .dimension(Dimension.builder().id("FREQ").position(1).name("Périodicité").codelist(cl_periodicite).build())
            .ref(DataStructureRef.of("FR1", "IPI-2010", "1.0"))
            .timeDimensionId("TIME_PERIOD")
            .primaryMeasureId("OBS_VALUE")
            .name("Indices de la production industrielle - Résultats par secteur d'activité détaillé et regroupements MIG")
            .build();
}
