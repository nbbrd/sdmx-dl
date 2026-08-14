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
public class PxNode {

    static final String LEVEL_TYPE = "l";
    static final String TABLE_TYPE = "t";

    String id;
    String type;
    String text;

    public boolean isLevel() {
        return LEVEL_TYPE.equals(type);
    }

    public boolean isTable() {
        return TABLE_TYPE.equals(type);
    }

    public Flow toFlow(String tablePath) {
        return Flow
                .builder()
                .ref(PxConverter.tablePathToFlowRef(tablePath))
                .structureRef(PxConverter.tablePathToStructureRef(tablePath))
                .name(text)
                .build();
    }

    public static final TextParser<PxNode[]> JSON_PARSER = GsonIO.GsonParser
            .builder(PxNode[].class)
            .deserializer(PxNode.class, PxNode::deserialize)
            .build();

    @MightBeGenerated
    private static PxNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new PxNode(
                GsonUtil.getAsString(obj, "id"),
                GsonUtil.getAsString(obj, "type"),
                GsonUtil.getAsString(obj, "text")
        );
    }
}
