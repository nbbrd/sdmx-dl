package sdmxdl.script.std.powershell;

import internal.sdmxdl.script.PowerShellScripts;
import internal.sdmxdl.script.RestCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates PowerShell scripts that call the web service.
 */
@DirectImpl
@ServiceProvider
public final class PowerShellRestScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("powershell", ScriptTarget.REST_TRANSPORT))
            .scriptFileExtension(PowerShellScripts.FILE_EXTENSION)
            .scriptRequestTypes(RestCalls.SUPPORTED_TYPES)
            .renderer(PowerShellScripts::renderRest)
            .build();
}
