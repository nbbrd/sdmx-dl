package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

public class RequestTest {

    @Test
    public void testAccept() {
        List<Request> requests = Arrays.asList(
                DatabasesRequest.DEFAULT,
                FlowsRequest.DEFAULT,
                MetaRequest.builder().flowOf("EXR").build(),
                DataRequest.builder().flowOf("EXR").build(),
                DimensionsRequest.builder().flowOf("EXR").build(),
                AttributesRequest.builder().flowOf("EXR").build(),
                CodesRequest.builder().flowOf("EXR").concept("FREQ").build(),
                AvailabilityRequest.builder()
                        .flowOf("EXR")
                        .keyOf("all")
                        .dimension("FREQ")
                        .build());

        assertThat(requests)
                .extracting(request -> request.accept(NameVisitor.INSTANCE))
                .containsExactly(
                        "databases", "flows", "meta", "data", "dimensions", "attributes", "codes", "availability");
    }

    private enum NameVisitor implements RequestVisitor<String> {
        INSTANCE;

        @Override
        public String visitDatabases(@NonNull DatabasesRequest request) {
            return "databases";
        }

        @Override
        public String visitFlows(@NonNull FlowsRequest request) {
            return "flows";
        }

        @Override
        public String visitMeta(@NonNull MetaRequest request) {
            return "meta";
        }

        @Override
        public String visitData(@NonNull DataRequest request) {
            return "data";
        }

        @Override
        public String visitDimensions(@NonNull DimensionsRequest request) {
            return "dimensions";
        }

        @Override
        public String visitAttributes(@NonNull AttributesRequest request) {
            return "attributes";
        }

        @Override
        public String visitCodes(@NonNull CodesRequest request) {
            return "codes";
        }

        @Override
        public String visitAvailability(@NonNull AvailabilityRequest request) {
            return "availability";
        }
    }
}
