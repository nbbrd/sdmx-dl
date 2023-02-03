/*
 * Copyright 2020 National Bank of Belgium
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

import java.net.MalformedURLException;
import java.net.URI;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class SdmxWebSourceTest {

    @Test
    public void testBuilderEndpointOf() throws MalformedURLException {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().endpointOf(null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SdmxWebSource.builder().endpointOf("h ttp://localhost"));

        assertThat(SdmxWebSource.builder().endpointOf("http://localhost").id("").driver("").build().getEndpoint())
                .isEqualTo(URI.create("http://localhost"));
    }

    @Test
    public void testBuilderDescription() {
        SdmxWebSource base = SdmxWebSource.builder().endpointOf("http://localhost").id("").driver("").build();

        assertThat(
                base
                        .toBuilder()
                        .name("en", "European Central Bank")
                        .name("fr", "Banque Centrale Européenne")
                        .build()
                        .getNames()
                        .keySet())
                .containsExactly("en", "fr");

        assertThat(
                base
                        .toBuilder()
                        .name("fr", "Banque Centrale Européenne")
                        .name("en", "European Central Bank")
                        .build()
                        .getNames()
                        .keySet())
                .containsExactly("fr", "en");

        assertThatNullPointerException()
                .isThrownBy(() -> base.toBuilder().nameOf(null));
    }

    @Test
    public void testBuilderPropertyOf() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().propertyOf(null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().propertyOf("", null));

        assertThat(SdmxWebSource.builder().propertyOf("hello", "world").endpointOf("http://localhost").id("").driver("").build().getProperties())
                .containsEntry("hello", "world");
    }

    @Test
    public void testAlias() {
        SdmxWebSource estat = SdmxWebSource.builder().id("ESTAT").alias("EUROSTAT").driver("").endpointOf("http://localhost").build();

        assertThat(estat.alias("EUROSTAT"))
                .isEqualTo(estat.toBuilder().id("EUROSTAT").build());

        assertThatNullPointerException()
                .isThrownBy(() -> estat.alias(null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> estat.alias("other"));
    }

    @Test
    public void testIsAlias() {
        SdmxWebSource base = SdmxWebSource.builder().id("ESTAT").driver("").endpointOf("http://localhost").build();
        assertThat(base.isAlias()).isFalse();
        assertThat(base.toBuilder().alias("EUROSTAT").build().isAlias()).isFalse();
        assertThat(base.toBuilder().id("EUROSTAT").alias("EUROSTAT").build().isAlias()).isTrue();
    }

    @Test
    public void testWebsite() {
        SdmxWebSource base = SdmxWebSource.builder().id("ESTAT").driver("").endpointOf("http://localhost").build();
        assertThat(base.getWebsite()).isNull();
        assertThat(base.toBuilder().websiteOf("http://somewhere").build().getWebsite())
                .asString()
                .isEqualTo("http://somewhere");
    }
}
