package sdmxdl.script.std.bash;

import internal.sdmxdl.script.BashScripts;
import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates bash scripts that call the command-line tool.
 */
@DirectImpl
@ServiceProvider
public final class BashCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.BASH_LANGUAGE, ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(BashScripts.FILE_EXTENSION)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(BashScripts::renderCli)
            .build();
}
