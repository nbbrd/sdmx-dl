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
import static sdmxdl.DataflowRef.of;
import static sdmxdl.ResourceRef.ALL_AGENCIES;
import static sdmxdl.ResourceRef.LATEST_VERSION;

/**
 * @author Philippe Charles
 */
public class DataflowRefTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(DataflowRef.parse("")).isEqualTo(of(null, "", null));
        assertThat(DataflowRef.parse("hello")).isEqualTo(of(null, "hello", null));
        assertThat(DataflowRef.parse("world,hello")).isEqualTo(of("world", "hello", null));
        assertThat(DataflowRef.parse("world,hello,123")).isEqualTo(of("world", "hello", "123"));
        assertThat(DataflowRef.parse("world,hello,")).isEqualTo(of("world", "hello", LATEST_VERSION));
        assertThat(DataflowRef.parse(",hello,")).isEqualTo(of(ALL_AGENCIES, "hello", LATEST_VERSION));
        assertThat(DataflowRef.parse(",,")).isEqualTo(of(ALL_AGENCIES, "", LATEST_VERSION));
        assertThatIllegalArgumentException().isThrownBy(() -> DataflowRef.parse(",,,,"));
        assertThatNullPointerException().isThrownBy(() -> DataflowRef.parse(null));
    }

    @Test
    @SuppressWarnings("null")
    public void testValueOf() {
        assertThat(of(null, "", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "", LATEST_VERSION, "all,,latest");

        assertThat(of("", "hello", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, Object::toString)
                .containsExactly(ALL_AGENCIES, "hello", LATEST_VERSION, "all,hello,latest");

        assertThat(of("world", "hello", null))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, Object::toString)
                .containsExactly("world", "hello", LATEST_VERSION, "world,hello,latest");

        assertThat(of("world", "hello", "123"))
                .extracting(DataflowRef::getAgency, DataflowRef::getId, DataflowRef::getVersion, Object::toString)
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
        DataflowRef x = of("ECB", "EXR", "1");

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
        DataflowRef x = of("ECB", "EXR", "1");

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
    @SuppressWarnings("null")
    public void testEqualsRef() {
        DataflowRef x = of("ECB", "EXR", "1");

        assertThatNullPointerException().isThrownBy(() -> x.equalsRef(null));

        assertThat(x.equalsRef(flowOf(of("ECB", "EXR", "1")))).isTrue();

        assertThat(of(ALL_AGENCIES, "EXR", "1").equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of(ALL_AGENCIES, "EXR", "1")))).isFalse();

        assertThat(of("ECB", "EXR", LATEST_VERSION).equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of("ECB", "EXR", LATEST_VERSION)))).isFalse();

        assertThat(of(ALL_AGENCIES, "EXR", LATEST_VERSION).equalsRef(flowOf(x))).isFalse();
        assertThat(x.equalsRef(flowOf(of(ALL_AGENCIES, "EXR", LATEST_VERSION)))).isFalse();
    }

    private Dataflow flowOf(DataflowRef ref) {
        return Dataflow.of(ref, DataStructureRef.parse(""), "");
    }
}
