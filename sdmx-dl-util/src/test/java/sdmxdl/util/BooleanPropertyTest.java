package sdmxdl.util;

import org.junit.Test;
import sdmxdl.util.Property;

import java.util.Properties;

import static nbbrd.io.text.Parser.onInteger;
import static nbbrd.io.text.Parser.onString;
import static org.assertj.core.api.Assertions.assertThat;

public class BooleanPropertyTest {

    @Test
    public void testGet() {
        Properties props = new Properties();

        assertThat(new BooleanProperty("k1", true).get(props)).isTrue();
        assertThat(new BooleanProperty("k1", false).get(props)).isFalse();

        props.put("k1", Boolean.TRUE);
        assertThat(new BooleanProperty("k1", true).get(props)).isTrue();
        assertThat(new BooleanProperty("k1", false).get(props)).isTrue();

        props.put("k1", Boolean.FALSE);
        assertThat(new BooleanProperty("k1", true).get(props)).isFalse();
        assertThat(new BooleanProperty("k1", false).get(props)).isFalse();

        props.put("k1", "stuff");
        assertThat(new BooleanProperty("k1", true).get(props)).isTrue();
        assertThat(new BooleanProperty("k1", false).get(props)).isFalse();
    }
}
