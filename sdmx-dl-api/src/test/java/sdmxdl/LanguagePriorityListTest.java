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
package sdmxdl;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class LanguagePriorityListTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        Assertions.assertThat(LanguagePriorityList.parse("*")).hasToString("*");
        Assertions.assertThat(LanguagePriorityList.parse("fr")).hasToString("fr");
        Assertions.assertThat(LanguagePriorityList.parse("fr-BE")).hasToString("fr-be");
        Assertions.assertThat(LanguagePriorityList.parse("fr-BE,fr;q=0.5")).hasToString("fr-be,fr;q=0.5");
        Assertions.assertThat(LanguagePriorityList.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")).hasToString("fr-ch,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5");
        assertThatIllegalArgumentException().isThrownBy(() -> LanguagePriorityList.parse("fr-BE;"));
        assertThatNullPointerException().isThrownBy(() -> LanguagePriorityList.parse(null));
    }

    @Test
    public void testEquals() {
        Assertions.assertThat(LanguagePriorityList.parse("*"))
                .isEqualTo(LanguagePriorityList.parse("*"))
                .isEqualTo(LanguagePriorityList.ANY);

        Assertions.assertThat(LanguagePriorityList.parse("fr-BE"))
                .isEqualTo(LanguagePriorityList.parse("fr-BE;q=1"))
                .isEqualTo(LanguagePriorityList.parse("fr-BE"));
    }

    @Test
    @SuppressWarnings("null")
    public void testLookupTag() {
        Assertions.assertThat(LanguagePriorityList.parse("fr").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        Assertions.assertThat(LanguagePriorityList.parse("fr-BE").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        Assertions.assertThat(LanguagePriorityList.parse("fr,nl;q=0.7,en;q=0.3").lookupTag(Arrays.asList("de", "nl", "en"))).isEqualTo("nl");
        Assertions.assertThat(LanguagePriorityList.parse("fr").lookupTag(Collections.singletonList("nl"))).isNull();
        assertThatNullPointerException().isThrownBy(() -> LanguagePriorityList.parse("fr").lookupTag(null));
    }
}
