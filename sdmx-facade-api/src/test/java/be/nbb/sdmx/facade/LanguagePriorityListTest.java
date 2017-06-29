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
package be.nbb.sdmx.facade;

import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class LanguagePriorityListTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(LanguagePriorityList.parse("*")).hasToString("*");
        assertThat(LanguagePriorityList.parse("fr")).hasToString("fr");
        assertThat(LanguagePriorityList.parse("fr-BE")).hasToString("fr-be");
        assertThat(LanguagePriorityList.parse("fr-BE,fr;q=0.5")).hasToString("fr-be,fr;q=0.5");
        assertThat(LanguagePriorityList.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")).hasToString("fr-ch,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5");
        assertThatThrownBy(() -> LanguagePriorityList.parse("fr-BE;")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LanguagePriorityList.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEquals() {
        assertThat(LanguagePriorityList.parse("*"))
                .isEqualTo(LanguagePriorityList.parse("*"))
                .isEqualTo(LanguagePriorityList.ANY);

        assertThat(LanguagePriorityList.parse("fr-BE"))
                .isEqualTo(LanguagePriorityList.parse("fr-BE;q=1"))
                .isEqualTo(LanguagePriorityList.parse("fr-BE"));
    }

    @Test
    @SuppressWarnings("null")
    public void testLookupTag() {
        assertThat(LanguagePriorityList.parse("fr").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        assertThat(LanguagePriorityList.parse("fr-BE").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        assertThat(LanguagePriorityList.parse("fr,nl;q=0.7,en;q=0.3").lookupTag(Arrays.asList("de", "nl", "en"))).isEqualTo("nl");
        assertThat(LanguagePriorityList.parse("fr").lookupTag(Arrays.asList("nl"))).isNull();
        assertThatThrownBy(() -> LanguagePriorityList.parse("fr").lookupTag(null)).isInstanceOf(NullPointerException.class);
    }
}
