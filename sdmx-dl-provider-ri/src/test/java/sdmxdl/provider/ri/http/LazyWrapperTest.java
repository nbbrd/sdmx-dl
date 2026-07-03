package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyWrapperTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void clientIsNotCreatedUntilFirstUse() throws IOException {
        LazyDecoration decorator = new LazyDecoration();
        AtomicInteger createCount = new AtomicInteger(0);

        HttpFactory base = HttpFactorySupport.builder()
                .name("CountingFactory")
                .supplier((s, c) -> {
                    createCount.incrementAndGet();
                    return stubClient();
                })
                .build();

        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        assertThat(createCount).hasValue(0);

        client.getDescription();

        assertThat(createCount).hasValue(1);
    }

    @Test
    public void clientIsCreatedOnlyOnce() throws IOException {
        LazyDecoration decorator = new LazyDecoration();
        AtomicInteger createCount = new AtomicInteger(0);

        HttpFactory base = HttpFactorySupport.builder()
                .name("CountingFactory")
                .supplier((s, c) -> {
                    createCount.incrementAndGet();
                    return stubClient();
                })
                .build();

        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        client.getDescription();
        client.getDescription();

        assertThat(createCount).hasValue(1);
    }

    @Test
    public void decoratorHasNoProperties() {
        LazyDecoration decorator = new LazyDecoration();
        assertThat(decorator.getDecoratorProperties()).isEmpty();
    }

    private static HttpClient stubClient() {
        return new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "LazyCreated";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }
}

