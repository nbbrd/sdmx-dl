package sdmxdl.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class ServiceDemo {

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 4567;

        Server server = ServerBuilder
                .forPort(port)
                .addService(new SdmxWebManagerService(SdmxWebFactory.create()))
                .addService(ProtoReflectionService.newInstance())
                .intercept(new LocalhostOnly())
                .build();
        server.start();

        System.out.println("Server available at localhost:" + port + "");
        System.out.println("Press Ctrl+C to stop");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down web server");
            server.shutdown();
        }));

        server.awaitTermination();
    }
}
