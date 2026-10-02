package sdmxdl.script.std.jupyter;

import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.PythonScripts;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates Jupyter notebook cells that call the command-line tool and load the result with pandas.
 */
@DirectImpl
@ServiceProvider
public final class JupyterCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.JUPYTER_LANGUAGE, ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(PythonScripts.FILE_EXTENSION)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(PythonScripts::renderJupyterCli)
            .build();
}
