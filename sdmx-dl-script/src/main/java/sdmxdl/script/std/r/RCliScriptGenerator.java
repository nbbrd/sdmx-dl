package sdmxdl.script.std.r;

import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.RScripts;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates R scripts that call the command-line tool.
 */
@DirectImpl
@ServiceProvider
public final class RCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("r", ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(RScripts.FILE_EXTENSION)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(RScripts::renderCli)
            .build();
}
