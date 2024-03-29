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
package sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.StructureRef.of;
import static sdmxdl.ResourceRef.ALL_AGENCIES;
import static sdmxdl.ResourceRef.LATEST_VERSION;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ALL")
public class StructureRefTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(StructureRef.parse("")).isEqualTo(of(null, "", null));
        assertThat(StructureRef.parse("hello")).isEqualTo(of(null, "hello", null));
        assertThat(StructureRef.parse("world,hello")).isEqualTo(of("world", "hello", null));
        assertThat(StructureRef.parse("world,hello,123")).isEqualTo(of("world", "hello", "123"));
        assertThat(StructureRef.parse("world,hello,")).isEqualTo(of("world", "hello", LATEST_VERSION));
        assertThat(StructureRef.parse(",hello,")).isEqualTo(of(ALL_AGENCIES, "hello", LATEST_VERSION));
        assertThat(StructureRef.parse(",,")).isEqualTo(of(ALL_AGENCIES, "", LATEST_VERSION));
        assertThatIllegalArgumentException().isThrownBy(() -> StructureRef.parse(",,,,"));
        assertThatNullPointerException().isThrownBy(() -> StructureRef.parse(null));
    }

    @Test
    @SuppressWarnings("null")
    public void testValueOf() {
        assertThat(of(null, "", null))
                .extracting(StructureRef::getAgency, StructureRef::getId, StructureRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "", LATEST_VERSION, "all,,latest");

        assertThat(of("", "hello", null))
                .extracting(StructureRef::getAgency, StructureRef::getId, StructureRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "hello", LATEST_VERSION, "all,hello,latest");

        assertThat(of("world", "hello", null))
                .extracting(StructureRef::getAgency, StructureRef::getId, StructureRef::getVersion, Object::toString)
                .containsExactly("world", "hello", LATEST_VERSION, "world,hello,latest");

        assertThat(of("world", "hello", "123"))
                .extracting(StructureRef::getAgency, StructureRef::getId, StructureRef::getVersion, Object::toString)
                .containsExactly("world", "hello", "123", "world,hello,123");

        assertThatIllegalArgumentException().isThrownBy(() -> of(null, "world,hello", null));
        assertThatNullPointerException().isThrownBy(() -> of(null, null, null));
    }

    @Test
    public void testEquals() {
        assertThat(of("", "", ""))
                .isEqualTo(of("", "", ""));

        assertThat(of("world", "hello", "123"))
                .isEqualTo(of("world", "hello", "123"));

        assertThat(of("world", "other", "123"))
                .isNotEqualTo(of("world", "hello", "123"));

        assertThat(of("", "", ""))
                .isNotEqualTo(of("world", "hello", "123"));
    }

    @Test
    @SuppressWarnings("null")
    public void testContains() {
        StructureRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.contains(null));

        assertThat(x.contains(of("ECB", "EXR", "1"))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").contains(x)).isTrue();
        assertThat(x.contains(of(ALL_AGENCIES, "EXR", "1"))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).contains(x)).isTrue();
        assertThat(x.contains(of("ECB", "EXR", LATEST_VERSION))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).contains(x)).isTrue();
        assertThat(x.contains(of(ALL_AGENCIES, "EXR", LATEST_VERSION))).isFalse();
    }

    @Test
    @SuppressWarnings("null")
    public void testContainsRef() {
        StructureRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.containsRef(null));

        assertThat(x.containsRef(structOf(of("ECB", "EXR", "1")))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").containsRef(structOf(x))).isTrue();
        assertThat(x.containsRef(structOf(of(ALL_AGENCIES, "EXR", "1")))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).containsRef(structOf(x))).isTrue();
        assertThat(x.containsRef(structOf(of("ECB", "EXR", LATEST_VERSION)))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).containsRef(structOf(x))).isTrue();
        assertThat(x.containsRef(structOf(of(ALL_AGENCIES, "EXR", LATEST_VERSION)))).isFalse();
    }

    @Test
    @SuppressWarnings("null")
    public void testEqualsRef() {
        StructureRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.equalsRef(null));

        assertThat(x.equalsRef(structOf(of("ECB", "EXR", "1")))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").equalsRef(structOf(x))).isFalse();
        assertThat(x.equalsRef(structOf(of(ALL_AGENCIES, "EXR", "1")))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).equalsRef(structOf(x))).isFalse();
        assertThat(x.equalsRef(structOf(of("ECB", "EXR", LATEST_VERSION)))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).equalsRef(structOf(x))).isFalse();
        assertThat(x.equalsRef(structOf(of(ALL_AGENCIES, "EXR", LATEST_VERSION)))).isFalse();
    }

    private Structure structOf(StructureRef ref) {
        return Structure.builder().ref(ref).primaryMeasureId("").name("").build();
    }
}
