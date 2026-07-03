package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class HttpFactorySupportTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void factoryNameIsExposed() {
        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .supplier(HttpFactorySupportTest::stubClient)
                .build();

        assertThat(factory.getFactoryName()).isEqualTo("TestFactory");
    }

    @Test
    public void propertiesAreEmptyByDefault() {
        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .supplier(HttpFactorySupportTest::stubClient)
                .build();

        assertThat(factory.getFactoryProperties()).isEmpty();
    }

    @Test
    public void propertiesAreExposed() {
        Property<String> prop1 = Property.of("prop1", "default1", Parser.onString(), Formatter.onString());
        Property<String> prop2 = Property.of("prop2", "default2", Parser.onString(), Formatter.onString());

        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .property(prop1)
                .property(prop2)
                .supplier(HttpFactorySupportTest::stubClient)
                .build();

        assertThat(factory.getFactoryProperties())
                .extracting(BaseProperty::getKey)
                .containsExactly("prop1", "prop2");
    }

    @Test
    public void createDelegatesToSupplier() {
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .supplier((s, c) -> {
                    supplierCalled.set(true);
                    return stubClient(s, c);
                })
                .build();

        factory.create(source, context);

        assertThat(supplierCalled).isTrue();
    }

    @Test
    public void createPassesSourceAndContextToSupplier() {
        AtomicReference<WebSource> capturedSource = new AtomicReference<>();
        AtomicReference<WebContext> capturedContext = new AtomicReference<>();

        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .supplier((s, c) -> {
                    capturedSource.set(s);
                    capturedContext.set(c);
                    return stubClient(s, c);
                })
                .build();

        factory.create(source, context);

        assertThat(capturedSource).hasValue(source);
        assertThat(capturedContext).hasValue(context);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void builderRejectsNullName() {
        assertThatNullPointerException().isThrownBy(() ->
                HttpFactorySupport.builder()
                        .name(null)
                        .supplier(HttpFactorySupportTest::stubClient)
                        .build()
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void builderRejectsNullSupplier() {
        assertThatNullPointerException().isThrownBy(() ->
                HttpFactorySupport.builder()
                        .name("TestFactory")
                        .supplier(null)
                        .build()
        );
    }

    @Test
    public void propertiesListIsImmutable() {
        Property<String> prop = Property.of("prop", "default", Parser.onString(), Formatter.onString());

        HttpFactorySupport factory = HttpFactorySupport.builder()
                .name("TestFactory")
                .property(prop)
                .supplier(HttpFactorySupportTest::stubClient)
                .build();

        assertThat(factory.getFactoryProperties()).isUnmodifiable();
    }

    private static HttpClient stubClient(WebSource source, WebContext context) {
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
}

