package sdmxdl.script.std.bash;

import internal.sdmxdl.script.BashScripts;
import internal.sdmxdl.script.RestCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates bash scripts that call the web service.
 */
@DirectImpl
@ServiceProvider
public final class BashRestScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("bash", ScriptTarget.REST_TRANSPORT))
            .scriptFileExtension(BashScripts.FILE_EXTENSION)
            .scriptRequestTypes(RestCalls.SUPPORTED_TYPES)
            .renderer(BashScripts::renderRest)
            .build();
}
