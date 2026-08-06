package internal.sdmxdl.provider.px.drivers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.TextFormatter;
import sdmxdl.Dimension;
import sdmxdl.Key;
import sdmxdl.Structure;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@VisibleForTesting
@lombok.Value
public class PxTableQuery {

    @lombok.Singular
    Map<String, Collection<String>> itemFilters;

    static PxTableQuery fromDataStructureAndKey(Structure dsd, Key key) {
        return new PxTableQuery(CollectionUtil.indexedStreamOf(dsd.getDimensions())
                .collect(Collectors.toMap(
                        dimension -> dimension.getElement().getId(),
                        dimension -> fromDimensionAndKey(dimension, key))
                ));
    }

    static Collection<String> fromDimensionAndKey(CollectionUtil.IndexedElement<Dimension> dimension, Key key) {
        return Key.ALL.equals(key) || key.isWildcard(dimension.getIndex())
                ? dimension.getElement().getCodes().keySet()
                : Arrays.asList(key.get(dimension.getIndex()).split("\\+", -1));
    }

    static final TextFormatter<PxTableQuery> FORMATTER = GsonIO.GsonFormatter
            .builder(PxTableQuery.class)
            .serializer(PxTableQuery.class, PxTableQuery::serialize)
            .build();

    @MightBeGenerated
    private static JsonElement serialize(PxTableQuery src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        JsonArray query = new JsonArray();
        src.getItemFilters().forEach((code, items) -> {
            JsonObject item = new JsonObject();
            item.addProperty("code", code);
            JsonObject selection = new JsonObject();
            selection.addProperty("filter", "item");
            JsonArray values = new JsonArray();
            items.forEach(values::add);
            selection.add("values", values);
            item.add("selection", selection);
            query.add(item);
        });
        result.add("query", query);

        JsonObject response = new JsonObject();
        response.addProperty("format", "sdmx");
        result.add("response", response);

        return result;
    }
}
