package sdmxdl.grpc;

import io.grpc.stub.StreamObserver;
import sdmxdl.Connection;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.stream.Stream;

import static sdmxdl.format.protobuf.ProtobufRepositories.*;

@lombok.AllArgsConstructor
class SdmxWebManagerService extends SdmxWebManagerGrpc.SdmxWebManagerImplBase {

    @lombok.NonNull
    private final SdmxWebManager manager;

    @Override
    public void getSources(Empty request, StreamObserver<SdmxWebSource> response) {
        manager.getSources()
                .values()
                .stream()
                .map(ProtobufSources::fromWebSource)
                .forEach(response::onNext);
        response.onCompleted();
    }

    @Override
    public void getMonitorReport(SourceRequest request, StreamObserver<MonitorReport> response) {
        try {
            response.onNext(ProtobufMonitors.fromMonitorReport(manager.getMonitorReport(request.getSource())));
            response.onCompleted();
        } catch (IOException ex) {
            response.onError(ex);
        }
    }

    @Override
    public void getFlows(SourceRequest request, StreamObserver<Dataflow> response) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            connection.getFlows()
                    .stream()
                    .map(ProtobufRepositories::fromDataflow)
                    .forEach(response::onNext);
            response.onCompleted();
        } catch (IOException ex) {
            response.onError(ex);
        }
    }

    @Override
    public void getFlow(FlowRequest request, StreamObserver<Dataflow> response) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            response.onNext(fromDataflow(connection.getFlow(DataflowRef.parse(request.getFlow()))));
            response.onCompleted();
        } catch (IOException ex) {
            response.onError(ex);
        }
    }

    @Override
    public void getStructure(FlowRequest request, StreamObserver<DataStructure> response) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            response.onNext(fromDataStructure(connection.getStructure(DataflowRef.parse(request.getFlow()))));
            response.onCompleted();
        } catch (IOException ex) {
            response.onError(ex);
        }
    }

    @Override
    public void getData(KeyRequest request, StreamObserver<DataSet> response) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            response.onNext(fromDataSet(connection.getData(getFlowRef(request), getDataQuery(request))));
            response.onCompleted();
        } catch (IOException ex) {
            response.onError(ex);
        }
    }

    @Override
    public void getDataStream(KeyRequest request, StreamObserver<Series> responseObserver) {
        try (Connection connection = manager.getConnection(request.getSource())) {
            try (Stream<sdmxdl.Series> stream = connection.getDataStream(getFlowRef(request), getDataQuery(request))) {
                stream.map(ProtobufRepositories::fromSeries).forEach(responseObserver::onNext);
            }
            responseObserver.onCompleted();
        } catch (IOException ex) {
            responseObserver.onError(ex);
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
