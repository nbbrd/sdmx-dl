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
package _test.sdmxdl.util;

import sdmxdl.ext.spi.Dialect;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DialectAssertions {

    @SuppressWarnings("null")
    public void assertDialectCompliance(Dialect d) {
        assertThat(d.getName()).isNotBlank();
        assertThat(d.getDescription()).isNotBlank();
        assertThat(d.getObsFactory()).isNotNull();

        assertThat(d.getClass()).isFinal();
    }
}
