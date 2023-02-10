package sdmxdl.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "sdmx-dl-grpc",
        description = "Service that provides sdmx-dl over gRPC.",
        scope = CommandLine.ScopeType.INHERIT,
        sortOptions = false,
        mixinStandardHelpOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        headerHeading = "%n"
)
public class ServiceDemo implements Callable<Void> {

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ServiceDemo());
        cmd.execute(args);
    }

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Service port",
            defaultValue = "4567"
    )
    int port;

    @Override
    public Void call() throws Exception {
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

        return null;
    }
}
