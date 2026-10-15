package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static sdmxdl.HasSearch.*;

import org.junit.jupiter.api.Test;

public class HasSearchTest {

    @Test
    public void testDefaultIsAutoLimit() {
        assertThat(FlowsRequest.DEFAULT.getMaxResults()).isEqualTo(AUTO_LIMIT);
    }

    @Test
    public void testGetEffectiveMaxResults() {
        assertThat(request(NO_QUERY, AUTO_LIMIT).getEffectiveMaxResults()).isEqualTo(Integer.MAX_VALUE);
        assertThat(request("abc", AUTO_LIMIT).getEffectiveMaxResults()).isEqualTo(DEFAULT_SEARCH_LIMIT);

        assertThat(request(NO_QUERY, NO_LIMIT).getEffectiveMaxResults()).isEqualTo(Integer.MAX_VALUE);
        assertThat(request("abc", NO_LIMIT).getEffectiveMaxResults()).isEqualTo(Integer.MAX_VALUE);

        assertThat(request(NO_QUERY, 3).getEffectiveMaxResults()).isEqualTo(3);
        assertThat(request("abc", 3).getEffectiveMaxResults()).isEqualTo(3);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> request(NO_QUERY, -2).getEffectiveMaxResults());
    }

    @Test
    public void testGetEffectiveOffset() {
        assertThat(FlowsRequest.DEFAULT.getEffectiveOffset()).isEqualTo(NO_OFFSET);
        assertThat(FlowsRequest.builder().offset(3).build().getEffectiveOffset())
                .isEqualTo(3);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FlowsRequest.builder().offset(-1).build().getEffectiveOffset());
    }

    private static FlowsRequest request(String query, int maxResults) {
        return FlowsRequest.builder().query(query).maxResults(maxResults).build();
    }
}
