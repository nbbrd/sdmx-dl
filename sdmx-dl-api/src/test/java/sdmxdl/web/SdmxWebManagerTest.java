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
import sdmxdl.Connection;
import sdmxdl.DataRepository;
import sdmxdl.Feature;
import sdmxdl.LanguagePriorityList;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebDriver;
import tests.sdmxdl.api.SdmxManagerAssert;
import tests.sdmxdl.web.MockedDriver;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.web.spi.WebDriver.NATIVE_RANK;
import static sdmxdl.web.spi.WebDriver.WRAPPED_RANK;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.builder().driver(sampleDriver).build(),
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
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetworking()).isEqualTo(Networking.getDefault());
            assertThat(o.getCaching()).isEqualTo(WebCaching.noOp());
            assertThat(o.getOnEvent()).isNull();
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
            assertThat(o.getSources()).isEmpty();
        });

        assertThat(SdmxWebManager.noOp()).satisfies(o -> {
            assertThat(o.getDrivers()).isEmpty();
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetworking()).isEqualTo(Networking.getDefault());
            assertThat(o.getCaching()).isEqualTo(WebCaching.noOp());
            assertThat(o.getOnEvent()).isNull();
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().driver(sampleDriver).build()).satisfies(o -> {
            assertThat(o.getDrivers()).containsExactly(sampleDriver);
            assertThat(o.getMonitorings()).isEmpty();
            assertThat(o.getLanguages()).isEqualTo(LanguagePriorityList.ANY);
            assertThat(o.getNetworking()).isEqualTo(Networking.getDefault());
            assertThat(o.getCaching()).isEqualTo(WebCaching.noOp());
            assertThat(o.getOnEvent()).isNull();
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).containsAll(sampleDriver.getDefaultSources());
            assertThat(o.getSources()).containsValues(sampleDriver.getDefaultSources().toArray(new SdmxWebSource[0]));
        });
    }

    @Test
    public void testGetSources() {
        SdmxWebSource nbb = SdmxWebSource.builder().id("nbb").alias("bnb").driver("sdmx21").endpointOf("http://nbb").build();
        SdmxWebSource ecb = SdmxWebSource.builder().id("ecb").driver("sdmx21").endpointOf("http://ecb").build();
        SdmxWebSource abs = SdmxWebSource.builder().id("abs").driver("sdmx21").endpointOf("http://abs").build();

        SdmxWebSource nbbAlias = nbb.alias("bnb");

        WebDriver sdmx21 = MockedDriver
                .builder()
                .id("sdmx21")
                .rank(WRAPPED_RANK)
                .available(true)
                .customSource(nbb)
                .customSource(ecb)
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
                        .customSource(abs)
                        .build()
                        .getSources()
        )
                .describedAs("WebManager with driver and custom-sources has both driver-sources and custom-sources sorted by name with order-based priority")
                .containsExactly(
                        entryOf("abs", abs),
                        entryOf("bnb", nbbAlias),
                        entryOf("ecb", ecb),
                        entryOf("nbb", nbb)
                );
    }

    @Test
    public void testGetDefaultSources() {
        SdmxWebSource source1a = SdmxWebSource.builder().id("s1").driver("dX").endpointOf("http://abc").build();
        SdmxWebSource source2 = SdmxWebSource.builder().id("s2").driver("dX").endpointOf("http://abc").build();
        WebDriver driverX = MockedDriver.builder().id("dX").rank(WRAPPED_RANK).available(true).customSource(source1a).customSource(source2).build();

        SdmxWebSource source1b = SdmxWebSource.builder().id("s1").driver("dY").endpointOf("http://xyz").build();
        SdmxWebSource source3 = SdmxWebSource.builder().id("s3").driver("dY").endpointOf("http://xyz").build();
        WebDriver driverY = MockedDriver.builder().id("dY").rank(NATIVE_RANK).available(true).customSource(source1b).customSource(source3).build();

        assertThat(SdmxWebManager.builder().driver(driverX).driver(driverY).build().getDefaultSources())
                .containsExactly(source1a, source2, source3);

        assertThat(SdmxWebManager.builder().driver(driverY).driver(driverX).build().getDefaultSources())
                .containsExactly(source1b, source3, source2);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnection() throws IOException {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((String) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection("ko"))
                .as("Invalid source name");

        assertThatCode(() -> manager.getConnection(sampleSource.getId()).close()).doesNotThrowAnyException();

        WebDriver driver1 = MockedDriver
                .builder()
                .id("d1")
                .rank(WRAPPED_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(SdmxWebSource.builder().id("source").driver("d1").endpointOf(sample.getName()).build())
                .build();

        WebDriver driver2 = MockedDriver
                .builder()
                .id("d2")
                .rank(NATIVE_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(SdmxWebSource.builder().id("source").driver("d2").endpointOf(sample.getName()).build())
                .build();

        try (Connection c = SdmxWebManager.builder().driver(driver2).driver(driver1).build().getConnection("source")) {
            // TODO: create code that verifies that driver2 is selected
//            assertThat(c.getDriver()).isEqualTo(driver2.getName());
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((SdmxWebSource) null));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().endpointOf("http://ko").build()))
                .as("Invalid source endpoint");

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().driver("ko").build()))
                .as("Invalid source driver");

        assertThatCode(() -> manager.getConnection(sampleSource).close()).doesNotThrowAnyException();
        assertThatCode(() -> manager.getConnection(sampleSource.toBuilder().id("other").build()).close()).doesNotThrowAnyException();
    }

    @SuppressWarnings("EmptyTryBlock")
    @Test
    public void testInvalidSourceProperties() throws IOException {
        List<String> events = new ArrayList<>();

        SdmxWebManager manager = SdmxWebManager
                .builder()
                .driver(sampleDriver)
                .onEvent((source, marker, event) -> events.add(source.getId() + ":" + event))
                .build();

        SdmxWebSource noProp = sampleSource.toBuilder().id("noProp").clearProperties().build();
        try (Connection ignored = manager.getConnection(noProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource validProp = sampleSource.toBuilder().id("validProp").build();
        try (Connection ignored = manager.getConnection(validProp)) {
        }
        assertThat(events).isEmpty();

        SdmxWebSource invalidProp = sampleSource.toBuilder().id("invalidProp").property("boom", "123").build();
        try (Connection ignored = manager.getConnection(invalidProp)) {
        }
        assertThat(events).singleElement(as(STRING))
                .contains(invalidProp.getId())
                .contains("boom");
    }

    private final DataRepository sample = DataRepository.builder().name("repo").build();
    private final SdmxWebSource sampleSource = SdmxWebSource
            .builder()
            .id("repoSource")
            .driver("repoDriver")
            .endpointOf(sample.getName())
            .build();
    private final WebDriver sampleDriver = MockedDriver
            .builder()
            .id("repoDriver")
            .rank(0)
            .available(true)
            .repo(sample, EnumSet.allOf(Feature.class))
            .customSource(sampleSource)
            .build();

    private static <K, V> AbstractMap.SimpleEntry<K, V> entryOf(K name, V source) {
        return new AbstractMap.SimpleEntry<>(name, source);
    }
}
