package internal.util.credentials;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class WinPasswordVaultTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void test() throws IOException {
        WinPasswordVault.PasswordCredential credential
                = new WinPasswordVault.PasswordCredential("WinPasswordVaultTest", "testuser", "testpassword".toCharArray());

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
}
