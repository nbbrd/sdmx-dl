package internal.sdmxdl.cli;

import java.util.Iterator;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptTarget;

/**
 * Completion candidates of the script target option, resolved from the available generators.
 */
public final class ScriptTargetCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return ScriptManager.ofServiceLoader().getTargets().stream()
                .map(ScriptTarget::toString)
                .sorted()
                .iterator();
    }
}
