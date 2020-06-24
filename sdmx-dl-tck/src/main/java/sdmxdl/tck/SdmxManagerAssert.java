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
@lombok.experimental.UtilityClass
public class SdmxManagerAssert {

    @lombok.Builder
    public static class Sample {
        String validName;
        String invalidName;
    }

    public void assertCompliance(SdmxManager manager, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, manager, sample));
    }

    public void assertCompliance(SoftAssertions s, SdmxManager manager, Sample sample) {
        checkGetLanguages(s, manager);
        checkGetConnection(s, manager, sample);
    }

    private void checkGetLanguages(SoftAssertions s, SdmxManager manager) {
        s.assertThat(manager.getLanguages()).isNotNull();
    }

    private void checkGetConnection(SoftAssertions s, SdmxManager manager, Sample sample) {
        s.assertThatThrownBy(() -> manager.getConnection(null))
                .as("Expecting 'getConnection(String)' to raise NPE when called with null name")
                .isInstanceOf(NullPointerException.class);

        if (sample.validName != null) {
            try (SdmxConnection conn = manager.getConnection(sample.validName)) {
                s.assertThat(conn)
                        .as("Expecting 'getConnection(String)' to return a non-null connection")
                        .isNotNull();
            } catch (IOException ex) {
                s.fail("Expecting 'getConnection(String)' to not raise IOException on valid name", ex);
            }
        }

        if (sample.invalidName != null) {
            s.assertThatThrownBy(() -> manager.getConnection(sample.invalidName))
                    .as("Expecting 'getConnection(String) to raise IOException on invalid name")
                    .isInstanceOf(IOException.class);
        }
    }
}
