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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class HttpDecorationSupportTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void decoratedFactoryNameCombinesBothNames() {
        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("MyDecorator")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        HttpFactory base = stubFactory("BaseFactory");
        HttpFactory decorated = decorator.decorate(base);

        assertThat(decorated.getFactoryName()).isEqualTo("BaseFactory with MyDecorator");
    }

    @Test
    public void decoratedFactoryCombinesProperties() {
        Property<String> decoratorProp = Property.of("dec.prop", "default", Parser.onString(), Formatter.onString());
        Property<String> factoryProp = Property.of("fac.prop", "default", Parser.onString(), Formatter.onString());

        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("MyDecorator")
                .property(decoratorProp)
                .superFactory(HttpFactory::createHttpClient)
                .build();

        HttpFactory base = stubFactory("BaseFactory", Collections.singletonList(factoryProp));
        HttpFactory decorated = decorator.decorate(base);

        assertThat(decorated.getHttpClientProperties())
                .extracting(BaseProperty::getKey)
                .containsExactly("fac.prop", "dec.prop");
    }

    @Test
    public void decoratedFactoryDelegatesCreateToSuperFactory() {
        AtomicBoolean superFactoryCalled = new AtomicBoolean(false);

        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("MyDecorator")
                .superFactory((d, s, c) -> {
                    superFactoryCalled.set(true);
                    return d.createHttpClient(s, c);
                })
                .build();

        HttpFactory decorated = decorator.decorate(stubFactory("BaseFactory"));
        decorated.createHttpClient(source, context);

        assertThat(superFactoryCalled).isTrue();
    }

    @Test
    public void decoratorPropertiesAreEmptyByDefault() {
        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("Empty")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        assertThat(decorator.getDecoratorProperties()).isEmpty();
    }

    @Test
    public void decoratorNameIsExposed() {
        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("TestName")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        assertThat(decorator.getDecoratorName()).isEqualTo("TestName");
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    @Test
    public void decorateRejectsNullFactory() {
        HttpDecorationSupport decorator = HttpDecorationSupport.builder()
                .name("MyDecorator")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        assertThatNullPointerException().isThrownBy(() -> decorator.decorate(null));
    }

    @Test
    public void chainingMultipleDecorators() {
        HttpDecorationSupport first = HttpDecorationSupport.builder()
                .name("First")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        HttpDecorationSupport second = HttpDecorationSupport.builder()
                .name("Second")
                .superFactory(HttpFactory::createHttpClient)
                .build();

        HttpFactory base = stubFactory("Base");
        HttpFactory decorated = second.decorate(first.decorate(base));

        assertThat(decorated.getFactoryName()).isEqualTo("Base with First with Second");
    }

    static HttpFactory stubFactory(String name) {
        return HttpFactorySupport.builder()
                .name(name)
                .supplier((s, c) -> stubClient(name))
                .build();
    }

    static HttpFactory stubFactory(String name, List<? extends BaseProperty> properties) {
        HttpFactorySupport.Builder builder = HttpFactorySupport.builder()
                .name(name)
                .supplier((s, c) -> stubClient(name));
        properties.forEach(builder::property);
        return builder.build();
    }

    private static HttpClient stubClient(String name) {
        return new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return name;
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }
}



