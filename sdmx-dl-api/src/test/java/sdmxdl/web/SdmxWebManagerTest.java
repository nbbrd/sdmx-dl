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

import org.junit.jupiter.api.Test;
import sdmxdl.SdmxConnection;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.web.spi.SdmxWebDriver.NATIVE_RANK;
import static sdmxdl.web.spi.SdmxWebDriver.WRAPPED_RANK;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThat(SdmxWebManager.ofServiceLoader()).satisfies(o -> {
            assertThat(o).isNotNull();
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().build()).satisfies(o -> {
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().driver(sampleDriver).build()).satisfies(o -> {
            assertThat(o.getDrivers()).containsExactly(sampleDriver);
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).containsAll(sampleDriver.getDefaultSources());
        });
    }

    @Test
    public void testGetSources() {
        SdmxWebSource nbb = SdmxWebSource.builder().name("nbb").alias("bnb").driver("sdmx21").uriOf("http://nbb").build();
        SdmxWebSource ecb = SdmxWebSource.builder().name("ecb").driver("sdmx21").uriOf("http://ecb").build();
        SdmxWebSource abs = SdmxWebSource.builder().name("abs").driver("sdmx21").uriOf("http://abs").build();

        SdmxWebSource nbbAlias = nbb.alias("bnb");
        SdmxWebSource nbbDialect = nbb.toBuilder().dialect("custom").clearAliases().build();

        SdmxWebDriver sdmx21 = MockedWebDriver
                .builder()
                .name("sdmx21")
                .rank(WRAPPED_RANK)
                .available(true)
                .source(nbb)
                .source(ecb)
                .build();

        assertThat(
                SdmxWebManager
                        .builder()
                        .build()
                        .getSources()
        )
                .describedAs("WebManager without driver nor custom-sources has no sources")
                .isEmpty();

        assertThat(
                SdmxWebManager
                        .builder()
                        .driver(sdmx21)
                        .build()
                        .getSources()
        )
                .describedAs("WebManager with driver but without custom-sources has only driver-sources sorted by name with order-based priority")
                .containsExactly(
                        entryOf("bnb", nbbAlias),
                        entryOf("ecb", ecb),
                        entryOf("nbb", nbb)
                );

        assertThat(
                SdmxWebManager
                        .builder()
                        .customSource(nbb)
                        .customSource(nbbDialect)
                        .customSource(abs)
                        .build()
                        .getSources()
        )
                .describedAs("WebManager without driver but with custom-sources has only custom-sources sorted by name with order-based priority")
                .containsExactly(
                        entryOf("abs", abs),
                        entryOf("bnb", nbbAlias),
                        entryOf("nbb", nbb)
                );

        assertThat(
                SdmxWebManager
                        .builder()
                        .driver(sdmx21)
                        .customSource(nbbDialect)
                        .customSource(abs)
                        .build()
                        .getSources()
        )
                .describedAs("WebManager with driver and custom-sources has both driver-sources and custom-sources sorted by name with order-based priority")
                .containsExactly(
                        entryOf("abs", abs),
                        entryOf("bnb", nbbAlias),
                        entryOf("ecb", ecb),
                        entryOf("nbb", nbbDialect)
                );
    }

    @Test
    public void testGetDefaultSources() {
        SdmxWebSource source1a = SdmxWebSource.builder().name("s1").driver("dX").uriOf("http://abc").build();
        SdmxWebSource source2 = SdmxWebSource.builder().name("s2").driver("dX").uriOf("http://abc").build();
        SdmxWebDriver driverX = MockedWebDriver.builder().name("dX").rank(WRAPPED_RANK).available(true).source(source1a).source(source2).build();

        SdmxWebSource source1b = SdmxWebSource.builder().name("s1").driver("dY").uriOf("http://xyz").build();
        SdmxWebSource source3 = SdmxWebSource.builder().name("s3").driver("dY").uriOf("http://xyz").build();
        SdmxWebDriver driverY = MockedWebDriver.builder().name("dY").rank(NATIVE_RANK).available(true).source(source1b).source(source3).build();

        assertThat(SdmxWebManager.builder().driver(driverX).driver(driverY).build().getDefaultSources())
                .containsExactly(source1a, source2, source3);

        assertThat(SdmxWebManager.builder().driver(driverY).driver(driverX).build().getDefaultSources())
                .containsExactly(source1b, source3, source2);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnection() throws IOException {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).dialect(sampleDialect).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((String) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection("ko"))
                .as("Invalid source name");

        assertThatCode(() -> manager.getConnection(sampleSource.getName()).close()).doesNotThrowAnyException();

        SdmxWebDriver driver1 = MockedWebDriver
                .builder()
                .name("d1")
                .rank(WRAPPED_RANK)
                .available(true)
                .repo(asURI("http://abc"), sample)
                .source(SdmxWebSource.builder().name("source").driver("d1").dialect("azerty").uriOf("http://abc").build())
                .build();

        SdmxWebDriver driver2 = MockedWebDriver
                .builder()
                .name("d2")
                .rank(NATIVE_RANK)
                .available(true)
                .repo(asURI("http://xyz"), sample)
                .source(SdmxWebSource.builder().name("source").driver("d2").dialect("azerty").uriOf("http://xyz").build())
                .build();

        try (SdmxWebConnection c = SdmxWebManager.builder().driver(driver2).driver(driver1).dialect(sampleDialect).build().getConnection("source")) {
            assertThat(c.getDriver()).isEqualTo(driver2.getName());
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).dialect(sampleDialect).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((SdmxWebSource) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().uriOf("http://ko").build()))
                .as("Invalid source endpoint");

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().driver("ko").build()))
                .as("Invalid source driver");

        assertThatCode(() -> manager.getConnection(sampleSource).close()).doesNotThrowAnyException();
        assertThatCode(() -> manager.getConnection(sampleSource.toBuilder().name("other").build()).close()).doesNotThrowAnyException();
    }

    @SuppressWarnings("EmptyTryBlock")
    @Test
    public void testInvalidSourceProperties() throws IOException {
        List<String> events = new ArrayList<>();

        SdmxWebManager manager = SdmxWebManager
                .builder()
                .driver(sampleDriver)
                .dialect(sampleDialect)
                .eventListener(SdmxWebListener.of((source, event) -> events.add(source.getName() + ":" + event)))
                .build();

        SdmxWebSource noProp = sampleSource.toBuilder().name("noProp").clearProperties().build();
        try (SdmxWebConnection ignored = manager.getConnection(noProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource validProp = sampleSource.toBuilder().name("validProp").build();
        try (SdmxWebConnection ignored = manager.getConnection(validProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource invalidProp = sampleSource.toBuilder().name("invalidProp").property("boom", "123").build();
        try (SdmxWebConnection ignored = manager.getConnection(invalidProp)) {
        }
        assertThat(events).hasSize(1).element(0, STRING)
                .contains(invalidProp.getName())
                .contains("boom");
    }

    private final SdmxRepository sample = SdmxRepository.builder().name("repo").build();
    private final SdmxWebSource sampleSource = SdmxWebSource
            .builder()
            .name("repoSource")
            .driver("repoDriver")
            .dialect("azerty")
            .uri(asURI(sample))
            .property("someproperty", "somevalue")
            .build();
    private final SdmxWebDriver sampleDriver = MockedWebDriver
            .builder()
            .name("repoDriver")
            .rank(0)
            .available(true)
            .repo(asURI(sample), sample)
            .supportedProperty("someproperty")
            .source(sampleSource)
            .build();
    private final SdmxDialect sampleDialect = new MockedDialect("azerty");

    private static URI asURI(SdmxRepository o) {
        return URI.create("http://" + o.getName());
    }

    private static URI asURI(String o) {
        return URI.create(o);
    }

    @lombok.RequiredArgsConstructor
    @lombok.Builder(toBuilder = true)
    private static final class MockedWebDriver implements SdmxWebDriver {

        @lombok.Getter
        private final String name;

        @lombok.Getter
        private final int rank;

        @lombok.Getter
        private final boolean available;

        @lombok.Singular
        private final Map<URI, SdmxRepository> repos;

        @lombok.Singular
        private final List<SdmxWebSource> sources;

        @lombok.Singular
        private final List<String> supportedProperties;

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
            return connect(source.getUri());
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            return sources;
        }

        @Override
        public Collection<String> getSupportedProperties() {
            return supportedProperties;
        }

        private SdmxWebConnection connect(URI uri) throws IOException {
            SdmxRepository result = repos.get(uri);
            if (result != null) {
                return new MockedWebConnection(name, result.asConnection());
            }
            throw new IOException(uri.toString());
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class MockedWebConnection implements SdmxWebConnection {

        @lombok.Getter
        private final String driver;

        @lombok.experimental.Delegate
        private final SdmxConnection delegate;

        @Override
        public Duration ping() {
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

    private static <K, V> AbstractMap.SimpleEntry<K, V> entryOf(K name, V source) {
        return new AbstractMap.SimpleEntry<>(name, source);
    }
}
