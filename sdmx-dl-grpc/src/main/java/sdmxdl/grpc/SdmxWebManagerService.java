package sdmxdl.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.DataSet;
import sdmxdl.format.protobuf.DataStructure;
import sdmxdl.format.protobuf.Dataflow;
import sdmxdl.format.protobuf.Series;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@GrpcService
public class SdmxWebManagerService implements sdmxdl.grpc.SdmxWebManager {

    private final SdmxWebManager manager = GrpcWebFactory.loadManager();
    private final Languages languages = Languages.ANY;

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
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getFlow(FlowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataflow);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Uni<DataStructure> getStructure(FlowRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getStructure(FlowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataStructure);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @Override
    public Uni<DataSet> getData(KeyRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
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
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getFlows().stream())
                    .map(ProtobufRepositories::fromDataflow);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @Override
    public Multi<Series> getDataStream(KeyRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getData(getFlowRef(request), getDataQuery(request)).getData().stream())
                    .map(ProtobufRepositories::fromSeries);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    private FlowRef getFlowRef(KeyRequest request) {
        return FlowRef.parse(request.getFlow());
    }

    private Query getDataQuery(KeyRequest request) {
        return Query
                .builder()
                .key(Key.parse(request.getKey()))
                .detail(Detail.FULL)
                .build();
    }
}
