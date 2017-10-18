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
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.tck.ConnectionSupplierAssert;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxWebManagerTest {

    @Test
    public void testCompliance() {
        ConnectionSupplierAssert.assertCompliance(SdmxWebManager.of(REPO), HELLO.getName(), "ko");
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatThrownBy(() -> SdmxWebManager.of((Iterable) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SdmxWebManager.of((SdmxWebDriver[]) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetConnectionOfEntryPoint() {
        SdmxWebManager manager = SdmxWebManager.of(REPO);
        assertThatThrownBy(() -> manager.getConnection((SdmxWebEntryPoint) null, LanguagePriorityList.ANY)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> manager.getConnection(HELLO, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> manager.getConnection(HELLO.toBuilder().uri("ko").build(), LanguagePriorityList.ANY)).isInstanceOf(IOException.class);
    }

    private static final SdmxWebEntryPoint HELLO = SdmxWebEntryPoint.builder().name("ok").uri(RepoDriver.PREFIX + "r1").build();
    private static final SdmxWebDriver REPO = new RepoDriver();

    private static final class RepoDriver implements SdmxWebDriver {

        static final String PREFIX = "sdmx:repo:";

        final List<SdmxRepository> repos = Collections.singletonList(SdmxRepository.builder().name("r1").build());

        @Override
        public SdmxConnection connect(URI uri, Map<?, ?> properties, LanguagePriorityList languages) throws IOException {
            String repoName = uri.toString().substring(PREFIX.length());
            return repos.stream()
                    .filter(o -> o.getName().equals(repoName))
                    .findFirst()
                    .map(SdmxRepository::asConnection)
                    .orElseThrow(IOException::new);
        }

        @Override
        public boolean acceptsURI(URI uri) throws IOException {
            return uri.toString().startsWith(PREFIX);
        }

        @Override
        public Collection<SdmxWebEntryPoint> getDefaultEntryPoints() {
            return Collections.singletonList(HELLO);
        }
    }
}
