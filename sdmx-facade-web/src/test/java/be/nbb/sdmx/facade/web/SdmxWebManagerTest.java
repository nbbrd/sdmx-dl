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
package be.nbb.sdmx.facade.web;

import be.nbb.sdmx.facade.LanguagePriorityList;
import static be.nbb.sdmx.facade.LanguagePriorityList.ANY;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.tck.ConnectionSupplierAssert;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        ConnectionSupplierAssert.assertCompliance(SdmxWebManager.of(SdmxWebBridge.getDefault(), REPO), HELLO.getName(), "ko");
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> SdmxWebManager.of(null, REPO));
        assertThatNullPointerException().isThrownBy(() -> SdmxWebManager.of(SdmxWebBridge.getDefault(), (Iterable) null));
        assertThatNullPointerException().isThrownBy(() -> SdmxWebManager.of(SdmxWebBridge.getDefault(), (SdmxWebDriver[]) null));
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfSource() {
        SdmxWebManager manager = SdmxWebManager.of(SdmxWebBridge.getDefault(), REPO);
        assertThatNullPointerException().isThrownBy(() -> manager.getConnection((SdmxWebSource) null, ANY));
        assertThatNullPointerException().isThrownBy(() -> manager.getConnection(HELLO, null));
        assertThatIOException().isThrownBy(() -> manager.getConnection(HELLO.toBuilder().uri("ko").build(), ANY));
    }

    private static final SdmxWebSource HELLO = SdmxWebSource.builder().name("ok").uri(RepoDriver.PREFIX + "r1").build();
    private static final SdmxWebDriver REPO = new RepoDriver();

    private static final class RepoDriver implements SdmxWebDriver {

        static final String PREFIX = "sdmx:repo:";

        final List<SdmxRepository> repos = Collections.singletonList(SdmxRepository.builder().name("r1").build());

        @Override
        public SdmxWebConnection connect(SdmxWebSource source, LanguagePriorityList languages, SdmxWebBridge bridge) throws IOException {
            String repoName = source.getUri().toString().substring(PREFIX.length());
            return repos.stream()
                    .filter(o -> o.getName().equals(repoName))
                    .findFirst()
                    .map(o -> new RepoWebConnection(o.asConnection()))
                    .orElseThrow(IOException::new);
        }

        @Override
        public boolean accepts(SdmxWebSource source) throws IOException {
            return source.getUri().toString().startsWith(PREFIX);
        }

        @Override
        public Collection<SdmxWebSource> getDefaultSources() {
            return Collections.singletonList(HELLO);
        }
    }

    @lombok.AllArgsConstructor
    private static final class RepoWebConnection implements SdmxWebConnection {

        @lombok.experimental.Delegate
        private final SdmxConnection delegate;

        @Override
        public Duration ping() throws IOException {
            return Duration.ZERO;
        }
    }
}
