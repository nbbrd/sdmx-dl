/*
 * Copyright 2019 National Bank of Belgium
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
package _test.sdmxdl;

import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Philippe Charles
 */
public enum TestDriver implements SdmxWebDriver {
    VALID {
        @Override
        public String getName() {
            return "valid";
        }

        @Override
        public int getRank() {
            return NATIVE_RANK;
        }

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException, IllegalArgumentException {
            return TestConnection.VALID;
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            return Collections.singletonList(SOURCE);
        }

        @Override
        public Collection<String> getSupportedProperties() {
            return Collections.singletonList("hello");
        }
    }, FAILING {
        @Override
        public String getName() {
            throw new CustomException();
        }

        @Override
        public int getRank() {
            throw new CustomException();
        }

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException, IllegalArgumentException {
            throw new CustomException();
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            throw new CustomException();
        }

        @Override
        public Collection<String> getSupportedProperties() {
            throw new CustomException();
        }
    }, NULL {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public int getRank() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException, IllegalArgumentException {
            return null;
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            return null;
        }

        @Override
        public Collection<String> getSupportedProperties() {
            return null;
        }
    };

    public static final SdmxWebSource SOURCE = SdmxWebSource.builder().name("123").driver("456").endpointOf("http://localhost").build();
}
