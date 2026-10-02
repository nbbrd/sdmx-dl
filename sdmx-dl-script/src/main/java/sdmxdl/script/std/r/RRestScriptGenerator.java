package sdmxdl.script.std.r;

import internal.sdmxdl.script.RScripts;
import internal.sdmxdl.script.RestCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates R scripts that call the web service.
 */
@DirectImpl
@ServiceProvider
public final class RRestScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.R_LANGUAGE, ScriptTarget.REST_TRANSPORT))
            .scriptFileExtension(RScripts.FILE_EXTENSION)
            .scriptMediaType(RScripts.MEDIA_TYPE)
            .scriptRequestTypes(RestCalls.SUPPORTED_TYPES)
            .renderer(RScripts::renderRest)
            .build();
}
