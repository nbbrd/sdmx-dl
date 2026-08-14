package internal.sdmxdl.provider.px.drivers;

import nbbrd.design.VisibleForTesting;
import sdmxdl.FlowRef;
import sdmxdl.StructureRef;
import sdmxdl.provider.URIs;

import java.util.Arrays;
import java.util.List;

@VisibleForTesting
@lombok.experimental.UtilityClass
public class PxConverter {

    // A "table path" is the location of a table relative to its database: the ordered
    // folder (level) ids followed by the table id, joined by '/'. It is stored URL-encoded
    // inside the flow/structure ref id so that it survives as a single opaque token.

    public static FlowRef tablePathToFlowRef(String tablePath) {
        return FlowRef.of(null, URIs.encode(tablePath), null);
    }

    public static String flowRefToTablePath(FlowRef ref) {
        return URIs.decode(ref.getId());
    }

    public static StructureRef tablePathToStructureRef(String tablePath) {
        return StructureRef.of(null, URIs.encode(tablePath), null);
    }

    public static String structureRefToTablePath(StructureRef ref) {
        return URIs.decode(ref.getId());
    }

    public static List<String> tablePathToSegments(String tablePath) {
        return Arrays.asList(tablePath.split("/", -1));
    }

    public static String segmentsToTablePath(List<String> segments) {
        return String.join("/", segments);
    }
}
