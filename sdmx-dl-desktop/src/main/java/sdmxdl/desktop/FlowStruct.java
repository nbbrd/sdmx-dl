package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.Flow;
import sdmxdl.Structure;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@lombok.Value
public class FlowStruct {

    @NonNull Flow flow;

    @NonNull Structure structure;

    public static FlowStruct load(SdmxWebManager manager, Languages languages, DataSourceRef ref) throws IOException {
        try (Connection conn = manager.getConnection(ref.getSource(), languages)) {
            return new FlowStruct(conn.getFlow(ref.getFlow()), conn.getStructure(ref.getFlow()));
        }
    }
}
