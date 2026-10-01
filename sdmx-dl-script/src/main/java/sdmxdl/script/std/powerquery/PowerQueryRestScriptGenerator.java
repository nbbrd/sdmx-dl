package sdmxdl.script.std.powerquery;

import internal.sdmxdl.script.PowerQueryScripts;
import internal.sdmxdl.script.RestCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates Power Query M scripts that call the web service, to be used in Excel or Power BI.
 */
@DirectImpl
@ServiceProvider
public final class PowerQueryRestScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("powerquery", ScriptTarget.REST_TRANSPORT))
            .scriptFileExtension(PowerQueryScripts.FILE_EXTENSION)
            .scriptRequestTypes(RestCalls.SUPPORTED_TYPES)
            .renderer(PowerQueryScripts::renderRest)
            .build();
}
