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
package sdmxdl.web;

import org.junit.Test;
import sdmxdl.SdmxConnection;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThat(SdmxWebManager.builder().build()).satisfies(o -> {
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().driver(repoDriver).build()).satisfies(o -> {
            assertThat(o.getDrivers()).containsExactly(repoDriver);
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).containsAll(repoDriver.getDefaultSources());
        });
    }

    @Test
    public void testGetDriverNames() {
        SdmxWebDriver driver1 = MockedWebDriver.builder().name("d1").rank(SdmxWebDriver.WRAPPED_RANK).build();
        SdmxWebDriver driver2 = MockedWebDriver.builder().name("d2").rank(SdmxWebDriver.NATIVE_RANK).build();

        assertThat(SdmxWebManager.builder().driver(driver2).driver(driver1).build().getDriverNames())
                .containsExactlyElementsOf(SdmxWebManager.builder().driver(driver2).driver(driver1).build().getDriverNames())
                .containsExactly(driver2.getName(), driver1.getName());
    }

    @Test
    public void testGetDefaultSources() {
        SdmxWebSource source1 = SdmxWebSource.builder().name("source").driver("d1").dialect("SDMX21").endpointOf("http://abc").build();
        SdmxWebDriver driver1 = MockedWebDriver.builder().name("d1").rank(SdmxWebDriver.WRAPPED_RANK).source(source1).build();

        SdmxWebSource source2 = SdmxWebSource.builder().name("source").driver("d2").dialect("SDMX21").endpointOf("http://xyz").build();
        SdmxWebDriver driver2 = MockedWebDriver.builder().name("d2").rank(SdmxWebDriver.NATIVE_RANK).source(source2).build();

        assertThat(SdmxWebManager.builder().driver(driver2).driver(driver1).build().getDefaultSources())
                .containsExactlyElementsOf(SdmxWebManager.builder().driver(driver2).driver(driver1).build().getDefaultSources())
                .containsExactly(source2, source1);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnection() throws IOException {
        SdmxWebManager manager = SdmxWebManager.builder().driver(repoDriver).dialect(repoDialect).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((String) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection("ko"))
                .as("Invalid source name");

        assertThatCode(() -> manager.getConnection(repoSource.getName()).close()).doesNotThrowAnyException();

        SdmxWebDriver driver1 = MockedWebDriver
                .builder()
                .name("d1")
                .rank(SdmxWebDriver.WRAPPED_RANK)
                .repo(asURL("http://abc"), repo)
                .source(SdmxWebSource.builder().name("source").driver("d1").dialect("azerty").endpointOf("http://abc").build())
                .build();

        SdmxWebDriver driver2 = MockedWebDriver
                .builder()
                .name("d2")
                .rank(SdmxWebDriver.NATIVE_RANK)
                .repo(asURL("http://xyz"), repo)
                .source(SdmxWebSource.builder().name("source").driver("d2").dialect("azerty").endpointOf("http://xyz").build())
                .build();

        try (SdmxWebConnection c = SdmxWebManager.builder().driver(driver2).driver(driver1).dialect(repoDialect).build().getConnection("source")) {
            assertThat(c.getDriver()).isEqualTo(driver2.getName());
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.builder().driver(repoDriver).dialect(repoDialect).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((SdmxWebSource) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(repoSource.toBuilder().endpointOf("http://ko").build()))
                .as("Invalid source endpoint");

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(repoSource.toBuilder().driver("ko").build()))
                .as("Invalid source driver");

        assertThatCode(() -> manager.getConnection(repoSource).close()).doesNotThrowAnyException();
        assertThatCode(() -> manager.getConnection(repoSource.toBuilder().name("other").build()).close()).doesNotThrowAnyException();
    }

    private final SdmxRepository repo = SdmxRepository.builder().name("repo").build();
    private final SdmxWebSource repoSource = SdmxWebSource.builder().name("repoSource").driver("repoDriver").dialect("azerty").endpoint(asURL(repo)).build();
    private final SdmxWebDriver repoDriver = MockedWebDriver
            .builder()
            .name("repoDriver")
            .rank(0)
            .repo(asURL(repo), repo)
            .source(repoSource)
            .build();
    private final SdmxDialect repoDialect = new MockedDialect("azerty");

    private static URL asURL(SdmxRepository o) {
        try {
            return new URL("http://" + o.getName());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static URL asURL(String o) {
        try {
            return new URL(o);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @lombok.RequiredArgsConstructor
    @lombok.Builder
    private static final class MockedWebDriver implements SdmxWebDriver {

        @lombok.Getter
        private final String name;

        @lombok.Getter
        private final int rank;

        @lombok.Singular
        private final Map<URL, SdmxRepository> repos;

        @lombok.Singular
        private final List<SdmxWebSource> sources;

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
            return connect(source.getEndpoint());
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            return sources;
        }

        @Override
        public Collection<String> getSupportedProperties() {
            return Collections.emptyList();
        }

        private SdmxWebConnection connect(URL endpoint) throws IOException {
            SdmxRepository result = repos.get(endpoint);
            if (result != null) {
                return new MockedWebConnection(name, result.asConnection());
            }
            throw new IOException(endpoint.toString());
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class MockedWebConnection implements SdmxWebConnection {

        @lombok.Getter
        private final String driver;

        @lombok.experimental.Delegate
        private final SdmxConnection delegate;

        @Override
        public Duration ping() throws IOException {
            return Duration.ZERO;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class MockedDialect implements SdmxDialect {

        @lombok.Getter
        private final String name;

        @Override
        public String getDescription() {
            return getName();
        }

        @Override
        public ObsFactory getObsFactory() {
            return dsd -> null;
        }
    }
}
