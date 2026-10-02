package sdmxdl.script.std.python;

import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.PythonScripts;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates Python scripts that call the command-line tool.
 */
@DirectImpl
@ServiceProvider
public final class PythonCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.PYTHON_LANGUAGE, ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(PythonScripts.FILE_EXTENSION)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(PythonScripts::renderCli)
            .build();
}
