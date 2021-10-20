package internal.sdmxdl.ri.web;

import org.junit.jupiter.api.Test;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;

import static internal.sdmxdl.ri.web.RiRestParsers.getResourceSelector;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.samples.RepoSamples.GOOD_STRUCT_REF;

public class RiRestParsersTest {

    @Test
    public void testGetResourceSelector() {
        assertThatNullPointerException()
                .isThrownBy(() -> getResourceSelector(null));

        DataflowRef fullRef1 = DataflowRef.of("NBB", "XYZ", "v1.0");
        Dataflow resource1 = Dataflow.of(fullRef1, GOOD_STRUCT_REF, "flow1");

        DataflowRef fullRef2 = DataflowRef.of("NBB", "XYZ", "v2.0");
        Dataflow resource2 = Dataflow.of(fullRef2, GOOD_STRUCT_REF, "flow2");

        assertThat(getResourceSelector(fullRef1).apply(emptyList()))
                .isEmpty();

        assertThat(getResourceSelector(fullRef1).apply(asList(resource2)))
                .isEmpty();

        assertThat(getResourceSelector(fullRef1).apply(asList(resource1, resource2)))
                .contains(resource1);

        assertThat(getResourceSelector(fullRef1).apply(asList(resource2, resource1)))
                .contains(resource1);

        DataflowRef partialRef = DataflowRef.of(null, "XYZ", null);

        assertThat(getResourceSelector(partialRef).apply(emptyList()))
                .isEmpty();

        assertThat(getResourceSelector(partialRef).apply(asList(resource2)))
                .contains(resource2);

        assertThat(getResourceSelector(partialRef).apply(asList(resource1, resource2)))
                .contains(resource1);

        assertThat(getResourceSelector(partialRef).apply(asList(resource2, resource1)))
                .contains(resource1);
    }
}
