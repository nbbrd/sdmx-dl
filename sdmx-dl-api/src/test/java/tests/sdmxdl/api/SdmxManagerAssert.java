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
package tests.sdmxdl.api;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.Connection;
import sdmxdl.SdmxManager;
import sdmxdl.SdmxSource;

import java.io.IOException;

import static sdmxdl.Languages.ANY;
import static tests.sdmxdl.api.TckUtil.nullDescriptionOf;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class SdmxManagerAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample<S extends SdmxSource> {
        S validSource;
        S invalidSource;
    }

    public <S extends SdmxSource> void assertCompliance(SdmxManager<S> manager, Sample<S> sample) {
        TckUtil.run(s -> assertCompliance(s, manager, sample));
    }

    public <S extends SdmxSource> void assertCompliance(SoftAssertions s, SdmxManager<S> manager, Sample<S> sample) {
        checkGetConnection(s, manager, sample);
    }

    private <S extends SdmxSource> void checkGetConnection(SoftAssertions s, SdmxManager<S> manager, Sample<S> sample) {
        s.assertThatThrownBy(() -> manager.getConnection(null, ANY))
                .as(nullDescriptionOf("getConnection(SOURCE,LANGUAGES)", "source"))
                .isInstanceOf(NullPointerException.class);

        if (sample.validSource != null) {
            s.assertThatThrownBy(() -> manager.getConnection(sample.validSource, null))
                    .as(nullDescriptionOf("getConnection(SOURCE,LANGUAGES)", "languages"))
                    .isInstanceOf(NullPointerException.class);

            try (Connection conn = manager.getConnection(sample.validSource, ANY)) {
                s.assertThat(conn)
                        .as("Expecting 'getConnection(SOURCE,LANGUAGES)' to return a non-null connection")
                        .isNotNull();
            } catch (IOException ex) {
                s.fail("Not expected to raise exception", ex);
            }
        }

        if (sample.invalidSource != null) {
            s.assertThatThrownBy(() -> manager.getConnection(sample.invalidSource, null))
                    .as(nullDescriptionOf("getConnection(SOURCE,LANGUAGES)", "languages"))
                    .isInstanceOf(NullPointerException.class);

            s.assertThatThrownBy(() -> manager.getConnection(sample.invalidSource, ANY))
                    .as("Expecting 'getConnection(SOURCE) to raise IOException on invalid name")
                    .isInstanceOf(IOException.class);
        }
    }
}
