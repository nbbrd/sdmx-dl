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
package internal.sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.WebDriverAssert;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class FileDriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new FileDriver());
    }

    @Test
    public void testToFile() throws IOException {
        assertThat(FileDriver.toFile(URI.create("file:/C:/temp/x.xml"))).isNotNull();

        URI illegal = URI.create("file://C:/temp/x.xml");
        assertThatIOException()
                .isThrownBy(() -> FileDriver.toFile(illegal))
                .withMessageStartingWith("Invalid file name: ")
                .withMessageContaining(illegal.toString())
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
    }
}
