package sdmxdl.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServiceDemo {

    public static void main(String[] args) throws IOException {

        int port = 4567;

        Server server = ServerBuilder
                .forPort(port)
                .addService(new SdmxWebManagerService(GrpcWebFactory.loadManager()))
                .intercept(new LocalhostOnly())
                .build();
        server.start();

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .build();

        SdmxWebManagerGrpc.SdmxWebManagerBlockingStub stub
                = SdmxWebManagerGrpc.newBlockingStub(channel);

        try {
            System.out.println(stub.getMonitorReport(SourceRequest.newBuilder().setSource("ECB").build()));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        channel.shutdown();

        server.shutdown();
    }
}
