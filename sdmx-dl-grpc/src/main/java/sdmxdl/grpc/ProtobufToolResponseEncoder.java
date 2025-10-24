package sdmxdl.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.Content;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.ToolResponseEncoder;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

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
                List<Content> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Message message) {
                        result.add(new TextContent(printer.print(message)));
                    }
                }
                return ToolResponse.success(result);
            } else {
                return ToolResponse.error("Unsupported type: " + value.getClass().getName());
            }
        } catch (InvalidProtocolBufferException e) {
            return ToolResponse.error(e.getMessage());
        }
    }
}
