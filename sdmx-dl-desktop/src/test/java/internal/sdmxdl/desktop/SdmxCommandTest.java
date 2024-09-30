package internal.sdmxdl.desktop;

import org.junit.jupiter.api.Test;
import sdmxdl.CatalogRef;

import static org.assertj.core.api.Assertions.assertThat;

class SdmxCommandTest {

    @Test
    public void test() {
        assertThat(SdmxCommand
                .builder()
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .option("c", CatalogRef.parse("hello_world").toString())
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