package internal.sdmxdl.desktop;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRef;

import static org.assertj.core.api.Assertions.assertThat;

class SdmxCommandTest {

    @Test
    public void test() {
        assertThat(SdmxCommand
                .builder()
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .option("c", DatabaseRef.parse("hello_world").toString())
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all -c hello_world");

        assertThat(SdmxCommand
                .builder()
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .option("c", null)
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all");
    }
}