package sdmxdl.script.std.jupyter;

import internal.sdmxdl.script.PythonScripts;
import internal.sdmxdl.script.RestCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates Jupyter notebook cells that call the web service and load the result with pandas.
 */
@DirectImpl
@ServiceProvider
public final class JupyterRestScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("jupyter", ScriptTarget.REST_TRANSPORT))
            .scriptFileExtension(PythonScripts.FILE_EXTENSION)
            .scriptRequestTypes(RestCalls.SUPPORTED_TYPES)
            .renderer(PythonScripts::renderJupyterRest)
            .build();
}
