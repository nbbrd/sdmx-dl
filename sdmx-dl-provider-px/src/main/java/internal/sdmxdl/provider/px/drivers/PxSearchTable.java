package internal.sdmxdl.provider.px.drivers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.TextParser;
import sdmxdl.Flow;

import java.lang.reflect.Type;

@VisibleForTesting
@lombok.Value
public class PxSearchTable {

    String id;
    String title;

    public Flow toFlow() {
        // The flat search identifies tables by id only; its "path" field is decorative and
        // inconsistent across servers. Tables listed this way are addressed directly by id
        // (single-segment table path).
        return Flow
                .builder()
                .ref(PxConverter.tablePathToFlowRef(id))
                .structureRef(PxConverter.tablePathToStructureRef(id))
                .name(title)
                .build();
    }

    public static final TextParser<PxSearchTable[]> JSON_PARSER = GsonIO.GsonParser
            .builder(PxSearchTable[].class)
            .deserializer(PxSearchTable.class, PxSearchTable::deserialize)
            .build();

    @MightBeGenerated
    private static PxSearchTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new PxSearchTable(
                GsonUtil.getAsString(obj, "id"),
                GsonUtil.getAsString(obj, "title")
        );
    }
}
