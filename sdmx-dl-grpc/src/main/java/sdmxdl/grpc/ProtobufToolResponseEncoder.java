package sdmxdl.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.ToolResponseEncoder;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public final class ProtobufToolResponseEncoder implements ToolResponseEncoder<Object> {

    @Override
    public boolean supports(Class<?> runtimeType) {
        return Message.class.isAssignableFrom(runtimeType) || List.class.isAssignableFrom(runtimeType);
    }

    @Override
    public ToolResponse encode(Object value) {
        JsonFormat.Printer printer = JsonFormat.printer();
        try {
            if (value instanceof Message message) {
                return ToolResponse.success(new TextContent(printer.print(message)));
            } else if (value instanceof List<?> list) {
                StringBuilder result = new StringBuilder();
                result.append("[\n");
                result.append(
                        list.stream()
                                .filter(Message.class::isInstance)
                                .map(Message.class::cast)
                                .map(item -> {
                                    try {
                                        return printer.print(item);
                                    } catch (InvalidProtocolBufferException e) {
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(",\n"))
                );
                result.append("]\n");
                return ToolResponse.success(new TextContent(result.toString()));
            } else {
                return ToolResponse.error("Unsupported type: " + value.getClass().getName());
            }
        } catch (InvalidProtocolBufferException e) {
            return ToolResponse.error(e.getMessage());
        }
    }
}
