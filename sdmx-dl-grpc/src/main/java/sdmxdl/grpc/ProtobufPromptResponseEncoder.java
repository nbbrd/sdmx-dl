package sdmxdl.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.PromptResponse;
import io.quarkiverse.mcp.server.PromptResponseEncoder;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public final class ProtobufPromptResponseEncoder implements PromptResponseEncoder<Object> {

    @Override
    public boolean supports(Class<?> runtimeType) {
        return Message.class.isAssignableFrom(runtimeType) || List.class.isAssignableFrom(runtimeType);
    }

    @Override
    public PromptResponse encode(Object value) {
        JsonFormat.Printer printer = JsonFormat.printer();
        try {
            if (value instanceof Message message) {
                return PromptResponse.withMessages(PromptMessage.withUserRole(printer.print(message)));
            } else if (value instanceof List<?> list) {
                List<PromptMessage> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Message message) {
                        result.add(PromptMessage.withUserRole(printer.print(message)));
                    }
                }
                return PromptResponse.withMessages(result);
            } else {
                return PromptResponse.withMessages(PromptMessage.withUserRole("Unsupported type: " + value.getClass().getName()));
            }
        } catch (InvalidProtocolBufferException e) {
            return PromptResponse.withMessages(PromptMessage.withUserRole(e.getMessage()));
        }
    }
}
