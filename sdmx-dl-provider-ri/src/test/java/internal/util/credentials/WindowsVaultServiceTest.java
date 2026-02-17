package internal.util.credentials;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import sdmxdl.provider.ri.spi.VaultService;

import java.io.IOException;
import java.util.Arrays;

import static internal.util.credentials.WindowsVaultService.MAX_PASSWORD_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class WindowsVaultServiceTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testAddGet() throws IOException {
        String resource = "WinPasswordVaultTest1";
        String userName = "testuser";
        String password = "testpassword";

        VaultService x = new WindowsVaultService();

        x.storePassword(resource, userName, null);
        assertThat(x.loadPassword(resource, userName))
                .isNull();

        x.storePassword(resource, userName, password);
        assertThat(x.loadPassword(resource, userName))
                .isEqualTo(password);

        x.storePassword(resource, userName, null);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testFieldOverflow() throws IOException {
        String resource = "WinPasswordVaultTest2";
        String userName = "testuser";
        String password = repeat('A', MAX_PASSWORD_SIZE);
        String overflow = repeat('A', MAX_PASSWORD_SIZE + 1);

        VaultService x = new WindowsVaultService();

        x.storePassword(resource, userName, null);
        x.storePassword(resource, userName, password);
        assertThat(x.loadPassword(resource, userName)).isEqualTo(password);

        assertThatIOException().isThrownBy(() -> x.storePassword(resource, userName, overflow))
                .withMessageContaining("Field overflow");

        x.storePassword(resource, userName, null);
    }

    private static String repeat(char c, int count) {
        char[] result = new char[count];
        Arrays.fill(result, c);
        return new String(result);
    }
}
