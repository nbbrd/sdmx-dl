/*
 * Copyright 2015 National Bank of Belgium
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

import static be.nbb.sdmx.facade.DataflowRef.ALL_AGENCIES;
import static be.nbb.sdmx.facade.DataflowRef.LATEST_VERSION;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataflowRefTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(DataflowRef.parse("")).isEqualTo(DataflowRef.of(null, "", null));
        assertThat(DataflowRef.parse("hello")).isEqualTo(DataflowRef.of(null, "hello", null));
        assertThat(DataflowRef.parse("world,hello")).isEqualTo(DataflowRef.of("world", "hello", null));
        assertThat(DataflowRef.parse("world,hello,123")).isEqualTo(DataflowRef.of("world", "hello", "123"));
        assertThat(DataflowRef.parse("world,hello,")).isEqualTo(DataflowRef.of("world", "hello", LATEST_VERSION));
        assertThat(DataflowRef.parse(",hello,")).isEqualTo(DataflowRef.of(ALL_AGENCIES, "hello", LATEST_VERSION));
        assertThat(DataflowRef.parse(",,")).isEqualTo(DataflowRef.of(ALL_AGENCIES, "", LATEST_VERSION));
        assertThatIllegalArgumentException().isThrownBy(() -> DataflowRef.parse(",,,,"));
        assertThatNullPointerException().isThrownBy(() -> DataflowRef.parse(null));
    }

    @Test
    @SuppressWarnings("null")
    public void testValueOf() {
        assertThat(DataflowRef.of(null, "", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, DataflowRef::toString)
                .containsExactly(ALL_AGENCIES, "", LATEST_VERSION, "all,,latest");

        assertThat(DataflowRef.of("", "hello", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, DataflowRef::toString)
                .containsExactly(ALL_AGENCIES, "hello", LATEST_VERSION, "all,hello,latest");

        assertThat(DataflowRef.of("world", "hello", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, DataflowRef::toString)
                .containsExactly("world", "hello", LATEST_VERSION, "world,hello,latest");

        assertThat(DataflowRef.of("world", "hello", "123"))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, DataflowRef::toString)
                .containsExactly("world", "hello", "123", "world,hello,123");

        assertThatIllegalArgumentException().isThrownBy(() -> DataflowRef.of(null, "world,hello", null));
        assertThatNullPointerException().isThrownBy(() -> DataflowRef.of(null, null, null));
    }

    @Test
    public void testEquals() {
        assertThat(DataflowRef.of("", "", "")).isEqualTo(DataflowRef.of("", "", ""));
        assertThat(DataflowRef.of("world", "hello", "123")).isEqualTo(DataflowRef.of("world", "hello", "123"));
        assertThat(DataflowRef.of("world", "other", "123")).isNotEqualTo(DataflowRef.of("world", "hello", "123"));
        assertThat(DataflowRef.of("", "", "")).isNotEqualTo(DataflowRef.of("world", "hello", "123"));
    }

    @Test
    @SuppressWarnings("null")
    public void testContains() {
        assertThat(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of("world", "hello", "123"))).isTrue();
        assertThat(DataflowRef.of(ALL_AGENCIES, "hello", "123").contains(DataflowRef.of("world", "hello", "123"))).isTrue();
        assertThat(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of(ALL_AGENCIES, "hello", "123"))).isFalse();
        assertThat(DataflowRef.of("world", "hello", LATEST_VERSION).contains(DataflowRef.of("world", "hello", "123"))).isTrue();
        assertThat(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of("world", "hello", LATEST_VERSION))).isFalse();
        assertThatNullPointerException().isThrownBy(() -> DataflowRef.of("world", "hello", "123").contains(null));
    }

    @Test
    @SuppressWarnings("null")
    public void testContainsRef() {
        assertThatNullPointerException().isThrownBy(() -> DataflowRef.of("world", "hello", "123").containsRef(null));
    }
}
