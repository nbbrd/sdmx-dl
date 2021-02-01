package sdmxdl.cli;

import org.junit.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class MainCommandTest {

    @Test
    public void test() {
        CommandLine cmd = new CommandLine(new MainCommand());

        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out));

        StringWriter err = new StringWriter();
        cmd.setErr(new PrintWriter(err));

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(out.toString()).isNotEmpty().contains("data", "meta", "list", "ping", "setup");
        assertThat(err.toString()).isEmpty();
    }
}
