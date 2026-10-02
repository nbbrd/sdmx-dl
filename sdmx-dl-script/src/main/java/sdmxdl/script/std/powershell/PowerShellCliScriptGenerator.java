package sdmxdl.script.std.powershell;

import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.PowerShellScripts;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates PowerShell scripts that call the command-line tool.
 */
@DirectImpl
@ServiceProvider
public final class PowerShellCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.POWERSHELL_LANGUAGE, ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(PowerShellScripts.FILE_EXTENSION)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(PowerShellScripts::renderCli)
            .build();
}
