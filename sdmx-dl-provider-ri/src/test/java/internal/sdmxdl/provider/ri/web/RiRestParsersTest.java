package internal.sdmxdl.provider.ri.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;

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

        DataflowRef fullRef1 = DataflowRef.of("NBB", "XYZ", "v1.0");
        Dataflow resource1 = Dataflow.builder().ref(fullRef1).structureRef(STRUCT_REF).label("flow1").build();

        DataflowRef fullRef2 = DataflowRef.of("NBB", "XYZ", "v2.0");
        Dataflow resource2 = Dataflow.builder().ref(fullRef2).structureRef(STRUCT_REF).label("flow2").build();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(emptyList()))
                .isEmpty();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(singletonList(resource2)))
                .isEmpty();

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(asList(resource1, resource2)))
                .contains(resource1);

        Assertions.assertThat(RiRestParsers.getResourceSelector(fullRef1).applyWithIO(asList(resource2, resource1)))
                .contains(resource1);

        DataflowRef partialRef = DataflowRef.of(null, "XYZ", null);

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
