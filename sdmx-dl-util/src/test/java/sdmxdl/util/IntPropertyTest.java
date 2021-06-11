package sdmxdl.util;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class IntPropertyTest {

    @Test
    public void testGet() {
        Properties props = new Properties();

        assertThat(new IntProperty("k1", 123).get(props)).isEqualTo(123);

        props.put("k1", 123);
        assertThat(new IntProperty("k1", 123).get(props)).isEqualTo(123);

        props.put("k1", 456);
        assertThat(new IntProperty("k1", 123).get(props)).isEqualTo(456);

        props.put("k1", "stuff");
        assertThat(new IntProperty("k1", 123).get(props)).isEqualTo(123);
    }
}
