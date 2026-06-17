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

public class HttpClientDecoratorSupportTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void decoratedFactoryNameCombinesBothNames() {
        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("MyDecorator")
                .superFactory(HttpClientFactory::create)
                .build();

        HttpClientFactory base = stubFactory("BaseFactory");
        HttpClientFactory decorated = decorator.decorate(base);

        assertThat(decorated.getFactoryName()).isEqualTo("BaseFactory with MyDecorator");
    }

    @Test
    public void decoratedFactoryCombinesProperties() {
        Property<String> decoratorProp = Property.of("dec.prop", "default", Parser.onString(), Formatter.onString());
        Property<String> factoryProp = Property.of("fac.prop", "default", Parser.onString(), Formatter.onString());

        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("MyDecorator")
                .property(decoratorProp)
                .superFactory(HttpClientFactory::create)
                .build();

        HttpClientFactory base = stubFactory("BaseFactory", Collections.singletonList(factoryProp));
        HttpClientFactory decorated = decorator.decorate(base);

        assertThat(decorated.getFactoryProperties())
                .extracting(BaseProperty::getKey)
                .containsExactly("fac.prop", "dec.prop");
    }

    @Test
    public void decoratedFactoryDelegatesCreateToSuperFactory() {
        AtomicBoolean superFactoryCalled = new AtomicBoolean(false);

        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("MyDecorator")
                .superFactory((d, s, c) -> {
                    superFactoryCalled.set(true);
                    return d.create(s, c);
                })
                .build();

        HttpClientFactory decorated = decorator.decorate(stubFactory("BaseFactory"));
        decorated.create(source, context);

        assertThat(superFactoryCalled).isTrue();
    }

    @Test
    public void decoratorPropertiesAreEmptyByDefault() {
        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("Empty")
                .superFactory(HttpClientFactory::create)
                .build();

        assertThat(decorator.getDecoratorProperties()).isEmpty();
    }

    @Test
    public void decoratorNameIsExposed() {
        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("TestName")
                .superFactory(HttpClientFactory::create)
                .build();

        assertThat(decorator.getDecoratorName()).isEqualTo("TestName");
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    @Test
    public void decorateRejectsNullFactory() {
        HttpClientDecoratorSupport decorator = HttpClientDecoratorSupport.builder()
                .name("MyDecorator")
                .superFactory(HttpClientFactory::create)
                .build();

        assertThatNullPointerException().isThrownBy(() -> decorator.decorate(null));
    }

    @Test
    public void chainingMultipleDecorators() {
        HttpClientDecoratorSupport first = HttpClientDecoratorSupport.builder()
                .name("First")
                .superFactory(HttpClientFactory::create)
                .build();

        HttpClientDecoratorSupport second = HttpClientDecoratorSupport.builder()
                .name("Second")
                .superFactory(HttpClientFactory::create)
                .build();

        HttpClientFactory base = stubFactory("Base");
        HttpClientFactory decorated = second.decorate(first.decorate(base));

        assertThat(decorated.getFactoryName()).isEqualTo("Base with First with Second");
    }

    static HttpClientFactory stubFactory(String name) {
        return HttpClientFactorySupport.builder()
                .name(name)
                .supplier((s, c) -> stubClient(name))
                .build();
    }

    static HttpClientFactory stubFactory(String name, List<? extends BaseProperty> properties) {
        HttpClientFactorySupport.Builder builder = HttpClientFactorySupport.builder()
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



