package sdmxdl.util;

import org.junit.Test;

import java.util.Properties;

import static nbbrd.io.text.Parser.onInteger;
import static nbbrd.io.text.Parser.onString;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertyTest {

    @Test
    public void testGet() {
        Properties props = new Properties();

        assertThat(new Property<>("k1", null, onString()).get(props)).isNull();
        assertThat(new Property<>("k1", "v1", onString()).get(props)).isEqualTo("v1");
        props.put("k1", "v2");
        assertThat(new Property<>("k1", "v1", onString()).get(props)).isEqualTo("v2");

        assertThat(new Property<>("k1", null, onInteger()).get(props)).isNull();
        assertThat(new Property<>("k1", 1234, onInteger()).get(props)).isEqualTo(1234);
        props.put("k1", 5678);
        assertThat(new Property<>("k1", 1234, onInteger()).get(props)).isEqualTo(5678);
    }
}
