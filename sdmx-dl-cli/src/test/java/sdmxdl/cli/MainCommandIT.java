package sdmxdl.cli;

import _test.ShadedJarLauncher;
import org.junit.jupiter.api.Test;
import sdmxdl.About;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static nbbrd.io.sys.ProcessReader.readToString;
import static org.assertj.core.api.Assertions.assertThat;

public class MainCommandIT {

    @Test
    public void testHelp() throws IOException {
        assertThat(ShadedJarLauncher.builder().build().readString())
                .contains("data", "meta", "list", "check", "setup");
    }

    @Test
    public void testVersion() throws IOException {
        assertThat(ShadedJarLauncher.builder().build().readString("--version"))
                .contains(About.NAME, About.VERSION);
    }
}
