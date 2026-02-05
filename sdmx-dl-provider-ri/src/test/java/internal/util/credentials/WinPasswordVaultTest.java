package internal.util.credentials;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.Arrays;

import static internal.util.credentials.WinPasswordVault.MAX_PASSWORD_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class WinPasswordVaultTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testAddGet() throws IOException {
        WinPasswordVault.PasswordCredential credential
                = new WinPasswordVault.PasswordCredential("WinPasswordVaultTest1", "testuser", "testpassword".toCharArray());

        try (WinPasswordVault vault = WinPasswordVault.open()) {
            vault.invalidate(credential.getResource());
            assertThat(vault.get(credential.getResource()))
                    .isNull();

            vault.add(credential);
            assertThat(vault.get(credential.getResource()))
                    .isEqualTo(credential);

            vault.invalidate(credential.getResource());
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testFieldOverflow() throws IOException {
        String resource = "WinPasswordVaultTest2";
        String user = "testuser";

        WinPasswordVault.PasswordCredential max
                = new WinPasswordVault.PasswordCredential(resource, user, repeat('A', MAX_PASSWORD_SIZE));

        WinPasswordVault.PasswordCredential overflow
                = new WinPasswordVault.PasswordCredential(resource, user, repeat('A', MAX_PASSWORD_SIZE + 1));

        try (WinPasswordVault vault = WinPasswordVault.open()) {
            vault.invalidate(resource);
            vault.add(max);
            assertThat(vault.get(resource)).isEqualTo(max);

            vault.invalidate(resource);
            vault.add(overflow);
            assertThatIOException().isThrownBy(() -> vault.get(resource))
                    .withMessageContaining("Field overflow");

            vault.invalidate(max.getResource());
        }
    }

    private static char[] repeat(char c, int count) {
        char[] result = new char[count];
        Arrays.fill(result, c);
        return result;
    }
}
