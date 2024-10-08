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
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.WebCaching;
import tests.sdmxdl.api.SdmxManagerAssert;
import tests.sdmxdl.web.spi.MockedDriver;
import tests.sdmxdl.web.spi.MockedRegistry;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;
import static sdmxdl.web.spi.Driver.NATIVE_DRIVER_RANK;
import static sdmxdl.web.spi.Driver.WRAPPED_DRIVER_RANK;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.builder().driver(sampleDriver).build(),
                SdmxManagerAssert.Sample
                        .<WebSource>builder()
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
            assertThat(o.getMonitors()).isEmpty();
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
            assertThat(o.getMonitors()).isEmpty();
            assertThat(o.getNetworking()).isEqualTo(Networking.getDefault());
            assertThat(o.getCaching()).isEqualTo(WebCaching.noOp());
            assertThat(o.getOnEvent()).isNull();
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).isEmpty();
        });

        assertThat(SdmxWebManager.builder().driver(sampleDriver).build()).satisfies(o -> {
            assertThat(o.getDrivers()).containsExactly(sampleDriver);
            assertThat(o.getMonitors()).isEmpty();
            assertThat(o.getNetworking()).isEqualTo(Networking.getDefault());
            assertThat(o.getCaching()).isEqualTo(WebCaching.noOp());
            assertThat(o.getOnEvent()).isNull();
            assertThat(o.getAuthenticators()).isEmpty();
            assertThat(o.getCustomSources()).isEmpty();
            assertThat(o.getDefaultSources()).containsAll(sampleDriver.getDefaultSources());
            assertThat(o.getSources()).containsValues(sampleDriver.getDefaultSources().toArray(new WebSource[0]));
        });
    }

    @Test
    public void testGetSources() {
        WebSource nbb = WebSource.builder().id("nbb").alias("bnb").driver("sdmx21").endpointOf("http://nbb").build();
        WebSource ecb = WebSource.builder().id("ecb").driver("sdmx21").endpointOf("http://ecb").build();
        WebSource abs = WebSource.builder().id("abs").driver("sdmx21").endpointOf("http://abs").build();

        WebSource nbbAlias = nbb.alias("bnb");

        Driver sdmx21 = MockedDriver
                .builder()
                .id("sdmx21")
                .rank(WRAPPED_DRIVER_RANK)
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
                        .registry(MockedRegistry.builder().source(nbb).source(abs).build())
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
                        .registry(MockedRegistry.builder().source(abs).build())
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
        WebSource source1a = WebSource.builder().id("s1").driver("dX").endpointOf("http://abc").build();
        WebSource source2 = WebSource.builder().id("s2").driver("dX").endpointOf("http://abc").build();
        Driver driverX = MockedDriver.builder().id("dX").rank(WRAPPED_DRIVER_RANK).available(true).customSource(source1a).customSource(source2).build();

        WebSource source1b = WebSource.builder().id("s1").driver("dY").endpointOf("http://xyz").build();
        WebSource source3 = WebSource.builder().id("s3").driver("dY").endpointOf("http://xyz").build();
        Driver driverY = MockedDriver.builder().id("dY").rank(NATIVE_DRIVER_RANK).available(true).customSource(source1b).customSource(source3).build();

        assertThat(SdmxWebManager.builder().driver(driverX).driver(driverY).build().getDefaultSources())
                .containsExactly(source1a, source2, source3);

        assertThat(SdmxWebManager.builder().driver(driverY).driver(driverX).build().getDefaultSources())
                .containsExactly(source1b, source3, source2);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnection() throws IOException {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((String) null, ANY));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection("ko", ANY))
                .as("Invalid source name");

        assertThatCode(() -> manager.getConnection(sampleSource.getId(), ANY).close()).doesNotThrowAnyException();

        Driver driver1 = MockedDriver
                .builder()
                .id("d1")
                .rank(WRAPPED_DRIVER_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(WebSource.builder().id("source").driver("d1").endpointOf(sample.getName()).build())
                .build();

        Driver driver2 = MockedDriver
                .builder()
                .id("d2")
                .rank(NATIVE_DRIVER_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(WebSource.builder().id("source").driver("d2").endpointOf(sample.getName()).build())
                .build();

        try (Connection c = SdmxWebManager.builder().driver(driver2).driver(driver1).build().getConnection("source", ANY)) {
            // TODO: create code that verifies that driver2 is selected
//            assertThat(c.getDriver()).isEqualTo(driver2.getName());
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.builder().driver(sampleDriver).build();

        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((WebSource) null, ANY));

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().endpointOf("http://ko").build(), ANY))
                .as("Invalid source endpoint");

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(sampleSource.toBuilder().driver("ko").build(), ANY))
                .as("Invalid source driver");

        assertThatCode(() -> manager.getConnection(sampleSource, ANY).close()).doesNotThrowAnyException();
        assertThatCode(() -> manager.getConnection(sampleSource.toBuilder().id("other").build(), ANY).close()).doesNotThrowAnyException();
    }

    private final DataRepository sample = DataRepository.builder().name("repo").build();
    private final WebSource sampleSource = WebSource
            .builder()
            .id("repoSource")
            .driver("repoDriver")
            .endpointOf(sample.getName())
            .build();
    private final Driver sampleDriver = MockedDriver
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
