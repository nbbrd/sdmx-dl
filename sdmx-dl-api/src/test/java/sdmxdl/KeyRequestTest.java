package sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.FLOW_REF;

/**
 * @author Philippe Charles
 */
public class KeyRequestTest {

    @Test
    public void testDefaults() {
        KeyRequest request = KeyRequest.builder().flow(FLOW_REF).build();

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.NO_DATABASE);
        assertThat(request.getFlow()).isEqualTo(FLOW_REF);
        assertThat(request.getKey()).isEqualTo(Key.ALL);
        assertThat(request.getDetail()).isEqualTo(Detail.FULL);
        assertThat(request.getLanguages()).isEqualTo(Languages.ANY);
    }

    @Test
    public void testBuilderConvenience() {
        KeyRequest request = KeyRequest.builder()
                .databaseOf("db")
                .flowOf("NBB,XYZ,v2.0")
                .keyOf("M.BE.INDUSTRY")
                .detailOf("DATA_ONLY")
                .languagesOf("fr")
                .build();

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.parse("db"));
        assertThat(request.getFlow()).isEqualTo(FlowRef.parse("NBB,XYZ,v2.0"));
        assertThat(request.getKey()).isEqualTo(Key.parse("M.BE.INDUSTRY"));
        assertThat(request.getDetail()).isEqualTo(Detail.DATA_ONLY);
        assertThat(request.getLanguages()).isEqualTo(Languages.parse("fr"));
    }

    @Test
    public void testToQuery() {
        KeyRequest request = KeyRequest.builder()
                .flow(FLOW_REF)
                .keyOf("M.BE.INDUSTRY")
                .detail(Detail.NO_DATA)
                .build();

        assertThat(request.toQuery())
                .isEqualTo(Query.builder().key(Key.parse("M.BE.INDUSTRY")).detail(Detail.NO_DATA).build());
    }

    @Test
    public void testBuilderOf() {
        FlowRequest source = FlowRequest.builder()
                .databaseOf("db")
                .flow(FLOW_REF)
                .languagesOf("fr")
                .build();

        KeyRequest request = KeyRequest.builderOf(source).build();

        assertThat(request.getDatabase()).isEqualTo(source.getDatabase());
        assertThat(request.getFlow()).isEqualTo(source.getFlow());
        assertThat(request.getLanguages()).isEqualTo(source.getLanguages());
    }
}

