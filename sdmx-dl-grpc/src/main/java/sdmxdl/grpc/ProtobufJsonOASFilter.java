package sdmxdl.grpc;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@OpenApiFilter(OpenApiFilter.RunStage.BOTH)
public final class ProtobufJsonOASFilter implements OASFilter {

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        Components components = openAPI.getComponents();
        if (components == null) return;

        List<String> builders = getBuilders(components);
        removeBuildersFromProperties(builders, components);
        removeBuildersFromComponents(builders, components);

        removePrivateFields(components);
        removeKeywords(components);
        removeValueSuffix(components);
        removeBytesSuffix(components);
        refactorLists(components);
    }

    private void refactorLists(Components components) {
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> {
                    Map<String, Schema> properties = schema.getProperties();
                    properties
                            .keySet()
                            .stream()
                            .map(property -> removeSuffix(property, "List"))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList().forEach(property -> {
                                schema.addProperty(property, properties.get(property + "List"));
                                schema.removeProperty(property + "MemoizedSerializedSize");
                                schema.removeProperty(property + "Count");
                                schema.removeProperty(property + "List");
                            });
                });
    }

    private void removeValueSuffix(Components components) {
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> {
                    Map<String, Schema> properties = schema.getProperties();
                    properties
                            .keySet()
                            .stream()
                            .filter(property -> contains(property, "Value", properties))
                            .toList()
                            .forEach(schema::removeProperty);
                });
    }

    private void removeBytesSuffix(Components components) {
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> {
                    Map<String, Schema> properties = schema.getProperties();
                    properties
                            .keySet()
                            .stream()
                            .filter(property -> contains(property, "Bytes", properties))
                            .toList()
                            .forEach(schema::removeProperty);
                });
    }

    private static boolean contains(String property, String suffix, Map<String, Schema> properties) {
        return removeSuffix(property, suffix).filter(properties::containsKey).isPresent();
    }

    private static Optional<String> removeSuffix(String text, String suffix) {
        return (text.length() > suffix.length() && text.endsWith(suffix))
                ? Optional.of(text.substring(0, text.length() - suffix.length()))
                : Optional.empty();
    }

    private void removeKeywords(Components components) {
        Set<String> keywords = Set.of("defaultInstanceForType", "parserForType", "serializedSize", "initialized", "memoizedIsInitialized");
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> keywords.forEach(schema::removeProperty));
    }

    private void removePrivateFields(Components components) {
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> {
                    schema.getProperties()
                            .keySet()
                            .stream()
                            .filter(property -> property.endsWith("_"))
                            .toList()
                            .forEach(schema::removeProperty);
                });
    }

    private List<String> getBuilders(Components components) {
        return components.getSchemas().keySet().stream().filter(key -> key.endsWith("OrBuilder")).toList();
    }

    private static void removeBuildersFromProperties(List<String> builders, Components components) {
        components.getSchemas()
                .values()
                .stream()
                .filter(ProtobufJsonOASFilter::hasProperties)
                .forEach(schema -> {
                    var properties = schema.getProperties().entrySet();
                    builders.stream()
                            .map(ProtobufJsonOASFilter::toSchemaRef)
                            .flatMap(ref -> properties.stream().filter(property -> getRef(property.getValue()).filter(ref::equals).isPresent()).map(Map.Entry::getKey))
                            .toList()
                            .forEach(schema::removeProperty);
                });
    }

    private static Optional<String> getRef(Schema schema) {
        return Schema.SchemaType.ARRAY.equals(schema.getType()) && schema.getItems() != null
                ? Optional.ofNullable(schema.getItems().getRef())
                : Optional.ofNullable(schema.getRef());
    }

    private static boolean hasProperties(Schema schema) {
        return schema.getProperties() != null;
    }

    private static String toSchemaRef(String name) {
        return "#/components/schemas/" + name;
    }

    private static void removeBuildersFromComponents(List<String> builders, Components components) {
        builders.forEach(components::removeSchema);
    }
}
