package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.stream.Stream;

@lombok.experimental.UtilityClass
public class ConnectionSupport {

    public static @NonNull Flow getFlowFromFlows(@NonNull FlowRef flowRef, @NonNull Connection connection, @NonNull HasMarker source) throws IOException, IllegalArgumentException {
        return connection
                .getFlows()
                .stream()
                .filter(flowRef::containsRef)
                .findFirst()
                .orElseThrow(() -> CommonSdmxExceptions.missingFlow(source, flowRef));
    }

    public static @NonNull DataSet getDataSetFromStream(@NonNull FlowRef flowRef, @NonNull Query query, @NonNull Connection connection) throws IOException, IllegalArgumentException {
        try (Stream<Series> stream = connection.getDataStream(flowRef, query)) {
            return stream.collect(DataSet.toDataSet(flowRef, query));
        }
    }
}
