package sdmxdl.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import sdmxdl.Connection;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@GrpcService
public class SdmxWebManagerService implements sdmxdl.grpc.SdmxWebManager {

    private final SdmxWebManager manager = GrpcWebFactory.loadManager();

    @Override
    public Uni<MonitorReport> getMonitorReport(SourceRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.getMonitorReport(request.getSource()))
                    .map(ProtobufMonitors::fromMonitorReport);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Uni<Dataflow> getFlow(FlowRequest request) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            return Uni.createFrom()
                    .item(connection.getFlow(DataflowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataflow);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Uni<DataStructure> getStructure(FlowRequest request) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            return Uni.createFrom()
                    .item(connection.getStructure(DataflowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataStructure);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Uni<DataSet> getData(KeyRequest request) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            return Uni.createFrom()
                    .item(connection.getData(getFlowRef(request), getDataQuery(request)))
                    .map(ProtobufRepositories::fromDataSet);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Multi<SdmxWebSource> getSources(Empty request) {
        return Multi.createFrom().items(manager.getSources()
                .values()
                .stream()
                .map(ProtobufSources::fromWebSource));
    }

    @Override
    public Multi<Dataflow> getFlows(SourceRequest request) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            return Multi.createFrom()
                    .items(connection.getFlows().stream())
                    .map(ProtobufRepositories::fromDataflow);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @Override
    public Multi<Series> getDataStream(KeyRequest request) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            return Multi.createFrom()
                    .items(connection.getData(getFlowRef(request), getDataQuery(request)).getData().stream())
                    .map(ProtobufRepositories::fromSeries);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    private DataflowRef getFlowRef(KeyRequest request) {
        return DataflowRef.parse(request.getFlow());
    }

    private sdmxdl.DataQuery getDataQuery(KeyRequest request) {
        return sdmxdl.DataQuery
                .builder()
                .key(Key.parse(request.getKey()))
                .detail(sdmxdl.DataDetail.FULL)
                .build();
    }
}
