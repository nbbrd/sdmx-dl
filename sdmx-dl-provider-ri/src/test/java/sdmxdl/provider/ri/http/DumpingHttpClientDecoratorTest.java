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

public class DumpingHttpClientDecoratorTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void clientIsNotWrappedWhenDumpFolderIsNotSet() {
        DumpingHttpClientDecorator decorator = new DumpingHttpClientDecorator();

        HttpClient stubClient = stubClient();
        HttpClientFactory base = stubFactory(stubClient);
        HttpClientFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        assertThat(client).isSameAs(stubClient);
    }

    @Test
    public void clientIsWrappedWhenDumpFolderIsSet(@TempDir Path tempDir) {
        DumpingHttpClientDecorator decorator = new DumpingHttpClientDecorator();

        WebSource sourceWithDump = source.toBuilder()
                .property(DumpingHttpClientDecorator.DUMP_FOLDER_PROPERTY.getKey(), tempDir.toString())
                .build();

        HttpClient stubClient = stubClient();
        HttpClientFactory base = stubFactory(stubClient);
        HttpClientFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(sourceWithDump, context);

        assertThat(client).isNotSameAs(stubClient);
    }

    @Test
    public void dumpFolderPropertyHasExpectedKey() {
        assertThat(DumpingHttpClientDecorator.DUMP_FOLDER_PROPERTY.getKey())
                .startsWith("sdmxdl.driver.")
                .endsWith(".dumpFolder");
    }

    @Test
    public void decoratorExposesProperty() {
        DumpingHttpClientDecorator decorator = new DumpingHttpClientDecorator();
        assertThat(decorator.getDecoratorProperties())
                .hasSize(1)
                .first()
                .satisfies(p -> assertThat(p.getKey()).isEqualTo(DumpingHttpClientDecorator.DUMP_FOLDER_PROPERTY.getKey()));
    }

    @Test
    public void eventsAreReportedWhenDumpingWithListener(@TempDir Path tempDir) {
        DumpingHttpClientDecorator decorator = new DumpingHttpClientDecorator();
        List<String> events = new ArrayList<>();

        WebSource sourceWithDump = source.toBuilder()
                .property(DumpingHttpClientDecorator.DUMP_FOLDER_PROPERTY.getKey(), tempDir.toString())
                .build();

        WebContext contextWithListener = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(marker + ": " + message))
                .build();

        HttpClientFactory base = stubFactory(stubClient());
        HttpClientFactory decorated = decorator.decorate(base);
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

    private static HttpClientFactory stubFactory(HttpClient client) {
        return HttpClientFactorySupport.builder()
                .name("StubFactory")
                .supplier((s, c) -> client)
                .build();
    }
}

