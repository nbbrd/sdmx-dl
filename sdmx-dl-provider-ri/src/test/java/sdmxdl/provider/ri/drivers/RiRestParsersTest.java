package sdmxdl.provider.ri.drivers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.Flow;
import sdmxdl.FlowRef;

import java.io.IOException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.RepoSamples.STRUCT_REF;

public class RiRestParsersTest {

    @Test
    public void testGetResourceSelector() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> RiRestParsers.getResourceSelector(null));

        FlowRef fullRef1 = FlowRef.of("NBB", "XYZ", "v1.0");
        Flow resource1 = Flow.builder().ref(fullRef1).structureRef(STRUCT_REF).name("flow1").build();

        FlowRef fullRef2 = FlowRef.of("NBB", "XYZ", "v2.0");
        Flow resource2 = Flow.builder().ref(fullRef2).structureRef(STRUCT_REF).name("flow2").build();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(emptyList()))
                .isEmpty();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(singletonList(resource2)))
                .isEmpty();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(asList(resource1, resource2)))
                .contains(resource1);

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(asList(resource2, resource1)))
                .contains(resource1);

        FlowRef partialRef = FlowRef.of(null, "XYZ", null);

        Assertions.assertThat(RiRestParsers.getResourceSelector(partialRef).applyWithIO(emptyList()))
                .isEmpty();

        Assertions.assertThat(RiRestParsers.getResourceSelector(partialRef).applyWithIO(singletonList(resource2)))
                .contains(resource2);

        Assertions.assertThat(RiRestParsers.getResourceSelector(partialRef).applyWithIO(asList(resource1, resource2)))
                .contains(resource1);

        Assertions.assertThat(RiRestParsers.getResourceSelector(partialRef).applyWithIO(asList(resource2, resource1)))
                .contains(resource1);
    }
}
