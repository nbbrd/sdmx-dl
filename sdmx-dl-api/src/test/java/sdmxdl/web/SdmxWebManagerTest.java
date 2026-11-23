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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static sdmxdl.Languages.ANY;
import static sdmxdl.web.spi.Driver.NATIVE_DRIVER_RANK;
import static sdmxdl.web.spi.Driver.WRAPPED_DRIVER_RANK;

import java.io.IOException;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.Confidentiality;
import sdmxdl.Connection;
import sdmxdl.DataRepository;
import sdmxdl.Database;
import sdmxdl.DatabaseRef;
import sdmxdl.Feature;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.StructureRef;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.api.SdmxManagerAssert;
import tests.sdmxdl.web.IconSamples;
import tests.sdmxdl.web.spi.MockedDriver;
import tests.sdmxdl.web.spi.MockedIconProvider;
import tests.sdmxdl.web.spi.MockedRegistry;

/**
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.builder().driver(sampleDriver).build(),
                SdmxManagerAssert.Sample.<WebSource>builder()
                        .validSource(sampleSource)
                        .invalidSource(sampleSource.toBuilder().driver("other").build())
                        .build());
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
            assertThat(o.getIconProviders()).isEmpty();
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
            assertThat(o.getSources())
                    .containsValues(sampleDriver.getDefaultSources().toArray(new WebSource[0]));
        });
    }

    @Test
    public void testGetSources() {
        WebSource nbb = WebSource.builder()
                .id("nbb")
                .alias("bnb")
                .driver("sdmx21")
                .endpointOf("http://nbb")
                .build();
        WebSource ecb = WebSource.builder()
                .id("ecb")
                .driver("sdmx21")
                .endpointOf("http://ecb")
                .build();
        WebSource abs = WebSource.builder()
                .id("abs")
                .driver("sdmx21")
                .endpointOf("http://abs")
                .build();

        WebSource nbbAlias = nbb.alias("bnb");

        Driver sdmx21 = MockedDriver.builder()
                .id("sdmx21")
                .rank(WRAPPED_DRIVER_RANK)
                .available(true)
                .customSource(nbb)
                .customSource(ecb)
                .build();

        assertThat(SdmxWebManager.builder().build().getSources())
                .describedAs("WebManager without driver nor custom-sources has no sources")
                .isEmpty();

        assertThat(SdmxWebManager.builder().driver(sdmx21).build().getSources())
                .describedAs(
                        "WebManager with driver but without custom-sources has only driver-sources sorted by name with order-based priority")
                .containsExactly(entryOf("bnb", nbbAlias), entryOf("ecb", ecb), entryOf("nbb", nbb));

        assertThat(SdmxWebManager.builder()
                        .registry(
                                MockedRegistry.builder().source(nbb).source(abs).build())
                        .build()
                        .getSources())
                .describedAs(
                        "WebManager without driver but with custom-sources has only custom-sources sorted by name with order-based priority")
                .containsExactly(entryOf("abs", abs), entryOf("bnb", nbbAlias), entryOf("nbb", nbb));

        assertThat(SdmxWebManager.builder()
                        .driver(sdmx21)
                        .registry(MockedRegistry.builder().source(abs).build())
                        .build()
                        .getSources())
                .describedAs(
                        "WebManager with driver and custom-sources has both driver-sources and custom-sources sorted by name with order-based priority")
                .containsExactly(
                        entryOf("abs", abs), entryOf("bnb", nbbAlias), entryOf("ecb", ecb), entryOf("nbb", nbb));
    }

    @Test
    public void testListSources() {
        WebSource nbb = WebSource.builder()
                .id("nbb")
                .alias("bnb")
                .driver("sdmx21")
                .endpointOf("http://nbb")
                .confidentiality(Confidentiality.PUBLIC)
                .build();
        WebSource ecb = WebSource.builder()
                .id("ecb")
                .driver("sdmx21")
                .endpointOf("http://ecb")
                .confidentiality(Confidentiality.PUBLIC)
                .build();
        WebSource abs = WebSource.builder()
                .id("abs")
                .driver("sdmx21")
                .endpointOf("http://abs")
                .confidentiality(Confidentiality.RESTRICTED)
                .build();

        Driver sdmx21 = MockedDriver.builder()
                .id("sdmx21")
                .rank(WRAPPED_DRIVER_RANK)
                .available(true)
                .customSource(nbb)
                .customSource(ecb)
                .customSource(abs)
                .build();

        SdmxWebManager manager = SdmxWebManager.builder().driver(sdmx21).build();

        assertThatNullPointerException().isThrownBy(() -> manager.listSources(null));

        assertThat(manager.listSources(WebSourcesRequest.DEFAULT).getItems())
                .describedAs("no query nor threshold restriction sorts all non-alias sources by id")
                .extracting(WebSource::getId)
                .containsExactly("abs", "ecb", "nbb");

        assertThat(manager.listSources(WebSourcesRequest.builder().maxResults(2).build())
                        .getItems())
                .describedAs("no query respects maxResults")
                .extracting(WebSource::getId)
                .containsExactly("abs", "ecb");

        assertThat(manager.listSources(WebSourcesRequest.builder()
                                .confidentialityThreshold(Confidentiality.PUBLIC)
                                .build())
                        .getItems())
                .describedAs("threshold filters out sources with a higher confidentiality")
                .extracting(WebSource::getId)
                .containsExactly("ecb", "nbb");

        assertThat(manager.listSources(WebSourcesRequest.builder().query("ecb").build())
                        .getItems())
                .describedAs("query ranks by relevance instead of sorting by id")
                .extracting(WebSource::getId)
                .containsExactly("ecb");

        assertThat(manager.listSources(
                        WebSourcesRequest.builder().maxResults(1).offset(1).build()))
                .describedAs("offset skips the first matches and total counts all matches")
                .satisfies(result -> {
                    assertThat(result.getItems()).extracting(WebSource::getId).containsExactly("ecb");
                    assertThat(result.getTotal()).isEqualTo(3);
                });

        assertThatIllegalArgumentException()
                .isThrownBy(() -> manager.listSources(
                        WebSourcesRequest.builder().offset(-1).build()));

        List<WebSource> allSources =
                manager.listSources(WebSourcesRequest.DEFAULT).getItems();
        assertThat(allSources)
                .describedAs("aliases are never returned as standalone entries")
                .extracting(WebSource::getId)
                .doesNotContain("bnb");
    }

    @Test
    public void testGetDefaultSources() {
        WebSource source1a = WebSource.builder()
                .id("s1")
                .driver("dX")
                .endpointOf("http://abc")
                .build();
        WebSource source2 = WebSource.builder()
                .id("s2")
                .driver("dX")
                .endpointOf("http://abc")
                .build();
        Driver driverX = MockedDriver.builder()
                .id("dX")
                .rank(WRAPPED_DRIVER_RANK)
                .available(true)
                .customSource(source1a)
                .customSource(source2)
                .build();

        WebSource source1b = WebSource.builder()
                .id("s1")
                .driver("dY")
                .endpointOf("http://xyz")
                .build();
        WebSource source3 = WebSource.builder()
                .id("s3")
                .driver("dY")
                .endpointOf("http://xyz")
                .build();
        Driver driverY = MockedDriver.builder()
                .id("dY")
                .rank(NATIVE_DRIVER_RANK)
                .available(true)
                .customSource(source1b)
                .customSource(source3)
                .build();

        assertThat(SdmxWebManager.builder()
                        .driver(driverX)
                        .driver(driverY)
                        .build()
                        .getDefaultSources())
                .containsExactly(source1a, source2, source3);

        assertThat(SdmxWebManager.builder()
                        .driver(driverY)
                        .driver(driverX)
                        .build()
                        .getDefaultSources())
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

        assertThatCode(() -> manager.getConnection(sampleSource.getId(), ANY).close())
                .doesNotThrowAnyException();

        Driver driver1 = MockedDriver.builder()
                .id("d1")
                .rank(WRAPPED_DRIVER_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(WebSource.builder()
                        .id("source")
                        .driver("d1")
                        .endpointOf(sample.getName())
                        .build())
                .build();

        Driver driver2 = MockedDriver.builder()
                .id("d2")
                .rank(NATIVE_DRIVER_RANK)
                .available(true)
                .repo(sample, EnumSet.allOf(Feature.class))
                .customSource(WebSource.builder()
                        .id("source")
                        .driver("d2")
                        .endpointOf(sample.getName())
                        .build())
                .build();

        try (Connection c =
                SdmxWebManager.builder().driver(driver2).driver(driver1).build().getConnection("source", ANY)) {
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
                .isThrownBy(() -> manager.getConnection(
                        sampleSource.toBuilder().endpointOf("http://ko").build(), ANY))
                .as("Invalid source endpoint");

        assertThatIOException()
                .isThrownBy(() -> manager.getConnection(
                        sampleSource.toBuilder().driver("ko").build(), ANY))
                .as("Invalid source driver");

        assertThatCode(() -> manager.getConnection(sampleSource, ANY).close()).doesNotThrowAnyException();
        assertThatCode(() -> manager.getConnection(
                                sampleSource.toBuilder().id("other").build(), ANY)
                        .close())
                .doesNotThrowAnyException();
    }

    @Test
    public void testCheckHealth() throws IOException {
        WebSource up = sampleSource.toBuilder().id("up").monitorOf("mock:up").build();
        WebSource down =
                sampleSource.toBuilder().id("down").monitorOf("mock:down").build();
        WebSource ko = sampleSource.toBuilder()
                .id("ko")
                .endpointOf("http://ko")
                .monitorOf("mock:up")
                .build();
        WebSource none = sampleSource.toBuilder().id("none").alias("alias").build();

        Monitor monitor = new Monitor() {
            @Override
            public @NonNull String getMonitorId() {
                return "MOCK";
            }

            @Override
            public @NonNull String getMonitorUriScheme() {
                return "mock";
            }

            @Override
            public @NonNull MonitorReport getReport(@NonNull WebSource source, @NonNull WebContext context) {
                return MonitorReport.builder()
                        .source(source.getId())
                        .status(MonitorStatus.valueOf(
                                source.getMonitor().getSchemeSpecificPart().toUpperCase(java.util.Locale.ROOT)))
                        .build();
            }

            @Override
            public @NonNull Collection<String> getMonitorPropertyNames() {
                return Collections.emptyList();
            }
        };

        SdmxWebManager manager = SdmxWebManager.builder()
                .driver(MockedDriver.builder()
                        .id("repoDriver")
                        .available(true)
                        .repo(sample, EnumSet.allOf(Feature.class))
                        .customSource(up)
                        .customSource(down)
                        .customSource(ko)
                        .customSource(none)
                        .build())
                .monitor(monitor)
                .build();

        assertThatNullPointerException().isThrownBy(() -> manager.checkHealth(null));

        assertThatIOException()
                .isThrownBy(() -> manager.checkHealth(
                        WebHealthRequest.builder().source("other").build()))
                .withMessageContaining("other");

        assertThat(manager.checkHealth(WebHealthRequest.DEFAULT))
                .describedAs("default request checks the monitor of every non-alias source")
                .extracting(
                        HealthReport::getSource,
                        HealthReport::getVerdict,
                        HealthReport::getMonitorError,
                        HealthReport::getAccess)
                .containsExactly(
                        tuple("down", HealthVerdict.REMOTE_OUTAGE, null, null),
                        tuple("ko", HealthVerdict.OK, null, null),
                        tuple("none", HealthVerdict.UNKNOWN, "No monitor defined", null),
                        tuple("repo", HealthVerdict.UNKNOWN, "No monitor defined", null),
                        tuple("up", HealthVerdict.OK, null, null));

        assertThat(manager.checkHealth(WebHealthRequest.builder()
                        .source("up")
                        .source("down")
                        .source("ko")
                        .source("none")
                        .checks(EnumSet.allOf(HealthCheck.class))
                        .build()))
                .describedAs("both checks are combined into a verdict")
                .extracting(
                        HealthReport::getSource,
                        HealthReport::getVerdict,
                        o -> o.getAccess().isAccessible())
                .containsExactly(
                        tuple("down", HealthVerdict.DEGRADED, true),
                        tuple("ko", HealthVerdict.LOCAL_ISSUE, false),
                        tuple("none", HealthVerdict.OK, true),
                        tuple("up", HealthVerdict.OK, true));

        assertThat(manager.checkHealth("ko", EnumSet.of(HealthCheck.ACCESS)))
                .describedAs("access check without monitor")
                .returns(HealthVerdict.UNREACHABLE, HealthReport::getVerdict)
                .returns(null, HealthReport::getMonitor)
                .returns(null, HealthReport::getMonitorError);
    }

    @Test
    public void testSearchFlows() throws IOException {
        Flow exr = Flow.builder()
                .ref(FlowRef.parse("EXR"))
                .structureRef(StructureRef.parse("EXR_DSD"))
                .name("Exchange rates")
                .description("<b>Daily</b> rates")
                .build();
        Flow gdp = Flow.builder()
                .ref(FlowRef.parse("GDP"))
                .structureRef(StructureRef.parse("GDP_DSD"))
                .name("Gross domestic product")
                .build();
        DataRepository single =
                DataRepository.builder().name("single").flow(exr).build();
        DataRepository multi = DataRepository.builder()
                .name("multi")
                .database(new Database(DatabaseRef.parse("db1"), "First"))
                .database(new Database(DatabaseRef.parse("db2"), "Second"))
                .flow(gdp)
                .build();
        WebSource secret = MockedDriver.sourceOf("secret", "repoDriver", single).toBuilder()
                .confidentiality(Confidentiality.CONFIDENTIAL)
                .build();
        WebSource broken = MockedDriver.sourceOf("broken", "repoDriver", single).toBuilder()
                .endpointOf("missing")
                .build();
        WebSource slow = WebSource.builder()
                .id("slow")
                .driver("slowDriver")
                .endpointOf("slow")
                .build();

        SdmxWebManager manager = SdmxWebManager.builder()
                .driver(MockedDriver.builder()
                        .id("repoDriver")
                        .available(true)
                        .repo(single, EnumSet.allOf(Feature.class))
                        .repo(multi, EnumSet.allOf(Feature.class))
                        .customSource(secret)
                        .customSource(broken)
                        .build())
                .driver(new SlowDriver(slow))
                .build();

        assertThatNullPointerException().isThrownBy(() -> manager.searchFlows(null));

        assertThatIOException()
                .isThrownBy(() -> manager.searchFlows(
                        WebFlowsRequest.builder().source("other").build()))
                .withMessageContaining("other");

        assertThatIOException()
                .describedAs("source not allowed by the threshold")
                .isThrownBy(() -> manager.searchFlows(WebFlowsRequest.builder()
                        .source("secret")
                        .confidentialityThreshold(Confidentiality.PUBLIC)
                        .build()));

        assertThat(WebFlowsRequest.DEFAULT.getEffectiveConfidentialityThreshold())
                .describedAs("public sources only by default when searching all sources")
                .isEqualTo(Confidentiality.PUBLIC);
        assertThat(manager.searchFlows(
                                WebFlowsRequest.builder().source("secret").build())
                        .getItems())
                .describedAs("requested sources are allowed by default")
                .extracting(o -> o.getSource().getId())
                .containsExactly("secret");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> manager.searchFlows(
                        WebFlowsRequest.builder().source("single").offset(-1).build()));

        WebFlowsResult all = manager.searchFlows(WebFlowsRequest.builder()
                .confidentialityThreshold(Confidentiality.RESTRICTED)
                .timeout(Duration.ofMillis(200))
                .build());
        assertThat(all.getItems())
                .describedAs("all databases of every allowed non-alias source, sorted without query")
                .extracting(
                        o -> o.getSource().getId(),
                        o -> o.getDatabase().getId(),
                        o -> o.getFlow().getRef().getId())
                .containsExactly(
                        tuple("multi", "db1", "GDP"), tuple("multi", "db2", "GDP"), tuple("single", "", "EXR"));
        assertThat(all.getTotal()).isEqualTo(3);
        assertThat(all.getScores()).describedAs("no score without query").isEmpty();
        assertThat(all.getFailures())
                .describedAs("failing and slow sources are reported, not thrown")
                .extracting(SourceFailure::getSource, o -> o.getCause().getClass())
                .containsExactly(tuple("broken", IOException.class), tuple("slow", TimeoutException.class));

        assertThat(manager.searchFlows(WebFlowsRequest.builder()
                        .source("single")
                        .source("multi")
                        .query("exchange")
                        .plainText(true)
                        .build()))
                .describedAs("ranked by relevance with transformed descriptions")
                .returns(1, WebFlowsResult::getTotal)
                .returns(Collections.emptyList(), WebFlowsResult::getFailures)
                .returns(Collections.singletonList(1.0), WebFlowsResult::getScores)
                .extracting(WebFlowsResult::getItems, list(FlowEntry.class))
                .singleElement()
                .returns("single", o -> o.getSource().getId())
                .returns("Daily rates", o -> o.getFlow().getDescription());

        assertThat(manager.searchFlows(WebFlowsRequest.builder()
                        .source("multi")
                        .source("single")
                        .offset(1)
                        .maxResults(1)
                        .build()))
                .describedAs("paging")
                .returns(3, WebFlowsResult::getTotal)
                .extracting(WebFlowsResult::getItems, list(FlowEntry.class))
                .extracting(o -> o.getDatabase().getId())
                .containsExactly("db2");
    }

    @lombok.AllArgsConstructor
    private static final class SlowDriver implements Driver {

        private final WebSource source;

        @Override
        public @NonNull String getDriverId() {
            return source.getDriver();
        }

        @Override
        public int getDriverRank() {
            return NATIVE_DRIVER_RANK;
        }

        @Override
        public boolean isDriverAvailable() {
            return true;
        }

        @Override
        public @NonNull Connection connect(
                @NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context)
                throws IOException {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            throw new IOException("Too late");
        }

        @Override
        public @NonNull Collection<WebSource> getDefaultSources() {
            return Collections.singletonList(source);
        }

        @Override
        public @NonNull Collection<String> getDriverPropertyNames() {
            return Collections.emptyList();
        }
    }

    @Test
    public void testGetIcons() throws IOException {
        SourceIcon svg = SourceIcon.ofSvg(IconSamples.svg("<svg xmlns='http://www.w3.org/2000/svg'/>"));
        SourceIcon png16 = SourceIcon.ofPng(IconSamples.png(16, 16));
        SourceIcon png64 = SourceIcon.ofPng(IconSamples.png(64, 64));
        String png16Data = "data:image/png;base64," + Base64.getEncoder().encodeToString(IconSamples.png(16, 16));

        List<String> errors = new ArrayList<>();
        SdmxWebManager manager = SdmxWebManager.builder()
                .driver(sampleDriver)
                .iconProvider(MockedIconProvider.builder()
                        .id("FAILING")
                        .failure("ECB")
                        .build())
                .iconProvider(MockedIconProvider.builder()
                        .id("EMPTY")
                        .icon("ECB", Collections.emptyList())
                        .build())
                .iconProvider(MockedIconProvider.builder()
                        .id("FIRST")
                        .icon("ECB", Arrays.asList(png16, png64))
                        .build())
                .iconProvider(MockedIconProvider.builder()
                        .id("SECOND")
                        .icon("ECB", Collections.singletonList(svg))
                        .build())
                .onError(
                        source -> (marker, message, error) -> errors.add(marker + ":" + source.getId() + ":" + message))
                .build();

        assertThat(manager.getIcons(sampleSource)).isEmpty();
        assertThat(manager.getIcons(sampleSource.getId())).isEmpty();
        assertThatIOException().isThrownBy(() -> manager.getIcons("other"));

        assertThat(manager.getIcons(
                        sampleSource.toBuilder().iconOf("sdmx-dl:icon/ECB").build()))
                .containsExactly(png16, png64);
        assertThat(errors).containsExactly("ICON:repoSource:Failed to load icon 'ECB' from 'FAILING'");
        errors.clear();

        assertThat(manager.getIcons(
                        sampleSource.toBuilder().iconOf("sdmx-dl:icon/XYZ").build()))
                .isEmpty();
        assertThat(manager.getIcons(sampleSource.toBuilder()
                        .iconOf("https://example.com/favicon.ico")
                        .build()))
                .isEmpty();
        assertThat(errors).isEmpty();

        assertThat(manager.getIcons(sampleSource.toBuilder().iconOf(png16Data).build()))
                .containsExactly(png16);
        assertThat(manager.getIcons(sampleSource.toBuilder()
                        .iconOf(png16Data)
                        .confidentiality(Confidentiality.SECRET)
                        .build()))
                .containsExactly(png16);

        assertThat(manager.getIcons(
                        sampleSource.toBuilder().iconOf("sdmx-dl:icon/../x").build()))
                .isEmpty();
        assertThat(manager.getIcons(sampleSource.toBuilder()
                        .iconOf("data:image/gif;base64,R0lGODlh")
                        .build()))
                .isEmpty();
        assertThat(errors)
                .containsExactly(
                        "ICON:repoSource:Invalid icon 'sdmx-dl:icon/../x'",
                        "ICON:repoSource:Invalid icon 'data:image/gif;base64,R0lGODlh'");
    }

    @Test
    public void testGetIcon() throws IOException {
        SourceIcon png16 = SourceIcon.ofPng(IconSamples.png(16, 16));
        SourceIcon png64 = SourceIcon.ofPng(IconSamples.png(64, 64));
        WebSource source = sampleSource.toBuilder().iconOf("sdmx-dl:icon/ECB").build();

        SdmxWebManager manager = SdmxWebManager.builder()
                .driver(MockedDriver.builder()
                        .id("repoDriver")
                        .rank(0)
                        .available(true)
                        .customSource(source)
                        .build())
                .iconProvider(MockedIconProvider.builder()
                        .icon("ECB", Arrays.asList(png16, png64))
                        .build())
                .build();

        assertThat(manager.getIcon(source, 16)).contains(png16);
        assertThat(manager.getIcon(source, 32)).contains(png64);
        assertThat(manager.getIcon(source.getId(), 128)).contains(png64);
        assertThat(manager.getIcon(sampleSource, 16)).isEmpty();
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getIcon(source, 0));
        assertThatIOException().isThrownBy(() -> manager.getIcon("other", 16));
    }

    private final DataRepository sample = DataRepository.builder().name("repo").build();
    private final WebSource sampleSource = WebSource.builder()
            .id("repoSource")
            .driver("repoDriver")
            .endpointOf(sample.getName())
            .build();
    private final Driver sampleDriver = MockedDriver.builder()
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
