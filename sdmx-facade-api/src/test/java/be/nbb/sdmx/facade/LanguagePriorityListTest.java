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

import static be.nbb.sdmx.facade.LanguagePriorityList.ANY;
import static be.nbb.sdmx.facade.LanguagePriorityList.parse;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LanguagePriorityListTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(parse("*")).hasToString("*");
        assertThat(parse("fr")).hasToString("fr");
        assertThat(parse("fr-BE")).hasToString("fr-be");
        assertThat(parse("fr-BE,fr;q=0.5")).hasToString("fr-be,fr;q=0.5");
        assertThat(parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")).hasToString("fr-ch,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5");
        assertThatIllegalArgumentException().isThrownBy(() -> parse("fr-BE;"));
        assertThatNullPointerException().isThrownBy(() -> parse(null));
    }

    @Test
    public void testEquals() {
        assertThat(parse("*"))
                .isEqualTo(parse("*"))
                .isEqualTo(ANY);

        assertThat(parse("fr-BE"))
                .isEqualTo(parse("fr-BE;q=1"))
                .isEqualTo(parse("fr-BE"));
    }

    @Test
    @SuppressWarnings("null")
    public void testLookupTag() {
        assertThat(parse("fr").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        assertThat(parse("fr-BE").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        assertThat(parse("fr,nl;q=0.7,en;q=0.3").lookupTag(Arrays.asList("de", "nl", "en"))).isEqualTo("nl");
        assertThat(parse("fr").lookupTag(Arrays.asList("nl"))).isNull();
        assertThatNullPointerException().isThrownBy(() -> parse("fr").lookupTag(null));
    }
}
