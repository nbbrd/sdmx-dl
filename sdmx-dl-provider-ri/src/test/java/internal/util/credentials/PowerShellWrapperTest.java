package internal.util.credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class PowerShellWrapperTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testExecToString() throws Exception {
        assertThat(PowerShellWrapper.execToString("echo 'é€'", Collections.emptyMap(), Duration.ofSeconds(30)))
                .isEqualTo("é€");

        long start = System.nanoTime();
        assertThatIOException()
                .isThrownBy(() -> PowerShellWrapper.execToString(
                        "Start-Sleep -Seconds 60", Collections.emptyMap(), Duration.ofSeconds(3)))
                .withMessageContaining("No answer from PowerShell");
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(30));
    }
}
