package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Philippe Charles
 */
public class FlowsRequestTest {

    @Test
    public void testDefaults() {
        FlowsRequest request = FlowsRequest.DEFAULT;

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.NO_DATABASE);
        assertThat(request.getLanguages()).isEqualTo(Languages.ANY);
    }

    @Test
    public void testBuilderConvenience() {
        FlowsRequest request =
                FlowsRequest.builder().databaseOf("db").languagesOf("fr").build();

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.parse("db"));
        assertThat(request.getLanguages()).isEqualTo(Languages.parse("fr"));
    }
}
