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
package sdmxdl.tck;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.SdmxConnection;
import sdmxdl.SdmxManager;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class SdmxManagerAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample<SOURCE> {
        SOURCE validSource;
        SOURCE invalidSource;
    }

    public <SOURCE> void assertCompliance(SdmxManager<SOURCE> manager, Sample<SOURCE> sample) {
        TckUtil.run(s -> assertCompliance(s, manager, sample));
    }

    public <SOURCE> void assertCompliance(SoftAssertions s, SdmxManager<SOURCE> manager, Sample<SOURCE> sample) {
        checkGetLanguages(s, manager);
        checkGetConnection(s, manager, sample);
    }

    private <SOURCE> void checkGetLanguages(SoftAssertions s, SdmxManager<SOURCE> manager) {
        s.assertThat(manager.getLanguages()).isNotNull();
    }

    private <SOURCE> void checkGetConnection(SoftAssertions s, SdmxManager<SOURCE> manager, Sample<SOURCE> sample) {
        s.assertThatThrownBy(() -> manager.getConnection(null))
                .as("Expecting 'getConnection(SOURCE)' to raise NPE when called with null name")
                .isInstanceOf(NullPointerException.class);

        if (sample.validSource != null) {
            try (SdmxConnection conn = manager.getConnection(sample.validSource)) {
                s.assertThat(conn)
                        .as("Expecting 'getConnection(SOURCE)' to return a non-null connection")
                        .isNotNull();
            } catch (IOException ex) {
                s.fail("Expecting 'getConnection(SOURCE)' to not raise IOException on valid name", ex);
            }
        }

        if (sample.invalidSource != null) {
            s.assertThatThrownBy(() -> manager.getConnection(sample.invalidSource))
                    .as("Expecting 'getConnection(SOURCE) to raise IOException on invalid name")
                    .isInstanceOf(IOException.class);
        }
    }
}
