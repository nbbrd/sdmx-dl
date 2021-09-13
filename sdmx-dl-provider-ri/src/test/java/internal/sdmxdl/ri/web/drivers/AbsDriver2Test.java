/*
 * Copyright 2018 National Bank of Belgium
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
package internal.sdmxdl.ri.web.drivers;

import org.junit.Test;
import sdmxdl.DataFilter;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.tck.web.SdmxWebDriverAssert;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class AbsDriver2Test {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new AbsDriver2());
    }

    @Test
    public void testQueries() throws MalformedURLException {
        URL endpoint = new URL("http://stat.data.abs.gov.au/restsdmx/sdmx.ashx");

        AbsDriver2.AbsQueries queries = new AbsDriver2.AbsQueries();

        assertThat(queries.getStructureQuery(endpoint, DataStructureRef.parse("ABS_REGIONAL_ASGS")))
                .describedAs("SdmxFix#1")
                .hasToString("http://stat.data.abs.gov.au/restsdmx/sdmx.ashx/GetDataStructure/ABS_REGIONAL_ASGS/ABS");

        assertThat(queries.getDataQuery(endpoint, DataflowRef.parse("ABS_REGIONAL_ASGS"), Key.parse("BANKRUPT_2.AUS.0.A"), DataFilter.FULL))
                .describedAs("SdmxFix#1")
                .hasToString("http://stat.data.abs.gov.au/restsdmx/sdmx.ashx/GetData/ABS_REGIONAL_ASGS/BANKRUPT_2.AUS.0.A/ABS?format=compact_v2");
    }
}
