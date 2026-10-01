package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.script.ScriptTarget;

public final class ScriptTargetConverter implements CommandLine.ITypeConverter<ScriptTarget> {

    @Override
    public ScriptTarget convert(String string) {
        try {
            return ScriptTarget.parse(string);
        } catch (IllegalArgumentException ex) {
            throw new CommandLine.TypeConversionException(
                    "Invalid script target '" + string + "', expecting <language>/<transport>");
        }
    }
}
