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
import static sdmxdl.FlowRef.of;
import static sdmxdl.ResourceRef.ALL_AGENCIES;
import static sdmxdl.ResourceRef.LATEST_VERSION;

/**
 * @author Philippe Charles
 */
public class FlowRefTest {

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testParse() {
        assertThat(FlowRef.parse("")).isEqualTo(of(null, "", null));
        assertThat(FlowRef.parse("hello")).isEqualTo(of(null, "hello", null));
        assertThat(FlowRef.parse("world,hello")).isEqualTo(of("world", "hello", null));
        assertThat(FlowRef.parse("world,hello,123")).isEqualTo(of("world", "hello", "123"));
        assertThat(FlowRef.parse("world,hello,")).isEqualTo(of("world", "hello", LATEST_VERSION));
        assertThat(FlowRef.parse(",hello,")).isEqualTo(of(ALL_AGENCIES, "hello", LATEST_VERSION));
        assertThat(FlowRef.parse(",,")).isEqualTo(of(ALL_AGENCIES, "", LATEST_VERSION));
        assertThatIllegalArgumentException().isThrownBy(() -> FlowRef.parse(",,,,"));
        assertThatNullPointerException().isThrownBy(() -> FlowRef.parse(null));
    }

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testValueOf() {
        assertThat(of(null, "", null))
                .extracting(FlowRef::getAgency, FlowRef::getId, FlowRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "", LATEST_VERSION, "all,,latest");

        assertThat(of("", "hello", null))
                .extracting(FlowRef::getAgency, FlowRef::getId, FlowRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "hello", LATEST_VERSION, "all,hello,latest");

        assertThat(of("world", "hello", null))
                .extracting(FlowRef::getAgency, FlowRef::getId, FlowRef::getVersion, Object::toString)
                .containsExactly("world", "hello", LATEST_VERSION, "world,hello,latest");

        assertThat(of("world", "hello", "123"))
                .extracting(FlowRef::getAgency, FlowRef::getId, FlowRef::getVersion, Object::toString)
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
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testContains() {
        FlowRef x = of("ECB", "EXR", "1");

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
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testContainsRef() {
        FlowRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.containsRef(null));

        assertThat(x.containsRef(flowOf(of("ECB", "EXR", "1")))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").containsRef(flowOf(x))).isTrue();
        assertThat(x.containsRef(flowOf(of(ALL_AGENCIES, "EXR", "1")))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).containsRef(flowOf(x))).isTrue();
        assertThat(x.containsRef(flowOf(of("ECB", "EXR", LATEST_VERSION)))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).containsRef(flowOf(x))).isTrue();
        assertThat(x.containsRef(flowOf(of(ALL_AGENCIES, "EXR", LATEST_VERSION)))).isFalse();
    }

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testEqualsRef() {
        FlowRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.equalsRef(null));

        assertThat(x.equalsRef(flowOf(of("ECB", "EXR", "1")))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of(ALL_AGENCIES, "EXR", "1")))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of("ECB", "EXR", LATEST_VERSION)))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of(ALL_AGENCIES, "EXR", LATEST_VERSION)))).isFalse();
    }

    private Flow flowOf(FlowRef ref) {
        return Flow.builder().ref(ref).structureRef(StructureRef.parse("")).name("").build();
    }
}
