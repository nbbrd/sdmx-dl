package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

@lombok.experimental.UtilityClass
public class ConnectionSupport {

    public static @NonNull Flow getFlowFromFlows(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Connection connection, @NonNull HasMarker source) throws IOException, IllegalArgumentException {
        return connection
                .getFlows(database)
                .stream()
                .filter(flowRef::containsRef)
                .findFirst()
                .orElseThrow(() -> CommonSdmxExceptions.missingFlow(source, flowRef));
    }

    public static @NonNull DataSet getDataSetFromStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query, @NonNull Connection connection) throws IOException, IllegalArgumentException {
        try (Stream<Series> stream = connection.getDataStream(database, flowRef, query)) {
            return stream.collect(DataSet.toDataSet(flowRef, query));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
}
