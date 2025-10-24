package internal.sdmxdl.desktop;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRef;

import static org.assertj.core.api.Assertions.assertThat;

class SdmxCommandTest {

    @Test
    public void testBuilder() {
        assertThat(SdmxCommand
                .builder()
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .option("d", DatabaseRef.parse("hello_world").toString())
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all -d hello_world");

        assertThat(SdmxCommand
                .builder()
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .option("d", null)
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all");
    }

    @Test
    public void testBuilderOf() {
        assertThat(SdmxCommand
                .builderOf(DatabaseRef.parse("hello_world"))
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all -d hello_world");

        assertThat(SdmxCommand
                .builderOf(DatabaseRef.NO_DATABASE)
                .parameter("fetch")
                .parameter("keys")
                .parameter("all")
                .build()
                .toText())
                .isEqualTo("sdmx-dl fetch keys all");
    }
}