package sdmxdl.script.std.jbang;

import internal.sdmxdl.script.ApiCalls;
import internal.sdmxdl.script.JBangScripts;
import internal.sdmxdl.script.ScriptGeneratorSupport;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generates single-file Java programs, run by jbang, that call the Java library.
 */
@DirectImpl
@ServiceProvider
public final class JBangJavaScriptGenerator implements ScriptGenerator {

    @lombok.experimental.Delegate(types = ScriptGenerator.class)
    private final ScriptGeneratorSupport generator = ScriptGeneratorSupport.builder()
            .scriptTarget(ScriptTarget.of("jbang", ScriptTarget.JAVA_TRANSPORT))
            .scriptFileExtension(JBangScripts.FILE_EXTENSION)
            .scriptRequestTypes(ApiCalls.SUPPORTED_TYPES)
            .renderer(JBangScripts::renderJava)
            .build();
}
