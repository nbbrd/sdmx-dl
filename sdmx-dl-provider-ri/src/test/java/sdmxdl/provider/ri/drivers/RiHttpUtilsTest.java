package sdmxdl.provider.ri.drivers;

import nbbrd.io.text.BaseProperty;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.http.DumpingHttpClientDecorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.provider.ri.drivers.RiHttpUtils.DEFAULT_HTTP_FACTORY;

public class RiHttpUtilsTest {

    @Test
    public void testFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> DEFAULT_HTTP_FACTORY.create(null, null));
    }

    @Test
    public void testDefaultFactory() {
        assertThat(DEFAULT_HTTP_FACTORY.getFactoryName())
                .isEqualTo("UrlConnectionHttpClientFactory with Lazy with Dumping with ByteCounting");

        assertThat(DEFAULT_HTTP_FACTORY.getFactoryProperties())
                .hasSize(6)
                .extracting(BaseProperty::getKey)
                .contains(DumpingHttpClientDecorator.DUMP_FOLDER_PROPERTY.getKey());
    }
}