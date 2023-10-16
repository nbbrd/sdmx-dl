package internal.sdmxdl.cli;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkingOptionsTest {

    @Nested
    class AutoProxyOptionTest {
        @Test
        @ClearEnvironmentVariable(key = "SDMXDL_NETWORKING_AUTOPROXY")
        public void testWithoutEnv() {
            Holder x = new Holder();
            assertThat(x.execute().isAutoProxy()).isFalse();
            assertThat(x.execute("--auto-proxy").isAutoProxy()).isTrue();
            assertThat(x.execute("--no-auto-proxy").isAutoProxy()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_AUTOPROXY", value = "false")
        public void testWithEnvFalse() {
            Holder x = new Holder();
            assertThat(x.execute().isAutoProxy()).isFalse();
            assertThat(x.execute("--auto-proxy").isAutoProxy()).isTrue();
            assertThat(x.execute("--no-auto-proxy").isAutoProxy()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_AUTOPROXY", value = "true")
        public void testWithEnvTrue() {
            Holder x = new Holder();
            assertThat(x.execute().isAutoProxy()).isTrue();
            assertThat(x.execute("--auto-proxy").isAutoProxy()).isTrue();
            assertThat(x.execute("--no-auto-proxy").isAutoProxy()).isFalse();
        }
    }

    @Nested
    class CurlBackendOptionTest {
        @Test
        @ClearEnvironmentVariable(key = "SDMXDL_NETWORKING_CURLBACKEND")
        public void testWithoutEnv() {
            Holder x = new Holder();
            assertThat(x.execute().isCurlBackend()).isFalse();
            assertThat(x.execute("--curl").isCurlBackend()).isTrue();
            assertThat(x.execute("--no-curl").isCurlBackend()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_CURLBACKEND", value = "false")
        public void testWithEnvFalse() {
            Holder x = new Holder();
            assertThat(x.execute().isCurlBackend()).isFalse();
            assertThat(x.execute("--curl").isCurlBackend()).isTrue();
            assertThat(x.execute("--no-curl").isCurlBackend()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_CURLBACKEND", value = "true")
        public void testWithEnvTrue() {
            Holder x = new Holder();
            assertThat(x.execute().isCurlBackend()).isTrue();
            assertThat(x.execute("--curl").isCurlBackend()).isTrue();
            assertThat(x.execute("--no-curl").isCurlBackend()).isFalse();
        }
    }

    @Nested
    class NoDefaultSslOptionTest {
        @Test
        @ClearEnvironmentVariable(key = "SDMXDL_NETWORKING_NODEFAULTSSL")
        public void testWithoutEnv() {
            Holder x = new Holder();
            assertThat(x.execute().isNoDefaultSsl()).isFalse();
            assertThat(x.execute("--no-default-ssl").isNoDefaultSsl()).isTrue();
            assertThat(x.execute("--default-ssl").isNoDefaultSsl()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_NODEFAULTSSL", value = "false")
        public void testWithEnvFalse() {
            Holder x = new Holder();
            assertThat(x.execute().isNoDefaultSsl()).isFalse();
            assertThat(x.execute("--no-default-ssl").isNoDefaultSsl()).isTrue();
            assertThat(x.execute("--default-ssl").isNoDefaultSsl()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_NODEFAULTSSL", value = "true")
        public void testWithEnvTrue() {
            Holder x = new Holder();
            assertThat(x.execute().isNoDefaultSsl()).isTrue();
            assertThat(x.execute("--no-default-ssl").isNoDefaultSsl()).isTrue();
            assertThat(x.execute("--default-ssl").isNoDefaultSsl()).isFalse();
        }
    }

    @Nested
    class NoSystemSslOptionTest {
        @Test
        @ClearEnvironmentVariable(key = "SDMXDL_NETWORKING_NOSYSTEMSSL")
        public void testWithoutEnv() {
            Holder x = new Holder();
            assertThat(x.execute().isNoSystemSsl()).isFalse();
            assertThat(x.execute("--no-system-ssl").isNoSystemSsl()).isTrue();
            assertThat(x.execute("--system-ssl").isNoSystemSsl()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_NOSYSTEMSSL", value = "false")
        public void testWithEnvFalse() {
            Holder x = new Holder();
            assertThat(x.execute().isNoSystemSsl()).isFalse();
            assertThat(x.execute("--no-system-ssl").isNoSystemSsl()).isTrue();
            assertThat(x.execute("--system-ssl").isNoSystemSsl()).isFalse();
        }

        @Test
        @SetEnvironmentVariable(key = "SDMXDL_NETWORKING_NOSYSTEMSSL", value = "true")
        public void testWithEnvTrue() {
            Holder x = new Holder();
            assertThat(x.execute().isNoSystemSsl()).isTrue();
            assertThat(x.execute("--no-system-ssl").isNoSystemSsl()).isTrue();
            assertThat(x.execute("--system-ssl").isNoSystemSsl()).isFalse();
        }
    }

    @CommandLine.Command
    static class Holder implements Callable<Void> {

        @CommandLine.Mixin
        NetworkingOptions options;

        @Override
        public Void call() {
            return null;
        }

        NetworkingOptions execute(String... args) {
            CommandLine cmd = new CommandLine(this);
            cmd.execute(args);
            return options;
        }
    }
}
