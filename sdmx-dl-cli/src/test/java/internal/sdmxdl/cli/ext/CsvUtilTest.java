package internal.sdmxdl.cli.ext;

import nbbrd.io.text.Formatter;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvUtilTest {

    @Test
    public void testFromMap() {
        Formatter<Map<Integer, Boolean>> formatter = CsvUtil.fromMap(Formatter.onInteger(), Formatter.onBoolean(), ',', '=');

        Map<Integer, Boolean> data = new HashMap<>();
        data.put(1, true);
        data.put(7, false);
        assertThat(formatter.format(data))
                .isEqualTo("1=true,7=false");

        assertThat(formatter.format(Collections.emptyMap()))
                .isEqualTo("");
    }
}
