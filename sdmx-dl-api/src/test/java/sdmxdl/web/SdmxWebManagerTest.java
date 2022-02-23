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
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxConnection;
import sdmxdl.SdmxManager;
import sdmxdl.ext.NetworkFactory;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.spi.SdmxWebDriver;
import tests.sdmxdl.api.SdmxManagerAssert;
import tests.sdmxdl.ext.MockedDialect;
import tests.sdmxdl.web.MockedWebDriver;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.web.spi.SdmxWebDriver.NATIVE_RANK;
import static sdmxdl.web.spi.SdmxWebDriver.WRAPPED_RANK;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.builder().driver(sampleDriver).dialect(sampleDialect).build(),
                SdmxManagerAssert.Sample
                        .<SdmxWebSource>builder()
                        .validSource(sampleSource)
                        .invalidSource(sampleSource.toBuilder().driver("other").build())
                        .build()
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThat(SdmxWebManager.ofServiceLoader()).satisfies(o -> {
            assertThat(o).isNotNull();
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetwork()).isEqualTo(NetworkFactory.getDefault());
            assertThat(o.getCache()).isEqualTo(SdmxCache.noOp());
            assertThat(o.getEventListener()).isEqualTo(SdmxManager.NO_OP_EVENT_LISTENER);
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
            assertThat(o.getSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().build()).satisfies(o -> {
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetwork()).isEqualTo(NetworkFactory.getDefault());
            assertThat(o.getCache()).isEqualTo(SdmxCache.noOp());
            assertThat(o.getEventListener()).isEqualTo(SdmxManager.NO_OP_EVENT_LISTENER);
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().driver(sampleDriver).build()).satisfies(o -> {
            assertThat(o.getDrivers()).containsExactly(sampleDriver);
            assertThat(o.getDialects()).isEmpty();
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetwork()).isEqualTo(NetworkFactory.getDefault());
            assertThat(o.getCache()).isEqualTo(SdmxCache.noOp());
            assertThat(o.getEventListener()).isEqualTo(SdmxManager.NO_OP_EVENT_LISTENER);
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).containsAll(sampleDriver.getDefaultSources());
            assertThat(o.getSources()).containsValues(sampleDriver.getDefaultSources().toArray(new SdmxWebSource[0]));
        });
    }

    @Test
    public void testGetSources() {
        SdmxWebSource nbb = SdmxWebSource.builder().name("nbb").alias("bnb").driver("sdmx21").endpointOf("http://nbb").build();
        SdmxWebSource ecb = SdmxWebSource.builder().name("ecb").driver("sdmx21").endpointOf("http://ecb").build();
        SdmxWebSource abs = SdmxWebSource.builder().name("abs").driver("sdmx21").endpointOf("http://abs").build();

        SdmxWebSource nbbAlias = nbb.alias("bnb");
        SdmxWebSource nbbDialect = nbb.toBuilder().dialect("custom").clearAliases().build();

        SdmxWebDriver sdmx21 = MockedWebDriver
                .builder()
                .name("sdmx21")
                .rank(WRAPPED_RANK)
                .available(true)
                .defaultSource(nbb)
                .defaultSource(ecb)
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
        SdmxWebSource source1a = SdmxWebSource.builder().name("s1").driver("dX").endpointOf("http://abc").build();
        SdmxWebSource source2 = SdmxWebSource.builder().name("s2").driver("dX").endpointOf("http://abc").build();
        SdmxWebDriver driverX = MockedWebDriver.builder().name("dX").rank(WRAPPED_RANK).available(true).defaultSource(source1a).defaultSource(source2).build();

        SdmxWebSource source1b = SdmxWebSource.builder().name("s1").driver("dY").endpointOf("http://xyz").build();
        SdmxWebSource source3 = SdmxWebSource.builder().name("s3").driver("dY").endpointOf("http://xyz").build();
        SdmxWebDriver driverY = MockedWebDriver.builder().name("dY").rank(NATIVE_RANK).available(true).defaultSource(source1b).defaultSource(source3).build();

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
                .repo(sample)
                .defaultSource(SdmxWebSource.builder().name("source").driver("d1").dialect("azerty").endpointOf(sample.getName()).build())
                .build();

        SdmxWebDriver driver2 = MockedWebDriver
                .builder()
                .name("d2")
                .rank(NATIVE_RANK)
                .available(true)
                .repo(sample)
                .defaultSource(SdmxWebSource.builder().name("source").driver("d2").dialect("azerty").endpointOf(sample.getName()).build())
                .build();

        try (SdmxConnection c = SdmxWebManager.builder().driver(driver2).driver(driver1).dialect(sampleDialect).build().getConnection("source")) {
            // TODO: create code that verifies that driver2 is selected
//            assertThat(c.getDriver()).isEqualTo(driver2.getName());
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).dialect(sampleDialect).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((SdmxWebSource) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().endpointOf("http://ko").build()))
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
                .eventListener((source, event) -> events.add(source.getName() + ":" + event))
                .build();

        SdmxWebSource noProp = sampleSource.toBuilder().name("noProp").clearProperties().build();
        try (SdmxConnection ignored = manager.getConnection(noProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource validProp = sampleSource.toBuilder().name("validProp").build();
        try (SdmxConnection ignored = manager.getConnection(validProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource invalidProp = sampleSource.toBuilder().name("invalidProp").property("boom", "123").build();
        try (SdmxConnection ignored = manager.getConnection(invalidProp)) {
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
            .endpointOf(sample.getName())
            .property("someproperty", "somevalue")
            .build();
    private final SdmxWebDriver sampleDriver = MockedWebDriver
            .builder()
            .name("repoDriver")
            .rank(0)
            .available(true)
            .repo(sample)
            .supportedProperty("someproperty")
            .defaultSource(sampleSource)
            .build();
    private final SdmxDialect sampleDialect = new MockedDialect("azerty");

    private static <K, V> AbstractMap.SimpleEntry<K, V> entryOf(K name, V source) {
        return new AbstractMap.SimpleEntry<>(name, source);
    }
}
