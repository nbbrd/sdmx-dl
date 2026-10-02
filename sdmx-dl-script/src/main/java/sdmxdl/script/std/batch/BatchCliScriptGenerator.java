package sdmxdl.script.std.batch;

import internal.sdmxdl.script.BatchScripts;
import internal.sdmxdl.script.CliCalls;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates Windows batch files that call the command-line tool.
 */
@DirectImpl
@ServiceProvider
public final class BatchCliScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of(ScriptTarget.BATCH_LANGUAGE, ScriptTarget.CLI_TRANSPORT))
            .scriptFileExtension(BatchScripts.FILE_EXTENSION)
            .scriptMediaType(BatchScripts.MEDIA_TYPE)
            .scriptRequestTypes(CliCalls.SUPPORTED_TYPES)
            .renderer(BatchScripts::renderCli)
            .build();
}
