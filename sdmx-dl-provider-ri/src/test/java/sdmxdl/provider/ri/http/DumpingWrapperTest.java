package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DumpingWrapperTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void clientIsNotWrappedWhenDumpFolderIsNotSet() {
        DumpingDecoration decorator = new DumpingDecoration();

        HttpClient stubClient = stubClient();
        HttpFactory base = stubFactory(stubClient);
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        assertThat(client).isSameAs(stubClient);
    }

    @Test
    public void clientIsWrappedWhenDumpFolderIsSet(@TempDir Path tempDir) {
        DumpingDecoration decorator = new DumpingDecoration();

        WebSource sourceWithDump = source.toBuilder()
                .property(DumpingDecoration.DUMP_FOLDER_PROPERTY.getKey(), tempDir.toString())
                .build();

        HttpClient stubClient = stubClient();
        HttpFactory base = stubFactory(stubClient);
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(sourceWithDump, context);

        assertThat(client).isNotSameAs(stubClient);
    }

    @Test
    public void dumpFolderPropertyHasExpectedKey() {
        assertThat(DumpingDecoration.DUMP_FOLDER_PROPERTY.getKey())
                .startsWith("sdmxdl.driver.")
                .endsWith(".dumpFolder");
    }

    @Test
    public void decoratorExposesProperty() {
        DumpingDecoration decorator = new DumpingDecoration();
        assertThat(decorator.getDecoratorProperties())
                .hasSize(1)
                .first()
                .satisfies(p -> assertThat(p.getKey()).isEqualTo(DumpingDecoration.DUMP_FOLDER_PROPERTY.getKey()));
    }

    @Test
    public void eventsAreReportedWhenDumpingWithListener(@TempDir Path tempDir) {
        DumpingDecoration decorator = new DumpingDecoration();
        List<String> events = new ArrayList<>();

        WebSource sourceWithDump = source.toBuilder()
                .property(DumpingDecoration.DUMP_FOLDER_PROPERTY.getKey(), tempDir.toString())
                .build();

        WebContext contextWithListener = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(marker + ": " + message))
                .build();

        HttpFactory base = stubFactory(stubClient());
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(sourceWithDump, contextWithListener);

        assertThat(client).isNotSameAs(stubClient());
    }

    private static HttpClient stubClient() {
        return new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "StubClient";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static HttpFactory stubFactory(HttpClient client) {
        return HttpFactorySupport.builder()
                .name("StubFactory")
                .supplier((s, c) -> client)
                .build();
    }
}

