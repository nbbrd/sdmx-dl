package sdmxdl.util;

import org.junit.Test;

import java.util.Properties;

import static nbbrd.io.text.Parser.onInteger;
import static nbbrd.io.text.Parser.onString;
import static org.assertj.core.api.Assertions.assertThat;

public class LongPropertyTest {

    @Test
    public void testGet() {
        Properties props = new Properties();

        assertThat(new LongProperty("k1", 123L).get(props)).isEqualTo(123L);

        props.put("k1", 123L);
        assertThat(new LongProperty("k1", 123L).get(props)).isEqualTo(123L);

        props.put("k1", 456L);
        assertThat(new LongProperty("k1", 123L).get(props)).isEqualTo(456L);

        props.put("k1", "stuff");
        assertThat(new LongProperty("k1", 123L).get(props)).isEqualTo(123L);
    }
}
