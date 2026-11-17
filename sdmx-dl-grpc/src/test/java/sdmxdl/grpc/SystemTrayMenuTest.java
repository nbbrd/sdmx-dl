package sdmxdl.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class SystemTrayMenuTest {

    @Test
    void testGetBaseUri() {
        assertThat(SystemTrayMenu.getBaseUri("127.0.0.1", 4559)).isEqualTo(URI.create("http://127.0.0.1:4559/"));
        assertThat(SystemTrayMenu.getBaseUri("0.0.0.0", 4559)).isEqualTo(URI.create("http://localhost:4559/"));
        assertThat(SystemTrayMenu.getBaseUri("::", 4559)).isEqualTo(URI.create("http://localhost:4559/"));
        assertThat(SystemTrayMenu.getBaseUri("::1", 4559)).isEqualTo(URI.create("http://[::1]:4559/"));
        assertThat(SystemTrayMenu.getBaseUri("[::1]", 4559)).isEqualTo(URI.create("http://[::1]:4559/"));
    }

    @Test
    void testIsWindowsDarkTheme() {
        String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        assertThat(SystemTrayMenu.isWindowsDarkTheme(key + "\r\n    SystemUsesLightTheme    REG_DWORD    0x0\r\n"))
                .isTrue();
        assertThat(SystemTrayMenu.isWindowsDarkTheme(key + "\r\n    SystemUsesLightTheme    REG_DWORD    0x1\r\n"))
                .isFalse();
        assertThat(SystemTrayMenu.isWindowsDarkTheme("")).isFalse();
    }
}
