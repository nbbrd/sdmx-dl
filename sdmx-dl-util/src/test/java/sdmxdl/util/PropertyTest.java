package sdmxdl.util;

import org.junit.Test;

import java.util.Properties;

import static nbbrd.io.text.Parser.onInteger;
import static nbbrd.io.text.Parser.onString;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.util.Property.get;

public class PropertyTest {

    @Test
    public void testGet() {
        Properties props = new Properties();

        assertThat(get("k1", null, props, onString())).isNull();
        assertThat(get("k1", "v1", props, onString())).isEqualTo("v1");
        props.put("k1", "v2");
        assertThat(get("k1", "v1", props, onString())).isEqualTo("v2");

        assertThat(get("k1", null, props, onInteger())).isNull();
        assertThat(get("k1", 1234, props, onInteger())).isEqualTo(1234);
        props.put("k1", 5678);
        assertThat(get("k1", 5678, props, onInteger())).isEqualTo(5678);
    }
}
